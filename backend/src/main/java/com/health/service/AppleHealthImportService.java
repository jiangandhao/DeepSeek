package com.health.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.health.common.BizException;
import com.health.entity.DietRecord;
import com.health.entity.ExerciseRecord;
import com.health.entity.GlucoseRecord;
import com.health.mapper.DietRecordMapper;
import com.health.mapper.ExerciseRecordMapper;
import com.health.mapper.GlucoseRecordMapper;

import lombok.RequiredArgsConstructor;

/**
 * 解析 Apple「健康」导出文件(export.zip / export.xml),将血糖、运动、饮食数据
 * 映射进系统现有的记录表。采用 SAX 流式解析,避免大文件(CGM 用户可达数十万条)OOM。
 */
@Service
@RequiredArgsConstructor
public class AppleHealthImportService {

    /** Apple 时间格式如 2024-05-01 08:30:00 +0800 */
    private static final DateTimeFormatter APPLE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
    /** mg/dL → mmol/L 换算系数 */
    private static final BigDecimal MGDL_TO_MMOL = new BigDecimal("18.0182");
    /** 单类型最多导入条数,防止 CGM 等高频数据撑爆数据库 */
    private static final int MAX_GLUCOSE = 2000;
    private static final int MAX_EXERCISE = 1000;
    private static final int MAX_DIET = 1000;

    private final GlucoseRecordMapper glucoseMapper;
    private final ExerciseRecordMapper exerciseMapper;
    private final DietRecordMapper dietMapper;

    public Map<String, Object> importApple(Long userId, MultipartFile file, int days) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请上传 Apple 健康导出文件(export.zip 或 export.xml)");
        }
        LocalDateTime cutoff = days > 0 ? LocalDateTime.now().minusDays(days) : null;
        Handler handler = new Handler(userId, cutoff);
        try (InputStream raw = file.getInputStream();
             InputStream xml = openXml(file.getOriginalFilename(), raw)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // 允许 Apple 导出内置的 DOCTYPE,但禁用外部实体,防止 XXE
            setFeatureQuietly(factory, "http://xml.org/sax/features/external-general-entities", false);
            setFeatureQuietly(factory, "http://xml.org/sax/features/external-parameter-entities", false);
            setFeatureQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            SAXParser parser = factory.newSAXParser();
            parser.parse(xml, handler);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("解析 Apple 健康数据失败:" + e.getMessage());
        }

        handler.glucose.forEach(glucoseMapper::insert);
        handler.exercise.forEach(exerciseMapper::insert);
        handler.diet.forEach(dietMapper::insert);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("glucose", handler.glucose.size());
        summary.put("exercise", handler.exercise.size());
        summary.put("diet", handler.diet.size());
        summary.put("total", handler.glucose.size() + handler.exercise.size() + handler.diet.size());
        summary.put("days", days);
        return summary;
    }

    /** 设置解析器特性,个别解析器实现不识别时忽略,不影响导入。 */
    private void setFeatureQuietly(SAXParserFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (Exception ignored) {
            // 不同 SAX 实现可能不支持该特性,降级处理
        }
    }

    /** zip 包则取出其中的 export.xml(排除 export_cda.xml),否则按 xml 直接解析。 */
    private InputStream openXml(String filename, InputStream raw) throws Exception {
        String name = filename == null ? "" : filename.toLowerCase();
        if (!name.endsWith(".zip")) {
            return raw;
        }
        ZipInputStream zip = new ZipInputStream(raw);
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            String entryName = entry.getName().toLowerCase();
            if (entryName.endsWith("export.xml") && !entryName.contains("cda")) {
                return zip; // 流定位到该条目,交给 SAX 读取
            }
        }
        throw new BizException("压缩包内未找到 export.xml,请确认是 Apple 健康导出文件");
    }

    /** SAX 处理器:边解析边筛选,结果收集进受容量上限保护的列表。 */
    private static class Handler extends DefaultHandler {
        private final Long userId;
        private final LocalDateTime cutoff;
        final List<GlucoseRecord> glucose = new ArrayList<>();
        final List<ExerciseRecord> exercise = new ArrayList<>();
        final List<DietRecord> diet = new ArrayList<>();
        /** 血糖按「小时」去重,避免 CGM 高频数据 */
        private final Set<Long> glucoseHours = new HashSet<>();

        Handler(Long userId, LocalDateTime cutoff) {
            this.userId = userId;
            this.cutoff = cutoff;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if ("Record".equals(qName)) {
                String type = attrs.getValue("type");
                if (type == null) return;
                if (type.endsWith("BloodGlucose")) handleGlucose(attrs);
                else if (type.endsWith("DietaryEnergyConsumed")) handleDiet(attrs);
            } else if ("Workout".equals(qName)) {
                handleWorkout(attrs);
            }
        }

        private void handleGlucose(Attributes attrs) {
            if (glucose.size() >= MAX_GLUCOSE) return;
            LocalDateTime t = parseTime(attrs);
            if (t == null || isBeforeCutoff(t)) return;
            long hourKey = t.withMinute(0).withSecond(0).withNano(0).toEpochSecond(java.time.ZoneOffset.UTC) / 3600;
            if (!glucoseHours.add(hourKey)) return; // 同一小时只保留一条
            BigDecimal value = parseDecimal(attrs.getValue("value"));
            if (value == null) return;
            String unit = attrs.getValue("unit");
            BigDecimal mmol = (unit != null && unit.toLowerCase().contains("mmol"))
                    ? value
                    : value.divide(MGDL_TO_MMOL, 1, RoundingMode.HALF_UP);
            mmol = mmol.setScale(1, RoundingMode.HALF_UP);
            if (mmol.doubleValue() <= 0 || mmol.doubleValue() > 40) return; // 异常值剔除
            GlucoseRecord r = new GlucoseRecord();
            r.setUserId(userId);
            r.setValueMmol(mmol);
            r.setPeriod(glucosePeriod(t.getHour()));
            r.setMeasuredAt(t);
            r.setNote("来自 Apple 健康导入");
            glucose.add(r);
        }

        private void handleWorkout(Attributes attrs) {
            if (exercise.size() >= MAX_EXERCISE) return;
            LocalDateTime t = parseTime(attrs);
            if (t == null || isBeforeCutoff(t)) return;
            BigDecimal duration = parseDecimal(attrs.getValue("duration"));
            int minutes = duration == null ? 0 : duration.setScale(0, RoundingMode.HALF_UP).intValue();
            String durationUnit = attrs.getValue("durationUnit");
            if (durationUnit != null && durationUnit.toLowerCase().contains("hour")) minutes *= 60;
            if (minutes <= 0) minutes = 1;
            ExerciseRecord r = new ExerciseRecord();
            r.setUserId(userId);
            r.setType(workoutName(attrs.getValue("workoutActivityType")));
            r.setDurationMin(minutes);
            r.setIntensity("MEDIUM");
            BigDecimal energy = parseDecimal(attrs.getValue("totalEnergyBurned"));
            r.setCalories(energy == null ? null : energy.setScale(0, RoundingMode.HALF_UP).intValue());
            r.setDoneAt(t);
            exercise.add(r);
        }

        private void handleDiet(Attributes attrs) {
            if (diet.size() >= MAX_DIET) return;
            LocalDateTime t = parseTime(attrs);
            if (t == null || isBeforeCutoff(t)) return;
            BigDecimal value = parseDecimal(attrs.getValue("value"));
            if (value == null) return;
            DietRecord r = new DietRecord();
            r.setUserId(userId);
            r.setMealType(mealType(t.getHour()));
            r.setFood("Apple 健康饮食记录");
            r.setCalories(value.setScale(0, RoundingMode.HALF_UP).intValue());
            r.setCarbsG(BigDecimal.ZERO);
            r.setEatenAt(t);
            diet.add(r);
        }

        private boolean isBeforeCutoff(LocalDateTime t) {
            return cutoff != null && t.isBefore(cutoff);
        }

        private LocalDateTime parseTime(Attributes attrs) {
            String s = attrs.getValue("startDate");
            if (s == null) s = attrs.getValue("creationDate");
            if (s == null) return null;
            try {
                return OffsetDateTime.parse(s, APPLE_FMT).toLocalDateTime();
            } catch (Exception e) {
                return null;
            }
        }

        private BigDecimal parseDecimal(String s) {
            if (s == null || s.isBlank()) return null;
            try {
                return new BigDecimal(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /** 依测量时段粗略推断血糖时段(Apple 不提供餐次上下文)。 */
        private String glucosePeriod(int hour) {
            if (hour >= 5 && hour <= 9) return "FASTING";
            if (hour >= 21 || hour <= 4) return "BEDTIME";
            return "RANDOM";
        }

        private String mealType(int hour) {
            if (hour >= 5 && hour <= 10) return "BREAKFAST";
            if (hour >= 11 && hour <= 14) return "LUNCH";
            if (hour >= 17 && hour <= 21) return "DINNER";
            return "SNACK";
        }

        private String workoutName(String activity) {
            if (activity == null) return "运动";
            String key = activity.replace("HKWorkoutActivityType", "");
            switch (key) {
                case "Running": return "跑步";
                case "Walking": return "步行";
                case "Cycling": return "骑行";
                case "Swimming": return "游泳";
                case "Hiking": return "徒步";
                case "Yoga": return "瑜伽";
                case "Elliptical": return "椭圆机";
                case "Rowing": return "划船";
                case "HighIntensityIntervalTraining": return "HIIT";
                case "TraditionalStrengthTraining":
                case "FunctionalStrengthTraining": return "力量训练";
                case "CoreTraining": return "核心训练";
                case "Dance": return "舞蹈";
                default: return key.isEmpty() ? "运动" : key;
            }
        }
    }
}

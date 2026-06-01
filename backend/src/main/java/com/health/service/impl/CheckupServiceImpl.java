package com.health.service.impl;

import com.health.entity.CheckupReport;
import com.health.mapper.CheckupReportMapper;
import com.health.service.CheckupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CheckupServiceImpl implements CheckupService {

    @Autowired
    private CheckupReportMapper checkupReportMapper;

    @Override
    public List<Map<String, Object>> getPackages() {
        List<Map<String, Object>> packages = new ArrayList<>();
        packages.add(createPackage(1L, "基础体检套餐", 298, "基础检查项目"));
        packages.add(createPackage(2L, "标准体检套餐", 598, "推荐套餐"));
        packages.add(createPackage(3L, "深度体检套餐", 1280, "全面检查"));
        return packages;
    }

    @Override
    public Map<String, Object> getPackageDetail(Long id) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", id);
        detail.put("name", id == 2 ? "标准体检套餐" : "基础体检套餐");
        List<String> items = Arrays.asList("血常规", "尿常规", "肝功能", "肾功能", "血脂", "血糖", "心电图");
        detail.put("items", items);
        return detail;
    }

    @Override
    public List<Map<String, Object>> getCenters(String city, String keyword) {
        List<Map<String, Object>> centers = new ArrayList<>();
        centers.add(createCenter(1L, "美年大健康(光谷店)", "武汉市洪山区光谷大道77号", 4.5, "2.3km", 15));
        centers.add(createCenter(2L, "爱康国宾(武昌店)", "武汉市武昌区中南路99号", 4.3, "5.1km", 25));
        centers.add(createCenter(3L, "瑞慈体检(汉口店)", "武汉市江汉区解放大道688号", 4.7, "8.5km", 45));
        return centers;
    }

    @Override
    public Map<String, Object> getCenterDetail(Long id) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", id);
        detail.put("name", "美年大健康(光谷店)");
        detail.put("address", "武汉市洪山区光谷大道77号");
        detail.put("phone", "027-87654321");
        return detail;
    }

    @Override
    public List<Map<String, Object>> getTimeSlots(Long centerId, String date) {
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(createTimeSlot("08:00-08:30", true, 5));
        slots.add(createTimeSlot("08:30-09:00", true, 3));
        slots.add(createTimeSlot("09:00-09:30", false, 0));
        slots.add(createTimeSlot("09:30-10:00", true, 8));
        return slots;
    }

    @Override
    public Map<String, Object> createAppointment(Map<String, Object> appointment) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appointmentId", System.currentTimeMillis());
        result.put("message", "预约成功");
        return result;
    }

    @Override
    public List<Map<String, Object>> getAppointments(Long userId, String status) {
        List<Map<String, Object>> appointments = new ArrayList<>();
        appointments.add(createAppointment(1L, "标准体检套餐", "美年大健康", "2025-01-20", "待体检"));
        appointments.add(createAppointment(2L, "基础体检套餐", "爱康国宾", "2024-06-15", "已完成"));
        return appointments;
    }

    @Override
    public Map<String, Object> getAppointmentDetail(Long id) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", id);
        detail.put("package", "标准体检套餐");
        detail.put("center", "美年大健康");
        detail.put("date", "2025-01-20");
        detail.put("time", "09:00-09:30");
        return detail;
    }

    @Override
    public Map<String, Object> cancelAppointment(Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "预约已取消");
        return result;
    }

    @Override
    public List<CheckupReport> getReports(Long userId) {
        return checkupReportMapper.selectByUserId(userId);
    }

    @Override
    public CheckupReport getReportDetail(Long id) {
        return checkupReportMapper.selectById(id);
    }

    @Override
    public Map<String, Object> analyzeReport(Long id) {
        Map<String, Object> analysis = new HashMap<>();

        List<Map<String, String>> sections = new ArrayList<>();
        sections.add(createAnalysisSection("总体评价", "您的体检结果总体尚可，有几项指标需要引起重视。"));
        sections.add(createAnalysisSection("血糖偏高", "空腹血糖6.8mmol/L超出正常范围，建议减少精制碳水摄入。"));
        sections.add(createAnalysisSection("血脂异常", "总胆固醇和甘油三酯均偏高，建议调整饮食结构。"));

        analysis.put("sections", sections);
        return analysis;
    }

    @Override
    public Map<String, Object> analyzeImage(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", UUID.randomUUID().toString());
        result.put("status", "processing");
        return result;
    }

    @Override
    public Map<String, Object> getAnalysisResult(String taskId) {
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("status", "completed");

        List<Map<String, Object>> findings = new ArrayList<>();
        findings.add(createFinding("肺结节", "中", "右肺上叶见一磨玻璃结节，大小约6mm"));
        findings.add(createFinding("肺纹理增多", "低", "双肺纹理增多、增粗"));

        result.put("findings", findings);
        result.put("diagnosis", "1. 右肺上叶磨玻璃结节，建议随访观察");
        return result;
    }

    @Override
    public List<Map<String, String>> getAiRecommendation(Long userId) {
        List<Map<String, String>> recommendations = new ArrayList<>();
        recommendations.add(createRecommendation("胸部CT检查", "家族有肺部疾病史"));
        recommendations.add(createRecommendation("糖化血红蛋白检测", "血糖偏高"));
        recommendations.add(createRecommendation("颈动脉超声", "年龄>40岁"));
        return recommendations;
    }

    // 辅助方法
    private Map<String, Object> createPackage(Long id, String name, int price, String suitable) {
        Map<String, Object> pkg = new HashMap<>();
        pkg.put("id", id);
        pkg.put("name", name);
        pkg.put("price", price);
        pkg.put("suitable", suitable);
        return pkg;
    }

    private Map<String, Object> createCenter(Long id, String name, String address, double rating, String distance, int waitTime) {
        Map<String, Object> center = new HashMap<>();
        center.put("id", id);
        center.put("name", name);
        center.put("address", address);
        center.put("rating", rating);
        center.put("distance", distance);
        center.put("waitTime", waitTime);
        return center;
    }

    private Map<String, Object> createTimeSlot(String time, boolean available, int count) {
        Map<String, Object> slot = new HashMap<>();
        slot.put("time", time);
        slot.put("available", available);
        slot.put("count", count);
        return slot;
    }

    private Map<String, Object> createAppointment(Long id, String packageName, String center, String date, String status) {
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("id", id);
        appointment.put("package", packageName);
        appointment.put("center", center);
        appointment.put("date", date);
        appointment.put("status", status);
        return appointment;
    }

    private Map<String, String> createAnalysisSection(String title, String content) {
        Map<String, String> section = new HashMap<>();
        section.put("title", title);
        section.put("content", content);
        return section;
    }

    private Map<String, Object> createFinding(String name, String severity, String description) {
        Map<String, Object> finding = new HashMap<>();
        finding.put("name", name);
        finding.put("severity", severity);
        finding.put("description", description);
        return finding;
    }

    private Map<String, String> createRecommendation(String item, String reason) {
        Map<String, String> rec = new HashMap<>();
        rec.put("item", item);
        rec.put("reason", reason);
        return rec;
    }
}

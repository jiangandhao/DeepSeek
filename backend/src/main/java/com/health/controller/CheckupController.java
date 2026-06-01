package com.health.controller;

import com.health.common.Result;
import com.health.entity.CheckupReport;
import com.health.service.CheckupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkup")
@CrossOrigin
public class CheckupController {

    @Autowired
    private CheckupService checkupService;

    /**
     * 获取体检套餐列表
     */
    @GetMapping("/packages")
    public Result<List<Map<String, Object>>> getPackages() {
        return Result.success(checkupService.getPackages());
    }

    /**
     * 获取套餐详情
     */
    @GetMapping("/packages/{id}")
    public Result<Map<String, Object>> getPackageDetail(@PathVariable Long id) {
        return Result.success(checkupService.getPackageDetail(id));
    }

    /**
     * 获取体检机构列表
     */
    @GetMapping("/centers")
    public Result<List<Map<String, Object>>> getCenters(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        return Result.success(checkupService.getCenters(city, keyword));
    }

    /**
     * 获取机构详情
     */
    @GetMapping("/centers/{id}")
    public Result<Map<String, Object>> getCenterDetail(@PathVariable Long id) {
        return Result.success(checkupService.getCenterDetail(id));
    }

    /**
     * 获取可用时间段
     */
    @GetMapping("/timeslots")
    public Result<List<Map<String, Object>>> getTimeSlots(
            @RequestParam Long centerId,
            @RequestParam String date) {
        return Result.success(checkupService.getTimeSlots(centerId, date));
    }

    /**
     * 创建预约
     */
    @PostMapping("/appointments")
    public Result<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> appointment) {
        return Result.success(checkupService.createAppointment(appointment));
    }

    /**
     * 获取预约列表
     */
    @GetMapping("/appointments")
    public Result<List<Map<String, Object>>> getAppointments(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        return Result.success(checkupService.getAppointments(userId, status));
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/appointments/{id}")
    public Result<Map<String, Object>> getAppointmentDetail(@PathVariable Long id) {
        return Result.success(checkupService.getAppointmentDetail(id));
    }

    /**
     * 取消预约
     */
    @PutMapping("/appointments/{id}/cancel")
    public Result<Map<String, Object>> cancelAppointment(@PathVariable Long id) {
        return Result.success(checkupService.cancelAppointment(id));
    }

    /**
     * 获取体检报告列表
     */
    @GetMapping("/reports")
    public Result<List<CheckupReport>> getReports(@RequestParam Long userId) {
        return Result.success(checkupService.getReports(userId));
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/reports/{id}")
    public Result<CheckupReport> getReportDetail(@PathVariable Long id) {
        return Result.success(checkupService.getReportDetail(id));
    }

    /**
     * AI解读报告
     */
    @PostMapping("/reports/{id}/analyze")
    public Result<Map<String, Object>> analyzeReport(@PathVariable Long id) {
        return Result.success(checkupService.analyzeReport(id));
    }

    /**
     * 影像分析
     */
    @PostMapping("/image/analyze")
    public Result<Map<String, Object>> analyzeImage(@RequestParam("file") MultipartFile file) {
        return Result.success(checkupService.analyzeImage(file));
    }

    /**
     * 获取影像分析结果
     */
    @GetMapping("/image/result/{taskId}")
    public Result<Map<String, Object>> getAnalysisResult(@PathVariable String taskId) {
        return Result.success(checkupService.getAnalysisResult(taskId));
    }

    /**
     * 获取AI智能推荐
     */
    @GetMapping("/recommendation")
    public Result<List<Map<String, String>>> getAiRecommendation(@RequestParam Long userId) {
        return Result.success(checkupService.getAiRecommendation(userId));
    }
}

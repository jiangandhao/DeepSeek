package com.health.service;

import com.health.entity.CheckupReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CheckupService {

    /**
     * 获取体检套餐列表
     */
    List<Map<String, Object>> getPackages();

    /**
     * 获取套餐详情
     */
    Map<String, Object> getPackageDetail(Long id);

    /**
     * 获取体检机构列表
     */
    List<Map<String, Object>> getCenters(String city, String keyword);

    /**
     * 获取机构详情
     */
    Map<String, Object> getCenterDetail(Long id);

    /**
     * 获取可用时间段
     */
    List<Map<String, Object>> getTimeSlots(Long centerId, String date);

    /**
     * 创建预约
     */
    Map<String, Object> createAppointment(Map<String, Object> appointment);

    /**
     * 获取预约列表
     */
    List<Map<String, Object>> getAppointments(Long userId, String status);

    /**
     * 获取预约详情
     */
    Map<String, Object> getAppointmentDetail(Long id);

    /**
     * 取消预约
     */
    Map<String, Object> cancelAppointment(Long id);

    /**
     * 获取体检报告列表
     */
    List<CheckupReport> getReports(Long userId);

    /**
     * 获取报告详情
     */
    CheckupReport getReportDetail(Long id);

    /**
     * AI解读报告
     */
    Map<String, Object> analyzeReport(Long id);

    /**
     * 影像分析
     */
    Map<String, Object> analyzeImage(MultipartFile file);

    /**
     * 获取影像分析结果
     */
    Map<String, Object> getAnalysisResult(String taskId);

    /**
     * 获取AI智能推荐
     */
    List<Map<String, String>> getAiRecommendation(Long userId);
}

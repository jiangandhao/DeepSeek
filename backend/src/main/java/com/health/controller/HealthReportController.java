package com.health.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.health.common.BizException;
import com.health.common.Result;
import com.health.entity.HealthReport;
import com.health.security.UserContext;
import com.health.service.HealthReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "体检报告")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class HealthReportController {
    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private final HealthReportService service;

    @Operation(summary = "上传并解析体检报告图片、PDF或医学影像")
    @PostMapping("/analyze")
    public Result<HealthReport> analyze(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new BizException("请上传报告文件");
        if (file.getSize() > MAX_SIZE) throw new BizException("文件不能超过 10MB");
        try {
            return Result.ok(service.analyze(UserContext.currentUserId(), file.getBytes(),
                    file.getOriginalFilename(), file.getContentType()));
        } catch (IOException e) {
            throw new BizException("报告文件读取失败");
        }
    }

    @Operation(summary = "体检报告历史")
    @GetMapping
    public Result<List<HealthReport>> list() {
        return Result.ok(service.list(UserContext.currentUserId()));
    }

    @Operation(summary = "体检报告结构化详情")
    @GetMapping("/{id}")
    public Result<HealthReport> get(@PathVariable Long id) {
        return Result.ok(service.get(UserContext.currentUserId(), id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(UserContext.currentUserId(), id);
        return Result.ok();
    }
}

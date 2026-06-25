package com.health.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.health.common.Result;
import com.health.security.UserContext;
import com.health.service.AppleHealthImportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "健康数据导入")
@RestController
@RequestMapping("/api/health/import")
@RequiredArgsConstructor
public class HealthImportController {

    private final AppleHealthImportService appleHealthImportService;

    @Operation(summary = "导入 Apple 健康导出文件(export.zip / export.xml)")
    @PostMapping("/apple")
    public Result<Map<String, Object>> importApple(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "days", defaultValue = "90") int days) {
        Long userId = UserContext.currentUserId();
        return Result.ok(appleHealthImportService.importApple(userId, file, days));
    }
}

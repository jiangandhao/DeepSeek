package com.health.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.health.common.BizException;
import com.health.common.Result;
import com.health.security.UserContext;
import com.health.service.AiClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "医学影像识别（兼容接口）")
@RestController
@RequestMapping("/api/imaging")
@RequiredArgsConstructor
public class ImagingController {

    private final AiClient aiClient;

    @Operation(summary = "肺 CT 结节候选检测；其他体检报告请使用 /api/reports/analyze")
    @PostMapping("/detect")
    public Result<Map<String, Object>> detect(@RequestParam("file") MultipartFile file) {
        UserContext.currentUserId(); // 鉴权
        if (file.isEmpty()) {
            throw new BizException("请上传图像文件");
        }
        try {
            return Result.ok(aiClient.detectImage(file.getBytes(), file.getOriginalFilename()));
        } catch (IOException e) {
            throw new BizException("图像读取失败");
        }
    }
}

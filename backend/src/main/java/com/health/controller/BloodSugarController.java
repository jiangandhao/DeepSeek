package com.health.controller;

import com.health.common.Result;
import com.health.entity.BloodSugarRecord;
import com.health.service.BloodSugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blood-sugar")
public class BloodSugarController {

    @Autowired
    private BloodSugarService bloodSugarService;

    @GetMapping("/records")
    public Result<List<BloodSugarRecord>> getRecords(@RequestParam Long userId) {
        return Result.success(bloodSugarService.getRecords(userId));
    }

    @PostMapping("/records")
    public Result<Void> addRecord(@RequestBody BloodSugarRecord record) {
        bloodSugarService.addRecord(record);
        return Result.success();
    }

    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(@RequestParam Long userId, @RequestParam String period) {
        return Result.success(bloodSugarService.getTrend(userId, period));
    }

    @GetMapping("/prediction")
    public Result<Map<String, Object>> getPrediction(@RequestParam Long userId) {
        return Result.success(bloodSugarService.getPrediction(userId));
    }

    @PostMapping("/diet/recommendation")
    public Result<List<Map<String, Object>>> getDietRecommendation(@RequestBody Map<String, Double> params) {
        return Result.success(bloodSugarService.getDietRecommendation(params.get("bloodSugar")));
    }

    @PostMapping("/exercise/recommendation")
    public Result<List<Map<String, Object>>> getExerciseRecommendation(@RequestBody Map<String, Double> params) {
        return Result.success(bloodSugarService.getExerciseRecommendation(params.get("bloodSugar")));
    }
}

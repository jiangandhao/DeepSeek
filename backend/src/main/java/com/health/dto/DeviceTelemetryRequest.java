package com.health.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceTelemetryRequest {
    @NotNull(message = "血糖值不能为空")
    private BigDecimal valueMmol;

    private String period;
    private Integer batteryLevel;
    private Integer signalStrength;
    private LocalDateTime measuredAt;
}

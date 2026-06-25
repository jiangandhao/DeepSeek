package com.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceBindRequest {
    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    private String deviceName;
    private String deviceType;
}

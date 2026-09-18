package com.shortlinkgeneration.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AccessStatisticsDTO {
    private Long totalPv;       // 总点击量
    private Long totalUv;       // 独立访客数（按IP）
    private List<Map<String, Object>> timeDistribution;   // 按天时间分布
    private List<Map<String, Object>> sourceDistribution; // 来源分布
    private List<Map<String, Object>> deviceDistribution; // 设备分布
}

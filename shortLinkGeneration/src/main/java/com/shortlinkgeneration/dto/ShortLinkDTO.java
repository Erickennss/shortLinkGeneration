package com.shortlinkgeneration.dto;

import lombok.Data;

@Data
public class ShortLinkDTO {
    /**
     * 原始长链接
     */
    private String originalUrl;
    /**
     * 过期小时数
     */
    private Integer expireHours;
}

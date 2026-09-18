package com.shortlinkgeneration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("short_url_access_log")
public class AccessLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String shortCode;
    private LocalDateTime accessTime;
    private String ip;
    private String userAgent;
    private String referer;
    private String device;
    private String browser;
}

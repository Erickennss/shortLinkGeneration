package com.shortlinkgeneration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("short_url")
public class ShortUrl {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始长链接
     */
    private String longUrl;

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 访问PV
     */
    private Integer pv;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}

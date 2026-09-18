CREATE TABLE `short_url` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `long_url` varchar(512) NOT NULL COMMENT '原始长链接',
                             `short_code` varchar(16) NOT NULL COMMENT '短链码',
                             `pv` int NOT NULL DEFAULT '0' COMMENT '访问量',
                             `expire_time` datetime NOT NULL COMMENT '过期时间',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_short_code` (`short_code`),
                             KEY `idx_long_url` (`long_url`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链表';



CREATE TABLE short_url_access_log (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志主键',
                                      short_code VARCHAR(32) NOT NULL COMMENT '关联短链码',
                                      access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
                                      ip VARCHAR(64) DEFAULT '' COMMENT '访客IP',
                                      user_agent TEXT COMMENT '客户端UA字符串',
                                      referer VARCHAR(1024) DEFAULT '' COMMENT '来源页面地址',
                                      device VARCHAR(32) DEFAULT '' COMMENT '设备类型：PC/Mobile/Tablet',
                                      browser VARCHAR(64) DEFAULT '' COMMENT '浏览器名称',
    -- 复合索引：按短码+时间范围查询统计时，性能提升显著
                                      INDEX idx_short_code_time (short_code, access_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链访问日志表';


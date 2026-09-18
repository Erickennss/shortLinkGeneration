package com.shortlinkgeneration.task;

import com.shortlinkgeneration.service.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiredLinkCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(ExpiredLinkCleanupTask.class);

    @Autowired
    private ShortUrlService shortUrlService;

    // 每天凌晨 3 点自动清理过期短链
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        int deleted = shortUrlService.deleteExpiredLinks();
        if (deleted > 0) {
            log.info("定时清理过期短链完成，共删除 {} 条", deleted);
        }
    }
}

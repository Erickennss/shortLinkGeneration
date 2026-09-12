package com.shortlinkgeneration.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlinkgeneration.entity.ShortUrl;
import com.shortlinkgeneration.mapper.ShortUrlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PvSyncTask {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ShortUrlMapper shortUrlMapper;

    // 每10分钟同步一次
    @Scheduled(fixedRate = 600000)
    public void syncPv() {
        Set<String> keys = redisTemplate.keys("short:pv:*");
        if (keys == null || keys.isEmpty()) return;

        keys.forEach(key -> {
            String code = key.replace("short:pv:", "");
            String pvStr = redisTemplate.opsForValue().get(key);
            if (pvStr != null) {
                int pv = Integer.parseInt(pvStr);
                ShortUrl url = new ShortUrl();
                url.setPv(pv);
                shortUrlMapper.update(url, new LambdaQueryWrapper<ShortUrl>()
                        .eq(ShortUrl::getShortCode, code));
            }
        });
    }
}

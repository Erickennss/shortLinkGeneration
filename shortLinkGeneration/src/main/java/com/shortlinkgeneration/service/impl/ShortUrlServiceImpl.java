package com.shortlinkgeneration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.util.concurrent.RateLimiter;
import com.shortlinkgeneration.dto.ShortLinkDTO;
import com.shortlinkgeneration.entity.ShortUrl;
import com.shortlinkgeneration.mapper.ShortUrlMapper;
import com.shortlinkgeneration.service.ShortUrlService;
import com.shortlinkgeneration.utils.HashUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ShortUrlServiceImpl extends ServiceImpl<ShortUrlMapper, ShortUrl> implements ShortUrlService {

    @Autowired
    private ShortUrlMapper shortUrlMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${short.url.domain}")
    private String domain;
    @Value("${short.url.expire-days}")
    private int expireDays;
    @Value("${short.url.code-length}")
    private int codeLength;

    private BloomFilter<String> bloomFilter;
    private final RateLimiter rateLimiter = RateLimiter.create(100);
    private static final String SHORT_URL_KEY = "short:url:";
    private static final String PV_KEY = "short:pv:";

    @PostConstruct
    public void initBloomFilter() {
        // 查询 `short_url` 表全部数据
        List<ShortUrl> list = shortUrlMapper.selectList(null);
        // 预估最多存 100 万个短码，误判率 0.01%
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 1000000, 0.0001);
        list.forEach(url -> bloomFilter.put(url.getShortCode()));
    }

    @Override
    public String generateShortUrl(String longUrl) {
        if (!rateLimiter.tryAcquire()) {
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }

        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrl::getLongUrl, longUrl)
                .gt(ShortUrl::getExpireTime, LocalDateTime.now());
        ShortUrl exist = shortUrlMapper.selectOne(wrapper);
        if (exist != null) {
            return domain + exist.getShortCode();
        }

        String shortCode;
        String salt = "";
        int retryCount = 0;
        do {
            shortCode = HashUtils.generateShortCode(longUrl + salt, codeLength);
            salt = String.valueOf(System.currentTimeMillis());
            retryCount++;
        } while (bloomFilter.mightContain(shortCode) && retryCount < 5);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setLongUrl(longUrl);
        shortUrl.setShortCode(shortCode);
        shortUrl.setPv(0);
        shortUrl.setExpireTime(LocalDateTime.now().plusDays(expireDays));
        shortUrlMapper.insert(shortUrl);

        bloomFilter.put(shortCode);
        redisTemplate.opsForValue().set(SHORT_URL_KEY + shortCode, longUrl, expireDays, TimeUnit.DAYS);

        return domain + shortCode;
    }

    @Override
    public String getLongUrl(String shortCode) {
        if (!bloomFilter.mightContain(shortCode)) {
            return null;
        }

        String key = SHORT_URL_KEY + shortCode;
        String longUrl = redisTemplate.opsForValue().get(key);
        if (longUrl != null) {
            redisTemplate.opsForValue().increment(PV_KEY + shortCode);
            return longUrl;
        }

        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrl::getShortCode, shortCode)
                .gt(ShortUrl::getExpireTime, LocalDateTime.now());
        ShortUrl shortUrl = shortUrlMapper.selectOne(wrapper);
        if (shortUrl == null) {
            return null;
        }

        redisTemplate.opsForValue().set(key, shortUrl.getLongUrl(), 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().increment(PV_KEY + shortCode);
        return shortUrl.getLongUrl();
    }

    @Override
    public ShortUrl generateByDto(ShortLinkDTO dto) {
        if (!rateLimiter.tryAcquire()) {
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }
        String longUrl = dto.getOriginalUrl();

        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrl::getLongUrl, longUrl)
                .gt(ShortUrl::getExpireTime, LocalDateTime.now());
        ShortUrl exist = shortUrlMapper.selectOne(wrapper);
        if(exist != null){
            return exist;
        }

        String shortCode;
        String salt = "";
        int retryCount = 0;
        do {
            shortCode = HashUtils.generateShortCode(longUrl + salt, codeLength);
            salt = String.valueOf(System.currentTimeMillis());
            retryCount++;
        } while (bloomFilter.mightContain(shortCode) && retryCount < 5);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setLongUrl(longUrl);
        shortUrl.setShortCode(shortCode);
        shortUrl.setPv(0);
        shortUrl.setCreateTime(LocalDateTime.now());

        Integer expireHours = dto.getExpireHours();
        if(expireHours == null || expireHours <= 0){
            expireHours = 24;
        }
        shortUrl.setExpireTime(LocalDateTime.now().plusHours(expireHours));

        shortUrlMapper.insert(shortUrl);

        bloomFilter.put(shortCode);
        redisTemplate.opsForValue().set(SHORT_URL_KEY + shortCode, longUrl, expireHours, TimeUnit.HOURS);

        return shortUrl;
    }
}

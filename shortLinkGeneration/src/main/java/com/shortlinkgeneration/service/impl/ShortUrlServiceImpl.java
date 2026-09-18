package com.shortlinkgeneration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.util.concurrent.RateLimiter;
import com.shortlinkgeneration.dto.AccessStatisticsDTO;
import com.shortlinkgeneration.dto.ShortLinkDTO;
import com.shortlinkgeneration.entity.AccessLog;
import com.shortlinkgeneration.entity.ShortUrl;
import com.shortlinkgeneration.mapper.AccessLogMapper;
import com.shortlinkgeneration.mapper.ShortUrlMapper;
import com.shortlinkgeneration.service.AsyncLogService;
import com.shortlinkgeneration.service.ShortUrlService;
import com.shortlinkgeneration.utils.HashUtils;
import com.shortlinkgeneration.utils.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ShortUrlServiceImpl extends ServiceImpl<ShortUrlMapper, ShortUrl> implements ShortUrlService {

    @Autowired
    private ShortUrlMapper shortUrlMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    // 新增注入
    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    private AccessLogMapper accessLogMapper;

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

    public String getLongUrl(String shortCode, HttpServletRequest request) {
        if (!bloomFilter.mightContain(shortCode)) {
            return null;
        }

        String key = SHORT_URL_KEY + shortCode;
        String longUrl = redisTemplate.opsForValue().get(key);
        if (longUrl != null) {
            redisTemplate.opsForValue().increment(PV_KEY + shortCode);
            // 异步记录日志
            saveAccessLog(shortCode, request);
            return longUrl;
        }

        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortUrl::getShortCode, shortCode)
                .gt(ShortUrl::getExpireTime, LocalDateTime.now());
        ShortUrl shortUrl = shortUrlMapper.selectOne(wrapper);
        if (shortUrl == null) {
            return null;
        }

        // 回写缓存 + 更新PV
        redisTemplate.opsForValue().set(key, shortUrl.getLongUrl(), 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().increment(PV_KEY + shortCode);

        // 异步记录日志
        saveAccessLog(shortCode, request);
        return shortUrl.getLongUrl();
    }


    /**
     * 封装日志对象并异步保存
     */
    private void saveAccessLog(String shortCode, HttpServletRequest request) {
        AccessLog log = new AccessLog();
        log.setShortCode(shortCode);
        log.setAccessTime(LocalDateTime.now());
        log.setIp(IpUtils.getClientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setReferer(request.getHeader("Referer") == null ? "" : request.getHeader("Referer"));

        // 解析UA，识别设备和浏览器
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        log.setDevice(userAgent.getOperatingSystem().getDeviceType().getName());
        log.setBrowser(userAgent.getBrowser().getName());

        asyncLogService.saveLogAsync(log);
    }

    /**
     * 统计接口实现
     */
    @Override
    public AccessStatisticsDTO getAccessStatistics(String shortCode, LocalDateTime startTime, LocalDateTime endTime) {
        AccessStatisticsDTO dto = new AccessStatisticsDTO();

        // 1. 总PV
        LambdaQueryWrapper<AccessLog> pvWrapper = Wrappers.lambdaQuery();
        pvWrapper.eq(AccessLog::getShortCode, shortCode)
                .between(AccessLog::getAccessTime, startTime, endTime);
        dto.setTotalPv(accessLogMapper.selectCount(pvWrapper));

        // 2. 总UV（按IP去重）
        QueryWrapper<AccessLog> uvWrapper = Wrappers.query();
        uvWrapper.eq("short_code", shortCode)
                .between("access_time", startTime, endTime)
                .select("COUNT(DISTINCT ip) as uv");
        Map<String, Object> uvResult = accessLogMapper.selectMaps(uvWrapper).get(0);
        dto.setTotalUv(((Number) uvResult.get("uv")).longValue());

        // 3. 按天时间分布
        QueryWrapper<AccessLog> timeWrapper = Wrappers.query();
        timeWrapper.eq("short_code", shortCode)
                .between("access_time", startTime, endTime)
                .select("DATE(access_time) as date", "COUNT(*) as count")
                .groupBy("date")
                .orderByAsc("date");
        dto.setTimeDistribution(accessLogMapper.selectMaps(timeWrapper));

        // 4. 来源分布
        QueryWrapper<AccessLog> sourceWrapper = Wrappers.query();
        sourceWrapper.eq("short_code", shortCode)
                .between("access_time", startTime, endTime)
                .select("IF(TRIM(referer) = '', '直接访问', referer) as source", "COUNT(*) as count")
                .groupBy("source")
                .orderByDesc("count");
        dto.setSourceDistribution(accessLogMapper.selectMaps(sourceWrapper));

        // 5. 设备分布
        QueryWrapper<AccessLog> deviceWrapper = Wrappers.query();
        deviceWrapper.eq("short_code", shortCode)
                .between("access_time", startTime, endTime)
                .select("IF(device = '', '未知', device) as device", "COUNT(*) as count")
                .groupBy("device")
                .orderByDesc("count");
        dto.setDeviceDistribution(accessLogMapper.selectMaps(deviceWrapper));

        return dto;
    }


//    @Resource
//    private ShortUrlMapper shortUrlMapper;
//
//    @Resource
//    private AccessLogMapper accessLogMapper;

//    @Override
//    public AccessStatisticsDTO getStatisticsByShortCode(String shortCode) {
//        // 1. 校验短链是否存在，拦截非法查询
//        ShortUrl shortUrl = shortUrlMapper.selectByShortCode(shortCode);
//        if (shortUrl == null) {
//            throw new RuntimeException("短链不存在，无法查询统计");
//        }
//
//        // 2. 多维度聚合访问日志
//        AccessStatisticsDTO statistics = new AccessStatisticsDTO();
//
//        // 2.1 总PV、总UV
//        statistics.setTotalPv(accessLogMapper.selectTotalPvByShortCode(shortCode));
//        statistics.setTotalUv(accessLogMapper.selectTotalUvByShortCode(shortCode));
//
//        // 2.2 按天统计访问趋势
//        statistics.setDailyTrend(accessLogMapper.selectDailyStatsByShortCode(shortCode));
//
//        // 2.3 访问来源分布
//        statistics.setSourceDistribution(accessLogMapper.selectSourceStatsByShortCode(shortCode));
//
//        return statistics;
//    }

    @Override
    public AccessStatisticsDTO getStatisticsByShortCode(String shortCode) {
        // 1. 校验短链是否存在，拦截非法查询
        LambdaQueryWrapper<ShortUrl> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(ShortUrl::getShortCode, shortCode);
        ShortUrl shortUrl = shortUrlMapper.selectOne(codeWrapper);
        if (shortUrl == null) {
            throw new RuntimeException("短链不存在，无法查询统计");
        }

        // 2. 组装统计数据，完全适配你的DTO字段
        AccessStatisticsDTO statistics = new AccessStatisticsDTO();

        // 2.1 总览数据
        statistics.setTotalPv(accessLogMapper.selectTotalPvByShortCode(shortCode));
        statistics.setTotalUv(accessLogMapper.selectTotalUvByShortCode(shortCode));

        // 2.2 按天时间分布
        statistics.setTimeDistribution(accessLogMapper.selectTimeDistribution(shortCode));

        // 2.3 来源分布
        statistics.setSourceDistribution(accessLogMapper.selectSourceDistribution(shortCode));

        // 2.4 设备分布
        statistics.setDeviceDistribution(accessLogMapper.selectDeviceDistribution(shortCode));

        return statistics;
    }

    @Override
    public int deleteExpiredLinks() {
        // 删除 expire_time 不为空 且 早于当前时间 的全部记录
        LambdaQueryWrapper<ShortUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(ShortUrl::getExpireTime)
               .lt(ShortUrl::getExpireTime, LocalDateTime.now());
        return shortUrlMapper.delete(wrapper);
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

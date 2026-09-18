package com.shortlinkgeneration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlinkgeneration.dto.AccessStatisticsDTO;
import com.shortlinkgeneration.dto.ShortLinkDTO;
import com.shortlinkgeneration.entity.ShortUrl;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

public interface ShortUrlService extends IService<ShortUrl> {

    String generateShortUrl(String longUrl);

    String getLongUrl(String shortCode, HttpServletRequest request);

    ShortUrl generateByDto(ShortLinkDTO dto);

    AccessStatisticsDTO getAccessStatistics(String shortCode, LocalDateTime startTime, LocalDateTime endTime);

    AccessStatisticsDTO getStatisticsByShortCode(String shortCode);

    /**
     * 删除所有已过期的短链记录
     * @return 删除的记录数
     */
    int deleteExpiredLinks();

}

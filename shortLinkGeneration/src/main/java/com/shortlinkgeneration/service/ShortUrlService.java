package com.shortlinkgeneration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlinkgeneration.dto.ShortLinkDTO;
import com.shortlinkgeneration.entity.ShortUrl;

public interface ShortUrlService extends IService<ShortUrl> {

    String generateShortUrl(String longUrl);

    String getLongUrl(String shortCode);

    ShortUrl generateByDto(ShortLinkDTO dto);
}

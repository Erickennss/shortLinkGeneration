package com.shortlinkgeneration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlinkgeneration.entity.ShortUrl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShortUrlMapper extends BaseMapper<ShortUrl> {

}

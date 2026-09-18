package com.shortlinkgeneration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlinkgeneration.dto.AccessStatisticsDTO;
import com.shortlinkgeneration.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {

    /**
     * 查询总PV
     */
    Long selectTotalPvByShortCode(@Param("shortCode") String shortCode);

    /**
     * 查询总UV（按IP去重）
     */
    Long selectTotalUvByShortCode(@Param("shortCode") String shortCode);

    /**
     * 查询按天时间分布
     * 返回Map结构：key=date(日期)、pv(访问量)
     */
    List<Map<String, Object>> selectTimeDistribution(@Param("shortCode") String shortCode);

    /**
     * 查询来源分布
     * 返回Map结构：key=referer(来源)、pv(访问量)
     */
    List<Map<String, Object>> selectSourceDistribution(@Param("shortCode") String shortCode);

    /**
     * 查询设备分布
     * 返回Map结构：key=device(设备类型)、pv(访问量)
     */
    List<Map<String, Object>> selectDeviceDistribution(@Param("shortCode") String shortCode);

//
//    List<AccessStatisticsDTO.DailyStats> selectDailyStatsByShortCode(@Param("shortCode") String shortCode);
//
//    List<AccessStatisticsDTO.SourceStats> selectSourceStatsByShortCode(@Param("shortCode") String shortCode);
}

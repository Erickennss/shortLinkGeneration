package com.shortlinkgeneration.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlinkgeneration.entity.AccessLog;
import com.shortlinkgeneration.mapper.AccessLogMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogService extends ServiceImpl<AccessLogMapper, AccessLog> {

    /**
     * 异步保存访问日志
     * 主线程直接返回重定向，日志写入在后台线程执行，不影响用户跳转体验
     */
    @Async
    public void saveLogAsync(AccessLog log) {
        this.save(log);
    }
}

package com.shortlinkgeneration.controller;

import com.shortlinkgeneration.common.Result;
import com.shortlinkgeneration.dto.AccessStatisticsDTO;
import com.shortlinkgeneration.dto.ShortLinkDTO;
import com.shortlinkgeneration.entity.ShortUrl;
import com.shortlinkgeneration.service.AsyncLogService;
import com.shortlinkgeneration.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@Tag(name = "短链接口", description = "短链生成与跳转")
@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    @Autowired
    private AsyncLogService asyncLogService;

    private final ShortUrlService shortUrlService;

    @Operation(summary = "长链转短链(表单参数)", description = "@RequestParam表单传参")
    @PostMapping("/generate")
    public ResponseEntity<String> generate(@RequestParam String longUrl) {
        String shortUrl = shortUrlService.generateShortUrl(longUrl);
        return ResponseEntity.ok(shortUrl);
    }

    /**
     * ✨前端Vue调用 JSON接口，@RequestBody接收JSON
     */
    @Operation(summary = "长链转短链(JSON)", description = "前端Vue调用，JSON入参")
    @PostMapping({"/api/generate", "/api/shortUrl/generate"})
    public Result<ShortUrl> generateByJson(@RequestBody ShortLinkDTO dto) {
        ShortUrl shortUrlEntity = shortUrlService.generateByDto(dto);
        return Result.success(shortUrlEntity);
    }


    @Operation(summary = "短链跳转",description = "访问短链码，302重定向到原始链接")
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String longUrl = shortUrlService.getLongUrl(shortCode, request);
        if (longUrl == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "短链不存在或已过期");
            return;
        }
        response.sendRedirect(longUrl);
    }

    @Operation(summary = "查询短链访问统计")
    @GetMapping("/api/statistics/{shortCode}")
    public Result<AccessStatisticsDTO> getStatistics(
            @PathVariable String shortCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 默认查询近7天数据
        if (startTime == null) startTime = LocalDateTime.now().minusDays(7);
        if (endTime == null) endTime = LocalDateTime.now();

        return Result.success(shortUrlService.getAccessStatistics(shortCode, startTime, endTime));
    }

    /**
     * 根据短链编码查询访问统计
     * 前端直接输入短码即可调用，无需先生成短链
     */
    @GetMapping({"/stats/{shortCode}", "/api/shortUrl/stats/{shortCode}"})
    @Operation(summary = "查询指定短链的访问统计")
    public Result<AccessStatisticsDTO> getShortUrlStats(
            @Parameter(description = "短链编码") @PathVariable String shortCode) {
        AccessStatisticsDTO statistics = shortUrlService.getStatisticsByShortCode(shortCode);
        return Result.success(statistics);
    }

    @Operation(summary = "删除所有过期短链", description = "手动触发清理：删除 expire_time 小于当前时间的记录")
    @DeleteMapping("/api/expired")
    public Result<Integer> deleteExpiredLinks() {
        int deleted = shortUrlService.deleteExpiredLinks();
        return Result.success(deleted);
    }

}

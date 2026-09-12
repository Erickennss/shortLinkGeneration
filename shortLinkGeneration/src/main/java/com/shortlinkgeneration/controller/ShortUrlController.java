package com.shortlinkgeneration.controller;

import com.shortlinkgeneration.common.Result;
import com.shortlinkgeneration.dto.ShortLinkDTO;
import com.shortlinkgeneration.entity.ShortUrl;
import com.shortlinkgeneration.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "短链接口", description = "短链生成与跳转")
@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    /**
     * 老接口：表单传参 @RequestParam longUrl
     */
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
    @PostMapping("/api/generate")
    public Result<ShortUrl> generateByJson(@RequestBody ShortLinkDTO dto) {
        ShortUrl shortUrlEntity = shortUrlService.generateByDto(dto);
        return Result.success(shortUrlEntity);
    }

    @Operation(summary = "短链跳转", description = "访问短链码，302重定向到原始链接")
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        if (longUrl == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "短链不存在或已过期");
            return;
        }
        response.sendRedirect(longUrl);
    }
}

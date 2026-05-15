package com.university.backend.content.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.content.application.AppSettingService;
import com.university.backend.content.application.NewsArticleService;
import com.university.backend.content.application.SitePageService;
import com.university.backend.content.domain.AppSetting;
import com.university.backend.content.domain.NewsArticle;
import com.university.backend.content.domain.SitePage;
import com.university.backend.content.dto.AppSettingRequest;
import com.university.backend.content.dto.NewsArticleRequest;
import com.university.backend.content.dto.SitePageRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminContentController {

    private final NewsArticleService newsArticleService;
    private final SitePageService sitePageService;
    private final AppSettingService appSettingService;

    public AdminContentController(
        NewsArticleService newsArticleService,
        SitePageService sitePageService,
        AppSettingService appSettingService
    ) {
        this.newsArticleService = newsArticleService;
        this.sitePageService = sitePageService;
        this.appSettingService = appSettingService;
    }

    @GetMapping("/news")
    public ApiResponse<PageResponse<NewsArticle>> news(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(PageResponse.from(newsArticleService.page(page, size, keyword)));
    }

    @PostMapping("/news")
    public ApiResponse<NewsArticle> createNews(@Valid @RequestBody NewsArticleRequest request) {
        return ApiResponse.ok(newsArticleService.create(request));
    }

    @PutMapping("/news/{id}")
    public ApiResponse<NewsArticle> updateNews(@PathVariable Long id, @Valid @RequestBody NewsArticleRequest request) {
        return ApiResponse.ok(newsArticleService.update(id, request));
    }

    @DeleteMapping("/news/{id}")
    public ApiResponse<Void> deleteNews(@PathVariable Long id) {
        newsArticleService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/pages")
    public ApiResponse<List<SitePage>> pages() {
        return ApiResponse.ok(sitePageService.listAll());
    }

    @PostMapping("/pages")
    public ApiResponse<SitePage> createPage(@Valid @RequestBody SitePageRequest request) {
        return ApiResponse.ok(sitePageService.create(request));
    }

    @PutMapping("/pages/{id}")
    public ApiResponse<SitePage> updatePage(@PathVariable Long id, @Valid @RequestBody SitePageRequest request) {
        return ApiResponse.ok(sitePageService.update(id, request));
    }

    @DeleteMapping("/pages/{id}")
    public ApiResponse<Void> deletePage(@PathVariable Long id) {
        sitePageService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/settings")
    public ApiResponse<List<AppSetting>> settings() {
        return ApiResponse.ok(appSettingService.listAll());
    }

    @PostMapping("/settings")
    public ApiResponse<AppSetting> createSetting(@Valid @RequestBody AppSettingRequest request) {
        return ApiResponse.ok(appSettingService.create(request));
    }

    @PutMapping("/settings/{id}")
    public ApiResponse<AppSetting> updateSetting(@PathVariable Long id, @Valid @RequestBody AppSettingRequest request) {
        return ApiResponse.ok(appSettingService.update(id, request));
    }

    @DeleteMapping("/settings/{id}")
    public ApiResponse<Void> deleteSetting(@PathVariable Long id) {
        appSettingService.delete(id);
        return ApiResponse.ok();
    }
}

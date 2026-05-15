package com.university.backend.content.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.content.application.NewsArticleService;
import com.university.backend.content.application.SitePageService;
import com.university.backend.content.domain.NewsArticle;
import com.university.backend.content.domain.SitePage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicContentController {

    private final NewsArticleService newsArticleService;
    private final SitePageService sitePageService;

    public PublicContentController(NewsArticleService newsArticleService, SitePageService sitePageService) {
        this.newsArticleService = newsArticleService;
        this.sitePageService = sitePageService;
    }

    @GetMapping("/news")
    public ApiResponse<PageResponse<NewsArticle>> news(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(PageResponse.from(newsArticleService.page(page, size, keyword)));
    }

    @GetMapping("/news/{id}")
    public ApiResponse<NewsArticle> newsDetail(@PathVariable Long id) {
        return ApiResponse.ok(newsArticleService.getRequired(id));
    }

    @GetMapping("/pages/{slug}")
    public ApiResponse<SitePage> page(@PathVariable String slug) {
        return ApiResponse.ok(sitePageService.getBySlug(slug));
    }
}

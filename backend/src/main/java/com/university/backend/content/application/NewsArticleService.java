package com.university.backend.content.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyNews;
import com.university.backend.legacy.infrastructure.LegacyNewsMapper;
import com.university.backend.content.domain.NewsArticle;
import com.university.backend.content.dto.NewsArticleRequest;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NewsArticleService {

    private final LegacyNewsMapper newsArticleMapper;

    public NewsArticleService(LegacyNewsMapper newsArticleMapper) {
        this.newsArticleMapper = newsArticleMapper;
    }

    public Page<NewsArticle> page(long page, long size, String keyword) {
        LambdaQueryWrapper<LegacyNews> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(LegacyNews::getTitle, keyword).or().like(LegacyNews::getIntroduction, keyword));
        }
        wrapper.orderByDesc(LegacyNews::getAddtime).orderByDesc(LegacyNews::getId);
        Page<LegacyNews> legacyPage = newsArticleMapper.selectPage(Page.of(page, size), wrapper);
        Page<NewsArticle> pageResult = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        pageResult.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return pageResult;
    }

    public NewsArticle getRequired(Long id) {
        return map(getLegacyRequired(id));
    }

    public NewsArticle create(NewsArticleRequest request) {
        LegacyNews article = new LegacyNews();
        apply(article, request);
        newsArticleMapper.insert(article);
        return map(article);
    }

    public NewsArticle update(Long id, NewsArticleRequest request) {
        LegacyNews article = getLegacyRequired(id);
        apply(article, request);
        newsArticleMapper.updateById(article);
        return map(article);
    }

    public void delete(Long id) {
        if (newsArticleMapper.deleteById(id) == 0) {
            throw ApiException.notFound("News article not found");
        }
    }

    private LegacyNews getLegacyRequired(Long id) {
        LegacyNews article = newsArticleMapper.selectById(id);
        if (article == null) {
            throw ApiException.notFound("News article not found");
        }
        return article;
    }

    private void apply(LegacyNews article, NewsArticleRequest request) {
        article.setTitle(request.title());
        article.setIntroduction(request.introduction());
        article.setPicture(StringUtils.hasText(request.picturePath()) ? request.picturePath() : "");
        article.setContent(request.content());
    }

    private NewsArticle map(LegacyNews legacy) {
        NewsArticle article = new NewsArticle();
        article.setId(legacy.getId());
        article.setCreatedAt(legacy.getAddtime());
        article.setUpdatedAt(legacy.getAddtime());
        article.setTitle(legacy.getTitle());
        article.setIntroduction(legacy.getIntroduction());
        article.setPicturePath(legacy.getPicture());
        article.setContent(legacy.getContent());
        article.setPublishedAt(legacy.getAddtime());
        return article;
    }
}

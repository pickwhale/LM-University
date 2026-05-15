package com.university.backend.content.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyAboutUs;
import com.university.backend.legacy.domain.LegacySystemIntro;
import com.university.backend.legacy.infrastructure.LegacyAboutUsMapper;
import com.university.backend.legacy.infrastructure.LegacySystemIntroMapper;
import com.university.backend.content.domain.SitePage;
import com.university.backend.content.dto.SitePageRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SitePageService {

    private final LegacyAboutUsMapper aboutUsMapper;
    private final LegacySystemIntroMapper systemIntroMapper;

    public SitePageService(LegacyAboutUsMapper aboutUsMapper, LegacySystemIntroMapper systemIntroMapper) {
        this.aboutUsMapper = aboutUsMapper;
        this.systemIntroMapper = systemIntroMapper;
    }

    public List<SitePage> listAll() {
        List<SitePage> pages = new ArrayList<>();
        aboutUsMapper.selectList(new LambdaQueryWrapper<LegacyAboutUs>().orderByAsc(LegacyAboutUs::getId))
            .forEach(page -> pages.add(mapAboutUs(page)));
        systemIntroMapper.selectList(new LambdaQueryWrapper<LegacySystemIntro>().orderByAsc(LegacySystemIntro::getId))
            .forEach(page -> pages.add(mapSystemIntro(page)));
        return pages;
    }

    public SitePage getBySlug(String slug) {
        return switch (slug) {
            case "about-us" -> mapAboutUs(getAboutUsRequired());
            case "system-intro" -> mapSystemIntro(getSystemIntroRequired());
            default -> throw ApiException.notFound("Site page not found");
        };
    }

    public SitePage getRequired(Long id) {
        LegacyAboutUs aboutUs = aboutUsMapper.selectById(id);
        if (aboutUs != null) {
            return mapAboutUs(aboutUs);
        }
        LegacySystemIntro systemIntro = systemIntroMapper.selectById(id);
        if (systemIntro != null) {
            return mapSystemIntro(systemIntro);
        }
        throw ApiException.notFound("Site page not found");
    }

    public SitePage create(SitePageRequest request) {
        return switch (request.slug()) {
            case "about-us" -> {
                LegacyAboutUs page = new LegacyAboutUs();
                apply(page, request);
                aboutUsMapper.insert(page);
                yield mapAboutUs(page);
            }
            case "system-intro" -> {
                LegacySystemIntro page = new LegacySystemIntro();
                apply(page, request);
                systemIntroMapper.insert(page);
                yield mapSystemIntro(page);
            }
            default -> throw ApiException.badRequest("Only about-us and system-intro are supported by the running schema");
        };
    }

    public SitePage update(Long id, SitePageRequest request) {
        LegacyAboutUs aboutUs = aboutUsMapper.selectById(id);
        if (aboutUs != null) {
            if (!"about-us".equals(request.slug())) {
                throw ApiException.badRequest("about-us records cannot be moved to another slug");
            }
            apply(aboutUs, request);
            aboutUsMapper.updateById(aboutUs);
            return mapAboutUs(aboutUs);
        }
        LegacySystemIntro systemIntro = systemIntroMapper.selectById(id);
        if (systemIntro != null) {
            if (!"system-intro".equals(request.slug())) {
                throw ApiException.badRequest("system-intro records cannot be moved to another slug");
            }
            apply(systemIntro, request);
            systemIntroMapper.updateById(systemIntro);
            return mapSystemIntro(systemIntro);
        }
        throw ApiException.notFound("Site page not found");
    }

    public void delete(Long id) {
        if (aboutUsMapper.deleteById(id) > 0) {
            return;
        }
        if (systemIntroMapper.deleteById(id) == 0) {
            throw ApiException.notFound("Site page not found");
        }
    }

    private LegacyAboutUs getAboutUsRequired() {
        LegacyAboutUs page = aboutUsMapper.selectOne(new LambdaQueryWrapper<LegacyAboutUs>().last("limit 1"));
        if (page == null) {
            throw ApiException.notFound("About us page not found");
        }
        return page;
    }

    private LegacySystemIntro getSystemIntroRequired() {
        LegacySystemIntro page = systemIntroMapper.selectOne(new LambdaQueryWrapper<LegacySystemIntro>().last("limit 1"));
        if (page == null) {
            throw ApiException.notFound("System intro page not found");
        }
        return page;
    }

    private void apply(LegacyAboutUs page, SitePageRequest request) {
        page.setTitle(request.title());
        page.setSubtitle(request.subtitle());
        page.setContent(request.content());
        page.setPicture1(request.picture1Path());
        page.setPicture2(request.picture2Path());
        page.setPicture3(request.picture3Path());
    }

    private void apply(LegacySystemIntro page, SitePageRequest request) {
        page.setTitle(request.title());
        page.setSubtitle(request.subtitle());
        page.setContent(request.content());
        page.setPicture1(request.picture1Path());
        page.setPicture2(request.picture2Path());
        page.setPicture3(request.picture3Path());
    }

    private SitePage mapAboutUs(LegacyAboutUs legacy) {
        SitePage page = new SitePage();
        page.setId(legacy.getId());
        page.setCreatedAt(legacy.getAddtime());
        page.setUpdatedAt(legacy.getAddtime());
        page.setSlug("about-us");
        page.setTitle(legacy.getTitle());
        page.setSubtitle(legacy.getSubtitle());
        page.setContent(legacy.getContent());
        page.setPicture1Path(legacy.getPicture1());
        page.setPicture2Path(legacy.getPicture2());
        page.setPicture3Path(legacy.getPicture3());
        return page;
    }

    private SitePage mapSystemIntro(LegacySystemIntro legacy) {
        SitePage page = new SitePage();
        page.setId(legacy.getId());
        page.setCreatedAt(legacy.getAddtime());
        page.setUpdatedAt(legacy.getAddtime());
        page.setSlug("system-intro");
        page.setTitle(legacy.getTitle());
        page.setSubtitle(legacy.getSubtitle());
        page.setContent(legacy.getContent());
        page.setPicture1Path(legacy.getPicture1());
        page.setPicture2Path(legacy.getPicture2());
        page.setPicture3Path(legacy.getPicture3());
        return page;
    }
}

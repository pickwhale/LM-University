package com.university.backend.university.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyProvince;
import com.university.backend.legacy.domain.LegacyUniversityInformation;
import com.university.backend.legacy.infrastructure.LegacyProvinceMapper;
import com.university.backend.legacy.infrastructure.LegacyUniversityInformationMapper;
import com.university.backend.university.domain.University;
import com.university.backend.university.dto.UniversityRequest;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class UniversityService {


    private final LegacyUniversityInformationMapper universityMapper;
    private final LegacyProvinceMapper provinceMapper;

    public UniversityService(
        LegacyUniversityInformationMapper universityMapper,
        LegacyProvinceMapper provinceMapper
    ) {
        this.universityMapper = universityMapper;
        this.provinceMapper = provinceMapper;
    }

    public Page<University> pagePublic(long page, long size, String keyword, Long provinceId) {
        LambdaQueryWrapper<LegacyUniversityInformation> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(LegacyUniversityInformation::getUniversityName, keyword);
        }
        if (provinceId != null) {
            LegacyProvince province = provinceMapper.selectById(provinceId);
            if (province == null) {
                return Page.of(page, size);
            }
            wrapper.eq(LegacyUniversityInformation::getProvince, province.getProvince());
        }
        wrapper.orderByAsc(LegacyUniversityInformation::getUniversityName);
        Page<LegacyUniversityInformation> legacyPage = universityMapper.selectPage(Page.of(page, size), wrapper);
        return mapPage(legacyPage);
    }
    //@Cacheable(value = "university", key = "#id")
    public Page<University> pageAdmin(long page, long size, String keyword) {
        return pagePublic(page, size, keyword, null);
    }

    public University getRequired(Long id) {
        return map(getLegacyRequired(id));
    }

    public University create(UniversityRequest request) {
        LegacyUniversityInformation university = new LegacyUniversityInformation();
        apply(university, request);
        university.setClicknum(0);
        universityMapper.insert(university);
        return map(university);
    }
    //@CacheEvict(value = "university", key = "#id")
    public University update(Long id, UniversityRequest request) {
        LegacyUniversityInformation university = getLegacyRequired(id);
        apply(university, request);
        universityMapper.updateById(university);
        return map(university);
    }
    //@CacheEvict(value = "university", key = "#id")
    public void delete(Long id) {
        if (universityMapper.deleteById(id) == 0) {
            throw ApiException.notFound("University not found");
        }
    }

    public LegacyUniversityInformation getLegacyRequired(Long id) {
        LegacyUniversityInformation university = universityMapper.selectById(id);
        if (university == null) {
            throw ApiException.notFound("University not found");
        }
        return university;
    }

    private void apply(LegacyUniversityInformation university, UniversityRequest request) {
        university.setUniversityName(request.name());
        university.setUniversityWebsite(request.website());
        university.setUniversityImage(request.imagePath());
        university.setProvince(resolveProvinceName(request.provinceId()));
        university.setInstitutionType(request.institutionType());
        university.setKeyness(request.keyness());
        university.setUniversityIntroduction(request.introduction());
        university.setPhone(request.phone());
    }

    private Page<University> mapPage(Page<LegacyUniversityInformation> legacyPage) {
        Page<University> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private University map(LegacyUniversityInformation record) {
        University university = new University();
        university.setId(record.getId());
        university.setCreatedAt(record.getAddtime());
        university.setUpdatedAt(record.getClicktime() != null ? record.getClicktime() : record.getAddtime());
        university.setName(record.getUniversityName());
        university.setWebsite(record.getUniversityWebsite());
        university.setImagePath(record.getUniversityImage());
        university.setProvinceId(resolveProvinceId(record.getProvince()));
        university.setInstitutionType(record.getInstitutionType());
        university.setKeyness(record.getKeyness());
        university.setIntroduction(record.getUniversityIntroduction());
        university.setPhone(record.getPhone());
        university.setClickCount(record.getClicknum() == null ? 0 : record.getClicknum());
        return university;
    }

    private Long resolveProvinceId(String provinceName) {
        if (!StringUtils.hasText(provinceName)) {
            return null;
        }
        LegacyProvince province = provinceMapper.selectOne(
            new LambdaQueryWrapper<LegacyProvince>().eq(LegacyProvince::getProvince, provinceName).last("limit 1")
        );
        return province == null ? null : province.getId();
    }

    private String resolveProvinceName(Long provinceId) {
        if (provinceId == null) {
            return null;
        }
        LegacyProvince province = provinceMapper.selectById(provinceId);
        return province == null ? null : province.getProvince();
    }
}

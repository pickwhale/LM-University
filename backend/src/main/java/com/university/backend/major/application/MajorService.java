package com.university.backend.major.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyProfessionalInformation;
import com.university.backend.legacy.domain.LegacyUniversityInformation;
import com.university.backend.legacy.infrastructure.LegacyProfessionalInformationMapper;
import com.university.backend.legacy.infrastructure.LegacyUniversityInformationMapper;
import com.university.backend.major.domain.Major;
import com.university.backend.major.dto.MajorRequest;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MajorService {

    private final LegacyProfessionalInformationMapper majorMapper;
    private final LegacyUniversityInformationMapper universityMapper;

    public MajorService(
        LegacyProfessionalInformationMapper majorMapper,
        LegacyUniversityInformationMapper universityMapper
    ) {
        this.majorMapper = majorMapper;
        this.universityMapper = universityMapper;
    }

    public Page<Major> pagePublic(long page, long size, String keyword, Long universityId) {
        LambdaQueryWrapper<LegacyProfessionalInformation> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(LegacyProfessionalInformation::getMajorName, keyword).or().like(LegacyProfessionalInformation::getMajorCode, keyword));
        }
        if (universityId != null) {
            LegacyUniversityInformation university = universityMapper.selectById(universityId);
            if (university == null) {
                return Page.of(page, size);
            }
            wrapper.eq(LegacyProfessionalInformation::getUniversityName, university.getUniversityName());
        }
        wrapper.orderByAsc(LegacyProfessionalInformation::getMajorName);
        return mapPage(majorMapper.selectPage(Page.of(page, size), wrapper));
    }

    public Page<Major> pageAdmin(long page, long size, String keyword) {
        return pagePublic(page, size, keyword, null);
    }

    public Major getRequired(Long id) {
        return map(getLegacyRequired(id));
    }

    public Major create(MajorRequest request) {
        LegacyProfessionalInformation major = new LegacyProfessionalInformation();
        apply(major, request);
        major.setClicknum(0);
        majorMapper.insert(major);
        return map(major);
    }

    public Major update(Long id, MajorRequest request) {
        LegacyProfessionalInformation major = getLegacyRequired(id);
        apply(major, request);
        majorMapper.updateById(major);
        return map(major);
    }

    public void delete(Long id) {
        if (majorMapper.deleteById(id) == 0) {
            throw ApiException.notFound("Major not found");
        }
    }

    public LegacyProfessionalInformation getLegacyRequired(Long id) {
        LegacyProfessionalInformation major = majorMapper.selectById(id);
        if (major == null) {
            throw ApiException.notFound("Major not found");
        }
        return major;
    }

    private void apply(LegacyProfessionalInformation major, MajorRequest request) {
        LegacyUniversityInformation university = request.universityId() == null ? null : universityMapper.selectById(request.universityId());
        major.setMajorCode(request.code());
        major.setMajorName(request.name());
        major.setCover(request.coverPath());
        major.setDurationOfStudy(request.durationOfStudy());
        major.setCutOffScore(request.cutOffScore());
        major.setEnrollmentQuota(request.enrollmentQuota());
        major.setCurriculum(request.curriculum());
        major.setUniversityName(university == null ? null : university.getUniversityName());
        major.setProvince(university == null ? null : university.getProvince());
    }

    private Page<Major> mapPage(Page<LegacyProfessionalInformation> legacyPage) {
        Page<Major> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private Major map(LegacyProfessionalInformation record) {
        Major major = new Major();
        major.setId(record.getId());
        major.setCreatedAt(record.getAddtime());
        major.setUpdatedAt(record.getClicktime() != null ? record.getClicktime() : record.getAddtime());
        major.setCode(record.getMajorCode());
        major.setUniversityId(resolveUniversityId(record.getUniversityName()));
        major.setName(record.getMajorName());
        major.setCoverPath(record.getCover());
        major.setDurationOfStudy(record.getDurationOfStudy());
        major.setCutOffScore(record.getCutOffScore());
        major.setEnrollmentQuota(record.getEnrollmentQuota());
        major.setCurriculum(record.getCurriculum());
        major.setClickCount(record.getClicknum() == null ? 0 : record.getClicknum());
        return major;
    }

    private Long resolveUniversityId(String universityName) {
        if (!StringUtils.hasText(universityName)) {
            return null;
        }
        LegacyUniversityInformation university = universityMapper.selectOne(
            new LambdaQueryWrapper<LegacyUniversityInformation>()
                .eq(LegacyUniversityInformation::getUniversityName, universityName)
                .last("limit 1")
        );
        return university == null ? null : university.getId();
    }
}

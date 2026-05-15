package com.university.backend.application.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.application.domain.UniversityApplication;
import com.university.backend.application.dto.ApplicationReviewRequest;
import com.university.backend.application.dto.UniversityApplicationRequest;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.LegacyStatusMapper;
import com.university.backend.legacy.domain.LegacyCollegeApplication;
import com.university.backend.legacy.domain.LegacyStudent;
import com.university.backend.legacy.domain.LegacyUniversityInformation;
import com.university.backend.legacy.infrastructure.LegacyCollegeApplicationMapper;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import com.university.backend.university.application.UniversityService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UniversityApplicationService {

    private final LegacyCollegeApplicationMapper applicationMapper;
    private final StudentProfileService studentProfileService;
    private final UniversityService universityService;

    public UniversityApplicationService(
        LegacyCollegeApplicationMapper applicationMapper,
        StudentProfileService studentProfileService,
        UniversityService universityService
    ) {
        this.applicationMapper = applicationMapper;
        this.studentProfileService = studentProfileService;
        this.universityService = universityService;
    }

    public UniversityApplication create(Long accountId, UniversityApplicationRequest request) {
        StudentProfile student = requireStudent(accountId);
        LegacyStudent legacyStudent = requireLegacyStudent(accountId);
        LegacyUniversityInformation university = universityService.getLegacyRequired(request.universityId());

        LegacyCollegeApplication application = new LegacyCollegeApplication();
        application.setRegistrationNumber(String.valueOf(System.currentTimeMillis()));
        application.setUniversityName(university.getUniversityName());
        application.setInstitutionType(university.getInstitutionType());
        application.setMajorOffered(university.getMajorOffered());
        application.setProvince(university.getProvince());
        application.setApplicationTime(LocalDateTime.now());
        application.setStudentID(student.getStudentNo());
        application.setStudentName(student.getFullName());
        application.setContactNumber(student.getContactNumber());
        application.setCollege(student.getCollege());
        application.setCrossuserid(legacyStudent.getId());
        application.setCrossrefid(university.getId());
        application.setSfsh(LegacyStatusMapper.toLegacyStatus("PENDING"));
        application.setShhf(null);
        applicationMapper.insert(application);
        return map(application);
    }

    public Page<UniversityApplication> pageOwn(Long accountId, long page, long size) {
        StudentProfile student = requireStudent(accountId);
        Page<LegacyCollegeApplication> legacyPage = applicationMapper.selectPage(
            Page.of(page, size),
            new LambdaQueryWrapper<LegacyCollegeApplication>()
                .eq(LegacyCollegeApplication::getStudentID, student.getStudentNo())
                .orderByDesc(LegacyCollegeApplication::getApplicationTime)
                .orderByDesc(LegacyCollegeApplication::getId)
        );
        return mapPage(legacyPage);
    }

    public Page<UniversityApplication> pageAdmin(long page, long size, String status) {
        LambdaQueryWrapper<LegacyCollegeApplication> wrapper = new LambdaQueryWrapper<>();
        applyStatusFilter(wrapper, status);
        wrapper.orderByDesc(LegacyCollegeApplication::getApplicationTime).orderByDesc(LegacyCollegeApplication::getId);
        return mapPage(applicationMapper.selectPage(Page.of(page, size), wrapper));
    }

    public UniversityApplication review(Long id, ApplicationReviewRequest request) {
        LegacyCollegeApplication application = getLegacyRequired(id);
        application.setSfsh(LegacyStatusMapper.toLegacyStatus(request.status()));
        application.setShhf(request.reviewComment());
        applicationMapper.updateById(application);
        return map(application);
    }

    public UniversityApplication getRequired(Long id) {
        return map(getLegacyRequired(id));
    }

    private StudentProfile requireStudent(Long accountId) {
        StudentProfile student = studentProfileService.findByAccountId(accountId);
        if (student == null) {
            throw ApiException.forbidden("Student profile not found");
        }
        return student;
    }

    private LegacyStudent requireLegacyStudent(Long accountId) {
        LegacyStudent student = studentProfileService.findLegacyByAccountId(accountId);
        if (student == null) {
            throw ApiException.forbidden("Student account not found");
        }
        return student;
    }

    private LegacyCollegeApplication getLegacyRequired(Long id) {
        LegacyCollegeApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw ApiException.notFound("University application not found");
        }
        return application;
    }

    private void applyStatusFilter(LambdaQueryWrapper<LegacyCollegeApplication> wrapper, String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        switch (status.trim().toUpperCase()) {
            case "APPROVED" -> wrapper.in(LegacyCollegeApplication::getSfsh, LegacyStatusMapper.APPROVED_VALUES);
            case "REJECTED" -> wrapper.in(LegacyCollegeApplication::getSfsh, LegacyStatusMapper.REJECTED_VALUES);
            default -> wrapper.and(q -> q.in(LegacyCollegeApplication::getSfsh, LegacyStatusMapper.PENDING_VALUES).or().isNull(LegacyCollegeApplication::getSfsh));
        }
    }

    private Page<UniversityApplication> mapPage(Page<LegacyCollegeApplication> legacyPage) {
        Page<UniversityApplication> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private UniversityApplication map(LegacyCollegeApplication legacy) {
        UniversityApplication application = new UniversityApplication();
        application.setId(legacy.getId());
        application.setCreatedAt(legacy.getAddtime());
        application.setUpdatedAt(legacy.getAddtime());
        application.setRegistrationNo(legacy.getRegistrationNumber());
        application.setStudentId(legacy.getCrossuserid());
        application.setUniversityId(legacy.getCrossrefid());
        application.setStatus(LegacyStatusMapper.toApiStatus(legacy.getSfsh()));
        application.setReviewComment(legacy.getShhf());
        application.setSubmittedAt(legacy.getApplicationTime());
        application.setReviewedAt(null);
        application.setStudentNoSnapshot(legacy.getStudentID());
        application.setStudentNameSnapshot(legacy.getStudentName());
        application.setContactNumberSnapshot(legacy.getContactNumber());
        application.setCollegeSnapshot(legacy.getCollege());
        application.setUniversityNameSnapshot(legacy.getUniversityName());
        application.setInstitutionTypeSnapshot(legacy.getInstitutionType());
        application.setProvinceNameSnapshot(legacy.getProvince());
        return application;
    }
}

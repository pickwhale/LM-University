package com.university.backend.application.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.application.domain.MajorApplication;
import com.university.backend.application.dto.ApplicationReviewRequest;
import com.university.backend.application.dto.MajorApplicationRequest;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.LegacyStatusMapper;
import com.university.backend.legacy.domain.LegacyProfessionalInformation;
import com.university.backend.legacy.domain.LegacyProfessionalRegistration;
import com.university.backend.legacy.domain.LegacyStudent;
import com.university.backend.legacy.infrastructure.LegacyProfessionalRegistrationMapper;
import com.university.backend.major.application.MajorService;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MajorApplicationService {

    private final LegacyProfessionalRegistrationMapper applicationMapper;
    private final StudentProfileService studentProfileService;
    private final MajorService majorService;

    public MajorApplicationService(
        LegacyProfessionalRegistrationMapper applicationMapper,
        StudentProfileService studentProfileService,
        MajorService majorService
    ) {
        this.applicationMapper = applicationMapper;
        this.studentProfileService = studentProfileService;
        this.majorService = majorService;
    }

    public MajorApplication create(Long accountId, MajorApplicationRequest request) {
        StudentProfile student = requireStudent(accountId);
        LegacyStudent legacyStudent = requireLegacyStudent(accountId);
        LegacyProfessionalInformation major = majorService.getLegacyRequired(request.majorId());

        LegacyProfessionalRegistration application = new LegacyProfessionalRegistration();
        application.setMajorCode(major.getMajorCode());
        application.setMajorName(major.getMajorName());
        application.setDurationOfStudy(major.getDurationOfStudy());
        application.setCover(major.getCover());
        application.setEnrollmentQuota(major.getEnrollmentQuota() == null ? null : String.valueOf(major.getEnrollmentQuota()));
        application.setCutOffScore(major.getCutOffScore());
        application.setCurriculum(major.getCurriculum());
        application.setApplicationTime(LocalDateTime.now());
        application.setUniversityName(major.getUniversityName());
        application.setProvince(major.getProvince());
        application.setStudentID(student.getStudentNo());
        application.setStudentName(student.getFullName());
        application.setContactNumber(student.getContactNumber());
        application.setCrossuserid(legacyStudent.getId());
        application.setCrossrefid(major.getId());
        application.setSfsh(LegacyStatusMapper.toLegacyStatus("PENDING"));
        application.setShhf(null);
        applicationMapper.insert(application);
        return map(application);
    }

    public Page<MajorApplication> pageOwn(Long accountId, long page, long size) {
        StudentProfile student = requireStudent(accountId);
        Page<LegacyProfessionalRegistration> legacyPage = applicationMapper.selectPage(
            Page.of(page, size),
            new LambdaQueryWrapper<LegacyProfessionalRegistration>()
                .eq(LegacyProfessionalRegistration::getStudentID, student.getStudentNo())
                .orderByDesc(LegacyProfessionalRegistration::getApplicationTime)
                .orderByDesc(LegacyProfessionalRegistration::getId)
        );
        return mapPage(legacyPage);
    }

    public Page<MajorApplication> pageAdmin(long page, long size, String status) {
        LambdaQueryWrapper<LegacyProfessionalRegistration> wrapper = new LambdaQueryWrapper<>();
        applyStatusFilter(wrapper, status);
        wrapper.orderByDesc(LegacyProfessionalRegistration::getApplicationTime).orderByDesc(LegacyProfessionalRegistration::getId);
        return mapPage(applicationMapper.selectPage(Page.of(page, size), wrapper));
    }

    public MajorApplication review(Long id, ApplicationReviewRequest request) {
        LegacyProfessionalRegistration application = getLegacyRequired(id);
        application.setSfsh(LegacyStatusMapper.toLegacyStatus(request.status()));
        application.setShhf(request.reviewComment());
        applicationMapper.updateById(application);
        return map(application);
    }

    public MajorApplication getRequired(Long id) {
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

    private LegacyProfessionalRegistration getLegacyRequired(Long id) {
        LegacyProfessionalRegistration application = applicationMapper.selectById(id);
        if (application == null) {
            throw ApiException.notFound("Major application not found");
        }
        return application;
    }

    private void applyStatusFilter(LambdaQueryWrapper<LegacyProfessionalRegistration> wrapper, String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        switch (status.trim().toUpperCase()) {
            case "APPROVED" -> wrapper.in(LegacyProfessionalRegistration::getSfsh, LegacyStatusMapper.APPROVED_VALUES);
            case "REJECTED" -> wrapper.in(LegacyProfessionalRegistration::getSfsh, LegacyStatusMapper.REJECTED_VALUES);
            default -> wrapper.and(q -> q.in(LegacyProfessionalRegistration::getSfsh, LegacyStatusMapper.PENDING_VALUES).or().isNull(LegacyProfessionalRegistration::getSfsh));
        }
    }

    private Page<MajorApplication> mapPage(Page<LegacyProfessionalRegistration> legacyPage) {
        Page<MajorApplication> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private MajorApplication map(LegacyProfessionalRegistration legacy) {
        MajorApplication application = new MajorApplication();
        application.setId(legacy.getId());
        application.setCreatedAt(legacy.getAddtime());
        application.setUpdatedAt(legacy.getAddtime());
        application.setStudentId(legacy.getCrossuserid());
        application.setMajorId(legacy.getCrossrefid());
        application.setStatus(LegacyStatusMapper.toApiStatus(legacy.getSfsh()));
        application.setReviewComment(legacy.getShhf());
        application.setSubmittedAt(legacy.getApplicationTime());
        application.setReviewedAt(null);
        application.setStudentNoSnapshot(legacy.getStudentID());
        application.setStudentNameSnapshot(legacy.getStudentName());
        application.setContactNumberSnapshot(legacy.getContactNumber());
        application.setMajorCodeSnapshot(legacy.getMajorCode());
        application.setMajorNameSnapshot(legacy.getMajorName());
        application.setUniversityNameSnapshot(legacy.getUniversityName());
        application.setProvinceNameSnapshot(legacy.getProvince());
        return application;
    }
}

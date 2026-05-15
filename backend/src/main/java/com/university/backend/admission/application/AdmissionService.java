package com.university.backend.admission.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.admission.domain.AcademicResult;
import com.university.backend.admission.domain.AdmissionResult;
import com.university.backend.admission.dto.AcademicResultRequest;
import com.university.backend.admission.dto.AdmissionResultRequest;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyAdmissionResults;
import com.university.backend.legacy.domain.LegacyCollegeApplication;
import com.university.backend.legacy.domain.LegacyResultsInformation;
import com.university.backend.legacy.domain.LegacyStudent;
import com.university.backend.legacy.infrastructure.LegacyAdmissionResultsMapper;
import com.university.backend.legacy.infrastructure.LegacyCollegeApplicationMapper;
import com.university.backend.legacy.infrastructure.LegacyResultsInformationMapper;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class AdmissionService {

    private final LegacyAdmissionResultsMapper admissionResultMapper;
    private final LegacyResultsInformationMapper academicResultMapper;
    private final LegacyCollegeApplicationMapper universityApplicationMapper;
    private final StudentProfileService studentProfileService;

    public AdmissionService(
        LegacyAdmissionResultsMapper admissionResultMapper,
        LegacyResultsInformationMapper academicResultMapper,
        LegacyCollegeApplicationMapper universityApplicationMapper,
        StudentProfileService studentProfileService
    ) {
        this.admissionResultMapper = admissionResultMapper;
        this.academicResultMapper = academicResultMapper;
        this.universityApplicationMapper = universityApplicationMapper;
        this.studentProfileService = studentProfileService;
    }

    public Page<AdmissionResult> pageAdminAdmission(long page, long size) {
        return mapAdmissionPage(
            admissionResultMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<LegacyAdmissionResults>().orderByDesc(LegacyAdmissionResults::getAddtime))
        );
    }

    public AdmissionResult createAdmission(AdmissionResultRequest request) {
        LegacyCollegeApplication application = universityApplicationMapper.selectById(request.applicationId());
        if (application == null) {
            throw ApiException.notFound("University application not found");
        }
        LegacyAdmissionResults result = new LegacyAdmissionResults();
        apply(result, request);
        result.setRegistrationNumber(application.getRegistrationNumber());
        result.setUniversityName(application.getUniversityName());
        result.setStudentID(application.getStudentID());
        result.setStudentName(application.getStudentName());
        result.setContactNumber(application.getContactNumber());
        admissionResultMapper.insert(result);
        return map(result);
    }

    public AdmissionResult updateAdmission(Long id, AdmissionResultRequest request) {
        LegacyAdmissionResults result = getLegacyAdmissionRequired(id);
        apply(result, request);
        admissionResultMapper.updateById(result);
        return map(result);
    }

    public Page<AdmissionResult> pageOwnAdmission(Long accountId, long page, long size) {
        StudentProfile student = requireStudent(accountId);
        return mapAdmissionPage(admissionResultMapper.selectPage(
            Page.of(page, size),
            new LambdaQueryWrapper<LegacyAdmissionResults>()
                .eq(LegacyAdmissionResults::getStudentID, student.getStudentNo())
                .orderByDesc(LegacyAdmissionResults::getAddtime)
        ));
    }

    public Page<AcademicResult> pageAdminAcademic(long page, long size) {
        return mapAcademicPage(
            academicResultMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<LegacyResultsInformation>().orderByDesc(LegacyResultsInformation::getAddtime))
        );
    }

    public AcademicResult createAcademic(AcademicResultRequest request) {
        LegacyStudent student = studentProfileService.findLegacyByAccountId(request.studentId());
        LegacyResultsInformation result = new LegacyResultsInformation();
        apply(result, request);
        if (student != null) {
            result.setStudentID(student.getStudentID());
            result.setStudentName(student.getStudentName());
            result.setContactNumber(student.getContactNumber());
        }
        academicResultMapper.insert(result);
        return map(result);
    }

    public AcademicResult updateAcademic(Long id, AcademicResultRequest request) {
        LegacyResultsInformation result = getLegacyAcademicRequired(id);
        apply(result, request);
        academicResultMapper.updateById(result);
        return map(result);
    }

    public Page<AcademicResult> pageOwnAcademic(Long accountId, long page, long size) {
        StudentProfile student = requireStudent(accountId);
        return mapAcademicPage(academicResultMapper.selectPage(
            Page.of(page, size),
            new LambdaQueryWrapper<LegacyResultsInformation>()
                .eq(LegacyResultsInformation::getStudentID, student.getStudentNo())
                .orderByDesc(LegacyResultsInformation::getAddtime)
        ));
    }

    private AdmissionResult getAdmissionRequired(Long id) {
        return map(getLegacyAdmissionRequired(id));
    }

    private AcademicResult getAcademicRequired(Long id) {
        return map(getLegacyAcademicRequired(id));
    }

    private LegacyAdmissionResults getLegacyAdmissionRequired(Long id) {
        LegacyAdmissionResults result = admissionResultMapper.selectById(id);
        if (result == null) {
            throw ApiException.notFound("Admission result not found");
        }
        return result;
    }

    private LegacyResultsInformation getLegacyAcademicRequired(Long id) {
        LegacyResultsInformation result = academicResultMapper.selectById(id);
        if (result == null) {
            throw ApiException.notFound("Academic result not found");
        }
        return result;
    }

    private StudentProfile requireStudent(Long accountId) {
        StudentProfile student = studentProfileService.findByAccountId(accountId);
        if (student == null) {
            throw ApiException.forbidden("Student profile not found");
        }
        return student;
    }

    private void apply(LegacyAdmissionResults result, AdmissionResultRequest request) {
        result.setAdmissionResults(request.resultStatus());
        result.setFeedback(request.feedback());
        result.setFeedbackTime(request.feedbackAt());
    }

    private void apply(LegacyResultsInformation result, AcademicResultRequest request) {
        result.setReportNumber(request.reportNo());
        result.setReportContent(request.reportContent());
        result.setGrade(request.grade());
        result.setGradepingding(request.gradeEvaluation());
        result.setEntryTime(request.enteredAt());
    }

    private Page<AdmissionResult> mapAdmissionPage(Page<LegacyAdmissionResults> legacyPage) {
        Page<AdmissionResult> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private Page<AcademicResult> mapAcademicPage(Page<LegacyResultsInformation> legacyPage) {
        Page<AcademicResult> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private AdmissionResult map(LegacyAdmissionResults legacy) {
        AdmissionResult result = new AdmissionResult();
        result.setId(legacy.getId());
        result.setCreatedAt(legacy.getAddtime());
        result.setUpdatedAt(legacy.getFeedbackTime() != null ? legacy.getFeedbackTime() : legacy.getAddtime());
        LegacyCollegeApplication application = legacy.getRegistrationNumber() == null ? null : universityApplicationMapper.selectOne(
            new LambdaQueryWrapper<LegacyCollegeApplication>()
                .eq(LegacyCollegeApplication::getRegistrationNumber, legacy.getRegistrationNumber())
                .last("limit 1")
        );
        result.setApplicationId(application == null ? null : application.getId());
        result.setResultStatus(legacy.getAdmissionResults());
        result.setFeedback(legacy.getFeedback());
        result.setFeedbackAt(legacy.getFeedbackTime());
        return result;
    }

    private AcademicResult map(LegacyResultsInformation legacy) {
        AcademicResult result = new AcademicResult();
        result.setId(legacy.getId());
        result.setCreatedAt(legacy.getAddtime());
        result.setUpdatedAt(legacy.getAddtime());
        result.setStudentId(resolveStudentId(legacy.getStudentID()));
        result.setReportNo(legacy.getReportNumber());
        result.setReportContent(legacy.getReportContent());
        result.setGrade(legacy.getGrade());
        result.setGradeEvaluation(legacy.getGradepingding());
        result.setEnteredAt(legacy.getEntryTime());
        return result;
    }

    private Long resolveStudentId(String studentNo) {
        if (studentNo == null) {
            return null;
        }
        StudentProfile profile = studentProfileService.findByStudentNo(studentNo);
        return profile == null ? null : profile.getId();
    }
}

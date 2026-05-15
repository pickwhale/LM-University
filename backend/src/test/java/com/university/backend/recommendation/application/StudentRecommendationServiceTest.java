package com.university.backend.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.university.backend.legacy.domain.LegacyAdmissionResults;
import com.university.backend.legacy.domain.LegacyCollegeApplication;
import com.university.backend.legacy.domain.LegacyProfessionalInformation;
import com.university.backend.legacy.domain.LegacyProfessionalRegistration;
import com.university.backend.legacy.domain.LegacyResultsInformation;
import com.university.backend.legacy.domain.LegacyStoreup;
import com.university.backend.legacy.domain.LegacyStudent;
import com.university.backend.legacy.domain.LegacyUniversityInformation;
import com.university.backend.legacy.infrastructure.LegacyAdmissionResultsMapper;
import com.university.backend.legacy.infrastructure.LegacyCollegeApplicationMapper;
import com.university.backend.legacy.infrastructure.LegacyProfessionalInformationMapper;
import com.university.backend.legacy.infrastructure.LegacyProfessionalRegistrationMapper;
import com.university.backend.legacy.infrastructure.LegacyResultsInformationMapper;
import com.university.backend.legacy.infrastructure.LegacyStoreupMapper;
import com.university.backend.legacy.infrastructure.LegacyStudentMapper;
import com.university.backend.legacy.infrastructure.LegacyUniversityInformationMapper;
import com.university.backend.recommendation.dto.StudentRecommendationResponse;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentRecommendationServiceTest {

    @Mock
    private StudentProfileService studentProfileService;
    @Mock
    private LegacyStudentMapper studentMapper;
    @Mock
    private LegacyResultsInformationMapper resultMapper;
    @Mock
    private LegacyProfessionalInformationMapper majorMapper;
    @Mock
    private LegacyUniversityInformationMapper universityMapper;
    @Mock
    private LegacyStoreupMapper favoriteMapper;
    @Mock
    private LegacyCollegeApplicationMapper universityApplicationMapper;
    @Mock
    private LegacyProfessionalRegistrationMapper majorApplicationMapper;
    @Mock
    private LegacyAdmissionResultsMapper admissionResultMapper;

    private StudentRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new StudentRecommendationService(
            studentProfileService,
            studentMapper,
            resultMapper,
            majorMapper,
            universityMapper,
            favoriteMapper,
            universityApplicationMapper,
            majorApplicationMapper,
            admissionResultMapper
        );
    }

    @Test
    void returnsEmptyRecommendationsWhenCurrentStudentHasNoGrade() {
        when(studentProfileService.findByAccountId(1L)).thenReturn(profile(1L, "S001"));
        when(resultMapper.selectList(any())).thenReturn(List.of(result("S001", null)));

        StudentRecommendationResponse response = service.recommend(1L, 10);

        assertThat(response.latestGrade()).isNull();
        assertThat(response.message()).isEqualTo("暂无成绩，无法生成推荐");
        assertThat(response.majors()).isEmpty();
        assertThat(response.universities()).isEmpty();
    }

    @Test
    void filtersByCutoffAndBoostsMajorRankWithSimilarStudentInteraction() {
        when(studentProfileService.findByAccountId(1L)).thenReturn(profile(1L, "S001"));
        when(resultMapper.selectList(any())).thenReturn(List.of(
            result("S001", 600),
            result("S002", 595)
        ));
        when(studentMapper.selectList(any())).thenReturn(List.of(
            student(1L, "S001"),
            student(2L, "S002")
        ));
        when(majorMapper.selectList(any())).thenReturn(List.of(
            major(10L, "M10", "稳妥专业", "测试大学", "550", 10, 10),
            major(11L, "M11", "相似学生关注专业", "测试大学", "550", 10, 10),
            major(12L, "M12", "未达线专业", "测试大学", "650", 10, 10)
        ));
        when(universityMapper.selectList(any())).thenReturn(List.of(university(20L, "测试大学")));
        when(favoriteMapper.selectList(any())).thenReturn(List.of(favorite(2L, 11L, "MAJOR", "相似学生关注专业")));
        when(majorApplicationMapper.selectList(any())).thenReturn(List.<LegacyProfessionalRegistration>of());
        when(universityApplicationMapper.selectList(any())).thenReturn(List.<LegacyCollegeApplication>of());
        when(admissionResultMapper.selectList(any())).thenReturn(List.<LegacyAdmissionResults>of());

        StudentRecommendationResponse response = service.recommend(1L, 10);

        assertThat(response.latestGrade()).isEqualTo(600.0);
        assertThat(response.majors()).extracting("id").containsExactly(11L, 10L);
        assertThat(response.majors()).extracting("id").doesNotContain(12L);
        assertThat(response.universities()).hasSize(1);
        assertThat(response.majors().get(0).reason()).contains("相似成绩学生");
    }

    @Test
    void usesStudentTableScoreBeforeLegacyResultGrade() {
        StudentProfile profile = profile(1L, "S001");
        profile.setScore(610.5);
        when(studentProfileService.findByAccountId(1L)).thenReturn(profile);
        when(resultMapper.selectList(any())).thenReturn(List.of(result("S001", 500)));
        when(studentMapper.selectList(any())).thenReturn(List.of(student(1L, "S001", 610.5)));
        when(majorMapper.selectList(any())).thenReturn(List.of(
            major(10L, "M10", "达线专业", "测试大学", "600", 10, 10),
            major(11L, "M11", "未达线专业", "测试大学", "620", 10, 10)
        ));
        when(universityMapper.selectList(any())).thenReturn(List.of(university(20L, "测试大学")));
        when(favoriteMapper.selectList(any())).thenReturn(List.<LegacyStoreup>of());
        when(majorApplicationMapper.selectList(any())).thenReturn(List.<LegacyProfessionalRegistration>of());
        when(universityApplicationMapper.selectList(any())).thenReturn(List.<LegacyCollegeApplication>of());
        when(admissionResultMapper.selectList(any())).thenReturn(List.<LegacyAdmissionResults>of());

        StudentRecommendationResponse response = service.recommend(1L, 10);

        assertThat(response.latestGrade()).isEqualTo(610.5);
        assertThat(response.majors()).extracting("id").containsExactly(10L);
    }

    private StudentProfile profile(Long id, String studentNo) {
        StudentProfile profile = new StudentProfile();
        profile.setId(id);
        profile.setStudentNo(studentNo);
        profile.setFullName("学生" + id);
        return profile;
    }

    private LegacyStudent student(Long id, String studentNo) {
        return student(id, studentNo, null);
    }

    private LegacyStudent student(Long id, String studentNo, Double score) {
        LegacyStudent student = new LegacyStudent();
        student.setId(id);
        student.setStudentID(studentNo);
        student.setScore(score);
        return student;
    }

    private LegacyResultsInformation result(String studentNo, Integer grade) {
        LegacyResultsInformation result = new LegacyResultsInformation();
        result.setStudentID(studentNo);
        result.setGrade(grade);
        return result;
    }

    private LegacyProfessionalInformation major(Long id, String code, String name, String universityName, String cutoff, Integer quota, Integer clicks) {
        LegacyProfessionalInformation major = new LegacyProfessionalInformation();
        major.setId(id);
        major.setMajorCode(code);
        major.setMajorName(name);
        major.setUniversityName(universityName);
        major.setCutOffScore(cutoff);
        major.setEnrollmentQuota(quota);
        major.setClicknum(clicks);
        return major;
    }

    private LegacyUniversityInformation university(Long id, String name) {
        LegacyUniversityInformation university = new LegacyUniversityInformation();
        university.setId(id);
        university.setUniversityName(name);
        university.setInstitutionType("公办");
        university.setClicknum(10);
        return university;
    }

    private LegacyStoreup favorite(Long userId, Long refId, String tableName, String name) {
        LegacyStoreup favorite = new LegacyStoreup();
        favorite.setUserid(userId);
        favorite.setRefid(refId);
        favorite.setTablename(tableName);
        favorite.setName(name);
        return favorite;
    }
}

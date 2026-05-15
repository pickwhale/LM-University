package com.university.backend.system.application;

import com.university.backend.legacy.LegacyStatusMapper;
import com.university.backend.legacy.infrastructure.LegacyAboutUsMapper;
import com.university.backend.legacy.infrastructure.LegacyCollegeApplicationMapper;
import com.university.backend.legacy.infrastructure.LegacyConfigMapper;
import com.university.backend.legacy.infrastructure.LegacyNewsMapper;
import com.university.backend.legacy.infrastructure.LegacyProfessionalInformationMapper;
import com.university.backend.legacy.infrastructure.LegacyProfessionalRegistrationMapper;
import com.university.backend.legacy.infrastructure.LegacyProvinceMapper;
import com.university.backend.legacy.infrastructure.LegacySystemIntroMapper;
import com.university.backend.legacy.infrastructure.LegacyStudentMapper;
import com.university.backend.legacy.infrastructure.LegacyUniversityInformationMapper;
import com.university.backend.system.dto.AdminDashboardSummary;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final LegacyProvinceMapper provinceMapper;
    private final LegacyStudentMapper studentMapper;
    private final LegacyUniversityInformationMapper universityMapper;
    private final LegacyProfessionalInformationMapper majorMapper;
    private final LegacyCollegeApplicationMapper universityApplicationMapper;
    private final LegacyProfessionalRegistrationMapper majorApplicationMapper;
    private final LegacyNewsMapper newsArticleMapper;
    private final LegacyAboutUsMapper aboutUsMapper;
    private final LegacySystemIntroMapper systemIntroMapper;
    private final LegacyConfigMapper appSettingMapper;

    public AdminDashboardService(
        LegacyProvinceMapper provinceMapper,
        LegacyStudentMapper studentMapper,
        LegacyUniversityInformationMapper universityMapper,
        LegacyProfessionalInformationMapper majorMapper,
        LegacyCollegeApplicationMapper universityApplicationMapper,
        LegacyProfessionalRegistrationMapper majorApplicationMapper,
        LegacyNewsMapper newsArticleMapper,
        LegacyAboutUsMapper aboutUsMapper,
        LegacySystemIntroMapper systemIntroMapper,
        LegacyConfigMapper appSettingMapper
    ) {
        this.provinceMapper = provinceMapper;
        this.studentMapper = studentMapper;
        this.universityMapper = universityMapper;
        this.majorMapper = majorMapper;
        this.universityApplicationMapper = universityApplicationMapper;
        this.majorApplicationMapper = majorApplicationMapper;
        this.newsArticleMapper = newsArticleMapper;
        this.aboutUsMapper = aboutUsMapper;
        this.systemIntroMapper = systemIntroMapper;
        this.appSettingMapper = appSettingMapper;
    }

    public AdminDashboardSummary summary() {
        return new AdminDashboardSummary(
            provinceMapper.selectCount(null),
            studentMapper.selectCount(null),
            universityMapper.selectCount(null),
            majorMapper.selectCount(null),
            universityApplicationMapper.selectCount(null),
            universityApplicationMapper.selectList(null).stream().filter(row -> "PENDING".equals(LegacyStatusMapper.toApiStatus(row.getSfsh()))).count(),
            majorApplicationMapper.selectCount(null),
            majorApplicationMapper.selectList(null).stream().filter(row -> "PENDING".equals(LegacyStatusMapper.toApiStatus(row.getSfsh()))).count(),
            newsArticleMapper.selectCount(null),
            aboutUsMapper.selectCount(null) + systemIntroMapper.selectCount(null),
            appSettingMapper.selectCount(null)
        );
    }
}

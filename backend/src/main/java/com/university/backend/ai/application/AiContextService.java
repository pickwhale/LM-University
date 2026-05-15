package com.university.backend.ai.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.admission.application.AdmissionService;
import com.university.backend.admission.domain.AcademicResult;
import com.university.backend.admission.domain.AdmissionResult;
import com.university.backend.ai.dto.AiSourceResponse;
import com.university.backend.application.application.MajorApplicationService;
import com.university.backend.application.application.UniversityApplicationService;
import com.university.backend.application.domain.MajorApplication;
import com.university.backend.application.domain.UniversityApplication;
import com.university.backend.content.application.NewsArticleService;
import com.university.backend.content.application.SitePageService;
import com.university.backend.content.domain.NewsArticle;
import com.university.backend.content.domain.SitePage;
import com.university.backend.interaction.application.FavoriteService;
import com.university.backend.interaction.domain.Favorite;
import com.university.backend.major.application.MajorService;
import com.university.backend.major.domain.Major;
import com.university.backend.recommendation.application.StudentRecommendationService;
import com.university.backend.recommendation.dto.RecommendationItem;
import com.university.backend.recommendation.dto.StudentRecommendationResponse;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import com.university.backend.university.application.UniversityService;
import com.university.backend.university.domain.University;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiContextService {

    private final StudentProfileService studentProfileService;
    private final UniversityService universityService;
    private final MajorService majorService;
    private final NewsArticleService newsArticleService;
    private final SitePageService sitePageService;
    private final UniversityApplicationService universityApplicationService;
    private final MajorApplicationService majorApplicationService;
    private final FavoriteService favoriteService;
    private final AdmissionService admissionService;
    private final StudentRecommendationService recommendationService;

    public AiContextService(
        StudentProfileService studentProfileService,
        UniversityService universityService,
        MajorService majorService,
        NewsArticleService newsArticleService,
        SitePageService sitePageService,
        UniversityApplicationService universityApplicationService,
        MajorApplicationService majorApplicationService,
        FavoriteService favoriteService,
        AdmissionService admissionService,
        StudentRecommendationService recommendationService
    ) {
        this.studentProfileService = studentProfileService;
        this.universityService = universityService;
        this.majorService = majorService;
        this.newsArticleService = newsArticleService;
        this.sitePageService = sitePageService;
        this.universityApplicationService = universityApplicationService;
        this.majorApplicationService = majorApplicationService;
        this.favoriteService = favoriteService;
        this.admissionService = admissionService;
        this.recommendationService = recommendationService;
    }

    public AiContextPackage build(Long accountId, String message) {
        StudentProfile student = studentProfileService.findByAccountId(accountId);
        List<AiSourceResponse> sources = new ArrayList<>();
        StringBuilder context = new StringBuilder();
        context.append("安全边界：以下学生信息只属于当前登录学生，不能推断或读取其他学生数据。\n");
        if (student != null) {
            context.append("\n[当前学生]\n")
                .append("姓名：").append(empty(student.getFullName())).append('\n')
                .append("学号：").append(empty(student.getStudentNo())).append('\n')
                .append("学院：").append(empty(student.getCollege())).append('\n')
                .append("联系电话：").append(empty(student.getContactNumber())).append('\n')
                .append("分数：").append(student.getScore() == null ? "暂无" : student.getScore()).append('\n');
        }

        Page<University> universityPage = universityService.pagePublic(1, 8, message, null);
        if (universityPage.getRecords().isEmpty()) {
            universityPage = universityService.pagePublic(1, 5, null, null);
        }
        context.append("\n[公开院校数据]\n");
        for (University university : universityPage.getRecords()) {
            context.append("- ").append(university.getName())
                .append("；类型：").append(empty(university.getInstitutionType()))
                .append("；层次：").append(empty(university.getKeyness()))
                .append("；电话：").append(empty(university.getPhone()))
                .append("；简介：").append(limit(university.getIntroduction(), 180))
                .append('\n');
            sources.add(new AiSourceResponse("university", String.valueOf(university.getId()), university.getName()));
        }

        Page<Major> majorPage = majorService.pagePublic(1, 8, message, null);
        if (majorPage.getRecords().isEmpty()) {
            majorPage = majorService.pagePublic(1, 5, null, null);
        }
        context.append("\n[公开专业数据]\n");
        for (Major major : majorPage.getRecords()) {
            context.append("- ").append(major.getName())
                .append("；代码：").append(empty(major.getCode()))
                .append("；学制：").append(empty(major.getDurationOfStudy()))
                .append("；分数线：").append(empty(major.getCutOffScore()))
                .append("；计划：").append(major.getEnrollmentQuota() == null ? "暂无" : major.getEnrollmentQuota())
                .append("；培养方案：").append(limit(major.getCurriculum(), 160))
                .append('\n');
            sources.add(new AiSourceResponse("major", String.valueOf(major.getId()), major.getName()));
        }

        Page<NewsArticle> newsPage = newsArticleService.page(1, 5, message);
        if (newsPage.getRecords().isEmpty()) {
            newsPage = newsArticleService.page(1, 5, null);
        }
        context.append("\n[最近资讯公告]\n");
        for (NewsArticle article : newsPage.getRecords()) {
            context.append("- ").append(article.getTitle())
                .append("；摘要：").append(limit(article.getIntroduction(), 180))
                .append('\n');
            sources.add(new AiSourceResponse("news", String.valueOf(article.getId()), article.getTitle()));
        }

        context.append("\n[静态页面公告]\n");
        for (SitePage page : sitePageService.listAll()) {
            context.append("- ").append(page.getTitle())
                .append("；").append(limit(page.getSubtitle() + " " + stripHtml(page.getContent()), 180))
                .append('\n');
            sources.add(new AiSourceResponse("page", String.valueOf(page.getId()), page.getTitle()));
        }

        context.append("\n[当前学生报名记录]\n");
        for (UniversityApplication item : universityApplicationService.pageOwn(accountId, 1, 10).getRecords()) {
            context.append("- 院校报名：").append(empty(item.getUniversityNameSnapshot()))
                .append("；状态：").append(empty(item.getStatus()))
                .append("；审核备注：").append(empty(item.getReviewComment()))
                .append('\n');
        }
        for (MajorApplication item : majorApplicationService.pageOwn(accountId, 1, 10).getRecords()) {
            context.append("- 专业报名：").append(empty(item.getMajorNameSnapshot()))
                .append("；院校：").append(empty(item.getUniversityNameSnapshot()))
                .append("；状态：").append(empty(item.getStatus()))
                .append("；审核备注：").append(empty(item.getReviewComment()))
                .append('\n');
        }

        context.append("\n[当前学生收藏]\n");
        for (Favorite item : favoriteService.pageOwn(accountId, 1, 10).getRecords()) {
            context.append("- ").append(empty(item.getTargetType()))
                .append("：").append(empty(item.getName()))
                .append("；备注：").append(empty(item.getRemark()))
                .append('\n');
        }

        context.append("\n[当前学生录取与成绩]\n");
        for (AdmissionResult item : admissionService.pageOwnAdmission(accountId, 1, 10).getRecords()) {
            context.append("- 录取结果：").append(empty(item.getResultStatus()))
                .append("；反馈：").append(empty(item.getFeedback()))
                .append('\n');
        }
        for (AcademicResult item : admissionService.pageOwnAcademic(accountId, 1, 10).getRecords()) {
            context.append("- 成绩报告：").append(empty(item.getReportNo()))
                .append("；成绩：").append(item.getGrade() == null ? "暂无" : item.getGrade())
                .append("；评价：").append(empty(item.getGradeEvaluation()))
                .append("；内容：").append(limit(item.getReportContent(), 160))
                .append('\n');
        }

        if (student != null && student.getScore() != null) {
            context.append("\n[基于当前学生分数的智能推荐]\n");
            try {
                StudentRecommendationResponse recommendations = recommendationService.recommend(accountId, 10);
                if (recommendations.latestGrade() != null) {
                    context.append("学生当前分数：").append(recommendations.latestGrade()).append(" 分\n\n");
                    
                    if (!recommendations.universities().isEmpty()) {
                        context.append("【推荐院校】\n");
                        for (int i = 0; i < recommendations.universities().size(); i++) {
                            RecommendationItem uni = recommendations.universities().get(i);
                            context.append("院校").append(i + 1).append("：").append(uni.name()).append("\n");
                            context.append("  推荐类型：").append(empty(uni.recommendationType())).append("\n");
                            context.append("  院校类型：").append(empty(uni.institutionType())).append("\n");
                            context.append("  所在省份：").append(empty(uni.province())).append("\n");
                            context.append("  录取分数线：").append(uni.cutOffScore() == null ? "暂无" : uni.cutOffScore() + "分").append("\n");
                            context.append("  分数优势：高出").append(uni.margin() == null ? "暂无" : String.format("%.1f", uni.margin())).append("分\n");
                            context.append("  推荐理由：").append(empty(uni.reason())).append("\n\n");
                            sources.add(new AiSourceResponse("university", String.valueOf(uni.id()), uni.name()));
                        }
                    }
                    
                    if (!recommendations.majors().isEmpty()) {
                        context.append("【推荐专业】\n");
                        for (int i = 0; i < recommendations.majors().size(); i++) {
                            RecommendationItem major = recommendations.majors().get(i);
                            context.append("专业").append(i + 1).append("：").append(major.name()).append("\n");
                            context.append("  所属院校：").append(empty(major.universityName())).append("\n");
                            context.append("  专业代码：").append(empty(major.majorCode())).append("\n");
                            context.append("  录取分数线：").append(major.cutOffScore() == null ? "暂无" : major.cutOffScore() + "分").append("\n");
                            context.append("  分数优势：高出").append(major.margin() == null ? "暂无" : String.format("%.1f", major.margin())).append("分\n");
                            context.append("  招生计划：").append(major.enrollmentQuota() == null ? "暂无" : major.enrollmentQuota() + "人").append("\n");
                            context.append("  推荐理由：").append(empty(major.reason())).append("\n\n");
                            sources.add(new AiSourceResponse("major", String.valueOf(major.id()), major.name()));
                        }
                    }
                    
                    if (recommendations.universities().isEmpty() && recommendations.majors().isEmpty()) {
                        context.append("【推荐结果】\n");
                        context.append("暂无匹配到适合该分数（").append(recommendations.latestGrade()).append("分）的院校或专业。\n\n");
                    }
                }
            } catch (Exception e) {
                context.append("【推荐系统状态】\n");
                context.append("推荐系统暂时不可用：").append(e.getMessage()).append("\n\n");
            }
        }

        return new AiContextPackage(context.toString(), sources);
    }

    private String empty(String value) {
        return StringUtils.hasText(value) ? stripHtml(value).trim() : "暂无";
    }

    private String limit(String value, int maxLength) {
        String text = empty(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private String stripHtml(String value) {
        return value == null ? "" : value.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}

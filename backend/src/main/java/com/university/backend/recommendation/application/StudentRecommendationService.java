package com.university.backend.recommendation.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.backend.common.error.ApiException;
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
import com.university.backend.recommendation.dto.RecommendationItem;
import com.university.backend.recommendation.dto.StudentRecommendationResponse;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentRecommendationService {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final double FAVORITE_WEIGHT = 1.0;
    private static final double APPLICATION_WEIGHT = 2.0;
    private static final double ADMISSION_WEIGHT = 3.0;

    private final StudentProfileService studentProfileService;
    private final LegacyStudentMapper studentMapper;
    private final LegacyResultsInformationMapper resultMapper;
    private final LegacyProfessionalInformationMapper majorMapper;
    private final LegacyUniversityInformationMapper universityMapper;
    private final LegacyStoreupMapper favoriteMapper;
    private final LegacyCollegeApplicationMapper universityApplicationMapper;
    private final LegacyProfessionalRegistrationMapper majorApplicationMapper;
    private final LegacyAdmissionResultsMapper admissionResultMapper;

    public StudentRecommendationService(
        StudentProfileService studentProfileService,
        LegacyStudentMapper studentMapper,
        LegacyResultsInformationMapper resultMapper,
        LegacyProfessionalInformationMapper majorMapper,
        LegacyUniversityInformationMapper universityMapper,
        LegacyStoreupMapper favoriteMapper,
        LegacyCollegeApplicationMapper universityApplicationMapper,
        LegacyProfessionalRegistrationMapper majorApplicationMapper,
        LegacyAdmissionResultsMapper admissionResultMapper
    ) {
        this.studentProfileService = studentProfileService;
        this.studentMapper = studentMapper;
        this.resultMapper = resultMapper;
        this.majorMapper = majorMapper;
        this.universityMapper = universityMapper;
        this.favoriteMapper = favoriteMapper;
        this.universityApplicationMapper = universityApplicationMapper;
        this.majorApplicationMapper = majorApplicationMapper;
        this.admissionResultMapper = admissionResultMapper;
    }

    public StudentRecommendationResponse recommend(Long accountId, int limit) {
        StudentProfile student = requireStudent(accountId);
        int safeLimit = Math.max(1, Math.min(limit, 50));

        List<LegacyResultsInformation> allResults = resultMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, LegacyResultsInformation> latestResults = latestResultsByStudentNo(allResults);
        List<LegacyStudent> allStudents = Optional.ofNullable(studentMapper.selectList(new LambdaQueryWrapper<>())).orElse(List.of());
        Map<String, Double> studentScores = studentScoresByStudentNo(allStudents, latestResults);
        if (student.getScore() != null) {
            studentScores.put(student.getStudentNo(), student.getScore());
        }
        Double latestGrade = studentScores.get(student.getStudentNo());
        if (latestGrade == null) {
            return new StudentRecommendationResponse(
                null,
                "暂无成绩，无法生成推荐",
                List.of(),
                List.of()
            );
        }

        List<LegacyProfessionalInformation> allMajors = majorMapper.selectList(new LambdaQueryWrapper<>());
        List<LegacyUniversityInformation> allUniversities = universityMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, LegacyUniversityInformation> universityByName = mapUniversitiesByName(allUniversities);

        Map<Long, String> studentNoById = studentNoById(allStudents);
        SimilarityContext similarity = buildSimilarityContext(student, latestGrade, studentScores, studentNoById);
        InteractionScores interactionScores = buildInteractionScores(similarity, studentNoById);

        List<MajorCandidate> majorCandidates = buildMajorCandidates(
            latestGrade,
            allMajors,
            universityByName,
            interactionScores.majorScores()
        );
        List<RecommendationItem> majorItems = rankMajorCandidates(majorCandidates, safeLimit);
        List<RecommendationItem> universityItems = rankUniversityCandidates(
            latestGrade,
            majorCandidates,
            universityByName,
            interactionScores.universityScores(),
            safeLimit
        );

        String message = majorItems.isEmpty() && universityItems.isEmpty()
            ? "当前成绩未匹配到可推荐的院校或专业"
            : "已根据最新成绩生成推荐";
        return new StudentRecommendationResponse(latestGrade, message, universityItems, majorItems);
    }

    private StudentProfile requireStudent(Long accountId) {
        StudentProfile student = studentProfileService.findByAccountId(accountId);
        if (student == null) {
            throw ApiException.forbidden("Student profile not found");
        }
        return student;
    }

    private Map<String, LegacyResultsInformation> latestResultsByStudentNo(List<LegacyResultsInformation> results) {
        Map<String, LegacyResultsInformation> latest = new HashMap<>();
        for (LegacyResultsInformation result : results) {
            if (!StringUtils.hasText(result.getStudentID()) || result.getGrade() == null) {
                continue;
            }
            latest.merge(result.getStudentID(), result, this::newerResult);
        }
        return latest;
    }

    private LegacyResultsInformation newerResult(LegacyResultsInformation left, LegacyResultsInformation right) {
        LocalDateTime leftTime = left.getAddtime();
        LocalDateTime rightTime = right.getAddtime();
        if (leftTime == null) {
            return rightTime == null && left.getId() != null && right.getId() != null && right.getId() > left.getId()
                ? right
                : left;
        }
        if (rightTime == null) {
            return left;
        }
        return rightTime.isAfter(leftTime) ? right : left;
    }

    private Map<String, Double> studentScoresByStudentNo(
        List<LegacyStudent> students,
        Map<String, LegacyResultsInformation> latestResults
    ) {
        Map<String, Double> scores = new HashMap<>();
        for (Map.Entry<String, LegacyResultsInformation> entry : latestResults.entrySet()) {
            Integer grade = entry.getValue().getGrade();
            if (grade != null) {
                scores.put(entry.getKey(), grade.doubleValue());
            }
        }
        for (LegacyStudent student : students) {
            if (StringUtils.hasText(student.getStudentID()) && student.getScore() != null) {
                scores.put(student.getStudentID(), student.getScore());
            }
        }
        return scores;
    }

    private Map<String, LegacyUniversityInformation> mapUniversitiesByName(List<LegacyUniversityInformation> universities) {
        Map<String, LegacyUniversityInformation> result = new LinkedHashMap<>();
        for (LegacyUniversityInformation university : universities) {
            if (StringUtils.hasText(university.getUniversityName())) {
                result.putIfAbsent(university.getUniversityName(), university);
            }
        }
        return result;
    }

    private Map<Long, String> studentNoById(List<LegacyStudent> students) {
        Map<Long, String> result = new HashMap<>();
        for (LegacyStudent student : students) {
            if (student.getId() != null && StringUtils.hasText(student.getStudentID())) {
                result.put(student.getId(), student.getStudentID());
            }
        }
        return result;
    }

    private SimilarityContext buildSimilarityContext(
        StudentProfile currentStudent,
        double currentGrade,
        Map<String, Double> studentScores,
        Map<Long, String> studentNoById
    ) {
        Map<String, Double> byStudentNo = new HashMap<>();
        Map<Long, Double> byStudentId = new HashMap<>();
        for (Map.Entry<String, Double> entry : studentScores.entrySet()) {
            String studentNo = entry.getKey();
            if (Objects.equals(studentNo, currentStudent.getStudentNo())) {
                continue;
            }
            Double grade = entry.getValue();
            if (grade == null) {
                continue;
            }
            double similarity = 1.0 / (1.0 + Math.abs(currentGrade - grade) / 100.0);
            byStudentNo.put(studentNo, similarity);
        }
        for (Map.Entry<Long, String> entry : studentNoById.entrySet()) {
            Double similarity = byStudentNo.get(entry.getValue());
            if (similarity != null) {
                byStudentId.put(entry.getKey(), similarity);
            }
        }
        return new SimilarityContext(byStudentNo, byStudentId);
    }

    private InteractionScores buildInteractionScores(SimilarityContext similarity, Map<Long, String> studentNoById) {
        Map<Long, Double> majorScores = new HashMap<>();
        Map<String, Double> universityScores = new HashMap<>();

        for (LegacyStoreup favorite : favoriteMapper.selectList(new LambdaQueryWrapper<>())) {
            Double similarScore = similarity.byStudentId().get(favorite.getUserid());
            if (similarScore == null || favorite.getRefid() == null) {
                continue;
            }
            if (isMajorType(favorite.getTablename())) {
                addScore(majorScores, favorite.getRefid(), similarScore * FAVORITE_WEIGHT);
            } else if (isUniversityType(favorite.getTablename())) {
                addScore(universityScores, favorite.getName(), similarScore * FAVORITE_WEIGHT);
            }
        }

        for (LegacyProfessionalRegistration application : majorApplicationMapper.selectList(new LambdaQueryWrapper<>())) {
            Double similarScore = resolveSimilarity(application.getStudentID(), application.getCrossuserid(), similarity, studentNoById);
            if (similarScore == null || application.getCrossrefid() == null) {
                continue;
            }
            addScore(majorScores, application.getCrossrefid(), similarScore * reviewWeight(application.getSfsh(), APPLICATION_WEIGHT));
        }

        for (LegacyCollegeApplication application : universityApplicationMapper.selectList(new LambdaQueryWrapper<>())) {
            Double similarScore = resolveSimilarity(application.getStudentID(), application.getCrossuserid(), similarity, studentNoById);
            if (similarScore == null) {
                continue;
            }
            addScore(universityScores, application.getUniversityName(), similarScore * reviewWeight(application.getSfsh(), APPLICATION_WEIGHT));
        }

        for (LegacyAdmissionResults admission : admissionResultMapper.selectList(new LambdaQueryWrapper<>())) {
            Double similarScore = similarity.byStudentNo().get(admission.getStudentID());
            if (similarScore == null) {
                continue;
            }
            addScore(universityScores, admission.getUniversityName(), similarScore * admissionWeight(admission.getAdmissionResults()));
        }

        return new InteractionScores(majorScores, universityScores);
    }

    private Double resolveSimilarity(String studentNo, Long studentId, SimilarityContext similarity, Map<Long, String> studentNoById) {
        if (StringUtils.hasText(studentNo)) {
            return similarity.byStudentNo().get(studentNo);
        }
        if (studentId != null) {
            Double byId = similarity.byStudentId().get(studentId);
            if (byId != null) {
                return byId;
            }
            return similarity.byStudentNo().get(studentNoById.get(studentId));
        }
        return null;
    }

    private boolean isMajorType(String type) {
        return type != null && List.of("major", "professionalinformation").contains(type.toLowerCase(Locale.ROOT));
    }

    private boolean isUniversityType(String type) {
        return type != null && List.of("university", "universityinformation").contains(type.toLowerCase(Locale.ROOT));
    }

    private double reviewWeight(String status, double baseWeight) {
        if (!StringUtils.hasText(status)) {
            return baseWeight;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("拒") || normalized.contains("reject") || normalized.equals("no")) {
            return baseWeight * 0.25;
        }
        if (normalized.contains("通") || normalized.contains("approved") || normalized.equals("yes")) {
            return baseWeight * 1.25;
        }
        return baseWeight;
    }

    private double admissionWeight(String result) {
        if (!StringUtils.hasText(result)) {
            return ADMISSION_WEIGHT;
        }
        String normalized = result.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("拒") || normalized.contains("fail") || normalized.contains("reject")) {
            return ADMISSION_WEIGHT * 0.25;
        }
        return ADMISSION_WEIGHT;
    }

    private void addScore(Map<Long, Double> scores, Long key, double value) {
        if (key != null) {
            scores.merge(key, value, Double::sum);
        }
    }

    private void addScore(Map<String, Double> scores, String key, double value) {
        if (StringUtils.hasText(key)) {
            scores.merge(key, value, Double::sum);
        }
    }

    private List<MajorCandidate> buildMajorCandidates(
        double latestGrade,
        List<LegacyProfessionalInformation> majors,
        Map<String, LegacyUniversityInformation> universityByName,
        Map<Long, Double> majorInteractionScores
    ) {
        List<MajorCandidate> candidates = new ArrayList<>();
        int maxClick = majors.stream().map(LegacyProfessionalInformation::getClicknum).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
        int maxQuota = majors.stream().map(LegacyProfessionalInformation::getEnrollmentQuota).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
        double maxCfScore = Math.max(1.0, majorInteractionScores.values().stream().max(Double::compareTo).orElse(0.0));

        for (LegacyProfessionalInformation major : majors) {
            Optional<Integer> cutoff = parseCutOffScore(major.getCutOffScore());
            if (cutoff.isEmpty() || cutoff.get() > latestGrade) {
                continue;
            }
            double margin = latestGrade - cutoff.get();
            double marginScore = Math.min(margin / 80.0, 1.0);
            double cfScore = majorInteractionScores.getOrDefault(major.getId(), 0.0) / maxCfScore;
            double popularityScore = normalize(major.getClicknum(), maxClick);
            double quotaScore = normalize(major.getEnrollmentQuota(), maxQuota);
            double finalScore = 0.55 * marginScore + 0.30 * cfScore + 0.10 * popularityScore + 0.05 * quotaScore;
            LegacyUniversityInformation university = universityByName.get(major.getUniversityName());
            candidates.add(new MajorCandidate(major, university, cutoff.get(), margin, cfScore, finalScore));
        }
        
        candidates.sort(Comparator.comparingDouble(MajorCandidate::margin).thenComparingDouble(MajorCandidate::score));
        return candidates;
    }

    private List<RecommendationItem> rankMajorCandidates(List<MajorCandidate> candidates, int limit) {
        return candidates.stream()
            .limit(limit)
            .map(this::toMajorItem)
            .toList();
    }

    private RecommendationItem toMajorItem(MajorCandidate candidate) {
        LegacyProfessionalInformation major = candidate.major();
        String type = recommendationType(candidate.margin());
        String reason = String.format(
            Locale.ROOT,
            "你的最新成绩为 %.1f 分，已达到该专业 %d 分的分数线，高出 %.1f 分，属于%s选择%s。",
            candidate.cutoff() + candidate.margin(),
            candidate.cutoff(),
            candidate.margin(),
            type,
            candidate.cfScore() > 0 ? "；相似成绩学生也关注或报名过该专业" : ""
        );
        return new RecommendationItem(
            major.getId(),
            "MAJOR",
            major.getMajorName(),
            major.getUniversityName(),
            major.getProvince(),
            candidate.university() == null ? null : candidate.university().getInstitutionType(),
            major.getMajorCode(),
            major.getCover(),
            candidate.cutoff() + candidate.margin(),
            candidate.cutoff(),
            candidate.margin(),
            type,
            reason,
            round(candidate.score()),
            major.getClicknum() == null ? 0 : major.getClicknum(),
            major.getEnrollmentQuota()
        );
    }

    private List<RecommendationItem> rankUniversityCandidates(
        double latestGrade,
        List<MajorCandidate> majorCandidates,
        Map<String, LegacyUniversityInformation> universityByName,
        Map<String, Double> universityInteractionScores,
        int limit
    ) {
        Map<String, List<MajorCandidate>> majorsByUniversity = new LinkedHashMap<>();
        for (MajorCandidate candidate : majorCandidates) {
            if (StringUtils.hasText(candidate.major().getUniversityName())) {
                majorsByUniversity.computeIfAbsent(candidate.major().getUniversityName(), key -> new ArrayList<>()).add(candidate);
            }
        }

        int maxClick = universityByName.values().stream().map(LegacyUniversityInformation::getClicknum).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
        int maxEligibleMajors = majorsByUniversity.values().stream().map(List::size).max(Integer::compareTo).orElse(1);
        double maxCfScore = Math.max(1.0, universityInteractionScores.values().stream().max(Double::compareTo).orElse(0.0));

        List<RecommendationItem> items = new ArrayList<>(majorsByUniversity.entrySet().stream()
            .map(entry -> toUniversityItem(
                latestGrade,
                entry.getKey(),
                entry.getValue(),
                universityByName.get(entry.getKey()),
                universityInteractionScores.getOrDefault(entry.getKey(), 0.0),
                maxCfScore,
                maxClick,
                maxEligibleMajors
            ))
            .filter(Objects::nonNull)
            .toList());
        
        items.sort(Comparator.comparingDouble((RecommendationItem item) -> {
            Double margin = item.margin();
            return margin == null ? Double.MAX_VALUE : margin;
        }).thenComparingDouble(RecommendationItem::score));
        
        return items.stream().limit(limit).toList();
    }

    private RecommendationItem toUniversityItem(
        double latestGrade,
        String universityName,
        List<MajorCandidate> eligibleMajors,
        LegacyUniversityInformation university,
        double cfRawScore,
        double maxCfScore,
        int maxClick,
        int maxEligibleMajors
    ) {
        if (university == null || eligibleMajors.isEmpty()) {
            return null;
        }
        
        eligibleMajors.sort(Comparator.comparingDouble(MajorCandidate::margin));
        MajorCandidate representativeMajor = eligibleMajors.get(0);
        
        double marginScore = Math.min(representativeMajor.margin() / 80.0, 1.0);
        double cfScore = cfRawScore / maxCfScore;
        double popularityScore = normalize(university.getClicknum(), maxClick);
        double eligibleMajorScore = maxEligibleMajors <= 0 ? 0.0 : eligibleMajors.size() / (double) maxEligibleMajors;
        double score = 0.55 * marginScore + 0.30 * cfScore + 0.10 * popularityScore + 0.05 * eligibleMajorScore;
        String type = recommendationType(representativeMajor.margin());
        String reason = String.format(
            Locale.ROOT,
            "你的最新成绩为 %.1f 分，当前可覆盖该校 %d 个专业，最高可高出分数线 %.1f 分，属于%s院校%s。",
            latestGrade,
            eligibleMajors.size(),
            eligibleMajors.stream().mapToDouble(MajorCandidate::margin).max().orElse(0),
            type,
            cfScore > 0 ? "；相似成绩学生对该校有收藏、报名或录取记录" : ""
        );
        return new RecommendationItem(
            university.getId(),
            "UNIVERSITY",
            universityName,
            universityName,
            university.getProvince(),
            university.getInstitutionType(),
            null,
            university.getUniversityImage(),
            latestGrade,
            representativeMajor.cutoff(),
            representativeMajor.margin(),
            type,
            reason,
            round(score),
            university.getClicknum() == null ? 0 : university.getClicknum(),
            null
        );
    }

    private Optional<Integer> parseCutOffScore(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        Matcher matcher = NUMBER_PATTERN.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(matcher.group()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private String recommendationType(double margin) {
        if (margin < 10) {
            return "冲刺";
        }
        if (margin < 30) {
            return "稳妥";
        }
        return "保底";
    }

    private double normalize(Integer value, int max) {
        if (value == null || value <= 0 || max <= 0) {
            return 0.0;
        }
        return Math.log1p(value) / Math.log1p(max);
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record SimilarityContext(Map<String, Double> byStudentNo, Map<Long, Double> byStudentId) {
    }

    private record InteractionScores(Map<Long, Double> majorScores, Map<String, Double> universityScores) {
    }

    private record MajorCandidate(
        LegacyProfessionalInformation major,
        LegacyUniversityInformation university,
        int cutoff,
        double margin,
        double cfScore,
        double score
    ) {
    }
}

package com.university.backend.major.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.university.backend.common.domain.AuditableEntity;
@TableName("major")
public class Major extends AuditableEntity {

    private String code;

    @TableField("university_id")
    private Long universityId;

    private String name;

    @TableField("cover_path")
    private String coverPath;

    @TableField("duration_of_study")
    private String durationOfStudy;

    @TableField("cut_off_score")
    private String cutOffScore;

    @TableField("enrollment_quota")
    private Integer enrollmentQuota;

    private String curriculum;

    @TableField("click_count")
    private Integer clickCount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getDurationOfStudy() {
        return durationOfStudy;
    }

    public void setDurationOfStudy(String durationOfStudy) {
        this.durationOfStudy = durationOfStudy;
    }

    public String getCutOffScore() {
        return cutOffScore;
    }

    public void setCutOffScore(String cutOffScore) {
        this.cutOffScore = cutOffScore;
    }

    public Integer getEnrollmentQuota() {
        return enrollmentQuota;
    }

    public void setEnrollmentQuota(Integer enrollmentQuota) {
        this.enrollmentQuota = enrollmentQuota;
    }

    public String getCurriculum() {
        return curriculum;
    }

    public void setCurriculum(String curriculum) {
        this.curriculum = curriculum;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }
}

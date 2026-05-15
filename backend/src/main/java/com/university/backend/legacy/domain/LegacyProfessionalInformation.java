package com.university.backend.legacy.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("professionalinformation")
public class LegacyProfessionalInformation {

    @TableId
    private Long id;

    private LocalDateTime addtime;
    private String majorCode;
    private String majorName;
    private String cover;
    private String durationOfStudy;
    private String cutOffScore;
    private Integer enrollmentQuota;
    private String curriculum;
    private String universityName;
    private String province;
    private LocalDateTime clicktime;
    private Integer clicknum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getAddtime() {
        return addtime;
    }

    public void setAddtime(LocalDateTime addtime) {
        this.addtime = addtime;
    }

    public String getMajorCode() {
        return majorCode;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
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

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public LocalDateTime getClicktime() {
        return clicktime;
    }

    public void setClicktime(LocalDateTime clicktime) {
        this.clicktime = clicktime;
    }

    public Integer getClicknum() {
        return clicknum;
    }

    public void setClicknum(Integer clicknum) {
        this.clicknum = clicknum;
    }
}

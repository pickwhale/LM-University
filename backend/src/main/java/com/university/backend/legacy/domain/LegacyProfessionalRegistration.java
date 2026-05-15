package com.university.backend.legacy.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("professionalregistration")
public class LegacyProfessionalRegistration {

    @TableId
    private Long id;

    private LocalDateTime addtime;
    private String majorCode;
    private String majorName;
    private String durationOfStudy;
    private String cover;
    private String enrollmentQuota;
    private String cutOffScore;
    private String curriculum;
    private LocalDateTime applicationTime;
    private String universityName;
    private String province;
    private String studentID;
    private String studentName;
    private String contactNumber;
    private Long crossuserid;
    private Long crossrefid;
    private String sfsh;
    private String shhf;

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

    public String getDurationOfStudy() {
        return durationOfStudy;
    }

    public void setDurationOfStudy(String durationOfStudy) {
        this.durationOfStudy = durationOfStudy;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getEnrollmentQuota() {
        return enrollmentQuota;
    }

    public void setEnrollmentQuota(String enrollmentQuota) {
        this.enrollmentQuota = enrollmentQuota;
    }

    public String getCutOffScore() {
        return cutOffScore;
    }

    public void setCutOffScore(String cutOffScore) {
        this.cutOffScore = cutOffScore;
    }

    public String getCurriculum() {
        return curriculum;
    }

    public void setCurriculum(String curriculum) {
        this.curriculum = curriculum;
    }

    public LocalDateTime getApplicationTime() {
        return applicationTime;
    }

    public void setApplicationTime(LocalDateTime applicationTime) {
        this.applicationTime = applicationTime;
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

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Long getCrossuserid() {
        return crossuserid;
    }

    public void setCrossuserid(Long crossuserid) {
        this.crossuserid = crossuserid;
    }

    public Long getCrossrefid() {
        return crossrefid;
    }

    public void setCrossrefid(Long crossrefid) {
        this.crossrefid = crossrefid;
    }

    public String getSfsh() {
        return sfsh;
    }

    public void setSfsh(String sfsh) {
        this.sfsh = sfsh;
    }

    public String getShhf() {
        return shhf;
    }

    public void setShhf(String shhf) {
        this.shhf = shhf;
    }
}

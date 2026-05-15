package com.university.backend.application.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.university.backend.common.domain.AuditableEntity;
import java.time.LocalDateTime;

@TableName("university_application")
public class UniversityApplication extends AuditableEntity {

    @TableField("registration_no")
    private String registrationNo;

    @TableField("student_id")
    private Long studentId;

    @TableField("university_id")
    private Long universityId;

    private String status;

    @TableField("review_comment")
    private String reviewComment;

    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    @TableField("student_no_snapshot")
    private String studentNoSnapshot;

    @TableField("student_name_snapshot")
    private String studentNameSnapshot;

    @TableField("contact_number_snapshot")
    private String contactNumberSnapshot;

    @TableField("college_snapshot")
    private String collegeSnapshot;

    @TableField("university_name_snapshot")
    private String universityNameSnapshot;

    @TableField("institution_type_snapshot")
    private String institutionTypeSnapshot;

    @TableField("province_name_snapshot")
    private String provinceNameSnapshot;

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getStudentNoSnapshot() {
        return studentNoSnapshot;
    }

    public void setStudentNoSnapshot(String studentNoSnapshot) {
        this.studentNoSnapshot = studentNoSnapshot;
    }

    public String getStudentNameSnapshot() {
        return studentNameSnapshot;
    }

    public void setStudentNameSnapshot(String studentNameSnapshot) {
        this.studentNameSnapshot = studentNameSnapshot;
    }

    public String getContactNumberSnapshot() {
        return contactNumberSnapshot;
    }

    public void setContactNumberSnapshot(String contactNumberSnapshot) {
        this.contactNumberSnapshot = contactNumberSnapshot;
    }

    public String getCollegeSnapshot() {
        return collegeSnapshot;
    }

    public void setCollegeSnapshot(String collegeSnapshot) {
        this.collegeSnapshot = collegeSnapshot;
    }

    public String getUniversityNameSnapshot() {
        return universityNameSnapshot;
    }

    public void setUniversityNameSnapshot(String universityNameSnapshot) {
        this.universityNameSnapshot = universityNameSnapshot;
    }

    public String getInstitutionTypeSnapshot() {
        return institutionTypeSnapshot;
    }

    public void setInstitutionTypeSnapshot(String institutionTypeSnapshot) {
        this.institutionTypeSnapshot = institutionTypeSnapshot;
    }

    public String getProvinceNameSnapshot() {
        return provinceNameSnapshot;
    }

    public void setProvinceNameSnapshot(String provinceNameSnapshot) {
        this.provinceNameSnapshot = provinceNameSnapshot;
    }
}

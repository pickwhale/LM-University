package com.university.backend.application.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.university.backend.common.domain.AuditableEntity;
import java.time.LocalDateTime;

@TableName("major_application")
public class MajorApplication extends AuditableEntity {

    @TableField("student_id")
    private Long studentId;

    @TableField("major_id")
    private Long majorId;

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

    @TableField("major_code_snapshot")
    private String majorCodeSnapshot;

    @TableField("major_name_snapshot")
    private String majorNameSnapshot;

    @TableField("university_name_snapshot")
    private String universityNameSnapshot;

    @TableField("province_name_snapshot")
    private String provinceNameSnapshot;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
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

    public String getMajorCodeSnapshot() {
        return majorCodeSnapshot;
    }

    public void setMajorCodeSnapshot(String majorCodeSnapshot) {
        this.majorCodeSnapshot = majorCodeSnapshot;
    }

    public String getMajorNameSnapshot() {
        return majorNameSnapshot;
    }

    public void setMajorNameSnapshot(String majorNameSnapshot) {
        this.majorNameSnapshot = majorNameSnapshot;
    }

    public String getUniversityNameSnapshot() {
        return universityNameSnapshot;
    }

    public void setUniversityNameSnapshot(String universityNameSnapshot) {
        this.universityNameSnapshot = universityNameSnapshot;
    }

    public String getProvinceNameSnapshot() {
        return provinceNameSnapshot;
    }

    public void setProvinceNameSnapshot(String provinceNameSnapshot) {
        this.provinceNameSnapshot = provinceNameSnapshot;
    }
}

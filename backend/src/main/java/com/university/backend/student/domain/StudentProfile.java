package com.university.backend.student.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.university.backend.common.domain.AuditableEntity;

public class StudentProfile extends AuditableEntity {

    @TableField("account_id")
    private Long accountId;

    @TableField("student_no")
    private String studentNo;

    @TableField("full_name")
    private String fullName;

    @TableField("avatar_path")
    private String avatarPath;

    private String gender;

    private String college;

    @TableField("contact_number")
    private String contactNumber;
    private Double score;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}

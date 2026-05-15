package com.university.backend.legacy.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("resultsinformation")
public class LegacyResultsInformation {

    @TableId
    private Long id;

    private LocalDateTime addtime;
    private String reportNumber;
    private String reportContent;
    private Integer grade;
    @TableField("gradeEvaluation")
    private String gradeEvaluation;
    private LocalDate entryTime;
    private String studentID;
    private String studentName;
    private String contactNumber;

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

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getGradepingding() {
        return gradeEvaluation;
    }

    public void setGradepingding(String gradepingding) {
        this.gradeEvaluation = gradepingding;
    }

    public String getGradeEvaluation() {
        return gradeEvaluation;
    }

    public void setGradeEvaluation(String gradeEvaluation) {
        this.gradeEvaluation = gradeEvaluation;
    }

    public LocalDate getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDate entryTime) {
        this.entryTime = entryTime;
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
}

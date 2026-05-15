package com.university.backend.admission.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.university.backend.common.domain.AuditableEntity;
import java.time.LocalDate;

@TableName("academic_result")
public class AcademicResult extends AuditableEntity {

    @TableField("student_id")
    private Long studentId;

    @TableField("report_no")
    private String reportNo;

    @TableField("report_content")
    private String reportContent;

    private Integer grade;

    @TableField("grade_evaluation")
    private String gradeEvaluation;

    @TableField("entered_at")
    private LocalDate enteredAt;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getReportNo() {
        return reportNo;
    }

    public void setReportNo(String reportNo) {
        this.reportNo = reportNo;
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

    public String getGradeEvaluation() {
        return gradeEvaluation;
    }

    public void setGradeEvaluation(String gradeEvaluation) {
        this.gradeEvaluation = gradeEvaluation;
    }

    public LocalDate getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(LocalDate enteredAt) {
        this.enteredAt = enteredAt;
    }
}

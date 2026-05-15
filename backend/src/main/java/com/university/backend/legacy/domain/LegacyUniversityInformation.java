package com.university.backend.legacy.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("universityinformation")
public class LegacyUniversityInformation {

    @TableId
    private Long id;

    private LocalDateTime addtime;
    private String universityName;
    private String universityWebsite;
    private String universityImage;
    private String province;
    private String institutionType;
    private String keyness;
    private String universityIntroduction;
    private String majorOffered;
    private String phone;
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

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getUniversityWebsite() {
        return universityWebsite;
    }

    public void setUniversityWebsite(String universityWebsite) {
        this.universityWebsite = universityWebsite;
    }

    public String getUniversityImage() {
        return universityImage;
    }

    public void setUniversityImage(String universityImage) {
        this.universityImage = universityImage;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(String institutionType) {
        this.institutionType = institutionType;
    }

    public String getKeyness() {
        return keyness;
    }

    public void setKeyness(String keyness) {
        this.keyness = keyness;
    }

    public String getUniversityIntroduction() {
        return universityIntroduction;
    }

    public void setUniversityIntroduction(String universityIntroduction) {
        this.universityIntroduction = universityIntroduction;
    }

    public String getMajorOffered() {
        return majorOffered;
    }

    public void setMajorOffered(String majorOffered) {
        this.majorOffered = majorOffered;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

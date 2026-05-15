package com.university.backend.content.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.university.backend.common.domain.AuditableEntity;
@TableName("site_page")
public class SitePage extends AuditableEntity {

    private String slug;

    private String title;

    private String subtitle;

    private String content;

    @TableField("picture1_path")
    private String picture1Path;

    @TableField("picture2_path")
    private String picture2Path;

    @TableField("picture3_path")
    private String picture3Path;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPicture1Path() {
        return picture1Path;
    }

    public void setPicture1Path(String picture1Path) {
        this.picture1Path = picture1Path;
    }

    public String getPicture2Path() {
        return picture2Path;
    }

    public void setPicture2Path(String picture2Path) {
        this.picture2Path = picture2Path;
    }

    public String getPicture3Path() {
        return picture3Path;
    }

    public void setPicture3Path(String picture3Path) {
        this.picture3Path = picture3Path;
    }
}

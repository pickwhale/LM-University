package com.university.backend.province.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.university.backend.common.domain.AuditableEntity;
@TableName("province")
public class Province extends AuditableEntity {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.university.backend.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public record PageResponse<T>(List<T> items, long total, long page, long size) {

    public static <T> PageResponse<T> from(IPage<T> page) {
        return new PageResponse<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}

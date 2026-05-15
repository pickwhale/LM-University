package com.university.backend.ai.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.backend.ai.domain.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
}

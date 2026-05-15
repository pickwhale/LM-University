package com.university.backend.content.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyConfig;
import com.university.backend.legacy.infrastructure.LegacyConfigMapper;
import com.university.backend.content.domain.AppSetting;
import com.university.backend.content.dto.AppSettingRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppSettingService {

    private final LegacyConfigMapper appSettingMapper;

    public AppSettingService(LegacyConfigMapper appSettingMapper) {
        this.appSettingMapper = appSettingMapper;
    }

    public List<AppSetting> listAll() {
        List<LegacyConfig> legacySettings = appSettingMapper.selectList(new LambdaQueryWrapper<LegacyConfig>().orderByAsc(LegacyConfig::getName));
        List<AppSetting> settings = new ArrayList<>(legacySettings.size());
        for (LegacyConfig setting : legacySettings) {
            if (setting.getName() != null && setting.getName().startsWith("ai.")) {
                continue;
            }
            settings.add(map(setting));
        }
        return settings;
    }

    public AppSetting create(AppSettingRequest request) {
        LegacyConfig setting = new LegacyConfig();
        setting.setName(request.settingKey());
        setting.setValue(request.settingValue());
        appSettingMapper.insert(setting);
        return map(setting);
    }

    public AppSetting update(Long id, AppSettingRequest request) {
        LegacyConfig setting = appSettingMapper.selectById(id);
        if (setting == null) {
            throw ApiException.notFound("Setting not found");
        }
        setting.setName(request.settingKey());
        setting.setValue(request.settingValue());
        appSettingMapper.updateById(setting);
        return map(setting);
    }

    public void delete(Long id) {
        if (appSettingMapper.deleteById(id) == 0) {
            throw ApiException.notFound("Setting not found");
        }
    }

    private AppSetting map(LegacyConfig legacy) {
        AppSetting setting = new AppSetting();
        setting.setId(legacy.getId());
        setting.setSettingKey(legacy.getName());
        setting.setSettingValue(legacy.getValue());
        return setting;
    }
}

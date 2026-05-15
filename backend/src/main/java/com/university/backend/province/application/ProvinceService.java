package com.university.backend.province.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyProvince;
import com.university.backend.legacy.infrastructure.LegacyProvinceMapper;
import com.university.backend.province.domain.Province;
import com.university.backend.province.dto.ProvinceRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProvinceService {

    private final LegacyProvinceMapper provinceMapper;

    public ProvinceService(LegacyProvinceMapper provinceMapper) {
        this.provinceMapper = provinceMapper;
    }

    public List<Province> listAll() {
        List<LegacyProvince> legacyRecords = provinceMapper.selectList(
            new LambdaQueryWrapper<LegacyProvince>().orderByAsc(LegacyProvince::getProvince)
        );
        List<Province> provinces = new ArrayList<>(legacyRecords.size());
        for (LegacyProvince record : legacyRecords) {
            provinces.add(map(record));
        }
        return provinces;
    }

    public Province create(ProvinceRequest request) {
        LegacyProvince province = new LegacyProvince();
        province.setProvince(request.name());
        provinceMapper.insert(province);
        return map(province);
    }

    public Province update(Long id, ProvinceRequest request) {
        LegacyProvince province = getLegacyRequired(id);
        province.setProvince(request.name());
        provinceMapper.updateById(province);
        return map(province);
    }

    public void delete(Long id) {
        if (provinceMapper.deleteById(id) == 0) {
            throw ApiException.notFound("Province not found");
        }
    }

    public Province getRequired(Long id) {
        return map(getLegacyRequired(id));
    }

    public LegacyProvince getLegacyRequired(Long id) {
        LegacyProvince province = provinceMapper.selectById(id);
        if (province == null) {
            throw ApiException.notFound("Province not found");
        }
        return province;
    }

    private Province map(LegacyProvince record) {
        Province province = new Province();
        province.setId(record.getId());
        province.setCreatedAt(record.getAddtime());
        province.setUpdatedAt(record.getAddtime());
        province.setName(record.getProvince());
        return province;
    }
}

package com.university.backend.interaction.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.common.error.ApiException;
import com.university.backend.interaction.domain.Favorite;
import com.university.backend.interaction.dto.FavoriteRequest;
import com.university.backend.legacy.domain.LegacyStoreup;
import com.university.backend.legacy.infrastructure.LegacyStoreupMapper;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService {

    private final LegacyStoreupMapper favoriteMapper;
    private final StudentProfileService studentProfileService;

    public FavoriteService(LegacyStoreupMapper favoriteMapper, StudentProfileService studentProfileService) {
        this.favoriteMapper = favoriteMapper;
        this.studentProfileService = studentProfileService;
    }

    public Favorite create(Long accountId, FavoriteRequest request) {
        StudentProfile student = requireStudent(accountId);

        LambdaQueryWrapper<LegacyStoreup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LegacyStoreup::getUserid, student.getId())
                .eq(LegacyStoreup::getTablename, request.targetType())
                .eq(LegacyStoreup::getRefid, request.targetId());

        LegacyStoreup existing = favoriteMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw ApiException.badRequest("Already favorited");
        }

        LegacyStoreup favorite = new LegacyStoreup();
        favorite.setUserid(student.getId());
        favorite.setTablename(request.targetType());
        favorite.setRefid(request.targetId());
        favorite.setName(request.name());
        favorite.setPicture(request.picturePath());
        favorite.setType("1");
        favorite.setInteltype(request.recommendationType());
        favorite.setRemark(request.remark());
        favoriteMapper.insert(favorite);
        return map(favorite);
    }

    public Page<Favorite> pageOwn(Long accountId, long page, long size) {
        StudentProfile student = requireStudent(accountId);
        return mapPage(favoriteMapper.selectPage(
            Page.of(page, size),
            new LambdaQueryWrapper<LegacyStoreup>().eq(LegacyStoreup::getUserid, student.getId()).orderByDesc(LegacyStoreup::getAddtime)
        ));
    }

    public void delete(Long accountId, Long id) {
        StudentProfile student = requireStudent(accountId);
        LegacyStoreup favorite = favoriteMapper.selectById(id);
        if (favorite == null || !favorite.getUserid().equals(student.getId())) {
            throw ApiException.notFound("Favorite not found");
        }
        favoriteMapper.deleteById(id);
    }

    public boolean isFavorited(Long accountId, String targetType, Long targetId) {
        StudentProfile student = requireStudent(accountId);
        LambdaQueryWrapper<LegacyStoreup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LegacyStoreup::getUserid, student.getId())
                    .eq(LegacyStoreup::getTablename, targetType)
                    .eq(LegacyStoreup::getRefid, targetId);
        return favoriteMapper.selectCount(queryWrapper) > 0;
    }

    private StudentProfile requireStudent(Long accountId) {
        StudentProfile student = studentProfileService.findByAccountId(accountId);
        if (student == null) {
            throw ApiException.forbidden("Student profile not found");
        }
        return student;
    }

    private Page<Favorite> mapPage(Page<LegacyStoreup> legacyPage) {
        Page<Favorite> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private Favorite map(LegacyStoreup legacy) {
        Favorite favorite = new Favorite();
        favorite.setId(legacy.getId());
        favorite.setCreatedAt(legacy.getAddtime());
        favorite.setUpdatedAt(legacy.getAddtime());
        favorite.setStudentId(legacy.getUserid());
        favorite.setTargetType(legacy.getTablename());
        favorite.setTargetId(legacy.getRefid());
        favorite.setName(legacy.getName());
        favorite.setPicturePath(legacy.getPicture());
        favorite.setRecommendationType(legacy.getInteltype());
        favorite.setRemark(legacy.getRemark());
        return favorite;
    }
}

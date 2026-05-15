package com.university.backend.student.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.common.error.ApiException;
import com.university.backend.legacy.domain.LegacyStudent;
import com.university.backend.legacy.infrastructure.LegacyStudentMapper;
import com.university.backend.student.domain.StudentProfile;
import com.university.backend.student.dto.AdminStudentRequest;
import java.util.ArrayList;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminStudentService {

    private final LegacyStudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminStudentService(LegacyStudentMapper studentMapper, PasswordEncoder passwordEncoder) {
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<StudentProfile> page(long page, long size, String keyword) {
        LambdaQueryWrapper<LegacyStudent> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q
                .like(LegacyStudent::getStudentID, keyword)
                .or()
                .like(LegacyStudent::getStudentName, keyword)
                .or()
                .like(LegacyStudent::getCollege, keyword)
                .or()
                .like(LegacyStudent::getContactNumber, keyword)
            );
        }
        wrapper.orderByDesc(LegacyStudent::getAddtime).orderByDesc(LegacyStudent::getId);
        return mapPage(studentMapper.selectPage(Page.of(page, size), wrapper));
    }

    public StudentProfile create(AdminStudentRequest request) {
        if (!StringUtils.hasText(request.password())) {
            throw ApiException.badRequest("学生密码不能为空");
        }
        ensureStudentNoAvailable(request.studentNo(), null);
        LegacyStudent student = new LegacyStudent();
        apply(student, request, true);
        studentMapper.insert(student);
        return map(student);
    }

    public StudentProfile update(Long id, AdminStudentRequest request) {
        LegacyStudent student = getRequired(id);
        ensureStudentNoAvailable(request.studentNo(), id);
        apply(student, request, false);
        studentMapper.updateById(student);
        return map(student);
    }

    public void delete(Long id) {
        if (studentMapper.deleteById(id) == 0) {
            throw ApiException.notFound("学生不存在");
        }
    }

    private LegacyStudent getRequired(Long id) {
        LegacyStudent student = studentMapper.selectById(id);
        if (student == null) {
            throw ApiException.notFound("学生不存在");
        }
        return student;
    }

    private void ensureStudentNoAvailable(String studentNo, Long currentId) {
        LegacyStudent existing = studentMapper.selectOne(
            new LambdaQueryWrapper<LegacyStudent>()
                .eq(LegacyStudent::getStudentID, studentNo)
                .last("limit 1")
        );
        if (existing != null && !existing.getId().equals(currentId)) {
            throw ApiException.badRequest("学号已存在");
        }
    }

    private void apply(LegacyStudent student, AdminStudentRequest request, boolean passwordRequired) {
        student.setStudentID(request.studentNo().trim());
        if (StringUtils.hasText(request.password())) {
            student.setStudentPassword(passwordEncoder.encode(request.password()));
        } else if (passwordRequired) {
            throw ApiException.badRequest("学生密码不能为空");
        }
        student.setStudentName(request.fullName().trim());
        student.setAvatar(request.avatarPath());
        student.setGender(request.gender());
        student.setCollege(request.college());
        student.setContactNumber(request.contactNumber());
        student.setScore(request.score());
    }

    private Page<StudentProfile> mapPage(Page<LegacyStudent> legacyPage) {
        Page<StudentProfile> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private StudentProfile map(LegacyStudent student) {
        StudentProfile profile = new StudentProfile();
        profile.setId(student.getId());
        profile.setAccountId(student.getId());
        profile.setCreatedAt(student.getAddtime());
        profile.setUpdatedAt(student.getAddtime());
        profile.setStudentNo(student.getStudentID());
        profile.setFullName(student.getStudentName());
        profile.setAvatarPath(student.getAvatar());
        profile.setGender(student.getGender());
        profile.setCollege(student.getCollege());
        profile.setContactNumber(student.getContactNumber());
        profile.setScore(student.getScore());
        return profile;
    }
}

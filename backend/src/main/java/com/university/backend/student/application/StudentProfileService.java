package com.university.backend.student.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.backend.legacy.domain.LegacyStudent;
import com.university.backend.legacy.infrastructure.LegacyStudentMapper;
import com.university.backend.student.domain.StudentProfile;
import com.university.backend.student.dto.StudentProfileUpdateRequest;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class StudentProfileService {

    private final LegacyStudentMapper legacyStudentMapper;

    public StudentProfileService(LegacyStudentMapper legacyStudentMapper) {
        this.legacyStudentMapper = legacyStudentMapper;
    }

    public StudentProfile findByAccountId(Long accountId) {
        return map(legacyStudentMapper.selectById(accountId));
    }

    public LegacyStudent findLegacyByAccountId(Long accountId) {
        return legacyStudentMapper.selectById(accountId);
    }

    public StudentProfile findByStudentNo(String studentNo) {
        return map(legacyStudentMapper.selectOne(
            new LambdaQueryWrapper<LegacyStudent>().eq(LegacyStudent::getStudentID, studentNo).last("limit 1")
        ));
    }

    public StudentProfile updateByAccountId(Long accountId, StudentProfileUpdateRequest request) {
        LegacyStudent student = legacyStudentMapper.selectById(accountId);
        if (student == null) {
            return null;
        }
        student.setStudentName(request.fullName());
        student.setAvatar(request.avatarPath());
        student.setGender(request.gender());
        student.setCollege(request.college());
        student.setContactNumber(request.contactNumber());
        legacyStudentMapper.updateById(student);
        return map(student);
    }

    private StudentProfile map(LegacyStudent student) {
        if (student == null) {
            return null;
        }
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

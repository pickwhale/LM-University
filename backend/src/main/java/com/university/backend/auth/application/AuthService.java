package com.university.backend.auth.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.backend.account.domain.Role;
import com.university.backend.auth.dto.AuthTokenResponse;
import com.university.backend.auth.dto.LoginRequest;
import com.university.backend.auth.dto.MeResponse;
import com.university.backend.auth.dto.RefreshTokenRequest;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import com.university.backend.common.security.JwtProperties;
import com.university.backend.common.security.JwtService;
import com.university.backend.legacy.domain.LegacyStudent;
import com.university.backend.legacy.domain.LegacyUsers;
import com.university.backend.legacy.infrastructure.LegacyStudentMapper;
import com.university.backend.legacy.infrastructure.LegacyUsersMapper;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final LegacyUsersMapper legacyUsersMapper;
    private final LegacyStudentMapper legacyStudentMapper;
    private final StudentProfileService studentProfileService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
        LegacyUsersMapper legacyUsersMapper,
        LegacyStudentMapper legacyStudentMapper,
        StudentProfileService studentProfileService,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        JwtProperties jwtProperties
    ) {
        this.legacyUsersMapper = legacyUsersMapper;
        this.legacyStudentMapper = legacyStudentMapper;
        this.studentProfileService = studentProfileService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public AuthTokenResponse login(LoginRequest request) {
        if (request.role() == Role.ADMIN) {
            return loginAdmin(request);
        }
        if (request.role() == Role.STUDENT) {
            return loginStudent(request);
        }
        throw ApiException.badRequest("Unsupported login role");
    }

    private AuthTokenResponse loginAdmin(LoginRequest request) {
        LegacyUsers admin = legacyUsersMapper.selectOne(
            new LambdaQueryWrapper<LegacyUsers>()
                .eq(LegacyUsers::getUsername, request.username())
                .last("limit 1")
        );
        if (admin != null && passwordMatches(request.password(), admin.getPassword())) {
            AuthenticatedAccount account = new AuthenticatedAccount(admin.getId(), admin.getUsername(), Role.ADMIN);
            return issueTokens(account, toAdminResponse(admin));
        }
        throw ApiException.unauthorized("管理员账号或密码错误");
    }

    private AuthTokenResponse loginStudent(LoginRequest request) {
        LegacyStudent student = legacyStudentMapper.selectOne(
            new LambdaQueryWrapper<LegacyStudent>()
                .eq(LegacyStudent::getStudentID, request.username())
                .last("limit 1")
        );
        if (student != null && passwordMatches(request.password(), student.getStudentPassword())) {
            AuthenticatedAccount account = new AuthenticatedAccount(student.getId(), student.getStudentID(), Role.STUDENT);
            return issueTokens(account, toStudentResponse(student));
        }
        throw ApiException.unauthorized("学生学号或密码错误");
    }

    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        AuthenticatedAccount account = jwtService.parseRefreshToken(request.refreshToken());
        return issueTokens(account, loadMe(account));
    }

    public void logout(RefreshTokenRequest request) {
        // The application uses stateless JWT tokens; client-side token clearing completes logout.
    }

    public MeResponse currentUser(AuthenticatedAccount authenticatedAccount) {
        return loadMe(authenticatedAccount);
    }

    private AuthTokenResponse issueTokens(AuthenticatedAccount authenticatedAccount, MeResponse meResponse) {
        String accessToken = jwtService.generateAccessToken(authenticatedAccount);
        String refreshTokenValue = jwtService.generateRefreshToken(authenticatedAccount);
        return new AuthTokenResponse(
            accessToken,
            refreshTokenValue,
            "Bearer",
            jwtProperties.accessTokenExpiration().toSeconds(),
            meResponse
        );
    }

    private MeResponse loadMe(AuthenticatedAccount authenticatedAccount) {
        if (authenticatedAccount.role() == Role.ADMIN) {
            LegacyUsers admin = legacyUsersMapper.selectById(authenticatedAccount.accountId());
            if (admin == null) {
                throw ApiException.notFound("Admin account not found");
            }
            return toAdminResponse(admin);
        }
        LegacyStudent student = legacyStudentMapper.selectById(authenticatedAccount.accountId());
        if (student == null) {
            throw ApiException.notFound("Student account not found");
        }
        return toStudentResponse(student);
    }

    private MeResponse toAdminResponse(LegacyUsers admin) {
        return new MeResponse(
            admin.getId(),
            admin.getUsername(),
            Role.ADMIN.name(),
            StringUtils.hasText(admin.getRole()) ? admin.getRole() : admin.getUsername(),
            null,
            null,
            null,
            null,
            null
        );
    }

    private MeResponse toStudentResponse(LegacyStudent student) {
        StudentProfile profile = studentProfileService.findByAccountId(student.getId());
        return new MeResponse(
            student.getId(),
            student.getStudentID(),
            Role.STUDENT.name(),
            StringUtils.hasText(student.getStudentName()) ? student.getStudentName() : student.getStudentID(),
            profile == null ? student.getId() : profile.getId(),
            student.getStudentID(),
            student.getStudentName(),
            student.getCollege(),
            student.getContactNumber()
        );
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith("{")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }
}

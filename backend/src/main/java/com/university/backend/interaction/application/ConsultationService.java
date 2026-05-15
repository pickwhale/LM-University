package com.university.backend.interaction.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.common.error.ApiException;
import com.university.backend.interaction.domain.Consultation;
import com.university.backend.interaction.dto.ConsultationReplyRequest;
import com.university.backend.interaction.dto.ConsultationRequest;
import com.university.backend.legacy.domain.LegacyChat;
import com.university.backend.legacy.infrastructure.LegacyChatMapper;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import java.util.ArrayList;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class ConsultationService {

    private final LegacyChatMapper consultationMapper;
    private final StudentProfileService studentProfileService;

    public ConsultationService(LegacyChatMapper consultationMapper, StudentProfileService studentProfileService) {
        this.consultationMapper = consultationMapper;
        this.studentProfileService = studentProfileService;
    }

    public Consultation create(Long accountId, ConsultationRequest request) {
        StudentProfile student = requireStudent(accountId);
        LegacyChat consultation = new LegacyChat();
        consultation.setUserid(student.getId());
        consultation.setAsk(request.question());
        consultation.setIsreply(0);
        consultationMapper.insert(consultation);
        return map(consultation);
    }

    public Page<Consultation> pageOwn(Long accountId, long page, long size) {
        StudentProfile student = requireStudent(accountId);
        return mapPage(consultationMapper.selectPage(
            Page.of(page, size),
            new LambdaQueryWrapper<LegacyChat>().eq(LegacyChat::getUserid, student.getId()).orderByDesc(LegacyChat::getAddtime)
        ));
    }

    public Page<Consultation> pageAdmin(long page, long size) {
        return mapPage(consultationMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<LegacyChat>().orderByDesc(LegacyChat::getAddtime)));
    }

    public Consultation reply(Long id, ConsultationReplyRequest request) {
        LegacyChat consultation = consultationMapper.selectById(id);
        if (consultation == null) {
            throw ApiException.notFound("Consultation not found");
        }
        consultation.setReply(request.reply());
        consultation.setIsreply(1);
        consultationMapper.updateById(consultation);
        return map(consultation);
    }

    private StudentProfile requireStudent(Long accountId) {
        StudentProfile student = studentProfileService.findByAccountId(accountId);
        if (student == null) {
            throw ApiException.forbidden("Student profile not found");
        }
        return student;
    }

    private Page<Consultation> mapPage(Page<LegacyChat> legacyPage) {
        Page<Consultation> page = Page.of(legacyPage.getCurrent(), legacyPage.getSize(), legacyPage.getTotal());
        page.setRecords(new ArrayList<>(legacyPage.getRecords().stream().map(this::map).toList()));
        return page;
    }

    private Consultation map(LegacyChat legacy) {
        Consultation consultation = new Consultation();
        consultation.setId(legacy.getId());
        consultation.setCreatedAt(legacy.getAddtime());
        consultation.setUpdatedAt(legacy.getAddtime());
        consultation.setStudentId(legacy.getUserid());
        consultation.setQuestion(legacy.getAsk());
        consultation.setReply(legacy.getReply());
        consultation.setReplied(legacy.getIsreply() != null && legacy.getIsreply() == 1);
        consultation.setRepliedAt(legacy.getIsreply() != null && legacy.getIsreply() == 1 ? legacy.getAddtime() : null);
        return consultation;
    }
}

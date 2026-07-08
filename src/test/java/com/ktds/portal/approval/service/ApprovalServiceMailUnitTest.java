package com.ktds.portal.approval.service;

import com.ktds.portal.approval.domain.Approval;
import com.ktds.portal.approval.repository.ApprovalRepository;
import com.ktds.portal.common.AuditLogger;
import com.ktds.portal.common.MailSender;
import com.ktds.portal.user.User;
import com.ktds.portal.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * [단위 테스트] 협력 객체 인터페이스 분리 + 생성자 주입(DIP)의 효과를 보여주는 테스트.
 *
 * 목적: Spring 컨텍스트/실 SMTP 없이 ApprovalService를 순수 단위로 검증한다.
 *  - MailSender 자리에 가짜 구현 FakeMailSender를 "주입"해 실제 발송 대신 send 호출만 기록·검증한다.
 *  - 이런 교체는 리팩토링 전(직접 new SmtpMailSender)에는 불가능했다 — DI 전환으로 가능해졌다.
 *
 * 특성화 테스트(ApprovalServiceCharacterizationTest)와 달리 이건 신규 단위 테스트다(안전망 아님).
 * 리포지토리는 이 테스트의 관심사가 아니라 Mockito로 최소 스텁만 한다.
 */
class ApprovalServiceMailUnitTest {

    /**
     * 테스트 더블 — 실제 발송은 하지 않고 send() 호출 인자만 리스트에 기록하는 가짜 MailSender.
     */
    static class FakeMailSender implements MailSender {
        record Sent(String to, String subject, String body) {}

        final List<Sent> sentList = new ArrayList<>();

        @Override
        public void send(String to, String subject, String body) {
            sentList.add(new Sent(to, subject, body));   // 실 발송 없이 기록만
        }
    }

    @Test
    void 상신하면_결재자에게_메일_send가_한번_호출된다_실발송없이() {
        // given — 리포지토리는 Mockito 스텁, 메일은 FakeMailSender 주입, 감사로그는 no-op
        ApprovalRepository approvalRepo = mock(ApprovalRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        FakeMailSender fakeMail = new FakeMailSender();
        AuditLogger noopAudit = line -> { };   // 감사로그는 이 테스트 관심사가 아니므로 아무 것도 안 함

        // 임시저장(status=0, DRAFT) 상태의 결재 — 상신 가능
        Approval draft = new Approval();
        draft.setId(1L);
        draft.setTitle("노트북 구매");
        draft.setType(2);        // 2=휴가(비-경비): 고액 우선순위 자동 상향 분기와 무관하게 단순화
        draft.setPriority(2);
        draft.setStatus(0);      // 0=임시저장(DRAFT)
        draft.setDrafterId(10L);
        draft.setApproverId(20L);
        draft.setAmount(0L);

        User approver = new User("결재자", "approver@test.com", 2, "개발팀");
        approver.setId(20L);

        when(approvalRepo.findById(1L)).thenReturn(Optional.of(draft));
        when(userRepo.findById(10L)).thenReturn(Optional.of(new User("기안자", "drafter@test.com", 1, "개발팀")));
        when(userRepo.findById(20L)).thenReturn(Optional.of(approver));

        ApprovalService service = new ApprovalService(approvalRepo, userRepo, fakeMail, noopAudit);

        // when — 기안자(10)가 상신(action=1)
        service.processApproval(1L, 10L, 1, "");

        // then — 실 발송 대신 send가 정확히 1번, 결재자에게 호출됐는지만 검증
        assertThat(fakeMail.sentList).hasSize(1);
        FakeMailSender.Sent sent = fakeMail.sentList.get(0);
        assertThat(sent.to()).isEqualTo("approver@test.com");
        assertThat(sent.subject()).isEqualTo("[결재요청] 노트북 구매");
    }
}

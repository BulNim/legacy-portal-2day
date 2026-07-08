package com.ktds.portal.approval.domain;

import com.ktds.portal.user.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 결재 엔티티.
 *
 * [리팩토링] Rich Domain — 상태 전이 규칙을 서비스에서 이 도메인으로 이동:
 *  레거시는 ApprovalService.submit/approve/reject/cancel 안에 "상태 가드 + 상태 변경"이 흩어져 있었다.
 *  "결재 자신의 상태 전이는 결재가 안다"는 원칙으로 submit()/approve()/reject()/cancel()을 여기에 둔다.
 *  서비스는 이 메서드의 결과(boolean)만 보고 저장·메일·감사로그 같은 부수효과를 조율(orchestrate)한다.
 * [개선 대상 잔존] 캡슐화 부재 — 아직 모든 필드에 public setter가 남아 있다(create()가 사용). 차후 단계에서 축소.
 *
 * [리팩토링] type/status/priority 필드를 enum으로 전환 + AttributeConverter로 DB엔 정수 그대로 저장
 * (docs/4-12 BL-02). @Enumerated(STRING/ORDINAL) 금지 — STRING은 저장값이 바뀌고 ORDINAL은 9와 순번이 어긋난다.
 * [보존 대상] getter/setter는 레거시처럼 int 시그니처를 유지한다(public 계약·JSON·특성화 테스트 불변).
 * 내부 필드만 enum이고, 경계(getter/setter)에서 code()/fromCode()로 int와 변환한다.
 */
@Entity
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    @Convert(converter = ApprovalTypeConverter.class)
    private ApprovalType type;         // DB: 1=지출 2=휴가 3=구매 4=기타
    @Convert(converter = ApprovalStatusConverter.class)
    private ApprovalStatus status;     // DB: 0=임시저장 1=상신 2=승인 3=반려 9=취소
    @Convert(converter = ApprovalPriorityConverter.class)
    private ApprovalPriority priority;         // DB: 1=낮음 2=보통 3=높음
    private Long drafterId;     // 기안자
    private Long approverId;    // 결재자
    private String rejectReason;
    private long amount;        // 지출/구매 금액
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    // [보존 대상] getter/setter는 int 시그니처 유지 — 내부 enum과 code()/fromCode()로 변환한다.
    public int getType() { return type.code(); }
    public void setType(int type) { this.type = ApprovalType.fromCode(type); }
    public int getStatus() { return status.code(); }
    public void setStatus(int status) { this.status = ApprovalStatus.fromCode(status); }
    public int getPriority() { return priority.code(); }
    public void setPriority(int priority) { this.priority = ApprovalPriority.fromCode(priority); }
    public Long getDrafterId() { return drafterId; }
    public void setDrafterId(Long drafterId) { this.drafterId = drafterId; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ================== [리팩토링] 상태 전이 규칙 (Rich Domain) ==================
    // 레거시: 아래 4개 전이의 가드+상태 변경이 ApprovalService에 흩어져 있었다. 여기로 옮긴다.
    // [동작 보존] 전이 조건 불만족 시 상태를 바꾸지 않고 false를 반환 → 서비스가 조용히 무시(레거시 동작 그대로).

    // [스멜3 해소] 고액 지출 우선순위 자동 상향 임계값(원). 범주형이 아니라 명명 상수로 의미를 부여한다.
    private static final long EXPENSE_HIGH_PRIORITY_THRESHOLD = 1_000_000L;

    /**
     * 상신: 임시저장(DRAFT)일 때만 상신(SUBMITTED)으로 전이한다.
     * 경비(EXPENSE) 유형이고 임계 금액 이상이면 우선순위를 높음으로 자동 상향한다.
     * @return 전이가 일어났으면 true, 조건 불만족이면 false(레거시의 조용한 무시)
     */
    public boolean submit() {
        if (this.status != ApprovalStatus.DRAFT) {
            return false;
        }
        if (this.type == ApprovalType.EXPENSE && this.amount >= EXPENSE_HIGH_PRIORITY_THRESHOLD) {
            this.priority = ApprovalPriority.HIGH;
        }
        this.status = ApprovalStatus.SUBMITTED;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * 승인: 상신 상태 + 지정 결재자 본인 + 팀장 이상일 때만 승인(APPROVED)으로 전이한다.
     */
    public boolean approve(Long actorUserId, int actorRoleCode) {
        if (!canBeReviewedBy(actorUserId, actorRoleCode)) {
            return false;
        }
        this.status = ApprovalStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * 반려: 승인과 동일한 조건(상신 상태 + 결재자 본인 + 팀장 이상)에서 반려(REJECTED)로 전이하고 사유를 저장한다.
     */
    public boolean reject(Long actorUserId, int actorRoleCode, String reason) {
        if (!canBeReviewedBy(actorUserId, actorRoleCode)) {
            return false;
        }
        this.status = ApprovalStatus.REJECTED;
        this.rejectReason = reason;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * 취소: 기안자 본인이고 아직 승인 전(DRAFT/SUBMITTED)일 때만 취소(CANCELED)로 전이한다.
     */
    public boolean cancel(Long actorUserId) {
        if (this.status != ApprovalStatus.DRAFT && this.status != ApprovalStatus.SUBMITTED) {
            return false;
        }
        if (this.drafterId == null || !this.drafterId.equals(actorUserId)) {
            return false;
        }
        this.status = ApprovalStatus.CANCELED;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    // [리팩토링] 승인/반려에 복붙되던 권한 판정(상신 상태 + 결재자 본인 + 팀장 이상)을 한 곳으로 통합.
    private boolean canBeReviewedBy(Long actorUserId, int actorRoleCode) {
        if (this.status != ApprovalStatus.SUBMITTED) {
            return false;
        }
        if (this.approverId == null || !this.approverId.equals(actorUserId)) {
            return false;
        }
        return actorRoleCode >= UserRole.MANAGER.code();
    }
}

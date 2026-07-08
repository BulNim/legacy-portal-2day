package com.ktds.portal.approval.dto;

import com.ktds.portal.approval.domain.Approval;

import java.time.LocalDateTime;

/**
 * [리팩토링] 결재 응답 DTO (record) — 엔티티(Approval)를 API에 직접 노출하지 않기 위한 경계.
 *
 * 레거시: Controller가 Approval 엔티티를 그대로 반환해 내부 구조가 API에 샜다.
 * [보존 대상] JSON 필드명·구조·값은 엔티티 직렬화 결과와 100% 동일하게 맞춘다:
 *  id/title/content/type/status/priority/drafterId/approverId/rejectReason/amount/createdAt/updatedAt.
 *  type·status·priority는 레거시처럼 정수 코드 그대로(getType()/getStatus()/getPriority()가 int 반환).
 *  from()에서 엔티티의 int 게터로 채우므로 계약(정수 status 포함)이 그대로 유지된다.
 */
public record ApprovalResponse(
        Long id,
        String title,
        String content,
        int type,
        int status,
        int priority,
        Long drafterId,
        Long approverId,
        String rejectReason,
        long amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ApprovalResponse from(Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getTitle(),
                approval.getContent(),
                approval.getType(),
                approval.getStatus(),
                approval.getPriority(),
                approval.getDrafterId(),
                approval.getApproverId(),
                approval.getRejectReason(),
                approval.getAmount(),
                approval.getCreatedAt(),
                approval.getUpdatedAt()
        );
    }
}

package com.ktds.portal.approval.dto;

/**
 * [리팩토링] 결재 생성 요청 DTO (record).
 *
 * 레거시: Controller가 {@code Map<String,Object>}로 받아 필드마다 캐스팅했다 — 키 오타·타입 오류가
 *  컴파일 때 안 잡히고 런타임에야 터졌다. 여기서 record로 받아 필드·타입을 컴파일 타임에 고정한다.
 * [보존 대상] JSON 필드명·구조는 레거시와 동일:
 *  title/content/type/priority/drafterId/approverId/amount/urgent.
 *  type/priority는 여전히 정수 코드(요청 계약 불변) — 서비스로 그대로 위임한다.
 *  amount·urgent는 원시형이라 JSON에서 누락되면 각각 0·false(레거시 getOrDefault 기본값과 동일).
 */
public record CreateApprovalRequest(
        String title,
        String content,
        int type,
        int priority,
        Long drafterId,
        Long approverId,
        long amount,
        boolean urgent
) {
}

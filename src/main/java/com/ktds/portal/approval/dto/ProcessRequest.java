package com.ktds.portal.approval.dto;

/**
 * [리팩토링] 결재 처리(상신/승인/반려/취소) 요청 DTO (record).
 *
 * 레거시: Controller가 {@code Map<String,Object>}로 받아 userId/action/reason을 캐스팅했다.
 * [보존 대상] JSON 필드명·구조·엔드포인트(POST /api/approvals/{id}/process)는 레거시와 동일:
 *  userId/action/reason. action은 여전히 정수 코드(1=상신 2=승인 3=반려 9=취소) — 요청 계약 불변.
 *  reason은 누락 시 record에선 null이 되므로, Controller가 레거시처럼 빈 문자열("")로 보정해 위임한다.
 */
public record ProcessRequest(
        Long userId,
        int action,
        String reason
) {
}

package com.ktds.portal.approval.controller;

import com.ktds.portal.approval.dto.ApprovalResponse;
import com.ktds.portal.approval.dto.CreateApprovalRequest;
import com.ktds.portal.approval.dto.ProcessRequest;
import com.ktds.portal.approval.service.ApprovalService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 결재 REST 컨트롤러.
 *
 * [리팩토링] 요청 바디를 {@code Map<String,Object>} → record DTO(CreateApprovalRequest·ProcessRequest)로 받고,
 *  응답은 엔티티를 직접 노출하지 않고 ApprovalResponse record로 감싼다(모두 dto 패키지).
 *  필드·타입이 컴파일 타임에 고정돼 키 오타·타입 오류를 즉시 잡고, 엔티티 내부 구조가 API에 새지 않는다.
 * [보존 대상] 엔드포인트(URL·HTTP 메서드)와 요청/응답 JSON 필드명·구조는 레거시와 100% 동일하게 유지한다.
 *  action/type/priority/status는 여전히 정수 코드로 오간다(계약 불변) — 매직넘버 자체는 개선 대상이나 계약이라 보존.
 */
@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService service;

    public ApprovalController(ApprovalService service) {
        this.service = service;
    }

    @PostMapping
    public ApprovalResponse create(@RequestBody CreateApprovalRequest request) {
        return ApprovalResponse.from(service.create(
                request.title(),
                request.content(),
                request.type(),
                request.priority(),
                request.drafterId(),
                request.approverId(),
                request.amount(),
                request.urgent()
        ));
    }

    // action: 1=상신, 2=승인, 3=반려, 9=취소  (요청 계약이라 정수 코드 그대로 유지)
    @PostMapping("/{id}/process")
    public void process(@PathVariable Long id, @RequestBody ProcessRequest request) {
        // [보존 대상] reason 누락 시 레거시(getOrDefault "")와 동일하게 빈 문자열로 보정.
        String reason = request.reason() == null ? "" : request.reason();
        service.processApproval(id, request.userId(), request.action(), reason);
    }

    @GetMapping("/drafts/{userId}")
    public List<ApprovalResponse> drafts(@PathVariable Long userId) {
        return service.myDrafts(userId).stream().map(ApprovalResponse::from).toList();
    }

    @GetMapping("/inbox/{userId}")
    public List<ApprovalResponse> inbox(@PathVariable Long userId) {
        return service.myInbox(userId).stream().map(ApprovalResponse::from).toList();
    }
}

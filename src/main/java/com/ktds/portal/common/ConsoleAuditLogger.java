package com.ktds.portal.common;

import org.springframework.stereotype.Component;

/**
 * [리팩토링] 레거시 FileAuditLogger → ConsoleAuditLogger (AuditLogger 인터페이스 구현, 이름을 정직하게).
 * 실제 파일 append 대신 콘솔에 출력하는 실습용 구현체. @Component로 등록해 생성자 주입 대상 빈이 된다.
 * [보존 대상] 출력 형식([AUDIT] …)은 레거시와 100% 동일 — 특성화 테스트/관찰 동작 불변.
 */
@Component
public class ConsoleAuditLogger implements AuditLogger {

    @Override
    public void write(String line) {
        // 실제로는 파일에 append. 실습용으로 콘솔 출력.
        System.out.println("[AUDIT] " + line);
    }
}

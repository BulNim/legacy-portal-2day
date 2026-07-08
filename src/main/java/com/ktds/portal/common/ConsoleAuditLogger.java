package com.ktds.portal.common;

import org.springframework.stereotype.Component;

/**
 * [리팩토링] 레거시 FileAuditLogger → ConsoleAuditLogger (AuditLogger 인터페이스 구현, 이름을 정직하게).
 * 실제 파일 append 대신 콘솔에 출력하는 실습용 구현체. @Component로 등록해 생성자 주입 대상 빈이 된다.
 *
 * 타임스탬프·라인 조립은 AuditLogger의 default 메서드가 담당하므로, 이 구현체는 저수준 출력만 책임진다.
 * [보존 대상] 출력 형식은 레거시와 100% 동일 — {@code [AUDIT] <line>}.
 */
@Component
public class ConsoleAuditLogger implements AuditLogger {

    @Override
    public void write(String line) {
        // 실제로는 파일에 append. 실습용으로 콘솔 출력.
        System.out.println("[AUDIT] " + line);
    }
}

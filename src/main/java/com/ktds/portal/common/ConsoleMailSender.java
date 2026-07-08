package com.ktds.portal.common;

import org.springframework.stereotype.Component;

/**
 * [리팩토링] 레거시 SmtpMailSender → ConsoleMailSender (MailSender 인터페이스 구현, 이름을 정직하게).
 * 실제 SMTP 대신 콘솔에 출력하는 실습용 구현체. @Component로 등록해 생성자 주입 대상 빈이 된다.
 * [보존 대상] 출력 형식(=== MAIL === … ============)은 레거시와 100% 동일 — 특성화 테스트/관찰 동작 불변.
 */
@Component
public class ConsoleMailSender implements MailSender {

    @Override
    public void send(String to, String subject, String body) {
        // 실제로는 JavaMailSender 등을 사용. 실습용으로 콘솔 출력.
        System.out.println("=== MAIL ===");
        System.out.println("TO: " + to);
        System.out.println("SUBJECT: " + subject);
        System.out.println(body);
        System.out.println("============");
    }
}

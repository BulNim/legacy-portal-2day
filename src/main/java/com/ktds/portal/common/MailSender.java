package com.ktds.portal.common;

/**
 * [리팩토링] 메일 발송 추상화 (DIP) — 서비스가 구체 클래스가 아니라 이 인터페이스에 의존한다.
 * 구현체를 콘솔/SMTP/목(mock)으로 갈아끼울 수 있어 테스트에서 교체 가능해진다.
 * [보존 대상] send(to, subject, body) 시그니처는 기존 발송기와 동일 — 호출부·출력 형식 불변.
 */
public interface MailSender {
    void send(String to, String subject, String body);
}

package com.ktds.portal.common;

/**
 * [리팩토링] 감사 로그 기록 추상화 (DIP) — 서비스가 구체 클래스가 아니라 이 인터페이스에 의존한다.
 * 출력 대상(콘솔/파일/DB/목)을 구현체 교체만으로 바꿀 수 있다.
 * [보존 대상] write(line) 시그니처는 기존 로거와 동일 — 호출부·출력 형식 불변.
 */
public interface AuditLogger {
    void write(String line);
}

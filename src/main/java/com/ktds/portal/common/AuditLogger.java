package com.ktds.portal.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * [리팩토링] 감사 로그 기록 추상화 (DIP) — 서비스가 구체 클래스가 아니라 이 인터페이스에 의존한다.
 * 출력 대상(콘솔/파일/DB/목)을 구현체 교체만으로 바꿀 수 있다.
 *
 * [리팩토링] 중복 제거(스멜4): 기존엔 서비스마다 "타임스탬프 포맷 + [now] ACTION id=.. by=.." 문자열을 직접 조립했다.
 * 이제 서비스는 write(action, id, userId)만 호출하고, 시각 포맷·라인 조립은 이 인터페이스의 default 메서드가 한 곳에서 담당한다.
 * 실제 기록(콘솔/파일 등)은 저수준 write(line)에만 남겨 구현체가 담당한다 → SAM 유지(기존 람다·목 테스트 불변).
 * [보존 대상] 최종 출력 라인 형식은 레거시와 동일 — [<now>] <action> id=<id> by=<userId> (결재 생성만 뒤에 type=N).
 */
public interface AuditLogger {

    DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 저수준 출력 — 실제 기록 대상(콘솔/파일/DB/목)만 구현체가 담당하는 단일 추상 메서드. */
    void write(String line);

    /** 고수준 감사 로그 — 서비스가 호출. 출력: {@code [<now>] <action> id=<id> by=<userId>} */
    default void write(String action, Long id, Long userId) {
        write(action, id, userId, null);
    }

    /** 추가 필드(예: 결재 생성의 {@code type=N})를 뒤에 덧붙이는 변형 — 레거시 출력 100% 보존용. */
    default void write(String action, Long id, Long userId, String extra) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP) + "] "
                + action + " id=" + id + " by=" + userId;
        if (extra != null && !extra.isEmpty()) {
            line = line + " " + extra;
        }
        write(line);
    }
}

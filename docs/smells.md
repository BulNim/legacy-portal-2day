# 스멜 진단 결과 (종합)

[4-4](./4-4.%20코드베이스%20구조%20파악.md) ~ [4-11](./4-11.%20심각도%20스코어링.md) 에서 도출한
스멜을 하나의 진단 결과로 통합한다. 리팩토링 백로그 작성의 근거 자료로 사용하며, 각 스멜에 `S01`~`S18`
ID를 부여해 백로그 티켓에서 역참조할 수 있게 한다.

| ID | 스멜 | 위치 | 심각도 | 영향도 | 수정난이도 | 우선순위(P) |
|---|---|---|:---:|:---:|:---:|:---:|
| S01 | No Tests — 테스트 코드 0건 | 프로젝트 전체 | 상 | 상 | 하 | **P1** |
| S02 | Tight Coupling — 협력 객체 직접 `new`(5곳) | Approval/Notice/ScheduleService | 상 | 상 | 하 | **P1** |
| S03 | Primitive Obsession — 매직넘버(status/type/priority/category/role) | 전 도메인 엔티티 | 상 | 상 | 중 | **P1** |
| S04 | 요청 DTO 부재 · 검증 없음 | `ApprovalController.java` 22-45행 | 상 | 중 | 하 | **P1** |
| S05 | Duplicated Code — 감사 로그 기록 블록(6곳) | Approval/Notice/ScheduleService | 중 | 중 | 하 | **P1** |
| S06 | Duplicated Code — `role>=2` 권한 판정(3곳) | ApprovalService, NoticeService | 중 | 중 | 하 | **P1** |
| S07 | Feature Envy — `statusLabel()`/`amountGrade()` | `ApprovalService.java` 169-188행 | 중 | 중 | 하 | **P1** |
| S08 | 깊은 중첩 — `NoticeService.publish()`(25줄/5단계) | `NoticeService.java` 49-73행 | 중 | 중 | 하 | **P1** |
| S09 | God Class | `ApprovalService.java` 전체 | 상 | 상 | 상 | **P2** |
| S10 | Long Method — `processApproval()`(86줄/6단계) | `ApprovalService.java` 75-160행 | 상 | 상 | 상 | **P2** |
| S11 | Duplicated Code — 메일 본문 조립 패턴(4곳) | ApprovalService, NoticeService | 중 | 중 | 중 | **P2** |
| S12 | Anemic Domain Model | Approval/Notice/Schedule 엔티티 | 중 | 중 | 상 | **P3** |
| S13 | 캡슐화 부재 — 전 필드 public setter | 전 엔티티 | 중 | 중 | 상 | **P3** |
| S14 | Duplicated Code — `statusLabel` 매핑 사슬(2곳) | ApprovalService, NoticeService | 하 | 하 | 하 | **P3** |
| S15 | Long Parameter List — `create()` 파라미터 8개 | `ApprovalService.java` 46-47행 | 하 | 하 | 하 | **P3** |
| S16 | Poor Naming — 의미 없는 약어(`d`,`u`,`s`,`proc`,`tmp`,`flag1`) | 세 서비스 전반 | 하 | 하 | 하 | **P3** |
| S17 | Comment Smell — 나쁜 이름을 주석으로 변명 | 세 서비스 전반 | 하 | 하 | 하 | **P3** |
| S18 | 조용한 실패 — 예외 없이 null/void 리턴 | `processApproval()`/`publish()`/`confirm()`/`ScheduleService.create()` | 중 | 중 | — | **제외(보존 대상)** |

**분포**: P1 8건 · P2 3건 · P3 6건 · 정책상 제외 1건 (총 18건)

상세 근거·코드 스니펫·판단 기준은 각 원본 문서를 참고한다.

- 구조·서비스 책임: [4-4](./4-4.%20코드베이스%20구조%20파악.md)
- 중복 코드: [4-5](./4-5.%20중복%20코드%20탐지.md)
- 긴 메서드·중첩: [4-7](./4-7.%20긴%20메서드%20탐지.md)
- 매직넘버: [4-8](./4-8.%20매직넘버%20탐지.md)
- 강결합: [4-9](./4-9.%20강결합%20탐지.md)
- 심각도 정성 평가: [4-10](./4-10.%20도출%20스멜%20-%20심각도%20표.md)
- 영향도×수정난이도 스코어링: [4-11](./4-11.%20심각도%20스코어링.md)

S18(조용한 실패)은 CLAUDE.md 불변 규칙상 리팩토링 단계에서 동작을 바꿀 수 없어 이번 백로그의 착수
대상에서 제외한다. 특성화 테스트로 현재 동작만 고정해두고, 수정 여부는 별도 재설계 단계에서 다룬다.

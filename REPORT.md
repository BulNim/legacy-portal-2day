# 리팩토링 리포트 (`refactor/start..HEAD`)

`refactor/start` 태그(특성화 테스트 완료 시점) → `HEAD` 구간의 변경 요약.

- 커밋: `82464d9` refactor(approval): 매직넘버 enum화 + processApproval 분해 (BL-02·BL-09)
- 변경 규모: **15개 파일, +624 / −128 줄**

## 1. 메서드 길이

| 메서드 | 이전 | 이후 | 비고 |
|---|---:|---:|---|
| `ApprovalService.processApproval` | **86줄 · 중첩 6단계** | dispatcher ~27줄 · **중첩 2단계** | guard + switch 위임 |
| ├ `submit`(추출) | — | 24줄 · 중첩 2 | 신규 |
| ├ `approve`(추출) | — | 24줄 · 중첩 2 | 신규 |
| ├ `reject`(추출) | — | 25줄 · 중첩 2 | 신규 |
| └ `cancel`(추출) | — | 15줄 · 중첩 2 | 신규 |
| `statusLabel` | 11줄 (tmp + 5분기 if) | **~3줄** (enum 위임) | 중복 라벨표 제거 |

- **최대 단일 메서드 길이: 86줄 → 27줄**, **최대 중첩 깊이: 6 → 2**
- 로직을 5개 메서드로 분산(총 줄 수는 guard clause 명시화로 소폭 증가하나 각 메서드가 짧고 평탄)

## 2. 클래스(파일) 수

| 구분 | 이전 | 이후 | 증감 |
|---|---:|---:|---:|
| `src/main/java` 소스 파일 | 18 | **27** | **+9** |
| `src/test/java` 테스트 파일 | 1 | 1 | 0 |

신규 9개 = enum 5 + AttributeConverter 4:

| 종류 | 파일 |
|---|---|
| enum | `ApprovalStatus`, `ApprovalAction`, `ApprovalType`, `Priority`, `Role` |
| converter | `ApprovalStatusConverter`, `ApprovalTypeConverter`, `PriorityConverter`, `RoleConverter` |

> 파일 수는 늘었지만 각각 단일 책임·소형이며, 흩어져 있던 매직넘버·상태 라벨표가 한곳으로 모였다.
> `action`은 저장 필드가 아니라 파라미터라 converter가 없다(enum만).

## 3. 테스트 변화

| 항목 | 값 |
|---|---|
| 특성화 테스트 개수 | 6 (변동 없음) |
| 테스트 파일 diff (`refactor/start..HEAD`) | **없음 (byte 단위 무변경)** |
| 리팩토링 후 결과 | **6 / 6 green** (Failures 0, Errors 0) |

리팩토링 3단계(① enum + AttributeConverter, ② guard clause + 메서드 추출, ③ switch 전환) 전 과정에서
특성화 테스트를 **한 글자도 고치지 않고** 계속 green 유지 → 겉보기 계약(시그니처·DB·JSON·동작)이
100% 보존됐음이 자동 검증됨.

## 4. 매직넘버 제거

- `processApproval` 계열 bare 정수 리터럴 **17개 → 0개** (status/action/type/priority/role 전부 enum).
- 필드는 enum + `@Convert`(AttributeConverter)로 **DB엔 정수 그대로 저장**, getter/setter는 int 시그니처 유지.
- 의도적으로 남긴 수치: 금액 임계값 `1000000`, `amountGrade()` 등급 기준 — 범주형이 아니라 enum 대상 아님.

## 5. 변경 파일 목록

| 파일 | 변경 |
|---|---|
| `approval/Approval.java` | 필드 enum + `@Convert`, getter/setter는 int 유지 |
| `approval/ApprovalService.java` | processApproval 분해, enum 변환, statusLabel 위임 (핵심) |
| `user/User.java` | role enum + `@Convert`, int 시그니처 유지 |
| `notice/NoticeService.java` | `role >= 2` → `Role.MANAGER.code()` |
| enum 5 + converter 4 | 신규 |
| `docs/enum · 약어 rename · Replace Temp 리팩토링.md` | 신규 |
| `docs/리팩토링 효과 분석.md` | 신규 |

## 6. 남은 과제 (참고)

God Class(메일·감사로그·라벨 책임 잔존), 메일 본문·권한판정 조건식 복붙은 이번 범위 밖 —
BL-03(DI) → BL-10(메일 템플릿 추출) 순으로 이어가야 "변경 용이성" 효과가 체감된다.
(상세: `docs/리팩토링 효과 분석.md`)

# Spring Boot + Kotlin 학습 가이드

환영합니다! 이 가이드는 Spring Boot와 Kotlin을 빠르게 이해하고 활용할 수 있도록 돕기 위해 만들어졌습니다.

---

## 학습 로드맵 (4주 과정)

### Week 1: 기초 다지기
**목표**: Kotlin과 Spring Boot 기본 문법 익히기

#### Day 1-3: Kotlin 기초
**학습 자료**: `01_KOTLIN_BASICS.md`

**학습 내용**:
- 변수, 함수, 클래스
- Null 안전성 (?, ?:, !!, let)
- 스코프 함수 (let, apply, run, also, with)
- 컬렉션 함수 (map, filter 등)
- 확장 함수, 고차 함수
- enum class, object, sealed class
- 스마트 캐스트, 구조 분해, by lazy

**실습**:
- 간단한 도서 관리 시스템 만들기 (파일 내 예제)

#### Day 4-7: Spring Boot 기초
**학습 자료**: `02_SPRING_BASICS.md`

**학습 내용**:
- 의존성 주입 (DI)
- Controller-Service-Repository 패턴
- Reactive Programming (suspend 함수, Flow)
- 3-Layer 모델 (Entity-DTO-Domain)
- 로깅 (KotlinLogging)
- @ConfigurationProperties, Spring Profiles

**실습**:
- TODO 앱 만들기 (Blocking → Reactive 변환)

---

### Week 2: 프로젝트 구조 파악
**목표**: Spring Boot 프로젝트 코드를 읽고 이해하기

#### Day 1-2: 프로젝트 전체 구조
**학습 자료**: `03_PROJECT_STRUCTURE.md`

**학습 순서**:
1. 디렉토리 구조 훑어보기
2. Application 진입점 읽기
3. 가장 간단한 API부터 따라가기

#### Day 3-5: 도메인 이해
**학습 흐름**:
1. Controller → Service → Repository
2. DTO 구조
3. 상태 머신
4. Kafka 메시징
5. 스케줄러

**실습**:
- 디버거로 API 흐름 추적하기
- 생성/조회 API 완전히 이해하기

#### Day 6-7: 인증/보안 이해
**학습 내용**:
- JWT 인증 흐름
- @CurrentUser 동작 원리
- 역할 기반 권한 관리

---

### Week 3: 실습 프로젝트
**목표**: 직접 코드를 작성하면서 배우기

#### 실습 프로젝트 시작
**디렉토리**: `practice/book/`

**Level 1: 기본 CRUD** (Day 1-2)
- [ ] TODO 주석 완성하기
- [ ] 6가지 API 테스트 (생성, 조회, 수정, 삭제, 대출, 반납)
- [ ] Postman Collection 만들기

**Level 2: 검색 기능** (Day 3-4)
- [ ] 복합 조건 검색 구현
- [ ] 가격 범위 검색 추가
- [ ] 페이징 동작 확인

**Level 3: 통계 기능** (Day 5-6)
- [ ] 카테고리/상태별 통계
- [ ] 대출 통계 기능 추가
- [ ] 샘플 데이터 생성

**Level 4: 도전 과제** (Day 7)
- [ ] 커스텀 예외 처리
- [ ] 대출 이력 기능
- [ ] 리뷰 기능

**상세 문제**: `practice/exercises/EXERCISES.md`

---

### Week 4: 심화 학습
**목표**: 고급 패턴 이해 및 적용

#### Day 1-3: 복잡한 기능 이해
**학습 내용**:
- Factory 패턴
- 복잡한 비즈니스 로직
- Redis 캐싱

#### Day 4-5: 기능 추가해보기
**연습 과제**:
1. DTO에 새 필드 추가
2. 검색 API 추가
3. 통계 API 추가
4. WebClient로 외부 API 연동
5. @Scheduled 스케줄러 구현

#### Day 6-7: 코드 리뷰 준비
- 코드 컨벤션 학습 (**06_KOTLIN_CONVENTIONS.md** 참고)
- Pull Request 작성법
- 테스트 코드 작성 (**05_TESTING.md** 참고)
- 자주 만나는 에러 정리 (**06_KOTLIN_CONVENTIONS.md** Part 2)

---

## 학습 전략

### 1. "읽기 → 따라하기 → 작은 기능 만들기" 순서

**비추천**:
- 처음부터 코드를 작성하려고 함
- 전체를 한 번에 이해하려고 함
- 문서만 읽고 코드를 안 봄

**추천**:
- 작은 기능 하나를 완전히 이해
- 예제 코드를 읽으면서 학습
- 비슷한 코드를 직접 작성해보기

### 2. 막힐 때 대처법

1. **변수/함수 추적**: IDE의 "Find Usages" (Cmd+B / Ctrl+B)
2. **타입 확인**: 마우스 오버해서 타입 보기
3. **디버거 사용**: 브레이크포인트 설정하고 Step Into
4. **로그 추가**: `println()` 또는 `logger.info()`
5. **테스트 작성**: 작은 부분만 실행해보기

### 3. 질문하는 법

**나쁜 질문**:
- "이 코드가 어떻게 동작하나요?" (너무 광범위)
- "이거 좀 봐주세요" (무엇을 봐야 할지 모름)

**좋은 질문**:
- "BookServiceImpl의 borrowBook 함수 57번째 줄에서, 왜 status를 검증하나요?"
- "BookRepository가 어떤 기준으로 검색하는지 알 수 있을까요?"

---

## 학습 자료 구조

```
spring-kotlin-guide/
├── docs/                           # 학습 문서
│   ├── 00_README.md                # 이 파일 (전체 가이드)
│   ├── 01_KOTLIN_BASICS.md         # Kotlin 기초 ~ 고급 문법
│   │                               #   변수/함수/클래스, Null 안전성, 스코프 함수,
│   │                               #   컬렉션, enum, object, 스마트 캐스트,
│   │                               #   구조 분해, by lazy 등
│   ├── 02_SPRING_BASICS.md         # Spring Boot 기초 ~ 심화
│   │                               #   DI, Controller-Service-Repository,
│   │                               #   Reactive/Flow, 로깅, @ConfigurationProperties,
│   │                               #   Spring Profiles, WebClient, @Scheduled 등
│   ├── 03_PROJECT_STRUCTURE.md     # 프로젝트 구조 읽기 가이드
│   ├── 04_SYNTAX_REFERENCE.md      # 문법 레퍼런스 (빠른 참조)
│   ├── 05_TESTING.md               # 테스트 코드 작성 가이드
│   └── 06_KOTLIN_CONVENTIONS.md    # 코드 컨벤션 + 자주 만나는 에러
├── examples/                       # 문법 예제 코드
│   └── *.kt
└── practice/                       # 실습 프로젝트
    ├── README.md
    ├── book/                       # 도서 관리 시스템 (실습용)
    │   ├── model/
    │   ├── infra/
    │   ├── service/
    │   └── ui/
    └── exercises/
        └── EXERCISES.md            # 단계별 연습 문제
```

---

## 학습 체크리스트

### Week 1: Kotlin + Spring 기초
- [ ] Kotlin 기초 문법 학습 완료 (01_KOTLIN_BASICS.md)
  - [ ] 변수, 함수, 클래스, Null 안전성
  - [ ] 스코프 함수, 컬렉션 함수, 확장 함수
  - [ ] enum class, object, 스마트 캐스트
  - [ ] 구조 분해, by lazy
- [ ] 도서 관리 시스템 실습 (Kotlin) 완료
- [ ] Spring Boot 개념 이해 (02_SPRING_BASICS.md)
  - [ ] DI, @Component 계열 어노테이션
  - [ ] Controller-Service-Repository 패턴
  - [ ] @Transactional, @ConfigurationProperties
  - [ ] 로깅 설정
- [ ] Reactive Programming 기본 개념 이해
  - [ ] suspend 함수, Flow
- [ ] TODO 앱 실습 완료

### Week 2: 프로젝트 이해
- [ ] 프로젝트 디렉토리 구조 파악
- [ ] 조회 API 흐름 완전히 이해
- [ ] 생성 API 흐름 완전히 이해
- [ ] 상태 머신 이해
- [ ] Kafka 메시징 개념 이해
- [ ] JWT 인증 흐름 이해
- [ ] Spring Profiles (dev/prod) 이해

### Week 3: 실습
- [ ] practice/book Level 1 완료 (기본 CRUD)
- [ ] practice/book Level 2 완료 (검색)
- [ ] practice/book Level 3 완료 (통계)
- [ ] practice/book Level 4 완료 (도전 과제)
- [ ] WebClient로 외부 API 호출 구현
- [ ] 모든 API Postman 테스트 완료

### Week 4: 심화
- [ ] 복잡한 도메인 구조 파악
- [ ] Factory 패턴 이해
- [ ] Redis 캐싱 동작 이해
- [ ] @Scheduled 스케줄러 구현
- [ ] 작은 기능 추가 성공
- [ ] 테스트 코드 작성 (05_TESTING.md 학습 완료)
- [ ] 코드 컨벤션 숙지 (06_KOTLIN_CONVENTIONS.md)
- [ ] 코드 리뷰 받기

---

## 학습 후 다음 단계

### Month 2: 다양한 도메인 학습
- 다른 도메인 패키지 분석
- 복잡한 비즈니스 로직 이해

### Month 3: 복잡한 기능 이해
- 복잡한 도메인 완전 정복
- 계산 로직 분석

### Month 4: 기능 개발
- 기능 설계
- 코드 작성 및 리뷰
- 배포

### Month 5-6: 독립적인 개발자
- 신규 기능 설계 참여
- 코드 리뷰어 되기
- 신입 멘토링

---

## 팁

### IDE 단축키 (IntelliJ IDEA)
- `Cmd/Ctrl + B`: 정의로 이동
- `Cmd/Ctrl + Alt + B`: 구현체로 이동
- `Cmd/Ctrl + F`: 파일 내 검색
- `Cmd/Ctrl + Shift + F`: 전체 검색
- `Cmd/Ctrl + Alt + L`: 코드 포맷팅
- `Shift + Shift`: 전체 검색 (파일, 클래스, 메서드 등)

### 학습 시간 배분
- 읽기: 40%
- 따라하기: 30%
- 직접 작성: 20%
- 질문/토론: 10%

### 일일 학습 루틴
1. 아침: 이론 학습 (1-2시간)
2. 점심 후: 코드 읽기 (1-2시간)
3. 오후: 실습 (2-3시간)
4. 저녁: 복습 및 정리 (30분)

---

## 참고 자료

### 공식 문서
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html)
- [Kotlin Coroutines 가이드](https://kotlinlang.org/docs/coroutines-guide.html)
- [Kotlin Flow 가이드](https://kotlinlang.org/docs/flow.html)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring WebFlux 문서](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Komapper 문서](https://www.komapper.org/)

### 온라인 실습
- [Kotlin Koans](https://play.kotlinlang.org/koans) - Kotlin 문법 연습
- [Spring Guides](https://spring.io/guides) - Spring Boot 튜토리얼

### 코드 품질 도구
- [ktlint](https://github.com/pinterest/ktlint) - Kotlin 코드 스타일 검사
- [detekt](https://detekt.dev/) - Kotlin 정적 분석

---

## 학습 노트

이 공간에 학습하면서 배운 내용, 막혔던 부분, 해결 방법 등을 기록하세요.

### 날짜별 학습 기록

#### 2025-01-XX
- 학습 내용:
- 어려웠던 점:
- 해결 방법:
- 내일 할 일:

---

**화이팅! 천천히, 확실하게 익혀나가세요.**

궁금한 점이 있으면 언제든지 물어보세요!
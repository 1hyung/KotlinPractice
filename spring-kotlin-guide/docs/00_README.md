# Spring Boot + Kotlin 백엔드 엔지니어 가이드

Kotlin과 Spring Boot 기초부터 Spring Security, Kafka, gRPC, 데이터베이스, 배포까지 — 실무 백엔드 개발자가 되기 위한 완전한 학습 가이드입니다.

---

## 전체 학습 흐름 (7주)

```
Week 1  →  Week 2  →  Week 3  →  Week 4  →  Week 5  →  Week 6  →  Week 7
Kotlin     Spring     보안 +      실습 +     Kafka /    데이터베이스  배포 +
기초       기초       프로젝트    코드 품질   gRPC       심화         운영
 01         02        10 + 03     04~06      07~08        09          11
```

> **04_SYNTAX_REFERENCE.md** 는 레퍼런스입니다. 처음부터 다 읽지 말고, 학습 중 모르는 문법이 나올 때마다 찾아보세요.

---

## 학습 로드맵

### Week 1: Kotlin 언어 익히기
**목표**: Spring 없이 Kotlin 언어 자체를 손에 익힌다
**학습 자료**: `01_KOTLIN_BASICS.md`

| Day | 학습 내용 |
|-----|----------|
| 1-2 | 변수, 함수, 클래스, data class |
| 3   | Null 안전성 (?, ?:, !!, as?, let) |
| 4   | 스코프 함수 (let, apply, run, also, with) |
| 5   | 컬렉션 함수 (map, filter, groupBy 등), 확장 함수 |
| 6   | enum class, object, sealed class, 스마트 캐스트 |
| 7   | 구조 분해, by lazy, interface 패턴 + **도서 관리 실습** |

**완료 기준**: 파일 끝의 실습 프로젝트를 보지 않고 직접 구현할 수 있다

---

### Week 2: Spring Boot 익히기
**목표**: DI, Controller-Service-Repository, Reactive 패턴을 이해한다
**학습 자료**: `02_SPRING_BASICS.md`

| Day | 학습 내용 |
|-----|----------|
| 1   | Spring Boot란?, 의존성 주입(DI), @Component 계열 |
| 2-3 | Controller → Service → Repository 패턴, TODO 앱 실습 |
| 4   | Reactive (suspend 함수, Flow), Non-blocking 개념 |
| 5   | @Transactional, @Configuration/@Bean |
| 6   | @ConfigurationProperties, Spring Profiles (dev/prod) |
| 7   | 로깅, WebClient, @Scheduled, @RestControllerAdvice |

**완료 기준**: TODO 앱을 Blocking → Reactive로 직접 변환할 수 있다

---

### Week 3: 보안 + 프로젝트 구조 읽기
**목표**: Spring Security + JWT 인증을 이해하고 실제 프로젝트 코드를 읽을 수 있다
**학습 자료**: `10_SECURITY.md`, `03_PROJECT_STRUCTURE.md`

| Day | 학습 내용 |
|-----|----------|
| 1-2 | Spring Security 6.x 개념, SecurityFilterChain, JWT 인증 흐름 |
| 3   | JwtTokenProvider, JwtAuthenticationFilter 구현 |
| 4   | 로그인/회원가입/토큰 갱신 API, @CurrentUser 어노테이션 |
| 5   | @PreAuthorize, 역할 기반 접근 제어 (RBAC) |
| 6   | 디렉토리 구조, Controller → Service → Repository 코드 따라가기 |
| 7   | GlobalExceptionHandler, 공통 응답 형식, 패턴 정리 |

**완료 기준**: JWT 인증이 포함된 새 API 엔드포인트를 처음부터 끝까지 혼자 추가할 수 있다

---

### Week 4: 실습 + 코드 품질
**목표**: 직접 코드를 작성하고, 품질을 갖춘 코드를 쓴다
**학습 자료**: `practice/book/`, `05_TESTING.md`, `06_KOTLIN_CONVENTIONS.md`

#### 실습 프로젝트 (`practice/book/`)

**Level 1: 기본 CRUD** (Day 1-2)
- [ ] TODO 주석 완성하기
- [ ] 6가지 API 테스트 (생성, 조회, 수정, 삭제, 대출, 반납)
- [ ] Postman Collection 만들기

**Level 2: 검색 기능** (Day 3-4)
- [ ] 복합 조건 검색 구현
- [ ] 가격 범위 검색, 페이징 추가

**Level 3: 통계 + 심화** (Day 5-6)
- [ ] 카테고리/상태별 통계 API
- [ ] 커스텀 예외 처리 적용

**Level 4: 도전 과제** (Day 7)
- [ ] 대출 이력 기능
- [ ] WebClient로 외부 API 연동

#### 코드 품질 (Day 6-7 병행)
- 코드 컨벤션: `06_KOTLIN_CONVENTIONS.md` Part 1
- 자주 만나는 에러: `06_KOTLIN_CONVENTIONS.md` Part 2
- 테스트 코드 작성: `05_TESTING.md`

**상세 문제**: `practice/exercises/EXERCISES.md`

---

### Week 5: Kafka & gRPC
**목표**: 서비스 간 통신 패턴을 이해하고 구현한다

#### Kafka (Day 1-3) — `07_KAFKA.md`

| Day | 학습 내용 |
|-----|----------|
| 1   | Kafka 개념 (Topic, Partition, Consumer Group), 환경 설정 |
| 2   | Producer 구현, Consumer 구현, 에러 처리 (DLQ) |
| 3   | Outbox 패턴, Coroutine 통합, EmbeddedKafka 테스트 |

#### gRPC (Day 4-6) — `08_GRPC.md`

| Day | 학습 내용 |
|-----|----------|
| 4   | gRPC vs REST, proto3 파일 작성, 환경 설정 |
| 5   | Server 구현, Client 구현, Interceptor (JWT 인증) |
| 6   | Kotlin Coroutine + gRPC, 에러 처리, 테스트 |

**Day 7**: 두 기술을 조합한 미니 아키텍처 설계해보기
(예: REST Controller → gRPC Client → 다른 서비스 → Kafka 이벤트 발행)

---

### Week 6: 데이터베이스 심화
**목표**: 실무에서 사용하는 DB 기술 스택을 직접 연동한다
**학습 자료**: `09_DATABASE.md`

| Day | 학습 내용 |
|-----|----------|
| 1   | JPA vs R2DBC vs Komapper 비교, MySQL/PostgreSQL 설정 |
| 2   | JPA Entity 설계, N+1 문제와 해결 (Fetch Join, @EntityGraph) |
| 3   | R2DBC + Komapper (Reactive DB 접근), JPA vs R2DBC 나란히 비교 |
| 4   | Flyway 마이그레이션 설정 |
| 5   | Aurora (읽기/쓰기 분리), Supabase 연동 |
| 6   | Redis 캐싱 (@Cacheable, Cache-Aside 패턴), 분산 락 (Redisson) |
| 7   | Redis Pub/Sub, 전체 복습 및 정리 |

**완료 기준**: Docker Compose로 DB 띄우고 Spring Boot에서 CRUD + 캐싱까지 직접 구현할 수 있다

---

### Week 7: 배포 + 운영
**목표**: Docker로 컨테이너화하고, CI/CD 파이프라인을 구성하고, 운영 환경에서 모니터링한다
**학습 자료**: `11_DEPLOYMENT.md`

| Day | 학습 내용 |
|-----|----------|
| 1   | Swagger/OpenAPI 설정, JWT Bearer 인증 헤더 추가 |
| 2   | Spring Boot Actuator, Micrometer, Prometheus + Grafana |
| 3   | Dockerfile 멀티 스테이지 빌드, .dockerignore |
| 4   | Docker Compose 전체 스택 (앱 + DB + Redis + Kafka + 모니터링) |
| 5   | GitHub Actions CI (테스트 + Docker 빌드), 환경 변수/시크릿 관리 |
| 6   | GitHub Actions CD (배포), Multi-module Gradle 프로젝트 구조 |
| 7   | 운영 팁 (Graceful Shutdown, JVM 튜닝, Zero-downtime 배포) |

**완료 기준**: 로컬에서 `docker-compose up` 한 번으로 전체 스택을 실행하고, GitHub push 시 자동으로 테스트 및 배포가 되도록 설정할 수 있다

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
│   ├── 00_README.md                # 이 파일 (전체 가이드 + 로드맵)
│   │
│   ├── [Kotlin & Spring 핵심]
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
│   ├── 06_KOTLIN_CONVENTIONS.md    # 코드 컨벤션 + 자주 만나는 에러
│   │
│   ├── [보안]
│   ├── 10_SECURITY.md              # Spring Security + JWT
│   │                               #   SecurityFilterChain, JwtTokenProvider,
│   │                               #   JwtAuthenticationFilter, @CurrentUser,
│   │                               #   @PreAuthorize, RBAC, Refresh Token
│   │
│   ├── [인프라 & 메시징]
│   ├── 07_KAFKA.md                 # Apache Kafka
│   │                               #   Producer/Consumer, Topic/Partition,
│   │                               #   Spring Kafka 설정, DLQ, Outbox 패턴,
│   │                               #   Kotlin Coroutine 통합, 테스트
│   ├── 08_GRPC.md                  # gRPC
│   │                               #   Protocol Buffers, 4가지 통신 패턴,
│   │                               #   grpc-spring-boot-starter, Kotlin 코루틴,
│   │                               #   인터셉터, 에러 처리, 테스트
│   │
│   ├── [데이터베이스]
│   ├── 09_DATABASE.md              # 데이터베이스 총망라
│   │                               #   MySQL, PostgreSQL, Aurora, Supabase,
│   │                               #   JPA/R2DBC, Flyway 마이그레이션, Redis
│   │                               #   (캐싱, Pub/Sub, 분산 락)
│   │
│   └── [배포 & 운영]
│       └── 11_DEPLOYMENT.md        # 배포 & 운영
│                                   #   Swagger/OpenAPI, Actuator, Micrometer,
│                                   #   Prometheus+Grafana, Dockerfile,
│                                   #   Docker Compose, GitHub Actions CI/CD,
│                                   #   Multi-module Gradle, 운영 팁
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

### Week 2: Spring Boot + 프로젝트 이해
- [ ] Spring Boot DI, Controller-Service-Repository 패턴
- [ ] @Transactional, @ConfigurationProperties, Spring Profiles
- [ ] AOP (@Around, @DistributedLock) 이해
- [ ] Reactive (suspend, Flow) 기본 개념
- [ ] WebClient, @Scheduled

### Week 3: 보안 + 프로젝트 구조
- [ ] Spring Security 6.x SecurityFilterChain 설정 (10_SECURITY.md)
- [ ] JwtTokenProvider 구현 이해
- [ ] JwtAuthenticationFilter 동작 방식 이해
- [ ] 로그인/회원가입/토큰 갱신 API 흐름
- [ ] @CurrentUser 어노테이션 동작 원리
- [ ] @PreAuthorize 역할 기반 접근 제어
- [ ] 프로젝트 디렉토리 구조 파악 (03_PROJECT_STRUCTURE.md)
- [ ] 조회/생성 API 흐름 완전히 이해
- [ ] GlobalExceptionHandler, 공통 응답 형식

### Week 4: 실습 + 코드 품질
- [ ] practice/book Level 1 완료 (기본 CRUD)
- [ ] practice/book Level 2 완료 (검색)
- [ ] practice/book Level 3 완료 (통계)
- [ ] practice/book Level 4 완료 (도전 과제)
- [ ] WebClient로 외부 API 호출 구현
- [ ] 모든 API Postman 테스트 완료
- [ ] @Scheduled 스케줄러 구현
- [ ] 테스트 코드 작성 (05_TESTING.md 학습 완료)
  - [ ] JUnit 5 + MockK 단위 테스트
  - [ ] @SpringBootTest 통합 테스트
  - [ ] TestContainers (PostgreSQL, Redis, Kafka)
- [ ] 코드 컨벤션 숙지 (06_KOTLIN_CONVENTIONS.md)
- [ ] 코드 리뷰 받기

### Week 5: Kafka & gRPC
- [ ] Kafka 핵심 개념 이해 (07_KAFKA.md)
  - [ ] Producer/Consumer, Topic/Partition, Consumer Group
  - [ ] Spring Kafka 설정 및 Producer 구현
  - [ ] Consumer 구현 및 에러 처리 (DLQ)
  - [ ] Outbox 패턴 이해
- [ ] gRPC 핵심 개념 이해 (08_GRPC.md)
  - [ ] Proto 파일 작성
  - [ ] Server / Client 구현
  - [ ] Kotlin Coroutine + gRPC 통합

### Week 6: 데이터베이스 심화
- [ ] MySQL / PostgreSQL Spring Boot 연동 (09_DATABASE.md)
  - [ ] JPA Entity 설계
  - [ ] N+1 문제 이해 및 해결
  - [ ] R2DBC (Reactive) 사용
- [ ] Aurora 연동 및 읽기/쓰기 분리 이해
- [ ] Supabase 연동
- [ ] Flyway 마이그레이션 설정
- [ ] Redis 활용
  - [ ] 캐싱 (@Cacheable, Cache-Aside 패턴)
  - [ ] 분산 락 (Redisson)
  - [ ] Pub/Sub

### Week 7: 배포 + 운영
- [ ] Swagger/OpenAPI 설정 (11_DEPLOYMENT.md)
  - [ ] JWT Bearer 인증 헤더 추가
  - [ ] @Operation, @ApiResponse 어노테이션
- [ ] Spring Boot Actuator 설정
  - [ ] /health, /info, /metrics 엔드포인트
  - [ ] Custom HealthIndicator
- [ ] Micrometer + Prometheus + Grafana 연동
- [ ] Dockerfile 멀티 스테이지 빌드 작성
- [ ] Docker Compose 전체 스택 구성 (앱 + DB + Redis + Kafka + 모니터링)
- [ ] GitHub Actions CI 파이프라인 구성
  - [ ] 테스트 자동화, Docker 이미지 빌드
- [ ] GitHub Actions CD 파이프라인 구성
- [ ] Multi-module Gradle 프로젝트 구조 이해
- [ ] 환경 변수 및 시크릿 관리 방법 숙지

---

## 7주 이후: 다음 단계

### Month 2-3: 실전 도메인 학습
- 회사 프로젝트 코드 직접 읽기 (`03_PROJECT_STRUCTURE.md` 패턴 적용)
- 복잡한 비즈니스 로직 분석
- 기존 기능에 작은 수정 기여

### Month 4: 기능 개발 참여
- 신규 기능 설계 참여
- 코드 작성 및 PR 리뷰 받기
- 배포 프로세스 이해

### Month 5-6: 독립적인 개발자
- 혼자 기능 설계 → 구현 → 배포
- 코드 리뷰어로 참여
- 팀 내 신입 멘토링

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

### Kotlin & Spring
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html)
- [Kotlin Coroutines 가이드](https://kotlinlang.org/docs/coroutines-guide.html)
- [Kotlin Flow 가이드](https://kotlinlang.org/docs/flow.html)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring WebFlux 문서](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Komapper 문서](https://www.komapper.org/)

### Kafka & gRPC
- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [gRPC 공식 문서](https://grpc.io/docs/)
- [grpc-spring-boot-starter](https://yidongnan.github.io/grpc-spring-boot-starter/)
- [Protocol Buffers 가이드](https://protobuf.dev/programming-guides/proto3/)

### 데이터베이스
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [Flyway 문서](https://flywaydb.org/documentation/)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Redisson 문서](https://redisson.org/docs/)
- [Supabase 문서](https://supabase.com/docs)

### 온라인 실습
- [Kotlin Koans](https://play.kotlinlang.org/koans) - Kotlin 문법 연습
- [Spring Guides](https://spring.io/guides) - Spring Boot 튜토리얼

### 보안
- [Spring Security 공식 문서](https://spring.io/projects/spring-security)
- [JWT 공식 사이트](https://jwt.io/)
- [JJWT 라이브러리](https://github.com/jwtk/jjwt)

### 배포 & 운영
- [springdoc-openapi](https://springdoc.org/) - Swagger/OpenAPI 자동 생성
- [Spring Boot Actuator 문서](https://docs.spring.io/spring-boot/reference/actuator/)
- [Micrometer 문서](https://micrometer.io/docs)
- [Docker 공식 문서](https://docs.docker.com/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)

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
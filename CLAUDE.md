# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 언어 및 커뮤니케이션 규칙

- **기본 응답 언어**: 한국어
- **코드 주석**: 한국어로 작성
- **커밋 메시지**: 한국어로 작성
- **문서화**: 한국어로 작성
- **변수명/함수명**: 영어 (코드 표준 준수)

## 프로젝트 개요

Kotlin 언어와 Spring Boot를 활용한 백엔드 개발자 7주 완전 교육 과정 학습 저장소.

### 주요 디렉토리 구조

```
KotlinPractice/
├── src/study/                    # 실습 코드 (직접 코딩하며 학습)
│   ├── 01_basics/               # 기초 (변수, 타입, 연산자, 조건문, 반복문, 배열)
│   ├── 02_function/             # 함수 (기본, 람다, 고차함수, 스코프/확장 함수)
│   ├── 03_class/                # 클래스 (기본, data class, enum, object)
│   ├── 04_collection/           # 컬렉션 (List, Set, Map 기초 + 함수)
│   ├── 05_oop/                  # OOP (상속, 인터페이스, 다형성)
│   ├── 06_advanced/             # 고급 (제네릭, sealed class, 예외처리)
│   ├── exercises/               # 실습 (Calculator, CodeKata)
│   └── reference/               # 참고자료
│
└── spring-kotlin-guide/          # 이론 가이드 (읽기 자료)
    ├── docs/                    # 12개 심화 가이드 문서 (00~11)
    ├── examples/                # 문서 참조용 예제 코드
    └── practice/book/           # Spring Boot 실습 프로젝트
```

### 기술 스택

- **언어**: Kotlin 1.8+
- **프레임워크**: Spring Boot 3.x, Spring WebFlux
- **데이터베이스**: JPA, R2DBC, Redis, MySQL, PostgreSQL
- **메시징**: Apache Kafka, gRPC
- **보안**: Spring Security 6.x, JWT
- **테스트**: JUnit 5, MockK, TestContainers
- **배포**: Docker, GitHub Actions

## 아키텍처

### 도서 관리 시스템 (practice/book/)

Controller-Service-Repository 패턴의 Reactive (suspend 함수) 기반 구현:

```
ui/BookController.kt          # REST API 엔드포인트
    ↓
service/BookService.kt        # 비즈니스 로직 인터페이스
    ↓
infra/service/BookServiceImpl.kt   # 비즈니스 로직 구현체
    ↓
infra/repository/BookRepository.kt # 데이터 접근 (In-Memory)
    ↓
model/dto/BookDTO.kt          # 데이터 전송 객체
model/enums/BookStatus.kt     # 도서 상태 열거형 (상태 전이 검증 포함)
```

### API 엔드포인트

- `GET /practice/books` - 전체 도서 조회
- `POST /practice/books` - 도서 생성
- `GET /practice/books/{bookId}` - 단건 조회
- `PUT /practice/books/{bookId}` - 도서 수정
- `DELETE /practice/books/{bookId}` - 도서 삭제
- `POST /practice/books/{bookId}/borrow` - 대출
- `POST /practice/books/{bookId}/return` - 반납
- `POST /practice/books/search` - 복합 검색

## 학습 문서 구조

| 파일 | 내용 |
|-----|------|
| `docs/01_KOTLIN_BASICS.md` | Kotlin 기초~고급 문법, 스코프 함수, 컬렉션 |
| `docs/02_SPRING_BASICS.md` | Spring Boot, DI, Controller-Service-Repository |
| `docs/05_TESTING.md` | JUnit 5, MockK, TestContainers |
| `docs/07_KAFKA.md` | Kafka Producer/Consumer, DLQ, Outbox 패턴 |
| `docs/08_GRPC.md` | gRPC, Protocol Buffers |
| `docs/09_DATABASE.md` | JPA, R2DBC, Redis, Flyway |
| `docs/10_SECURITY.md` | Spring Security, JWT 인증 |
| `docs/11_DEPLOYMENT.md` | Docker, CI/CD, 모니터링 |

## 코드 컨벤션

- Kotlin 공식 코딩 컨벤션 준수
- data class 적극 활용
- Null 안전성: `?.`, `?:`, `!!` (최소화)
- 스코프 함수: `let`, `apply`, `run`, `also`, `with` 상황에 맞게 사용
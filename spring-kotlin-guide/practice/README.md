# 실습 프로젝트: 도서 관리 시스템

Spring Boot 프로젝트 구조를 따라하는 실습 프로젝트입니다.

## 학습 목표

1. 패키지 구조를 이해한다
2. Controller → Service → Repository 패턴을 익힌다
3. DTO, Entity, Domain 3계층 모델을 연습한다
4. Reactive Programming (suspend 함수)을 연습한다

## 프로젝트 구조

```
practice/
├── book/                    # 도메인 패키지
│   ├── model/               # 데이터 모델
│   │   ├── dto/             # 데이터 전송 객체
│   │   ├── entity/          # 데이터베이스 엔티티 (실습에서는 생략)
│   │   └── enums/           # 열거형
│   ├── infra/               # 인프라 계층
│   │   ├── repository/      # 데이터 접근
│   │   └── service/         # 서비스 구현체
│   ├── service/             # 서비스 인터페이스
│   └── ui/                  # 웹 계층 (Controller)
└── exercises/               # 연습 문제
```

## 시작하기

### Step 1: 기본 구조 이해하기
각 디렉토리의 역할을 파악하고, 제공된 예제 코드를 읽어보세요.

### Step 2: TODO 완성하기
각 파일에 있는 `// TODO:` 주석을 찾아서 코드를 완성하세요.

### Step 3: 연습 문제 풀기
`exercises/EXERCISES.md`의 문제를 순서대로 풀어보세요.

### Step 4: 실행 및 테스트
완성한 코드를 실행해서 동작을 확인하세요.

## 실습 순서

### Level 1: 기본 CRUD (필수)
1. `BookDTO.kt` - 도서 데이터 구조 정의
2. `BookRepository.kt` - 데이터 저장/조회
3. `BookService.kt` - 비즈니스 로직
4. `BookController.kt` - REST API

### Level 2: 상태 관리 (중급)
1. `BookStatus.kt` - 도서 상태 정의
2. 대출/반납 기능 추가
3. 상태 전이 검증

### Level 3: 고급 기능 (도전)
1. 도서 검색 (제목, 저자)
2. 카테고리별 조회
3. 통계 API (대출 중인 책 수 등)

## 참고

| 파일 | 역할 |
|------|------|
| `BookDTO` | 데이터 구조 |
| `BookRepository` | 데이터 접근 |
| `BookService` | 비즈니스 로직 |
| `BookController` | REST API |
| `BookStatus` | 상태 관리 |

## 주의사항

- 이 코드는 **실습용**입니다.
- 실제 데이터베이스 연결은 하지 않습니다 (In-Memory 리스트 사용).

## 도움말

막히는 부분이 있다면:
1. `docs/` 폴더의 학습 자료를 참고하세요
2. 주석을 잘 읽어보세요
3. Spring Boot 공식 문서를 참고하세요

화이팅!
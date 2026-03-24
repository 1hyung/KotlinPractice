# Spring Boot 기초 실습

이 폴더는 Spring Boot의 핵심 개념을 Kotlin 코드로 학습하기 위한 실습 파일입니다.

## 관련 문서

- [02_SPRING_BASICS.md](../../../spring-kotlin-guide/docs/02_SPRING_BASICS.md) - Spring Boot 이론
- [practice/book/](../../../spring-kotlin-guide/practice/book/) - 실제 Spring Boot 프로젝트

## 파일 목록

| 파일 | 주제 | 내용 |
|-----|------|------|
| `01_DependencyInjection.kt` | 의존성 주입 (DI) | @Component, @Service, @Repository, 생성자 주입 |
| `02_ControllerBasics.kt` | 컨트롤러 기초 | @RestController, @GetMapping, @PostMapping |
| `03_ServiceLayer.kt` | 서비스 계층 | 비즈니스 로직 분리, 인터페이스-구현체 패턴 |
| `04_DTOPattern.kt` | DTO 패턴 | 요청/응답 DTO, Entity 변환 |
| `05_ExceptionHandling.kt` | 예외 처리 | @RestControllerAdvice, 커스텀 예외 |

## 학습 순서

```
1. DI 개념 이해 (01_DependencyInjection.kt)
        ↓
2. Controller 구조 (02_ControllerBasics.kt)
        ↓
3. Service 계층 (03_ServiceLayer.kt)
        ↓
4. DTO 패턴 (04_DTOPattern.kt)
        ↓
5. 예외 처리 (05_ExceptionHandling.kt)
        ↓
6. practice/book/ 프로젝트 분석
```

## 주의사항

이 파일들은 **개념 학습용 코드**입니다. 실제 Spring Boot 프로젝트에서 실행하려면:

1. [Spring Initializr](https://start.spring.io/)에서 프로젝트 생성
2. 의존성 추가: `spring-boot-starter-web`, `spring-boot-starter-webflux`
3. 이 코드들을 참고하여 실제 프로젝트에 적용

## 실제 실행 가능한 프로젝트

완전한 Spring Boot 프로젝트 예제는 `practice/book/`를 참고하세요:

```
spring-kotlin-guide/practice/book/
├── ui/BookController.kt           # REST API
├── service/BookService.kt         # 비즈니스 로직 인터페이스
├── infra/service/BookServiceImpl.kt   # 구현체
├── infra/repository/BookRepository.kt # 데이터 접근
└── model/dto/BookDTO.kt          # DTO
```

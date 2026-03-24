# Kotlin 학습 실습 코드

이 디렉토리는 Kotlin 문법을 직접 실행하며 학습하는 실습 코드입니다.

## 학습 순서

| 순서 | 폴더 | 주제 | 관련 문서 |
|------|------|------|----------|
| 1 | `01_basics/` | 기초 (변수, 타입, 연산자, 조건문, 반복문) | [01_KOTLIN_BASICS.md](../../spring-kotlin-guide/docs/01_KOTLIN_BASICS.md) |
| 2 | `02_function/` | 함수 (기본, 람다, 고차함수, 스코프함수) | [01_KOTLIN_BASICS.md](../../spring-kotlin-guide/docs/01_KOTLIN_BASICS.md) |
| 3 | `03_class/` | 클래스 (기본, data class, enum, object) | [04_SYNTAX_REFERENCE.md](../../spring-kotlin-guide/docs/04_SYNTAX_REFERENCE.md) |
| 4 | `04_collection/` | 컬렉션 (List, Set, Map, 컬렉션 함수) | [01_KOTLIN_BASICS.md](../../spring-kotlin-guide/docs/01_KOTLIN_BASICS.md) |
| 5 | `05_oop/` | OOP (상속, 인터페이스, 다형성) | [06_KOTLIN_CONVENTIONS.md](../../spring-kotlin-guide/docs/06_KOTLIN_CONVENTIONS.md) |
| 6 | `06_advanced/` | 고급 (제네릭, sealed class, 예외처리) | [04_SYNTAX_REFERENCE.md](../../spring-kotlin-guide/docs/04_SYNTAX_REFERENCE.md) |
| 7 | `07_spring/` | Spring Boot 기초 (DI, Controller, Service) | [02_SPRING_BASICS.md](../../spring-kotlin-guide/docs/02_SPRING_BASICS.md) |

## 폴더별 상세

### 01_basics - 기초 문법
```
01_basics/
├── intro/          # HelloKotlin, 디버깅
├── variable/       # 변수, 타입, Null Safety
├── operator/       # 산술, 비교, 논리 연산자
├── condition/      # if, when, Scanner 실습
├── loop/           # for, while, break, continue
└── array/          # 배열
```

### 02_function - 함수
```
02_function/
├── FunctionBasics.kt       # 함수 선언, 매개변수, 기본값
├── LambdaAndHigherOrder.kt # 람다, 고차함수, 클로저
├── EX_ScopeFunction.kt     # let, apply, run, also, with
└── EX_ExtensionFunction.kt # 확장 함수
```

### 03_class - 클래스
```
03_class/
├── ClassBasics.kt          # 생성자, 프로퍼티, init, data/enum/object
├── EX_Class.kt             # 클래스 기본 개념
├── EX_ObjectInstance.kt    # 객체와 인스턴스
└── User.kt                 # data class 예제
```

### 04_collection - 컬렉션
```
04_collection/
├── basics/
│   └── CollectionBasics.kt  # List, Set, Map 기초
└── functions/
    ├── Ex_map.kt           # 변환
    ├── EX_filter.kt        # 필터링
    ├── Ex_groupBy.kt       # 그룹화
    ├── EX_foldAndsumOf.kt  # 집계
    └── ...                 # 기타 컬렉션 함수
```

### 05_oop - 객체지향
```
05_oop/
├── Inheritance.kt   # 상속, 추상 클래스, 다형성
└── Interface.kt     # 인터페이스, 다중 구현, 위임
```

### 06_advanced - 고급
```
06_advanced/
├── Generics.kt           # 제네릭 클래스/함수, 타입 제한
├── SealedClass.kt        # sealed class/interface, 상태 기계
└── ExceptionHandling.kt  # try-catch, 커스텀 예외, runCatching
```

### 07_spring - Spring Boot 기초
```
07_spring/
├── README.md              # Spring Boot 실습 안내
├── 01_HelloSpring.kt      # 기본 애플리케이션
├── 02_DependencyInjection.kt  # DI 개념
└── 03_ControllerService.kt    # Controller-Service 패턴
```

## 실습 방법

1. **이론 먼저**: `spring-kotlin-guide/docs/` 문서를 읽습니다
2. **코드 실행**: 해당 `src/study/` 폴더의 코드를 직접 실행합니다
3. **수정해보기**: 코드를 수정하고 결과를 확인합니다
4. **연습문제**: `exercises/` 폴더에서 추가 연습합니다

## 실행 방법

IntelliJ IDEA에서:
1. `.kt` 파일 열기
2. `fun main()` 옆의 실행 버튼 클릭
3. 또는 `Ctrl+Shift+F10` (Windows) / `Ctrl+Shift+R` (Mac)

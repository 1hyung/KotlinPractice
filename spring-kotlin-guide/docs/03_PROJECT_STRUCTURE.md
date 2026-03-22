# Spring Boot 프로젝트 구조 이해하기

실제로 동작하는 **도서 대출 API**를 예제로, 프로젝트 구조를 처음부터 끝까지 따라가는 가이드입니다.

**선행 학습**: `01_KOTLIN_BASICS.md` → `02_SPRING_BASICS.md` 완료 후 학습하세요.

---

## 이 문서에서 만드는 것

> 도서관 API — 책을 등록하고, 대출하고, 반납하는 시스템

```
GET    /api/books          → 도서 목록 조회
POST   /api/books          → 도서 등록
GET    /api/books/{id}     → 도서 단건 조회
PUT    /api/books/{id}     → 도서 수정
DELETE /api/books/{id}     → 도서 삭제
POST   /api/books/{id}/borrow  → 도서 대출
POST   /api/books/{id}/return  → 도서 반납
```

---

## 1. 프로젝트 디렉토리 구조

```
src/main/kotlin/com/example/library/
│
├── LibraryApplication.kt          ← 1. 앱 진입점 (여기서 시작)
│
├── book/                          ← 도서 도메인 (핵심)
│   ├── model/
│   │   ├── BookDTO.kt             ← 2. 데이터 구조 정의
│   │   └── BookStatus.kt          ← 3. 상태 열거형
│   ├── BookRepository.kt          ← 4. DB 접근 계층
│   ├── BookService.kt             ← 5. 비즈니스 로직 (인터페이스)
│   ├── BookServiceImpl.kt         ← 6. 비즈니스 로직 (구현체)
│   └── BookController.kt          ← 7. HTTP API 처리
│
└── common/                        ← 공통 모듈
    ├── exception/
    │   ├── GlobalExceptionHandler.kt  ← 8. 전역 에러 처리
    │   └── CustomExceptions.kt        ← 9. 커스텀 예외
    └── response/
        └── ApiResponse.kt             ← 10. 공통 응답 형식
```

> **학습 순서**: 번호 순서대로 읽으면 자연스럽게 이해됩니다.

---

## 2. 데이터 흐름 전체 그림

```
[클라이언트]
    │
    │ HTTP 요청 (GET /api/books?status=AVAILABLE)
    ▼
[BookController]          ← @RestController: HTTP 요청/응답 담당
    │
    │ bookService.list(request) 호출
    ▼
[BookService (interface)]
    │
    │ (Spring이 자동으로 BookServiceImpl로 연결)
    ▼
[BookServiceImpl]         ← @Service: 비즈니스 로직 담당
    │
    │ bookRepository.findAll(request) 호출
    ▼
[BookRepository]          ← @Repository: DB 접근 담당
    │
    │ SQL 실행
    ▼
[Database]
    │
    │ 결과 반환 (List<BookDTO>)
    ▼
[BookServiceImpl]         ← 필요하면 추가 처리
    │
    │ 결과 반환
    ▼
[BookController]          ← ApiResponse로 감싸서 반환
    │
    │ HTTP 응답 (JSON)
    ▼
[클라이언트]
```

---

## 3. 코드 파일 하나씩 읽기

### 파일 1: LibraryApplication.kt — 앱 진입점

```kotlin
package com.example.library

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

// ① Spring Boot 앱임을 선언
//    내부적으로 @Configuration + @EnableAutoConfiguration + @ComponentScan 포함
@SpringBootApplication
@EnableScheduling  // ② @Scheduled 어노테이션 활성화
class LibraryApplication

fun main(args: Array<String>) {
    // ③ 앱 실행 — Netty(WebFlux) 또는 Tomcat(MVC) 서버가 시작됨
    runApplication<LibraryApplication>(*args)
}
```

**읽기 포인트**:
- `@SpringBootApplication` 하나가 수백 줄의 XML 설정을 대체
- `main` 함수가 진입점 — 여기서 Spring Container가 시작됨
- `@EnableScheduling`이 없으면 `@Scheduled` 어노테이션이 동작 안 함

---

### 파일 2: BookDTO.kt — 데이터 구조

```kotlin
package com.example.library.book.model

import java.time.LocalDateTime

// ① API 요청/응답 + DB 매핑을 모두 담당하는 DTO
//    실제 프로젝트에서는 Request/Response DTO를 분리하기도 함
data class BookDTO(
    var id: Long? = null,               // DB에서 자동 생성 → 생성 시엔 null
    var title: String = "",             // 도서 제목 (필수)
    var author: String = "",            // 저자 (필수)
    var isbn: String? = null,           // ISBN (선택)
    var price: Int = 0,                 // 가격
    var status: BookStatus = BookStatus.AVAILABLE,  // 상태 (기본: 대출 가능)
    var borrowedAt: LocalDateTime? = null,           // 대출 시작 시간 (대출 시에만 값 존재)
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
)

// ② 생성 요청 전용 DTO — ID, 시간 필드가 없음
data class CreateBookRequest(
    val title: String,
    val author: String,
    val isbn: String? = null,
    val price: Int = 0
)

// ③ DTO 간 변환 함수 — 확장 함수로 정의
fun CreateBookRequest.toBookDTO(): BookDTO {
    return BookDTO(
        title = this.title,
        author = this.author,
        isbn = this.isbn,
        price = this.price
    )
}
```

**읽기 포인트**:
- `var id: Long? = null` → DB 저장 전엔 ID가 없으므로 nullable
- `var status: BookStatus = BookStatus.AVAILABLE` → 기본값이 있으므로 생성 시 생략 가능
- `borrowedAt: LocalDateTime? = null` → 대출 중일 때만 값 존재 (nullable의 의미가 명확)
- 생성용/응답용 DTO를 분리하면 API 계약이 명확해짐

---

### 파일 3: BookStatus.kt — 상태 열거형

```kotlin
package com.example.library.book.model

// ① 도서가 가질 수 있는 상태를 열거형으로 제한
//    문자열("AVAILABLE", "borrowed")로 관리하면 오타 위험 → enum 사용
enum class BookStatus(
    val displayName: String,   // 화면에 표시할 이름
    val code: String           // 외부 시스템 연동용 코드
) {
    AVAILABLE("대출 가능", "BK001"),
    BORROWED("대출 중", "BK002"),
    RESERVED("예약됨", "BK003"),
    MAINTENANCE("점검 중", "BK004");

    // ② 상태 전이 규칙을 enum 안에 정의
    //    비즈니스 규칙이 여기 집중됨
    fun canBorrow(): Boolean = this == AVAILABLE
    fun canReturn(): Boolean = this == BORROWED

    fun nextStatusOnBorrow(): BookStatus {
        check(canBorrow()) { "대출 불가 상태입니다: $displayName" }
        return BORROWED
    }

    fun nextStatusOnReturn(): BookStatus {
        check(canReturn()) { "반납 불가 상태입니다: $displayName" }
        return AVAILABLE
    }
}
```

**읽기 포인트**:
- `enum class`로 가능한 상태를 컴파일 타임에 제한 → 잘못된 상태값 원천 차단
- 상태 전이 로직을 enum 안에 넣으면 Service 코드가 깔끔해짐
- `check()` → 조건이 false면 `IllegalStateException` 발생 (Kotlin 내장)

---

### 파일 4: BookRepository.kt — DB 접근 계층

```kotlin
package com.example.library.book

import com.example.library.book.model.BookDTO
import com.example.library.book.model.BookStatus
import org.springframework.stereotype.Repository

// ① @Repository = Spring이 이 클래스를 DB 접근 컴포넌트로 인식
//    예외를 Spring의 DataAccessException으로 변환해주는 역할도 함
@Repository
class BookRepository {

    // ② 실제 프로젝트에서는 여기에 DB 클라이언트가 주입됨
    //    이 예제는 메모리 저장소로 동작 원리를 설명함
    private val store = mutableMapOf<Long, BookDTO>()
    private var sequence = 1L

    // ③ 전체 조회 — 상태 필터링 포함
    suspend fun findAll(status: BookStatus? = null): List<BookDTO> {
        return if (status == null) {
            store.values.toList()
        } else {
            store.values.filter { it.status == status }
        }
    }

    // ④ 단건 조회 — 없으면 null 반환 (nullable 반환 타입이 의도를 명확히 함)
    suspend fun findById(id: Long): BookDTO? {
        return store[id]
    }

    // ⑤ 저장/수정 — id가 있으면 수정, 없으면 새로 저장
    suspend fun save(book: BookDTO): BookDTO {
        val saved = if (book.id == null) {
            // 새 도서: ID 자동 할당
            book.copy(id = sequence++)
        } else {
            book
        }
        store[saved.id!!] = saved
        return saved
    }

    // ⑥ 삭제
    suspend fun deleteById(id: Long): Boolean {
        return store.remove(id) != null
    }

    // ⑦ 조건 검색 — 실제 DB에서는 QueryDsl/JPQL로 대체
    suspend fun findByTitle(title: String): List<BookDTO> {
        return store.values.filter {
            it.title.contains(title, ignoreCase = true)
        }
    }
}
```

**읽기 포인트**:
- Repository는 **DB 접근만** 담당 — 비즈니스 규칙이 없어야 함
- 반환 타입 `BookDTO?`의 `?` → "없을 수 있다"는 의도가 타입에 드러남
- `suspend fun` → 비동기 DB 호출 (실제 R2DBC/Komapper에서 사용)
- 실제 프로젝트에서 이 부분이 `R2dbcEntityDatabase` 또는 `JpaRepository`로 교체됨

> **실제 프로젝트와의 연결**: Komapper 사용 시 `findAll()`이 이렇게 바뀜
> ```kotlin
> suspend fun findAll(status: BookStatus? = null): List<BookDTO> {
>     return db.runQuery {
>         QueryDsl.from(bookMeta)
>             .where {
>                 status?.let { bookMeta.status eq it }
>             }
>             .orderBy(bookMeta.createdAt.desc())
>     }
> }
> ```

---

### 파일 5: BookService.kt — 서비스 인터페이스

```kotlin
package com.example.library.book

import com.example.library.book.model.BookDTO
import com.example.library.book.model.BookStatus
import com.example.library.book.model.CreateBookRequest

// ① 인터페이스 = "BookService는 이런 기능을 제공한다"는 약속
//    Controller는 이 인터페이스만 알면 됨 (구현 방법 몰라도 됨)
interface BookService {

    // ② 목록 조회 — 상태 필터 옵션
    suspend fun list(status: BookStatus? = null): List<BookDTO>

    // ③ 단건 조회 — 없으면 예외 발생 (nullable 반환 X)
    //    "없으면 어떻게 할지"는 Service가 결정
    suspend fun findById(id: Long): BookDTO

    // ④ 등록
    suspend fun create(request: CreateBookRequest): BookDTO

    // ⑤ 수정
    suspend fun update(id: Long, request: CreateBookRequest): BookDTO

    // ⑥ 삭제
    suspend fun delete(id: Long)

    // ⑦ 대출 — 상태 변경 + 대출 시간 기록
    suspend fun borrow(id: Long): BookDTO

    // ⑧ 반납
    suspend fun returnBook(id: Long): BookDTO
}
```

**읽기 포인트**:
- 인터페이스는 **무엇을**만 정의, **어떻게**는 구현체가 결정
- `findById`가 `BookDTO?`가 아닌 `BookDTO` 반환 → "없으면 예외"라는 정책이 시그니처에 반영
- Controller 코드는 이 파일만 보면 서비스의 모든 기능을 파악 가능

---

### 파일 6: BookServiceImpl.kt — 비즈니스 로직 구현

```kotlin
package com.example.library.book

import com.example.library.book.model.BookDTO
import com.example.library.book.model.BookStatus
import com.example.library.book.model.CreateBookRequest
import com.example.library.book.model.toBookDTO
import com.example.library.common.exception.BookNotFoundException
import com.example.library.common.exception.BookNotAvailableException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service  // ① Spring이 이 클래스를 서비스 컴포넌트로 인식 + 빈으로 등록
class BookServiceImpl(
    // ② 생성자 주입 — Spring이 BookRepository 인스턴스를 자동으로 넣어줌
    private val bookRepository: BookRepository
) : BookService {  // ③ BookService 인터페이스 구현

    // ④ 로거 — KotlinLogging 방식 (람다로 문자열 생성 → 레벨 비활성화 시 문자열 생성 안 함)
    private val logger = KotlinLogging.logger {}

    // ────────────────────────────────
    // 조회
    // ────────────────────────────────

    override suspend fun list(status: BookStatus?): List<BookDTO> {
        logger.info { "도서 목록 조회: status=$status" }
        return bookRepository.findAll(status)
    }

    override suspend fun findById(id: Long): BookDTO {
        // ⑤ Repository는 null 반환, Service에서 예외로 변환
        //    "없으면 어떻게 할지"는 Service의 책임
        return bookRepository.findById(id)
            ?: throw BookNotFoundException(id)
    }

    // ────────────────────────────────
    // 생성 / 수정 / 삭제
    // ────────────────────────────────

    override suspend fun create(request: CreateBookRequest): BookDTO {
        logger.info { "도서 등록: title=${request.title}" }

        // ⑥ 확장 함수로 DTO 변환 (BookDTO.kt에 정의됨)
        val book = request.toBookDTO()
        val saved = bookRepository.save(book)

        logger.info { "도서 등록 완료: id=${saved.id}" }
        return saved
    }

    override suspend fun update(id: Long, request: CreateBookRequest): BookDTO {
        val existing = findById(id)  // ⑦ 없으면 여기서 예외 발생

        // ⑧ data class copy() — 기존 객체에서 일부 필드만 변경한 새 객체 생성
        val updated = existing.copy(
            title = request.title,
            author = request.author,
            isbn = request.isbn,
            price = request.price,
            updatedAt = LocalDateTime.now()
        )

        return bookRepository.save(updated)
    }

    override suspend fun delete(id: Long) {
        findById(id)  // 존재 확인 (없으면 예외)
        bookRepository.deleteById(id)
        logger.info { "도서 삭제: id=$id" }
    }

    // ────────────────────────────────
    // 핵심 비즈니스 로직: 대출 / 반납
    // ────────────────────────────────

    override suspend fun borrow(id: Long): BookDTO {
        val book = findById(id)

        // ⑨ 상태 검증을 enum에 위임 — Service 코드가 깔끔해짐
        if (!book.status.canBorrow()) {
            throw BookNotAvailableException(id, book.status)
        }

        val borrowed = book.copy(
            status = book.status.nextStatusOnBorrow(),  // AVAILABLE → BORROWED
            borrowedAt = LocalDateTime.now()
        )

        val saved = bookRepository.save(borrowed)
        logger.info { "대출 처리 완료: id=$id, status=${saved.status}" }
        return saved
    }

    override suspend fun returnBook(id: Long): BookDTO {
        val book = findById(id)

        if (!book.status.canReturn()) {
            throw BookNotAvailableException(id, book.status)
        }

        val returned = book.copy(
            status = book.status.nextStatusOnReturn(),  // BORROWED → AVAILABLE
            borrowedAt = null  // 반납 시 대출 시간 초기화
        )

        val saved = bookRepository.save(returned)
        logger.info { "반납 처리 완료: id=$id" }
        return saved
    }
}
```

**읽기 포인트**:
- `findById()` 재사용 → `update`, `delete`, `borrow`, `returnBook` 모두 호출. 중복 제거
- `book.copy(...)` → val 불변 객체를 업데이트하는 Kotlin 패턴
- 비즈니스 규칙(`canBorrow()`, `canReturn()`)이 enum에 있어서 Service가 단순
- `logger.info { "..." }` 람다 방식 → 로그 레벨 OFF 시 문자열 연산 자체를 건너뜀

---

### 파일 7: BookController.kt — HTTP API

```kotlin
package com.example.library.book

import com.example.library.book.model.BookDTO
import com.example.library.book.model.BookStatus
import com.example.library.book.model.CreateBookRequest
import com.example.library.common.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// ① @RestController = @Controller + @ResponseBody
//    모든 메서드의 반환값을 JSON으로 직렬화해서 HTTP 응답으로 보냄
@RestController
@RequestMapping("/api/books")  // ② 이 Controller의 기본 경로
class BookController(
    // ③ 인터페이스 타입으로 주입 — 구현체(BookServiceImpl)를 몰라도 됨
    private val bookService: BookService
) {

    // ④ GET /api/books?status=AVAILABLE
    @GetMapping
    suspend fun list(
        @RequestParam(required = false) status: BookStatus?  // 쿼리 파라미터 (선택)
    ): ApiResponse<List<BookDTO>> {
        val books = bookService.list(status)
        return ApiResponse.success(books)
    }

    // ⑤ GET /api/books/1
    @GetMapping("/{id}")
    suspend fun findById(
        @PathVariable id: Long  // URL 경로에서 추출
    ): ApiResponse<BookDTO> {
        val book = bookService.findById(id)
        return ApiResponse.success(book)
    }

    // ⑥ POST /api/books
    //    요청 Body: {"title": "Kotlin in Action", "author": "Dmitry"}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 성공 시 200 대신 201 반환
    suspend fun create(
        @RequestBody request: CreateBookRequest  // 요청 Body → 객체로 역직렬화
    ): ApiResponse<BookDTO> {
        val book = bookService.create(request)
        return ApiResponse.success(book)
    }

    // ⑦ PUT /api/books/1
    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: Long,
        @RequestBody request: CreateBookRequest
    ): ApiResponse<BookDTO> {
        val book = bookService.update(id, request)
        return ApiResponse.success(book)
    }

    // ⑧ DELETE /api/books/1
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)  // 성공 시 204 반환 (Body 없음)
    suspend fun delete(@PathVariable id: Long) {
        bookService.delete(id)
    }

    // ⑨ POST /api/books/1/borrow
    @PostMapping("/{id}/borrow")
    suspend fun borrow(@PathVariable id: Long): ApiResponse<BookDTO> {
        val book = bookService.borrow(id)
        return ApiResponse.success(book)
    }

    // ⑩ POST /api/books/1/return
    @PostMapping("/{id}/return")
    suspend fun returnBook(@PathVariable id: Long): ApiResponse<BookDTO> {
        val book = bookService.returnBook(id)
        return ApiResponse.success(book)
    }
}
```

**읽기 포인트**:
- Controller는 **HTTP 관련 처리만** (URL 매핑, 파라미터 추출, 응답 형식)
- 비즈니스 로직 없음 — 전부 `bookService`에 위임
- `@PathVariable` vs `@RequestParam` vs `@RequestBody` 차이 확인
- `@ResponseStatus(HttpStatus.CREATED)` → 직접 `ResponseEntity`를 쓰지 않고 어노테이션으로 상태코드 지정

---

### 파일 8: GlobalExceptionHandler.kt — 전역 에러 처리

```kotlin
package com.example.library.common.exception

import com.example.library.common.response.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

// ① @RestControllerAdvice = 모든 @RestController에 공통 적용되는 전역 핸들러
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = KotlinLogging.logger {}

    // ② 404 - 도서를 찾을 수 없음
    @ExceptionHandler(BookNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleBookNotFound(e: BookNotFoundException): ApiResponse<Nothing> {
        logger.warn { "도서 없음: ${e.message}" }
        return ApiResponse.error("NOT_FOUND", e.message ?: "도서를 찾을 수 없습니다")
    }

    // ③ 400 - 대출 불가 상태
    @ExceptionHandler(BookNotAvailableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBookNotAvailable(e: BookNotAvailableException): ApiResponse<Nothing> {
        logger.warn { "대출 불가: ${e.message}" }
        return ApiResponse.error("BOOK_NOT_AVAILABLE", e.message ?: "대출 불가 상태입니다")
    }

    // ④ 400 - @Valid 검증 실패
    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: WebExchangeBindException): ApiResponse<Nothing> {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ApiResponse.error("VALIDATION_FAILED", message)
    }

    // ⑤ 500 - 예상치 못한 서버 에러 (최후의 보루)
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneral(e: Exception): ApiResponse<Nothing> {
        // 운영에서는 여기에 Slack 알림, Sentry 에러 리포팅 등 추가
        logger.error(e) { "서버 오류 발생: ${e.message}" }
        return ApiResponse.error("INTERNAL_ERROR", "서버 오류가 발생했습니다")
    }
}
```

**읽기 포인트**:
- 각 Controller에 try-catch 없이도 에러가 일관된 형식으로 응답됨
- `@ExceptionHandler`의 순서: 구체적인 예외 → 일반 예외 순으로 선언
- `logger.error(e) { "..." }` → 예외 스택 트레이스도 함께 로깅

---

### 파일 9: CustomExceptions.kt — 커스텀 예외

```kotlin
package com.example.library.common.exception

import com.example.library.book.model.BookStatus

// ① 도메인 특화 예외 — 메시지가 명확해서 디버깅이 쉬움
class BookNotFoundException(id: Long) :
    RuntimeException("도서를 찾을 수 없습니다: id=$id")

// ② 상태 정보를 예외 메시지에 포함
class BookNotAvailableException(id: Long, status: BookStatus) :
    RuntimeException("도서 대출이 불가합니다: id=$id, 현재 상태=${status.displayName}")

// ③ 인증 관련 예외
class UnauthorizedException(message: String = "인증이 필요합니다") :
    RuntimeException(message)

// ④ 비즈니스 규칙 위반
class BusinessException(message: String) : RuntimeException(message)
```

**읽기 포인트**:
- `RuntimeException` 상속 → `@Transactional` 자동 롤백 대상
- 예외 이름만 봐도 무슨 상황인지 알 수 있게 네이밍
- `GlobalExceptionHandler`에서 각 예외 타입별로 HTTP 상태코드 매핑

---

### 파일 10: ApiResponse.kt — 공통 응답 형식

```kotlin
package com.example.library.common.response

// ① 모든 API 응답을 일관된 형식으로 감싸는 래퍼 클래스
//    클라이언트가 항상 같은 구조로 응답을 파싱할 수 있음
data class ApiResponse<T>(
    val success: Boolean,    // 성공 여부
    val data: T? = null,     // 성공 시 데이터
    val error: ErrorDetail? = null  // 실패 시 에러 정보
) {
    // ② 정적 팩토리 메서드 — companion object 안에 정의
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }

        fun error(code: String, message: String): ApiResponse<Nothing> {
            return ApiResponse(
                success = false,
                error = ErrorDetail(code = code, message = message)
            )
        }
    }
}

data class ErrorDetail(
    val code: String,      // 에러 코드 (클라이언트 분기 처리용)
    val message: String    // 사람이 읽을 수 있는 메시지
)
```

**API 응답 예시**:
```json
// 성공
{
  "success": true,
  "data": { "id": 1, "title": "Kotlin in Action", "status": "AVAILABLE" },
  "error": null
}

// 실패
{
  "success": false,
  "data": null,
  "error": { "code": "NOT_FOUND", "message": "도서를 찾을 수 없습니다: id=99" }
}
```

---

## 4. 전체 흐름 실습: 대출 API 추적하기

`POST /api/books/1/borrow` 요청이 처리되는 과정을 코드와 함께 따라가보세요.

```
① 클라이언트 → POST /api/books/1/borrow

② BookController.borrow(id=1) 실행
   └─ bookService.borrow(1) 호출

③ BookServiceImpl.borrow(id=1) 실행
   ├─ findById(1) 호출 → BookServiceImpl.findById(1)
   │   └─ bookRepository.findById(1)
   │       └─ store[1] 반환 → BookDTO(id=1, status=AVAILABLE, ...)
   │
   ├─ book.status.canBorrow() 확인
   │   └─ BookStatus.AVAILABLE.canBorrow() = true → 통과
   │
   ├─ book.copy(status=BORROWED, borrowedAt=now()) 실행
   │   └─ 새 BookDTO 생성 (원본 불변)
   │
   └─ bookRepository.save(borrowed) 저장 후 반환

④ BookController → ApiResponse.success(book) 감싸기

⑤ JSON 직렬화 후 클라이언트에 응답
   {
     "success": true,
     "data": { "id": 1, "status": "BORROWED", "borrowedAt": "2026-03-22T10:00:00" }
   }
```

**실습**: IntelliJ에서 `BookServiceImpl.borrow()`에 브레이크포인트를 걸고 디버거로 한 줄씩 따라가보세요.

---

## 5. 패턴별 코드 읽는 법

### 새 기능이 추가됐을 때 읽는 순서

```
1. BookService.kt     → "어떤 기능이 추가됐나?" (인터페이스 확인)
2. BookController.kt  → "어떤 URL로 호출하나?" (엔드포인트 확인)
3. BookServiceImpl.kt → "실제 로직이 뭔가?" (구현 확인)
4. BookRepository.kt  → "DB를 어떻게 조회하나?" (쿼리 확인)
```

### 버그가 생겼을 때 읽는 순서

```
1. 로그 확인           → 어느 계층에서 에러가 났는지
2. GlobalExceptionHandler → 어떤 예외가 잡혔는지
3. 에러 메시지의 클래스 → 해당 파일로 이동
4. 브레이크포인트      → 실제 실행 흐름 확인
```

### 처음 보는 어노테이션이 나왔을 때

```kotlin
@SomethingUnknown  // ← 이런 게 있으면
class SomeClass
```

1. 어노테이션에 커서 올리고 `Cmd + B` → 정의로 이동
2. 어노테이션 클래스 읽어서 `@Target`, `@Retention` 확인
3. 사용된 다른 파일 찾기: `Cmd + Shift + F`로 전체 검색

---

## 6. 실제 프로젝트에서 다른 점

이 예제는 학습용으로 단순화했습니다. 실제 프로젝트에서는 이런 차이가 있습니다:

| 이 예제 | 실제 프로젝트 |
|--------|-------------|
| 메모리 저장소 (`MutableMap`) | R2DBC / JPA / Komapper로 실제 DB 접근 |
| `CreateBookRequest` 하나 | 기능별 Request/Response DTO 분리 |
| 단일 모듈 | 멀티 모듈 (common, api, batch 분리) |
| 인증 없음 | JWT + Spring Security |
| 단순 예외 | 에러 코드 enum + 에러 응답 표준화 |
| 단일 서비스 | Kafka로 다른 서비스와 이벤트 통신 |

---

## 7. 다음 단계: 직접 확장해보기

이 예제를 기반으로 다음 기능을 직접 추가해보세요.

**Level 1**: (02_SPRING_BASICS.md 내용)
- [ ] `SearchRequest`에 저자명 검색 추가
- [ ] 대출 중인 도서 목록만 조회하는 API 추가
- [ ] `@Valid`로 요청 데이터 검증 추가 (`@NotBlank`, `@Min`)

**Level 2**: (09_DATABASE.md 내용)
- [ ] 메모리 저장소를 R2DBC + PostgreSQL로 교체
- [ ] Flyway로 테이블 생성 마이그레이션 작성
- [ ] 페이징 처리 추가 (page, size 파라미터)

**Level 3**: (07_KAFKA.md 내용)
- [ ] 대출 시 `book.borrowed` 토픽으로 Kafka 이벤트 발행
- [ ] Kafka Consumer로 알림 서비스 구현

**Level 4**: (08_GRPC.md 내용)
- [ ] 도서 조회 기능을 gRPC로 구현
- [ ] REST Controller에서 gRPC 클라이언트 호출

---

## 학습 체크리스트

### 구조 이해
- [ ] 디렉토리 구조와 각 파일의 역할 설명할 수 있다
- [ ] Controller → Service → Repository 흐름을 그릴 수 있다
- [ ] DTO, Entity, Request/Response의 차이를 설명할 수 있다

### 코드 읽기
- [ ] `@RestController`, `@Service`, `@Repository` 어노테이션 역할 이해
- [ ] `@PathVariable`, `@RequestParam`, `@RequestBody` 차이 이해
- [ ] `suspend fun`이 왜 사용되는지 설명할 수 있다
- [ ] `GlobalExceptionHandler`가 왜 필요한지 설명할 수 있다

### 실습
- [ ] 대출 API 흐름을 디버거로 처음부터 끝까지 추적
- [ ] 새 API 엔드포인트 하나 직접 추가
- [ ] 커스텀 예외 하나 만들어서 GlobalExceptionHandler에 연결

---

## 참고 자료

| 주제 | 파일 |
|------|------|
| Kotlin 문법 (data class, enum, suspend) | `01_KOTLIN_BASICS.md` |
| Spring DI, Controller, @Transactional | `02_SPRING_BASICS.md` |
| 문법 빠른 참조 | `04_SYNTAX_REFERENCE.md` |
| 테스트 코드 작성 | `05_TESTING.md` |
| 코드 컨벤션 & 에러 해결 | `06_KOTLIN_CONVENTIONS.md` |
| DB 연동 (R2DBC, JPA) | `09_DATABASE.md` |

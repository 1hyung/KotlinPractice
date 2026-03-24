package study.`07_spring`

import java.time.LocalDateTime

/**
 * Spring 예외 처리 패턴 학습
 *
 * Spring에서 예외를 체계적으로 처리하는 방법을 학습합니다.
 * @RestControllerAdvice를 사용한 전역 예외 처리가 핵심입니다.
 *
 * 관련 문서: spring-kotlin-guide/docs/02_SPRING_BASICS.md
 */

fun main() {
    println("=== Spring 예외 처리 학습 ===\n")

    // ========================================
    // 1. 커스텀 예외 정의
    // ========================================
    println("--- 1. 커스텀 예외 정의 ---")
    demonstrateCustomExceptions()

    // ========================================
    // 2. 예외 응답 형식
    // ========================================
    println("\n--- 2. 예외 응답 형식 ---")
    demonstrateErrorResponse()

    // ========================================
    // 3. @RestControllerAdvice
    // ========================================
    println("\n--- 3. @RestControllerAdvice ---")
    printControllerAdvice()

    // ========================================
    // 4. 실제 예외 처리 흐름
    // ========================================
    println("\n--- 4. 실제 예외 처리 흐름 ---")
    demonstrateExceptionFlow()

    // ========================================
    // 5. 검증 예외 처리
    // ========================================
    println("\n--- 5. 검증 예외 처리 ---")
    printValidationHandling()
}

// ========================================
// 커스텀 예외
// ========================================

// 기본 비즈니스 예외
open class BusinessException(
    val errorCode: String,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

// 리소스를 찾을 수 없음
class ResourceNotFoundException(
    resourceType: String,
    resourceId: Any
) : BusinessException(
    errorCode = "RESOURCE_NOT_FOUND",
    message = "$resourceType 을(를) 찾을 수 없습니다: $resourceId"
)

// 잘못된 상태
class InvalidStateException(
    message: String
) : BusinessException(
    errorCode = "INVALID_STATE",
    message = message
)

// 중복 리소스
class DuplicateResourceException(
    resourceType: String,
    field: String,
    value: Any
) : BusinessException(
    errorCode = "DUPLICATE_RESOURCE",
    message = "$resourceType 의 $field 가 이미 존재합니다: $value"
)

// 권한 없음
class UnauthorizedException(
    message: String = "인증이 필요합니다"
) : BusinessException(
    errorCode = "UNAUTHORIZED",
    message = message
)

// 접근 거부
class ForbiddenException(
    message: String = "접근 권한이 없습니다"
) : BusinessException(
    errorCode = "FORBIDDEN",
    message = message
)

fun demonstrateCustomExceptions() {
    val exceptions = listOf(
        ResourceNotFoundException("Book", 123),
        InvalidStateException("대출 중인 도서는 삭제할 수 없습니다"),
        DuplicateResourceException("User", "email", "test@email.com"),
        UnauthorizedException(),
        ForbiddenException("관리자만 접근할 수 있습니다")
    )

    for (e in exceptions) {
        println("${e.errorCode}: ${e.message}")
    }
}

// ========================================
// 예외 응답 형식
// ========================================

data class ErrorResponse(
    val timestamp: String = LocalDateTime.now().toString(),
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
    val path: String? = null,
    val details: List<FieldError>? = null
)

data class FieldError(
    val field: String,
    val value: Any?,
    val reason: String
)

fun demonstrateErrorResponse() {
    // 일반 에러 응답
    val errorResponse = ErrorResponse(
        status = 404,
        error = "Not Found",
        code = "RESOURCE_NOT_FOUND",
        message = "Book 을(를) 찾을 수 없습니다: 123",
        path = "/api/books/123"
    )
    println("일반 에러 응답:")
    println("""
    {
        "timestamp": "${errorResponse.timestamp}",
        "status": ${errorResponse.status},
        "error": "${errorResponse.error}",
        "code": "${errorResponse.code}",
        "message": "${errorResponse.message}",
        "path": "${errorResponse.path}"
    }
    """.trimIndent())

    // 검증 에러 응답
    val validationError = ErrorResponse(
        status = 400,
        error = "Bad Request",
        code = "VALIDATION_ERROR",
        message = "입력값이 올바르지 않습니다",
        path = "/api/users",
        details = listOf(
            FieldError("email", "invalid", "이메일 형식이 올바르지 않습니다"),
            FieldError("age", -5, "나이는 0 이상이어야 합니다")
        )
    )
    println("\n검증 에러 응답:")
    println("""
    {
        "status": ${validationError.status},
        "code": "${validationError.code}",
        "message": "${validationError.message}",
        "details": [
            {"field": "email", "value": "invalid", "reason": "이메일 형식이 올바르지 않습니다"},
            {"field": "age", "value": -5, "reason": "나이는 0 이상이어야 합니다"}
        ]
    }
    """.trimIndent())
}

fun printControllerAdvice() {
    val advice = """
    @RestControllerAdvice로 전역 예외 처리:

    @RestControllerAdvice
    class GlobalExceptionHandler {

        // 1. 리소스 없음 (404)
        @ExceptionHandler(ResourceNotFoundException::class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        fun handleNotFound(e: ResourceNotFoundException): ErrorResponse {
            return ErrorResponse(
                status = 404,
                error = "Not Found",
                code = e.errorCode,
                message = e.message
            )
        }

        // 2. 잘못된 요청 (400)
        @ExceptionHandler(InvalidStateException::class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        fun handleBadRequest(e: InvalidStateException): ErrorResponse {
            return ErrorResponse(
                status = 400,
                error = "Bad Request",
                code = e.errorCode,
                message = e.message
            )
        }

        // 3. 인증 필요 (401)
        @ExceptionHandler(UnauthorizedException::class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        fun handleUnauthorized(e: UnauthorizedException): ErrorResponse {
            return ErrorResponse(
                status = 401,
                error = "Unauthorized",
                code = e.errorCode,
                message = e.message
            )
        }

        // 4. 권한 없음 (403)
        @ExceptionHandler(ForbiddenException::class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        fun handleForbidden(e: ForbiddenException): ErrorResponse {
            return ErrorResponse(
                status = 403,
                error = "Forbidden",
                code = e.errorCode,
                message = e.message
            )
        }

        // 5. 검증 실패 (400)
        @ExceptionHandler(MethodArgumentNotValidException::class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        fun handleValidation(e: MethodArgumentNotValidException): ErrorResponse {
            val details = e.bindingResult.fieldErrors.map { error ->
                FieldError(
                    field = error.field,
                    value = error.rejectedValue,
                    reason = error.defaultMessage ?: "Invalid"
                )
            }
            return ErrorResponse(
                status = 400,
                error = "Bad Request",
                code = "VALIDATION_ERROR",
                message = "입력값이 올바르지 않습니다",
                details = details
            )
        }

        // 6. 기타 예외 (500)
        @ExceptionHandler(Exception::class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        fun handleException(e: Exception): ErrorResponse {
            // 로깅
            logger.error("Unexpected error", e)

            return ErrorResponse(
                status = 500,
                error = "Internal Server Error",
                code = "INTERNAL_ERROR",
                message = "서버 오류가 발생했습니다"
            )
        }
    }

    핵심 포인트:
    - @RestControllerAdvice: 모든 Controller에 적용
    - @ExceptionHandler: 특정 예외 타입 처리
    - @ResponseStatus: HTTP 상태 코드 설정
    - 예외 타입별로 적절한 상태 코드와 메시지 반환
    """.trimIndent()

    println(advice)
}

// ========================================
// 예외 처리 흐름
// ========================================

// 시뮬레이션용 Service
class BookServiceSim {
    private val books = mutableMapOf(
        1L to mapOf("id" to 1L, "title" to "Kotlin", "status" to "AVAILABLE"),
        2L to mapOf("id" to 2L, "title" to "Spring", "status" to "BORROWED")
    )

    fun getBook(id: Long): Map<String, Any> {
        return books[id] ?: throw ResourceNotFoundException("Book", id)
    }

    fun deleteBook(id: Long) {
        val book = getBook(id)
        if (book["status"] == "BORROWED") {
            throw InvalidStateException("대출 중인 도서는 삭제할 수 없습니다")
        }
        books.remove(id)
    }
}

fun demonstrateExceptionFlow() {
    val service = BookServiceSim()

    // 1. 정상 조회
    println("1. 정상 조회:")
    try {
        val book = service.getBook(1)
        println("   성공: $book")
    } catch (e: BusinessException) {
        println("   실패: [${e.errorCode}] ${e.message}")
    }

    // 2. 존재하지 않는 리소스
    println("\n2. 존재하지 않는 리소스:")
    try {
        service.getBook(999)
    } catch (e: BusinessException) {
        println("   실패: [${e.errorCode}] ${e.message}")
    }

    // 3. 잘못된 상태에서 작업
    println("\n3. 잘못된 상태에서 작업:")
    try {
        service.deleteBook(2)  // 대출 중인 책 삭제 시도
    } catch (e: BusinessException) {
        println("   실패: [${e.errorCode}] ${e.message}")
    }
}

fun printValidationHandling() {
    val validation = """
    Bean Validation + 예외 처리:

    1. DTO에 검증 어노테이션 추가
    data class CreateUserRequest(
        @field:NotBlank(message = "이름은 필수입니다")
        val name: String,

        @field:Email(message = "이메일 형식이 올바르지 않습니다")
        val email: String,

        @field:Min(value = 0, message = "나이는 0 이상이어야 합니다")
        val age: Int,

        @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        val password: String
    )

    2. Controller에서 @Valid 사용
    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): UserResponse {
        return userService.create(request)
    }

    3. GlobalExceptionHandler에서 처리
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ErrorResponse {
        val details = e.bindingResult.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                value = error.rejectedValue,
                reason = error.defaultMessage ?: "Invalid"
            )
        }
        return ErrorResponse(
            status = 400,
            code = "VALIDATION_ERROR",
            message = "입력값이 올바르지 않습니다",
            details = details
        )
    }

    주요 검증 어노테이션:
    - @NotNull: null 불가
    - @NotBlank: null, 빈 문자열, 공백만 불가
    - @NotEmpty: null, 빈 컬렉션 불가
    - @Email: 이메일 형식
    - @Size(min, max): 길이/크기 제한
    - @Min, @Max: 숫자 범위
    - @Pattern: 정규식 매칭
    """.trimIndent()

    println(validation)
}

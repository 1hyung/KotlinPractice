package study.`07_spring`

import java.time.LocalDateTime

/**
 * DTO (Data Transfer Object) 패턴 학습
 *
 * DTO는 계층 간 데이터 전송을 위한 객체입니다.
 * Entity와 분리하여 API 변경에 유연하게 대응할 수 있습니다.
 *
 * 관련 문서: spring-kotlin-guide/docs/02_SPRING_BASICS.md
 * 실제 예제: spring-kotlin-guide/practice/book/model/dto/BookDTO.kt
 */

fun main() {
    println("=== DTO 패턴 학습 ===\n")

    // ========================================
    // 1. DTO vs Entity
    // ========================================
    println("--- 1. DTO vs Entity ---")
    printDtoVsEntity()

    // ========================================
    // 2. 요청/응답 DTO 분리
    // ========================================
    println("\n--- 2. 요청/응답 DTO 분리 ---")
    demonstrateRequestResponseDto()

    // ========================================
    // 3. Entity ↔ DTO 변환
    // ========================================
    println("\n--- 3. Entity ↔ DTO 변환 ---")
    demonstrateConversion()

    // ========================================
    // 4. 중첩 DTO
    // ========================================
    println("\n--- 4. 중첩 DTO ---")
    demonstrateNestedDto()

    // ========================================
    // 5. 실제 DTO 예제
    // ========================================
    println("\n--- 5. 실제 DTO 예제 ---")
    printRealDtoExample()
}

fun printDtoVsEntity() {
    val comparison = """
    Entity vs DTO:

    ┌──────────────────────────────────────────────────────────────┐
    │  Entity (Database)           │  DTO (API)                    │
    ├──────────────────────────────┼───────────────────────────────┤
    │  데이터베이스 테이블 매핑     │  API 요청/응답 데이터         │
    │  JPA 어노테이션 포함         │  직렬화 어노테이션 포함       │
    │  모든 필드 포함              │  필요한 필드만 포함           │
    │  연관관계 포함               │  필요한 정보만 포함           │
    │  비즈니스 로직 포함 가능     │  순수 데이터만                │
    └──────────────────────────────┴───────────────────────────────┘

    왜 분리하는가?
    1. 보안: 민감한 정보(password 등) 노출 방지
    2. 유연성: API 변경 시 Entity 영향 없음
    3. 성능: 필요한 데이터만 전송
    4. 관심사 분리: DB 구조와 API 구조 독립

    예시:
    // Entity (DB에 저장)
    @Entity
    class UserEntity(
        @Id val id: Long,
        val name: String,
        val email: String,
        val password: String,      // 민감 정보!
        val createdAt: LocalDateTime,
        val isDeleted: Boolean     // 소프트 삭제
    )

    // DTO (API 응답)
    data class UserResponse(
        val id: Long,
        val name: String,
        val email: String
        // password, isDeleted 제외!
    )
    """.trimIndent()

    println(comparison)
}

// ========================================
// 요청/응답 DTO 분리
// ========================================

// 생성 요청 DTO
data class CreateBookRequest(
    val title: String,
    val author: String,
    val isbn: String?,
    val category: String,
    val price: Int
)

// 수정 요청 DTO (일부 필드만)
data class UpdateBookRequest(
    val title: String?,
    val author: String?,
    val price: Int?
)

// 응답 DTO
data class BookResponse(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String?,
    val category: String,
    val price: Int,
    val status: String,
    val createdAt: String
)

// 목록 응답 DTO (간략 정보)
data class BookSummaryResponse(
    val id: Long,
    val title: String,
    val author: String,
    val status: String
)

fun demonstrateRequestResponseDto() {
    println("요청/응답 DTO 분리 예시:\n")

    // 1. 생성 요청
    val createRequest = CreateBookRequest(
        title = "Kotlin in Action",
        author = "Dmitry Jemerov",
        isbn = "978-1617293290",
        category = "프로그래밍",
        price = 45000
    )
    println("생성 요청: $createRequest")

    // 2. 수정 요청 (일부만)
    val updateRequest = UpdateBookRequest(
        title = null,        // 변경 안 함
        author = null,       // 변경 안 함
        price = 42000        // 가격만 변경
    )
    println("수정 요청: $updateRequest")

    // 3. 응답 (전체 정보)
    val response = BookResponse(
        id = 1,
        title = "Kotlin in Action",
        author = "Dmitry Jemerov",
        isbn = "978-1617293290",
        category = "프로그래밍",
        price = 42000,
        status = "AVAILABLE",
        createdAt = "2025-01-15T10:30:00"
    )
    println("응답: $response")

    // 4. 목록 응답 (간략)
    val summaryResponse = BookSummaryResponse(
        id = 1,
        title = "Kotlin in Action",
        author = "Dmitry Jemerov",
        status = "AVAILABLE"
    )
    println("목록 응답: $summaryResponse")
}

// ========================================
// Entity ↔ DTO 변환
// ========================================

// Entity
data class BookEntity(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String?,
    val category: String,
    val price: Int,
    val status: BookStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val isDeleted: Boolean = false
)

enum class BookStatus { AVAILABLE, BORROWED, RESERVED, LOST }

// 변환 확장 함수
fun BookEntity.toResponse(): BookResponse {
    return BookResponse(
        id = this.id,
        title = this.title,
        author = this.author,
        isbn = this.isbn,
        category = this.category,
        price = this.price,
        status = this.status.name,
        createdAt = this.createdAt.toString()
    )
}

fun BookEntity.toSummary(): BookSummaryResponse {
    return BookSummaryResponse(
        id = this.id,
        title = this.title,
        author = this.author,
        status = this.status.name
    )
}

fun CreateBookRequest.toEntity(): BookEntity {
    return BookEntity(
        id = 0,  // DB에서 생성
        title = this.title,
        author = this.author,
        isbn = this.isbn,
        category = this.category,
        price = this.price,
        status = BookStatus.AVAILABLE,
        createdAt = LocalDateTime.now(),
        updatedAt = null
    )
}

fun demonstrateConversion() {
    println("Entity ↔ DTO 변환:\n")

    // 1. Request → Entity
    val request = CreateBookRequest(
        title = "Clean Code",
        author = "Robert C. Martin",
        isbn = "978-0132350884",
        category = "프로그래밍",
        price = 35000
    )
    val entity = request.toEntity()
    println("1. Request → Entity")
    println("   Request: $request")
    println("   Entity: $entity")

    // 2. Entity → Response
    val savedEntity = entity.copy(id = 1)
    val response = savedEntity.toResponse()
    println("\n2. Entity → Response")
    println("   Entity: $savedEntity")
    println("   Response: $response")

    // 3. Entity → Summary
    val summary = savedEntity.toSummary()
    println("\n3. Entity → Summary")
    println("   Summary: $summary")

    println("""

    변환 방법들:
    1. 확장 함수 (권장)
       fun BookEntity.toResponse(): BookResponse

    2. companion object
       companion object {
           fun from(entity: BookEntity): BookResponse
       }

    3. MapStruct (라이브러리)
       @Mapper
       interface BookMapper {
           fun toResponse(entity: BookEntity): BookResponse
       }
    """.trimIndent())
}

// ========================================
// 중첩 DTO
// ========================================

data class AuthorDto(
    val id: Long,
    val name: String,
    val nationality: String
)

data class PublisherDto(
    val id: Long,
    val name: String,
    val address: String
)

data class BookDetailResponse(
    val id: Long,
    val title: String,
    val author: AuthorDto,         // 중첩 DTO
    val publisher: PublisherDto?,  // nullable 중첩 DTO
    val categories: List<String>,  // 컬렉션
    val tags: Set<String>          // Set
)

fun demonstrateNestedDto() {
    val bookDetail = BookDetailResponse(
        id = 1,
        title = "Effective Kotlin",
        author = AuthorDto(
            id = 1,
            name = "Marcin Moskala",
            nationality = "Poland"
        ),
        publisher = PublisherDto(
            id = 1,
            name = "Kt. Academy",
            address = "Warsaw, Poland"
        ),
        categories = listOf("프로그래밍", "Kotlin", "베스트프랙티스"),
        tags = setOf("kotlin", "best-practices", "advanced")
    )

    println("중첩 DTO 예시:")
    println(bookDetail)
    println("\n작가: ${bookDetail.author.name}")
    println("출판사: ${bookDetail.publisher?.name}")
    println("카테고리: ${bookDetail.categories.joinToString(", ")}")
}

fun printRealDtoExample() {
    val example = """
    실제 프로젝트 예제 (practice/book/model/dto/):

    // BookDTO.kt
    data class BookDTO(
        var bookId: Long? = null,
        var title: String = "",
        var author: String = "",
        var isbn: String? = null,
        var category: String = "",
        var price: Int = 0,
        var status: BookStatus = BookStatus.AVAILABLE,
        var publishedDate: LocalDateTime? = null,
        var createdAt: LocalDateTime? = null,
        var updatedAt: LocalDateTime? = null
    ) {
        // Entity로 변환
        fun toEntity(): BookEntity {
            return BookEntity(
                id = this.bookId ?: 0,
                title = this.title,
                author = this.author,
                // ...
            )
        }

        companion object {
            // Entity에서 DTO 생성
            fun from(entity: BookEntity): BookDTO {
                return BookDTO(
                    bookId = entity.id,
                    title = entity.title,
                    // ...
                )
            }
        }
    }

    // Controller에서 사용
    @PostMapping
    suspend fun createBook(@RequestBody book: BookDTO): ResponseEntity<BookDTO> {
        val created = bookService.createBook(book)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    요점:
    - var + 기본값: JSON 역직렬화 용이
    - toEntity(), from(): 변환 메서드 포함
    - nullable 필드: 선택적 입력 지원
    """.trimIndent()

    println(example)
}

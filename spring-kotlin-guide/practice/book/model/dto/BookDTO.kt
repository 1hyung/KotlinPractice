package com.example.practice.book.model.dto

import com.example.practice.book.model.enums.BookStatus
import java.time.LocalDateTime

/**
 * 도서 DTO (Data Transfer Object)
 *
 * API 요청/응답에 사용되는 데이터 클래스입니다.
 */
data class BookDTO(
    var bookId: Long?,              // 도서 ID (Primary Key)
    var title: String,              // 제목
    var author: String,             // 저자
    var isbn: String?,              // ISBN
    var category: String,           // 카테고리 (IT, 문학, 과학 등)
    var price: Int,                 // 가격
    var status: BookStatus,         // 도서 상태 (AVAILABLE, BORROWED 등)
    var publishedDate: LocalDateTime?,  // 출판일
    var createdAt: LocalDateTime?,  // 등록일
    var updatedAt: LocalDateTime?   // 수정일
) {
    /**
     * TODO: 도서가 대출 가능한지 확인하는 메서드를 작성하세요
     *
     * 힌트:
     * - status가 AVAILABLE이어야 합니다
     * - 반환 타입: Boolean
     */
    fun isAvailable(): Boolean {
        // TODO: 여기에 코드 작성
        return status == BookStatus.AVAILABLE
    }

    /**
     * TODO: 도서 정보를 요약하는 메서드를 작성하세요
     *
     * 힌트:
     * - 형식: "[ID] 제목 - 저자 (가격원)"
     * - 예: "[1] Kotlin in Action - Dmitry (30000원)"
     * - 반환 타입: String
     */
    fun summary(): String {
        // TODO: 여기에 코드 작성
        return "[$bookId] $title - $author (${price}원)"
    }
}

/**
 * 도서 생성 요청 DTO
 *
 * 클라이언트가 도서를 생성할 때 보내는 데이터입니다.
 * bookId, createdAt, updatedAt은 서버에서 자동으로 생성됩니다.
 */
data class CreateBookRequest(
    val title: String,
    val author: String,
    val isbn: String?,
    val category: String,
    val price: Int,
    val publishedDate: LocalDateTime?
) {
    /**
     * CreateBookRequest를 BookDTO로 변환합니다.
     */
    fun toDTO(): BookDTO {
        return BookDTO(
            bookId = null,  // 아직 생성 안 됨
            title = this.title,
            author = this.author,
            isbn = this.isbn,
            category = this.category,
            price = this.price,
            status = BookStatus.AVAILABLE,  // 기본값: 대출 가능
            publishedDate = this.publishedDate,
            createdAt = null,  // Repository에서 설정
            updatedAt = null
        )
    }
}

/**
 * 도서 수정 요청 DTO
 */
data class UpdateBookRequest(
    val title: String?,
    val author: String?,
    val isbn: String?,
    val category: String?,
    val price: Int?
)

/**
 * 도서 검색 요청 DTO
 */
data class SearchBookRequest(
    val title: String?,         // 제목 검색 (부분 일치)
    val author: String?,        // 저자 검색 (부분 일치)
    val category: String?,      // 카테고리 필터
    val status: BookStatus?,    // 상태 필터
    val page: Int = 0,          // 페이지 번호 (0부터 시작)
    val size: Int = 10          // 페이지 크기
)
package com.example.practice.book.ui

import com.example.practice.book.model.dto.*
import com.example.practice.book.model.enums.BookStatus
import com.example.practice.book.service.BookService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * 도서 컨트롤러
 *
 * REST API 엔드포인트를 정의합니다.
 */
@RestController
@RequestMapping("/practice/books")
class BookController(
    private val bookService: BookService  // DI (Dependency Injection)
) {

    /**
     * 모든 도서 조회
     *
     * GET /practice/books
     */
    @GetMapping
    suspend fun getAllBooks(): ApiListResponse<BookDTO> {
        val books = bookService.getAllBooks()
        return ApiListResponse(
            list = books,
            count = books.size.toLong(),
            mode = true
        )
    }

    /**
     * ID로 도서 조회
     *
     * GET /practice/books/{bookId}
     */
    @GetMapping("/{bookId}")
    suspend fun getBookById(@PathVariable bookId: Long): ApiDataResponse<BookDTO> {
        val book = bookService.getBookById(bookId)
        return ApiDataResponse(data = book, mode = true)
    }

    /**
     * 도서 생성
     *
     * POST /practice/books
     *
     * TODO: 이 메서드를 완성하세요
     *
     * 힌트:
     * 1. @PostMapping 어노테이션 사용
     * 2. @RequestBody로 CreateBookRequest 받기
     * 3. bookService.createBook() 호출
     * 4. ApiDataResponse로 감싸서 반환
     * 5. @ResponseStatus(HttpStatus.CREATED) 추가 (201 응답 코드)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createBook(
        @RequestBody request: CreateBookRequest
    ): ApiDataResponse<BookDTO> {
        // TODO: 여기에 코드 작성
        val created = bookService.createBook(request)
        return ApiDataResponse(data = created, mode = true)
    }

    /**
     * 도서 수정
     *
     * PUT /practice/books/{bookId}
     *
     * TODO: 이 메서드를 완성하세요
     */
    @PutMapping("/{bookId}")
    suspend fun updateBook(
        @PathVariable bookId: Long,
        @RequestBody request: UpdateBookRequest
    ): ApiDataResponse<BookDTO> {
        // TODO: 여기에 코드 작성
        val updated = bookService.updateBook(bookId, request)
        return ApiDataResponse(data = updated, mode = true)
    }

    /**
     * 도서 삭제
     *
     * DELETE /practice/books/{bookId}
     *
     * TODO: 이 메서드를 완성하세요
     */
    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteBook(@PathVariable bookId: Long) {
        // TODO: 여기에 코드 작성
        bookService.deleteBook(bookId)
    }

    /**
     * 도서 검색
     *
     * POST /practice/books/search
     */
    @PostMapping("/search")
    suspend fun searchBooks(
        @RequestBody request: SearchBookRequest
    ): ApiListResponse<BookDTO> {
        val books = bookService.searchBooks(request)
        val count = bookService.countBooks(request)
        return ApiListResponse(
            list = books,
            count = count,
            mode = true
        )
    }

    /**
     * 도서 대출
     *
     * PUT /practice/books/{bookId}/borrow
     *
     * TODO: 이 메서드를 완성하세요
     */
    @PutMapping("/{bookId}/borrow")
    suspend fun borrowBook(@PathVariable bookId: Long): ApiDataResponse<BookDTO> {
        // TODO: 여기에 코드 작성
        val borrowed = bookService.borrowBook(bookId)
        return ApiDataResponse(data = borrowed, mode = true)
    }

    /**
     * 도서 반납
     *
     * PUT /practice/books/{bookId}/return
     *
     * TODO: 이 메서드를 완성하세요
     */
    @PutMapping("/{bookId}/return")
    suspend fun returnBook(@PathVariable bookId: Long): ApiDataResponse<BookDTO> {
        // TODO: 여기에 코드 작성
        val returned = bookService.returnBook(bookId)
        return ApiDataResponse(data = returned, mode = true)
    }

    /**
     * 도서 예약
     *
     * PUT /practice/books/{bookId}/reserve
     */
    @PutMapping("/{bookId}/reserve")
    suspend fun reserveBook(@PathVariable bookId: Long): ApiDataResponse<BookDTO> {
        val reserved = bookService.reserveBook(bookId)
        return ApiDataResponse(data = reserved, mode = true)
    }

    /**
     * 도서 상태 변경
     *
     * PUT /practice/books/{bookId}/status
     */
    @PutMapping("/{bookId}/status")
    suspend fun changeStatus(
        @PathVariable bookId: Long,
        @RequestBody request: ChangeStatusRequest
    ): ApiDataResponse<BookDTO> {
        val updated = bookService.changeStatus(bookId, request.status)
        return ApiDataResponse(data = updated, mode = true)
    }

    /**
     * 카테고리별 통계
     *
     * GET /practice/books/stats/category
     */
    @GetMapping("/stats/category")
    suspend fun getStatsByCategory(): ApiDataResponse<Map<String, Int>> {
        val stats = bookService.getStatsByCategory()
        return ApiDataResponse(data = stats, mode = true)
    }

    /**
     * 상태별 통계
     *
     * GET /practice/books/stats/status
     *
     * TODO: 이 메서드를 완성하세요
     */
    @GetMapping("/stats/status")
    suspend fun getStatsByStatus(): ApiDataResponse<Map<BookStatus, Int>> {
        // TODO: 여기에 코드 작성
        val stats = bookService.getStatsByStatus()
        return ApiDataResponse(data = stats, mode = true)
    }
}

/**
 * API 응답 래퍼 클래스
 */
data class ApiDataResponse<T>(
    val data: T,
    val mode: Boolean
)

data class ApiListResponse<T>(
    val list: List<T>,
    val count: Long,
    val mode: Boolean
)

/**
 * 상태 변경 요청 DTO
 */
data class ChangeStatusRequest(
    val status: BookStatus
)
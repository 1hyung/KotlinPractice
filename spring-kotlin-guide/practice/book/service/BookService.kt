package com.example.practice.book.service

import com.example.practice.book.model.dto.BookDTO
import com.example.practice.book.model.dto.CreateBookRequest
import com.example.practice.book.model.dto.SearchBookRequest
import com.example.practice.book.model.dto.UpdateBookRequest
import com.example.practice.book.model.enums.BookStatus

/**
 * 도서 서비스 인터페이스
 *
 * 비즈니스 로직을 정의합니다.
 */
interface BookService {

    /**
     * 모든 도서 조회
     */
    suspend fun getAllBooks(): List<BookDTO>

    /**
     * ID로 도서 조회
     *
     * @throws IllegalArgumentException 도서를 찾을 수 없는 경우
     */
    suspend fun getBookById(bookId: Long): BookDTO

    /**
     * 도서 생성
     */
    suspend fun createBook(request: CreateBookRequest): BookDTO

    /**
     * 도서 수정
     *
     * @throws IllegalArgumentException 도서를 찾을 수 없는 경우
     */
    suspend fun updateBook(bookId: Long, request: UpdateBookRequest): BookDTO

    /**
     * 도서 삭제
     *
     * @return 삭제 성공 여부
     */
    suspend fun deleteBook(bookId: Long): Boolean

    /**
     * 도서 검색
     */
    suspend fun searchBooks(request: SearchBookRequest): List<BookDTO>

    /**
     * 검색 결과 개수
     */
    suspend fun countBooks(request: SearchBookRequest): Long

    /**
     * 도서 대출
     *
     * @throws IllegalArgumentException 도서를 찾을 수 없는 경우
     * @throws IllegalStateException 대출 불가능한 상태인 경우
     */
    suspend fun borrowBook(bookId: Long): BookDTO

    /**
     * 도서 반납
     *
     * @throws IllegalArgumentException 도서를 찾을 수 없는 경우
     * @throws IllegalStateException 반납 불가능한 상태인 경우
     */
    suspend fun returnBook(bookId: Long): BookDTO

    /**
     * 도서 예약
     */
    suspend fun reserveBook(bookId: Long): BookDTO

    /**
     * 도서 상태 변경
     *
     * @throws IllegalArgumentException 도서를 찾을 수 없는 경우
     * @throws IllegalStateException 유효하지 않은 상태 전이인 경우
     */
    suspend fun changeStatus(bookId: Long, newStatus: BookStatus): BookDTO

    /**
     * 카테고리별 통계
     */
    suspend fun getStatsByCategory(): Map<String, Int>

    /**
     * 상태별 통계
     */
    suspend fun getStatsByStatus(): Map<BookStatus, Int>
}
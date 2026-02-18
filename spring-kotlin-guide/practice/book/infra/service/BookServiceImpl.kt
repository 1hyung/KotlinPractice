package com.example.practice.book.infra.service

import com.example.practice.book.infra.repository.BookRepository
import com.example.practice.book.model.dto.BookDTO
import com.example.practice.book.model.dto.CreateBookRequest
import com.example.practice.book.model.dto.SearchBookRequest
import com.example.practice.book.model.dto.UpdateBookRequest
import com.example.practice.book.model.enums.BookStatus
import com.example.practice.book.model.enums.InvalidBookStatusTransitionException
import com.example.practice.book.model.enums.canTransitionTo
import com.example.practice.book.service.BookService
import org.springframework.stereotype.Service

/**
 * 도서 서비스 구현체
 *
 * 비즈니스 로직을 구현합니다.
 */
@Service
class BookServiceImpl(
    private val bookRepository: BookRepository  // DI (Dependency Injection)
) : BookService {

    override suspend fun getAllBooks(): List<BookDTO> {
        return bookRepository.findAll()
    }

    override suspend fun getBookById(bookId: Long): BookDTO {
        return bookRepository.findById(bookId)
            ?: throw IllegalArgumentException("도서를 찾을 수 없습니다: $bookId")
    }

    override suspend fun createBook(request: CreateBookRequest): BookDTO {
        // TODO: CreateBookRequest를 BookDTO로 변환하고 저장하세요
        //
        // 힌트:
        // 1. request.toDTO()로 BookDTO 생성
        // 2. bookRepository.save()로 저장
        // 3. 저장된 BookDTO 반환

        val bookDTO = request.toDTO()
        return bookRepository.save(bookDTO)
    }

    override suspend fun updateBook(bookId: Long, request: UpdateBookRequest): BookDTO {
        // TODO: 기존 도서를 찾아서 수정하세요
        //
        // 힌트:
        // 1. getBookById()로 기존 도서 조회
        // 2. request의 nullable 필드들을 체크해서 null이 아닌 것만 변경
        // 3. copy()를 사용해서 새로운 BookDTO 생성
        // 4. bookRepository.save()로 저장

        val existing = getBookById(bookId)

        val updated = existing.copy(
            title = request.title ?: existing.title,
            author = request.author ?: existing.author,
            isbn = request.isbn ?: existing.isbn,
            category = request.category ?: existing.category,
            price = request.price ?: existing.price
        )

        return bookRepository.save(updated)
    }

    override suspend fun deleteBook(bookId: Long): Boolean {
        return bookRepository.deleteById(bookId)
    }

    override suspend fun searchBooks(request: SearchBookRequest): List<BookDTO> {
        return bookRepository.search(request)
    }

    override suspend fun countBooks(request: SearchBookRequest): Long {
        return bookRepository.count(request)
    }

    override suspend fun borrowBook(bookId: Long): BookDTO {
        // TODO: 도서를 대출 상태로 변경하세요
        //
        // 힌트:
        // 1. getBookById()로 도서 조회
        // 2. 현재 상태가 AVAILABLE인지 확인
        // 3. AVAILABLE이 아니면 IllegalStateException 발생
        // 4. changeStatus()를 사용해서 BORROWED로 변경

        val book = getBookById(bookId)

        if (book.status != BookStatus.AVAILABLE) {
            throw IllegalStateException(
                "대출 불가능한 상태입니다: ${book.status.description}"
            )
        }

        return changeStatus(bookId, BookStatus.BORROWED)
    }

    override suspend fun returnBook(bookId: Long): BookDTO {
        // TODO: 도서를 반납 처리하세요 (BORROWED → AVAILABLE)
        //
        // 힌트:
        // 1. getBookById()로 도서 조회
        // 2. 현재 상태가 BORROWED인지 확인
        // 3. BORROWED가 아니면 IllegalStateException 발생
        // 4. changeStatus()를 사용해서 AVAILABLE로 변경

        val book = getBookById(bookId)

        if (book.status != BookStatus.BORROWED) {
            throw IllegalStateException(
                "반납할 수 없는 상태입니다: ${book.status.description}"
            )
        }

        return changeStatus(bookId, BookStatus.AVAILABLE)
    }

    override suspend fun reserveBook(bookId: Long): BookDTO {
        // TODO: 도서를 예약 처리하세요 (AVAILABLE → RESERVED)

        val book = getBookById(bookId)

        if (book.status != BookStatus.AVAILABLE) {
            throw IllegalStateException(
                "예약할 수 없는 상태입니다: ${book.status.description}"
            )
        }

        return changeStatus(bookId, BookStatus.RESERVED)
    }

    override suspend fun changeStatus(bookId: Long, newStatus: BookStatus): BookDTO {
        // TODO: 도서 상태를 변경하세요
        //
        // 힌트:
        // 1. getBookById()로 도서 조회
        // 2. 현재 상태에서 새 상태로 전이 가능한지 검증
        //    - BookStatus.canTransitionTo() 확장 함수 사용
        // 3. 불가능하면 InvalidBookStatusTransitionException 발생
        // 4. copy()로 상태 변경된 새 BookDTO 생성
        // 5. bookRepository.save()로 저장

        val book = getBookById(bookId)

        // 상태 전이 검증
        if (!book.status.canTransitionTo(newStatus)) {
            throw InvalidBookStatusTransitionException(book.status, newStatus)
        }

        val updated = book.copy(status = newStatus)
        return bookRepository.save(updated)
    }

    override suspend fun getStatsByCategory(): Map<String, Int> {
        return bookRepository.countByCategory()
    }

    override suspend fun getStatsByStatus(): Map<BookStatus, Int> {
        return bookRepository.countByStatus()
    }
}
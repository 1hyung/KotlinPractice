package com.example.practice.book.infra.repository

import com.example.practice.book.model.dto.BookDTO
import com.example.practice.book.model.dto.SearchBookRequest
import com.example.practice.book.model.enums.BookStatus
import kotlinx.coroutines.delay
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 도서 Repository
 *
 * 실제로는 데이터베이스에 접근하지만, 실습에서는 In-Memory 리스트를 사용합니다.
 */
@Repository
class BookRepository {
    // In-Memory 데이터 저장소 (실제로는 Database)
    private val books = mutableListOf<BookDTO>()
    private var nextId = 1L

    /**
     * 모든 도서 조회
     *
     * suspend 함수: Reactive (Non-blocking) 처리
     */
    suspend fun findAll(): List<BookDTO> {
        delay(50)  // DB 조회 시뮬레이션 (50ms)
        return books.toList()
    }

    /**
     * ID로 도서 조회
     */
    suspend fun findById(bookId: Long): BookDTO? {
        delay(30)
        return books.find { it.bookId == bookId }
    }

    /**
     * 도서 저장 (생성 또는 수정)
     *
     * TODO: 이 메서드를 완성하세요
     *
     * 힌트:
     * 1. bookId가 null이면 새로운 도서 생성
     *    - nextId를 사용해서 ID 부여
     *    - createdAt, updatedAt을 현재 시간으로 설정
     *    - books 리스트에 추가
     * 2. bookId가 있으면 기존 도서 수정
     *    - 기존 도서를 찾아서 삭제
     *    - updatedAt을 현재 시간으로 설정
     *    - books 리스트에 추가
     * 3. 저장된 도서 반환
     */
    suspend fun save(book: BookDTO): BookDTO {
        delay(100)  // DB 저장 시뮬레이션

        val saved = if (book.bookId == null) {
            // TODO: 새로운 도서 생성 로직
            book.copy(
                bookId = nextId++,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ).also { books.add(it) }
        } else {
            // TODO: 기존 도서 수정 로직
            books.removeIf { it.bookId == book.bookId }
            book.copy(
                updatedAt = LocalDateTime.now()
            ).also { books.add(it) }
        }

        return saved
    }

    /**
     * 도서 삭제
     *
     * TODO: 이 메서드를 완성하세요
     *
     * 힌트:
     * - books.removeIf를 사용하세요
     * - 삭제 성공 시 true, 실패 시 false 반환
     */
    suspend fun deleteById(bookId: Long): Boolean {
        delay(50)
        // TODO: 여기에 코드 작성
        return books.removeIf { it.bookId == bookId }
    }

    /**
     * 도서 검색
     *
     * TODO: 이 메서드를 완성하세요
     *
     * 힌트:
     * 1. 모든 조건을 AND로 연결해서 필터링하세요
     * 2. null인 조건은 무시하세요 (모든 결과 포함)
     * 3. title, author는 contains()로 부분 일치 검색
     * 4. category, status는 정확히 일치 검색
     * 5. 페이징 처리: skip(page * size).take(size)
     */
    suspend fun search(request: SearchBookRequest): List<BookDTO> {
        delay(80)

        var filtered = books.asSequence()

        // TODO: title 조건 추가 (부분 일치)
        request.title?.let { title ->
            filtered = filtered.filter {
                it.title.contains(title, ignoreCase = true)
            }
        }

        // TODO: author 조건 추가 (부분 일치)
        request.author?.let { author ->
            filtered = filtered.filter {
                it.author.contains(author, ignoreCase = true)
            }
        }

        // TODO: category 조건 추가 (정확히 일치)
        request.category?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // TODO: status 조건 추가 (정확히 일치)
        request.status?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        // TODO: 페이징 처리
        return filtered
            .drop(request.page * request.size)
            .take(request.size)
            .toList()
    }

    /**
     * 검색 결과 개수
     *
     * TODO: 이 메서드를 완성하세요
     *
     * 힌트:
     * - search()와 동일한 조건으로 필터링
     * - 페이징 없이 전체 개수만 반환
     */
    suspend fun count(request: SearchBookRequest): Long {
        delay(50)

        var filtered = books.asSequence()

        request.title?.let { title ->
            filtered = filtered.filter {
                it.title.contains(title, ignoreCase = true)
            }
        }

        request.author?.let { author ->
            filtered = filtered.filter {
                it.author.contains(author, ignoreCase = true)
            }
        }

        request.category?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        request.status?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        return filtered.count().toLong()
    }

    /**
     * 카테고리별 도서 개수
     *
     * TODO: 이 메서드를 완성하세요
     *
     * 힌트:
     * - groupBy { it.category }를 사용하세요
     * - 각 그룹의 개수를 세서 Map으로 반환
     */
    suspend fun countByCategory(): Map<String, Int> {
        delay(60)
        // TODO: 여기에 코드 작성
        return books.groupBy { it.category }
            .mapValues { it.value.size }
    }

    /**
     * 상태별 도서 개수
     *
     * TODO: 이 메서드를 완성하세요
     */
    suspend fun countByStatus(): Map<BookStatus, Int> {
        delay(60)
        // TODO: 여기에 코드 작성
        return books.groupBy { it.status }
            .mapValues { it.value.size }
    }

    /**
     * 초기 데이터 생성 (테스트용)
     */
    fun initSampleData() {
        books.clear()
        nextId = 1L

        val sampleBooks = listOf(
            BookDTO(
                bookId = null,
                title = "Kotlin in Action",
                author = "Dmitry Jemerov",
                isbn = "9781617293290",
                category = "IT",
                price = 30000,
                status = BookStatus.AVAILABLE,
                publishedDate = LocalDateTime.of(2017, 2, 1, 0, 0),
                createdAt = null,
                updatedAt = null
            ),
            BookDTO(
                bookId = null,
                title = "Spring Boot in Practice",
                author = "Somnath Musib",
                isbn = "9781617298813",
                category = "IT",
                price = 35000,
                status = BookStatus.BORROWED,
                publishedDate = LocalDateTime.of(2022, 7, 1, 0, 0),
                createdAt = null,
                updatedAt = null
            ),
            BookDTO(
                bookId = null,
                title = "Clean Code",
                author = "Robert Martin",
                isbn = "9780132350884",
                category = "IT",
                price = 28000,
                status = BookStatus.AVAILABLE,
                publishedDate = LocalDateTime.of(2008, 8, 1, 0, 0),
                createdAt = null,
                updatedAt = null
            )
        )

        sampleBooks.forEach { book ->
            books.add(
                book.copy(
                    bookId = nextId++,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            )
        }
    }
}
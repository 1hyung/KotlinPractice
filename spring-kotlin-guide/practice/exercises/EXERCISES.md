# 실습 문제

단계별로 문제를 풀면서 프로젝트 구조를 익혀봅시다.

---

## Level 1: 기본 CRUD (필수)

### Exercise 1-1: TODO 완성하기

각 파일의 `// TODO:` 주석을 찾아서 코드를 완성하세요.

**체크리스트**:
- [ ] `BookDTO.kt` - isAvailable(), summary() 메서드
- [ ] `BookRepository.kt` - save(), deleteById(), search(), count() 메서드
- [ ] `BookServiceImpl.kt` - createBook(), updateBook(), borrowBook() 등
- [ ] `BookController.kt` - createBook(), updateBook(), deleteBook() 등

**테스트 방법**:
```bash
# 애플리케이션 실행
./gradlew bootRun

# 또는 IDE에서 Application 실행
```

### Exercise 1-2: API 테스트하기

Postman 또는 curl로 API를 테스트하세요.

#### 1. 도서 생성
```bash
curl -X POST http://localhost:8080/practice/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Effective Kotlin",
    "author": "Marcin Moskala",
    "isbn": "9788395495229",
    "category": "IT",
    "price": 32000,
    "publishedDate": "2019-10-01T00:00:00"
  }'
```

**예상 응답**:
```json
{
  "data": {
    "bookId": 1,
    "title": "Effective Kotlin",
    "author": "Marcin Moskala",
    "isbn": "9788395495229",
    "category": "IT",
    "price": 32000,
    "status": "AVAILABLE",
    "publishedDate": "2019-10-01T00:00:00",
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  },
  "mode": true
}
```

#### 2. 전체 도서 조회
```bash
curl http://localhost:8080/practice/books
```

#### 3. 도서 수정
```bash
curl -X PUT http://localhost:8080/practice/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "price": 28000
  }'
```

#### 4. 도서 대출
```bash
curl -X PUT http://localhost:8080/practice/books/1/borrow
```

#### 5. 도서 반납
```bash
curl -X PUT http://localhost:8080/practice/books/1/return
```

#### 6. 도서 삭제
```bash
curl -X DELETE http://localhost:8080/practice/books/1
```

**과제**: 위 6가지 API를 모두 테스트하고, 정상 동작하는지 확인하세요.

---

## Level 2: 검색 기능 (중급)

### Exercise 2-1: 도서 검색 API 구현

이미 구현되어 있지만, 다양한 조건으로 검색을 테스트해보세요.

#### 제목으로 검색
```bash
curl -X POST http://localhost:8080/practice/books/search \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Kotlin"
  }'
```

#### 카테고리 + 상태로 검색
```bash
curl -X POST http://localhost:8080/practice/books/search \
  -H "Content-Type: application/json" \
  -d '{
    "category": "IT",
    "status": "AVAILABLE",
    "page": 0,
    "size": 10
  }'
```

**과제**:
1. 제목, 저자, 카테고리, 상태를 조합해서 5가지 검색 쿼리를 만들어보세요
2. 페이징이 제대로 동작하는지 확인하세요 (page=0, page=1 비교)

---

### Exercise 2-2: 복합 조건 검색

**문제**: 다음 조건을 만족하는 도서를 찾는 검색 쿼리를 작성하세요.

1. IT 카테고리의 대출 가능한 책
2. 저자 이름에 "Martin"이 포함된 책
3. 가격이 30000원 이하인 책 (힌트: Repository에 새 메서드 추가 필요)

**과제**:
```kotlin
// BookRepository에 다음 메서드 추가
suspend fun findByPriceRange(minPrice: Int, maxPrice: Int): List<BookDTO>

// BookService에도 추가
suspend fun getBooksByPriceRange(minPrice: Int, maxPrice: Int): List<BookDTO>

// Controller에도 엔드포인트 추가
@GetMapping("/price-range")
suspend fun getBooksByPriceRange(
    @RequestParam minPrice: Int,
    @RequestParam maxPrice: Int
): ApiListResponse<BookDTO>
```

---

## Level 3: 통계 기능 (고급)

### Exercise 3-1: 통계 API 테스트

#### 카테고리별 통계
```bash
curl http://localhost:8080/practice/books/stats/category
```

**예상 응답**:
```json
{
  "data": {
    "IT": 5,
    "문학": 3,
    "과학": 2
  },
  "mode": true
}
```

#### 상태별 통계
```bash
curl http://localhost:8080/practice/books/stats/status
```

**과제**:
1. 샘플 데이터를 10개 이상 추가하세요
2. 각 카테고리별로 최소 2개 이상의 책이 있어야 합니다
3. 통계 API를 호출해서 결과를 확인하세요

---

### Exercise 3-2: 대출 통계 추가

**문제**: 다음 통계 기능을 추가하세요.

```kotlin
// BookRepository에 추가
suspend fun getBorrowedBooksCount(): Int
suspend fun getAvailableBooksCount(): Int
suspend fun getMostPopularCategory(): String  // 가장 책이 많은 카테고리

// BookService에 추가
suspend fun getBorrowStatistics(): BorrowStatistics

// DTO 정의
data class BorrowStatistics(
    val totalBooks: Int,
    val availableBooks: Int,
    val borrowedBooks: Int,
    val borrowRate: Double,  // 대출률 = borrowed / total * 100
    val mostPopularCategory: String
)

// Controller에 엔드포인트 추가
@GetMapping("/stats/borrow")
suspend fun getBorrowStatistics(): ApiDataResponse<BorrowStatistics>
```

**테스트**:
```bash
curl http://localhost:8080/practice/books/stats/borrow
```

**예상 응답**:
```json
{
  "data": {
    "totalBooks": 10,
    "availableBooks": 7,
    "borrowedBooks": 3,
    "borrowRate": 30.0,
    "mostPopularCategory": "IT"
  },
  "mode": true
}
```

---

## Level 4: 실전 과제 (도전)

### Exercise 4-1: 예외 처리 개선

**문제**: 커스텀 예외를 만들어보세요.

```kotlin
// BookException.kt
class BookException(
    val error: BookError,
    override val message: String = error.message
) : RuntimeException(message)

enum class BookError(val code: String, val message: String) {
    NOT_FOUND("BOOK001", "도서를 찾을 수 없습니다"),
    NOT_AVAILABLE("BOOK002", "대출 가능한 상태가 아닙니다"),
    ALREADY_BORROWED("BOOK003", "이미 대출 중인 도서입니다"),
    INVALID_STATUS_TRANSITION("BOOK004", "유효하지 않은 상태 전이입니다")
}
```

**과제**:
1. BookException과 BookError를 정의하세요
2. BookServiceImpl의 모든 IllegalArgumentException, IllegalStateException을 BookException으로 변경하세요
3. @RestControllerAdvice로 전역 예외 처리를 추가하세요

---

### Exercise 4-2: 도서 대출 이력 추가

**문제**: 누가 언제 대출했는지 기록하는 기능을 추가하세요.

```kotlin
// BorrowHistoryDTO.kt
data class BorrowHistoryDTO(
    val historyId: Long?,
    val bookId: Long,
    val borrowerName: String,
    val borrowDate: LocalDateTime,
    val returnDate: LocalDateTime?,
    val status: BorrowStatus  // BORROWED, RETURNED
)

enum class BorrowStatus {
    BORROWED,
    RETURNED
}
```

**과제**:
1. BorrowHistoryRepository 만들기
2. BookService.borrowBook()에서 이력 생성
3. BookService.returnBook()에서 이력 업데이트
4. 도서별 대출 이력 조회 API 추가

**API**:
```
GET /practice/books/{bookId}/history
```

---

### Exercise 4-3: 도서 리뷰 기능 추가

**문제**: 도서에 대한 리뷰를 작성하고 조회하는 기능을 추가하세요.

**요구사항**:
1. ReviewDTO 정의 (리뷰 ID, 도서 ID, 리뷰어 이름, 평점, 내용, 작성일)
2. ReviewRepository, ReviewService, ReviewController 구현
3. 도서별 리뷰 목록 조회
4. 도서별 평균 평점 계산
5. 평점 높은 순으로 도서 정렬

**API**:
```
POST   /practice/books/{bookId}/reviews
GET    /practice/books/{bookId}/reviews
GET    /practice/books/top-rated
```

---

## Level 5: 고급 기능 (최고급)

### Exercise 5-1: 메시징 추가

**문제**: 도서 대출/반납 시 이벤트를 발행하세요.

**구현 힌트**:
```kotlin
@Service
class BookEventService {
    suspend fun sendBorrowEvent(book: BookDTO) {
        // 이벤트 발행 로직
        println("Book borrowed: ${book.bookId}")
    }

    suspend fun sendReturnEvent(book: BookDTO) {
        // 이벤트 발행 로직
        println("Book returned: ${book.bookId}")
    }
}
```

---

### Exercise 5-2: 캐싱 추가

**문제**: 도서 조회 시 캐시를 사용하세요.

**구현 힌트**:
```kotlin
@Service
class BookCacheService {
    private val cache = mutableMapOf<Long, BookDTO>()

    suspend fun get(bookId: Long): BookDTO? {
        return cache[bookId]
    }

    suspend fun set(book: BookDTO) {
        book.bookId?.let { cache[it] = book }
    }

    suspend fun invalidate(bookId: Long) {
        cache.remove(bookId)
    }
}
```

---

### Exercise 5-3: 스케줄러 추가

**문제**: 매일 자정에 연체된 도서를 확인하는 스케줄러를 만드세요.

**구현 힌트**:
```kotlin
@Component
class BookScheduler(
    private val bookService: BookService
) {
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    fun checkOverdueBooks() = runBlocking {
        val overdueBooks = bookService.findOverdueBooks()

        overdueBooks.forEach { book ->
            println("Overdue book: ${book.title}")
            // 알림 발송 등 처리
        }
    }
}
```

---

## 학습 체크리스트

**Level 1: 기본 CRUD**
- [ ] 모든 TODO 완성
- [ ] 6가지 API 테스트 완료
- [ ] Postman Collection 생성

**Level 2: 검색 기능**
- [ ] 복합 조건 검색 테스트
- [ ] 가격 범위 검색 기능 추가
- [ ] 페이징 동작 확인

**Level 3: 통계 기능**
- [ ] 카테고리/상태별 통계 테스트
- [ ] 대출 통계 기능 추가
- [ ] 샘플 데이터 10개 이상

**Level 4: 실전 과제**
- [ ] 커스텀 예외 처리
- [ ] 대출 이력 기능 추가
- [ ] 리뷰 기능 추가

**Level 5: 고급 기능**
- [ ] 메시징 연동
- [ ] 캐싱 적용
- [ ] 스케줄러 구현

---

## 다음 단계

모든 연습 문제를 풀었다면:

1. **더 복잡한 기능 추가하기**
   - 예약 기능
   - 회원 관리
   - 알림 시스템

2. **테스트 코드 작성하기**
   - 단위 테스트
   - 통합 테스트

3. **코드 리뷰 받기**
   - 다른 개발자에게 코드 리뷰 요청
   - 피드백 반영

축하합니다!
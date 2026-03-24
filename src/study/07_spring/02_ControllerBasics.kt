package study.`07_spring`

/**
 * Controller 기초 개념 학습
 *
 * Spring MVC의 Controller는 HTTP 요청을 받아 처리하고 응답을 반환합니다.
 * 이 파일은 Controller 구조를 순수 Kotlin으로 시뮬레이션합니다.
 *
 * 관련 문서: spring-kotlin-guide/docs/02_SPRING_BASICS.md
 * 실제 예제: spring-kotlin-guide/practice/book/ui/BookController.kt
 */

fun main() {
    println("=== Controller 기초 학습 ===\n")

    // ========================================
    // 1. 기본 Controller 구조
    // ========================================
    println("--- 1. 기본 Controller 구조 ---")
    printControllerStructure()

    // ========================================
    // 2. HTTP 메서드별 어노테이션
    // ========================================
    println("\n--- 2. HTTP 메서드별 어노테이션 ---")
    printHttpMethods()

    // ========================================
    // 3. 요청 파라미터 처리
    // ========================================
    println("\n--- 3. 요청 파라미터 처리 ---")
    printRequestParameters()

    // ========================================
    // 4. 응답 처리
    // ========================================
    println("\n--- 4. 응답 처리 ---")
    printResponseHandling()

    // ========================================
    // 5. 실제 Controller 예제
    // ========================================
    println("\n--- 5. 실제 Controller 예제 ---")
    printRealControllerExample()

    // ========================================
    // 6. Reactive Controller (WebFlux)
    // ========================================
    println("\n--- 6. Reactive Controller ---")
    printReactiveController()
}

fun printControllerStructure() {
    val structure = """
    Controller의 기본 구조:

    @RestController                     // JSON 응답을 반환하는 Controller
    @RequestMapping("/api/users")       // 기본 경로 설정
    class UserController(
        private val userService: UserService   // Service 주입
    ) {
        @GetMapping                     // GET /api/users
        fun getAllUsers(): List<User> {
            return userService.findAll()
        }

        @GetMapping("/{id}")            // GET /api/users/1
        fun getUser(@PathVariable id: Long): User {
            return userService.findById(id)
        }

        @PostMapping                    // POST /api/users
        fun createUser(@RequestBody request: CreateUserRequest): User {
            return userService.create(request)
        }
    }

    핵심 어노테이션:
    - @RestController = @Controller + @ResponseBody
    - @RequestMapping: 기본 URL 경로
    - @GetMapping, @PostMapping, @PutMapping, @DeleteMapping: HTTP 메서드
    """.trimIndent()

    println(structure)
}

fun printHttpMethods() {
    val methods = """
    HTTP 메서드와 어노테이션:

    | HTTP 메서드 | 어노테이션      | 용도           | 예시                    |
    |------------|----------------|----------------|------------------------|
    | GET        | @GetMapping    | 조회           | GET /users             |
    | POST       | @PostMapping   | 생성           | POST /users            |
    | PUT        | @PutMapping    | 전체 수정      | PUT /users/1           |
    | PATCH      | @PatchMapping  | 부분 수정      | PATCH /users/1         |
    | DELETE     | @DeleteMapping | 삭제           | DELETE /users/1        |

    RESTful API 설계 원칙:
    - GET: 멱등성 O, 안전함 (조회만)
    - POST: 멱등성 X (같은 요청 여러 번 = 여러 개 생성)
    - PUT: 멱등성 O (같은 요청 여러 번 = 같은 결과)
    - DELETE: 멱등성 O
    """.trimIndent()

    println(methods)
}

fun printRequestParameters() {
    val params = """
    요청 파라미터 처리:

    1. @PathVariable - URL 경로 변수
       @GetMapping("/users/{id}")
       fun getUser(@PathVariable id: Long): User

       요청: GET /users/123
       결과: id = 123

    2. @RequestParam - 쿼리 파라미터
       @GetMapping("/users")
       fun searchUsers(
           @RequestParam(required = false) name: String?,
           @RequestParam(defaultValue = "1") page: Int
       ): List<User>

       요청: GET /users?name=Alice&page=2
       결과: name = "Alice", page = 2

    3. @RequestBody - 요청 본문 (JSON)
       @PostMapping("/users")
       fun createUser(@RequestBody request: CreateUserRequest): User

       요청: POST /users
       Body: {"name": "Alice", "email": "alice@email.com"}
       결과: request = CreateUserRequest(name="Alice", email="alice@email.com")

    4. @RequestHeader - HTTP 헤더
       @GetMapping("/users")
       fun getUsers(@RequestHeader("Authorization") token: String): List<User>
    """.trimIndent()

    println(params)
}

fun printResponseHandling() {
    val response = """
    응답 처리:

    1. 기본 반환 (200 OK)
       @GetMapping("/users/{id}")
       fun getUser(@PathVariable id: Long): User {
           return userService.findById(id)
       }

    2. ResponseEntity로 상태 코드 제어
       @GetMapping("/users/{id}")
       fun getUser(@PathVariable id: Long): ResponseEntity<User> {
           val user = userService.findById(id)
           return if (user != null) {
               ResponseEntity.ok(user)           // 200 OK
           } else {
               ResponseEntity.notFound().build() // 404 Not Found
           }
       }

    3. 생성 응답 (201 Created)
       @PostMapping("/users")
       fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<User> {
           val user = userService.create(request)
           return ResponseEntity
               .status(HttpStatus.CREATED)       // 201 Created
               .body(user)
       }

    4. 삭제 응답 (204 No Content)
       @DeleteMapping("/users/{id}")
       fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
           userService.delete(id)
           return ResponseEntity.noContent().build()  // 204 No Content
       }

    주요 HTTP 상태 코드:
    - 200 OK: 성공
    - 201 Created: 생성 성공
    - 204 No Content: 성공 (응답 본문 없음)
    - 400 Bad Request: 잘못된 요청
    - 401 Unauthorized: 인증 필요
    - 403 Forbidden: 권한 없음
    - 404 Not Found: 리소스 없음
    - 500 Internal Server Error: 서버 오류
    """.trimIndent()

    println(response)
}

fun printRealControllerExample() {
    val example = """
    실제 프로젝트 예제 (practice/book/):

    @RestController
    @RequestMapping("/practice/books")
    class BookController(
        private val bookService: BookService
    ) {
        // 전체 조회
        @GetMapping
        suspend fun getAllBooks(): List<BookDTO> {
            return bookService.getAllBooks()
        }

        // 단건 조회
        @GetMapping("/{bookId}")
        suspend fun getBook(@PathVariable bookId: Long): ResponseEntity<BookDTO> {
            val book = bookService.getBook(bookId)
            return if (book != null) {
                ResponseEntity.ok(book)
            } else {
                ResponseEntity.notFound().build()
            }
        }

        // 생성
        @PostMapping
        suspend fun createBook(@RequestBody book: BookDTO): ResponseEntity<BookDTO> {
            val created = bookService.createBook(book)
            return ResponseEntity.status(HttpStatus.CREATED).body(created)
        }

        // 수정
        @PutMapping("/{bookId}")
        suspend fun updateBook(
            @PathVariable bookId: Long,
            @RequestBody book: BookDTO
        ): ResponseEntity<BookDTO> {
            val updated = bookService.updateBook(bookId, book)
            return if (updated != null) {
                ResponseEntity.ok(updated)
            } else {
                ResponseEntity.notFound().build()
            }
        }

        // 삭제
        @DeleteMapping("/{bookId}")
        suspend fun deleteBook(@PathVariable bookId: Long): ResponseEntity<Void> {
            bookService.deleteBook(bookId)
            return ResponseEntity.noContent().build()
        }

        // 검색 (POST + RequestBody)
        @PostMapping("/search")
        suspend fun searchBooks(@RequestBody criteria: SearchCriteria): List<BookDTO> {
            return bookService.searchBooks(criteria)
        }
    }
    """.trimIndent()

    println(example)
}

fun printReactiveController() {
    val reactive = """
    Reactive Controller (WebFlux):

    Spring WebFlux에서는 suspend 함수를 사용합니다:

    @RestController
    class UserController(
        private val userService: UserService
    ) {
        // suspend 함수: Non-blocking 비동기 처리
        @GetMapping("/users/{id}")
        suspend fun getUser(@PathVariable id: Long): User? {
            return userService.findById(id)  // suspend 함수 호출
        }

        // Flow 반환: 스트리밍 응답
        @GetMapping("/users/stream")
        fun getUsersStream(): Flow<User> {
            return userService.findAllAsFlow()
        }
    }

    WebFlux vs WebMVC:
    - WebMVC: 동기, 블로킹 (스레드 풀)
    - WebFlux: 비동기, 논블로킹 (이벤트 루프)

    언제 WebFlux를 사용?
    - 많은 동시 연결 처리
    - 외부 API 호출이 많을 때
    - 실시간 스트리밍
    """.trimIndent()

    println(reactive)
}

// ========================================
// 예제용 데이터 클래스
// ========================================

data class CreateUserRequest(
    val name: String,
    val email: String
)

data class SearchCriteria(
    val keyword: String?,
    val category: String?,
    val minPrice: Int?,
    val maxPrice: Int?
)

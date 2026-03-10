# Spring Boot & WebFlux 기초 학습 가이드

Spring Boot 핵심 개념을 학습합니다.

**선행 학습**: 01_KOTLIN_BASICS.md를 먼저 완료하세요.

---

## Week 1: Spring Boot 핵심 개념

### 1. Spring Boot란?

**전통적인 Java 개발**:
- XML 설정 파일 작성
- 서버 설정
- 의존성 관리
- 수동 빈 등록

**Spring Boot**:
- 자동 설정 (Auto Configuration)
- 내장 서버 (Tomcat, Netty)
- 의존성 관리 (spring-boot-starter-*)
- 간편한 실행 (jar로 바로 실행)

---

### 2. 의존성 주입 (DI, Dependency Injection)

**나쁜 예 (강한 결합)**:
```kotlin
class UserService {
    private val userRepository = UserRepository()  // 직접 생성

    fun getUser(id: Long): User {
        return userRepository.findById(id)
    }
}
```
문제: UserRepository를 교체하려면 UserService 코드를 수정해야 함

**좋은 예 (느슨한 결합)**:
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository  // 주입받음
) {
    fun getUser(id: Long): User {
        return userRepository.findById(id)
    }
}
```
장점: Spring이 자동으로 UserRepository를 주입해줌. 테스트나 구현 변경이 쉬움

---

### 3. Spring의 3가지 핵심 어노테이션

#### @Component 계열
```kotlin
// 일반 컴포넌트
@Component
class MyComponent

// 서비스 계층 (비즈니스 로직)
@Service
class UserService

// 데이터 액세스 계층
@Repository
class UserRepository

// 웹 계층 (REST API)
@RestController
class UserController
```

**예시**:
```kotlin
// OrderService.kt
@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val notificationService: NotificationService
) : OrderService {
    // 비즈니스 로직
}

// OrderController.kt
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService  // 주입받음
) {
    // API 엔드포인트
}
```

---

### 4. Spring의 핵심 패턴: Controller → Service → Repository

```
Client Request
    ↓
Controller (웹 계층)
    ↓
Service (비즈니스 로직)
    ↓
Repository (데이터 접근)
    ↓
Database
```

**실습: 간단한 TODO 앱 만들기**

#### Step 1: Entity (데이터 모델)
```kotlin
data class Todo(
    val id: Long?,
    val title: String,
    val completed: Boolean = false
)
```

#### Step 2: Repository (데이터 접근)
```kotlin
@Repository
class TodoRepository {
    private val todos = mutableListOf<Todo>()
    private var nextId = 1L

    fun findAll(): List<Todo> = todos.toList()

    fun findById(id: Long): Todo? = todos.find { it.id == id }

    fun save(todo: Todo): Todo {
        val saved = if (todo.id == null) {
            todo.copy(id = nextId++).also { todos.add(it) }
        } else {
            todos.removeIf { it.id == todo.id }
            todo.also { todos.add(it) }
        }
        return saved
    }

    fun deleteById(id: Long): Boolean {
        return todos.removeIf { it.id == id }
    }
}
```

#### Step 3: Service (비즈니스 로직)
```kotlin
@Service
class TodoService(
    private val todoRepository: TodoRepository  // DI!
) {
    fun getAllTodos(): List<Todo> {
        return todoRepository.findAll()
    }

    fun getTodoById(id: Long): Todo {
        return todoRepository.findById(id)
            ?: throw IllegalArgumentException("Todo not found: $id")
    }

    fun createTodo(title: String): Todo {
        val todo = Todo(id = null, title = title)
        return todoRepository.save(todo)
    }

    fun completeTodo(id: Long): Todo {
        val todo = getTodoById(id)
        val updated = todo.copy(completed = true)
        return todoRepository.save(updated)
    }

    fun deleteTodo(id: Long): Boolean {
        return todoRepository.deleteById(id)
    }
}
```

#### Step 4: Controller (REST API)
```kotlin
@RestController
@RequestMapping("/api/todos")
class TodoController(
    private val todoService: TodoService  // DI!
) {

    @GetMapping
    fun getAllTodos(): List<Todo> {
        return todoService.getAllTodos()
    }

    @GetMapping("/{id}")
    fun getTodoById(@PathVariable id: Long): Todo {
        return todoService.getTodoById(id)
    }

    @PostMapping
    fun createTodo(@RequestBody request: CreateTodoRequest): Todo {
        return todoService.createTodo(request.title)
    }

    @PutMapping("/{id}/complete")
    fun completeTodo(@PathVariable id: Long): Todo {
        return todoService.completeTodo(id)
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(@PathVariable id: Long): Map<String, Boolean> {
        val deleted = todoService.deleteTodo(id)
        return mapOf("deleted" to deleted)
    }
}

data class CreateTodoRequest(val title: String)
```

#### Step 5: Application (실행)
```kotlin
@SpringBootApplication
class TodoApplication

fun main(args: Array<String>) {
    runApplication<TodoApplication>(*args)
}
```

**테스트 방법**:
```bash
# 애플리케이션 실행
./gradlew bootRun

# API 호출 (curl)
# 생성
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Kotlin 공부하기"}'

# 전체 조회
curl http://localhost:8080/api/todos

# 완료 처리
curl -X PUT http://localhost:8080/api/todos/1/complete

# 삭제
curl -X DELETE http://localhost:8080/api/todos/1
```

---

## Week 2: Reactive Programming (WebFlux)

### 5. Reactive가 왜 필요한가?

**전통적인 방식 (Blocking)**:
```kotlin
fun getUser(id: Long): User {
    val user = database.findById(id)  // 대기...
    return user
}
```
- 데이터베이스 응답을 기다리는 동안 스레드가 멈춤 (Blocking)
- 동시 요청이 많으면 스레드가 부족해짐

**Reactive 방식 (Non-blocking)**:
```kotlin
suspend fun getUser(id: Long): User {
    val user = database.findById(id)  // 다른 작업 가능
    return user
}
```
- 대기하는 동안 다른 작업 수행 가능
- 적은 스레드로 많은 요청 처리

---

### 6. Kotlin Coroutine과 suspend 함수

**suspend 함수**:
```kotlin
// 일반 함수
fun normalFunction(): String {
    return "Hello"
}

// suspend 함수 (코루틴에서만 호출 가능)
suspend fun suspendFunction(): String {
    delay(1000)  // 1초 대기 (Non-blocking)
    return "Hello"
}

// suspend 함수 호출
suspend fun example() {
    val result = suspendFunction()  // OK
    println(result)
}
```

**Service에서의 사용 예시**:
```kotlin
// OrderService.kt
interface OrderService {
    suspend fun save(order: OrderDTO): OrderDTO
    suspend fun list(request: SearchRequest): List<OrderDTO>
    suspend fun findById(id: Long): OrderDTO
}

// 구현
@Service
class OrderServiceImpl : OrderService {
    override suspend fun save(order: OrderDTO): OrderDTO {
        // DB 저장 (Non-blocking)
        return orderRepository.save(order)
    }
}
```

---

### 7. Reactive TODO 앱으로 변경하기

#### Repository (Reactive)
```kotlin
@Repository
class ReactiveTodoRepository {
    private val todos = mutableListOf<Todo>()

    suspend fun findAll(): List<Todo> = withContext(Dispatchers.IO) {
        delay(100)  // DB 조회 시뮬레이션
        todos.toList()
    }

    suspend fun findById(id: Long): Todo? = withContext(Dispatchers.IO) {
        delay(50)
        todos.find { it.id == id }
    }

    suspend fun save(todo: Todo): Todo = withContext(Dispatchers.IO) {
        delay(100)
        // 저장 로직
        todo
    }
}
```

#### Service (Reactive)
```kotlin
@Service
class ReactiveTodoService(
    private val todoRepository: ReactiveTodoRepository
) {
    suspend fun getAllTodos(): List<Todo> {
        return todoRepository.findAll()
    }

    suspend fun getTodoById(id: Long): Todo {
        return todoRepository.findById(id)
            ?: throw IllegalArgumentException("Todo not found")
    }

    suspend fun createTodo(title: String): Todo {
        val todo = Todo(id = null, title = title)
        return todoRepository.save(todo)
    }
}
```

#### Controller (Reactive)
```kotlin
@RestController
@RequestMapping("/api/reactive/todos")
class ReactiveTodoController(
    private val todoService: ReactiveTodoService
) {

    @GetMapping
    suspend fun getAllTodos(): List<Todo> {
        return todoService.getAllTodos()  // suspend!
    }

    @GetMapping("/{id}")
    suspend fun getTodoById(@PathVariable id: Long): Todo {
        return todoService.getTodoById(id)
    }

    @PostMapping
    suspend fun createTodo(@RequestBody request: CreateTodoRequest): Todo {
        return todoService.createTodo(request.title)
    }
}
```

**차이점**:
- 모든 함수에 `suspend` 키워드 추가
- Non-blocking으로 동작
- 많은 동시 요청 처리 가능

---

## Week 3: 프로젝트 패턴 이해하기

### 8. 3-Layer 데이터 모델

프로젝트는 Entity - DTO - Domain 3계층을 사용합니다.

```kotlin
// 1. Entity (데이터베이스 매핑)
@KomapperTable("tb_order")
@KomapperEntityDef(entity = OrderDTO::class)
data class OrderEntity(
    @KomapperId var id: Nothing,
    var customerName: Nothing,
    var status: Nothing
)

// 2. DTO (데이터 전송)
data class OrderDTO(
    var id: Long?,
    var customerName: String,
    var productName: String,
    var status: OrderStatus,
    var items: List<OrderItemDTO>?
)

// 3. Domain (비즈니스 로직)
data class OrderDomain(
    val order: OrderDTO,
    val items: List<OrderItemDTO>
) {
    fun calculateTotalPrice(): Double {
        return items.sumOf { it.price }
    }
}
```

**왜 3계층을 사용하나?**
- Entity: 데이터베이스 구조
- DTO: API 입출력
- Domain: 복잡한 비즈니스 로직

---

### 9. Service 인터페이스 패턴

```kotlin
// 인터페이스 정의
interface OrderService {
    suspend fun save(order: OrderDTO): OrderDTO
    suspend fun list(request: SearchRequest): List<OrderDTO>
}

// 구현체
@Service
class OrderServiceImpl(
    private val repository: OrderRepository,
    private val notificationService: NotificationService
) : OrderService {

    override suspend fun save(order: OrderDTO): OrderDTO {
        // 구현
    }
}
```

**장점**:
- 테스트할 때 Mock 객체로 교체 가능
- 여러 구현체 전환 가능
- 느슨한 결합

---

### 10. 예외 처리 패턴

```kotlin
// 커스텀 예외
class OrderException(
    val error: OrderError,
    override val message: String = error.message
) : ResponseStatusException(HttpStatus.BAD_REQUEST, message)

enum class OrderError(val code: String, val message: String) {
    NOT_FOUND("O001", "주문을 찾을 수 없습니다"),
    EXPIRED("O002", "주문이 만료되었습니다")
}

// 사용
suspend fun getOrder(id: Long): OrderDTO {
    return repository.findById(id)
        ?: throw OrderException(OrderError.NOT_FOUND)
}
```

---

---

## 심화: 트랜잭션, 설정, 예외 처리, 검증

### 11. @Transactional - 트랜잭션 처리

**트랜잭션이란?** 여러 DB 작업을 하나의 단위로 묶는 것. 중간에 오류가 나면 전부 취소(롤백)됩니다.

**은행 이체 예시:**
```kotlin
// 트랜잭션 없이
fun transfer(fromId: Long, toId: Long, amount: Int) {
    val from = repository.findById(fromId)
    repository.save(from.copy(balance = from.balance - amount))  // ← 여기서 서버 다운되면?

    val to = repository.findById(toId)
    repository.save(to.copy(balance = to.balance + amount))  // ← 이건 실행 안됨!
    // 돈은 나갔는데 안 들어온 상태로 영원히 남음!
}

// @Transactional 사용
@Transactional
fun transfer(fromId: Long, toId: Long, amount: Int) {
    val from = repository.findById(fromId)
    repository.save(from.copy(balance = from.balance - amount))  // ← 여기서 서버 다운되면?

    val to = repository.findById(toId)
    repository.save(to.copy(balance = to.balance + amount))
    // 첫 번째 저장도 자동 롤백! 데이터 안전
}
```

**기본 사용법:**
```kotlin
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository
) {
    // 주문 생성: 주문 저장 + 재고 감소를 하나의 트랜잭션으로
    @Transactional
    fun createOrder(dto: OrderDTO): OrderDTO {
        // 1. 주문 저장
        val order = orderRepository.save(dto)

        // 2. 재고 감소
        val stock = stockRepository.findById(dto.productId)
        stockRepository.save(stock.copy(quantity = stock.quantity - dto.quantity))

        // 여기서 오류 발생 시 → 주문 저장도 자동 롤백!
        return order
    }

    // 조회만 할 때는 readOnly = true (성능 최적화)
    @Transactional(readOnly = true)
    fun findById(id: Long): OrderDTO {
        return orderRepository.findById(id) ?: throw IllegalArgumentException("주문 없음")
    }
}
```

**롤백 조건:**
```kotlin
@Transactional
fun processOrder(dto: OrderDTO): OrderDTO {
    val order = orderRepository.save(dto)

    // RuntimeException 발생 시 자동 롤백
    if (dto.amount < 0) {
        throw IllegalArgumentException("금액은 0 이상이어야 합니다")  // 롤백!
    }

    // 체크드 예외(checked exception)는 기본적으로 롤백 안됨
    // 롤백하려면 rollbackFor 옵션 사용
    return order
}

// 모든 예외에서 롤백
@Transactional(rollbackFor = [Exception::class])
fun riskyOperation() { ... }
```

**Reactive(suspend) 환경에서:**
```kotlin
// Spring WebFlux + Kotlin Coroutine에서는 @Transactional 대신
// TransactionalOperator 또는 @Transactional과 coroutineScope 함께 사용

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val transactionalOperator: TransactionalOperator  // 주입
) {
    suspend fun createOrder(dto: OrderDTO): OrderDTO {
        return transactionalOperator.executeAndAwait { tx ->
            // 이 블록 내 모든 작업이 하나의 트랜잭션
            val order = orderRepository.save(dto)
            // 오류 발생 시 롤백
            order
        }!!
    }
}
```

> **기억**: 여러 DB 작업이 한 세트인 경우 `@Transactional` 필수. 조회만 할 때는 `readOnly = true`.

---

### 12. @Configuration / @Bean - Spring 설정

Spring은 객체 생성과 관리를 자동으로 해주지만, 외부 라이브러리나 복잡한 설정이 필요한 객체는 직접 등록해야 합니다.

**@Bean이란?** Spring이 관리할 객체를 직접 만들어 등록하는 것.

```kotlin
// 기본 구조
@Configuration  // "이 클래스는 설정 파일"
class AppConfig {

    @Bean  // "이 함수가 반환하는 객체를 Spring이 관리"
    fun myBean(): MyClass {
        return MyClass()
    }
}
```

**실전 예시 1: Redis 설정**
```kotlin
@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties  // application.yml 값 주입
) {
    // Redis 연결 설정
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val config = RedisStandaloneConfiguration().apply {
            hostName = redisProperties.host
            port = redisProperties.port
            password = RedisPassword.of(redisProperties.password)
        }
        return LettuceConnectionFactory(config)
    }

    // RedisTemplate 빈 등록 (String 값 저장용)
    @Bean
    fun redisTemplate(): ReactiveRedisTemplate<String, String> {
        val serializer = StringRedisSerializer()
        val context = RedisSerializationContext
            .newSerializationContext<String, String>(serializer)
            .build()
        return ReactiveRedisTemplate(redisConnectionFactory(), context)
    }
}
```

**실전 예시 2: Security 설정**
```kotlin
@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }  // CSRF 비활성화 (REST API는 불필요)
            .authorizeExchange {
                it.pathMatchers("/api/auth/**").permitAll()  // 인증 없이 접근 가능
                it.pathMatchers("/api/admin/**").hasRole("ADMIN")  // ADMIN만 접근
                it.anyExchange().authenticated()  // 나머지는 인증 필요
            }
            .build()
    }
}
```

**실전 예시 3: ObjectMapper (JSON 설정)**
```kotlin
@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            // LocalDateTime을 ISO 8601 형식으로 직렬화
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // null 필드는 응답에서 제외
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}
```

**@Configuration vs @Component:**
| 어노테이션 | 용도 |
|-----------|------|
| `@Component` | 일반 클래스를 Spring 빈으로 등록 |
| `@Configuration` | 설정 클래스 (내부에 @Bean 메서드 포함) |
| `@Bean` | @Configuration 클래스 안에서 특정 객체를 빈으로 등록 |

> **기억**: 직접 만든 클래스는 `@Component`, 외부 라이브러리 객체는 `@Configuration + @Bean`.

---

### 13. @RestControllerAdvice - 전역 예외 처리

모든 Controller에서 동일한 방식으로 예외를 처리하는 전역 핸들러입니다.

**왜 필요한가?**
```kotlin
// 나쁜 예: 각 Controller마다 try-catch 반복
@RestController
class OrderController(private val service: OrderService) {
    @GetMapping("/{id}")
    suspend fun getOrder(@PathVariable id: Long): OrderDTO {
        try {
            return service.findById(id)
        } catch (e: IllegalArgumentException) {
            // 모든 Controller에서 이걸 반복해야 함...
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        }
    }
}

// 좋은 예: 한 곳에서 모든 예외 처리
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "NOT_FOUND", message = e.message ?: "찾을 수 없습니다"))
    }
}
```

**에러 응답 형식 정의:**
```kotlin
// 에러 응답 DTO
data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

// 커스텀 예외 클래스들
class NotFoundException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
```

**전역 예외 핸들러:**
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    // 커스텀 404 예외 처리
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "NOT_FOUND", message = e.message ?: "리소스를 찾을 수 없습니다"))
    }

    // 유효성 검증 실패 처리
    @ExceptionHandler(ValidationException::class)
    fun handleValidation(e: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "INVALID_PARAMETER", message = e.message ?: "잘못된 요청입니다"))
    }

    // 인증 실패 처리
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(code = "UNAUTHORIZED", message = e.message ?: "인증이 필요합니다"))
    }

    // 예상치 못한 서버 오류 처리 (마지막 보루)
    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ErrorResponse> {
        // 실제 운영에서는 여기에 에러 알림(슬랙, 이메일 등) 로직 추가
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = "INTERNAL_ERROR", message = "서버 오류가 발생했습니다"))
    }
}
```

**Service에서 커스텀 예외 사용:**
```kotlin
@Service
class OrderServiceImpl(
    private val repository: OrderRepository
) : OrderService {

    override suspend fun findById(id: Long): OrderDTO {
        return repository.findById(id)
            ?: throw NotFoundException("주문을 찾을 수 없습니다: $id")  // 전역 핸들러가 처리
    }

    override suspend fun createOrder(dto: OrderDTO): OrderDTO {
        if (dto.amount <= 0) {
            throw ValidationException("금액은 0보다 커야 합니다")  // 전역 핸들러가 처리
        }
        return repository.save(dto)
    }
}
```

**실제 API 에러 응답 예시:**
```json
// GET /api/orders/999 → 없는 주문 조회 시
{
    "code": "NOT_FOUND",
    "message": "주문을 찾을 수 없습니다: 999",
    "timestamp": "2025-01-15T10:30:00"
}
```

---

### 14. Request Validation - 요청 데이터 검증

API로 들어오는 데이터가 올바른지 확인합니다.

**의존성 추가 (build.gradle.kts):**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-validation")
```

**DTO에 검증 어노테이션 추가:**
```kotlin
data class CreateOrderRequest(
    @field:NotBlank(message = "고객명은 필수입니다")
    val customerName: String,

    @field:NotBlank(message = "상품명은 필수입니다")
    val productName: String,

    @field:Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    val quantity: Int,

    @field:Min(value = 0, message = "금액은 0 이상이어야 합니다")
    val amount: Int,

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @field:Size(max = 500, message = "메모는 500자 이하여야 합니다")
    val memo: String? = null
)
```

**Controller에서 @Valid 사용:**
```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    suspend fun createOrder(
        @RequestBody @Valid request: CreateOrderRequest  // @Valid 추가!
    ): OrderDTO {
        return orderService.createOrder(request)
    }
}
```

**주요 검증 어노테이션:**
```kotlin
@NotNull        // null 불가
@NotBlank       // null, 빈 문자열, 공백 불가 (String에 사용)
@NotEmpty       // null, 빈 문자열 불가
@Size(min, max) // 문자열 길이, 컬렉션 크기 범위
@Min(value)     // 최솟값 (숫자)
@Max(value)     // 최댓값 (숫자)
@Email          // 이메일 형식
@Pattern        // 정규식 패턴
@Positive       // 양수 (0 제외)
@PositiveOrZero // 0 이상
```

> **Kotlin 주의사항**: Kotlin에서는 `@field:NotBlank` 처럼 `@field:` 접두사 필요.

---

## 실습 프로젝트: 주문 관리 미니 시스템

주문(Order) 기능을 단순화한 버전을 만들어봅시다.

### 요구사항
1. 주문 생성 (고객명, 금액, 상태)
2. 주문 목록 조회
3. 주문 상태 변경 (PENDING → CONFIRMED → COMPLETED)
4. 주문 취소 처리

### 코드 구조
```
src/main/kotlin/com/example/order/
├── model/
│   ├── dto/
│   │   └── OrderDTO.kt
│   └── enums/
│       └── OrderStatus.kt
├── infra/
│   ├── repository/
│   │   └── OrderRepository.kt
│   └── service/
│       └── OrderServiceImpl.kt
├── service/
│   └── OrderService.kt
└── ui/
    └── OrderController.kt
```

**practice/ 디렉토리에서 유사한 구조로 도서 관리 시스템을 구현해봅니다.**

---

## 학습 체크리스트

**Week 1: Spring Boot 기초**
- [ ] Spring Boot가 무엇인지 설명할 수 있다
- [ ] 의존성 주입(DI)을 이해하고 사용할 수 있다
- [ ] @Component, @Service, @Repository, @RestController를 구별할 수 있다
- [ ] Controller-Service-Repository 패턴을 구현할 수 있다
- [ ] TODO 앱 실습 완료

**Week 2: Reactive Programming**
- [ ] Blocking vs Non-blocking을 설명할 수 있다
- [ ] suspend 함수를 작성하고 호출할 수 있다
- [ ] Kotlin Coroutine 기본 개념 이해
- [ ] Reactive TODO 앱으로 변환 완료

**Week 3: 프로젝트 패턴**
- [ ] Entity-DTO-Domain 3계층 구조 이해
- [ ] Interface-Implementation 패턴 이해
- [ ] 예외 처리 방식 이해
- [ ] 주문 관리 미니 시스템 구현 완료

---

## 다음 단계

Spring Boot 기초를 마스터했다면:
1. **03_PROJECT_STRUCTURE.md** - 프로젝트 코드 따라가기
2. **실습 프로젝트 구현** - practice/ 디렉토리에서 직접 코딩
3. **작은 기능 수정** - 코드 기여 시작

---

## 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring WebFlux 가이드](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Kotlin Coroutines 가이드](https://kotlinlang.org/docs/coroutines-guide.html)
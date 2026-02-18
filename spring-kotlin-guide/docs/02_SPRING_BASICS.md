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
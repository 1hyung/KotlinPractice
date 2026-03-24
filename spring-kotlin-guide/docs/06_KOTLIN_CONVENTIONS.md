# Kotlin 코드 컨벤션 & 자주 만나는 에러

Spring Boot + Kotlin 프로젝트에서의 코드 작성 규칙과 흔한 실수들을 정리합니다.

---

## Part 1: Kotlin 코드 컨벤션

### 1. 네이밍 규칙

```kotlin
// 클래스 / 인터페이스 / enum / object: UpperCamelCase
class OrderServiceImpl
interface OrderService
enum class OrderStatus
object DateUtils

// 함수 / 변수 / 프로퍼티: lowerCamelCase
fun createOrder() { }
val customerName = "1hyung"
var totalAmount = 0

// 상수: UPPER_SNAKE_CASE (companion object 또는 object 안)
companion object {
    const val MAX_RETRY_COUNT = 3
    const val DEFAULT_TIMEOUT_MS = 5000L
}

// 패키지: 소문자, 점(.) 구분
package com.example.order.service

// 파일명: 클래스명과 동일 (UpperCamelCase)
// OrderServiceImpl.kt, OrderDTO.kt
```

**Spring 프로젝트 특화 네이밍**:
```kotlin
// DTO: Data Transfer Object
data class CreateOrderRequest(...)    // 요청 DTO: [동사+명사+Request]
data class OrderResponse(...)         // 응답 DTO: [명사+Response]
data class OrderDTO(...)              // 내부 전달용: [명사+DTO]

// Service
interface OrderService               // 인터페이스: [명사+Service]
class OrderServiceImpl               // 구현체: [명사+ServiceImpl]

// Repository
interface OrderRepository            // [명사+Repository]

// Controller
class OrderController                // [명사+Controller]

// Exception
class OrderNotFoundException(...)    // [명사+상황+Exception]
class InvalidOrderStatusException(...)
```

---

### 2. 클래스 구조 순서

```kotlin
@Service
class OrderServiceImpl(
    // 1. 생성자 주입 (DI)
    private val orderRepository: OrderRepository,
    private val notificationService: NotificationService
) : OrderService {

    // 2. companion object (상수, 팩토리)
    companion object {
        private const val MAX_ITEMS = 100
    }

    // 3. 프로퍼티
    private val logger = KotlinLogging.logger {}

    // 4. 초기화 블록 (필요시)
    init {
        logger.info { "OrderServiceImpl 초기화" }
    }

    // 5. 오버라이드 함수 (인터페이스 구현)
    override suspend fun createOrder(dto: OrderDTO): OrderDTO {
        // ...
    }

    override suspend fun findById(id: Long): OrderDTO {
        // ...
    }

    // 6. private 헬퍼 함수
    private fun validate(dto: OrderDTO) {
        // ...
    }
}
```

---

### 3. 함수 작성 규칙

**단순한 함수는 단일 표현식으로**:
```kotlin
// 권장
fun isValid(email: String): Boolean = email.contains("@")

fun toUpperCase(s: String) = s.uppercase()

// 비권장 (간단한 로직에 return 블록 불필요)
fun isValid(email: String): Boolean {
    return email.contains("@")
}
```

**매개변수가 많으면 named argument 사용**:
```kotlin
// 비권장 - 순서를 외워야 함
createOrder("1hyung", "laptop", 2, 1500000)

// 권장 - 의미가 명확
createOrder(
    customerName = "1hyung",
    productName = "laptop",
    quantity = 2,
    amount = 1500000
)
```

**함수는 한 가지 일만**:
```kotlin
// 비권장 - 너무 많은 일
fun createOrderAndNotifyAndLog(dto: OrderDTO): OrderDTO {
    val order = repository.save(dto)
    notificationService.send(order)
    logger.info { "주문 생성: ${order.id}" }
    return order
}

// 권장 - 역할 분리
override suspend fun createOrder(dto: OrderDTO): OrderDTO {
    val order = repository.save(dto)
    notifyCreation(order)
    return order
}

private suspend fun notifyCreation(order: OrderDTO) {
    notificationService.send(order)
    logger.info { "주문 생성: ${order.id}" }
}
```

---

### 4. Null 처리 규칙

```kotlin
// ❌ 비권장: !! 남용
val name = user!!.name!!.uppercase()

// ✅ 권장: 안전 호출 + 엘비스
val name = user?.name?.uppercase() ?: "UNKNOWN"

// ❌ 비권장: if-null 체크를 여러 번
if (user != null && user.email != null) {
    sendEmail(user.email)
}

// ✅ 권장: let으로 한 번에
user?.email?.let { email ->
    sendEmail(email)
}

// ❌ 비권장: nullable을 반환하고 호출자에게 처리 떠넘기기
fun findUser(id: Long): User? = repository.findById(id)

// ✅ 권장: Service 계층에서 명시적 예외
fun findUser(id: Long): User {
    return repository.findById(id)
        ?: throw NotFoundException("사용자 없음: $id")
}
```

---

### 5. 컬렉션 처리 규칙

```kotlin
// ❌ 비권장: 명령형 스타일
val result = mutableListOf<OrderDTO>()
for (order in orders) {
    if (order.status == OrderStatus.ACTIVE) {
        result.add(order.toResponse())
    }
}

// ✅ 권장: 함수형 스타일
val result = orders
    .filter { it.status == OrderStatus.ACTIVE }
    .map { it.toResponse() }

// 체이닝이 길어지면 변수에 담기
val activeOrders = orders.filter { it.status == OrderStatus.ACTIVE }
val responses = activeOrders.map { it.toResponse() }
val sorted = responses.sortedBy { it.createdAt }
```

---

### 6. data class 활용

```kotlin
// ✅ 불변 객체를 copy()로 업데이트
data class Order(
    val id: Long,
    val status: OrderStatus,
    val amount: Int
)

// 나쁜 방법: var + 직접 수정
// 좋은 방법: copy()로 새 객체 생성
fun confirmOrder(order: Order): Order {
    return order.copy(status = OrderStatus.CONFIRMED)
}

// 여러 필드 동시 변경
fun updateOrder(order: Order, newAmount: Int, newStatus: OrderStatus): Order {
    return order.copy(amount = newAmount, status = newStatus)
}
```

---

### 7. 주석 규칙

```kotlin
// ✅ 왜(Why)를 설명하는 주석
// 결제 API가 멱등성을 보장하지 않아 중복 호출 방지 필요
if (order.paymentRequestId != null) return order

// ❌ 무엇(What)을 설명하는 주석 (코드 자체가 설명)
// orderId를 가져온다
val orderId = order.id

// ✅ 복잡한 비즈니스 로직에는 주석 필수
// 할인 계산 순서:
// 1. 회원 등급 할인 (최대 15%)
// 2. 쿠폰 할인 (금액 차감)
// 3. 최소 결제 금액 검증 (1,000원 이상)
fun calculateFinalPrice(order: Order, coupon: Coupon?): Int {
    val memberDiscount = applyMemberDiscount(order)
    val couponDiscount = coupon?.let { applyCoupon(order, it) } ?: 0
    val finalPrice = memberDiscount - couponDiscount
    require(finalPrice >= 1000) { "최소 결제 금액은 1,000원입니다" }
    return finalPrice
}
```

---

## Part 2: 자주 만나는 에러와 해결법

### 에러 1: NullPointerException (NPE)

**증상**:
```
java.lang.NullPointerException: null cannot be cast to non-null type kotlin.String
```

**원인과 해결**:
```kotlin
// 원인: Java 라이브러리가 null을 반환하는데 Kotlin에서 non-null로 받음
val name: String = javaLibrary.getName()  // Java가 null 반환 시 NPE

// 해결: nullable 타입으로 받기
val name: String? = javaLibrary.getName()
val safeName = name ?: "기본값"

// 원인: !! 사용 중 실제로 null인 경우
val user = repository.findById(id)
val name = user!!.name  // user가 null이면 NPE

// 해결: 안전하게 처리
val name = user?.name ?: throw NotFoundException("사용자 없음")
```

---

### 에러 2: LazyInitializationException

**증상**:
```
org.hibernate.LazyInitializationException: could not initialize proxy
```

**원인과 해결**:
```kotlin
// 원인: 트랜잭션 밖에서 지연 로딩 컬렉션 접근
@Service
class OrderService(private val repository: OrderRepository) {
    fun getOrderItems(orderId: Long): List<OrderItem> {
        val order = repository.findById(orderId).orElseThrow()
        return order.items  // 트랜잭션이 끝난 후 접근 → 에러!
    }
}

// 해결 1: @Transactional 추가
@Transactional(readOnly = true)
fun getOrderItems(orderId: Long): List<OrderItem> {
    val order = repository.findById(orderId).orElseThrow()
    return order.items  // 트랜잭션 안에서 접근 → OK
}

// 해결 2: fetch join으로 처음부터 함께 로딩
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
fun findByIdWithItems(id: Long): Order?
```

---

### 에러 3: 코루틴 컨텍스트 오류

**증상**:
```
java.lang.IllegalStateException: This job has not completed yet
```
```
kotlinx.coroutines.JobCancellationException
```

**원인과 해결**:
```kotlin
// 원인: suspend 함수를 코루틴 없이 호출
fun normalFunction() {
    val result = suspendFunction()  // 컴파일 에러: suspend 함수는 코루틴에서만 호출 가능
}

// 해결: runBlocking (테스트나 최상위에서만)
fun main() = runBlocking {
    val result = suspendFunction()  // OK
}

// 해결: coroutineScope (서비스 코드에서)
suspend fun processAll(ids: List<Long>) = coroutineScope {
    ids.map { id ->
        async { processOne(id) }  // 병렬 실행
    }.awaitAll()
}

// 원인: 코루틴 안에서 블로킹 I/O
suspend fun badExample() {
    Thread.sleep(1000)  // ❌ 코루틴 스레드를 블로킹
}

// 해결: delay 사용 또는 IO 디스패처로 전환
suspend fun goodExample() {
    delay(1000)  // ✅ non-blocking 대기
}

suspend fun goodIoExample() = withContext(Dispatchers.IO) {
    blockingIoOperation()  // ✅ IO 스레드에서 실행
}
```

---

### 에러 4: @Transactional이 동작하지 않음

**증상**: 예외가 발생해도 롤백이 안 됨

**원인과 해결**:
```kotlin
// 원인 1: 같은 클래스 내부에서 호출 (프록시 우회)
@Service
class OrderService {
    @Transactional
    fun outer() {
        inner()  // ❌ 프록시를 거치지 않아 @Transactional 무시됨
    }

    @Transactional
    fun inner() { }
}

// 해결: 별도 클래스로 분리하거나 self-injection

// 원인 2: checked exception은 자동 롤백 안 됨
@Transactional
fun process() {
    throw IOException("IO 오류")  // checked exception → 롤백 안 됨!
}

// 해결: rollbackFor 지정
@Transactional(rollbackFor = [Exception::class])
fun process() {
    throw IOException("IO 오류")  // 이제 롤백됨
}

// 원인 3: private 메서드에는 @Transactional 적용 안 됨
@Transactional
private fun process() { }  // ❌ 동작 안 함

// 해결: public 또는 internal로 변경
@Transactional
fun process() { }  // ✅
```

---

### 에러 5: JSON 직렬화/역직렬화 오류

**증상**:
```
com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
No serializer found for class ...
```
```
com.fasterxml.jackson.databind.exc.MismatchedInputException
```

**원인과 해결**:
```kotlin
// 원인 1: 기본 생성자가 없는 클래스
data class OrderDTO(val id: Long, val name: String)  // Jackson이 역직렬화 불가

// 해결: kotlin-jackson 모듈 추가 (build.gradle.kts)
implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

// 설정에서 모듈 등록
@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule.Builder().build())
    }
}

// 원인 2: data class에 val 프로퍼티인데 기본값 없음
data class Request(val name: String)
// POST body에 name 없으면 → MismatchedInputException

// 해결: 기본값 추가
data class Request(val name: String = "")

// 원인 3: enum 값이 JSON과 불일치
enum class Status { ACTIVE, INACTIVE }
// JSON에 "active" (소문자) 오면 → 역직렬화 실패

// 해결: @JsonProperty 또는 대소문자 무시 설정
@Bean
fun objectMapper(): ObjectMapper = ObjectMapper().apply {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
}
```

---

### 에러 6: Bean 충돌 / 순환 의존성

**증상**:
```
No qualifying bean of type '...' available: expected single matching bean but found 2
```
```
The dependencies of some of the beans in the application context form a cycle
```

**원인과 해결**:
```kotlin
// 원인: 같은 타입의 Bean이 두 개
@Component
class KakaoPayService : PaymentService

@Component
class NaverPayService : PaymentService

// Bean이 두 개라 어떤 걸 주입해야 할지 모름
@Service
class OrderService(private val paymentService: PaymentService)  // ❌ 충돌

// 해결 1: @Primary - 기본 Bean 지정
@Primary
@Component
class KakaoPayService : PaymentService

// 해결 2: @Qualifier - 주입 시 이름 지정
@Service
class OrderService(
    @Qualifier("kakaoPayService")
    private val paymentService: PaymentService
)

// 순환 의존성
@Service
class A(private val b: B)

@Service
class B(private val a: A)  // A→B→A 순환!

// 해결: 의존성 구조 재설계 (공통 부분을 C로 분리)
@Service
class C { /* 공통 로직 */ }

@Service
class A(private val c: C)

@Service
class B(private val c: C)
```

---

### 에러 7: application.yml 설정 오류

**증상**:
```
Caused by: java.lang.IllegalStateException:
No active profile set, falling back to 1 default profile: "default"
```
```
Could not resolve placeholder '${app.secret}' in value "${app.secret}"
```

**해결**:
```yaml
# ❌ 들여쓰기 오류 (YAML은 들여쓰기가 매우 중요)
app:
secret: "my-secret"   # ← app 하위가 아님!

# ✅ 올바른 들여쓰기 (스페이스 2칸)
app:
  secret: "my-secret"

# ❌ 탭 문자 사용 (YAML은 탭 불가)
app:
	secret: "my-secret"   # 탭 사용 → 파싱 오류

# ✅ 스페이스만 사용
app:
  secret: "my-secret"
```

```kotlin
// @ConfigurationProperties 사용 시 prefix 확인
@ConfigurationProperties(prefix = "app")   // "app.secret"을 "secret"으로 접근
data class AppProperties(val secret: String)

// @Value 사용 시 $ 이스케이프
@Value("\${app.secret}")   // Kotlin에서는 \$ 필요
private lateinit var secret: String
```

---

### 에러 8: Kotlin과 Java 라이브러리 호환 문제

**증상**:
```
Type mismatch: inferred type is String! but String was expected
```

**설명**: Java 코드에서 오는 타입은 null 가능 여부가 불명확합니다 (`String!` = platform type).

**해결**:
```kotlin
// Java 메서드가 null을 반환할 수 있는지 확인 후 처리
val javaResult: String? = javaClass.possiblyNullMethod()   // nullable로 받기
val safeResult = javaResult ?: "기본값"

// @NotNull / @Nullable 어노테이션이 있다면 Kotlin이 타입을 추론
// @NotNull String methodName() → fun methodName(): String
// @Nullable String methodName() → fun methodName(): String?
```

---

## Part 3: IntelliJ IDEA 활용 팁

### 유용한 단축키

| 단축키 | 기능 |
|--------|------|
| `Cmd/Ctrl + B` | 선언/정의로 이동 |
| `Cmd/Ctrl + Alt + B` | 구현체로 이동 (인터페이스 → 구현 클래스) |
| `Cmd/Ctrl + Shift + F` | 전체 파일에서 검색 |
| `Cmd/Ctrl + Alt + L` | 코드 자동 포맷팅 |
| `Shift + Shift` | 전체 검색 (파일, 클래스, 메서드, 액션) |
| `Cmd/Ctrl + E` | 최근 파일 목록 |
| `Cmd/Ctrl + P` | 함수 매개변수 정보 보기 |
| `Alt + Enter` | 빠른 수정 제안 (Quick Fix) |
| `Cmd/Ctrl + Click` | 심볼 정의로 이동 |
| `F2` | 다음 오류로 이동 |
| `Cmd/Ctrl + D` | 현재 줄 복제 |
| `Cmd/Ctrl + /` | 줄 주석 토글 |

### 유용한 기능

```
디버깅:
- 브레이크포인트: 줄 번호 클릭
- 조건부 브레이크포인트: 브레이크포인트 우클릭 → Condition
- "Evaluate Expression": 디버그 중 코드 실행 (Alt+F8)

리팩토링:
- Shift+F6: 이름 변경 (Rename)
- Ctrl+Alt+M: 메서드 추출 (Extract Method)
- Ctrl+Alt+V: 변수 추출 (Extract Variable)

코드 생성:
- Alt+Insert: 생성자, getter/setter, 오버라이드 메서드 자동 생성
```

---

## 학습 체크리스트

### 코드 컨벤션
- [ ] 네이밍 규칙 숙지 (클래스, 함수, 변수, 상수)
- [ ] 클래스 구조 순서 이해
- [ ] Null 처리 모범 사례 적용
- [ ] 컬렉션 함수형 처리 습관화

### 에러 해결
- [ ] NullPointerException 대응법
- [ ] @Transactional 동작 원리 이해
- [ ] JSON 직렬화 설정 이해
- [ ] Bean 충돌 해결 방법

---

## 실습 코드

이 문서의 개념을 직접 실행하며 학습할 수 있습니다:

| 주제 | 실습 코드 |
|------|----------|
| 상속, 다형성 | [`src/study/05_oop/Inheritance.kt`](../../src/study/05_oop/Inheritance.kt) |
| 인터페이스, 위임 | [`src/study/05_oop/Interface.kt`](../../src/study/05_oop/Interface.kt) |
| 예외처리 패턴 | [`src/study/06_advanced/ExceptionHandling.kt`](../../src/study/06_advanced/ExceptionHandling.kt) |

---

## 참고 자료

- [Kotlin 공식 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html)
- [ktlint - Kotlin 린터](https://github.com/pinterest/ktlint)
- [detekt - Kotlin 정적 분석](https://detekt.dev/)

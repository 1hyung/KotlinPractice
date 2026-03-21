# 테스트 코드 작성 가이드

Spring Boot + Kotlin 환경에서 테스트를 작성하는 방법을 학습합니다.

**선행 학습**: 01_KOTLIN_BASICS.md, 02_SPRING_BASICS.md 완료 후 학습하세요.

---

## 테스트란 무엇인가?

**테스트 코드**: 내가 작성한 코드가 의도대로 동작하는지 자동으로 확인하는 코드

**왜 테스트를 작성해야 하나?**
- 코드 수정 후 기존 기능이 망가졌는지 자동으로 확인
- 리팩토링 시 자신감 부여
- 코드의 사용 방법을 문서화
- 버그를 빠르게 발견

**테스트의 종류:**
```
단위 테스트 (Unit Test)
  → 하나의 함수/클래스만 테스트
  → 빠르고 외부 의존성 없음
  → Service, Domain 로직 테스트에 사용

통합 테스트 (Integration Test)
  → 여러 컴포넌트가 함께 동작하는지 테스트
  → DB, 외부 API 등 포함
  → 느리지만 실제 환경과 가까움

E2E 테스트 (End-to-End)
  → 실제 사용자 시나리오 전체를 테스트
  → 가장 느리고 비용 높음
```

---

## 의존성 설정

`build.gradle.kts`:
```kotlin
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.8")              // Kotlin용 Mock 라이브러리
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")  // Kotlin 테스트 DSL (선택)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")  // suspend 테스트
}
```

---

## Week 1: JUnit 5 기초

### 1. 기본 테스트 구조

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*  // assertEquals, assertTrue 등

class CalculatorTest {

    @Test
    fun `두 숫자를 더하면 합계가 반환된다`() {
        // Given (준비)
        val a = 3
        val b = 5

        // When (실행)
        val result = a + b

        // Then (검증)
        assertEquals(8, result)
    }

    @Test
    fun `0으로 나누면 예외가 발생한다`() {
        // assertThrows: 예외 발생을 검증
        assertThrows<ArithmeticException> {
            val result = 10 / 0
        }
    }
}
```

**테스트 메서드 이름 규칙:**
```kotlin
// Kotlin에서는 백틱(`)으로 한글 메서드명 사용 가능
@Test
fun `사용자가 로그인하면 토큰이 반환된다`() { }

@Test
fun `잘못된 비밀번호로 로그인하면 UnauthorizedException이 발생한다`() { }
```

### 2. 주요 Assertion

```kotlin
@Test
fun `assertion 예시`() {
    val user = UserDTO(id = 1L, name = "1hyung", email = "1hyung@example.com")

    // 값이 같은지
    assertEquals("1hyung", user.name)

    // null 여부
    assertNotNull(user.id)
    assertNull(user.deletedAt)

    // 참/거짓
    assertTrue(user.name.isNotEmpty())
    assertFalse(user.name.isEmpty())

    // 컬렉션
    val numbers = listOf(1, 2, 3)
    assertTrue(numbers.contains(2))
    assertEquals(3, numbers.size)

    // 예외 발생
    val exception = assertThrows<IllegalArgumentException> {
        // 이 코드가 IllegalArgumentException을 던져야 함
        UserDTO(id = -1L, name = "", email = "")
    }
    assertEquals("이름은 필수입니다", exception.message)
}
```

### 3. @BeforeEach / @AfterEach

```kotlin
class OrderServiceTest {
    private lateinit var orderService: OrderService
    private val testOrders = mutableListOf<OrderDTO>()

    @BeforeEach
    fun setup() {
        // 각 테스트 전에 실행
        orderService = OrderServiceImpl(FakeOrderRepository())
        testOrders.clear()
    }

    @AfterEach
    fun cleanup() {
        // 각 테스트 후에 실행
        testOrders.clear()
    }

    @Test
    fun `주문 생성 테스트`() { }

    @Test
    fun `주문 조회 테스트`() { }
}
```

### 4. @ParameterizedTest - 여러 입력값으로 테스트

```kotlin
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.CsvSource

class ValidationTest {

    // 여러 입력값으로 같은 테스트 반복
    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    fun `빈 문자열은 유효하지 않다`(input: String) {
        assertFalse(input.isNotBlank())
    }

    @ParameterizedTest
    @CsvSource(
        "1hyung@example.com, true",
        "invalid-email, false",
        "missing-at.com, false"
    )
    fun `이메일 유효성 검사`(email: String, expected: Boolean) {
        assertEquals(expected, email.contains("@"))
    }
}
```

---

## Week 2: MockK - Kotlin용 Mock 라이브러리

### 5. Mock이란?

실제 객체 대신 **가짜 객체**를 사용해서 테스트합니다.

```kotlin
// 실제 DB 없이 OrderServiceImpl 테스트하기

@Test
fun `주문 생성 테스트 - 실제 DB 없이`() {
    // Mock 객체 생성 (가짜 Repository)
    val mockRepository = mockk<OrderRepository>()

    // 동작 정의: findByCustomerId(1L) 호출 시 빈 리스트 반환
    every { mockRepository.findByCustomerId(1L) } returns emptyList()
    // 동작 정의: save() 호출 시 id가 설정된 DTO 반환
    every { mockRepository.save(any()) } answers {
        firstArg<OrderDTO>().copy(id = 100L)
    }

    val service = OrderServiceImpl(mockRepository)
    val result = service.createOrder(OrderDTO(customerId = 1L, amount = 50000))

    assertEquals(100L, result.id)
}
```

### 6. MockK 기본 사용법

```kotlin
import io.mockk.*

class OrderServiceTest {
    // Mock 객체 생성 방법 1
    private val orderRepository = mockk<OrderRepository>()

    // Mock 객체 생성 방법 2 (어노테이션 방식)
    // @MockK private lateinit var orderRepository: OrderRepository

    private val orderService = OrderServiceImpl(orderRepository)

    @Test
    fun `주문 조회 성공`() {
        // Given: mock 동작 정의
        val expectedOrder = OrderDTO(id = 1L, customerName = "1hyung", amount = 30000)
        every { orderRepository.findById(1L) } returns expectedOrder

        // When: 실제 테스트
        val result = orderService.findById(1L)

        // Then: 결과 검증
        assertEquals(expectedOrder, result)

        // 메서드 호출 여부 검증
        verify { orderRepository.findById(1L) }
    }

    @Test
    fun `없는 주문 조회 시 예외 발생`() {
        // Given
        every { orderRepository.findById(999L) } returns null

        // When & Then
        assertThrows<NotFoundException> {
            orderService.findById(999L)
        }
    }

    @Test
    fun `주문 생성 시 Repository save 호출 확인`() {
        // Given
        val request = OrderDTO(customerName = "1hyung", amount = 30000)
        every { orderRepository.save(any()) } returns request.copy(id = 1L)

        // When
        orderService.createOrder(request)

        // Then: save가 정확히 1번 호출됐는지 검증
        verify(exactly = 1) { orderRepository.save(any()) }
    }
}
```

### 7. suspend 함수 테스트 (코루틴)

```kotlin
import kotlinx.coroutines.test.runTest

class ReactivOrderServiceTest {
    private val orderRepository = mockk<OrderRepository>()
    private val orderService = OrderServiceImpl(orderRepository)

    @Test
    fun `suspend 함수 테스트`() = runTest {
        // suspend 함수는 runTest 블록 안에서 테스트
        val expected = OrderDTO(id = 1L, customerName = "1hyung")

        // coEvery: suspend 함수용 every
        coEvery { orderRepository.findById(1L) } returns expected

        // 일반 호출처럼 테스트
        val result = orderService.findById(1L)

        assertEquals(expected, result)

        // coVerify: suspend 함수 호출 검증
        coVerify { orderRepository.findById(1L) }
    }

    @Test
    fun `suspend 함수 예외 테스트`() = runTest {
        coEvery { orderRepository.findById(any()) } throws NotFoundException("주문 없음")

        assertThrows<NotFoundException> {
            orderService.findById(999L)
        }
    }
}
```

### 8. MockK 주요 함수 정리

```kotlin
// 동작 정의
every { mock.method() } returns value           // 값 반환
every { mock.method() } throws Exception()      // 예외 발생
every { mock.method() } answers { /* 복잡한 로직 */ }

// suspend 함수용
coEvery { mock.suspendMethod() } returns value
coEvery { mock.suspendMethod() } throws Exception()

// 인자 매처 (any, specific value, etc.)
every { mock.find(any()) } returns null          // any: 아무 값이나
every { mock.find(1L) } returns someValue        // 정확한 값만 매칭
every { mock.find(more(0)) } returns someValue   // 조건 매처

// 호출 검증
verify { mock.method() }                         // 1번 이상 호출 확인
verify(exactly = 2) { mock.method() }            // 정확히 2번 호출 확인
verify(exactly = 0) { mock.method() }            // 호출 안됨 확인
verifyAll { mock.method1(); mock.method2() }     // 모두 호출 확인

// suspend 함수 검증
coVerify { mock.suspendMethod() }
```

---

## Week 3: Service 계층 단위 테스트

### 9. Service 테스트 패턴

```kotlin
class UserServiceTest {
    // 의존성 Mock 생성
    private val userRepository = mockk<UserRepository>()
    private val emailService = mockk<EmailService>()

    // 테스트 대상 생성 (실제 구현체, Mock 주입)
    private val userService = UserServiceImpl(userRepository, emailService)

    @Test
    fun `사용자 생성 - 성공`() = runTest {
        // Given
        val request = CreateUserRequest(name = "1hyung", email = "1hyung@example.com")
        val savedUser = UserDTO(id = 1L, name = "1hyung", email = "1hyung@example.com")

        coEvery { userRepository.findByEmail(request.email) } returns null  // 중복 없음
        coEvery { userRepository.save(any()) } returns savedUser
        coEvery { emailService.sendWelcome(any()) } just runs  // Unit 반환 함수

        // When
        val result = userService.createUser(request)

        // Then
        assertEquals(savedUser, result)
        coVerify { emailService.sendWelcome("1hyung@example.com") }  // 이메일 전송 확인
    }

    @Test
    fun `사용자 생성 - 이메일 중복 시 예외 발생`() = runTest {
        // Given
        val request = CreateUserRequest(name = "1hyung", email = "1hyung@example.com")
        val existingUser = UserDTO(id = 1L, name = "1hyung", email = "1hyung@example.com")

        coEvery { userRepository.findByEmail(request.email) } returns existingUser  // 중복!

        // When & Then
        val exception = assertThrows<ValidationException> {
            userService.createUser(request)
        }

        assertTrue(exception.message!!.contains("이미 사용 중인 이메일"))

        // save가 호출되지 않았는지 확인
        coVerify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `사용자 조회 - 존재하지 않는 ID`() = runTest {
        // Given
        coEvery { userRepository.findById(999L) } returns null

        // When & Then
        assertThrows<NotFoundException> {
            userService.findById(999L)
        }
    }
}
```

---

## Week 4: Spring 통합 테스트

### 10. @SpringBootTest - 전체 Spring 컨텍스트 로드

```kotlin
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")  // application-test.yml 설정 사용
class OrderIntegrationTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Test
    fun `주문 생성 통합 테스트`() = runTest {
        // Given
        val request = OrderDTO(customerName = "1hyung", amount = 50000)

        // When (실제 DB 사용)
        val result = orderService.createOrder(request)

        // Then
        assertNotNull(result.id)
        assertEquals("1hyung", result.customerName)

        // DB에 실제로 저장됐는지 확인
        val found = orderRepository.findById(result.id!!)
        assertNotNull(found)
    }
}
```

### 11. @WebFluxTest - Controller 계층 테스트

```kotlin
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.boot.test.mock.mockito.MockBean

@WebFluxTest(OrderController::class)
class OrderControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var orderService: OrderService

    @Test
    fun `GET /api/orders/{id} - 성공`() {
        // Given
        val order = OrderDTO(id = 1L, customerName = "1hyung", amount = 50000)
        given(orderService.findById(1L)).willReturn(order)

        // When & Then (HTTP 요청 시뮬레이션)
        webTestClient.get()
            .uri("/api/orders/1")
            .exchange()
            .expectStatus().isOk
            .expectBody(OrderDTO::class.java)
            .isEqualTo(order)
    }

    @Test
    fun `GET /api/orders/{id} - 없는 주문 404 반환`() {
        // Given
        given(orderService.findById(999L)).willThrow(NotFoundException("주문 없음"))

        // When & Then
        webTestClient.get()
            .uri("/api/orders/999")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `POST /api/orders - 주문 생성 성공`() {
        // Given
        val request = CreateOrderRequest(customerName = "1hyung", amount = 50000)
        val created = OrderDTO(id = 1L, customerName = "1hyung", amount = 50000)
        given(orderService.createOrder(any())).willReturn(created)

        // When & Then
        webTestClient.post()
            .uri("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1L)
            .jsonPath("$.customerName").isEqualTo("1hyung")
    }
}
```

### 12. test 환경 설정 (application-test.yml)

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
  r2dbc:
    url: r2dbc:h2:mem:///testdb;MODE=PostgreSQL

  # 테스트용 Redis (내장 Redis 사용)
  data:
    redis:
      host: localhost
      port: 6370

logging:
  level:
    root: WARN
    com.example: DEBUG
```

---

## Week 5: 테스트 작성 실전 팁

### 13. 좋은 테스트 작성 원칙

**FIRST 원칙:**
- **F**ast (빠름): 테스트는 빠르게 실행되어야 함
- **I**ndependent (독립적): 테스트끼리 의존하지 않아야 함
- **R**epeatable (반복 가능): 언제 실행해도 같은 결과
- **S**elf-validating (자기 검증): 성공/실패가 명확해야 함
- **T**imely (적시에): 코드 작성과 함께 테스트도 작성

**좋은 테스트 vs 나쁜 테스트:**
```kotlin
// 나쁜 테스트 - 테스트 이름이 모호함
@Test
fun test1() {
    val service = OrderService()
    val result = service.process(null)
    assertNull(result)
}

// 좋은 테스트 - 명확한 이름, Given/When/Then 구조
@Test
fun `주문 요청이 null이면 null을 반환한다`() {
    // Given
    val service = OrderServiceImpl(mockk())

    // When
    val result = service.process(null)

    // Then
    assertNull(result)
}
```

### 14. 테스트 커버리지 체크리스트

각 Service 메서드마다 최소 다음 케이스를 테스트하세요:

```
정상 케이스 (Happy Path)
   - 올바른 입력 → 올바른 출력

경계 케이스 (Edge Case)
   - 빈 리스트, null, 0, 최대값 등

오류 케이스 (Error Case)
   - 없는 데이터 조회
   - 잘못된 입력값
   - 권한 없는 접근
```

**예시:**
```kotlin
class BookServiceTest {
    // 정상 케이스
    @Test
    fun `도서 생성 - 정상 입력`() = runTest { }

    // 경계 케이스
    @Test
    fun `도서 목록 조회 - 빈 목록`() = runTest { }

    @Test
    fun `도서 생성 - 제목 최대 길이(255자)`() = runTest { }

    // 오류 케이스
    @Test
    fun `도서 조회 - 없는 ID`() = runTest { }

    @Test
    fun `도서 생성 - 제목 빈 문자열`() = runTest { }

    @Test
    fun `도서 대출 - 이미 대출 중인 도서`() = runTest { }
}
```

### 15. practice/book 테스트 예시

`practice/book` 실습 프로젝트에서 테스트 작성하는 방법입니다.

```kotlin
// BookServiceTest.kt
class BookServiceTest {
    private val bookRepository = mockk<BookRepository>()
    private val bookService = BookServiceImpl(bookRepository)

    // ===== 도서 조회 =====

    @Test
    fun `도서 조회 - ID로 찾기 성공`() = runTest {
        // Given
        val expected = BookDTO(id = 1L, title = "Kotlin in Action", author = "Dmitry")
        coEvery { bookRepository.findById(1L) } returns expected

        // When
        val result = bookService.findById(1L)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `도서 조회 - 없는 ID 예외 발생`() = runTest {
        // Given
        coEvery { bookRepository.findById(999L) } returns null

        // When & Then
        assertThrows<NotFoundException> {
            bookService.findById(999L)
        }
    }

    // ===== 도서 생성 =====

    @Test
    fun `도서 생성 - 성공`() = runTest {
        // Given
        val request = CreateBookRequest(title = "Clean Code", author = "1hyung", price = 28000)
        val saved = BookDTO(id = 1L, title = "Clean Code", author = "1hyung", price = 28000)
        coEvery { bookRepository.save(any()) } returns saved

        // When
        val result = bookService.createBook(request)

        // Then
        assertNotNull(result.id)
        assertEquals("Clean Code", result.title)
        coVerify(exactly = 1) { bookRepository.save(any()) }
    }

    // ===== 도서 대출 =====

    @Test
    fun `도서 대출 - 성공`() = runTest {
        // Given
        val book = BookDTO(id = 1L, title = "Kotlin", status = BookStatus.AVAILABLE)
        coEvery { bookRepository.findById(1L) } returns book
        coEvery { bookRepository.save(any()) } returns book.copy(status = BookStatus.BORROWED)

        // When
        val result = bookService.borrowBook(1L)

        // Then
        assertEquals(BookStatus.BORROWED, result.status)
    }

    @Test
    fun `도서 대출 - 이미 대출 중인 경우 예외 발생`() = runTest {
        // Given
        val book = BookDTO(id = 1L, title = "Kotlin", status = BookStatus.BORROWED)
        coEvery { bookRepository.findById(1L) } returns book

        // When & Then
        val exception = assertThrows<ValidationException> {
            bookService.borrowBook(1L)
        }
        assertTrue(exception.message!!.contains("대출 중"))

        // save가 호출되지 않아야 함
        coVerify(exactly = 0) { bookRepository.save(any()) }
    }
}
```

---

## 학습 체크리스트

**Week 1: JUnit 5 기초**
- [ ] @Test 어노테이션으로 테스트 작성 가능
- [ ] assertEquals, assertNull, assertThrows 사용 가능
- [ ] Given/When/Then 구조로 테스트 작성 가능
- [ ] @BeforeEach로 테스트 준비 코드 작성 가능

**Week 2: MockK**
- [ ] mockk()로 Mock 객체 생성 가능
- [ ] every/coEvery로 동작 정의 가능
- [ ] verify/coVerify로 호출 검증 가능
- [ ] runTest로 suspend 함수 테스트 가능

**Week 3: Service 테스트**
- [ ] Service 단위 테스트 작성 가능
- [ ] 정상/경계/오류 케이스 모두 테스트 작성
- [ ] Mock Repository를 주입하여 테스트 가능

**Week 4: 통합 테스트**
- [ ] @SpringBootTest로 통합 테스트 작성 가능
- [ ] @WebFluxTest로 Controller 테스트 작성 가능
- [ ] test 환경 설정 파일 작성 가능

---

## 참고 자료

- [JUnit 5 공식 문서](https://junit.org/junit5/docs/current/user-guide/)
- [MockK 공식 문서](https://mockk.io/)
- [Kotlin Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/testing.html)

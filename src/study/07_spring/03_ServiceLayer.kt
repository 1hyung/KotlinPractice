package study.`07_spring`

/**
 * Service 계층 개념 학습
 *
 * Service 계층은 비즈니스 로직을 담당하며,
 * Controller와 Repository 사이에서 중간 역할을 합니다.
 *
 * 관련 문서: spring-kotlin-guide/docs/02_SPRING_BASICS.md
 * 실제 예제: spring-kotlin-guide/practice/book/service/BookService.kt
 */

fun main() {
    println("=== Service 계층 학습 ===\n")

    // ========================================
    // 1. 계층 구조 이해
    // ========================================
    println("--- 1. 계층 구조 ---")
    printLayerStructure()

    // ========================================
    // 2. Interface-Implementation 패턴
    // ========================================
    println("\n--- 2. Interface-Implementation 패턴 ---")
    demonstrateInterfacePattern()

    // ========================================
    // 3. 비즈니스 로직 예제
    // ========================================
    println("\n--- 3. 비즈니스 로직 예제 ---")
    demonstrateBusinessLogic()

    // ========================================
    // 4. 트랜잭션 관리
    // ========================================
    println("\n--- 4. 트랜잭션 관리 ---")
    printTransactionExample()

    // ========================================
    // 5. 실제 Service 예제
    // ========================================
    println("\n--- 5. 실제 Service 예제 ---")
    printRealServiceExample()
}

fun printLayerStructure() {
    val structure = """
    Spring Boot 계층 구조:

    ┌─────────────────────────────────────────────────────┐
    │                    Client (브라우저/앱)              │
    └─────────────────────────────────────────────────────┘
                              ↓ HTTP 요청
    ┌─────────────────────────────────────────────────────┐
    │  Controller Layer (@RestController)                  │
    │  - HTTP 요청/응답 처리                               │
    │  - 입력 검증 (기본)                                  │
    │  - Service 호출                                      │
    └─────────────────────────────────────────────────────┘
                              ↓
    ┌─────────────────────────────────────────────────────┐
    │  Service Layer (@Service)                            │
    │  - 비즈니스 로직                                     │
    │  - 트랜잭션 관리                                     │
    │  - 여러 Repository 조합                              │
    └─────────────────────────────────────────────────────┘
                              ↓
    ┌─────────────────────────────────────────────────────┐
    │  Repository Layer (@Repository)                      │
    │  - 데이터 접근 (CRUD)                                │
    │  - 데이터베이스 쿼리                                 │
    └─────────────────────────────────────────────────────┘
                              ↓
    ┌─────────────────────────────────────────────────────┐
    │                    Database                          │
    └─────────────────────────────────────────────────────┘

    각 계층의 책임:
    - Controller: "어떤 요청을 어떻게 받을까?"
    - Service: "비즈니스 규칙은 무엇인가?"
    - Repository: "데이터를 어떻게 저장/조회할까?"
    """.trimIndent()

    println(structure)
}

// ========================================
// Interface-Implementation 패턴
// ========================================

// 인터페이스 정의
interface OrderService {
    fun createOrder(userId: Long, items: List<OrderItem>): Order
    fun getOrder(orderId: Long): Order?
    fun cancelOrder(orderId: Long): Boolean
}

// 구현체
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepositoryForOrder,
    private val inventoryService: InventoryService
) : OrderService {

    override fun createOrder(userId: Long, items: List<OrderItem>): Order {
        // 1. 사용자 존재 확인
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")

        // 2. 재고 확인
        for (item in items) {
            if (!inventoryService.checkStock(item.productId, item.quantity)) {
                throw IllegalStateException("재고 부족: ${item.productId}")
            }
        }

        // 3. 주문 생성
        val order = Order(
            id = System.currentTimeMillis(),
            userId = userId,
            items = items,
            status = OrderStatus.CREATED,
            totalAmount = items.sumOf { it.price * it.quantity }
        )

        // 4. 재고 차감
        for (item in items) {
            inventoryService.decreaseStock(item.productId, item.quantity)
        }

        // 5. 주문 저장
        return orderRepository.save(order)
    }

    override fun getOrder(orderId: Long): Order? {
        return orderRepository.findById(orderId)
    }

    override fun cancelOrder(orderId: Long): Boolean {
        val order = orderRepository.findById(orderId) ?: return false

        if (order.status != OrderStatus.CREATED) {
            throw IllegalStateException("취소할 수 없는 상태입니다: ${order.status}")
        }

        // 재고 복구
        for (item in order.items) {
            inventoryService.increaseStock(item.productId, item.quantity)
        }

        // 상태 변경
        val cancelledOrder = order.copy(status = OrderStatus.CANCELLED)
        orderRepository.save(cancelledOrder)

        return true
    }
}

fun demonstrateInterfacePattern() {
    val explanation = """
    Interface-Implementation 패턴의 장점:

    // 인터페이스 (추상화)
    interface OrderService {
        fun createOrder(...): Order
    }

    // 구현체 (실제 로직)
    @Service
    class OrderServiceImpl(
        private val orderRepository: OrderRepository
    ) : OrderService {
        override fun createOrder(...): Order { ... }
    }

    장점:
    1. 테스트 용이: Mock 구현체로 교체 가능
    2. 유연성: 다른 구현체로 쉽게 교체
    3. 관심사 분리: "무엇을 하는가"와 "어떻게 하는가" 분리

    테스트 예시:
    class OrderServiceTest {
        @MockK
        lateinit var orderRepository: OrderRepository

        @InjectMockKs
        lateinit var orderService: OrderServiceImpl

        @Test
        fun `주문 생성 테스트`() {
            // Mock 설정
            every { orderRepository.save(any()) } returns mockOrder

            // 테스트 실행
            val result = orderService.createOrder(...)

            // 검증
            verify { orderRepository.save(any()) }
        }
    }
    """.trimIndent()

    println(explanation)
}

// ========================================
// 비즈니스 로직 예제
// ========================================

fun demonstrateBusinessLogic() {
    // 의존성 생성 (실제로는 Spring이 주입)
    val orderRepository = InMemoryOrderRepository()
    val userRepository = InMemoryUserRepositoryForOrder()
    val inventoryService = SimpleInventoryService()

    val orderService = OrderServiceImpl(orderRepository, userRepository, inventoryService)

    // 1. 주문 생성
    println("1. 주문 생성")
    val items = listOf(
        OrderItem(productId = 1, quantity = 2, price = 10000),
        OrderItem(productId = 2, quantity = 1, price = 25000)
    )

    try {
        val order = orderService.createOrder(userId = 1, items = items)
        println("   주문 생성됨: ${order.id}, 총액: ${order.totalAmount}원")

        // 2. 주문 조회
        println("\n2. 주문 조회")
        val found = orderService.getOrder(order.id)
        println("   조회 결과: $found")

        // 3. 주문 취소
        println("\n3. 주문 취소")
        val cancelled = orderService.cancelOrder(order.id)
        println("   취소 결과: $cancelled")

    } catch (e: Exception) {
        println("   오류: ${e.message}")
    }
}

fun printTransactionExample() {
    val transaction = """
    @Transactional 사용:

    @Service
    class OrderServiceImpl(
        private val orderRepository: OrderRepository,
        private val inventoryService: InventoryService
    ) : OrderService {

        @Transactional  // 이 메서드는 트랜잭션 내에서 실행
        override fun createOrder(userId: Long, items: List<OrderItem>): Order {
            // 1. 주문 저장
            val order = orderRepository.save(Order(...))

            // 2. 재고 차감
            inventoryService.decreaseStock(...)

            // 3. 결제 처리
            paymentService.process(...)

            // 만약 중간에 예외 발생 → 모든 변경 롤백!
            return order
        }
    }

    @Transactional 옵션:
    - readOnly = true: 읽기 전용 (조회 최적화)
    - propagation: 트랜잭션 전파 방식
    - isolation: 격리 수준
    - rollbackFor: 롤백 조건

    예시:
    @Transactional(readOnly = true)
    fun findAll(): List<Order>

    @Transactional(rollbackFor = [Exception::class])
    fun createOrder(...): Order
    """.trimIndent()

    println(transaction)
}

fun printRealServiceExample() {
    val example = """
    실제 프로젝트 예제 (practice/book/):

    // 인터페이스
    interface BookService {
        suspend fun getAllBooks(): List<BookDTO>
        suspend fun getBook(bookId: Long): BookDTO?
        suspend fun createBook(book: BookDTO): BookDTO
        suspend fun updateBook(bookId: Long, book: BookDTO): BookDTO?
        suspend fun deleteBook(bookId: Long)
        suspend fun borrowBook(bookId: Long): BookDTO?
        suspend fun returnBook(bookId: Long): BookDTO?
    }

    // 구현체
    @Service
    class BookServiceImpl(
        private val bookRepository: BookRepository
    ) : BookService {

        override suspend fun createBook(book: BookDTO): BookDTO {
            // 비즈니스 로직
            val newBook = book.copy(
                status = BookStatus.AVAILABLE,
                createdAt = LocalDateTime.now()
            )
            return bookRepository.save(newBook)
        }

        override suspend fun borrowBook(bookId: Long): BookDTO? {
            val book = bookRepository.findById(bookId) ?: return null

            // 비즈니스 규칙: 대출 가능한 상태인지 확인
            if (!book.status.canTransitionTo(BookStatus.BORROWED)) {
                throw IllegalStateException("대출할 수 없는 상태입니다: \${book.status}")
            }

            val borrowedBook = book.copy(
                status = BookStatus.BORROWED,
                updatedAt = LocalDateTime.now()
            )
            return bookRepository.save(borrowedBook)
        }
    }

    핵심 포인트:
    - suspend 함수: 비동기/논블로킹 처리
    - 상태 전이 검증: canTransitionTo() 메서드
    - 불변 객체: copy()로 새 인스턴스 생성
    """.trimIndent()

    println(example)
}

// ========================================
// 지원 클래스들
// ========================================

data class Order(
    val id: Long,
    val userId: Long,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val totalAmount: Int
)

data class OrderItem(
    val productId: Long,
    val quantity: Int,
    val price: Int
)

enum class OrderStatus {
    CREATED, PAID, SHIPPED, DELIVERED, CANCELLED
}

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Long): Order?
}

interface UserRepositoryForOrder {
    fun findById(id: Long): User?
}

interface InventoryService {
    fun checkStock(productId: Long, quantity: Int): Boolean
    fun decreaseStock(productId: Long, quantity: Int)
    fun increaseStock(productId: Long, quantity: Int)
}

// 간단한 구현체들
class InMemoryOrderRepository : OrderRepository {
    private val orders = mutableMapOf<Long, Order>()
    override fun save(order: Order): Order {
        orders[order.id] = order
        return order
    }
    override fun findById(id: Long) = orders[id]
}

class InMemoryUserRepositoryForOrder : UserRepositoryForOrder {
    private val users = mapOf(1L to User(1L, "Alice", "alice@email.com"))
    override fun findById(id: Long) = users[id]
}

class SimpleInventoryService : InventoryService {
    private val stock = mutableMapOf(1L to 100, 2L to 50)
    override fun checkStock(productId: Long, quantity: Int) = (stock[productId] ?: 0) >= quantity
    override fun decreaseStock(productId: Long, quantity: Int) {
        stock[productId] = (stock[productId] ?: 0) - quantity
    }
    override fun increaseStock(productId: Long, quantity: Int) {
        stock[productId] = (stock[productId] ?: 0) + quantity
    }
}

package study.`06_advanced`

/**
 * Sealed Class & Sealed Interface
 *
 * 제한된 클래스 계층 구조를 정의하여 타입 안전성을 높이는 방법을 학습합니다.
 * when 표현식에서 모든 케이스를 컴파일 타임에 검증할 수 있습니다.
 */

fun main() {
    // ========================================
    // 1. Sealed Class 기초
    // ========================================

    println("--- Sealed Class 기초 ---")

    val results = listOf(
        NetworkResult.Success(User(1, "Alice")),
        NetworkResult.Error(404, "Not Found"),
        NetworkResult.Loading
    )

    for (result in results) {
        // when에서 else가 필요 없음 (모든 케이스가 명확)
        val message = when (result) {
            is NetworkResult.Success -> "성공: ${result.data}"
            is NetworkResult.Error -> "에러 ${result.code}: ${result.message}"
            is NetworkResult.Loading -> "로딩 중..."
        }
        println(message)
    }

    // ========================================
    // 2. Sealed Class의 프로퍼티와 메서드
    // ========================================

    println("\n--- 프로퍼티와 메서드 ---")

    val success = NetworkResult.Success(User(1, "Alice"))
    val error = NetworkResult.Error(500, "Server Error")

    println("Success isSuccess: ${success.isSuccess}")
    println("Error isSuccess: ${error.isSuccess}")

    success.log()
    error.log()

    // ========================================
    // 3. 중첩된 Sealed Class
    // ========================================

    println("\n--- 중첩된 Sealed Class ---")

    val paymentResults = listOf(
        PaymentResult.Success.Completed("TXN-123", 50000),
        PaymentResult.Success.Pending("TXN-456"),
        PaymentResult.Failure.InsufficientFunds(30000, 50000),
        PaymentResult.Failure.NetworkError("연결 시간 초과"),
        PaymentResult.Cancelled("사용자 취소")
    )

    for (result in paymentResults) {
        handlePaymentResult(result)
    }

    // ========================================
    // 4. Sealed Interface
    // ========================================

    println("\n--- Sealed Interface ---")

    val errors: List<AppError> = listOf(
        NetworkError.Timeout(30),
        NetworkError.NoConnection,
        DatabaseError.ConnectionFailed("DB 서버 응답 없음"),
        DatabaseError.QueryFailed("SELECT * FROM users", "Syntax error"),
        ValidationError("email", "이메일 형식이 올바르지 않습니다")
    )

    for (error in errors) {
        println(error.displayMessage())
    }

    // ========================================
    // 5. 상태 기계 (State Machine)
    // ========================================

    println("\n--- 상태 기계 ---")

    var orderState: OrderState = OrderState.Created

    // 상태 전이
    orderState = transition(orderState, OrderEvent.Pay)
    println("현재 상태: $orderState")

    orderState = transition(orderState, OrderEvent.Ship)
    println("현재 상태: $orderState")

    orderState = transition(orderState, OrderEvent.Deliver)
    println("현재 상태: $orderState")

    // 잘못된 전이 시도
    val invalidTransition = transition(orderState, OrderEvent.Pay)
    println("잘못된 전이 후: $invalidTransition")

    // ========================================
    // 6. UI 상태 관리
    // ========================================

    println("\n--- UI 상태 관리 ---")

    val screenStates = listOf(
        ScreenState.Loading,
        ScreenState.Content(listOf("Item 1", "Item 2", "Item 3")),
        ScreenState.Error("데이터를 불러올 수 없습니다"),
        ScreenState.Empty("표시할 항목이 없습니다")
    )

    for (state in screenStates) {
        renderScreen(state)
        println()
    }

    // ========================================
    // 7. 대수적 데이터 타입 (ADT)
    // ========================================

    println("\n--- 대수적 데이터 타입 ---")

    val expr1 = Expression.Add(
        Expression.Number(10),
        Expression.Multiply(
            Expression.Number(3),
            Expression.Number(4)
        )
    )  // 10 + (3 * 4) = 22

    val expr2 = Expression.Divide(
        Expression.Number(20),
        Expression.Subtract(
            Expression.Number(7),
            Expression.Number(2)
        )
    )  // 20 / (7 - 2) = 4

    println("expr1 = ${evaluate(expr1)}")
    println("expr2 = ${evaluate(expr2)}")
    println("expr1 표현: ${expr1.print()}")

    // ========================================
    // 8. JSON 파싱 결과
    // ========================================

    println("\n--- JSON 파싱 ---")

    val jsonValues = listOf(
        JsonValue.JsonString("Hello"),
        JsonValue.JsonNumber(42),
        JsonValue.JsonBoolean(true),
        JsonValue.JsonNull,
        JsonValue.JsonArray(
            listOf(
                JsonValue.JsonNumber(1),
                JsonValue.JsonNumber(2),
                JsonValue.JsonNumber(3)
            )
        ),
        JsonValue.JsonObject(
            mapOf(
                "name" to JsonValue.JsonString("Alice"),
                "age" to JsonValue.JsonNumber(25)
            )
        )
    )

    for (value in jsonValues) {
        println("${value.type}: ${value.stringify()}")
    }
}

// ========================================
// Sealed Class 정의
// ========================================

// 1. 기본 Sealed Class
sealed class NetworkResult<out T> {
    // 추상 프로퍼티
    abstract val isSuccess: Boolean

    // 공통 메서드
    fun log() {
        when (this) {
            is Success -> println("[LOG] 성공: $data")
            is Error -> println("[LOG] 에러: $code - $message")
            is Loading -> println("[LOG] 로딩 중")
        }
    }

    data class Success<T>(val data: T) : NetworkResult<T>() {
        override val isSuccess = true
    }

    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>() {
        override val isSuccess = false
    }

    data object Loading : NetworkResult<Nothing>() {
        override val isSuccess = false
    }
}

data class User(val id: Int, val name: String)

// 2. 중첩된 Sealed Class
sealed class PaymentResult {
    sealed class Success : PaymentResult() {
        data class Completed(val transactionId: String, val amount: Int) : Success()
        data class Pending(val transactionId: String) : Success()
    }

    sealed class Failure : PaymentResult() {
        data class InsufficientFunds(val available: Int, val required: Int) : Failure()
        data class NetworkError(val message: String) : Failure()
    }

    data class Cancelled(val reason: String) : PaymentResult()
}

fun handlePaymentResult(result: PaymentResult) {
    when (result) {
        is PaymentResult.Success.Completed ->
            println("결제 완료: ${result.transactionId}, ${result.amount}원")
        is PaymentResult.Success.Pending ->
            println("결제 대기: ${result.transactionId}")
        is PaymentResult.Failure.InsufficientFunds ->
            println("잔액 부족: ${result.available}원 / ${result.required}원 필요")
        is PaymentResult.Failure.NetworkError ->
            println("네트워크 오류: ${result.message}")
        is PaymentResult.Cancelled ->
            println("결제 취소: ${result.reason}")
    }
}

// 3. Sealed Interface
sealed interface AppError {
    fun displayMessage(): String
}

sealed class NetworkError : AppError {
    data class Timeout(val seconds: Int) : NetworkError() {
        override fun displayMessage() = "네트워크 타임아웃: ${seconds}초"
    }

    data object NoConnection : NetworkError() {
        override fun displayMessage() = "인터넷 연결이 없습니다"
    }
}

sealed class DatabaseError : AppError {
    data class ConnectionFailed(val reason: String) : DatabaseError() {
        override fun displayMessage() = "DB 연결 실패: $reason"
    }

    data class QueryFailed(val query: String, val error: String) : DatabaseError() {
        override fun displayMessage() = "쿼리 실패: $error"
    }
}

data class ValidationError(val field: String, val message: String) : AppError {
    override fun displayMessage() = "유효성 검사 실패 ($field): $message"
}

// 4. 상태 기계
sealed class OrderState {
    data object Created : OrderState()
    data object Paid : OrderState()
    data object Shipped : OrderState()
    data object Delivered : OrderState()
    data object Cancelled : OrderState()
}

sealed class OrderEvent {
    data object Pay : OrderEvent()
    data object Ship : OrderEvent()
    data object Deliver : OrderEvent()
    data object Cancel : OrderEvent()
}

fun transition(state: OrderState, event: OrderEvent): OrderState {
    return when (state) {
        is OrderState.Created -> when (event) {
            is OrderEvent.Pay -> OrderState.Paid
            is OrderEvent.Cancel -> OrderState.Cancelled
            else -> state  // 유효하지 않은 전이
        }
        is OrderState.Paid -> when (event) {
            is OrderEvent.Ship -> OrderState.Shipped
            is OrderEvent.Cancel -> OrderState.Cancelled
            else -> state
        }
        is OrderState.Shipped -> when (event) {
            is OrderEvent.Deliver -> OrderState.Delivered
            else -> state
        }
        is OrderState.Delivered, is OrderState.Cancelled -> state  // 최종 상태
    }
}

// 5. UI 상태 관리
sealed class ScreenState {
    data object Loading : ScreenState()
    data class Content(val items: List<String>) : ScreenState()
    data class Error(val message: String) : ScreenState()
    data class Empty(val message: String) : ScreenState()
}

fun renderScreen(state: ScreenState) {
    when (state) {
        is ScreenState.Loading -> {
            println("┌────────────────────┐")
            println("│     로딩 중...     │")
            println("└────────────────────┘")
        }
        is ScreenState.Content -> {
            println("┌────────────────────┐")
            for (item in state.items) {
                println("│ • $item")
            }
            println("└────────────────────┘")
        }
        is ScreenState.Error -> {
            println("┌────────────────────┐")
            println("│ ❌ ${state.message}")
            println("└────────────────────┘")
        }
        is ScreenState.Empty -> {
            println("┌────────────────────┐")
            println("│ 📭 ${state.message}")
            println("└────────────────────┘")
        }
    }
}

// 6. 대수적 데이터 타입 (ADT) - 수식 표현
sealed class Expression {
    data class Number(val value: Int) : Expression()
    data class Add(val left: Expression, val right: Expression) : Expression()
    data class Subtract(val left: Expression, val right: Expression) : Expression()
    data class Multiply(val left: Expression, val right: Expression) : Expression()
    data class Divide(val left: Expression, val right: Expression) : Expression()

    fun print(): String = when (this) {
        is Number -> value.toString()
        is Add -> "(${left.print()} + ${right.print()})"
        is Subtract -> "(${left.print()} - ${right.print()})"
        is Multiply -> "(${left.print()} * ${right.print()})"
        is Divide -> "(${left.print()} / ${right.print()})"
    }
}

fun evaluate(expr: Expression): Int = when (expr) {
    is Expression.Number -> expr.value
    is Expression.Add -> evaluate(expr.left) + evaluate(expr.right)
    is Expression.Subtract -> evaluate(expr.left) - evaluate(expr.right)
    is Expression.Multiply -> evaluate(expr.left) * evaluate(expr.right)
    is Expression.Divide -> evaluate(expr.left) / evaluate(expr.right)
}

// 7. JSON 값 표현
sealed class JsonValue {
    abstract val type: String
    abstract fun stringify(): String

    data class JsonString(val value: String) : JsonValue() {
        override val type = "String"
        override fun stringify() = "\"$value\""
    }

    data class JsonNumber(val value: Number) : JsonValue() {
        override val type = "Number"
        override fun stringify() = value.toString()
    }

    data class JsonBoolean(val value: Boolean) : JsonValue() {
        override val type = "Boolean"
        override fun stringify() = value.toString()
    }

    data object JsonNull : JsonValue() {
        override val type = "Null"
        override fun stringify() = "null"
    }

    data class JsonArray(val elements: List<JsonValue>) : JsonValue() {
        override val type = "Array"
        override fun stringify() = elements.joinToString(", ", "[", "]") { it.stringify() }
    }

    data class JsonObject(val properties: Map<String, JsonValue>) : JsonValue() {
        override val type = "Object"
        override fun stringify() = properties.entries.joinToString(", ", "{", "}") {
            "\"${it.key}\": ${it.value.stringify()}"
        }
    }
}

package study.`06_advanced`

/**
 * 예외 처리 (Exception Handling)
 *
 * Kotlin의 예외 처리 방법과 커스텀 예외를 학습합니다.
 * Kotlin에서는 모든 예외가 unchecked 예외입니다.
 */

fun main() {
    // ========================================
    // 1. 기본 try-catch
    // ========================================

    println("--- 기본 try-catch ---")

    try {
        val result = divide(10, 0)
        println("결과: $result")
    } catch (e: ArithmeticException) {
        println("산술 오류: ${e.message}")
    }

    // ========================================
    // 2. 여러 예외 처리
    // ========================================

    println("\n--- 여러 예외 처리 ---")

    val inputs = listOf("123", "abc", null, "456")

    for (input in inputs) {
        try {
            val number = parseNumber(input)
            println("'$input' -> $number")
        } catch (e: NumberFormatException) {
            println("'$input' -> 숫자 형식 오류: ${e.message}")
        } catch (e: NullPointerException) {
            println("'$input' -> Null 오류: ${e.message}")
        } catch (e: Exception) {
            println("'$input' -> 기타 오류: ${e.message}")
        }
    }

    // ========================================
    // 3. try-catch-finally
    // ========================================

    println("\n--- try-catch-finally ---")

    val resource = Resource("DB 연결")

    try {
        resource.open()
        resource.use()
        // 예외 발생 시뮬레이션
        // throw RuntimeException("작업 중 오류 발생!")
    } catch (e: Exception) {
        println("오류 발생: ${e.message}")
    } finally {
        // 항상 실행됨 (리소스 정리용)
        resource.close()
    }

    // ========================================
    // 4. try를 표현식으로 사용
    // ========================================

    println("\n--- try 표현식 ---")

    val number1 = try {
        "123".toInt()
    } catch (e: NumberFormatException) {
        0  // 실패 시 기본값
    }
    println("number1: $number1")

    val number2 = try {
        "abc".toInt()
    } catch (e: NumberFormatException) {
        -1  // 실패 시 기본값
    }
    println("number2: $number2")

    // ========================================
    // 5. throw 표현식
    // ========================================

    println("\n--- throw 표현식 ---")

    val name: String? = "Alice"
    val validName = name ?: throw IllegalArgumentException("이름은 필수입니다")
    println("validName: $validName")

    // Elvis 연산자와 함께 사용
    val age: Int? = null
    val validAge = try {
        age ?: throw IllegalStateException("나이가 설정되지 않았습니다")
    } catch (e: IllegalStateException) {
        0  // 기본값
    }
    println("validAge: $validAge")

    // ========================================
    // 6. 커스텀 예외
    // ========================================

    println("\n--- 커스텀 예외 ---")

    try {
        validateUser("", 25)
    } catch (e: ValidationException) {
        println("유효성 검사 실패: ${e.message} (필드: ${e.field})")
    }

    try {
        validateUser("Alice", -5)
    } catch (e: ValidationException) {
        println("유효성 검사 실패: ${e.message} (필드: ${e.field})")
    }

    // ========================================
    // 7. use 함수 (자동 리소스 관리)
    // ========================================

    println("\n--- use 함수 ---")

    // Closeable/AutoCloseable 인터페이스를 구현한 객체에 사용
    AutoCloseResource("파일").use { resource ->
        println("리소스 사용 중: ${resource.name}")
        // use 블록이 끝나면 자동으로 close() 호출
    }

    // 예외가 발생해도 close()가 호출됨
    try {
        AutoCloseResource("네트워크").use { resource ->
            println("리소스 사용 중: ${resource.name}")
            throw RuntimeException("네트워크 오류!")
        }
    } catch (e: RuntimeException) {
        println("오류 처리됨: ${e.message}")
    }

    // ========================================
    // 8. runCatching (함수형 예외 처리)
    // ========================================

    println("\n--- runCatching ---")

    // runCatching은 Result 타입을 반환
    val result1 = runCatching { divide(10, 2) }
    val result2 = runCatching { divide(10, 0) }

    println("result1 성공: ${result1.isSuccess}, 값: ${result1.getOrNull()}")
    println("result2 성공: ${result2.isSuccess}, 예외: ${result2.exceptionOrNull()?.message}")

    // getOrElse: 실패 시 기본값
    val value1 = result1.getOrElse { -1 }
    val value2 = result2.getOrElse { -1 }
    println("value1: $value1, value2: $value2")

    // getOrDefault: 더 간단한 기본값
    val value3 = result2.getOrDefault(-1)
    println("value3: $value3")

    // map: 성공 시 변환
    val doubled = result1.map { it * 2 }
    println("doubled: ${doubled.getOrNull()}")

    // recover: 실패 시 복구
    val recovered = result2.recover { 0 }
    println("recovered: ${recovered.getOrNull()}")

    // onSuccess, onFailure: 부수 효과
    result1
        .onSuccess { println("성공: $it") }
        .onFailure { println("실패: ${it.message}") }

    result2
        .onSuccess { println("성공: $it") }
        .onFailure { println("실패: ${it.message}") }

    // ========================================
    // 9. 예외 체이닝
    // ========================================

    println("\n--- 예외 체이닝 ---")

    try {
        processOrder("order-123")
    } catch (e: OrderProcessingException) {
        println("주문 처리 실패: ${e.message}")
        println("원인: ${e.cause?.message}")
    }

    // ========================================
    // 10. require, check, error 함수
    // ========================================

    println("\n--- require, check, error ---")

    // require: IllegalArgumentException (인자 검증)
    try {
        setAge(-5)
    } catch (e: IllegalArgumentException) {
        println("require 실패: ${e.message}")
    }

    // check: IllegalStateException (상태 검증)
    try {
        val user = User()
        user.performAction()  // 초기화되지 않은 상태
    } catch (e: IllegalStateException) {
        println("check 실패: ${e.message}")
    }

    // error: IllegalStateException (즉시 예외)
    try {
        val status = getStatus(-1)
        println("status: $status")
    } catch (e: IllegalStateException) {
        println("error 발생: ${e.message}")
    }

    // ========================================
    // 11. Nothing 타입
    // ========================================

    println("\n--- Nothing 타입 ---")

    // fail 함수는 Nothing을 반환 (정상 종료하지 않음)
    val value = try {
        parsePositiveNumber("-5")
    } catch (e: Exception) {
        println("오류: ${e.message}")
        null
    }
    println("value: $value")
}

// ========================================
// 함수 및 클래스 정의
// ========================================

// 기본 나누기 함수
fun divide(a: Int, b: Int): Int {
    if (b == 0) throw ArithmeticException("0으로 나눌 수 없습니다")
    return a / b
}

// 여러 예외 발생 가능한 함수
fun parseNumber(input: String?): Int {
    if (input == null) throw NullPointerException("입력값이 null입니다")
    return input.toInt()  // NumberFormatException 발생 가능
}

// 리소스 클래스 (수동 관리)
class Resource(private val name: String) {
    fun open() = println("$name 열기")
    fun use() = println("$name 사용 중")
    fun close() = println("$name 닫기")
}

// 커스텀 예외
class ValidationException(
    message: String,
    val field: String
) : Exception(message)

fun validateUser(name: String, age: Int) {
    if (name.isBlank()) {
        throw ValidationException("이름은 비어있을 수 없습니다", "name")
    }
    if (age < 0) {
        throw ValidationException("나이는 음수일 수 없습니다", "age")
    }
}

// AutoCloseable 구현
class AutoCloseResource(val name: String) : AutoCloseable {
    override fun close() {
        println("$name 리소스 자동 종료")
    }
}

// 예외 체이닝
class OrderProcessingException(message: String, cause: Throwable) : Exception(message, cause)

fun processOrder(orderId: String) {
    try {
        // 데이터베이스 조회 시뮬레이션
        throw RuntimeException("데이터베이스 연결 실패")
    } catch (e: RuntimeException) {
        // 원인 예외를 포함하여 새 예외 던지기
        throw OrderProcessingException("주문 $orderId 처리 실패", e)
    }
}

// require, check, error 예제
fun setAge(age: Int) {
    require(age >= 0) { "나이는 0 이상이어야 합니다: $age" }
    println("나이 설정: $age")
}

class User {
    var isInitialized = false

    fun initialize() {
        isInitialized = true
    }

    fun performAction() {
        check(isInitialized) { "User가 초기화되지 않았습니다" }
        println("액션 수행")
    }
}

fun getStatus(code: Int): String {
    return when (code) {
        0 -> "OK"
        1 -> "WARNING"
        2 -> "ERROR"
        else -> error("알 수 없는 상태 코드: $code")
    }
}

// Nothing 타입 (정상 종료하지 않는 함수)
fun fail(message: String): Nothing {
    throw IllegalArgumentException(message)
}

fun parsePositiveNumber(input: String): Int {
    val number = input.toIntOrNull() ?: fail("유효한 숫자가 아닙니다: $input")
    if (number <= 0) fail("양수가 아닙니다: $number")
    return number
}

package study.`02_function`

/**
 * 함수 기초 (Function Basics)
 *
 * Kotlin에서 함수를 선언하고 사용하는 다양한 방법을 학습합니다.
 */

fun main() {
    // ========================================
    // 1. 기본 함수 선언
    // ========================================

    // 함수 호출
    sayHello()
    greet("원형")

    // 반환값이 있는 함수
    val sum = add(10, 20)
    println("10 + 20 = $sum")

    // ========================================
    // 2. 단일 표현식 함수 (Single Expression Function)
    // ========================================

    // 함수 본문이 한 줄일 때 = 으로 간결하게 작성
    println("5 * 3 = ${multiply(5, 3)}")
    println("10 / 3 = ${divide(10.0, 3.0)}")

    // ========================================
    // 3. 기본값 매개변수 (Default Parameters)
    // ========================================

    // 기본값이 있으면 인자를 생략할 수 있음
    printUserInfo("원형")                    // 나이: 0, 이메일: 없음
    printUserInfo("원형", 25)                // 이메일: 없음
    printUserInfo("원형", 25, "test@email.com")

    // ========================================
    // 4. 명명된 인자 (Named Arguments)
    // ========================================

    // 인자 이름을 명시하면 순서 상관없이 전달 가능
    printUserInfo(name = "원형", email = "test@email.com", age = 25)
    printUserInfo(email = "hello@world.com", name = "홍길동")

    // 가독성 향상에 유용
    createUser(
        name = "김개발",
        age = 30,
        isAdmin = true,
        department = "개발팀"
    )

    // ========================================
    // 5. 가변인자 (Vararg)
    // ========================================

    // 여러 개의 인자를 받을 수 있음
    printNumbers(1, 2, 3, 4, 5)
    printNumbers(10, 20)

    // 배열을 가변인자로 전달할 때는 * (spread operator) 사용
    val numbers = intArrayOf(1, 2, 3)
    printNumbers(*numbers)

    // 여러 타입도 가능
    printAll("Hello", 123, true, 3.14)

    // ========================================
    // 6. Unit 반환 타입
    // ========================================

    // Unit은 Java의 void와 유사 (반환값 없음)
    // 명시하지 않아도 됨
    val result: Unit = sayHello()
    println("Unit 반환값: $result")

    // ========================================
    // 7. Nothing 반환 타입
    // ========================================

    // Nothing: 함수가 정상적으로 종료되지 않음을 나타냄
    // - 예외를 던지거나
    // - 무한 루프
    // fail("이 함수는 항상 예외를 던집니다")  // 주석 해제하면 예외 발생

    // ========================================
    // 8. 지역 함수 (Local Function)
    // ========================================

    // 함수 안에 함수를 정의할 수 있음
    fun validateInput(input: String): Boolean {
        // 지역 함수: 외부 함수의 변수에 접근 가능
        fun isNotEmpty() = input.isNotEmpty()
        fun hasValidLength() = input.length >= 3

        return isNotEmpty() && hasValidLength()
    }

    println("'ab' 유효성: ${validateInput("ab")}")      // false (길이 부족)
    println("'abc' 유효성: ${validateInput("abc")}")    // true

    // ========================================
    // 9. 중위 함수 (Infix Function)
    // ========================================

    // 중위 표기법으로 호출 가능한 함수
    val pair1 = 1 to "one"              // 표준 라이브러리의 to 함수
    val pair2 = 2.combine("two")        // 일반 호출
    val pair3 = 3 combine "three"       // 중위 호출

    println("pair1: $pair1, pair2: $pair2, pair3: $pair3")

    // ========================================
    // 10. 연산자 오버로딩 (Operator Overloading)
    // ========================================

    val point1 = Point(1, 2)
    val point2 = Point(3, 4)
    val point3 = point1 + point2        // plus 연산자 오버로딩

    println("Point: $point3")           // Point(x=4, y=6)
}

// ========================================
// 함수 정의들
// ========================================

// 반환값이 없는 함수 (Unit 생략)
fun sayHello() {
    println("Hello, Kotlin!")
}

// 매개변수가 있는 함수
fun greet(name: String) {
    println("안녕하세요, ${name}님!")
}

// 반환값이 있는 함수
fun add(a: Int, b: Int): Int {
    return a + b
}

// 단일 표현식 함수 (반환 타입 추론)
fun multiply(a: Int, b: Int) = a * b

// 단일 표현식 함수 (반환 타입 명시)
fun divide(a: Double, b: Double): Double = a / b

// 기본값 매개변수
fun printUserInfo(name: String, age: Int = 0, email: String = "없음") {
    println("이름: $name, 나이: $age, 이메일: $email")
}

// 명명된 인자 예시용 함수
fun createUser(name: String, age: Int, isAdmin: Boolean = false, department: String = "일반") {
    println("사용자 생성: $name ($age세) - $department [관리자: $isAdmin]")
}

// 가변인자 함수 (Int)
fun printNumbers(vararg numbers: Int) {
    print("숫자들: ")
    for (num in numbers) {
        print("$num ")
    }
    println()
}

// 가변인자 함수 (Any)
fun printAll(vararg items: Any) {
    println("모든 항목: ${items.joinToString(", ")}")
}

// Nothing 반환 함수 (항상 예외 발생)
fun fail(message: String): Nothing {
    throw IllegalArgumentException(message)
}

// 중위 함수 정의 (확장 함수로)
infix fun Int.combine(str: String): Pair<Int, String> = Pair(this, str)

// 연산자 오버로딩을 위한 data class
data class Point(val x: Int, val y: Int) {
    // + 연산자 오버로딩
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

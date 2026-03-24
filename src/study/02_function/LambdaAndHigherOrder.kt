package study.`02_function`

/**
 * 람다와 고차함수 (Lambda & Higher-Order Functions)
 *
 * 람다 표현식과 고차함수를 이해하면 Kotlin의 컬렉션 함수들을 더 잘 활용할 수 있습니다.
 */

fun main() {
    // ========================================
    // 1. 람다 표현식 기초
    // ========================================

    // 람다: 이름 없는 함수 (익명 함수)
    // 형태: { 매개변수 -> 본문 }

    // 가장 기본적인 람다
    val greet = { println("Hello, Lambda!") }
    greet()  // 호출

    // 매개변수가 있는 람다
    val add = { a: Int, b: Int -> a + b }
    println("3 + 5 = ${add(3, 5)}")

    // 타입을 변수에 명시하면 람다에서 생략 가능
    val multiply: (Int, Int) -> Int = { a, b -> a * b }
    println("4 * 6 = ${multiply(4, 6)}")

    // ========================================
    // 2. 람다의 it 키워드
    // ========================================

    // 매개변수가 하나일 때 it으로 참조 가능
    val double = { x: Int -> x * 2 }
    val doubleWithIt: (Int) -> Int = { it * 2 }  // it 사용

    println("double(5) = ${double(5)}")
    println("doubleWithIt(5) = ${doubleWithIt(5)}")

    // 컬렉션에서 자주 사용
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubled = numbers.map { it * 2 }
    println("doubled: $doubled")

    // ========================================
    // 3. 고차함수 (Higher-Order Function)
    // ========================================

    // 고차함수: 함수를 매개변수로 받거나 함수를 반환하는 함수

    // 함수를 매개변수로 받는 예
    val result = calculate(10, 5) { a, b -> a + b }
    println("calculate(10, 5, +): $result")

    val result2 = calculate(10, 5) { a, b -> a * b }
    println("calculate(10, 5, *): $result2")

    // ========================================
    // 4. 함수 타입 (Function Type)
    // ========================================

    // 함수 타입 선언: (매개변수타입) -> 반환타입
    val operation: (Int, Int) -> Int = { a, b -> a - b }

    // 함수 타입을 매개변수로 사용
    println("performOperation: ${performOperation(20, 7, operation)}")

    // 함수 참조 (::) 사용
    println("performOperation with sum: ${performOperation(20, 7, ::sum)}")

    // ========================================
    // 5. 함수를 반환하는 함수
    // ========================================

    val adder = createAdder(10)  // 10을 더하는 함수 반환
    println("adder(5) = ${adder(5)}")   // 15
    println("adder(20) = ${adder(20)}") // 30

    val multiplier = createMultiplier(3)  // 3을 곱하는 함수 반환
    println("multiplier(7) = ${multiplier(7)}")  // 21

    // ========================================
    // 6. 후행 람다 (Trailing Lambda)
    // ========================================

    // 마지막 매개변수가 람다일 때, 괄호 밖에 작성 가능

    // 일반적인 호출
    repeat(3, { println("Hello!") })

    // 후행 람다 (권장)
    repeat(3) {
        println("후행 람다!")
    }

    // 람다가 유일한 인자일 때 괄호 생략 가능
    val filtered = numbers.filter { it > 2 }
    println("filtered: $filtered")

    // ========================================
    // 7. 클로저 (Closure)
    // ========================================

    // 람다는 외부 변수를 캡처할 수 있음 (클로저)
    var counter = 0
    val increment = {
        counter++  // 외부 변수 캡처
        println("counter: $counter")
    }

    increment()  // 1
    increment()  // 2
    increment()  // 3

    // ========================================
    // 8. 인라인 함수 (Inline Function)
    // ========================================

    // inline 키워드: 람다 호출 오버헤드를 줄임
    // 컴파일 시 함수 본문이 호출 위치에 직접 삽입됨

    measureTime {
        // 시간 측정이 필요한 코드
        Thread.sleep(100)
        println("작업 완료")
    }

    // ========================================
    // 9. 실전 활용 예제
    // ========================================

    // 리스트 처리
    val names = listOf("Alice", "Bob", "Charlie", "David")

    // 필터링 + 변환 + 정렬
    val result3 = names
        .filter { it.length > 3 }           // 길이 3 초과
        .map { it.uppercase() }              // 대문자로 변환
        .sortedByDescending { it.length }    // 길이 내림차순 정렬

    println("처리 결과: $result3")

    // 커스텀 고차함수 활용
    val users = listOf(
        User("Alice", 25, true),
        User("Bob", 17, false),
        User("Charlie", 30, true)
    )

    // 성인 회원만 필터링
    val adults = users.filterBy { it.age >= 18 }
    println("성인: ${adults.map { it.name }}")

    // 활성 사용자만 필터링
    val activeUsers = users.filterBy { it.isActive }
    println("활성 사용자: ${activeUsers.map { it.name }}")

    // ========================================
    // 10. SAM 변환 (Single Abstract Method)
    // ========================================

    // Java 인터페이스와 호환 - 람다로 간결하게 표현
    // 예: Runnable, Comparator 등

    val runnable = Runnable { println("Runnable 실행!") }
    runnable.run()

    // Comparator 예제
    val sortedNames = names.sortedWith(Comparator { a, b ->
        a.length - b.length
    })
    println("길이순 정렬: $sortedNames")

    // 더 간결하게
    val sortedNames2 = names.sortedWith(compareBy { it.length })
    println("길이순 정렬2: $sortedNames2")
}

// ========================================
// 함수 정의들
// ========================================

// 고차함수: 함수를 매개변수로 받음
fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

// 함수 타입을 매개변수로 받는 함수
fun performOperation(a: Int, b: Int, op: (Int, Int) -> Int): Int {
    return op(a, b)
}

// 일반 함수 (함수 참조로 사용)
fun sum(a: Int, b: Int): Int = a + b

// 함수를 반환하는 함수
fun createAdder(x: Int): (Int) -> Int {
    return { y -> x + y }
}

fun createMultiplier(x: Int): (Int) -> Int = { y -> x * y }

// 인라인 함수 예제
inline fun measureTime(block: () -> Unit) {
    val start = System.currentTimeMillis()
    block()
    val end = System.currentTimeMillis()
    println("실행 시간: ${end - start}ms")
}

// 실전 예제용 data class
data class User(val name: String, val age: Int, val isActive: Boolean)

// 확장 함수 + 고차함수 조합
fun <T> List<T>.filterBy(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}

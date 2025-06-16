package basic.operator

fun main() {
    println("산술 연산자 시작")

    // 1. 산술 연산자
    // +, -, *, /, %
    var a: Int = 5
    var b: Int = 1

    println(a + b)
    println(a - b)
    println(a * b)
    println(a / b)
    println(a % b)

    println("복합 대입 연산자 시작")
    // 2. 복합 대입 연산자
    // 연산하여 왼쪽 변수에 할당
    // +=, -=, *=, /=, %=
    a += 3 // a = a + 3
    println(a) // 8
    a -= 3 // a = a - 3
    println(a) // 5
    a *= 3 // a = a * 3
    println(a) // 15
    a /= 3 // a = a / 3
    println(a) // 5
    a %= 3 // a = a % 3
    println("a=${a}") // 2

    println("증감 연산자 시작")
    // 3. 증감 연산자
    // ++ 1씩 증가
    // -- 1씩 감소
    a++
    println("a++= ${a}")
    a--
    println("a--= ${a}")

    println("비교 연산자 시작")

    // 4. 비교 연산자
    // <, <=, >, >=, ==, !=
    // a = 2, b = 1
    println(a > b) // true
    println(a >= b) // true
    println(a < b) // false
    println(a <= b) // false
    println(a == b) // false
    println(a != b) // true

    println("논리 연산자 시작")

    // 5. 논리 연산자
    // || : OR연산자 두 항 중 하나라도 true이면 true, 아니면 false
    // && : AND 연산자 두 항 모두 true이면 true, 아니면 false
    // ! : NOT 연산자 반대

    println("|| 연산자 시작")
    // || : OR
    println(true || true) // true
    println(true || false) // true
    println(false || true) // true
    println(false || false) // false

    println("&& 연산자 시작")
    // && : AND
    println(true && true) // true
    println(true && false) // false
    println(false && true) // false
    println(false && false) // false

    println("! 연산자 시작")
    // ! NOT
    println(!true) // false
    println(!false) // true

    println("활용 시작")
    // 활용
    val trueValue: Boolean = true // true
    val falseValue: Boolean = false // false
    println(trueValue)
    println(falseValue)
    println(trueValue || falseValue) // true
    println(trueValue && falseValue) // false
    println(!trueValue) // false
    println(!falseValue) // true
}

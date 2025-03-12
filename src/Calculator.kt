// Calculator 클래스 정의
class Calculator {
    // 덧셈 메소드
    fun add(a: Double, b: Double): Double {
        return a + b
    }

    // 뺄셈
    fun subtract(a: Double, b: Double): Double {
        return a - b
    }

    // 곱셈
    fun multiply(a: Double, b: Double): Double {
        return a * b
    }

    //나눗셈
    fun divide(a: Double, b: Double): Double? {
        return if (b == 0.0) {
            null
        } else {
            a / b
        }
    }

    fun calculate(a: Double, operator: String, b: Double): Double? {
        return when (operator) {
            "+" -> add(a, b)
            "-" -> subtract(a, b)
            "*" -> multiply(a, b)
            "/" -> divide(a, b)
            else -> null
        }
    }
}

fun main() {
    // Calculator 객체 생성
    val calculator = Calculator()

    // 첫 번째 숫자 입력
    print("첫 번째 숫자를 입력하세요: ")
    val num1 = readln().toDouble()

    // 연산자 입력
    print("연산자를 입력하세요 (+, -, *, /): ")
    val operator = readln()

    // 두 번째 숫자  입력
    print("두 번째 숫자를 입력하세요: ")
    val num2 = readln().toDouble()

    // 계산 수행
    val result = calculator.calculate(num1, operator, num2)
    println("결과: $num1 $operator $num2 = $result")
}
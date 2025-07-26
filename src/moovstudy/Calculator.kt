package moovstudy

import java.util.Scanner

class Calculator {
    // 덧셈
    fun add(num1: Double, num2: Double): Double = num1 + num2

    // 뻬기
    fun minus(num1: Double, num2: Double): Double = num1 - num2

    // 곱셈
    fun multiply(num1: Double, num2: Double): Double = num1 * num2

    // 나눗셈
    fun divide(num1: Double, num2: Double): Double? {
        return if (num2 == 0.0) {
            null
        } else {
            num1 / num2
        }
    }

    // 연산자
    fun calculate(num1: Double, operator: String, num2: Double) = when (operator) {
        "+" -> add(num1, num2)
        "-" -> minus(num1, num2)
        "*" -> multiply(num1, num2)
        "/" -> divide(num1, num2)
        else -> null
    }
}

fun main() {
    val sc = Scanner(System.`in`)

    // 객체 생성
    val calculator = Calculator()

    // 첫번째 숫자 입력
    print("num1: ")
    val num1 = sc.nextDouble()

    // 연산자 입력
    print("연산자를 입력하세요 (+, -, *, /):")
    val operator = readln()

    // 두번째 숫자 입력
    print("num2: ")
    val num2 = readln().toDouble()

    // 계산
    val result = calculator.calculate(num1, operator, num2)

    // 에러처리 및 결과 출력
    if (result == null) {
        if (operator == "/") {
            println("분모가 0일 수 없습니다. 계산을 종료합니다.")
        } else {
            println("잘못된 연산자입니다. 계산을 종료합니다.")
        }
    } else {
        println("결과: $num1 $operator $num2 = $result")
    }
}
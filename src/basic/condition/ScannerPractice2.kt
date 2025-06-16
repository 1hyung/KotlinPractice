package basic.condition

import java.util.Scanner

/*    금액 측정 프로그램 만들기
요구사항
금액이 10,000이 넘으면 10% 할인합니다.
최종 금액을 출력합니다
최종 금액의 변수는 finalPrice: Int로 지정해주세요*/

fun main() {
    val sc: Scanner = Scanner(System.`in`)

    println("금액을 입력해주세요: ")
    var price = sc.nextInt()
    var finalPrice: Int = 0

    finalPrice = if (price > 10_000) {
        (price * 0.9).toInt()
    } else {
        price
    }
    println(finalPrice)

    if (price > 10_000) {
        finalPrice = (price / 100) * 90
    } else {
        finalPrice = price
    }
    println(finalPrice)
}
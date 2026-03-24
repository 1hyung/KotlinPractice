package basic.condition

import java.util.Scanner

/**
사용자의 나이를 입력합니다
나이가 20살 미만이면 30% 할인을 해줍니다.
정가 금액은 20,000입니다
최종 금액의 변수는 'finalPrice:Int'로 지정
 */

fun main() {
    val sc = Scanner(System.`in`)
    var finalPrice: Int = 0
    val price = 20_000
    println("나이를 입력하세요")
    var age = sc.nextInt()

    finalPrice = if (age < 20) {
        (price * 0.7).toInt()
    } else {
        price
    }
    println("최종 금액은 ${finalPrice}")
}
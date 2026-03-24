package basic.condition

import java.util.Scanner

/**
 *자신의 고객 등급과 구매하고 싶은 물품을 입력해주세요
 * 고객 등급에 따라 물품의 할인률이 다르게 적용됩니다
 *  -VVIP: 30% 할인
 *  -VIP: 15% 할인
 *  -GOLD: 10% 할인
 *  -SILVER: 0% 할인
 *  최종 금액의 변수는 finalPice:Int로 지정해주세요
 */

fun main() {

    val sc = Scanner(System.`in`)
    var finalPrice: Int = 0

    println("1. VVIP, 2. VIP, 3. GOLD, 4. SILVER")
    print("등급을 입력하세요 : ")
    var grade = sc.nextInt()

    val salePercent = when (grade) {
        1 -> 30 // VVIP
        2 -> 15 // VIP
        3 -> 10 // GOLD
        4 -> 0 // SILVER
        else -> {
            println("없는 고객 등급입니다.")
            return
        }
    }

    println("1. 맥북, 2. LG 그램 3. 삼성")
    print("구매할 제품을 선택하세요: ")
    var product = sc.nextInt()

    product = when (product) {
        1 -> 3_000_000
        2 -> 2_500_000
        3 -> 2_300_000
        else -> {
            println("없는 제품입니다.")
            return
        }
    }

    finalPrice = (product / 100 * (100 - salePercent))
    println("최종금액: 1${finalPrice}")
}
package basic.loop

import java.util.Scanner

/**
 * 사용자가 숫자를 입력합니다.
 * 1부터 사용자가 입력한 숫자까지 모두 더한 값을 출력합니다.
 */

fun main() {
    val sc = Scanner(System.`in`)
    print("숫자를 입력해주세요: ")
    val number = sc.nextInt()
    var sum = 0

    for (i in 1..number) {
        sum += i
    }
    println(sum)

    var total = 0
    var count = 0
    while (count <= number) {
        total += count
        count++
    }
    println(total)
}
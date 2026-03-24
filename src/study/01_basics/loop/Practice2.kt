package basic.loop

/**
 * 사용자가 숫자를 입력합니다
 * 입력한 숫자의 구구단을 출력해주세요
 */

fun main() {
    print("구구단 몇 단을 할까요? ")
    val num = readln().toInt()
    for (i in 1 until 10) {
        println("${num} x ${i} = ${num * i}")
    }
}
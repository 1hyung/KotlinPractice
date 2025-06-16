package basic.loop

/**
 * 사용자가 숫자를 입력합니다
 * 1단부터 입력한 숫자의 구구단까지 모두 출력해주세요
 */
fun main() {
    print("구구단 몇 단까지 할까요? ")
    val num = readln().toInt()
    for (i in 1..num) {
        for (j in 1..9) {
            println("${i} x ${j} = ${i * j}")
        }
    }
}
package basic.loop

fun main() {
    /**    while (조건) {
    // 조건 참일 경우 코드 실행
    }
     */

    /*    var count = 0 // 증가
        while (count <= 5) { //count = 0 조건문에 계속 참이기 때문에
            println("count=$count") // 무한루프
            count++ // count 11이 되는 순간 while문 빠져 나감
        }*/

    var reversecount = 10 // 감소
    while (reversecount >= 5) {
        println("reverse=$reversecount")
        reversecount--
    }
}
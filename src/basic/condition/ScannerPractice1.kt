package basic.condition

import java.util.Scanner

/*사용자가 점수를 입력합니다
점수에 따라 학점을 출력합니다
학점
    90점 이상 -> A
    80점 이상 -> B
    70점 이상 -> C
    60점 이상 -> D
    60점 미만 -> F*/

fun main() {
    val sc: Scanner = Scanner(System.`in`)
    println("학점을 입력해 주세요: ")
    val score = sc.nextInt() // 사용자가 원하는 값을 입력할 수 있음
    //  if문 사용
    if (score >= 90) {
        println("A")
    } else if (score >= 80) {
        println("B")
    } else if (score >= 70) {
        println("C")
    } else if (score >= 60) {
        println("D")
    } else {
        println("F")
    }

    // 변수 할당
    val result = if (score >= 90) {
        "A"
    } else if (score >= 80) {
        "B"
    } else if (score >= 70) {
        "C"
    } else if (score >= 60) {
        "D"
    } else {
        "F"
    }
    println("학점은 ${result}입니다.")
}
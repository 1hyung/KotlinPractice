package basic.operator

/**
 * 1. 주어진 세 과목의 평균 출력하기
 */

val mathScore: Int = 78
val koreanScore: Int = 88
val englishScore: Int = 89

/**
 * 2. 세 과목의 점수를 제공
 * 60점 미만이면 Fail
 * 세 과목 중 하나라도 Fail이면 결과는 Fail
 * Pass인지 Fail인지 결과 출력
 */

fun main() {
    // 1. 주어진 세 과목의 평균 출력하기
    val sum = mathScore + koreanScore + englishScore
    println(sum)
    println(sum / 3).
    val average1 = sum / 3
    println(average1)
    val average2 = (mathScore + koreanScore + englishScore) / 3
    println(average2)

    //2. Pass or Fail
    val mathResult = mathScore >= 60 // true -> Pass, false -> Fail
    val koreanResult = koreanScore >= 60 // true -> Pass, false -> Fail
    val englishResult = englishScore >= 60 // true -> Pass, false -> Fail

    val result = mathResult && koreanResult && englishResult

    if (result == true) {
        println("Pass")
    } else {
        println("Fail")
    }
}
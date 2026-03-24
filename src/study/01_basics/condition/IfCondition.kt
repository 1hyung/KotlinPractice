package basic.condition

var dust: Int = 35

fun main() {

    /**    // if문의 기본 사용 방법
    if (조건식) {
    // 조건식이 참일 경우 실행되는 코드
    }*/

    /**    만약 미세먼지 농도가 35 이하면 밖에 놀러 나간다
    만약 미세먼지 농도가 35 초과면 집에서 논다*/
    if (dust <= 35) {
        println("밖에서 논다")
    }
    if (dust > 35) {
        println("집에서 논다")
    }

    /**    else
    else문은 if문과 같이 사용
    if (조건식) {
    조건식이 참일 경우 실행되는 코드
    } else {
    모든 조건식이 거짓일 경우 실행되는 코드
    }*/

    if (dust <= 35) {
        println("밖에서 논다")
    } else {
        println("집에서 논다")
    }

    /**    else if
    if문과 else문 사이에 조건과 함께 사용
    else 는 생략할 수 있음
    else if는 계속해서 사용할 수 있음    */

    /**    if (조건식1) {
    조건식1이 참일 경우 실행되는 코드
    } else if (조건식 2) {
    조건식1이 거짓이고, 조건식 2가 참일 경우 실행되는 코드
    } else if (조건식 3) {
    조건식2가 거짓이고, 조건식3이 참일 경우 실행되는 코드
    } else {
    모든 조건식 (조건식 1, 2, 3)이 거짓일 경우 실행되는 코드
    }*/

    if (dust < 35) {
        println("밖에서 논다")
    } else if (dust == 35) {
        println("고민해봄")
    } else {
        println("집에서 논다")
    }

    /**    미세먼지 농도가 0 ~ 15일 경우 좋음
    미세먼지 농도가 16 ~ 35일 경우 보통
    미세먼지 농도가 36 ~ 75일 경우 나쁨
    미세먼지 농도가 75초과일 경우 매우 나쁨*/

    dust = 77

    if (dust >= 0 && dust <= 15) {
        println("좋음")
    } else if (dust >= 16 && dust < 36) {
        println("보통")
    } else if (dust >= 36 && dust < 76) {
        println("나쁨")
    } else {
        println("매우 나쁨")
    }

    dust = 76

    if (dust <= 15) {
        println("좋음")
    } else if (dust < 36) {
        println("보통")
    } else if (dust < 76) {
        println("나쁨")
    } else {
        println("매우 나쁨")
    }
}
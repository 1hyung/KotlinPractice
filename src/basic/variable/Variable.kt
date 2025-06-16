package basic.variable

fun main() {
    // var/val 변수명: 변수타입 = 초기화값
    val name: String = "원형"
    var age: Int = 30
    val alive: Boolean = true

    println(age)
    println(name)

    age = 31
    println(age)
    /*    name = "행도" val은 컴파일 에러 발생
        println(name)*/

    // 변수는 숫자로 시작할 수 없음
    /*    var 1st: Int = 1 // 컴파일 에러*/
    var first1: Int = 1

    // 변수의 초기화 값을 넣지 않고 사용할 경우 컴파일 에러 발생
    /*    var age: Int
        println(age) // 컴파일 에러 발생*/

    var score: Int
    score = 30

    println(score) // 정상 동작
}
package basic.variable

val name: String = "원형"
var age: Int = 30
var email: String = "RYU@example.com"

fun main() {
    // 1. 자기소개 하기
    age = 31

    println("이름은 ${name}이고, 나이는 ${age}이고, 이메일은 ${email}입니다")

    val pr: String = "이름은 ${name}이고, 나이는 ${age}이고, 이메일은 ${email}입니다"
    println(pr)

    // 2. 적절한 변수 타입 넣기
    val age: Int = 10
    val pi: Double = 3.1415
    val isPossible: Boolean = true
    val constructionCosts: Long = 5_000_000_000
    val name: String = "원형"
}
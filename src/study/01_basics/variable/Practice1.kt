package basic.variable

fun main() {
    // 1. 자기소개 하기
    val name = "원형"
    var age = 30
    val email = "RYU@example.com"

    age = 31

    println("이름은 $name 이고, 나이는 $age 이고, 이메일은 $email 입니다")

    val pr = "이름은 $name 이고, 나이는 $age 이고, 이메일은 $email 입니다"
    println(pr)

    // 2. 적절한 변수 타입 넣기
    val childAge = 10
    val pi = 3.1415
    val isPossible = true
    val constructionCosts = 5_000_000_000L
    val anotherName = "행도"
}
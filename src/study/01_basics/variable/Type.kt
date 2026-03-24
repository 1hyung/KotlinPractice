package basic.variable

fun main() {
    // 정수형 (Integer types)
    val one: Int = 1
    // val threeBillion: Int = 3000000000000 // Int로 하면 2억이 넘어가는 숫자는 컴파일 에러 발생
    val threeBillion: Long = 3000000000
    val oneLong = 1L // 이 변수가 Long형이어야 할 경우가 있다.
    // 30억도 충분히 넘을 가능성이 있다면 Long형으로 선언을 해줘야 함
    val oneLong1 = 1 // L을 지우면 자연스럽게 Int가 됨
    println("${one::class.java}")
    println("${oneLong::class.java}")
    println("${oneLong1::class.java}")

    val threeBillion1: Long = 3_000_000_000 // 가독성이 안좋기 때문에 _로 똑같이 숫자로 표현 가능

    // 실수형 (Floating-point types)
    val pi = 3.14 // Double, 타입을 안적으면 소수점이면 자연스럽게 Double
    // val one: Double = 1 // Error: type mismatch
    val e = 2.7182818284 // Double
    val eFloat = 2.7182818284f // Float, actual value is 2.7182817
    // float을 사용하기 위해서는 반드시 숫자 뒤에 f를 넣어줘야함
    println("${eFloat::class.java}")

    // 논리타입
    val isAive: Boolean = true

    // 문자열(String)
    val name: String = "원형"

    // 문자열 사이에 변수를 선언
    val age: Int = 29
    val result: String = "이름은 ${name}이고 나이는 ${age}입니다."
    println(result)
    println(name)
    println("내 친구들은 ${age}살이야")
}
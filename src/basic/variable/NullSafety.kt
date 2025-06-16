package basic.variable

// var/val 변수명 : 변수타입 = 초기화값

fun main() {
    var name: String? = "원형"
    println(name)
    name = null
    println(name)
    name = "행도"
    println(name)

    var name1: String = "원형"
    var nullableName: String? = null

    // nullable한 변수를 사용할 때 변수가 null인지 아닌지 고려해서 사용해야 함
    println(name1.length) // null 타입이 아니기 때문에 값이 확실, 컴파일 에러 발생하지 않음
    /*
        nullableName.length // 컴파일 에러 발생
    */
    // ? 선언했을 때 컴파일 에러 해결 방법
    // 1. 조건문을 통해 null 확인
    if (nullableName != null) {
        println(nullableName.length)
    }

    // 2. ?. 연산자를 통해서 안전하게 호출 (Safe Call 연산자)
    // ?. nullableName이 null이면 null을 반환하고 아니면 다음을 실행하라
    println(nullableName?.length)

    // 3. ?: Elvis Operator, 엘비스 연산자
    // null이면 초기값을 설정할 수 있음
    val length: Int = nullableName?.length ?: 50
    println(length)

    // 4. !! 연산자
    /*
    // !! 사용할 때는 많은 주의가 필요하고 가능하면 사용하지 않는 것이 좋음
    // nullableName이 null이 아니라면 정상적이게 작동하지만 Exception은 미연의 방지가 중요
     println(nullableName!!.length) // NullPointException이 발생
     */
}
package kotlininaction.part1_intro.chapter01_what_is_kotlin

data class Person( // 데이터 클래스
    val name: String, // 읽기 전용 프로퍼티
    val age: Int? = null // 널이 될 수 있는 타입(Int?)과 파라미터 기본값
)

fun main () { // 최상위 함수
    val persons = listOf(
        Person("영희", age =  29), // 이름 붙은 파라미터
        Person("철수"), // 트레일링 콤마
    )

    val oldest = persons.maxBy { // 람다식
        it.age ?: 0 // 널에 작용하는 엘비스 연산자
    }
    println("가장 나이가 많은 사람: $oldest") // 문자열 템플릿
}
// 가장 나이가 많은 사람: Person(name=영희, age=29) <- toString 자동 생성
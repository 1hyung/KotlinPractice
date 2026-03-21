// ============================================
//    스코프 함수와 Sealed Class 상세 가이드
// ============================================

// 1. let - 널 체크와 결과 반환
// - 람다 결과를 반환, it으로 객체 참조
val name: String? = "Kotlin"
val length = name?.let {
    println("이름: $it")
    it.length  // 마지막 값 반환
}  // length = 6

// 2. apply - 객체 초기화와 설정
// - 객체 자체를 반환, this로 객체 참조
data class Person(var name: String = "", var age: Int = 0)
val person = Person().apply {
    name = "1hyung"  // this.name (this 생략 가능)
    age = 20
}  // person 객체 반환

// 3. also - 추가 작업 (로깅, 디버깅)
// - 객체 자체를 반환, it으로 객체 참조
val numbers = mutableListOf(1, 2, 3).also {
    println("리스트 생성: $it")
    it.add(4)
}  // numbers = [1, 2, 3, 4]

// 4. run - 객체의 메서드 실행과 결과 반환
// - 람다 결과를 반환, this로 객체 참조
val message = "Hello".run {
    println("길이: $length")
    uppercase()  // this.uppercase()
}  // message = "HELLO"

// 5. with - 객체의 여러 메서드 호출
// - 람다 결과를 반환, this로 객체 참조
val result = with(StringBuilder()) {
    append("Hello ")
    append("World")
    toString()
}  // result = "Hello World"

// ============================================
// 스코프 함수 정리: it vs this, 반환값
// let(it, 결과) / apply(this, 객체) / also(it, 객체)
// run(this, 결과) / with(this, 결과)
// ============================================
//        코틀린 필수 문법 치트시트
// ============================================

// 1. 변수 선언
val immutable = "불변 변수"        // 읽기 전용 (권장)
var mutable = "가변 변수"           // 재할당 가능
val inferred = 42                  // 타입 추론
val explicit: String = "명시적"     // 타입 명시

// 2. 널 안전성
val nonNull: String = "널 불가"
val nullable: String? = null       // 널 가능
val length = nullable?.length      // 안전 호출
val default = nullable ?: "기본값"  // 엘비스 연산자

// 3. 함수 선언
fun add(a: Int, b: Int): Int { return a + b }
fun multiply(a: Int, b: Int) = a * b  // 단일 표현식

// 4. 클래스
class Person(val name: String, var age: Int)  // 기본 생성자
data class User(val id: Int, val name: String)  // 데이터 클래스

// 5. 컬렉션
val list = listOf(1, 2, 3)         // 불변 리스트
val mutableList = mutableListOf(1, 2, 3)
val map = mapOf("key" to "value")
val set = setOf(1, 2, 3)

// 6. 제어문
if (true) "참" else "거짓"          // if는 표현식
when (x) {                         // switch 대체
    1 -> "one"
    2 -> "two"
    else -> "other"
}

// 7. 람다와 고차함수
val square = { x: Int -> x * x }
list.map { it * 2 }.filter { it > 2 }
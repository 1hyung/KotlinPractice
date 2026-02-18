// ============================================
//        코틀린 필수 문법 치트시트 v3
// ============================================

// 15. 생성자와 초기화
class User(val name: String) {
    var age: Int = 0
    init { println("초기화 블록") }
    constructor(name: String, age: Int) : this(name) {
        this.age = age
    }
}

// 16. 상속과 오버라이드
open class Animal {
    open fun sound() = "소리"
}
class Dog : Animal() {
    override fun sound() = "멍멍"
}

// 17. 인터페이스
interface Clickable {
    fun click()
    fun showOff() = println("기본 구현")  // 기본 구현 가능
}

// 18. 프로퍼티 접근자
class Rectangle(val width: Int, val height: Int) {
    val area: Int
        get() = width * height
    var isSquare: Boolean
        get() = width == height
        set(value) { /* 커스텀 setter */ }
}

// 19. 지연 초기화
lateinit var lateInit: String        // 나중에 초기화
val lazy: String by lazy { "지연" }   // 처음 접근 시 초기화

// 20. 제네릭
fun <T> singletonList(item: T): List<T> = listOf(item)
class Box<T>(val value: T)
fun <T : Comparable<T>> max(a: T, b: T) = if (a > b) a else b

// 21. 연산자 오버로딩
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}
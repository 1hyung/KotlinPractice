// ============================================
//        코틀린 필수 문법 치트시트 v2
// ============================================

// 8. 확장 함수
fun String.addExclamation() = "$this!"
"Hello".addExclamation()  // "Hello!"

// 9. 범위와 반복문
for (i in 1..10) { }              // 1부터 10까지
for (i in 1 until 10) { }         // 1부터 9까지
for (i in 10 downTo 1 step 2) { } // 10부터 1까지 2씩 감소
repeat(3) { println(it) }         // 3번 반복

// 10. 스마트 캐스트
if (obj is String) {
    println(obj.length)  // 자동 캐스팅
}

// 11. 객체와 동반 객체
object Singleton { val name = "싱글톤" }
class MyClass {
    companion object {
        fun create() = MyClass()
    }
}

// 12. let, apply, also, run, with
val result = "text".let { it.length }
val person = Person().apply { name = "1hyung" }
"log".also { println(it) }
val length = with(text) { this.length }

// 13. Sealed Class & Enum
sealed class Result {
    data class Success(val data: String): Result()
    data class Error(val message: String): Result()
}
enum class Direction { NORTH, SOUTH, EAST, WEST }

// 14. 예외 처리
try { riskyOperation() } catch (e: Exception) { handle(e) } finally { cleanup() }
val result = runCatching { riskyOp() }.getOrElse { default }
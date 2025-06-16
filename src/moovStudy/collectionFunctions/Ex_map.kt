package moovStudy.collectionFunctions

data class User(val id: Int, val name: String, val email: String)

val users = listOf(
    User(1, "류원형", "Ryu@example.com"),
    User(2, "이행도", "lee@example.com"),
    User(3, "이승원", "monkey@example.com")
)

fun main() {
    // 사용자 목록에서 이름만 추출하여 새로운 리스트를 만듬
    val userNames = users.map { user ->  // it 대신 user로 명시적 이름 사용
        user.name
    }
    println("사용자 이름 : $userNames")

    val userEmail = users.map { user ->
        user.email
    }
    println("사용자 이메일 : $userEmail")
}
package moovstudy.collectionFunctions

/**
 * map 함수란?
 * - 컬렉션의 각 요소를 변환하여 새로운 컬렉션을 생성하는 고차 함수
 * - 원본 컬렉션은 변경하지 않음 (불변성)
 * - 변환 로직을 람다 함수로 전달
 *
 * 기본 구조: collection.map { 변환 로직 }
 * 반환값: 변환된 요소들로 이루어진 새로운 List
 */

data class User(val id: Int, val name: String, val email: String)

val users = listOf(
    User(1, "류원형", "Ryu@example.com"),
    User(2, "이행도", "lee@example.com"),
    User(3, "이승원", "monkey@example.com")
)

fun main() {
    println("=== 1. 기본 사용법: 객체에서 특정 속성 추출 ===")
    // 사용자 목록에서 이름만 추출하여 새로운 리스트를 만듬
    val userNames = users.map { user ->  // it 대신 user로 명시적 이름 사용
        user.name
    }
    println("사용자 이름 : $userNames")  // [류원형, 이행도, 이승원]

    val userEmail1 = users.map { user ->
        user.email
    }
    println("사용자 이메일 1 : $userEmail1")

    // it을 사용한 간결한 표현
    val userEmail2 = users.map { it.email }
    println("사용자 이메일 2 : $userEmail2")

    println("\n=== 2. 숫자 변환 예시 ===")
    val numbers = listOf(1, 2, 3, 4, 5)

    // 각 숫자를 2배로 변환
    val doubled = numbers.map { it * 2 }
    println("원본: $numbers")           // [1, 2, 3, 4, 5]
    println("2배: $doubled")            // [2, 4, 6, 8, 10]

    // 각 숫자를 제곱으로 변환
    val squared = numbers.map { it * it }
    println("제곱: $squared")           // [1, 4, 9, 16, 25]

    println("\n=== 3. 문자열 변환 예시 ===")
    val fruits = listOf("apple", "banana", "cherry")

    // 대문자로 변환
    val uppercased = fruits.map { it.uppercase() }
    println("대문자: $uppercased")      // [APPLE, BANANA, CHERRY]

    // 문자열 길이로 변환
    val lengths = fruits.map { it.length }
    println("길이: $lengths")           // [5, 6, 6]

    println("\n=== 4. 복잡한 객체 변환 ===")
    // User를 다른 형태의 문자열로 변환
    val userSummaries = users.map { user ->
        "${user.name} (ID: ${user.id})"
    }
    println("사용자 요약: $userSummaries")
    // [류원형 (ID: 1), 이행도 (ID: 2), 이승원 (ID: 3)]

    println("\n=== 5. 중첩 map 사용 ===")
    val matrix = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9)
    )

    // 2차원 리스트의 모든 요소를 10배로 변환
    val multipliedMatrix = matrix.map { row ->
        row.map { element -> element * 10 }
    }
    println("원본 행렬: $matrix")
    println("10배 행렬: $multipliedMatrix")
    // [[10, 20, 30], [40, 50, 60], [70, 80, 90]]

    println("\n=== 6. 인덱스와 함께 사용 (mapIndexed) ===")
    val colors = listOf("Red", "Green", "Blue")
    val indexedColors = colors.mapIndexed { index, color ->
        "$index: $color"
    }
    println("인덱스 포함: $indexedColors")  // [0: Red, 1: Green, 2: Blue]

    println("\n=== 7. null 안전한 변환 (mapNotNull) ===")
    val mixedNumbers = listOf("1", "2", "abc", "3", "def")

    // 숫자로 변환 가능한 것만 변환 (불가능한 것은 제외)
    val validNumbers = mixedNumbers.mapNotNull { it.toIntOrNull() }
    println("유효한 숫자만: $validNumbers")  // [1, 2, 3]
}
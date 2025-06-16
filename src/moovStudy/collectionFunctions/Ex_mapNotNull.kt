package moovStudy.collectionFunctions

val nullableNumbers = listOf(1, 2, null, 4, 5, null)

fun main() {
    // 각 숫자를 2배로 만들고, null은 제외합니다.
    val doubledNumbers = nullableNumbers.mapNotNull { it?.times(2) } // null이 아닌 경우에만 2배
    println("2배로 만든 숫자 (null 제외): $doubledNumbers") // 출력: 2배로 만든 숫자 (null 제외): [2, 4, 8, 10]

    val notNullNumbers = nullableNumbers.mapNotNull { it }
    println("Null이 아닌 숫자 : $notNullNumbers")
}
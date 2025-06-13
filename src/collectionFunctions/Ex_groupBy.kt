package collectionFunctions

data class Friend1(val name: String, val school: String, val city: String)

val friends1 = listOf(
    Friend1("원형", "대동고", "광주"),
    Friend1("행도", "전남공고", "광주"),
    Friend1("승원", "금파공고", "광주"),
    Friend1("민성", "전남고", "헝가리")
)

val LottoNumbers = listOf(1, 4, 5, 8, 9, 10, 20)

fun main() {
    // 친구들을 도시별로 그룹화
    val friendByCity = friends1.groupBy { friend -> friend.city }
    println("$friendByCity")

    val friendBySchool = friends1.groupBy { friend -> friend.school }
    println("$friendBySchool")

    val lottoEvenOddNumbers = LottoNumbers.groupBy { number -> number % 2 == 0 }
    println("로또 번호 짝수/홀수 그룹화 : $lottoEvenOddNumbers")

}
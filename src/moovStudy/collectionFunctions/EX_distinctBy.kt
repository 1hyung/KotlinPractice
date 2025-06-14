package moovStudy.collectionFunctions

data class BestFriend(val name: String, val school: String, val city: String)

val bestfriends = listOf(
    BestFriend("원형", "대동고", "광주"),
    BestFriend("행도", "전남공고", "광주"),
    BestFriend("승원", "전남공고", "광주"),
    BestFriend("민성", "전남고", "헝가리")
)

fun main() {
    val uniqueFriendByCity = bestfriends.distinctBy { friend -> friend.city }
    println(uniqueFriendByCity)

    val (number, name) = Pair(1, "안녕")
    val (city, firends) = Pair(
        "광주", listOf(
            BestFriend("원형", "대동고", "광주"),
            BestFriend("행도", "전남공고", "광주"),
            BestFriend("승원", "금파공고", "광주"),
        )
    )

    val exfriend = bestfriends.groupBy { it.city }
        .filter { (city, friends) -> city == "광주" }
        .flatMap { (city, friends) ->
            friends.map { friend -> friend.school }
        }.distinct()


    println(exfriend)
}
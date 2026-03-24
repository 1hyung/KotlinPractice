package moovstudy.collectionFunctions

data class Friend(val name: String, val score: Int)

val friends = listOf(
    Friend("원형", 100),
    Friend("행도", 98),
    Friend("승원", 95),
    Friend("민성", 70),
)

fun main() {
    val sortedFriends = friends.sortedBy { it.score }
    println("오름차순 정렬 : $sortedFriends")
    val sortedFriendsDesc1 = friends.sortedBy { it.score }.reversed()
    println("내림차순 정렬 reversed() 사용 : $sortedFriendsDesc1")
    val sortedFriendsDesc2 = friends.sortedByDescending { friend -> friend.score }
    println("내림차순 정렬 storedByDescending 사용 : $sortedFriendsDesc2")
}
package moovStudy.collectionFunctions

data class Ord(val name: String, val type: String)

val ords = listOf(
    Ord("루피", "물딜"),
    Ord("조로", "물딜"),
    Ord("나미", "마딜")
)

fun main() {
    // 타입이 마딜인 캐릭을 찾음
    val firstMagicOrd = ords.firstOrNull { ord -> ord.type == "마딜" }
    println(firstMagicOrd?.name)

    val firstMuliOrd = ords.firstOrNull { it.type == "물딜" }
    println(firstMuliOrd?.name)

    // null이 아닐 때 출력하고싶다
    ords.firstOrNull { it.type == "듀얼" }?.also { firstDualOrd ->
        println(firstDualOrd.name)
    }
}
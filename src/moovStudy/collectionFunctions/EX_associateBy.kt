package moovStudy.collectionFunctions

data class Ord1(val name: String, val type: String)

val ords1 = listOf(
    Ord1("루피", "물딜"),
    Ord1("조로", "물딜"),
    Ord1("나미", "마딜")
)

fun main() {
    // 'type'을 키로 하여 목록을 맵으로 변경
    // {물딜=Ord1(name=조로, type=물딜), 마딜=Ord1(name=나미, type=마딜)}
    // 키가 중복되면 나중 값이 앞의 값을 덮어씀
    val ordMapByType = ords1.associateBy { type -> type.type }
    println("type을 키로 만든 $ordMapByType")

    val specificOrd = ordMapByType["마딜"]
    println("type이 마딜인 $specificOrd")
}
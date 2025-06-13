package collectionFunctions

val stops = listOf("상무역", "마륵역", "운천역")

fun main () {
    val routes = stops.zipWithNext{A, B -> A to B}
    println(routes)

    val routes1 = stops.zipWithNext{A, B -> "$A 에서 $B 까지"}
    println(routes1)
}
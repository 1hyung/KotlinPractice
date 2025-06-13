package collectionFunctions

val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

data class Item(val name: String, val price: Int, val quantity: Int)

val items = listOf(
    Item("돈까스", 10000, 2),
    Item("김치찜", 50000, 4),
    Item("냉면", 8000,3)
)

fun main () {
    // 리스트의 모든 숫자를 더함  (초기값 0)
    val sum = numbers.fold(0) { sum, number -> sum + number }
    println("총합 : $sum")

    val words = listOf("MOOV", "as", "a", "VIP")
    val sentence = words.fold("") { acc, word -> acc + word }
    println(sentence)

    val sentence1 = words.fold("") { acc, word -> "$acc $word" }.trim() // 앞 뒤 공백 제거
    println(sentence1)

    val sentence2 = words.fold("") { acc, word -> "$acc $word" }
    println(sentence2)

    val sum1 = numbers.sumOf { number -> number}
    println(sum1)

    val totalPrice = items.sumOf { food -> food.price * food.quantity}
    println(totalPrice)

    val totalPrice1 = items.fold(0) {sum, food -> sum + food.price*food.quantity}
    println(totalPrice1)}
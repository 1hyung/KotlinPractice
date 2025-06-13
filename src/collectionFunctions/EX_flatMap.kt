package collectionFunctions

data class Order(val id: Int, val productNames: List<String>)

val orders = listOf(
    Order(1, listOf("딸기", "레몬", "딸기")),
    Order(2, listOf("사과", "체리", "포도")),
    Order(3, listOf("수박", "메론", "레몬"))
)

fun main() {
    // 모든 주문의 상품 이름을 하나의 리스트로 합침
    val allProductNames = orders.flatMap { order ->
        order.productNames // 각 주문에서 상품 이름 리스트를 반환
    }
    println("모든 상품 이름 : $allProductNames") // 같은 아이디나 다른 아이디에서 같은 주문을 해도 합쳐서 출력됨
    // 출력 : [딸기, 레몬, 딸기, 사과, 체리, 포도, 수박, 메론, 레몬]

    // 상품 이름들을 각각의 문자로 분리하여 단일 문자 리스트 만들기
    val characters = orders.flatMap { order -> order.productNames }
        .flatMap { word -> word.toList() }
    println("모든 상품 이름의 문자 : $characters")
}
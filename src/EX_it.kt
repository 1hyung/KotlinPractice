data class Product(val name: String, val price: Int, val category: String)

fun main() {

    val products = listOf(
        Product("노트북", 1200000, "전자제품"),
        Product("티셔츠", 25000, "의류"),
        Product("스마트폰", 800000, "전자제품"),
        Product("청바지", 55000, "의류"),
        Product("냉장고", 1500000, "가전제품"),
    )

    // rkrurdl 100만원 이상인 제품만 필터링
    val expensiveProduct = products.filter { product -> product.price > 1000000 }
    println(expensiveProduct)

    val number = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    // 5보다 큰  숫자만 필터링해 리스트 생성
    val overFive = number.filter { it > 5 }
    println("5보다 큰 숫자 : $overFive")

    // 5보다 작은 숫자만 필터링해 리스트 생성
    val underFive = number.filter { num -> num < 5 }
    println("5보다 작은 숫자 : $underFive")

    // 문자열 리스트 길이가 3 이상인 단어만 필터링해서 리스트 생성
    val fruits = listOf("Apple", "Banana", "Pear", "Grapes", "Pineapple")

    val lengthOverThree = fruits.filter { word -> word.length > 3 }
    println("길이가 3보다 긴 과일 이름 : $lengthOverThree")

    // 홀수만 필터링해서 새로운 리스트 생성, 람다 함수의 파라미터 이름을 num으로 지정
    val oddNumbers = number.filter { num -> num % 2 == 1 }
    println("홀수 : $oddNumbers")
}
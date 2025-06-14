package moovStudy

// String 클래스에 'printLength()'라는 새로운 확장 함수를 추가합니다.
// 이 함수는 어떤 String 객체에서든 호출될 수 있습니다.
fun String.printLength() {
    // 'this'는 이 함수가 호출된 String 객체 자체를 가리킵니다.
    // 예를 들어, "Hello".printLength()를 호출했다면, 여기서 'this'는 "Hello" 문자열이 됩니다.
    println("이 문자열의 길이는 ${this.length}입니다.")
}

fun Int.isEven(): Boolean {
    return this % 2 == 0
}

fun List<String>.printAllWithPrefix(prefix: String) {
    // (1) 이 확장 함수가 호출된 List<String> 객체를 'this'로 접근합니다.
    // 즉, collectionFunctions.collectionFunctions.main 함수에서 names.printAllWithPrefix(...)를 호출했다면,
    // 여기서 'this'는 names 변수가 가리키는 listOf("Alice", "Bob", "Charlie") 리스트가 됩니다.

    // (2) 'this' (List<String> 객체)의 각 요소에 접근하기 위해 forEach 람다 함수를 사용합니다.
    // forEach는 리스트의 모든 요소를 하나씩 순회하며 람다 블록 안의 코드를 실행합니다.
    // 람다 블록에서 'it'은 현재 순회 중인 리스트의 요소를 암시적으로 가리킵니다.
    // 이 경우, 'it'은 List<String> 안의 각 String 요소 ("Alice", "Bob", "Charlie" 등)가 됩니다.
    this.forEach { it ->
        // (3) 각 요소(it) 앞에 매개변수로 받은 'prefix'를 붙여서 출력합니다.
        println("$prefix$it")
    }
}

// 메인 함수: 프로그램의 시작점입니다.
fun main() {

    println("--- 확장 함수 예제 시작 ---")

    // 1. "안녕하세요" 문자열에 확장 함수 호출
    val greeting = "안녕하세요"
    println("문자열: \"$greeting\"")
    greeting.printLength() // (1) String 객체인 'greeting'에 확장 함수 'printLength()'를 호출합니다.
    // 이 시점에서 'printLength()' 함수 내부의 'this'는 "안녕하세요"가 됩니다.
    println("--------------------")

    // 2. 다른 문자열에 확장 함수 호출
    val animal = "코틀린"
    println("문자열: \"$animal\"")
    animal.printLength() // (2) 또 다른 String 객체인 'animal'에 확장 함수 호출
    // 이 시점에서 'printLength()' 함수 내부의 'this'는 "코틀린"이 됩니다.
    println("--------------------")

    // 3. 리터럴 문자열에 직접 확장 함수 호출
    println("문자열: \"Extension Function\"")
    "Extension Function".printLength() // (3) 변수에 저장하지 않고 문자열 리터럴에 직접 확장 함수 호출
    // 이 시점에서 'printLength()' 함수 내부의 'this'는 "Extension Function"이 됩니다.
    println("--------------------")

    println("------확장 함수 isEven 예제 시작--------")
    println(4.isEven())
    println(3.isEven())
    println(1.isEven())
    println("------확장 함수 isEven 예제 종료--------")

    println("--- printAllWithPrefix 확장 함수 예제 시작 ---")

    val names1 = listOf("Alice", "Bob", "Charlie", "1hyung", "<UNK>")
    names1.printAllWithPrefix("Name: ")

    val names = listOf("Alice", "Bob", "Charlie") // String 객체들을 담은 List<String> 객체 생성

    // names라는 List<String> 객체에 printAllWithPrefix 확장 함수를 호출합니다.
    // 이때 "Name: " 문자열이 prefix 매개변수로 전달됩니다.
    names.printAllWithPrefix("Name: ")

    println("--------------------")

    val fruits = listOf("Apple", "Banana", "Cherry") // 또 다른 List<String> 객체 생성
    // fruits 객체에 확장 함수를 호출하여 "Fruit: " 접두사를 붙여 출력합니다.
    fruits.printAllWithPrefix("Fruit: ")

    println("--- printAllWithPrefix 확장 함수 예제 종료 ---")


    println("--- 확장 함수 예제 종료 ---")
}
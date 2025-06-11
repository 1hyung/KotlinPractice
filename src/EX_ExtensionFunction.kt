// String 클래스에 'printLength()'라는 새로운 확장 함수를 추가합니다.
// 이 함수는 어떤 String 객체에서든 호출될 수 있습니다.
fun String.printLength() {
    // 'this'는 이 함수가 호출된 String 객체 자체를 가리킵니다.
    // 예를 들어, "Hello".printLength()를 호출했다면, 여기서 'this'는 "Hello" 문자열이 됩니다.
    println("이 문자열의 길이는 ${this.length}입니다.")
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

    println("--- 확장 함수 예제 종료 ---")
}
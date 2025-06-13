fun main() {
    // fun은 function(함수)의 약자로, 해당 코드 블록이 함수(Function)임을 나타내는 키워드입니다.
    // main은 함수의 이름을 의미하며, 코틀린 프로그램의 시작점(entry point) 역할을 합니다.
    // 모든 코틀린 프로그램은 collectionFunctions.collectionFunctions.main() 함수부터 실행됩니다.

    // {} 중괄호는 collectionFunctions.collectionFunctions.main 함수의 시작과 끝을 나타내며, 중괄호 사이에 실행할 명령어를 작성합니다.

    println("Hello, Kotlin!")
    // println은 화면에 텍스트를 출력하는 함수입니다.
    // println 함수는 코틀린 표준 라이브러리에 내장된 함수로, 별도의 추가 설정 없이 사용 가능합니다.
    // println()과 같이 함수 이름을 적고 소괄호를 열고 닫으면, 해당 함수가 실행됩니다.

    // println 함수는 전달된 값을 출력하는 기능을 합니다.
    // 예를 들어, println("Hello, Kotlin!")을 실행하면 "Hello, Kotlin!"이 화면에 출력됩니다.

    // 문자열(String)은 큰따옴표("")로 감싸야 합니다.
    // 예를 들어, Hello, Kotlin!은 코틀린 문법에서 인식되지 않는 단어이지만,
    // "Hello, Kotlin!"처럼 큰따옴표로 감싸면 문자열로 인식됩니다.
    // 큰따옴표 없이 작성하면 문법 오류(Syntax Error)가 발생합니다.

    val name = "Kotlin"
    // val은 변수를 선언할 때 사용하는 키워드로, 변경할 수 없는(read-only) 변수를 생성합니다.
    // name 변수에 문자열 "Kotlin"을 할당합니다.

    // IntelliJ IDEA에서 Alt + Enter를 누르면 하이라이트된 코드에 대한 수정 방법을 추천받을 수 있습니다.

    // 문자열 연결 방식 (Concatenation)
    println("Hello, " + name + "!")
    // 문자열을 연결할 때는 `+` 연산자를 사용할 수 있으며, 이는 Java 스타일의 문자열 연결 방식입니다.
    // 하지만, Kotlin에서는 문자열 연결보다 **문자열 템플릿(String Template)을 권장**합니다.

    // 문자열 템플릿 방식 (String Template)
    println("Hello, $name!")
    // 문자열 템플릿을 사용하면 `$변수명`을 문자열 안에서 직접 사용할 수 있어 가독성이 향상됩니다.
    // `$name`은 name 변수의 값을 자동으로 문자열에 삽입하여 "Hello, Kotlin!"이 출력됩니다.
} 
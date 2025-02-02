fun main() { // fun은 function의 약자로, 앞으로 나올 코드가 함수(Function)임을 나타내는 키워드, 뒤이어 오는 main은 함수의 이름을 나타낸다.
// {} 1 ~ 11 함수의 시작과 끝을 나타낸다. 중괄호 사이에 main 함수를 담을 명령어를 작성
    println("Hello, Kotlin!")
    // println은 화면에 텍스트를 출력하는 함수, println 함수 안에는 화면에 텍스트를 출력하는 데 필요한 명령어들이 담겨있음.
    // 참고로 println은 코틀린에 기본적으로 내장되어 있는 함수
    // 함수의 이름을 적고 소괄호를 열고 닫으면 해당 함수에 들어있는 명령어를 실행
    // 예를 들어 println()과 같이 적으면, println 함수 속에 담긴 명령거가 실행됨
    // 소괄호 안에 무언가를 적으면 함수를 실행하면서 특정 값을 전달할 수 있다. 여기서는 println 함수에 "Hello, Kotlin!"을 전달했다.

    // 텍스트를 큰따옴표로 감싸면 명령어가 아닌 텍스트 그 자체로 인식된다. Hello, Kotlin!은 코틀린 문법에 없는 단어이므로, Hello, Kotlin!을 큰따옴표로 감싸지 않으면 오류가 발생한다.
    // 최종적으로, println 함수에 "Hello, Kotlin!"을 전달하여 Hello, Kotlin!이 화면에 출력된다.
    val name = "Kotlin"
    // 하이라이트된 텍스트에 커서(caret)를 두고 Alt + Enter를 누르면 IntelliJ IDEA가 수정 방법을 제안하는 것을 볼 수 있음.
    // Kotlin에서는 문자열 연결(concatenation)보다 문자열 템플릿(String Template)을 사용하는 것이 권장됨
    println("Hello, " + name + "!") // 문자열 연결 방식, + 연산자를 사용하여 문자열을 결합하는 방식, Java 스타일의 코드

    println("Hello, $name!") // 문자열 템플릿, $name을 그대로 문자열 안에 넣어 더 직관적으로 작성 가능
}
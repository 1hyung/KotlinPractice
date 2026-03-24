//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    /*   val name = "Kotlin"
           //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
           // to see how IntelliJ IDEA suggests fixing it.
           // 하이라이트된 텍스트에 커서(caret)를 두고 Alt + Enter를 누르면 IntelliJ IDEA가 수정 방법을 제안하는 것을 볼 수 있음.
           println("Hello, " + name + "!") // 문자열 연결 방식, + 연산자를 사용하여 문자열을 결합하는 방식, Java 스타일의 코드

           println("Hello, $name!") // 문자열 템플릿, $name을 그대로 문자열 안에 넣어 더 직관적으로 작성 가능

           for (i in 1..5) {
               //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
               // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
               // 디버깅을 시작하려면 Shift + F9를 누르세요. 우리는 하나의 브레이크포인트를 설정해 두었지만, Ctrl + F8을 눌러 추가할 수도 있습니다.
               println("i = $i")
           }*//*


    */
    /*
        // 함수 정의
        fun sum(a: Int, b: Int): Int {
                return a + b
            }

            fun sum(a: Int, b: Int) = a + b

            fun printKotlin(): Unit {
                println("hello Kotlin")
            }

            fun printKotlin() {
                println("hello Kotlin")
            }*//*


    */
    /*
        // 지역 변수 정의
        val a: Int = 1 // 즉시 할당
            val b = 2 // 'Int' type 추론
            val c: Int // 컴파일 오류, 초기화가 필요함
            c = 3 // 컴파일 오류, 읽기 전용*//*


// 한줄 주석

    */
    /* 여러줄
        주석 *//*


    */
    /* block comment가
        /* 중첩도 가능 *//*

        *//*


        */
    /*
        // 문자열 템플릿
        var a = 1
        // simple name in template:
            val s1 = "a is $a"

            a = 2
        // arbitrary expression in template(arbitrary expression in template):
            val s2 = "${s1.replace("is", "was")}, but now is $a"
            println(s1)
            println(s2)*//*


        */
    /*
        // 조건문
        fun maxOf(a: Int, b: Int): Int {
               if (a > b) {
                   return a
               } else {
                   return b
               }
           }

           fun maxOf(a: Int, b: Int) = if (a > b) a else b*//*


        */
    /*
         // nullable
         fun parseInt(str: String): Int? { // 1. parseInt 함수
                // 이 함수는 문자열을 정수로 변환하려 시도합니다.
                // 만약 변환에 성공하면 Int 값을 반환하고,
                // 실패하면 null을 반환합니다.
                // 현재 코드는 본문이 비어있지만, 실제로는 아래와 같이 구현될 수 있습니다.
                // return str.toIntOrNull() // 코틀린 표준 라이브러리의 함수
            }*//*


        */
    /*    fun printProduct(arg1: String, arg2: String) { // 2. printProduct 함수
                val x: Int? = parseInt(arg1) // 3. arg1을 정수로 변환 시도. 결과는 Int 또는 null.
                val y: Int? = parseInt(arg2) // 4. arg2를 정수로 변환 시도. 결과는 Int 또는 null.

                // 5. 중요한 조건문: x와 y가 모두 null이 아닌지 확인
                if (x != null && y != null) {
                    println(x * y) // 6. 둘 다 null이 아니면 곱해서 출력
                } else {
                    println("either '$arg1' or '$arg2' is not a number ") // 7. 하나라도 null이면 오류 메시지 출력
                }
            }*//*

    // 자동 타입 변환
        fun getStringLength(obj: Any): Int? {
            if (obj is String) { // 1. 'obj'가 String 타입인지 체크
                // 2. 이 블록 안에서는 'obj'가 자동으로 String 타입으로 스마트 캐스트 됨
                return obj.length // 3. 따라서 'obj'를 String처럼 사용 가능 (length 속성 접근)
            }
            return null // 4. String이 아니면 null 반환
        }


     */
       // while loop
        val items = listOf("apple", "banana", "kiwi") // 1. 문자열 리스트(목록)를 생성합니다.
        var index = 0 // 2. 'index'라는 변수를 0으로 초기화합니다. 이 변수는 리스트의 인덱스(위치)를 추적하는 데 사용됩니다.

        while (index < items.size) { // 3. while 루프 시작: 'index'가 'items' 리스트의 크기(개수)보다 작은 동안 반복합니다.
            println("item at $index is ${items[index]}") // 4. 현재 인덱스에 해당하는 아이템을 출력합니다.
            index++ // 5. 'index' 값을 1 증가시킵니다.
        } // 6. 루프의 끝: 다시 3번 조건으로 돌아가 'index'가 'items.size'보다 작은지 확인합니다.


     */
// while expressionm
    fun describe(obj: Any): String = // 이 함수는 Any 타입의 객체를 받아 String을 반환합니다.
        when (obj) { // obj를 기준으로 조건을 검사합니다.
            1 -> "One" // 1. obj가 숫자 1과 정확히 같으면 "One" 반환
            "Hello" -> "Greeting" // 2. obj가 문자열 "Hello"와 정확히 같으면 "Greeting" 반환
            is Long -> "Long" // 3. obj가 Long 타입이면 "Long" 반환 (타입 체크)
            !is String -> "Not String" // 4. obj가 String 타입이 아니면 "Not String" 반환 (타입 체크의 부정)
            else -> "Unknown" // 5. 위 어떤 조건도 해당하지 않으면 "Unknown" 반환
        }
    //ranges
    val x = 3
    if (x in 1..10) {
        println("fits in range")
    }

    for (x in 1..5) {
        print(x)
    }

// // collections
//     val items = listOf("apple", "banana", "kiwi")
//     for (item in items) {
//         println(item)
//     }

    val items = setOf("apple", "banana", "kiwi")
    when {
        "orange" in items -> println("juicy")
        "apple" in items -> println("apple is fine too")
    }

    val fruits = listOf("banana", "avocado", "apple", "kiwi")
    fruits
        .filter { it.startsWith("a") }
        .sortedBy { it }
        .map { it.uppercase() }
        .forEach { println(it) }
}
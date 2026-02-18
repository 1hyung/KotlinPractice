package basic.variable

// var/val 변수명 : 변수타입 = 초기화값

fun main() {
    var name: String? = "원형"
    println(name)
    name = null
    println(name)
    name = "행도"
    println(name)

    var name1: String = "원형"
    var nullableName: String? = null

    // nullable한 변수를 사용할 때 변수가 null인지 아닌지 고려해서 사용해야 함
    println(name1.length) // null 타입이 아니기 때문에 값이 확실, 컴파일 에러 발생하지 않음
    /*
        nullableName.length // 컴파일 에러 발생
    */
    // ? 선언했을 때 컴파일 에러 해결 방법
    // 1. 조건문을 통해 null 확인
    if (nullableName != null) {
        println(nullableName.length)
    }

    // 2. ?. 연산자를 통해서 안전하게 호출 (Safe Call 연산자)
    // ?. nullableName이 null이면 null을 반환하고 아니면 다음을 실행하라
    println(nullableName?.length)

    // 3. ?: Elvis Operator, 엘비스 연산자
    // null이면 초기값을 설정할 수 있음
    val length: Int = nullableName?.length ?: 50
    println(length)

    // 4. !! 연산자
    /*
    // !! 사용할 때는 많은 주의가 필요하고 가능하면 사용하지 않는 것이 좋음
    // nullableName이 null이 아니라면 정상적이게 작동하지만 Exception은 미연의 방지가 중요
     println(nullableName!!.length) // NullPointException이 발생
     */

    // 5. let 함수와 Safe Call 조합
    // null이 아닐 때만 블록 실행, it으로 값에 접근
    nullableName?.let {
        println("이름: $it, 길이: ${it.length}")
    }

    // 6. takeIf / takeUnless 활용
    // 조건을 만족하면 값 반환, 아니면 null 반환
    val validName = name?.takeIf { it.length > 2 }
    println("validName: $validName")

    // takeUnless는 반대로 조건을 만족하지 않으면 값 반환
    val shortName = name?.takeUnless { it.length > 5 }
    println("shortName: $shortName")

    // 7. 컬렉션에서의 Null Safety
    val names: List<String?> = listOf("원형", null, "행도", null, "철수")
    val nonNullNames: List<String> = names.filterNotNull()
    println("nonNullNames: $nonNullNames")

    // firstOrNull, lastOrNull 등도 null safety하게 사용 가능
    val firstName = names.firstOrNull()
    println("firstName: $firstName")

    // 8. requireNotNull / checkNotNull
    // null이면 IllegalArgumentException 발생 (명시적 예외 처리)
    val testName: String? = "테스트"
    val guaranteedName = requireNotNull(testName) { "이름은 필수입니다" }
    println("guaranteedName: $guaranteedName")

    // 9. 스마트 캐스트 심화
    // 한 번 null 체크하면 해당 스코프에서 non-null로 취급
    val anotherName: String? = "스마트캐스트"
    if (anotherName != null && anotherName.length > 3) {
        // anotherName은 여기서 String 타입으로 스마트 캐스트됨
        println("스마트 캐스트된 이름: $anotherName")
    }

    // 10. run, also 등 스코프 함수와 조합
    val result = nullableName?.run {
        // this가 nullableName (non-null)
        "이름 길이: $length"
    } ?: "이름이 없습니다"
    println(result)
}

// lateinit 예시 (main 함수 밖에서 선언)
class LateinitExample {
    lateinit var lateInitName: String

    fun initialize() {
        lateInitName = "나중에 초기화"
    }

    fun printName() {
        // 사용 전 초기화 여부 확인 가능
        if (::lateInitName.isInitialized) {
            println(lateInitName)
        } else {
            println("아직 초기화되지 않음")
        }
    }
}
package basic.condition

/**
 * // when
if, else if, else문이 많아 질 경우 when을 사용하면 가독성을 향상 시킬 수 있음
enum class와 조합하여 사용하면 가독성 높고 안전한 코드를 작성할 수 있음*/

var number = 10
fun main() {
    /**    when (변수) {
    조건1 -> {
    조건1이 참일 경우 실행되는 코드
    }
    조건2 -> {
    조건2가 참일 경우 실행되는 코드
    }
    else -> {
    모든 조건 (1, 2)가 거짓일 경우 실행되는 코드
    }
    }*/

    number = 20
    when (number) {
        in 1..10 -> {
            println("1 ~ 10 사이 입니다.")
        }

        20 -> {
            println("20입니다")
        }

        30 -> {
            println("30입니다.")
        }

        else -> {
            println("예상치 못한 값입니다.")
        }
    }
    /**    if, else if, else는 범위 조건일 때 사용하기 적절하고,
    when은 값이 제한되고 특정 될 경우에 쓰기 적절합니다.*/
}
package basic.condition

/**
 * 조건문을 통해 변수 할당
 * 조건에 따른 변수에 값 할당을 할 수 있다
 */

fun main() {
    // if 표현식을 통한 변수 할당 (한 줄)
    var dust: Int = 40
    val result = if (dust <= 30) "놀 수 있다" else "집에서 쉰다"
    println("dust=${dust} ${result}")

    // if 표현식을 통한 변수 할당 (블록)
    dust = 28
    val ifResult = if (dust <= 30) {
        "놀 수 있다"
    } else {
        "집에서 쉰다"
    }
    println("dust=${dust} ${ifResult}")

    // when 표현식을 통한 변수 할당
    dust = 36
    val whenResult = when (dust) {
        in 0..35 -> {
            "놀 수 있다"
        }

        else -> {
            "집에서 쉰다"
        }
    }

    println("dust=${dust} ${whenResult}")
}
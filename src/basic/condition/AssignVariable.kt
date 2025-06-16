package basic.condition

/**    조건문을 통해 변수 할당
조건에 따른 변수에 값 할당을 할 수 있다*/

fun main() {
    // if문을 통한 변수 할당
    var dust: Int = 30
    var result: String = ""
    dust = 40

    if (dust <= 30) result = "놀 수 있다" else result = "집에서 쉰다"
    println("dust=${dust} ${result}")
    dust = 28
    val ifresult = if (dust <= 30) {
        "놀 수 있다"
    } else {
        "집에서 쉰다"
    }
    println("dust=${dust} ${ifresult}")

    dust = 36
    //when문을 통한 변수 할당
    val whenresult = when (dust) {
        in 0..35 -> {
            "놀 수 있다"
        }

        else -> {
            "집에서 쉰다"
        }
    }

    println("dust=${dust} ${whenresult}")
}
package basic.loop

fun main() {
    /**
    // break -> 코드 블럭 내에서 break를 만나면 즉시 반복문 종료
    // for
    for (index: Int in 1..10) {
    if (index == 5) {
    break
    }
    println("$index")
    }
     */

    /**
    // while
    var count: Int = 0
    while (count <= 5) {
    if (count == 3)
    break
    count++
    }
    println("$count")
     */

    // continue -> 아래 있는 코드들은 건너뛰고 다음 반복 진행
    /**    // for
    for (index: Int in 1..5) {
    if (index == 3) { // 3을 건너뛰고 다음으로 반복
    continue
    }
    println("$index") // 출력값: 1, 2, 4, 5
    }*/

    // while
    var count: Int = 0
    while (count <= 5) {
        count++
        if (count == 3)
            continue
    }
    println("$count")
}
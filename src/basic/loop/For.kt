package basic.loop

fun main() {
    /*    var total: Int = 0
        // 1 ~ 10까지 실행
        for (index: Int in 1..10) {
            total += index
        }
        println(total)*/

    /*    for (i in 1..10)
            println("i=$i")
        // 1 ~ 9까지 실행
        for (index: Int in 1 until 10)
            println(index)*/

    /*    // 2씩 증가 1, 3, 5, 7, 9
        for (i in 1..10 step (2))
            println("step(2)=$i")*/

    /*    // 10부터 1까지 하나씩 감소하면서 실행
        for (index in 10 downTo 1)
            println("downTo= $index")

        // 10부터 1까지 2씩 감소하면서 실행 10, 8, 6, 4, 2
        for (index in 10 downTo 1 step (2))
            println("downTo + step(2)= $index")*/

    val name = "wonhyung"
    for (i in 1..name.length) {
        println("name.length= $i")
    }

    val ten = 10
    for (i in 1..ten)
        println("ten= $i")
}
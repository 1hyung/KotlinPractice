package basic.loop

// 이중반복문
fun main() {
    for (outIndex: Int in 0..3) {
        println("outIndex 시작= $outIndex")
        for (inIndex: Int in 0..3) {
            println("   inIndex=$inIndex")
        }
        println("outIndex=$outIndex")
    }
}
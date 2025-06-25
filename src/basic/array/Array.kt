package basic.array

fun main() {
    /**
     * 배열 생성
     * 1. 배열 크기를 정하고 초기값 지정하여 생성하는 경우
     * val array = Array<변수타입>(배열사이즈) { 초기값 }
     */
    val array = Array<Int>(3) { 0 } // 0, 1, 2번째까지 있음, 배열은 0번째 인덱스부터 시작

    /**
     * 2. 특정값을 넣어서 배열을 생성하는 경우
     * val array = arrayOf<변수타입>(특정값1, 특정값2, 특정값3)
     */
    val array2 = arrayOf<Int>(100, 300, 200)
    println(array2.contentToString())

    // 배열 값 입력
    array2[0] = 120
    println(array2.contentToString())

    // 배열 값 읽기
    println("값= ${array2[0]}")
    println("값= ${array2[1]}")
    /*
        println("값= ${array2[3]}") // size가 3이라 0, 1, 2번째까지 없기 때문에 ArrayIndexOutOfBoundsException 발생
    */

    // 헬스장 회원의 3대 운동 기록 배열에 담기
    val records = Array(3) { 0 }

    records[0] = 120
    records[1] = 300
    records[2] = 200
    println(records.contentToString())

    // 가독성 높인 코드
    val records1 = arrayOf<Int>(
        140,
        320,
        220
    )



    println("---println 출력---")

    println("멤버 1의 3대 운동 기록은 ${records[0]}입니다.")
    println("멤버 2의 3대 운동 기록은 ${records[1]}입니다.")
    println("멤버 3의 3대 운동 기록은 ${records[2]}입니다.")

    println("---for 반복문 출력---")

    // 1 ~ 3
    for (index in 1..records.size) {
        println("맴버 ${index}의 3대 운동 기록은 ${records[index - 1]}입니다.")
    }
    println("---for 반복문 indices 출력---")
    // 0 ~ 2
    for (index in records1.indices) {
        println("맴버 ${index + 1}의 3대 운동 기록은 ${records1[index]}입니다.")
    }

    val records2 = arrayOf(
        arrayOf(1, 2, 3, 4, 5),
        arrayOf(1, 2, 3, 4, 5)
    )
    records2[0][1] = 100

    for (row in records2) {
        for (column in row) {
            println("value ${column}")
        }
    }

    for (row in records2.indices) {
        for (column in records2[row].indices) {
            println("value ${column}")
        }
    }
}
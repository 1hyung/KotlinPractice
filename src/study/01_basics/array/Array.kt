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

    /**
     * 3. 원시 타입 배열 (IntArray, DoubleArray 등)
     * Array<Int>는 내부적으로 Integer[] (박싱)으로 컴파일됨
     * IntArray는 int[]로 컴파일되어 박싱 비용이 없어 성능상 유리
     */
    val boxed = Array<Int>(3) { 0 }        // Integer[] (박싱 발생)
    val primitive = IntArray(3)             // int[] (박싱 없음) - 성능상 권장
    val primitive2 = intArrayOf(120, 300, 200)
    println("원시 타입 배열: ${primitive2.contentToString()}")

    // 헬스장 회원의 3대 운동 기록 배열에 담기
    val records = intArrayOf(120, 300, 200)
    println(records.contentToString())

    // 가독성 높인 코드
    val records1 = intArrayOf(
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

    println("---forEachIndexed 출력 (Kotlin 관용적 표현)---")
    records1.forEachIndexed { index, record ->
        println("맴버 ${index + 1}의 3대 운동 기록은 ${record}입니다.")
    }

    /**
     * 배열 유틸리티 함수
     * 실무에서 자주 사용하는 배열 관련 함수들
     */
    println("---배열 유틸리티 함수---")
    println("정렬: ${records.sorted()}")
    println("내림차순 정렬: ${records.sortedDescending()}")
    println("최대: ${records.max()}")
    println("최소: ${records.min()}")
    println("합계: ${records.sum()}")
    println("평균: ${records.average()}")

    // 필터 & 변환
    val over150 = records.filter { it > 150 }
    println("150 초과 기록: $over150")
    val doubled = records.map { it * 2 }
    println("2배 변환: $doubled")

    /**
     * 배열 복사 & 잘라내기
     */
    println("---배열 복사 & 잘라내기---")
    val original = intArrayOf(1, 2, 3, 4, 5)
    val copy = original.copyOf()
    println("전체 복사: ${copy.contentToString()}")
    val partial = original.copyOfRange(1, 3)
    println("부분 복사 (1~2 인덱스): ${partial.contentToString()}")
    val sliced = original.sliceArray(0..2)
    println("슬라이스 (0~2 인덱스): ${sliced.contentToString()}")

    /**
     * 2차원 배열
     */
    println("---2차원 배열---")
    val records2 = arrayOf(
        arrayOf(1, 2, 3, 4, 5),
        arrayOf(1, 2, 3, 4, 5)
    )
    records2[0][1] = 100

    // 값 직접 순회
    for (row in records2) {
        for (column in row) {
            println("value $column")
        }
    }

    // 인덱스로 순회하여 값 접근
    for (row in records2.indices) {
        for (column in records2[row].indices) {
            println("[$row][$column] = ${records2[row][column]}")
        }
    }

    /**
     * 배열 vs 리스트
     * 실무(특히 Spring 백엔드)에서는 배열보다 List를 훨씬 더 많이 사용
     * - 배열: 크기 고정, 값 변경 가능
     * - List: 크기 유동적, 불변/가변 선택 가능, 더 풍부한 API 제공
     */
    println("---배열 vs 리스트---")
    val immutableList = listOf(1, 2, 3)       // 읽기 전용
    val mutableList = mutableListOf(1, 2, 3)  // 추가/삭제 가능
    mutableList.add(4)
    println("불변 리스트: $immutableList")
    println("가변 리스트: $mutableList")

    // 배열 <-> 리스트 변환
    val toList = records.toList()
    val toArray = immutableList.toTypedArray()
    println("배열 -> 리스트: $toList")
    println("리스트 -> 배열: ${toArray.contentToString()}")
}
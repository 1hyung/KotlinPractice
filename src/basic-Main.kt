fun main() {
    println("Hello Kotlin#1") // 출력하고 한 칸 뜀
    print("Hello Kotlin#2") // 출력만
    println("Hello Kotlin#3")
    print("Hello Kotlin#4")

    /**
     * 사용자의 나이를 입력받아 성인인지 여부를 판단하는 함수입니다.
     * 성인의 기준은 18세 이상입니다.
     *
     * @param age 사용자의 나이 (정수), 재료
     * @return 성인이면 true, 그렇지 않으면 false, 완성된 요리
     */
    fun isAdult(age: Int): Boolean {
        // 성인 기준 나이
        val adultAge = 18

        /*
        만약 나이가 0보다 작거나 120보다 크면
        유효하지 않은 나이로 간주하고 예외를 발생시킬 수 있지만
        현재는 간단하게 처리합니다.
        */
        return age >= adultAge
    }

    fun main() {
        val myAge = 25
        val result = isAdult(myAge) // isAdult 함수 호출
        println("저는 성인인가요? $result") // 결과 출력
    }
}
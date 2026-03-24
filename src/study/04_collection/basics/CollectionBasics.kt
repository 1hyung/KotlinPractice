package study.`04_collection`.basics

/**
 * 컬렉션 기초 (Collection Basics)
 *
 * Kotlin의 List, Set, Map 기본 개념과 사용법을 학습합니다.
 * 불변(Immutable)과 가변(Mutable) 컬렉션의 차이를 이해합니다.
 */

fun main() {
    // ========================================
    // 1. List 기초
    // ========================================

    // 불변 리스트 (읽기 전용)
    val fruits = listOf("사과", "바나나", "오렌지")
    println("fruits: $fruits")
    println("첫 번째: ${fruits[0]}")
    println("마지막: ${fruits.last()}")
    println("크기: ${fruits.size}")

    // 빈 리스트
    val emptyList = emptyList<String>()
    println("빈 리스트: $emptyList, isEmpty: ${emptyList.isEmpty()}")

    // 가변 리스트 (수정 가능)
    val mutableFruits = mutableListOf("사과", "바나나")
    mutableFruits.add("오렌지")           // 추가
    mutableFruits.add(0, "포도")      // 특정 위치에 추가
    mutableFruits.remove("바나나")        // 삭제
    mutableFruits[0] = "청포도"           // 수정
    println("mutableFruits: $mutableFruits")

    // 리스트 순회
    println("\n--- 리스트 순회 ---")
    for (fruit in fruits) {
        println("과일: $fruit")
    }

    // 인덱스와 함께 순회
    for ((index, fruit) in fruits.withIndex()) {
        println("$index: $fruit")
    }

    // forEach 사용
    fruits.forEach { println("forEach: $it") }

    // forEachIndexed 사용
    fruits.forEachIndexed { index, fruit ->
        println("$index -> $fruit")
    }

    // ========================================
    // 2. Set 기초
    // ========================================

    println("\n--- Set 기초 ---")

    // Set: 중복을 허용하지 않는 컬렉션
    val numbers = setOf(1, 2, 3, 2, 1)  // 중복 자동 제거
    println("numbers: $numbers")  // [1, 2, 3]

    // 포함 여부 확인
    println("2 포함? ${2 in numbers}")      // true
    println("5 포함? ${numbers.contains(5)}")  // false

    // 가변 Set
    val mutableNumbers = mutableSetOf(1, 2, 3)
    mutableNumbers.add(4)
    mutableNumbers.add(2)  // 이미 있으므로 추가 안됨
    mutableNumbers.remove(1)
    println("mutableNumbers: $mutableNumbers")

    // HashSet vs LinkedHashSet
    val hashSet = hashSetOf(3, 1, 2)        // 순서 보장 안됨
    val linkedHashSet = linkedSetOf(3, 1, 2) // 삽입 순서 유지
    val sortedSet = sortedSetOf(3, 1, 2)    // 정렬된 순서

    println("hashSet: $hashSet")
    println("linkedHashSet: $linkedHashSet")
    println("sortedSet: $sortedSet")

    // ========================================
    // 3. Map 기초
    // ========================================

    println("\n--- Map 기초 ---")

    // Map: 키-값 쌍의 컬렉션
    val scores = mapOf(
        "Alice" to 95,
        "Bob" to 87,
        "Charlie" to 92
    )

    println("scores: $scores")
    println("Alice 점수: ${scores["Alice"]}")
    println("없는 키: ${scores["Unknown"]}")  // null
    println("기본값 사용: ${scores.getOrDefault("Unknown", 0)}")

    // Map 순회
    for ((name, score) in scores) {
        println("$name: $score점")
    }

    // keys, values
    println("키 목록: ${scores.keys}")
    println("값 목록: ${scores.values}")

    // 가변 Map
    val mutableScores = mutableMapOf("Alice" to 95)
    mutableScores["Bob"] = 87           // 추가
    mutableScores["Alice"] = 100        // 수정
    mutableScores.remove("Bob")         // 삭제
    mutableScores.putIfAbsent("Charlie", 92)  // 없을 때만 추가
    println("mutableScores: $mutableScores")

    // HashMap vs LinkedHashMap
    val hashMap = hashMapOf("c" to 3, "a" to 1, "b" to 2)
    val linkedHashMap = linkedMapOf("c" to 3, "a" to 1, "b" to 2)
    val sortedMap = sortedMapOf("c" to 3, "a" to 1, "b" to 2)

    println("hashMap: $hashMap")          // 순서 보장 안됨
    println("linkedHashMap: $linkedHashMap")  // 삽입 순서 유지
    println("sortedMap: $sortedMap")      // 키 기준 정렬

    // ========================================
    // 4. 컬렉션 변환
    // ========================================

    println("\n--- 컬렉션 변환 ---")

    val list = listOf(1, 2, 2, 3, 3, 3)

    // List → Set (중복 제거)
    val set = list.toSet()
    println("list to set: $set")

    // Set → List
    val backToList = set.toList()
    println("set to list: $backToList")

    // List → MutableList
    val mutableList = list.toMutableList()
    mutableList.add(4)
    println("mutableList: $mutableList")

    // Map 변환
    val pairs = listOf("a" to 1, "b" to 2, "c" to 3)
    val mapFromPairs = pairs.toMap()
    println("pairs to map: $mapFromPairs")

    // ========================================
    // 5. 컬렉션 기본 연산
    // ========================================

    println("\n--- 기본 연산 ---")

    val nums = listOf(5, 2, 8, 1, 9, 3)

    println("최솟값: ${nums.min()}")
    println("최댓값: ${nums.max()}")
    println("합계: ${nums.sum()}")
    println("평균: ${nums.average()}")
    println("개수: ${nums.count()}")

    // 조건부 카운트
    println("5 이상 개수: ${nums.count { it >= 5 }}")

    // 포함 여부
    println("5 포함? ${5 in nums}")
    println("10 미포함? ${10 !in nums}")

    // 첫 번째, 마지막
    println("first: ${nums.first()}")
    println("last: ${nums.last()}")
    println("firstOrNull (조건): ${nums.firstOrNull { it > 10 }}")  // null

    // ========================================
    // 6. 컬렉션 결합
    // ========================================

    println("\n--- 컬렉션 결합 ---")

    val list1 = listOf(1, 2, 3)
    val list2 = listOf(4, 5, 6)

    // + 연산자
    val combined = list1 + list2
    println("list1 + list2: $combined")

    // 요소 추가
    val withElement = list1 + 10
    println("list1 + 10: $withElement")

    // - 연산자 (요소 제거)
    val removed = list1 - 2
    println("list1 - 2: $removed")

    // union, intersect, subtract (Set 연산)
    val setA = setOf(1, 2, 3, 4)
    val setB = setOf(3, 4, 5, 6)

    println("합집합: ${setA union setB}")      // [1, 2, 3, 4, 5, 6]
    println("교집합: ${setA intersect setB}")  // [3, 4]
    println("차집합: ${setA subtract setB}")   // [1, 2]

    // ========================================
    // 7. 실전 예제
    // ========================================

    println("\n--- 실전 예제 ---")

    // 학생 점수 관리
    val studentScores = mutableMapOf<String, MutableList<Int>>()

    // 점수 추가 함수
    fun addScore(name: String, score: Int) {
        studentScores.getOrPut(name) { mutableListOf() }.add(score)
    }

    addScore("Alice", 85)
    addScore("Alice", 90)
    addScore("Alice", 88)
    addScore("Bob", 75)
    addScore("Bob", 80)

    println("학생별 점수: $studentScores")

    // 평균 계산
    for ((name, scoreList) in studentScores) {
        val avg = scoreList.average()
        println("$name 평균: ${"%.1f".format(avg)}점")
    }

    // 중복 제거 예제
    val items = listOf("apple", "banana", "apple", "orange", "banana")
    val uniqueItems = items.toSet().toList()
    println("중복 제거: $uniqueItems")

    // 빈도수 계산
    val frequency = items.groupingBy { it }.eachCount()
    println("빈도수: $frequency")
}

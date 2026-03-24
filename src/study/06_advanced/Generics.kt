package study.`06_advanced`

/**
 * 제네릭 (Generics)
 *
 * 타입 파라미터를 사용하여 재사용 가능한 클래스와 함수를 작성하는 방법을 학습합니다.
 */

fun main() {
    // ========================================
    // 1. 제네릭 클래스 기초
    // ========================================

    // 다양한 타입으로 Box 사용
    val intBox = Box(10)
    val stringBox = Box("Hello")
    val doubleBox = Box(3.14)

    println("intBox: ${intBox.value}")
    println("stringBox: ${stringBox.value}")
    println("doubleBox: ${doubleBox.value}")

    // ========================================
    // 2. 제네릭 함수
    // ========================================

    println("\n--- 제네릭 함수 ---")

    // 타입 추론
    val first = firstElement(listOf(1, 2, 3))
    val last = lastElement(listOf("a", "b", "c"))
    println("first: $first, last: $last")

    // 두 값 교환
    val pair = swap(1, "one")
    println("swap(1, \"one\"): $pair")

    // ========================================
    // 3. 여러 타입 파라미터
    // ========================================

    println("\n--- 여러 타입 파라미터 ---")

    val keyValue = KeyValue("name", "Alice")
    println("key: ${keyValue.key}, value: ${keyValue.value}")

    val result = keyValue.mapValue { it.uppercase() }
    println("mapped: ${result.value}")

    // ========================================
    // 4. 타입 제한 (Upper Bound)
    // ========================================

    println("\n--- 타입 제한 ---")

    // Number를 상속받는 타입만 사용 가능
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubles = listOf(1.5, 2.5, 3.5)

    println("Int 합계: ${sumOfNumbers(numbers)}")
    println("Double 합계: ${sumOfNumbers(doubles)}")

    // Comparable을 구현한 타입만 사용 가능
    println("최댓값: ${findMax(listOf(3, 1, 4, 1, 5))}")
    println("최댓값: ${findMax(listOf("apple", "banana", "cherry"))}")

    // ========================================
    // 5. 제네릭 제약 (where)
    // ========================================

    println("\n--- 제네릭 제약 ---")

    val sortableBox = SortableBox(listOf(3, 1, 4, 1, 5, 9, 2, 6))
    println("정렬 전: ${sortableBox.items}")
    println("정렬 후: ${sortableBox.sorted()}")

    // ========================================
    // 6. 공변성 (Covariance) - out
    // ========================================

    println("\n--- 공변성 (out) ---")

    // Producer<Dog>를 Producer<Animal>로 사용 가능
    val dogProducer: Producer<Dog> = DogProducer()
    val animalProducer: Producer<Animal> = dogProducer  // out 덕분에 가능

    val animal = animalProducer.produce()
    println("produced: ${animal.name}")

    // ========================================
    // 7. 반공변성 (Contravariance) - in
    // ========================================

    println("\n--- 반공변성 (in) ---")

    // Consumer<Animal>을 Consumer<Dog>로 사용 가능
    val animalConsumer: Consumer<Animal> = AnimalConsumer()
    val dogConsumer: Consumer<Dog> = animalConsumer  // in 덕분에 가능

    dogConsumer.consume(Dog("멍멍이"))

    // ========================================
    // 8. 타입 프로젝션 (Type Projection)
    // ========================================

    println("\n--- 타입 프로젝션 ---")

    val ints = mutableListOf(1, 2, 3)
    val anys: MutableList<Any> = mutableListOf("a", "b", "c")

    // out 프로젝션: 읽기만 가능
    copyData(ints, anys)
    println("copied: $anys")

    // ========================================
    // 9. 스타 프로젝션 (Star Projection)
    // ========================================

    println("\n--- 스타 프로젝션 ---")

    val mixedList: List<*> = listOf(1, "two", 3.0)
    for (item in mixedList) {
        println("item: $item (${item?.javaClass?.simpleName})")
    }

    printListInfo(listOf(1, 2, 3))
    printListInfo(listOf("a", "b", "c"))

    // ========================================
    // 10. reified 타입 파라미터
    // ========================================

    println("\n--- reified ---")

    val items = listOf(1, "two", 3, "four", 5.0)

    // reified를 사용하면 런타임에 타입 정보 접근 가능
    val strings = items.filterIsInstance<String>()
    val ints2 = items.filterIsInstance<Int>()

    println("strings: $strings")
    println("ints: $ints2")

    // 커스텀 reified 함수
    println("String 타입명: ${getTypeName<String>()}")
    println("Int 타입명: ${getTypeName<Int>()}")

    // ========================================
    // 11. 실전 예제: Result 타입
    // ========================================

    println("\n--- 실전 예제: Result 타입 ---")

    val successResult = fetchData(1)
    val errorResult = fetchData(-1)

    when (successResult) {
        is Result.Success -> println("성공: ${successResult.data}")
        is Result.Error -> println("실패: ${successResult.message}")
    }

    when (errorResult) {
        is Result.Success -> println("성공: ${errorResult.data}")
        is Result.Error -> println("실패: ${errorResult.message}")
    }

    // map 연산
    val mapped = successResult.map { it.uppercase() }
    println("mapped: $mapped")
}

// ========================================
// 제네릭 클래스와 함수 정의
// ========================================

// 1. 기본 제네릭 클래스
class Box<T>(val value: T) {
    fun getValue(): T = value

    fun <R> map(transform: (T) -> R): Box<R> {
        return Box(transform(value))
    }
}

// 2. 제네릭 함수
fun <T> firstElement(list: List<T>): T? = list.firstOrNull()
fun <T> lastElement(list: List<T>): T? = list.lastOrNull()
fun <A, B> swap(a: A, b: B): Pair<B, A> = Pair(b, a)

// 3. 여러 타입 파라미터
class KeyValue<K, V>(val key: K, val value: V) {
    fun <R> mapValue(transform: (V) -> R): KeyValue<K, R> {
        return KeyValue(key, transform(value))
    }
}

// 4. 타입 제한 (Upper Bound)
fun <T : Number> sumOfNumbers(list: List<T>): Double {
    return list.sumOf { it.toDouble() }
}

fun <T : Comparable<T>> findMax(list: List<T>): T? {
    return list.maxOrNull()
}

// 5. where를 사용한 다중 제약
class SortableBox<T>(val items: List<T>) where T : Comparable<T>, T : Number {
    fun sorted(): List<T> = items.sorted()
    fun sum(): Double = items.sumOf { it.toDouble() }
}

// 6. 공변성 (out) - Producer 역할
interface Producer<out T> {
    fun produce(): T
}

open class Animal(val name: String)
class Dog(name: String) : Animal(name)
class Cat(name: String) : Animal(name)

class DogProducer : Producer<Dog> {
    override fun produce(): Dog = Dog("새 강아지")
}

// 7. 반공변성 (in) - Consumer 역할
interface Consumer<in T> {
    fun consume(item: T)
}

class AnimalConsumer : Consumer<Animal> {
    override fun consume(item: Animal) {
        println("${item.name}을(를) 처리합니다.")
    }
}

// 8. 타입 프로젝션
fun copyData(source: List<out Any>, destination: MutableList<Any>) {
    for (item in source) {
        destination.add(item)
    }
}

// 9. 스타 프로젝션
fun printListInfo(list: List<*>) {
    println("리스트 크기: ${list.size}, 첫 번째 요소: ${list.firstOrNull()}")
}

// 10. reified (inline 함수에서만 사용 가능)
inline fun <reified T> getTypeName(): String {
    return T::class.simpleName ?: "Unknown"
}

inline fun <reified T> isType(value: Any): Boolean {
    return value is T
}

// 11. 실전 예제: Result 타입
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()

    fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }
}

fun fetchData(id: Int): Result<String> {
    return if (id > 0) {
        Result.Success("Data for id: $id")
    } else {
        Result.Error("Invalid id: $id")
    }
}

package study.`05_oop`

/**
 * 상속 (Inheritance)
 *
 * Kotlin에서 클래스 상속과 메서드 오버라이딩을 학습합니다.
 * Kotlin의 클래스는 기본적으로 final이므로 open 키워드가 필요합니다.
 */

fun main() {
    // ========================================
    // 1. 기본 상속
    // ========================================

    val dog = Dog("멍멍이", 3)
    dog.eat()       // Animal에서 상속
    dog.sleep()     // Animal에서 상속
    dog.bark()      // Dog 고유 메서드

    println()

    val cat = Cat("야옹이", 2)
    cat.eat()       // 오버라이딩된 메서드
    cat.sleep()     // Animal에서 상속
    cat.meow()      // Cat 고유 메서드

    // ========================================
    // 2. 다형성 (Polymorphism)
    // ========================================

    println("\n--- 다형성 ---")

    // 부모 타입으로 자식 객체 참조 가능
    val animals: List<Animal> = listOf(
        Dog("멍멍이", 3),
        Cat("야옹이", 2),
        Dog("바둑이", 5)
    )

    for (animal in animals) {
        animal.eat()      // 각 타입에 맞는 메서드 호출 (동적 바인딩)
        animal.makeSound()
    }

    // ========================================
    // 3. 타입 체크와 캐스팅
    // ========================================

    println("\n--- 타입 체크와 캐스팅 ---")

    for (animal in animals) {
        // is 연산자로 타입 체크
        when (animal) {
            is Dog -> {
                // 스마트 캐스트: 자동으로 Dog 타입으로 캐스팅
                animal.bark()
            }
            is Cat -> {
                animal.meow()
            }
        }
    }

    // as 연산자로 명시적 캐스팅
    val firstAnimal = animals[0]
    val dog2 = firstAnimal as Dog  // 확실할 때만 사용
    dog2.bark()

    // as? 연산자로 안전한 캐스팅
    val maybeCat = firstAnimal as? Cat  // 실패하면 null
    println("maybeCat: $maybeCat")

    // ========================================
    // 4. 추상 클래스 (Abstract Class)
    // ========================================

    println("\n--- 추상 클래스 ---")

    val circle = Circle(5.0)
    val rectangle = Rectangle(4.0, 3.0)

    println("원 넓이: ${circle.area()}")
    println("원 둘레: ${circle.perimeter()}")
    println("사각형 넓이: ${rectangle.area()}")
    println("사각형 둘레: ${rectangle.perimeter()}")

    // 도형 목록 처리
    val shapes: List<Shape> = listOf(circle, rectangle)
    val totalArea = shapes.sumOf { it.area() }
    println("전체 넓이 합: $totalArea")

    // ========================================
    // 5. 생성자 상속
    // ========================================

    println("\n--- 생성자 상속 ---")

    val employee = Employee("김개발", 30, "개발팀", 5000)
    employee.introduce()
    employee.work()

    val manager = Manager("이매니저", 40, "개발팀", 8000, 10)
    manager.introduce()
    manager.work()
    manager.manageTeam()

    // ========================================
    // 6. super 키워드
    // ========================================

    println("\n--- super 키워드 ---")

    val sportsCar = SportsCar("페라리", 350)
    sportsCar.start()  // 부모 메서드 + 추가 기능
    sportsCar.accelerate()

    // ========================================
    // 7. sealed class와 상속
    // ========================================

    println("\n--- sealed class ---")

    val results = listOf(
        Result.Success("데이터 로드 완료"),
        Result.Error(404, "페이지를 찾을 수 없습니다"),
        Result.Loading
    )

    for (result in results) {
        val message = when (result) {
            is Result.Success -> "성공: ${result.data}"
            is Result.Error -> "에러 ${result.code}: ${result.message}"
            is Result.Loading -> "로딩 중..."
            // sealed class는 모든 케이스를 알 수 있어 else 불필요
        }
        println(message)
    }
}

// ========================================
// 클래스 정의들
// ========================================

// 1. 부모 클래스 (open 키워드 필요)
open class Animal(val name: String, val age: Int) {
    // open 메서드: 오버라이딩 가능
    open fun eat() {
        println("$name 이(가) 먹이를 먹습니다.")
    }

    // open 없으면 오버라이딩 불가
    fun sleep() {
        println("$name 이(가) 잠을 잡니다.")
    }

    // 추상 메서드처럼 사용 (자식에서 구현 강제하려면 abstract class 사용)
    open fun makeSound() {
        println("$name 이(가) 소리를 냅니다.")
    }
}

// 2. 자식 클래스
class Dog(name: String, age: Int) : Animal(name, age) {
    // 고유 메서드
    fun bark() {
        println("$name: 멍멍!")
    }

    // 메서드 오버라이딩
    override fun makeSound() {
        bark()
    }
}

class Cat(name: String, age: Int) : Animal(name, age) {
    fun meow() {
        println("$name: 야옹~")
    }

    // eat 메서드 오버라이딩
    override fun eat() {
        println("$name 이(가) 우아하게 먹이를 먹습니다.")
    }

    override fun makeSound() {
        meow()
    }
}

// 3. 추상 클래스
abstract class Shape(val name: String) {
    // 추상 메서드: 반드시 구현해야 함
    abstract fun area(): Double
    abstract fun perimeter(): Double

    // 일반 메서드: 공통 기능
    fun describe() {
        println("$name - 넓이: ${area()}, 둘레: ${perimeter()}")
    }
}

class Circle(private val radius: Double) : Shape("원") {
    override fun area(): Double = Math.PI * radius * radius
    override fun perimeter(): Double = 2 * Math.PI * radius
}

class Rectangle(private val width: Double, private val height: Double) : Shape("사각형") {
    override fun area(): Double = width * height
    override fun perimeter(): Double = 2 * (width + height)
}

// 4. 생성자 상속
open class Person(val name: String, val age: Int) {
    open fun introduce() {
        println("안녕하세요, 저는 $name 이고 $age 세입니다.")
    }
}

open class Employee(
    name: String,
    age: Int,
    val department: String,
    val salary: Int
) : Person(name, age) {

    override fun introduce() {
        super.introduce()  // 부모 메서드 호출
        println("$department 에서 일하고 있습니다.")
    }

    open fun work() {
        println("$name 이(가) 업무를 수행합니다.")
    }
}

class Manager(
    name: String,
    age: Int,
    department: String,
    salary: Int,
    val teamSize: Int
) : Employee(name, age, department, salary) {

    override fun work() {
        println("$name 매니저가 팀을 이끌며 업무를 수행합니다.")
    }

    fun manageTeam() {
        println("$teamSize 명의 팀원을 관리합니다.")
    }
}

// 5. super 키워드 활용
open class Car(val brand: String) {
    open fun start() {
        println("$brand 시동을 겁니다.")
    }
}

class SportsCar(brand: String, val maxSpeed: Int) : Car(brand) {
    override fun start() {
        super.start()  // 부모 메서드 호출
        println("스포츠 모드를 활성화합니다. 최고 속도: ${maxSpeed}km/h")
    }

    fun accelerate() {
        println("$brand 가 빠르게 가속합니다!")
    }
}

// 6. sealed class (제한된 상속)
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val code: Int, val message: String) : Result()
    data object Loading : Result()
}

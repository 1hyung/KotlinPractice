package study.`03_class`

/**
 * 클래스 기초 (Class Basics)
 *
 * Kotlin의 클래스 선언, 생성자, 프로퍼티, 초기화 블록 등을 학습합니다.
 */

fun main() {
    // ========================================
    // 1. 기본 클래스 생성
    // ========================================

    val person1 = Person("원형", 25)
    println("${person1.name}, ${person1.age}세")
    person1.introduce()

    // ========================================
    // 2. 기본값이 있는 생성자
    // ========================================

    val person2 = Person("홍길동")  // 나이 기본값 0
    println("${person2.name}, ${person2.age}세")

    // ========================================
    // 3. 보조 생성자 (Secondary Constructor)
    // ========================================

    val employee1 = Employee("김개발", 30, "개발팀")
    val employee2 = Employee("이기획")  // 보조 생성자 사용

    employee1.showInfo()
    employee2.showInfo()

    // ========================================
    // 4. 초기화 블록 (init block)
    // ========================================

    val student = Student("박학생", -5)  // 음수 나이 → 0으로 보정

    // ========================================
    // 5. 프로퍼티 (Property)
    // ========================================

    val rectangle = Rectangle(10, 5)
    println("넓이: ${rectangle.area}")          // getter 자동 생성
    println("둘레: ${rectangle.perimeter}")

    // custom setter
    val counter = Counter()
    counter.count = 5
    println("count: ${counter.count}")
    counter.count = -3  // 음수 무시
    println("count: ${counter.count}")  // 여전히 5

    // ========================================
    // 6. 지연 초기화 (Late Initialization)
    // ========================================

    val service = UserService()
    service.initialize("원형")
    service.printUserName()

    // ========================================
    // 7. by lazy (지연 초기화 - val 전용)
    // ========================================

    val config = Configuration()
    println("Configuration 객체 생성됨")
    println("아직 settings에 접근하지 않음")
    println("settings 접근: ${config.settings}")  // 이 시점에 초기화

    // ========================================
    // 8. Data Class
    // ========================================

    val user1 = User("alice", "alice@email.com")
    val user2 = User("alice", "alice@email.com")

    // equals 자동 생성
    println("user1 == user2: ${user1 == user2}")  // true

    // toString 자동 생성
    println("user1: $user1")

    // copy 메서드
    val user3 = user1.copy(email = "newemail@test.com")
    println("user3: $user3")

    // 구조 분해 선언
    val (name, email) = user1
    println("이름: $name, 이메일: $email")

    // ========================================
    // 9. Enum Class
    // ========================================

    val today = DayOfWeek.MONDAY
    println("오늘: ${today.displayName}")
    println("주말인가? ${today.isWeekend}")

    // when과 함께 사용
    val message = when (today) {
        DayOfWeek.MONDAY -> "한 주의 시작!"
        DayOfWeek.FRIDAY -> "불금!"
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> "주말!"
        else -> "평일..."
    }
    println(message)

    // enum 순회
    println("모든 요일:")
    DayOfWeek.entries.forEach { println("  ${it.displayName}") }

    // ========================================
    // 10. Object (싱글톤)
    // ========================================

    // object는 싱글톤 - 인스턴스가 하나만 존재
    DatabaseConnection.connect()
    DatabaseConnection.query("SELECT * FROM users")
    DatabaseConnection.disconnect()

    // ========================================
    // 11. Companion Object
    // ========================================

    // companion object: 클래스 내부의 싱글톤
    // Java의 static과 유사하게 사용

    val id = IdGenerator.generate()
    println("생성된 ID: $id")
    println("다음 ID: ${IdGenerator.generate()}")

    // 팩토리 메서드 패턴
    val book1 = Book.createWithTitle("Kotlin in Action")
    val book2 = Book.createWithAuthor("Joshua Bloch")
    println("book1: ${book1.title} by ${book1.author}")
    println("book2: ${book2.title} by ${book2.author}")

    // ========================================
    // 12. 중첩 클래스와 내부 클래스
    // ========================================

    // 중첩 클래스 (nested class) - 외부 클래스 참조 없음
    val engine = Car.Engine("V8")
    engine.start()

    // 내부 클래스 (inner class) - 외부 클래스 참조 있음
    val car = Car("BMW")
    val wheel = car.Wheel("앞 왼쪽")
    wheel.rotate()
}

// ========================================
// 클래스 정의들
// ========================================

// 1. 기본 클래스 (주 생성자 사용)
class Person(val name: String, val age: Int = 0) {
    fun introduce() {
        println("안녕하세요, 저는 ${name}이고 ${age}세입니다.")
    }
}

// 2. 보조 생성자가 있는 클래스
class Employee(val name: String, val age: Int, val department: String) {
    // 보조 생성자 - 반드시 주 생성자를 호출해야 함
    constructor(name: String) : this(name, 0, "미정")

    constructor(name: String, department: String) : this(name, 0, department)

    fun showInfo() {
        println("$name ($age세) - $department")
    }
}

// 3. init 블록
class Student(name: String, age: Int) {
    val name: String
    val age: Int

    init {
        // 유효성 검사 및 초기화
        this.name = name
        this.age = if (age < 0) {
            println("경고: 나이가 음수입니다. 0으로 설정합니다.")
            0
        } else {
            age
        }
        println("Student 객체 생성: ${this.name}, ${this.age}세")
    }
}

// 4. 커스텀 getter/setter
class Rectangle(val width: Int, val height: Int) {
    // 커스텀 getter (계산된 프로퍼티)
    val area: Int
        get() = width * height

    val perimeter: Int
        get() = 2 * (width + height)
}

class Counter {
    var count: Int = 0
        set(value) {
            // 음수는 무시
            if (value >= 0) {
                field = value  // field는 실제 값을 저장하는 backing field
            }
        }
}

// 5. lateinit (var 전용, non-null 타입만)
class UserService {
    lateinit var userName: String

    fun initialize(name: String) {
        userName = name
    }

    fun printUserName() {
        // isInitialized로 초기화 여부 확인 가능
        if (::userName.isInitialized) {
            println("사용자: $userName")
        } else {
            println("아직 초기화되지 않음")
        }
    }
}

// 6. by lazy (val 전용)
class Configuration {
    // 처음 접근할 때 한 번만 초기화됨
    val settings: Map<String, String> by lazy {
        println("settings 초기화 중...")
        loadSettings()
    }

    private fun loadSettings(): Map<String, String> {
        // 설정 파일 로드 시뮬레이션
        return mapOf(
            "theme" to "dark",
            "language" to "ko"
        )
    }
}

// 7. Data Class
data class User(val name: String, val email: String)

// 8. Enum Class
enum class DayOfWeek(val displayName: String, val isWeekend: Boolean = false) {
    MONDAY("월요일"),
    TUESDAY("화요일"),
    WEDNESDAY("수요일"),
    THURSDAY("목요일"),
    FRIDAY("금요일"),
    SATURDAY("토요일", true),
    SUNDAY("일요일", true);

    fun nextDay(): DayOfWeek {
        return entries[(ordinal + 1) % entries.size]
    }
}

// 9. Object (싱글톤)
object DatabaseConnection {
    private var isConnected = false

    fun connect() {
        if (!isConnected) {
            println("데이터베이스 연결 중...")
            isConnected = true
            println("연결 완료!")
        }
    }

    fun query(sql: String) {
        if (isConnected) {
            println("쿼리 실행: $sql")
        } else {
            println("먼저 연결해주세요!")
        }
    }

    fun disconnect() {
        if (isConnected) {
            println("연결 해제")
            isConnected = false
        }
    }
}

// 10. Companion Object
class IdGenerator {
    companion object {
        private var currentId = 0

        fun generate(): Int {
            return ++currentId
        }

        // 상수 정의
        const val PREFIX = "ID_"
    }
}

// 팩토리 메서드 패턴
class Book private constructor(val title: String, val author: String) {
    companion object {
        fun createWithTitle(title: String): Book {
            return Book(title, "Unknown Author")
        }

        fun createWithAuthor(author: String): Book {
            return Book("Untitled", author)
        }
    }
}

// 11. 중첩 클래스와 내부 클래스
class Car(val brand: String) {
    // 중첩 클래스 (nested) - 외부 클래스 참조 불가
    class Engine(val type: String) {
        fun start() {
            println("$type 엔진 시작")
        }
    }

    // 내부 클래스 (inner) - 외부 클래스 참조 가능
    inner class Wheel(val position: String) {
        fun rotate() {
            // this@Car로 외부 클래스 참조
            println("${this@Car.brand}의 $position 바퀴가 회전합니다")
        }
    }
}

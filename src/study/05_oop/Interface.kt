package study.`05_oop`

/**
 * 인터페이스 (Interface)
 *
 * Kotlin 인터페이스의 선언, 구현, 다중 구현을 학습합니다.
 * 인터페이스는 추상 메서드와 기본 구현을 가질 수 있습니다.
 */

fun main() {
    // ========================================
    // 1. 기본 인터페이스 구현
    // ========================================

    val dog = Dog("멍멍이")
    dog.makeSound()
    dog.move()

    val bird = Bird("참새")
    bird.makeSound()
    bird.move()
    bird.fly()

    // ========================================
    // 2. 인터페이스 타입으로 참조
    // ========================================

    println("\n--- 인터페이스 타입 참조 ---")

    val animals: List<Animal> = listOf(dog, bird)
    for (animal in animals) {
        println("--- ${animal.name} ---")
        animal.makeSound()
        animal.move()
    }

    // ========================================
    // 3. 다중 인터페이스 구현
    // ========================================

    println("\n--- 다중 인터페이스 구현 ---")

    val smartphone = Smartphone("Galaxy")
    smartphone.call("010-1234-5678")
    smartphone.sendMessage("안녕하세요!")
    smartphone.takePhoto()
    smartphone.recordVideo()

    // ========================================
    // 4. 인터페이스의 기본 구현
    // ========================================

    println("\n--- 기본 구현 ---")

    val document = Document("보고서.pdf", 1024)
    document.printInfo()
    document.save()
    document.load()

    val image = Image("사진.jpg", 2048)
    image.printInfo()  // 오버라이딩된 메서드
    image.save()
    image.load()

    // ========================================
    // 5. 인터페이스 프로퍼티
    // ========================================

    println("\n--- 인터페이스 프로퍼티 ---")

    val adminUser = AdminUser(1, "admin@example.com")
    println("ID: ${adminUser.id}")
    println("Email: ${adminUser.email}")
    println("Display Name: ${adminUser.displayName}")
    println("Admin: ${adminUser.isAdmin}")

    // ========================================
    // 6. 인터페이스 상속
    // ========================================

    println("\n--- 인터페이스 상속 ---")

    val button = Button("확인")
    button.onClick()
    button.onLongClick()
    button.onDoubleClick()  // AdvancedClickable에서 상속

    // ========================================
    // 7. 충돌 해결 (다이아몬드 문제)
    // ========================================

    println("\n--- 충돌 해결 ---")

    val flyingFish = FlyingFish()
    flyingFish.move()  // 어떤 것을 사용할지 명시적으로 선택해야 함

    // ========================================
    // 8. 함수형 인터페이스 (SAM)
    // ========================================

    println("\n--- 함수형 인터페이스 ---")

    // 람다로 구현
    val doubler = IntOperation { it * 2 }
    println("5 * 2 = ${doubler.operate(5)}")

    // 함수 참조로 구현
    val squarer = IntOperation(::square)
    println("5^2 = ${squarer.operate(5)}")

    // 고차함수에서 활용
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubled = numbers.map { doubler.operate(it) }
    println("doubled: $doubled")

    // ========================================
    // 9. 실전 예제: Repository 패턴
    // ========================================

    println("\n--- Repository 패턴 ---")

    val userRepository: UserRepository = InMemoryUserRepository()

    // 사용자 추가
    userRepository.save(User(1, "Alice"))
    userRepository.save(User(2, "Bob"))
    userRepository.save(User(3, "Charlie"))

    // 조회
    println("ID 2: ${userRepository.findById(2)}")
    println("전체: ${userRepository.findAll()}")

    // 삭제
    userRepository.delete(2)
    println("삭제 후: ${userRepository.findAll()}")

    // ========================================
    // 10. 위임 (Delegation)
    // ========================================

    println("\n--- 위임 ---")

    val printer = ConsolePrinter()
    val logger = PrefixLogger(printer, "[LOG]")

    logger.print("시스템이 시작되었습니다.")
    logger.print("사용자가 로그인했습니다.")
}

// ========================================
// 인터페이스 정의들
// ========================================

// 1. 기본 인터페이스
interface Animal {
    val name: String  // 추상 프로퍼티

    fun makeSound()   // 추상 메서드
    fun move()        // 추상 메서드
}

class Dog(override val name: String) : Animal {
    override fun makeSound() {
        println("$name: 멍멍!")
    }

    override fun move() {
        println("$name 이(가) 네 발로 뜁니다.")
    }
}

// 2. 다중 인터페이스 구현
interface Flyable {
    fun fly()
}

class Bird(override val name: String) : Animal, Flyable {
    override fun makeSound() {
        println("$name: 짹짹!")
    }

    override fun move() {
        println("$name 이(가) 날아갑니다.")
    }

    override fun fly() {
        println("$name 이(가) 하늘 높이 날아갑니다!")
    }
}

// 3. 다중 인터페이스 구현 예제
interface Phone {
    fun call(number: String)
    fun sendMessage(text: String)
}

interface Camera {
    fun takePhoto()
    fun recordVideo()
}

class Smartphone(private val model: String) : Phone, Camera {
    override fun call(number: String) {
        println("$model: $number 로 전화를 겁니다.")
    }

    override fun sendMessage(text: String) {
        println("$model: 메시지 전송 - $text")
    }

    override fun takePhoto() {
        println("$model: 사진을 찍습니다.")
    }

    override fun recordVideo() {
        println("$model: 동영상을 녹화합니다.")
    }
}

// 4. 기본 구현이 있는 인터페이스
interface FileHandler {
    val fileName: String
    val fileSize: Int

    // 기본 구현
    fun printInfo() {
        println("파일명: $fileName, 크기: ${fileSize}KB")
    }

    // 추상 메서드
    fun save()
    fun load()
}

class Document(
    override val fileName: String,
    override val fileSize: Int
) : FileHandler {
    override fun save() {
        println("$fileName 문서를 저장합니다.")
    }

    override fun load() {
        println("$fileName 문서를 불러옵니다.")
    }
}

class Image(
    override val fileName: String,
    override val fileSize: Int
) : FileHandler {
    // 기본 구현 오버라이딩
    override fun printInfo() {
        println("이미지 파일: $fileName (${fileSize}KB)")
    }

    override fun save() {
        println("$fileName 이미지를 압축 저장합니다.")
    }

    override fun load() {
        println("$fileName 이미지를 불러옵니다.")
    }
}

// 5. 인터페이스 프로퍼티
interface UserInfo {
    val id: Int              // 추상 프로퍼티
    val email: String        // 추상 프로퍼티
    val displayName: String  // 기본 구현이 있는 프로퍼티
        get() = email.substringBefore("@")
}

class AdminUser(
    override val id: Int,
    override val email: String
) : UserInfo {
    val isAdmin = true
}

// 6. 인터페이스 상속
interface Clickable {
    fun onClick()
    fun onLongClick() {
        println("길게 클릭됨")
    }
}

interface AdvancedClickable : Clickable {
    fun onDoubleClick()
}

class Button(private val text: String) : AdvancedClickable {
    override fun onClick() {
        println("'$text' 버튼 클릭됨")
    }

    override fun onDoubleClick() {
        println("'$text' 버튼 더블 클릭됨")
    }
}

// 7. 충돌 해결
interface Swimmer {
    fun move() {
        println("수영으로 이동합니다.")
    }
}

interface Flyer {
    fun move() {
        println("날아서 이동합니다.")
    }
}

class FlyingFish : Swimmer, Flyer {
    // 충돌하는 메서드는 반드시 오버라이딩해야 함
    override fun move() {
        // super를 사용해 특정 인터페이스의 구현 호출
        println("물 위로 점프해서:")
        super<Flyer>.move()
        println("그리고 물 속에서:")
        super<Swimmer>.move()
    }
}

// 8. 함수형 인터페이스 (SAM - Single Abstract Method)
fun interface IntOperation {
    fun operate(value: Int): Int
}

fun square(x: Int): Int = x * x

// 9. Repository 패턴 예제
data class User(val id: Int, val name: String)

interface UserRepository {
    fun findById(id: Int): User?
    fun findAll(): List<User>
    fun save(user: User)
    fun delete(id: Int)
}

class InMemoryUserRepository : UserRepository {
    private val users = mutableMapOf<Int, User>()

    override fun findById(id: Int): User? = users[id]

    override fun findAll(): List<User> = users.values.toList()

    override fun save(user: User) {
        users[user.id] = user
    }

    override fun delete(id: Int) {
        users.remove(id)
    }
}

// 10. 위임 (Delegation)
interface Printer {
    fun print(message: String)
}

class ConsolePrinter : Printer {
    override fun print(message: String) {
        println(message)
    }
}

// by 키워드로 위임
class PrefixLogger(
    private val printer: Printer,
    private val prefix: String
) : Printer by printer {
    // print 메서드를 오버라이딩하여 prefix 추가
    override fun print(message: String) {
        printer.print("$prefix $message")
    }
}

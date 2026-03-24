package study.`07_spring`

/**
 * 의존성 주입 (Dependency Injection) 개념 학습
 *
 * Spring에서 DI는 객체 간의 의존 관계를 외부에서 주입받는 방식입니다.
 * 이 파일은 DI 개념을 순수 Kotlin으로 시뮬레이션합니다.
 *
 * 관련 문서: spring-kotlin-guide/docs/02_SPRING_BASICS.md
 */

fun main() {
    println("=== 의존성 주입 (DI) 개념 학습 ===\n")

    // ========================================
    // 1. DI 없이 (강한 결합)
    // ========================================
    println("--- 1. DI 없이 (강한 결합) ---")

    // 문제점: UserService가 직접 UserRepository를 생성
    // - 테스트하기 어려움 (Mock 사용 불가)
    // - 다른 구현체로 교체하기 어려움
    val badService = BadUserService()
    badService.getUser(1)

    // ========================================
    // 2. DI 사용 (느슨한 결합)
    // ========================================
    println("\n--- 2. DI 사용 (느슨한 결합) ---")

    // Repository 생성 (Spring이 자동으로 해줌)
    val repository: UserRepository = InMemoryUserRepository()

    // Service에 Repository 주입 (생성자 주입)
    val userService = UserService(repository)
    userService.getUser(1)

    // ========================================
    // 3. 인터페이스로 추상화
    // ========================================
    println("\n--- 3. 다른 구현체로 교체 ---")

    // 같은 인터페이스, 다른 구현체
    val dbRepository: UserRepository = DatabaseUserRepository()
    val userService2 = UserService(dbRepository)
    userService2.getUser(1)

    // ========================================
    // 4. Spring에서의 DI
    // ========================================
    println("\n--- 4. Spring에서의 DI 방식 ---")
    printSpringDIExample()

    // ========================================
    // 5. 의존성 주입 방식 비교
    // ========================================
    println("\n--- 5. 의존성 주입 방식 비교 ---")
    compareDIStyles()
}

// ========================================
// DI 없이 (강한 결합) - 나쁜 예
// ========================================

class BadUserService {
    // 직접 구현체를 생성 (강한 결합)
    private val repository = InMemoryUserRepository()

    fun getUser(id: Long) {
        val user = repository.findById(id)
        println("BadUserService: $user")
    }
}

// ========================================
// DI 사용 (느슨한 결합) - 좋은 예
// ========================================

// Repository 인터페이스 (추상화)
interface UserRepository {
    fun findById(id: Long): User?
    fun save(user: User): User
    fun findAll(): List<User>
}

// In-Memory 구현체
class InMemoryUserRepository : UserRepository {
    private val users = mutableMapOf(
        1L to User(1L, "Alice", "alice@email.com"),
        2L to User(2L, "Bob", "bob@email.com")
    )

    override fun findById(id: Long): User? {
        println("  [InMemoryRepository] findById($id)")
        return users[id]
    }

    override fun save(user: User): User {
        users[user.id] = user
        return user
    }

    override fun findAll(): List<User> = users.values.toList()
}

// Database 구현체 (시뮬레이션)
class DatabaseUserRepository : UserRepository {
    override fun findById(id: Long): User? {
        println("  [DatabaseRepository] SELECT * FROM users WHERE id = $id")
        // 실제로는 DB 조회
        return User(id, "DB User", "db@email.com")
    }

    override fun save(user: User): User {
        println("  [DatabaseRepository] INSERT INTO users ...")
        return user
    }

    override fun findAll(): List<User> = emptyList()
}

// Service (Repository에 의존)
class UserService(
    private val userRepository: UserRepository  // 생성자 주입
) {
    fun getUser(id: Long) {
        val user = userRepository.findById(id)
        println("  UserService: $user")
    }

    fun createUser(name: String, email: String): User {
        val user = User(System.currentTimeMillis(), name, email)
        return userRepository.save(user)
    }
}

// 데이터 클래스
data class User(
    val id: Long,
    val name: String,
    val email: String
)

// ========================================
// Spring DI 예제 설명
// ========================================

fun printSpringDIExample() {
    val example = """
    Spring에서는 어노테이션으로 DI를 설정합니다:

    // 1. Repository 등록
    @Repository
    class UserRepositoryImpl : UserRepository {
        override fun findById(id: Long): User? { ... }
    }

    // 2. Service에서 주입받기 (생성자 주입 - 권장)
    @Service
    class UserService(
        private val userRepository: UserRepository  // 자동 주입
    ) {
        fun getUser(id: Long) = userRepository.findById(id)
    }

    // 3. Controller에서 Service 주입
    @RestController
    class UserController(
        private val userService: UserService  // 자동 주입
    ) {
        @GetMapping("/users/{id}")
        fun getUser(@PathVariable id: Long) = userService.getUser(id)
    }

    Spring이 알아서:
    - @Repository, @Service, @Controller 클래스를 빈(Bean)으로 등록
    - 생성자의 파라미터를 보고 자동으로 의존성 주입
    """.trimIndent()

    println(example)
}

// ========================================
// 의존성 주입 방식 비교
// ========================================

fun compareDIStyles() {
    val comparison = """
    1. 생성자 주입 (권장)
       class UserService(private val repo: UserRepository)
       - 불변성 보장 (val)
       - 테스트 용이
       - 순환 참조 방지

    2. 필드 주입 (비권장)
       @Autowired
       lateinit var repo: UserRepository
       - 테스트하기 어려움
       - 불변성 보장 안됨

    3. Setter 주입 (선택적 의존성)
       @Autowired
       fun setRepo(repo: UserRepository) { ... }
       - 선택적 의존성일 때만 사용

    Kotlin에서는 생성자 주입이 기본이며 권장됩니다!
    """.trimIndent()

    println(comparison)
}

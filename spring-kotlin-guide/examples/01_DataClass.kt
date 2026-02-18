/**
 * 1. 데이터 클래스 (Data Classes)
 *
 * 데이터를 담기 위한 목적으로 설계된 클래스
 * data 키워드를 사용하면 다음 메서드가 자동으로 생성됩니다:
 * - equals() / hashCode()
 * - toString()
 * - copy()
 * - componentN() (구조 분해)
 */

// ============================================
// 1.1 기본 데이터 클래스
// ============================================

// 가장 간단한 형태의 데이터 클래스
data class User(
    val id: Long,
    val name: String,
    val email: String
)

// 사용 예시
fun basicDataClassExample() {
    val user1 = User(1L, "원형", "bro@example.com")
    val user2 = User(1L, "홍길동", "hong@example.com")

    // toString() 자동 생성
    println(user1)  // User(id=1, name=원형, email=bro@example.com)

    // equals() 자동 생성 - 모든 프로퍼티 값 비교
    println(user1 == user2)  // true

    // copy() 자동 생성 - 일부 값만 변경하여 복사
    val user3 = user1.copy(name = "김철수")
    println(user3)  // User(id=1, name=김철수, email=hong@example.com)

    // 구조 분해 선언 (destructuring)
    val (id, name, email) = user1
    println("ID: $id, Name: $name, Email: $email")
}

// ============================================
// 1.2 Nullable 타입과 기본값
// ============================================

// nullable 타입(?)과 기본값 설정
data class CustomerDTO(
    var customerId: Long? = null,        // nullable Long, 기본값 null
    var customerName: String = "",       // non-nullable String, 기본값 빈 문자열
    var age: Int? = null,                // nullable Int
    var phoneNumber: String? = null      // nullable String
)

fun nullableTypeExample() {
    // 모든 기본값 사용
    val customer1 = CustomerDTO()
    println(customer1)  // CustomerDTO(customerId=null, customerName=, age=null, phoneNumber=null)

    // 일부 값만 지정
    val customer2 = CustomerDTO(customerId = 100L, customerName = "이영희")
    println(customer2)  // CustomerDTO(customerId=100, customerName=이영희, age=null, phoneNumber=null)

    // named argument로 명확하게 지정
    val customer3 = CustomerDTO(
        customerId = 200L,
        customerName = "박민수",
        age = 30,
        phoneNumber = "010-1234-5678"
    )
    println(customer3)
}

// ============================================
// 1.3 var vs val
// ============================================

// var: 가변(mutable) - 값 변경 가능
// val: 불변(immutable) - 값 변경 불가
data class Product(
    val productId: Long,           // 불변 - 한번 설정하면 변경 불가
    var productName: String,       // 가변 - 언제든 변경 가능
    var price: Double,             // 가변
    val createdAt: String          // 불변
)

fun varVsValExample() {
    val product = Product(
        productId = 1L,
        productName = "노트북",
        price = 1500000.0,
        createdAt = "2025-12-04"
    )

    // var 프로퍼티는 변경 가능
    product.productName = "게이밍 노트북"
    product.price = 2000000.0

    // val 프로퍼티는 변경 불가 (컴파일 에러)
    // product.productId = 2L  // 에러!
    // product.createdAt = "2025-12-05"  // 에러!

    println(product)
}

// ============================================
// 1.4 복합 데이터 클래스
// ============================================

// 다른 데이터 클래스나 컬렉션을 포함하는 복합 구조
data class Address(
    val city: String,
    val street: String,
    val zipCode: String
)

data class PhoneNumber(
    val type: String,  // "mobile", "home", "work"
    val number: String
)

enum class UserRole {
    ADMIN, USER, GUEST
}

data class DetailedUser(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,                    // Enum 타입
    val address: Address?,                 // nullable 데이터 클래스
    val phoneNumbers: List<PhoneNumber>,   // 리스트 타입
    val tags: Set<String>,                 // 세트 타입
    val metadata: Map<String, Any>         // 맵 타입
)

fun complexDataClassExample() {
    val user = DetailedUser(
        id = 1L,
        name = "최수진",
        email = "choi@example.com",
        role = UserRole.ADMIN,
        address = Address(
            city = "서울",
            street = "강남대로 123",
            zipCode = "06000"
        ),
        phoneNumbers = listOf(
            PhoneNumber("mobile", "010-1111-2222"),
            PhoneNumber("work", "02-3333-4444")
        ),
        tags = setOf("VIP", "프리미엄", "신규"),
        metadata = mapOf(
            "joinDate" to "2025-01-01",
            "loginCount" to 50,
            "verified" to true
        )
    )

    println(user)

    // 중첩된 프로퍼티 접근
    println("도시: ${user.address?.city}")  // safe call (?.) 사용
    println("첫 번째 전화번호: ${user.phoneNumbers.firstOrNull()?.number}")
    println("태그 수: ${user.tags.size}")
    println("가입일: ${user.metadata["joinDate"]}")
}

// ============================================
// 1.5 생성자 파라미터 외에 추가 프로퍼티
// ============================================

// 생성자 파라미터 외에 클래스 본문에서 추가 프로퍼티 선언 가능
data class CarDTO(
    var carId: Long? = null,
    var maker: String = "",
    var model: String = "",
    var seat: Int = 0,
    var premium: Boolean = false
) {
    // 클래스 본문에서 추가 프로퍼티 정의
    var availableColors: List<String>? = null
    var fuelType: String? = null
    var rating: Double? = null

    // 계산된 프로퍼티 (getter만 있음)
    val displayName: String
        get() = "$maker $model"

    val description: String
        get() = "$displayName (${seat}인승${if (premium) ", 프리미엄" else ""})"
}

fun additionalPropertiesExample() {
    val car = CarDTO(
        carId = 1L,
        maker = "현대",
        model = "아반떼",
        seat = 5,
        premium = false
    ).apply {
        // 추가 프로퍼티 설정
        availableColors = listOf("흰색", "검정", "은색")
        fuelType = "가솔린"
        rating = 4.5
    }

    println(car.displayName)     // 현대 아반떼
    println(car.description)     // 현대 아반떼 (5인승)
    println("색상: ${car.availableColors}")
    println("연료: ${car.fuelType}")
}

// ============================================
// 1.6 데이터 클래스 실전 활용 패턴
// ============================================

// 패턴 1: API 요청/응답 DTO
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val age: Int
)

data class CreateUserResponse(
    val success: Boolean,
    val userId: Long?,
    val message: String
)

// 패턴 2: 데이터베이스 Entity
data class UserEntity(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String
)

// 패턴 3: 검색 조건 (Request Object)
data class UserSearchCriteria(
    val name: String? = null,
    val email: String? = null,
    val ageFrom: Int? = null,
    val ageTo: Int? = null,
    val role: UserRole? = null,
    val page: Int = 1,
    val pageSize: Int = 20
)

fun practicalPatternsExample() {
    // API 요청 예시
    val request = CreateUserRequest(
        name = "김개발",
        email = "dev@example.com",
        password = "secret123",
        age = 28
    )

    // 검색 조건 예시
    val criteria = UserSearchCriteria(
        name = "김",
        ageFrom = 20,
        ageTo = 40,
        page = 1
    )

    println("요청: $request")
    println("검색 조건: $criteria")
}

// ============================================
// 1.7 copy()로 불변성 유지하기
// ============================================

// copy()를 사용하면 원본을 변경하지 않고 새로운 인스턴스 생성
data class Settings(
    val theme: String,
    val fontSize: Int,
    val notifications: Boolean
)

fun copyExample() {
    val originalSettings = Settings(
        theme = "dark",
        fontSize = 14,
        notifications = true
    )

    // copy()로 일부만 변경한 새 인스턴스 생성
    val updatedSettings = originalSettings.copy(
        theme = "light"  // theme만 변경
    )

    println("원본: $originalSettings")
    println("수정본: $updatedSettings")

    // 원본은 변경되지 않음 (불변성 유지)
    println("원본 theme: ${originalSettings.theme}")  // dark
}

// ============================================
// 메인 함수
// ============================================

fun main() {
    println("=== 1.1 기본 데이터 클래스 ===")
    basicDataClassExample()

    println("\n=== 1.2 Nullable 타입과 기본값 ===")
    nullableTypeExample()

    println("\n=== 1.3 var vs val ===")
    varVsValExample()

    println("\n=== 1.4 복합 데이터 클래스 ===")
    complexDataClassExample()

    println("\n=== 1.5 추가 프로퍼티 ===")
    additionalPropertiesExample()

    println("\n=== 1.6 실전 활용 패턴 ===")
    practicalPatternsExample()

    println("\n=== 1.7 copy() 예시 ===")
    copyExample()
}
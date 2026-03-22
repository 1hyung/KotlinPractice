/te# Kotlin 기초 문법 학습 가이드

Kotlin 기본 문법을 학습합니다.

---

## Day 1-2: 기본 문법

### 1. 변수 선언
```kotlin
// val: 불변 (Java의 final)
val name: String = "1hyung"
val age = 25  // 타입 추론

// var: 가변
var count = 0
count = 10  // OK

// Null 안전성
var nullable: String? = null  // ?를 붙여야 null 가능
var nonNull: String = "안녕"  // null 불가
```

**연습 문제**:
```kotlin
// 당신의 이름, 나이를 val로 선언해보세요
// val name: String = "1hyung"
// val age: Int = 31

// nullable한 이메일 변수를 선언해보세요
// var email: String? = null
// email = "1hyung@example.com"
```

---

### 2. 함수
```kotlin
// 기본 함수
fun add(a: Int, b: Int): Int {
    return a + b
}

// 단일 표현식 함수 (return 생략)
fun add2(a: Int, b: Int): Int = a + b

// 반환 타입이 없는 함수 (Unit은 생략 가능)
fun printHello() {
    println("Hello")
}

// 기본 매개변수
fun greet(name: String = "Guest") {
    println("Hello, $name")
}

// 명명된 인자
greet(name = "1hyung")
```

**연습 문제**:
```kotlin
// 두 숫자를 곱하는 함수를 작성해보세요
fun multiply(a: Int, b: Int) = a * b

// 이름을 받아서 "안녕하세요, {이름}님"을 출력하는 함수를 작성해보세요
fun greet(name: String) {
    println("안녕하세요, ${name}님")
}
```

---

### 3. 클래스와 데이터 클래스
```kotlin
// 일반 클래스
class Person(val name: String, var age: Int) {
    fun introduce() {
        println("제 이름은 $name이고, 나이는 $age살입니다.")
    }
}

// 데이터 클래스 (DTO용): equals/hashCode/toString/copy() 메서드가 자동으로 생성됨
data class User(
    val id: Long,
    val name: String,
    val email: String?
)

// 사용 예시
val user = User(
    id = 1,
    name = "1hyung",
    email = "1hyung@example.com"
)

// copy() - 일부 필드만 변경한 복사본 생성
val updatedUser = user.copy(email = "new@example.com")
```

#### data class가 자동으로 만들어주는 것들

`data class`를 선언하면 다음 4가지 메서드가 자동으로 생성됩니다. 직접 작성하지 않아도 됩니다.

**① equals() - 값이 같은지 비교**

```kotlin
// 일반 클래스는 메모리 주소(객체의 위치)를 비교
class PersonNormal(val name: String, val age: Int)
val p1 = PersonNormal("1hyung", 25)
val p2 = PersonNormal("1hyung", 25)
println(p1 == p2)  // false! (같은 값이지만 다른 객체이므로)

// data class는 필드 값을 비교
data class PersonData(val name: String, val age: Int)
val p3 = PersonData("1hyung", 25)
val p4 = PersonData("1hyung", 25)
println(p3 == p4)  // true! (name과 age 값이 같으므로)
```

→ API 응답 비교, 테스트 코드 작성 시 매우 중요합니다.

**② hashCode() - Map/Set에서 빠른 검색**

```kotlin
data class User(val id: Long, val name: String)
val user1 = User(1, "1hyung")
val user2 = User(1, "1hyung")  // user1과 다른 객체지만 같은 값

// Set에서 중복 제거
val userSet = setOf(user1, user2)
println(userSet.size)  // 1 (같은 값 = 중복으로 인식, 자동 제거!)

// Map에서 키로 사용
val map = mapOf(user1 to "서울시 강남구")
println(map[user2])  // "서울시 강남구" (같은 값이라 찾을 수 있음!)
```

→ 일반 클래스였다면 `user1`과 `user2`는 다른 객체라 Map에서 찾을 수 없습니다.

**③ toString() - 보기 좋은 출력**

```kotlin
class PersonNormal(val name: String, val age: Int)
data class PersonData(val name: String, val age: Int)

val normal = PersonNormal("1hyung", 25)
val data = PersonData("1hyung", 25)

println(normal)  // com.example.PersonNormal@7852e922 (메모리 주소, 알아보기 불편)
println(data)    // PersonData(name=1hyung, age=25) (필드값이 그대로 보임!)
```

→ 로그 출력이나 디버깅 시 `println()`만 해도 어떤 값인지 바로 확인할 수 있습니다.

**④ copy() - 일부 값만 바꾼 새 객체 생성**

```kotlin
data class User(val id: Long, val name: String, val email: String)
val user = User(1, "1hyung", "1hyung@example.com")

// 이메일만 바꾼 새 객체 생성
val updated = user.copy(email = "new@example.com")
println(user)    // User(id=1, name=1hyung, email=1hyung@example.com) - 원본 그대로!
println(updated) // User(id=1, name=1hyung, email=new@example.com) - 이메일만 변경

// 여러 필드 동시 변경도 가능
val renamed = user.copy(name = "new", email = "new@example.com")
```

→ `val`로 선언한 불변 객체를 업데이트할 때 사용합니다. 원본은 변경하지 않습니다.

> **요약**: `data class` = 값을 담기 위한 클래스. `equals`, `hashCode`, `toString`, `copy`가 자동 생성되어 편리합니다.

**연습 문제**:
```kotlin
// Car 데이터 클래스를 만들어보세요 (제조사, 모델, 연식)
data class Car(
    val maker: String,
    val model: String,
    val year: Int
)
```

---

## Day 3-4: Null 안전성과 스코프 함수

### 4. Null 안전성

Kotlin에서 가장 중요한 개념 중 하나입니다. Java의 NullPointerException을 방지하기 위해 설계되었습니다.

#### 4-1. 안전 호출 (?.)

**의미**: null이 아닐 때만 메서드/프로퍼티 호출

```kotlin
var name: String? = null

// name이 null이면 length를 호출하지 않고 null 반환
val length = name?.length
println(length)  // null

// name이 값이 있으면 length 호출
name = "1hyung"
val length2 = name?.length
println(length2)  // 6
```

**실생활 비유**: "문이 열려있으면 들어가고, 닫혀있으면 그냥 null 반환"

---

#### 4-2. 엘비스 연산자 (?:)

**의미**: null이면 기본값 사용

```kotlin
var name: String? = null

// name이 null이면 0 반환, 아니면 length 반환
val nameLength = name?.length ?: 0
println(nameLength)  // 0

name = "1hyung"
val nameLength2 = name?.length ?: 0
println(nameLength2)  // 6 (1hyung의 글자 수)
```

**실생활 비유**: "냉장고에 우유가 있으면 우유 마시고, 없으면 물 마시기"

---

#### 4-3. !! 연산자 (절대 확신할 때만!)

**의미**: "이건 절대 null이 아니야!"라고 강제로 선언 (위험!)

```kotlin
var name: String? = "1hyung"

// !! 사용 - null이 아니라고 확신
val definitelyNotNull = name!!.length  // OK - 6

// 하지만 실제로 null이면?
name = null
val crash = name!!.length  // 앱 터짐! (NullPointerException)
```

**주의**: 가급적 사용하지 마세요! 대신 `?.`이나 `?:`를 사용하세요.

---

#### 4-4. Safe Cast (as?)

**의미**: 타입 변환을 시도하고, 실패하면 null 반환 (오류 안남)

```kotlin
// 예시 1: 성공하는 경우
val obj: Any = "문자열"
val str: String? = obj as? String  // 성공! "문자열" 반환
println(str)  // "문자열"

// 예시 2: 실패하는 경우
val obj2: Any = 123
val str2: String? = obj2 as? String  // 실패! null 반환 (오류 안남)
println(str2)  // null
```

**왜 이해가 맞는가?**
- `obj`가 문자열이면 → `str: String?`에 문자열 저장 (성공)
- `obj2: Any = 123`은 String이 아니니까 → `str2: String?`에 null 반환 (실패, 오류 안남)

**as vs as? 비교**:
```kotlin
// as - 위험! 실패하면 앱 터짐
val str: String = obj2 as String  // 앱 터짐!

// as? - 안전! 실패하면 null
val str: String? = obj2 as? String  // null 반환
```

---

#### 4-5. let 함수

**의미**: null이 아닐 때만 코드 블록 실행

```kotlin
var name: String? = null

// 전통적인 방법 (if 사용)
if (name != null) {
    println("이름: $name")
}

// Kotlin 스타일 (let 사용)
name?.let {
    println("이름: $it")  // it = name의 값
}

// 둘 다 실행 안됨 (name이 null이니까)
```

**null이 아닌 경우**:
```kotlin
name = "1hyung"

// if 사용
if (name != null) {
    println("이름: $name")          // 출력: 이름: 1hyung
    println("길이: ${name.length}") // 출력: 길이: 6
}

// let 사용 (더 간결!)
name?.let {
    println("이름: $it")          // 출력: 이름: 1hyung
    println("길이: ${it.length}") // 출력: 길이: 6
}
```

**let을 왜 쓸까?**
1. **더 간결함**: if 없이 한 줄로 null 체크
2. **여러 줄 처리 가능**: 블록 안에 여러 코드 작성
3. **스코프 제한**: it은 블록 안에서만 사용

```kotlin
// 여러 줄 처리
name?.let {
    val upper = it.uppercase()
    val first = it[0]
    println("$upper 의 첫 글자는 $first")
}
// name이 null이면 위 3줄 모두 실행 안됨!
```

---

#### 연습 문제 1: Safe Cast 연습

다음 코드의 출력 결과를 예측하고, 직접 실행해보세요.

```kotlin
fun main() {
    // 문제 1
    val data1: Any = "Kotlin"
    val text1: String? = data1 as? String
    println(text1)  // 출력: ?

    // 문제 2
    val data2: Any = 100
    val text2: String? = data2 as? String
    println(text2)  // 출력: ?

    // 문제 3
    val data3: Any = listOf(1, 2, 3)
    val list3: List<Int>? = data3 as? List<Int>
    println(list3)  // 출력: ?

    // 문제 4 - 타입이 맞지 않으면?
    val data4: Any = 3.14
    val num4: Int? = data4 as? Int
    println(num4)  // 출력: ?
}
```

<details>
<summary>정답 보기</summary>

```kotlin
println(text1)  // "Kotlin" - String이니까 성공
println(text2)  // null - Int라서 String 변환 실패
println(list3)  // [1, 2, 3] - List<Int>가 맞으니까 성공
println(num4)   // null - Double이라서 Int 변환 실패
```
</details>

---

#### 연습 문제 2: let vs if 연습

다음 상황을 `if`와 `let` 두 가지 방법으로 작성해보세요.

```kotlin
// 상황 1: email이 null이 아니면 이메일 전송
var email: String? = "1hyung@example.com"

// TODO: if 사용해서 작성
// if (email != null) {
//     println("이메일 전송: $email")
// }

// TODO: let 사용해서 작성
// email?.let {
//     println("이메일 전송: $it")
// }


// 상황 2: age가 null이 아니면 "나이: XX살" 출력
var age: Int? = 25

// TODO: if 사용해서 작성


// TODO: let 사용해서 작성


// 상황 3: phone이 null이 아니면, "-" 제거하고 출력
var phone: String? = "010-1234-5678"

// TODO: if 사용해서 작성
// if (phone != null) {
//     val cleaned = phone.replace("-", "")
//     println("전화번호: $cleaned")
// }

// TODO: let 사용해서 작성
// phone?.let {
//     val cleaned = it.replace("-", "")
//     println("전화번호: $cleaned")
// }
```

---

#### 연습 문제 3: 종합 문제

사용자 정보를 처리하는 함수를 만들어보세요.

```kotlin
data class User(
    val name: String,
    val email: String?,
    val age: Int?
)

fun processUser(user: User) {
    // TODO 1: name 출력 (name은 항상 있음)
    // println("이름: ${user.name}")

    // TODO 2: email이 있으면 "이메일: XXX" 출력, 없으면 "이메일: 없음" 출력
    // (힌트: ?: 사용)


    // TODO 3: age가 있으면 "XX살입니다" 출력 (let 사용)


    // TODO 4: email이 있고 "@gmail.com"을 포함하면 "Gmail 사용자" 출력
    // (힌트: let과 if 조합)

}

// 테스트
fun main() {
    val user1 = User("1hyung", "1hyung@example.com", 25)
    val user2 = User("new", null, null)

    println("=== 사용자 1 ===")
    processUser(user1)

    println("\n=== 사용자 2 ===")
    processUser(user2)
}
```

<details>
<summary>정답 보기</summary>

```kotlin
fun processUser(user: User) {
    // TODO 1
    println("이름: ${user.name}")

    // TODO 2
    val emailText = user.email ?: "없음"
    println("이메일: $emailText")

    // TODO 3
    user.age?.let {
        println("${it}살입니다")
    }

    // TODO 4
    user.email?.let { email ->
        if (email.contains("@gmail.com")) {
            println("Gmail 사용자")
        }
    }
}
```

**출력 결과**:
```
=== 사용자 1 ===
이름: 1hyung
이메일: 1hyung@example.com
25살입니다

=== 사용자 2 ===
이름: new
이메일: 없음
```
</details>

---

#### 연습 문제 4: 실전 문제

온라인 쇼핑몰의 할인 쿠폰 검증 시스템을 만들어보세요.

```kotlin
data class Coupon(
    val code: String,
    val discount: Int?,      // null이면 쿠폰 만료
    val minAmount: Int?      // 최소 구매 금액
)

fun applyCoupon(coupon: Coupon, orderAmount: Int): Int {
    // TODO 1: discount가 null이면 "쿠폰이 만료되었습니다" 출력하고 원래 금액 반환


    // TODO 2: minAmount가 있고, orderAmount보다 크면
    //         "최소 구매 금액 미달" 출력하고 원래 금액 반환


    // TODO 3: 할인 적용
    // val finalAmount = orderAmount - discount
    // println("할인 적용: ${discount}원")
    // return finalAmount

    return orderAmount  // 임시
}

// 테스트
fun main() {
    val coupon1 = Coupon("WELCOME", 5000, 30000)
    val coupon2 = Coupon("EXPIRED", null, 10000)
    val coupon3 = Coupon("VIPONLY", 10000, 100000)

    println("최종 금액: ${applyCoupon(coupon1, 50000)}원")  // 45000원
    println("최종 금액: ${applyCoupon(coupon2, 50000)}원")  // 50000원 (만료)
    println("최종 금액: ${applyCoupon(coupon3, 50000)}원")  // 50000원 (최소 금액 미달)
}
```

<details>
<summary>정답 보기</summary>

```kotlin
fun applyCoupon(coupon: Coupon, orderAmount: Int): Int {
    // TODO 1
    val discount = coupon.discount ?: run {
        println("쿠폰이 만료되었습니다")
        return orderAmount
    }

    // TODO 2
    coupon.minAmount?.let { minAmount ->
        if (orderAmount < minAmount) {
            println("최소 구매 금액 미달 (필요: ${minAmount}원)")
            return orderAmount
        }
    }

    // TODO 3
    val finalAmount = orderAmount - discount
    println("할인 적용: ${discount}원")
    return finalAmount
}
```

**출력 결과**:
```
할인 적용: 5000원
최종 금액: 45000원
쿠폰이 만료되었습니다
최종 금액: 50000원
최소 구매 금액 미달 (필요: 100000원)
최종 금액: 50000원
```
</details>

---

**예시 코드**:
```kotlin
// OrderDTO.kt
data class OrderDTO(
    var orderId: String?,           // nullable
    var firstName: String,          // non-null
    var lastName: String,
    var deadline: LocalDateTime,
    var currency: String?,          // nullable
    var items: List<OrderItemDTO>?  // nullable 리스트
)

// 사용 예시
fun processOrder(order: OrderDTO) {
    // 안전하게 currency 사용
    val currencyCode = order.currency ?: "USD"

    // 리스트가 null이 아닐 때만 처리
    order.items?.forEach { item ->
        println(item)
    }
}
```

---

### 5. 스코프 함수 (let, run, with, apply, also)
```kotlin
// let: 결과 반환, null 체크에 자주 사용
val result = user?.let {
    it.name.uppercase()
}

// apply: 객체 설정 후 객체 자체 반환
val person = Person("1hyung", 25).apply {
    age = 26  // this는 생략 가능
}

// run: 결과 반환
val greeting = user.run {
    "Hello, $name"
}

// also: 객체에 부가 작업 후 객체 반환 (로깅 등)
val savedUser = user.also {
    println("Saving user: $it")
}

// with: 객체의 여러 메서드 호출
with(user) {
    println(name)
    println(email)
}
```

**예시 코드**:
```kotlin
// OrderService.kt
suspend fun save(order: OrderDTO): OrderDTO {
    return OrderDTO().apply {
        this.orderId = OrderKeyGenerator.generate()
        this.firstName = order.firstName
        this.lastName = order.lastName
        this.status = OrderStatus.REQUEST
    }
}
```

---

## Day 5: 컬렉션 & 확장 함수

### 6. 컬렉션 함수
```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// map: 변환
val doubled = numbers.map { it * 2 }  // [2, 4, 6, 8, 10]

// filter: 필터링
val evens = numbers.filter { it % 2 == 0 }  // [2, 4]

// find: 첫 번째 일치 항목
val firstEven = numbers.find { it % 2 == 0 }  // 2

// groupBy: 그룹화
val grouped = numbers.groupBy { it % 2 == 0 }
// {false=[1, 3, 5], true=[2, 4]}

// sortedBy: 정렬
val users = listOf(
    User(1, "1hyung", null),
    User(2, "new", null)
)
val sorted = users.sortedBy { it.name }

// forEach: 반복
numbers.forEach { println(it) }
```

**예시 코드**:
```kotlin
// ProductService.kt
val filteredProducts = products
    .filter { it.status == ProductStatus.ACTIVE }
    .filter { it.category == request.category }
    .map { it.toResponseDTO() }
    .sortedBy { it.price }
```

---

### 7. 확장 함수
```kotlin
// 기존 클래스에 새 함수 추가
fun String.isEmailValid(): Boolean {
    return this.contains("@")
}

// 사용
val email = "1hyung@example.com"
println(email.isEmailValid())  // true

// Nullable 확장
fun String?.orEmpty(): String {
    return this ?: ""
}
```

**예시 코드**:
```kotlin
// FilePartExtensions.kt
suspend fun FilePart.toBytes(): ByteArray {
    return DataBufferUtils.join(this.content())
        .map { dataBuffer ->
            val bytes = ByteArray(dataBuffer.readableByteCount())
            dataBuffer.read(bytes)
            DataBufferUtils.release(dataBuffer)
            bytes
        }
        .awaitSingle()
}

// 사용
val filePart: FilePart = ...
val bytes = filePart.toBytes()  // 확장 함수 호출
```

---

## Day 6-7: 고급 문법

### 8. when 표현식 (switch문의 강력한 버전)
```kotlin
val status = OrderStatus.REQUEST

val message = when (status) {
    OrderStatus.REQUEST -> "요청됨"
    OrderStatus.PROCESSING -> "처리중"
    OrderStatus.CONFIRMED -> "확정됨"
    OrderStatus.COMPLETED -> "완료됨"
    else -> "알 수 없음"
}

// 여러 조건
when (x) {
    0, 1 -> println("0 또는 1")
    in 2..10 -> println("2에서 10 사이")
    else -> println("그 외")
}
```

---

### 9. sealed class (제한된 클래스 계층)
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// when과 함께 사용 (모든 경우 체크)
fun handleResult(result: Result<String>) {
    when (result) {
        is Result.Success -> println(result.data)
        is Result.Error -> println(result.message)
        Result.Loading -> println("로딩 중...")
    }
}
```

---

### 10. Companion Object (정적 멤버)
```kotlin
class OrderKeyGenerator {
    companion object {
        fun generate(): String {
            return TSID.Factory.getTsid().toString()
        }
    }
}

// 사용
val key = OrderKeyGenerator.generate()
```

---

### 11. 고차 함수와 람다
```kotlin
// 고차 함수: 함수를 매개변수로 받음
fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

// 사용
val sum = calculate(3, 5) { x, y -> x + y }
val product = calculate(3, 5) { x, y -> x * y }

// 컬렉션에서의 예시: filter, map 등이 모두 고차 함수
val filtered = list.filter { it.age > 18 }
```

---

---

### 12. interface (인터페이스)

인터페이스는 클래스가 반드시 구현해야 할 **행동을 정의**합니다. Spring 프로젝트에서 매우 자주 사용됩니다.

#### 기본 사용법

```kotlin
// 인터페이스 정의 (무엇을 해야 하는지만 정의, 어떻게는 구현체가 결정)
interface Animal {
    val name: String                    // 반드시 구현해야 함
    fun speak(): String                 // 반드시 구현해야 함
    fun breathe(): String = "숨쉬기..."  // 기본 구현 제공 (선택적으로 오버라이드)
}

// 구현 클래스
class Dog(override val name: String) : Animal {
    override fun speak(): String = "$name: 멍멍!"
}

class Cat(override val name: String) : Animal {
    override fun speak(): String = "$name: 야옹~"
    override fun breathe(): String = "고요하게 숨쉬기..."  // 기본 구현 덮어씀
}

// 사용
val animals: List<Animal> = listOf(Dog("바둑이"), Cat("나비"), Dog("초코"))
animals.forEach { println(it.speak()) }
// 바둑이: 멍멍!
// 나비: 야옹~
// 초코: 멍멍!
```

#### Spring에서 interface를 왜 쓸까?

Spring 프로젝트에서는 Service를 항상 interface + 구현체로 분리합니다.

```kotlin
// 1. 인터페이스: "OrderService는 이런 기능을 가져야 한다"는 약속
interface OrderService {
    suspend fun createOrder(dto: OrderDTO): OrderDTO
    suspend fun findById(id: Long): OrderDTO
    suspend fun cancelOrder(id: Long): OrderDTO
}

// 2. 구현체: 실제 비즈니스 로직 작성
@Service
class OrderServiceImpl(
    private val repository: OrderRepository
) : OrderService {

    override suspend fun createOrder(dto: OrderDTO): OrderDTO {
        // 실제 구현
        return repository.save(dto)
    }

    override suspend fun findById(id: Long): OrderDTO {
        return repository.findById(id)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
    }

    override suspend fun cancelOrder(id: Long): OrderDTO {
        val order = findById(id)
        return repository.save(order.copy(status = "CANCELLED"))
    }
}

// 3. Controller는 인터페이스 타입으로 주입받음 (구현체가 아닌!)
@RestController
class OrderController(
    private val orderService: OrderService  // 구현체가 아닌 인터페이스!
) {
    @PostMapping("/api/orders")
    suspend fun createOrder(@RequestBody dto: OrderDTO): OrderDTO {
        return orderService.createOrder(dto)
    }
}
```

**왜 인터페이스를 쓰는가?**

| 상황 | 일반 클래스만 쓸 때 | 인터페이스 쓸 때 |
|------|---------------------|-----------------|
| 테스트 | 실제 DB 연결 필요 | 가짜(Mock) 객체로 교체 가능 |
| 구현 변경 | 모든 사용처 수정 | 구현체만 바꾸면 됨 |
| 협업 | 구현 완성 전 사용 불가 | 인터페이스만 있으면 사용 가능 |

```kotlin
// 테스트 시 가짜 구현체 사용 예시
class FakeOrderService : OrderService {
    override suspend fun createOrder(dto: OrderDTO): OrderDTO {
        return dto.copy(id = 999L)  // DB 없이 가짜 응답
    }

    override suspend fun findById(id: Long): OrderDTO {
        return OrderDTO(id = id, status = "PENDING")  // 가짜 데이터 반환
    }

    override suspend fun cancelOrder(id: Long): OrderDTO {
        return OrderDTO(id = id, status = "CANCELLED")
    }
}

// 테스트에서
val controller = OrderController(FakeOrderService())  // 가짜 서비스 주입
```

#### 인터페이스 체크리스트

- [ ] `interface` 키워드로 정의한다
- [ ] 구현 클래스는 `: InterfaceName`으로 구현한다
- [ ] 메서드 앞에 `override` 키워드를 붙인다
- [ ] Spring Service는 인터페이스 + 구현체로 분리한다

---

## 실습 프로젝트: 간단한 도서 관리 시스템

위에서 배운 내용을 종합해서 만들어봅시다.

```kotlin
// 1. 데이터 클래스 정의
data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val price: Int,
    val category: String
)

// 2. 도서 관리 클래스
class BookManager {
    private val books = mutableListOf<Book>()

    // 도서 추가
    fun addBook(book: Book) {
        books.add(book)
        println("도서 추가: ${book.title}")
    }

    // 제목으로 검색
    fun findByTitle(title: String): Book? {
        return books.find { it.title.contains(title, ignoreCase = true) }
    }

    // 카테고리별 도서 목록
    fun findByCategory(category: String): List<Book> {
        return books.filter { it.category == category }
    }

    // 가격대 검색
    fun findByPriceRange(min: Int, max: Int): List<Book> {
        return books.filter { it.price in min..max }
    }

    // 전체 도서 출력
    fun printAllBooks() {
        books.forEach { book ->
            println("${book.id}. ${book.title} - ${book.author} (${book.price}원)")
        }
    }
}

// 3. 실행
fun main() {
    val manager = BookManager()

    manager.addBook(Book(1, "Kotlin in Action", "Dmitry", 30000, "IT"))
    manager.addBook(Book(2, "Spring Boot", "1hyung", 25000, "IT"))
    manager.addBook(Book(3, "Clean Code", "1hyung", 28000, "IT"))

    // 검색
    val found = manager.findByTitle("Kotlin")
    println("검색 결과: $found")

    // 가격대 검색
    val affordable = manager.findByPriceRange(20000, 27000)
    println("2만원~2.7만원 도서: $affordable")

    // 전체 출력
    manager.printAllBooks()
}
```

**TODO: 직접 추가 기능 구현해보기**
1. `removeBook(id: Long)` - 도서 삭제 함수
2. `updatePrice(id: Long, newPrice: Int)` - 가격 수정 함수
3. `getMostExpensiveBook()` - 가장 비싼 도서 찾기
4. `getAveragePrice()` - 평균 가격 계산

---

### 13. enum class (열거형 클래스)

상태, 카테고리처럼 **정해진 값의 집합**을 표현할 때 사용합니다. Spring 프로젝트에서 상태 관리에 필수입니다.

#### 기본 사용법

```kotlin
// 기본 enum
enum class Direction {
    NORTH, SOUTH, EAST, WEST
}

// 프로퍼티를 가진 enum (Spring 프로젝트에서 가장 많이 쓰는 형태)
enum class OrderStatus(val displayName: String, val code: String) {
    PENDING("대기중", "O001"),
    CONFIRMED("확정", "O002"),
    PROCESSING("처리중", "O003"),
    COMPLETED("완료", "O004"),
    CANCELLED("취소됨", "O005")
}

// 사용
val status = OrderStatus.PENDING
println(status.displayName)  // "대기중"
println(status.code)         // "O001"
println(status.name)         // "PENDING" (모든 enum이 가진 이름)
println(status.ordinal)      // 0 (순서, 0부터 시작)
```

#### when과 함께 사용 (가장 중요한 패턴)

```kotlin
fun getStatusMessage(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING   -> "주문이 접수되었습니다"
        OrderStatus.CONFIRMED -> "주문이 확정되었습니다"
        OrderStatus.PROCESSING -> "상품을 준비 중입니다"
        OrderStatus.COMPLETED -> "배송이 완료되었습니다"
        OrderStatus.CANCELLED -> "주문이 취소되었습니다"
        // else 없어도 됨: 모든 경우를 다뤘으므로 컴파일러가 보장
    }
}
```

> **핵심**: sealed class와 달리 enum은 모든 값이 같은 타입이고 인스턴스를 추가로 만들 수 없습니다. 단순한 상태/카테고리에는 enum, 각 케이스가 다른 데이터를 가져야 한다면 sealed class를 쓰세요.

#### enum 유틸리티

```kotlin
// 모든 값 순회
OrderStatus.values().forEach { println(it.displayName) }

// 이름으로 찾기 (없으면 예외)
val status = OrderStatus.valueOf("PENDING")

// 안전하게 찾기 (없으면 null)
val status = enumValues<OrderStatus>().find { it.name == "PENDING" }

// 코드로 찾기 (커스텀 검색)
val status = OrderStatus.values().find { it.code == "O001" }
```

#### Spring에서의 실전 예시

```kotlin
// 1. API 요청/응답에서 enum 사용
data class OrderDTO(
    val id: Long,
    val status: OrderStatus,   // enum 타입으로 직접 받기
    val customerName: String
)

// 2. 상태 전이 검증을 enum 안에 넣기
enum class OrderStatus(val displayName: String) {
    PENDING("대기중"),
    CONFIRMED("확정"),
    COMPLETED("완료"),
    CANCELLED("취소됨");

    fun canTransitionTo(next: OrderStatus): Boolean {
        return when (this) {
            PENDING   -> next == CONFIRMED || next == CANCELLED
            CONFIRMED -> next == COMPLETED || next == CANCELLED
            COMPLETED -> false   // 완료된 주문은 변경 불가
            CANCELLED -> false   // 취소된 주문은 변경 불가
        }
    }
}

// 사용
val current = OrderStatus.PENDING
if (current.canTransitionTo(OrderStatus.CONFIRMED)) {
    println("상태 변경 가능")
}
```

---

### 14. object 키워드 (싱글톤과 익명 객체)

#### 14-1. object 선언 (싱글톤)

프로그램 전체에서 단 하나의 인스턴스만 존재하는 객체입니다.

```kotlin
// 싱글톤 유틸리티 객체
object DateUtils {
    fun now(): LocalDateTime = LocalDateTime.now()

    fun format(date: LocalDateTime): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

// 사용 - 인스턴스 생성 없이 바로 사용
val now = DateUtils.now()
val formatted = DateUtils.format(now)
```

> companion object와의 차이: `companion object`는 특정 클래스 안에 정의, `object`는 독립적으로 존재

#### 14-2. 익명 객체 (Anonymous Object)

인터페이스나 추상 클래스를 일회성으로 구현할 때 사용합니다.

```kotlin
interface ClickListener {
    fun onClick(id: Long)
}

// 익명 객체로 일회성 구현
val listener = object : ClickListener {
    override fun onClick(id: Long) {
        println("클릭됨: $id")
    }
}

listener.onClick(1L)
```

#### 14-3. companion object 심화

```kotlin
class User(val name: String, val email: String) {
    companion object {
        // 팩토리 메서드 패턴 - 생성 로직을 companion object에 넣기
        fun create(name: String, email: String): User {
            require(name.isNotBlank()) { "이름은 필수입니다" }
            require(email.contains("@")) { "이메일 형식이 올바르지 않습니다" }
            return User(name, email)
        }

        // 상수 정의
        const val MAX_NAME_LENGTH = 50
    }
}

// 사용
val user = User.create("1hyung", "1hyung@example.com")
println(User.MAX_NAME_LENGTH)  // 50
```

---

### 15. 스마트 캐스트 (Smart Cast)

`is` 로 타입을 확인하면 해당 블록 안에서 **자동으로 캐스팅**됩니다. 별도의 as 변환 없이 바로 타입의 멤버에 접근 가능합니다.

```kotlin
// is 체크 후 스마트 캐스트
fun describe(obj: Any): String {
    return when (obj) {
        is String -> "문자열, 길이: ${obj.length}"     // obj가 자동으로 String 타입
        is Int    -> "정수, 두 배: ${obj * 2}"         // obj가 자동으로 Int 타입
        is List<*> -> "리스트, 크기: ${obj.size}"      // obj가 자동으로 List 타입
        else      -> "알 수 없음"
    }
}

println(describe("Kotlin"))  // "문자열, 길이: 6"
println(describe(42))        // "정수, 두 배: 84"
```

#### sealed class와 스마트 캐스트 (가장 중요한 조합)

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

fun handleResult(result: ApiResult<String>) {
    when (result) {
        is ApiResult.Success -> {
            // result가 자동으로 ApiResult.Success 타입으로 인식
            println("성공: ${result.data}")
        }
        is ApiResult.Error -> {
            // result가 자동으로 ApiResult.Error 타입으로 인식
            println("오류 ${result.code}: ${result.message}")
        }
        ApiResult.Loading -> println("로딩 중...")
    }
}
```

#### if 블록에서의 스마트 캐스트

```kotlin
fun processInput(input: Any?) {
    // null 체크 후 스마트 캐스트
    if (input == null) return
    // 여기서부터 input은 non-nullable 타입

    if (input is String) {
        println(input.uppercase())   // String 타입으로 자동 캐스트
    }
}
```

---

### 16. 구조 분해 선언 (Destructuring)

data class의 값들을 **여러 변수에 한 번에** 꺼낼 수 있습니다.

#### data class 구조 분해

```kotlin
data class Point(val x: Int, val y: Int)
data class User(val name: String, val age: Int, val email: String)

val point = Point(10, 20)
val (x, y) = point
println("x=$x, y=$y")  // x=10, y=20

val user = User("1hyung", 25, "1hyung@example.com")
val (name, age, email) = user
println("$name ($age)") // "1hyung (25)"

// 일부만 꺼낼 때: 필요 없는 것은 _ 로 무시
val (_, _, userEmail) = user
println(userEmail)  // "1hyung@example.com"
```

#### Map 구조 분해 (실전에서 자주 사용)

```kotlin
val map = mapOf("name" to "1hyung", "age" to "25")

// for 루프에서 구조 분해
for ((key, value) in map) {
    println("$key = $value")
}

// forEach에서 구조 분해
map.forEach { (key, value) ->
    println("$key: $value")
}
```

#### 함수 반환값 구조 분해

```kotlin
// Pair 반환
fun getMinMax(list: List<Int>): Pair<Int, Int> {
    return list.min() to list.max()
}

val (min, max) = getMinMax(listOf(3, 1, 4, 1, 5, 9))
println("최솟값: $min, 최댓값: $max")

// Spring에서 자주 보이는 패턴
data class PageResult<T>(val items: List<T>, val totalCount: Long)

fun getBooks(): PageResult<Book> = PageResult(books, 100L)

val (items, total) = getBooks()
println("총 ${total}개 중 ${items.size}개 조회")
```

---

### 17. by lazy / 프로퍼티 위임 (Property Delegation)

#### by lazy - 처음 사용할 때만 초기화

무거운 객체 초기화를 **처음 필요한 시점까지 미루는** 패턴입니다.

```kotlin
class HeavyService {
    // 처음 호출할 때 한 번만 초기화
    val expensiveData: List<String> by lazy {
        println("데이터 로딩 중...")  // 처음 접근 시 한 번만 실행
        loadFromDatabase()
    }

    private fun loadFromDatabase(): List<String> {
        return listOf("data1", "data2", "data3")
    }
}

val service = HeavyService()
// 아직 loadFromDatabase() 호출 안 됨
println(service.expensiveData)  // 여기서 처음 초기화
println(service.expensiveData)  // 두 번째부터는 캐시된 값 반환
```

**출력**:
```
데이터 로딩 중...
[data1, data2, data3]
[data1, data2, data3]
```

#### Spring에서의 실전 사용

```kotlin
@Component
class OrderProcessor {
    // 로깅 - 가장 흔한 by lazy 사용 패턴
    private val logger by lazy { LoggerFactory.getLogger(this::class.java) }

    // 정규식 - 한 번만 컴파일
    private val emailRegex by lazy {
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    fun validateEmail(email: String): Boolean {
        return emailRegex.matches(email)
    }
}
```

#### by Delegates.observable - 값 변경 감지

```kotlin
import kotlin.properties.Delegates

class Order {
    var status: String by Delegates.observable("PENDING") { property, oldValue, newValue ->
        println("${property.name} 변경: $oldValue → $newValue")
    }
}

val order = Order()
order.status = "CONFIRMED"  // "status 변경: PENDING → CONFIRMED"
order.status = "COMPLETED"  // "status 변경: CONFIRMED → COMPLETED"
```

---

## 실습 프로젝트: 간단한 도서 관리 시스템

위에서 배운 내용을 종합해서 만들어봅시다.

```kotlin
// 1. 데이터 클래스 정의
data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val price: Int,
    val category: String
)

// 2. 도서 관리 클래스
class BookManager {
    private val books = mutableListOf<Book>()

    // 도서 추가
    fun addBook(book: Book) {
        books.add(book)
        println("도서 추가: ${book.title}")
    }

    // 제목으로 검색
    fun findByTitle(title: String): Book? {
        return books.find { it.title.contains(title, ignoreCase = true) }
    }

    // 카테고리별 도서 목록
    fun findByCategory(category: String): List<Book> {
        return books.filter { it.category == category }
    }

    // 가격대 검색
    fun findByPriceRange(min: Int, max: Int): List<Book> {
        return books.filter { it.price in min..max }
    }

    // 전체 도서 출력
    fun printAllBooks() {
        books.forEach { book ->
            println("${book.id}. ${book.title} - ${book.author} (${book.price}원)")
        }
    }
}

// 3. 실행
fun main() {
    val manager = BookManager()

    manager.addBook(Book(1, "Kotlin in Action", "Dmitry", 30000, "IT"))
    manager.addBook(Book(2, "Spring Boot", "1hyung", 25000, "IT"))
    manager.addBook(Book(3, "Clean Code", "1hyung", 28000, "IT"))

    // 검색
    val found = manager.findByTitle("Kotlin")
    println("검색 결과: $found")

    // 가격대 검색
    val affordable = manager.findByPriceRange(20000, 27000)
    println("2만원~2.7만원 도서: $affordable")

    // 전체 출력
    manager.printAllBooks()
}
```

**TODO: 직접 추가 기능 구현해보기**
1. `removeBook(id: Long)` - 도서 삭제 함수
2. `updatePrice(id: Long, newPrice: Int)` - 가격 수정 함수
3. `getMostExpensiveBook()` - 가장 비싼 도서 찾기
4. `getAveragePrice()` - 평균 가격 계산

---

## 학습 체크리스트

### 기본 문법
- [ ] 변수 선언 (val, var, nullable)
- [ ] 함수 작성 (기본, 단일 표현식, 기본 매개변수)
- [ ] 클래스와 데이터 클래스

### Null 안전성
- [ ] 안전 호출 (?.)
- [ ] 엘비스 연산자 (?:)
- [ ] !! 연산자
- [ ] Safe Cast (as?)
- [ ] let으로 null 체크

### 함수형 프로그래밍
- [ ] 스코프 함수 (let, apply, run, also, with)
- [ ] 컬렉션 함수 (map, filter, find, groupBy, sortedBy)
- [ ] 고차 함수와 람다
- [ ] 확장 함수

### 클래스 계층
- [ ] when 표현식
- [ ] sealed class
- [ ] enum class (상태/카테고리 표현)
- [ ] companion object
- [ ] object 키워드 (싱글톤, 익명 객체)
- [ ] interface 패턴 이해

### 고급 기능
- [ ] 스마트 캐스트 (is + when)
- [ ] 구조 분해 선언 (data class, Map)
- [ ] by lazy / 프로퍼티 위임

### 실습
- [ ] 실습 프로젝트 완성
- [ ] TODO 추가 기능 구현

---

## 다음 단계

Kotlin 기초를 마스터했다면:
1. **Coroutine 학습** - `suspend` 함수, Flow 등 → `02_SPRING_BASICS.md` 참고
2. **Spring Boot 기초** - DI, Controller-Service-Repository 패턴
3. **코드 컨벤션** - `06_KOTLIN_CONVENTIONS.md` 참고

---

## 참고 자료

- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin Koans (실습)](https://play.kotlinlang.org/koans)
- [Kotlin 언어 레퍼런스](https://kotlinlang.org/docs/reference/)
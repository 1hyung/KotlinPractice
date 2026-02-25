# Kotlin 기초 문법 학습 가이드

Kotlin 기본 문법을 학습합니다.

---

## Day 1-2: 기본 문법

### 1. 변수 선언
```kotlin
// val: 불변 (Java의 final)
val name: String = "홍길동"
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
// val name: String = "류원형"
// val age: Int = 31

// nullable한 이메일 변수를 선언해보세요
// var email: String? = null
// email = "1hyung@gmail.com"
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

// 데이터 클래스 (DTO용, equals/hashCode/toString 자동 생성) -> equals/hashCode/toString 자동 생성 된다는 것이 무슨 말인지 모르겠음
data class User(
    val id: Long,
    val name: String,
    val email: String?
)

// 사용 예시
val user = User(
    id = 1,
    name = "김철수",
    email = "kim@example.com"
)

// copy() - 일부 필드만 변경한 복사본 생성
val updatedUser = user.copy(email = "new@example.com")
```

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
name = "홍길동"
val length2 = name?.length
println(length2)  // 3
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

name = "김철수"
val nameLength2 = name?.length ?: 0
println(nameLength2)  // 3 (김철수의 글자 수)
```

**실생활 비유**: "냉장고에 우유가 있으면 우유 마시고, 없으면 물 마시기"

---

#### 4-3. !! 연산자 (절대 확신할 때만!)

**의미**: "이건 절대 null이 아니야!"라고 강제로 선언 (위험!)

```kotlin
var name: String? = "홍길동"

// !! 사용 - null이 아니라고 확신
val definitelyNotNull = name!!.length  // OK - 3

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
name = "홍길동"

// if 사용
if (name != null) {
    println("이름: $name")          // 출력: 이름: 홍길동
    println("길이: ${name.length}") // 출력: 길이: 3
}

// let 사용 (더 간결!)
name?.let {
    println("이름: $it")          // 출력: 이름: 홍길동
    println("길이: ${it.length}") // 출력: 길이: 3
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
var email: String? = "user@example.com"

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
    val user1 = User("홍길동", "hong@gmail.com", 25)
    val user2 = User("김철수", null, null)

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
이름: 홍길동
이메일: hong@gmail.com
25살입니다
Gmail 사용자

=== 사용자 2 ===
이름: 김철수
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
val person = Person("홍길동", 25).apply {
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
    User(1, "Alice", null),
    User(2, "Bob", null)
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
val email = "test@example.com"
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
    manager.addBook(Book(2, "Spring Boot", "John", 25000, "IT"))
    manager.addBook(Book(3, "Clean Code", "Martin", 28000, "IT"))

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

- [ ] 변수 선언 (val, var, nullable)
- [ ] 함수 작성 (기본, 단일 표현식)
- [ ] 클래스와 데이터 클래스
- [ ] Null 안전성 (?, ?:, !!, let)
- [ ] 스코프 함수 (let, apply, run, also, with)
- [ ] 컬렉션 함수 (map, filter, find, groupBy)
- [ ] 확장 함수
- [ ] when 표현식
- [ ] sealed class
- [ ] companion object
- [ ] 고차 함수와 람다
- [ ] 실습 프로젝트 완성

---

## 다음 단계

Kotlin 기초를 마스터했다면:
1. **Coroutine 학습** - `suspend` 함수 사용법
2. **Spring Boot 기초** - 다음 학습 파일 참고

---

## 참고 자료

- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin Koans (실습)](https://play.kotlinlang.org/koans)
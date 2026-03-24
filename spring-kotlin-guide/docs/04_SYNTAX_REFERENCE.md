# Kotlin 문법 정리

> Kotlin 학습을 위한 문법 정리 자료

---

## 목차

1. [데이터 클래스 (Data Classes)](#1-데이터-클래스-data-classes)
2. [Null Safety](#2-null-safety)
3. [확장 함수 (Extension Functions)](#3-확장-함수-extension-functions)
4. [스코프 함수 (Scope Functions)](#4-스코프-함수-scope-functions)
5. [고차 함수와 람다](#5-고차-함수와-람다)
6. [컬렉션 처리 함수](#6-컬렉션-처리-함수)
7. [코루틴 (Coroutines)](#7-코루틴-coroutines)
8. [When 표현식](#8-when-표현식)
9. [Sealed Class](#9-sealed-class)
10. [Companion Object](#10-companion-object)
11. [프로퍼티 위임 (Property Delegation)](#11-프로퍼티-위임-property-delegation)
12. [제네릭 (Generics)](#12-제네릭-generics)
13. [Enum 클래스](#13-enum-클래스)
14. [Interface (인터페이스)](#14-interface-인터페이스)

---

## 1. 데이터 클래스 (Data Classes)

### 개념
데이터를 담기 위한 목적으로 설계된 클래스. `data` 키워드를 붙이면 `equals()`, `hashCode()`, `toString()`, `copy()` 등의 메서드가 자동으로 생성됩니다.

### 예시 1: 기본 DTO
```kotlin
data class UserDTO(
    var id: Long? = null,       // nullable 타입 + 기본값 null
    var name: String = "",      // 기본값 빈 문자열
    var email: String? = null
)
```

**핵심 포인트:**
- `var`로 선언하면 가변(mutable) 프로퍼티
- `?`를 붙이면 nullable 타입 (null을 허용)
- `= null` 또는 `= ""`로 기본값 설정 가능
- 기본값이 있으면 생성자 호출 시 해당 파라미터 생략 가능

```kotlin
// 사용 예시
val dto1 = UserDTO()  // 모든 기본값 사용
val dto2 = UserDTO(id = 123L)  // 일부만 지정
val dto3 = UserDTO(id = 123L, name = "1hyung", email = "1hyung@example.com")  // 모두 지정
```

### 예시 2: 복합 DTO
```kotlin
data class ProductDTO(
    var productId: Long? = null,
    var category: ProductCategory? = null,
    var name: String = "",
    var brand: String = "",
    var model: String = "",
    var quantity: Int = 0,
    var price: Int = 0,
    var images: List<ImageInfo>? = null,
    var isPremium: Boolean = false,
    var tags: List<TagInfo>? = null,
    var details: List<ProductDetail>? = null,
    var type: ProductType? = null,
) {
    // 클래스 본문에서 추가 프로퍼티 선언 가능
    var translations: List<ProductTranslationDTO>? = null
    var variants: List<ProductVariantDTO>? = null
}
```

**핵심 포인트:**
- 생성자 파라미터 외에 클래스 본문에서 추가 프로퍼티 정의 가능
- 다른 데이터 클래스나 Enum을 타입으로 사용 가능
- 복잡한 중첩 구조도 표현 가능

### 언제 사용할까?
- DTO (Data Transfer Object): API 요청/응답 데이터
- Entity: 데이터베이스 테이블과 매핑되는 객체
- 간단한 값 객체: 여러 값을 묶어서 전달할 때

---

## 2. Null Safety

### 개념
Kotlin은 null 참조로 인한 오류(NullPointerException)를 컴파일 시점에 방지합니다. nullable 타입과 non-nullable 타입을 명확히 구분합니다.

### 2.1 Safe Call Operator (`?.`)

**의미**: null이 아닐 때만 메서드/프로퍼티에 접근

```kotlin
suspend fun getLocation(address: String): LatLng? {
    val response = locationService.geocode(GeocodeRequest(address = address))

    // response.location이 null이면 toLatLng()를 호출하지 않고 null 반환
    return response.location?.toLatLng()
}
```

**실전 예시:**
```kotlin
val user: User? = getUser()
val name = user?.name  // user가 null이면 name도 null

// 체이닝 가능
val city = user?.address?.city  // user나 address가 null이면 city도 null
```

### 2.2 Elvis Operator (`?:`)

**의미**: 왼쪽 값이 null이면 오른쪽 값을 사용 (기본값 제공)

```kotlin
@GetMapping("/search")
suspend fun search(request: SearchRequest, locale: Locale?) = searchService.search(
    request.apply {
        // locale이 null이면 기본값 "en_US" 사용
        this.locale = locale?.toString() ?: Locale.US.toString()
    }
)
```

**실전 예시:**
```kotlin
val name = user?.name ?: "Unknown"  // user나 name이 null이면 "Unknown"
val count = list?.size ?: 0         // list가 null이면 0

// early return에도 사용
fun process(data: Data?) {
    val validData = data ?: return  // data가 null이면 함수 종료
    // validData는 이제 non-nullable 타입
    println(validData.value)
}
```

### 2.3 Not-null Assertion (`!!`)

**의미**: 개발자가 null이 아님을 보장. null이면 NullPointerException 발생

```kotlin
data.price = data.calculate(
    startDate,
    additionalTime ?: 0,
    data.serviceType,
    sourceRate,
    targetRate,
    commission,
    ::convertCurrency,
    false,
    CURRENCY_MAP["KRW"]!!  // KRW는 반드시 존재한다고 확신
)
```

**주의사항:**
- `!!`는 가급적 사용하지 않는 것이 좋습니다
- null일 가능성이 조금이라도 있다면 `?.`나 `?:`를 사용하세요
- 사용하더라도 주석으로 왜 null이 아닌지 설명하세요

### 2.4 let 블록과 함께 사용

**의미**: null이 아닐 때만 블록 실행

```kotlin
.where {
    request.keyword?.let {  // keyword가 null이 아닐 때만 실행
        and {
            or { productMeta.name contains it }
            or { productMeta.brand contains it }
            or { productMeta.description contains it }
        }
    }
}
```

**실전 예시:**
```kotlin
val user: User? = getUser()

// 방법 1: if문 사용
if (user != null) {
    println(user.name)
}

// 방법 2: let 사용 (더 코틀린스러움)
user?.let {
    println(it.name)  // it = user (non-nullable)
}

// 복잡한 처리에 유용
user?.let {
    val fullName = "${it.firstName} ${it.lastName}"
    println(fullName)
    saveToDatabase(it)
}
```

---

## 3. 확장 함수 (Extension Functions)

### 개념
기존 클래스를 수정하지 않고 새로운 함수를 추가할 수 있습니다. 마치 그 클래스의 멤버 함수처럼 사용할 수 있습니다.

### 예시 1: DTO 변환
```kotlin
fun ProductView.toProductDTO(cacheKey: String): ProductDTO {
    return ProductDTO(
        cacheKey = cacheKey,
        productId = this.productId,  // this는 ProductView 인스턴스
        categoryId = this.categoryId,
        productInfo = ProductInfo(
            brand = this.brand,
            model = this.model,
            quantity = this.quantity,
            price = this.price,
        ),
        discountPrice = this.discountPrice ?: 0.0,
        currency = this.currency!!,
    )
}
```

**사용 방법:**
```kotlin
val productView: ProductView = getProductView()
val productDTO = productView.toProductDTO("CACHE123")  // 마치 ProductView의 메서드처럼 호출
```

### 예시 2: 기본 타입 확장
```kotlin
data class LatLng(var lat: Double = 0.0, var lng: Double = 0.0)

// LatLng 클래스에 toPoint() 함수 추가
fun LatLng.toPoint() = Point.fromLngLat(lng, lat)
```

**사용 방법:**
```kotlin
val location = LatLng(lat = 37.5665, lng = 126.9780)
val point = location.toPoint()  // 확장 함수 호출
```

### 실전 팁
```kotlin
// String 확장 함수 예시
fun String.isEmail(): Boolean {
    return this.contains("@") && this.contains(".")
}

val email = "1hyung@example.com"
if (email.isEmail()) {
    println("Valid email")
}

// List 확장 함수 예시
fun <T> List<T>.second(): T? {
    return if (this.size >= 2) this[1] else null
}

val numbers = listOf(1, 2, 3)
println(numbers.second())  // 2
```

### 언제 사용할까?
- 타입 변환 함수 (`toDTO()`, `toEntity()`)
- 유틸리티 함수 (기존 클래스에 편의 기능 추가)
- 도메인 특화 함수 (비즈니스 로직)

---

## 4. 스코프 함수 (Scope Functions)

### 개념
객체의 컨텍스트 내에서 코드 블록을 실행하는 함수들. 코드를 더 간결하고 읽기 쉽게 만듭니다.

### 주요 스코프 함수 비교

| 함수 | 객체 참조 | 반환 값 | 주요 용도 |
|------|----------|---------|----------|
| `apply` | `this` | 객체 자신 | 객체 초기화 및 설정 |
| `also` | `it` | 객체 자신 | 추가 작업 (로깅, 검증 등) |
| `let` | `it` | 람다 결과 | null 체크, 변환 |
| `run` | `this` | 람다 결과 | 계산 및 결과 반환 |
| `with` | `this` | 람다 결과 | 여러 메서드 호출 |

### 4.1 apply - 객체 초기화

**특징**: 객체를 반환하므로 체이닝 가능

```kotlin
list.addAll(
    database.runQuery(query).map { rec ->
        ProductDTO().apply {  // ProductDTO 인스턴스를 생성하고 설정
            this.productId = rec[productMeta.id]
            this.brand = rec[brandMeta.name]!!
            this.model = rec[modelMeta.name] ?: rec[productMeta.model]!!
            this.quantity = rec[productMeta.quantity] ?: 0
            this.isPremium = rec[productMeta.premium] ?: false
            this.type = rec[productMeta.type]
            this.category = ProductCategory.SINGLE
        }  // apply는 ProductDTO 인스턴스를 반환
    }.distinctBy { it.productId }
)
```

**실전 예시:**
```kotlin
// apply 없이
val user = User()
user.name = "1hyung"
user.age = 30
user.email = "1hyung@example.com"

// apply 사용
val user = User().apply {
    name = "1hyung"  // this.name과 동일
    age = 30
    email = "1hyung@example.com"
}
```

### 4.2 let - null 체크 및 변환

**특징**: 람다의 결과를 반환

```kotlin
val authorities: Collection<GrantedAuthority> = jwt.claims["authorities"]?.let {
    // it = jwt.claims["authorities"] (non-nullable)
    AuthorityUtils.commaSeparatedStringToAuthorityList(
        it.asList(String::class.java).joinToString(",")
    )
} ?: AuthorityUtils.NO_AUTHORITIES  // null이면 기본값
```

**실전 예시:**
```kotlin
// null 체크와 변환
val user: User? = getUser()
val userName: String = user?.let {
    "${it.firstName} ${it.lastName}"
} ?: "Unknown"

// 여러 단계 처리
val result = data?.let {
    validate(it)
}?.let {
    transform(it)
}?.let {
    save(it)
}
```

### 4.3 also - 추가 작업

**특징**: 객체를 반환하므로 체이닝 가능, `it`으로 참조

```kotlin
if (request.includeMulti == true) {
    list.addAll(
        getMultiList(request).onEach { it.type = ProductCategory.MULTI }
    )
}
```

**실전 예시:**
```kotlin
// 로깅과 함께
val user = getUser().also {
    println("User fetched: ${it.name}")
}

// 디버깅
val numbers = mutableListOf(1, 2, 3).also {
    println("Before: $it")
}.also {
    it.add(4)
}.also {
    println("After: $it")
}
```

### 4.4 run - 계산 및 결과 반환

**특징**: 람다의 결과를 반환, `this`로 참조

```kotlin
val result = user?.run {
    // this = user
    "${firstName} ${lastName} (${age})"
}
```

### 스코프 함수 선택 가이드

```kotlin
// 1. 객체 초기화 → apply
val dialog = Dialog().apply {
    title = "Welcome"
    message = "Hello!"
    show()
}

// 2. null 체크 후 처리 → let
user?.let {
    println(it.name)
}

// 3. 추가 작업 (로깅 등) → also
val result = calculate().also {
    log("Result: $it")
}

// 4. 계산 후 결과 반환 → run
val fullName = user?.run {
    "$firstName $lastName"
}
```

---

## 5. 고차 함수와 람다

### 개념
- **고차 함수**: 함수를 파라미터로 받거나 함수를 반환하는 함수
- **람다**: 익명 함수를 간결하게 표현하는 방법

### 예시 1: 함수를 파라미터로 전달

```kotlin
fun calculate(
    startDate: LocalDateTime,
    additionalTime: Int,
    type: ServiceType,
    sourceRate: RateDTO,
    targetRate: RateDTO,
    commission: Double,
    converter: (Double, RateDTO, RateDTO) -> Double,  // 함수 타입 파라미터
    isWholesale: Boolean,
    baseRate: RateDTO
): Double {
    val price = if (isWholesale) wholesalePrice else retailPrice

    // 전달받은 함수 호출
    return converter(price ?: 0.0, sourceRate, targetRate)
}
```

**함수 타입 문법:**
```kotlin
(파라미터 타입들) -> 반환 타입

// 예시
(Int, Int) -> Int               // 두 Int를 받아 Int 반환
(String) -> Unit                // String을 받아 아무것도 반환하지 않음
() -> String                    // 파라미터 없이 String 반환
(User, (String) -> Unit) -> Int // 복잡한 예시
```

### 예시 2: 람다 표현식

```kotlin
@ExceptionHandler(WebExchangeBindException::class)
fun handleBindException(ex: WebExchangeBindException) {
    val errorMessages = ex.bindingResult.allErrors.joinToString(", ") { error ->
        error.defaultMessage ?: ""  // 람다: error를 받아 String 반환
    }
    throw ParamException("PARAM", errorMessages)
}
```

**람다 문법:**
```kotlin
// 기본 형태
{ 파라미터 -> 본문 }

// 예시
val sum = { a: Int, b: Int -> a + b }
println(sum(3, 5))  // 8

// 파라미터가 하나면 'it' 사용 가능
val double = { it: Int -> it * 2 }
val double2 = { it * 2 }  // 타입 추론 가능하면 생략

// 파라미터가 없으면
val hello = { println("Hello") }
```

### 실전 예시: 함수 참조

```kotlin
// 일반 함수
fun isEven(num: Int): Boolean = num % 2 == 0

// 방법 1: 람다로 전달
val evenNumbers = numbers.filter { it % 2 == 0 }

// 방법 2: 함수 참조로 전달 (::)
val evenNumbers = numbers.filter(::isEven)

// 멤버 함수 참조
val names = users.map(User::name)  // users.map { it.name }
```

### 실무에서 자주 보는 패턴

```kotlin
// 1. 컬렉션 처리
list.filter { it.age > 18 }
    .map { it.name }
    .forEach { println(it) }

// 2. DSL 스타일 (Domain Specific Language)
query.where {
    and { productMeta.quantity eq 5 }
    or { productMeta.isPremium eq true }
}

// 3. 콜백 함수
button.setOnClickListener { view ->
    println("Button clicked!")
}
```

---

## 6. 컬렉션 처리 함수

### 개념
Kotlin은 컬렉션(리스트, 맵, 셋 등)을 처리하는 강력한 함수들을 제공합니다. 함수형 프로그래밍 스타일로 데이터를 변환, 필터링, 집계할 수 있습니다.

### 6.1 map - 변환

**의미**: 각 요소를 다른 형태로 변환

```kotlin
val list = mutableListOf<ExtraService>()
extras?.let {
    list.addAll(
        it.filter { ext -> ext.isActive != false }
            .map {  // 각 요소를 ExtraService로 변환
                ExtraService(
                    type = it.type,
                    price = it.price
                )
            }
    )
}
```

**실전 예시:**
```kotlin
val numbers = listOf(1, 2, 3, 4, 5)
val doubled = numbers.map { it * 2 }  // [2, 4, 6, 8, 10]

val users = listOf(User("1hyung", 30), User("new", 25))
val names = users.map { it.name }  // ["1hyung", "new"]
val ages = users.map { it.age }    // [30, 25]
```

### 6.2 filter - 필터링

**의미**: 조건을 만족하는 요소만 선택

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)
val evenNumbers = numbers.filter { it % 2 == 0 }  // [2, 4]

val users = getUsers()
val adults = users.filter { it.age >= 18 }
val activeUsers = users.filter { it.isActive }
```

### 6.3 groupBy - 그룹화

**의미**: 특정 기준으로 요소들을 그룹으로 묶음

```kotlin
return database.runQuery(query).groupBy { it.country }.map { (key, value) ->
    val codeList = value.map { it.code }.distinct()

    CountryGroup(
        countryCode = key ?: "",
        codeList = codeList.filterNotNull()
    )
}
```

**실전 예시:**
```kotlin
val users = listOf(
    User("1hyung", 30, "USA"),
    User("new", 25, "UK"),
    User("final", 30, "USA")
)

// 나이별 그룹화
val byAge = users.groupBy { it.age }
// {30=[User(1hyung, 30, USA), User(final, 30, USA)], 25=[User(new, 25, UK)]}

// 국가별 그룹화
val byCountry = users.groupBy { it.country }
// {USA=[User(1hyung, 30, USA), User(final, 30, USA)], UK=[User(new, 25, UK)]}
```

### 6.4 associateBy - 맵으로 변환

**의미**: 리스트를 맵으로 변환 (키-값 쌍)

```kotlin
CURRENCY_MAP = currencyRepository.getCurrencyList()
    .associateBy { it.code }  // code를 키로 사용
    .toMutableMap()
```

**실전 예시:**
```kotlin
val users = listOf(
    User(id = 1, name = "1hyung"),
    User(id = 2, name = "new")
)

// id를 키로 하는 맵 생성
val userMap = users.associateBy { it.id }
// {1=User(1, 1hyung), 2=User(2, new)}

// 커스텀 키-값
val nameAgeMap = users.associate { it.name to it.age }
// {1hyung=30, new=25}
```

### 6.5 flatMap - 평탄화와 변환

**의미**: 각 요소를 컬렉션으로 변환한 후 하나의 리스트로 합침

```kotlin
val users = listOf(
    User("1hyung", listOf("A", "B")),
    User("new", listOf("C", "D"))
)

val allGrades = users.flatMap { it.grades }
// ["A", "B", "C", "D"]
```

### 6.6 distinctBy - 중복 제거

**의미**: 특정 기준으로 중복 제거

```kotlin
database.runQuery(query)
    .map { rec -> ProductDTO().apply { ... } }
    .distinctBy { it.productId }  // productId 기준으로 중복 제거
```

### 6.7 체이닝 - 여러 함수 조합

```kotlin
val users = getUsers()

val result = users
    .filter { it.age >= 18 }           // 성인만
    .filter { it.isActive }            // 활성 사용자만
    .map { it.name.toUpperCase() }     // 이름을 대문자로
    .sorted()                          // 정렬
    .take(10)                          // 상위 10개
```

### 6.8 자주 사용하는 컬렉션 함수 요약

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// 변환
numbers.map { it * 2 }                    // [2, 4, 6, 8, 10]
numbers.mapNotNull { if (it > 2) it else null }  // [3, 4, 5]

// 필터링
numbers.filter { it > 2 }                 // [3, 4, 5]
numbers.filterNot { it > 2 }              // [1, 2]

// 검색
numbers.find { it > 3 }                   // 4 (첫 번째)
numbers.firstOrNull { it > 10 }           // null

// 집계
numbers.sum()                             // 15
numbers.average()                         // 3.0
numbers.maxOrNull()                       // 5
numbers.count { it > 2 }                  // 3

// 그룹화
numbers.groupBy { it % 2 == 0 }           // {false=[1,3,5], true=[2,4]}

// 정렬
numbers.sorted()                          // [1, 2, 3, 4, 5]
numbers.sortedDescending()                // [5, 4, 3, 2, 1]
users.sortedBy { it.age }                 // 나이순 정렬

// 기타
numbers.take(3)                           // [1, 2, 3]
numbers.drop(2)                           // [3, 4, 5]
numbers.distinct()                        // 중복 제거
```

---

## 7. 코루틴 (Coroutines)

### 개념
비동기 프로그래밍을 간단하게 만들어주는 Kotlin의 강력한 기능입니다. 스레드보다 가볍고, 콜백 지옥을 피할 수 있습니다.

### 7.1 suspend 함수

**의미**: 일시 중단 가능한 함수. 다른 suspend 함수나 코루틴 스코프에서만 호출 가능

```kotlin
suspend fun search(request: SearchRequest): List<ProductDTO> {
    logger.info("[Product] [Search] request: $request")

    val query = QueryDsl.from(productMeta)
        .innerJoin(categoryMeta, on { productMeta.categoryId eq categoryMeta.id })

    val list: MutableList<ProductDTO> = mutableListOf()
    list.addAll(database.runQuery(query).map { rec -> ... })
    return list
}
```

**핵심 포인트:**
- `suspend` 키워드를 붙이면 suspend 함수
- 내부에서 다른 suspend 함수 호출 가능
- 함수 실행을 일시 중단하고 나중에 재개 가능
- 스레드를 블로킹하지 않음

### 7.2 runBlocking - 코루틴 시작

**의미**: 코루틴을 시작하고 완료될 때까지 현재 스레드를 블로킹

```kotlin
fun validateToken(token: String?): Boolean = runBlocking {
    if (shouldCheck) {
        val count = reactiveRedisTemplate.opsForSet().size("session:${token}").awaitSingle()
        if (count == 0L) throw AuthException(HttpStatus.UNAUTHORIZED.toString())
    }

    return@runBlocking try {
        val jwt = verifier.verify(token)
        !jwt.expiresAt.before(Date())
    } catch (e: JWTVerificationException) {
        throw AuthException("Expired or invalid JWT token")
    }
}
```

**주의사항:**
- 주로 테스트나 메인 함수에서 사용
- 프로덕션 코드에서는 적게 사용하는 것이 좋음

### 7.3 Flow - 비동기 스트림

**의미**: 여러 값을 비동기적으로 반환하는 스트림

```kotlin
override fun getProducts(request: ProductRequest): Flow<ProductResponse> {
    return flow {
        try {
            cacheService.list(request, false).forEach {
                emit(it)  // 각 항목을 하나씩 방출
            }
        } catch (e: SearchException) {
            logger.error("Search Product Exception ==> {}", e.printStackTrace())
            ProductResponse()
        }
    }
}
```

**Flow vs List:**
```kotlin
// List: 모든 데이터를 한 번에 로드
fun getUsers(): List<User> {
    return database.query("SELECT * FROM users")  // 100만 건이면 메모리 부담
}

// Flow: 필요할 때마다 하나씩 처리
fun getUsers(): Flow<User> = flow {
    database.query("SELECT * FROM users").forEach {
        emit(it)  // 하나씩 방출
    }
}
```

### 7.4 실전 코루틴 패턴

```kotlin
// 1. Controller에서 suspend 함수 사용
@GetMapping("/users")
suspend fun getUsers(): List<User> {
    return userService.findAll()  // suspend 함수
}

// 2. 병렬 처리 (async/await)
suspend fun getUserDetails(userId: Long): UserDetails {
    val user = async { userRepository.findById(userId) }
    val orders = async { orderRepository.findByUserId(userId) }
    val reviews = async { reviewRepository.findByUserId(userId) }

    return UserDetails(
        user = user.await(),      // 결과 기다림
        orders = orders.await(),
        reviews = reviews.await()
    )
}

// 3. delay (비동기 대기)
suspend fun retry(times: Int, block: suspend () -> Unit) {
    repeat(times) {
        try {
            block()
            return
        } catch (e: Exception) {
            delay(1000)  // 1초 대기 (스레드 블로킹 안 함)
        }
    }
}
```

### 7.5 코루틴 스코프

```kotlin
// 1. GlobalScope (권장하지 않음)
GlobalScope.launch {
    // 앱 종료 시까지 실행
}

// 2. CoroutineScope (권장)
class MyService(private val scope: CoroutineScope) {
    fun doSomething() {
        scope.launch {
            // 작업
        }
    }
}

// 3. Spring에서
@Service
class UserService {
    @EventListener
    fun onStart(event: ApplicationStartedEvent) {
        runBlocking {
            delay(2000)
            loadInitialData()
        }
    }
}
```

---

## 8. When 표현식

### 개념
Java의 `switch`를 더 강력하게 만든 것. 다양한 조건을 간결하게 표현할 수 있습니다.

### 8.1 열거형과 함께 사용

```kotlin
fun ProductRequestV2.toSearchRequest(): ProductSearchV2 {
    val serviceType = when (this.serviceType) {
        ProductRequest.ServiceType.STANDARD -> SearchServiceType.STANDARD
        ProductRequest.ServiceType.EXPRESS -> SearchServiceType.EXPRESS
        ProductRequest.ServiceType.PREMIUM -> SearchServiceType.PREMIUM
        ProductRequest.ServiceType.ECONOMY -> SearchServiceType.ECONOMY
        ProductRequest.ServiceType.BULK -> SearchServiceType.BULK
        ProductRequest.ServiceType.SUBSCRIPTION -> SearchServiceType.SUBSCRIPTION
        ProductRequest.ServiceType.CUSTOM -> SearchServiceType.CUSTOM
        else -> null
    }
    // ...
}
```

### 8.2 값 비교

```kotlin
val score = 85

val grade = when (score) {
    100 -> "Perfect!"
    in 90..99 -> "A"      // 범위 체크
    in 80..89 -> "B"
    in 70..79 -> "C"
    else -> "F"
}

println(grade)  // "B"
```

### 8.3 타입 검사

```kotlin
fun describe(obj: Any): String = when (obj) {
    is String -> "String of length ${obj.length}"
    is Int -> "Integer: $obj"
    is List<*> -> "List of size ${obj.size}"
    else -> "Unknown type"
}
```

### 8.4 조건 표현식

```kotlin
val age = 25

val category = when {
    age < 13 -> "Child"
    age < 20 -> "Teenager"
    age < 65 -> "Adult"
    else -> "Senior"
}
```

### 8.5 when without argument

```kotlin
val temperature = 28

val weather = when {
    temperature < 0 -> "Freezing"
    temperature < 15 -> "Cold"
    temperature < 25 -> "Mild"
    temperature < 35 -> "Hot"
    else -> "Very Hot"
}
```

### 8.6 실무 코드 패턴

```kotlin
when (search.searchType) {
    ProductSearchType.QUANTITY -> productMeta.quantity eq it.toInt()
    ProductSearchType.PRICE -> productMeta.price eq it.toInt()
    else -> productMeta.id.isNotNull()
}
```

### 실전 팁

```kotlin
// 1. 여러 값 동시에 체크
when (x) {
    1, 2, 3 -> println("Small")
    4, 5, 6 -> println("Medium")
    else -> println("Large")
}

// 2. 스마트 캐스트
fun process(obj: Any) = when (obj) {
    is String -> obj.toUpperCase()  // obj가 String으로 자동 캐스트
    is Int -> obj * 2               // obj가 Int로 자동 캐스트
    else -> obj
}

// 3. 결과를 변수에 저장
val result = when (status) {
    Status.SUCCESS -> processSuccess()
    Status.ERROR -> handleError()
    Status.PENDING -> waitForResponse()
}
```

---

## 9. Sealed Class

### 개념
`sealed class`는 제한된 클래스 계층을 표현합니다. `when` 표현식과 함께 사용할 때 모든 경우를 컴파일러가 강제로 확인합니다.

**일반 클래스와의 차이:**
```kotlin
// 일반 when - else 필요
fun handleStatus(status: String): String {
    return when (status) {
        "SUCCESS" -> "성공"
        "ERROR" -> "오류"
        else -> "???"  // else 필수 (다른 문자열이 올 수 있으니까)
    }
}

// sealed class - 모든 경우 컴파일러가 체크
sealed class OrderStatus {
    object Pending : OrderStatus()     // 대기중
    object Processing : OrderStatus() // 처리중
    data class Success(val orderId: Long) : OrderStatus()  // 성공
    data class Failed(val reason: String) : OrderStatus()  // 실패
}

fun handleStatus(status: OrderStatus): String {
    return when (status) {
        is OrderStatus.Pending -> "주문 대기중"
        is OrderStatus.Processing -> "처리중"
        is OrderStatus.Success -> "주문 완료: ${status.orderId}"  // orderId 접근 가능!
        is OrderStatus.Failed -> "주문 실패: ${status.reason}"    // reason 접근 가능!
        // else 불필요! 컴파일러가 모든 경우를 체크
    }
}
```

### 실전 예시: API 결과 처리

```kotlin
// API 호출 결과를 sealed class로 표현
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: String, val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// 사용
suspend fun fetchOrder(id: Long): ApiResult<OrderDTO> {
    return try {
        val order = orderRepository.findById(id)
            ?: return ApiResult.Error("NOT_FOUND", "주문을 찾을 수 없습니다")
        ApiResult.Success(order)
    } catch (e: Exception) {
        ApiResult.Error("SERVER_ERROR", e.message ?: "서버 오류")
    }
}

// 결과 처리
suspend fun processResult() {
    when (val result = fetchOrder(1L)) {
        is ApiResult.Success -> {
            println("성공: ${result.data}")
        }
        is ApiResult.Error -> {
            println("오류 [${result.code}]: ${result.message}")
        }
        ApiResult.Loading -> {
            println("로딩 중...")
        }
    }
}
```

### sealed class vs enum 비교

```kotlin
// enum: 각 케이스가 동일한 구조
enum class Status { PENDING, SUCCESS, FAILED }

// sealed class: 각 케이스가 다른 데이터를 가질 수 있음
sealed class Result {
    object Pending : Result()
    data class Success(val data: String) : Result()  // data 포함
    data class Failed(val code: Int, val msg: String) : Result()  // 여러 필드
}
```

| 특성 | enum | sealed class |
|------|------|--------------|
| 각 케이스 데이터 | 동일한 구조 | 다른 구조 가능 |
| 추가 메서드 | 가능 | 가능 |
| when 완전성 체크 | O | O |
| 상속 | 불가 | 가능 |

### 언제 sealed class를 쓸까?

```kotlin
// 1. 네트워크 상태
sealed class NetworkState {
    object Connected : NetworkState()
    object Disconnected : NetworkState()
    data class Error(val exception: Exception) : NetworkState()
}

// 2. UI 상태
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// 3. 이벤트 처리
sealed class OrderEvent {
    data class Created(val orderId: Long, val customerId: Long) : OrderEvent()
    data class Shipped(val orderId: Long, val trackingNumber: String) : OrderEvent()
    data class Cancelled(val orderId: Long, val reason: String) : OrderEvent()
}
```

---

## 10. Companion Object

### 개념
클래스의 인스턴스 없이 사용할 수 있는 멤버를 정의합니다. Java의 `static`과 유사하지만 더 강력합니다.

### 10.1 정적 변수 및 메서드

```kotlin
@Service
class SearchServiceImpl(...) : SearchService {
    companion object {
        var CURRENCY_MAP = mutableMapOf<String, CurrencyDTO>()  // 정적 변수
    }

    @EventListener
    fun eventStartUp(event: ApplicationStartedEvent) {
        runBlocking {
            try {
                delay(2000L)
                // companion object의 변수에 접근
                CURRENCY_MAP = currencyRepository.getCurrencyList()
                    .associateBy { it.code }
                    .toMutableMap()
            } catch (e: Exception) {
                logger.info("통화 정보 불러오기 실패")
            }
        }
    }
}
```

**사용 방법:**
```kotlin
// 클래스 이름으로 직접 접근
SearchServiceImpl.CURRENCY_MAP
```

### 10.2 팩토리 메서드

```kotlin
@Configuration
class RedisConfig {
    companion object {
        fun <T> template(
            factory: LettuceConnectionFactory,
            clz: Class<T>
        ): ReactiveRedisTemplate<String, T> {
            val stringRedisSerializer = StringRedisSerializer()
            val fastJsonRedisSerializer = FastJsonRedisSerializer<T>(clz).apply {
                this.fastJsonConfig = FastJsonConfig()
            }

            val context = RedisSerializationContext
                .newSerializationContext<String, T>(stringRedisSerializer)
                .key(stringRedisSerializer)
                .value(fastJsonRedisSerializer)
                .hashKey(stringRedisSerializer)
                .hashValue(fastJsonRedisSerializer)
                .build()

            return ReactiveRedisTemplate(factory, context)
        }
    }
}
```

**사용 방법:**
```kotlin
// 팩토리 메서드 호출
val template = RedisConfig.template(factory, ProductView::class.java)
```

### 10.3 실전 예시

```kotlin
class User(val name: String, val age: Int) {
    companion object {
        // 1. 상수
        const val MIN_AGE = 18
        const val MAX_AGE = 100

        // 2. 팩토리 메서드
        fun create(name: String, age: Int): User? {
            if (age < MIN_AGE || age > MAX_AGE) return null
            return User(name, age)
        }

        // 3. 정적 유틸리티
        fun isValidAge(age: Int): Boolean {
            return age in MIN_AGE..MAX_AGE
        }
    }
}

// 사용
println(User.MIN_AGE)  // 18
val user = User.create("1hyung", 25)
if (User.isValidAge(30)) { ... }
```

### 10.4 companion object vs object

```kotlin
// 1. companion object (클래스와 연관)
class MyClass {
    companion object {
        fun hello() = "Hello from companion"
    }
}
MyClass.hello()  // 클래스 이름으로 접근

// 2. object (싱글톤)
object MySingleton {
    fun hello() = "Hello from singleton"
}
MySingleton.hello()  // 객체 이름으로 접근

// 3. object 선언 (익명 객체)
val listener = object : ClickListener {
    override fun onClick() {
        println("Clicked!")
    }
}
```

---

## 11. 프로퍼티 위임 (Property Delegation)

### 개념
프로퍼티의 getter/setter 로직을 다른 객체에 위임합니다. `by` 키워드를 사용합니다.

### 11.1 by lazy - 지연 초기화

**의미**: 처음 사용될 때까지 초기화를 지연

```kotlin
private val reactiveRedisTemplate: ReactiveRedisTemplate<String, ProductView> by lazy {
    RedisConfig.template(factory, ProductView::class.java)
}
```

**동작 방식:**
1. 프로퍼티를 처음 접근할 때 람다 실행
2. 결과를 캐시
3. 이후 접근 시 캐시된 값 반환

**실전 예시:**
```kotlin
class DatabaseConnection {
    // 생성 비용이 큰 객체를 지연 초기화
    private val connection by lazy {
        println("Creating connection...")
        createDatabaseConnection()  // 실제 생성은 처음 사용할 때
    }

    fun query(sql: String) {
        connection.execute(sql)  // 여기서 처음 초기화됨
    }
}
```

### 11.2 언제 사용할까?

```kotlin
class ExpensiveResource {
    // 1. 무거운 객체 (항상 사용되지 않을 수 있음)
    private val heavyObject by lazy {
        // 로딩에 시간이 오래 걸림
        loadHeavyResource()
    }

    // 2. 다른 프로퍼티에 의존
    private val config by lazy {
        readConfigFile()
    }

    private val service by lazy {
        ServiceFactory.create(config)  // config가 먼저 필요
    }
}
```

### 11.3 lateinit vs lazy

```kotlin
class Example {
    // lateinit: var에만 사용 가능, 나중에 명시적으로 초기화
    private lateinit var userService: UserService

    fun init(service: UserService) {
        userService = service  // 명시적 초기화
    }

    // lazy: val에만 사용 가능, 자동으로 초기화
    private val configService by lazy {
        ConfigService()  // 처음 사용 시 자동 초기화
    }
}
```

### 11.4 커스텀 위임 (고급)

```kotlin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LoggingDelegate<T>(private var value: T) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        println("Getting ${property.name} = $value")
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        println("Setting ${property.name} = $value")
        this.value = value
    }
}

class User {
    var name: String by LoggingDelegate("Unknown")
}

// 사용
val user = User()
user.name = "1hyung"  // "Setting name = 1hyung" 출력
println(user.name)  // "Getting name = 1hyung" 출력
```

---

## 12. 제네릭 (Generics)

### 개념
타입을 파라미터로 받아 재사용 가능한 코드를 작성합니다. 타입 안정성을 유지하면서 코드 중복을 줄입니다.

### 12.1 제네릭 함수

```kotlin
companion object {
    fun <T> template(
        factory: LettuceConnectionFactory,
        clz: Class<T>  // T는 타입 파라미터
    ): ReactiveRedisTemplate<String, T> {
        val stringRedisSerializer = StringRedisSerializer()
        val fastJsonRedisSerializer = FastJsonRedisSerializer<T>(clz).apply {
            this.fastJsonConfig = FastJsonConfig()
        }

        return ReactiveRedisTemplate(factory, context)
    }
}
```

**사용 방법:**
```kotlin
// String 타입으로 사용
val stringTemplate = RedisConfig.template(factory, String::class.java)

// ProductView 타입으로 사용
val productTemplate = RedisConfig.template(factory, ProductView::class.java)
```

### 12.2 Reified Type Parameter - 타입 정보 보존

**문제**: 일반 제네릭은 런타임에 타입 정보가 지워집니다 (Type Erasure)

```kotlin
// 일반 제네릭 (작동 안 함)
fun <T> create(): T {
    return T()  // 컴파일 에러! T가 뭔지 모름
}
```

**해결**: `inline` + `reified`로 타입 정보 보존

```kotlin
inline fun <reified T : Any> redisTemplate(
    connection: LettuceConnectionFactory
): RedisTemplate<String, T> {
    val template = RedisTemplate<String, T>()
    template.connectionFactory = connection
    template.keySerializer = StringRedisSerializer()
    template.valueSerializer = FastJsonRedisSerializer(T::class.java)  // T의 클래스 정보 사용 가능!
    template.afterPropertiesSet()
    return template
}
```

**사용 방법:**
```kotlin
// 타입을 명시하면 T::class.java가 자동으로 설정됨
val userTemplate = redisTemplate<User>(connection)
val productTemplate = redisTemplate<Product>(connection)
```

### 12.3 제네릭 클래스

```kotlin
// 간단한 박스 클래스
class Box<T>(val item: T) {
    fun getItem(): T = item
}

// 사용
val intBox = Box(123)
val stringBox = Box("Hello")
val userBox = Box(User("1hyung", 30))

println(intBox.getItem())     // 123
println(stringBox.getItem())  // "Hello"
```

### 12.4 타입 제약

```kotlin
// T는 Number의 하위 타입이어야 함
fun <T : Number> sum(a: T, b: T): Double {
    return a.toDouble() + b.toDouble()
}

sum(1, 2)       // OK
sum(1.5, 2.3)   // OK
sum("a", "b")   // 컴파일 에러!
```

### 12.5 변성 (Variance)

```kotlin
// 1. 공변 (out) - 생산자
interface Producer<out T> {
    fun produce(): T  // T를 반환만 함
}

// 2. 반공변 (in) - 소비자
interface Consumer<in T> {
    fun consume(item: T)  // T를 받기만 함
}

// 3. 무공변 (기본) - 생산자이자 소비자
interface Box<T> {
    fun get(): T
    fun set(item: T)
}
```

**실전 예시:**
```kotlin
// List<T>는 out T (읽기 전용, 공변)
val strings: List<String> = listOf("a", "b")
val objects: List<Any> = strings  // OK! String은 Any의 하위 타입

// MutableList<T>는 무공변
val mutableStrings: MutableList<String> = mutableListOf("a", "b")
val mutableObjects: MutableList<Any> = mutableStrings  // 컴파일 에러!
```

---

## 13. Enum 클래스

### 개념
고정된 상수 집합을 타입 안전하게 표현합니다. 상태, 타입, 옵션 등을 정의할 때 사용합니다.

### 13.1 기본 Enum

```kotlin
enum class ProductCategory(val type: String) {
    SINGLE("SINGLE"),
    MULTI("MULTI")
}
```

**사용 방법:**
```kotlin
val category = ProductCategory.SINGLE
println(category.type)  // "SINGLE"

// 문자열에서 Enum으로 변환
val typeFromString = ProductCategory.valueOf("MULTI")

// 모든 값 순회
ProductCategory.values().forEach {
    println(it.type)
}
```

### 13.2 복합 Enum (여러 프로퍼티)

```kotlin
enum class OrderStatus(val status: String) {
    PENDING("PENDING"),      // 대기중
    PROCESSING("PROCESSING"), // 처리중
    SHIPPED("SHIPPED"),      // 배송중
    DELIVERED("DELIVERED"),  // 배송완료
    CANCELLED("CANCELLED"),  // 취소됨
}
```

### 13.3 Enum with 메서드

```kotlin
enum class PaymentMethod(val displayName: String, val fee: Double) {
    CREDIT_CARD("신용카드", 0.03),
    DEBIT_CARD("체크카드", 0.02),
    BANK_TRANSFER("계좌이체", 0.01),
    CASH("현금", 0.0);

    // 메서드 추가 가능
    fun calculateFee(amount: Double): Double {
        return amount * fee
    }

    // 인터페이스 구현 가능
    companion object {
        fun fromString(value: String): PaymentMethod? {
            return values().find { it.name == value }
        }
    }
}
```

**사용 방법:**
```kotlin
val method = PaymentMethod.CREDIT_CARD
println(method.displayName)  // "신용카드"
println(method.calculateFee(10000.0))  // 300.0
```

### 13.4 Enum과 when 표현식

```kotlin
fun processPayment(method: PaymentMethod, amount: Double): String {
    return when (method) {
        PaymentMethod.CREDIT_CARD -> "신용카드 결제 처리: $amount"
        PaymentMethod.DEBIT_CARD -> "체크카드 결제 처리: $amount"
        PaymentMethod.BANK_TRANSFER -> "계좌이체 처리: $amount"
        PaymentMethod.CASH -> "현금 결제 처리: $amount"
    }
}
```

### 13.5 실전 패턴

```kotlin
// 1. HTTP 상태 코드
enum class HttpStatus(val code: Int, val message: String) {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error");

    fun isSuccess(): Boolean = code in 200..299
    fun isError(): Boolean = code >= 400
}

// 2. 권한 레벨
enum class Role(val level: Int) {
    GUEST(0),
    USER(1),
    ADMIN(2),
    SUPER_ADMIN(3);

    fun hasPermission(required: Role): Boolean {
        return this.level >= required.level
    }
}

// 사용
val currentUser = Role.ADMIN
if (currentUser.hasPermission(Role.USER)) {
    println("접근 허용")
}
```

---

## 14. Interface (인터페이스)

### 개념
클래스가 반드시 구현해야 할 동작을 정의합니다. 구현 세부사항은 없고 "무엇을 해야 하는가"만 정의합니다.

### 기본 사용법

```kotlin
// 인터페이스 정의
interface Printable {
    fun print()                           // 추상 메서드 (구현 필수)
    fun preview(): String = "미리보기..."  // 기본 구현 (오버라이드 선택)
}

// 구현
class Document(val title: String) : Printable {
    override fun print() {
        println("문서 출력: $title")
    }
}

class Image(val filename: String) : Printable {
    override fun print() {
        println("이미지 출력: $filename")
    }

    override fun preview(): String = "이미지 미리보기: $filename"  // 오버라이드
}

// 사용
val items: List<Printable> = listOf(Document("보고서"), Image("photo.jpg"))
items.forEach { it.print() }
```

### 다중 인터페이스 구현

```kotlin
interface Saveable {
    suspend fun save(): Boolean
}

interface Deletable {
    suspend fun delete(id: Long): Boolean
}

// 여러 인터페이스 동시 구현
interface UserRepository : Saveable, Deletable {
    suspend fun findById(id: Long): UserDTO?
    suspend fun findAll(): List<UserDTO>
}

// 구현체
@Repository
class UserRepositoryImpl(
    private val db: R2dbcDatabase
) : UserRepository {
    override suspend fun save(): Boolean { TODO() }
    override suspend fun delete(id: Long): Boolean { TODO() }
    override suspend fun findById(id: Long): UserDTO? { TODO() }
    override suspend fun findAll(): List<UserDTO> { TODO() }
}
```

### Spring에서의 Interface 패턴

```kotlin
// Service 인터페이스
interface UserService {
    suspend fun createUser(request: CreateUserRequest): UserDTO
    suspend fun findById(id: Long): UserDTO
    suspend fun updateUser(id: Long, request: UpdateUserRequest): UserDTO
    suspend fun deleteUser(id: Long)
    suspend fun findAll(pageable: Pageable): Page<UserDTO>
}

// 구현체 (비즈니스 로직)
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) : UserService {

    override suspend fun createUser(request: CreateUserRequest): UserDTO {
        // 중복 이메일 체크
        userRepository.findByEmail(request.email)?.let {
            throw ValidationException("이미 사용 중인 이메일입니다: ${request.email}")
        }

        val user = UserDTO(
            name = request.name,
            email = request.email
        )
        val saved = userRepository.save(user)

        // 환영 이메일 전송
        emailService.sendWelcome(saved.email)

        return saved
    }

    override suspend fun findById(id: Long): UserDTO {
        return userRepository.findById(id)
            ?: throw NotFoundException("사용자를 찾을 수 없습니다: $id")
    }

    // ... 나머지 구현
}
```

### 인터페이스 vs 추상 클래스

```kotlin
// 인터페이스: 행동 계약 (상태 없음, 다중 구현 가능)
interface Flyable {
    fun fly(): String
}

interface Swimmable {
    fun swim(): String
}

// 추상 클래스: 공통 구현 제공 (상태 있음, 단일 상속)
abstract class Bird(val name: String) {
    abstract fun makeSound(): String  // 구현 강제

    fun breathe(): String = "$name 숨쉬기"  // 공통 구현 제공
}

// 추상 클래스 상속 + 인터페이스 구현 동시에 가능
class Duck(name: String) : Bird(name), Flyable, Swimmable {
    override fun makeSound(): String = "꽥꽥"
    override fun fly(): String = "$name 날기"
    override fun swim(): String = "$name 수영"
}
```

| 특성 | Interface | Abstract Class |
|------|-----------|---------------|
| 상태(필드) | 불가 | 가능 |
| 다중 구현 | 가능 | 불가 (단일 상속) |
| 생성자 | 없음 | 있음 |
| 기본 구현 | 가능 | 가능 |
| 용도 | 행동 계약 | 공통 구현 공유 |

> **Spring에서는 대부분 Interface를 사용합니다.** 상태를 가지지 않고, 행동(메서드)만 정의하기 때문입니다.

---

## 부록: 아키텍처 패턴

### A. Controller → Service → Repository 패턴

```kotlin
// 1. Controller (API 엔드포인트)
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {
    @GetMapping("/search")
    suspend fun search(request: SearchRequest, locale: Locale?) =
        productService.search(request.apply {
            this.locale = locale?.toString() ?: Locale.US.toString()
        })
}

// 2. Service (비즈니스 로직)
@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository
) : ProductService {
    override suspend fun search(request: SearchRequest): List<ProductDTO> {
        return productRepository.search(request)
    }
}

// 3. Repository (데이터 접근)
@Repository
class ProductRepository(
    private val database: R2dbcDatabase
) {
    suspend fun search(request: SearchRequest): List<ProductDTO> {
        val query = QueryDsl.from(productMeta)
            .where { /* 조건 */ }
            .select(/* 컬럼 */)

        return database.runQuery(query).map { /* 변환 */ }
    }
}
```

### B. DTO 변환 패턴

```kotlin
// Entity → DTO 변환 (확장 함수)
fun ProductEntity.toDTO(): ProductDTO {
    return ProductDTO(
        productId = this.productId,
        brand = this.brand,
        model = this.model,
        quantity = this.quantity
    )
}

// 사용
val entities = repository.findAll()
val dtos = entities.map { it.toDTO() }
```

### C. 의존성 주입 패턴

```kotlin
// 생성자 주입 (권장)
@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val cacheManager: CacheManager
) {
    fun createUser(request: CreateUserRequest) {
        // 모든 의존성 사용 가능
    }
}
```

---

## 학습 팁

### 1. 우선순위가 높은 문법
1. **Null Safety** - 매 줄마다 사용
2. **Data Class** - DTO/Entity 작성
3. **스코프 함수 (apply, let)** - 코드 간결화
4. **컬렉션 함수 (map, filter)** - 데이터 처리
5. **코루틴 기본 (suspend)** - 비동기 처리

### 2. 연습 방법
1. 기존 코드를 읽으면서 패턴 파악
2. 작은 기능부터 직접 작성
3. 코드 리뷰에서 피드백 받기
4. IntelliJ IDEA의 "Convert to Kotlin" 기능 활용

### 3. 참고 자료
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [코틀린 코루틴 가이드](https://kotlinlang.org/docs/coroutines-guide.html)

---

## 실습 코드

이 문서의 개념을 직접 실행하며 학습할 수 있습니다:

| 주제 | 실습 코드 |
|------|----------|
| 클래스, data class, enum, object | [`src/study/03_class/`](../../src/study/03_class/) |
| 제네릭 | [`src/study/06_advanced/Generics.kt`](../../src/study/06_advanced/Generics.kt) |
| sealed class | [`src/study/06_advanced/SealedClass.kt`](../../src/study/06_advanced/SealedClass.kt) |
| 예외처리 | [`src/study/06_advanced/ExceptionHandling.kt`](../../src/study/06_advanced/ExceptionHandling.kt) |

---

## 마무리

이 문서는 Kotlin 문법을 정리한 것입니다. 각 문법을 완벽하게 이해하려고 하기보다는, 실제 코드를 작성하면서 점진적으로 익혀나가는 것이 좋습니다.

**핵심은 "읽고 → 이해하고 → 작성하기"의 반복입니다!**

화이팅!
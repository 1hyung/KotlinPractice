# Spring Boot 프로젝트 구조 이해하기

Spring Boot 프로젝트를 단계별로 읽고 이해하는 학습 가이드입니다.

**선행 학습**:
1. 01_KOTLIN_BASICS.md 완료
2. 02_SPRING_BASICS.md 완료

---

## 학습 전략: "작은 기능 → 큰 기능" 순서로 읽기

복잡한 프로젝트를 읽을 때는 **전체를 한 번에 이해하려고 하지 말고**, 작은 기능 하나를 완전히 이해한 후 점진적으로 확장하세요.

---

## Week 1: 프로젝트 구조 파악

### Day 1: 프로젝트 전체 구조 훑어보기

#### 1. 일반적인 Spring Boot 디렉토리 구조

```
com.example.server/
├── order/        # 주문 도메인
├── product/      # 상품 관리
├── common/       # 공통 모듈 (보안, 설정 등)
├── member/       # 회원 관리
└── ui/           # REST API 컨트롤러
```

**학습 팁**:
- 처음에는 하나의 도메인 패키지만 집중하세요
- 다른 패키지는 나중에!

---

#### 2. Application 진입점

```kotlin
@SpringBootApplication
@EnableScheduling
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
```

**체크리스트**:
- [ ] `@SpringBootApplication`이 무엇을 하는지 아시나요? (Spring Boot 기초 참고)
- [ ] `@EnableScheduling`은 무엇인가요? (스케줄러 활성화)
- [ ] `runApplication`이 무엇을 하나요? (앱 실행)

---

### Day 2: API 흐름 이해하기 - Controller → Service → Repository

#### Step 1: Controller (시작점)

```kotlin
@RestController
@RequestMapping("/api/items")
class ItemController(
    private val itemService: ItemService  // ← DI 주입
) {

    @GetMapping("/list")
    suspend fun list(
        @CurrentUser user: CurrentUserData,
        request: SearchRequest
    ): ApiListResponse<ItemDTO> {
        val list = itemService.list(request)
        val count = itemService.count(request)

        return ApiListResponse(
            list = list,
            count = count,
            mode = true
        )
    }
}
```

**코드 읽기 연습**:
1. 이 Controller는 어떤 URL을 처리하나요?
   - 답: `GET /api/items/list`

2. `@CurrentUser`는 무엇인가요?
   - JWT 토큰에서 현재 로그인한 사용자 정보를 추출합니다

3. `suspend fun`이 왜 사용되었나요?
   - Reactive (Non-blocking) 처리를 위해

4. `itemService`는 어디서 왔나요?
   - 생성자에서 DI로 주입받음

---

#### Step 2: Service (비즈니스 로직)

```kotlin
// 인터페이스
interface ItemService {
    suspend fun list(request: SearchRequest): List<ItemDTO>
    suspend fun count(request: SearchRequest): Long
    suspend fun save(item: ItemDTO): ItemDTO
    suspend fun findById(id: String): ItemDTO
}

// 구현체
@Service
class ItemServiceImpl(
    private val itemRepository: ItemRepository,
    private val kafkaService: KafkaService
) : ItemService {

    override suspend fun list(request: SearchRequest): List<ItemDTO> {
        return itemRepository.findList(request)
    }

    override suspend fun count(request: SearchRequest): Long {
        return itemRepository.count(request)
    }
}
```

**코드 읽기 연습**:
1. 왜 Interface와 Implementation을 분리했나요?
   - 테스트 시 Mock 객체로 교체 가능
   - 구현체를 쉽게 변경 가능

2. `itemRepository`는 무엇인가요?
   - 데이터베이스에 접근하는 Repository

---

#### Step 3: Repository (데이터 접근)

```kotlin
@Repository
class ItemRepository(
    private val db: R2dbcEntityDatabase
) {
    suspend fun findList(request: SearchRequest): List<ItemDTO> {
        return db.runQuery {
            QueryDsl.from(itemMeta)
                .where {
                    itemMeta.categoryId eq request.categoryId
                }
                .orderBy(itemMeta.createdAt.desc())
        }
    }

    suspend fun count(request: SearchRequest): Long {
        return db.runQuery {
            QueryDsl.from(itemMeta)
                .where { itemMeta.categoryId eq request.categoryId }
                .selectCount()
        }
    }
}
```

**코드 읽기 연습**:
1. `QueryDsl`은 무엇인가요?
   - Komapper의 쿼리 빌더 (SQL을 Kotlin 코드로 작성)

2. 이 코드는 어떤 SQL과 동일한가요?
   ```sql
   SELECT * FROM items
   WHERE category_id = ?
   ORDER BY created_at DESC
   ```

---

#### Step 4: DTO (데이터 구조)

```kotlin
data class ItemDTO(
    var id: String?,              // 아이템 ID (PK)
    var name: String,             // 이름
    var description: String?,     // 설명
    var categoryId: Long,         // 카테고리 ID
    var price: Int,               // 가격
    var status: ItemStatus,       // 상태
    var createdAt: LocalDateTime?,
    var updatedAt: LocalDateTime?
)
```

**코드 읽기 연습**:
1. `data class`는 무엇인가요? (Kotlin 기초 참고)
2. nullable 필드(`?`)는 어떤 것들인가요?
3. 왜 일부는 nullable이고 일부는 non-null인가요?
   - 필수 입력 vs 선택 입력

---

### Day 3: 데이터 흐름 따라가기

API 호출이 어떻게 흐르는지 따라가봅시다.

```
Client 요청: GET /api/items/list?categoryId=1
    ↓
Controller.list()
    ↓ service.list() 호출
ServiceImpl.list()
    ↓ repository.findList() 호출
Repository.findList()
    ↓ SQL 실행
Database
    ↓ 결과 반환
List<ItemDTO>
    ↓ Service로 반환
Service (비즈니스 로직 처리)
    ↓ Controller로 반환
Controller (ApiListResponse로 감싸기)
    ↓ JSON 응답
Client 응답: {"list": [...], "count": 10, "mode": true}
```

**실습: 디버거로 따라가기**
1. Controller에 브레이크포인트 설정
2. Postman으로 API 호출
3. Step Into로 코드 한 줄씩 따라가기

---

## Week 2: 고급 기능 이해하기

### Kafka 메시징 패턴

```kotlin
@Service
class ItemKafkaService(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    suspend fun sendEvent(item: ItemDTO) {
        val json = JSON.toJSONString(item)

        kafkaTemplate
            .send("item-events", item.id, json)
            .await()
    }
}
```

**Kafka Listener 패턴**:
```kotlin
@Service
class ItemEventListener(
    private val itemService: ItemService
) {
    @KafkaListener(
        topics = ["item-response"],
        groupId = "item-service"
    )
    fun listen(data: ItemEventData) {
        println("Kafka 메시지 수신: ${data.id}")

        runBlocking {
            itemService.updateStatus(data.id, data.status)
        }
    }
}
```

---

### Redis 캐싱 패턴

```kotlin
@Service
class ItemRedisService(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    suspend fun get(key: String): List<ItemDTO>? {
        val json = redisTemplate
            .opsForValue()
            .get(key)
            .awaitFirstOrNull()

        return json?.let { JSON.parseArray(it, ItemDTO::class.java) }
    }

    suspend fun set(key: String, value: List<ItemDTO>, ttl: Long) {
        val json = JSON.toJSONString(value)

        redisTemplate
            .opsForValue()
            .set(key, json, Duration.ofSeconds(ttl))
            .await()
    }
}
```

---

### 스케줄러 패턴

```kotlin
@Component
@Profile("!local")  // local 환경에서는 비활성화
class ItemScheduler(
    private val itemService: ItemService
) {
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    fun expireOldItems() = runBlocking {
        val expired = itemService.findExpiredItems()

        expired.forEach { item ->
            itemService.updateStatus(item.id, ItemStatus.EXPIRED)
        }
    }
}
```

---

## Week 3: 인증/보안 이해하기

### JWT 토큰 처리

```kotlin
@Component
class JwtTokenProvider {
    fun validateToken(token: String?): Boolean = runBlocking {
        return@runBlocking try {
            val jwt = verifier.verify(token)
            !jwt.expiresAt.before(Date())
        } catch (e: JWTVerificationException) {
            throw AuthException("Expired or invalid JWT token")
        }
    }
}
```

### @CurrentUser 어노테이션

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser

@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java)
    }

    override suspend fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Any {
        // JWT에서 사용자 정보 추출
        val principal = exchange.getPrincipal<JwtAuthenticationToken>().awaitSingle()
        return CurrentUserData(
            userId = principal.token.subject.toLong(),
            email = principal.token.getClaimAsString("email")
        )
    }
}
```

---

---

## 로깅 패턴

### 로거 설정

```kotlin
import org.slf4j.LoggerFactory

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository
) : OrderService {

    // 1. companion object 방식 (클래스 밖에서도 사용 가능)
    companion object {
        private val logger = LoggerFactory.getLogger(OrderServiceImpl::class.java)
    }

    // 2. 또는 lazy 방식
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun createOrder(dto: OrderDTO): OrderDTO {
        logger.info("[Order] 주문 생성 시작: customerId=${dto.customerId}")

        val saved = orderRepository.save(dto)

        logger.info("[Order] 주문 생성 완료: orderId=${saved.id}")
        return saved
    }
}
```

### 로그 레벨 사용법

```kotlin
class SomeService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun process(data: DataDTO) {
        logger.debug("상세 디버그 정보: $data")       // 개발 시 디버깅용
        logger.info("주요 흐름 기록: id=${data.id}")   // 일반적인 처리 흐름
        logger.warn("주의가 필요한 상황 발생")         // 문제는 아니지만 주의 필요
        logger.error("오류 발생!", exception)         // 에러 (서버 알림 대상)
    }
}
```

| 레벨 | 용도 | 운영 환경 출력 여부 |
|------|------|-------------------|
| DEBUG | 개발 중 상세 정보 | 보통 OFF |
| INFO | 주요 처리 흐름 | ON |
| WARN | 주의 상황 | ON |
| ERROR | 오류 발생 | ON (알림 발송) |

### 실전 로깅 패턴

```kotlin
@Service
class ItemServiceImpl(
    private val itemRepository: ItemRepository,
    private val kafkaService: KafkaService
) : ItemService {
    companion object {
        private val logger = LoggerFactory.getLogger(ItemServiceImpl::class.java)
    }

    override suspend fun save(item: ItemDTO): ItemDTO {
        logger.info("[Item][save] 시작 - name=${item.name}, category=${item.categoryId}")

        return try {
            val saved = itemRepository.save(item)
            logger.info("[Item][save] 완료 - id=${saved.id}")
            saved
        } catch (e: Exception) {
            logger.error("[Item][save] 실패 - item=$item", e)
            throw e
        }
    }

    override suspend fun list(request: SearchRequest): List<ItemDTO> {
        logger.debug("[Item][list] 검색 조건: $request")

        val result = itemRepository.findList(request)

        logger.info("[Item][list] 검색 완료 - 결과 수: ${result.size}")
        return result
    }
}
```

> **팁**: 로그에 `[도메인][기능]` 형식으로 태그를 붙이면 나중에 검색하기 쉽습니다.

---

## @Transactional 실전 패턴

### 여러 Repository가 연관된 경우

```kotlin
@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository,
    private val notificationService: NotificationService
) : OrderService {

    // 여러 Repository 작업을 하나의 트랜잭션으로 묶기
    @Transactional
    override suspend fun createOrder(dto: OrderDTO): OrderDTO {
        // 1. 재고 확인
        val stock = stockRepository.findByProductId(dto.productId)
            ?: throw NotFoundException("상품을 찾을 수 없습니다")

        if (stock.quantity < dto.quantity) {
            throw ValidationException("재고가 부족합니다 (현재: ${stock.quantity})")
        }

        // 2. 주문 저장
        val order = orderRepository.save(dto)

        // 3. 재고 감소
        stockRepository.save(stock.copy(quantity = stock.quantity - dto.quantity))

        // 4. 알림 전송 (트랜잭션 범위 밖에서 처리하는 것이 좋음)
        notificationService.sendOrderCreated(order)

        return order
        // 여기까지 오류 없으면 커밋, 어디서든 오류 나면 전부 롤백
    }
}
```

---

## 학습 체크리스트

### Week 1: 기본 구조
- [ ] Controller → Service → Repository 패턴 이해
- [ ] DTO 구조 파악
- [ ] 디버거로 API 흐름 추적

### Week 2: 고급 기능
- [ ] Kafka 메시징 개념 이해
- [ ] Redis 캐싱 패턴 이해
- [ ] 스케줄러 구현 방법

### Week 3: 보안
- [ ] JWT 인증 흐름 이해
- [ ] @CurrentUser 동작 원리

---

## 참고 자료

- **Kotlin 기초**: 01_KOTLIN_BASICS.md
- **Spring Boot 기초**: 02_SPRING_BASICS.md
- **Komapper 공식 문서**: https://www.komapper.org/
- **Spring WebFlux 가이드**: https://docs.spring.io/spring-framework/reference/web/webflux.html
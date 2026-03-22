# 09. Spring Boot + Kotlin 데이터베이스 완전 가이드

> Spring Data JPA, Spring Data R2DBC, Komapper, Flyway, AWS Aurora, Supabase, Redis를 다루는 실무 가이드입니다.
> MySQL, PostgreSQL 환경을 기준으로 처음부터 실무까지 설명합니다.

---

## 목차

- [Part 0: ORM/DB 클라이언트 선택 기준](#part-0-ormdb-클라이언트-선택-기준)
- [Part 1: 관계형 DB 기초 (MySQL & PostgreSQL)](#part-1-관계형-db-기초-mysql--postgresql)
- [Part 2: Spring Data JPA (Blocking 방식)](#part-2-spring-data-jpa-blocking-방식)
- [Part 3: Spring Data R2DBC + Komapper (Reactive 방식)](#part-3-spring-data-r2dbc--komapper-reactive-방식)
- [Part 4: DB 마이그레이션 (Flyway)](#part-4-db-마이그레이션-flyway)
- [Part 5: AWS Aurora](#part-5-aws-aurora)
- [Part 6: Supabase](#part-6-supabase)
- [Part 7: Redis](#part-7-redis)
- [학습 체크리스트](#학습-체크리스트)

---

## Part 0: ORM/DB 클라이언트 선택 기준

### 핵심 비교표

| 항목 | Spring Data JPA | Spring Data R2DBC | Komapper |
|------|----------------|-------------------|----------|
| 실행 모델 | Blocking (동기) | Non-blocking (비동기) | 둘 다 지원 |
| Spring WebFlux 궁합 | 보통 (별도 스레드풀 필요) | 최적 | 최적 |
| 학습 곡선 | 낮음 (자료 많음) | 보통 | 보통 |
| 연관 관계 | @OneToMany 등 자동 처리 | 없음 (직접 처리) | 없음 (직접 처리) |
| 코틀린 친화성 | 보통 (open class 필요) | 좋음 | 매우 좋음 |
| Lazy Loading | 지원 | 미지원 | 미지원 |
| 쿼리 복잡도 | JPQL / Criteria API | SQL 직접 작성 | DSL로 타입 안전 |
| 트랜잭션 | @Transactional (blocking) | @Transactional (reactive) | 둘 다 지원 |
| 생태계 성숙도 | 매우 높음 | 중간 | 낮음 (Kotlin 전용) |

---

### 언제 JPA를 쓰고, 언제 R2DBC / Komapper를 쓰는가?

#### JPA를 선택해야 하는 상황

```
1. Spring MVC (Servlet 기반) 프로젝트
   - 전통적인 REST API, 배치 서버, 어드민 서비스 등
   - 동기 처리로도 충분한 트래픽

2. 복잡한 도메인 모델을 다룰 때
   - 다단계 연관 관계 (Order → OrderItem → Product → Category)
   - Lazy Loading으로 필요할 때만 조회
   - 영속성 컨텍스트의 변경 감지(Dirty Checking) 활용

3. 팀 내 JPA 숙련도가 있을 때
   - 자료가 풍부하고, 트러블슈팅 경험이 축적되어 있음

4. 도메인 주도 설계(DDD)를 적용할 때
   - Aggregate, Repository 패턴과 자연스럽게 매핑됨
```

#### R2DBC / Komapper를 선택해야 하는 상황

```
1. Spring WebFlux 프로젝트
   - 전체 스택이 Non-blocking이어야 할 때
   - JPA를 WebFlux와 함께 쓰면 별도 스레드풀이 필요하고 이점이 반감됨

2. 높은 동시성이 요구될 때
   - 수천 개의 동시 연결을 적은 스레드로 처리해야 하는 서비스
   - 실시간 데이터 스트리밍, 알림 서버

3. 단순한 CRUD 위주의 서비스
   - 복잡한 연관 관계 없이 단일 테이블 또는 명시적 JOIN으로 처리 가능한 경우

4. Kotlin Coroutine을 적극 활용할 때
   - suspend 함수, Flow로 코드를 간결하게 유지하고 싶을 때
```

#### Komapper를 R2DBC 대신 선택하는 상황

```
- 타입 안전한 쿼리 DSL이 필요할 때 (SQL 문자열 실수 방지)
- Kotlin-first API가 필요할 때 (Java 스타일 API 지양)
- R2DBC와 JDBC 둘 다 지원하는 ORM이 필요할 때
```

#### 아키텍처 결정 흐름

```
Spring MVC 사용? ──Yes──> JPA 사용 권장
       │
      No
       │
Spring WebFlux 사용?
       │
      Yes
       │
복잡한 연관 관계 있음? ──Yes──> JPA + 전용 스레드풀 (boundedElastic) 고려
       │                          또는 연관 관계 재설계 후 R2DBC
      No
       │
타입 안전 DSL 원함? ──Yes──> Komapper
       │
      No
       │
     R2DBC
```

---

## Part 1: 관계형 DB 기초 (MySQL & PostgreSQL)

### Docker Compose로 로컬 환경 구성

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: local-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: myapp_db
      MYSQL_USER: myapp_user
      MYSQL_PASSWORD: myapp_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/mysql:/docker-entrypoint-initdb.d
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgresql:
    image: postgres:15
    container_name: local-postgres
    environment:
      POSTGRES_DB: myapp_db
      POSTGRES_USER: myapp_user
      POSTGRES_PASSWORD: myapp_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/postgres:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myapp_user -d myapp_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7.2
    container_name: local-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

volumes:
  mysql_data:
  postgres_data:
  redis_data:
```

```bash
# 실행
docker-compose up -d

# 상태 확인
docker-compose ps

# MySQL 접속
docker exec -it local-mysql mysql -u myapp_user -pmyapp_password myapp_db

# PostgreSQL 접속
docker exec -it local-postgres psql -U myapp_user -d myapp_db
```

---

### MySQL vs PostgreSQL 선택 기준

#### 언제 MySQL을 선택하는가

```
- 단순한 CRUD 위주의 서비스 (블로그, 커머스 기본 기능)
- 팀 내 MySQL 운영 경험이 많을 때
- AWS Aurora MySQL을 사용할 계획일 때
- 레거시 시스템과의 호환이 필요할 때
- 읽기 트래픽이 압도적으로 많은 서비스 (복제 최적화)
```

#### 언제 PostgreSQL을 선택하는가

```
- JSON 데이터를 DB에서 쿼리해야 할 때 (JSONB 인덱싱)
- 복잡한 쿼리, 분석 쿼리가 많을 때
- UUID를 기본 키로 사용할 때 (네이티브 UUID 타입)
- 배열, 범위(range) 타입 등 고급 데이터 타입 필요
- Supabase 사용 시 (PostgreSQL 기반)
- Full-text Search를 DB에서 처리할 때
- 지리 데이터(PostGIS) 필요 시
```

#### 데이터 타입 차이

| 타입 | MySQL | PostgreSQL |
|------|-------|-----------|
| JSON | JSON (텍스트 저장) | JSON / **JSONB** (이진 저장, 인덱싱 가능) |
| UUID | VARCHAR(36) 또는 BINARY(16) | uuid (네이티브 타입) |
| 배열 | 없음 (별도 테이블 필요) | INTEGER[], TEXT[] 등 네이티브 지원 |
| 불리언 | TINYINT(1) | BOOLEAN |
| 자동 증가 | AUTO_INCREMENT | SERIAL / GENERATED ALWAYS AS IDENTITY |
| 전문 검색 | FULLTEXT 인덱스 | tsvector / tsquery |
| 범위 타입 | 없음 | int4range, tstzrange 등 |
| 문자열 | VARCHAR(n) | VARCHAR(n) / TEXT (길이 제한 없음) |

#### 성능 특성 요약

| 특성 | MySQL (InnoDB) | PostgreSQL |
|------|---------------|-----------|
| 읽기 성능 | 단순 조회에 강함 | 복잡한 쿼리에 강함 |
| 쓰기 성능 | 높은 INSERT 처리량 | 동시 쓰기에 강함 (MVCC) |
| 복제 | 바이너리 로그 기반 | WAL(Write-Ahead Log) 기반 |
| 잠금 | 행 수준 잠금 | 행 수준 잠금 (더 세밀한 MVCC) |
| 플래너 | 통계 기반 | 고급 쿼리 플래너 (더 정교) |

---

## Part 2: Spring Data JPA (Blocking 방식)

### 환경 설정

#### build.gradle.kts

```kotlin
plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    kotlin("plugin.jpa") version "1.9.21"   // JPA Entity용 필수 플러그인
}

dependencies {
    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // MySQL 드라이버
    runtimeOnly("com.mysql:mysql-connector-j")

    // PostgreSQL 드라이버
    runtimeOnly("org.postgresql:postgresql")

    // Flyway (마이그레이션)
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql") // MySQL 사용 시 추가
}

// JPA Entity에 필요한 allOpen, noArg 설정
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
```

#### application.yml - MySQL 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/myapp_db?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
    username: myapp_user
    password: myapp_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: HikariPool-MySQL

  jpa:
    hibernate:
      ddl-auto: validate          # 운영: validate, 개발: create-drop 또는 update
    show-sql: true                # 개발 시에만 true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
        default_batch_fetch_size: 100   # N+1 완화
        jdbc:
          batch_size: 50          # 배치 INSERT 성능 향상
        order_inserts: true
        order_updates: true
```

#### application.yml - PostgreSQL 설정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myapp_db
    username: myapp_user
    password: myapp_password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_batch_fetch_size: 100
        jdbc:
          batch_size: 50
        # PostgreSQL의 경우 SEQUENCE 전략 권장
        id:
          new_generator_mappings: true
```

---

### Kotlin에서 JPA Entity 작성 시 주의사항

#### data class를 Entity로 쓰면 안 되는 이유

```kotlin
// 잘못된 예시 - data class 사용
@Entity
data class Order(  // 이렇게 하면 안 됨!
    @Id val id: Long,
    val status: String
)

// 문제점:
// 1. equals/hashCode가 id 기준으로 자동 생성되는데,
//    JPA 프록시 객체는 equals 비교 시 문제 발생
// 2. copy()로 필드를 변경하면 영속성 컨텍스트가 추적하지 못함
// 3. Lazy Loading 프록시가 final 클래스를 상속할 수 없음
```

#### open class가 필요한 이유

```kotlin
// JPA는 Lazy Loading을 위해 Entity의 서브클래스(프록시)를 런타임에 생성함
// Kotlin 클래스는 기본이 final이므로 프록시 생성 불가

// kotlin("plugin.jpa") 플러그인이 @Entity, @MappedSuperclass,
// @Embeddable에 자동으로 open을 붙여줌
// build.gradle.kts의 allOpen 설정도 동일한 역할
```

#### 올바른 Entity 예시 (주문 도메인)

```kotlin
// 공통 BaseEntity
@MappedSuperclass
abstract class BaseEntity {
    @CreationTimestamp
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now()
}

// Order Entity
@Entity
@Table(name = "orders")
class Order(
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseEntity() {

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val orderItems: MutableList<OrderItem> = mutableListOf()

    // 비즈니스 메서드
    fun addItem(item: OrderItem) {
        orderItems.add(item)
        item.order = this
        recalculateTotal()
    }

    fun cancel() {
        check(status == OrderStatus.PENDING) { "이미 처리된 주문은 취소할 수 없습니다." }
        status = OrderStatus.CANCELLED
    }

    private fun recalculateTotal() {
        totalAmount = orderItems.sumOf { it.price * it.quantity.toBigDecimal() }
    }
}

enum class OrderStatus {
    PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
}

@Entity
@Table(name = "order_items")
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val price: BigDecimal,

    @Column(nullable = false)
    val quantity: Int,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseEntity()

@Entity
@Table(name = "members")
class Member(
    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    @Column(nullable = false, length = 50)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseEntity()
```

#### var vs val 선택 기준

```kotlin
@Entity
class Product(
    // val: 변경되지 않아야 하는 필드 (PK, 생성 시점에 확정된 불변 데이터)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // var: 비즈니스 로직으로 변경될 수 있는 필드
    var name: String,
    var price: BigDecimal,
    var stockQuantity: Int,
)
// 규칙:
// - PK(id)는 val
// - 비즈니스상 변경 가능한 필드는 var
// - 연관 관계 컬렉션(OneToMany)은 val (참조 자체를 바꾸지 않음)
// - 단, MutableList 내부 요소는 추가/삭제 가능
```

---

### Repository 패턴

#### JpaRepository vs CrudRepository

```kotlin
// CrudRepository: 기본 CRUD (save, findById, delete, count 등)
// JpaRepository: CrudRepository 확장 + flush, saveAndFlush, deleteInBatch, findAll(Sort) 등
// 일반적으로 JpaRepository를 사용

interface OrderRepository : JpaRepository<Order, Long> {

    // 쿼리 메서드 - 메서드 이름으로 자동 쿼리 생성
    fun findByStatus(status: OrderStatus): List<Order>
    fun findByMemberId(memberId: Long): List<Order>
    fun findByStatusAndMemberId(status: OrderStatus, memberId: Long): List<Order>
    fun countByStatus(status: OrderStatus): Long
    fun existsByMemberIdAndStatus(memberId: Long, status: OrderStatus): Boolean

    // 페이징
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order>

    // @Query - JPQL (엔티티와 필드 이름 기준)
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount ORDER BY o.createdAt DESC")
    fun findLargeOrders(@Param("minAmount") minAmount: BigDecimal): List<Order>

    // @Query - Native SQL (테이블과 컬럼 이름 기준)
    @Query(
        value = "SELECT * FROM orders WHERE DATE(created_at) = CURDATE()",
        nativeQuery = true
    )
    fun findTodayOrders(): List<Order>

    // Fetch Join으로 연관 관계 즉시 로딩
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.member.id = :memberId")
    fun findByMemberIdWithItems(@Param("memberId") memberId: Long): List<Order>
}
```

#### 페이징 처리

```kotlin
@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun getOrdersByStatus(status: OrderStatus, page: Int, size: Int): Page<Order> {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return orderRepository.findByStatus(status, pageable)
    }
}

// Controller에서 사용
@GetMapping("/orders")
fun getOrders(
    @RequestParam status: OrderStatus,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int
): ResponseEntity<Page<OrderResponse>> {
    val orders = orderService.getOrdersByStatus(status, page, size)
    return ResponseEntity.ok(orders.map { OrderResponse.from(it) })
}
```

---

### 연관 관계 매핑과 N+1 문제

#### N+1 문제 발생 예시

```kotlin
// 문제 상황: 주문 목록 조회 시 각 주문마다 member 조회 쿼리가 발생

@Service
class OrderService(private val orderRepository: OrderRepository) {

    fun getAllOrdersWithMember(): List<OrderDto> {
        val orders = orderRepository.findAll()
        // orders가 100개라면 → 1(orders 조회) + 100(member 조회) = 101번 쿼리 실행!
        return orders.map { order ->
            OrderDto(
                orderId = order.id,
                memberName = order.member.name,  // 여기서 Lazy Loading 발생
                status = order.status
            )
        }
    }
}
```

#### 해결법 1: Fetch Join

```kotlin
// Repository에 Fetch Join 쿼리 추가
interface OrderRepository : JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.member")
    fun findAllWithMember(): List<Order>

    // 컬렉션 Fetch Join 시 DISTINCT 필수
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.member JOIN FETCH o.orderItems")
    fun findAllWithMemberAndItems(): List<Order>
}

// 사용
fun getAllOrdersWithMember(): List<OrderDto> {
    val orders = orderRepository.findAllWithMember()
    // 쿼리 1번으로 해결!
    return orders.map { ... }
}
```

#### 해결법 2: @EntityGraph

```kotlin
interface OrderRepository : JpaRepository<Order, Long> {

    // attributePaths에 즉시 로딩할 연관 관계 지정
    @EntityGraph(attributePaths = ["member", "orderItems", "orderItems.product"])
    fun findAll(): List<Order>

    @EntityGraph(attributePaths = ["member"])
    fun findByStatus(status: OrderStatus): List<Order>
}
```

#### 해결법 3: hibernate.default_batch_fetch_size (N+1 → 1+1로)

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
# IN 절을 사용해서 100개씩 묶어서 조회
# SELECT * FROM members WHERE id IN (1, 2, 3, ..., 100)
```

#### FetchType.LAZY vs EAGER 선택 기준

```kotlin
@Entity
class Order(
    // LAZY: 기본값으로 사용 권장
    // - 실제로 접근할 때만 쿼리 실행
    // - 필요 없으면 쿼리 안 나감
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    // EAGER: 가급적 사용 자제
    // - Order를 조회할 때 항상 함께 조회
    // - JPQL에서 N+1 문제 발생 위험
    // - 컬렉션(OneToMany)에 EAGER는 특히 위험
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)  // 컬렉션은 반드시 LAZY
    val orderItems: MutableList<OrderItem> = mutableListOf(),
)
// 원칙: 모든 연관 관계는 LAZY로 설정하고, 필요할 때 Fetch Join / @EntityGraph 사용
```

---

## Part 3: Spring Data R2DBC + Komapper (Reactive 방식)

### R2DBC란?

```
JDBC (Java Database Connectivity)
- 동기 / Blocking IO
- 스레드당 1개 커넥션 점유
- 동시 요청 1000개 = 스레드 1000개 필요

R2DBC (Reactive Relational Database Connectivity)
- 비동기 / Non-blocking IO
- 적은 스레드로 많은 요청 처리
- Spring WebFlux + Kotlin Coroutine과 궁합 최적

[요청] → [WebFlux 핸들러] → [R2DBC Repository] → [DB]
           (Coroutine)         (suspend/Flow)       (Non-blocking)
```

### R2DBC 환경 설정

#### build.gradle.kts

```kotlin
plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    // plugin.jpa 불필요 - R2DBC는 JPA 어노테이션 사용 안 함
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // MySQL R2DBC 드라이버
    runtimeOnly("io.asyncer:r2dbc-mysql:1.0.5")

    // PostgreSQL R2DBC 드라이버
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    // Flyway는 JDBC가 필요하므로 별도 JDBC 의존성 추가
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("com.mysql:mysql-connector-j")      // MySQL인 경우
    // runtimeOnly("org.postgresql:postgresql")     // PostgreSQL인 경우

    // Kotlin Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}
```

#### application.yml - R2DBC 설정

```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/myapp_db     # MySQL
    # url: r2dbc:postgresql://localhost:5432/myapp_db  # PostgreSQL
    username: myapp_user
    password: myapp_password
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m
      validation-query: SELECT 1

  # Flyway는 JDBC 사용 (R2DBC 환경에서 마이그레이션용)
  flyway:
    url: jdbc:mysql://localhost:3306/myapp_db
    user: myapp_user
    password: myapp_password
    enabled: true
```

#### R2dbcConfig 클래스

```kotlin
@Configuration
@EnableR2dbcRepositories
class R2dbcConfig : AbstractR2dbcConfiguration() {

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactories.get(
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "mysql")
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.PORT, 3306)
                .option(ConnectionFactoryOptions.DATABASE, "myapp_db")
                .option(ConnectionFactoryOptions.USER, "myapp_user")
                .option(ConnectionFactoryOptions.PASSWORD, "myapp_password")
                .build()
        )
    }

    // 커스텀 컨버터 등록 (예: Enum 변환)
    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters = listOf(
            OrderStatusReadConverter(),
            OrderStatusWriteConverter(),
        )
        return R2dbcCustomConversions(getStoreConversions(), converters)
    }
}

// Enum 컨버터 예시
@ReadingConverter
class OrderStatusReadConverter : Converter<String, OrderStatus> {
    override fun convert(source: String): OrderStatus =
        OrderStatus.valueOf(source)
}

@WritingConverter
class OrderStatusWriteConverter : Converter<OrderStatus, String> {
    override fun convert(source: OrderStatus): String = source.name
}
```

---

### R2DBC Entity & Repository

#### Entity 작성 (JPA와 다른 점)

```kotlin
// R2DBC Entity - JPA와 달리 jakarta.persistence 어노테이션 사용 안 함!
// org.springframework.data.annotation 과 org.springframework.data.relational.core.mapping 사용

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("orders")
data class OrderEntity(   // R2DBC는 data class 사용 가능!
    @Id
    val id: Long? = null,  // null이면 INSERT, non-null이면 UPDATE

    @Column("status")
    val status: String = OrderStatus.PENDING.name,

    @Column("total_amount")
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column("member_id")
    val memberId: Long,    // R2DBC는 연관 관계 없음 → FK만 저장

    @CreatedDate
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    val updatedAt: LocalDateTime? = null,
)

// 주의: R2DBC는 @OneToMany, @ManyToOne 등 연관 관계 어노테이션 없음
// OrderItems는 별도 Repository로 조회 후 직접 조합해야 함
```

#### Repository - suspend 함수와 Flow

```kotlin
@Repository
interface OrderR2dbcRepository : CoroutineCrudRepository<OrderEntity, Long> {

    // suspend 함수로 단건 조회
    suspend fun findByMemberId(memberId: Long): OrderEntity?

    // Flow로 복수 조회
    fun findAllByStatus(status: String): Flow<OrderEntity>

    // @Query로 커스텀 SQL
    @Query("SELECT * FROM orders WHERE member_id = :memberId AND status = :status")
    fun findByMemberIdAndStatus(memberId: Long, status: String): Flow<OrderEntity>

    // 페이징
    fun findAllByStatus(status: String, pageable: Pageable): Flow<OrderEntity>

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    suspend fun countByStatus(status: String): Long
}

// CoroutineCrudRepository vs ReactiveCrudRepository
// - CoroutineCrudRepository: suspend / Flow 기반 (Kotlin 권장)
// - ReactiveCrudRepository: Mono / Flux 기반 (Java 스타일)
```

#### Service 계층에서 사용

```kotlin
@Service
class OrderService(
    private val orderRepository: OrderR2dbcRepository,
    private val orderItemRepository: OrderItemR2dbcRepository,
) {

    // suspend 함수로 단건 조회
    suspend fun getOrder(orderId: Long): OrderDetailDto {
        val order = orderRepository.findById(orderId)
            ?: throw NoSuchElementException("Order not found: $orderId")

        // 연관 데이터는 별도 조회 (R2DBC는 연관 관계 없음)
        val items = orderItemRepository.findAllByOrderId(orderId).toList()

        return OrderDetailDto(
            orderId = order.id!!,
            status = order.status,
            items = items.map { ItemDto.from(it) }
        )
    }

    // Flow로 스트리밍 조회
    fun getOrdersByStatus(status: String): Flow<OrderSummaryDto> {
        return orderRepository.findAllByStatus(status)
            .map { OrderSummaryDto.from(it) }
    }

    // 트랜잭션
    @Transactional
    suspend fun createOrder(request: CreateOrderRequest): OrderEntity {
        val order = OrderEntity(
            memberId = request.memberId,
            status = OrderStatus.PENDING.name,
            totalAmount = request.totalAmount,
        )
        val savedOrder = orderRepository.save(order)

        val items = request.items.map { item ->
            OrderItemEntity(
                orderId = savedOrder.id!!,
                productId = item.productId,
                price = item.price,
                quantity = item.quantity,
            )
        }
        orderItemRepository.saveAll(items).collect()

        return savedOrder
    }
}
```

---

### Komapper 소개

#### Komapper란?

```
Komapper는 Kotlin-first ORM으로:
- 타입 안전한 쿼리 DSL 제공 (SQL 문자열 실수 없음)
- R2DBC / JDBC 둘 다 지원
- Kotlin Symbol Processing(KSP)으로 메타데이터 생성
- Coroutine 네이티브 지원
```

#### build.gradle.kts - Komapper 설정

```kotlin
plugins {
    kotlin("jvm") version "1.9.21"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"  // KSP 플러그인
}

dependencies {
    // Komapper R2DBC (비동기)
    implementation(platform("org.komapper:komapper-platform:2.1.0"))
    implementation("org.komapper:komapper-starter-r2dbc")
    implementation("org.komapper:komapper-dialect-mysql-r2dbc")      // MySQL
    // implementation("org.komapper:komapper-dialect-postgresql-r2dbc") // PostgreSQL

    ksp("org.komapper:komapper-processor")  // KSP로 메타데이터 생성
}
```

#### Entity 정의

```kotlin
// Komapper는 엔티티 클래스와 매핑 정의를 분리함

// 1. 엔티티 클래스 (순수 data class)
data class Product(
    val id: Int,
    val name: String,
    val price: BigDecimal,
    val categoryId: Int,
    val stockQuantity: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

// 2. 매핑 정의 (KSP가 메타데이터를 생성해줌)
@KomapperEntityDef(Product::class)
@KomapperTable("products")
data class ProductDef(
    @KomapperId
    @KomapperAutoIncrement
    val id: Nothing,

    @KomapperColumn("category_id")
    val categoryId: Nothing,

    @KomapperColumn("stock_quantity")
    val stockQuantity: Nothing,

    @KomapperCreatedAt
    val createdAt: Nothing,

    @KomapperUpdatedAt
    val updatedAt: Nothing,
)
// KSP가 _Product 메타 클래스를 자동 생성
```

#### 쿼리 DSL 사용

```kotlin
@Service
class ProductService(
    private val db: R2dbcDatabase  // Komapper R2DBC 데이터베이스
) {

    // 단건 조회
    suspend fun findById(id: Int): Product? {
        val p = Meta.product  // KSP가 생성한 메타 클래스
        return db.runQuery {
            QueryDsl.from(p).where { p.id eq id }.first()
        }
    }

    // 복합 조건 검색
    suspend fun searchProducts(
        categoryId: Int?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        name: String?,
        page: Int,
        size: Int,
    ): List<Product> {
        val p = Meta.product
        return db.runQuery {
            QueryDsl.from(p)
                .where {
                    and {
                        categoryId?.let { p.categoryId eq it }
                        minPrice?.let { p.price greaterEq it }
                        maxPrice?.let { p.price lessEq it }
                        name?.let { p.name like "%$it%" }
                    }
                }
                .orderBy(p.createdAt.desc())
                .offset(page * size)
                .limit(size)
        }
    }

    // INSERT
    suspend fun create(name: String, price: BigDecimal, categoryId: Int): Product {
        val p = Meta.product
        return db.runQuery {
            QueryDsl.insert(p).single(
                Product(
                    id = 0,  // AutoIncrement이므로 무시됨
                    name = name,
                    price = price,
                    categoryId = categoryId,
                    stockQuantity = 0,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )
            )
        }
    }

    // UPDATE
    suspend fun updatePrice(id: Int, newPrice: BigDecimal): Long {
        val p = Meta.product
        return db.runQuery {
            QueryDsl.update(p)
                .set { p.price eq newPrice }
                .where { p.id eq id }
        }
    }

    // DELETE
    suspend fun delete(id: Int): Long {
        val p = Meta.product
        return db.runQuery {
            QueryDsl.delete(p).where { p.id eq id }
        }
    }

    // Flow로 스트리밍
    fun findAllByCategory(categoryId: Int): Flow<Product> {
        val p = Meta.product
        return db.flowQuery {
            QueryDsl.from(p)
                .where { p.categoryId eq categoryId }
                .orderBy(p.name.asc())
        }
    }
}
```

---

### JPA vs R2DBC 실전 비교

#### 단순 CRUD 비교

```kotlin
// ============================================================
// JPA (Blocking)
// ============================================================
@Service
class ProductServiceJpa(
    private val productRepository: ProductJpaRepository
) {
    fun findById(id: Long): Product =
        productRepository.findById(id).orElseThrow { NoSuchElementException("Not found") }

    fun create(request: CreateProductRequest): Product =
        productRepository.save(
            Product(name = request.name, price = request.price)
        )

    fun update(id: Long, request: UpdateProductRequest): Product {
        val product = findById(id)
        product.name = request.name
        product.price = request.price
        return product  // Dirty Checking으로 자동 UPDATE
    }

    fun delete(id: Long) = productRepository.deleteById(id)
}

// ============================================================
// R2DBC (Non-blocking)
// ============================================================
@Service
class ProductServiceR2dbc(
    private val productRepository: ProductR2dbcRepository
) {
    suspend fun findById(id: Long): ProductEntity =
        productRepository.findById(id) ?: throw NoSuchElementException("Not found")

    suspend fun create(request: CreateProductRequest): ProductEntity =
        productRepository.save(
            ProductEntity(name = request.name, price = request.price)
        )

    suspend fun update(id: Long, request: UpdateProductRequest): ProductEntity {
        val product = findById(id)
        // R2DBC는 Dirty Checking 없음 → 명시적으로 save 호출 필요
        return productRepository.save(
            product.copy(name = request.name, price = request.price)
        )
    }

    suspend fun delete(id: Long) = productRepository.deleteById(id)
}
```

#### 페이징 비교

```kotlin
// ============================================================
// JPA 페이징
// ============================================================
@Repository
interface ProductJpaRepository : JpaRepository<Product, Long> {
    fun findByCategory(category: String, pageable: Pageable): Page<Product>
}

@Service
class ProductServiceJpa(...) {
    fun getProducts(category: String, page: Int, size: Int): Page<Product> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return productRepository.findByCategory(category, pageable)
        // Page<T>에는 totalElements, totalPages 정보 포함
    }
}

// ============================================================
// R2DBC 페이징
// ============================================================
@Repository
interface ProductR2dbcRepository : CoroutineCrudRepository<ProductEntity, Long> {
    fun findAllByCategory(category: String, pageable: Pageable): Flow<ProductEntity>

    @Query("SELECT COUNT(*) FROM products WHERE category = :category")
    suspend fun countByCategory(category: String): Long
}

@Service
class ProductServiceR2dbc(...) {
    suspend fun getProducts(category: String, page: Int, size: Int): PageResult<ProductEntity> {
        val pageable = PageRequest.of(page, size, Sort.by("created_at").descending())
        val items = productRepository.findAllByCategory(category, pageable).toList()
        val total = productRepository.countByCategory(category)
        // R2DBC는 Page<T>를 직접 지원하지 않아 수동으로 구성
        return PageResult(items = items, total = total, page = page, size = size)
    }
}

data class PageResult<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int = ((total + size - 1) / size).toInt()
)
```

#### 트랜잭션 비교

```kotlin
// ============================================================
// JPA 트랜잭션
// ============================================================
@Service
class OrderServiceJpa(
    private val orderRepository: OrderJpaRepository,
    private val productRepository: ProductJpaRepository,
) {
    @Transactional  // 동기 트랜잭션
    fun placeOrder(request: PlaceOrderRequest): Order {
        val product = productRepository.findById(request.productId)
            .orElseThrow { NoSuchElementException("Product not found") }

        check(product.stockQuantity >= request.quantity) { "재고 부족" }
        product.stockQuantity -= request.quantity  // Dirty Checking

        return orderRepository.save(
            Order(member = ..., status = OrderStatus.CONFIRMED)
        )
    }
}

// ============================================================
// R2DBC 트랜잭션
// ============================================================
@Service
class OrderServiceR2dbc(
    private val orderRepository: OrderR2dbcRepository,
    private val productRepository: ProductR2dbcRepository,
) {
    @Transactional  // Reactive 트랜잭션 (ReactiveTransactionManager 자동 사용)
    suspend fun placeOrder(request: PlaceOrderRequest): OrderEntity {
        val product = productRepository.findById(request.productId)
            ?: throw NoSuchElementException("Product not found")

        check(product.stockQuantity >= request.quantity) { "재고 부족" }

        // R2DBC는 Dirty Checking 없음 → 명시적 save 필요
        productRepository.save(
            product.copy(stockQuantity = product.stockQuantity - request.quantity)
        )

        return orderRepository.save(
            OrderEntity(memberId = request.memberId, status = OrderStatus.CONFIRMED.name)
        )
    }
}
```

---

## Part 4: DB 마이그레이션 (Flyway)

### Flyway란?

```
문제: 팀 개발 환경에서 DB 스키마를 수동으로 관리하면?
- "A 개발자가 컬럼 추가했는데 B 개발자 DB에는 반영 안 됨"
- "운영 배포 시 누가 ALTER TABLE 실행했는지 모름"
- "롤백 시 어느 시점으로 돌아가야 하는지 모름"

해결: Flyway - 마이그레이션 파일을 버전 관리하여 자동 실행
- 실행된 스크립트 이력을 flyway_schema_history 테이블에 저장
- 애플리케이션 시작 시 자동으로 미실행 스크립트 순차 실행
- 이미 실행한 스크립트는 체크섬으로 변경 감지
```

#### Flyway vs Liquibase 비교

| 항목 | Flyway | Liquibase |
|------|--------|-----------|
| 스크립트 형식 | SQL (기본) | XML, YAML, JSON, SQL |
| 학습 곡선 | 낮음 | 보통 |
| 롤백 | 유료 기능 | 내장 지원 |
| 복잡한 조건 마이그레이션 | 어려움 | 쉬움 |
| 팀 선호도 | SQL에 익숙한 팀 | 다양한 환경 팀 |

### 설정

#### build.gradle.kts

```kotlin
dependencies {
    implementation("org.flywaydb:flyway-core")

    // MySQL 사용 시 (Flyway 10+는 MySQL 드라이버 별도 필요)
    implementation("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")

    // PostgreSQL 사용 시
    runtimeOnly("org.postgresql:postgresql")
}
```

#### application.yml

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration    # 스크립트 위치
    baseline-on-migrate: false           # 기존 DB에 처음 적용 시 true
    validate-on-migrate: true            # 실행된 스크립트 변경 감지
    out-of-order: false                  # 순서 어긋난 스크립트 실행 금지
    table: flyway_schema_history         # 이력 테이블 이름

  # R2DBC 환경에서는 Flyway용 JDBC 별도 설정
  datasource:
    url: jdbc:mysql://localhost:3306/myapp_db
    username: myapp_user
    password: myapp_password
```

#### R2DBC 환경에서 Flyway 사용 시 주의사항

```kotlin
// R2DBC를 쓰면 spring.datasource가 없으므로 Flyway용 DataSource를 수동 생성
@Configuration
class FlywayConfig {

    @Bean(initMethod = "migrate")
    fun flyway(
        @Value("\${spring.flyway.url}") url: String,
        @Value("\${spring.flyway.user}") user: String,
        @Value("\${spring.flyway.password}") password: String,
    ): Flyway {
        return Flyway.configure()
            .dataSource(url, user, password)
            .locations("classpath:db/migration")
            .load()
    }
}
```

### 마이그레이션 파일 구조

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__init_tables.sql
        ├── V2__add_member_phone.sql
        ├── V3__add_product_index.sql
        └── V4__insert_initial_data.sql
```

#### 네이밍 규칙

```
V{버전}__{설명}.sql

V    = 버전 마이그레이션 (한 번 실행, 변경 불가)
R    = 반복 실행 (R__seed_data.sql - 내용 변경 시 재실행)
U    = Undo 마이그레이션 (유료 Teams/Enterprise)

버전: 숫자 (1, 2, 3) 또는 날짜 (20240101, 20240101_1)
설명: 언더스코어로 구분 (두 개 언더스코어가 구분자)
```

#### V1__init_tables.sql

```sql
-- V1: 초기 테이블 생성
CREATE TABLE members (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    email       VARCHAR(100)    NOT NULL,
    name        VARCHAR(50)     NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_members_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    price           DECIMAL(15, 2)  NOT NULL,
    stock_quantity  INT             NOT NULL DEFAULT 0,
    category        VARCHAR(50)     NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    member_id       BIGINT          NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    total_amount    DECIMAL(15, 2)  NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### V2__add_member_phone.sql

```sql
-- V2: 회원 테이블에 전화번호 컬럼 추가
ALTER TABLE members
    ADD COLUMN phone VARCHAR(20) NULL AFTER email,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER name;
```

#### V3__add_product_index.sql

```sql
-- V3: 성능 향상을 위한 인덱스 추가
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_price ON products (price);
CREATE INDEX idx_orders_member_id ON orders (member_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);
```

#### V4__insert_initial_data.sql

```sql
-- V4: 초기 데이터 삽입
INSERT INTO members (email, name, status) VALUES
    ('admin@example.com', '관리자', 'ACTIVE'),
    ('test@example.com', '테스트유저', 'ACTIVE');

INSERT INTO products (name, price, stock_quantity, category) VALUES
    ('테스트 상품 A', 10000.00, 100, 'ELECTRONICS'),
    ('테스트 상품 B', 25000.00, 50, 'CLOTHING');
```

#### 실수했을 때 대처법

```
Flyway는 기본적으로 롤백이 없음 (무료 버전)
따라서 실수를 V{N+1} 스크립트로 수정해야 함

예시: V2에서 컬럼명을 잘못 추가했을 때
→ V2 파일 수정 불가 (체크섬 오류 발생)
→ V3__fix_column_name.sql 파일을 새로 생성

# 이미 배포된 스크립트를 수정해야 하는 긴급 상황:
# 1. flyway_schema_history 테이블에서 해당 버전 레코드 삭제
# 2. 수정된 스크립트로 재실행
# (비권장 - 운영 환경에서는 절대 사용 자제)

운영 베스트 프랙티스:
- 개발/스테이징에서 충분히 검증 후 배포
- 컬럼 삭제 대신 먼저 null 허용으로 변경 → 코드 제거 → 나중에 컬럼 삭제
- 대용량 테이블 ALTER는 pt-online-schema-change 또는 gh-ost 사용
```

---

## Part 5: AWS Aurora

### Aurora란?

```
Amazon Aurora = AWS가 만든 클라우드 최적화 관계형 DB

일반 RDS와 차이점:
- 스토리지가 3개 AZ에 6개 복사본으로 자동 복제 (내구성 향상)
- 읽기 복제본이 최대 15개까지 (일반 RDS는 5개)
- 장애 복구(Failover) 시간: Aurora 약 30초, 일반 RDS 약 2분
- 스토리지 자동 확장 (10GB 단위, 최대 128TB)
- MySQL 5배, PostgreSQL 3배 처리량 (AWS 벤치마크)

Aurora MySQL vs Aurora PostgreSQL:
- Aurora MySQL: MySQL 8.0 호환, 쓰기 성능 특화
- Aurora PostgreSQL: PostgreSQL 15 호환, 분석/복잡한 쿼리 특화
```

#### 클러스터 구조

```
                    [클러스터 엔드포인트]        [읽기 전용 엔드포인트]
                          |                              |
[애플리케이션] --쓰기--> [Writer 인스턴스]     [Reader 인스턴스 1]
                                    |           [Reader 인스턴스 2]  <--- 읽기
                         [분산 스토리지 (6-way 복제)]
                              /         \
                      [AZ-1]     [AZ-2]     [AZ-3]

클러스터 엔드포인트: 항상 현재 Writer로 라우팅
읽기 전용 엔드포인트: Reader들 사이에 로드 밸런싱
인스턴스 엔드포인트: 특정 인스턴스에 직접 연결 (테스트용)
```

### Spring Boot 연동

#### application-prod.yml (Writer/Reader 분리)

```yaml
spring:
  # Writer DataSource (쓰기 전용)
  datasource:
    writer:
      jdbc-url: jdbc:mysql://cluster-endpoint.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com:3306/myapp_db
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        pool-name: WriterPool
        maximum-pool-size: 10
        minimum-idle: 3
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        # Aurora Failover 대응
        keepalive-time: 60000
        connection-test-query: SELECT 1

    reader:
      jdbc-url: jdbc:mysql://cluster-ro-endpoint.cluster-ro-xxxxx.ap-northeast-2.rds.amazonaws.com:3306/myapp_db
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        pool-name: ReaderPool
        maximum-pool-size: 20    # 읽기는 더 많은 커넥션 허용
        minimum-idle: 5
        connection-timeout: 30000
```

#### 읽기/쓰기 분리 (AbstractRoutingDataSource 패턴)

```kotlin
// 현재 트랜잭션의 읽기/쓰기 모드를 ThreadLocal로 관리
enum class DataSourceType { WRITER, READER }

object DataSourceContextHolder {
    private val context = ThreadLocal<DataSourceType>()

    fun set(type: DataSourceType) = context.set(type)
    fun get(): DataSourceType = context.get() ?: DataSourceType.WRITER
    fun clear() = context.remove()
}

// AbstractRoutingDataSource: 요청마다 어느 DataSource를 쓸지 결정
class RoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any = DataSourceContextHolder.get()
}

@Configuration
class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.writer.hikari")
    fun writerDataSource(): DataSource = HikariDataSource()

    @Bean
    @ConfigurationProperties("spring.datasource.reader.hikari")
    fun readerDataSource(): DataSource = HikariDataSource()

    @Bean
    @Primary
    fun routingDataSource(
        @Qualifier("writerDataSource") writer: DataSource,
        @Qualifier("readerDataSource") reader: DataSource,
    ): DataSource {
        val routing = RoutingDataSource()
        routing.setTargetDataSources(
            mapOf(
                DataSourceType.WRITER to writer,
                DataSourceType.READER to reader,
            )
        )
        routing.setDefaultTargetDataSource(writer)
        return routing
    }

    // LazyConnectionDataSourceProxy: 실제 커넥션을 쿼리 직전에 획득 (라우팅 후 커넥션 획득)
    @Bean
    fun dataSource(@Qualifier("routingDataSource") routing: DataSource): DataSource =
        LazyConnectionDataSourceProxy(routing)
}

// AOP로 @Transactional(readOnly = true)에 Reader 자동 라우팅
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class DataSourceRoutingAspect {

    @Around("@annotation(transactional)")
    fun routeDataSource(
        joinPoint: ProceedingJoinPoint,
        transactional: Transactional,
    ): Any? {
        try {
            if (transactional.readOnly) {
                DataSourceContextHolder.set(DataSourceType.READER)
            } else {
                DataSourceContextHolder.set(DataSourceType.WRITER)
            }
            return joinPoint.proceed()
        } finally {
            DataSourceContextHolder.clear()
        }
    }
}

// 사용 예시
@Service
class ProductService(private val productRepository: ProductRepository) {

    @Transactional(readOnly = true)  // Reader DataSource 자동 선택
    fun findAll(): List<Product> = productRepository.findAll()

    @Transactional                   // Writer DataSource 자동 선택
    fun create(request: CreateProductRequest): Product =
        productRepository.save(Product(...))
}
```

### 운영 팁

#### Failover 대응 설정

```yaml
spring:
  datasource:
    hikari:
      # Aurora Failover 시 새 Writer에 빠르게 재연결
      connection-timeout: 30000
      keepalive-time: 60000          # 60초마다 커넥션 유지 확인
      max-lifetime: 1800000          # 30분마다 커넥션 재생성 (DNS 갱신 반영)
      connection-test-query: SELECT 1
      # Aurora DNS TTL이 짧음 (5초) → JVM DNS 캐시도 짧게
```

```kotlin
// JVM DNS 캐시 설정 (Aurora Failover 대응)
// main() 또는 @Configuration에서 설정
java.security.Security.setProperty("networkaddress.cache.ttl", "10")
java.security.Security.setProperty("networkaddress.cache.negative.ttl", "5")
```

#### 파라미터 그룹 주요 설정

```
Aurora MySQL 파라미터 그룹 권장값:

character_set_server          = utf8mb4
collation_server              = utf8mb4_unicode_ci
time_zone                     = Asia/Seoul
max_connections               = 트래픽에 맞게 (기본 1000)
innodb_buffer_pool_size       = 인스턴스 메모리의 75%
slow_query_log                = 1
long_query_time               = 1    (1초 이상 쿼리 로깅)
log_queries_not_using_indexes = 1    (인덱스 미사용 쿼리 로깅)
```

---

## Part 6: Supabase

### Supabase란?

```
Supabase = Open Source Firebase Alternative
- PostgreSQL 기반의 Backend-as-a-Service
- Firebase가 NoSQL(Firestore)인 것과 달리 관계형 DB(PostgreSQL)

제공 기능:
- Database: PostgreSQL
- Auth: JWT, OAuth 2.0
- Storage: 파일 저장
- Realtime: WebSocket 구독 (PostgreSQL LISTEN/NOTIFY 기반)
- Edge Functions: Deno 런타임
```

### Spring Boot + Supabase 연동

#### Connection String 가져오기

```
Supabase 대시보드 → Project Settings → Database
→ Connection string → JDBC 탭에서 복사

Transaction mode (PgBouncer): port 6543
Session mode (직접 연결): port 5432

Spring Boot에서는 일반적으로 Transaction mode(6543) 권장
(서버리스 환경 또는 단기 커넥션이 많을 때)
```

#### application.yml

```yaml
spring:
  datasource:
    # Transaction mode (PgBouncer) - 권장
    url: jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:6543/postgres?pgbouncer=true
    # Session mode (직접 연결)
    # url: jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres
    username: postgres
    password: ${SUPABASE_DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      # PgBouncer Transaction mode에서는 prepared statement 사용 불가
      data-source-properties:
        prepareThreshold: 0           # Prepared Statement 비활성화 (PgBouncer 호환)
        preparedStatementCacheQueries: 0

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

#### R2DBC로 Supabase 연결

```yaml
spring:
  r2dbc:
    # PgBouncer Session mode 사용 (R2DBC는 Transaction mode 미지원)
    url: r2dbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres
    username: postgres
    password: ${SUPABASE_DB_PASSWORD}

  # Flyway용 JDBC는 PgBouncer Transaction mode 사용 가능
  flyway:
    url: jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:6543/postgres?pgbouncer=true
    user: postgres
    password: ${SUPABASE_DB_PASSWORD}
```

### Supabase 특화 기능

#### Row Level Security (RLS)

```sql
-- Supabase는 기본적으로 RLS 활성화 권장
-- Spring Boot에서 서비스 계정으로 접근 시 RLS를 bypass하는 role 사용

-- 1. RLS 활성화
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

-- 2. Service Role로 접근 시 모든 행 허용 (Spring Boot용)
CREATE POLICY "service_role_all" ON orders
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- 3. 일반 사용자는 자신의 데이터만 접근 (Supabase Auth 사용 시)
CREATE POLICY "users_own_orders" ON orders
    FOR ALL
    TO authenticated
    USING (member_id = auth.uid()::bigint);
```

```
Spring Boot에서 주의사항:
- Spring Boot에서 DB에 직접 연결 시 service_role 또는 postgres role 사용
- 이 role들은 RLS를 bypass하므로 애플리케이션 레벨에서 권한 제어 필요
- Supabase Auth는 클라이언트(Frontend)에서 직접 Supabase API 호출 시 활용
- Spring Boot API 서버에서는 자체 인증(Spring Security)으로 처리하고
  DB는 service_role로 연결하는 패턴이 일반적
```

#### Supabase Auth JWT를 Spring Security에서 검증

```kotlin
// build.gradle.kts
implementation("io.jsonwebtoken:jjwt-api:0.11.5")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

// JWT 검증 필터
@Component
class SupabaseJwtFilter(
    @Value("\${supabase.jwt-secret}") private val jwtSecret: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)

        if (token != null) {
            try {
                val claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.toByteArray())
                    .build()
                    .parseClaimsJws(token)
                    .body

                val userId = claims.subject
                val role = claims["role"] as String

                val authentication = UsernamePasswordAuthenticationToken(
                    userId, null,
                    listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
                )
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: JwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        return if (header.startsWith("Bearer ")) header.substring(7) else null
    }
}
```

#### Realtime 구독 (PostgreSQL LISTEN/NOTIFY)

```kotlin
// Supabase Realtime은 WebSocket 기반이며 Supabase JS 클라이언트로 주로 사용
// Spring Boot에서 PostgreSQL LISTEN/NOTIFY 직접 사용 예시

@Service
class RealtimeService(
    @Value("\${spring.datasource.url}") private val jdbcUrl: String,
    @Value("\${spring.datasource.username}") private val username: String,
    @Value("\${spring.datasource.password}") private val password: String,
) {
    private val notifyChannel = MutableSharedFlow<String>()
    val events: SharedFlow<String> = notifyChannel

    @PostConstruct
    fun startListening() {
        val connection = DriverManager.getConnection(jdbcUrl, username, password)
        val pgConn = connection.unwrap(PGConnection::class.java)

        // LISTEN 채널 등록
        connection.createStatement().execute("LISTEN order_events")

        // 백그라운드에서 알림 대기
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val notifications = pgConn.getNotifications(1000) ?: continue
                notifications.forEach { notification ->
                    notifyChannel.emit(notification.parameter)
                }
            }
        }
    }
}

// DB에서 알림 발송 (트리거 또는 수동)
// SELECT pg_notify('order_events', '{"orderId": 123, "status": "CONFIRMED"}');
```

---

## Part 7: Redis

### Redis 핵심 개념

```
Redis가 필요한 이유:
1. 캐싱: DB 쿼리 결과를 메모리에 저장 → 응답 속도 향상 (ms → μs)
2. 세션: 분산 서버에서 세션 공유 (서버 재시작해도 세션 유지)
3. 분산 락: 여러 서버에서 동시 실행 방지 (재고 차감, 포인트 지급 등)
4. Pub/Sub: 실시간 메시지 브로드캐스트 (알림, 채팅)
5. Rate Limiting: API 요청 횟수 제한
6. 순위(Leaderboard): Sorted Set으로 실시간 랭킹
```

#### Redis 데이터 구조별 용도

| 구조 | 명령어 예시 | 주요 용도 |
|------|------------|----------|
| String | GET/SET | 캐싱, 카운터, 세션, 분산 락 |
| Hash | HGET/HSET | 객체 저장 (사용자 프로필 등) |
| List | LPUSH/RPOP | 작업 큐, 최근 방문 목록 |
| Set | SADD/SMEMBERS | 유니크 값 집합 (좋아요 목록) |
| Sorted Set | ZADD/ZRANGE | 순위/점수 기반 정렬 (랭킹) |
| Stream | XADD/XREAD | 이벤트 스트리밍 (Kafka 경량 대안) |

#### TTL 개념

```
TTL (Time To Live): 키 만료 시간 설정
- 만료 시 Redis가 자동으로 키 삭제
- 캐시 오염 방지, 메모리 자동 회수

설정 예시:
SET session:user:123 "{...}" EX 3600    # 1시간 후 만료
SET cache:product:456 "{...}" EX 300    # 5분 후 만료

TTL 없는 키 = 영구 저장 → 메모리 부족 위험!
중요: 모든 캐시 키에는 반드시 TTL 설정
```

### 환경 설정

#### build.gradle.kts

```kotlin
dependencies {
    // Spring Data Redis (Reactive + Blocking 둘 다 포함)
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Redisson (분산 락)
    implementation("org.redisson:redisson-spring-boot-starter:3.25.0")

    // Jackson (Kotlin data class 직렬화)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```

#### application.yml

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:               # 비밀번호 없을 때 빈 값
    timeout: 2000ms
    lettuce:                # Lettuce 클라이언트 (Reactive 지원)
      pool:
        max-active: 10
        max-idle: 5
        min-idle: 2
        max-wait: 1000ms
```

#### RedisConfig 클래스

```kotlin
@Configuration
@EnableCaching
class RedisConfig {

    // Reactive용 (WebFlux / Coroutine)
    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, Any> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper())

        val context = RedisSerializationContext
            .newSerializationContext<String, Any>(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, context)
    }

    // Blocking용 (Spring MVC / JPA)
    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper())
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper())
        }
    }

    // CacheManager (@Cacheable 어노테이션용)
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper())
                )
            )

        // 캐시별 TTL 설정
        val cacheConfigurations = mapOf(
            "products" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "members"  to defaultConfig.entryTtl(Duration.ofMinutes(60)),
            "sessions" to defaultConfig.entryTtl(Duration.ofHours(24)),
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    private fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        activateDefaultTyping(
            polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY,
        )
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        registerModule(JavaTimeModule())
    }
}
```

---

### 캐싱 패턴

#### 방법 1: @Cacheable 어노테이션 방식

```kotlin
@Service
class ProductService(private val productRepository: ProductJpaRepository) {

    // 캐시에서 먼저 조회, 없으면 DB 조회 후 캐시 저장
    @Cacheable(value = ["products"], key = "#id")
    fun findById(id: Long): ProductDto {
        return productRepository.findById(id)
            .map { ProductDto.from(it) }
            .orElseThrow { NoSuchElementException("Product not found: $id") }
    }

    // 목록 캐싱 (카테고리별)
    @Cacheable(value = ["products"], key = "'category:' + #category")
    fun findByCategory(category: String): List<ProductDto> {
        return productRepository.findByCategory(category).map { ProductDto.from(it) }
    }

    // DB 업데이트 후 캐시 삭제
    @CacheEvict(value = ["products"], key = "#id")
    @Transactional
    fun update(id: Long, request: UpdateProductRequest): ProductDto {
        val product = productRepository.findById(id)
            .orElseThrow { NoSuchElementException("Not found") }
        product.name = request.name
        product.price = request.price
        return ProductDto.from(product)
    }

    // 캐시 업데이트 (삭제 없이 갱신)
    @CachePut(value = ["products"], key = "#result.id")
    @Transactional
    fun updateAndRefreshCache(id: Long, request: UpdateProductRequest): ProductDto {
        val product = productRepository.findById(id)
            .orElseThrow { NoSuchElementException("Not found") }
        product.name = request.name
        return ProductDto.from(product)
    }

    // 여러 캐시 삭제
    @Caching(evict = [
        CacheEvict(value = ["products"], key = "#id"),
        CacheEvict(value = ["products"], key = "'category:' + #category"),
    ])
    fun deleteProduct(id: Long, category: String) {
        productRepository.deleteById(id)
    }

    // 해당 캐시 전체 삭제
    @CacheEvict(value = ["products"], allEntries = true)
    fun clearAllProductCache() {}
}
```

#### 방법 2: Cache-Aside 패턴 (수동)

```kotlin
@Service
class ProductCacheService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val productRepository: ProductR2dbcRepository,
) {
    companion object {
        private const val CACHE_PREFIX = "cache:product:"
        private val CACHE_TTL = Duration.ofMinutes(30)
    }

    suspend fun findById(id: Long): ProductDto {
        val cacheKey = "$CACHE_PREFIX$id"

        // 1. 캐시 조회
        val cached = reactiveRedisTemplate.opsForValue()
            .get(cacheKey)
            .awaitFirstOrNull()

        if (cached != null) {
            return cached as ProductDto  // 캐시 히트
        }

        // 2. DB 조회
        val product = productRepository.findById(id)
            ?: throw NoSuchElementException("Product not found: $id")

        val dto = ProductDto.from(product)

        // 3. 캐시 저장 (TTL 설정)
        reactiveRedisTemplate.opsForValue()
            .set(cacheKey, dto, CACHE_TTL)
            .awaitSingle()

        return dto
    }

    suspend fun update(id: Long, request: UpdateProductRequest): ProductDto {
        val product = productRepository.findById(id)
            ?: throw NoSuchElementException("Not found")

        val updated = productRepository.save(
            product.copy(name = request.name, price = request.price)
        )
        val dto = ProductDto.from(updated)

        // 캐시 갱신
        val cacheKey = "$CACHE_PREFIX$id"
        reactiveRedisTemplate.opsForValue()
            .set(cacheKey, dto, CACHE_TTL)
            .awaitSingle()

        return dto
    }

    suspend fun delete(id: Long) {
        productRepository.deleteById(id)

        // 캐시 삭제
        reactiveRedisTemplate.delete("$CACHE_PREFIX$id").awaitSingle()
    }
}
```

#### 두 방법 비교

| 항목 | @Cacheable 어노테이션 | Cache-Aside 수동 |
|------|----------------------|-----------------|
| 코드 간결성 | 높음 (어노테이션만) | 낮음 (직접 구현) |
| Reactive 지원 | 제한적 (Spring 6.1+부터 일부 지원) | 완전 지원 |
| 캐시 제어 | 제한적 | 세밀한 제어 가능 |
| 조건부 캐싱 | condition/unless 속성 | 코드로 자유롭게 |
| 복잡한 키 전략 | SpEL로 제한적 | 자유롭게 |
| 적합한 상황 | Spring MVC + JPA (Blocking) | WebFlux + R2DBC (Reactive) |

---

### 분산 락 (Distributed Lock)

#### 분산 환경에서 락이 왜 필요한가

```
재고 차감 문제 (Race Condition):

서버 A: 재고 조회(10) → 차감 → 재고 저장(9)
서버 B: 재고 조회(10) → 차감 → 재고 저장(9)  <- 동시 실행!
결과: 재고 2개 차감됐는데 1개만 차감된 것처럼 저장됨

DB 트랜잭션만으로는?
- 같은 DB 서버라면 SELECT FOR UPDATE로 해결 가능
- 분산 서버 환경에서는 부족 → Redis 분산 락 필요
```

#### Redisson 설정

```kotlin
@Configuration
class RedissonConfig {

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://localhost:6379")
            .setConnectionPoolSize(10)
            .setConnectionMinimumIdleSize(5)
        return Redisson.create(config)
    }
}
```

#### Redisson 분산 락 사용

```kotlin
@Service
class OrderService(
    private val redissonClient: RedissonClient,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {

    fun placeOrder(productId: Long, quantity: Int, memberId: Long): Order {
        val lockKey = "lock:product:$productId"
        val lock = redissonClient.getLock(lockKey)

        // 락 획득 시도: 최대 5초 대기, 획득 후 30초 후 자동 해제
        val acquired = lock.tryLock(5, 30, TimeUnit.SECONDS)

        if (!acquired) {
            throw IllegalStateException("현재 처리 중입니다. 잠시 후 다시 시도해주세요.")
        }

        try {
            val product = productRepository.findById(productId)
                .orElseThrow { NoSuchElementException("Product not found") }

            check(product.stockQuantity >= quantity) { "재고가 부족합니다." }
            product.stockQuantity -= quantity

            return orderRepository.save(
                Order(memberId = memberId, status = OrderStatus.CONFIRMED)
            )
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}
```

#### 커스텀 @DistributedLock 어노테이션 (AOP 패턴)

```kotlin
// 어노테이션 정의
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,              // SpEL 표현식 지원 (예: "#productId")
    val waitTime: Long = 5,       // 락 획득 대기 시간 (초)
    val leaseTime: Long = 30,     // 락 유지 시간 (초)
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
)

// AOP Aspect
@Aspect
@Component
class DistributedLockAspect(
    private val redissonClient: RedissonClient,
) {
    private val parser = SpelExpressionParser()

    @Around("@annotation(distributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any? {
        val key = resolveKey(joinPoint, distributedLock.key)
        val lock = redissonClient.getLock("lock:$key")

        val acquired = lock.tryLock(
            distributedLock.waitTime,
            distributedLock.leaseTime,
            distributedLock.timeUnit
        )

        if (!acquired) {
            throw IllegalStateException("락 획득 실패: $key")
        }

        try {
            return joinPoint.proceed()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    private fun resolveKey(joinPoint: ProceedingJoinPoint, keyExpression: String): String {
        if (!keyExpression.startsWith("#")) return keyExpression

        val method = (joinPoint.signature as MethodSignature).method
        val paramNames = method.parameters.map { it.name }
        val args = joinPoint.args

        val context = StandardEvaluationContext()
        paramNames.forEachIndexed { index, name ->
            context.setVariable(name, args[index])
        }

        return parser.parseExpression(keyExpression).getValue(context, String::class.java)
            ?: throw IllegalArgumentException("락 키 표현식 오류: $keyExpression")
    }
}

// 사용 예시
@Service
class ProductService(...) {

    @DistributedLock(key = "#productId", waitTime = 3, leaseTime = 10)
    @Transactional
    fun decreaseStock(productId: Long, quantity: Int) {
        val product = productRepository.findById(productId).orElseThrow()
        check(product.stockQuantity >= quantity) { "재고 부족" }
        product.stockQuantity -= quantity
    }
}
```

---

### Pub/Sub

#### 개념

```
Redis Pub/Sub:
- Publisher가 채널에 메시지 발행
- Subscriber는 채널을 구독하여 메시지 수신
- 메시지는 저장되지 않음 (구독 전 메시지는 수신 불가)
- 실시간 알림, 캐시 무효화 이벤트 등에 적합

Kafka와 차이:
- Redis Pub/Sub: 간단, 메시지 저장 없음, 소규모 브로드캐스트
- Kafka: 메시지 저장, 재처리 가능, 대용량 이벤트 스트리밍
```

#### 설정 및 구현

```kotlin
// Publisher
@Service
class NotificationPublisher(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>
) {
    suspend fun publishOrderEvent(event: OrderEvent) {
        val channel = "notifications:orders"
        reactiveRedisTemplate.convertAndSend(channel, event).awaitSingle()
    }
}

// Subscriber (Reactive)
@Service
class NotificationSubscriber(
    private val reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory,
    private val objectMapper: ObjectMapper,
) {
    private val notificationFlow = MutableSharedFlow<OrderEvent>()
    val events: SharedFlow<OrderEvent> = notificationFlow

    @PostConstruct
    fun subscribe() {
        val container = ReactiveRedisMessageListenerContainer(reactiveRedisConnectionFactory)

        container.receive(ChannelTopic.of("notifications:orders"))
            .map { message ->
                objectMapper.readValue(message.message, OrderEvent::class.java)
            }
            .onEach { event -> notificationFlow.emit(event) }
            .catch { e -> println("구독 오류: ${e.message}") }
            .launchIn(CoroutineScope(Dispatchers.IO))
    }
}

data class OrderEvent(
    val orderId: Long,
    val memberId: Long,
    val eventType: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

// SSE로 실시간 전송 예시
@RestController
class NotificationController(
    private val subscriber: NotificationSubscriber
) {
    @GetMapping("/sse/notifications", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamNotifications(@RequestParam memberId: Long): Flow<ServerSentEvent<OrderEvent>> {
        return subscriber.events
            .filter { it.memberId == memberId }
            .map { event -> ServerSentEvent.builder(event).build() }
    }
}
```

---

### 자주 만나는 문제

#### 캐시 스탬피드 (Cache Stampede)

```
문제: 인기 캐시 키가 만료되는 순간 수백 개의 요청이 동시에 DB 조회
→ DB 과부하 발생

해결: 캐시 재구축 시 분산 락으로 한 요청만 DB 조회하게 제한
```

```kotlin
@Service
class ProductCacheService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    private val redissonClient: RedissonClient,
    private val productRepository: ProductRepository,
) {
    suspend fun findByIdWithLock(id: Long): ProductDto {
        val cacheKey = "cache:product:$id"
        val lockKey = "rebuild-lock:product:$id"

        // 1. 캐시 조회
        val cached = redisTemplate.opsForValue().get(cacheKey).awaitFirstOrNull()
        if (cached != null) return cached as ProductDto

        // 2. 락 획득 후 캐시 재구축
        val lock = redissonClient.getLock(lockKey)
        val acquired = lock.tryLock(1, 5, TimeUnit.SECONDS)

        if (!acquired) {
            // 락 못 얻으면 잠깐 대기 후 캐시 재시도 (다른 스레드가 재구축 중)
            delay(100)
            return redisTemplate.opsForValue().get(cacheKey).awaitFirstOrNull() as? ProductDto
                ?: findByIdWithLock(id)
        }

        try {
            // 다시 한번 캐시 확인 (Double-checked locking)
            val cachedAgain = redisTemplate.opsForValue().get(cacheKey).awaitFirstOrNull()
            if (cachedAgain != null) return cachedAgain as ProductDto

            val dto = productRepository.findById(id)
                .map { ProductDto.from(it) }
                .orElseThrow { NoSuchElementException("Not found") }

            redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofMinutes(30)).awaitSingle()
            return dto
        } finally {
            if (lock.isHeldByCurrentThread) lock.unlock()
        }
    }
}
```

#### 캐시 일관성 문제

```kotlin
// 패턴: DB 업데이트 후 캐시 삭제 (Cache Invalidation)
// Write-Through보다 Delete 전략이 더 안전

@Transactional
suspend fun updateProduct(id: Long, request: UpdateProductRequest): ProductDto {
    val product = productRepository.findById(id) ?: throw NoSuchElementException()
    val updated = productRepository.save(product.copy(name = request.name, price = request.price))

    // DB 커밋 후 캐시 삭제
    // @TransactionalEventListener(phase = AFTER_COMMIT)를 활용하면 더 안전
    redisTemplate.delete("cache:product:$id").awaitSingle()

    return ProductDto.from(updated)
}
```

#### 메모리 관리

```
docker-compose.yml에서 Redis 메모리 정책 설정:
command: redis-server --maxmemory 512mb --maxmemory-policy allkeys-lru

주요 정책:
- allkeys-lru: 모든 키 중 LRU 방식으로 삭제 (캐시 전용 Redis 권장)
- volatile-lru: TTL 있는 키 중 LRU 방식으로 삭제
- noeviction: 메모리 초과 시 쓰기 오류 (데이터 보존 중요 시)
```

#### Serialization 오류 해결

```kotlin
// 문제: Kotlin data class가 역직렬화 안 될 때
// 원인: ObjectMapper에 KotlinModule 미등록

// 해결: RedisConfig의 ObjectMapper에 반드시 추가
private fun objectMapper(): ObjectMapper = ObjectMapper().apply {
    registerModule(KotlinModule.Builder().build())  // 필수!
    // ...
}

// GenericJackson2JsonRedisSerializer 사용 시 @class 필드가 JSON에 포함됨
// 클래스 경로(패키지) 변경 시 기존 캐시 역직렬화 실패
// → 클래스 이동/패키지 변경 시 해당 캐시 전체 초기화 필요
```

---

## 학습 체크리스트

### JPA

- [ ] Entity 클래스에 `open` 또는 `allOpen` 플러그인 적용 이유를 설명할 수 있다
- [ ] `data class`를 JPA Entity로 사용하면 안 되는 이유를 설명할 수 있다
- [ ] `FetchType.LAZY`와 `EAGER`의 차이와 각 선택 기준을 설명할 수 있다
- [ ] N+1 문제가 발생하는 상황을 코드로 보여주고 해결책 3가지를 적용할 수 있다
- [ ] `@Query` JPQL과 Native SQL의 차이를 설명하고 작성할 수 있다
- [ ] `Pageable`을 사용한 페이징 처리를 구현할 수 있다
- [ ] `@Transactional(readOnly = true)`가 성능에 미치는 영향을 설명할 수 있다

### R2DBC / Komapper

- [ ] R2DBC Entity에서 JPA 어노테이션 대신 사용하는 어노테이션을 설명할 수 있다
- [ ] `CoroutineCrudRepository`에서 `suspend` 함수와 `Flow`의 차이를 설명할 수 있다
- [ ] R2DBC에 연관 관계가 없어서 직접 JOIN 처리하는 코드를 작성할 수 있다
- [ ] Komapper 쿼리 DSL로 복합 조건 검색 쿼리를 작성할 수 있다
- [ ] JPA와 R2DBC에서 트랜잭션 처리 방식의 차이를 설명할 수 있다

### Aurora

- [ ] Aurora와 일반 RDS의 차이를 고가용성 관점에서 설명할 수 있다
- [ ] Writer 엔드포인트와 Reader 엔드포인트의 역할을 설명할 수 있다
- [ ] `AbstractRoutingDataSource`로 읽기/쓰기 분리를 구현할 수 있다
- [ ] Failover 시 애플리케이션이 재연결하는 설정을 구성할 수 있다

### Supabase

- [ ] Supabase와 Firebase의 차이를 설명할 수 있다
- [ ] PgBouncer Transaction mode에서 Prepared Statement를 비활성화하는 이유를 설명할 수 있다
- [ ] Row Level Security(RLS)가 Spring Boot 서버 연결에 미치는 영향을 설명할 수 있다
- [ ] Supabase Auth JWT를 Spring Security 필터에서 검증하는 코드를 작성할 수 있다

### Redis

- [ ] Redis의 주요 데이터 구조와 각 용도를 설명할 수 있다
- [ ] `@Cacheable`과 Cache-Aside 패턴의 차이를 설명하고 상황에 맞게 선택할 수 있다
- [ ] 분산 락이 필요한 상황을 설명하고 Redisson으로 구현할 수 있다
- [ ] 커스텀 `@DistributedLock` 어노테이션을 AOP로 구현할 수 있다
- [ ] 캐시 스탬피드 문제를 인지하고 해결책을 적용할 수 있다
- [ ] Redis Pub/Sub으로 실시간 알림을 구현할 수 있다

### Flyway

- [ ] 마이그레이션 파일 네이밍 규칙 (`V1__description.sql`)을 설명할 수 있다
- [ ] R2DBC 환경에서 Flyway를 위한 별도 JDBC DataSource 설정을 구성할 수 있다
- [ ] 이미 배포된 마이그레이션 파일을 수정하면 안 되는 이유를 설명할 수 있다
- [ ] 마이그레이션 실수 시 새 버전으로 수정하는 방법을 적용할 수 있다

---

## ORM 선택 최종 요약

```
Spring MVC 프로젝트
└── 복잡한 도메인 모델 → JPA
└── 단순 CRUD → JPA 또는 직접 JDBC

Spring WebFlux 프로젝트
└── 타입 안전 DSL 원함 → Komapper
└── 심플한 쿼리 위주 → R2DBC
└── 복잡한 연관 관계 불가피 → JPA + Schedulers.boundedElastic()

DB 선택
└── JSON 쿼리, UUID, 복잡한 쿼리 → PostgreSQL (Supabase 포함)
└── 고성능 단순 쿼리, Aurora 사용 → MySQL

캐싱 전략
└── Spring MVC + 간단한 캐시 → @Cacheable
└── WebFlux / 세밀한 제어 → Cache-Aside (ReactiveRedisTemplate)
└── 분산 환경 동시성 제어 → Redisson 분산 락
```

# 11. 배포 및 운영 인프라 가이드

> Spring Boot + Kotlin 애플리케이션을 프로덕션에 배포하고 운영하기 위한 종합 가이드입니다.
> Swagger, Actuator, Docker, CI/CD, Multi-module 구조까지 실무에서 바로 사용할 수 있는 설정을 다룹니다.

---

## 목차

1. [Swagger / OpenAPI 3.0](#1-swagger--openapi-30)
2. [Spring Boot Actuator + Micrometer + Prometheus](#2-spring-boot-actuator--micrometer--prometheus)
3. [Dockerfile (Multi-stage Build)](#3-dockerfile-multi-stage-build)
4. [Docker Compose (Full Stack)](#4-docker-compose-full-stack)
5. [환경 변수 및 시크릿 관리](#5-환경-변수-및-시크릿-관리)
6. [GitHub Actions CI/CD](#6-github-actions-cicd)
7. [Multi-Module Gradle 프로젝트](#7-multi-module-gradle-프로젝트)
8. [실무 운영 팁](#8-실무-운영-팁)
9. [체크리스트](#체크리스트)

---

## 1. Swagger / OpenAPI 3.0

### 왜 Swagger인가?

API 문서를 코드에서 자동으로 생성하고, 브라우저에서 직접 테스트할 수 있게 해주는 도구입니다.
`springdoc-openapi`는 Spring Boot 3.x와 완벽하게 호환되는 라이브러리입니다.

### 의존성 추가

```kotlin
// build.gradle.kts
dependencies {
    // Spring MVC 기반
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // WebFlux(Reactive) 기반이라면 아래 사용
    // implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
}
```

### application.yml 설정

```yaml
springdoc:
  api-docs:
    path: /api-docs           # OpenAPI JSON 스펙 경로
    enabled: true
  swagger-ui:
    path: /swagger-ui.html    # Swagger UI 접근 경로
    enabled: true
    tags-sorter: alpha         # 태그 알파벳 정렬
    operations-sorter: alpha   # 오퍼레이션 알파벳 정렬
    display-request-duration: true
    try-it-out-enabled: true   # UI에서 직접 API 호출 활성화
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
  # 특정 패키지만 스캔
  packages-to-scan: com.example.api.controller
  # 특정 경로만 포함
  paths-to-match: /api/**
```

### OpenAPI 전역 설정 클래스

```kotlin
package com.example.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "My API",
        version = "v1.0",
        description = "Spring Boot + Kotlin REST API",
        contact = Contact(
            name = "Dev Team",
            email = "dev@example.com"
        ),
        license = License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    ),
    servers = [
        Server(url = "http://localhost:8080", description = "Local"),
        Server(url = "https://api.example.com", description = "Production")
    ]
)
@SecurityScheme(
    name = "bearerAuth",                        // 스킴 이름 (컨트롤러에서 참조)
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT Bearer 토큰을 입력하세요. 예: Bearer eyJhbGci..."
)
@Configuration
class OpenApiConfig
```

### 컨트롤러에서 Swagger 어노테이션 사용

```kotlin
package com.example.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @Operation(
        summary = "사용자 목록 조회",
        description = "페이지네이션을 지원하는 사용자 목록 조회 API",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = UserPageResponse::class))]
        ),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @GetMapping
    fun getUsers(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<UserPageResponse> {
        return ResponseEntity.ok(userService.getUsers(page, size))
    }

    @Operation(
        summary = "사용자 단건 조회",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "사용자 없음")
    @GetMapping("/{id}")
    fun getUser(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUser(id))
    }

    @Operation(
        summary = "사용자 생성",
        description = "새로운 사용자를 생성합니다."
    )
    @PostMapping
    fun createUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "사용자 생성 요청",
            required = true,
            content = [Content(schema = Schema(implementation = CreateUserRequest::class))]
        )
        @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.createUser(request))
    }
}
```

### OperationCustomizer로 전역 인증 헤더 추가

```kotlin
package com.example.config

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod

@Component
class GlobalHeaderOperationCustomizer : OperationCustomizer {

    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        // 모든 API에 X-Request-ID 헤더 추가
        operation.addParametersItem(
            Parameter()
                .`in`("header")
                .name("X-Request-ID")
                .description("요청 추적용 UUID (선택사항)")
                .required(false)
                .schema(StringSchema())
                .example("550e8400-e29b-41d4-a716-446655440000")
        )
        return operation
    }
}
```

### API 그룹 분리 (GroupedOpenApi)

```kotlin
package com.example.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerGroupConfig {

    @Bean
    fun publicApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("public")
        .displayName("Public API")
        .pathsToMatch("/api/v1/auth/**", "/api/v1/products/**")
        .build()

    @Bean
    fun adminApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("admin")
        .displayName("Admin API")
        .pathsToMatch("/api/v1/admin/**")
        .build()

    @Bean
    fun internalApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("internal")
        .displayName("Internal API")
        .pathsToMatch("/internal/**")
        .build()
}
```

### 프로덕션 환경에서 Swagger 비활성화

```yaml
# application-prod.yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

```kotlin
// 또는 조건부 빈 등록으로 제어
@Configuration
@Profile("!prod")  // prod 프로파일이 아닐 때만 활성화
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("My API")
                .version("1.0")
        )
}
```

---

## 2. Spring Boot Actuator + Micrometer + Prometheus

### 왜 필요한가?

프로덕션 환경에서 애플리케이션의 상태를 모니터링하고 지표를 수집하기 위한 필수 도구입니다.
Actuator는 헬스체크와 메트릭 엔드포인트를 제공하고, Micrometer는 다양한 모니터링 시스템과 연동합니다.

### 의존성 추가

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // 선택사항: Micrometer tracing (분산 추적)
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
}
```

### application.yml 설정

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers,threaddump,heapdump,env
        # 프로덕션에서는 필요한 것만 노출: health,info,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized   # 인증된 사용자에게만 상세 정보 표시
      show-components: always
      probes:
        enabled: true                  # Kubernetes liveness/readiness probe 활성화
    info:
      enabled: true
    prometheus:
      enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
  info:
    env:
      enabled: true
    build:
      enabled: true
    git:
      enabled: true
      mode: full
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active:local}
    export:
      prometheus:
        enabled: true
        step: 1m

# /actuator/info에 표시될 정보
info:
  app:
    name: My Spring Boot App
    version: '@project.version@'
    description: Production-ready Spring Boot application
  java:
    version: '@java.version@'
```

### Custom HealthIndicator

```kotlin
package com.example.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import javax.sql.DataSource

// 데이터베이스 커넥션 풀 상태 확인
@Component("database-pool")
class DatabasePoolHealthIndicator(
    private val dataSource: DataSource
) : HealthIndicator {

    override fun health(): Health {
        return try {
            dataSource.connection.use { conn ->
                val maxPool = 10 // 설정값
                val activeConnections = getActiveConnections()

                if (activeConnections > maxPool * 0.9) {
                    Health.down()
                        .withDetail("message", "Connection pool near capacity")
                        .withDetail("active", activeConnections)
                        .withDetail("max", maxPool)
                        .build()
                } else {
                    Health.up()
                        .withDetail("active", activeConnections)
                        .withDetail("max", maxPool)
                        .withDetail("database", conn.metaData.databaseProductName)
                        .build()
                }
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }

    private fun getActiveConnections(): Int = 0 // 실제 구현 필요
}

// 외부 API 의존성 상태 확인
@Component("external-payment-api")
class ExternalPaymentApiHealthIndicator(
    private val restTemplate: RestTemplate,
    @Value("\${payment.api.health-url}") private val healthUrl: String
) : HealthIndicator {

    override fun health(): Health {
        return try {
            val start = System.currentTimeMillis()
            val response = restTemplate.getForEntity(healthUrl, String::class.java)
            val elapsed = System.currentTimeMillis() - start

            if (response.statusCode.is2xxSuccessful) {
                Health.up()
                    .withDetail("responseTime", "${elapsed}ms")
                    .withDetail("url", healthUrl)
                    .build()
            } else {
                Health.down()
                    .withDetail("statusCode", response.statusCode.value())
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .build()
        }
    }
}
```

### Micrometer 메트릭 수집

```kotlin
package com.example.metrics

import io.micrometer.core.instrument.*
import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@Service
class OrderService(
    private val meterRegistry: MeterRegistry,
    private val orderRepository: OrderRepository
) {
    // Counter: 이벤트 발생 횟수 추적
    private val orderCreatedCounter: Counter = Counter.builder("orders.created")
        .description("생성된 주문 수")
        .tag("service", "order")
        .register(meterRegistry)

    private val orderFailedCounter: Counter = Counter.builder("orders.failed")
        .description("실패한 주문 수")
        .register(meterRegistry)

    // Gauge: 현재 값 추적 (큐 크기, 현재 활성 세션 수 등)
    private val pendingOrders = AtomicInteger(0)

    init {
        Gauge.builder("orders.pending", pendingOrders, AtomicInteger::get)
            .description("처리 대기 중인 주문 수")
            .register(meterRegistry)
    }

    // Timer: 작업 소요 시간 측정
    @Timed(value = "orders.processing.time", description = "주문 처리 시간", percentiles = [0.5, 0.95, 0.99])
    fun processOrder(orderId: Long): OrderResult {
        pendingOrders.incrementAndGet()
        return try {
            val result = doProcessOrder(orderId)
            orderCreatedCounter.increment()
            result
        } catch (e: Exception) {
            orderFailedCounter.increment()
            // 태그로 오류 유형 분류
            meterRegistry.counter(
                "orders.errors",
                "type", e::class.simpleName ?: "unknown"
            ).increment()
            throw e
        } finally {
            pendingOrders.decrementAndGet()
        }
    }

    // 수동 Timer 사용
    fun fetchOrderWithTiming(orderId: Long): Order {
        val timer = Timer.builder("orders.fetch.time")
            .description("주문 조회 시간")
            .tag("operation", "fetch")
            .register(meterRegistry)

        return timer.recordCallable {
            orderRepository.findById(orderId).orElseThrow()
        }!!
    }

    // Distribution Summary: 크기/금액 같은 분포 측정
    fun recordOrderAmount(amount: Double) {
        DistributionSummary.builder("orders.amount")
            .description("주문 금액 분포")
            .baseUnit("KRW")
            .publishPercentiles(0.5, 0.75, 0.95)
            .register(meterRegistry)
            .record(amount)
    }

    private fun doProcessOrder(orderId: Long): OrderResult = TODO()
}
```

### Actuator 보안 설정

```kotlin
package com.example.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class ActuatorSecurityConfig {

    @Bean
    fun actuatorSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests { auth ->
                // health, info는 누구나 접근 가능
                auth.requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                // prometheus는 모니터링 서버만 접근 (IP 제한 또는 별도 인증)
                auth.requestMatchers(EndpointRequest.to("prometheus")).hasRole("MONITORING")
                // 나머지는 ADMIN만
                auth.anyRequest().hasRole("ADMIN")
            }
        return http.build()
    }
}
```

### Prometheus + Grafana Docker Compose 스니펫

```yaml
# monitoring/docker-compose.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:v2.48.0
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    restart: unless-stopped

  grafana:
    image: grafana/grafana:10.2.0
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    depends_on:
      - prometheus
    restart: unless-stopped

volumes:
  prometheus-data:
  grafana-data:
```

```yaml
# monitoring/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']
    # 기본 인증이 필요한 경우
    # basic_auth:
    #   username: 'prometheus'
    #   password: 'secret'
```

---

## 3. Dockerfile (Multi-stage Build)

### 왜 Multi-stage Build인가?

단일 스테이지 빌드는 JDK, Gradle, 소스코드가 모두 이미지에 포함되어 이미지 크기가 1GB를 넘기도 합니다.
Multi-stage build는 빌드 결과물(JAR)만 최종 이미지에 포함하여 이미지 크기를 대폭 줄입니다.

### Dockerfile

```dockerfile
# ====== Stage 1: Build ======
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 의존성 캐싱 최적화: Gradle 설정 파일 먼저 복사
# 소스코드가 바뀌어도 의존성이 같으면 이 레이어는 캐시 사용
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Gradle Wrapper 실행 권한 부여
RUN chmod +x gradlew

# 의존성만 먼저 다운로드 (캐시 레이어 생성)
RUN ./gradlew dependencies --no-daemon --quiet

# 소스 코드 복사 후 빌드
COPY src/ src/

# 테스트 제외하고 빌드 (CI에서는 별도로 테스트)
RUN ./gradlew bootJar --no-daemon -x test

# JAR 파일 레이어 분리 (Spring Boot Layered JAR)
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted

# ====== Stage 2: Runtime ======
FROM eclipse-temurin:21-jre-alpine AS runtime

# 보안: non-root 사용자 생성
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 레이어드 JAR 복사 (변경 빈도 순서대로: 자주 안 바뀌는 것 먼저)
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

# 사용자 전환
USER appuser

# 포트 노출
EXPOSE 8080

# JVM 튜닝 옵션
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heap-dump.hprof \
    -Djava.security.egd=file:/dev/./urandom"

# Health check (컨테이너 자체 헬스체크)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Spring Boot Layered JAR 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

### .dockerignore

```dockerignore
# .dockerignore
.git
.gitignore
.gradle
build/
out/
*.md
*.log
.idea/
*.iml
*.iws
.DS_Store
Dockerfile*
docker-compose*
.env*
src/test/
```

### 환경 변수를 활용한 실행

```bash
# 이미지 빌드
docker build -t my-app:latest .

# 개발 환경 실행
docker run -d \
  --name my-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_HOST=localhost \
  -e DB_PORT=5432 \
  -e DB_NAME=mydb \
  -e DB_USERNAME=myuser \
  -e DB_PASSWORD=secret \
  -e JWT_SECRET=my-super-secret-key \
  --memory="512m" \
  --cpus="1.0" \
  my-app:latest
```

### Multi-module 프로젝트용 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 루트 및 모든 모듈의 Gradle 파일 복사
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY core/build.gradle.kts core/
COPY domain/build.gradle.kts domain/
COPY api/build.gradle.kts api/
COPY infra/build.gradle.kts infra/

RUN chmod +x gradlew && ./gradlew :api:dependencies --no-daemon --quiet

COPY . .

# :api 모듈만 빌드
RUN ./gradlew :api:bootJar --no-daemon -x test

RUN java -Djarmode=layertools -jar api/build/libs/*.jar extract --destination extracted

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
            "org.springframework.boot.loader.launch.JarLauncher"]
```

---

## 4. Docker Compose (Full Stack)

### 전체 스택 구성

```yaml
# docker-compose.yml
version: '3.8'

services:
  # ===== Spring Boot Application =====
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: my-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-local}
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=${DB_NAME:-mydb}
      - DB_USERNAME=${DB_USERNAME:-myuser}
      - DB_PASSWORD=${DB_PASSWORD:-secret}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      kafka:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - app-network
    volumes:
      - heap-dumps:/tmp

  # ===== PostgreSQL =====
  postgres:
    image: postgres:16-alpine
    container_name: postgres
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=${DB_NAME:-mydb}
      - POSTGRES_USER=${DB_USERNAME:-myuser}
      - POSTGRES_PASSWORD=${DB_PASSWORD:-secret}
      - PGDATA=/var/lib/postgresql/data/pgdata
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./scripts/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME:-myuser} -d ${DB_NAME:-mydb}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    restart: unless-stopped
    networks:
      - app-network

  # ===== Redis =====
  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD:-redissecret} --maxmemory 256mb --maxmemory-policy allkeys-lru
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD:-redissecret}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - app-network

  # ===== Zookeeper (Kafka 의존) =====
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: zookeeper
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
      - ZOOKEEPER_TICK_TIME=2000
    volumes:
      - zookeeper-data:/var/lib/zookeeper/data
      - zookeeper-logs:/var/lib/zookeeper/log
    healthcheck:
      test: ["CMD-SHELL", "echo ruok | nc localhost 2181 | grep imok"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - app-network

  # ===== Kafka =====
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092
      - KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      - KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
      - KAFKA_AUTO_CREATE_TOPICS_ENABLE=true
    volumes:
      - kafka-data:/var/lib/kafka/data
    depends_on:
      zookeeper:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "kafka-topics --bootstrap-server localhost:9092 --list"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 30s
    restart: unless-stopped
    networks:
      - app-network

  # ===== Prometheus =====
  prometheus:
    image: prom/prometheus:v2.48.0
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.retention.time=15d'
    depends_on:
      - app
    restart: unless-stopped
    networks:
      - app-network

  # ===== Grafana =====
  grafana:
    image: grafana/grafana:10.2.0
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=${GRAFANA_USER:-admin}
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:-admin123}
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
    depends_on:
      - prometheus
    restart: unless-stopped
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  postgres-data:
  redis-data:
  kafka-data:
  zookeeper-data:
  zookeeper-logs:
  prometheus-data:
  grafana-data:
  heap-dumps:
```

### .env 파일

```dotenv
# .env (절대 Git에 커밋하지 말 것!)
SPRING_PROFILES_ACTIVE=local

# Database
DB_NAME=mydb
DB_USERNAME=myuser
DB_PASSWORD=SuperSecret123!

# Redis
REDIS_PASSWORD=RedisSecret456!

# JWT
JWT_SECRET=my-very-long-secret-key-for-jwt-signing-at-least-256-bits

# Grafana
GRAFANA_USER=admin
GRAFANA_PASSWORD=GrafanaAdmin789!
```

```dotenv
# .env.example (Git에 커밋하는 예시 파일)
SPRING_PROFILES_ACTIVE=local
DB_NAME=mydb
DB_USERNAME=myuser
DB_PASSWORD=change_me
REDIS_PASSWORD=change_me
JWT_SECRET=change_me_with_a_long_random_string
GRAFANA_USER=admin
GRAFANA_PASSWORD=change_me
```

### docker-compose.override.yml (로컬 개발 환경 오버라이드)

```yaml
# docker-compose.override.yml
# docker-compose up 실행 시 자동으로 적용됨
version: '3.8'

services:
  app:
    environment:
      - SPRING_PROFILES_ACTIVE=local
      - SPRING_DEVTOOLS_RESTART_ENABLED=true
      - LOGGING_LEVEL_COM_EXAMPLE=DEBUG
    volumes:
      - ./src:/app/src
      - ~/.gradle:/root/.gradle  # Gradle 캐시 재사용
    ports:
      - "5005:5005"  # 원격 디버깅 포트
    command: ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
              "org.springframework.boot.loader.launch.JarLauncher"]

  postgres:
    ports:
      - "5432:5432"  # 로컬에서 직접 DB 접근 허용

  kafka:
    environment:
      - KAFKA_LOG_RETENTION_HOURS=1  # 로컬에서는 짧은 보존 기간
```

### 프로파일별 Compose 파일

```yaml
# docker-compose.staging.yml
version: '3.8'
services:
  app:
    image: my-ecr-registry.amazonaws.com/my-app:${IMAGE_TAG:-latest}
    environment:
      - SPRING_PROFILES_ACTIVE=staging
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 1g
          cpus: '1.0'
```

### 유용한 Docker Compose 명령어

```bash
# 전체 스택 시작 (백그라운드)
docker-compose up -d

# 특정 서비스만 시작
docker-compose up -d postgres redis

# 로그 확인 (실시간)
docker-compose logs -f app

# 특정 서비스 재시작
docker-compose restart app

# 컨테이너 내부 접속
docker-compose exec app sh
docker-compose exec postgres psql -U myuser -d mydb

# 스케일 조정
docker-compose up -d --scale app=3

# 오버라이드 파일과 함께 실행
docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d

# 전체 정리 (볼륨 포함)
docker-compose down -v

# 이미지 재빌드 후 시작
docker-compose up -d --build app

# 실행 중인 서비스 상태 확인
docker-compose ps

# 서비스 리소스 사용량 확인
docker stats
```

---

## 5. 환경 변수 및 시크릿 관리

### 핵심 원칙: 절대 시크릿을 코드에 하드코딩하지 말 것

```kotlin
// BAD - 절대 이렇게 하지 말 것
val jwtSecret = "hardcoded-secret-key-in-source-code"
val dbPassword = "mypassword123"

// GOOD - 환경 변수에서 읽기
@Value("\${jwt.secret}")
lateinit var jwtSecret: String
```

### application.yml 환경 변수 패턴

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:mydb}
    username: ${DB_USERNAME:myuser}
    password: ${DB_PASSWORD}          # 기본값 없음 = 필수값
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:10}
      minimum-idle: ${DB_POOL_MIN_IDLE:5}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}    # 비어있을 수도 있는 값

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

jwt:
  secret: ${JWT_SECRET}              # 필수값 - 없으면 시작 실패
  expiration: ${JWT_EXPIRATION:86400000}  # 기본값 24시간

external:
  payment:
    api-key: ${PAYMENT_API_KEY}
    base-url: ${PAYMENT_BASE_URL:https://api.payment.com}
```

### 프로파일별 설정 파일 구조

```
src/main/resources/
├── application.yml              # 공통 설정
├── application-local.yml        # 로컬 개발 환경
├── application-dev.yml          # 개발 서버
├── application-staging.yml      # 스테이징 서버
└── application-prod.yml         # 프로덕션 서버
```

```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb_local
    username: localuser
    password: localpass

logging:
  level:
    com.example: DEBUG
    org.springframework.security: DEBUG

# Swagger 활성화 (로컬에서만)
springdoc:
  swagger-ui:
    enabled: true
```

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    hikari:
      maximum-pool-size: 50

logging:
  level:
    root: WARN
    com.example: INFO

# Swagger 비활성화
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
```

### Spring Cloud Config (개요)

대규모 MSA 환경에서 설정을 중앙화하는 방법입니다.

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config")
}
```

```yaml
# bootstrap.yml
spring:
  config:
    import: optional:configserver:http://config-server:8888
  cloud:
    config:
      uri: http://config-server:8888
      label: main          # Git 브랜치
      profile: ${spring.profiles.active}
      fail-fast: true      # Config Server 없으면 시작 실패
      retry:
        max-attempts: 6
        initial-interval: 1000
```

### AWS Secrets Manager 연동

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.awspring.cloud:spring-cloud-aws-starter-secrets-manager:3.1.0")
}
```

```yaml
# application.yml
spring:
  config:
    import: aws-secretsmanager:/myapp/prod/secrets

aws:
  secretsmanager:
    region: ap-northeast-2
    # EC2/ECS에서는 IAM Role로 자동 인증됨
```

```kotlin
// AWS Secrets Manager에 저장된 시크릿 사용 예시
// Secret name: /myapp/prod/secrets
// Secret value: {"db-password": "actual-secret", "jwt-secret": "actual-jwt-secret"}

@Configuration
class AwsSecretsConfig(
    @Value("\${db-password}") private val dbPassword: String,
    @Value("\${jwt-secret}") private val jwtSecret: String
)
```

### AWS Parameter Store 연동

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store:3.1.0")
}
```

```yaml
# application.yml
spring:
  config:
    import: aws-parameterstore:/myapp/prod/

aws:
  paramstore:
    prefix: /myapp
    profile-separator: _
    fail-fast: true
```

### HashiCorp Vault 연동 (개요)

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config")
}
```

```yaml
# application.yml
spring:
  cloud:
    vault:
      uri: https://vault.example.com:8200
      token: ${VAULT_TOKEN}              # 또는 Kubernetes/AWS 인증 사용
      kv:
        enabled: true
        backend: secret
        application-name: my-app
      generic:
        enabled: false
  config:
    import: vault://
```

---

## 6. GitHub Actions CI/CD

### CI 파이프라인 (테스트 + 빌드 + Docker Push)

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

env:
  JAVA_VERSION: '21'
  IMAGE_NAME: my-app

jobs:
  # ===== 테스트 =====
  test:
    name: Test
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      # Gradle 의존성 캐싱 (CI 속도 대폭 향상)
      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      # TestContainers는 GitHub Actions 환경에서 Docker가 기본 제공됨
      - name: Run tests
        run: ./gradlew test --no-daemon
        env:
          TESTCONTAINERS_RYUK_DISABLED: true

      - name: Publish test results
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Test Results
          path: '**/build/test-results/**/*.xml'
          reporter: java-junit

      - name: Upload coverage report
        uses: codecov/codecov-action@v3
        with:
          files: build/reports/jacoco/test/jacocoTestReport.xml

  # ===== 빌드 =====
  build:
    name: Build
    runs-on: ubuntu-latest
    needs: test

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}
          restore-keys: ${{ runner.os }}-gradle-

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew bootJar --no-daemon -x test

      - name: Upload build artifact
        uses: actions/upload-artifact@v3
        with:
          name: app-jar
          path: build/libs/*.jar
          retention-days: 1

  # ===== Docker 이미지 빌드 및 푸시 =====
  docker-build:
    name: Docker Build & Push
    runs-on: ubuntu-latest
    needs: build
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop'

    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      # AWS ECR 로그인 (Docker Hub 대신 사용 시)
      # - name: Configure AWS credentials
      #   uses: aws-actions/configure-aws-credentials@v4
      #   with:
      #     aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
      #     aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
      #     aws-region: ap-northeast-2
      # - name: Login to Amazon ECR
      #   id: login-ecr
      #   uses: aws-actions/amazon-ecr-login@v2

      - name: Extract metadata for Docker
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}
          tags: |
            type=sha,prefix=sha-
            type=ref,event=branch
            type=semver,pattern={{version}}
            type=raw,value=latest,enable=${{ github.ref == 'refs/heads/main' }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha           # GitHub Actions 캐시 활용
          cache-to: type=gha,mode=max
          build-args: |
            BUILD_DATE=${{ github.event.repository.updated_at }}
            VCS_REF=${{ github.sha }}

  # ===== 다중 JDK 버전 매트릭스 빌드 =====
  matrix-test:
    name: Test on JDK ${{ matrix.java }}
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java: [ '17', '21' ]
      fail-fast: false  # 하나 실패해도 나머지 계속 실행

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ matrix.java }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java }}
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-java${{ matrix.java }}-${{ hashFiles('**/*.gradle.kts') }}

      - name: Run tests
        run: chmod +x gradlew && ./gradlew test --no-daemon
```

### CD 파이프라인 (배포)

```yaml
# .github/workflows/cd.yml
name: CD Pipeline

on:
  workflow_run:
    workflows: ["CI Pipeline"]
    types: [completed]
    branches: [main]
  # 수동 트리거도 지원
  workflow_dispatch:
    inputs:
      environment:
        description: 'Deployment environment'
        required: true
        default: 'staging'
        type: choice
        options: [staging, production]
      image_tag:
        description: 'Docker image tag'
        required: false
        default: 'latest'

env:
  AWS_REGION: ap-northeast-2
  ECR_REPOSITORY: my-app
  ECS_SERVICE: my-app-service
  ECS_CLUSTER: my-cluster
  CONTAINER_NAME: my-app

jobs:
  deploy-staging:
    name: Deploy to Staging
    runs-on: ubuntu-latest
    if: ${{ github.event.workflow_run.conclusion == 'success' || github.event_name == 'workflow_dispatch' }}
    environment: staging

    steps:
      - uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Get image tag
        id: image-tag
        run: |
          echo "IMAGE_TAG=sha-$(echo ${{ github.sha }} | head -c7)" >> $GITHUB_OUTPUT

      # ECS Task Definition 업데이트
      - name: Download task definition
        run: |
          aws ecs describe-task-definition \
            --task-definition ${{ env.ECS_SERVICE }} \
            --query taskDefinition \
            > task-definition.json

      - name: Update ECS task definition with new image
        id: task-def
        uses: aws-actions/amazon-ecs-render-task-definition@v1
        with:
          task-definition: task-definition.json
          container-name: ${{ env.CONTAINER_NAME }}
          image: ${{ steps.login-ecr.outputs.registry }}/${{ env.ECR_REPOSITORY }}:${{ steps.image-tag.outputs.IMAGE_TAG }}

      # ECS 서비스 업데이트 (Rolling Update)
      - name: Deploy to ECS
        uses: aws-actions/amazon-ecs-deploy-task-definition@v1
        with:
          task-definition: ${{ steps.task-def.outputs.task-definition }}
          service: ${{ env.ECS_SERVICE }}
          cluster: ${{ env.ECS_CLUSTER }}
          wait-for-service-stability: true

      # EC2 SSH 배포 방식 (대안)
      # - name: Deploy to EC2
      #   uses: appleboy/ssh-action@master
      #   with:
      #     host: ${{ secrets.EC2_HOST }}
      #     username: ec2-user
      #     key: ${{ secrets.EC2_SSH_PRIVATE_KEY }}
      #     script: |
      #       cd /app
      #       docker pull $IMAGE:$TAG
      #       docker-compose up -d --no-deps app
      #       docker system prune -f

      - name: Notify Slack on success
        if: success()
        uses: 8398a7/action-slack@v3
        with:
          status: success
          text: |
            *배포 성공* :rocket:
            환경: Staging
            이미지 태그: `${{ steps.image-tag.outputs.IMAGE_TAG }}`
            커밋: `${{ github.sha }}`
            배포자: ${{ github.actor }}
          webhook_url: ${{ secrets.SLACK_WEBHOOK_URL }}

      - name: Notify Slack on failure
        if: failure()
        uses: 8398a7/action-slack@v3
        with:
          status: failure
          text: |
            *배포 실패* :x:
            환경: Staging
            커밋: `${{ github.sha }}`
            워크플로: ${{ github.workflow }}
            담당자: @channel 확인 요망
          webhook_url: ${{ secrets.SLACK_WEBHOOK_URL }}

  deploy-production:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: deploy-staging
    environment:
      name: production
      url: https://api.example.com
    # GitHub Environment Protection Rules로 수동 승인 설정 가능

    steps:
      - uses: actions/checkout@v4

      - name: Configure AWS credentials (Production)
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID_PROD }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY_PROD }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Deploy to Production ECS
        run: echo "Deploying to production ECS cluster..."

      - name: Run smoke tests
        run: |
          # 배포 후 기본 API 동작 확인
          curl -f https://api.example.com/actuator/health
          curl -f https://api.example.com/api/v1/ping

      - name: Notify Slack on production deploy
        if: always()
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          text: |
            *프로덕션 배포 ${{ job.status }}*
            태그: `${{ github.sha }}`
          webhook_url: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### GitHub Actions Secrets 관리

```
Repository Settings > Secrets and Variables > Actions에서 설정:

필수 Secrets:
  DOCKERHUB_USERNAME          - Docker Hub 사용자명
  DOCKERHUB_TOKEN             - Docker Hub 액세스 토큰 (비밀번호 아님)
  AWS_ACCESS_KEY_ID           - AWS IAM 액세스 키 ID
  AWS_SECRET_ACCESS_KEY       - AWS IAM 시크릿 액세스 키
  AWS_ACCESS_KEY_ID_PROD      - 프로덕션 전용 AWS 키
  AWS_SECRET_ACCESS_KEY_PROD  - 프로덕션 전용 AWS 시크릿
  EC2_SSH_PRIVATE_KEY         - EC2 SSH 개인키 (PEM 내용 전체)
  EC2_HOST                    - EC2 퍼블릭 IP 또는 도메인
  SLACK_WEBHOOK_URL           - Slack Incoming Webhook URL
```

```yaml
# Secrets 사용 예시
- name: Use secret in step
  run: echo "Configured with secret"
  env:
    MY_ENV_VAR: ${{ secrets.MY_SECRET }}

# 여러 환경에 다른 시크릿 사용
- name: Deploy with environment-specific secret
  env:
    DB_PASSWORD: ${{ secrets[format('DB_PASSWORD_{0}', matrix.environment)] }}
```

---

## 7. Multi-Module Gradle 프로젝트

### 왜 멀티 모듈인가?

| 단일 모듈 | 멀티 모듈 |
|-----------|-----------|
| 빠른 초기 개발 | 명확한 도메인 경계 |
| 모든 코드가 한 곳 | 순환 의존성 방지 |
| 의존성 관리 어려움 | 독립적인 빌드/테스트 |
| 팀 협업 시 충돌 | 팀별 독립적 개발 |
| 빌드 캐시 효율 낮음 | Gradle 증분 빌드 최적화 |

### 프로젝트 구조

```
my-project/
├── settings.gradle.kts
├── build.gradle.kts          # 루트 빌드 파일
├── gradle/
│   └── libs.versions.toml    # Version Catalog
├── core/                     # 공유 코드 (DTO, 예외, 유틸)
│   ├── build.gradle.kts
│   └── src/
├── domain/                   # 엔티티, 레포지토리, 비즈니스 로직
│   ├── build.gradle.kts
│   └── src/
├── api/                      # 컨트롤러, Spring Boot 앱
│   ├── build.gradle.kts
│   └── src/
├── batch/                    # Spring Batch 작업
│   ├── build.gradle.kts
│   └── src/
└── infra/                    # 외부 연동 (kafka, redis, grpc clients)
    ├── build.gradle.kts
    └── src/
```

### settings.gradle.kts

```kotlin
// settings.gradle.kts
rootProject.name = "my-project"

// 모든 서브모듈 등록
include(
    ":core",
    ":domain",
    ":api",
    ":batch",
    ":infra"
)

// 모듈 위치 커스텀 지정 (기본값과 다를 경우)
// project(":core").projectDir = file("modules/core")

// Gradle Version Catalog 활성화
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}
```

### gradle/libs.versions.toml (Version Catalog)

```toml
# gradle/libs.versions.toml
[versions]
kotlin = "1.9.22"
spring-boot = "3.2.2"
spring-dependency-management = "1.1.4"
springdoc = "2.3.0"
testcontainers = "1.19.3"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-boot-starter-security = { module = "org.springframework.boot:spring-boot-starter-security" }
spring-boot-starter-actuator = { module = "org.springframework.boot:spring-boot-starter-actuator" }
spring-boot-starter-test = { module = "org.springframework.boot:spring-boot-starter-test" }
springdoc-openapi-webmvc = { module = "org.springdoc:springdoc-openapi-starter-webmvc-ui", version.ref = "springdoc" }
micrometer-prometheus = { module = "io.micrometer:micrometer-registry-prometheus" }
testcontainers-junit = { module = "org.testcontainers:junit-jupiter", version.ref = "testcontainers" }
testcontainers-postgresql = { module = "org.testcontainers:postgresql", version.ref = "testcontainers" }
testcontainers-kafka = { module = "org.testcontainers:kafka", version.ref = "testcontainers" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
kotlin-jpa = { id = "org.jetbrains.kotlin.plugin.jpa", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "spring-dependency-management" }
```

### 루트 build.gradle.kts

```kotlin
// build.gradle.kts (루트)
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

// 모든 서브프로젝트에 공통 설정 적용
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>()
        .imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.2")
        }

    dependencies {
        // 모든 모듈에서 공통으로 사용
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("io.mockk:mockk:1.13.8")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-XX:+EnableDynamicAgentLoading")  // Java 21 경고 억제
    }

    apply(plugin = "jacoco")
    tasks.named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

// Spring Boot 실행 가능 JAR가 필요한 모듈
configure(subprojects.filter { it.name in listOf("api", "batch") }) {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

// 라이브러리 모듈 (bootJar 불필요)
configure(subprojects.filter { it.name in listOf("core", "domain", "infra") }) {
    tasks.named("jar") { enabled = true }
}
```

### 각 모듈의 build.gradle.kts

```kotlin
// core/build.gradle.kts
// 공유 DTO, 예외, 유틸리티 - Spring Boot 불필요
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("jakarta.validation:jakarta.validation-api")
}
```

```kotlin
// domain/build.gradle.kts
// 엔티티, 레포지토리, 비즈니스 로직
apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

dependencies {
    implementation(project(":core"))                       // core 모듈 의존
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
}
```

```kotlin
// api/build.gradle.kts
// REST API 컨트롤러, Spring Boot 메인 앱
apply(plugin = "org.jetbrains.kotlin.plugin.spring")

dependencies {
    implementation(project(":core"))      // 공유 코드
    implementation(project(":domain"))    // 비즈니스 로직
    implementation(project(":infra"))     // 외부 연동

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

```kotlin
// infra/build.gradle.kts
// 외부 시스템 연동 (Kafka, Redis, gRPC 클라이언트 등)
dependencies {
    implementation(project(":core"))      // DTO 등 공유 코드 사용

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.grpc:grpc-stub:1.60.0")

    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}
```

```kotlin
// batch/build.gradle.kts
// Spring Batch 작업
apply(plugin = "org.jetbrains.kotlin.plugin.spring")

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
}
```

### 모듈 간 의존성 구조

```
[의존성 방향: 아래 모듈이 위 모듈에 의존]

      core          (가장 하위 - 외부 의존 없음)
     /    \
  domain  infra    (core에만 의존)
     \    /
      api          (domain + infra + core에 의존)

  batch            (core + domain에 의존, api와 독립)
```

### 멀티 모듈 빌드 및 테스트 명령어

```bash
# 전체 빌드
./gradlew build

# 특정 모듈만 빌드
./gradlew :api:bootJar
./gradlew :batch:bootJar

# 특정 모듈만 테스트
./gradlew :domain:test
./gradlew :api:test --tests "com.example.controller.UserControllerTest"

# 특정 모듈 의존성 확인
./gradlew :api:dependencies

# 전체 테스트 + 커버리지 리포트
./gradlew test jacocoTestReport

# 변경된 모듈만 빌드 (Gradle 증분 빌드)
./gradlew :api:build --build-cache

# 병렬 빌드 (속도 향상)
./gradlew build --parallel

# 특정 모듈 실행
./gradlew :api:bootRun
./gradlew :batch:bootRun --args="--spring.batch.job.names=myJob"

# 특정 모듈만 빌드 아티팩트 생성
./gradlew :api:assemble
```

---

## 8. 실무 운영 팁

### Graceful Shutdown

서버를 갑자기 종료하면 진행 중인 요청이 유실됩니다. Graceful Shutdown은 현재 처리 중인 요청을 완료한 후 종료합니다.

```yaml
# application.yml
server:
  shutdown: graceful    # 기본값은 immediate

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # 최대 30초 대기 후 강제 종료
```

```kotlin
// 커스텀 Graceful Shutdown 처리
@Component
class GracefulShutdownHook(
    private val taskExecutor: ThreadPoolTaskExecutor
) : DisposableBean {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun destroy() {
        log.info("Graceful shutdown initiated. Waiting for active tasks to complete...")
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true)
        taskExecutor.setAwaitTerminationSeconds(30)
        taskExecutor.shutdown()
        log.info("Graceful shutdown complete.")
    }
}
```

### 컨테이너 환경 JVM 튜닝

```bash
# Dockerfile 또는 환경 변수로 JVM 옵션 설정
JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heap.hprof \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/./urandom"
```

| JVM 옵션 | 설명 |
|----------|------|
| `-XX:+UseContainerSupport` | 컨테이너 메모리 제한 자동 인식 (Java 10+) |
| `-XX:MaxRAMPercentage=75.0` | 컨테이너 메모리의 75%를 JVM 힙으로 사용 |
| `-XX:+UseG1GC` | G1 가비지 컬렉터 사용 (Java 9+) |
| `-XX:+HeapDumpOnOutOfMemoryError` | OOM 발생 시 힙 덤프 자동 생성 |
| `-XX:+ExitOnOutOfMemoryError` | OOM 시 프로세스 종료 (재시작 정책 활용) |

### GC 로깅 설정

```bash
# GC 로그 활성화 (성능 분석 및 튜닝에 필수)
JAVA_OPTS="${JAVA_OPTS} \
  -Xlog:gc*:file=/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10m \
  -Xlog:gc+heap=debug \
  -Xlog:safepoint"
```

### 비동기 스레드 풀 튜닝

```kotlin
@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["asyncTaskExecutor"])
    fun asyncTaskExecutor(): AsyncTaskExecutor = ThreadPoolTaskExecutor().apply {
        val cpuCores = Runtime.getRuntime().availableProcessors()

        // IO 바운드 작업: 코어 수의 2~4배
        corePoolSize = cpuCores * 2
        maxPoolSize = cpuCores * 4
        queueCapacity = 500
        threadNamePrefix = "async-"
        // 큐가 꽉 찼을 때 caller 스레드에서 직접 실행 (배압 제어)
        setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        setWaitForTasksToCompleteOnShutdown(true)
        setAwaitTerminationSeconds(60)
        initialize()
    }

    @Bean(name = ["kafkaTaskExecutor"])
    fun kafkaTaskExecutor(): AsyncTaskExecutor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 3
        maxPoolSize = 10
        queueCapacity = 100
        threadNamePrefix = "kafka-handler-"
        initialize()
    }
}
```

### Readiness vs Liveness Probe

```
Liveness Probe:  "애플리케이션이 살아있는가?"
                 실패 시 -> 컨테이너 재시작
                 Dead Lock, 무한루프 감지에 사용

Readiness Probe: "애플리케이션이 트래픽을 받을 준비가 되었는가?"
                 실패 시 -> 로드밸런서에서 제외 (재시작 안 함)
                 DB 연결 확인, 캐시 워밍업 완료 등에 사용
```

```yaml
# Kubernetes Deployment 설정 예시
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
        - name: my-app
          image: my-app:latest
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60     # 시작 후 60초 대기
            periodSeconds: 30           # 30초마다 확인
            failureThreshold: 3         # 3번 실패 시 재시작

          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 3
```

```yaml
# application.yml - Spring Boot Actuator 프로브 설정
management:
  endpoint:
    health:
      probes:
        enabled: true    # /actuator/health/liveness, /actuator/health/readiness 활성화
      group:
        readiness:
          include: readinessState,db,redis    # readiness에 DB, Redis 상태 포함
        liveness:
          include: livenessState              # liveness는 앱 상태만
```

### 무중단 배포 체크리스트

```
Zero-Downtime Deployment 체크리스트

배포 전:
  [ ] DB 마이그레이션은 하위 호환성 유지 (컬럼 추가 시 nullable로)
  [ ] 새 코드가 이전 DB 스키마와 함께 동작하는지 확인
  [ ] API 변경사항이 하위 호환성 유지하는지 확인
  [ ] 환경 변수 및 시크릿 프로덕션에 적용 여부 확인
  [ ] Readiness Probe 정상 동작 확인

배포 중:
  [ ] Rolling Update 또는 Blue/Green 배포 사용
  [ ] Readiness Probe 통과 전까지 트래픽 전환 금지
  [ ] 최소 1개의 구 버전 인스턴스 유지
  [ ] 배포 모니터링 (에러율, 응답시간 대시보드)

배포 후:
  [ ] Health Check 엔드포인트 정상 확인
  [ ] 주요 API 스모크 테스트
  [ ] 에러 로그 모니터링 (최소 10분)
  [ ] 메트릭 정상 범위 확인 (CPU, 메모리, GC)
  [ ] 롤백 계획 준비 (이전 버전 이미지 태그 보관)
```

### 프로덕션 로깅 설정

```yaml
# application-prod.yml
logging:
  level:
    root: WARN
    com.example: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN              # SQL 로그 비활성화 (성능)
  pattern:
    # JSON 포맷 로그 (ELK Stack, CloudWatch 등과 연동 용이)
    console: >-
      {"timestamp":"%d{yyyy-MM-dd HH:mm:ss.SSS}",
       "level":"%level",
       "thread":"%thread",
       "logger":"%logger{36}",
       "message":"%message",
       "traceId":"%X{traceId}",
       "spanId":"%X{spanId}"}%n
```

```kotlin
// MDC를 이용한 요청 추적 (Request ID 로그에 포함)
@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader("X-Request-ID")
            ?: UUID.randomUUID().toString()

        MDC.put("requestId", requestId)
        MDC.put("userId", SecurityContextHolder.getContext().authentication?.name)
        response.addHeader("X-Request-ID", requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
```

---

## 체크리스트

> 이 파일의 내용을 학습하고 실무에 적용하기 위한 체크리스트입니다.

### Swagger / OpenAPI

- [ ] Swagger UI 설정 완료 (`springdoc-openapi-starter-webmvc-ui` 의존성 추가)
- [ ] JWT Bearer 인증 헤더 Swagger에 추가 (`@SecurityScheme` 어노테이션 설정)
- [ ] `@Tag`, `@Operation`, `@ApiResponse`로 API 문서화
- [ ] 프로덕션 프로파일에서 Swagger 비활성화 설정

### Actuator / Monitoring

- [ ] Actuator 엔드포인트 설정 (`health`, `info`, `prometheus` 노출)
- [ ] Custom `HealthIndicator` 구현
- [ ] Micrometer `Counter`, `Timer`, `Gauge` 사용법 이해
- [ ] Prometheus + Grafana 연동 (Docker Compose로 로컬 테스트)

### Docker

- [ ] Dockerfile 멀티 스테이지 빌드 작성
- [ ] `.dockerignore` 파일 설정
- [ ] Layered JAR로 이미지 레이어 최적화
- [ ] Dockerfile에 HEALTHCHECK 추가

### Docker Compose

- [ ] Docker Compose 전체 스택 구성 (app, DB, Redis, Kafka, Prometheus, Grafana)
- [ ] `.env` 파일로 환경 변수 관리
- [ ] `docker-compose.override.yml`로 로컬 개발 환경 설정
- [ ] 볼륨 마운트 및 헬스체크 설정

### CI/CD

- [ ] GitHub Actions CI 파이프라인 구성 (테스트 + 빌드 + Docker Push)
- [ ] Gradle 의존성 캐싱으로 CI 속도 최적화
- [ ] GitHub Actions CD 파이프라인 구성 (스테이징/프로덕션 배포)
- [ ] Slack 배포 알림 설정
- [ ] GitHub Actions Secrets 관리

### Multi-Module

- [ ] Multi-module Gradle 프로젝트 구조 이해 (`:core`, `:domain`, `:api`, `:infra`, `:batch`)
- [ ] `settings.gradle.kts`에 모든 모듈 등록
- [ ] 루트 `build.gradle.kts`에서 공통 설정 관리 (`subprojects { }`)
- [ ] 모듈 간 의존성 방향 이해 (단방향, 순환 의존성 없음)
- [ ] `gradle/libs.versions.toml`로 버전 중앙화

### 환경 변수 및 시크릿

- [ ] 환경 변수 관리 방법 숙지 (`${ENV_VAR:default}` 패턴)
- [ ] 프로파일별 `application-{profile}.yml` 구조 설정
- [ ] `.env` 파일은 `.gitignore`에 등록, `.env.example`만 커밋
- [ ] 프로덕션 시크릿은 AWS Secrets Manager 또는 Vault 사용 계획 수립

### 운영

- [ ] Graceful Shutdown 설정 (`spring.lifecycle.timeout-per-shutdown-phase`)
- [ ] JVM 튜닝 플래그 이해 (`-XX:+UseContainerSupport`, `-XX:MaxRAMPercentage`)
- [ ] Liveness vs Readiness Probe 차이 이해 및 설정
- [ ] 무중단 배포 체크리스트 숙지

---

## 참고 자료

- [springdoc-openapi 공식 문서](https://springdoc.org/)
- [Spring Boot Actuator 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer 공식 문서](https://micrometer.io/docs)
- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Gradle Multi-project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Spring Boot Layered JAR](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#container-images.efficient-images.layering)
- [AWS Spring Cloud](https://awspring.io/)
- [Prometheus + Grafana 모니터링](https://prometheus.io/docs/visualization/grafana/)

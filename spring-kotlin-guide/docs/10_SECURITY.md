# Spring Security + JWT 완전 가이드 (Spring Boot + Kotlin + WebFlux)

> Spring Boot 3.x / Spring Security 6.x / Kotlin / WebFlux + Coroutine 기준
> 처음부터 실무까지 단계별로 다룬다.

---

## 목차

1. [Spring Security 핵심 개념](#1-spring-security-핵심-개념)
2. [환경 설정](#2-환경-설정)
3. [JWT 기초](#3-jwt-기초)
4. [JWT Provider 구현](#4-jwt-provider-구현)
5. [JWT 인증 필터 구현](#5-jwt-인증-필터-구현)
6. [SecurityFilterChain 구성](#6-securityfilterchain-구성-spring-security-6x)
7. [회원 인증 API 구현](#7-회원-인증-api-구현)
8. [@CurrentUser 어노테이션 구현](#8-currentuser-어노테이션-구현)
9. [권한(Role) 기반 접근 제어](#9-권한role-기반-접근-제어)
10. [실전: 전체 인증 플로우 완성](#10-실전-전체-인증-플로우-완성)
11. [테스트](#11-테스트)
12. [자주 만나는 보안 에러](#12-자주-만나는-보안-에러)
13. [학습 체크리스트](#13-학습-체크리스트)

---

## 1. Spring Security 핵심 개념

### 왜 Security가 필요한가?

웹 애플리케이션은 기본적으로 다음과 같은 위협에 노출된다.

| 위협 | 설명 |
|------|------|
| 인증 우회 | 로그인 없이 보호된 리소스에 접근 |
| 권한 상승 | 일반 사용자가 관리자 기능 접근 |
| CSRF | 사용자 브라우저를 통한 위조 요청 |
| 세션 하이재킹 | 타인의 세션 쿠키 탈취 |

Spring Security는 이러한 위협을 필터 체인 수준에서 일관되게 처리해준다. 매 컨트롤러마다 인증 코드를 작성하지 않아도 된다.

---

### 인증(Authentication) vs 인가(Authorization)

```
인증 (Authentication): "당신은 누구입니까?"
  → 사용자가 주장하는 신원이 맞는지 확인 (로그인)
  → 결과: SecurityContext에 Authentication 객체 저장

인가 (Authorization): "당신은 이것을 할 수 있습니까?"
  → 인증된 사용자가 특정 리소스에 접근할 권한이 있는지 확인
  → 결과: 접근 허용 또는 403 Forbidden
```

**실제 예시:**
- 인증: 아이디/비밀번호로 로그인 → 맞으면 JWT 발급
- 인가: `/admin/**` 경로는 `ROLE_ADMIN`을 가진 사용자만 접근 가능

---

### Spring Security 필터 체인 동작 원리

Spring Security는 Servlet Filter(MVC) 또는 WebFilter(WebFlux) 체인으로 동작한다.
모든 HTTP 요청은 컨트롤러에 도달하기 전에 필터 체인을 통과한다.

**WebMVC 흐름:**

```
HTTP Request
     │
     ▼
[DelegatingFilterProxy]          ← Servlet 컨테이너와 Spring 연결
     │
     ▼
[FilterChainProxy]               ← Spring Security의 핵심 진입점
     │
     ├── SecurityContextPersistenceFilter  ← SecurityContext 로드/저장
     ├── LogoutFilter                      ← 로그아웃 처리
     ├── UsernamePasswordAuthenticationFilter (필요시)
     ├── JwtAuthenticationFilter           ← 우리가 만들 커스텀 필터
     ├── ExceptionTranslationFilter        ← 보안 예외 → HTTP 응답 변환
     └── AuthorizationFilter               ← 최종 권한 검사
             │
             ▼
     [DispatcherServlet]
             │
             ▼
     [Controller]
```

**WebFlux 흐름:**

```
HTTP Request
     │
     ▼
[HttpHandlerConnector / Netty]
     │
     ▼
[WebFilterChain]                 ← Spring Security WebFlux
     │
     ├── SecurityContextServerWebExchangeWebFilter
     ├── JwtAuthenticationWebFilter        ← 우리가 만들 커스텀 필터
     ├── AuthorizationWebFilter
     └── ...
             │
             ▼
     [RouterFunction / Controller]
```

**핵심 포인트:**
- MVC: `OncePerRequestFilter` 상속, `SecurityContextHolder` (ThreadLocal)
- WebFlux: `WebFilter` 구현, `ReactiveSecurityContextHolder` (Reactor Context)

---

### Spring Security 6.x 람다 DSL 방식

Security 5.x까지는 메서드 체이닝 방식이었지만, 6.x부터는 람다 DSL이 표준이다.

```kotlin
// 5.x 방식 (Deprecated)
http.csrf().disable()
    .authorizeRequests()
    .antMatchers("/api/auth/**").permitAll()

// 6.x 방식 (표준)
http {
    csrf { disable() }
    authorizeHttpRequests {
        authorize("/api/auth/**", permitAll)
        authorize(anyRequest, authenticated)
    }
}
```

---

## 2. 환경 설정

### build.gradle.kts 의존성

```kotlin
dependencies {
    // Spring WebFlux (Reactive)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Spring Security (WebFlux)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT (JJWT 라이브러리 - 0.12.x 기준)
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Redis (Refresh Token 관리)
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Kotlin Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // BCrypt (비밀번호 해싱 - security starter에 포함)
    // 별도 의존성 불필요

    // Test
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

---

### Security 설정이 없을 때 기본 동작

`spring-boot-starter-security`를 추가하면 자동 설정이 적용된다.

| 상황 | 동작 |
|------|------|
| SecurityConfig 없음 | 모든 요청에 HTTP Basic 인증 요구 |
| 기본 사용자 | username: `user`, password: 시작 로그시 콘솔 출력 |
| Form Login | 자동으로 `/login` 페이지 생성 |

REST API에서는 이 기본 동작이 불필요하므로 반드시 커스텀 SecurityConfig를 작성해야 한다.

---

### SecurityConfig 기본 뼈대 (WebFlux)

```kotlin
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }
            authorizeExchange {
                authorize("/api/auth/**", permitAll)
                authorize(anyExchange, authenticated)
            }
        }
    }
}
```

---

## 3. JWT 기초

### JWT란?

JWT (JSON Web Token)는 당사자 간에 정보를 JSON 형태로 안전하게 전달하기 위한 개방형 표준(RFC 7519)이다.

**구조: Header.Payload.Signature**

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9   ← Header (Base64URL)
.
eyJzdWIiOiIxMjM0NTY3ODkwIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIn0  ← Payload (Base64URL)
.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature (HMAC-SHA256)
```

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload (Claims):**
```json
{
  "sub": "1234567890",       // subject (사용자 ID)
  "email": "user@example.com",
  "roles": ["ROLE_USER"],
  "iat": 1516239022,         // issued at
  "exp": 1516242622          // expiration
}
```

**Signature:**
```
HMAC-SHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

> Payload는 Base64URL 인코딩이므로 누구나 디코딩할 수 있다.
> 민감 정보(비밀번호, 주민번호 등)는 절대 Payload에 넣지 않는다.

---

### JWT의 장단점

| | 설명 |
|---|---|
| **장점 - Stateless** | 서버가 세션을 저장하지 않아도 된다. 수평 확장(Scale-out)에 유리하다. |
| **장점 - 자기 포함** | 토큰 자체에 사용자 정보가 있어 DB 조회 없이 인증 가능하다. |
| **장점 - Cross-domain** | 쿠키와 달리 도메인 제한이 없다. |
| **단점 - 취소 불가** | 발급된 토큰은 만료 전에 서버에서 강제 무효화하기 어렵다. |
| **단점 - 페이로드 크기** | 매 요청마다 토큰 전송으로 헤더 크기가 증가한다. |
| **단점 - 탈취 위험** | 탈취된 토큰은 만료 전까지 유효하다. |

---

### Access Token / Refresh Token 패턴

단일 토큰의 단점을 보완하기 위해 두 토큰을 함께 사용한다.

```
Access Token
  - 수명: 짧게 (15분 ~ 1시간)
  - 용도: API 인증에 사용 (Authorization 헤더)
  - 저장: 메모리 또는 localStorage (주의: XSS)

Refresh Token
  - 수명: 길게 (7일 ~ 30일)
  - 용도: Access Token 재발급에만 사용
  - 저장: HttpOnly 쿠키 또는 Redis (서버 측 관리 권장)
```

**흐름:**

```
1. 로그인 → Access Token(15분) + Refresh Token(7일) 발급
2. API 요청 → Access Token으로 인증
3. Access Token 만료 → Refresh Token으로 /api/auth/refresh 호출
4. 새 Access Token 발급
5. Refresh Token 만료 → 재로그인 필요
6. 로그아웃 → Refresh Token을 Redis 블랙리스트에 등록
```

---

## 4. JWT Provider 구현

### JwtProperties 설정 클래스

```kotlin
// application.yml
jwt:
  secret: ${JWT_SECRET}                    # 환경 변수로 주입 (최소 32자 이상 권장)
  access-token-expiration: 900000          # 15분 (ms)
  refresh-token-expiration: 604800000      # 7일 (ms)
```

```kotlin
@ConfigurationProperties(prefix = "jwt")
@ConstructorBinding
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long
)
```

```kotlin
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class Application
```

---

### JwtTokenProvider 구현

```kotlin
@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(Charsets.UTF_8))
    }

    // Access Token 생성
    fun createAccessToken(userId: Long, email: String, roles: List<String>): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("roles", roles)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    // Refresh Token 생성
    fun createRefreshToken(userId: Long): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.refreshTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    // 토큰 검증
    fun validateToken(token: String): Boolean {
        return runCatching {
            val claims = parseClaims(token)
            !claims.expiration.before(Date())
        }.getOrDefault(false)
    }

    // 사용자 ID 추출
    fun getUserId(token: String): Long {
        return parseClaims(token).subject.toLong()
    }

    // 이메일 추출
    fun getEmail(token: String): String {
        return parseClaims(token).get("email", String::class.java)
    }

    // 권한 목록 추출
    @Suppress("UNCHECKED_CAST")
    fun getRoles(token: String): List<String> {
        return parseClaims(token).get("roles", List::class.java) as List<String>
    }

    // 토큰 타입 확인 (access / refresh)
    fun getTokenType(token: String): String {
        return parseClaims(token).get("type", String::class.java)
    }

    // 만료 시간 추출 (Redis TTL 설정용)
    fun getExpiration(token: String): Date {
        return parseClaims(token).expiration
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
```

---

### 비밀 키 안전하게 관리하는 법

```
나쁜 예:
  jwt.secret=mysupersecretkey  ← application.yml에 하드코딩

좋은 예:
  1. 환경 변수 사용
     JWT_SECRET=<openssl rand -base64 32로 생성한 랜덤 문자열>
     application.yml: jwt.secret=${JWT_SECRET}

  2. AWS Secrets Manager / Vault 사용
     spring-cloud-aws 또는 spring-vault로 시크릿 주입

  3. .gitignore에 .env 파일 등록
     application-local.yml은 반드시 .gitignore에 추가
```

**비밀 키 생성 예시:**
```bash
# 32바이트 랜덤 비밀 키 생성
openssl rand -base64 32
# 출력: a8fL9mKdN2pQrXvYzB3cE5gH7jI0kM1n (예시)
```

---

## 5. JWT 인증 필터 구현

### WebFlux용 JWT 인증 필터

WebFlux에서는 `OncePerRequestFilter` 대신 `WebFilter`를 구현한다.

```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = resolveToken(exchange.request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val authentication = getAuthentication(token)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }

        return chain.filter(exchange)
    }

    // Authorization 헤더에서 토큰 추출
    private fun resolveToken(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    // 토큰으로 Authentication 객체 생성
    private fun getAuthentication(token: String): Authentication {
        val userId = jwtTokenProvider.getUserId(token)
        val email = jwtTokenProvider.getEmail(token)
        val roles = jwtTokenProvider.getRoles(token)

        val authorities = roles.map { SimpleGrantedAuthority(it) }
        val principal = UserPrincipal(userId = userId, email = email)

        return UsernamePasswordAuthenticationToken(principal, null, authorities)
    }
}

// 커스텀 Principal 객체
data class UserPrincipal(
    val userId: Long,
    val email: String
)
```

---

### WebMVC용 JWT 인증 필터 (참고)

MVC 환경에서는 `OncePerRequestFilter`를 상속한다.
`OncePerRequestFilter`를 쓰는 이유는 forward/include 등 내부 디스패치 시 필터가 중복 실행되는 것을 방지하기 위해서다.

```kotlin
// MVC 방식 (참고용)
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val authentication = getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    private fun getAuthentication(token: String): Authentication {
        val userId = jwtTokenProvider.getUserId(token)
        val email = jwtTokenProvider.getEmail(token)
        val roles = jwtTokenProvider.getRoles(token)
        val authorities = roles.map { SimpleGrantedAuthority(it) }
        val principal = UserPrincipal(userId = userId, email = email)
        return UsernamePasswordAuthenticationToken(principal, null, authorities)
    }
}
```

---

### WebFlux vs WebMVC 핵심 차이

| | WebMVC | WebFlux |
|---|---|---|
| 필터 기반 클래스 | `OncePerRequestFilter` | `WebFilter` |
| 요청 객체 | `HttpServletRequest` | `ServerHttpRequest` |
| SecurityContext 저장 | `SecurityContextHolder` (ThreadLocal) | `ReactiveSecurityContextHolder` (Reactor Context) |
| 반환 타입 | `void` | `Mono<Void>` |
| Context 전파 | 스레드 로컬로 자동 | `contextWrite()`로 명시적 전파 |

---

## 6. SecurityFilterChain 구성 (Spring Security 6.x)

### WebFlux SecurityConfig 전체 구성

```kotlin
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            // CSRF 비활성화 이유:
            // REST API는 Stateless하므로 세션/쿠키 기반 CSRF 공격 대상이 아니다.
            // JWT를 Authorization 헤더로 전송하므로 CSRF 토큰 불필요.
            csrf { disable() }

            // HTTP Basic, Form 로그인 비활성화 (REST API에서 불필요)
            httpBasic { disable() }
            formLogin { disable() }

            // 경로별 접근 권한 설정
            authorizeExchange {
                // 인증 불필요 경로
                authorize("/api/auth/**", permitAll)
                authorize("/api/public/**", permitAll)
                authorize("/actuator/health", permitAll)

                // 권한 필요 경로
                authorize("/api/admin/**", hasRole("ADMIN"))
                authorize("/api/users/**", hasAuthority("ROLE_USER"))

                // 나머지 모든 요청은 인증 필요
                authorize(anyExchange, authenticated)
            }

            // JWT 인증 필터 등록
            // AuthorizationWebFilter 이전에 실행되어야 SecurityContext가 채워진다.
            addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            // 예외 처리
            exceptionHandling {
                authenticationEntryPoint = HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
                accessDeniedHandler = HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN)
            }

            // CORS 설정
            cors {
                configurationSource = corsConfigurationSource()
            }
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000", "https://your-frontend.com")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600L
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

---

### WebMVC SecurityConfig (참고)

```kotlin
// MVC 방식 (참고용)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http {
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            authorizeHttpRequests {
                authorize("/api/auth/**", permitAll)
                authorize("/api/admin/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }
    }
}
```

---

## 7. 회원 인증 API 구현

### 도메인 모델

```kotlin
// User 엔티티 (R2DBC 예시)
@Table("users")
data class User(
    @Id val id: Long? = null,
    val email: String,
    val password: String,       // BCrypt 해싱된 비밀번호
    val nickname: String,
    val role: String = "ROLE_USER",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

---

### 회원가입 API (비밀번호 BCrypt 해싱)

```kotlin
// Request/Response DTO
data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)
```

```kotlin
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository  // Redis
) {

    suspend fun signUp(request: SignUpRequest): AuthResponse {
        // 중복 이메일 확인
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }

        // 비밀번호 BCrypt 해싱
        val encodedPassword = passwordEncoder.encode(request.password)

        // 사용자 저장
        val user = userRepository.save(
            User(
                email = request.email,
                password = encodedPassword,    // 해싱된 비밀번호만 저장
                nickname = request.nickname
            )
        )

        // JWT 발급
        return issueTokens(user)
    }

    suspend fun signIn(email: String, password: String): AuthResponse {
        // 사용자 조회
        val user = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.")

        // BCrypt 비밀번호 검증 (matches: 평문 vs 해시 비교)
        if (!passwordEncoder.matches(password, user.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        return issueTokens(user)
    }

    suspend fun refreshToken(refreshToken: String): AuthResponse {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw UnauthorizedException("유효하지 않은 Refresh Token입니다.")
        }

        // 타입 확인
        if (jwtTokenProvider.getTokenType(refreshToken) != "refresh") {
            throw UnauthorizedException("Refresh Token이 아닙니다.")
        }

        // Redis에서 블랙리스트 확인
        val userId = jwtTokenProvider.getUserId(refreshToken)
        if (refreshTokenRepository.isBlacklisted(refreshToken)) {
            throw UnauthorizedException("이미 로그아웃된 토큰입니다.")
        }

        // 사용자 조회 후 새 토큰 발급
        val user = userRepository.findById(userId)
            ?: throw UnauthorizedException("사용자를 찾을 수 없습니다.")

        return issueTokens(user)
    }

    suspend fun signOut(refreshToken: String) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            val expiration = jwtTokenProvider.getExpiration(refreshToken)
            val ttl = expiration.time - System.currentTimeMillis()
            if (ttl > 0) {
                // Redis에 블랙리스트 등록 (만료 시간까지만 유지)
                refreshTokenRepository.addToBlacklist(refreshToken, Duration.ofMillis(ttl))
            }
        }
    }

    private suspend fun issueTokens(user: User): AuthResponse {
        val userId = user.id!!
        val roles = listOf(user.role)

        val accessToken = jwtTokenProvider.createAccessToken(userId, user.email, roles)
        val refreshToken = jwtTokenProvider.createRefreshToken(userId)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
```

---

### 인증 컨트롤러

```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    suspend fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<AuthResponse> {
        val response = authService.signUp(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/signin")
    suspend fun signIn(@RequestBody request: SignInRequest): ResponseEntity<AuthResponse> {
        val response = authService.signIn(request.email, request.password)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    suspend fun refresh(@RequestHeader("Refresh-Token") refreshToken: String): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken(refreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/signout")
    suspend fun signOut(@RequestHeader("Refresh-Token") refreshToken: String): ResponseEntity<Unit> {
        authService.signOut(refreshToken)
        return ResponseEntity.noContent().build()
    }
}
```

---

### Redis Refresh Token Repository

```kotlin
@Repository
class RefreshTokenRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    private val blacklistPrefix = "blacklist:"

    suspend fun addToBlacklist(token: String, ttl: Duration) {
        redisTemplate.opsForValue()
            .set("$blacklistPrefix$token", "1", ttl)
            .awaitSingleOrNull()
    }

    suspend fun isBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey("$blacklistPrefix$token")
            .awaitSingle()
    }
}
```

---

## 8. @CurrentUser 어노테이션 구현

### 어노테이션 정의

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser
```

---

### WebFlux용 HandlerMethodArgumentResolver 구현

WebFlux에서는 `HandlerMethodArgumentResolver` 대신 `HandlerMethodArgumentResolverSupport` 또는 `SyncHandlerMethodArgumentResolver`를 구현한다.
Coroutine과 함께 쓸 때는 `ReactiveSecurityContextHolder`에서 비동기로 꺼낸다.

```kotlin
@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) &&
                parameter.parameterType == UserPrincipal::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> {
        return ReactiveSecurityContextHolder.getContext()
            .map { securityContext ->
                val authentication = securityContext.authentication
                    ?: throw UnauthorizedException("인증 정보가 없습니다.")
                authentication.principal as? UserPrincipal
                    ?: throw UnauthorizedException("잘못된 인증 정보입니다.")
            }
    }
}
```

---

### ArgumentResolver 등록

```kotlin
@Configuration
class WebConfig(
    private val currentUserArgumentResolver: CurrentUserArgumentResolver
) : WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(currentUserArgumentResolver)
    }
}
```

---

### Controller에서 사용 예시

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    // @CurrentUser로 현재 로그인한 사용자 정보를 바로 주입받는다
    @GetMapping("/me")
    suspend fun getMyProfile(@CurrentUser currentUser: UserPrincipal): ResponseEntity<UserProfileResponse> {
        val profile = userService.getProfile(currentUser.userId)
        return ResponseEntity.ok(profile)
    }

    @PutMapping("/me")
    suspend fun updateMyProfile(
        @CurrentUser currentUser: UserPrincipal,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserProfileResponse> {
        val profile = userService.updateProfile(currentUser.userId, request)
        return ResponseEntity.ok(profile)
    }

    @DeleteMapping("/me")
    suspend fun deleteMyAccount(@CurrentUser currentUser: UserPrincipal): ResponseEntity<Unit> {
        userService.deleteUser(currentUser.userId)
        return ResponseEntity.noContent().build()
    }
}
```

---

### Service에서 SecurityContext 직접 접근 (대안)

```kotlin
// ArgumentResolver 없이 Service에서 직접 꺼내는 방법
suspend fun getCurrentUserId(): Long {
    val context = ReactiveSecurityContextHolder.getContext().awaitSingle()
    val principal = context.authentication.principal as UserPrincipal
    return principal.userId
}
```

---

## 9. 권한(Role) 기반 접근 제어

### @EnableReactiveMethodSecurity 활성화

```kotlin
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = true)  // 6.x 방식
class SecurityConfig { ... }
```

---

### @PreAuthorize / @PostAuthorize

```kotlin
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {

    // ADMIN 권한이 있는 사용자만 접근 가능
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(adminService.getAllUsers())
    }

    // 여러 권한 중 하나라도 있으면 접근 가능
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    suspend fun getReports(): ResponseEntity<List<ReportResponse>> {
        return ResponseEntity.ok(adminService.getReports())
    }

    // @PostAuthorize: 메서드 실행 후 반환값 기반 검사
    @GetMapping("/users/{userId}")
    @PostAuthorize("returnObject.body?.email == authentication.name or hasRole('ADMIN')")
    suspend fun getUserById(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(adminService.getUserById(userId))
    }
}
```

---

### 커스텀 권한 표현식

```kotlin
// 커스텀 Security 표현식 정의
@Component("customSecurity")
class CustomSecurityExpression {

    fun isOwner(authentication: Authentication, userId: Long): Boolean {
        val principal = authentication.principal as? UserPrincipal ?: return false
        return principal.userId == userId
    }

    fun isPremiumUser(authentication: Authentication): Boolean {
        return authentication.authorities.any { it.authority == "ROLE_PREMIUM" }
    }
}
```

```kotlin
@RestController
@RequestMapping("/api/posts")
class PostController {

    // 커스텀 표현식 사용
    @DeleteMapping("/{postId}/users/{userId}")
    @PreAuthorize("@customSecurity.isOwner(authentication, #userId) or hasRole('ADMIN')")
    suspend fun deletePost(
        @PathVariable postId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Unit> {
        // ...
        return ResponseEntity.noContent().build()
    }
}
```

---

### 실전 예시: ADMIN만 접근 가능한 API

```kotlin
// SecurityConfig에서 경로 레벨 보호
authorizeExchange {
    authorize("/api/admin/**", hasRole("ADMIN"))
}

// + 메서드 레벨 이중 보호 (권장)
@PreAuthorize("hasRole('ADMIN')")
suspend fun deleteUser(userId: Long) { ... }
```

경로 레벨과 메서드 레벨을 함께 적용하면 잘못된 SecurityConfig 설정이 있어도 메서드 레벨에서 방어할 수 있다.

---

## 10. 실전: 전체 인증 플로우 완성

### 전체 흐름도

```
[클라이언트]                              [서버]
     │
     │  POST /api/auth/signin
     │  { email, password }
     │ ─────────────────────────────────────► AuthController.signIn()
     │                                              │
     │                                        AuthService.signIn()
     │                                              │
     │                                        UserRepository.findByEmail()
     │                                              │
     │                                        passwordEncoder.matches()
     │                                              │
     │                                        JwtTokenProvider.createAccessToken()
     │                                        JwtTokenProvider.createRefreshToken()
     │                                              │
     │  { accessToken, refreshToken }  ◄────────────┘
     │
     │  (이후 API 요청)
     │
     │  GET /api/users/me
     │  Authorization: Bearer <accessToken>
     │ ─────────────────────────────────────► JwtAuthenticationFilter.filter()
     │                                              │
     │                                        resolveToken() → "Bearer " 제거
     │                                              │
     │                                        jwtTokenProvider.validateToken()
     │                                              │
     │                                        getAuthentication() → Authentication 생성
     │                                              │
     │                                        ReactiveSecurityContextHolder.withAuthentication()
     │                                              │
     │                                        ─────────────────────────────────
     │                                        AuthorizationWebFilter (권한 검사)
     │                                              │
     │                                        UserController.getMyProfile()
     │                                              │
     │                                        @CurrentUser → UserPrincipal 주입
     │                                              │
     │  { id, email, nickname, ... }   ◄────────────┘
```

---

### 단계별 코드 (전체 연결)

**1단계: 로그인 요청**

```kotlin
// POST /api/auth/signin
@PostMapping("/signin")
suspend fun signIn(@RequestBody request: SignInRequest): ResponseEntity<AuthResponse> {
    val response = authService.signIn(request.email, request.password)
    return ResponseEntity.ok(response)
}
```

**2단계: 사용자 조회 + 비밀번호 검증**

```kotlin
suspend fun signIn(email: String, password: String): AuthResponse {
    val user = userRepository.findByEmail(email)
        ?: throw UnauthorizedException("인증 실패")

    // BCrypt: matches(평문, 해시) → true/false
    if (!passwordEncoder.matches(password, user.password)) {
        throw UnauthorizedException("인증 실패")
    }

    return issueTokens(user)
}
```

**3단계: JWT 발급**

```kotlin
private suspend fun issueTokens(user: User): AuthResponse {
    val accessToken = jwtTokenProvider.createAccessToken(
        userId = user.id!!,
        email = user.email,
        roles = listOf(user.role)
    )
    val refreshToken = jwtTokenProvider.createRefreshToken(user.id)

    return AuthResponse(accessToken = accessToken, refreshToken = refreshToken)
}
```

**4단계: 이후 요청 - JwtAuthenticationFilter 처리**

```kotlin
override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    val token = resolveToken(exchange.request) ?: return chain.filter(exchange)

    if (!jwtTokenProvider.validateToken(token)) {
        return chain.filter(exchange)    // 토큰 무효 → 인증 없이 통과 (이후 권한 검사에서 막힘)
    }

    val authentication = getAuthentication(token)
    return chain.filter(exchange)
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
}
```

**5단계: SecurityContext 저장 후 Controller 진입**

```kotlin
@GetMapping("/me")
suspend fun getMyProfile(@CurrentUser currentUser: UserPrincipal): ResponseEntity<UserProfileResponse> {
    // @CurrentUser가 ReactiveSecurityContextHolder에서 UserPrincipal을 꺼내 주입
    val profile = userService.getProfile(currentUser.userId)
    return ResponseEntity.ok(profile)
}
```

**6단계: @CurrentUser로 사용자 정보 추출 (ArgumentResolver)**

```kotlin
override fun resolveArgument(
    parameter: MethodParameter,
    bindingContext: BindingContext,
    exchange: ServerWebExchange
): Mono<Any> {
    return ReactiveSecurityContextHolder.getContext()
        .map { ctx -> ctx.authentication.principal as UserPrincipal }
}
```

---

### 전체 패키지 구조

```
src/main/kotlin/com/example/
├── config/
│   ├── SecurityConfig.kt
│   ├── WebConfig.kt
│   └── RedisConfig.kt
├── auth/
│   ├── controller/
│   │   └── AuthController.kt
│   ├── service/
│   │   └── AuthService.kt
│   ├── dto/
│   │   ├── SignUpRequest.kt
│   │   ├── SignInRequest.kt
│   │   └── AuthResponse.kt
│   └── repository/
│       └── RefreshTokenRepository.kt
├── security/
│   ├── JwtTokenProvider.kt
│   ├── JwtAuthenticationFilter.kt
│   ├── JwtProperties.kt
│   ├── UserPrincipal.kt
│   └── CurrentUser.kt
└── user/
    ├── controller/
    │   └── UserController.kt
    ├── service/
    │   └── UserService.kt
    └── repository/
        └── UserRepository.kt
```

---

## 11. 테스트

### @WithMockUser 사용법

```kotlin
@WebFluxTest(UserController::class)
@Import(SecurityConfig::class)
class UserControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var userService: UserService

    // 인증된 사용자로 요청 (기본 username="user", roles=["USER"])
    @Test
    @WithMockUser(username = "user@example.com", roles = ["USER"])
    fun `내 프로필 조회 - 인증된 사용자`() {
        // given
        val profile = UserProfileResponse(id = 1L, email = "user@example.com", nickname = "테스트")
        coEvery { userService.getProfile(any()) } returns profile

        // when & then
        webTestClient.get()
            .uri("/api/users/me")
            .exchange()
            .expectStatus().isOk
            .expectBody<UserProfileResponse>()
            .isEqualTo(profile)
    }

    @Test
    fun `내 프로필 조회 - 미인증 사용자`() {
        webTestClient.get()
            .uri("/api/users/me")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `관리자 API - 권한 없는 사용자`() {
        webTestClient.get()
            .uri("/api/admin/users")
            .exchange()
            .expectStatus().isForbidden
    }
}
```

---

### SecurityContext 모킹 (WebFlux + Coroutine)

```kotlin
@Test
fun `SecurityContext 직접 모킹`() {
    // UserPrincipal 생성
    val principal = UserPrincipal(userId = 1L, email = "user@example.com")
    val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
    val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
    val securityContext = SecurityContextImpl(authentication)

    // ReactiveSecurityContextHolder에 모킹된 컨텍스트 주입
    val result = webTestClient.get()
        .uri("/api/users/me")
        .exchange()
        // mutateWith로 SecurityContext 주입
        .expectStatus().isOk
}

// mutateWith 방식 (Spring Security Test 지원)
@Test
fun `mutateWith mockUser 방식`() {
    webTestClient
        .mutateWith(mockUser().username("user@example.com").roles("USER"))
        .get()
        .uri("/api/users/me")
        .exchange()
        .expectStatus().isOk
}
```

---

### JWT 토큰이 필요한 API 테스트

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `JWT 토큰으로 인증된 요청`() {
        // given: 테스트용 토큰 생성
        val accessToken = jwtTokenProvider.createAccessToken(
            userId = 1L,
            email = "test@example.com",
            roles = listOf("ROLE_USER")
        )

        // when & then
        webTestClient.get()
            .uri("/api/users/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `만료된 토큰으로 요청 - 401 반환`() {
        // 만료된 토큰 (테스트용으로 만료 시간 0ms로 설정)
        val expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.invalid"

        webTestClient.get()
            .uri("/api/users/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $expiredToken")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
```

---

## 12. 자주 만나는 보안 에러

### 401 Unauthorized vs 403 Forbidden

```
401 Unauthorized
  - "당신이 누구인지 모릅니다" (인증 실패)
  - 원인: 토큰 없음, 토큰 만료, 토큰 형식 오류
  - 처리: 재로그인 유도

403 Forbidden
  - "당신이 누구인지는 알지만, 이건 할 수 없습니다" (인가 실패)
  - 원인: 권한 부족 (ROLE_USER가 ADMIN 전용 API 접근)
  - 처리: 권한 없음 메시지 표시
```

```kotlin
// 예외 처리 커스터마이징
@Component
class SecurityExceptionHandler : WebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response

        when (ex) {
            is AccessDeniedException -> {
                response.statusCode = HttpStatus.FORBIDDEN
            }
            is AuthenticationException -> {
                response.statusCode = HttpStatus.UNAUTHORIZED
            }
        }

        val body = """{"error": "${ex.message}"}"""
        val buffer = response.bufferFactory().wrap(body.toByteArray())
        response.headers.contentType = MediaType.APPLICATION_JSON
        return response.writeWith(Mono.just(buffer))
    }
}
```

---

### CORS 설정 (프론트엔드 연동)

```kotlin
// SecurityConfig 내 CORS 설정 (가장 우선순위가 높다)
@Bean
fun corsConfigurationSource(): CorsConfigurationSource {
    val config = CorsConfiguration().apply {
        // 개발환경
        allowedOrigins = listOf("http://localhost:3000")

        // 운영환경 (패턴 사용)
        // allowedOriginPatterns = listOf("https://*.yourdomain.com")

        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "Refresh-Token"
        )
        exposedHeaders = listOf("Authorization")  // 클라이언트에서 읽을 수 있는 헤더
        allowCredentials = true   // 쿠키 허용 시 true (allowedOrigins에 * 사용 불가)
        maxAge = 3600L            // preflight 결과 캐싱 시간 (초)
    }

    return UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/**", config)
    }
}
```

> `allowCredentials = true`일 때 `allowedOrigins = listOf("*")`는 사용할 수 없다.
> 구체적인 origin을 명시하거나 `allowedOriginPatterns`를 사용한다.

---

### Token Expired 에러 처리

```kotlin
// JwtTokenProvider에서 만료 여부 명확히 구분
fun validateToken(token: String): TokenValidationResult {
    return try {
        parseClaims(token)
        TokenValidationResult.VALID
    } catch (e: ExpiredJwtException) {
        TokenValidationResult.EXPIRED        // 만료됨 → Refresh Token으로 갱신 유도
    } catch (e: MalformedJwtException) {
        TokenValidationResult.INVALID        // 형식 오류
    } catch (e: SignatureException) {
        TokenValidationResult.INVALID        // 서명 오류 (위변조)
    } catch (e: Exception) {
        TokenValidationResult.INVALID
    }
}

enum class TokenValidationResult { VALID, EXPIRED, INVALID }
```

```kotlin
// 클라이언트 응답에 만료 여부 포함
override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    val token = resolveToken(exchange.request)

    if (token != null) {
        return when (jwtTokenProvider.validateToken(token)) {
            TokenValidationResult.VALID -> {
                val auth = getAuthentication(token)
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            }
            TokenValidationResult.EXPIRED -> {
                // 401과 함께 만료 메시지 전달 (클라이언트에서 refresh 요청 가능하도록)
                writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED")
            }
            TokenValidationResult.INVALID -> {
                writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN")
            }
        }
    }

    return chain.filter(exchange)
}

private fun writeErrorResponse(
    exchange: ServerWebExchange,
    status: HttpStatus,
    code: String
): Mono<Void> {
    exchange.response.statusCode = status
    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
    val body = """{"code": "$code", "message": "Token error"}"""
    val buffer = exchange.response.bufferFactory().wrap(body.toByteArray())
    return exchange.response.writeWith(Mono.just(buffer))
}
```

---

### SecurityContext가 비어있는 경우

```
증상: SecurityContextHolder.getContext().authentication == null
      또는 ReactiveSecurityContextHolder.getContext()가 빈 Mono 반환

원인 1: JwtAuthenticationFilter가 필터 체인에 등록되지 않음
  → SecurityConfig에서 addFilterAt() 확인

원인 2: WebFlux에서 Reactor Context 전파 누락
  → contextWrite() 호출 여부 확인
  → Coroutine에서는 kotlinx.coroutines.reactor.ReactorContext 사용 필요

원인 3: permitAll 경로에서 SecurityContext 접근 시도
  → permitAll은 인증 없이 통과시키므로 SecurityContext가 비어있을 수 있음

원인 4: @Async 또는 새 스레드에서 SecurityContext 접근 (MVC)
  → ThreadLocal 기반이므로 새 스레드에는 컨텍스트가 없음
  → SecurityContextHolder.MODE_INHERITABLETHREADLOCAL 설정 필요
```

```kotlin
// Coroutine에서 SecurityContext 올바르게 접근
suspend fun getCurrentUser(): UserPrincipal {
    // 올바른 방법: ReactiveSecurityContextHolder 사용
    val context = ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()
        ?: throw UnauthorizedException("SecurityContext가 없습니다.")

    return context.authentication?.principal as? UserPrincipal
        ?: throw UnauthorizedException("인증 정보가 없습니다.")
}
```

---

## 13. 학습 체크리스트

### 기본 개념

- [ ] 인증(Authentication)과 인가(Authorization)의 차이를 설명할 수 있다
- [ ] Spring Security 필터 체인의 동작 순서를 그림으로 설명할 수 있다
- [ ] JWT의 Header.Payload.Signature 각 부분의 역할을 안다
- [ ] Access Token과 Refresh Token을 나누는 이유를 설명할 수 있다

### 구현

- [ ] JwtTokenProvider로 토큰 생성/검증/파싱을 구현할 수 있다
- [ ] WebFlux용 JwtAuthenticationFilter(WebFilter)를 구현할 수 있다
- [ ] SecurityConfig에서 경로별 접근 권한을 설정할 수 있다
- [ ] BCryptPasswordEncoder로 비밀번호를 해싱하고 검증할 수 있다
- [ ] @CurrentUser 어노테이션과 HandlerMethodArgumentResolver를 구현할 수 있다
- [ ] Redis를 사용한 Refresh Token 블랙리스트를 구현할 수 있다

### 보안

- [ ] JWT Secret을 환경 변수로 관리할 수 있다
- [ ] CORS 설정을 올바르게 할 수 있다
- [ ] 401과 403의 차이를 알고 적절히 반환할 수 있다
- [ ] 토큰 만료 시 클라이언트가 처리할 수 있는 응답을 내려줄 수 있다

### 테스트

- [ ] @WithMockUser로 인증된 사용자를 모킹할 수 있다
- [ ] 실제 JWT 토큰을 생성해서 통합 테스트를 작성할 수 있다
- [ ] 권한이 없는 사용자의 403 응답을 테스트할 수 있다

### 심화

- [ ] @PreAuthorize와 커스텀 보안 표현식을 적용할 수 있다
- [ ] WebFlux와 MVC의 SecurityContext 전파 방식 차이를 안다
- [ ] Token Expired 시 자동으로 갱신하는 클라이언트 로직을 설계할 수 있다
- [ ] Refresh Token Rotation 전략을 구현할 수 있다

---

> 참고 자료
> - [Spring Security 공식 문서](https://docs.spring.io/spring-security/reference/)
> - [Spring WebFlux Security](https://docs.spring.io/spring-security/reference/reactive/index.html)
> - [JJWT GitHub](https://github.com/jwtk/jjwt)
> - [RFC 7519 - JSON Web Token](https://datatracker.ietf.org/doc/html/rfc7519)

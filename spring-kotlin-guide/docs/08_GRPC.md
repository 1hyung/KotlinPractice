# Spring Boot + Kotlin gRPC 완전 가이드

> Spring Boot와 Kotlin 환경에서 gRPC를 처음부터 실무까지 단계별로 학습하는 가이드입니다.
> grpc-spring-boot-starter와 grpc-kotlin을 기반으로 작성되었습니다.

---

## 목차

1. [gRPC 핵심 개념](#1-grpc-핵심-개념)
2. [환경 설정](#2-환경-설정)
3. [Proto 파일 작성](#3-proto-파일-작성)
4. [gRPC Server 구현](#4-grpc-server-구현)
5. [gRPC Client 구현](#5-grpc-client-구현)
6. [Interceptor - 인증/로깅](#6-interceptor---인증로깅)
7. [Kotlin Coroutine과 gRPC](#7-kotlin-coroutine과-grpc)
8. [에러 처리 전략](#8-에러-처리-전략)
9. [테스트](#9-테스트)
10. [REST ↔ gRPC 혼용 패턴](#10-rest--grpc-혼용-패턴)
11. [실무 운영 팁](#11-실무-운영-팁)
12. [학습 체크리스트](#12-학습-체크리스트)

---

## 1. gRPC 핵심 개념

### REST API와 gRPC 차이 (왜 gRPC를 쓰는가?)

REST API는 HTTP/1.1 기반의 텍스트 프로토콜(JSON)을 사용합니다. 이는 사람이 읽기 쉽고 브라우저에서 바로 사용할 수 있다는 장점이 있지만, 서비스 간 대량 통신에서는 오버헤드가 발생합니다.

gRPC(Google Remote Procedure Call)는 다음 문제를 해결하기 위해 등장했습니다.

- **성능**: Protocol Buffers(바이너리)로 JSON 대비 3~10배 빠른 직렬화/역직렬화
- **타입 안전성**: proto 파일로 계약(contract)을 정의하므로 양측 코드가 자동 생성됨
- **다양한 통신 패턴**: 단순 요청/응답 외에 스트리밍 지원
- **코드 생성**: 서버/클라이언트 코드가 자동으로 생성되어 휴먼 에러 감소
- **HTTP/2**: 멀티플렉싱, 헤더 압축으로 네트워크 효율 향상

### Protocol Buffers (protobuf)란?

Protocol Buffers(이하 protobuf)는 Google이 만든 언어 중립적인 직렬화 포맷입니다.

```
# JSON (텍스트, 사람이 읽기 쉽지만 크기가 큼)
{"orderId": 12345, "userId": "user_001", "status": "PENDING"}

# Protobuf (바이너리, 위 JSON보다 60~80% 작음)
[바이너리 데이터 - 사람이 읽을 수 없지만 빠르고 작음]
```

proto 파일(`.proto`)에 데이터 구조를 정의하면, 프로토콜 버퍼 컴파일러(`protoc`)가 해당 언어의 클래스를 자동으로 생성합니다.

### HTTP/2 기반 통신

gRPC는 HTTP/2 위에서 동작합니다. HTTP/2의 핵심 기능은 다음과 같습니다.

| 기능 | 설명 |
|------|------|
| 멀티플렉싱 | 하나의 TCP 연결로 여러 요청을 동시에 처리 |
| 헤더 압축 | HPACK 알고리즘으로 반복되는 헤더를 압축 |
| 서버 푸시 | 서버가 클라이언트 요청 없이 데이터를 전송 가능 |
| 바이너리 프레임 | 텍스트 대신 바이너리로 더 효율적 |

### 4가지 통신 패턴

#### 1. Unary RPC (단순 요청/응답)
가장 기본적인 패턴으로 REST API와 유사합니다. 클라이언트가 하나의 요청을 보내고 서버가 하나의 응답을 반환합니다.

```
Client ──[Request]──▶ Server
Client ◀──[Response]── Server
```

```proto
// proto 정의
rpc GetOrder (GetOrderRequest) returns (OrderResponse);
```

#### 2. Server Streaming RPC (서버 스트리밍)
클라이언트가 하나의 요청을 보내고 서버는 여러 개의 응답을 스트림으로 전송합니다. 실시간 데이터 피드, 대용량 데이터 분할 전송 등에 사용합니다.

```
Client ──[Request]──────────────────────▶ Server
Client ◀──[Response1][Response2][Response3]── Server
```

```proto
rpc ListOrders (ListOrdersRequest) returns (stream OrderResponse);
```

#### 3. Client Streaming RPC (클라이언트 스트리밍)
클라이언트가 여러 개의 메시지를 스트림으로 전송하고 서버는 모두 받은 후 하나의 응답을 반환합니다. 파일 업로드, 배치 데이터 전송 등에 사용합니다.

```
Client ──[Req1][Req2][Req3]──▶ Server
Client ◀──[Response]──────── Server
```

```proto
rpc CreateOrders (stream CreateOrderRequest) returns (BatchOrderResponse);
```

#### 4. Bidirectional Streaming RPC (양방향 스트리밍)
클라이언트와 서버 양쪽 모두 스트림으로 메시지를 주고받습니다. 실시간 채팅, 양방향 데이터 동기화 등에 사용합니다.

```
Client ──[Req1]──▶ Server
Client ◀──[Res1]── Server
Client ──[Req2]──▶ Server
Client ◀──[Res2]── Server
```

```proto
rpc TrackOrder (stream TrackOrderRequest) returns (stream TrackOrderResponse);
```

### gRPC vs REST 비교표

| 항목 | REST | gRPC |
|------|------|------|
| 프로토콜 | HTTP/1.1 | HTTP/2 |
| 데이터 형식 | JSON (텍스트) | Protocol Buffers (바이너리) |
| API 계약 | OpenAPI/Swagger (선택) | .proto 파일 (필수) |
| 코드 생성 | 선택적 | 자동 생성 |
| 스트리밍 | 제한적 (SSE, WebSocket 별도) | 기본 지원 (4가지 패턴) |
| 브라우저 지원 | 완벽 지원 | 제한적 (grpc-web 필요) |
| 사람이 읽기 | 쉬움 (JSON) | 어려움 (바이너리) |
| 성능 | 보통 | 빠름 |
| 사용 사례 | 공개 API, 브라우저 클라이언트 | 마이크로서비스 내부 통신 |

---

## 2. 환경 설정

### build.gradle.kts 의존성 설정

```kotlin
import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    // protobuf 플러그인: .proto 파일을 컴파일하여 코틀린/자바 코드를 생성
    id("com.google.protobuf") version "0.9.4"
}

val grpcVersion = "1.64.0"         // gRPC 코어 버전
val grpcKotlinVersion = "1.4.1"    // gRPC Kotlin stub 버전
val protobufVersion = "3.25.3"     // Protocol Buffers 버전
val grpcStarterVersion = "3.1.0"   // grpc-spring-boot-starter 버전

dependencies {
    // Spring Boot 기본 의존성
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // grpc-spring-boot-starter: @GrpcService, @GrpcClient 어노테이션 제공
    // 서버 모드: gRPC 서버를 Spring Bean으로 관리
    implementation("net.devh:grpc-server-spring-boot-starter:$grpcStarterVersion")

    // 클라이언트로도 사용할 경우 아래 의존성 추가
    // implementation("net.devh:grpc-client-spring-boot-starter:$grpcStarterVersion")

    // gRPC Kotlin: suspend 함수와 Flow 기반의 Kotlin 친화적 API 제공
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")

    // Protocol Buffers Kotlin: protobuf 메시지의 Kotlin DSL 빌더 제공
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    // gRPC 네트워크 구현체 (Netty 기반, HTTP/2 지원)
    runtimeOnly("io.grpc:grpc-netty-shaded:$grpcVersion")

    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.grpc:grpc-testing:$grpcVersion") // GrpcServerExtension 포함
}

// protobuf 플러그인 설정
protobuf {
    // protoc: .proto 파일을 컴파일하는 컴파일러
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }

    plugins {
        // grpc: Java gRPC 코드 생성 플러그인
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        // grpckt: Kotlin gRPC 코드 생성 플러그인 (suspend 함수, Flow 생성)
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }

    // 각 소스 세트에 대해 코드 생성 설정
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                // Java gRPC stub 생성
                id("grpc")
                // Kotlin gRPC stub 생성 (CoroutineImplBase, 코루틴 기반 stub)
                id("grpckt")
            }
            // protobuf-kotlin DSL 빌더 생성
            task.builtins {
                id("kotlin")
            }
        }
    }
}

// 생성된 protobuf 코드를 소스 세트에 추가
// (IntelliJ에서 자동으로 인식되도록 설정)
sourceSets {
    main {
        kotlin {
            // 생성된 Kotlin 코드 경로 추가
            srcDirs(
                "build/generated/source/proto/main/kotlin",
                "build/generated/source/proto/main/grpckt"
            )
        }
        java {
            // 생성된 Java 코드 경로 추가 (gRPC stub은 Java로 생성됨)
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/grpc"
            )
        }
    }
}
```

### proto 파일 위치

proto 파일은 기본적으로 아래 경로에 위치해야 합니다.

```
src/
└── main/
    ├── kotlin/           # Kotlin 소스
    ├── resources/        # application.yml 등
    └── proto/            # .proto 파일 위치 (기본 경로)
        ├── common.proto
        └── order.proto
```

### application.yml (서버 포트 설정)

```yaml
# gRPC 서버 설정
grpc:
  server:
    port: 9090          # gRPC 서버 포트 (기본값: 9090, REST는 보통 8080)
    # in-process-name: test  # 테스트 시 in-process 서버 사용 (네트워크 없이 동작)

  # 클라이언트 설정 (다른 gRPC 서버에 연결할 때)
  client:
    # 클라이언트 이름 (코드에서 @GrpcClient("order-service")로 참조)
    order-service:
      address: "static://localhost:9090"  # 연결할 서버 주소
      negotiation-type: plaintext         # TLS 없이 평문 통신 (개발 환경)
      # negotiation-type: tls             # 프로덕션에서는 TLS 사용
      enable-keep-alive: true             # 연결 유지 (서버가 연결을 끊지 않도록)
      keep-alive-without-calls: true      # 활성 RPC 없어도 keep-alive 전송

spring:
  application:
    name: order-service

server:
  port: 8080  # REST API 포트 (gRPC 포트와 다름)
```

---

## 3. Proto 파일 작성

### proto3 기본 문법

```proto
// 항상 파일 첫 줄에 proto 버전을 명시합니다 (proto2와 proto3는 문법이 다름)
syntax = "proto3";

// 패키지 선언: 네임스페이스 역할, 다른 proto 파일과 이름 충돌 방지
package com.example.order;

// Java/Kotlin 코드 생성 시 사용할 패키지 경로
option java_package = "com.example.order.grpc";

// 각 message를 별도의 Java 파일로 생성 (false면 하나의 파일에 모두 생성)
option java_multiple_files = true;
```

### message 타입

```proto
syntax = "proto3";
package com.example.order;

// message: Java/Kotlin의 data class에 해당하는 데이터 구조 정의
message OrderItem {
    // 기본 스칼라 타입
    string product_id = 1;    // 문자열 (기본값: "")
    int32  quantity   = 2;    // 32비트 정수 (기본값: 0)
    int64  price      = 3;    // 64비트 정수, 금액 등 큰 수에 사용 (기본값: 0)
    bool   is_gift    = 4;    // 불리언 (기본값: false)
    double weight_kg  = 5;    // 64비트 부동소수점 (기본값: 0.0)
    bytes  image_data = 6;    // 임의 바이트 배열 (기본값: empty)

    // = 뒤의 숫자는 필드 번호 (1~536,870,911)
    // 필드 번호는 직렬화 시 사용되므로 한번 정하면 변경 불가
    // 1~15: 1바이트로 인코딩 (자주 사용하는 필드에 할당)
    // 16~2047: 2바이트로 인코딩
}

message Order {
    string order_id = 1;

    // repeated: Java의 List<T>에 해당, 0개 이상의 값
    repeated OrderItem items = 2;

    // map: Java의 Map<K, V>에 해당
    // key는 스칼라 타입(string, int32 등)만 가능
    map<string, string> metadata = 3;  // 예: {"source": "mobile", "promo": "SAVE10"}

    // oneof: 여러 필드 중 하나만 설정 가능 (Java의 sealed class 유사)
    oneof delivery_type {
        HomeDelivery home_delivery = 4;
        StorePickup  store_pickup  = 5;
    }

    OrderStatus status = 6;  // enum 타입 사용
}

// enum: 미리 정의된 상수 집합
// proto3에서 enum의 첫 번째 값은 반드시 0이어야 함
enum OrderStatus {
    ORDER_STATUS_UNSPECIFIED = 0;  // 기본값 (0은 항상 정의해야 함)
    ORDER_STATUS_PENDING     = 1;
    ORDER_STATUS_CONFIRMED   = 2;
    ORDER_STATUS_SHIPPED     = 3;
    ORDER_STATUS_DELIVERED   = 4;
    ORDER_STATUS_CANCELLED   = 5;
}

message HomeDelivery {
    string address = 1;
    string zip_code = 2;
}

message StorePickup {
    string store_id = 1;
}
```

### import (다른 proto 파일 참조)

```proto
// common.proto - 공통으로 사용하는 메시지 정의
syntax = "proto3";
package com.example.common;
option java_package = "com.example.common.grpc";
option java_multiple_files = true;

// Google의 공통 타입 사용 (Timestamp, Empty 등)
import "google/protobuf/timestamp.proto";  // Timestamp 타입
import "google/protobuf/empty.proto";      // Empty 타입 (응답이 없을 때)

message PageInfo {
    int32 page       = 1;
    int32 page_size  = 2;
    int32 total_count = 3;
}

message Timestamp {
    // google.protobuf.Timestamp를 래핑하거나 직접 사용
    google.protobuf.Timestamp created_at = 1;
    google.protobuf.Timestamp updated_at = 2;
}
```

```proto
// order.proto - common.proto를 가져와서 사용
syntax = "proto3";
package com.example.order;
option java_package = "com.example.order.grpc";
option java_multiple_files = true;

// 같은 프로젝트의 다른 proto 파일 import
import "common.proto";
import "google/protobuf/empty.proto";

message ListOrdersResponse {
    repeated Order orders = 1;
    com.example.common.PageInfo page_info = 2;  // 패키지명 포함한 전체 이름 사용
}
```

### 실전 예시: 주문 서비스 proto 파일

```proto
// src/main/proto/order_service.proto
syntax = "proto3";

package com.example.order;

option java_package = "com.example.order.grpc";
option java_multiple_files = true;

import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";

// ============================================================
// 공통 메시지
// ============================================================

enum OrderStatus {
    ORDER_STATUS_UNSPECIFIED = 0;
    ORDER_STATUS_PENDING     = 1;
    ORDER_STATUS_CONFIRMED   = 2;
    ORDER_STATUS_PAID        = 3;
    ORDER_STATUS_SHIPPED     = 4;
    ORDER_STATUS_DELIVERED   = 5;
    ORDER_STATUS_CANCELLED   = 6;
    ORDER_STATUS_REFUNDED    = 7;
}

message Money {
    int64  amount   = 1;  // 금액 (원 단위, 소수점 방지를 위해 정수 사용)
    string currency = 2;  // 통화 코드: "KRW", "USD" 등
}

message Address {
    string recipient_name = 1;
    string phone_number   = 2;
    string zip_code       = 3;
    string address_line1  = 4;
    string address_line2  = 5;
}

message OrderItem {
    string product_id   = 1;
    string product_name = 2;
    int32  quantity     = 3;
    Money  unit_price   = 4;
    Money  total_price  = 5;
}

message Order {
    string                    order_id    = 1;
    string                    user_id     = 2;
    repeated OrderItem        items       = 3;
    Money                     total_amount = 4;
    OrderStatus               status      = 5;
    Address                   shipping_address = 6;
    google.protobuf.Timestamp created_at  = 7;
    google.protobuf.Timestamp updated_at  = 8;
    map<string, string>       metadata    = 9;  // 확장 가능한 메타데이터
}

// ============================================================
// Request / Response 메시지
// ============================================================

// Unary: 단일 주문 조회
message GetOrderRequest {
    string order_id = 1;
}

// Unary: 주문 생성
message CreateOrderRequest {
    string             user_id          = 1;
    repeated OrderItem items            = 2;
    Address            shipping_address = 3;
    map<string, string> metadata        = 4;
}

// Unary: 주문 상태 변경
message UpdateOrderStatusRequest {
    string      order_id = 1;
    OrderStatus status   = 2;
    string      reason   = 3;  // 취소/환불 사유 (선택적)
}

// Server Streaming: 주문 목록 조회
message ListOrdersRequest {
    string user_id    = 1;
    int32  page       = 2;
    int32  page_size  = 3;
    OrderStatus status_filter = 4;  // 상태 필터 (UNSPECIFIED면 전체)
}

// Client Streaming: 대량 주문 생성
message BatchCreateOrderResponse {
    int32          success_count = 1;
    int32          fail_count    = 2;
    repeated string created_order_ids = 3;
    repeated string failed_reasons    = 4;
}

// Bidirectional Streaming: 주문 추적
message TrackOrderRequest {
    string order_id = 1;
}

message TrackOrderEvent {
    string                    order_id   = 1;
    OrderStatus               status     = 2;
    string                    message    = 3;
    string                    location   = 4;  // 배송 위치
    google.protobuf.Timestamp event_time = 5;
}

// ============================================================
// Service 정의
// ============================================================
service OrderService {
    // Unary RPC: 단일 요청 → 단일 응답
    rpc GetOrder (GetOrderRequest) returns (Order);
    rpc CreateOrder (CreateOrderRequest) returns (Order);
    rpc UpdateOrderStatus (UpdateOrderStatusRequest) returns (Order);
    rpc DeleteOrder (GetOrderRequest) returns (google.protobuf.Empty);

    // Server Streaming: 단일 요청 → 다수 응답 스트림
    // 주문 목록을 페이지 단위가 아닌 스트림으로 전송
    rpc ListOrders (ListOrdersRequest) returns (stream Order);

    // Client Streaming: 다수 요청 스트림 → 단일 응답
    // 여러 주문을 한 번에 생성
    rpc BatchCreateOrders (stream CreateOrderRequest) returns (BatchCreateOrderResponse);

    // Bidirectional Streaming: 다수 요청 스트림 ↔ 다수 응답 스트림
    // 실시간 주문 추적
    rpc TrackOrders (stream TrackOrderRequest) returns (stream TrackOrderEvent);
}
```

---

## 4. gRPC Server 구현

### @GrpcService 어노테이션

`@GrpcService`는 grpc-spring-boot-starter가 제공하는 어노테이션입니다. 이 어노테이션이 붙은 클래스는 gRPC 서비스로 등록됩니다.

```kotlin
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService  // Spring Bean + gRPC 서비스로 등록
class OrderGrpcService : OrderServiceGrpc.OrderServiceImplBase() {
    // OrderServiceGrpc.OrderServiceImplBase: proto 컴파일러가 생성한 추상 클래스
    // 서비스에 정의된 각 RPC 메서드를 override해서 구현
}
```

### StreamObserver 패턴

gRPC Java/Kotlin에서 비동기 응답을 처리할 때 `StreamObserver<T>` 인터페이스를 사용합니다.

```kotlin
interface StreamObserver<V> {
    fun onNext(value: V)      // 다음 메시지 전송 (스트리밍에서 여러 번 호출 가능)
    fun onError(t: Throwable) // 에러 발생 시 호출 (이후 onNext/onCompleted 호출 불가)
    fun onCompleted()         // 스트림 완료 시 호출 (반드시 마지막에 호출해야 함)
}
```

### Unary RPC 구현

```kotlin
import com.example.order.grpc.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Autowired

@GrpcService
class OrderGrpcService @Autowired constructor(
    private val orderService: OrderService,  // 비즈니스 로직 서비스 (Spring Bean)
) : OrderServiceGrpc.OrderServiceImplBase() {

    // Unary RPC: 단일 주문 조회
    // - request: 클라이언트가 보낸 요청 메시지
    // - responseObserver: 응답을 전송하기 위한 StreamObserver
    override fun getOrder(
        request: GetOrderRequest,
        responseObserver: StreamObserver<Order>,
    ) {
        try {
            val orderId = request.orderId  // proto의 order_id가 camelCase로 변환됨

            // 비즈니스 로직 호출
            val order = orderService.findById(orderId)
                ?: throw Status.NOT_FOUND
                    .withDescription("주문을 찾을 수 없습니다: $orderId")
                    .asRuntimeException()  // gRPC 예외로 변환

            // Unary에서는 onNext를 정확히 한 번만 호출
            responseObserver.onNext(order.toProto())

            // 반드시 onCompleted를 호출해 스트림 종료를 알림
            responseObserver.onCompleted()

        } catch (e: StatusRuntimeException) {
            // gRPC 상태 예외는 그대로 전달
            responseObserver.onError(e)
        } catch (e: Exception) {
            // 일반 예외는 INTERNAL 상태로 변환
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("서버 내부 오류가 발생했습니다")
                    .withCause(e)
                    .asRuntimeException()
            )
        }
    }

    // Unary RPC: 주문 생성
    override fun createOrder(
        request: CreateOrderRequest,
        responseObserver: StreamObserver<Order>,
    ) {
        try {
            // 입력값 검증
            if (request.userId.isBlank()) {
                throw Status.INVALID_ARGUMENT
                    .withDescription("user_id는 필수입니다")
                    .asRuntimeException()
            }
            if (request.itemsCount == 0) {
                throw Status.INVALID_ARGUMENT
                    .withDescription("주문 항목이 비어 있습니다")
                    .asRuntimeException()
            }

            val createdOrder = orderService.create(request.toDomain())

            responseObserver.onNext(createdOrder.toProto())
            responseObserver.onCompleted()

        } catch (e: StatusRuntimeException) {
            responseObserver.onError(e)
        } catch (e: Exception) {
            responseObserver.onError(
                Status.INTERNAL.withDescription(e.message).asRuntimeException()
            )
        }
    }
}
```

### Server Streaming 구현

```kotlin
// Server Streaming: 주문 목록을 스트림으로 전송
override fun listOrders(
    request: ListOrdersRequest,
    responseObserver: StreamObserver<Order>,
) {
    try {
        val orders = orderService.findByUserId(
            userId = request.userId,
            status = request.statusFilter.toDomain(),
        )

        // onNext를 여러 번 호출하여 스트림으로 전송
        orders.forEach { order ->
            // 클라이언트가 취소했는지 확인 (장시간 스트리밍 시 중요)
            if (Thread.currentThread().isInterrupted) {
                return@forEach
            }
            responseObserver.onNext(order.toProto())
        }

        // 모든 데이터를 전송한 후 반드시 완료 알림
        responseObserver.onCompleted()

    } catch (e: Exception) {
        responseObserver.onError(
            Status.INTERNAL.withDescription(e.message).asRuntimeException()
        )
    }
}
```

### Client Streaming 구현

```kotlin
// Client Streaming: 클라이언트가 여러 요청을 보내고 서버는 하나의 응답 반환
override fun batchCreateOrders(
    responseObserver: StreamObserver<BatchCreateOrderResponse>,
): StreamObserver<CreateOrderRequest> {
    // 반환값이 요청을 받을 StreamObserver (클라이언트가 보내는 스트림을 처리)
    val createdOrderIds = mutableListOf<String>()
    val failedReasons = mutableListOf<String>()

    return object : StreamObserver<CreateOrderRequest> {
        // 클라이언트가 onNext를 호출할 때마다 실행됨
        override fun onNext(request: CreateOrderRequest) {
            try {
                val order = orderService.create(request.toDomain())
                createdOrderIds.add(order.id)
            } catch (e: Exception) {
                failedReasons.add("${request.userId}: ${e.message}")
            }
        }

        // 클라이언트 스트림에서 에러가 발생한 경우
        override fun onError(t: Throwable) {
            // 에러 로깅 처리
            responseObserver.onError(
                Status.INTERNAL.withDescription("클라이언트 스트림 오류").asRuntimeException()
            )
        }

        // 클라이언트가 모든 요청 전송 완료 시 호출됨
        override fun onCompleted() {
            // 최종 집계 응답 전송
            val response = batchCreateOrderResponse {
                successCount = createdOrderIds.size
                failCount = failedReasons.size
                this.createdOrderIds += createdOrderIds
                this.failedReasons += failedReasons
            }
            responseObserver.onNext(response)  // 단 한 번만 응답
            responseObserver.onCompleted()
        }
    }
}
```

### 에러 처리 (Status, StatusException, StatusRuntimeException)

```kotlin
// gRPC 에러는 Status 코드와 함께 전달됩니다
// Status.INVALID_ARGUMENT.withDescription("메시지").asRuntimeException()

// StatusRuntimeException: 블로킹 stub에서 발생하는 예외
// StatusException: 코루틴/비동기 stub에서 발생하는 예외

fun handleGrpcError(orderId: String): Order {
    return try {
        orderService.findById(orderId) ?: throw EntityNotFoundException(orderId)
    } catch (e: EntityNotFoundException) {
        throw Status.NOT_FOUND
            .withDescription("주문 없음: $orderId")
            .asRuntimeException()
    } catch (e: IllegalArgumentException) {
        throw Status.INVALID_ARGUMENT
            .withDescription(e.message)
            .asRuntimeException()
    } catch (e: AccessDeniedException) {
        throw Status.PERMISSION_DENIED
            .withDescription("접근 권한 없음")
            .asRuntimeException()
    } catch (e: Exception) {
        throw Status.INTERNAL
            .withDescription("서버 오류")
            .withCause(e)
            .asRuntimeException()
    }
}
```

### 실전 예시: 주문 서비스 서버 구현

```kotlin
// src/main/kotlin/com/example/grpc/server/OrderGrpcService.kt
package com.example.grpc.server

import com.example.order.domain.OrderDomainService
import com.example.order.grpc.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@GrpcService
class OrderGrpcService @Autowired constructor(
    private val orderDomainService: OrderDomainService,
    private val orderProtoMapper: OrderProtoMapper,  // 도메인 ↔ Proto 변환 담당
) : OrderServiceGrpc.OrderServiceImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getOrder(
        request: GetOrderRequest,
        responseObserver: StreamObserver<Order>,
    ) {
        log.info("getOrder 호출: orderId={}", request.orderId)

        runCatching {
            val order = orderDomainService.findById(request.orderId)
                ?: throw Status.NOT_FOUND
                    .withDescription("주문 없음: ${request.orderId}")
                    .asRuntimeException()

            responseObserver.onNext(orderProtoMapper.toProto(order))
            responseObserver.onCompleted()
        }.onFailure { e ->
            log.error("getOrder 실패: {}", e.message, e)
            responseObserver.onError(toGrpcException(e))
        }
    }

    override fun createOrder(
        request: CreateOrderRequest,
        responseObserver: StreamObserver<Order>,
    ) {
        log.info("createOrder 호출: userId={}", request.userId)

        runCatching {
            validateCreateOrderRequest(request)
            val order = orderDomainService.create(orderProtoMapper.toDomain(request))
            responseObserver.onNext(orderProtoMapper.toProto(order))
            responseObserver.onCompleted()
        }.onFailure { e ->
            log.error("createOrder 실패: {}", e.message, e)
            responseObserver.onError(toGrpcException(e))
        }
    }

    override fun listOrders(
        request: ListOrdersRequest,
        responseObserver: StreamObserver<Order>,
    ) {
        log.info("listOrders 호출: userId={}", request.userId)

        runCatching {
            orderDomainService
                .findByUserId(request.userId)
                .map { orderProtoMapper.toProto(it) }
                .forEach { responseObserver.onNext(it) }  // 각 주문을 스트림으로 전송

            responseObserver.onCompleted()
        }.onFailure { e ->
            log.error("listOrders 실패: {}", e.message, e)
            responseObserver.onError(toGrpcException(e))
        }
    }

    private fun validateCreateOrderRequest(request: CreateOrderRequest) {
        if (request.userId.isBlank())
            throw Status.INVALID_ARGUMENT.withDescription("user_id 필수").asRuntimeException()
        if (request.itemsCount == 0)
            throw Status.INVALID_ARGUMENT.withDescription("주문 항목 없음").asRuntimeException()
    }

    // 일반 예외를 gRPC StatusRuntimeException으로 변환
    private fun toGrpcException(e: Throwable): StatusRuntimeException {
        return when (e) {
            is StatusRuntimeException -> e
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
            is NoSuchElementException  -> Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
            else -> Status.INTERNAL.withDescription("서버 내부 오류").withCause(e).asRuntimeException()
        }
    }
}
```

---

## 5. gRPC Client 구현

### @GrpcClient 어노테이션 사용

```kotlin
import net.devh.boot.grpc.client.inject.GrpcClient

@Service
class OrderClientService {

    // @GrpcClient: application.yml의 grpc.client.order-service 설정을 참조
    // Stub을 자동으로 주입해줌 (채널 생성, 연결 관리를 starter가 담당)
    @GrpcClient("order-service")  // application.yml의 클라이언트 이름과 일치해야 함
    private lateinit var orderStub: OrderServiceGrpc.OrderServiceBlockingStub

    @GrpcClient("order-service")
    private lateinit var orderAsyncStub: OrderServiceGrpc.OrderServiceStub

    // grpc-kotlin을 사용하는 경우 (권장)
    @GrpcClient("order-service")
    private lateinit var orderCoroutineStub: OrderServiceGrpcKt.OrderServiceCoroutineStub
}
```

### Blocking Stub vs Async Stub

```kotlin
@Service
class OrderClientService {

    @GrpcClient("order-service")
    private lateinit var blockingStub: OrderServiceGrpc.OrderServiceBlockingStub

    @GrpcClient("order-service")
    private lateinit var asyncStub: OrderServiceGrpc.OrderServiceStub

    // ── Blocking Stub ──────────────────────────────────────────
    // 동기 방식, 현재 스레드를 블록킹
    // 단순하지만 스레드가 낭비될 수 있음
    fun getOrderBlocking(orderId: String): Order {
        val request = getOrderRequest { this.orderId = orderId }
        return blockingStub.getOrder(request)  // 응답이 올 때까지 블록킹
    }

    // Server Streaming - Blocking: Iterator로 처리
    fun listOrdersBlocking(userId: String): List<Order> {
        val request = listOrdersRequest { this.userId = userId }
        val iterator = blockingStub.listOrders(request)  // Iterator<Order> 반환
        return iterator.asSequence().toList()
    }

    // ── Async Stub ──────────────────────────────────────────────
    // 비동기 방식, 콜백 기반
    // 복잡하지만 스레드 효율적
    fun getOrderAsync(orderId: String, callback: (Order) -> Unit) {
        val request = getOrderRequest { this.orderId = orderId }
        asyncStub.getOrder(request, object : StreamObserver<Order> {
            override fun onNext(value: Order) = callback(value)
            override fun onError(t: Throwable) { /* 에러 처리 */ }
            override fun onCompleted() { /* 완료 처리 */ }
        })
    }
}
```

### Kotlin Coroutine과 통합 (grpc-kotlin)

grpc-kotlin을 사용하면 `suspend` 함수와 `Flow`로 gRPC를 사용할 수 있습니다. 이 방식이 Kotlin에서 가장 권장됩니다.

```kotlin
@Service
class OrderCoroutineClientService {

    // grpc-kotlin이 생성한 CoroutineStub 사용
    @GrpcClient("order-service")
    private lateinit var stub: OrderServiceGrpcKt.OrderServiceCoroutineStub

    // suspend 함수로 Unary RPC 호출 (블로킹 없음)
    suspend fun getOrder(orderId: String): Order {
        val request = getOrderRequest { this.orderId = orderId }
        return stub.getOrder(request)  // 코루틴 안에서 suspend
    }

    // Flow를 이용한 Server Streaming 수신
    suspend fun listOrders(userId: String): Flow<Order> {
        val request = listOrdersRequest { this.userId = userId }
        return stub.listOrders(request)  // Flow<Order> 반환
    }

    // Flow를 이용한 Client Streaming 전송
    suspend fun batchCreateOrders(
        requests: Flow<CreateOrderRequest>,
    ): BatchCreateOrderResponse {
        return stub.batchCreateOrders(requests)
    }

    // Bidirectional Streaming
    suspend fun trackOrders(
        orderIds: Flow<TrackOrderRequest>,
    ): Flow<TrackOrderEvent> {
        return stub.trackOrders(orderIds)
    }
}

// 실제 사용 예시
@RestController
class OrderController(
    private val clientService: OrderCoroutineClientService,
) {
    @GetMapping("/orders/{orderId}")
    suspend fun getOrder(@PathVariable orderId: String): OrderResponse {
        val order = clientService.getOrder(orderId)
        return OrderResponse.from(order)
    }

    @GetMapping("/users/{userId}/orders", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamOrders(@PathVariable userId: String): Flow<OrderResponse> {
        return clientService.listOrders(userId).map { OrderResponse.from(it) }
    }
}
```

### 연결 타임아웃 설정

```yaml
# application.yml
grpc:
  client:
    order-service:
      address: "static://localhost:9090"
      negotiation-type: plaintext
      # 데드라인 설정 (이 시간 내에 응답이 없으면 DEADLINE_EXCEEDED 에러)
      # 개별 RPC 호출 시 withDeadlineAfter로도 설정 가능
```

```kotlin
import io.grpc.Deadline
import java.util.concurrent.TimeUnit

@Service
class OrderClientServiceWithTimeout {

    @GrpcClient("order-service")
    private lateinit var stub: OrderServiceGrpcKt.OrderServiceCoroutineStub

    suspend fun getOrderWithTimeout(orderId: String): Order {
        val request = getOrderRequest { this.orderId = orderId }

        // withDeadlineAfter: 이 RPC 호출에만 타임아웃 적용
        return stub
            .withDeadlineAfter(5, TimeUnit.SECONDS)  // 5초 내 응답 없으면 예외
            .getOrder(request)
    }

    suspend fun getOrderWithDeadline(orderId: String): Order {
        val request = getOrderRequest { this.orderId = orderId }

        // Deadline: 절대 시간 기준 타임아웃
        val deadline = Deadline.after(3, TimeUnit.SECONDS)
        return stub.withDeadline(deadline).getOrder(request)
    }
}
```

### 실전 예시: 주문 서비스 클라이언트 구현

```kotlin
// src/main/kotlin/com/example/grpc/client/OrderGrpcClient.kt
package com.example.grpc.client

import com.example.order.grpc.*
import io.grpc.StatusException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class OrderGrpcClient {

    private val log = LoggerFactory.getLogger(javaClass)

    @GrpcClient("order-service")
    private lateinit var stub: OrderServiceGrpcKt.OrderServiceCoroutineStub

    // 단일 주문 조회
    suspend fun getOrder(orderId: String): Order? {
        return try {
            val request = getOrderRequest { this.orderId = orderId }
            stub.withDeadlineAfter(5, TimeUnit.SECONDS).getOrder(request)
        } catch (e: StatusException) {
            // io.grpc.Status.Code로 분기 처리
            when (e.status.code) {
                io.grpc.Status.Code.NOT_FOUND -> {
                    log.warn("주문 없음: {}", orderId)
                    null
                }
                io.grpc.Status.Code.DEADLINE_EXCEEDED -> {
                    log.error("주문 조회 타임아웃: {}", orderId)
                    throw RuntimeException("주문 서비스 응답 지연", e)
                }
                else -> {
                    log.error("주문 조회 실패: {}", e.message, e)
                    throw RuntimeException("주문 조회 실패", e)
                }
            }
        }
    }

    // 주문 생성
    suspend fun createOrder(
        userId: String,
        items: List<OrderItemRequest>,
        address: AddressInfo,
    ): Order {
        val request = createOrderRequest {
            this.userId = userId
            this.items += items.map { item ->
                orderItem {
                    productId = item.productId
                    productName = item.productName
                    quantity = item.quantity
                    unitPrice = money {
                        amount = item.price
                        currency = "KRW"
                    }
                }
            }
            shippingAddress = address {
                recipientName = address.name
                phoneNumber = address.phone
                zipCode = address.zipCode
                addressLine1 = address.line1
                addressLine2 = address.line2 ?: ""
            }
        }

        return stub.createOrder(request)
    }

    // 주문 목록 스트림 수신
    fun listOrders(userId: String): Flow<Order> {
        val request = listOrdersRequest {
            this.userId = userId
            pageSize = 50
        }
        return stub.listOrders(request)
            .catch { e ->
                log.error("주문 목록 스트림 오류: {}", e.message, e)
                throw e
            }
    }
}

// 요청/주소 DTO
data class OrderItemRequest(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Long,
)

data class AddressInfo(
    val name: String,
    val phone: String,
    val zipCode: String,
    val line1: String,
    val line2: String? = null,
)
```

---

## 6. Interceptor - 인증/로깅

### Server Interceptor (JWT 인증)

```kotlin
// src/main/kotlin/com/example/grpc/interceptor/AuthServerInterceptor.kt
package com.example.grpc.interceptor

import io.grpc.*
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.beans.factory.annotation.Autowired

// 메타데이터 키: 클라이언트가 보내는 헤더 이름
val JWT_TOKEN_KEY: Metadata.Key<String> = Metadata.Key.of(
    "authorization",  // 헤더 이름 (소문자)
    Metadata.ASCII_STRING_MARSHALLER
)

// Context 키: 인터셉터에서 핸들러로 인증 정보를 전달할 때 사용
val USER_ID_CONTEXT_KEY: Context.Key<String> = Context.key("userId")

@GrpcGlobalServerInterceptor  // 모든 gRPC 서비스에 자동으로 적용
class AuthServerInterceptor @Autowired constructor(
    private val jwtService: JwtService,
) : ServerInterceptor {

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,      // RPC 호출 정보
        headers: Metadata,                   // 클라이언트가 보낸 메타데이터(헤더)
        next: ServerCallHandler<ReqT, RespT>, // 다음 처리 단계 (실제 서비스 메서드)
    ): ServerCall.Listener<ReqT> {

        // 1. 헤더에서 JWT 토큰 추출
        val token = headers.get(JWT_TOKEN_KEY)

        // 2. 인증이 필요 없는 메서드는 통과 (예: 헬스체크)
        val methodName = call.methodDescriptor.bareMethodName
        if (methodName in PUBLIC_METHODS) {
            return next.startCall(call, headers)
        }

        // 3. 토큰이 없으면 UNAUTHENTICATED 에러
        if (token == null || !token.startsWith("Bearer ")) {
            call.close(
                Status.UNAUTHENTICATED.withDescription("인증 토큰 없음"),
                Metadata()
            )
            return object : ServerCall.Listener<ReqT>() {}  // 빈 리스너 반환
        }

        // 4. 토큰 검증
        val userId = try {
            jwtService.validateAndExtractUserId(token.removePrefix("Bearer "))
        } catch (e: Exception) {
            call.close(
                Status.UNAUTHENTICATED.withDescription("유효하지 않은 토큰"),
                Metadata()
            )
            return object : ServerCall.Listener<ReqT>() {}
        }

        // 5. Context에 userId 저장 (서비스 메서드에서 접근 가능)
        val context = Context.current().withValue(USER_ID_CONTEXT_KEY, userId)
        return Contexts.interceptCall(context, call, headers, next)
    }

    companion object {
        private val PUBLIC_METHODS = setOf("HealthCheck", "GetVersion")
    }
}

// 서비스에서 Context를 통해 userId 접근
@GrpcService
class SecureOrderGrpcService : OrderServiceGrpc.OrderServiceImplBase() {
    override fun createOrder(
        request: CreateOrderRequest,
        responseObserver: StreamObserver<Order>,
    ) {
        // 인터셉터가 저장한 userId 조회
        val authenticatedUserId = USER_ID_CONTEXT_KEY.get()
        // authenticatedUserId를 사용해 권한 체크 등 수행
    }
}
```

### Client Interceptor (헤더 추가)

```kotlin
// src/main/kotlin/com/example/grpc/interceptor/TokenClientInterceptor.kt
package com.example.grpc.interceptor

import io.grpc.*
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import org.springframework.beans.factory.annotation.Autowired

@GrpcGlobalClientInterceptor  // 모든 gRPC 클라이언트 호출에 자동으로 적용
class TokenClientInterceptor @Autowired constructor(
    private val tokenProvider: ServiceTokenProvider,
) : ClientInterceptor {

    override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,  // 호출할 RPC 메서드 정보
        callOptions: CallOptions,                // 타임아웃 등 호출 옵션
        next: Channel,                           // 다음 처리 단계 (실제 네트워크 채널)
    ): ClientCall<ReqT, RespT> {

        // 다음 단계의 호출을 먼저 생성
        val call = next.newCall(method, callOptions)

        // ForwardingClientCall: 기존 호출을 감싸서 일부 동작만 오버라이드
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
            override fun start(
                responseListener: Listener<RespT>,
                headers: Metadata,
            ) {
                // 서비스 간 통신용 내부 JWT 토큰 추가
                val serviceToken = tokenProvider.getServiceToken()
                headers.put(JWT_TOKEN_KEY, "Bearer $serviceToken")

                // 추적을 위한 요청 ID 추가
                headers.put(
                    Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER),
                    generateRequestId()
                )

                super.start(responseListener, headers)
            }
        }
    }

    private fun generateRequestId(): String = java.util.UUID.randomUUID().toString()
}
```

### 인터셉터 등록 방법

| 방식 | 어노테이션 | 범위 |
|------|-----------|------|
| 전역 서버 인터셉터 | `@GrpcGlobalServerInterceptor` | 모든 서비스에 적용 |
| 특정 서비스만 | `@GrpcService(interceptors = [MyInterceptor::class])` | 해당 서비스에만 적용 |
| 전역 클라이언트 인터셉터 | `@GrpcGlobalClientInterceptor` | 모든 클라이언트 호출에 적용 |

```kotlin
// 특정 서비스에만 인터셉터 적용
@GrpcService(interceptors = [AuthServerInterceptor::class, LoggingInterceptor::class])
class PrivateOrderGrpcService : OrderServiceGrpc.OrderServiceImplBase() {
    // 이 서비스에만 위 인터셉터들이 적용됨
}
```

---

## 7. Kotlin Coroutine과 gRPC (grpc-kotlin)

### suspend 함수로 gRPC 구현

grpc-kotlin은 `.proto` 파일에서 `CoroutineImplBase`를 생성합니다. 이를 상속하면 `suspend` 함수로 구현할 수 있습니다.

```kotlin
// grpc-kotlin 방식: suspend 함수로 서버 구현
@GrpcService
class OrderCoroutineGrpcService(
    private val orderService: OrderDomainService,
) : OrderServiceGrpcKt.OrderServiceCoroutineImplBase() {  // CoroutineImplBase 상속

    // StreamObserver 대신 suspend 함수 사용 (훨씬 간결)
    override suspend fun getOrder(request: GetOrderRequest): Order {
        val order = orderService.findById(request.orderId)
            ?: throw StatusException(
                Status.NOT_FOUND.withDescription("주문 없음: ${request.orderId}")
            )
        return orderProtoMapper.toProto(order)
        // onNext, onCompleted를 직접 호출할 필요 없음
        // return 값이 자동으로 클라이언트에게 전달됨
    }

    override suspend fun createOrder(request: CreateOrderRequest): Order {
        // suspend 함수이므로 다른 suspend 함수(DB 조회 등) 직접 호출 가능
        val order = orderService.create(orderProtoMapper.toDomain(request))
        return orderProtoMapper.toProto(order)
    }
}
```

### Flow를 이용한 스트리밍

```kotlin
@GrpcService
class OrderCoroutineGrpcService(
    private val orderService: OrderDomainService,
) : OrderServiceGrpcKt.OrderServiceCoroutineImplBase() {

    // Server Streaming: Flow<Order> 반환
    override fun listOrders(request: ListOrdersRequest): Flow<Order> = flow {
        // flow { } 블록 안에서 emit()으로 각 아이템 전송
        orderService.findByUserId(request.userId).forEach { order ->
            emit(orderProtoMapper.toProto(order))  // 클라이언트에게 하나씩 전송
        }
    }

    // DB에서 대용량 데이터를 스트림으로 조회하는 경우
    override fun exportOrders(request: ExportRequest): Flow<Order> {
        // DB 커서를 Flow로 래핑하여 메모리 효율적으로 처리
        return orderService.streamAllOrders()  // Flow<OrderDomain> 반환
            .map { orderProtoMapper.toProto(it) }
            .catch { e ->
                throw StatusException(Status.INTERNAL.withDescription(e.message))
            }
    }

    // Client Streaming: Flow<CreateOrderRequest>를 받아 단일 응답 반환
    override suspend fun batchCreateOrders(
        requests: Flow<CreateOrderRequest>,  // 클라이언트의 스트림을 Flow로 받음
    ): BatchCreateOrderResponse {
        var successCount = 0
        var failCount = 0
        val createdIds = mutableListOf<String>()

        requests.collect { request ->
            try {
                val order = orderService.create(orderProtoMapper.toDomain(request))
                createdIds.add(order.id)
                successCount++
            } catch (e: Exception) {
                failCount++
            }
        }

        return batchCreateOrderResponse {
            this.successCount = successCount
            this.failCount = failCount
            this.createdOrderIds += createdIds
        }
    }

    // Bidirectional Streaming: Flow<Request> → Flow<Response>
    override fun trackOrders(
        requests: Flow<TrackOrderRequest>,
    ): Flow<TrackOrderEvent> = flow {
        requests.collect { request ->
            // 각 요청에 대해 추적 이벤트를 조회하여 emit
            val events = orderService.getTrackingEvents(request.orderId)
            events.forEach { event ->
                emit(orderProtoMapper.toTrackEventProto(event))
            }
        }
    }
}
```

### CoroutineScope와 gRPC 연동

```kotlin
@GrpcService
class AsyncOrderGrpcService(
    private val orderService: OrderDomainService,
    private val notificationService: NotificationService,
) : OrderServiceGrpcKt.OrderServiceCoroutineImplBase() {

    override suspend fun createOrder(request: CreateOrderRequest): Order {
        // coroutineScope: 하위 코루틴이 모두 완료될 때까지 기다림
        return coroutineScope {
            // 주문 생성과 재고 확인을 병렬로 실행
            val orderDeferred = async { orderService.create(request.toDomain()) }
            val stockDeferred = async { inventoryService.checkStock(request.items) }

            val order = orderDeferred.await()
            val stockOk = stockDeferred.await()

            if (!stockOk) {
                throw StatusException(
                    Status.FAILED_PRECONDITION.withDescription("재고 부족")
                )
            }

            // 알림 전송은 결과를 기다리지 않음 (fire-and-forget)
            launch { notificationService.sendOrderConfirmation(order) }

            orderProtoMapper.toProto(order)
        }
    }

    // 타임아웃이 있는 코루틴 처리
    override suspend fun getOrder(request: GetOrderRequest): Order {
        return withTimeout(3000L) {  // 3초 타임아웃
            val order = orderService.findById(request.orderId)
                ?: throw StatusException(Status.NOT_FOUND.withDescription("주문 없음"))
            orderProtoMapper.toProto(order)
        }
    }
}
```

---

## 8. 에러 처리 전략

### gRPC Status 코드 목록과 의미

| Status 코드 | HTTP 유사 코드 | 의미 | 사용 예시 |
|-------------|---------------|------|----------|
| `OK` | 200 | 성공 | 정상 응답 |
| `CANCELLED` | - | 클라이언트가 취소 | 클라이언트 타임아웃 |
| `UNKNOWN` | 500 | 알 수 없는 에러 | 예상치 못한 예외 |
| `INVALID_ARGUMENT` | 400 | 잘못된 요청 파라미터 | 필수 필드 누락, 형식 오류 |
| `DEADLINE_EXCEEDED` | 504 | 타임아웃 | 처리 시간 초과 |
| `NOT_FOUND` | 404 | 리소스 없음 | 주문/사용자 없음 |
| `ALREADY_EXISTS` | 409 | 이미 존재 | 중복 주문 |
| `PERMISSION_DENIED` | 403 | 권한 없음 | 타인 주문 접근 |
| `UNAUTHENTICATED` | 401 | 인증 실패 | 토큰 없음/만료 |
| `RESOURCE_EXHAUSTED` | 429 | 자원 고갈 | 요청 한도 초과 |
| `FAILED_PRECONDITION` | 400 | 사전 조건 실패 | 재고 부족, 상태 불일치 |
| `ABORTED` | 409 | 중단됨 | 동시성 충돌 |
| `UNIMPLEMENTED` | 501 | 미구현 | 지원하지 않는 메서드 |
| `INTERNAL` | 500 | 서버 내부 오류 | DB 오류, 예상치 못한 예외 |
| `UNAVAILABLE` | 503 | 서비스 불가 | 서버 다운, 오버로드 |

### 커스텀 에러 변환

```kotlin
// src/main/kotlin/com/example/grpc/exception/GrpcExceptionTranslator.kt
package com.example.grpc.exception

import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException

// 도메인 예외 → gRPC Status 변환 유틸리티
object GrpcExceptionTranslator {

    fun translate(e: Throwable): StatusRuntimeException {
        return when (e) {
            // 이미 gRPC 예외면 그대로 반환
            is StatusRuntimeException -> e
            is StatusException        -> e.status.asRuntimeException()

            // 도메인 예외 변환
            is EntityNotFoundException   -> Status.NOT_FOUND
                .withDescription(e.message)
                .asRuntimeException()

            is DuplicateEntityException  -> Status.ALREADY_EXISTS
                .withDescription(e.message)
                .asRuntimeException()

            is ValidationException       -> Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .asRuntimeException()

            is InsufficientStockException -> Status.FAILED_PRECONDITION
                .withDescription("재고 부족: ${e.productId}")
                .asRuntimeException()

            is AccessDeniedException     -> Status.PERMISSION_DENIED
                .withDescription("접근 권한 없음")
                .asRuntimeException()

            is RateLimitException        -> Status.RESOURCE_EXHAUSTED
                .withDescription("요청 한도 초과, 잠시 후 다시 시도해주세요")
                .asRuntimeException()

            // 기타 모든 예외 → 내부 서버 오류
            else -> Status.INTERNAL
                .withDescription("서버 내부 오류가 발생했습니다")
                .withCause(e)  // 서버 로그에는 원인 포함 (클라이언트에겐 노출 안 됨)
                .asRuntimeException()
        }
    }
}

// 도메인 예외 정의
class EntityNotFoundException(val entityId: String, entity: String = "Entity") :
    RuntimeException("$entity 없음: $entityId")

class DuplicateEntityException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
class InsufficientStockException(val productId: String) : RuntimeException("재고 부족")
class RateLimitException : RuntimeException("요청 한도 초과")
```

### GlobalExceptionHandler 패턴

grpc-kotlin에서는 `AbstractCoroutineServerImpl`을 활용하거나 인터셉터로 전역 예외 처리를 구현합니다.

```kotlin
// 전역 예외 처리 인터셉터
@GrpcGlobalServerInterceptor
class GlobalExceptionHandlerInterceptor : ServerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ): ServerCall.Listener<ReqT> {
        val listener = next.startCall(call, headers)

        // 리스너 래핑: 요청 처리 중 예외 포착
        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            override fun onHalfClose() {
                try {
                    super.onHalfClose()
                } catch (e: Exception) {
                    log.error("처리되지 않은 gRPC 예외", e)
                    call.close(
                        GrpcExceptionTranslator.translate(e).status,
                        Metadata()
                    )
                }
            }
        }
    }
}

// grpc-kotlin에서 BaseCoroutineServerImpl 확장으로 예외 처리 (권장)
abstract class BaseGrpcService<T> where T : BindableService {
    // 공통 에러 처리 래퍼
    protected suspend fun <R> withExceptionHandling(
        block: suspend () -> R,
    ): R {
        return try {
            block()
        } catch (e: StatusException) {
            throw e  // 이미 gRPC 예외면 그대로
        } catch (e: Exception) {
            throw GrpcExceptionTranslator.translate(e)
        }
    }
}
```

---

## 9. 테스트

### GrpcServerExtension으로 단위 테스트

```kotlin
// src/test/kotlin/com/example/grpc/OrderGrpcServiceTest.kt
package com.example.grpc

import com.example.order.grpc.*
import io.grpc.testing.GrpcServerRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class OrderGrpcServiceTest {

    // GrpcServerRule: 테스트용 인메모리 gRPC 서버 (JUnit 4 스타일)
    @get:Rule
    val grpcServerRule = GrpcServerRule().directExecutor()

    // JUnit 5 방식은 아래 InProcessServerBuilder 섹션 참조

    private val orderService: OrderDomainService = mockk()

    @Test
    fun `getOrder - 존재하는 주문 조회 성공`() {
        // given: 서비스 등록
        grpcServerRule.serviceRegistry.addService(
            OrderGrpcService(orderService, OrderProtoMapper())
        )

        // Mock 설정
        every { orderService.findById("order-001") } returns createMockOrder("order-001")

        // when: 클라이언트 생성 및 호출
        val stub = OrderServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val response = stub.getOrder(getOrderRequest { orderId = "order-001" })

        // then: 응답 검증
        assertEquals("order-001", response.orderId)
        assertEquals(OrderStatus.ORDER_STATUS_PENDING, response.status)
    }

    @Test
    fun `getOrder - 존재하지 않는 주문 조회 시 NOT_FOUND 에러`() {
        grpcServerRule.serviceRegistry.addService(
            OrderGrpcService(orderService, OrderProtoMapper())
        )
        every { orderService.findById(any()) } returns null

        val stub = OrderServiceGrpc.newBlockingStub(grpcServerRule.channel)

        val exception = assertThrows(StatusRuntimeException::class.java) {
            stub.getOrder(getOrderRequest { orderId = "not-exist" })
        }

        assertEquals(Status.Code.NOT_FOUND, exception.status.code)
    }
}
```

### InProcessServerBuilder 사용 (JUnit 5)

```kotlin
// src/test/kotlin/com/example/grpc/OrderGrpcServiceIntegrationTest.kt
package com.example.grpc

import com.example.order.grpc.*
import io.grpc.ManagedChannel
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import java.util.UUID
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderGrpcServiceIntegrationTest {

    private val orderService: OrderDomainService = mockk()
    private val orderProtoMapper = OrderProtoMapper()

    private lateinit var serverName: String
    private lateinit var channel: ManagedChannel
    private lateinit var stub: OrderServiceGrpc.OrderServiceBlockingStub

    @BeforeAll
    fun setup() {
        // 고유한 서버 이름 생성 (테스트 간 격리)
        serverName = InProcessServerBuilder.generateName()

        // 인메모리 서버 생성 및 시작
        InProcessServerBuilder
            .forName(serverName)         // 서버 이름으로 식별 (실제 포트 없음)
            .directExecutor()            // 테스트 스레드에서 직접 실행 (단순화)
            .addService(OrderGrpcService(orderService, orderProtoMapper))
            .build()
            .start()

        // 인메모리 채널 생성 (네트워크 없이 서버와 통신)
        channel = InProcessChannelBuilder
            .forName(serverName)
            .directExecutor()
            .build()

        stub = OrderServiceGrpc.newBlockingStub(channel)
    }

    @AfterAll
    fun teardown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    @Test
    fun `주문 생성 후 조회 성공`() {
        val mockOrder = createMockOrder("order-123")
        every { orderService.create(any()) } returns mockOrder
        every { orderService.findById("order-123") } returns mockOrder

        // 주문 생성
        val createRequest = createOrderRequest {
            userId = "user-001"
            items += orderItem {
                productId = "product-001"
                productName = "테스트 상품"
                quantity = 2
                unitPrice = money { amount = 10000; currency = "KRW" }
            }
        }
        val created = stub.createOrder(createRequest)
        assertEquals("order-123", created.orderId)

        // 생성된 주문 조회
        val found = stub.getOrder(getOrderRequest { orderId = "order-123" })
        assertEquals("order-123", found.orderId)
    }

    @Test
    fun `Server Streaming - 주문 목록 수신`() {
        val mockOrders = listOf(
            createMockOrder("order-001"),
            createMockOrder("order-002"),
            createMockOrder("order-003"),
        )
        every { orderService.findByUserId("user-001") } returns mockOrders

        val request = listOrdersRequest { userId = "user-001" }
        val orders = stub.listOrders(request).asSequence().toList()

        assertEquals(3, orders.size)
        assertEquals("order-001", orders[0].orderId)
    }
}
```

### Mockk으로 서비스 모킹

```kotlin
// src/test/kotlin/com/example/grpc/OrderGrpcServiceMockTest.kt
package com.example.grpc

import com.example.order.grpc.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OrderGrpcServiceMockTest {

    private val orderService: OrderDomainService = mockk()
    private val mapper: OrderProtoMapper = mockk()

    // grpc-kotlin CoroutineImplBase 테스트 (suspend 함수)
    private val service = OrderCoroutineGrpcService(orderService, mapper)

    @Test
    fun `suspend 함수 - 주문 조회 성공`() = runTest {
        // given
        val mockProto = Order.newBuilder()
            .setOrderId("order-001")
            .setStatus(OrderStatus.ORDER_STATUS_PENDING)
            .build()

        coEvery { orderService.findById("order-001") } returns createMockDomainOrder()
        every { mapper.toProto(any()) } returns mockProto

        // when
        val request = getOrderRequest { orderId = "order-001" }
        val result = service.getOrder(request)

        // then
        assertEquals("order-001", result.orderId)
        coVerify { orderService.findById("order-001") }
    }

    @Test
    fun `suspend 함수 - 존재하지 않는 주문 StatusException 발생`() = runTest {
        coEvery { orderService.findById(any()) } returns null

        val request = getOrderRequest { orderId = "not-exist" }

        val exception = assertThrows<StatusException> {
            service.getOrder(request)
        }

        assertEquals(Status.Code.NOT_FOUND, exception.status.code)
    }
}
```

---

## 10. REST ↔ gRPC 혼용 패턴

### grpc-gateway 개요

grpc-gateway는 REST API 요청을 gRPC 호출로 변환해주는 리버스 프록시입니다. Go 생태계에서 주로 사용되며, proto 파일에 HTTP 옵션을 추가하여 REST ↔ gRPC 매핑을 정의합니다.

```proto
// grpc-gateway 방식: proto 파일에 HTTP 옵션 추가
import "google/api/annotations.proto";

service OrderService {
    rpc GetOrder (GetOrderRequest) returns (Order) {
        option (google.api.http) = {
            get: "/v1/orders/{order_id}"  // GET /v1/orders/{id} → GetOrder RPC
        };
    }
    rpc CreateOrder (CreateOrderRequest) returns (Order) {
        option (google.api.http) = {
            post: "/v1/orders"
            body: "*"  // 요청 body 전체를 proto 메시지로 변환
        };
    }
}
```

### REST 요청을 gRPC로 변환하는 패턴

Spring Boot 환경에서는 grpc-gateway 없이도 REST Controller에서 gRPC 클라이언트를 호출하는 방식으로 혼용할 수 있습니다.

```kotlin
// 패턴 1: REST Controller가 gRPC 클라이언트를 호출
// REST(8080) → gRPC Client → gRPC Server(9090)
@RestController
@RequestMapping("/api/v1/orders")
class OrderRestController(
    private val orderGrpcClient: OrderGrpcClient,  // gRPC 클라이언트 주입
) {
    // REST GET 요청 → gRPC GetOrder 호출
    @GetMapping("/{orderId}")
    suspend fun getOrder(@PathVariable orderId: String): OrderResponseDto {
        val grpcOrder = orderGrpcClient.getOrder(orderId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "주문 없음")

        // gRPC proto 메시지 → REST 응답 DTO 변환
        return OrderResponseDto(
            orderId = grpcOrder.orderId,
            userId = grpcOrder.userId,
            status = grpcOrder.status.name,
            totalAmount = grpcOrder.totalAmount.amount,
        )
    }

    // REST GET 요청 → gRPC Server Streaming → SSE로 변환
    @GetMapping("/stream/{userId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamOrders(@PathVariable userId: String): Flow<OrderResponseDto> {
        return orderGrpcClient.listOrders(userId)
            .map { grpcOrder -> OrderResponseDto.from(grpcOrder) }
    }

    // REST POST 요청 → gRPC CreateOrder 호출
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createOrder(@RequestBody request: CreateOrderRequestDto): OrderResponseDto {
        val grpcOrder = orderGrpcClient.createOrder(
            userId = request.userId,
            items = request.items.map { it.toGrpcItem() },
            address = request.address.toGrpcAddress(),
        )
        return OrderResponseDto.from(grpcOrder)
    }
}

data class OrderResponseDto(
    val orderId: String,
    val userId: String,
    val status: String,
    val totalAmount: Long,
) {
    companion object {
        fun from(order: Order) = OrderResponseDto(
            orderId = order.orderId,
            userId = order.userId,
            status = order.status.name,
            totalAmount = order.totalAmount.amount,
        )
    }
}
```

```kotlin
// 패턴 2: 한 서버에서 REST와 gRPC를 동시에 제공
// application.yml
// server.port: 8080 (REST)
// grpc.server.port: 9090 (gRPC)
// → 같은 Spring Boot 앱이 두 포트를 동시에 열어 각각 처리

// @GrpcService → gRPC 포트(9090)에서 처리
@GrpcService
class OrderGrpcService : OrderServiceGrpc.OrderServiceImplBase() { ... }

// @RestController → HTTP 포트(8080)에서 처리
@RestController
class OrderRestController : { ... }
```

---

## 11. 실무 운영 팁

### 서비스 간 통신에서 gRPC 사용 시점

**gRPC를 선택해야 하는 경우**
- 마이크로서비스 간 내부 통신 (브라우저 없음)
- 초당 수천 건 이상의 고빈도 API 호출
- 실시간 스트리밍이 필요한 경우 (이벤트 피드, 파일 전송)
- 강타입 계약이 필요한 팀 간 협업
- 다국어 클라이언트 지원 (Go, Python, Java 등)

**REST를 유지해야 하는 경우**
- 브라우저에서 직접 호출하는 공개 API
- 외부 파트너/서드파티에게 노출하는 API
- 개발 편의성과 디버깅이 중요한 경우

### proto 파일 버전 관리 (Backward Compatibility)

```proto
// ✅ 안전한 변경 (하위 호환 유지)
// 1. 새 필드 추가 (기존 클라이언트는 무시함)
message Order {
    string order_id = 1;
    string user_id  = 2;
    // 새 필드: 기존 클라이언트는 이 필드를 모르지만 정상 동작함
    string tracking_number = 10;  // 큰 번호 사용 권장 (기존 번호와 충돌 방지)
}

// 2. 새 RPC 메서드 추가 (기존 메서드 유지)
service OrderService {
    rpc GetOrder (GetOrderRequest) returns (Order);       // 기존
    rpc GetOrderV2 (GetOrderV2Request) returns (OrderV2); // 새로 추가
}

// ❌ 위험한 변경 (하위 호환 파괴)
// 1. 필드 번호 변경 (절대 금지)
message Order {
    string order_id = 2;  // 기존 1에서 2로 변경 → 기존 클라이언트 오동작
}

// 2. 필드 타입 변경 (절대 금지)
message Order {
    int64 order_id = 1;   // 기존 string에서 int64로 변경 → 파싱 오류
}

// 3. 필드 삭제 (주의 필요)
// 삭제 대신 deprecated 처리하거나 reserved 선언
message Order {
    reserved 5;               // 이 번호는 다시 사용할 수 없음
    reserved "old_field";     // 이 이름은 다시 사용할 수 없음
    // string old_field = 5;  // 삭제된 필드
}
```

**버전 관리 전략**

```
# 방법 1: 패키지 버전 관리
src/main/proto/
├── v1/
│   └── order_service.proto   (package com.example.order.v1)
└── v2/
    └── order_service.proto   (package com.example.order.v2)

# 방법 2: Git 태그 기반 proto 배포 (proto 파일을 별도 저장소로 관리)
# 팀 공통 proto 저장소를 만들고 Gradle 의존성으로 배포
```

### 자주 발생하는 문제와 해결법

**문제 1: UNAVAILABLE - 서버에 연결할 수 없음**
```yaml
# 원인: 잘못된 주소 또는 서버 미실행
# 해결: application.yml 주소 확인
grpc:
  client:
    order-service:
      address: "static://localhost:9090"  # 주소 오타 확인
      negotiation-type: plaintext         # TLS/plaintext 설정 확인
```

**문제 2: DEADLINE_EXCEEDED - 타임아웃**
```kotlin
// 원인: 처리 시간이 설정된 데드라인 초과
// 해결 1: 데드라인 증가
stub.withDeadlineAfter(30, TimeUnit.SECONDS).getOrder(request)

// 해결 2: 비즈니스 로직 최적화 (DB 쿼리 튜닝, 캐싱 등)

// 해결 3: 서버 스트리밍으로 변경 (대용량 데이터의 경우)
```

**문제 3: UNIMPLEMENTED - 메서드 없음**
```kotlin
// 원인: 서버에 해당 RPC 메서드가 등록되지 않음
// 확인: @GrpcService 어노테이션 확인, 올바른 ImplBase 상속 확인

// 잘못된 예: ImplBase를 상속하지 않음
@GrpcService
class WrongService {  // OrderServiceGrpc.OrderServiceImplBase() 미상속
    fun getOrder(...) { }
}

// 올바른 예
@GrpcService
class CorrectService : OrderServiceGrpc.OrderServiceImplBase() {
    override fun getOrder(...) { }
}
```

**문제 4: proto 컴파일 후 생성된 코드가 IDE에서 인식 안 됨**
```kotlin
// 해결: build.gradle.kts에 sourceSets 추가
sourceSets {
    main {
        kotlin {
            srcDirs(
                "build/generated/source/proto/main/kotlin",
                "build/generated/source/proto/main/grpckt"
            )
        }
    }
}
// 이후 IntelliJ: File → Invalidate Caches → Restart
// Gradle: ./gradlew generateProto
```

**문제 5: SSL/TLS 연결 오류**
```yaml
# 개발 환경: plaintext 사용
grpc:
  client:
    order-service:
      negotiation-type: plaintext

# 프로덕션: TLS 설정 필요
grpc:
  server:
    security:
      enabled: true
      certificate-chain: classpath:server.crt
      private-key: classpath:server.key
  client:
    order-service:
      negotiation-type: tls
      security:
        trust-cert-collection: classpath:ca.crt
```

**문제 6: Kotlin proto DSL 빌더를 사용하려면**
```kotlin
// 의존성 확인
implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

// build.gradle.kts generateProtoTasks에 kotlin builtin 추가
task.builtins {
    id("kotlin")  // 이 설정이 있어야 DSL 빌더가 생성됨
}

// 사용 예시 (Java 방식 vs Kotlin DSL)
// Java 방식
val order = Order.newBuilder()
    .setOrderId("123")
    .setUserId("user-001")
    .build()

// Kotlin DSL 방식 (훨씬 간결)
val order = order {
    orderId = "123"
    userId = "user-001"
}
```

---

## 12. 학습 체크리스트

### Phase 1: 개념 이해
- [ ] gRPC가 REST 대비 갖는 장점과 단점을 설명할 수 있다
- [ ] Protocol Buffers의 역할과 JSON과의 차이를 설명할 수 있다
- [ ] 4가지 gRPC 통신 패턴(Unary, Server Streaming, Client Streaming, Bidirectional)을 설명할 수 있다
- [ ] HTTP/2의 멀티플렉싱이 gRPC 성능에 미치는 영향을 이해한다

### Phase 2: 환경 설정
- [ ] `build.gradle.kts`에 grpc-spring-boot-starter 의존성을 추가할 수 있다
- [ ] protobuf 플러그인을 설정하고 `./gradlew generateProto`를 실행할 수 있다
- [ ] 생성된 코드 경로를 `sourceSets`에 등록하여 IDE가 인식하게 할 수 있다
- [ ] `application.yml`에 gRPC 서버/클라이언트 포트와 주소를 설정할 수 있다

### Phase 3: Proto 파일 작성
- [ ] proto3 기본 문법(message, field, enum, service, rpc)을 작성할 수 있다
- [ ] `repeated`, `map`, `oneof` 필드를 사용할 수 있다
- [ ] 다른 proto 파일을 import하여 공통 메시지를 재사용할 수 있다
- [ ] `google.protobuf.Timestamp`, `google.protobuf.Empty`를 사용할 수 있다
- [ ] 4가지 RPC 패턴을 proto 파일로 정의할 수 있다

### Phase 4: 서버 구현
- [ ] `@GrpcService`로 gRPC 서비스를 Spring Bean으로 등록할 수 있다
- [ ] Unary RPC를 `StreamObserver`로 구현할 수 있다
- [ ] Server Streaming RPC를 `onNext` 반복 호출로 구현할 수 있다
- [ ] Client Streaming RPC를 `StreamObserver` 반환으로 구현할 수 있다
- [ ] `Status` 코드로 적절한 gRPC 에러를 반환할 수 있다

### Phase 5: 클라이언트 구현
- [ ] `@GrpcClient`로 Stub을 주입받을 수 있다
- [ ] Blocking Stub과 Async Stub의 차이를 이해하고 사용할 수 있다
- [ ] `withDeadlineAfter`로 타임아웃을 설정할 수 있다
- [ ] `StatusRuntimeException`/`StatusException`을 catch하여 에러를 처리할 수 있다

### Phase 6: grpc-kotlin 활용
- [ ] `CoroutineImplBase`를 상속하여 `suspend` 함수로 서버를 구현할 수 있다
- [ ] `Flow`를 반환하여 Server Streaming을 구현할 수 있다
- [ ] `Flow`를 파라미터로 받아 Client/Bidirectional Streaming을 구현할 수 있다
- [ ] `CoroutineCoroutineStub`을 사용하여 코루틴 클라이언트를 구현할 수 있다

### Phase 7: 인터셉터
- [ ] `@GrpcGlobalServerInterceptor`로 JWT 인증 인터셉터를 구현할 수 있다
- [ ] `Context`를 사용하여 인터셉터에서 서비스로 데이터를 전달할 수 있다
- [ ] `@GrpcGlobalClientInterceptor`로 요청 헤더를 자동으로 추가할 수 있다

### Phase 8: 테스트
- [ ] `InProcessServerBuilder`로 인메모리 gRPC 서버를 생성할 수 있다
- [ ] `InProcessChannelBuilder`로 인메모리 클라이언트를 생성할 수 있다
- [ ] Mockk으로 서비스를 모킹하여 gRPC 서버를 단위 테스트할 수 있다
- [ ] grpc-kotlin의 suspend 함수를 `runTest`로 테스트할 수 있다

### Phase 9: 운영
- [ ] proto 파일에서 하위 호환을 유지하며 필드를 추가할 수 있다
- [ ] `reserved`로 삭제된 필드 번호를 보호할 수 있다
- [ ] gRPC Status 코드를 보고 원인을 파악하고 해결할 수 있다
- [ ] REST와 gRPC를 같은 Spring Boot 앱에서 혼용할 수 있다

---

## 참고 자료

- [gRPC 공식 문서](https://grpc.io/docs/)
- [Protocol Buffers 공식 문서](https://protobuf.dev/)
- [grpc-spring-boot-starter GitHub](https://github.com/grpc-ecosystem/grpc-spring)
- [grpc-kotlin GitHub](https://github.com/grpc/grpc-kotlin)
- [grpc-spring-boot-starter 설정 문서](https://yidongnan.github.io/grpc-spring-boot-starter/)

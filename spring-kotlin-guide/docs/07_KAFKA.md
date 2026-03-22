# 07. Spring Boot + Kotlin 환경에서 Apache Kafka 완전 가이드

> Apache Kafka 공식 문서 및 Spring for Apache Kafka 공식 문서 기반으로 작성된 가이드입니다.
> 처음 Kafka를 접하는 개발자부터 실무에서 활용하려는 개발자까지 모두를 대상으로 합니다.

---

## 목차

1. [Kafka 핵심 개념](#1-kafka-핵심-개념)
2. [Spring Kafka 환경 설정](#2-spring-kafka-환경-설정)
3. [Producer 구현](#3-producer-구현)
4. [Consumer 구현](#4-consumer-구현)
5. [고급 패턴](#5-고급-패턴)
6. [Kotlin Coroutine과 Kafka 통합](#6-kotlin-coroutine과-kafka-통합)
7. [테스트](#7-테스트)
8. [실무 운영 팁](#8-실무-운영-팁)
9. [학습 체크리스트](#9-학습-체크리스트)

---

## 1. Kafka 핵심 개념

### 1.1 Kafka가 왜 필요한가?

#### 기존 HTTP(동기) 방식의 한계

```
[주문 서비스] --HTTP--> [재고 서비스]
             --HTTP--> [결제 서비스]
             --HTTP--> [알림 서비스]
             --HTTP--> [배송 서비스]
```

주문이 들어오면 위처럼 여러 서비스를 순차적으로(또는 병렬로) HTTP 호출해야 합니다.

**문제점:**
- **강한 결합(Tight Coupling)**: 주문 서비스가 다른 서비스의 주소와 API 스펙을 모두 알아야 함
- **가용성 문제**: 알림 서비스가 다운되면 주문 전체가 실패할 수 있음
- **확장성 한계**: 서비스가 추가될 때마다 주문 서비스를 수정해야 함
- **응답 지연**: 모든 서비스 응답을 기다려야 사용자에게 응답 가능

#### 이벤트 드리븐 아키텍처(Event-Driven Architecture)

```
[주문 서비스] --이벤트 발행--> [Kafka]
                                    |
                    +---------------+---------------+
                    v               v               v
              [재고 서비스]   [결제 서비스]   [알림 서비스]
```

**장점:**
- **느슨한 결합(Loose Coupling)**: 주문 서비스는 Kafka에만 이벤트를 발행하면 끝
- **고가용성**: 알림 서비스가 다운되어도 주문은 성공, 서비스 복구 후 이벤트 재처리
- **확장성**: 새로운 서비스 추가 시 주문 서비스 수정 불필요
- **빠른 응답**: 이벤트 발행 후 즉시 사용자에게 응답 가능

### 1.2 핵심 개념 설명

#### Topic (토픽)
메시지가 저장되는 카테고리 또는 피드 이름입니다. 파일 시스템의 폴더와 유사합니다.

```
토픽 예시:
- order-created       (주문 생성 이벤트)
- order-cancelled     (주문 취소 이벤트)
- payment-completed   (결제 완료 이벤트)
- user-registered     (회원 가입 이벤트)
```

#### Partition (파티션)
토픽을 물리적으로 나눈 단위입니다. 파티션이 많을수록 병렬 처리 성능이 올라갑니다.

```
order-created 토픽 (파티션 3개)
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Partition 0 │  │ Partition 1 │  │ Partition 2 │
│ [msg0][msg3]│  │ [msg1][msg4]│  │ [msg2][msg5]│
└─────────────┘  └─────────────┘  └─────────────┘
```

- 같은 Key를 가진 메시지는 항상 같은 파티션에 저장됩니다 (순서 보장)
- Key가 없으면 라운드로빈 방식으로 분산됩니다

#### Offset (오프셋)
파티션 내에서 메시지의 위치를 나타내는 순차 번호입니다. Kafka는 오프셋을 통해 어디까지 읽었는지 추적합니다.

```
Partition 0:  [0: msg_a] [1: msg_b] [2: msg_c] [3: msg_d]
                                         ^
                                    현재 Consumer가 읽은 위치 (offset=2)
```

#### Producer (프로듀서)
메시지를 Kafka 토픽에 발행하는 주체입니다. Spring에서는 `KafkaTemplate`을 사용합니다.

#### Consumer (컨슈머)
Kafka 토픽에서 메시지를 읽어 처리하는 주체입니다. Spring에서는 `@KafkaListener`를 사용합니다.

#### Consumer Group (컨슈머 그룹)
여러 컨슈머가 하나의 그룹을 이루어 협력하여 메시지를 처리합니다.

```
order-created 토픽 (파티션 3개)
  Partition 0 ──→ Consumer A (재고 서비스 그룹)
  Partition 1 ──→ Consumer B (재고 서비스 그룹)
  Partition 2 ──→ Consumer C (재고 서비스 그룹)

  같은 토픽을 다른 그룹도 동시에 구독 가능:
  Partition 0,1,2 ──→ Consumer D (알림 서비스 그룹, 단일 컨슈머)
```

**핵심 규칙:** 하나의 파티션은 같은 그룹 내에서 오직 하나의 컨슈머만 담당합니다.

### 1.3 실생활 비유

**Kafka = 편의점 물류 시스템**으로 이해해봅시다.

| Kafka 개념 | 실생활 비유 |
|---|---|
| Topic | 물류 카테고리 (식품, 음료, 생활용품) |
| Partition | 지역별 창고 (서울 창고, 부산 창고, 대구 창고) |
| Producer | 제조사 (물건을 창고에 입고) |
| Consumer | 편의점 (창고에서 물건을 출고) |
| Consumer Group | 같은 회사 편의점 체인 (GS25, CU 각각 독립적으로 출고) |
| Offset | 출고 번호표 (내가 몇 번까지 가져갔는지 기억) |
| Broker | 물류 창고 건물 자체 |

**핵심 포인트:**
- GS25가 음료 카테고리 창고에서 물건을 가져가도, CU는 동일한 음료 창고에서 독립적으로 물건을 가져갈 수 있습니다 (Consumer Group 독립성)
- 창고가 여러 지역(Partition)으로 나뉘어 있어 여러 편의점이 동시에 다른 창고에서 물건을 가져갈 수 있습니다 (병렬 처리)

### 1.4 Kafka vs RabbitMQ 간단 비교

| 항목 | Apache Kafka | RabbitMQ |
|---|---|---|
| 메시지 모델 | 로그 기반 (토픽/파티션) | 큐 기반 (Exchange/Queue) |
| 메시지 보존 | 설정한 기간 동안 유지 (기본 7일) | 소비 즉시 삭제 |
| 처리량 | 매우 높음 (백만 TPS) | 보통 (수만 TPS) |
| 메시지 순서 | 파티션 내 순서 보장 | 큐 내 순서 보장 |
| 메시지 재처리 | 오프셋 리셋으로 재처리 가능 | 소비 후 재처리 어려움 |
| 복잡도 | 높음 (ZooKeeper/KRaft 필요) | 낮음 (설치 간단) |
| 적합한 사용처 | 이벤트 스트리밍, 대용량 로그 | 작업 큐, 간단한 메시지 전달 |
| 프로토콜 | 자체 바이너리 프로토콜 | AMQP |

**선택 기준:**
- 대용량 이벤트 스트리밍, 메시지 재처리 필요 → **Kafka**
- 단순한 작업 큐, 빠른 설치 필요 → **RabbitMQ**

---

## 2. Spring Kafka 환경 설정

### 2.1 build.gradle.kts 의존성

```kotlin
// build.gradle.kts
dependencies {
    // Spring Kafka (Spring Boot가 버전 자동 관리)
    implementation("org.springframework.kafka:spring-kafka")

    // JSON 직렬화를 위한 Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // 테스트용 EmbeddedKafka
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
```

### 2.2 application.yml 설정

```yaml
spring:
  kafka:
    # Kafka 브로커 주소 (여러 개일 경우 쉼표로 구분)
    bootstrap-servers: localhost:9092

    producer:
      # 메시지 Key 직렬화 클래스
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      # 메시지 Value 직렬화 클래스 (JSON 사용)
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      # acks=all: 모든 복제본이 메시지를 받았을 때 성공 (가장 안전)
      # acks=1: 리더만 받으면 성공 (기본값, 적절한 균형)
      # acks=0: 응답을 기다리지 않음 (가장 빠르지만 유실 가능)
      acks: all
      # 전송 실패 시 재시도 횟수
      retries: 3
      # 재시도 간격 (ms)
      properties:
        retry.backoff.ms: 1000
        # 멱등성 프로듀서 활성화 (중복 메시지 방지, acks=all 필요)
        enable.idempotence: true

    consumer:
      # 컨슈머 그룹 ID (같은 그룹 내에서 파티션을 나눠서 처리)
      group-id: my-app-group
      # Key 역직렬화 클래스
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      # Value 역직렬화 클래스 (JSON 사용)
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      # 오프셋 초기화 전략
      # earliest: 처음부터 읽음 (새 그룹이 처음 실행될 때)
      # latest: 이후 메시지만 읽음 (기본값)
      auto-offset-reset: earliest
      # 오프셋 자동 커밋 여부 (false = 수동 커밋으로 정확한 처리 보장)
      enable-auto-commit: false
      properties:
        # 신뢰할 수 있는 패키지 (JsonDeserializer 보안 설정)
        spring.json.trusted.packages: "com.example.*"

    listener:
      # 수동 커밋 모드 설정
      ack-mode: manual_immediate
```

### 2.3 KafkaConfig 클래스

```kotlin
// KafkaConfig.kt
package com.example.config

import com.example.dto.OrderEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(
    // application.yml에서 주입
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
) {

    // ───────────────────────────────────────────
    // Producer 설정
    // ───────────────────────────────────────────

    /**
     * Producer 설정 맵
     * ProducerFactory가 이 설정으로 Producer를 생성함
     */
    private fun producerConfigs(): Map<String, Any> = mapOf(
        // Kafka 브로커 주소
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        // Key 직렬화
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        // Value 직렬화 (JSON)
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        // 모든 복제본 확인 후 성공 응답
        ProducerConfig.ACKS_CONFIG to "all",
        // 재시도 횟수
        ProducerConfig.RETRIES_CONFIG to 3,
        // 멱등성 활성화 (정확히 한 번 전송 보장)
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
    )

    /**
     * ProducerFactory: KafkaTemplate이 사용할 Producer 인스턴스를 생성하는 팩토리
     */
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> =
        DefaultKafkaProducerFactory(producerConfigs())

    /**
     * KafkaTemplate: 실제 메시지 발행에 사용하는 핵심 클래스
     * Spring의 JdbcTemplate, RestTemplate과 유사한 역할
     */
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> =
        KafkaTemplate(producerFactory())

    // ───────────────────────────────────────────
    // Consumer 설정
    // ───────────────────────────────────────────

    /**
     * Consumer 설정 맵
     */
    private fun consumerConfigs(): Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ConsumerConfig.GROUP_ID_CONFIG to "my-app-group",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
        // 처음부터 읽기
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        // 자동 커밋 비활성화 (수동 커밋 사용)
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        // JsonDeserializer에서 신뢰할 패키지
        JsonDeserializer.TRUSTED_PACKAGES to "com.example.*",
    )

    /**
     * ConsumerFactory: @KafkaListener가 사용할 Consumer 인스턴스를 생성하는 팩토리
     */
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> =
        DefaultKafkaConsumerFactory(consumerConfigs())

    /**
     * ConcurrentKafkaListenerContainerFactory:
     * @KafkaListener 어노테이션이 붙은 메서드를 실행하는 컨테이너를 생성하는 팩토리
     * concurrency: 동시에 실행할 컨슈머 스레드 수 (파티션 수 이하로 설정)
     */
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()
        // 동시 실행 스레드 수 (파티션 수와 동일하게 설정하면 최대 병렬 처리)
        factory.setConcurrency(3)
        // 수동 커밋 모드 (처리 완료 후 직접 커밋)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        return factory
    }
}
```

---

## 3. Producer 구현

### 3.1 기본 전송

```kotlin
// OrderProducer.kt
package com.example.producer

import com.example.dto.OrderCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderProducer(
    // Key: String, Value: Any (또는 구체 타입)
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TOPIC = "order-created"
    }

    /**
     * 기본 전송: send(topic, key, value)
     * Key는 파티션 결정에 사용됨 (같은 Key → 같은 파티션 → 순서 보장)
     */
    fun sendOrderCreated(event: OrderCreatedEvent) {
        kafkaTemplate.send(TOPIC, event.orderId, event)
        log.info("주문 이벤트 발행: orderId=${event.orderId}")
    }
}
```

### 3.2 전송 결과 확인 (CompletableFuture)

```kotlin
// OrderProducer.kt (전송 결과 확인 추가)
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

@Component
class OrderProducerWithCallback(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * send()는 CompletableFuture<SendResult>를 반환
     * 콜백을 등록하여 성공/실패 처리 가능
     */
    fun sendWithCallback(event: OrderCreatedEvent) {
        val future: CompletableFuture<SendResult<String, Any>> =
            kafkaTemplate.send("order-created", event.orderId, event)

        future.whenComplete { result, ex ->
            if (ex == null) {
                // 성공: RecordMetadata에서 토픽, 파티션, 오프셋 확인 가능
                val metadata = result.recordMetadata
                log.info(
                    "전송 성공 - topic=${metadata.topic()}, " +
                    "partition=${metadata.partition()}, " +
                    "offset=${metadata.offset()}"
                )
            } else {
                // 실패: 재시도 또는 알림 처리
                log.error("전송 실패 - orderId=${event.orderId}", ex)
                // 실패 시 대안 처리 (DB 저장, 알림 등)
            }
        }
    }

    /**
     * 동기적으로 전송 결과를 기다리는 방법 (권장하지 않음 - 블로킹 발생)
     * 테스트나 특수한 경우에만 사용
     */
    fun sendSync(event: OrderCreatedEvent) {
        try {
            val result = kafkaTemplate.send("order-created", event.orderId, event).get()
            log.info("동기 전송 성공: offset=${result.recordMetadata.offset()}")
        } catch (ex: Exception) {
            log.error("동기 전송 실패", ex)
            throw ex
        }
    }
}
```

### 3.3 Kotlin suspend 함수와 함께 사용

```kotlin
// OrderProducer.kt (Coroutine 버전)
import kotlinx.coroutines.future.await

@Component
class OrderProducerCoroutine(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * CompletableFuture를 await()로 코루틴 방식으로 기다림
     * 스레드를 블로킹하지 않고 중단(suspend)만 됨
     */
    suspend fun sendOrderCreated(event: OrderCreatedEvent): SendResult<String, Any> {
        return kafkaTemplate
            .send("order-created", event.orderId, event)
            .await() // kotlinx-coroutines-jdk8 의존성 필요
    }
}
```

```kotlin
// build.gradle.kts에 추가 필요
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
```

### 3.4 실전 예시: 주문 생성 시 이벤트 발행

```kotlin
// DTO
// OrderCreatedEvent.kt
package com.example.dto

import java.time.LocalDateTime

data class OrderCreatedEvent(
    val orderId: String,           // 주문 ID
    val customerId: String,        // 고객 ID
    val productId: String,         // 상품 ID
    val quantity: Int,             // 수량
    val totalPrice: Long,          // 총 금액
    val createdAt: LocalDateTime = LocalDateTime.now(), // 이벤트 발생 시각
)
```

```kotlin
// OrderService.kt
package com.example.service

import com.example.dto.OrderCreatedEvent
import com.example.producer.OrderProducer
import com.example.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderProducer: OrderProducer,
) {
    /**
     * 주문 생성 시 이벤트 발행 흐름:
     * 1. DB에 주문 저장
     * 2. Kafka에 이벤트 발행
     * 주의: DB와 Kafka는 단일 트랜잭션으로 묶이지 않음 (Outbox 패턴 참고)
     */
    @Transactional
    fun createOrder(customerId: String, productId: String, quantity: Int): String {
        val orderId = UUID.randomUUID().toString()

        // 1. DB 저장
        orderRepository.save(/* 주문 엔티티 */)

        // 2. Kafka 이벤트 발행
        val event = OrderCreatedEvent(
            orderId = orderId,
            customerId = customerId,
            productId = productId,
            quantity = quantity,
            totalPrice = calculatePrice(productId, quantity),
        )
        orderProducer.sendOrderCreated(event)

        return orderId
    }

    private fun calculatePrice(productId: String, quantity: Int): Long {
        // 가격 계산 로직
        return 10000L * quantity
    }
}
```

---

## 4. Consumer 구현

### 4.1 @KafkaListener 기본 사용법

```kotlin
// OrderEventConsumer.kt
package com.example.consumer

import com.example.dto.OrderCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * @KafkaListener 기본 사용
     * topics: 구독할 토픽 이름
     * groupId: 컨슈머 그룹 (없으면 application.yml의 group-id 사용)
     * containerFactory: 사용할 ListenerContainerFactory 빈 이름
     */
    @KafkaListener(
        topics = ["order-created"],
        groupId = "inventory-service-group",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleOrderCreated(event: OrderCreatedEvent) {
        log.info("주문 이벤트 수신: orderId=${event.orderId}")
        // 비즈니스 로직 처리
        decreaseInventory(event.productId, event.quantity)
    }

    private fun decreaseInventory(productId: String, quantity: Int) {
        log.info("재고 차감: productId=$productId, quantity=$quantity")
        // 재고 서비스 호출 또는 DB 업데이트
    }
}
```

### 4.2 ConsumerRecord 사용법

```kotlin
import org.apache.kafka.clients.consumer.ConsumerRecord

@Component
class DetailedConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * ConsumerRecord로 메타데이터(토픽, 파티션, 오프셋, Key, 헤더 등)까지 접근
     */
    @KafkaListener(topics = ["order-created"])
    fun handleWithMetadata(record: ConsumerRecord<String, OrderCreatedEvent>) {
        log.info(
            "수신 - topic=${record.topic()}, " +
            "partition=${record.partition()}, " +
            "offset=${record.offset()}, " +
            "key=${record.key()}, " +
            "timestamp=${record.timestamp()}"
        )

        val event = record.value()
        log.info("주문 이벤트: $event")

        // 헤더 접근
        record.headers().forEach { header ->
            log.info("헤더: ${header.key()} = ${String(header.value())}")
        }
    }
}
```

### 4.3 수동 커밋 (AckMode.MANUAL)

```kotlin
import org.springframework.kafka.support.Acknowledgment

@Component
class ManualAckConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 수동 커밋: 처리 완료 후 ack.acknowledge() 호출
     * enable-auto-commit: false, ack-mode: MANUAL_IMMEDIATE 설정 필요
     *
     * 장점: 메시지 처리 실패 시 커밋하지 않아 재처리 가능 (At-Least-Once 보장)
     * 주의: acknowledge() 호출 안 하면 같은 메시지를 계속 받음
     */
    @KafkaListener(topics = ["order-created"])
    fun handleWithManualAck(
        event: OrderCreatedEvent,
        ack: Acknowledgment, // 수동 커밋 객체
    ) {
        try {
            log.info("처리 시작: orderId=${event.orderId}")
            processOrder(event)

            // 성공 시 커밋 (이 오프셋까지 처리했다고 Kafka에 알림)
            ack.acknowledge()
            log.info("커밋 완료: orderId=${event.orderId}")

        } catch (ex: Exception) {
            log.error("처리 실패: orderId=${event.orderId}", ex)
            // acknowledge() 호출하지 않으면 이 메시지를 다시 받음
            // 또는 nack(재시도 대기시간)으로 명시적 재처리 요청
            ack.nack(Duration.ofSeconds(5)) // 5초 후 재처리
        }
    }

    private fun processOrder(event: OrderCreatedEvent) {
        // 비즈니스 로직
    }
}
```

### 4.4 에러 처리: DeadLetterPublishingRecoverer + DefaultErrorHandler

```kotlin
// KafkaConfig.kt에 에러 핸들러 빈 추가
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaErrorHandlerConfig(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    /**
     * DeadLetterPublishingRecoverer:
     * 최대 재시도 횟수 초과 시 메시지를 DLQ(Dead Letter Queue) 토픽으로 전송
     * 기본 DLQ 토픽명: {원본 토픽명}.DLT
     * 예: order-created → order-created.DLT
     */
    @Bean
    fun deadLetterPublishingRecoverer(): DeadLetterPublishingRecoverer =
        DeadLetterPublishingRecoverer(kafkaTemplate)

    /**
     * DefaultErrorHandler:
     * 컨슈머 처리 중 예외 발생 시 재시도 및 복구 처리
     * FixedBackOff(간격, 최대재시도횟수)
     */
    @Bean
    fun defaultErrorHandler(
        recoverer: DeadLetterPublishingRecoverer,
    ): DefaultErrorHandler {
        // 1초 간격으로 최대 3번 재시도, 그 후 DLQ로 전송
        val backOff = FixedBackOff(1000L, 3L)
        return DefaultErrorHandler(recoverer, backOff).apply {
            // 특정 예외는 재시도하지 않고 바로 DLQ로 전송
            addNotRetryableExceptions(IllegalArgumentException::class.java)
        }
    }
}
```

```kotlin
// ConcurrentKafkaListenerContainerFactory에 에러 핸들러 적용
@Bean
fun kafkaListenerContainerFactory(
    defaultErrorHandler: DefaultErrorHandler,
): ConcurrentKafkaListenerContainerFactory<String, Any> {
    val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
    factory.consumerFactory = consumerFactory()
    factory.setConcurrency(3)
    factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
    // 에러 핸들러 적용
    factory.setCommonErrorHandler(defaultErrorHandler)
    return factory
}
```

### 4.5 실전 예시: 주문 이벤트 수신 처리

```kotlin
// InventoryConsumer.kt
package com.example.consumer

@Component
class InventoryConsumer(
    private val inventoryService: InventoryService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["order-created"],
        groupId = "inventory-service-group",
    )
    fun handleOrderCreated(
        record: ConsumerRecord<String, OrderCreatedEvent>,
        ack: Acknowledgment,
    ) {
        val event = record.value()
        log.info("재고 차감 시작: orderId=${event.orderId}, productId=${event.productId}")

        try {
            inventoryService.decreaseStock(event.productId, event.quantity)
            ack.acknowledge()
            log.info("재고 차감 완료: orderId=${event.orderId}")
        } catch (ex: InsufficientStockException) {
            // 재고 부족: 주문 취소 이벤트 발행 후 커밋
            log.warn("재고 부족: orderId=${event.orderId}")
            // kafkaTemplate.send("order-cancelled", event.orderId, ...)
            ack.acknowledge() // 이 메시지는 처리했음을 커밋
        } catch (ex: Exception) {
            log.error("재고 차감 실패: orderId=${event.orderId}", ex)
            // acknowledge() 호출 안 함 → 재처리 또는 DLQ로 이동
            throw ex // DefaultErrorHandler가 재시도/DLQ 처리
        }
    }
}
```

---

## 5. 고급 패턴

### 5.1 여러 토픽 구독

```kotlin
@Component
class MultiTopicConsumer {

    /**
     * 여러 토픽을 하나의 리스너에서 처리
     * 토픽별 분기가 필요하면 ConsumerRecord의 topic() 확인
     */
    @KafkaListener(topics = ["order-created", "order-updated", "order-cancelled"])
    fun handleMultipleTopics(record: ConsumerRecord<String, Any>) {
        when (record.topic()) {
            "order-created" -> handleOrderCreated(record.value() as OrderCreatedEvent)
            "order-updated" -> handleOrderUpdated(record.value() as OrderUpdatedEvent)
            "order-cancelled" -> handleOrderCancelled(record.value() as OrderCancelledEvent)
        }
    }

    /**
     * 토픽 패턴으로 구독 (정규식 지원)
     * "order-.*" → order-로 시작하는 모든 토픽 구독
     */
    @KafkaListener(topicPattern = "order-.*")
    fun handleOrderTopicsByPattern(record: ConsumerRecord<String, Any>) {
        // order-로 시작하는 모든 토픽의 메시지 처리
    }

    private fun handleOrderCreated(event: OrderCreatedEvent) { /* ... */ }
    private fun handleOrderUpdated(event: OrderUpdatedEvent) { /* ... */ }
    private fun handleOrderCancelled(event: OrderCancelledEvent) { /* ... */ }
}
```

### 5.2 파티션 지정 전송

```kotlin
@Component
class PartitionAwareProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    /**
     * 특정 파티션에 직접 전송
     * 실무에서는 Key를 사용한 자동 파티션 분배를 권장
     */
    fun sendToSpecificPartition(event: OrderCreatedEvent, partition: Int) {
        // send(topic, partition, timestamp, key, value)
        kafkaTemplate.send("order-created", partition, null, event.orderId, event)
    }

    /**
     * Key 기반 파티션 분배 (권장 방식)
     * 같은 customerId → 같은 파티션 → 고객별 순서 보장
     */
    fun sendByCustomerId(event: OrderCreatedEvent) {
        kafkaTemplate.send("order-created", event.customerId, event)
    }
}
```

### 5.3 트랜잭션 처리

```kotlin
// application.yml에 트랜잭션 ID 접두어 설정
// spring.kafka.producer.transaction-id-prefix: order-tx-

@Component
class TransactionalProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    /**
     * Kafka 트랜잭션: 여러 토픽/파티션에 원자적으로 메시지 발행
     * 모두 성공하거나 모두 실패
     * 주의: DB 트랜잭션과 다름 (Kafka 내부 트랜잭션)
     */
    fun sendTransactional(orderEvent: OrderCreatedEvent, paymentEvent: PaymentRequestedEvent) {
        kafkaTemplate.executeInTransaction { ops ->
            ops.send("order-created", orderEvent.orderId, orderEvent)
            ops.send("payment-requested", paymentEvent.orderId, paymentEvent)
            // 하나라도 실패하면 둘 다 롤백
        }
    }
}
```

### 5.4 메시지 헤더 사용

```kotlin
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.messaging.support.MessageBuilder

@Component
class HeaderAwareProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    /**
     * 메시지 헤더에 메타데이터 추가 (버전, 추적 ID 등)
     * 헤더는 byte[] 타입
     */
    fun sendWithHeaders(event: OrderCreatedEvent, traceId: String) {
        val message = MessageBuilder
            .withPayload(event)
            .setHeader(KafkaHeaders.TOPIC, "order-created")
            .setHeader(KafkaHeaders.KEY, event.orderId)
            .setHeader("X-Trace-Id", traceId)
            .setHeader("X-Event-Version", "1.0")
            .setHeader("X-Source-Service", "order-service")
            .build()

        kafkaTemplate.send(message)
    }
}
```

```kotlin
// 헤더 수신 측
@Component
class HeaderAwareConsumer {
    @KafkaListener(topics = ["order-created"])
    fun handleWithHeaders(
        event: OrderCreatedEvent,
        @Header("X-Trace-Id") traceId: String?,       // 헤더 직접 바인딩
        @Header("X-Event-Version") version: String?,
    ) {
        log.info("traceId=$traceId, version=$version, event=$event")
    }
}
```

### 5.5 Dead Letter Queue (DLQ) 패턴

DLQ는 처리 실패한 메시지를 격리하여 시스템 전체 중단 없이 나중에 재처리할 수 있게 합니다.

```
정상 흐름:    order-created → [Consumer] → 처리 성공 → 커밋
실패 흐름:    order-created → [Consumer] → 실패(3회) → order-created.DLT → [DLQ Consumer] → 수동/자동 재처리
```

```kotlin
// DLQ 컨슈머: 실패 메시지 모니터링 및 재처리
@Component
class OrderDlqConsumer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val alertService: AlertService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * DLT(Dead Letter Topic) 컨슈머
     * 실패 원인 분석, 알림 발송, 재처리 등 수행
     */
    @KafkaListener(
        topics = ["order-created.DLT"],
        groupId = "order-dlq-group",
    )
    fun handleDeadLetter(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
    ) {
        log.error("DLQ 메시지 수신: key=${record.key()}, value=${record.value()}")

        // 실패 원인 헤더 확인 (Spring Kafka가 자동으로 추가)
        val exception = record.headers()
            .lastHeader("kafka_dlt-exception-message")
            ?.let { String(it.value()) }
        log.error("실패 원인: $exception")

        // 운영팀 알림
        alertService.sendAlert("DLQ 메시지 발생: key=${record.key()}, reason=$exception")

        // 재처리 가능한 경우 원본 토픽으로 재발행
        // kafkaTemplate.send("order-created", record.key() as String, record.value())

        ack.acknowledge()
    }
}
```

### 5.6 Outbox 패턴 (DB 트랜잭션 + Kafka 발행 보장)

**문제:** DB 저장 성공 → Kafka 발행 실패 → 데이터 불일치

**해결:** DB 트랜잭션에 Outbox 테이블 저장도 포함, 별도 프로세스가 Outbox를 읽어 Kafka에 발행

```kotlin
// OutboxEvent.kt (엔티티)
@Entity
@Table(name = "outbox_events")
data class OutboxEvent(
    @Id val id: String = UUID.randomUUID().toString(),
    val aggregateType: String,  // "ORDER"
    val aggregateId: String,    // 주문 ID
    val eventType: String,      // "OrderCreated"
    val payload: String,        // JSON 직렬화된 이벤트
    val status: String = "PENDING", // PENDING, SENT, FAILED
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
```

```kotlin
// OrderService.kt (Outbox 패턴 적용)
@Service
class OrderServiceWithOutbox(
    private val orderRepository: OrderRepository,
    private val outboxRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
) {
    /**
     * Outbox 패턴:
     * 1. DB 트랜잭션 내에서 주문 + Outbox 이벤트를 함께 저장
     * 2. 별도 스케줄러가 Outbox에서 읽어 Kafka로 발행
     * → DB와 Kafka 간 원자성 보장
     */
    @Transactional
    fun createOrder(customerId: String, productId: String, quantity: Int): String {
        val orderId = UUID.randomUUID().toString()

        // 1. 주문 저장
        orderRepository.save(/* 주문 엔티티 */)

        // 2. Outbox 이벤트 저장 (같은 트랜잭션!)
        val event = OrderCreatedEvent(orderId, customerId, productId, quantity, 10000L)
        outboxRepository.save(
            OutboxEvent(
                aggregateType = "ORDER",
                aggregateId = orderId,
                eventType = "OrderCreated",
                payload = objectMapper.writeValueAsString(event),
            )
        )
        // DB 트랜잭션 커밋 시 주문 + Outbox 이벤트가 함께 저장됨

        return orderId
    }
}
```

```kotlin
// OutboxEventPublisher.kt (스케줄러)
@Component
class OutboxEventPublisher(
    private val outboxRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 주기적으로 PENDING 상태 Outbox 이벤트를 Kafka로 발행
     * 실제 운영에서는 Debezium CDC(Change Data Capture)를 권장
     */
    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    @Transactional
    fun publishPendingEvents() {
        val pendingEvents = outboxRepository.findByStatus("PENDING")
        pendingEvents.forEach { outboxEvent ->
            try {
                kafkaTemplate.send(
                    "order-created",
                    outboxEvent.aggregateId,
                    objectMapper.readValue(outboxEvent.payload, OrderCreatedEvent::class.java),
                ).get() // 동기 대기 (발행 확인 후 상태 변경)

                outboxEvent.status = "SENT"
                outboxRepository.save(outboxEvent)
            } catch (ex: Exception) {
                log.error("Outbox 이벤트 발행 실패: id=${outboxEvent.id}", ex)
                outboxEvent.status = "FAILED"
                outboxRepository.save(outboxEvent)
            }
        }
    }
}
```

---

## 6. Kotlin Coroutine과 Kafka 통합

### 6.1 @KafkaListener에서 suspend 함수 사용

`@KafkaListener`는 기본적으로 일반 함수를 기대하므로 `suspend` 함수를 직접 사용할 수 없습니다. `runBlocking`으로 코루틴 컨텍스트를 생성하여 우회할 수 있습니다.

```kotlin
import kotlinx.coroutines.runBlocking

@Component
class CoroutineConsumer(
    private val inventoryService: InventoryService, // suspend 함수를 가진 서비스
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * @KafkaListener는 suspend 함수를 직접 지원하지 않으므로
     * runBlocking으로 코루틴 스코프를 열어 suspend 함수 호출
     *
     * 주의: runBlocking은 현재 스레드를 블로킹하므로
     * 컨슈머 스레드 수(concurrency)를 고려해야 함
     */
    @KafkaListener(topics = ["order-created"])
    fun handleOrderCreated(event: OrderCreatedEvent, ack: Acknowledgment) {
        runBlocking {
            try {
                // suspend 함수 호출 가능
                inventoryService.decreaseStockSuspend(event.productId, event.quantity)
                ack.acknowledge()
            } catch (ex: Exception) {
                log.error("처리 실패", ex)
                throw ex
            }
        }
    }
}
```

```kotlin
// CoroutineScope를 클래스 수준에서 관리하는 방법 (더 세밀한 제어)
@Component
class ScopeAwareConsumer : CoroutineScope by CoroutineScope(Dispatchers.IO) {

    @KafkaListener(topics = ["order-created"])
    fun handleOrderCreated(event: OrderCreatedEvent) {
        // launch로 별도 코루틴에서 처리 (fire-and-forget)
        // 주의: 예외 처리와 커밋 타이밍 신중히 고려해야 함
        launch {
            processEvent(event)
        }
    }

    private suspend fun processEvent(event: OrderCreatedEvent) {
        // suspend 함수 처리
    }
}
```

### 6.2 ReactiveKafka 소개

Spring Kafka는 Project Reactor 기반의 ReactiveKafkaProducerTemplate과 ReactiveKafkaConsumerTemplate을 제공합니다.

```kotlin
// build.gradle.kts에 추가
implementation("io.projectreactor.kafka:reactor-kafka")
```

```kotlin
// ReactiveKafkaConfig.kt
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions

@Configuration
class ReactiveKafkaConfig {

    @Bean
    fun reactiveKafkaSender(): ReactiveKafkaProducerTemplate<String, Any> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        )
        return ReactiveKafkaProducerTemplate(SenderOptions.create(props))
    }
}
```

```kotlin
// ReactiveOrderProducer.kt
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Mono

@Component
class ReactiveOrderProducer(
    private val reactiveKafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
) {
    /**
     * Reactive Producer: Mono/Flux를 반환하는 비동기 방식
     */
    fun sendOrderEvent(event: OrderCreatedEvent): Mono<SenderResult<Void>> {
        return reactiveKafkaTemplate.send("order-created", event.orderId, event)
            .doOnSuccess { result ->
                log.info("발행 성공: offset=${result.recordMetadata().offset()}")
            }
            .doOnError { ex ->
                log.error("발행 실패", ex)
            }
    }
}
```

---

## 7. 테스트

### 7.1 EmbeddedKafka 설정

```kotlin
// build.gradle.kts (이미 추가했다면 생략)
testImplementation("org.springframework.kafka:spring-kafka-test")
```

```kotlin
// KafkaTestConfig.kt
package com.example.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.test.EmbeddedKafkaBroker

@TestConfiguration
class KafkaTestConfig {
    // EmbeddedKafka는 @EmbeddedKafka 어노테이션으로도 설정 가능
}
```

### 7.2 Producer 테스트

```kotlin
// OrderProducerTest.kt
package com.example.producer

import com.example.dto.OrderCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.test.context.ActiveProfiles
import java.time.Duration

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,                         // 파티션 수
    topics = ["order-created"],             // 생성할 토픽 목록
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9092", // 포트 지정
        "port=9092",
    ],
)
class OrderProducerTest {

    @Autowired
    private lateinit var orderProducer: OrderProducer

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Test
    fun `주문 이벤트 발행 시 Kafka 토픽에 메시지가 존재해야 한다`() {
        // given
        val event = OrderCreatedEvent(
            orderId = "test-order-001",
            customerId = "customer-001",
            productId = "product-001",
            quantity = 2,
            totalPrice = 20000L,
        )

        // when
        orderProducer.sendOrderCreated(event)

        // then: 테스트용 Consumer로 메시지 수신 확인
        val consumerProps = KafkaTestUtils.consumerProps(
            "test-group",
            "true",
            embeddedKafkaBroker,
        )
        val consumer = DefaultKafkaConsumerFactory<String, String>(consumerProps)
            .createConsumer()

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "order-created")

        val records: ConsumerRecord<String, String> =
            KafkaTestUtils.getSingleRecord(consumer, "order-created", Duration.ofSeconds(5).toMillis())

        assertNotNull(records)
        assertEquals("test-order-001", records.key())
        assertTrue(records.value().contains("test-order-001"))

        consumer.close()
    }
}
```

### 7.3 Consumer 테스트

```kotlin
// OrderEventConsumerTest.kt
package com.example.consumer

import com.example.dto.OrderCreatedEvent
import com.example.service.InventoryService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = ["order-created"],
)
class OrderEventConsumerTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @MockBean
    private lateinit var inventoryService: InventoryService

    /**
     * CountDownLatch: 비동기 처리 완료를 기다리기 위해 사용
     * 컨슈머가 메시지를 처리하면 latch.countDown() 호출
     */
    @Test
    fun `Kafka 메시지 수신 시 재고 차감 서비스가 호출되어야 한다`() {
        // given
        val latch = CountDownLatch(1)
        doAnswer { latch.countDown() }
            .`when`(inventoryService).decreaseStock(anyString(), anyInt())

        val event = OrderCreatedEvent(
            orderId = "test-order-001",
            customerId = "customer-001",
            productId = "product-001",
            quantity = 2,
            totalPrice = 20000L,
        )

        // when: 메시지 발행
        kafkaTemplate.send("order-created", event.orderId, event)

        // then: 컨슈머가 처리할 때까지 최대 10초 대기
        val messageReceived = latch.await(10, TimeUnit.SECONDS)

        assertTrue(messageReceived, "컨슈머가 메시지를 처리하지 못했습니다")
        verify(inventoryService, times(1)).decreaseStock("product-001", 2)
    }
}
```

```kotlin
// application-test.yml (테스트 프로파일 설정)
spring:
  kafka:
    # EmbeddedKafka 사용 시 자동으로 주소가 교체됨
    bootstrap-servers: ${spring.embedded.kafka.brokers}
    consumer:
      group-id: test-group
      auto-offset-reset: earliest
      enable-auto-commit: true
```

---

## 8. 실무 운영 팁

### 8.1 토픽 네이밍 컨벤션

**권장 형식:** `{서비스명}.{집합체}.{이벤트명}`

```
# 좋은 예시 (케밥케이스)
order-service.order.created
order-service.order.cancelled
payment-service.payment.completed
inventory-service.stock.decreased

# 환경별 구분 (접두어 방식)
prod.order-service.order.created
dev.order-service.order.created

# 나쁜 예시 (피해야 할 패턴)
orderCreated          # 서비스 구분 없음
ORDER_CREATED         # 스네이크케이스 + 대문자
order_created_topic   # "topic" 불필요한 중복
```

**DLQ 토픽:** `{원본 토픽명}.DLT` (Spring Kafka 기본 규칙)
```
order-service.order.created.DLT
```

### 8.2 파티션 수 결정 기준

```
파티션 수 = max(목표 처리량 / 단일 컨슈머 처리량, 목표 컨슈머 수)
```

**실무 가이드라인:**
- 시작 시 **과하게 늘리기 어렵고, 줄이기는 더 어려움** (늘리는 것만 가능)
- 처음에는 `3 ~ 12` 정도로 시작
- 파티션 수 >= 컨슈머 수 (컨슈머가 파티션보다 많으면 유휴 컨슈머 발생)
- 순서가 중요한 경우: Key를 활용하고 파티션을 신중히 설계

```
예시: 초당 10만 건 처리 목표
- 단일 컨슈머 처리량: 1만 건/초
- 필요 컨슈머 수: 10개
- 파티션 수: 10개 (또는 여유있게 12개)
```

### 8.3 컨슈머 그룹 설계

```
# 서비스별로 별도 그룹 ID 사용
order-created 토픽
  └── inventory-service-group   (재고 서비스)
  └── payment-service-group     (결제 서비스)
  └── notification-service-group (알림 서비스)
  └── analytics-service-group   (분석 서비스)

# 같은 서비스 내 용도별로도 분리 가능
  └── order-service-main-group
  └── order-service-audit-group
```

**컨슈머 그룹 내 인스턴스 수:**
```kotlin
// 각 서비스 인스턴스마다 동일한 group-id를 사용하면
// Kafka가 자동으로 파티션을 분배 (리밸런싱)
spring.kafka.consumer.group-id: inventory-service-group

// 인스턴스 3개, 파티션 6개 → 각 인스턴스가 2개 파티션 담당
// 인스턴스 6개, 파티션 6개 → 각 인스턴스가 1개 파티션 담당 (최대 병렬)
// 인스턴스 7개, 파티션 6개 → 1개 인스턴스는 유휴 상태
```

### 8.4 모니터링 (Lag 확인)

**Consumer Lag = 최신 오프셋 - 컨슈머 현재 오프셋**

Lag이 계속 증가하면 컨슈머 처리 속도가 프로듀서 발행 속도를 따라가지 못하는 것입니다.

```bash
# Kafka CLI로 Lag 확인
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group inventory-service-group

# 결과 예시:
# GROUP                     TOPIC          PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
# inventory-service-group   order-created  0          1250            1255            5
# inventory-service-group   order-created  1          980             980             0
# inventory-service-group   order-created  2          1100            1103            3
```

**Spring Actuator + Micrometer로 Lag 모니터링:**

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-registry-prometheus")
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

Prometheus + Grafana에서 `kafka_consumer_fetch_manager_records_lag` 메트릭으로 Lag 시각화

### 8.5 자주 발생하는 문제와 해결법

#### 문제 1: 메시지 중복 처리 (At-Least-Once)

```kotlin
// 원인: 처리 성공 후 acknowledge() 전에 컨슈머 재시작
// 해결: 멱등성(Idempotency) 보장 로직 구현

@Component
class IdempotentConsumer(
    private val processedEventRepository: ProcessedEventRepository,
) {
    @KafkaListener(topics = ["order-created"])
    fun handleOrderCreated(record: ConsumerRecord<String, OrderCreatedEvent>, ack: Acknowledgment) {
        val eventId = record.key()

        // 이미 처리한 이벤트인지 확인
        if (processedEventRepository.existsByEventId(eventId)) {
            log.warn("중복 이벤트 무시: eventId=$eventId")
            ack.acknowledge()
            return
        }

        // 처리 + 처리 이력 저장 (트랜잭션으로 묶기)
        processEventAndSaveHistory(record.value(), eventId)
        ack.acknowledge()
    }
}
```

#### 문제 2: 역직렬화 실패 (Deserialization Exception)

```kotlin
// 원인: 메시지 스키마 변경, 잘못된 형식의 메시지
// 해결: ErrorHandlingDeserializer 사용

// application.yml
spring:
  kafka:
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
```

#### 문제 3: 리밸런싱 시간이 너무 김

```yaml
# application.yml
spring:
  kafka:
    consumer:
      properties:
        # 컨슈머가 heartbeat를 보내는 간격 (기본 3초)
        heartbeat.interval.ms: 3000
        # 이 시간 내에 heartbeat 없으면 그룹에서 제외 (기본 45초)
        session.timeout.ms: 45000
        # poll() 호출 간 최대 간격 (기본 5분)
        # 이 시간 초과 시 리밸런싱 발생
        max.poll.interval.ms: 300000
        # 한 번에 가져오는 최대 레코드 수 (처리가 오래 걸리면 줄일 것)
        max.poll.records: 500
```

#### 문제 4: 오프셋 커밋 누락

```kotlin
// 원인: 예외 발생 시 acknowledge() 미호출
// 해결: try-finally로 확실한 처리
@KafkaListener(topics = ["order-created"])
fun handleSafely(event: OrderCreatedEvent, ack: Acknowledgment) {
    var success = false
    try {
        processEvent(event)
        success = true
    } finally {
        if (success) {
            ack.acknowledge()
        }
        // 실패 시 에러 핸들러(DefaultErrorHandler)가 재시도/DLQ 처리
    }
}
```

#### 문제 5: 컨슈머 그룹 ID 중복

```yaml
# 잘못된 설정: 모든 서비스가 같은 그룹 ID 사용
# inventory-service와 notification-service가 동일한 group-id를 쓰면
# 메시지가 두 서비스 중 하나로만 전달됨 (분산 처리, 독립 소비 불가)

# 올바른 설정: 각 서비스마다 고유한 그룹 ID
# inventory-service
spring.kafka.consumer.group-id: inventory-service-group

# notification-service
spring.kafka.consumer.group-id: notification-service-group
```

---

## 9. 학습 체크리스트

### Kafka 핵심 개념

- [ ] Kafka가 필요한 이유와 HTTP 방식과의 차이를 설명할 수 있다
- [ ] Topic, Partition, Offset, Producer, Consumer, Consumer Group의 관계를 설명할 수 있다
- [ ] 같은 Key를 가진 메시지가 같은 파티션으로 가는 이유를 설명할 수 있다
- [ ] Consumer Group을 여러 서비스가 독립적으로 사용하는 원리를 설명할 수 있다
- [ ] Kafka와 RabbitMQ의 차이를 설명하고 적절한 사용처를 선택할 수 있다

### Spring Kafka 환경 설정

- [ ] build.gradle.kts에 spring-kafka 의존성을 추가할 수 있다
- [ ] application.yml에서 producer/consumer 설정을 구분하여 작성할 수 있다
- [ ] KafkaConfig 클래스에서 ProducerFactory, ConsumerFactory, KafkaTemplate, ListenerContainerFactory를 빈으로 등록할 수 있다
- [ ] JsonSerializer/JsonDeserializer를 사용하여 Kotlin data class를 JSON으로 직렬화할 수 있다
- [ ] `spring.json.trusted.packages` 설정의 필요성을 설명할 수 있다

### Producer 구현

- [ ] KafkaTemplate을 주입받아 메시지를 발행할 수 있다
- [ ] `send(topic, key, value)` 메서드로 메시지를 발행할 수 있다
- [ ] CompletableFuture의 `whenComplete`로 전송 성공/실패 콜백을 처리할 수 있다
- [ ] Kotlin 코루틴에서 `await()`으로 비동기 전송 결과를 처리할 수 있다
- [ ] `acks`, `retries`, `enable.idempotence` 설정의 의미를 설명할 수 있다

### Consumer 구현

- [ ] `@KafkaListener`로 특정 토픽의 메시지를 수신할 수 있다
- [ ] `ConsumerRecord`로 메시지 메타데이터(파티션, 오프셋 등)에 접근할 수 있다
- [ ] `Acknowledgment.acknowledge()`로 수동 커밋을 처리할 수 있다
- [ ] `DefaultErrorHandler`와 `DeadLetterPublishingRecoverer`로 에러를 처리할 수 있다
- [ ] DLQ 토픽을 구독하여 실패 메시지를 모니터링/재처리할 수 있다

### 고급 패턴

- [ ] 여러 토픽을 하나의 `@KafkaListener`에서 처리할 수 있다
- [ ] 토픽 패턴(정규식)으로 여러 토픽을 구독할 수 있다
- [ ] `executeInTransaction`으로 Kafka 트랜잭션을 사용할 수 있다
- [ ] 메시지 헤더에 메타데이터를 추가하고 수신 측에서 읽을 수 있다
- [ ] Outbox 패턴을 이해하고 DB와 Kafka 간의 원자성을 보장하는 이유를 설명할 수 있다

### Kotlin Coroutine 통합

- [ ] `@KafkaListener` 메서드에서 `runBlocking`으로 suspend 함수를 호출할 수 있다
- [ ] ReactiveKafka와 일반 Kafka의 차이를 설명할 수 있다

### 테스트

- [ ] `@EmbeddedKafka`로 실제 Kafka 없이 테스트 환경을 구성할 수 있다
- [ ] `KafkaTestUtils`로 테스트용 Consumer를 생성하여 발행된 메시지를 검증할 수 있다
- [ ] `CountDownLatch`를 활용하여 비동기 컨슈머 처리를 테스트할 수 있다

### 실무 운영

- [ ] 팀 내에서 일관된 토픽 네이밍 컨벤션을 적용할 수 있다
- [ ] 파티션 수 결정 시 처리량과 컨슈머 수를 고려할 수 있다
- [ ] Consumer Lag의 의미와 CLI로 확인하는 방법을 알고 있다
- [ ] 메시지 중복 처리 문제를 멱등성으로 해결할 수 있다
- [ ] 역직렬화 실패, 리밸런싱 문제의 일반적인 해결법을 알고 있다

---

## 참고 자료

- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka 공식 문서](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Spring Kafka GitHub](https://github.com/spring-projects/spring-kafka)
- [Confluent Kafka 튜토리얼](https://developer.confluent.io/tutorials/)

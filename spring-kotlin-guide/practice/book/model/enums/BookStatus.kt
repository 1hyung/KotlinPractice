package com.example.practice.book.model.enums

/**
 * 도서 상태
 *
 * 도서의 현재 상태를 나타냅니다.
 */
enum class BookStatus(val description: String) {
    AVAILABLE("대출 가능"),           // 도서관에 있고 대출 가능
    BORROWED("대출 중"),             // 누군가 대출 중
    RESERVED("예약됨"),              // 다른 사람이 예약함
    LOST("분실"),                    // 분실됨
    DAMAGED("훼손"),                 // 훼손되어 대출 불가
    DISCONTINUED("폐기")             // 폐기됨
}

/**
 * 상태 전이 규칙
 *
 * AVAILABLE → BORROWED (대출)
 * AVAILABLE → RESERVED (예약)
 * BORROWED → AVAILABLE (반납)
 * BORROWED → LOST (분실 신고)
 * BORROWED → DAMAGED (훼손 신고)
 * ANY → DISCONTINUED (폐기 처리)
 *
 * TODO: 상태 전이가 유효한지 검증하는 함수를 작성하세요
 */
fun BookStatus.canTransitionTo(newStatus: BookStatus): Boolean {
    return when (this) {
        BookStatus.AVAILABLE -> newStatus in listOf(
            BookStatus.BORROWED,
            BookStatus.RESERVED,
            BookStatus.DISCONTINUED
        )
        BookStatus.BORROWED -> newStatus in listOf(
            BookStatus.AVAILABLE,  // 반납
            BookStatus.LOST,
            BookStatus.DAMAGED,
            BookStatus.DISCONTINUED
        )
        BookStatus.RESERVED -> newStatus in listOf(
            BookStatus.AVAILABLE,  // 예약 취소
            BookStatus.BORROWED,   // 예약자가 대출
            BookStatus.DISCONTINUED
        )
        BookStatus.LOST -> newStatus in listOf(
            BookStatus.AVAILABLE,  // 찾음
            BookStatus.DISCONTINUED
        )
        BookStatus.DAMAGED -> newStatus in listOf(
            BookStatus.AVAILABLE,  // 수리 완료
            BookStatus.DISCONTINUED
        )
        BookStatus.DISCONTINUED -> false  // 폐기된 책은 복구 불가
    }
}

/**
 * TODO: 상태 전이 예외 클래스를 작성하세요
 *
 * 힌트:
 * - IllegalStateException을 상속받으세요
 * - 현재 상태와 시도한 상태를 메시지에 포함하세요
 */
class InvalidBookStatusTransitionException(
    currentStatus: BookStatus,
    attemptedStatus: BookStatus
) : IllegalStateException(
    "도서 상태를 ${currentStatus.description}에서 ${attemptedStatus.description}(으)로 변경할 수 없습니다"
)
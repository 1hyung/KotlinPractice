/*// 두 정수의 차 단일 표현식으로 작성
class Solution {
    fun solution(num1: Int, num2: Int): Int = num1 - num2
}*/

/*// 두 정수의 곱을 구하는 함수 단일 표현식 함수로 표현
class Solution {
    fun solution(num1: Int, num2: Int): Int = num1 * num2
}*/

/*// 두 정수로 나눈 몫을 반환
class Solution {
    fun solution(num1: Int, num2: Int): Int = num1 / num2
}*/

/*
// 출생 연도 구하기
class Solution {
    fun solution(age: Int): Int = 2022 - age + 1
}*/

/*
// 두 수가 같으면 1, 다르면 -1을 반환
class Solution {
    fun solution(num1: Int, num2: Int): Int = if (num1 == num2) 1 else -1
}*/

/*// 두 정수의 합
class Solution {
    fun solution(num1: Int, num2: Int): Int = num1 + num2
}*/

/*
// 두 수의 나눗셈
// num1에 num2로 나눈 값에 1000을 곱한 뒤 정수 부분을 반환
// num1에 1000을 곱한 뒤 num2로 나누면 매개변수가 Int이기 때문에 정수 반환
// Kotlin에서는 정수 연산이므로 소수점 이하가 자동으로 제거
class Solution {
    fun solution(num1: Int, num2: Int): Int = num1 * 1000 / num2
}*/

/*
// 각도기
// angle이 매개변수로 주어질 때 예각일 때 1, 직각일 때 2, 둔각일 때 3, 평각일 때 4를 반환
class Solution {
    fun solution(angle: Int): Int = when {
        angle < 90 -> 1 // 예각
        angle == 90 -> 2 // 직각
        angle < 180 -> 3 // 둔각
        else -> 4 // 평각
    }
}*/

/*
// 짝수의 합
// n 이하의 모든 짝수를 모두 더한 값을 반환
class Solution {
    fun solution(n: Int): Int =
        (2..n step 2) // 2부터 n까지 2씩 증가
            .sum() // 해당 숫자들을 모두 더함
}*/

/*
// 짝수와 홀수
// 정수 num이 짝수일 경우 "Even"을 반환하고 홀수인 경우 "Odd"를 반환
class Solution {
    fun solution(num: Int): String = when (num % 2) {
        0 -> "Even"
        else -> "Odd"
    }
}*/
/*
// 자릿수 더하기
class Solution {
    fun solution(n: Int): Int =
        n.toString() // 숫자를 문자열로 변환 (예: 123 → "123")
            .sumOf { it.digitToInt() } // 각 문자를 숫자로 변환 후 합산
}*/

/*
// 약수의 합
class Solution {
    fun solution(n: Int): Int =
        (1..n) // 범위 생성
            .filter { n % it == 0 } // n을 it으로 나눌 때 0이 되는 수 필터링 (약수 필터링)
            .sum() // 약수 합산
}*/

// 나머지가 1이 되는 수 찾기
class Solution {
    fun solution(n: Int): Int =
        (2 until n) // 2부터 n-1까지 범위 생성 (n 포함 x)
            .first { n % it == 1 }// 나머지 1이 되는 첫 번째 숫자를 찾고 반환
}
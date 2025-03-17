// 0도 초과 90도 미만은 예각, 90도는 직각, 90도 초과 180도 미만은 둔각 180도는 평각으로 분류
// 각 angle이 매개변수로 주어질 때 예각일 때 1, 직각일 때 2, 둔각일 때 3, 평각일 때 4를 return하도록 solution 함수
class Solution {
    // 주어진 각도를 조건에 따라 분류하여 반환하는 함수
    fun solution(angle: Int): Int = when {
        angle < 90 -> 1 //예각
        angle == 90 -> 2 //직각
        angle < 180 -> 3 //둔각
        else -> 4 //평각
    }
}
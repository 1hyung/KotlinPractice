package moovStudy

//객체(Object)와 인스턴스(Instance)는 기본적으로 같은 것을 의미 클래스라는 설계도에 따라 만들어진 '실제 사물'을 말함
/*
자동차 설계도로 만들어진 실제 자동차 : 설계도만 가지고는 운전할 수 없음
설계도를 바탕으로 실제로 만들어진 A자동차, B자동차가 있어야 운전할 수 있음
이 실제 자동차들이 객체 또는 인스턴스*/
/*
붕어빵 틀로 찍어낸 실제 붕어빵 : 붕어빵 틀 하나로 수많은 붕어빵을 찍어낼 수 있음
이 하나하나의 붕어빵들이 바로 객체 또는 인스턴스
 */
/* 프로그래밍에서 클래스를 기반으로 메모리에 생성된 것을 객체/인스턴스라고 함
예를 들어, Person Class 설계도를 바탕으로 '철수'라는 실제 사람을 만들거나, '영희'라는 실제 사람을 만들 수 있음
철수와 영희는 같은 Person 클래스의 인스턴스지만, 각각 다른 속성 값(이름, 나이)을 가질 수 있음
 */
fun main() {
    // Person 클래스의 설계도를 바탕으로 '철수'라는 인스턴스(객체)를 만듬
    val cheolsu: Person = Person() // 'cheolsu'는 Person 클래스의 인스턴스
    cheolsu.name = "철수" // 철수 인스턴스의 이름을 "철수"로 설정
    cheolsu.age = 20 // 철수 인스턴스의 나이를 20으로 설정

    // Person 클래스의 설계도를 바탕으로 '영희'라는 또 다른 인스턴스(객체)를 만듬
    val younghee: Person = Person() // 'younghee'라는 또 다른 Person 클래스의 인스턴스
    younghee.name = "영희" // 영희 인스턴스의 이름을 "영희"로 설정
    younghee.age = 22 // 영희 인스턴스의 나이를 22로 설정

    // 각 인스턴스(객체)의 동작을 실행
    cheolsu.walk()   // 출력: 철수이(가) 걷습니다.
    younghee.talk()  // 출력: 영희이(가) 이야기합니다.
}
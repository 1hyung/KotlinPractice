data class Person2(var name: String = "", var age: Int = 0)

class Book(var name: String, var price: Int) {
    fun discount() {
        price -= 2000
    }
}

fun main() {
    var price = 5000

    /*
        아래처럼 apply 함수를 적용하면 간단하고 가독성 좋게 코드 작성 가능
        var a = Book("원형의 코틀린", 10000)
        a.name = "[초특가]" + a.name
        a.discount()*/


    var a = Book("원형의 코틀린", 10000).apply {
        name = "[초특가]" + name
        discount()
    }
    println(a)

    a.apply {
        println("상품명 run: ${name}, 가격 run: ${price}원")
    }

    a.run {
        println("상품명 run: ${name}, 가격 run: ${price}원")
    }

    with(a) {
        println("상품명 with: ${name}, 가격 with: ${price}")
    }


    a.let {
        println("상품명 let it: ${it.name}, 가격 let it: ${it.price}원 ")
    }

    a.let { myBook ->
        println("상품명 let myBook: ${myBook.name}, 가격 let myBook: ${myBook.price}원 ")
    }

    // Person2 객체를 생성하고, apply 블록 안에서 속성을 설정합니다.
    val person1 = Person2().apply {
        // this.name = "김철수" // this 생략 가능
        name = "김철수"       // Person 객체(this)의 name 속성에 값 할당
        age = 30           // Person 객체(this)의 age 속성에 값 할당
        println("apply 블록 안에서: $this") // apply 블록 안에서는 'this'가 Person 객체 자신을 가리킵니다.
        // 출력: apply 블록 안에서: Person(name=김철수, age=30)
    }
    // apply는 Person 객체 자신(person1)을 반환합니다.
    println("apply 블록 밖에서: $person1") // 출력: apply 블록 밖에서: Person(name=김철수, age=30)

}
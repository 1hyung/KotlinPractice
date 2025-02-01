//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    // 하이라이트된 텍스트에 커서(caret)를 두고 Alt + Enter를 누르면 IntelliJ IDEA가 수정 방법을 제안하는 것을 볼 수 있음.
    println("Hello, " + name + "!")
    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        // 디버깅을 시작하려면 Shift + F9를 누르세요. 우리는 하나의 브레이크포인트를 설정해 두었지만, Ctrl + F8을 눌러 추가할 수도 있습니다.
        println("i = $i")
    }
}
package Gemini

data class User(
    val id: Long,
    val name: String,
    var email: String,
    var age: Int? = null
) {
    /**
     * 이메일 주소에 '@'가 포함되어 있는지 확인합니다.
     * @return 포함되어 있으면 true, 그렇지 않으면 false
     */
    fun hasValidEmail1(): Boolean {
        return this.email.contains("@")
    }

    fun hasValidEmail2(): Boolean = this.email.contains("@")
}
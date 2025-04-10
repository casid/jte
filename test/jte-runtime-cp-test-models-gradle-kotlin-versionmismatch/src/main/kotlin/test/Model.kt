package test

@Suppress("unused")
class Model {
    val hello: String = "Hello"
    fun thatThrows(): String = throw java.lang.NullPointerException("Oops")
}
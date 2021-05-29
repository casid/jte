package test

@Suppress("unused")
class Model {
    var hello:String = ""
    fun getThatThrows():String {
        throw NullPointerException()
    }
}
package test

@Suppress("unused")
class Model {
    var hello:String = ""
    fun getThatThrows():String {
        throw NullPointerException()
    }

    inline fun inlineMethod():String {
        return "inline";
    }
}
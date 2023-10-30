package test

@JvmInline
value class ValueClass(private val s: String) {
    fun s() : String {
        return s
    }
}
package net.opatry.nanoc.kt.util

class Logger(val verbose: Boolean) {
    fun log(message: String, lineBreak: Boolean = true) = synchronized(this) {
        if (lineBreak)
            println(message)
        else
            print(message)
    }

    fun log(before: String, vararg after: String, block: () -> Unit) = synchronized(this) {
        log(before, lineBreak = false)
        block()
        after.forEach {
            log(it, lineBreak = false)
        }
        log("", lineBreak = true)
    }
}
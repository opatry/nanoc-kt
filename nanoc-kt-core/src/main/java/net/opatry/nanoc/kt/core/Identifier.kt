package net.opatry.nanoc.kt.core

import java.io.File

data class Identifier(val file: File, private val id: String) {
    val withoutExt: String
        get() = id.substringBeforeLast('.')

    override fun toString(): String {
        return id
    }
}
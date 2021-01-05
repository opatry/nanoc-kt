package net.opatry.nanoc.kt.core

import com.esotericsoftware.yamlbeans.YamlReader
import java.io.File
import java.io.FileReader

class Config private constructor(private val config: MutableMap<String, Any>) {

    operator fun get(key: String): Any? = config[key]

    operator fun set(key: String, value: Any) {
        config[key] = value
    }

    companion object {
        fun fromYaml(file: File): Config = Config(
            YamlReader(FileReader(file)).read() as? MutableMap<String, Any> ?: mutableMapOf()
        )
        fun fromYaml(input: String): Config = Config(
            YamlReader(input).read() as? MutableMap<String, Any> ?: mutableMapOf()
        )
    }
}
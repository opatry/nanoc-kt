package net.opatry.nanoc.kt.core

import java.io.File

class Item(val identifier: Identifier, val isBinary: Boolean) {
    private val frontMatter: Config
    val rawContent: String
    var compiledContent: String
    val rawFileName: String
        get() = identifier.file.absolutePath

    init {
        if (isBinary) {
            with(identifier.file) {
                val yaml = File("${absolutePath.removeSuffix(".${extension}")}.yaml")
                frontMatter = if (yaml.exists()) {
                    Config.fromYaml(yaml)
                } else {
                    Config.fromYaml("")
                }
            }
            rawContent = identifier.file.absolutePath
        } else {
            val fileContent = identifier.file.readText()
            val matches =
                Regex("^[-]{3,}\n(.*)\n[-]{3,}\n(.*)", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)).find(
                    fileContent
                )
            if (matches != null) {
                val (frontMatterStr, content) = matches.destructured
                frontMatter = Config.fromYaml(frontMatterStr)
                rawContent = content
            } else {
                frontMatter = Config.fromYaml("")
                rawContent = fileContent
            }
        }
        frontMatter["extension"] = identifier.file.extension
        compiledContent = rawContent
    }
    operator fun get(key: String): Any? = frontMatter[key]
    operator fun set(key: String, value: Any) {
        frontMatter[key] = value
    }
}
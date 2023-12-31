/*
 * Copyright (c) 2023 Olivier Patry
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
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

abstract class Filter(private val repository: Repository, val identifier: String, val binary: Boolean = false): ContextProvider by repository {
    val outputFile: File
        get() {
            // TODO find route to item
            return File(repository.outputDir, item.identifier.file.toRelativeString(repository.contentDir))
        }

    internal fun run(item: Item, params: Map<String, Any>): String {
        // TODO define filter.setOutputFileName
        if (item.isBinary) {
            item.compiledContent = run(File(item.rawFileName), params)
        } else {
            item.compiledContent = run(item.compiledContent, params)
        }
        return item.compiledContent
    }

    open fun run(file: File, params: Map<String, Any>): String {
        error("Should be overridden for binary items")
    }

    open fun run(content: String, params: Map<String, Any>): String {
        error("Should be overridden for text items")
    }
}
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

package net.opatry.nanoc.kt.dsl

import net.opatry.nanoc.kt.core.ContextProvider
import net.opatry.nanoc.kt.core.Filter
import net.opatry.nanoc.kt.core.Item
import net.opatry.nanoc.kt.core.Repository
import kotlin.reflect.KClass

class CompileRule(
    private val repository: Repository,
    val pattern: String,
    private val run: CompileRule.(item: Item) -> Unit = {}
): Rule(), ContextProvider by repository, (Item) -> Unit {

    override fun invoke(item: Item) {
        try {
            run(item)
        } finally {
            // FIXME compiled state should be stored in item
            repository.compiled(item)
        }
    }

    fun <T : Filter> filter(filterClass: KClass<T>, params: Map<String, Any> = mapOf()) {
        val filter = checkNotNull(repository.filterWithClass(filterClass)) { "No filter '$filterClass' found" }
        filter.run(item, params)
    }

    fun layout(layoutIdentifier: String) {
        val layout = repository.layoutMatching(layoutIdentifier)
        item.compiledContent = layout.identifier.file.readText().replace("{%yield%}", item.compiledContent)
    }
}
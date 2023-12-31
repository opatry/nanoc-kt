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

import net.opatry.nanoc.kt.core.Repository
import net.opatry.nanoc.kt.util.Logger
import java.io.File
import java.util.Locale

class RulesProcessor(private val logger: Logger, private val rootDir: File, val process: Rules.() -> Unit) {
    private val repository = Repository(rootDir)
    private val rules = Rules(repository)

    init {
        logger.log("Loading site… ", "done") {
            process(rules)
        }
    }

    fun compile() {
        val t0 = System.currentTimeMillis()
        logger.log("Compiling site…")

        // TODO coroutines

        repository.ignoreRules.forEach(IgnoreRule::invoke)
        repository.passthroughRules.forEach(PassthroughRule::invoke)

        repository.compileRules.forEach { compileRule ->
            repository.itemsMatching(compileRule.pattern).forEach { item ->
                if (repository.needsCompilation(item)) {
                    repository.withItem(item) {
                        compileRule(item)
                    }
                }
            }
        }

        // FIXME should apply routing rules right after compilation of related items is done(?)
        repository.routingRules.forEach { routeRule ->
            repository.itemsMatching(routeRule.pattern).forEach item@{ item ->
                if (repository.needsRouting(item)) {
                    repository.withItem(item) {
                        val outputFile = routeRule(item) ?: return@withItem
                        logger.log("\tcreate\t[0.00s]\t${outputFile.relativeTo(rootDir)}")
                    }
                }
            }
        }

        // TODO use time formatter
        val duration = (System.currentTimeMillis() - t0) / 1000f;
        val durationStr = "%.2f".format(duration, Locale.US)
        logger.log("Site compiled in ${durationStr}s.")
    }

    fun preprocess() {
        repository.preProcessRule?.let { it.run(it) }
    }

    fun postprocess() {
        repository.postProcessRule?.let { it.run(it) }
    }

    fun help() {

    }
}

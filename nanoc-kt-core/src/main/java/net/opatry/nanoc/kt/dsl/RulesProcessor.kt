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

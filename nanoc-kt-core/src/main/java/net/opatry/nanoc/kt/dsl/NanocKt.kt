@file:JvmName("NanocKt")

package net.opatry.nanoc.kt.dsl

import net.opatry.nanoc.kt.core.Item
import net.opatry.nanoc.kt.core.Repository
import net.opatry.nanoc.kt.util.Logger
import java.io.File

@DslMarker
annotation class NanocDslMarker

@NanocDslMarker
abstract class Rule

@NanocDslMarker
class Rules(private val repository: Repository) {

    fun preprocess(preprocess: PreProcessRule.() -> Unit): PreProcessRule {
        val rule = PreProcessRule(repository, preprocess)
        if (repository.preProcessRule != null)
            error("preprocess already defined")
        repository.preProcessRule = rule
        return rule
    }

    fun postprocess(postprocess: PostProcessRule.() -> Unit): PostProcessRule {
        val rule = PostProcessRule(repository, postprocess)
        if (repository.postProcessRule != null)
            error("postprocess already defined")
        repository.postProcessRule = rule
        return rule
    }

    fun passthrough(regex: Regex): PassthroughRule = TODO()

    // FIXME should we expose rep?
    fun passthrough(pattern: String): PassthroughRule {
        val rule = PassthroughRule(repository, pattern)
        repository.passthroughRules += rule
        return rule
    }

    fun ignore(regex: Regex): IgnoreRule = TODO()

    // FIXME should we expose rep?
    fun ignore(pattern: String): IgnoreRule {
        val rule = IgnoreRule(repository, pattern)
        repository.ignoreRules += rule
        return rule
    }

    fun compile(regex: Regex, rep: String = "default", compile: CompileRule.(item: Item) -> Unit): CompileRule = TODO()

    fun compile(pattern: String, rep: String = "default", compile: CompileRule.(item: Item) -> Unit): CompileRule {
        // TODO use rep
        val rule = CompileRule(repository, pattern, compile)
        repository.compileRules += rule
        return rule
    }

    /**
     * TODO should also make available regex matches
     * eg.
     * ```ruby
     * route %r[/blog/([0-9]+)\-([0-9]+)\-([0-9]+)\-([^\/]+)\..*] do |y, m, d, slug|
     *   "/blog/#{y}/#{m}/#{slug}/index.html"
     * end
     * ```
     */
    fun route(regex: Regex, rep: String = "default", route: RouteRule.(item: Item) -> String): RouteRule = TODO()

    fun route(pattern: String, rep: String = "default", route: RouteRule.(item: Item) -> String): RouteRule {
        // TODO use rep
        val rule = RouteRule(repository, pattern, route)
        repository.routingRules += rule
        return rule
    }
}

fun nanoc(args: Array<String>, rules: Rules.() -> Unit) {
    val logger = Logger(args.contains("-V") || args.contains("--verbose"))
    val command = args.firstOrNull() ?: "compile"
    val processor = RulesProcessor(logger, (File(System.getProperty("user.dir"))), rules)

    processor.preprocess()
    when (command) {
        "compile" -> processor.compile()
        else -> processor.help()
    }
    processor.postprocess()
}

typealias RulesEntryPoint = Rules.() -> Unit

fun main(args: Array<String>) {
    val rulesClass = Class.forName("Rules") ?: Class.forName("RulesKt") ?: error("Can't find Rules class.")
    val rules = rulesClass.declaredFields.map {
        it.isAccessible = true
        it.get(null) as? RulesEntryPoint
    }.firstOrNull { it != null } ?: error("No RulesEntryPoint field found.")
    nanoc(args, rules)
}
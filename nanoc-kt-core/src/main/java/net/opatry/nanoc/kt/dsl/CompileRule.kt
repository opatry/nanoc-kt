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
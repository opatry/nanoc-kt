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
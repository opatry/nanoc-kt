package lib.filters

import com.github.mustachejava.DefaultMustacheFactory
import net.opatry.nanoc.kt.core.Filter
import net.opatry.nanoc.kt.core.Repository
import java.io.StringReader
import java.io.StringWriter


class Mustache internal constructor(repository: Repository) : Filter(repository, "Mustache") {
    private val factory = DefaultMustacheFactory()
    override fun run(content: String, params: Map<String, Any>): String {
        val mustache = factory.compile(StringReader(content), "toto.mustache")
        val output = StringWriter()
        // FIXME how to give item metadata
        mustache.execute(output, params).flush()
        return output.toString()
    }
}

val mustache = Mustache::class

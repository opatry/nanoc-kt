package lib.filters

import net.opatry.nanoc.kt.core.Filter
import net.opatry.nanoc.kt.core.Repository
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer


class Commonmark internal constructor(repository: Repository) : Filter(repository, "Commonmark") {
    private val parser: Parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()
    override fun run(content: String, params: Map<String, Any>): String {
        val document = parser.parse(content)
        return renderer.render(document)
    }
}

val commonmark = Commonmark::class

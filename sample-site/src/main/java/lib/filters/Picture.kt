package lib.filters

import net.opatry.nanoc.kt.core.Filter
import net.opatry.nanoc.kt.core.Repository


class Picture(repository: Repository) : Filter(repository, "Picture") {
    override fun run(content: String, params: Map<String, Any>): String {
        return "<img src=\"https://picsum.photos/200/300\">"
    }
}

val picture = Picture::class

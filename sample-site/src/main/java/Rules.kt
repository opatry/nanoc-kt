@file:JvmName("Rules")

import lib.filters.commonmark
import lib.filters.mustache
import lib.filters.picture
import lib.filters.thumbnailize
import net.opatry.nanoc.kt.dsl.nanoc
import java.util.UUID

// TODO try to extract main in nanoc-kt-core and lookup for the `nanoc {}` lambda (val nanoc = { preprocess {} } maybe?)
//val nanoc: RulesEntryPoint = {
fun main(args: Array<String>) = nanoc(args) {
    val allExcerptPictures = mutableListOf<String>()
    preprocess {
        println("preprocess")

        items.filter { it["kind"] == "page" || it["kind"] == "post" }.forEach { item ->
            checkNotNull(item["uuid"]) { "item ${item.identifier} doesn't have a uuid (suggested: ${UUID.randomUUID()})" }
        }
    }

    postprocess {
        println("postprocess")
    }

    passthrough("/assets/**/*")

    ignore("/foo-item.*")

    arrayOf("excerpt", "default").forEach { rep ->
        compile("/posts/**/*", rep) {
            filter(picture)
            filter(commonmark)
            layout("/post-$rep.*")
            layout("/index.*")
            filter(mustache)
        }

        compile("/photos/**/*", rep) {
            val pictureWidths = config["picture_width"] as? Map<String, Any> ?: error("can't find valid 'picture_width' config")
            val pictureWidth = checkNotNull(pictureWidths[rep]) { "No 'picture_wdith' for '$rep'" }
            filter(thumbnailize, mapOf("width" to pictureWidth))
        }
    }

    compile("/**/*") {
        filter(commonmark)
        layout("/index.*")
    }

    route("/posts/**/*") {
        "${item.identifier.withoutExt}/index.html"
    }

    arrayOf("excerpt", "default").forEach { rep ->
        route("/photos/**/*") {
            val suffix = if (rep == "excerpt") "-$rep" else ""
            "${item.identifier.withoutExt}${suffix}.${item["extension"]}"
        }
    }

    route("/index.*") {
        "/index.html"
    }

    route("/**/*") {
        if (item.isBinary) {
            item.identifier.toString()
        } else {
            "${item.identifier.withoutExt}/index.html"
        }
    }
}

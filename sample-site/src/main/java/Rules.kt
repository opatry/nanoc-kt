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

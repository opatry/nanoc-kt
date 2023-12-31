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

package lib.filters

import net.opatry.nanoc.kt.core.Filter
import net.opatry.nanoc.kt.core.Repository
import net.opatry.nanoc.kt.util.runCommand
import java.io.File

class Thumbnailize(repository: Repository) : Filter(
    repository,
    identifier = "Thumbnailize",
    binary = true
) {
    override fun run(file: File, params: Map<String, Any>): String {
        val width = when (val widthP = params["width"]) {
            is Int -> widthP
            is String -> Integer.parseInt(widthP as String?)
            else -> error("\"width\" param must be provided")
        }
        val quality = config["picture_quality"]
        outputFile.parentFile.mkdirs()
        "convert -resize $width -strip -interlace Plane -quality $quality ${file.absolutePath} ${outputFile.absolutePath}".runCommand(
            outputFile.parentFile
        )
        return outputFile.absolutePath
    }
}

val thumbnailize = Thumbnailize::class

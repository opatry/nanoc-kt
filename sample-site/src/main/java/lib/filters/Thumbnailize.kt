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

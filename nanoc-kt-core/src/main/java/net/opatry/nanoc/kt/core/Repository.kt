package net.opatry.nanoc.kt.core

import net.opatry.nanoc.kt.dsl.CompileRule
import net.opatry.nanoc.kt.dsl.IgnoreRule
import net.opatry.nanoc.kt.dsl.PassthroughRule
import net.opatry.nanoc.kt.dsl.PostProcessRule
import net.opatry.nanoc.kt.dsl.PreProcessRule
import net.opatry.nanoc.kt.dsl.RouteRule
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.io.File
import kotlin.reflect.KClass

operator fun MutableList<Item>.get(pattern: String): Item {
    val workaround = pattern.removeSuffix(".*")
    return first { it.identifier.toString().startsWith(workaround) }
}

class Repository(private val rootDir: File) : ContextProvider {
    private val filters: Map<KClass<Filter>, Filter>
    private var _item: Item? = null

    override val config: Config
    override val items: MutableList<Item>
    override val item: Item
        get() = checkNotNull(_item) { "No item being processed right now." }

    val layouts: MutableList<Layout>
    val passthroughRules = mutableListOf<PassthroughRule>()
    val ignoreRules = mutableListOf<IgnoreRule>()
    var preProcessRule: PreProcessRule? = null
    var postProcessRule: PostProcessRule? = null
    val compileRules = mutableListOf<CompileRule>()
    val routingRules = mutableListOf<RouteRule>()

    private val compiled = mutableListOf<Item>()
    private val routed = mutableListOf<Item>()

    val contentDir: File
        get() = File(rootDir, config["content_dir"] as? String ?: "content")
    val layoutsDir: File
        get() = File(rootDir, config["layouts_dir"] as? String ?: "layouts")
    val outputDir: File
        get() {
            val outputDirName = config["output_dir"] as? String ?: "output"
            return File(rootDir, outputDirName)
        }

    init {
        val configFile = when {
            File(rootDir, "nanoc.yaml").isFile -> File(rootDir, "nanoc.yaml")
            File(rootDir, "nanoc.yml").isFile -> File(rootDir, "nanoc.yml")
            else -> error("Can't find nanoc.yaml nor nanoc.yml in $rootDir")
        }
        config = Config.fromYaml(configFile)
        items = loadItems(contentDir)
        layouts = loadLayouts(layoutsDir)
        filters = loadFilters()
    }

    // FIXME why File.walkTopDown() isn't working?
    private fun listFiles(f: File, aggregator: MutableList<File>) {
        if (f.isFile)
            aggregator += f
        else
            f.listFiles()?.forEach { listFiles(it, aggregator) }
    }

    private fun File.myWalk(): List<File> {
        val files = mutableListOf<File>()
        if (isFile) {
            files += this
        } else {
            listFiles(this, files)
        }
        return files
    }

    private fun loadLayouts(layoutsDir: File): MutableList<Layout> {
        // FIXME use walkTopDown to list recursively
        // TODO error management (no dir, empty, no file)
        val layouts = mutableListOf<Layout>()
//        layoutsDir.walkTopDown().onEach {
        layoutsDir.myWalk().onEach {
            layouts += Layout(Identifier(it, "/${it.relativeTo(layoutsDir)}"))
        }
        return layouts
    }

    private fun loadItems(contentDir: File): MutableList<Item> {
        // TODO error management (no dir, empty, no file)
        val items = mutableListOf<Item>()
//        contentDir.walkTopDown().onEach {
        contentDir.myWalk().onEach {
            items += Item(Identifier(it, "/${it.relativeTo(contentDir)}"), isBinary(it))
        }
        return items
    }

    private fun loadFilters(): Map<KClass<Filter>, Filter> {
        // For now, package name of user site is forced to empty and so `lib.filters` for filters
        val reflections = Reflections(
            ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("lib.filters"))
                .setScanners(SubTypesScanner())
//                .filterInputsBy(FilterBuilder().includePackage("net.opatry.nanoc.kt"))
        )
        val classes = reflections.getSubTypesOf(Filter::class.java)

        val filters = mutableMapOf<KClass<Filter>, Filter>()
        classes.forEach { filterClass ->
            if (!Filter::class.java.isAssignableFrom(filterClass)) error("$filterClass isn't a ${Filter::class.java}")
            // FIXME client code relies on Repository impl :(
            val filterCtor = filterClass.getConstructor(Repository::class.java)
                ?: error("Filter ${filterClass.name} must define a constructor(Repository) ctor.")
            val filter = filterCtor.newInstance(this)
            val filterKClass = filterClass.kotlin as? KClass<Filter> ?: error("Can't convert $filterClass to KClass<Filter>")
            filters[filterKClass] = filter
        }
        return filters
    }

    fun isBinary(file: File): Boolean {
        val textExts = config["text_extensions"] as? Iterable<String> ?: listOf()
        return !textExts.contains(file.extension)
    }

    fun itemsMatching(pattern: String): List<Item> {
        val workaround = pattern.removeSuffix("**/*").removeSuffix(".*")
        return items.filter { it.identifier.toString().startsWith(workaround) }
    }

    fun layoutMatching(pattern: String): Layout {
        val workaround = pattern.removeSuffix("**/*").removeSuffix(".*")
        return layouts.firstOrNull { it.identifier.toString().startsWith(workaround) } ?: error("No layout matching $pattern")
    }

    fun <T : Filter> filterWithClass(filterClass: KClass<T>): Filter? {
        @Suppress("USELESS_CAST")
        return filters[filterClass as KClass<*>]
    }

    fun needsCompilation(item: Item) = !compiled.contains(item)

    fun compiled(item: Item) {
        compiled += item
    }

    fun needsRouting(item: Item) = !routed.contains(item)

    fun routed(item: Item) {
        routed += item
    }

    fun withItem(item: Item, action: (Item) -> Unit) {
        if (_item != null) error("An item is already being processed")
        _item = item
        try {
            action(item)
        } finally {
            _item = null
        }
    }
}
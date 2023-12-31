[![Build Status](https://github.com/opatry/nanoc-kt/actions/workflows/Build.yml/badge.svg)](https://github.com/opatry/nanoc-kt/actions/workflows/Build.yml)

# NanocKt

NanocKt is a pedagogical and experimental toy project meant to mimic the
DSL and logic behind the original [Nanoc](https://nanoc.ws/) static site generator.

## Disclaimer

This project shouldn't be used by anyone, there is no goal to make it a product 
on par with the original Nanoc project.

This is a Frankenstein clone, meaning:

> it's ugly, it's barely working, and it's not meant to live for long.

## Motivation

The original motivation was to determine what a Ruby DSL could look like
as a Kotlin DSL. How could original syntax be reused almost identically.

A Nanoc website logic mostly resides in the `Rules` file describing how
to process content (`compile` rules) and where to put the result (`route` rules).

Example in Ruby for Nanoc:
```ruby
preprocess do
  # ignore unpublished items
  @items.delete_if { |item| item[:published] == false }
end

# simply copy other assets
passthrough '/assets/fonts/**/*'
passthrough '/assets/img/**/*'
passthrough '/attachments/**/*'

compile '/notes/*' do
  filter :kramdown
  filter :colorize_syntax, default_colorizer: :rouge
  layout '/default.*'
end

compile '**/*' do
  unless item.binary?
    filter :kramdown
    item_layout = item[:layout] || 'default'
    layout "/#{item_layout}.*" unless item_layout == 'none'
  end
end

route '/notes/*' do
  raise "item #{item.identifier} doesn't have a slug" if item[:slug].nil?

  slug = item[:slug]
  post_date = Date.parse(item[:created_at].to_s)
  month_2d = format('%02d', post_date.month)
  day_2d = format('%02d', post_date.mday)
  "/#{post_date.year}/#{month_2d}/#{day_2d}/#{slug}/index.html"
end

route '**/*' do
  if item.binary?
    item.identifier.to_s
  else
    "#{item.identifier.without_ext}.html"
  end
end
```

Equivalent in Kotlin with NanocKt:
```kotlin
fun main(args: Array<String>) = nanoc(args) {
    preprocess {
        // ignore unpublished items
        items.removeIf { it["published"] != true }
    }

    // simply copy other assets
    passthrough("/assets/fonts/**/*")
    passthrough("/assets/img/**/*")
    passthrough("/attachments/**/*")

    compile("/notes/*") {
        filter(commonmark)
        filter(colorizeSyntax, mapOf("defaultColorizer" to "rouge"))
        layout("/default.*")
    }

    compile("**/*") {
        if (!item.isBinary) {
            filter(commonmark)
            val itemLayout = item["layout"] ?: "default"
            if (itemLayout != "none") {
                layout("/${itemLayout}.*")
            }
        }
    }

    route("/notes/*") {
        checkNotNull(item["slug"]) { "item ${item.identifier} doesn't have a slug" }

        val slug = item["slug"] as String
        val postDate = LocalDate.parse(item["created_at"] as String, DateTimeFormatter.ISO_DATE)
        val month2d = postDate.month.toString().padStart(2, '0')
        val day2d = postDate.dayOfMonth.toString().padStart(2, '0')
        "/${postDate.year}/${month2d}/${day2d}/${slug}/index.html"
    }

    route("**/*") {
        if (item.isBinary)
            item.identifier.toString()
        else
            "${item.identifier.withoutExt}.html"
    }
}
```

## Useful resources

- [Nanoc variables](https://nanoc.ws/doc/reference/variables/)
- [Nanoc internals](https://nanoc.ws/doc/internals/#data)
- [Nanoc GitHub repository](https://github.com/nanoc/nanoc/)

## Known issues/shortcomings

- everything should be a rule (incl. `filter`, `layout`, ...)
- consider kscript vs `main`
- no item dependency management
- no auto-prune
- no cache/incremental compilation
- no management of `create`/`update`/`skip`/`identical`/`delete`
- no management of item representation
- no management of item snapshot
- no equivalent of `:erb` filter (could kscript be helpful here?)
- a lot more!

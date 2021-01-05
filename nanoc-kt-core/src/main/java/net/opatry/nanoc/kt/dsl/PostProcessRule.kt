package net.opatry.nanoc.kt.dsl

import net.opatry.nanoc.kt.core.ContextProvider
import net.opatry.nanoc.kt.core.Repository

class PostProcessRule(private val repository: Repository, val run: PostProcessRule.() -> Unit): Rule(), ContextProvider by repository
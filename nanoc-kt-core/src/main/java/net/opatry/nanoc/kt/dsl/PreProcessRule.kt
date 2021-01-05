package net.opatry.nanoc.kt.dsl

import net.opatry.nanoc.kt.core.ContextProvider
import net.opatry.nanoc.kt.core.Repository

class PreProcessRule(private val repository: Repository, val run: PreProcessRule.() -> Unit): Rule(), ContextProvider by repository
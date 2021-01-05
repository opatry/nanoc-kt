package net.opatry.nanoc.kt.dsl

import net.opatry.nanoc.kt.core.Repository

class PassthroughRule(private val repository: Repository, val pattern: String): Rule(), () -> Unit {
    override fun invoke() {
        // FIXME shouldn't be "0" but index before non-user-defined "compile" and "route" rules
        repository.compileRules.add(0, CompileRule(repository, pattern))
        repository.routingRules.add(0, RouteRule(repository, pattern))
    }
}
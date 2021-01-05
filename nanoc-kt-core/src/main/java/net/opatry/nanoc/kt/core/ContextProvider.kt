package net.opatry.nanoc.kt.core

interface ContextProvider {
    val config: Config
    val items: MutableList<Item>
    val item: Item
//    val itemRep: ItemRep
}
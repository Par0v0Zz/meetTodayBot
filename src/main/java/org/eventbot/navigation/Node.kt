package org.eventbot.navigation

class Node (
        var children: MutableList<Node>,
        val screenId: Int,
        val screenLabel: String,
        var button: MenuItemObject? = null,
        var parent: Node? = null
){
    fun id(): String {
        return "${screenId};${button?.id}"
    }

    override fun toString(): String {
        return id()
    }
}
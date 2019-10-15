package org.eventbot.navigation

class Node (
        var children: MutableList<Node>,
        var screen: MenuScreenObject,
        var button: MenuItemObject = ,
        var parent: Node? = null
){
    fun id(): String {
        return "${screen.id};${button.i}"
    }
}
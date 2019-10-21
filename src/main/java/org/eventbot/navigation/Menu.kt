package org.eventbot.navigation

class Menu(){
    lateinit var root: Node

    init{
        root = Node(ArrayList(), MenuScreenObject(0, "main"))
        

    }

}

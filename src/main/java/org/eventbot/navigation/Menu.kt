package org.eventbot.navigation

class Menu(){
    lateinit var root: Node

    init{

        root = Node(ArrayList(), MenuScreenObject(0, "main"), )

    }

//    S_0_MAIN(0),
//    S_1_GROUPS(1),
//    S_2_GROUP_ADMIN(2),
//    S_2_GROUPS_ALL(3),
//    S_3_GROUP_INFO(4),
//    S_3_GROUP_ALL(5),
//    S_3_GROUP_ADMIN(6),

    fun findNode(id: String): Node{

    }
}

package org.eventbot.constant

import java.util.HashMap

enum class MenuScreen(private val id: Int) {
    S_0_MAIN(0),
    S_1_GROUPS(1),
    S_2_GROUP_ADMIN(2),
    S_2_GROUPS_ALL(3),
    S_3_GROUP_INFO(4),
    S_3_GROUP_ALL(5),
    S_3_GROUP_ADMIN(6),
    ;

    var parent: MenuScreen? = null
        private set

    override fun toString(): String {
        return this.id.toString()
    }

    companion object {

        private val map: MutableMap<Int, MenuScreen>

        init {
            map = HashMap()
            for (v in values()) {
                map[v.id] = v
            }

            S_0_MAIN.parent = S_0_MAIN
            S_1_GROUPS.parent = S_0_MAIN
            S_2_GROUP_ADMIN.parent = S_1_GROUPS
            S_2_GROUPS_ALL.parent = S_1_GROUPS
            S_3_GROUP_INFO.parent = S_2_GROUP_ADMIN
            S_3_GROUP_ALL.parent = S_2_GROUPS_ALL
            S_3_GROUP_ADMIN.parent = S_2_GROUP_ADMIN
        }

        fun findByKey(i: Int): MenuScreen {
            return map[i]!!
        }
    }

}

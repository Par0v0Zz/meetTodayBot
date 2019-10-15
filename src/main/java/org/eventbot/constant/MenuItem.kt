package org.eventbot.constant

import java.util.HashMap

enum class MenuItem private constructor(private val id: Int, private val targetScreen: MenuScreen?) {
    BACK(0, null),
    GROUPS(1, MenuScreen.S_1_GROUPS),
    GROUPS_ADMIN(1, MenuScreen.S_2_GROUP_ADMIN),
    GROUPS_ALL(2, MenuScreen.S_2_GROUPS_ALL),
    GROUP_INFO(3, MenuScreen.S_3_GROUP_INFO),
    LUNCH(4, MenuScreen.S_2_GROUPS_ALL),
    LEAVE_GROUP(5, MenuScreen.S_2_GROUPS_ALL),

    ;

    fun getTargetScreen(currentScreen: MenuScreen): MenuScreen? {
        if (targetScreen != null) {
            return targetScreen
        }
        when (this) {
            BACK -> return currentScreen.parent
            else -> throw IllegalStateException("can't find target screen for screen $currentScreen, navigation item = $this")
        }
    }

    companion object {

        private val map: MutableMap<Int, MenuItem>

        init {
            map = HashMap<Int, MenuItem>()
            for (v in MenuItem.values()) {
                map[v.id] = v
            }
        }

        fun findByKey(i: Int): MenuItem {
            return map[i]!!
        }
    }
}

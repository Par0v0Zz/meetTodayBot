package org.eventbot.navigation

import org.eventbot.constant.MenuScreen
import kotlin.collections.HashMap

class MenuItemObject (
        val id: Int,
        val name: String,
        val label: String,
        val targetScreen: MenuScreen) {
    fun getTargetScreen(currentScreen: MenuScreenObject): MenuScreen? {
        if (targetScreen != MenuScreen.S_00_SELF) {
            return targetScreen
        }
        when (this.id) {
            0 -> return currentScreen.parent
            else -> throw IllegalStateException("can't find target screen for screen $currentScreen, navigation item = $this")
        }
    }

    companion object {

        private val map: MutableMap<Int, MenuItemObject> = mutableMapOf(
            0 to MenuItemObject(0, "back", "<- back", )
        )

        init {
            map = HashMap<Int, MenuItemObject>()
            for (v in MenuItemObject.values()) {
                map[v.id] = v
            }
        }

        fun findByKey(i: Int): MenuItemObject {
            return map[i]!!
        }

//        BACK(0, null),
//        GROUPS(1, MenuScreen.S_1_GROUPS),
//        GROUPS_ADMIN(1, MenuScreen.S_2_GROUP_ADMIN),
//        GROUPS_ALL(2, MenuScreen.S_2_GROUPS_ALL),
//        GROUP_INFO(3, MenuScreen.S_3_GROUP_INFO),
//        LUNCH(4, MenuScreen.S_2_GROUPS_ALL),
//        LEAVE_GROUP(5, MenuScreen.S_2_GROUPS_ALL),
    }
}

package org.eventbot.navigation

import org.eventbot.callback.CallbackParams
import org.eventbot.constant.MenuScreen
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.ArrayList

@Component
class MenuKeyboardComposer {
    val menu: Menu = Menu()

    fun compose(screen: MenuScreen, context: Map<CallbackParams, Any>): List<List<InlineKeyboardButton>> {
        val rows = ArrayList<List<InlineKeyboardButton>>()

        // get current screen vertex id from context
        // enable labeled edges of jgrapht
        // get current menu item (edge id) from context)
        menu.graph.vertexSet().find{ it == "S_0_MAIN"}

        return rows
//        val menuItem = menuItem(context)
//        val targetScreen = menuItem.getTargetScreen(screen)
//
//        when (targetScreen) {
//            MenuScreen.S_0_MAIN -> addMainButtons(rows)
//            MenuScreen.S_1_GROUPS -> addGroupsButtons(rows)
//            MenuScreen.S_2_GROUP_ADMIN -> addGroupAdminButtons(context, rows)
//            MenuScreen.S_2_GROUPS_ALL -> addGroupListButtons(context, rows)
//        }
//
//        return getMultiRowKeyboard(rows)
    }

}
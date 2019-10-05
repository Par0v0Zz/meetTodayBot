package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.constant.MenuScreen
import org.eventbot.model.UserInfo
import org.eventbot.service.MenuService
import org.springframework.stereotype.Component


@Component
class MenuCallbackAction(
        val menuService: MenuService
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val user = context[CallbackParams.USER_INFO] as UserInfo

        menuService.updateMenu(
                user.userId,
                menuScreen(context),
                context)

        return "done"
    }

    private fun menuScreen(context: Map<CallbackParams, Any>): MenuScreen {
        var screenId: Int
        try {
            screenId = Integer.parseInt(context[CallbackParams.ARG] as String)
        } catch (e: NumberFormatException) {
            screenId = 1
        }

        return MenuScreen.findByKey(screenId)
    }

}

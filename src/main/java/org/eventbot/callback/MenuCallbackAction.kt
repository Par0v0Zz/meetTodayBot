package org.eventbot.callback

import org.eventbot.CallbackAction
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
                context)

        return "done"
    }

}

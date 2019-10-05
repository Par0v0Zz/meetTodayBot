package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.UserInfo
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component

@Component
class AllGroupsCallbackAction(
        private val messageService: MessageService,
        private val keyboardService: KeyboardService

) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val userInfo = context[CallbackParams.USER_INFO] as UserInfo
        val listOfGroups = userInfo.groups

        if (listOfGroups.isEmpty()) {
            messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, "No groups found")
        } else {
            listOfGroups.stream().forEach {
                messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, it.name ?: "noname group",
                        keyboardService.groupActionsKeyboard(it))
            }
        }

        return ""
    }

}

package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.UserInfo
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component

@Component
class AllGroupsCallbackAction(private val messageService: MessageService) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val userInfo = context[CallbackParams.USER_INFO] as UserInfo
        val listOfGroups = userInfo.groups.map { it.token }.joinToString("\n")

        if (listOfGroups.isEmpty()) {
            messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, "No groups found")
        } else {
            messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, listOfGroups)
        }

        return ""
    }

}

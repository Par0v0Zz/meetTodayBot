package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component

@Component
class MyGroupsCallbackAction(
        private val GroupRepository: GroupRepository,
        private val messageService: MessageService,
        private val keyboardService: KeyboardService
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val listOfGroups = GroupRepository.findByCreator(context[CallbackParams.USER_INFO] as UserInfo)
        if (listOfGroups.isEmpty()) {
            messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, "No groups found")
        } else {
            messageService.sendMessage(
                    context[CallbackParams.CHAT_ID] as Long,
                    messageService.groupList(listOfGroups),
                    keyboardService.groupsAdminKeyboard(listOfGroups))
        }

        return ""
    }

}

package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.UserInfo
import org.eventbot.repository.TeamRepository
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component

@Component
class ListGroupsByCreatorCallbackAction(
        private val teamRepository: TeamRepository,
        private val messageService: MessageService
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val listOfGroups = teamRepository.findByCreator(context[CallbackParams.USER_INFO] as UserInfo)
                .map { it.token } // TODO: replace by names
                .joinToString("\n")

        messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, listOfGroups)

        return ""
    }

}

package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.event.EventOrganizer
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.springframework.stereotype.Component

@Component
class CreateEventCallbackAction(
        val groupRepository: GroupRepository,
        val eventOrganizer: EventOrganizer
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val argument: Long = (context[CallbackParams.ARG] as String).toLong()
        val user = context[CallbackParams.USER_INFO] as UserInfo

        val group = groupRepository.findByPk(java.lang.Long.valueOf(argument))
        eventOrganizer.organizeEvent(user, group)
        return "Invitations sent!"
    }

}
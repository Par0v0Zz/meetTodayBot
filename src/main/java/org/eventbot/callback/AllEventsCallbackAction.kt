package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.ParticipantId
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component
import java.util.Date


@Component
class AllEventsCallbackAction(
        val messageService: MessageService,
        val keyboardService: KeyboardService,
        val eventsRepository: EventRepository,
        val participantRepository: ParticipantRepository
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val userInfo = context[CallbackParams.USER_INFO] as UserInfo

        val events = eventsRepository.getEventsAfter(Date(), userInfo)

        if (events.isEmpty()) {
            messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, "No events found")
        } else {
            events.forEach {
                val participant = participantRepository.getOne(ParticipantId(userInfo.pk, it.pk))
                messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long,
                        "${it.name} with ${it.creator.firstName} ${it.creator.lastName}",
                        keyboardService.acceptedInviteKeyboard(participant, it))
            }
        }

        return ""
    }

}

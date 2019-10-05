package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.EventStatus
import org.eventbot.model.ParticipantId
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import java.util.Date

class AcceptedEventsCallbackAction(
        val eventsRepository: EventRepository,
        val messageService: MessageService,
        val participantRepository: ParticipantRepository,
        val keyboardService: KeyboardService
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val userInfo = context[CallbackParams.USER_INFO] as UserInfo

        val events = eventsRepository.getEventsAfterByStatus(Date(), userInfo, EventStatus.ACCEPTED)

        if (events.isEmpty()) {
            messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long, "No accepted events found")
        } else {
            events.forEach {
                val participant = participantRepository.getOne(ParticipantId(userInfo.pk, it.pk))
                messageService.sendMessage(context[CallbackParams.CHAT_ID] as Long,
                        "${participant.accepted} at ${it.date} created by ${it.creator.firstName} ${it.creator.lastName}",
                        keyboardService.acceptedInviteKeyboard(participant, it))
            }
        }

        return ""
    }

}

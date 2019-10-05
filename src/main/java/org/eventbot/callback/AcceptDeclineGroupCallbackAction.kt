package org.eventbot.callback

import org.apache.commons.lang3.BooleanUtils
import org.eventbot.CallbackAction
import org.eventbot.model.Event
import org.eventbot.model.EventStatus
import org.eventbot.model.ParticipantId
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.repository.UserRepository
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component
import java.lang.Long.valueOf
import java.util.Date


@Component
class AcceptDeclineGroupCallbackAction(
        val participantRepository: ParticipantRepository,
        val userRepository: UserRepository,
        val eventRepository: EventRepository,
        val messageService: MessageService,
        val keyboardService: KeyboardService
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val eventPk = valueOf(context[CallbackParams.ARG] as String)

        val user = context[CallbackParams.USER_INFO] as UserInfo
        val participant = participantRepository.getOne(ParticipantId(user.pk, eventPk))

        val accepted = BooleanUtils.toBoolean(CallbackParams.ARG2 as String)

        participant.accepted = if (accepted) EventStatus.ACCEPTED else EventStatus.DECLINED

        participantRepository.save(participant)

        if (!accepted) {
            user.lastDeclineDate = Date()
            userRepository.save(user)
        }

        val event = participant.event
        updateEvent(event, participant.accepted)
        updateInvite(event)

        return "ok"
    }

    private fun updateEvent(event: Event, participantAccept: EventStatus) {
        val responses = event.participants.map { it.accepted }

        if (participantAccept == EventStatus.DECLINED) {
            event.accepted = EventStatus.DECLINED
        } else if (responses.all { it == EventStatus.ACCEPTED }) {
            event.accepted = EventStatus.ACCEPTED
        }

        if (event.accepted != EventStatus.NO_RESPONSE) {
            eventRepository.save(event)
        }
    }

    private fun updateInvite(event: Event) {
        messageService.updateToAll(
                event,
                messageService::eventDescriptionText,
                keyboardService::acceptedInviteKeyboard)
    }

}

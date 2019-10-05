package org.eventbot.callback

import org.apache.commons.lang3.BooleanUtils
import org.eventbot.CallbackAction
import org.eventbot.model.Event
import org.eventbot.model.EventStatus
import org.eventbot.model.Participant
import org.eventbot.model.ParticipantId
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.repository.UserRepository
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component


@Component
class AcceptDeclineGroupCallbackAction(
        val participantRepository: ParticipantRepository,
        val userRepository: UserRepository,
        val eventRepository: EventRepository,
        val messageService: MessageService,
        val keyboardService: KeyboardService
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val eventPk = (context[CallbackParams.ARG] as String).toLong()

        val user = context[CallbackParams.USER_INFO] as UserInfo
        val participant = participantRepository.getOne(ParticipantId(user.pk, eventPk))

        val accepted = BooleanUtils.toBoolean(context[CallbackParams.ARG2] as String)

        participant.accepted = if (accepted) EventStatus.ACCEPTED else EventStatus.DECLINED

        participantRepository.save(participant)

        val event = participant.event
        updateEvent(event, participant)
        updateInvite(event)
        return "ok"
    }

    private fun updateEvent(event: Event, participant: Participant) {
        if (participant.accepted == EventStatus.DECLINED) event.removeParticipant(participant)
        eventRepository.save(event)
    }

    //TODO: implement the way to inform creator about current participants status
    private fun updateInvite(event: Event) {

//        messageService.updateToAll(
//                event,
//                messageService::eventDescriptionText,
//                keyboardService::acceptedInviteKeyboard)
    }

}

package org.eventbot.event

import org.eventbot.event.generator.EventGenerator
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component
import java.util.Date

@Component
class EventOrganizer(
        private val eventGenerator: EventGenerator,
        private val eventRepository: EventRepository,
        val messageService: MessageService,
        val keyboardService: KeyboardService
) {
    fun organizeEvent(user: UserInfo, group: Group) {
        val event = eventGenerator.organizeLunch(user, group, Date())
        eventRepository.save(event)
        messageService.sendToAll(event, messageService::inviteText, keyboardService::getInviteKeyboard)
    }
}
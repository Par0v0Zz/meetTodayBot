package org.eventbot.event

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.eventbot.event.generator.PairGenerator
import org.eventbot.model.Event
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.service.ChatService
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.eventbot.service.TimeService
import java.util.*

@Component
class EventOrganizer(
        private val pairGenerator: PairGenerator,
        private val eventRepository: EventRepository,
        val messageService: MessageService,
        val keyboardService: KeyboardService,
        val chatService: ChatService,
        val timeService: TimeService
) {
    fun tryOrganizeEvent(user: UserInfo) {

        val group: Group = user.group ?: return

        if (hasDeclinedRecently(user) || hasUpcomingEvents(user)) {
            sendTryLater(user)
            return
        }
        val event = pairGenerator.findPair(user, group)

        if (event != null) {
            invite(user, event)
        } else {
            sendNoPairsAvailable(user)
        }
    }

    fun hasDeclinedRecently(user: UserInfo): Boolean {
        val lastDecline = user.lastDeclineDate
        return lastDecline != null && lastDecline.after(timeService.lastDeclineThreshold())
    }

    private fun hasUpcomingEvents(user: UserInfo): Boolean {
        return eventRepository.existsByDateAfterAndParticipants_User(Date(), user)
    }

    private fun sendTryLater(user: UserInfo) {
        messageService.sendMessage(
                chatService.getPrivateChatId(user),
                messageService.tryLaterText(user, hasDeclinedRecently(user)))
    }

    @Throws(TelegramApiException::class)
    private fun invite(user: UserInfo, event: Event) {

        eventRepository.save(event)

        messageService.sendToAll(event, messageService::inviteText, keyboardService::getInviteKeyboard)
    }

    @Throws(TelegramApiException::class)
    private fun sendNoPairsAvailable(user: UserInfo) {
        val message = messageService.getMessage(
                chatService.getPrivateChatId(user),
                "No pairs available.\nEveryone is busy or no one joined yet \uD83D\uDE1E"
        )
        messageService.sendMessage(message)
    }

}
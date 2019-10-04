package org.eventbot.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.eventbot.model.Event
import org.eventbot.model.Participant
import org.eventbot.repository.UserRepository
import java.time.Duration
import javax.transaction.Transactional

@Transactional
@Component
open class NotificationService {
    private val LOG = LoggerFactory.getLogger(javaClass)

    @Autowired
    private val userService: UserService? = null
    @Autowired
    private val chatService: ChatService? = null
    @Autowired
    private val messageService: MessageService? = null
    @Autowired
    private val userRepository: UserRepository? = null

    fun notifyAllUpcomingIn(expiringIn: Duration, scanPeriod: Duration) {
        LOG.info("Start notify about upcoming events")
        val events = userService!!.findUpcomingEvents(expiringIn, scanPeriod)
        LOG.info("Found {} people to notify", events.size)

        events.forEach { this.notifyUpcoming(it) }

        LOG.info("End notify about upcoming events")
    }

    private fun notifyUpcoming(event: Event) {
        try {
            event.participants.forEach { this.notifyUpcoming(it) }
        } catch (e: Exception) {
            LOG.error("Can't notify about upcoming event pk = {}\n{}", event.pk, e.toString(), e)
        }

    }

    private fun notifyUpcoming(participant: Participant) {
        val user = participant.user
        val userId = user!!.userId
        LOG.info("Notifying upcoming user {}", userId)
        try {
            val sentMessage = messageService!!.sendMessage(
                    messageService.getUpcomingNotificationMessage(participant))

            LOG.info("Notified upcoming user {}, id {}", user.firstName, userId)

            user.lastMessageId = sentMessage

            userRepository!!.save(user)
        } catch (e: TelegramApiException) {
            LOG.error("Can't notify expiring user {}\n{}", user.pk, e.toString(), e)
        }

    }

}

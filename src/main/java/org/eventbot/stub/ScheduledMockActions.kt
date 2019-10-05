package org.eventbot.stub

import org.eventbot.event.EventOrganizer
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import javax.transaction.Transactional

@Component
@Profile("test_local")
open class ScheduledMockActions(
        private val userRepository: UserRepository,
        private val groupRepository: GroupRepository,
        private val eventOrganizer: EventOrganizer) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    @Scheduled(cron = "*/180 * * * * *") // every N sec
    @Throws(TelegramApiException::class)
    open fun doFrequently() {
        logger.info("Adding mock user to random group")
        dummyJoinPublicGroup()

        dummySendEvent()
    }

    private fun dummySendEvent() {
        val user = randomDummy()

        if (user.groups.isNotEmpty()) {
            val group = randomPublicGroup()

            group?.let{
                eventOrganizer.organizeEvent(user, group)
            }
        }
    }

    private fun dummyJoinPublicGroup() {
        val user = randomDummy()

        joinRandomGroup(user)

    }

    private fun randomDummy() = userRepository.findByIsBot(isBot = true).random()

    private fun joinRandomGroup(user: UserInfo) {
        val randomGroup = randomPublicGroup()

        randomGroup?.let {
            it.addMember(user)
            userRepository.save(user)
            groupRepository.save(randomGroup)

        }
    }

    private fun randomPublicGroup(): Group? {
        return groupRepository.findByPrivateFalse().random()
    }
}

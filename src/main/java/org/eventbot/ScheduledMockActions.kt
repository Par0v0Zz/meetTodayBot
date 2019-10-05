package org.eventbot

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
import java.util.*

@Component
@Profile("test_local")
class ScheduledMockActions(
        private val userRepository: UserRepository,
        private val groupRepository: GroupRepository) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(cron = "*/5 * * * * *") // every 5 sec
    @Throws(TelegramApiException::class)
    fun createUserAndJoinPublicGroup() {
        logger.info("Adding mock user to random group")
        createDummyUserAndJoinSomePublicGroup()
    }

    private fun createDummyUserAndJoinSomePublicGroup() {
        val user = createDummyUser()

        val groups = groupRepository.findByPrivateFalse()

        if (groups.isNotEmpty()) {
            joinRandomGroup(groups, user)
        }

    }

    private fun joinRandomGroup(groups: Collection<Group>, user: UserInfo) {
        val randomGroup = groups.random()

        randomGroup.addMember(user)

        userRepository.save(user)
        groupRepository.save(randomGroup)

    }

    private fun createDummyUser(): UserInfo {
        val user = UserInfo(0, "DummyUser" + Date())

        userRepository.save(user)
        return user
    }
}
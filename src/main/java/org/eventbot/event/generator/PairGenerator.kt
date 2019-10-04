package org.eventbot.event.generator



import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.eventbot.constant.BotConstants.MIN_DAYS_BETWEEN_SESSIONS
import org.eventbot.model.Event
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.UserRepository
import org.eventbot.service.TimeService
import java.util.*
import java.util.concurrent.ThreadLocalRandom
@Component
class PairGenerator(
        private val timeService: TimeService,
        private val userRepository: UserRepository
) {

    private val LOG = LoggerFactory.getLogger(javaClass)

    fun findPair(user: UserInfo, group: Group): Event? {
        val sessionDate = timeService.chooseSessionDate()
        return findPair(user, group, sessionDate)
    }

    fun findPair(user: UserInfo, group: Group, sessionDate: Date): Event? {
        val others = findAvailablePeers(user, group, sessionDate)

        if (others.isEmpty()) {
            LOG.debug("Pair not found, no available peers")
            return null
        }
        val event = pair(user, others, sessionDate)

        return null
    }

    private fun findAvailablePeers(user: UserInfo, group: Group, date: Date): List<UserInfo> {
        val dateThreshold = timeService.beginningOfDateMinusDaysFrom(date, MIN_DAYS_BETWEEN_SESSIONS)

        return userRepository.findByNoEventsAfter(dateThreshold, user, group)
    }

    private fun pair(first: UserInfo, others: List<UserInfo>, sessionDate: Date): Event {

        val random = ThreadLocalRandom.current()
        val pairIndex = random.nextInt(others.size)
        val second = others[pairIndex]

        val event = Event(
                first,
                sessionDate,
                false
        )

        event.addParticipant(first)
        event.addParticipant(second)

        return event
    }

}


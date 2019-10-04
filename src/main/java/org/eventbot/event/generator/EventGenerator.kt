package org.eventbot.event.generator



import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.eventbot.model.Event
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.UserRepository
import org.eventbot.service.TimeService
import java.util.*
import java.util.concurrent.ThreadLocalRandom
@Component
class EventGenerator(
        private val timeService: TimeService,
        private val userRepository: UserRepository
) {

    private val LOG = LoggerFactory.getLogger(javaClass)

    fun organizeLunch(user: UserInfo, group: Group, eventDate: Date): Event? {
        val others = findAvailablePeers(group)

        if (others.isEmpty()) {
            LOG.debug("Event not found, no available peers")
            return null
        }
        val event = lunch(user, others, eventDate)

        return null
    }

    private fun findAvailablePeers(group: Group): List<UserInfo> {
        return userRepository.findByGroup(group)
    }

    private fun lunch(first: UserInfo, others: List<UserInfo>, sessionDate: Date): Event {

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


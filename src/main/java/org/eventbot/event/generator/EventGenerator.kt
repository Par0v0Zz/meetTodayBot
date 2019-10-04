package org.eventbot.event.generator



import org.eventbot.model.Event
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.UserRepository
import org.eventbot.service.TimeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@Component
class EventGenerator(
        private val timeService: TimeService,
        private val userRepository: UserRepository
) {

    private val LOG = LoggerFactory.getLogger(javaClass)

    fun findPair(user: UserInfo, group: Group): Event? {
        val others = findAvailablePeers(group)

        if (others.isEmpty()) {
            LOG.debug("Pair not found, no available peers")
            return null
        }
        val event = pair(user, others)

        return null
    }

    private fun findAvailablePeers(group: Group): List<UserInfo> {
        return userRepository.findByGroup(group)
    }

    private fun pair(first: UserInfo, others: List<UserInfo>): Event {

        val random = ThreadLocalRandom.current()
        val pairIndex = random.nextInt(others.size)
        val second = others[pairIndex]

        val event = Event(
                first,
                second,
                ThreadLocalRandom.current().nextBoolean(),
                timeService.chooseSessionDate()
        )

        event.addParticipant(first)
        event.addParticipant(second)

        return event
    }

}


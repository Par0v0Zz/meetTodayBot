package org.eventbot.event.generator


import org.eventbot.model.Event
import org.eventbot.model.Group
import org.eventbot.model.Participant
import org.eventbot.model.UserInfo
import org.eventbot.service.TimeService
import org.eventbot.service.UserInfoLinkResolver
import org.springframework.stereotype.Component
import java.util.Date

@Component
class EventGenerator(val timeService: TimeService, val userInfoLinkResolver: UserInfoLinkResolver) {

    fun organizeLunch(user: UserInfo, group: Group, eventDate: Date): Event {
        val others = group.members.filter { it != user }.toSet()
        return lunch(user, others, timeService.datePlusHours(eventDate, 4))
    }

    private fun lunch(first: UserInfo, users: Set<UserInfo>, sessionDate: Date): Event {
        val event = Event(
                first,
                sessionDate,
                false,
                name = "Lunch time with ${userInfoLinkResolver.resolve(first)} and others",
                description = """
                    Lunch time is a great opportunity to find a friend. Just try!
                """.trimIndent()
        )
        users.forEach {
            val participant = Participant(it, event)
            event.addParticipant(participant)
        }
        return event
    }

}


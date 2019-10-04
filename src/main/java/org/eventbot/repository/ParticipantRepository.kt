package org.eventbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.eventbot.model.Participant
import org.eventbot.model.ParticipantId
import org.eventbot.model.UserInfo
import java.util.*

@Repository
interface ParticipantRepository : JpaRepository<Participant, ParticipantId> {
    @Query(value =
    """
        SELECT p FROM Participant p 
        JOIN Event e ON e = p.event
        WHERE e.date > :date
            AND p.accepted != org.eventbot.model.EventStatus.DECLINED
            AND p.user = :user
        ORDER BY e.date
    """)
    fun getParticipantsAfter(date: Date, user: UserInfo): List<Participant>
}
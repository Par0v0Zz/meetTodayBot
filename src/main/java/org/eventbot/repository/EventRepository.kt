package org.eventbot.repository

import org.eventbot.model.Event
import org.eventbot.model.EventStatus
import org.eventbot.model.UserInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface EventRepository : JpaRepository<Event, Long> {

    @Query(value = "SELECT e FROM Event e " +
            "JOIN Participant p ON e = p.event " +
            "WHERE e.date > :date " +
            "AND p.user = :user " +
            "ORDER BY e.date")
    fun getEventsAfter(date: Date, user: UserInfo): List<Event>

    @Query("""
        SELECT e FROM Event e 
        JOIN Participant p ON e = p.event 
        WHERE e.date > :date 
        AND p.user = :user 
        AND p.accepted = :status
        ORDER BY e.date
    """)
    fun getEventsAfterByStatus(date: Date, user: UserInfo, status: EventStatus): List<Event>

    fun existsByDateAfterAndParticipants_User(date: Date, user: UserInfo): Boolean

    fun findByDateBetweenAndAcceptedTrue(start: Date, end: Date): List<Event>

}
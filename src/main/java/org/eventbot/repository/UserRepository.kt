package org.eventbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.eventbot.model.Group
import org.eventbot.model.UserInfo

import java.util.Date
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserInfo, Long> {
    fun findByUserId(userId: Int?): Optional<UserInfo>

    fun existsByUserId(userId: Int?): Boolean

    @Query(value = "SELECT u FROM UserInfo AS u " +
            "WHERE u.group = :group " +
            "AND u != :user " +
            "AND NOT EXISTS (" +
            "SELECT e FROM Event AS e " +
            "JOIN Participant AS p ON e = p.event " +
            "WHERE p.user = u " +
            "AND e.date > :date" +
            ")")
    fun findByNoEventsAfter(date: Date, user: UserInfo, group: Group): List<UserInfo>

}
package org.eventbot.repository

import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserInfo, Long> {
    fun findByUserId(userId: Int?): Optional<UserInfo>

    fun existsByUserId(userId: Int?): Boolean

    @Query(value =
    """
        SELECT u FROM UserInfo u 
        WHERE :group in u.groups
    """)
    fun findByGroup(group: Group): List<UserInfo>
}
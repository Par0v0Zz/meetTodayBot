package org.eventbot.repository

import org.eventbot.model.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface GroupRepository : JpaRepository<Group, Long> {

    fun findByToken(token: UUID): Optional<Group>
}
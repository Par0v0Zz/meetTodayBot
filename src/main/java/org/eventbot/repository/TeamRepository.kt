package org.eventbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.eventbot.model.Group

import java.util.Optional
import java.util.UUID

@Repository
interface TeamRepository : JpaRepository<Group, Long> {

    fun findByToken(token: UUID): Optional<Group>
}
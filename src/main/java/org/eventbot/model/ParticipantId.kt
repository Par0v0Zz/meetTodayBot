package org.eventbot.model

import java.io.Serializable
import java.util.Objects

class ParticipantId : Serializable {
    val user: Long
    val event: Long

    constructor(user: Long, event: Long) {
        this.user = user
        this.event = event
    }

    override fun hashCode(): Int {
        return Objects.hash(this.user, this.event)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (!ParticipantId::class.java.isAssignableFrom(obj.javaClass)) {
            return false
        }
        val other = obj as ParticipantId?

        return this.user == other!!.user && this.event == other.event
    }

}
package org.eventbot.model

import java.io.Serializable
import java.util.Objects

class ParticipantId : Serializable {
    var user: Long? = null
    var event: Long? = null

    constructor(user: Long?, event: Long?) {
        this.user = user
        this.event = event
    }

    constructor()

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
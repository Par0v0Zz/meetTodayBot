package org.eventbot.model

import java.util.Date
import java.util.HashSet
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "events")
class Event(
        @ManyToOne @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
        var creator: UserInfo,
        var date: Date,
        var private: Boolean,
        var description: String = "",
        var accepted: EventStatus = EventStatus.NO_RESPONSE,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var pk: Long = 0,
        @OneToMany(cascade = [CascadeType.PERSIST], mappedBy = "event")
        var participants: MutableSet<Participant> = HashSet()
) {

    fun addParticipant(participant: Participant) {
        participants.add(participant)
        participant.event = this
    }

    fun removeParticipant(participant: Participant) {
        participants.remove(participant)
    }
}

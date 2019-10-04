package org.eventbot.model

import javax.persistence.*
import java.util.Date
import java.util.HashSet

@Entity
@Table(name = "events")
class Event(
        @ManyToOne @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
        var creator: UserInfo,

        @ManyToOne @JoinColumn(name = "partner_pk", referencedColumnName = "pk")
        var partner: UserInfo,
        var creatorHost: Boolean,
        var date: Date,
        var accepted: EventStatus = EventStatus.NO_RESPONSE,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var pk: Long = 0,
        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "event")
        var participants: MutableSet<Participant> = HashSet()
) {

    fun addParticipant(user: UserInfo) {
        val participant = Participant(user, this)
        participants.add(participant)
    }
}

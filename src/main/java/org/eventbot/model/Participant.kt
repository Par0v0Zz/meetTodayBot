package org.eventbot.model

import javax.persistence.*

@Entity
@Table(name = "participants")
@IdClass(ParticipantId::class)
class Participant(
        @Id @ManyToOne @JoinColumn (name = "user_pk", referencedColumnName = "pk")
        var user: UserInfo,
        @Id @ManyToOne @JoinColumn(name = "event_pk", referencedColumnName = "pk")
        var event: Event,
        var accepted: EventStatus = EventStatus.NO_RESPONSE,
        var host: Boolean = true
)

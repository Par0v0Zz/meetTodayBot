package org.eventbot.model

import javax.persistence.*
import java.util.HashSet
import java.util.UUID

@Entity
@Table(name = "teams")
class Group (
        @Column(name = "token", columnDefinition = "BINARY(16)")
        var token: UUID,
        @ManyToOne @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
        var creator: UserInfo,
        @OneToMany(mappedBy = "group")
        var members: MutableSet<UserInfo> = HashSet(),
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        var pk: Long = 0
) {
    fun addMember(user: UserInfo) {
        members.add(user)
        user.group = this
    }
}

package org.eventbot.model

import java.util.HashSet
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "teams")
class Group(
        @Column(name = "token", columnDefinition = "BINARY(16)")
        var token: UUID,
        @ManyToOne @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
        var creator: UserInfo,
        var private: Boolean,
        @ManyToMany(mappedBy = "groups")
        var members: MutableSet<UserInfo> = HashSet(),
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        var pk: Long = 0
) {
    fun addMember(user: UserInfo) {
        members.add(user)
        user.groups.add(this)
    }
}

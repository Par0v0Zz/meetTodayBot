package org.eventbot.model

import org.hibernate.annotations.CacheConcurrencyStrategy
import org.springframework.data.annotation.CreatedDate
import java.time.ZoneId
import java.util.Date
import java.util.HashSet
import javax.persistence.Cacheable
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users")
class UserInfo(
        var userId: Int,
        var firstName: String,

        var lastName: String? = null,

        @Temporal(TemporalType.TIMESTAMP)
        var lastDeclineDate: Date? = null,
        var lastMessageId: Int? = null,
        var timezone: ZoneId? = null,
        @ManyToMany
        @JoinTable(name = "user_group")
        var groups: MutableSet<Group> = HashSet(),

        @Temporal(TemporalType.TIMESTAMP) @CreatedDate
        var createdDate: Date? = null,

        @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
        var settings: MutableSet<UserSetting> = HashSet(),
        var xp: Int = 0,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var pk: Long = 0
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as UserInfo

                if (pk != other.pk) return false

                return true
        }

        override fun hashCode(): Int {
                return pk.hashCode()
        }
}

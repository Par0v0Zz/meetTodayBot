package org.eventbot.model

import org.hibernate.annotations.CacheConcurrencyStrategy
import org.springframework.data.annotation.CreatedDate
import java.time.ZoneId
import java.util.*
import javax.persistence.*

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
        @ManyToOne @JoinColumn(name = "team_pk", referencedColumnName = "pk")
        var group: Group? = null,

        @Temporal(TemporalType.TIMESTAMP) @CreatedDate
        var createdDate: Date? = null,

        @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
        var settings: MutableSet<UserSetting> = HashSet(),
        var xp: Int = 0,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var pk: Long = 0
)

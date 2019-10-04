package org.eventbot.model

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.eventbot.constant.SettingKey

import javax.persistence.*

@Entity
@Table(name = "user_settings")
@EntityListeners(AuditingEntityListener::class)
@IdClass(UserSettingId::class)
class UserSetting (
        @Id @ManyToOne @JoinColumn(name = "user_pk", referencedColumnName = "pk", nullable = false)
        var user: UserInfo,

        @Id @Enumerated @Column(columnDefinition = "smallint")
        var setting: SettingKey,

        var value: String
)

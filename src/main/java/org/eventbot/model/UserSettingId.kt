package org.eventbot.model

import org.eventbot.constant.SettingKey
import java.io.Serializable
import java.util.Objects

class UserSettingId : Serializable {
    var user: Long
    var setting: SettingKey

    constructor(user: Long, setting: SettingKey) {
        this.user = user
        this.setting = setting
    }

    override fun hashCode(): Int {
        return Objects.hash(this.user, this.setting)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (!UserSettingId::class.java.isAssignableFrom(obj.javaClass)) {
            return false
        }
        val other = obj as UserSettingId?

        return this.user == other!!.user && this.setting == other.setting
    }
}
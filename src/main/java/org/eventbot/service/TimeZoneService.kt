package org.eventbot.service

import net.iakovlev.timeshape.TimeZoneEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.eventbot.model.UserInfo
import org.eventbot.repository.UserRepository

import java.time.ZoneId
import java.util.Optional

@Component
class TimeZoneService {
    private val tzEngine = TimeZoneEngine.initialize()
    @Autowired
    private val userRepository: UserRepository? = null

    fun setTimeZone(lat: Float, lng: Float, user: UserInfo): Optional<ZoneId> {
        val maybeZone = getTimeZone(lat, lng)

        maybeZone.ifPresent { zone ->
            user.timezone = zone
            userRepository!!.save(user)
        }

        return maybeZone
    }

    private fun getTimeZone(lat: Float, lng: Float): Optional<ZoneId> {

        return tzEngine.query(lat.toDouble(), lng.toDouble())
    }

}

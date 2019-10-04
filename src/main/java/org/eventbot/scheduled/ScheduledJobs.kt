package org.eventbot.scheduled

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.eventbot.service.NotificationService

import java.time.Duration

@Component
class ScheduledJobs {

    @Autowired
    private val notificationService: NotificationService? = null

    @Scheduled(cron = "0 0/30 * * * *") // every 30 min
    fun updateStatsForAll() {
        notificationService!!.notifyAllUpcomingIn(Duration.ofHours(1), SCAN_PERIOD)
    }

    companion object {
        private val SCAN_PERIOD = Duration.ofMinutes(30)
    }
}

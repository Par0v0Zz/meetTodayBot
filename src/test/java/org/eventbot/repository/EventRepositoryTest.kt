package org.eventbot.repository

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.eventbot.Application
import org.eventbot.model.Event
import org.eventbot.model.UserInfo
import org.eventbot.service.TimeService

import java.util.Date

import org.junit.Assert.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Application::class])
class EventRepositoryTest {

    @Autowired
    private val systemUnderTest: EventRepository? = null

    private val timeService = TimeService()
    private val date = Date()
    private var event: Event? = null
    private var user: UserInfo? = null

    @Before
    fun setup() {
        user = UserInfo(0, "Vasya")
        val partner = UserInfo(0, "Petya")

        event = Event(user!!, Date(), true)
        event!!.addParticipant(user!!)
    }

    @Test
    fun givenEventBeforeGivenDate_whenExistsByDate_thenFalse() {
        //given
        saveEventWithDateShift(-1)

        //then
        assertFalse(systemUnderTest!!.existsByDateAfterAndParticipants_User(date, user!!))
    }

    @Test
    fun givenEventAfterGivenDate_whenExistsByDate_thenTrue() {
        //given
        saveEventWithDateShift(1)

        //then
        assertTrue(systemUnderTest!!.existsByDateAfterAndParticipants_User(date, user!!))
    }

    private fun saveEventWithDateShift(hours: Int) {
        event!!.date = timeService.datePlusHours(date, hours)
        systemUnderTest!!.save(event!!)
    }

}
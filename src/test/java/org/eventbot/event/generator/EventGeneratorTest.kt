package org.eventbot.event.generator

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import org.eventbot.Application
import org.eventbot.constant.BotConstants.MIN_DAYS_BETWEEN_SESSIONS
import org.eventbot.model.Event
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository

import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Application::class])
@Transactional
class PairGeneratorTest {

    private val format = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    @Autowired
    private val systemUnderTest: PairGenerator? = null
    @Autowired
    private val groupRepository: GroupRepository? = null
    @Autowired
    private val eventRepository: EventRepository? = null
    @Autowired
    private val userRepository: UserRepository? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {

    }

    @Test
    fun givenPartnerHasNoRecentEvents_whenFindPair_thenFound() {

    }

    @Test
    fun givenOneOfPartnersHasNoRecentEvents_whenFindPair_thenFindThisMember() {

    }

    @Test
    fun givenAllPartnersHaveRecentEvent_whenFindPair_thenNotFound() {

    }

    private fun newDummyUser(lastSessionDate: Date): UserInfo {
        val randomInt = ThreadLocalRandom.current().nextInt()

        return UserInfo(randomInt, "" + randomInt)
    }

    private fun datePlusDays(date: Date, days: Int): Date {
        return Date.from(date.toInstant().plus(days.toLong(), ChronoUnit.DAYS))
    }

}
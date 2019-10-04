package org.eventbot.event.generator

import org.eventbot.Application
import org.eventbot.constant.BotConstants.MIN_DAYS_BETWEEN_SESSIONS
import org.eventbot.model.Event
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [Application::class])
@Transactional
class EventGeneratorTest {

    private val format = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    private var sessionDate: Date? = null
    private var group: Group? = null
    private var user: UserInfo? = null
    private var member_noRecentEvent: UserInfo? = null
    private var member_recentEvent: UserInfo? = null

    @Autowired
    private val systemUnderTest: EventGenerator? = null
    @Autowired
    private val groupRepository: GroupRepository? = null
    @Autowired
    private val eventRepository: EventRepository? = null
    @Autowired
    private val userRepository: UserRepository? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        sessionDate = format.parse("01-01-2000")
        val oldSessionDate = datePlusDays(sessionDate!!, -MIN_DAYS_BETWEEN_SESSIONS - 1)
        val recentSessionDate = datePlusDays(sessionDate!!, -MIN_DAYS_BETWEEN_SESSIONS + 1)

        user = UserInfo(0, "Vasya")
        member_noRecentEvent = newDummyUser(oldSessionDate)
        member_recentEvent = newDummyUser(recentSessionDate)

        var event = Event(user!!, member_noRecentEvent!!, true, oldSessionDate)
        event.addParticipant(member_noRecentEvent!!)
        event.date = oldSessionDate
        eventRepository!!.save(event)

        event = Event(user!!, member_recentEvent!!, true, recentSessionDate)
        eventRepository.save(event)

        group = Group(UUID.randomUUID(), user!!, false)
        group!!.addMember(user!!)

    }

    @Test
    fun givenPartnerHasNoRecentEvents_whenFindPair_thenFound() {
        //given
        group!!.addMember(member_noRecentEvent!!)
        groupRepository!!.save(group!!)

        //when
        val pair = systemUnderTest!!.findPair(user!!, group!!)

        //then
        val actual = pair?.partner
        Assert.assertEquals(member_noRecentEvent, actual)
    }

    @Test
    fun givenOneOfPartnersHasNoRecentEvents_whenFindPair_thenFindThisMember() {
        //given
        group!!.addMember(member_recentEvent!!)
        group!!.addMember(member_noRecentEvent!!)
        groupRepository!!.save(group!!)

        //when
        val pair = systemUnderTest!!.findPair(user!!, group!!)

        //then
        val actual = pair?.partner
        Assert.assertEquals(member_noRecentEvent, actual)
    }

    @Test
    fun givenAllPartnersHaveRecentEvent_whenFindPair_thenNotFound() {
        //given
        group!!.addMember(member_recentEvent!!)
        groupRepository!!.save(group!!)

        //when
        val pair = systemUnderTest!!.findPair(user!!, group!!)

        //then
        Assert.assertTrue(pair == null)
    }


    private fun newDummyUser(lastSessionDate: Date): UserInfo {
        val randomInt = ThreadLocalRandom.current().nextInt()

        return UserInfo(randomInt, "" + randomInt)
    }

    private fun datePlusDays(date: Date, days: Int): Date {
        return Date.from(date.toInstant().plus(days.toLong(), ChronoUnit.DAYS))
    }

}
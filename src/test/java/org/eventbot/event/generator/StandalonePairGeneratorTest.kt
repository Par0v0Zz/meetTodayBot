package org.eventbot.event.generator

import org.apache.commons.lang3.tuple.Pair
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.eventbot.event.generator.standalone.StandalonePairGenerator
import org.eventbot.model.Person

import java.util.*

class StandalonePairGeneratorTest {
    private val LOG = LoggerFactory.getLogger(javaClass)

    private val systemUnderTest = StandalonePairGenerator()

    private var members: MutableList<Person>? = null

    @Before
    fun setup() {
        members = ArrayList()

        members!!.add(Person(0, "A", true))
        members!!.add(Person(1, "B", true))
        members!!.add(Person(2, "C", true))
        members!!.add(Person(3, "D", true))
        members!!.add(Person(4, "E", true))
        members!!.add(Person(5, "F", true))
    }

    @Test
    fun testGeneratePairs() {
        val pairs = systemUnderTest.generatePairs(members as List<Person>)

        val membersNotRepete = membersNotRepete(pairs)
        if (!membersNotRepete) {
            LOG.warn("Members repete in pairs!")
        }

        Assert.assertTrue(membersNotRepete)

        val allMastersHavePairs = allMastersHavePairs(members as List<Person>, pairs)
        if (!allMastersHavePairs) {
            LOG.warn("Not all members have pairs!")
        }
        Assert.assertTrue(allMastersHavePairs)

        //todo: test all masters have pairs

        pairs.forEach { pair ->
            val left = pair.left
            val firstMemberLabel = systemUnderTest.projectHolderPrefix(left) + left.name!!
            val right = pair.right
            val secondMemberLabel = systemUnderTest.projectHolderPrefix(right) + right.name!!
            LOG.info(String.format("%s, %s", firstMemberLabel, secondMemberLabel))
        }
    }

    private fun allMastersHavePairs(members: List<Person>, pairs: List<Pair<Person, Person>>): Boolean {
        val pairedMembers = HashSet<Person>()
        pairs.forEach { pair ->
            pairedMembers.add(pair.right)
            pairedMembers.add(pair.left)
        }
        return members.filter { it.active }.filter { it.master }.all { pairedMembers.contains(it) }
    }

    private fun membersNotRepete(pairs: List<Pair<Person, Person>>): Boolean {
        val persons = HashSet<Person>(pairs.size * 2)

        pairs.forEach { pair ->
            persons.add(pair.left)
            persons.add(pair.right)
        }
        return persons.size == pairs.size * 2
    }


}



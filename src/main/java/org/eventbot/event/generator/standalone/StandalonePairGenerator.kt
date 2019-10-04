package org.eventbot.event.generator.standalone

import com.google.common.collect.Iterables
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.slf4j.LoggerFactory
import org.eventbot.model.Person

import java.util.ArrayList
import java.util.LinkedList
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

class StandaloneEventGenerator {
    private val LOG = LoggerFactory.getLogger(javaClass)

    fun generatePairs(members: List<Person>): List<Pair<Person, Person>> {
        val pairs = ArrayList<Pair<Person, Person>>()

        val activeMembers = members.filter { it.active } as MutableList<Person>

        val activeMasters = activeMembers.filter{ it.master } as MutableList<Person>

        var person: Person?
        while (activeMasters.isNotEmpty()) {
            person = Iterables.getFirst(activeMasters, null)
            val other = findPair(person, activeMembers, activeMasters)

            pairs.add(ImmutablePair(person, other))
        }
        return pairs
    }

    fun projectHolderPrefix(person: Person): String {
        return if (person.isProjectHolder) "*" else ""
    }

    private fun findPair(currentUser: Person?, members: MutableList<Person>, masters: MutableList<Person>): Person {
        val random = ThreadLocalRandom.current()
        while (members.size > 1) {
            val pairIndex = random.nextInt(members.size)
            val pair = members[pairIndex]
            if (pair !== currentUser) {
                masters.remove(pair)
                members.removeAt(pairIndex)

                if (!masters.isEmpty()) {
                    masters.removeAt(0)
                }
                if (!members.isEmpty()) {
                    members.removeAt(0)
                }

                chooseProjectHolder(currentUser, pair)
                return pair
            }
        }
        throw IllegalStateException("Algorithm should never get there, check your logic")
    }

    private fun chooseProjectHolder(currentUser: Person?, pair: Person) {
        val firstIsProjectHolder = ThreadLocalRandom.current().nextBoolean()

        if (firstIsProjectHolder) {
            currentUser!!.isProjectHolder = true
        } else {
            pair.isProjectHolder = true
        }
    }
}


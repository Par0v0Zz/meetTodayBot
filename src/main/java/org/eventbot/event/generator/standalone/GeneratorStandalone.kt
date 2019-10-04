package org.eventbot.event.generator.standalone

import org.eventbot.model.Person

import java.util.ArrayList

object GeneratorStandalone {
    private val EventGenerator = StandaloneEventGenerator()
    private var members: MutableList<Person> = ArrayList()

    init {
        members.add(Person(0, "Sergey", true))
        members.add(Person(1, "Vlad", true))
        members.add(Person(2, "Maksim", true))
        members.add(Person(3, "Elena", true))
        members.add(Person(4, "Nikita", true))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val pairs = EventGenerator.generatePairs(members)

        println("Pairs for this week:")
        pairs.forEach { pair ->
            val left = pair.left
            val firstMemberLabel = EventGenerator.projectHolderPrefix(left) + left.name!!
            val right = pair.right
            val secondMemberLabel = EventGenerator.projectHolderPrefix(right) + right.name!!
            println(String.format("%s, %s", firstMemberLabel, secondMemberLabel))
        }
    }
}

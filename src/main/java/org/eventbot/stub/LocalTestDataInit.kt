package org.eventbot.stub

import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID
import javax.annotation.PostConstruct
import kotlin.random.Random

@Component
@Profile("test_local")
class LocalTestDataInit(
        open var groupRepository: GroupRepository,
        open var userRepository: UserRepository) {

    private val femaleFirstnames = listOf("Galina", "Larisa", "Anfisa", "Raisa", "Natasha", "Nadiya", "Tatiana", "Valentina", "Veronika",
            "Zenya", "Alena", "Alina", "Svetlana", "Katya")

    private val femaleLastnames = listOf("Kuznetsova", "Popova", "Vasilieva", "Petrova", "Sokolova", "Fedorova", "Volkova", "Lebedeva",
            "Egorova", "Pavlova", "Kozlova", "Stepanova", "Nikolaeva", "Orlova")

    private val maleFirstnames = listOf("Kirill", "Timofei", "Tolik", "Matvei", "Danila", "Maksim")

    private val maleLastnames = listOf("Kuznetsov", "Popov", "Vasiliev", "Petrov", "Sokolov", "Fedorov", "Volkov", "Lebedev",
            "Egorov", "Pavlov", "Kozlov", "Stepanov", "Nikolaev", "Orlov")

    @PostConstruct
    fun initData() {
        createDummyGroup("lunches", createDummyUser())
        createDummyGroup("Kicker", createDummyUser())
        createDummyGroup("Guitar Hero", createDummyUser())
        createDummyGroup("Cinema", createDummyUser())
        createDummyGroup("Soccer", createDummyUser())
        createDummyGroup("Evening Run", createDummyUser())
        createDummyGroup("Boardgames", createDummyUser())
        createDummyGroup("Spanish Speaking Club", createDummyUser())

    }

    private fun createDummyUser(): UserInfo {
        val female: Boolean = Random.nextBoolean()
        val firstnamesSet = if (female) femaleFirstnames else maleFirstnames
        val lastnamesSet = if (female) femaleLastnames else maleLastnames

        val user = UserInfo(
                userId = 0,
                firstName = firstnamesSet.random(),
                lastName = lastnamesSet.random(),
                isBot = true
        )

        userRepository.save(user)
        return user
    }

    private fun createDummyGroup(name: String, creator: UserInfo) {

        val group = Group(UUID.randomUUID(), creator, false, name, name)

        userRepository.save(creator)
        groupRepository.save(group)
    }
}
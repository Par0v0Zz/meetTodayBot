package org.eventbot.stub

import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID
import javax.annotation.PostConstruct

@Component
@Profile("test_local")
class LocalTestDataInit(
        open var groupRepository: GroupRepository,
        open var userRepository: UserRepository) {

    private val firstnames = listOf("Muhammad", "Sai", "Madhavaditya", "Raahithya", "Rudra", "Advaith", "Shivansh", "Zayn",
            "Gautam", "Agastya", "Aadesh", "Amandeep", "Bharat", "Nakul", "Mehul", "Lalit", "Karthik", "Vijay", "Vinod", "Kamal")

    private val lastnames = listOf("Feliciano", "Fayre", "Chukwueneka", "Canciana", "Berengár", "Bárbara", "Arama", "Alipha",
            "Affonso", "Flávia", "Francisca", "Nathalia", "Okorie", "Oson", "Shel", "Tequila", "Tor", "Tristao", "Zoie", "Gijima")

    @PostConstruct
    fun initData() {

        createDummyGroup("Dummy lunches", createDummyUser())
        createDummyGroup("Dummy Kicker", createDummyUser())
        createDummyGroup("Dummy Guitar Hero", createDummyUser())
        createDummyGroup("Dummy Cinema", createDummyUser())
        createDummyGroup("Dummy Soccer", createDummyUser())
        createDummyGroup("Dummy Evening Run", createDummyUser())
        createDummyGroup("Dummy Boardgames", createDummyUser())
        createDummyGroup("Dummy Spanish Speaking Club", createDummyUser())

    }

    private fun createDummyUser(): UserInfo {
        val user = UserInfo(
                userId = 0,
                firstName = "Dummy" + firstnames.shuffled().take(1)[0],
                lastName = lastnames.shuffled().take(1)[0]
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
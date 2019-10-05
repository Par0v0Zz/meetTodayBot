package org.eventbot

import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
@Profile("test_local")
class LocalDataInit(
        open var groupRepository: GroupRepository,
        open var userRepository: UserRepository) {
    @PostConstruct
    fun initData(){
        createDummyGroup("Dummy Lunches")
        createDummyGroup("Dummy Kicker")
        createDummyGroup("Dummy Guitar Hero")
        createDummyGroup("Dummy Cinema")
        createDummyGroup("Dummy Soccer")
        createDummyGroup("Dummy Evening Run")
        createDummyGroup("Dummy Boardgames")
        createDummyGroup("Dummy Spanish Speaking Club")
    }

    private fun createDummyGroup(name: String) {
        val creator = UserInfo(0, "DummyUser0")
        val group = Group(UUID.randomUUID(), creator, false, name, name)

        userRepository.save(creator)
        groupRepository.save(group)
    }
}
package org.eventbot.stub

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
class LocalTestDataInit(
        open var groupRepository: GroupRepository,
        open var userRepository: UserRepository) {
    @PostConstruct
    fun initData(){

        createDummyUser()
        createDummyUser()
        createDummyUser()
        createDummyGroup("Dummy lunches", createDummyUser())

    }

    private fun createDummyUser(): UserInfo {
        val user = UserInfo(0, "DummyUser" + Date())

        userRepository.save(user)
        return user
    }

    private fun createDummyGroup(name: String, creator: UserInfo) {

        val group = Group(UUID.randomUUID(), creator, false, name, name)

        userRepository.save(creator)
        groupRepository.save(group)
    }
}
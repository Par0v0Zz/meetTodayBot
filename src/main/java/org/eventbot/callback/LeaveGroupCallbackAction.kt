package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component

@Component
class LeaveGroupCallbackAction(
        val groupRepository: GroupRepository,
        val userRepository: UserRepository,
        val messageService: MessageService,
        val keyboardService: KeyboardService
) : CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val argument: String? = context[CallbackParams.ARG] as String?
        val user = context[CallbackParams.USER_INFO] as UserInfo

        val group = groupRepository.findByPk(java.lang.Long.valueOf(argument))

        if (group.creator.equals(user)) {
            if (group.members.size > 1) {
                group.creator = group.members.first { it != user }
            } else {
                return "Can't remove last member"
            }
        }
        group.removeMember(user)
        groupRepository.save(group)
        userRepository.save(user)
        return "Removed"
    }

}
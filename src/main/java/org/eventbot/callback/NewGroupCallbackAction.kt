package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.eventbot.service.FreeTextInputService
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.lang.Boolean.valueOf
import java.util.UUID


@Component
class NewGroupCallbackAction(
        val groupRepository: GroupRepository,
        val userRepository: UserRepository,
        val messageService: MessageService,
        val keyboardService: KeyboardService,
        val freeTextInputService: FreeTextInputService
) : CallbackAction {

    val LOG: Logger = LoggerFactory.getLogger(NewGroupCallbackAction::class.java)

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val argument: String? = context[CallbackParams.ARG] as String?
        val private = if (argument != null) valueOf(argument) else false
        val user = context[CallbackParams.USER_INFO] as UserInfo
        val chatId = context[CallbackParams.CHAT_ID] as Long

        val group = newGroup(user, private)
        sendJoinLink(chatId, group)

        freeTextInputService.registerFreeTextContextObject(chatId, group)
        return ""
    }

    private fun newGroup(creator: UserInfo, private: Boolean): Group {
        val group = Group(
                UUID.randomUUID(),
                creator,
                private
        )
        group.addMember(creator)
//        group.addMember(newDummyUser())

        //todo: change to one save with cascade = merge and optional save if new UserInfo ?
        groupRepository.save(group)
        userRepository.save(creator)

        return group
    }

    private fun sendJoinLink(chatId: Long, group: Group) {
        val message = messageService.getMessageWithKeyboard(
                chatId,
                messageService.getJoinTeamText(group),
                keyboardService.groupActionsKeyboard())
        try {
            messageService.sendMessage(message)
        } catch (e: TelegramApiException) {
            LOG.error("Sending join link failed", e)
        }
    }

}
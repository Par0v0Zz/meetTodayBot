package org.eventbot.callback

import org.eventbot.CallbackAction
import org.eventbot.model.Group
import org.eventbot.repository.GroupRepository
import org.eventbot.service.FreeTextInputService
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
open class RenameGroupCallbackAction(private val freeTextInputService: FreeTextInputService,
                                     private val groupRepository: GroupRepository,
                                     val messageService: MessageService,
                                     val keyboardService: KeyboardService) : CallbackAction {

    @Transactional
    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val chatId = context[CallbackParams.CHAT_ID] as Long
        freeTextInputService.registerFreeTextContextAction(chatId) { group: Any, text ->
            if (group is Group) {
                group.name = text
                groupRepository.save(group)

                messageService.sendMessage(
                        chatId,
                        "Group was renamed, to view all groups type /groups"
                )
            }
        }

        messageService.sendMessage(chatId, "Type name with prefix '>' and press enter")
        return ""
    }

}

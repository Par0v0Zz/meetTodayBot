package org.eventbot.callback

import org.apache.commons.lang3.StringUtils
import org.eventbot.CallbackAction
import org.eventbot.model.Group
import org.eventbot.repository.GroupRepository
import org.eventbot.service.FreeTextInputService
import org.eventbot.service.KeyboardService
import org.eventbot.service.MessageService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
open class AddDescriptionCallbackAction(private val freeTextInputService: FreeTextInputService,
                                        private val groupRepository: GroupRepository,
                                        val messageService: MessageService,
                                        val keyboardService: KeyboardService) : CallbackAction {

    @Transactional
    override fun doAction(context: Map<CallbackParams, Any>): String? {
        val chatId = context[CallbackParams.CHAT_ID] as Long
        freeTextInputService.registerFreeTextContextAction(chatId) { group: Any, text ->
            if (group is Group) {
                var trimmedText = StringUtils.abbreviate(text, 200)
                group.description = trimmedText
                groupRepository.save(group)

                messageService.sendMessage(chatId, "Group ${group.name} description added")
            }
        }

        messageService.sendMessage(chatId, "Type description (200 symbols max) with prefix '>' and press enter")
        return ""
    }

}

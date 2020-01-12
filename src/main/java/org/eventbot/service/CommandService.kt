package org.eventbot.service

import org.eventbot.constant.BotCommand
import org.eventbot.model.UserInfo
import org.eventbot.navigation.Menu
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.util.Optional
import java.util.UUID


@Component
open class CommandService(
        open var userService: UserService,
        open var messageService: MessageService,
        open var keyboardService: KeyboardService,
        open var groupRepository: GroupRepository,
        open var userRepository: UserRepository,
        val menu: Menu,
        var text: String = """Hi! 
I'm very young bot, but I already know some tricks!
What you can do with me:
- You can list all available public groups using /publicgroups command 
and join them with just one tap on their names

- You can ask your mates for invitation link to join their public or private group

- Or, you even can create your own group! Just don't forget to name it properly 
and give good description to attract more people to join.
    """.trimIndent()
) {

    @Throws(TelegramApiException::class)
    @Transactional
    open fun processCommand(message: Message) {
        val commandTextOpt = extractCommandText(message)

        if (commandTextOpt.isPresent) {
            val commandText = commandTextOpt.get()

            val chatId = message.chatId
            val user = userService.findByUserId(message.from.id)
            when (BotCommand.valueOf(commandText.toUpperCase())) {
                BotCommand.START -> {
                    val messageText = message.text
                    if (commandText.length < messageText.length - 1) {
                        val groupToken = messageText.substring(commandText.length + 1).trim()

                        user?.let {
                            val groupName = joinTeamByToken(groupToken, user)
                            val messageToSend = messageService.getMessage(chatId, messageService.groupInfo(user, groupName))
                            messageService.sendMessage(messageToSend)
                        }
                    } else {
                        val sendMessage = messageService.getMessageWithKeyboard(
                                chatId,
                                text,
                                keyboardService.startKeyboard)
                        messageService.sendMessage(sendMessage)
                    }
                }
                BotCommand.SET_LOCATION -> messageService.requestLocation(chatId)
                BotCommand.VOID -> TODO()
                BotCommand.GROUPS -> {
                    user?.let {
                        sendMainMenu(chatId, user)
                    }
                }
                BotCommand.PUBLICGROUPS -> messageService.sendMessage(chatId, messageService.publicGroupsInfo())
                BotCommand.EVENTS -> {
                    val sendMessage = messageService.getMessageWithKeyboard(chatId, "Select event type",
                            keyboardService.infoEventOptionsKeyboard())
                    messageService.sendMessage(sendMessage)
                }
            }
        }
    }

    private fun sendMainMenu(chatId: Long, user: UserInfo) {

        val sendMessage = messageService.getMessageWithKeyboard(chatId, "Select group type",
                menu.getMenuKeyboardScreenOne(chatId, user))
        val sentMessageId = messageService.sendMessage(sendMessage)

        val user = userService.findByUserId(chatId.toInt())

        user?.lastMessageId = sentMessageId
    }

    private fun joinTeamByToken(token: String, user: UserInfo): String {
        val groupOpt = groupRepository.findByToken(UUID.fromString(token))
        var groupName = " "
        groupOpt.ifPresent { group ->
            group.addMember(user)
            groupRepository.save(group)
            userRepository.save(user)
            groupName = group.name ?: group.token.toString()
        }
        return groupName
    }

    private fun extractCommandText(message: Message): Optional<String> {
        val entitiesOpt = Optional.ofNullable(message.entities)

        return entitiesOpt.flatMap(this::extractCommandText)
    }

    private fun extractCommandText(entities: List<MessageEntity>): Optional<String> {
        return entities.stream()
                .filter { e -> e != null && e.offset == 0 && EntityType.BOTCOMMAND == e.type }
                .findFirst()
                .map { this.removeMentionAndSlash(it.text) }
    }

    private fun removeMentionAndSlash(commandText: String): String {

        val startPos = if (commandText[0] == '/') 1 else 0

        val indexOfMention = commandText.indexOf("@")

        return if (indexOfMention != -1) {
            commandText.substring(startPos, indexOfMention)
        } else {
            commandText.substring(startPos)
        }
    }

}

package org.vors.pairbot.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.vors.pairbot.constant.BotCommand
import org.vors.pairbot.event.EventOrganizer
import org.vors.pairbot.model.UserInfo
import org.vors.pairbot.repository.TeamRepository
import java.util.Optional
import java.util.UUID


@Transactional
@Component
open class CommandService(
        open var userService: UserService,
        open var messageService: MessageService,
        open var keyboardService: KeyboardService,
        open var chatService: ChatService,
        open var teamRepository: TeamRepository,
        open var eventOrganizer: EventOrganizer
) {
    private val LOG = LoggerFactory.getLogger(CommandService::class.java)


    @Throws(TelegramApiException::class)
    fun processCommand(message: Message) {
        val commandTextOpt = extractCommandText(message)

        if (commandTextOpt.isPresent) {
            val commandText = commandTextOpt.get()

            val chatId = message.chatId
            val user = userService.findByUserId(message.from.id).get()
            when (BotCommand.valueOf(commandText.toUpperCase())) {
                BotCommand.START -> {
                    val messageText = message.text
                    if (commandText.length < messageText.length - 1) {
                        val teamToken = messageText.substring(commandText.length + 1).trim()
                        joinTeamByToken(teamToken, user)

                        messageService.sendMessage(chatId, messageService.teamInfo(user))

                    } else {
                        val sendMessage = messageService.getMessageWithKeyboard(
                                chatId,
                                "Hi! Let's make a team. \nOr ask your peers for a link to join",
                                keyboardService.startKeyboard)
                        messageService.sendMessage(sendMessage)
                    }
                }
                BotCommand.PAIR -> {
                    eventOrganizer.tryOrganizeEvent(user)
                }
                BotCommand.MYTEAM -> messageService.sendMessage(chatId, messageService.teamInfo(user))
                BotCommand.SET_LOCATION -> messageService.requestLocation(chatId)
                BotCommand.VOID -> TODO()
            }
        }
    }

    private fun joinTeamByToken(token: String, user: UserInfo) {
        val teamOpt = teamRepository.findByToken(UUID.fromString(token))

        teamOpt.ifPresent { team ->
            team.addMember(user)
            teamRepository.save(team)
        }
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

package org.eventbot.telegram

import com.google.common.base.Preconditions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.eventbot.constant.ChatUpdateHandlerFlow
import org.eventbot.service.UserService
import org.eventbot.telegram.handler.ChatUpdateHandler
import org.eventbot.telegram.handler.impl.CommandHandler
import org.eventbot.telegram.handler.impl.SetLocationHandler

import org.eventbot.constant.ChatUpdateHandlerFlow.*


@Component
class PairBot : TelegramLongPollingBot() {
    private val LOG = LoggerFactory.getLogger(PairBot::class.java)
    @Value("\${bot.token}")
    private val botToken: String? = null
    @Value("\${bot.username}")
    private val botUsername: String? = null
    var botUserId: Int? = null

    @Autowired
    private val callbackHandler: ChatUpdateHandler? = null
    @Autowired
    private val commandHandler: CommandHandler? = null
    @Autowired
    private val setLocationHandler: SetLocationHandler? = null
    @Autowired
    private val userService: UserService? = null

    override fun onUpdateReceived(update: Update) {
        try {
            callHandlers(update)

        } catch (e: RuntimeException) {
            LOG.error("Update handling failed!", e)
        }

    }

    private fun callHandlers(update: Update) {
        if (update.message != null) {
            LOG.info("Handling update for chat {}", update.message.chatId)
        }

        val handlerFlow = chooseHandlerFlow(update)

        val currentUser = resolveCurrentUser(update, handlerFlow)
        if (isNew(currentUser!!)) {
            userService!!.createAndSaveUser(currentUser)
        }

        when (handlerFlow) {
            CALLBACK -> try {
                callCallbackHandlers(update)
            } catch (e: TelegramApiException) {
                LOG.error("Callback processing failed", e)
            }

            COMMAND -> try {
                callCommandHandlers(update)
            } catch (e: TelegramApiException) {
                LOG.error("Command processing failed", e)
            }

            SET_LOCATION -> try {
                setLocationHandler!!.handle(update)
            } catch (e: TelegramApiException) {
                LOG.error("Setting location failed", e)
            }

        }
    }


    private fun isNew(currentUser: User): Boolean {
        return !userService!!.findByUserId(currentUser.id).isPresent
    }


    private fun resolveCurrentUser(update: Update, handlerFlow: ChatUpdateHandlerFlow): User? {
        when (handlerFlow) {
            CALLBACK -> return update.callbackQuery.from
            MEMBER_REMOVED -> return update.message.leftChatMember
            CHAT, COMMAND, SET_LOCATION -> return update.message.from
            else -> return null
        }
    }

    @Throws(TelegramApiException::class)
    private fun callCallbackHandlers(update: Update) {
        callbackHandler!!.handle(update)
    }

    @Throws(TelegramApiException::class)
    private fun callCommandHandlers(update: Update) {
        commandHandler!!.handle(update)
    }

    private fun chooseHandlerFlow(update: Update): ChatUpdateHandlerFlow {
        if (update.hasCallbackQuery()) {
            return CALLBACK
        }
        if (isCommand(update)) {
            return COMMAND
        }

        val message = update.message
        Preconditions.checkNotNull(message)

        if (message.location != null) {
            return SET_LOCATION
        }
        if (message.leftChatMember != null) {
            return MEMBER_REMOVED
        }
        return if (message.chat.isUserChat!!) {
            CHAT
        } else VOID
    }

    private fun isCommand(update: Update): Boolean {
        val message = update.message
        return message != null && message.isCommand
    }

    override fun getBotUsername(): String? {
        return botUsername
    }

    override fun getBotToken(): String? {
        return botToken
    }

}

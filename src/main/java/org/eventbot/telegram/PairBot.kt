package org.eventbot.telegram

import com.google.common.base.Preconditions
import org.eventbot.constant.ChatUpdateHandlerFlow
import org.eventbot.constant.ChatUpdateHandlerFlow.CALLBACK
import org.eventbot.constant.ChatUpdateHandlerFlow.CHAT
import org.eventbot.constant.ChatUpdateHandlerFlow.COMMAND
import org.eventbot.constant.ChatUpdateHandlerFlow.FREE_TEXT_INPUT
import org.eventbot.constant.ChatUpdateHandlerFlow.MEMBER_REMOVED
import org.eventbot.constant.ChatUpdateHandlerFlow.SET_LOCATION
import org.eventbot.constant.ChatUpdateHandlerFlow.VOID
import org.eventbot.service.UserService
import org.eventbot.telegram.handler.ChatUpdateHandler
import org.eventbot.telegram.handler.impl.CommandHandler
import org.eventbot.telegram.handler.impl.FreeTextInputHandler
import org.eventbot.telegram.handler.impl.SetLocationHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


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
    @Autowired
    private lateinit var freeTextInputHandler: FreeTextInputHandler

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

            FREE_TEXT_INPUT -> try {
                freeTextInputHandler.handle(update)
            } catch (e: TelegramApiException) {
                LOG.error("Frocessing user input failed", e)
            }

            MEMBER_REMOVED -> {
            }
            CHAT -> {
            }
            VOID -> {
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
            CHAT, COMMAND, SET_LOCATION, FREE_TEXT_INPUT -> return update.message.from
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

        if (message.hasText() && message.text.startsWith(">")) {
            return FREE_TEXT_INPUT
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

package org.eventbot.service

import com.google.common.base.Splitter
import org.eventbot.callback.AcceptDeclineGroupCallbackAction
import org.eventbot.callback.AcceptedEventsCallbackAction
import org.eventbot.callback.AddDescriptionCallbackAction
import org.eventbot.callback.AllEventsCallbackAction
import org.eventbot.callback.AllGroupsCallbackAction
import org.eventbot.callback.CallbackParams
import org.eventbot.callback.CreateEventCallbackAction
import org.eventbot.callback.LeaveGroupCallbackAction
import org.eventbot.callback.MyGroupsCallbackAction
import org.eventbot.callback.NewGroupCallbackAction
import org.eventbot.callback.RenameGroupCallbackAction
import org.eventbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR
import org.eventbot.constant.Callback
import org.eventbot.constant.Callback.ACCEPTED_EVENTS
import org.eventbot.constant.Callback.ACCEPT_DECLINE
import org.eventbot.constant.Callback.ADD_DESCRIPTION
import org.eventbot.constant.Callback.ADD_TO_GROUP
import org.eventbot.constant.Callback.ALL_EVENTS
import org.eventbot.constant.Callback.ALL_GROUPS
import org.eventbot.constant.Callback.LEAVE_GROUP
import org.eventbot.constant.Callback.LUNCH
import org.eventbot.constant.Callback.MY_GROUPS
import org.eventbot.constant.Callback.NEW_GROUP
import org.eventbot.constant.Callback.RENAME_GROUP
import org.eventbot.constant.Callback.VOID
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
open class CallbackService(
        @Autowired
        @Lazy
        open var bot: AbsSender,
        @Autowired
        open var userService: UserService,
        @Autowired
        open var eventRepository: EventRepository,
        @Autowired
        open var groupRepository: GroupRepository,
        @Autowired
        open var messageService: MessageService,
        @Autowired
        open var userRepository: UserRepository,
        @Autowired
        open var keyboardService: KeyboardService,
        @Autowired
        open var participantRepository: ParticipantRepository,
        @Autowired
        open var applicationContext: ApplicationContext
) {
    val LOG: Logger = LoggerFactory.getLogger(CommandService::class.java)

    @Throws(TelegramApiException::class)
    @Transactional
    open fun processKeyboardCallback(callbackquery: CallbackQuery) {
        val callbackParts = extractCallbackParts(callbackquery)
        if (CollectionUtils.isEmpty(callbackParts)) {
            return
        }

        val userId = callbackquery.from.id

        val userOpt = userService.findByUserId(userId)

        if (!userOpt.isPresent) {
            return
        }

        val user = userOpt.get()

        val params = mutableMapOf(
                CallbackParams.USER_INFO to user,
                CallbackParams.CHAT_ID to callbackquery.message.chatId,
                CallbackParams.MESSAGE_FROM to userId
        )

        if (callbackParts.size > 1) {
            params[CallbackParams.ARG] = callbackParts[1]
        }

        if (callbackParts.size > 2) {
            params[CallbackParams.ARG2] = callbackParts[2]
        }

        val answerText: String? = when (Callback.valueOf(callbackParts[0])) {
            NEW_GROUP -> applicationContext.getBean(NewGroupCallbackAction::class.java).doAction(params)
            ADD_TO_GROUP -> "ask your peers for a link"
            ACCEPT_DECLINE -> applicationContext.getBean(AcceptDeclineGroupCallbackAction::class.java).doAction(params)
            VOID -> TODO()
            ALL_GROUPS -> applicationContext.getBean(AllGroupsCallbackAction::class.java).doAction(params)
            MY_GROUPS -> applicationContext.getBean(MyGroupsCallbackAction::class.java).doAction(params)
            RENAME_GROUP -> applicationContext.getBean(RenameGroupCallbackAction::class.java).doAction(params)
            LEAVE_GROUP -> applicationContext.getBean(LeaveGroupCallbackAction::class.java).doAction(params)
            LUNCH -> applicationContext.getBean(CreateEventCallbackAction::class.java).doAction(params)
            ADD_DESCRIPTION -> applicationContext.getBean(AddDescriptionCallbackAction::class.java).doAction(params)
            Callback.GROUP_INFO -> {
                messageService.sendMessage(callbackquery.message.chatId, messageService.groupInfo(user, callbackParts[1]))

                ""
            }
            ALL_EVENTS -> applicationContext.getBean(AllEventsCallbackAction::class.java).doAction(params)
            ACCEPTED_EVENTS -> applicationContext.getBean(AcceptedEventsCallbackAction::class.java).doAction(params)
        }
        sendAnswerCallbackQuery(answerText, false, callbackquery)
    }

    /**
     * For testing when there's no second account
     */
    private fun newDummyUser(): UserInfo {
        val user = UserInfo(0, "Partner")
        userRepository.save(user)
        return user
    }


    @Throws(TelegramApiException::class)
    private fun sendAnswerCallbackQuery(text: String?, alert: Boolean, callbackquery: CallbackQuery) {
        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callbackquery.id
        answerCallbackQuery.showAlert = alert
        answerCallbackQuery.text = text
        bot.execute(answerCallbackQuery)
    }

    private fun extractCallbackParts(callbackquery: CallbackQuery): List<String> {
        val callbackData = callbackquery.data

        return Splitter.on(CALLBACK_DATA_SEPARATOR).splitToList(callbackData)
    }

}

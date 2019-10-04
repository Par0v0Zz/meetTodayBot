package org.eventbot.service

import com.google.common.base.Splitter
import org.eventbot.callback.CallbackParams
import org.eventbot.callback.ListGroupsByCreatorCallbackAction
import org.eventbot.callback.ListGroupsByMemberCallbackAction
import org.eventbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR
import org.eventbot.constant.Callback
import org.eventbot.model.Event
import org.eventbot.model.EventStatus
import org.eventbot.model.EventStatus.ACCEPTED
import org.eventbot.model.EventStatus.DECLINED
import org.eventbot.model.EventStatus.NO_RESPONSE
import org.eventbot.model.Group
import org.eventbot.model.ParticipantId
import org.eventbot.model.UserInfo
import org.eventbot.repository.EventRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.repository.TeamRepository
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
import org.eventbot.constant.Callback
import org.eventbot.model.*
import org.eventbot.repository.EventRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.UserRepository

import java.util.Date
import java.util.UUID

@Transactional
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
    fun processKeyboardCallback(callbackquery: CallbackQuery) {
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
        val chatId = callbackquery.message.chatId

        val answerText: String? = when (Callback.valueOf(callbackParts[0])) {
            Callback.NEW_TEAM -> {
                val group = newTeam(user)
                sendJoinLink(chatId, group)
                null
            }
            Callback.ADD_TO_TEAM -> "ask your peers for a link"
            Callback.ACCEPT_DECLINE -> {
                val eventPk = java.lang.Long.valueOf(callbackParts[1])

                val participant = participantRepository.getOne(ParticipantId(user.pk, eventPk))

                val accepted = java.lang.Boolean.valueOf(callbackParts[2])

                participant.accepted = if (accepted) ACCEPTED else DECLINED

                participantRepository.save(participant)

                if (!accepted) {
                    user.lastDeclineDate = Date()
                    userRepository.save(user)
                }

                val event = participant.event
                updateEvent(event, participant.accepted)
                updateInvite(event)
                "ok"
            }
            Callback.VOID -> TODO()
            Callback.GROUPS -> applicationContext.getBean(ListGroupsByCreatorCallbackAction::class.java)
                    .doAction(mapOf(
                            CallbackParams.USER_INFO to user,
                            CallbackParams.CHAT_ID to chatId
                    ))
            Callback.MY_GROUPS -> applicationContext.getBean(ListGroupsByMemberCallbackAction::class.java)
                    .doAction(mapOf(
                            CallbackParams.USER_INFO to user,
                            CallbackParams.CHAT_ID to chatId
                    ))
        }
        sendAnswerCallbackQuery(answerText, false, callbackquery)
    }

    private fun updateEvent(event: Event, participantAccept: EventStatus) {
        val responses = event.participants.map { it.accepted }

        if (participantAccept == DECLINED) {
            event.accepted = DECLINED
        } else if (responses.all { it == ACCEPTED }) {
            event.accepted = ACCEPTED
        }

        if (event.accepted != NO_RESPONSE) {
            eventRepository.save(event)
        }
    }

    private fun updateInvite(event: Event) {
        messageService.updateToAll(
                event,
                messageService::eventDescriptionText,
                keyboardService::acceptedInviteKeyboard)
    }

    open fun newTeam(creator: UserInfo): Group {
        val group = Group(
                UUID.randomUUID(),
                creator
        )
        group.addMember(creator)
//        group.addMember(newDummyUser())

        //todo: change to one save with cascade = merge and optional save if new UserInfo ?
        groupRepository.save(group)
        userRepository.save(creator)

        return group
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

    private fun sendJoinLink(chatId: Long, group: Group) {
        try {
            messageService.sendMessage(chatId, messageService.getJoinTeamText(group))
        } catch (e: TelegramApiException) {
            LOG.error("Sending join link failed", e)
        }

    }

}

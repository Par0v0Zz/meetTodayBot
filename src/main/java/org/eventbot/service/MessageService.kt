package org.eventbot.service

import com.ocpsoft.pretty.time.PrettyTime
import freemarker.template.Configuration
import freemarker.template.TemplateException
import org.apache.commons.lang3.StringUtils
import org.eventbot.model.Event
import org.eventbot.model.EventStatus
import org.eventbot.model.Group
import org.eventbot.model.Participant
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.repository.ParticipantRepository
import org.eventbot.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.IOException
import java.io.Serializable
import java.io.StringWriter
import java.time.ZoneId
import java.util.ArrayList
import java.util.Comparator.nullsFirst
import java.util.Date
import java.util.HashMap
import java.util.Optional
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2


@Component
class MessageService(
        @Value("\${bot.url}")
        var botUrl: String,
        @Lazy
        var bot: AbsSender,
        var keyboardService: KeyboardService,
        var userService: UserService,
        var freemarkerConfig: Configuration,
        var timeService: TimeService,
        var chatService: ChatService,
        var userRepository: UserRepository,
        var participantRepository: ParticipantRepository,
        var groupRepository: GroupRepository,
        var gameService: GameService
) {
    companion object {
        const val MAX_TEXT_MESSAGE_LENGTH = 4095
        const val EVENT_DESCRIPTION_TEMPLATE = "event_description.ftl"
    }

    private val LOG = LoggerFactory.getLogger(javaClass)
    private val prettyTime = PrettyTime()

    @Throws(TelegramApiException::class)
    fun sendMessage(chatId: Long, text: String): Int {
        return sendMessage(getMessage(chatId, truncateToMaxMessageLength(text)))
    }

    @Throws(TelegramApiException::class)
    fun sendMessage(sendMessage: SendMessage): Int {

        return Optional.ofNullable(bot.execute(sendMessage))
                .map { it.messageId }
                .orElse(null)
    }

    fun getMessage(chatId: Long, text: String): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.disableNotification()
        sendMessage.setChatId(chatId)
        sendMessage.text = text

        return sendMessage
    }

    @Throws(TelegramApiException::class)
    fun sendMessage(chatId: Long, text: String, keyboard: ReplyKeyboard): Int {
        return sendMessage(getMessageWithKeyboard(chatId, text, keyboard, ParseMode.MARKDOWN))
    }

    @JvmOverloads
    fun getMessageWithKeyboard(chatId: Long, text: String, keyboard: ReplyKeyboard, parseMode: String = ParseMode.MARKDOWN): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.replyMarkup = keyboard
        sendMessage.setParseMode(parseMode)
        sendMessage.disableNotification()
        sendMessage.setChatId(chatId)
        sendMessage.text = text
        sendMessage.disableWebPagePreview()

        return sendMessage
    }

    @Throws(TelegramApiException::class)
    fun editMessage(chatId: Long, messageId: Int, text: String, keyboard: InlineKeyboardMarkup): Serializable {
        val messageToEdit = getEditMessage(chatId, messageId, text, keyboard)

        return bot.execute(messageToEdit)
    }

    fun getEditMessage(chatId: Long, messageId: Int, text: String, keyboard: InlineKeyboardMarkup): EditMessageText {
        val messageToEdit = EditMessageText()
        messageToEdit.setChatId(chatId)
        messageToEdit.messageId = messageId
        messageToEdit.text = text
        messageToEdit.replyMarkup = keyboard
        messageToEdit.setParseMode(ParseMode.MARKDOWN)
        messageToEdit.disableWebPagePreview()
        return messageToEdit
    }


    fun truncateToMaxMessageLength(text: String): String {
        return StringUtils.abbreviate(text, MAX_TEXT_MESSAGE_LENGTH)
    }

    fun inviteText(user: UserInfo, pair: Event): String {
//        val eventDescription = eventDescriptionText(user, pair)

        return String.format("How about lunch today?")
    }

    //TODO: write correct description handling
    fun eventDescriptionText(user: UserInfo, event: Event): String {
        val ctx = HashMap<String, Any>()

        val creator = event.creator
        val creatorOk = isAccepted(event, creator)

        val instant = event.date.toInstant()
        val zone = chooseTimezone(creator)

        ctx["date"] = instant.atZone(zone)
        ctx["zone"] = zone.toString()
        ctx["accepted"] = event.accepted
        ctx["creatorLink"] = userLink(creator)

        creatorOk.let { ctx["creatorOk"] = it }
        ctx["pendingOther"] = false

        try {
            val stringWriter = StringWriter()
            freemarkerConfig.getTemplate(EVENT_DESCRIPTION_TEMPLATE).process(ctx, stringWriter)

            return stringWriter.toString()
        } catch (e: IOException) {
            LOG.error("Can't construct description from template", e)
            throw RuntimeException(e)
        } catch (e: TemplateException) {
            LOG.error("Can't construct description from template", e)
            throw RuntimeException(e)
        }

    }

    private fun chooseTimezone(creator: UserInfo): ZoneId {
        return if (creator.timezone == null) {
            ZoneId.of("UTC")
        } else {
            creator.timezone!!
        }
    }

    fun getUpcomingNotificationMessage(participant: Participant): SendMessage {
        val user = participant.user
        val chatId = chatService.getPrivateChatId(user)
        val text = getUpcomingNotificationText(participant)
        return getMessage(chatId, text)
    }

    private fun getUpcomingNotificationText(participant: Participant): String {
        val user = participant.user
        val event = participant.event
        return "Upcoming session in " + prettyTime.format(event.date) + ":\n\n" + eventDescriptionText(user, event)
    }

    @Throws(TelegramApiException::class)
    fun tryLaterText(user: UserInfo, hasDeclinedRecently: Boolean): String {
        return if (hasDeclinedRecently) {
            "To make sure the choice is random, everyone has one shot.\nNext try is available in " +
                    prettyTime.format(timeService.nextDateToCreateEvent(user))
        } else {
            val upcomingParticipants = participantRepository.getParticipantsAfter(Date(), user)
            if (upcomingParticipants.isNotEmpty()) {
                getUpcomingNotificationText(upcomingParticipants[0])
            } else {
                "Pair already created"
            }
        }

    }

    private fun isAccepted(event: Event, creator: UserInfo): Boolean {
        return event.participants
                .filter { it.user == creator }
                .single().accepted == EventStatus.ACCEPTED
    }

    fun userLink(user: UserInfo): String {
        return userMentionText(user)
    }

    fun userMentionText(user: UserInfo): String {
        val label = Optional.ofNullable(user.firstName).orElse("Noname")
        return String.format("[%s](tg://user?id=%s)", label, user.userId)
    }

    fun sendToAll(event: Event, textProvider: KFunction2<@ParameterName(name = "user") UserInfo, @ParameterName(name = "pair") Event, String>, keyboardProvider: KFunction1<@ParameterName(name = "event") Event, InlineKeyboardMarkup>) {
        val keyboard = keyboardProvider.invoke(event)

        for (p in event.participants) {
            val user = p.user
            val text = textProvider.invoke(user, event)
            try {
                val message = getMessageWithKeyboard(
                        chatService.getPrivateChatId(user),
                        text,
                        keyboard)
                user.lastMessageId = sendMessage(message)
                userRepository.save(user)
            } catch (e: TelegramApiException) {
                LOG.error("Sending failed: {}", e.toString(), e)
            }

        }
    }

    fun updateToAll(event: Event, textProvider: Function2<UserInfo, Event, String>, keyboardProvider: Function2<Participant, Event, InlineKeyboardMarkup>) {

        for (p in event.participants) {
            val user = p.user
            val text = textProvider.invoke(user, event)
            val keyboard = keyboardProvider.invoke(p, event)
            try {
                user.lastMessageId?.let {
                    editMessage(
                            chatService.getPrivateChatId(user),
                            it,
                            text,
                            keyboard)
                }
            } catch (e: TelegramApiException) {
                LOG.error("Sending failed: {}", e.toString(), e)
            }

        }
    }

    fun groupInfo(user: UserInfo): String {
        val group = if (user.groups.isEmpty()) null else user.groups.random()
        if (group != null) {
            val memberList = group.members
                    .sortedWith(nullsFirst(compareBy(UserInfo::firstName)))
                    .joinToString(separator = "\n") { this.userLine(it) }

            val groupInlineLink = inlineLink("group", groupLink(group))

            return "Your $groupInlineLink:\n$memberList"
        } else {
            return "You have no group"
        }
    }

    fun publicGroupsInfo(): String {
        val groups = groupRepository.findByPrivateFalse()
        val groupList = groupList(groups)

        return "Found public groups:\n$groupList\nFeel free to join! :)"
    }

    fun groupList(groups: Collection<Group>) = groups
            .joinToString(separator = "\n") { this.inlineLink(it.name ?: "noname group", groupLink(it)) }

    private fun userLine(user: UserInfo): String {
        return userLink(user)
    }

    fun getJoinTeamText(group: Group): String {
        val groupInlineLink = inlineLink("Right-click to copy link", groupLink(group))
        return ("""
            Your group created!
            $groupInlineLink
            
            After someone joins, use /lunch command
            Temporary name is ${group.token}
            """.trimIndent())
    }

    fun groupLink(group: Group): String {
        return botUrl + "?start=" + group.token
    }

    private fun inlineLink(label: String, link: String): String {
        return String.format("[%s](%s)", label, link)
    }

    @Throws(TelegramApiException::class)
    fun requestLocation(chatId: Long?) {
        val sendMessage = SendMessage(chatId!!, "Share location")
        sendMessage.replyMarkup = requestLocationKeyboard()

        val message = bot.execute(sendMessage)

        val location = message.location
        if (location != null) {
            LOG.info("Location acquired: lat {} , long {}", location.latitude, location.longitude)
        }

    }

    private fun requestLocationKeyboard(): ReplyKeyboard {
        val keyboardMarkup = ReplyKeyboardMarkup()
        keyboardMarkup.resizeKeyboard = true
        keyboardMarkup.oneTimeKeyboard = true
        keyboardMarkup.selective = true
        val keyboard = ArrayList<KeyboardRow>()
        val row = KeyboardRow()
        val button = KeyboardButton()
        button.text = "Share location"
        button.requestLocation = true
        row.add(button)
        keyboard.add(row)
        keyboardMarkup.keyboard = keyboard
        return keyboardMarkup
    }
}

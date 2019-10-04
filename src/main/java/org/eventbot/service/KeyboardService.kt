package org.eventbot.service

import com.google.common.base.Joiner
import org.eventbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR
import org.eventbot.constant.Callback
import org.eventbot.model.Event
import org.eventbot.model.EventStatus.DECLINED
import org.eventbot.model.EventStatus.NO_RESPONSE
import org.eventbot.model.Participant
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.ArrayList
import java.util.Arrays

@Component
class KeyboardService {
    val startKeyboard: InlineKeyboardMarkup
        get() = getOneRowKeyboard(button("New group", Callback.NEW_TEAM.toString()))

    val removeKeyboardMarkup: InlineKeyboardMarkup
        get() = getOneRowKeyboard()

    fun infoOptionsKeyboard(): InlineKeyboardMarkup {
        return getOneRowKeyboard(
                button("All groups", "GROUPS"),
                button("Groups created by me", "MY_GROUPS")
        )
    }

    fun getInviteKeyboard(event: Event): InlineKeyboardMarkup {
        return getOneRowKeyboard(
                button(
                        "Decline",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.ACCEPT_DECLINE.toString(), event.pk, java.lang.Boolean.FALSE)
                ),
                button(
                        "Accept",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.ACCEPT_DECLINE.toString(), event.pk, java.lang.Boolean.TRUE)
                ))
    }

    fun acceptedInviteKeyboard(participant: Participant, event: Event): InlineKeyboardMarkup {
        return if (participant.accepted != NO_RESPONSE || event.accepted == DECLINED) {
            removeKeyboardMarkup
        } else {
            getInviteKeyboard(event)
        }
    }

    fun getRemoveKeyboard(chatId: Long?, messageId: Int?): EditMessageReplyMarkup {
        return EditMessageReplyMarkup()
                .setChatId(chatId!!)
                .setMessageId(messageId)
                .setReplyMarkup(removeKeyboardMarkup)
    }

    private fun button(label: String, callbackData: String): InlineKeyboardButton {
        return callbackButton(label, callbackData)
    }

    private fun callbackButton(text: String, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton()
                .setText(text)
                .setCallbackData(callbackData)
    }

    private fun createLinkButton(text: String, url: String): InlineKeyboardButton {
        return InlineKeyboardButton()
                .setText(text)
                .setUrl(url)
    }

    private fun getOneRowKeyboard(vararg buttons: InlineKeyboardButton): InlineKeyboardMarkup {
        return getMultiRowKeyboard(ArrayList(listOf(row(*buttons))))
    }

    private fun getMultiRowKeyboard(rows: List<List<InlineKeyboardButton>>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().setKeyboard(rows)
    }

    private fun row(vararg buttons: InlineKeyboardButton): List<InlineKeyboardButton> {
        return ArrayList(Arrays.asList(*buttons))
    }

    fun removeCustomKeyboard(): ReplyKeyboard {
        return ReplyKeyboardRemove()
    }
}
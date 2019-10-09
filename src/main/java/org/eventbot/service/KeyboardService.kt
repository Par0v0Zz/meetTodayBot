package org.eventbot.service

import com.google.common.base.Joiner
import org.eventbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR
import org.eventbot.constant.Callback
import org.eventbot.model.Event
import org.eventbot.model.EventStatus.DECLINED
import org.eventbot.model.EventStatus.NO_RESPONSE
import org.eventbot.model.Group
import org.eventbot.model.Participant
import org.eventbot.model.UserInfo
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
        get() = getOneRowKeyboard(
                button(
                        "New public group",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.NEW_GROUP.toString(), java.lang.Boolean.FALSE)
                ),
                button(
                        "New private group",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.NEW_GROUP.toString(), java.lang.Boolean.TRUE)
                ))

    fun groupsAdminKeyboard(group: Group): InlineKeyboardMarkup {
        return getMultiRowKeyboard(listOf(
                listOf(
                        button("Group " + (group.name ?: group.token) + ": show participants",
                                Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.GROUP_INFO.toString(), group.name
                                        ?: group.token)
                        )
                ),
                listOf(
                        button("Invite for lunch!",
                                Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.LUNCH.toString(), group.pk)),
                        button("Leave group",
                                Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.LEAVE_GROUP.toString(), group.pk))
                )))
    }

    val removeKeyboardMarkup: InlineKeyboardMarkup
        get() = getOneRowKeyboard()

    fun infoGroupOptionsKeyboard(): InlineKeyboardMarkup {
        return getOneRowKeyboard(
                button("Groups I'm in", Callback.ALL_GROUPS.toString()),
                button("Groups I've created", Callback.MY_GROUPS.toString())
        )
    }

    fun infoEventOptionsKeyboard(): InlineKeyboardMarkup {
        getMultiRowKeyboard(listOf(
                listOf(
                        button("All my events", Callback.ALL_EVENTS.toString()),
                        button("Only accepted", Callback.ACCEPTED_EVENTS.toString())
                ),
                listOf(
                        button("New event", Callback.ALL_EVENTS.toString())
                )
        ))
        return getOneRowKeyboard(
                button("All my events", Callback.ALL_EVENTS.toString()),
                button("Only accepted", Callback.ACCEPTED_EVENTS.toString())
        )
    }

    fun groupActionsKeyboard(group: Group, userInfo: UserInfo): InlineKeyboardMarkup {

        val twoButtonsKeyboard = getOneRowKeyboard(
                button("Invite for lunch!",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.LUNCH.toString(), group.pk)),
                button("Leave group",
                        Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.LEAVE_GROUP.toString(), group.pk))
        )

        val threeButtonsKeyboard = getMultiRowKeyboard(listOf(
                listOf(
                        button("Invite for lunch!",
                                Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.LUNCH.toString(), group.pk)),
                        button("Leave group",
                                Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.LEAVE_GROUP.toString(), group.pk))
                ),
                listOf(
                        button("Add group description",
                                Joiner.on(CALLBACK_DATA_SEPARATOR).join(Callback.ADD_DESCRIPTION.toString(), group.pk)),
                        button("Rename group", Callback.RENAME_GROUP.toString())
                )))
        return if (group.creator == userInfo) threeButtonsKeyboard else twoButtonsKeyboard

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

    fun groupActionsKeyboard(): InlineKeyboardMarkup = getOneRowKeyboard(
            button("Name group", Callback.RENAME_GROUP.toString())
    )

}

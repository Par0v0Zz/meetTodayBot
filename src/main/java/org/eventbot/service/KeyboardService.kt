package org.eventbot.service

import com.google.common.base.Joiner
import org.eventbot.callback.CallbackParams
import org.eventbot.constant.BotConstants.CALLBACK_DATA_SEPARATOR
import org.eventbot.constant.Callback
import org.eventbot.constant.MenuItem
import org.eventbot.constant.MenuScreen
import org.eventbot.model.Event
import org.eventbot.model.EventStatus.DECLINED
import org.eventbot.model.EventStatus.NO_RESPONSE
import org.eventbot.model.Group
import org.eventbot.model.Participant
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.ArrayList
import java.util.Arrays

@Component
class KeyboardService(val userService: UserService, val groupRepository: GroupRepository) {
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

    fun getMenuKeyboardScreenOne(): InlineKeyboardMarkup {
        val context = HashMap<CallbackParams, Any>()

        context.put(CallbackParams.ARG, MenuScreen.S_0_MAIN.toString())
        context.put(CallbackParams.ARG2, MenuItem.GROUPS.toString())

        return getMenuKeyboard(MenuScreen.S_0_MAIN, context)
    }

    fun getMenuKeyboard(screen: MenuScreen, context: Map<CallbackParams, Any>): InlineKeyboardMarkup {

        val rows = ArrayList<List<InlineKeyboardButton>>()
        val menuItem = menuItem(context)
        val targetScreen = menuItem.getTargetScreen(screen)

        when (targetScreen) {
            MenuScreen.S_0_MAIN -> addMainButtons(rows)
            MenuScreen.S_1_GROUPS -> addGroupsButtons(rows)
            MenuScreen.S_2_GROUP_ADMIN -> addGroupAdminButtons(context, rows)
            MenuScreen.S_2_GROUPS_ALL -> addGroupListButtons(context, rows)
        }

        return getMultiRowKeyboard(rows)
    }

    private fun menuItem(context: Map<CallbackParams, Any>): MenuItem {
        return MenuItem.valueOf(context[CallbackParams.ARG2] as String)
    }

    private fun addMainButtons(rows: ArrayList<List<InlineKeyboardButton>>) {
        val currentScreen = MenuScreen.S_0_MAIN

        rows.add(
                row(
                        getMenuButton(currentScreen, MenuItem.GROUPS, "Groups")
                )
        )
    }

    private fun addGroupsButtons(rows: ArrayList<List<InlineKeyboardButton>>) {
        val currentScreen = MenuScreen.S_1_GROUPS
        rows.add(gobackButtonRow(currentScreen))

        rows.add(
                row(
                        getMenuButton(currentScreen, MenuItem.GROUPS_ALL, "All groups"),
                        getMenuButton(currentScreen, MenuItem.GROUPS_ADMIN, "My groups")
                )
        )
    }

    private fun addGroupAdminButtons(context: Map<CallbackParams, Any>, rows: ArrayList<List<InlineKeyboardButton>>) {
        val currentScreen = MenuScreen.S_2_GROUP_ADMIN
        rows.add(gobackButtonRow(currentScreen))

        val user = context[CallbackParams.USER_INFO] as UserInfo
        val groups = user.groups

        rows.addAll(groups.map { groupAdminRow(it) })
    }

    private fun groupAdminRow(group: Group): List<InlineKeyboardButton> {
        return listOf(
                button(group.name ?: group.token.toString(), Callback.VOID.toString()),
                button("Name group", Callback.RENAME_GROUP.toString())
        )
    }

    private fun addGroupListButtons(context: Map<CallbackParams, Any>, rows: ArrayList<List<InlineKeyboardButton>>) {
        val currentScreen = MenuScreen.S_2_GROUPS_ALL
        rows.add(gobackButtonRow(currentScreen))

        val user = context[CallbackParams.USER_INFO] as UserInfo
        val groups = groupRepository.findByCreator(user)
        rows.addAll(groups.map { groupListRow(it) })
    }

    private fun groupListRow(group: Group): List<InlineKeyboardButton> {
        return listOf(
                button(group.name ?: group.token.toString(), Callback.VOID.toString()),
                groupButton(group, "Invite for lunch!", MenuScreen.S_2_GROUPS_ALL, MenuItem.LUNCH),
                groupButton(group, "Leave group", MenuScreen.S_2_GROUPS_ALL, MenuItem.LEAVE_GROUP)
        )
    }

    private fun groupButton(group: Group, label: String, screen: MenuScreen, menuItem: MenuItem): InlineKeyboardButton {
        return callbackButton(label, callbackData(Callback.SETTING, screen, menuItem, group.pk))
    }

    private fun gobackButtonRow(screen: MenuScreen): List<InlineKeyboardButton> {
        return row(getMenuButton(screen, MenuItem.BACK, "<- back"))
    }

    private fun getMenuButton(screen: MenuScreen, menuItem: MenuItem, label: String): InlineKeyboardButton {
        return callbackButton(label,
                callbackData(Callback.SETTING, screen, menuItem))
    }

    private fun callbackData(vararg parts: Any): String {
        return Joiner.on(CALLBACK_DATA_SEPARATOR).join(parts)
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

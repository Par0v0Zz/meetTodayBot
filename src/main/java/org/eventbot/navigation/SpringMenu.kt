package org.eventbot.navigation

import com.google.common.base.Joiner
import org.eventbot.callback.CallbackParams
import org.eventbot.constant.BotConstants
import org.eventbot.constant.Callback
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.service.KeyboardService
import org.slf4j.LoggerFactory
import org.springframework.statemachine.StateMachine
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.*
import kotlin.collections.HashMap


@Component
class SpringMenu(
        val keyboardService: KeyboardService,
        val groupRepository: GroupRepository,
        val stateMachine: StateMachine<SpringState, SpringEvent>) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val chatIdToMenu = HashMap<Long, StateMachine<SpringState, SpringEvent>>()

    private fun newMenu(userId: Long): StateMachine<SpringState, SpringEvent> {
        //todo: create new stateMachine using StateMachineFactory here
        stateMachine.start()
        chatIdToMenu.put(userId, stateMachine)
        return stateMachine
    }

    fun getMenuKeyboardScreenOne(chatId: Long, user: UserInfo): InlineKeyboardMarkup {
        val context = HashMap<CallbackParams, Any>()

        context.put(CallbackParams.CHAT_ID, chatId)
        context.put(CallbackParams.USER_INFO, user)
        context.put(CallbackParams.ARG, State.S_0_MAIN.javaClass.simpleName)
        context.put(CallbackParams.ARG2, MEvent.OnGroups.javaClass.simpleName)

        return getMenuKeyboard(context)
    }

    fun getMenuKeyboard(context: Map<CallbackParams, Any>): InlineKeyboardMarkup {
        val rows = ArrayList<List<InlineKeyboardButton>>()
        val menuItem = menuItem(context)
        val event: SpringEvent? = SpringEvent.valueOf(menuItem)

        event?.let {
            val chatId = context[CallbackParams.CHAT_ID] as Long
            val stateMachine = getMenuForChatId(chatId)
            stateMachine.sendEvent(it)
            val state = stateMachine.state
            addButtons(state, rows, context)
        }

        return keyboardService.getMultiRowKeyboard(rows)
    }

    private fun addButtons(state: org.springframework.statemachine.state.State<SpringState, SpringEvent>, rows: ArrayList<List<InlineKeyboardButton>>, context: Map<CallbackParams, Any>) {
        //todo: add buttons for all states
        when (state.id) {
            SpringState.S_0_MAIN ->
                rows.add(row(button(state.id, SpringEvent.OnGroups, "Groups")))
            SpringState.S_1_GROUPS -> TODO()
            SpringState.S_2_GROUP_ADMIN -> TODO()
            SpringState.S_2_GROUPS_ALL -> TODO()
            SpringState.S_3_GROUP_INFO -> TODO()
        }
    }

    private fun getMenuForChatId(userId: Long): StateMachine<SpringState, SpringEvent> {
        val existingMenu = chatIdToMenu[userId]
        return existingMenu ?: newMenu(userId)
    }

    private fun menuItem(context: Map<CallbackParams, Any>): String {
        return context[CallbackParams.ARG2] as String
    }

    fun row(vararg buttons: InlineKeyboardButton): List<InlineKeyboardButton> {
        return ArrayList(Arrays.asList(*buttons))
    }

    fun button(currentState: SpringState, menuItem: SpringEvent, label: String): InlineKeyboardButton {
        return callbackButton(label,
                callbackData(Callback.SETTING, currentState.toString(), menuItem.toString()))
    }

    fun callbackButton(text: String, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton()
                .setText(text)
                .setCallbackData(callbackData)
    }

    fun callbackData(vararg parts: Any): String {
        return Joiner.on(BotConstants.CALLBACK_DATA_SEPARATOR).join(parts)
    }
}


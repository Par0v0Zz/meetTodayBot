package org.eventbot.navigation

import com.google.common.base.Joiner
import com.tinder.StateMachine
import org.eventbot.callback.CallbackParams
import org.eventbot.constant.BotConstants
import org.eventbot.constant.Callback
import org.eventbot.model.Group
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.service.KeyboardService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.*
import kotlin.collections.HashMap

@Component
class Menu(val keyboardService: KeyboardService,
           val groupRepository: GroupRepository) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val chatIdToMenu = HashMap<Long, StateMachine<State, MEvent, SideEffect>>()

    init {
        State.S_2_GROUPS_ALL.groupRepository = groupRepository
    }

    private fun newMenu(userId: Long): StateMachine<State, MEvent, SideEffect> {
        val newMenu = StateMachine.create<State, MEvent, SideEffect> {
            initialState(State.S_0_MAIN)
            state<State.S_0_MAIN> {
                on<MEvent.OnGroups> {
                    transitionTo(State.S_1_GROUPS, SideEffect.Log)
                }
            }
            state<State.S_1_GROUPS> {
                on<MEvent.OnBack> {
                    transitionTo(State.S_0_MAIN, SideEffect.Log)
                }
                on<MEvent.OnAllGroups> {
                    transitionTo(State.S_2_GROUPS_ALL, SideEffect.Log)
                }
                on<MEvent.OnMyGroups> {
                    transitionTo(State.S_2_GROUP_ADMIN, SideEffect.Log)
                }
            }
            state<State.S_2_GROUPS_ALL> {
                on<MEvent.OnBack> {
                    transitionTo(State.S_1_GROUPS, SideEffect.Log)
                }
                on<MEvent.OnGroupLunch> {
                    transitionTo(State.S_2_GROUPS_ALL, SideEffect.Log)
                }
                on<MEvent.OnGroupInfo> {
                    transitionTo(State.S_3_GROUP_INFO, SideEffect.Log)
                }
                on<MEvent.OnGroupLeave> {
                    transitionTo(State.S_2_GROUPS_ALL, SideEffect.Log)
                }
            }
            state<State.S_2_GROUP_ADMIN> {
                on<MEvent.OnBack> {
                    transitionTo(State.S_1_GROUPS, SideEffect.Log)
                }
                on<MEvent.OnGroupRename> {
                    transitionTo(State.S_2_GROUP_ADMIN)
                }
            }
            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
                when (validTransition.sideEffect) {
                    SideEffect.Log -> log.info(validTransition.toString())
                }
            }
        }
        chatIdToMenu.put(userId, newMenu)
        return newMenu
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
        val event: MEvent? = MEvent.OnGroups.valueOf(menuItem)

        event?.let {
            val chatId = context[CallbackParams.CHAT_ID] as Long
            val stateMachine = getMenuForChatId(chatId)
            val transition = stateMachine.transition(it)
            val fromState = transition.fromState
            val state = stateMachine.state
            state.addButtons(rows, context)
        }

        return keyboardService.getMultiRowKeyboard(rows)
    }

    private fun getMenuForChatId(userId: Long): StateMachine<State, MEvent, SideEffect> {
        val existingMenu = chatIdToMenu[userId]
        return existingMenu ?: newMenu(userId)
    }

    private fun menuItem(context: Map<CallbackParams, Any>): String {
        return context[CallbackParams.ARG2] as String
    }
}

sealed class State {
    object S_0_MAIN : State() {
        override fun addButtons(rows: ArrayList<List<InlineKeyboardButton>>, context: Map<CallbackParams, Any>) {
            rows.add(row(button(this, MEvent.OnGroups, "Groups")))
        }
    }

    object S_1_GROUPS : State() {
        override fun addButtons(rows: ArrayList<List<InlineKeyboardButton>>, context: Map<CallbackParams, Any>) {
            rows.add(row(button(this, MEvent.OnBack, "<- back")))
            rows.add(row(
                    button(this, MEvent.OnAllGroups, "All groups"),
                    button(this, MEvent.OnMyGroups, "My groups")
            ))
        }

    }

    object S_2_GROUP_ADMIN : State() {
        override fun addButtons(rows: ArrayList<List<InlineKeyboardButton>>, context: Map<CallbackParams, Any>) {
            rows.add(row(button(this, MEvent.OnBack, "<- back")))

            val user = context[CallbackParams.USER_INFO] as UserInfo
            val groups = user.groups
            rows.addAll(groups.map { groupAdminRow(it) })
        }

        fun groupAdminRow(group: Group): List<InlineKeyboardButton> {
            return listOf(
                    callbackButton(group.name ?: group.token.toString(), Callback.VOID.toString()),
                    callbackButton("Rename group", Callback.RENAME_GROUP.toString())
            )
        }
    }

    object S_2_GROUPS_ALL : State() {
        lateinit var groupRepository: GroupRepository

        //TODO: add buttons
        override fun addButtons(rows: ArrayList<List<InlineKeyboardButton>>, context: Map<CallbackParams, Any>) {
            rows.add(S_2_GROUP_ADMIN.row(S_2_GROUP_ADMIN.button(this, MEvent.OnBack, "<- back")))

            val user = context[CallbackParams.USER_INFO] as UserInfo
            val groups = groupRepository.findByCreator(user)
            rows.addAll(groups.map { groupListRow(it) })
        }

        private fun groupListRow(group: Group): List<InlineKeyboardButton> {
            return listOf(
                    callbackButton(group.name ?: group.token.toString(), Callback.VOID.toString()),
                    callbackButton("Invite for lunch!", getGroupCallbackData(S_2_GROUPS_ALL, MEvent.OnGroupLunch, group)),
                    callbackButton("Leave group", getGroupCallbackData(S_2_GROUPS_ALL, MEvent.OnGroupLeave, group))
            )
        }

        fun getGroupCallbackData(state: State, event: MEvent, group: Group): String {
            return callbackData(Callback.SETTING, state.javaClass.simpleName, event.javaClass.simpleName, group.pk)
        }
    }

    object S_3_GROUP_INFO : State() {
        //TODO: add buttons
    }

    open fun addButtons(rows: ArrayList<List<InlineKeyboardButton>>, context: Map<CallbackParams, Any>) {}

    fun row(vararg buttons: InlineKeyboardButton): List<InlineKeyboardButton> {
        return ArrayList(Arrays.asList(*buttons))
    }

    fun button(currentState: State, menuItem: MEvent, label: String): InlineKeyboardButton {
        return callbackButton(label,
                callbackData(Callback.SETTING, currentState.javaClass.simpleName, menuItem.javaClass.simpleName))
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

sealed class MEvent {
    object OnGroups : MEvent()
    object OnAllGroups : MEvent()
    object OnMyGroups : MEvent()
    object OnGroupRename : MEvent()
    object OnGroupLunch : MEvent()
    object OnGroupInfo : MEvent()
    object OnGroupLeave : MEvent()
    object OnBack : MEvent()

    fun valueOf(id: String): MEvent? {
        val find = MEvent::class.sealedSubclasses.find { it.simpleName.equals(id) }

        return find?.objectInstance
    }

}

sealed class SideEffect {
    object Log : SideEffect()
}
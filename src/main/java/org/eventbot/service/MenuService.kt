package org.eventbot.service

import org.eventbot.callback.CallbackParams
import org.eventbot.constant.MenuScreen
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class MenuService(
        val keyboardService: KeyboardService,
        val messageService: MessageService,
        val chatService: ChatService,
        val userService: UserService) {

    @Throws(TelegramApiException::class)
    fun updateMenu(userId: Int, targetScreen: MenuScreen, context: Map<CallbackParams, Any>) {
        val text: String = "Main screen"
        val keyboard: InlineKeyboardMarkup = keyboardService.getMenuKeyboard(targetScreen, context)

        messageService.sendMessage(
                chatService.getPrivateChatId(userService.getExistingUser(userId)),
                text,
                keyboard)
    }
}
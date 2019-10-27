package org.eventbot.service

import org.eventbot.callback.CallbackParams
import org.eventbot.navigation.Menu
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class MenuService(
        val messageService: MessageService,
        val chatService: ChatService,
        val userService: UserService,
        val menu: Menu) {

    @Throws(TelegramApiException::class)
    fun updateMenu(userId: Int, context: Map<CallbackParams, Any>) {
        val text = "Main screen"
        val keyboard: InlineKeyboardMarkup = menu.getMenuKeyboard(context)

        messageService.sendMessage(
                chatService.getPrivateChatId(userService.getExistingUser(userId)),
                text,
                keyboard)
    }
}
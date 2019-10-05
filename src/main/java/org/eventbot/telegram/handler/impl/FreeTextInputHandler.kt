package org.eventbot.telegram.handler.impl

import org.eventbot.service.FreeTextInputService
import org.eventbot.telegram.handler.ChatUpdateHandler
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update


@Component
class FreeTextInputHandler(val freeTextInputService: FreeTextInputService) : ChatUpdateHandler {

    override fun handle(update: Update): Any {
        freeTextInputService.processKeyboardCallback(update.message)
        return ""
    }

}
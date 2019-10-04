package org.eventbot.telegram.handler

import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

/**
 * Chain of handlers is to be executed when bot gets telegram chat update.
 */
interface ChatUpdateHandler {
    @Throws(TelegramApiException::class)
    fun handle(update: Update): Any
}

package org.eventbot.telegram.handler.impl


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.eventbot.service.CallbackService
import org.eventbot.telegram.handler.ChatUpdateHandler

import org.eventbot.constant.UpdateHandlerResult.HANDLING_FINISHED_OK
import org.eventbot.constant.UpdateHandlerResult.STOP_HANDLING

@Component
class CallbackHandler : ChatUpdateHandler {
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private val callbackService: CallbackService? = null

    @Throws(TelegramApiException::class)
    override fun handle(update: Update): Any {
        try {
            callbackService!!.processKeyboardCallback(update.callbackQuery)
        } catch (e: TelegramApiException) {
            LOG.error("Cannot process keyboard callback!\n{}", e.toString(), e)
            return STOP_HANDLING
        }

        return HANDLING_FINISHED_OK
    }
}

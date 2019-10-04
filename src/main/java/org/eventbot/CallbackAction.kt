package org.eventbot

import org.eventbot.callback.CallbackParams

interface CallbackAction {

    fun doAction(context: Map<CallbackParams, Any>): String?

}
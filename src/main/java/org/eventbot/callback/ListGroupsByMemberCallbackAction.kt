package org.eventbot.callback

import org.eventbot.CallbackAction
import org.springframework.stereotype.Component

@Component
class ListGroupsByMemberCallbackAction(): CallbackAction {

    override fun doAction(context: Map<CallbackParams, Any>): String? {
        return "not implemented, yet!"
    }

}

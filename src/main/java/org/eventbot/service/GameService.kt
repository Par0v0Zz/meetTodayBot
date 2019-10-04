package org.eventbot.service

import org.springframework.stereotype.Component
import org.eventbot.model.UserInfo

@Component
class GameService {

    fun xpShort(user: UserInfo): String{
        return String.format("%dxp", user.xp)
    }
}

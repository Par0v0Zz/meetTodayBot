package org.eventbot.service

import org.springframework.stereotype.Component
import org.eventbot.model.UserInfo

@Component
class ChatService {
    fun getPrivateChatId(user: UserInfo): Long {
        return user.userId.toLong()
    }
}

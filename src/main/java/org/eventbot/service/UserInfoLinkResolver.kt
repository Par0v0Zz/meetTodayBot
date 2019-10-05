package org.eventbot.service

import org.eventbot.model.UserInfo
import org.springframework.stereotype.Component


@Component
class UserInfoLinkResolver() : LinkResolver<UserInfo> {

    override fun resolve(arg: UserInfo): String {
        var label = arg.firstName.trim()
        if (arg.lastName != null) {
            label += " " + arg.lastName!!.trim()
        }
        return "[$label](tg://user?id=${arg.userId})"
    }

}

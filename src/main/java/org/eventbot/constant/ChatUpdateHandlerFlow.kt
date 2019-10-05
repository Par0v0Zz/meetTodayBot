package org.eventbot.constant

enum class ChatUpdateHandlerFlow {
    CALLBACK,
    MEMBER_REMOVED,
    COMMAND,
    CHAT,
    SET_LOCATION,
    VOID,
    FREE_TEXT_INPUT
}
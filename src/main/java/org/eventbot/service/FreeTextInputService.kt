package org.eventbot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.concurrent.ConcurrentHashMap
import javax.persistence.EntityManager
import javax.transaction.Transactional


@Component
open class FreeTextInputService() {

    @Autowired
    open var entityManager: EntityManager? = null
    private val freeTextContextObject = ConcurrentHashMap<Long, Any>()
    private val freeTextContextAction = ConcurrentHashMap<Long, (Any, String) -> Any>()

    @Transactional
    open fun processKeyboardCallback(message: Message) {
        val key = message.chatId
        val text = message.text.replace(">", "").trim()
        freeTextContextAction[key]?.invoke(freeTextContextObject[key]!!, text)
    }

    open fun registerFreeTextContextAction(key: Long, context: (Any, String) -> Any) {
        freeTextContextAction[key] = context
    }

    open fun registerFreeTextContextObject(key: Long, ob: Any) {
        freeTextContextObject[key] = ob
    }

}

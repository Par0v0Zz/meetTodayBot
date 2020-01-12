package org.eventbot.navigation

import com.nhaarman.mockito_kotlin.mock
import org.eventbot.model.UserInfo
import org.eventbot.repository.GroupRepository
import org.eventbot.service.KeyboardService
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class MenuTest {
    private val keyboardService: KeyboardService = KeyboardService(mock(), mock())
    private val groupRepository: GroupRepository = mock()

    private val log = LoggerFactory.getLogger(javaClass)

    val menu: Menu = Menu(keyboardService, groupRepository)

    @Test
    fun testInitMenu() {
        val result = menu.getMenuKeyboardScreenOne(1L, UserInfo(0, "Vasya"))
        assert(result.keyboard.size == 2)
    }
}
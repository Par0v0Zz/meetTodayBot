package org.eventbot.navigation

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class MenuTest {
    private val log = LoggerFactory.getLogger(javaClass)

    val menu: Menu = Menu()

    @Test
    fun testInitMenuFromFile() {
        log.info("Menu: ${menu.graph}")
    }
}
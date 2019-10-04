package org.eventbot

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.telegram.telegrambots.starter.TelegramBotInitializer
import org.eventbot.telegram.PairBot

@TestConfiguration
class TestConfig {
    @MockBean
    var pairBot: PairBot? = null

    @MockBean
    var telegramBotInitializer: TelegramBotInitializer? = null

}
package org.eventbot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.telegram.telegrambots.ApiContextInitializer

fun main(args: Array<String>) {
    ApiContextInitializer.init()

    SpringApplication.run(Application::class.java, *args)
}

@SpringBootApplication
open class Application {



}

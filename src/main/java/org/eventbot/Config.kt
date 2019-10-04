package org.eventbot

import freemarker.template.TemplateExceptionHandler
import freemarker.template.Version
import no.api.freemarker.java8.Java8ObjectWrapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

import java.util.Locale

@Configuration
@EnableScheduling
open class Config {
    @Bean(name = ["freemarkerConfig"])
    open fun freemarkerConfig(): freemarker.template.Configuration {
        val cfg = freemarker.template.Configuration(Version(2, 3, 23))

        cfg.objectWrapper = Java8ObjectWrapper(freemarker.template.Configuration.VERSION_2_3_23)

        // Where do we load the templates from:
        cfg.setClassForTemplateLoading(this.javaClass, "/templates/")

        // Some other recommended settings:
        cfg.defaultEncoding = "UTF-8"
        cfg.locale = Locale.US
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER

        return cfg
    }
}

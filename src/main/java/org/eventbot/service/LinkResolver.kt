package org.eventbot.service

interface LinkResolver<in T> {

    fun resolve(arg: T): String

}

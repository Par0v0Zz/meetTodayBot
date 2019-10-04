package org.eventbot.model

class Person(var id: Int, var name: String?, var master: Boolean) {
    var active = true
    var isProjectHolder: Boolean = false

    constructor(id: Int, name: String, master: Boolean, active: Boolean) : this(id, name, master) {
        this.active = active
    }
}

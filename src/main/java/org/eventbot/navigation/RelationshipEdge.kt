package org.eventbot.navigation

import org.jgrapht.graph.DefaultEdge


class RelationshipEdge(val label: String) : DefaultEdge() {

    override fun toString(): String {
        return "($source : $target : $label)"
    }
}
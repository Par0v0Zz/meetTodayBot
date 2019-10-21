package org.eventbot.navigation

import org.jgrapht.io.GraphImporter
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedMultigraph
import org.jgrapht.io.DOTImporter
import org.springframework.core.io.ClassPathResource


class Menu {

    private val menuFile: String = "menu.puml"
    var graph: DirectedMultigraph<String, DefaultEdge>

    init{
        val file = ClassPathResource(menuFile).file
        graph = DirectedMultigraph(RelationshipEdge::class.java)
        buildGraphIDImporter().importGraph(graph, file)


    }

    private fun buildGraphIDImporter(): GraphImporter<String, DefaultEdge> {
        return DOTImporter(
                { label, attributes -> label }, { from, to, label, attributes -> DefaultEdge() }, null)
    }


}

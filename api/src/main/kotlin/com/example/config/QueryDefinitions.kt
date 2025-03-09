package com.example.config

import com.example.model.TableQuery
import com.example.model.JoinTableQuery
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory

data class QueryDefinition(
    val mainTable: TableQuery,
    val relationName: String,
    val joins: List<JoinTableQuery>
)

class QueryDefinitions(configPath: String = "/query-definitions.yaml") {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val definitions: Map<String, QueryDefinition>

    init {
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val resource = this::class.java.getResource(configPath)
        definitions = resource?.let {
            val tree = mapper.readTree(it)
            tree.get("queries").fields().asSequence().associate { (key, value) ->
                key to mapper.treeToValue(value, QueryDefinition::class.java)
            }
        } ?: emptyMap()
        
        logger.info("Loaded ${definitions.size} query definitions")
    }

    fun getDefinition(name: String): QueryDefinition? = definitions[name]
} 
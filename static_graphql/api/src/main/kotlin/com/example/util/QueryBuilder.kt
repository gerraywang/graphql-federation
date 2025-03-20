package com.example.util

import com.example.model.*
import com.example.config.AppConfig

object QueryBuilder {
    private lateinit var appConfig: AppConfig

    fun initialize(config: AppConfig) {
        appConfig = config
    }

    fun buildQuery(config: com.example.model.QueryConfig): String {
        return """
            query {
                ${config.queries.joinToString("\n")}
            }
        """.trimIndent()
    }

    fun buildTableQuery(table: TableQuery, queryType: String, alias: String? = null): String {
        val fields = table.fields.joinToString("\n")
        val pagination = buildPaginationString(table.conditions)
        
        // 从配置中获取查询名称
        val queryName = appConfig.getQueryName(table.tableName)

        return """
            ${queryName}${pagination} {
                id
                $fields
            }
        """.trimIndent()
    }

    private fun buildConditions(conditions: List<Condition>): String {
        if (conditions.isEmpty()) return ""
        
        return conditions.joinToString(",\n") { condition ->
            """
            ${condition.field}_${condition.operator.name.lowercase()}: "${condition.value}"
            """.trimIndent()
        }
    }

    private fun buildPaginationString(conditions: List<Condition>?): String {
        if (conditions == null) return ""
        
        val offset = conditions.find { it.field == "offset" }?.value?.toIntOrNull() ?: 0
        val limit = conditions.find { it.field == "limit" }?.value?.toIntOrNull() ?: 10
        return "(pagination: {offset: $offset, limit: $limit})"
    }

    private fun buildSorting(sorting: List<SortField>): String {
        if (sorting.isEmpty()) return ""
        return ", sorting: ${sorting.map { 
            "{field: \"${it.field}\", direction: ${it.direction}}"
        }}"
    }

    private fun buildTransforms(transforms: List<Transform>): String {
        if (transforms.isEmpty()) return ""
        return ", transforms: ${transforms.map { 
            "{field: \"${it.field}\", type: ${it.type}${it.format?.let { ", format: \"$it\"" } ?: ""}}"
        }}"
    }

    private fun formatValue(value: Any?): String {
        return when (value) {
            is String -> "\"$value\""
            is Number -> value.toString()
            is Boolean -> value.toString()
            null -> "null"
            else -> "\"$value\""
        }
    }
} 
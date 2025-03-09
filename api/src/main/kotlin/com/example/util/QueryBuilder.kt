package com.example.util

import com.example.model.*
import java.util.Locale

object QueryBuilder {
    fun buildQuery(config: com.example.model.QueryConfig): String {
        return """
            query {
                ${config.queries.joinToString("\n")}
            }
        """.trimIndent()
    }

    fun buildTableQuery(
        table: TableQuery,
        queryName: String,
        alias: String? = null
    ): String {
        val conditions = table.conditions?.let { buildConditions(it) } ?: ""
        
        return """
            ${alias ?: table.alias}: $queryName(
                args: {
                    tableName: "${table.tableName}",
                    fields: ${table.fields.map { "\"$it\"" }},
                    $conditions
                }
            ) {
                id
                fieldName
                fieldValue
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

    private fun buildPagination(pagination: PaginationInput): String {
        return ", pagination: {offset: ${pagination.offset}, limit: ${pagination.limit}}"
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
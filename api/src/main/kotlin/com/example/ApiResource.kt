package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import com.example.config.TableRelations
import com.example.config.JoinTable
import com.example.model.*  // 导入共享模型类
import java.util.Locale  // 添加这行

class ApiResource(
    private val graphQLService: GraphQLService,
    private val tableRelations: TableRelations
) {
    fun Route.apiRoutes() {
        // 1. 多表查询
        post("/query/tables") {
            val config = call.receive<MultiTableConfig>()
            val queries = config.tables.map { table ->
                val queryName = if (table.tableName in listOf("customer", "product")) {
                    "queryMstTable"
                } else {
                    "queryTranTable"
                }
                buildTableQuery(table, queryName)
            }.joinToString("\n")
            
            val query = """
                query {
                    $queries
                }
            """.trimIndent()
            
            val result = graphQLService.executeQuery(query)
            call.respond(result)
        }

        // 2. 关联查询
        post("/query/join") {
            val config = call.receive<JoinConfig>()
            val relation = tableRelations.getRelation(config.relationName) 
                ?: throw IllegalArgumentException("Unknown relation: ${config.relationName}")
            
            // 构建主表查询
            val mainQueryName = if (relation.mainTable in listOf("customer", "product")) {
                "queryMstTable"
            } else {
                "queryTranTable"
            }
            val mainQuery = buildTableQuery(config.mainTable, mainQueryName, "main")
            
            // 根据配置构建join查询
            val joinQueries = relation.joinTables.map { joinTable ->
                val joinConfig = config.joins?.find { it.tableName == joinTable.table }
                if (joinConfig != null) {
                    buildJoinQuery(joinConfig, joinTable)
                } else {
                    buildDefaultJoinQuery(joinTable)
                }
            }.joinToString("\n")
            
            val query = """
                query {
                    $mainQuery
                    $joinQueries
                }
            """.trimIndent()
            
            val result = graphQLService.executeQuery(query)
            call.respond(result)
        }

        // 3. 获取表的可用字段
        get("/fields/{tableName}") {
            val tableName = call.parameters["tableName"]!!
            val queryName = if (tableName in listOf("customer", "product")) {
                "getMstAvailableFields"
            } else {
                "getTranAvailableFields"
            }
            
            val query = """
                query {
                  $queryName(tableName: "$tableName")
                }
            """.trimIndent()
            
            val result = graphQLService.executeQuery(query)
            call.respond(result)
        }
    }

    private fun buildTableQuery(table: TableQuery, queryName: String, alias: String? = null): String {
        return """
            ${alias ?: table.alias}: $queryName(
                args: {
                    tableName: "${table.tableName}",
                    fields: ${table.fields.map { "\"$it\"" }},
                    ${table.conditions?.let { buildConditions(it) } ?: ""}
                    ${table.pagination?.let { buildPagination(it) } ?: ""}
                    ${table.sorting?.let { buildSorting(it) } ?: ""}
                    ${table.transforms?.let { buildTransforms(it) } ?: ""}
                }
            ) {
                id
                fieldName
                fieldValue
            }
        """.trimIndent()
    }

    private fun buildConditions(conditions: List<com.example.model.Condition>): String {
        if (conditions.isEmpty()) return ""
        return "conditions: [${conditions.map { 
            "{field: \"${it.field}\", operator: ${it.operator.toString().uppercase(Locale.getDefault())}, value: ${formatValue(it.value)}}"
        }.joinToString(",")}]"
    }

    private fun buildPagination(pagination: com.example.model.PaginationInput): String {
        return ", pagination: {offset: ${pagination.offset}, limit: ${pagination.limit}}"
    }

    private fun buildSorting(sorting: List<com.example.model.SortField>): String {
        if (sorting.isEmpty()) return ""
        return ", sorting: ${sorting.map { 
            "{field: \"${it.field}\", direction: ${it.direction}}"
        }}"
    }

    private fun buildTransforms(transforms: List<com.example.model.Transform>): String {
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

    private fun buildJoinQuery(config: JoinTableQuery, relation: com.example.config.JoinTable): String {
        val queryName = if (config.tableName in listOf("customer", "product")) {
            "queryMstTable"
        } else {
            "queryTranTable"
        }
        val tableQuery = config.toTableQuery().copy(
            alias = relation.table
        )
        return buildTableQuery(tableQuery, queryName)
    }

    private fun buildDefaultJoinQuery(relation: com.example.config.JoinTable): String {
        val queryName = if (relation.table in listOf("customer", "product")) {
            "queryMstTable"
        } else {
            "queryTranTable"
        }
        return buildTableQuery(
            TableQuery(
                tableName = relation.table,
                fields = listOf("*"),  // 默认查询所有字段
                alias = relation.table
            ),
            queryName
        )
    }
}

// 查询配置类
data class MultiTableConfig(
    val tables: List<TableQuery>
)

data class TableQuery(
    val tableName: String,
    val fields: List<String>,
    val alias: String,
    val conditions: List<com.example.model.Condition>? = null,
    val pagination: com.example.model.PaginationInput? = null,
    val sorting: List<com.example.model.SortField>? = null,
    val transforms: List<com.example.model.Transform>? = null
)

// 关联查询配置
data class JoinConfig(
    val mainTable: TableQuery,
    val relationName: String,  // 引用配置中定义的关系名称
    val joins: List<JoinTableQuery>? = null  // 可选的额外join配置
)

data class JoinTableQuery(
    val tableName: String,
    val fields: List<String>,
    val alias: String,
    val conditions: List<com.example.model.Condition>? = null,
    val pagination: com.example.model.PaginationInput? = null,
    val sorting: List<com.example.model.SortField>? = null,
    val transforms: List<com.example.model.Transform>? = null
) {
    fun toTableQuery() = TableQuery(
        tableName = tableName,
        fields = fields,
        alias = alias,
        conditions = conditions,
        pagination = pagination,
        sorting = sorting,
        transforms = transforms
    )
} 
package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import com.example.config.TableRelations
import com.example.config.JoinTable
import com.example.model.*  // 导入所有模型类
import java.util.Locale
import com.example.config.QueryDefinitions

// 在 model 包中创建这些配置类
data class MultiTableConfig(
    val tables: List<com.example.model.TableQuery>
)

data class JoinConfig(
    val mainTable: com.example.model.TableQuery,
    val relationName: String,
    val joins: List<com.example.model.JoinTableQuery>? = null
)

class ApiResource(
    private val graphQLService: GraphQLService,
    private val tableRelations: TableRelations,
    private val queryDefinitions: QueryDefinitions
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

        // 添加新的REST端点
        get("/api/{queryName}") {
            val queryName = call.parameters["queryName"]!!
            val definition = queryDefinitions.getDefinition(queryName)
                ?: throw IllegalArgumentException("Unknown query: $queryName")
            
            // 从请求参数获取条件
            val conditions = call.request.queryParameters.entries().mapNotNull { entry ->
                val (field, operator) = entry.key.split("_", limit = 2)
                Condition(
                    field = field,
                    operator = OperatorType.valueOf(operator.uppercase()),
                    value = entry.value.firstOrNull()
                )
            }
            
            // 应用条件到查询定义
            val joinConfig = JoinConfig(
                mainTable = definition.mainTable,
                relationName = definition.relationName,
                joins = definition.joins.map { join ->
                    join.copy(
                        conditions = conditions.toList()  // 转换为非空列表
                    )
                }
            )
            
            // 执行查询
            val relation = tableRelations.getRelation(joinConfig.relationName)
                ?: throw IllegalArgumentException("Unknown relation: ${joinConfig.relationName}")
            
            val mainQuery = buildTableQuery(joinConfig.mainTable, getQueryName(joinConfig.mainTable.tableName), "main")
            val joinQueries = relation.joinTables.map { joinTable ->
                val joinConfig = joinConfig.joins?.find { it.tableName == joinTable.table }
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
    }

    private fun buildTableQuery(table: com.example.model.TableQuery, queryName: String, alias: String? = null): String {
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

    private fun buildJoinQuery(config: com.example.model.JoinTableQuery, relation: com.example.config.JoinTable): String {
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
        return buildTableQuery(
            com.example.model.TableQuery(
                tableName = relation.table,
                fields = listOf("*"),
                alias = relation.table
            ),
            getQueryName(relation.table)
        )
    }

    private fun getQueryName(tableName: String): String {
        return if (tableName in listOf("customer", "product")) {
            "queryMstTable"
        } else {
            "queryTranTable"
        }
    }
} 
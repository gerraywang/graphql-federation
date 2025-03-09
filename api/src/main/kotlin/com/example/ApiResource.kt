package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*

class ApiResource(private val graphQLService: GraphQLService) {
    fun Route.apiRoutes() {
        // 1. 查询单个表的数据
        post("/query/table") {
            val config = call.receive<TableConfig>()
            val queryName = if (config.tableName in listOf("customer", "product")) {
                "queryMstTable"
            } else {
                "queryTranTable"
            }
            
            val query = """
                query {
                  $queryName(tableName: "${config.tableName}", fields: ${config.fields.map { "\"$it\"" }}) {
                    id
                    fieldName
                    fieldValue
                  }
                }
            """.trimIndent()
            
            val result = graphQLService.executeQuery(query)
            call.respond(result)
        }

        // 2. 查询客户及其订单
        get("/query/customer-orders/{customerId}") {
            val customerId = call.parameters["customerId"] ?: "1"
            val query = """
                query {
                  customer: queryMstTable(tableName: "customer", fields: ["name", "age"]) {
                    id
                    fieldName
                    fieldValue
                  }
                  orders: queryTranTable(tableName: "order", fields: ["orderNo", "amount", "status"]) {
                    id
                    fieldName
                    fieldValue
                  }
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
}

data class TableConfig(
    val tableName: String,
    val fields: List<String>
)

data class QueryRequest(
    val tableName: String,
    val fields: List<String>
)

data class JoinConfig(
    val sourceField: String,
    val targetTable: String,
    val targetField: String,
    val fields: List<String>
) 
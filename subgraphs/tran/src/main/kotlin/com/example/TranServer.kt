package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import com.apurebase.kgraphql.GraphQL
import com.example.model.DynamicEntity
import com.example.model.DynamicResult

// Transaction数据源模拟
object TranDataSource {
    private val tables = mutableMapOf<String, List<DynamicEntity>>()

    init {
        // 模拟交易数据
        tables["order"] = listOf(
            DynamicEntity("1", mapOf(
                "orderNo" to "ORD001",
                "amount" to 1000.00,
                "customerId" to "1",
                "status" to "COMPLETED"
            ), "tran"),
            DynamicEntity("2", mapOf(
                "orderNo" to "ORD002",
                "amount" to 500.00,
                "customerId" to "2",
                "status" to "PENDING"
            ), "tran")
        )
        
        tables["payment"] = listOf(
            DynamicEntity("1", mapOf(
                "paymentNo" to "PAY001",
                "orderId" to "1",
                "amount" to 1000.00,
                "status" to "SUCCESS"
            ), "tran")
        )
    }

    fun getTableData(tableName: String, fields: List<String>): List<DynamicResult> {
        return tables[tableName]?.flatMap { entity ->
            fields.mapNotNull { field ->
                entity.fields[field]?.let { value ->
                    DynamicResult(entity.id, field, value.toString())
                }
            }
        } ?: emptyList()
    }
    
    fun getFields(tableName: String): Set<String> {
        return tables[tableName]?.firstOrNull()?.fields?.keys ?: emptySet()
    }
}

fun main() {
    embeddedServer(Netty, port = 4002) {
        install(ContentNegotiation) {
            gson()
        }
        install(CORS) {
            anyHost()
        }
        install(GraphQL) {
            playground = true
            endpoint = "/graphql"
            schema {
                query("queryTranTable") {
                    resolver { tableName: String, fields: List<String> ->
                        TranDataSource.getTableData(tableName, fields)
                    }
                }

                query("getTranAvailableFields") {
                    resolver { tableName: String ->
                        TranDataSource.getFields(tableName)
                    }
                }

                type<DynamicResult> {
                    description = "Dynamic query result for Transaction"
                    property(DynamicResult::id) {
                        description = "Entity ID"
                    }
                    property(DynamicResult::fieldName) {
                        description = "Field name"
                    }
                    property(DynamicResult::fieldValue) {
                        description = "Field value"
                    }
                }
            }
        }
    }.start(wait = true)
} 
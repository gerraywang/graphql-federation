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
import com.example.model.*

// Transaction data source simulation
object TranDataSource {
    private val tables = mutableMapOf<String, List<DynamicEntity>>()

    init {
        // Simulate transaction data
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
            gson {
                setPrettyPrinting()
            }
        }
        install(CORS) {
            anyHost()
        }
        install(GraphQL) {
            playground = true
            endpoint = "/graphql"
            schema {
                query("queryTranTable") {
                    resolver { args: QueryArgs ->
                        var results = TranDataSource.getTableData(args.tableName, args.fields)
                        
                        args.conditions?.let { conds ->
                            results = results.filter { result ->
                                conds.all { condition ->
                                    when (condition.operator) {
                                        OperatorType.GT -> (result.fieldValue.toDoubleOrNull() ?: 0.0) > (condition.value?.toDoubleOrNull() ?: 0.0)
                                        OperatorType.EQ -> result.fieldValue == condition.value
                                        else -> true
                                    }
                                }
                            }
                        }
                        
                        args.pagination?.let { page ->
                            results = results.drop(page.offset).take(page.limit)
                        }
                        
                        results
                    }
                }

                query("getTranAvailableFields") {
                    resolver { tableName: String ->
                        TranDataSource.getFields(tableName)
                    }
                }

                type<DynamicResult>()
                type<Condition>()
                type<PaginationInput>()
                type<SortField>()
                type<Transform>()
                
                enum<OperatorType>()
                enum<SortDirection>()
                enum<TransformType>()
            }
        }
    }.start(wait = true)
} 
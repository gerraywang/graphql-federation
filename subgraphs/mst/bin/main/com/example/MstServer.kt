package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import com.apurebase.kgraphql.GraphQL
import com.example.model.*

// MST data source simulation
object MstDataSource {
    private val tables = mutableMapOf<String, List<DynamicEntity>>()

    init {
        // Simulate MST data
        tables["customer"] = listOf(
            DynamicEntity("1", mapOf(
                "name" to "John Smith",
                "age" to 30,
                "address" to "Beijing"
            ), "mst"),
            DynamicEntity("2", mapOf(
                "name" to "Mike Johnson",
                "age" to 25,
                "address" to "Shanghai"
            ), "mst")
        )
        
        tables["product"] = listOf(
            DynamicEntity("1", mapOf(
                "name" to "Product A",
                "price" to 100.00,
                "stock" to 50
            ), "mst")
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
    embeddedServer(Netty, port = 4001) {
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
                query("queryMstTable") {
                    resolver { args: QueryArgs ->
                        // Process query conditions
                        var results = MstDataSource.getTableData(args.tableName, args.fields)
                        
                        // Apply condition filters
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
                        
                        // Apply pagination
                        args.pagination?.let { page ->
                            results = results.drop(page.offset).take(page.limit)
                        }
                        
                        results
                    }
                }

                query("getMstAvailableFields") {
                    resolver { tableName: String ->
                        MstDataSource.getFields(tableName)
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
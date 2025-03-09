package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import com.apurebase.kgraphql.GraphQL

// 数据模型
data class DynamicEntity(
    val id: String,
    val fields: Map<String, Any?>,
    val source: String = "mst"
)

data class DynamicResult(
    val id: String,
    val fieldName: String,
    val fieldValue: String
)

// MST数据源模拟
object MstDataSource {
    private val tables = mutableMapOf<String, List<DynamicEntity>>()

    init {
        // 模拟MST数据
        tables["customer"] = listOf(
            DynamicEntity("1", mapOf(
                "name" to "张三",
                "age" to 30,
                "address" to "北京"
            )),
            DynamicEntity("2", mapOf(
                "name" to "李四",
                "age" to 25,
                "address" to "上海"
            ))
        )
        
        tables["product"] = listOf(
            DynamicEntity("1", mapOf(
                "name" to "商品A",
                "price" to 100.00,
                "stock" to 50
            ))
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
            gson()
        }
        install(CORS) {
            anyHost()
        }
        install(GraphQL) {
            playground = true
            endpoint = "/graphql"
            schema {
                query("queryMstTable") {
                    resolver { tableName: String, fields: List<String> ->
                        MstDataSource.getTableData(tableName, fields)
                    }
                }

                query("getMstAvailableFields") {
                    resolver { tableName: String ->
                        MstDataSource.getFields(tableName)
                    }
                }

                type<DynamicResult> {
                    description = "Dynamic query result for MST"
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
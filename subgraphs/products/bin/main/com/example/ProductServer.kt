package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.gson.*
import com.apurebase.kgraphql.GraphQL
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder

// 通用数据模型
data class DynamicEntity(
    val id: String,
    val fields: Map<String, Any?>
)

data class DynamicResult(
    val id: String,
    val fieldName: String,
    val fieldValue: String
)

// 数据源模拟（实际项目中可以替换为数据库）
object DataSource {
    private val tables = mutableMapOf<String, List<DynamicEntity>>()

    init {
        // 模拟数据
        tables["products"] = listOf(
            DynamicEntity("101", mapOf(
                "name" to "Laptop",
                "price" to 999.99,
                "owner_id" to "1"
            )),
            DynamicEntity("102", mapOf(
                "name" to "Phone",
                "price" to 499.99,
                "owner_id" to "2"
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

fun SchemaBuilder.dynamicSchema() {
    query("queryTable") {
        resolver { tableName: String, fields: List<String> ->
            DataSource.getTableData(tableName, fields)
        }
    }

    query("getAvailableFields") {
        resolver { tableName: String ->
            DataSource.getFields(tableName)
        }
    }

    type<DynamicResult> {
        description = "Dynamic query result"
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
                dynamicSchema()
            }
        }
    }.start(wait = true)
}

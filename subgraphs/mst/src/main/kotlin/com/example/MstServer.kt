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
import com.example.model.*  // 添加这行导入

// MST数据源模拟
object MstDataSource {
    private val tables = mutableMapOf<String, List<com.example.model.DynamicEntity>>()

    init {
        // 模拟MST数据
        tables["customer"] = listOf(
            DynamicEntity("1", mapOf(
                "name" to "张三",
                "age" to 30,
                "address" to "北京"
            ), "mst"),
            DynamicEntity("2", mapOf(
                "name" to "李四",
                "age" to 25,
                "address" to "上海"
            ), "mst")
        )
        
        tables["product"] = listOf(
            DynamicEntity("1", mapOf(
                "name" to "商品A",
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
                        // 处理查询条件
                        var results = MstDataSource.getTableData(args.tableName, args.fields)
                        
                        // 应用条件过滤
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
                        
                        // 应用分页
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

// 查询参数包装类
data class QueryArgs(
    val tableName: String,
    val fields: List<String>,
    val conditions: List<Condition>? = null,
    val pagination: PaginationInput? = null,
    val sorting: List<SortField>? = null,
    val transforms: List<Transform>? = null
)

// 输入类型定义
data class Condition(
    val field: String,
    val operator: OperatorType,
    val value: String?
)

enum class OperatorType {
    EQ, NE, GT, LT, GTE, LTE, LIKE, IN
}

data class PaginationInput(
    val offset: Int,
    val limit: Int
)

data class SortField(
    val field: String,
    val direction: SortDirection
)

enum class SortDirection {
    ASC, DESC
}

data class Transform(
    val field: String,
    val type: TransformType,
    val format: String?
)

enum class TransformType {
    DATE, NUMBER, STRING
} 
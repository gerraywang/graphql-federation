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
import com.example.handler.CommonQueryHandler
import com.example.model.MultiTableConfig
import com.example.model.JoinConfig

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
    private val commonQueryHandler: CommonQueryHandler
) {
    fun Route.apiRoutes() {
        // 通用查询端点
        get("/api/{queryName}") {
            val queryName = call.parameters["queryName"]!!
            commonQueryHandler.handleQuery(
                queryName = queryName,
                conditions = call.request.queryParameters.entries().associate { 
                    it.key to listOf(it.value.first()) 
                },
                call = call
            )
        }
    }
} 
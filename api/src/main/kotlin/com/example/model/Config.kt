package com.example.model

data class QueryConfig(
    val queries: List<String>
)

data class MultiTableConfig(
    val tables: List<TableQuery>
)

data class JoinConfig(
    val mainTable: TableQuery,
    val relationName: String,
    val joins: List<JoinTableQuery>? = null
)

// 用于配置文件中的查询定义
data class QueryDefinition(
    val mainTable: TableQuery,
    val relationName: String,
    val joins: List<JoinTableQuery>
) 
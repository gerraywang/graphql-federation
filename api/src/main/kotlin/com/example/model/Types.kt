package com.example.model

// Query configuration
data class QueryConfig(
    val queries: List<String>
)

// Table query
data class TableQuery(
    val tableName: String,
    val fields: List<String>,
    val alias: String,
    val conditions: List<com.example.model.Condition>? = null
)

// Join table query
data class JoinTableQuery(
    val tableName: String,
    val fields: List<String>,
    val alias: String,
    val conditions: List<com.example.model.Condition>? = null
) {
    fun toTableQuery() = TableQuery(
        tableName = tableName,
        fields = fields,
        alias = alias,
        conditions = conditions
    )
}
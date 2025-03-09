package com.example.model

data class MultiTableConfig(
    val tables: List<TableQuery>
)

data class JoinConfig(
    val mainTable: TableQuery,
    val relationName: String,
    val joins: List<JoinTableQuery>? = null
) 
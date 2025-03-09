package com.example.model

data class TableQuery(
    val tableName: String,
    val fields: List<String>,
    val alias: String,
    val conditions: List<Condition>? = null,
    val pagination: PaginationInput? = null,
    val sorting: List<SortField>? = null,
    val transforms: List<Transform>? = null
)

data class JoinTableQuery(
    val tableName: String,
    val fields: List<String>,
    val alias: String,
    val conditions: List<Condition>? = null,
    val pagination: PaginationInput? = null,
    val sorting: List<SortField>? = null,
    val transforms: List<Transform>? = null
) {
    fun toTableQuery() = TableQuery(
        tableName = tableName,
        fields = fields,
        alias = alias,
        conditions = conditions,
        pagination = pagination,
        sorting = sorting,
        transforms = transforms
    )
} 
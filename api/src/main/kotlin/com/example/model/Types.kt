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
    val conditions: List<Condition>? = null
)

data class JoinTableQuery(
    val tableName: String,
    val fields: List<String>,
    val alias: String,
    val conditions: List<Condition>? = null
) {
    fun toTableQuery() = TableQuery(
        tableName = tableName,
        fields = fields,
        alias = alias,
        conditions = conditions
    )
}

// Query conditions
data class Condition(
    val field: String,
    val operator: OperatorType,
    val value: Any?
)

// Operator type
enum class OperatorType {
    EQ,    // equals
    NEQ,   // not equals
    GT,    // greater than
    GTE,   // greater than or equals
    LT,    // less than
    LTE,   // less than or equals
    LIKE,  // like
    IN,    // in
    NIN    // not in
}

// Pagination
data class PaginationInput(
    val offset: Int,
    val limit: Int
)

// Sorting
data class SortField(
    val field: String,
    val direction: SortDirection
)

enum class SortDirection {
    ASC,
    DESC
}

// Field transformation
data class Transform(
    val field: String,
    val type: TransformType,
    val format: String? = null
)

enum class TransformType {
    DATE,
    NUMBER,
    STRING
} 
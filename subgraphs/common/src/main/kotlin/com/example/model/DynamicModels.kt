package com.example.model

data class DynamicEntity(
    val id: String,
    val fields: Map<String, Any?>,
    val source: String
)

data class DynamicResult(
    val id: String,
    val fieldName: String,
    val fieldValue: String
)

// Query parameters wrapper
data class QueryArgs(
    val tableName: String,
    val fields: List<String>,
    val conditions: List<Condition>? = null,
    val pagination: PaginationInput? = null,
    val sorting: List<SortField>? = null,
    val transforms: List<Transform>? = null
)

// Input type definitions
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
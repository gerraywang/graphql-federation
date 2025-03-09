package com.example.model

// 表查询
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

// 查询条件
data class Condition(
    val field: String,
    val operator: OperatorType,
    val value: Any?
)

// 操作符类型
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

// 分页
data class PaginationInput(
    val offset: Int,
    val limit: Int
)

// 排序
data class SortField(
    val field: String,
    val direction: SortDirection
)

enum class SortDirection {
    ASC,
    DESC
}

// 字段转换
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
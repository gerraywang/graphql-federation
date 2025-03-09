package com.example.model

// 查询配置
data class QueryConfig(
    val queries: List<String>
)

// 表查询
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
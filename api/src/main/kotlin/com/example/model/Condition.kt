package com.example.model

data class Condition(
    val field: String,
    val operator: OperatorType,
    val value: Any?
)

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
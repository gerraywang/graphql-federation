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
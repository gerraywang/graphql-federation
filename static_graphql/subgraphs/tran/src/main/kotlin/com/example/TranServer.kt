package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import com.apurebase.kgraphql.GraphQL

// 静态数据模型
data class Order(
    val id: String,
    val orderNo: String,
    val amount: Double,
    val customerId: String,
    val status: String
)

data class Payment(
    val id: String,
    val paymentNo: String,
    val orderId: String,
    val amount: Double,
    val status: String
)

data class PaginationInput(
    val offset: Int,
    val limit: Int
)

// 静态数据源
object TranDataSource {
    val orders = listOf(
        Order(
            id = "1",
            orderNo = "ORD001",
            amount = 1000.00,
            customerId = "1",
            status = "COMPLETED"
        ),
        Order(
            id = "2",
            orderNo = "ORD002",
            amount = 500.00,
            customerId = "2",
            status = "PENDING"
        )
    )
    
    val payments = listOf(
        Payment(
            id = "1",
            paymentNo = "PAY001",
            orderId = "1",
            amount = 1000.00,
            status = "SUCCESS"
        )
    )
}

fun main() {
    embeddedServer(Netty, port = 4002) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        install(CORS) {
            anyHost()
        }
        install(GraphQL) {
            playground = true
            endpoint = "/graphql"
            schema {
                query("getOrders") {
                    description = "获取所有订单信息"
                    
                    resolver { pagination: PaginationInput? ->
                        var results = TranDataSource.orders
                        
                        if (pagination != null) {
                            results = results.drop(pagination.offset).take(pagination.limit)
                        }
                        
                        results
                    }
                }

                query("getPayments") {
                    description = "获取所有支付信息"
                    
                    resolver { pagination: PaginationInput? ->
                        var results = TranDataSource.payments
                        
                        if (pagination != null) {
                            results = results.drop(pagination.offset).take(pagination.limit)
                        }
                        
                        results
                    }
                }

                type<Order>()
                type<Payment>()
                type<PaginationInput>()
            }
        }
    }.start(wait = true)
} 
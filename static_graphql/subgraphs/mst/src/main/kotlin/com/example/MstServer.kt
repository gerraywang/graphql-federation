package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.gson.*
import com.apurebase.kgraphql.GraphQL

// 静态数据模型
data class Customer(
    val id: String,
    val name: String,
    val age: Int,
    val address: String
)

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int
)

data class PaginationInput(
    val offset: Int,
    val limit: Int
)

// 静态数据源
object MstDataSource {
    val customers = listOf(
        Customer("1", "John Smith", 30, "Beijing"),
        Customer("2", "Mike Johnson", 25, "Shanghai")
    )
    
    val products = listOf(
        Product("1", "Product A", 100.00, 50)
    )
}

fun main() {
    embeddedServer(Netty, port = 4001) {
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
                query("getCustomers") {
                    description = "获取所有客户信息"
                    
                    resolver { pagination: PaginationInput? ->
                        var results = MstDataSource.customers
                        
                        if (pagination != null) {
                            results = results.drop(pagination.offset).take(pagination.limit)
                        }
                        
                        results
                    }
                }

                query("getProducts") {
                    description = "获取所有产品信息"
                    
                    resolver { pagination: PaginationInput? ->
                        var results = MstDataSource.products
                        
                        if (pagination != null) {
                            results = results.drop(pagination.offset).take(pagination.limit)
                        }
                        
                        results
                    }
                }

                type<Customer>()
                type<Product>()
                type<PaginationInput>()
            }
        }
    }.start(wait = true)
}
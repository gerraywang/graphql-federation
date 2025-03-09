package com.example

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ApiResource {
    @Inject
    lateinit var graphQLService: GraphQLService

    @GET
    @Path("/products")
    fun getProducts(): Map<String, Any?> {
        val query = """
            query {
                products {
                    id
                    name
                    price
                    owner {
                        id
                        name
                        email
                    }
                }
            }
        """.trimIndent()
        
        return graphQLService.executeQuery(query)
    }

    @GET
    @Path("/users")
    fun getUsers(): Map<String, Any?> {
        val query = """
            query {
                users {
                    id
                    name
                    email
                }
            }
        """.trimIndent()
        
        return graphQLService.executeQuery(query)
    }
} 
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

    @POST
    @Path("/query")
    fun queryTable(request: QueryRequest): Map<String, Any?> {
        val query = """
            query {
                queryTable(tableName: "${request.tableName}", fields: ${request.fields.map { "\"$it\"" }}) {
                    id
                    fieldName
                    fieldValue
                }
            }
        """.trimIndent()
        
        return graphQLService.executeQuery(query)
    }

    @GET
    @Path("/fields/{tableName}")
    fun getAvailableFields(@PathParam("tableName") tableName: String): Map<String, Any?> {
        val query = """
            query {
                getAvailableFields(tableName: "$tableName")
            }
        """.trimIndent()
        
        return graphQLService.executeQuery(query)
    }
}

data class QueryRequest(
    val tableName: String,
    val fields: List<String>
) 
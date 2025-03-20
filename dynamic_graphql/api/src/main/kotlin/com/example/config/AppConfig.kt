package com.example.config

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.LoaderOptions
import java.io.InputStreamReader
import com.example.model.TableQuery
import com.example.model.JoinTableQuery

// Application configuration
class AppConfig {
    var relations: List<Relation> = emptyList()
    var queries: Map<String, QueryDefinition> = emptyMap()
    
    private val relationMap by lazy { relations.associateBy { it.name } }
    fun getRelation(name: String): Relation? = relationMap[name]
    
    fun getQueryDefinition(name: String): QueryDefinition? = queries[name]
}

// Query definition
class QueryDefinition {
    var mainTable: TableQueryConfig = TableQueryConfig()
    var relationName: String = ""
    var joins: List<JoinTableQueryConfig> = emptyList()
}

// Configuration class version of TableQuery
class TableQueryConfig {
    var tableName: String = ""
    var fields: List<String> = emptyList()
    var alias: String = ""
    
    fun toTableQuery() = TableQuery(
        tableName = tableName,
        fields = fields,
        alias = alias
    )
}

// Configuration class version of JoinTableQuery
class JoinTableQueryConfig {
    var tableName: String = ""
    var fields: List<String> = emptyList()
    var alias: String = ""
    
    fun toJoinTableQuery() = JoinTableQuery(
        tableName = tableName,
        fields = fields,
        alias = alias
    )
}

// Relation definition
class Relation {
    var name: String = ""
    var mainTable: String = ""
    var joinTables: List<JoinTable> = emptyList()
}

// Join table definition
class JoinTable {
    var table: String = ""
    var mainField: String = ""
    var joinField: String = ""
    var type: JoinType = JoinType.ONE_TO_ONE
    var parentTable: String? = null
}

// Join type
enum class JoinType {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE
}

// Configuration loader
object AppConfigLoader {
    fun load(configPath: String = "/app-config.yaml"): AppConfig {
        val loaderOptions = LoaderOptions()
        val constructor = Constructor(AppConfig::class.java, loaderOptions)
        val yaml = Yaml(constructor)
        
        val inputStream = this::class.java.getResourceAsStream(configPath)
            ?: throw IllegalArgumentException("Cannot find resource: $configPath")
            
        return InputStreamReader(inputStream).use { reader ->
            yaml.loadAs(reader, AppConfig::class.java)
        }
    }
} 
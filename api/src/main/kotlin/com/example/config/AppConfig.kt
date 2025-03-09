package com.example.config

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.LoaderOptions
import java.io.InputStreamReader
import com.example.model.TableQuery
import com.example.model.JoinTableQuery

// 应用配置
class AppConfig {
    var relations: List<Relation> = emptyList()
    var queries: Map<String, QueryDefinition> = emptyMap()
    
    private val relationMap by lazy { relations.associateBy { it.name } }
    fun getRelation(name: String): Relation? = relationMap[name]
    
    fun getQueryDefinition(name: String): QueryDefinition? = queries[name]
}

// 查询定义
class QueryDefinition {
    var mainTable: TableQueryConfig = TableQueryConfig()
    var relationName: String = ""
    var joins: List<JoinTableQueryConfig> = emptyList()
}

// 配置类版本的 TableQuery
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

// 配置类版本的 JoinTableQuery
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

// 关系定义
class Relation {
    var name: String = ""
    var mainTable: String = ""
    var joinTables: List<JoinTable> = emptyList()
}

// 连接表定义
class JoinTable {
    var table: String = ""
    var mainField: String = ""
    var joinField: String = ""
    var type: JoinType = JoinType.ONE_TO_ONE
    var parentTable: String? = null
}

// 连接类型
enum class JoinType {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE
}

// 配置加载器
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
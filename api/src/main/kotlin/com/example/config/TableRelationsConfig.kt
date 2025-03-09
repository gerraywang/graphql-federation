package com.example.config

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.LoaderOptions
import java.io.InputStreamReader

// 表关系配置
class TableRelations {
    lateinit var relations: List<Relation>
    
    private val relationMap by lazy { relations.associateBy { it.name } }
    fun getRelation(name: String): Relation? = relationMap[name]
}

// 关系定义
class Relation {
    lateinit var name: String
    lateinit var mainTable: String
    lateinit var joinTables: List<JoinTable>
}

// 连接表定义
class JoinTable {
    lateinit var table: String
    lateinit var mainField: String
    lateinit var joinField: String
    lateinit var type: JoinType
    var parentTable: String? = null
}

// 连接类型
enum class JoinType {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE
}

// 加载器
object TableRelationsLoader {
    fun load(configPath: String = "/table-relations.yaml"): TableRelations {
        val loaderOptions = LoaderOptions()
        val constructor = Constructor(TableRelations::class.java, loaderOptions)
        val yaml = Yaml(constructor)
        
        val inputStream = this::class.java.getResourceAsStream(configPath)
            ?: throw IllegalArgumentException("Cannot find resource: $configPath")
            
        return InputStreamReader(inputStream).use { reader ->
            yaml.loadAs(reader, TableRelations::class.java)
        }
    }
} 
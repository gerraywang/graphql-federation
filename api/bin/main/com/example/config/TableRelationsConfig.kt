package com.example.config

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.LoaderOptions
import java.io.InputStreamReader

// 使用普通类而不是data class，并提供var属性
class TableRelations {
    var relations: List<Relation> = emptyList()
    
    private val relationMap by lazy { relations.associateBy { it.name } }
    
    fun getRelation(name: String): Relation? = relationMap[name]
}

class Relation {
    var name: String = ""
    var mainTable: String = ""
    var joinTables: List<JoinTable> = emptyList()
}

class JoinTable {
    var table: String = ""
    var mainField: String = ""
    var joinField: String = ""
    var type: JoinType = JoinType.one_to_one
    var parentTable: String? = null
}

enum class JoinType {
    one_to_one,
    one_to_many
}

object TableRelationsLoader {
    fun load(): TableRelations {
        val loaderOptions = LoaderOptions()
        val constructor = Constructor(TableRelations::class.java, loaderOptions)
        val yaml = Yaml(constructor)
        
        val inputStream = this.javaClass.classLoader.getResourceAsStream("table-relations.yaml")
            ?: throw IllegalStateException("Could not find table-relations.yaml")
            
        return inputStream.use { stream ->
            yaml.loadAs(InputStreamReader(stream), TableRelations::class.java)
        }
    }
} 
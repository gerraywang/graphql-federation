plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":static_graphql:subgraphs:common"))
    implementation(kotlin("stdlib"))
    
    // Ktor
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-gson:2.3.7")
    implementation("io.ktor:ktor-server-cors:2.3.7")
    
    // GraphQL
    implementation("com.apurebase:kgraphql:0.19.0")
    implementation("com.apurebase:kgraphql-ktor:0.19.0")
}

application {
    mainClass.set("com.example.MstServerKt")
} 
plugins {
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
    
    // HTTP Client
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-client-gson:2.3.7")
    
    // YAML
    implementation("org.yaml:snakeyaml:2.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
}

application {
    mainClass.set("com.example.ApiServerKt")
} 
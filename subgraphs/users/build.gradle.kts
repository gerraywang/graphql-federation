plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("com.example.UserServerKt")
}

dependencies {
    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core-jvm:2.3.7")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.7")
    implementation("io.ktor:ktor-serialization-gson-jvm:2.3.7")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.7")
    
    // GraphQL dependencies
    implementation("com.apurebase:kgraphql:0.19.0")
    implementation("com.apurebase:kgraphql-ktor:0.19.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
}

repositories {
    mavenCentral()
    // Add jitpack repository for kgraphql
    maven { url = uri("https://jitpack.io") }
} 
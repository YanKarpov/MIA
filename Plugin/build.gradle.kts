plugins {
    id("java")
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.14.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.register<Copy>("copyToServer") {
    dependsOn("build")
    from(layout.buildDirectory.dir("libs"))
    include("*.jar")
    into("../minecraft/plugins")
}

tasks.register<Exec>("restartServer") {
    commandLine("docker", "restart", "minecraft-dev")
}

tasks.register("deploy") {
    dependsOn("copyToServer")
    finalizedBy("restartServer")
}
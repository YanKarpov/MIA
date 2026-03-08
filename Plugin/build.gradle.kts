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
    implementation("org.postgresql:postgresql:42.7.10")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("QuestAIPlugin")
    archiveVersion.set(version.toString())

    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.example.questai.QuestPlugin"
    }
}

tasks.register<Copy>("copyToServer") {
    dependsOn("fatJar")
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
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
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.6.0")

    testCompileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
}

tasks.jar {
    enabled = false
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("QuestAIPlugin")
    archiveVersion.set(version.toString())

    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.example.questai.QuestPlugin"
    }
}

tasks.register<Delete>("cleanPlugins") {
    delete(fileTree("../minecraft/plugins") {
        include("*.jar")
    })
}

tasks.register<Copy>("copyToServer") {
    dependsOn("fatJar")
    from(layout.buildDirectory.file("libs/QuestAIPlugin-${version}.jar"))
    into("../minecraft/plugins")
}

tasks.register<Exec>("restartServer") {
    commandLine("docker", "restart", "minecraft-dev")
}

tasks.register("deploy") {
    dependsOn("cleanPlugins")
    dependsOn("copyToServer")
    finalizedBy("restartServer")
}

tasks.build {
    dependsOn("fatJar")
}
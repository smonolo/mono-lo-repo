plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "dev.smnl"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:5.1.0")
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.24.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("*.gradle.kts", "src/**/*.yml")
        trimTrailingWhitespace()
        indentWithSpaces(2)
        endWithNewline()
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveFileName.set("SMEssential.jar")
}

val copyJar = tasks.register<Copy>("copyJar") {
    from(tasks.shadowJar)
    into(projectDir)
}

tasks.build {
    dependsOn(tasks.shadowJar)
    finalizedBy(copyJar)
}

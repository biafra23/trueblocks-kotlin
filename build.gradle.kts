plugins {
//    id("org.jetbrains.kotlin.kapt") version "1.9.0"
    kotlin("jvm") version "1.9.0"
    application
    id("maven-publish")
}

group = "com.github.biafra23"
version = "1.0-SNAPSHOT"

publishing {
    publications {
        create<MavenPublication>("mavenLocal") {
            from(components["kotlin"])

            groupId = "com.github.biafra23"
            artifactId = "trueblocks"
            version = "1.0-SNAPSHOT"
        }
    }
}

repositories {
//    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    implementation("com.github.komputing.kethereum:model:0.86.0")
    implementation("com.github.komputing.khex:core:1.1.2")
    implementation("com.github.komputing.khex:extensions:1.1.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Use the latest version
    implementation("com.squareup.moshi:moshi:1.9.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.1")
//    implementation("com.github.ligi:ipfs-api-kotlin:0.16.0-SNAPSHOT")
    implementation("com.github.biafra23:ipfs-api-kotlin:peers-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
//    mainClass.set("CheckAddressKt")
}

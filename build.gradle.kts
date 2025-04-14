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

    implementation("com.squareup.okhttp3:okhttp:4.2.2")
    implementation("com.squareup.moshi:moshi:1.9.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.1")
//    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.1")
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

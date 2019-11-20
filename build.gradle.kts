plugins {
    application
    kotlin("jvm") version "1.3.60"
}

group = "io.github.dgahn"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.typesafe.akka:akka-stream_2.13:2.6.0")
    testImplementation("com.typesafe.akka:akka-actor-testkit-typed_2.13:2.6.0")
    implementation("com.typesafe.akka:akka-actor-typed_2.13:2.6.0")
    implementation("com.typesafe.akka:akka-serialization-jackson_2.13:2.6.0")
    implementation("com.typesafe.akka:akka-http-jackson_2.13:10.1.10")
    implementation("com.typesafe.akka:akka-http_2.13:10.1.10")

    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("org.slf4j:slf4j-api:1.7.29")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.12.1")
    implementation("org.apache.logging.log4j:log4j-api:2.12.1")
    implementation("org.apache.logging.log4j:log4j-core:2.12.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

application {
    mainClassName = "io.github.dgahn.akkadocs.QuickstartAppKt"
}
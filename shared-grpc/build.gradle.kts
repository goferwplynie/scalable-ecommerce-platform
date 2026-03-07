import com.google.protobuf.gradle.id

plugins {
    `java-library`
    id("com.google.protobuf") version "0.9.4"
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.grpc:grpc-netty-shaded:1.75.0")
    api("io.grpc:grpc-protobuf:1.61.1")
    api("io.grpc:grpc-stub:1.61.1")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.61.1"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {}
            }
        }
    }
}

sourceSets {
    getByName("main") {
        java {
            srcDirs("build/generated/source/proto/main/grpc", "build/generated/source/proto/main/java")
        }
    }
}
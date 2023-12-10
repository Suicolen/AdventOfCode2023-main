plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("one.util:streamex:0.8.2")
    val lombok = module("org.projectlombok", "lombok", "1.18.30")

    compileOnly(lombok)
    annotationProcessor(lombok)

    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.jooq:jool:0.9.14")
    implementation("io.github.zabuzard.maglev:maglev:1.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
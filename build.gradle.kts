plugins {
    id("java")
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
}

group = "au.lainey"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.ladysnake.org/releases")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.116.17+1.21.1")

    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-base:6.1.3")
    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-chunk:6.1.3")
}

tasks.test {
    useJUnitPlatform()
}

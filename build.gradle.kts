plugins {
	kotlin("jvm") version "1.8.10"
	id("java-library")
	id("application")
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.iwareq.mcperpencrdec"
version = "1.0.0"

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	api("org.bouncycastle:bcprov-jdk15on:1.70")
	api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
}

application {
	mainClass.set("me.iwareq.mcperpencrdec.MainKt")
}

kotlin {
	jvmToolchain(8)
}

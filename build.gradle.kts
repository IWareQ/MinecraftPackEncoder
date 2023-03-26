plugins {
	kotlin("jvm") version "1.8.10"
}

group = "me.iwareq.mcperpencrdec"
version = "1.0.0"

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation("org.bouncycastle:bcprov-jdk15on:1.70")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
}

kotlin {
	jvmToolchain(17)
}

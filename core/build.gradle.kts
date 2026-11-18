plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}
kotlin { jvmToolchain(17) }
dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
  testImplementation("junit:junit:4.13.2")
}
tasks.withType<Test>().configureEach {
  useJUnit()
  systemProperty("crossroads.repoDir", rootProject.projectDir.absolutePath)
}

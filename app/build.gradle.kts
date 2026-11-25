import java.security.MessageDigest
import java.net.URI
import java.awt.RenderingHints
import java.awt.Color
import java.util.ArrayDeque
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
}
val requestedQaVersion = providers.gradleProperty("crossroadsQaVersionCode").orNull?.toIntOrNull()
if (providers.gradleProperty("crossroadsQaVersionCode").isPresent) {
    require(requestedQaVersion != null && requestedQaVersion in 2..2_100_000_000) { "Invalid QA versionCode" }
}
// Pinned, checksum-verified build-time import. Installed games never fetch branding.
val startupAssetsDir = layout.buildDirectory.dir("generated/startupBranding/assets")
val prepareStartupBranding = tasks.register("prepareStartupBranding") {
    val pins = listOf(
        Triple("spudacus.png", "015645bffb612e0f4311ec5a2d29e9c8bad91289/branding/spudacus/v1.0/full-transparent.png", "74773dbda822161f73a639f959e8bce5c6e0b333a94ebeb8ee27f620d4995d25"),
        Triple("gridmates.png", "9affedc94ca5a6b896d36148f46f14a4e8218c3a/branding/gridmates/v1.0/runtime-transparent.png", "eef6d631cac0b9523f7e0dbebac229f9a2458d36fe2e73e111d251704d9ff292")
    )
    inputs.property("startupPins", pins.toString())
    outputs.dir(startupAssetsDir)
    doLast {
        val folder = startupAssetsDir.get().dir("branding/startup/v1").asFile
        folder.mkdirs()
        fun hash(bytes: ByteArray) = MessageDigest.getInstance("SHA-256")
            .digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
        for ((name, source, expected) in pins) {
            val dest = folder.resolve(name)
            if (!dest.isFile || hash(dest.readBytes()) != expected) {
                val url = URI("https://raw.githubusercontent.com/EdenP-Development/Eden-Development-Standards/" + source).toURL()
                val connection = url.openConnection().apply { connectTimeout = 15000; readTimeout = 30000 }
                val bytes = connection.getInputStream().use { it.readBytes() }
                check(hash(bytes) == expected) { "Startup branding checksum mismatch: " + name }
                dest.writeBytes(bytes)
            }
            check(hash(dest.readBytes()) == expected) { "Startup branding checksum mismatch: " + name }
        }
    }
}
 // BRANDING-115: preserve the exact master; generate a mask-safe adaptive foreground.
val launcherMaster = rootProject.file("design/art/imports/Crossroads-approved-icon-v1.png")
val launcherResDir = layout.buildDirectory.dir("generated/crossroadsLauncher/res")
val prepareCrossroadsLauncher = tasks.register("prepareCrossroadsLauncher") {
    inputs.file(launcherMaster)
    outputs.dir(launcherResDir)
    doLast {
        val bytes = launcherMaster.readBytes()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        check(hash == "711cde86e7d7da605206b3c531ee80ba0d768860ab5f528ec836388555208d4b") { "Crossroads launcher master checksum mismatch" }
        val source = requireNotNull(ImageIO.read(launcherMaster))
        check(source.width == 1254 && source.height == 1254)
        // Remove only near-black pixels connected to the outer canvas edge.
        // Interior outlines and the approved original remain untouched.
        val w = source.width
        val h = source.height
        val cleaned = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        cleaned.setRGB(0, 0, w, h, source.getRGB(0, 0, w, h, null, 0, w), 0, w)
        val visited = BooleanArray(w * h)
        val queue = ArrayDeque<Int>()
        fun offer(x: Int, y: Int) {
            if (x !in 0 until w || y !in 0 until h) return
            val i = y * w + x
            if (visited[i]) return
            visited[i] = true
            val c = Color(source.getRGB(x, y))
            if (maxOf(c.red, c.green, c.blue) <= 48) queue.addLast(i)
        }
        for (x in 0 until w) { offer(x, 0); offer(x, h - 1) }
        for (y in 0 until h) { offer(0, y); offer(w - 1, y) }
        var removed = 0
        while (queue.isNotEmpty()) {
            val i = queue.removeFirst()
            val x = i % w
            val y = i / w
            cleaned.setRGB(x, y, 0)
            removed++
            offer(x - 1, y); offer(x + 1, y); offer(x, y - 1); offer(x, y + 1)
        }
        check(removed > 0 && removed < w * h / 4) { "Unexpected launcher corner removal: $removed" }
        // 56 dp artwork on a 108 dp layer: slightly larger, still conservative.
        val layer = BufferedImage(432, 432, BufferedImage.TYPE_INT_ARGB)
        val graphics = layer.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            graphics.drawImage(cleaned, 104, 104, 224, 224, null)
        } finally { graphics.dispose() }
        val output = launcherResDir.get().file("drawable-nodpi/ic_crossroads_family_foreground.png").asFile
        output.parentFile.mkdirs()
        check(ImageIO.write(layer, "png", output))
    }
}
android {
    namespace = "com.playereden.gridmates.crossroads"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.playereden.gridmates.crossroads"
        minSdk = 26
        targetSdk = 35
        versionCode = requestedQaVersion ?: 1
        versionName = "0.1.0"
        // Public-facing label by default. The .qa build overrides it below.
        // This does NOT configure or authorize a production-signed release.
        resValue("string", "app_name", "GridMates: Crossroads")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        // Deliberately PUBLIC QA-only identity, pinned to the sibling Word Search test signer.
        // DO NOT use this public key for production/public releases.
        create("qaPublicTestOnly") {
            storeFile = rootProject.file("qa-signing/wordsearch-qa.p12")
            storeType = "pkcs12"
            storePassword = "android"
            keyAlias = "wordsearchQa"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            resValue("string", "app_name", "Crossroads QA")
            signingConfig = signingConfigs.getByName("qaPublicTestOnly")
        }
        getByName("release") {
            isMinifyEnabled = false
            // Production signing is NOT yet configured. Do not distribute release APKs.
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    sourceSets.getByName("main").assets.srcDir(rootProject.file("data"))
    sourceSets.getByName("main").assets.srcDir(startupAssetsDir)
    sourceSets.getByName("main").res.srcDir(launcherResDir)
    // QA-only prototype assets; not bundled into the public release.
    sourceSets.getByName("debug").assets.srcDir(rootProject.file("design/prototypes"))
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
tasks.named("preBuild").configure { dependsOn(prepareStartupBranding, prepareCrossroadsLauncher) }
dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}

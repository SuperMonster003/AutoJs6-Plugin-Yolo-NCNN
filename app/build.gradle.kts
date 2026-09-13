import com.android.build.api.variant.FilterConfiguration
import org.gradle.api.provider.Property

plugins {
    id("io.github.supermonster003.autojs6-native-alignment")
    id("org.autojs.build.utils")
    id("org.autojs.build.versions")
    id("org.autojs.build.signs")
    id("org.autojs.build.jvm-convention")
    id("com.android.application")
}

val globalApplicationId = "io.github.supermonster003.autojs6.plugin.yolo.ncnn"
val supportedAbi = "arm64-v8a"
val buildTypeDebug = "debug"
val buildTypeRc = "rc"
val buildTypeRelease = "release"

android {
    namespace = globalApplicationId
    compileSdk = versions.sdkVersionCompile
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = globalApplicationId
        minSdk = versions.sdkVersionMin
        targetSdk = versions.sdkVersionTarget
        versionCode = versions.appVersionCode
        versionName = versions.appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk.abiFilters += supportedAbi

        externalNativeBuild {
            cmake {
                arguments += "-DANDROID_STL=c++_static"
            }
        }

        buildConfigField("String", "VERSION_DATE", "\"${utils.getDateString("MMM d, yyyy", "GMT+08:00")}\"")
        buildConfigField("boolean", "NCNN_RUNTIME_STAGED", "true")
        resValue("string", "plugin_author", "SuperMonster003")
        resValue("string", "plugin_id", "yolo-ncnn")
        resValue("string", "plugin_engine", "yolo")
        resValue("string", "plugin_variant", "ncnn")
        resValue("string", "plugin_requires_host_version", "5275")
        resValue("string", "plugin_runtime_component", "io.github.supermonster003.autojs6.plugin.yolo.ncnn/io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider.YoloProviderService")
        resValue("string", "plugin_protocol_api_min", "1.0")
        resValue("string", "plugin_protocol_api_max", "1.0")
        resValue("string", "plugin_backend", "ncnn")
        resValue("string", "plugin_task", "detect")
        resValue("string", "plugin_decoder", "ultralytics-detect")
        resValue("string", "plugin_supported_abis", "arm64-v8a")
        resValue("string", "plugin_version_date", utils.getDateString("MMM d, yyyy", "GMT+08:00"))
    }

    lint {
        abortOnError = true
    }

    signingConfigs {
        if (signs.isValid) {
            create(buildTypeRelease) {
                storeFile = signs.properties["storeFile"]?.let { file(it as String) }
                keyPassword = signs.properties["keyPassword"] as String
                keyAlias = signs.properties["keyAlias"] as String
                storePassword = signs.properties["storePassword"] as String
            }
        }
    }

    buildTypes {
        val rules = arrayOf<Any>(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
        val releaseSigning = takeIf { signs.isValid }?.let {
            signingConfigs.getByName(buildTypeRelease)
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(*rules)
            releaseSigning?.let { signingConfig = it }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(*rules)
            releaseSigning?.let { signingConfig = it }
        }
        create(buildTypeRc) {
            initWith(getByName(buildTypeRelease))
            isDebuggable = false
            signingConfig = signingConfigs.getByName(buildTypeDebug)
            matchingFallbacks += listOf(buildTypeRelease)
            versionNameSuffix = "-rc-test-signed"
            buildConfigField("String", "BUILD_CHANNEL", "\"RC_TEST_SIGNED\"")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*android.defaultConfig.ndk.abiFilters.toTypedArray())
            // AGP requires a universal output when NDK filters and ABI splits coexist.
            isUniversalApk = true
        }
    }

    buildFeatures {
        aidl = true
        buildConfig = true
        resValues = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets.named("main") {
        kotlin.directories += "src/main/java"
    }

    sourceSets.named("androidTest") {
        assets.srcDirs(
            "$rootDir/fixtures/yolo11n",
            "$rootDir/fixtures/local/yolo11n",
        )
    }

    packaging {
        jniLibs.useLegacyPackaging = true
        resources.pickFirsts.addAll(
            listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.*",
                "META-INF/NOTICE",
                "META-INF/NOTICE.*",
                "META-INF/*.kotlin_module",
            ),
        )
    }

    bundle {
        language.enableSplit = false
        density.enableSplit = false
        abi.enableSplit = false
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val architecture = output.filters.find {
                it.filterType == FilterConfiguration.FilterType.ABI
            }?.identifier ?: "universal"
            val outputFileNameProperty = output.javaClass.methods.firstOrNull {
                it.name == "getOutputFileName" && it.parameterTypes.isEmpty()
            }?.invoke(output) as? Property<*>

            @Suppress("UNCHECKED_CAST")
            (outputFileNameProperty as? Property<String>)?.set(
                output.versionName.map { versionName ->
                    val version = versionName.replace("\\s".toRegex(), "-")
                    "${rootProject.name}-v$version-$architecture.${utils.FILE_EXTENSION_APK}".lowercase()
                },
            )
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    // Flat local AARs do not carry the Parcelize runtime dependency metadata that
    // would normally be supplied by a published POM. R8 must be able to resolve
    // the binary-retained @Parcelize annotation on PluginInfo while shrinking.
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.2.21")

    implementation(files("$rootDir/libs/common-plugin-api.aar"))
    implementation(files("$rootDir/libs/protocol-wire-api.aar"))
    implementation(files("$rootDir/libs/yolo-api.aar"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.test.runner)
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
}

extra {
    versions.handleIfNeeded(project, "", listOf(buildTypeDebug, buildTypeRelease))
}

apply(from = rootProject.file("gradle/release-archive.gradle"))

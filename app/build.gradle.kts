plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.jaredsburrows.license")
}

licenseReport {
    generateCsvReport = false
    generateHtmlReport = false
    generateJsonReport = true

    copyHtmlReportToAssets = false
    copyJsonReportToAssets = true
}

android {
    namespace = "com.thirdworlds.wakeonlan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thirdworlds.wakeonlan"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("signingConfig") {
            val storeFilePath =
                if (project.hasProperty("signing.storeFile")) project.property("signing.storeFile") as String else null
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = project.findProperty("signing.storePassword") as String? ?: ""
                keyAlias = project.findProperty("signing.keyAlias") as String? ?: ""
                keyPassword = project.findProperty("signing.storePassword") as String? ?: ""
                enableV2Signing = (project.findProperty("signing.v2SigningEnabled") as String?)?.toBoolean() ?: true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingConfigs.findByName("signingConfig")?.storeFile?.exists() == true) {
                signingConfig = signingConfigs.getByName("signingConfig")
            }
        }

        debug {
            if (signingConfigs.findByName("signingConfig")?.storeFile?.exists() == true) {
                signingConfig = signingConfigs.getByName("signingConfig")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "META-INF/versions/**/OSGI-INF/MANIFEST.MF"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                val config = project.android.defaultConfig
                val versionName = config.versionName
                this.outputFileName = "${rootProject.name}_v${versionName}_${this.name}.apk"
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    val roomVersion = "2.7.0"

    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")

    // SSH 连接
    implementation("org.apache.sshd:sshd-core:2.15.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.79")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.79")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.leonlatsch:OssLicenseView:1.1.0")
}

tasks.register("printVersion") {
    doLast {
        println(android.defaultConfig.versionName)
    }
}

tasks.register("printName") {
    doLast {
        println(rootProject.name)
    }
}
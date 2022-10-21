plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.netease.yunxin.app.medical"
        minSdk = 21
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        jniLibs.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        jniLibs.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 输出类型
    android.applicationVariants.all {
        // 编译类型
        val buildType = this.buildType.name
        outputs.all {
            // 判断是否是输出 apk 类型
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName = "medical-demo-$buildType.apk"
            }
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
    }

    sourceSets["main"].jniLibs.srcDirs("libs")

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation("com.google.android.material:material:1.5.0")
    implementation("com.gyf.immersionbar:immersionbar:3.0.0")

    implementation("com.squareup.okhttp3:okhttp:4.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.netease.yunxin.kit:alog:1.0.8")
    implementation("com.netease.yunxin.kit.auth:auth-yunxin-login:1.0.4")
    implementation("com.netease.yunxin.kit:call-pstn:1.6.4")
    implementation("com.netease.yunxin.kit.common:common-network:1.1.6")
    implementation("com.netease.yunxin.kit.common:common-image:1.1.6")
    implementation("com.netease.yunxin.kit.common:common-ui:1.1.9")
    implementation("com.netease.yunxin.kit.common:common:1.1.9")
    implementation("com.netease.yunxin.kit.chat:chatkit-ui:9.2.10-rc01")
    implementation("com.netease.yunxin.kit.conversation:conversationkit-ui:9.2.10-rc01")
}
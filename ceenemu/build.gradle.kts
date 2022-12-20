val apiCode: Int by rootProject.extra
val verName: String by rootProject.extra
val verCode: Int by rootProject.extra

plugins {
    id("com.android.library")
}

android {
    namespace = "com.ceen.emu"
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        buildConfigField("int", "API_CODE", "$apiCode")
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    compileOnly(projects.core)
}
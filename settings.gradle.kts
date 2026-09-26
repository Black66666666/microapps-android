pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "microapps-android"
include(":apps:screenshot-inbox")
include(":apps:match-choice")
include(":apps:who-brings-what")
include(":apps:buy-tomorrow")
include(":apps:meeting-meter")

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
include(":core:designsystem")
include(":core:microappkit")
include(":apps:screenshot-inbox")
include(":apps:match-choice")
include(":apps:who-brings-what")
include(":apps:buy-tomorrow")
include(":apps:meeting-meter")
include(":apps:scroll-receipt")
include(":apps:worth-it")
include(":apps:borrow-back")
include(":apps:box-qr")
include(":apps:refill")
include(":apps:turn-keeper")
include(":apps:five-minutes")
include(":apps:return-clock")
include(":apps:where-is-it")
include(":apps:opened-on")
include(":apps:pack-together")
include(":apps:promise")
include(":apps:gift-pocket")
include(":apps:before-leave")
include(":apps:fair-pick")

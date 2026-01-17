pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "Blockwise"

// App module
include(":app")

// Core modules
include(":core:common")
include(":core:designsystem")
include(":core:data")
include(":core:domain")
include(":core:testing")

// Feature modules
include(":feature:timeentry")
include(":feature:statistics")
include(":feature:goal")
include(":feature:settings")
include(":feature:onboarding")

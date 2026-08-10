import java.util.Properties

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
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = getMapboxDownloadToken()
            }
        }
    }
}

fun getMapboxDownloadToken(): String{
    val localProps = Properties().apply {
        val file = file("local.properties")
        if (file.exists()) load(file.inputStream())
    }
    return localProps.getProperty("MAPBOX_DOWNLOADS_TOKEN")
        ?: System.getenv("MAPBOX_DOWNLOADS_TOKEN")
        ?: ""
}
rootProject.name = "VoltWay"
include(":app")

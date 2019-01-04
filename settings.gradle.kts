rootProject.name = "backend"
buildscript {
    repositories {
        maven {
            setUrl("https://artifactory.open-xchange.com/artifactory/libs-release/")
        }
    }
    dependencies {
        classpath("com.openexchange.build:projectset:1.2.0")
    }
}

// Just for testing some newly developed plugin feature
//includeBuild("../gradle/<plugin>")

apply(action = {
    plugin("com.openexchange.build.projectset")
})

include("com.openexchange.test")

// TODO enable later when JARs will be removed from com.openexchange.bundles
// includeBuild("target-platform")

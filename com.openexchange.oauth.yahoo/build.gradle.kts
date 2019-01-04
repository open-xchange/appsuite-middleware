repositories {
    maven {
        url = uri("https://artifactory.open-xchange.com/artifactory/libs-release")
    }
}

// TODO dependency is defined as project library which is not yet detected by the new buildsystem
dependencies {
    testCompile(group = "junit", name = "junit", version = "4.12")
}


// TODO the test source folder is not part of the .classpath file but considered by the Gradle buildsystem
sourceSets {
    test {
        java {
            setSrcDirs(files())
        }
    }
}


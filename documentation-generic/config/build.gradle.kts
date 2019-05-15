plugins {
    java
}

repositories {
    maven {
        url = uri("https://artifactory.open-xchange.com/artifactory/libs-snapshot")
    }
}

dependencies {
    runtime("com.openexchange.appsuite.mw", "config-doc-processor", "1.+")
}

tasks.create("runConfigDocuProcessor", JavaExec::class.java) {
    classpath = sourceSets["main"].runtimeClasspath
    main = "com.openexchange.config.docu.parser.Parser"
    val arguments = mutableListOf<String>()
    arguments.add(project.projectDir.absolutePath)
    if (project.hasProperty("targetDirectory")) {
        arguments.add(project.property("targetDirectory").toString())
    }
    if (project.hasProperty("targetVersion")) {
        arguments.add(project.property("targetVersion").toString())
    }
    args(arguments)
}

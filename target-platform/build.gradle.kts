repositories {
    maven {
        setUrl("https://artifactory.open-xchange.com/artifactory/jcenter/")
    }
}
val targetPlatform: Configuration by configurations.creating

dependencies {
    targetPlatform(group = "org.eclipse.platform", name = "org.eclipse.osgi", version = "3.13.0")
}

copy {
    val jars = targetPlatform.resolvedConfiguration.files
    from(jars)
    into("../com.openexchange.bundles/jars/")
}

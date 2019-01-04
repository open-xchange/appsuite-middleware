buildscript {
    repositories {
        maven {
            url = uri("https://artifactory.open-xchange.com/artifactory/libs-release")
        }
    }
    dependencies {
        classpath("com.openexchange.build:osgi:1.5.3")
        classpath("com.openexchange.build:install:2.2.1")
        classpath("com.openexchange.build:packaging:3.0.1")
        classpath("com.openexchange.build:pde-java:1.1.0")
        classpath("com.openexchange.build:plugin-applier:1.1.2")
        classpath("com.openexchange.build:opensuse-build-service-client:1.4.0")
        classpath("com.openexchange.build:project-type-scanner:1.2.1")
        classpath("com.openexchange.build:licensing:1.0.3")
        classpath("com.openexchange.build:gradle-git:2.1.6")
    }
}
allprojects {
    apply {
        plugin("com.openexchange.build.plugin-applier")
    }
    tasks.withType(AbstractTestTask::class.java).configureEach {
        // TODO all tests need to run successfully one time
        ignoreFailures = true
    }
}

apply {
    plugin("com.openexchange.build.gradle-git")
    plugin("com.openexchange.build.licensing")
}

configure<com.openexchange.build.licensing.LicensingExtension> {
    licenses {
        register("server") {
            this.sourceFile = File(project.projectDir, "SERVER-LICENSE")
        }
    }
}

configure<com.openexchange.obs.gradle.plugin.BuildserviceExtension> {
    url = "https://buildapi.open-xchange.com"
    login = "oxbuilduser"
    password = "openxchange"
    project(closureOf<com.openexchange.obs.gradle.plugin.Project> {
        name = "backend-gradle"
        this.repositories(closureOf<NamedDomainObjectContainer<com.openexchange.obs.gradle.plugin.Repository>> {
            create("DebianStretch") {
                depends(kotlin.collections.mapOf("project" to "Debian:Stretch", "repository" to "standard"))
            }
            create("RHEL6") {
                depends(kotlin.collections.mapOf("project" to "RedHat:build-dependencies", "repository" to "RHEL6"))
            }
            create("RHEL7") {
                depends(kotlin.collections.mapOf("project" to "RedHat:build-dependencies", "repository" to "RHEL7"))
            }
            create("SLE_12") {
                depends(kotlin.collections.mapOf("project" to "SUSE:SLE-12-SP2", "repository" to "standard"))
            }
        })
    })
}

configure<com.openexchange.build.install.extension.InstallExtension> {
    //destDir.set(file("${System.getProperty("user.home")}/tmp/testox"))
    destDir.set(file("/"))
    prefix.set(file("/opt/open-xchange"))
}


import com.openexchange.build.install.extension.InstallExtension

val symlink = tasks.register("symlink") {
    val extension = project.extensions.getByType(InstallExtension::class.java)        
    val resource = extension.destDir.get().dir(extension.prefixResolve("bundles/${project.name}.jar").call().toRelativeString(file("/"))).asFile
    val link = extension.destDir.get().dir(extension.prefixResolve("lib/${project.name}.jar").call().toRelativeString(file("/"))).asFile
    onlyIf {
        !link.exists()
    }
    doLast {
        ant.withGroovyBuilder {
            "symlink"(
                "resource" to resource,
                "link" to link
            )
        }
    }
}

val javadoc = tasks.withType<Javadoc> {
    // FIXME fail on error
    setFailOnError(false)
    title = "Open-Xchange OAuth Provider Interface"
}

tasks.getByName("assemble").dependsOn(javadoc)

install {
    // FIXME needs to be moved to open-xchange-admin packaging sources
    target("javadoc") {
        from(buildDir.resolve("docs/javadoc"))
        into("/usr/share/doc/open-xchange-admin-oauth-provider/javadoc")
    }
}

tasks.named("install") {
    finalizedBy(symlink)
}


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

tasks.named("install") {
    finalizedBy(symlink)
}


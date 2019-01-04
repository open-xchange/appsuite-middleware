import com.openexchange.build.install.extension.InstallExtension
import com.openexchange.build.install.tasks.ReplaceRegexTask

pdeJava {
    installBundleAsDirectory.set(true)
}

val patchBundleManifest by tasks.existing(ReplaceRegexTask::class) {
    onlyIf { false }
}

val symlink = tasks.register("symlink") {
    val extension = project.extensions.getByType(InstallExtension::class.java)        
    val resource = extension.destDir.get().dir(extension.prefixResolve("etc/logback.xml").call().toRelativeString(file("/"))).asFile
    val link = extension.destDir.get().dir(extension.prefixResolve("bundles/${project.name}/logback.xml").call().toRelativeString(file("/"))).asFile
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


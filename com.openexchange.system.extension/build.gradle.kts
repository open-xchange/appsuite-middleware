import com.openexchange.build.install.extension.InstallExtension

configure<InstallExtension> {
    target("libs") {
        from("build/libs")
        include("*.jar")
        into(prefixResolve("bundles"))
    }
}

pdeJava {
    installBundleAsDirectory.set(true)
}

install {
    target("conf") {
        from(project.files("conf"))
        into(prefixResolve("etc"))
    }
    target("templates") {
        from(project.files("templates"))
        into(prefixResolve("templates"))
    }
}

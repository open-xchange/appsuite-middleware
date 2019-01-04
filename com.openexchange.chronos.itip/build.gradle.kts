install {
    target("templates") {
        from(project.files("templates"))
        into(prefixResolve("templates"))
    }
}
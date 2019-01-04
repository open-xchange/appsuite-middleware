install {
    target("conf") {
        from(project.files("conf"))
        into(prefixResolve("etc"))
    }
}

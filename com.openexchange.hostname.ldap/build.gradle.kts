install {
    patch("conf") {
        from("conf")
        into("conf")
    }
    target("conf") {
        from(buildDir.resolve("conf"))
        into(prefixResolve("etc"))
    }
}
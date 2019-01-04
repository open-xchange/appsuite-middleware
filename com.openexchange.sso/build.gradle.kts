install {
    target("libs") {
        from("build/libs")
        include("*.jar")
        into(prefixResolve("bundles"))
    }
}

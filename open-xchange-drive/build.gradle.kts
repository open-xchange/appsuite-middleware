packaging {
    copyright("server")
}

install {
    target("libs") {
        from("build/libs")
        include("*.jar")
        into(prefixResolve("bundles"))
    }
}


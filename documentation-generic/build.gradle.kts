install {
    target("configDocs") {
        from("config") {
            include("*.yml")
        }
        into(prefixResolve("documentation/etc"))
    }
}

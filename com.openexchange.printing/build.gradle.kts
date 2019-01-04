install {
    target("templates") {
        from("templates")
        into(prefixResolve("templates"))
    }
}

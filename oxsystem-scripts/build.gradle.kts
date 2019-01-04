install {
    target("oxfunctions") {
        from("lib")
        into(prefixResolve("lib"))
    }
    target("insufficientjava") {
        from("sbin")
        into(prefixResolve("sbin"))
    }
}


install {
    target("docs") {
        from("doc")
        into("/usr/share/doc/open-xchange-client-onboarding")
    }

    target("templates") {
        from(project.files("templates"))
        into(prefixResolve("templates"))
    }
}
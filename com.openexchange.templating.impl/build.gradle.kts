install {
    target("conf") {}
    patch("templating.properties") {
        from("conf")
        into("conf")
    }
    templates {
        register("@templatepath@") {
            buildConfig = com.openexchange.build.install.templating.BuildConfig.PATH
            path = "templates"
        }
    }
    target("templating.properties") {
        from(buildDir.resolve("conf"))
        into(prefixResolve("etc"))
    }
}

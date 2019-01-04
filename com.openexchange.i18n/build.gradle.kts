install {
    target("conf") {}
    patch("i18n.properties") {
        from("conf")
        into("conf")
    }
    templates {
        register("@i18nDir@") {
            buildConfig = com.openexchange.build.install.templating.BuildConfig.PATH
            path = "i18n"
        }
    }
    target("i18n.properties") {
        from(buildDir.resolve("conf"))
        into(prefixResolve("etc"))
    }
    target("i18n") {
        createEmptyDir(prefixResolve("i18n").call().absolutePath)
    }
}

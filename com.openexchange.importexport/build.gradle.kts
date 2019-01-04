install {
    target("conf") {}
    patch("import.properties") {
        from("conf")
        into("conf")
    }
    templates {
        register("@rootinstalldir@") {
            buildConfig = com.openexchange.build.install.templating.BuildConfig.PREFIX
        }
    }
    target("import.properties") {
        from(buildDir.resolve("conf"))
        into(prefixResolve("etc"))
    }
    target("importCSV") {
        from("importCSV")
        into(prefixResolve("importCSV"))
    }
}

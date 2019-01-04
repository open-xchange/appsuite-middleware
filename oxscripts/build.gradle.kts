install {
    patch("config.ini.template") {
        from("osgi")
        into("osgi")
    }
    templates {
        register("@logfile@") {
            buildConfig = com.openexchange.build.install.templating.BuildConfig.PATH
            path = "/var/log/open-xchange/open-xchange-osgi.log"
            relativeToPrefix = false
        }
    }
    target("config.ini.template") {
        from(buildDir.resolve("osgi"))
        into(prefixResolve("osgi"))
    }
}


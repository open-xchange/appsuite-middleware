dependencies {
    // TODO workaround for missing interface org.bouncycastle.util.Encodable needed for compilation
    compile(files("../com.openexchange.bundles/jars/bcprov-jdk15on-162.jar"))
}

install {
    target("conf") {
        from("conf") {
            exclude("ox-scriptconf.sh.in")
        }
        into(prefixResolve("etc"))
    }
    target("templates") {
        from("templates")
        into(prefixResolve("templates"))
    }
    patch("ox-scriptconf.sh") {
        from("conf") {
            include("ox-scriptconf.sh.in")
        }
        into("conf")
    }
    templates {
        register("@libDir@") {
            buildConfig = com.openexchange.build.install.templating.BuildConfig.PATH
            path = "lib"
        }
        register("@propertiesdir@") {
            buildConfig = com.openexchange.build.install.templating.BuildConfig.PATH
            path = "etc"
        }
        register("@oxgroupwaresysconfdir@") {
             buildConfig = com.openexchange.build.install.templating.BuildConfig.PREFIX
        }
    }
    target("ox-scriptconf.sh") {
        from(buildDir.resolve("conf"))
        into(prefixResolve("etc"))
    }
}

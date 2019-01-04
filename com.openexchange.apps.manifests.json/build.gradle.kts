install {
    target("conf") {
        from("conf") {
            exclude("*.in")
        }
        into(prefixResolve("etc"))
    }
    templates {
        // @prefix@ exists by default
    }
    patch("manifests.properties") {
        from("conf") {
            include("*.in")
        }
        into("conf")
    }
    target("manifests.properties") {
        from(buildDir.resolve("conf"))
        into(prefixResolve("etc"))
    }
}

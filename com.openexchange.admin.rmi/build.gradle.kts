val javadoc = tasks.withType<Javadoc> {
    // FIXME fail on error
    setFailOnError(false)
    title = "Open-Xchange Administration Interface"
}

tasks.getByName("assemble").dependsOn(javadoc)

install {
    // FIXME needs to be moved to open-xchange-admin packaging sources
    target("javadoc") {
        from(buildDir.resolve("docs/javadoc"))
        into("/usr/share/doc/open-xchange-admin/javadoc")
    }
}

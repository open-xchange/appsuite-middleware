packaging {
    copyright("server")
}

install {
    target("docs") {
        from("docs")
        into("/usr/share/doc/${project.name}/docs")
    }
}

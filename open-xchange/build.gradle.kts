packaging {
    sources {
        from("docs")
        into("docs")
    }
    upload {
        files.from("open-xchange.service")
        files.from("open-xchange.init")
    }
}

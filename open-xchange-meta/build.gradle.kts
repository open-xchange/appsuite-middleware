
packaging {
    copyright("server")
}

install {
    target("bla") {
        from(files("README.TXT"))
        into("/")
    }
}

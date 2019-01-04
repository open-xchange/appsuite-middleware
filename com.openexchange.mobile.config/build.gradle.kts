install {
    target("etc") {
        from("conf")
        into("/opt/open-xchange/etc/")
    }
}
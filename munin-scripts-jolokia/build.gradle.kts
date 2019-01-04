install {
    target("scripts") {
        from("ox_munin_scripts")
        into("usr/share/munin/plugins")
    }

    target("conf") {
        from("plugin-conf.d")
        into("/etc/munin/plugin-conf.d/")
    }
}
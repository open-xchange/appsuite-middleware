---
title: IMAP
icon: fa-inbox
---

With 7.10.4, advanced monitoring capabilities for IMAP have been introduced. While the default configuration is defined to keep the monitoring footprint small, output can be made more verbose.

The following lean properties have been introduced (`imap.properties`):

 * `com.openexchange.imap.metrics.enabled`: Enables/disables IMAP command monitoring. Default is "true". Set to "false" to disable. Not cascading, but reloadable.

 * `com.openexchange.imap.metrics.groupByPrimaryHosts`: If "true", commands against the primary mail account are tagged with their configured IMAP host name. This is useful in case of a limited set of different mail backend clusters with distinguishable host names. It might negatively affect resource consumption in case of many different host names though, as per each tag value a metric instance is held in memory and an according time series is published. If "false", the "host" tag is always set to "primary". Default is "true". Not cascading, but reloadable.

 * `com.openexchange.imap.metrics.groupByPrimaryEndpoints`: If "true", commands against the primary mail account are tagged with their resolved IP/port combination. This is useful to observe all primary mail backend IPs that are returned by DNS when resolving the IMAP host name. It might negatively affect resource consumption in case of many different returned IPs or many different primary host names though, as per each tag value a metric instance is held in memory and an according time series is published. If "false", "com.openexchange.imap.metrics.groupByPrimaryHosts" applies. Default is "false". Not cascading, but reloadable.

 * `com.openexchange.imap.metrics.measureExternalAccounts`: Controls whether commands against external mail accounts are also monitored. Default is "true". Set to "false" to disable. Not cascading, but reloadable.

 * `com.openexchange.imap.metrics.groupByExternalHosts`: If "true", commands against external mail accounts are tagged with their configured IMAP host name. This can be useful when debugging latency or other issues with external email services. Depending on the variety of external IMAP servers configured by users, this negatively affects resource consumption, as per each tag value a metric instance is held in memory and an according time series is published. If "false", the "host" tag is always set to "external". This setting is ignored in case of "com.openexchange.imap.metrics.measureExternalAccounts=false". Default is "true". Not cascading, but reloadable.

 * `com.openexchange.imap.metrics.groupByCommands`: If "true", command latencies and response status are tagged with the respective command key, if that matches a certain whitelist. Non-matching commands are aggregated under tag value "OTHER". If "false", the "cmd" tag is always set to "ALL". Default is "false". Not cascading, but reloadable.

 * `com.openexchange.imap.metrics.commandWhitelist`: If "groupByCommands" is "true", any command contained in this whitelist is measured as a separate value of tag "cmd". All commands that do not match the whitelist are aggregated as 'cmd="OTHER"'. Commands must be single words without whitespace. At runtime, "UID <cmd>" commands are matched without the UID prefix. I.e. "FETCH" and "UID FETCH" are both tagged with 'cmd="FETCH"'. Value is a comma-separated list of commands. Not cascading, but reloadable. Default value is "SELECT, EXAMINE, CREATE, DELETE, RENAME, SUBSCRIBE, UNSUBSCRIBE, LIST, LSUB, STATUS, APPEND, EXPUNGE, CLOSE, SEARCH, FETCH, STORE, COPY, SORT"

---
title: Snippet quota
---

Until middleware version 7.8.3 the size of snippets counted towards the quota of the filestore. For most cases this approach was suitable, but in some other scenarios it caused some troubles. For example when a user recieved some extra filestore space for a period of time (e.g. a pro trial) which timed out. In this scenario the user wasn't able to create any snippets (e.g. signatures) anymore if the usage was above his old limit. 

With middleware 7.8.4 this behaviour can now be changed. The middleware still stores the snippets within the filestore, but now it can use a dedicated quota for it instead. In order to active this quota you need to configure the following properties and restart the middleware.

```
com.openexchange.snippet.filestore.quota.mode=dedicated
com.openexchange.snippet.filestore.quota.perUserLimit=5MB
```

The first property activates the dedicated quota mode and the second one defines the quota limit for this quota. See [config docu](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#mode=search&term=com.openexchange.snippet.filestore.quota) for more informations about those properties.

Please note that the `com.openexchange.snippet.mime.groupware.SnippetSizeColumnUpdateTask` update task has to be run before the 'dedicated' quota mode can be activated.


When the quota is needed for the first time (any update/create snippete operation) the usage of snippets are calculated and stored within the `snippet` table. Please note that the usage of old snippets still applies to the filestore quota. If you changed the quota mode you need to run either the `checkconsistency` tool or better the new `recalculatefilestoreusage` command-line tool. Both tools are able to properly recalculate the filestore usage. The new tool let you recalculate single filestores, filestores in a context or all at once. You can even recalculate only context-filestores or only user-filestores.

```
usage: recalculatefilestoreusage [-c <context-id> [-u <user-id>] | --scope
                                 <scope>]
 -A,--adminuser <arg>         Admin username
 -c,--context <arg>           The identifier of the context for which the
                              file storage usage shall be recalculated. If
                              a user identifier is also specified, only
                              the user-associated file storage is
                              considered.
 -h,--help                    Prints a help text
 -P,--adminpass <arg>         Admin password
 -p,--port <arg>              The optional RMI port (default:1099)
    --responsetimeout <arg>   The optional response timeout in seconds
                              when reading data from server (default: 0s;
                              infinite)
 -s,--server <arg>            The optional RMI server (default: localhost)
    --scope <scope>           Scope can be either set to either 'all',
                              'context' or 'user'. If set to 'all', all
                              usages of all context and user file stores
                              are recalculated. Otherwise the usages of
                              either context- or user-associated file
                              storages are recalculated. Cannot be used in
                              conjunction with the '--context' or
                              '--user'.
 -u,--user <arg>              The identifier of the user for which the
                              file storage usage shall be recalculated or
                              'all' to recalculates the usages for all
                              user-associated file storage in the given
                              context.
The command-line tool to recalculate the usage of file storages
```







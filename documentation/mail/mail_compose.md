---
title: Mail Compose
icon: fa-envelope-open
tags: Mail, Configuration, Installation
---

# New mail compose
Starting with v7.10.2 the App Sutie Middleware ships with a refactored mail compose API. That API opens a new composition space whenever the user starts to write a new mail.

Such a composition space represents the current state of the mail that is supposed to be composed such as recipients, subject, content, etc.

## Mail headers and (textual) content

Mail headers and the textual content of a mail are stored in database in table `compositionSpace`.

That table offers individual columns for important mail headers such as `From`, `To`, `Cc`, `Bcc`, and `Subject`. But it does also hold meta-data for a mail that is needed to e.g. remember that a mail is a reply for a certain message.

Moreover, the textual content of a mail is stored in database. This could be plain text or HTML content. That associated column is of type `MEDIUMTEXT` and thus allows at max. approximately 16MB.

## Mail attachments

An important change is the feature of instant attachment uploads. In contrast to former mail compose API, attachments are now stored and held in used storage as long as associated composition space is alive.

As occupying space in storage is a crucial element for administrators/operators, there are a few relevant configuration options that influence composition spaces and used storage.

* ``com.openexchange.mail.compose.maxSpacesPerUser`` Specifies the max. number of concurrently opened composition spaces that are allowed for a single user. Default is ``20``. This configuration option is config-cascade aware and reloadable.
* ``com.openexchange.mail.compose.maxIdleTimeMillis`` Defines the time span in milliseconds a composition space may be idle before it gets deleted. If zero or less than zero is specified, composition spaces may be idle forever. Can contain units of measurement: D(=days) W(=weeks) H(=hours) M(=minutes). Default is ``1W`` (1 week). This configuration option is config-cascade aware and reloadable.

Those two configuration options specify how many composition spaces are allowed to be created and how long they are allowed to exist (when idling). To directly address storage possibilities, there two possible ways to operate:

### Default (context-associated) storage
By default, uploaded attachments are stored in context-associated file storage, but without affecting its quota. Meaning, attachments are managed in context-associated file storage regardless of any quota limitations that were specified during context provisioning. This is the appropriate choice for installations, in which the registered file storage has plenty of space and has not been space-wise restricted according to quota usages.

If the context-associated file storage is used, the ``checkconsistency`` command-line tool cares about orphaned/non-existent references.

### Dedicated storage
To have a dedicated file storage that is supposed to be used for uploaded attachments to not use the context-associated one, there is the opportunity to specify following configuration option:

* ``com.openexchange.mail.compose.fileStorageId`` Specifies the identifier for a dedicated file storage that is supposed to be used for mail compose attachments. By default that option empty, but config-cascade aware and reloadable.

#### Register a dedicated file storage

To register a dedicated file storage that is supposed to be solely used for mail compose attachments, please use the `registerfilestore` command-line tool.

To prevent the application from using that storage for contexts and/or users, ensure that the `-x|--maxcontexts` option is set to `0` (zero).

Example:

```
$ registerfilestore -A oxadminmaster -P secret -t file:///var/opt/filestore -s 2000000 -x 0
```

Registers a file-backed file storage mounted to `/var/opt/filestore` and a max. size of 2,000,000 MB (~ 2 TB) having max. number of entities set to `0`(zero). Thus it is not used for e.g. newly created contexts/users.

#### Ensure consistency of dedicated file storage

Once a dedicated file storage is used, the ``checkconsistency`` command-line tool is not effective for caring about orphaned/non-existent references. Instead, there is a dedicated tooling available:

```
deleteorphanedattachments -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server]
                                 [--responsetimeout <responseTimeout>] | [-h]
 -A,--adminuser <adminUser>       Admin username
 -f,--filestores <filestores>     Accepts a comma-separated value of one or more file storage identifiers
 -h,--help                        Prints this help text
 -p,--port <rmiPort>              The optional RMI port (default:1099)
 -P,--adminpass <adminPassword>   Admin password
    --responsetimeout <timeout>   The optional response timeout in seconds when reading data from server (default: 0s;
                                  infinite)
 -s,--server <rmiHost>            The optional RMI server (default: localhost)


The command-line tool for deleting orphaned references from mail compose for specified file storage identifiers
```



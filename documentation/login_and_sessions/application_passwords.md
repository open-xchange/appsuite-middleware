---
title: Application-specific Passwords
icon: fa-key
tags: Authentication, Session, Security
---

# Introduction

With version 7.10.4 App Suite introduces the ability to create passwords specific to client applications, such as Mobile Mail, CalDAV, and others. These passwords will have permissions limited to the requirements of the application. For example, passwords created for CalDAV would not have the ability to list a users inbox.

The Application Password implementation if fully customizable, displaying only the applications that the installer has chosen to support, as well as the custom list of permissions for each type of application.

# Limitations

Applications that were equipped with an application-specific password will use this password when authenticating requests against certain APIs of the server. That means that the interfaces the clients are using need to be enabled to support these kind of authentication explicitly. For applications that only access the App Suite middleware (like Drive synchronization clients, or a calendar client based on CalDAV), app-specific authentication is supported with a default database-backed authenticator plugin, or a customized version that takes care of managing app-specific passwords of the users separately. However, this does not cover scenarios where applications access client protocols that are not routed through the App Suite middleware, most prominently Mail clients using IMAP/SMTP/POP3. In order to support application-specific passwords for these protocols, too, a custom solution that tightly integrates with the authentication system of the platform needs to be used instead.  

Also, some special care needs to be taken whenever an application-password enabled client of the App Suite middleware performs operations which implicitly access subsystems that require a separate authentication. Therefore, different options are possible, see below for further details.  

# Installation & Configuration

The core functionality of the application password service is included in the `open-xchange-core` package. How these passwords are stored and validated, however, is implemented in a separate package. For the default database storage, the package `open-xchange-authentication-application-storage-rdb` needs to be installed.


## Application Types

The first thing to define is the list of available applications, each identified with a specific *type*, e.g. ``caldav``, ``carddav``, ``mail``, ``eas``, etc. The property ``com.openexchange.com.openexchange.authentication.application.appTypes`` defines these applications, as a comma separated list of available types. The used identifier types here are not displayed to the user. Example:

```
com.openexchange.authentication.application.appTypes=mail,caldav,carddav,drive,dav,eas
```

The property can be defined using the config-cascade. Users must have at least one application defined, otherwise the service will be disabled for that user.

## Application Details

The details and restrictions of the applications are then defined in a ``yml`` file. There is a sample yml shipped with the installation, `app-password-apps-template.yml` This file should contain the specifics of the various applications. These include the translatable display title, the permission scopes, and the order in which they are offered to the User. Example:

```
drive:
    displayName_t10e: Drive Sync App
    restrictedScopes: [read_drive,write_drive,read_folders,write_folders,read_files,write_files]
    sortOrder: 20
```

Within the middleware core, the following restricted scopes are predefined for the HTTP API modules:

- ``folders``: read_folders, write_folders
- ``mail`` / ``mailcompose``: read_mail, write_mail
- ``calendar`` / ``chronos``: read_calendar, write_calendar
- ``contacts``: read_contacts, write_contacts
- ``infostore`` / ``files`` / ``fileaccount`` / ``fileservice``: read_files, write_files
- ``drive``: read_drive, write_drive
- ``tasks``: read_tasks, write_tasks
- ``reminder``: read_reminder, write_reminder

The modules ``appPasswords`` and ``multifactor`` of the HTTP API are not available for applications with app-specific passwords; any other modules are not decorated with scopes and can be accessed independently of the restricted scopes associated with the application type.

For WebDAV interfaces, the following scopes are available:

- WebDAV access to InfoStore/Files: read_webdav, write_webdav
- General CalDAV/CardDAV: dav
- CalDAV: read_caldav, write_caldav
- CardDAV: read_carddav, write_carddav

Here, *read_webdav* / *write_webdav* are used to grant access to the user's Drive or Files module via WebDAV (at ``/servlet/webdav.infostore/`` or its aliases). Any CalDAV- and CardDAV-related endpoints (usually behind ``/servlet/dav/``) require the general scope *dav* for basic functionality, as well as the more concrete scopes *read_caldav*, *write_caldav* for CalDAV-, and *read_carddav*, *write_carddav*-specific actions.


## Blacklists

Application passwords should really not be used to log into the App Suite UI, as there will be failures due to the permission restrictions. A blacklist defines the client identifiers where application passwords should not be used.

```
com.openexchange.com.openexchange.authentication.application.blacklistedClients=open-xchange-appsuite,com.openexchange.ajax.framework.AJAXClient
```

## Enabling

Application passwords must be enabled globally by setting

```
com.openexchange.com.openexchange.authentication.application.enabled=true
```

Enabling/disabling at the user or context level is done by configuring the above `com.openexchange.com.openexchange.authentication.application.appTypes` setting.  Users must have at least one application defined.  If this is empty, then the service will not be enabled for that user.


# Application Password Storage

Storage and authentication of application specific passwords is provided by additional packages. By default, a database-backed storage is available named `open-xchange-authentication-application-storage-rdb`, but also other custom plugins are possible, targeting the authentication infrastructure of the installation, and/or further plugins for other types of client applications.

The following section shows the configuration options for the default database storage.

## Context Lookup

When a user logs in using an application password, the provided login name is used to derive the user's context identifier and associated database schema. How this is done can be configured with the setting `com.openexchange.authentication.application.storage.rdb.contextLookupNamePart`, which defines the part of the login name used as context lookup source.  This can be full, just the domain, or just the local-part:
```
com.openexchange.authentication.application.storage.rdb.contextLookupNamePart=domain
```

## Authentication against External Subsystems 

Applications that explicitly or implicitly access other external subsystem under the hood, most commonly the user's primary mail/transport account, will require proper authentication. Generally, the options are a global password, session password, or oauth, correlating with the configured value in ``com.openexchange.mail.passwordSource``.

A related use case where authentication against the mail transport server is regularly required are notification messages that are generated by the server for different purposes (e.g. invitations to shared folders, or calendar scheduling messages). Whenever suitable, the special *no-reply* transport account is preferred for sessions originating in the login with an application specific password, that are not explicitly equipped with the restricted scope `write_mail`. So, this no-reply account should be configured appropriately when using app-specific passwords.       

### Master Authentication

If the system is set up for global password, then no additional configuration is required.

### Session Password Authentication

If mail/transport authentication is based on the users main email/password, then the users regular password will need to be stored for use by the application passwords. The default storage package can do this, and will store the password in an encrypted format, only usable if the user properly authenticates with the application login.

To enable:

```
com.openexchange.authentication.application.storage.rdb.storeUserPassword=true
```

### OAuth Authentication

If the IMAP server uses OAuth tokens, then an additional package will need to be installed to perform authentication against an authentication server. This package registers an AppPasswordMailOauthService to get the required token.

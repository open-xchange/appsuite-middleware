```yaml
title: Application Passwords
icon: fab fa-mobile
tags: Application, Passwords, Configuration
```

# Introduction

With version 7.10.4 App Suite introduces the ability to create passwords specific to client applications, such as Mobile Mail, CalDav, and others. These passwords will have permissions limited to the requirements of the application. For example, passwords created for CalDav would not have the ability to list a users inbox.

The Application Password implementation if fully customizable, displaying only the applications that the installer has chosen to support, as well as the custom list of permissions for each type of application.

# Installation

The core functionality of the application password service is included in the `open-xchange-core` package. How these passwords are stored, however, is implemented in a separate package. For the default database storage, the package `open-xchange-authentication-application-storage-rdb` needs to be installed.

# Configuration

## Application Types

The first thing to define is the list of available applications. These can be applications such as caldav, carddav, mail, eas, etc. com.openexchange.com.openexchange.authentication.application.appTypes defines these applications, and is a comma separated list of available types. The naming here is not displayed to the user. Example:

```
com.openexchange.authentication.application.appTypes=mail,caldav,carddav,drive,dav,eas
```

Users must have at least one application defined, otherwise the service will be disabled for that user.

## Application Details

The details of the applications are then defined in a yml file. There is a sample yml shipped with the installation, `app-password-apps-template.yml` This file should contain the specifics of the various applications. These include the translatable display title, the permission scopes, and the order in which they are offered to the User. Example:

```
mail:
    displayName_t10e: Mail App
    restrictedScopes: [read_mail, write_mail, read_folder, write_folder, read_contacts, write_contacts]
    sortOrder: 10

caldav:
    displayName_t10e: Calendar Client (CalDAV)
    restrictedScopes: [dav,read_caldav,write_caldav]
    sortOrder: 20

carddav:
    displayName_t10e: Addressbook Client (CardDAV)
    restrictedScopes: [dav,read_carddav,write_carddav]
    sortOrder: 30

webdav:
    displayName_t10e: WebDAV Client
    restrictedScopes: [webdav, read_webdav, write_webdav]
    sortOrder: 40

eas:
    displayName_t10e: Exchange ActiveSync
    restrictedScopes: [read_mail, write_mail, read_folder, write_folder, read_contacts, write_contacts, read_calendar, write_calendar]
    sortOrder: 50
```

The scopes are predefined in the core, and include the following:

- Calendar: read_calendar, write_calendar
  
- Folders: read_folder, write_folder
  
- Mail: read_mail, write_mail
  
- Contacts: read_contacts, write_contacts
  
- Drive: read_drive, write_drive
  
- FileStorage: read_files, write_files
  
- Tasks: read_tasks, write_tasks
  
- Caldav: read_caldav, write_caldav, dav
  
- Carddav: read_carddav, write_carddav, dav
  

## Blacklists

Application passwords should really not be used to log into the Appsuite UI, as there will be failures due to the permission restrictions. A blacklist defines the useragents where application passwords should not be used.

```
com.openexchange.com.openexchange.authentication.application.blacklistedClients=open-xchange-appsuite,com.openexchange.ajax.framework.AJAXClient
```

## Enabling

Application passwords must be enabled globally by setting

```
com.openexchange.com.openexchange.authentication.application.enabled=true
```

Enabling/disabling at the user or context level is done by configuring the above `com.openexchange.com.openexchange.authentication.application.appTypes` setting.  Users must have at least one application defined.  If this is empty, then the service will not be enabled for that user.

## Application Password Storage

The default storage package `open-xchange-authentication-application-storage-rdb` requires some additional configuration

When a user logs in using an application password, the username is used to establish the users contextId. How this is done is defined with the setting `com.openexchange.authentication.application.storage.rdb.contextLookupNamePart` which defines the namepart of the context lookup source.  This can be full, just the domain, or just the local-part.

```
com.openexchange.authentication.application.storage.rdb.contextLookupNamePart=domain
```

# IMAP Password Configuration

Applications that access IMAP will require proper authentication against the IMAP server. How this is done depends on the system configuration. Generally, the options are a global password, session password, or oauth.

If the system is set up for global password, then no additional configuration is required

## IMAP session password authentication

If the IMAP authentication is based on the users main email/password, then the users regular password will need to be stored for use by the application passwords. The default storage package can do this, and will store the password in an encrypted format, only usable if the the user properly authenticates with the application login.

To enable:

```
com.openexchange.authentication.application.storage.rdb.storeUserPassword=true
```

## IMAP OAuth authentication

If the IMAP server uses oauth tokens, then an additional package will need to be installed to perform authentication against an authentication server. This package registers an AppPasswordMailOauthService to get the required token.
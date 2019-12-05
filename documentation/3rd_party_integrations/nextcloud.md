---
title: Nextcloud
icon: fab fa-cloud
tags: 3rd Party, nextcloud, Installation, Configuration, Filestore
---

Unlike oauth based file storages the Nextcloud integration works with basic authentication. Currently the user can configure his credentials himself and they are stored encrypted. The admin just needs to configure the appropriate capability for this user:

```
com.openexchange.capability.filestorage_nextcloud=true 
```

A client can trigger the account creation on behalf of the user by calling the _new_ action in the _fileaccount_ module (__fileaccount?action=new__). 
The _configuration_ object within the request body must contain the WebDAV root URL and the user's Nextcloud credentials.

For example:

```json
{
  "filestorageService": "nextcloud",
  "displayName": "My Nextcloud Account",
  "configuration": {
    "login": "username",
    "password": "password",
    "url": "http://example.com:8080/remote.php/dav/files/username"
  }
}
```

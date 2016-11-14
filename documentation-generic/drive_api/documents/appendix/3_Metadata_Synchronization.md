# Metadata Synchronization

Previously, only the "raw" folders and files were synchronized between server and clients. While this is sufficient for basic synchronization, there are cases where the clients could benefit from additional data - "metadata" - that is already available on the server. For example, clients could display directories that have been shared or published to other people in a different way. Or, clients could consider folder permissions directly in case the user is performing a local change that would be rejected by the server in the next synchronization cycle anyway.

To supply the clients with those additional information without any influence on the existing synchronization protocol (!), .drive-meta files are introduced for each synchronized directory. Regarding synchronization, such files are treated like any other ordinary file. Especially, those files are taken into account when it comes to directory checksum calculation. Doing so, metadata updates result in a changed .drive-meta file, which in turn causes the parent directory checksum to change, hence synchronization is triggered.

However, some special handling applies for those files:

* Clients are not allowed to change metadata, so modifications of metadata files or the deletion of them is rejected. Recovery is done via the protocol here, i.e. the client is instructed to re-download the file.
* .drive-meta files are actually not stored physically on the file storage backend, but created on the fly based on the actual metadata of the directory.
* Client applications may either store such files on the client file system, or evaluate and store the contained metadata information in a local database for later retrieval. If the file is not saved physically on the client (which is actually recommended), the client is responsible to consider the metadata file in a virtual way and include it's checksum for the directory checksum calculation - similar to the server's internal handling.

Note: Embedded metadata synchronization is enabled by default, but can be forcibly disabled by setting the driveMeta parameter to `false` in each request.

## Metadata format

The metadata in .drive-meta files is serialized in JSON format to allow easy processing at the clients. The following shows an example of the contents:

```
{
  "path": "/",
  "localized_name": "Meine Dateien",
  "own_rights": 403710016,
  "permissions": [{
    "bits": 403710016,
    "group": false,
    "entity": 182,
    "display_name": "Mander, Jens",
    "email_address": "jens.mander@example.com",
    "guest": false
  }],
  "shareable": true,
  "jump": ["permissions"],
  "files": [{
    "name": "Koala.jpg",
    "created": 1418024190565,
    "modified": 1418026995663,
    "created_by": {
      "group": false,
      "entity": 182,
      "display_name": "Mander, Jens",
      "email_address": "jens.mander@example.com",
      "guest": false
    },
    "modified_by": {
      "group": false,
      "entity": 182,
      "display_name": "Mander, Jens",
      "email_address": "jens.mander@example.com",
      "guest": false
    },
    "preview": "http://192.168.32.191/ajax/files?action=document&folder=268931&id=268931/297620&version=1&delivery=download&scaleType=contain&width=800&height=800&rotate=true",
    "thumbnail": "http://192.168.32.191/ajax/files?action=document&folder=268931&id=268931/297620&version=1&delivery=download&scaleType=contain&width=100&height=100&rotate=true",
    "object_permissions": [{
      "bits": 1,
      "group": false,
      "entity": 10,
      "display_name": "Klaus Mander",
      "email_address": "klaus.mander@example.com",
      "guest": false
    },
    {
      "bits": 1,
      "group": false,
      "entity": 8338,
      "email_address": "horst@example.com",
      "guest": true
    }],
    "shareable": true,
    "shared": true,
    "number_of_versions": 1,
    "version": "1",
    "jump": ["preview",
    "permissions",
    "version_history"]
  },
  {
    "name": "test.txt",
    "created": 1418024198520,
    "modified": 1418027394897,
    "created_by": {
      "group": false,
      "entity": 182,
      "display_name": "Mander, Jens",
      "email_address": "jens.mander@example.com",
      "guest": false
    },
    "modified_by": {
      "group": false,
      "entity": 182,
      "display_name": "Mander, Jens",
      "email_address": "jens.mander@example.com",
      "guest": false
    },
    "preview": "http://192.168.32.191/ajax/files?action=document&format=preview_image&folder=268931&id=268931/297621&version=6&delivery=download&scaleType=contain&width=800&height=800",
    "thumbnail": "http://192.168.32.191/ajax/files?action=document&format=preview_image&folder=268931&id=268931/297621&version=6&delivery=download&scaleType=contain&width=100&height=100",
    "locked": true,
    "shareable": true,
    "number_of_versions": 4,
    "version": "6",
    "version_comment": "Uploaded with OX Drive (TestDrive)",
    "versions": [{
      "name": "test.txt",
      "file_size": 23,
      "created": 1418024198520,
      "modified": 1418024202878,
      "created_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "modified_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "version": "1",
      "version_comment": "Uploaded with OX Drive (TestDrive)"
    },
    {
      "name": "test.txt",
      "file_size": 54,
      "created": 1418024234782,
      "modified": 1418024231522,
      "created_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "modified_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "version": "2",
      "version_comment": "Uploaded with OX Drive (TestDrive)"
    },
    {
      "name": "test.txt",
      "file_size": 120,
      "created": 1418027349026,
      "modified": 1418027355957,
      "created_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "modified_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "version": "5"
    },
    {
      "name": "test.txt",
      "file_size": 127,
      "created": 1418027370051,
      "modified": 1418027366945,
      "created_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "modified_by": {
        "group": false,
        "entity": 182,
        "display_name": "Mander, Jens",
        "email_address": "jens.mander@example.com",
        "guest": false
      },
      "version": "6",
      "version_comment": "Uploaded with OX Drive (TestDrive)"
    }],
    "jump": ["preview",
    "edit",
    "permissions",
    "version_history"]
  },
  {
    "name": "Kalimba.mp3",
    "created": 1418026529047,
    "modified": 1247549551659,
    "created_by": {
      "group": false,
      "entity": 182,
      "display_name": "Mander, Jens",
      "email_address": "jens.mander@example.com",
      "guest": false
    },
    "modified_by": {
      "group": false,
      "entity": 182,
      "display_name": "Mander, Jens",
      "email_address": "jens.mander@example.com",
      "guest": false
    },
    "preview": "http://192.168.32.191/ajax/image/file/mp3Cover?folder=268931&id=268931/297623&version=1&delivery=download&scaleType=contain&width=800&height=800",
    "thumbnail": "http://192.168.32.191/ajax/image/file/mp3Cover?folder=268931&id=268931/297623&version=1&delivery=download&scaleType=contain&width=100&height=100",
    "shareable": true,
    "number_of_versions": 1,
    "version": "1",
    "version_comment": "Uploaded with OX Drive (TestDrive)",
    "jump": ["preview",
    "permissions",
    "version_history"]
  }]
}

```

The following objects describe the JSON structure of the metadata for a directory:

### Directory Metadata
|Name|Type|Value|
|:---|:---|:----|
|id|String|The server-side unique identifier of the directory.|
|localized_name|String|The localized display name of the directory, if different from the physical name.|
|checksum|String|The directory's checksum. Only set if metadata is not retrieved through Metadata Synchronization.|
|own_rights|Number|Folder permissions which apply to the current user, as described in HTTP API (Permission Flags).|
|permissions|Array|All folder permissions, each element is an object as described in HTTP API (Permission Object).|
|extended_permissions|Array|All folder permissions including some additional information, each element is an object as described in HTTP API (Extended Permission Object).|
|default_folder|Boolean|`true` if the folder is a default folder, `false` or not set, otherwise.|
|has_subfolders|Boolean|`true` if the folder (potentially) has subfolders, `false` or not set, otherwise.|
|shared|Boolean|`true` if the folder is shared, `false` or not set, otherwise.|
|shareable|Boolean|`true` if the folder can be shared to others by the user, `false` or not set, otherwise.|
|not_synchronizable|Boolean|`true` if the folder is exluded from synchronization, `false` or not set, otherwise.|
|type|Number|The special folder type, or not set, if not available.|
|jump|Array|An array containing the names of possible jump methods to use for the folder.|
|files|Array|Metadata for the contained files, each element is an object as described in File Metadata. Only set if metadata is retrieved through Metadata Synchronization.|


### File Metadata

|Name|Type|Value|
|:---|:---|:----|
|name|String|The name of the file the metadata belongs to.|
|checksum|String|The file's checksum. Only set if metadata is not retrieved through Metadata Synchronization.|
|path|String|The path of the parent directory. Only set if metadata is not retrieved through Metadata Synchronization.|
|created|Timestamp|The file's last modification time (always UTC, not translated into user time).|
|modified|Timestamp|The file's last modification time (always UTC, not translated into user time).|
|created_by|Number|User ID of the user who created this object.|
|modified_by|Number|User ID of the user who last modified this object.|
|content_type|String|The content type of the file.|
|preview|String|A URL to a preview image for the file.|
|thumbnail|String|A URL to a thumbnail image for the file.|
|object_permissions|Array|All file permissions, each element is an object as described in HTTP API (Object Permission Object).|
|extended_object_permissions|Array|All file permissions including some additional information, each element is an object as described in HTTP API (Extended Object Permission Object).|
|shared|Boolean|`true` if the file is shared, `false` or not set, otherwise.|
|shareable|Boolean|`true` if the file can be shared to others by the user, `false` or not set, otherwise.|
|locked|Boolean|`true` if the file is locked, `false` or not set, otherwise.|
|jump|Array|An array containing the names of possible jump methods to use for the file.|
|number_of_versions|Number|The number of all versions of the file.|
|version|String|The current version identifier (usually, but not necessarily a numerical value) of the file.|
|version_comment|String|An additional comment for the file version.|
|versions|Array|Metadata for all versions of the file, each element is an object as described in File Version.|

### File Version
|Name|Type|Value|
|:---|:---|:----|
|name|String|The name of the file version.|
|file_size|Number|The file size of the version in bytes.|
|created|Timestamp|The file version's last modification time (always UTC, not translated into user time).|
|modified|Timestamp|The file version's last modification time (always UTC, not translated into user time).|
|created_by|Number|User ID of the user who created this object.|
|modified_by|Number|User ID of the user who last modified this object.|
|version|String|The version identifier (usually, but not necessarily a numerical value) of the file version.|
|version_comment|String|An additional comment for the file version.|

## Client-side implementation

In order to make use of the metadata, clients should roughly implement the following:

* Include the apiVersion parameter in each request, and set it to at least 3 in order to include .drive-meta during synchronization
* Evaluate .drive-meta files and store the information, as well as the file's checksums in a local database
* Include this file in the calculation of the parent directory checksum, just like an ordinary file in that directory
* Do something useful with the metadata information.

## Additional notes

* The metadata synchronization via .drive-meta files embedded into the synchronization protocol obsoletes the previously used methods to receive metadata information ([fileMetadata](#Drive_getFileMetadata) and columns parameter in [syncFiles](#Drive_syncFiles)).
* Depending on the underlying file storage backend, the included metadata may vary, so each information should be treatened as optional.
* Embedded metadata synchronization is enabled by default, but can be forcibly disabled by setting the driveMeta parameter to `false` in each request.

## Possible use cases

* For files where the locked property is `true`, display some kind of "lock" icon (-overlay) in the file list / explorer view
* For files or folders where the shared property is `true`, display some kind of "cloud" icon (-overlay) in the file list / explorer view
* For files or folders where the user is not allowed to perform an action with, don't offer such actions (e.g. if a file cannot be deleted or renamed by the user due to insufficient permissions, disable the corresponding options)
* Use the URLs in preview and thumbnail to get a preview image for the files
* Display the server creation / last modification timestamps of files and folders
* Embed a version history for files with multiple versions
* Show to which users a file or folder is currently shared
* Offer appropriate "jump" actions to the groupware web interface for more advanced options (e.g. to directly edit an .xlsx file in the spreadsheet application of the web interface, or to manage a folder's permission

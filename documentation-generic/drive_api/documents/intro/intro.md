# Introduction

This page describes the http api of the ox drive module.    

The module `drive` is used to synchronize files and folders between server and client, using a server-centric approach to allow an easy implementation on the client-side.  

The synchronization is based on checksums for files and folders, differences between the server- and client-side are determined using a three-way comparison of server, client and previously acknowledged file- and directory-versions. The synchronization logic is performed by the server, who instructs the client with a set of actions that should be executed in order to come to a synchronized state.  

Therefore, the client takes a snapshot of it's local files and directories, calculates their checksums, and sends them as a list to the server, along with a list of previously acknowledged checksums. The server takes a similar snapshot of the files and directories on the underlying file storages and evaluates which further actions are necessary for synchronization. After executing the server-side actions, the client receives a list of actions that should be executed on the client-side. These steps are repeated until the server-state matches the client-state.  

Key concept is that the synchronization works stateless, i.e. it can be interrupted and restarted at any time, following the eventual consistency model.  

Entry point for the synchronization is the [syncfolders](#Drive_syncFolders) request, where the directories are compared, and further actions are determined by the server, amongst others actions to synchronize the files in a specific directory using the [syncfiles](#Drive_syncFiles) request. After executing the actions, the client should send another `syncfolders` request to the server and execute the returned actions (if present), or finish the synchronization if there are no more actions to execute. In pseudo-code, the synchronization routine could be implemented as follows:

```
WHILE TRUE
{
  response = SYNCFOLDERS()
  IF 0 == response.actions.length
    BREAK
  ELSE
    EXECUTE(response.actions)
}

```

Basically, it's up to the client how often such a synchronization cycle is initiated. For example, he could start a new synchronization cycle after a fixed interval, if he recognizes that the client directories have changed, or if he is informed that something has changed on the server by an event. It's also up to the client to interrupt the synchronization cycle at any time during execution of the actions and continue later on, however, it's recommended to start a new synchronization cycle each time to avoid possibly outdated actions.


## API

As part of the HTTP API, the basic conventions for exchanging messages described there are also valid for this case, especially the [HTTP API (low level protocol)](https://documentation.open-xchange.com/components/middleware/http-api/develop/index.html?version=develop) and [HTTP API (error handling)](https://documentation.open-xchange.com/components/middleware/http-api/develop/index.html?version=develop). Each request against the Drive API assumes a valid server session that is uniquely identified by the session id and the corresponding cookies and are sent with each request. A new session can be created via the [HTTP API (login module)](https://documentation.open-xchange.com/components/middleware/http-api/develop/index.html?version=develop#/Login).  

The root folder plays another important role for the message exchange. The root folder has a unique identifier. It is the parent server folder for the synchronization. All path details for directories and files are relative to this folder. This folder's id is sent with each request. To select the root folder during initial client configuration, the client may get a list of synchronizable folders with the [subfolders](#Drive_getSynchronizableFolders) action.  

Subsequently all transferred objects and all possible actions are listed.

### File Version

A file in a directory is uniquely identified by its filename and the checksum of its content.


|Name|Type|Value|
|:---|:---|:----|
|name|String|The name of the file, including its extension, e.g. `test.doc`.|
|checksum|String|The MD5 hash of the file, expressed as a lowercase hexadecimal number string, 32 characters long, e.g. f8cacac95379527cd4fa15f0cb782a09.|

### Directory Version

A directory is uniquely identified by its full path, relative to the root folder, and the checksum of its content.


|Name|Type|Value|
|:---|:---|:----|
|path|String|The path of the directory, including the directory's name, relative to the root folder, e.g. `/sub/test/letters`.|
|checksum|String|	The MD5 hash of the directory, expressed as a lowercase hexadecimal number string, 32 characters long, e.g. `f8cacac95379527cd4fa15f0cb782a09`.|

Note: the checksum of a directory is calculated based on its contents in the following algorithm:

* Build a list containing each file in the directory (not including subfolders or files in subfolders)
* Ensure a lexicographically order in the following way:
    * Normalize the filename using the `NFC` normalization form (canonical decomposition, followed by canonical composition) - see [http://www.unicode.org/reports/tr15/tr15-23.html](http://www.unicode.org/reports/tr15/tr15-23.html) for details
    * Encode the filename to an array of UTF-8 unsigned bytes (array of codepoints)
    * Compare the filename (encoded as byte array "fn1") to another one "fn2" using the following comparator algorithm:
```
min_length = MIN(LENGTH(fn1), LENGTH(fn2))
FOR i = 0; i < min_length; i++
{
  result = fn1[i] - fn2[i]
  IF 0 != result RETURN result
}
RETURN LENGTH(fn1) - LENGTH(fn2)
```
* Calculate the aggregated MD5 checksum for the directory based on each file in the ordered list:
    * Append the file's NFC-normalized (see above) name, encoded as UTF-8 bytes
    * Append the file's MD5 checksum string, encoded as UTF-8 bytes


### Actions

All actions are encoded in the following format. Depending on the action type, not all properties may be present.

|Name|Type|Value|
|:---|:---|:----|
|action|String|The type of action to execute, currently one of `acknowledge`, `edit`, `download`, `upload`, `remove`, `sync`, `error`.|
|version|Object|The (original) file- or directory-version referenced by the action.|
|newVersion|Object|The (new) file- or directory-version referenced by the action.|
|path|String|The path to the synchronized folder, relative to the root folder.|
|root|String|The corresponding root folder identifier (optional, available since API version 5).|
|offset|Number|The requested start offset in bytes for file uploads.|
|totalLength|Number|The total length in bytes for file downloads.|
|contentType|String|The file's content type for downloads (deprecated, available until API version 2).|
|created|Timestamp|The file's creation time (always UTC, not translated into user time).|
|modified|Timestamp|The file's last modification time (always UTC, not translated into user time).|
|error|Object|The error object in case of synchronization errors.|
|quarantine|Boolean|The flag to indicate whether versions need to be excluded from synchronization.|
|reset|Boolean|The flag to indicate whether locally stored checksums should be invalidated.|
|stop|Boolean|The flag to signal that the client should stop the current synchronization cycle.|
|acknowledge|Boolean|The flag to signal if the client should not update it's stored checksums when performing an `EDIT` action.|
|thumbnailLink|String|A direct link to a small thumbnail image of the file if available (deprecated, available until API version 2).|
|previewLink|String|A direct link to a medium-sized preview image of the file if available (deprecated, available until API version 2).|
|directLink|String|A direct link to the detail view of the file in the web interface (deprecated, available until API version 2).|
|directLinkFragments|String|The fragments part of the direct link (deprecated, available until API version 2).|


The following list gives an overview about the used action types:

#### acknowledge

Acknowledges the successful synchronization of a file- or directory version, i.e., the client should treat the version as synchronized by updating the corresponding entry in its metadata store and including this updated information in all following `originalVersions` arrays of the `syncfiles` / `syncfolders` actions. Depending on the `version` and `newVersion` parameters of the action, the following acknowledge operations should be executed (exemplarily for directory versions, file versions are acknowledged in the same way):

**Example 1:** Acknowledge a first time synchronized directory  
The server sends an `acknowledge` action where the newly synchronized directory version is encoded in the `newVersion` parameter. The client should store the version in his local checksum store and send this version in the `originalVersions` array in upcoming `syncfolders` requests.

~~~{json}
{
  "action" : "acknowledge",
  "newVersion" : {
      "path" : "/",
      "checksum" : "d41d8cd98f00b204e9800998ecf8427e"
  }
}
~~~

**Example 2:** Acknowledge a synchronized directory after updates  
The server sends an `acknowledge` action where the previous directory version is encoded in the `version`, and the newly synchronized directory in the `newVersion` parameter. The client should replace any previously stored entries of the directory version in his local checksum store with the updated version, and send this version in the `originalVersions` array in upcoming `syncfolders` requests.

~~~{json}
{
  "action" : "acknowledge",
  "newVersion" : {
    "path" : "/",
    "checksum" : "7bb1f1a550e9b9ab4be8a12246f9d5fb"
  },
  "version" : {
    "path" : "/",
    "checksum" : "d41d8cd98f00b204e9800998ecf8427e"
  }
}
~~~

**Example 3:** Acknowledge the deletion of a previously synchronized directory  
The server sends an `acknowledge` where the `newVersion` parameter is set to `null` to acknowledge the deletion of the previously synchronized directory version as found in the `version` parameter. The client should remove any stored entries for this directory from his local checksum store, and no longer send this version in the `originalVersions` array in upcoming `syncfolders` requests.
Note that an acknowledged deletion of a directory implicitly acknowledges the deletion of all contained files and subfolders, too, so the client should also remove those `originalVersions` from his local checksum store.

~~~{json}
{
  "action" : "acknowledge",
  "version" : {
    "path" : "/test",
    "checksum" : "3525d6f28eb8cb30eb61ab7932367c35"
  }
}
~~~

#### edit

Instructs the client to edit a file- or directory version. This is used for move/rename operations. The `version` parameter is set to the version as sent in the `clientVersions` array of the preceding `syncfiles`/`syncfolders` action. The `newVersion` contains the new name/path the client should use. Unless the optional boolean parameter `acknowledge` is set to `false` an `edit` action implies that the client updates its known versions store accordingly, i.e. removes the previous entry for `version` and adds a new entry for `newVersion`. When editing a directory version, the client should implicitly take care to create any not existing subdirectories in the `path` of the `newVersion` parameter. A concurrent client-side modification of the file/directory version can be detected by the client by comparing the current checksum against the one in the passed `newVersion` parameter.

**Example 1:** Rename a file  
The server sends an `edit` action where the source file is encoded in the `version`, and the target file in the `newVersion` parameter. The client should rename the file identified by the `version` parameter to the name found in the `newVersion` parameter. Doing so, the stored checksum entry for the file in `version` should be updated, too, to reflect the changes.

~~~{json}
{
  "path" : "/",
  "action" : "edit",
  "newVersion" : {
    "name" : "test_1.txt",
    "checksum" : "03395a94b57eef069d248d90a9410650"
  },
  "version" : {
    "name" : "test.txt",
    "checksum" : "03395a94b57eef069d248d90a9410650"
  }
}
~~~

**Example 2:** Move a directory  
The server sends an edit action where the source directory is encoded in the version, and the target directory in the newVersion parameter. The client should move the directory identified by the version parameter to the path found in the newVersion parameter. Doing so, the stored checksum entry for the directory in version should be updated, too, to reflect the changes.

~~~{json}
{
  "action" : "edit",
  "newVersion" : {
    "path" : "/test2",
    "checksum" : "3addd6de801f4a8650c5e089769bdb62"
  },
  "version" : {
    "path" : "/test1/test2",
    "checksum" : "3addd6de801f4a8650c5e089769bdb62"
  }
}
~~~

**Example 3:** Rename a conflicting file  
The server sends an `edit` action where the original client file is encoded in the `version`, and the target filename in the `newVersion` parameter. The client should rename the file identified by the `version` parameter to the new filename found in the `newVersion` parameter. If the `acknowledge` parameter is set to `true` or is not set, the stored checksum entry for the file in `version` should be updated, too, to reflect the changes, otherwise, as in this example, no changes should be done to the stored checksums.

~~~{json}
{
  "action" : "edit",
  "version" : {
    "checksum" : "fade32203220752f1fa0e168889cf289",
    "name" : "test.txt"
  },
  "newVersion" : {
    "checksum" : "fade32203220752f1fa0e168889cf289",
    "name" : "test (TestDrive).txt"
  },
  "acknowledge" : false,
  "path" : "/"
}
~~~

#### download

Contains information about a file version the client should download. For updates of existing files, the previous client version is supplied in the `version` parameter. For new files, the `version` parameter is omitted. The `newVersion` holds the target file version, i.e. filename and checksum, and should be used for the following `download` request. The `totalLength` parameter is set to the file size in bytes, allowing the client to recognize when a download is finished. Given the supplied checksum, the client may decide on its own if the target file needs to be downloaded from the server, or can be created by copying a file with the same checksum to the target location, e.g. from a trash folder. The file's content type can be retrieved from the `contentType` parameter, similar to the file's creation and modification times that are available in the `created` and `modified` parameters.  

**Example 1:** Download a new file  
The server sends a `download` action where the file version to download is encoded in the `newVersion` paramter. The client should download and save the file as indicated by the `name` property of the `newVersion` in the directory identified by the supplied `path`. After downloading, the `newVersion` should be added to the client's known file versions database.

~~~{json}
{
  "totalLength" : 536453,
  "path" : "/",
  "action" : "download",
  "newVersion" : {
    "name" : "test.pdf",
    "checksum" : "3e0d7541b37d332c42a9c3adbe34aca2"
  },
  "contentType" : "application/pdf",
  "created" : 1375276738232,
  "modified" : 1375343720985
}
~~~

**Example 2:** Download an updated file  
The server sends a `download` action where the previous file version is encoded in the `version`, and the file version to download in the `newVersion` parameter. The client should download and save the file as indicated by the `name` property of the `newVersion` in the directory identified by the supplied `path`, replacing the previous file. After downloading, the `newVersion` should be added to the client's known file versions database, replacing an existing entry for the previous version.

~~~{json}
{
  "totalLength" : 1599431,
  "path" : "/",
  "action" : "download",
  "newVersion" : {
    "name" : "test.pdf",
    "checksum" : "bb198790904f5a1785d7402b0d8c390e"
  },
  "contentType" : "application/pdf",
  "version" : {
    "name" : "test.pdf",
    "checksum" : "3e0d7541b37d332c42a9c3adbe34aca2"
  },
  "created" : 1375276738232,
  "modified" : 1375343720985
}
~~~

#### upload

Instructs the client to upload a file to the server. For updates of existing files, the previous server version is supplied in the `version` parameter, and should be used for the following `upload` request. For new files, the `version` parameter is omitted. The `newVersion` holds the target file version, i.e. filename and checksum, and should be used for the following `upload` request. When resuming a previously partly completed upload, the `offset` parameter contains the offset in bytes from which the file version should be uploaded by the client. If possible, the client should set the `contentType` parameter for the uploaded file, otherwise, the content type falls back to `application/octet-stream`.

#### remove

Instructs the client to delete a file or directory version. The `version` parameter contains the version to delete. A deletion also implies a removal of the corresponding entry in the client's known versions store. A concurrent client-side modification of the file/directory version can be detected by comparing the current checksum against the one in the passed `version` parameter.  

**Example 1:** Remove a file  
The server sends a `remove` action where the file to be removed is encoded as `version` parameter. The `newVersion` parameter is not set in the action. The client should delete the file identified by the `version` parameter. A stored checksum entry for the file in `version` should be removed, too, to reflect the changes. The `newVersion` parameter is not set in the action.

~~~{json}
{
  "path" : "/test2",
  "action" : "remove",
  "version" : {
    "name" : "test.txt",
    "checksum" : "03395a94b57eef069d248d90a9410650"
  }
}
~~~

**Example 2:** Remove a directory  
The server sends a `remove` action where the directory to be removed is encoded as `version` parameter. The `newVersion` parameter is not set in the action. The client should delete the directory identified by the `version` parameter. A stored checksum entry for the directory in `version` should be removed, too, to reflect the changes.

~~~{json}
{
  "action" : "remove",
  "version" : {
    "path" : "/test1",
    "checksum" : "d41d8cd98f00b204e9800998ecf8427e"
  }
}
~~~

#### sync

The client should trigger a synchronization of the files in the directory supplied in the `version` parameter using the `syncfiles` request. A `sync` action implies the client-side creation of the referenced directory if it not yet exists, in case of a new directory on the server.

If the `version` parameter is not specified, a synchronization of all folders using the `syncfolders` request should be initiated by the client.

If the `reset` flag in the `SYNC` action is set to `true`, the client should reset his local state before synchronizing the files in the directory. This may happen when the server detects a synchronization cycle, or believes something else is going wrong. Reset means that the client should invalidate any stored original checksums for the directory itself and any contained files, so that they get re-calculated upon the next synchronization. If the `reset` flag is set in a `SYNC` action without a specific directory version, the client should invalidate any stored checksums, so that all file- and directory-versions get re-calculated during the following synchronizations.

**Example 1:** Synchronize folder  
The server sends a `sync` action with a version. The client should trigger a `syncfiles` request for the specified folder.

~~~{json}
{
  "action": "sync",
  "version": {
    "path": "<folder>",
    "checksum": "<md5>"
  }
}
~~~

**Example 2:** Synchronize all folders  
The server sends a `sync` action without version (or version is //`null`//). The client should trigger a `syncfolder` request, i.e. the client should synchronize all folders.

~~~{json}
{
  "action": "sync",
  "version": null
}
~~~

#### error

With the `error` action, file- or directory versions causing a synchronization problem can be identified. The root cause of the error is encoded in the `error` parameter as described at the [HTTP API (error handling)](https://documentation.open-xchange.com/components/middleware/http-api/develop/index.html?version=develop).

Basically, there are two scenarios where either the errorneous version affects the synchronization state or not. For example, a file that was deleted at the client without sufficient permissions on the server can just be downloaded again by the client, and afterwards, client and server are in-sync again. On the other hand, e.g. when creating a new file at the client and this file can't be uploaded to the server due to missing permissions, the client is out of sync as long as the file is present. Therefore, the boolean parameter `quarantine` instructs the client whether the file or directory version must be excluded from the synchronization or not. If it is set to `true`, the client should exclude the version from the `clientVersions` array, and indicate the issue to the enduser. However, if the synchronization itself is not affected and the `quarantine` flag is set to `false`, the client may still indicate the issue once to the user in the background, e.g. as a balloontip notification.

The client may reset it's quarantined versions on it's own, e.g. if the user decides to "try again", or automatically after a configurable interval.

The server may also decide that further synchronization should be suspended, e.g. in case of repeated synchronization problems. Such a situation is indicated with the parameter `stop` set to true. In this case, the client should at least cancel the current synchronization cycle. If appropriate, the client should also be put into a 'paused' mode, and the user should be informed accordingly.

There may also be situations where a error or warning is sent to the client, independently of a file- or directory version, e.g. when the client version is outdated and a newer version is available for download.

The most common examples for errors are insufficient permissions or exceeded quota restrictions, see examples below.

**Example 1:** Create a file in a read-only folder  
The server sends an `error` action where the errorneous file is encoded in the `newVersion` parameter and the `quarantine` flag is set to `true`. The client should exclude the version from the `clientVersions` array in upcoming `syncFiles` requests so that it doesn't affect the synchronization algorithm. The error message and further details are encoded in the `error` object of the action.

~~~{json}
{
  "error" : {
    "category" : 3,
    "error_params" : ["/test"],
    "error" : "You are not allowed to create files at \"/test\"",
    "error_id" : "1358320776-69",
    "categories" : "PERMISSION_DENIED",
    "code" : "DRV-0012"
  },
  "path" : "/test",
  "quarantine" : true,
  "action" : "error",
  "newVersion" : {
    "name" : "test.txt",
    "checksum" : "3f978a5a54cef77fa3a4d3fe9a7047d2"
  }
}
~~~

**Example 2:** Delete a file without sufficient permissions  
Besides a new `download` action to restore the locally deleted file again, the server sends an `error` action where the errorneous file is encoded in the `version` parameter and the `quarantine` flag is set to `false`. Further synchronizations are not affected, but the client may still inform the user about the rejected operation. The error message and further details are encoded in the `error` object of the action.

~~~{json}
{
  "error" : {
    "category" : 3,
    "error_params" : ["test.png", "/test"],
    "error" : "You are not allowed to delete the file \"test.png\" at \"/test\"",
    "error_id" : "1358320776-74",
    "categories" : "PERMISSION_DENIED",
    "code" : "DRV-0011"
  },
  "path" : "/test",
  "quarantine" : false,
  "action" : "error",
  "newVersion" : {
    "name" : "test.png",
    "checksum" : "438f06398ce968afdbb7f4db425aff09"
  }
}
~~~

**Example 3:** Upload a file that exceeds the quota  
The server sends an `error` action where the errorneous file is encoded in the `newVersion` parameter and the `quarantine` flag is set to `true`. The client should exclude the version from the `clientVersions` array in upcoming `syncFiles` requests so that it doesn't affect the synchronization algorithm. The error message and further details are encoded in the `error` object of the action.

~~~{json}
{
  "error" : {
    "category" : 3,
    "error_params" : [],
    "error" : "The allowed Quota is reached",
    "error_id" : "-485491844-918",
    "categories" : "PERMISSION_DENIED",
    "code" : "DRV-0016"
  },
  "path" : "/",
  "quarantine" : true,
  "action" : "error",
  "newVersion" : {
    "name" : "test.txt",
    "checksum" : "0ca6033e2a9c2bea1586a2984bf111e6"
  }
}
~~~

**Example 4:** Synchronize with a client where the version is no longer supported.  
The server sends an `error` action with code DRV-0028 and an appropriate error message. The `stop` flag is set to `true` to interrupt the synchronization cycle.

~~~{json}
{
  "stop" : true,
  "error" : {
    "category" : 13,
    "error_params" : [],
    "error" : "The client application you're using is outdated and no longer supported - please upgrade to a newer version.",
    "error_id" : "103394512-13",
    "categories" : "WARNING",
    "code" : "DRV-0028",
    "error_desc" : "Client outdated - current: \"0.9.2\", required: \"0.9.10\""
  },
  "quarantine" : false,
  "action" : "error"
}
~~~

**Example 5:** Synchronize with a client where a new version of the client application is available.  
The server sends an `error` action with code DRV-0029 and an appropriate error message. The `stop` flag is set to `false` to indicate that the synchronization can continue.

~~~{json}
{
  "stop" : false,
  "error" : {
    "category" : 13,
    "error_params" : [],
    "error" : "A newer version of your client application is available for download.",
    "error_id" : "103394512-29",
    "categories" : "WARNING",
    "code" : "DRV-0029",
    "error_desc" : "Client update available - current: \"0.9.10\", available: \"0.9.12\""
  },
  "quarantine" : false,
  "action" : "error"
}
~~~

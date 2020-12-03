---
title: Drive folder permission mode on folder move
icon: fa-folder-plus
tags: Configuration
---

# Configuration

With OX App Suite 7.10.4, several modes were introduced defining the behaviour on Drive folder move. This mode can be set for every folder tree separately via three new properties:

 - `com.openexchange.folderstorage.permissions.moveToPublic` - sets the mode for moving folders within or into the public folder tree
 - `com.openexchange.folderstorage.permissions.moveToPrivate` - sets the mode for moving folders within or into the private folder tree
 - `com.openexchange.folderstorage.permissions.moveToShared` - sets the mode for moving folders within or into the shared folder tree

Possible modes are one of

 - `merge` - Merge permissions from moved folder and new parent folder
 - `inherit` - Inherit folder from new parent folder, previous permissions from moved folder are dropped
 - `keep` - Keep permissions from moved folder as is, don't change anything

where `inherit` is the default for all three properties. Please be aware that when merging two permissions for the same user results in the permissions with more rights to win. E.g. when merging a viewer and and author permission for the same user the author permissions wins.

# Examples

Assuming User1 does move operations in the following Drive folder tree:

```
My files
 - Folder 1
 - Folder 2
 - Folder 3

Public files
 - Public folder 1
 - Public folder 2

Shared files
 - User2
    - Shared folder 1
```

where User1 has administrative permission for folders `Folder 1`, `Folder 2` and `Folder 3` and Author permission for `Public folder 1`, `Public folder 2` and `Shared folder 1`.  
User2 has administrative permission for folder `Shared folder 1`, Reviewer permission for `Folder 1`, Viewer permission for `Folder 2` and `Folder 3` and Author permission for `Public folder 1`.  
And all users have viewer permission for public folders and `Folder 3`.  
_Note_: In this scenario the public folders have to be created by a third user who granted Author permission to User1 and User2 for those folders. Apart from that the third user is not required and won't be mentioned in this scenario for simplification.

## Private folders

### `com.openexchange.folderstorage.permissions.moveToPrivate=merge`
When moving `Folder 2` into `Folder 1` and `com.openexchange.folderstorage.permissions.moveToPrivate` set to `merge`, `Folder 2` has following permissions after the move operation:

 - User1 has administrative permission for both folders, no permission change
 - User2 had Reviewer permission for `Folder 1` and Viewer permission for `Folder 2`. Merging results in Reviewer permission for `Folder 2`

### `com.openexchange.folderstorage.permissions.moveToPrivate=inherit`
When moving `Folder 1` into `Folder 2` and `com.openexchange.folderstorage.permissions.moveToPrivate` set to `inherit`, `Folder 1` has following permissions after the move operation:

 - User1 has administrative permission for both folders, no permission change
 - User2 had Reviewer permission for `Folder 1` and Viewer permission for `Folder 2`. Inheriting `Folder 2`'s permission lead to Viewer permission for `Folder 1`

### `com.openexchange.folderstorage.permissions.moveToPrivate=keep`
When moving `Folder 1` into `Folder 2` and `com.openexchange.folderstorage.permissions.moveToPrivate` set to `keep`, `Folder 1` has following permissions after the move operation:

 - User1 has administrative permission for both folders, no permission change
 - User2 had Reviewer permission for `Folder 1`, no permission change

## Public folders

### `com.openexchange.folderstorage.permissions.moveToPublic=merge`
When moving `Folder 1` into `Public folder 1` and `com.openexchange.folderstorage.permissions.moveToPublic` set to `merge`, `Folder 1` has following permissions after the move operation:

 - User1 keeps administrative permission for `Folder 1`
 - User2 had Reviewer permission for `Folder 1`, merge with Author permission for `Public folder 1` results in Author permission for `Folder 1`
 - Viewer permission for group 'AllUsers' is unchanged

### `com.openexchange.folderstorage.permissions.moveToPublic=inherit`
When moving `Folder 1` into `Public folder 2` and `com.openexchange.folderstorage.permissions.moveToPublic` set to `inherit`, `Folder 1` has following permissions after the move operation:

 - User1 keeps administrative permission for `Folder 1`
 - User2 had Reviewer permission for `Folder 1` and no permissions for `Public folder 2`, so User2's permissions for `Folder 1` are dropped
 - Viewer permission for group 'AllUsers' is unchanged

### `com.openexchange.folderstorage.permissions.moveToPublic=keep`
When moving `Folder 1` into `Public folder 2` and `com.openexchange.folderstorage.permissions.moveToPublic` set to `keep`, `Folder 1` has following permissions after the move operation:

 - User1 keeps administrative permission for `Folder 1`
 - User2 keeps Reviewer permission for `Folder 1`
 - Viewer permission for group 'AllUsers' is unchanged

## Shared folders

### `com.openexchange.folderstorage.permissions.moveToShared=merge`
When moving `Folder 3` into `Shared folder 1` and `com.openexchange.folderstorage.permissions.moveToShared` set to `merge`, `Folder 3` has following permissions after the move operation:

 - User1 keeps administrative permission for `Folder 3`
 - User2 gains administrative permission for `Folder 3`
 - Viewer permission for group 'AllUsers' is unchanged

### `com.openexchange.folderstorage.permissions.moveToShared=inherit`
When moving `Folder 3` into `Shared folder 1` and `com.openexchange.folderstorage.permissions.moveToShared` set to `inherit`, `Folder 3` has following permissions after the move operation:

 - User1 keeps administrative permission for `Folder 3`
 - User2 gains administrative permission for `Folder 3`
 - Viewer permission for group 'AllUsers' is dropped

### `com.openexchange.folderstorage.permissions.moveToShared=keep`
When moving `Folder 3` into `Shared folder 1` and `com.openexchange.folderstorage.permissions.moveToShared` set to `keep`, `Folder 3` has following permissions after the move operation:

 - User1 keeps administrative permission for `Folder 3`
 - User2 keeps Viewer permission for `Folder 3`
 - Viewer permission for group 'AllUsers' is unchanged

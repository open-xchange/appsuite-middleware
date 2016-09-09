# Flags and bitmasks

## Permission flags

<div class="simpleTable">

| Bits       | Value                   |
|:-----------|:------------------------|
| **0\-6**   | **Folder permissions:** |
|            | 0 (no permissions) |
|            | 1 (see the folder) |
|            | 2 (create objects in folder) |
|            | 4 (create subfolders) |
|            | 64 (all permissions) |
| **7\-13**  | **Read permissions for objects in folder:** |
|            | 0 (no permissions) |
|            | 1 (read only own objects) |
|            | 2 (read all objects) |
|            | 64 (all permissions) |
| **14\-20** | **Write permissions for objects in folder:** |
|            | 0 (no permissions) |
|            | 1 (modify only own objects) |
|            | 2 (modify all objects) |
|            | 64 (all permissions) |
| **21\-27** | **Delete permissions for objects in folder:** |
|            | 0 (no permissions) |
|            | 1 (delete only own objects) |
|            | 2 (delete all objects) |
|            | 64 (all permissions) |
| **28**     | **Admin flag:** |
|            | 0 (no permissions) |
|            | 1 (every operation modifying the folder in some way requires this permission (e.g. changing the folder name) |
</div>

## Capabilities

| Bit        | Description             |
|:-----------|:------------------------|
|0 | Mailing system supports permissions.|
|1 | Mailing system supports ordering mails by their thread reference.|
|2 | Mailing system supports quota restrictions.|
|3 | Mailing system supports sorting.|
|4 | Mailing system supports folder subscription.|

Note: Capabilities describe the entire mailing system (mail account), not the specific folder in which they are transmitted. E.g. bit 4 of the capabilities on the user's inbox describes whether subscriptions are supported by the default account, even though the inbox itself cannot be unsubscribed because it's a standard folder.


## Standard Folder Types

| Bit        | Description             |
|:-----------|:------------------------|
|0 | No default folder.|
|1 | Task.|
|2 | Calendar.|
|3 | Contact.|
|7 | Inbox.|
|8 | Infostore.|
|9 | Drafts.|
|10 | Sent.|
|11 | Spam.|
|12 | Trash.|


## Supported Capabilities

|Name        | Description   |
|:-----------|:--------------|
|permissions | Folder storage supports permissions.|
|publication | Folder storage supports folder publication.|
|quota | Folder storage supports quota restrictions.|
|sort | Folder storage supports sorting.|
|subscription | Folder storage supports folder subscription.|

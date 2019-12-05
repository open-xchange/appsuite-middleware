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
| **29**     | **User flag support:** (for Mail folder only)|
|            | 0 (no user flag support) |
|            | 1 (user flag support) |
| **30**     | **Rename support:** (for Mail folder only)|
|            | 0 (not allowed to rename) |
|            | 1 (allowed to rename) |
</div>

## Capabilities

<div class="simpleTable">

| Bit        | Description             |
|:-----------|:------------------------|
|0 | Mailing system supports permissions.|
|1 | Mailing system supports ordering mails by their thread reference.|
|2 | Mailing system supports quota restrictions.|
|3 | Mailing system supports sorting.|
|4 | Mailing system supports folder subscription.|

</div>

Note: Capabilities describe the entire mailing system (mail account), not the specific folder in which they are transmitted. E.g. bit 4 of the capabilities on the user's inbox describes whether subscriptions are supported by the default account, even though the inbox itself cannot be unsubscribed because it's a standard folder.


## Standard Folder Types

<div class="simpleTable">

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

</div>

## Supported Capabilities

<div class="simpleTable">

|Name        | Description   |
|:-----------|:--------------|
|permissions | Folder storage supports permissions.|
|publication | Folder storage supports folder publication. Deprecated with v7.10.2!|
|quota | Folder storage supports quota restrictions.|
|sort | Folder storage supports sorting.|
|subscription | Folder storage supports folder subscription.|

</div>

## Event Flags

<div class="simpleTable">

|Name        | Description   |
|:-----------|:--------------|
|attachments | The event contains at least one attachment.|
|alarms | The calendar user has at least one alarm associated with the event.|
|scheduled | Event is a <i>group-scheduled</i> meeting with an organizer.|
|organizer | The calendar user is the <i>organizer<i> of the meeting.|
|organizer_on_behalf | The calendar user is the <i>organizer<i> of the meeting, and the current user acts on behalf of him.|
|attendee | The calendar user is <i>attendee<i> of the meeting.|
|attendee_on_behalf | The calendar user is <i>attendee<i> of the meeting, and the current user acts on behalf of him.|
|private | Event is classified <i>private</i>, so is invisible for others. |
|confidential | Event is classified as <i>confidential</i>, so only start and end time are visible for others.|
|transparent | Event is <i>transparent</i> for the calendar user, i.e. invisible to free/busy time searches.|
|event_tentative | Indicates that the event's overall status is <i>tentative</i>.|
|event_confirmed | Indicates that the event's overall status is <i>definite</i>.|
|event_cancelled | Indicates that the event's overall status is <i>cancelled</i>.|
|needs_action | The calendar user's participation status is <i>needs action</i>. |
|accepted | The calendar user's participation status is <i>accepted</i>.|
|declined | The calendar user's participation status is <i>declined</i>.|
|tentative | The calendar user's participation status is <i>tentative</i>.|
|delegated | The calendar user's participation status is <i>delegated</i>.|
|series | The event represents the <i>master</i> of a recurring event series, or an expanded (regular) occurrence of a series. |
|overridden | The event represents an exception / overridden instance of a recurring event series.|
|first_occurrence | The event represents the <i>first</i> occurrence of a recurring event series.|
|last_occurrence | The event represents the <i>last</i> occurrence of a recurring event series.|

</div>
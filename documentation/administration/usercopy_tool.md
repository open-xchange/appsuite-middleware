---
title: User Copy Tool
icon: fas fa-clone
tags: Administration, User
---

General
=======

The user copy tool was introduced to copy a user along with his private data to another context. The tool is very limited in its functionality and not usually supposed to be used as part of normal user liefecycle management. Typically one would remove the former user afterwards, after verifying data completeness and integrity. For the same reason, that data completeness and integrity might neither fulfill expectations nor is fully given due to a defect, the tool does not perform any data deletion on its own.


Installation
------------

The feature is divided into an administration plugin package, a SOAP interface and a command line tool.

* The plugin is contained in package `open-xchange-admin-user-copy`. The package provides an RMI interface and the `usercopy` CLI. See its [manpage](../command_line_tools/user/usercopy.html) for synopsis.
* The SOAP interface is contained in `open-xchange-admin-soap-usercopy`. It provides a service endpoint `/webservices/OXUserCopyService`. WSDL can be obtained under `/webservices/OXUserCopyService?wsdl`.


Prerequisites and Assumptions
=============================

* A target context must exist upfront.
* Each copied folder and item including the user object itself get assigned new unique IDs within the target context.
* The primary email account is not touched at all but user-specific access properties are copied over. I.e. the copied user accesses the very same primary email account as the source user.
* Data of any feature not mentioned in this document is typically not preserved. Anything that is not explicitely mentioned as being preserved under "Copied Data" is currently out of scope for the tool.


Limitations
===========

* Users assigned to a per-user filestore of a different user (foreign filestore owner) cannot be copied.
* Users having unified quota enabled cannot be copied.
* Any copied data that consumed quota in the source context will consume quota in the target context. It must be ensured that enough quota is available before copying or the process will terminate with an error. 
* Many App Suite UI settings and remembered states will not survive the copy roundtrip. Technically this refers to anything stored in "JSlob" (DB table `jsonStorage`). This data is solely managed by App Suite UI and has no server-side semantics. As it relies on folder and item IDs of the source context, it cannot be properly adjusted during copying and is therefore not copied at all.
* Any share links the user created will break after deletion of the user from the source contet. As long as both user entities (the one in the old context and the newly copied one) exist, share links will point to the old account.
* User accounts are not automatically locked when starting the copy process. The system administrator needs to take care about the user not being active during the process.
* No data from public folders is ever copied. Any items or folders created by the user below "Public files" or any public folder from other modules are not considered.
* No data from shared folders is ever copied. Any items or folders created by the user below a folder shared by another user are not considered.
* External accounts from modules other than Mail, that have been added by the user directly (e.g. an added Google Drive integration), are not preserved.
* In case of unexpected errors during the copy operation, uncommitted changes in the destination database are rolled back automatically. However, the destination filestore may need to be cleaned up manually if some data was already copied, e.g. using the `checkconsistency` commandline tool.


Copied Data
===========

## User

The user itself along with its contact data.

### Settings

Only a small subset of web UI settings will survive the copy roundtrip, namely everything stored in database tables `user_attribute`, `user_setting`, `user_setting_mail` and `user_setting_server`.

**Any JSLob data, which is the main settings storage for App Suite UI, will not survive the roundtrip. See limitations.**


## Folders

Only private folders are copied. These are folders that the user had created himself or were created on his behalf during creation (e.g. "Contacts" or "My calendar").

Any additional permissions on folders are not copied. The resulting folders of the user only have that very user set as owner and no other permission assigned.

**Any sharing relationships with internal or external users within the source context will not be reflected in the target context. Any public or invitation share links to folders of that user will stop working after the user has been deleted from the source context.**


### Calendar

Besides the user's primary calendar account configuration, all further calendar accounts are copied, yet without any cached calendar data that would be retrieved upon the first usage.

For the internal default account, all private calendar folders and the contained appointments are transferred. This includes the event data itself, alarms and attendees. Attachments are implicitly taken over, too.   

Calendar users associated with the appointments (either as organizer or attendee) are exchanged, so that individual calendar users other than the copied user are effectively converted to external calendar users. Group- and resource attendees are silently discarded.


### Contacts

All contacts and distribution lists from private address book folders are copied. The "Collected addresses" folder is also copied including its contents.

Every contact has a per-user "use-count" assigned to rank frequently used contacts higher in auto-complete search result sets. This use-count is also copied, restoring the contact relevance within the target context.


### Tasks

All tasks from private task folders are copied. Task participants are preserved. If a task participant refers to a user from the source context, it is converted into an external participant. In that case, the participant's email address and display name are preserved. Note that display name refers to the provisioned display name, this might differ from the actually displayed name within App Suite UI.


### PIM Attachments

Any file attachments of contacts, appointments or tasks are copied into the target context filestore.

Attachments might have originally been created/uploaded by users different from the copied user. They will be copied in any case and have their creator set to the copied user within the target context. Any information about the original creator is lost.

**It must be ensured that enough quota is available. If quota gets exceeded during copying over the files, the user copy operation is aborted and rolled back.**


### Drive

Files and folders of the user including and below "My files" are copied. Versions of files are preserved.

**Any shared links to files of that user will stop working after the user has been deleted from the source context.**


### External Accounts

#### Email

External mail accounts are copied including unified inbox configuration.

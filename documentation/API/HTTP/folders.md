Module "folders"
================

The folders module is used to access the OX folder structure.


Data transport objects
----------------------

### Common folder data

| ID  | Name               | Type      | Value                                                                                                                                                                                         |
|-----|--------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1   | id                 | String    | Object ID                                                                                                                                                                                     |
| 2   | created_by         | String    | User ID of the user who created this object.                                                                                                                                                  |
| 3   | modified_by        | String    | User ID of the user who last modified this object.                                                                                                                                            |
| 4   | creation_date      | Time      | Date and time of creation.                                                                                                                                                                    |
| 5   | last_modified      | Time      | Date and time of the last modification.                                                                                                                                                       |
| 6   | last_modified_utc  | Timestamp | Timestamp of the last modification. Note that the type is Timestamp, not Time. See [Date and time](#Date_and_time "wikilink") for details. (added 2008-10-17, with SP5, temporary workaround) |
| 20  | folder_id          | String    | Object ID of the parent folder.                                                                                                                                                               |


### Detailed folder data

| ID   | Name                                        | Type             | Value |
|------|---------------------------------------------|------------------|-------|
| 300  | title                                       | String           | Name of this folder. |
| 301  | module                                      | String           | Name of the module which implements this folder; e.g. "tasks", "calendar", "contacts", "infostore", or "mail". |
| 302  | type                                        | Number           | Folder type ID as below in folder types. |
| 304  | subfolders                                  | Boolean          | true if this folder has subfolders. |
| 305  | own_rights                                  | Number or String | Permissions which apply to the current user, as described either in permission flags or in RFC 2086. |
| 306  | permissions                                 | Array            | Each element is an object described in permission object. |
| 307  | summary                                     | String           | Information about contained objects. |
| 308  | standard_folder                             | Boolean          | Indicates whether or not folder is marked as a default folder (only OX folder) |
| 309  | total                                       | Number           | The number of objects in this Folder. |
| 310  | new                                         | Number           | The number of new objects in this Folder. |
| 311  | unread                                      | Number           | The number of unread objects in this Folder. |
| 312  | deleted                                     | Number           | The number of deleted objects in this Folder. |
| 313  | capabilities                                | Number           | Bit mask containing information about mailing system capabilites, as described in capabilities. |
| 314  | subscribed                                  | Boolean          | Indicates whether this folder should appear in folder tree or not. **Note:** Standard folders cannot be unsubscribed. |
| 315  | subscr_subflds                              | Boolean          | Indicates whether subfolders should appear in folder tree or not. |
| 316  | standard_folder_type                        | Number           | Indicates the default folder type. Zero for non-default folder. See standard folder types. |
| 317  | supported_capabilities                      | Array            | Each element is a String identifying a supported folder capability as described in supported capabilities. Only applicable for non-mail folders. Read Only, Since 7.4.0. |
| 3010 | com.openexchange.publish.publicationFlag    | Boolean          | Indicates whether this folder is published. Read Only, provided by the com.openexchange.publish plugin, since 6.14. |
| 3020 | com.openexchange.subscribe.subscriptionFlag | Boolean          | Indicates whether this folder has subscriptions storing their content in this folder. Read Only, provided by the com.openexchange.subscribe plugin, since 6.14. |
| 3030 | com.openexchange.folderstorage.displayName  | String           | Provides the display of the folder's owner. Read Only, Since 6.20. |
| 3060 | com.openexchange.share.extendedPermissions  | Array            | Each element is an object described in extended permission object. Read Only, Since 7.8.0. |


### Folder types

| ID  | Value |
|-----|-------|
| 1   | private |
| 2   | public  |
| 3   | shared |
| 5   | system folder |
| 7   | This type is no more in use (legacy type). Will be removed with a future update! |
| 16  | trash                                                                            |
| 20  | pictures                                                                         |
| 21  | documents                                                                        |
| 22  | music                                                                            |
| 23  | videos                                                                           |
| 24  | templates |


### Permission flags

<table>
 <tbody>
  <tr>
   <th>Bits</th>
   <th>Value</th>
  </tr>
  <tr>
   <td>0-6</td>
   <td>Folder permissions:
    <table>
     <tbody>
      <tr>
       <td>0</td>
       <td> No permissions.</td>
      </tr>
      <tr>
       <td>1</td>
       <td>See the folder.</td>
      </tr>
      <tr>
       <td>2</td>
       <td>Create objects in the folder. <b>Note</b>: <b>Does not apply to folders of module <i>system</i></b>.</td>
      </tr>
      <tr>
       <td>4</td>
       <td>Create subfolders.</td>
      </tr>
      <tr>
       <td>64</td>
       <td>All permissions. This is currently the same as "Create subfolders" but in the future additional permissions may be added that will be given to the user when using this value.</td>
      </tr>
     </tbody>
    </table>
    <p>The values are scalars and not bit sets. Any other than the described values should not be used. If they are used expect an exception from the backend. Every value automatically contains the access rights covered by lower values.<br><b>NOTE</b>: <i>Create objects in the folder</i> is not covered by <i>Create subfolders</i> if folder's module is <i>system</i>.</p>
   </td>
  </tr>
  <tr>
   <td>7-13</td>
   <td>Read permissions for objects in the folder:
    <table>
     <tbody>
      <tr>
       <td>0</td>
       <td>No permissions.</td>
      </tr>
      <tr>
       <td>1</td>
       <td>Read only own objects.</td>
      </tr>
      <tr>
       <td>2</td>
       <td>Read all objects.</td>
      </tr>
      <tr>
       <td>64</td>
       <td>All permissions. This is currently the same as "Read all objects" but in the future additional permissions may be added that will be given to the user when using this value.</td>
      </tr>
     </tbody>
    </table>
    <p>The values are scalars and not bit sets. Any other than the described values should not be used. If they are used expect an exception from the backend. Every value automatically contains the access rights covered by lower values.</p>
   </td>
  </tr>
  <tr>
   <td>14-20</td>
   <td>Write permissions for objects in the folder:
   <table>
    <tbody>
     <tr>
      <td>0</td>
      <td>No permissions.</td>
     </tr>
     <tr>
      <td>1</td>
      <td>Modify only own objects.</td>
     </tr>
     <tr>
      <td>2</td>
      <td>Modify all objects.</td>
     </tr>
     <tr>
      <td>64</td>
      <td>All permissions. This is currently the same as "Modify all objects" but in the future additional permissions may be added that will be given to the user when using this value.</td>
     </tr>
    </tbody>
   </table>
   <p>The values are scalars and not bit sets. Any other than the described values should not be used. If they are used expect an exception from the backend. Every value automatically contains the access rights covered by lower values.</p>
  </td>
 </tr>
 <tr>
  <td>21-27</td>
  <td>Delete permissions for objects in the folder:
   <table>
    <tbody>
     <tr>
      <td>0</td>
      <td>No permissions.</td>
     </tr>
     <tr>
      <td>1</td>
      <td>Delete only own objects.</td>
     </tr>
     <tr>
      <td>2</td>
      <td>Delete all objects.</td>
     </tr>
     <tr>
      <td>64</td>
      <td>All permissions. This is currently the same as "Delete all objects" but in the future additional permissions may be added that will be given to the user when using this value.</td>
     </tr>
    </tbody>
   </table>
   <p>The values are scalars and not bit sets. Any other than the described values should not be used. If they are used expect an exception from the backend. Every value automatically contains the access rights covered by lower values.</p>
  </td>
 </tr>
 <tr>
  <td>28</td>
  <td>Admin flag:
   <table>
    <tbody>
     <tr>
      <td>0</td>
      <td>No permissions.</td>
     </tr>
     <tr>
      <td>1</td>
      <td>Every operation modifying the folder in some way requires this permission. This are e.g. changing the folder name, modifying the permissions, deleting or moving the folder.</td>
     </tr>
    </tbody>
   </table>
  </td>
 </tr>
</tbody>
</table>


### Permission object

| Name            | Type    | Value                                                                                                                                                            |
|-----------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| bits            | Number  | For non-mail folders, a number as described in permission flags.                                                                                                 |
| rights          | String  | For mail folders, the rights string as defined in RFC 2086.                                                                                                      |
| entity          | Number  | User ID of the user or group to which this permission applies (ignored for type "anonymous" or "guest").                                                         |
| group           | Boolean | true if entity refers to a group, false if it refers to a user (ignored for type "anonymous" or "guest").                                                        |
| type            | String  | The recipient type, i.e. one of "guest", "anonymous" (required if no internal "entity" defined).                                                                 |
| password        | String  | An additional secret / pin number an anonymous user needs to enter when accessing the share (for type "anonymous", optional) .                                   |
| email_address   | String  | The e-mail address of the recipient (for type "guest").                                                                                                          |
| display_name    | String  | The display name of the recipient (for type "guest", optional).                                                                                                  |
| contact_id      | String  | The object identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", optional).                         |
| contact_folder  | String  | The folder identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", required if "contact\_id" is set). |
| expiry_date     | Time    | The end date / expiration time after which the share link is no longer accessible (for type "guest" or "anonymous", optional).                                   |


### Extended permission object

| Name          | Type   | Value                                                                                                                     |
|---------------|--------|---------------------------------------------------------------------------------------------------------------------------|
| entity        | Number | Identifier of the permission entity (i.e. user-, group- or guest-ID).                                                     |
| bits          | Number | A number as described in permission flags.                                                |
| type          | String | "user" for an internal user, "group" for a group, "guest" for a guest, or "anonymous" for an anonymous permission entity. |
| display_name  | String | A display name for the permission entity.                                                                                 |
| contact       | Object | A (reduced) set of detailed contact data for "user" and "guest" entities.             |
| share_url     | String | The share link for "guest" and "anonymous" entities.                                                                      |
| password      | String | The optionally set password for "anonymous" entities.                                                                     |
| expiry_date   | Date   | The optionally set expiry date for "anonymous" entities.                                                                  |


### Capabilities

| Bit | Description                                                       |
|-----|-------------------------------------------------------------------|
| 0   | Mailing system supports permissions.                              |
| 1   | Mailing system supports ordering mails by their thread reference. |
| 2   | Mailing system supports quota restrictions.                       |
| 3   | Mailing system supports sorting.                                  |
| 4   | Mailing system supports folder subscription.                      |

**Note**: Capabilities describe the entire mailing system (mail account), not the specific folder in which they are transmitted. E.g. bit 4 of the capabilities on the user's inbox describes whether subscriptions are supported by the default account, even though the inbox itself cannot be unsubscribed because it's a standard folder.


### Standard folder types

| Bit | Description        |
|-----|--------------------|
| 0   | No default folder. |
| 1   | Task.              |
| 2   | Calendar.          |
| 3   | Contact.           |
| 7   | Inbox.             |
| 8   | Infostore.         |
| 9   | Drafts.            |
| 10  | Sent.              |
| 11  | Spam.              |
| 12  | Trash.             |


### Supported capabilities

| Name         | Description                                  |
|--------------|----------------------------------------------|
| permissions  | Folder storage supports permissions.         |
| publication  | Folder storage supports folder publication.  |
| quota        | Folder storage supports quota restrictions.  |
| sort         | Folder storage supports sorting.             |
| subscription | Folder storage supports folder subscription. |


### Change notifications

Change notifications can be sent out to permission entities of folders. This is currently possible on create and update actions. The notifications object is defined as follows:

| Name      | Type   | Value |
|-----------|--------|-------|
| transport | String | Currently only "mail" is supported. |
| message   | String | (Optional) A personal message to be contained in the notification. |


Special system folders
----------------------

Folders with some kind of special.

| ID  | Type     | Description  |
|-----|----------|--------------|
| 6   | contacts | System Users |


Get root folders
----------------

`GET /ajax/folders?action=root`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `columns` – A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in common folder data and detailed folder data.
-   `tree` – The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.

Response: An array with data for all folders at the root level of the folder structure. Each array element describes one folder and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.


Get subfolders
--------------

`GET /ajax/folders?action=list`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `parent` – Object ID of a folder, which is the parent folder of the requested folders.
-   `columns` – A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in common folder data and detailed folder data.
-   `all` – Set to `1` to list even not subscribed folders.
-   `tree` – The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.
-   `errorOnDuplicateName` – An optional flag to enable or disable (default) check for duplicate folder names within returned folder response (since v6.20.1). If a duplicate folder name is detected, an appropriate error is returned.

Response with timestamp: An array with data for all folders, which have the folder with the requested object ID as parent. Each array element describes one folder and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.


Get path
--------

`GET /ajax/folders?action=path`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `id` – Object ID of a folder.
-   `columns` – A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in common folder data and detailed folder data.
-   `tree` – (Preliminary) The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.

Response with timestamp: An array with data for all parent nodes until root folder. Each array element describes one folder and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.


Get updated folders
-------------------

`GET /ajax/folders?action=updates`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `parent` – Object ID of a folder, which is the parent folder of the requested folders.
-   `timestamp` – Timestamp of the last update of the requested folders.
-   `ignore` (optional) – Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.
-   `columns` – A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in common folder data and detailed folder data.
-   `tree` – (Preliminary) The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.

Response with timestamp: An array with data for new, modified and deleted folders. New and modified folders are represented by arrays. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter. Deleted folders (should the `ignore` parameter be ever implemented) would be identified by their object IDs as plain strings, without being part of a nested array.


Get a folder
------------

`GET /ajax/folders?action=get`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `id` – Object ID of the requested folder.
-   `tree` – (Preliminary) The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.

Response with timestamp: An object containing all data of the requested folder. The fields of the object are listed in common folder data and detailed folder data. The field id is not present. Since OX access controls are folder-based, the folder object also defines the permissions for the objects it contains. The permissions for a given user or group are defined by the object described in permission object. The format of the actual permissions depends on the type of the folder. The permissions of mail folders are transmitted as a rights string as defined in section 3 of RFC 2086. Permissions of all other folders are transmitted as a single nonnegative integer number. The permissions for any given action on the folder or on contained objects is defined by a group of bits in the binary representation of this number. Each group of bits is interpreted as a separate number. Zero always means "no permissions". Any other values add new permissions and always include the permissions of all lower values. The individual values are described in permission flags.


Update a folder
---------------

`PUT /ajax/folders?action=update`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `id` – Object ID of the updated folder.
-   `timestamp` – Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail.
-   `tree` – (Preliminary) The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.
-   `cascadePermissions` – (Optional. Defaults to false) Flag to cascade permissions to all sub-folders. The user must have administrative permissions to all sub-folders subject to change. If one permission change fails, the entire operation fails. (Since 7.8.0)

Request body: Folder object as described in common folder data and detailed folder data. Only modified fields are present. It is possible to let added permission entities be notified about newly shared folders. In that case you need to provide the folder data as an object "folder" and add a "notification" object beside it:

```json
{
  "folder":{
    "permissions":[
      {
        "bits":403710016,
        "entity":84,
        "group":false
      },
      {
        "type":"guest",
        "email_address":"john.doe@example.com",
        "display_name":"John Doe",
        "bits":257
      }
    ]
  },
  "notification":{
    "transport":"mail",
    "message":"Hi!\nHave a look at this!"
  }
}
```


Create a folder
---------------

`PUT /ajax/folders?action=new`

Parameters:

-   `folder_id` – The parent folder of the newly created folder
-   `session` – A session ID previously obtained from the login module.
-   `tree` – (Preliminary) The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.

Request body: Folder object as described in common folder data and detailed folder data. The field id should not be present. It is possible to let added permission entities be notified about newly shared folders. In that case you need to provide the folder data as an object "folder" and add a "notification" object beside it:

```json
{
  "folder":{
    "permissions":[
      {
        "bits":403710016,
        "entity":84,
        "group":false
      },
      {
        "type":"guest",
        "email_address":"john.doe@example.com",
        "display_name":"John Doe",
        "bits":257
      }
    ]
  },
  "notification":{
    "transport":"mail",
    "message":"Hi!\nHave a look at this!"
  }
}
```

Provided that permission is granted to create a folder, its module is bound to the limitation, that the new folder's module must be equal to parent folder's module except that:

-   Parent folder is one of the system folders `private`, `public`, or `shared`. Below these folders task, calendar, and contact modules are permitted.
-   Parent folder's module is one of task, calendar, or contact. Below this kind of folders task, calendar, and contact modules are permitted.

Response: Object ID of the newly created folder.


Delete folders
--------------

`PUT /ajax/folders?action=delete`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `timestamp` – The optional timestamp of the last update of the deleted folders.
-   `tree` – (Preliminary) The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.
-   `hardDelete` - Optional, defaults to \\"false\\". If set to \\"true\\", the folders are deleted permanently. Otherwise, and if the underlying storage supports a trash folder and the folders are not yet located below the trash folder, they are moved to the trash folder.

Request body: An array with object IDs of the folders that shall be deleted.

Response: An array with object IDs of folders that were **NOT** deleted. There may be a lot of different causes for a not deleted folder: A folder has been modified in the mean time, the user does not have the permission to delete it or those permissions have just been removed, the folder does not exist, etc.


Clearing a folder's content
---------------------------

`PUT /ajax/folders?action=clear`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `tree` – (Preliminary) The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `allowed_modules` – (Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.

Request body: A JSON array containing the folder ID(s) whose content should be cleared. **NOTE:** Although the requests offers to clear multiple folders at once it is recommended to clear only one folder per request since if any exception occurs (e.g. missing permissions) the complete request is going to be aborted.

Response: A JSON array containing the IDs of folders that could not be cleared due to a concurrent modification. Meaning you receive an empty JSON array if everything worked well.


Get all visible folder of a certain module (since v6.18.2)
----------------------------------------------------------

`PUT /ajax/folders?action=allVisible`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `tree` – The identifier of the folder tree. If missing `0` (primary folder tree) is assumed.
-   `content_type` – The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "mail")
-   `columns` – A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in common folder data and detailed folder data.

Request body: None

Response with timestamp: A JSON object containing three fields: "private", "public, and "shared". Each field is a JSON array with data for all folders. Each folder is itself described by an array.


Get shared folders (Since 7.8.0, Preliminary)
---------------------------------------------

`GET /ajax/folders?action=shares`

Parameters:

-   `session` – A session ID previously obtained from the login module.
-   `columns` – A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for folders are defined in common folder data and detailed folder data.
-   `all` – Set to `1` to list even not subscribed folders.
-   `tree` – The identifier of the folder tree. If missing `0`' (primary folder tree) is assumed.
-   `content_type` – The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "infostore"). Note: this action is not implemented for module "mail".

Response with timestamp: An array with data for all folders that are considered as shared by the user. Each array element describes one folder and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.

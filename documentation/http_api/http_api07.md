---
title: API Paths and Definitions
classes: no-affix
---

<a name="getallaccounts"></a>
# Gets all mail accounts (**available since v6.12**).
```
GET /account?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Mail account data](#mail-account-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all mail accounts. Each array element describes one account and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailAccountsResponse](#mailaccountsresponse)|


## Tags

* mailaccount


<a name="deleteaccount"></a>
# Deletes a mail account (**available since v6.12**).
```
PUT /account?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with the ID of the mail account that shall be deleted.|< integer > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with identifiers of deleted accounts. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailAccountDeletionResponse](#mailaccountdeletionresponse)|


## Consumes

* `application/json`


## Tags

* mailaccount


<a name="getaccount"></a>
# Gets a mail account (**available since v6.12**).
```
GET /account?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Account ID of the requested account.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An object containing all data of the requested account. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[MailAccountResponse](#mailaccountresponse)|


## Tags

* mailaccount


<a name="createaccount"></a>
# Creates a new mail account (**available since v6.12**).
```
PUT /account?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the new account to create.|[MailAccountData](#mailaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the inserted mail account. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailAccountUpdateResponse](#mailaccountupdateresponse)|


## Consumes

* `application/json`


## Tags

* mailaccount


<a name="updateaccount"></a>
# Updates a mail account (**available since v6.12**).
```
PUT /account?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object identifying (by field `id`) and describing the account to update. Only modified fields are present.|[MailAccountData](#mailaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the updated mail account. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailAccountUpdateResponse](#mailaccountupdateresponse)|


## Consumes

* `application/json`


## Tags

* mailaccount


<a name="validateaccount"></a>
# Validates a mail account which shall be created (**available since v6.12**).
```
PUT /account?action=validate
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|Indicates whether on successful validation the folder tree shall be returned (or `null`on failure) or<br>if set to `false` or missing only a boolean is returned which indicates validation result.|boolean||
|**Body**|**body**  <br>*required*|A JSON object describing the account to validate.|[MailAccountData](#mailaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object that contains a value representing the validation result (may be a boolean or a folder tree object).<br>If the validation fails then the error fields are filled and an additional `warnings` field might be added.|[MailAccountValidationResponse](#mailaccountvalidationresponse)|


## Consumes

* `application/json`


## Tags

* mailaccount


<a name="getallattachments"></a>
# Gets all attachments for an object.
```
GET /attachment?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attached**  <br>*required*|The ID of the object to which the attachment belongs.|integer||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|string||
|**Query**|**folder**  <br>*required*|The folder ID of the object.|integer||
|**Query**|**module**  <br>*required*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all attachments. Each array element describes one attachment and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[AttachmentsResponse](#attachmentsresponse)|


## Tags

* attachments


<a name="createattachment"></a>
# Creates an attachment.
```
POST /attachment?action=attach
```


## Description
## Note
It is possible to create multiple attachments at once. Therefor add additional form fields and replace "[index]" in `json_[index]`
and `file_[index]` with the appropriate index, like `json_1`, `file_1`. The index always starts with 0 (mandatory attachment object).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**file_0**  <br>*required*|The attachment file as per `<input type="file" />`.|file||
|**FormData**|**json_0**  <br>*required*|A JSON string representing an attachment object as described in [AttachmentData](#/definitions/AttachmentData) model with at least the fields `folder`, `attached` and `module`.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing a JSON array of object IDs of the newly created attachments or errors if some occurred.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* attachments


<a name="deleteattachments"></a>
# Deletes attachments.
```
PUT /attachment?action=detach
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attached**  <br>*required*|The ID of the object to which the attachment belongs.|integer||
|**Query**|**folder**  <br>*required*|The folder ID of the object.|integer||
|**Query**|**module**  <br>*required*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with the identifiers of the attachments that shall be deleted.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* attachments


<a name="getattachmentdocument"></a>
# Gets an attachment's document/filedata.
```
GET /attachment?action=document
```


## Description
It is possible to add a filename to the request's URI like `/attachment/{filename}?action=document`.
The filename may be added to the customary attachment path to suggest a filename to a Save-As dialog.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attached**  <br>*required*|The ID of the object to which the attachment belongs.|integer||
|**Query**|**content_type**  <br>*optional*|If present the response declares the given `content_type` in the Content-Type header and not the attachments file MIME type.|string||
|**Query**|**folder**  <br>*required*|The folder ID of the object.|integer||
|**Query**|**id**  <br>*required*|Object ID of the requested attachment.|string||
|**Query**|**module**  <br>*required*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The raw byte data of the document. The response type for the HTTP request is set accordingly to the defined MIME type for this attachment or the content_type given.|string(binary)|


## Produces

* `application/octet-stream`


## Tags

* attachments


<a name="getattachment"></a>
# Gets an attachment.
```
GET /attachment?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attached**  <br>*required*|The ID of the object to which the attachment belongs.|integer||
|**Query**|**folder**  <br>*required*|The folder ID of the object.|integer||
|**Query**|**id**  <br>*required*|Object ID of the requested infoitem.|string||
|**Query**|**module**  <br>*required*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing all data of the requested attachment. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[AttachmentResponse](#attachmentresponse)|


## Tags

* attachments


<a name="getattachmentlist"></a>
# Gets a list of attachments.
```
PUT /attachment?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attached**  <br>*required*|The ID of the object to which the attachment belongs.|integer||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|string||
|**Query**|**folder**  <br>*required*|The folder ID of the object.|integer||
|**Query**|**module**  <br>*required*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with the identifiers of the requested attachments.|< integer > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested infoitems. Each array element describes one infoitem and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemsResponse](#infoitemsresponse)|


## Consumes

* `application/json`


## Tags

* attachments


<a name="getattachmentupdates"></a>
# Gets the new and deleted attachments.
```
GET /attachment?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attached**  <br>*required*|The ID of the object to which the attachment belongs.|integer||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|string||
|**Query**|**folder**  <br>*required*|The folder ID of the object.|integer||
|**Query**|**ignore**  <br>*optional*|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|enum (deleted)||
|**Query**|**module**  <br>*required*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||
|**Query**|**timestamp**  <br>*optional*|Timestamp of the last update of the requested infoitems.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with new and deleted attachments. New attachments are represented by arrays. The<br>elements of each array contain the information specified by the corresponding identifiers in the `columns`<br>parameter. Deleted attachments would be identified by their object IDs as integer, without being part of<br>a nested array. In case of errors the responsible fields in the response are filled (see<br>[Error handling](#error-handling)).|[AttachmentUpdatesResponse](#attachmentupdatesresponse)|


## Tags

* attachments


<a name="getautoconfig"></a>
# Gets the auto configuration for a mail account (**available since v6.22**).
```
POST /autoconfig?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**email**  <br>*required*|The email address for which a mail configuration will be discovered.|string||
|**FormData**|**force_secure**  <br>*optional*|Enforces a secure connection for configured mail account, default is `true` (available since v7.8.2).|boolean||
|**FormData**|**password**  <br>*required*|The corresponding password for the mail account.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the best available settings for an appropriate mail server for the given email<br>address. The data may be incomplete or empty. In case of errors the responsible fields in the response<br>are filled (see [Error handling](#error-handling)).|[AutoConfigResponse](#autoconfigresponse)|


## Tags

* autoconfig


<a name="getallappointments"></a>
# Gets all appointments.
```
GET /calendar?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|string||
|**Query**|**end**  <br>*required*|Upper exclusive limit of the queried range as a Date. Only appointments which end before this date are returned.|integer(int64)||
|**Query**|**folder**  <br>*optional*|Object ID of the folder, whose contents are queried. If not specified, defaults to all calendar folders.|string||
|**Query**|**recurrence_master**  <br>*optional*|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**showPrivate**  <br>*optional*|Only works in shared folders: When enabled, shows private appointments of the folder owner. Such appointments are anonymized by stripping away all information except start date, end date and recurrence information (since 6.18)|boolean||
|**Query**|**start**  <br>*required*|Lower inclusive limit of the queried range as a Date. Only appointments which start on or after this date are returned.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with appointment data. Each array element describes one appointment and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. Appointment sequencies are broken up into individual appointments<br>and each occurrence of a sequence in the requested range is returned separately. The appointments are<br>sorted in ascending order by the field `start_date`. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[AppointmentsResponse](#appointmentsresponse)|


## Tags

* calendar


<a name="confirmappointment"></a>
# Confirms an appointment.
```
PUT /calendar?action=confirm
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the appointments.|string||
|**Query**|**id**  <br>*required*|Object ID of the appointment that shall be confirmed.|string||
|**Query**|**occurrence**  <br>*optional*|The numeric identifier of the occurrence to which the confirmation applies (in case "id" denotes a series appointment). Available since v7.6.0.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the appointment.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object with the fields `confirmation`, `confirmmessage` and optionally `id`.|[AppointmentConfirmationBody](#appointmentconfirmationbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Nothing, except the standard response object with empty data, the timestamp of the confirmed and thereby<br>updated appointment, and maybe errors. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* calendar


<a name="deleteappointment"></a>
# Deletes appointments (**available since v6.22**).
```
PUT /calendar?action=delete
```


## Description
Before version 6.22 the request body contained a JSON object with the fields `id`, `folder` and
optionally `pos` and could only delete one appointment.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the deleted appointments.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id, folder and optionally the recurrence position (if present in an appointment to fully identify it) of the appointments.|< [AppointmentDeletionsElement](#appointmentdeletionselement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON array of objects identifying the appointments which were modified after the specified timestamp and were therefore not deleted. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[AppointmentDeletionsResponse](#appointmentdeletionsresponse)|


## Consumes

* `application/json`


## Tags

* calendar


<a name="getfreeandbusy"></a>
# Gets appointments between a specified time range.
```
GET /calendar?action=freebusy
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**end**  <br>*required*|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned.|integer(int64)||
|**Query**|**id**  <br>*required*|Internal user id. Must be obtained from the contact module.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**start**  <br>*required*|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned.|integer(int64)||
|**Query**|**type**  <br>*required*|Constant for user or resource (1 for users, 3 for resources).|enum (, )||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array of objects identifying the appointments which lie between start and end as described. In<br>case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[AppointmentFreeBusyResponse](#appointmentfreebusyresponse)|


## Tags

* calendar


<a name="getappointment"></a>
# Gets an appointment.
```
GET /calendar?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the appointments.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested appointment.|string||
|**Query**|**recurrence_position**  <br>*optional*|Recurrence position of requested appointment.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An object containing all data of the requested appointment. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[AppointmentResponse](#appointmentresponse)|


## Tags

* calendar


<a name="getchangeexceptions"></a>
# Gets all change exceptions (**available since v7.2.0**).
```
GET /calendar?action=getChangeExceptions
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the appointments.|string||
|**Query**|**id**  <br>*required*|Object ID of the appointment series.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with appointment data. Each array element describes one appointment and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are filled<br>(see [Error handling](#error-handling)).|[AppointmentsResponse](#appointmentsresponse)|


## Tags

* calendar


<a name="hasappointmentsondays"></a>
# Requests whether there are appointments on days in a specified time range.
```
GET /calendar?action=has
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**end**  <br>*required*|Upper exclusive limit of the queried range as a Date. Only appointments which end before this date are returned.|integer(int64)||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**start**  <br>*required*|Lower inclusive limit of the queried range as a Date. Only appointments which start on or after this date are returned.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with the length of the number of days between `start` and `end`. Meaning,<br>each element corresponds with one day in the range that was queried, explaining whether there is an<br>appointment on this day (true) or not (false). In case of errors the responsible fields in the response<br>are filled (see [Error handling](#error-handling)).|[AppointmentInfoResponse](#appointmentinforesponse)|


## Tags

* calendar


<a name="getappointmentlist"></a>
# Gets a list of appointments.
```
PUT /calendar?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|string||
|**Query**|**recurrence_master**  <br>*optional*|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id, folder and optionally either recurrence_position or recurrence_date_position of the requested appointments.|< [AppointmentListElement](#appointmentlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested appointments. Each array element describes one appointment and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response<br>are filled (see [Error handling](#error-handling)).|[AppointmentsResponse](#appointmentsresponse)|


## Consumes

* `application/json`


## Tags

* calendar


<a name="createappointment"></a>
# Creates an appointment.
```
PUT /calendar?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the appointment's data.|[AppointmentData](#appointmentdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the id of the newly created appointment if it was created successfully.<br>If the appointment could not be created due to conflicts, the response body is an object with the<br>field `conflicts`, which is an array of appointment objects which caused the conflict. Each appointment<br>object which represents a resource conflict contains an additional field `hard_conflict` with the<br>Boolean value true. If the user does not have read access to a conflicting appointment, only the<br>fields `id`, `start_date`, `end_date`, `shown_as` and `participants` are present and the field `participants`<br>contains only the participants which caused the conflict. In case of errors the responsible fields<br>in the response are filled (see [Error handling](#error-handling)).|[AppointmentCreationResponse](#appointmentcreationresponse)|


## Consumes

* `application/json`


## Tags

* calendar


<a name="getnewappointments"></a>
# Gets new appointments.
```
GET /calendar?action=newappointments
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|string||
|**Query**|**end**  <br>*required*|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned.|integer(int64)||
|**Query**|**limit**  <br>*required*|Limits the number of returned objects to the given value.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified and holds a column number, then the parameter order must be also specified.|string||
|**Query**|**start**  <br>*required*|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with appointment data. Appointments are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[AppointmentsResponse](#appointmentsresponse)|


## Tags

* calendar


<a name="resolveuid"></a>
# Resolves the UID to an OX object ID.
```
GET /calendar?action=resolveuid
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**uid**  <br>*required*|The UID that shall be resolved.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the related object ID in the field `id`. If no object exists with the<br>specified UID or in case of errors the responsible fields in the response are filled<br>(see [Error handling](#error-handling)).|[AppointmentUpdateResponse](#appointmentupdateresponse)|


## Tags

* calendar


<a name="searchappointments"></a>
# Searches for appointments.
```
PUT /calendar?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing search parameters.|[AppointmentSearchBody](#appointmentsearchbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching appointments. Appointments are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[AppointmentsResponse](#appointmentsresponse)|


## Consumes

* `application/json`


## Tags

* calendar


<a name="updateappointment"></a>
# Updates an appointment.
```
PUT /calendar?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the appointments.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested appointment.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the updated appointment. If the appointment was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing the appointment's data. The field `recurrence_id` is always present<br>if it is present in the original appointment. The field `recurrence_position` is present if<br>it is present in the original appointment and only this single appointment should be modified.<br>The field `id` is not present because it is already included as a parameter. Other fields are<br>present only if modified.|[AppointmentData](#appointmentdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the id of the updated appointment. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[AppointmentUpdateResponse](#appointmentupdateresponse)|


## Consumes

* `application/json`


## Tags

* calendar


<a name="getappointmentupdates"></a>
# Gets updated appointments.
```
GET /calendar?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|string||
|**Query**|**end**  <br>*optional*|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned. This parameter is optional in case a certain folder is queried, but mandatory if all accessible calendar folders are supposed to be considered (folder not specified).|integer(int64)||
|**Query**|**folder**  <br>*optional*|Object ID of the folder, whose contents are queried. That parameter may be absent in case ignore is set to "deleted", which means all accessible calendar folders are considered. If ignore is not set to "deleted", that parameter is mandatory.|string||
|**Query**|**ignore**  <br>*optional*|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|enum (deleted)||
|**Query**|**recurrence_master**  <br>*optional*|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**showPrivate**  <br>*optional*|Only works in shared folders: When enabled, shows private appointments of the folder owner. Such appointments are anonymized by stripping away all information except start date, end date and recurrence information (since 6.18)|boolean||
|**Query**|**start**  <br>*optional*|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned. This parameter is optional in case a certain folder is queried, but mandatory if all accessible calendar folders are supposed to be considered (folder not specified).|integer(int64)||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested appointments.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with new, modified and deleted appointments. New and modified appointments are represented by arrays.<br>The elements of each array contain the information specified by the corresponding identifiers in the<br>`columns` parameter. Deleted appointments (should the ignore parameter be ever implemented) would be identified<br>by their object IDs as integers, without being part of a nested array. Appointment sequencies are broken up<br>into individual appointments and each modified occurrence of a sequence in the requested range is returned<br>separately. The appointments are sorted in ascending order by the field start_date. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[AppointmentUpdatesResponse](#appointmentupdatesresponse)|


## Tags

* calendar


<a name="getallcapabilities"></a>
# Gets all capabilities (**available since v7.4.2**).
```
GET /capabilities?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all capabilities. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[CapabilitiesResponse](#capabilitiesresponse)|


## Tags

* capabilities


<a name="getcapability"></a>
# Gets a capability (**available since v7.4.2**).
```
GET /capabilities?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The identifier of the capability|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the capability or an empty result, if capability not available. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[CapabilityResponse](#capabilityresponse)|


## Tags

* capabilities


<a name="getconfignode"></a>
# Gets data of a configuration node.
```
GET /config/{path}
```


## Description
The configuration is stored in a tree. Each node of the tree has a name and a value.
The values of leaf nodes are strings which store the actual configuration data. The
values of inner nodes are defined recursively as objects with one field for each child node.
The name and the value of each field is the name and the value of the corresponding child
node, respectively.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**path**  <br>*required*|The path to the node.|enum (gui, fastgui, context_id, cookielifetime, identifier, contact_id, language, timezone, availableTimeZones, calendarnotification, tasknotification, reloadTimes, serverVersion, currentTime, maxUploadIdleTimeout, search, folder, folder/tasks, folder/calendar, folder/contacts, folder/infostore, folder/eas, mail, mail/addresses, mail/defaultaddress, mail/sendaddress, mail/folder, mail/folder/inbox, mail/folder/drafts, mail/folder/trash, mail/folder/spam, mail/folder/sent, mail/htmlinline, mail/colorquote, mail/emoticons, mail/harddelete, mail/inlineforward, mail/vcard, mail/notifyonreadack, mail/msgpreview, mail/ignorereplytext, mail/nocopytosent, mail/spambutton, participants, participants/autoSearch, participants/maximumNumberParticipants, participants/showWithoutEmail, participants/showDialog, availableModules, minimumSearchCharacters, modules, modules/portal, modules/portal/gui, modules/portal/module, modules/mail, modules/mail/addresses, modules/mail/appendmailtext, modules/mail/allowhtmlimages, modules/mailcolorquoted, modules/mail/contactCollectFolder, modules/mail/contactCollectEnabled, modules/mail/contactCollectOnMailAccess, modules/mail/contactCollectOnMailTransport, modules/mail/defaultaddress, modules/mail/deletemail, modules/mail/emoticons, modules/mail/defaultFolder, modules/mail/defaultFolder/drafts, modules/mail/defaultFolder/inbox, modules/mail/defaultFolder/sent, modules/mail/defaultFolder/spam, modules/mail/defaultFolder/trash, modules/mail/forwardmessage, modules/mail/gui, modules/mail/inlineattachments, modules/mail/linewrap, modules/mail/module, modules/mail/phishingheaders, modules/mail/replyallcc, modules/mail/sendaddress, modules/mail/spambutton, modules/mail/vcard, modules/calendar, modules/calendar/calendar_conflict, modules/calendar/calendar_freebusy, modules/calendar/calendar_teamview, modules/calendar/gui, modules/calendar/module, modules/calendar/notifyNewModifiedDeleted, modules/calendar/notifyAcceptedDeclinedAsCreator, modules/calendar/notifyAcceptedDeclinedAsParticipant, modules/calendar/defaultStatusPrivate, modules/calendar/defaultStatusPublic, modules/contacts, modules/contacts/gui, modules/contacts/mailAddressAutoSearch, modules/contacts/module, modules/contacts/singleFolderSearch, modules/contacts/characterSearch, modules/contacts/allFoldersForAutoComplete, modules/tasks, modules/tasks/gui, modules/tasks/module, modules/tasks/delegate_tasks, modules/tasks/notifyNewModifiedDeleted, modules/tasks/notifyAcceptedDeclinedAsCreator, modules/tasks/notifyAcceptedDeclinedAsParticipant, modules/infostore, modules/infostore/gui, modules/infostore/folder, modules/infostore/folder/trash, modules/infostore/folder/pictures, modules/infostore/folder/documents, modules/infostore/folder/music, modules/infostore/folder/videos, modules/infostore/folder/templates, modules/infostore/module, modules/interfaces, modules/interfaces/ical, modules/interfaces/vcard, modules/interfaces/syncml, modules/folder, modules/folder/gui, modules/folder/public_folders, modules/folder/read_create_shared_folders, modules/folder/tree, modules/com.openexchange.extras, modules/com.openexchange.extras/module, modules/com.openexchange.user.passwordchange, modules/com.openexchange.user.passwordchange/module, modules/com.openexchange.user.personaldata, modules/com.openexchange.user.personaldata/module, modules/com.openexchange.group, modules/com.openexchange.group/enabled, modules/com.openexchange.resource, modules/com.openexchange.resource/enabled, modules/com.openexchange.publish, modules/com.openexchange.publish/enabled, modules/com.openexchange.subscribe, modules/com.openexchange.subscribe/enabled, modules/olox20, modules/olox20/active, modules/olox20/module, modules/com.openexchange.oxupdater, modules/com.openexchange.oxupdater/module, modules/com.openexchange.oxupdater/active, modules/com.openexchange.passwordchange, modules/com.openexchange.passwordchange/showStrength, modules/com.openexchange.passwordchange/minLength, modules/com.openexchange.passwordchange/maxLength, modules/com.openexchange.passwordchange/regexp, modules/com.openexchange.passwordchange/special)||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Value of the node specified by path. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[ConfigResponse](#configresponse)|


## Tags

* config


<a name="putconfignode"></a>
# Sets the value of a configuration node.
```
PUT /config/{path}
```


## Description
The configuration is stored in a tree. Each node of the tree has a name and a value.
The values of leaf nodes are strings which store the actual configuration data. The
values of inner nodes are defined recursively as objects with one field for each child node.
The name and the value of each field is the name and the value of the corresponding child
node, respectively.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**path**  <br>*required*|The path to the node.|enum (gui, fastgui, context_id, cookielifetime, identifier, contact_id, language, timezone, availableTimeZones, calendarnotification, tasknotification, reloadTimes, serverVersion, currentTime, maxUploadIdleTimeout, search, folder, folder/tasks, folder/calendar, folder/contacts, folder/infostore, folder/eas, mail, mail/addresses, mail/defaultaddress, mail/sendaddress, mail/folder, mail/folder/inbox, mail/folder/drafts, mail/folder/trash, mail/folder/spam, mail/folder/sent, mail/htmlinline, mail/colorquote, mail/emoticons, mail/harddelete, mail/inlineforward, mail/vcard, mail/notifyonreadack, mail/msgpreview, mail/ignorereplytext, mail/nocopytosent, mail/spambutton, participants, participants/autoSearch, participants/maximumNumberParticipants, participants/showWithoutEmail, participants/showDialog, availableModules, minimumSearchCharacters, modules, modules/portal, modules/portal/gui, modules/portal/module, modules/mail, modules/mail/addresses, modules/mail/appendmailtext, modules/mail/allowhtmlimages, modules/mailcolorquoted, modules/mail/contactCollectFolder, modules/mail/contactCollectEnabled, modules/mail/contactCollectOnMailAccess, modules/mail/contactCollectOnMailTransport, modules/mail/defaultaddress, modules/mail/deletemail, modules/mail/emoticons, modules/mail/defaultFolder, modules/mail/defaultFolder/drafts, modules/mail/defaultFolder/inbox, modules/mail/defaultFolder/sent, modules/mail/defaultFolder/spam, modules/mail/defaultFolder/trash, modules/mail/forwardmessage, modules/mail/gui, modules/mail/inlineattachments, modules/mail/linewrap, modules/mail/module, modules/mail/phishingheaders, modules/mail/replyallcc, modules/mail/sendaddress, modules/mail/spambutton, modules/mail/vcard, modules/calendar, modules/calendar/calendar_conflict, modules/calendar/calendar_freebusy, modules/calendar/calendar_teamview, modules/calendar/gui, modules/calendar/module, modules/calendar/notifyNewModifiedDeleted, modules/calendar/notifyAcceptedDeclinedAsCreator, modules/calendar/notifyAcceptedDeclinedAsParticipant, modules/calendar/defaultStatusPrivate, modules/calendar/defaultStatusPublic, modules/contacts, modules/contacts/gui, modules/contacts/mailAddressAutoSearch, modules/contacts/module, modules/contacts/singleFolderSearch, modules/contacts/characterSearch, modules/contacts/allFoldersForAutoComplete, modules/tasks, modules/tasks/gui, modules/tasks/module, modules/tasks/delegate_tasks, modules/tasks/notifyNewModifiedDeleted, modules/tasks/notifyAcceptedDeclinedAsCreator, modules/tasks/notifyAcceptedDeclinedAsParticipant, modules/infostore, modules/infostore/gui, modules/infostore/folder, modules/infostore/folder/trash, modules/infostore/folder/pictures, modules/infostore/folder/documents, modules/infostore/folder/music, modules/infostore/folder/videos, modules/infostore/folder/templates, modules/infostore/module, modules/interfaces, modules/interfaces/ical, modules/interfaces/vcard, modules/interfaces/syncml, modules/folder, modules/folder/gui, modules/folder/public_folders, modules/folder/read_create_shared_folders, modules/folder/tree, modules/com.openexchange.extras, modules/com.openexchange.extras/module, modules/com.openexchange.user.passwordchange, modules/com.openexchange.user.passwordchange/module, modules/com.openexchange.user.personaldata, modules/com.openexchange.user.personaldata/module, modules/com.openexchange.group, modules/com.openexchange.group/enabled, modules/com.openexchange.resource, modules/com.openexchange.resource/enabled, modules/com.openexchange.publish, modules/com.openexchange.publish/enabled, modules/com.openexchange.subscribe, modules/com.openexchange.subscribe/enabled, modules/olox20, modules/olox20/active, modules/olox20/module, modules/com.openexchange.oxupdater, modules/com.openexchange.oxupdater/module, modules/com.openexchange.oxupdater/active, modules/com.openexchange.passwordchange, modules/com.openexchange.passwordchange/showStrength, modules/com.openexchange.passwordchange/minLength, modules/com.openexchange.passwordchange/maxLength, modules/com.openexchange.passwordchange/regexp, modules/com.openexchange.passwordchange/special)||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the value of the config node.|[ConfigBody](#configbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* config


<a name="getconfigproperty"></a>
# Gets a property of the configuration (**available since v7.6.2**).
```
GET /config?action=get_property
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**name**  <br>*required*|The name of the property to return.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON response providing the property's name and its value. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[ConfigPropertyResponse](#configpropertyresponse)|


## Tags

* config


<a name="setconfigproperty"></a>
# Sets a property of the configuration (**available since v7.6.2**).
```
PUT /config?action=set_property
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**name**  <br>*required*|The name of the property to return.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object providing the value to set (Example: {"value":"test123"}).|[ConfigPropertyBody](#configpropertybody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON response providing the property's name and its value. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[ConfigPropertyResponse](#configpropertyresponse)|


## Consumes

* `application/json`


## Tags

* config


<a name="searchcontactsadvanced"></a>
# Search for contacts by filter (**available since v6.20**).
```
PUT /contacts?action=advancedSearch
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**admin**  <br>*optional*|Specifies whether to include the contact representing the admin in the result or not. Defaults to `true`. (preliminary, since 7.4.2)|boolean||
|**Query**|**collation**  <br>*optional*|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|enum (gbk, gb2312)||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the search term as introducted in [Advanced search](#advanced-search). Example:<br>`{"filter":["and",["=", {"field":"last_name"},"Mustermann"],["=",{"field":"first_name"},"Max"]]}`<br>which represents 'last_name = "Mustermann" AND first_name = "Max"'. Valid fields are the ones specified in [Contact data](#/definitions/ContactData) model.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Consumes

* `application/json`


## Tags

* contacts


<a name="getallcontacts"></a>
# Gets all contacts.
```
GET /contacts?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**collation**  <br>*optional*|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|enum (gbk, gb2312)||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the contacts.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all contacts. Each array element describes one contact and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Tags

* contacts


<a name="searchcontactsbyanniversary"></a>
# Search for contacts by anniversary (**available since v6.22.1, preliminary**).
```
GET /contacts?action=anniversaries
```


## Description
Finds contacts whose anniversary falls into a specified time range.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**collation**  <br>*optional*|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|enum (gbk, gb2312)||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**end**  <br>*required*|The upper (exclusive) limit of the requested time range.|integer(int64)||
|**Query**|**folder**  <br>*optional*|Object ID of the parent folder that is searched. If not set, all visible folders are used.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|string||
|**Query**|**start**  <br>*required*|The lower (inclusive) limit of the requested time range.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Tags

* contacts


<a name="doautocompletecontacts"></a>
# Auto-complete conntacts (**available since v7.6.1, preliminary**).
```
GET /contacts?action=autocomplete
```


## Description
Finds contacts based on a prefix, usually used to auto-complete e-mail recipients while the user is typing.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**collation**  <br>*optional*|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|enum (gbk, gb2312)||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**email**  <br>*optional*|Whether to only include contacts with at least one e-mail address. Defaults to `true`.|boolean|`"true"`|
|**Query**|**folder**  <br>*optional*|Object ID of the parent folder that is searched. If not set, all visible folders are used.|string||
|**Query**|**left_hand_limit**  <br>*optional*|A positive integer number to specify the "left-hand" limit of the range to return.|integer||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**query**  <br>*required*|The query to search for.|string||
|**Query**|**right_hand_limit**  <br>*optional*|A positive integer number to specify the "right-hand" limit of the range to return.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified. Since 7.8.1: If this parameter is missing, response is sorted by a user-specific use count of contacts, ID of contacts' parent folder and display name.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the contact data. Contacts are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Tags

* contacts


<a name="searchcontactsbybirthday"></a>
# Search for contacts by birthday (**available since v6.22.1, preliminary**).
```
GET /contacts?action=birthdays
```


## Description
Finds contacts whose birthday falls into a specified time range.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**collation**  <br>*optional*|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|enum (gbk, gb2312)||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**end**  <br>*required*|The upper (exclusive) limit of the requested time range.|integer(int64)||
|**Query**|**folder**  <br>*optional*|Object ID of the parent folder that is searched. If not set, all visible folders are used.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|string||
|**Query**|**start**  <br>*required*|The lower (inclusive) limit of the requested time range.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Tags

* contacts


<a name="deletecontacts"></a>
# Deletes contacts (**available since v6.22**).
```
PUT /contacts?action=delete
```


## Description
Before version 6.22 the request body contained a JSON object with the fields `id` and `folder` and could
only delete one contact.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the deleted contacts.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id and folder of the contacts.|< [ContactListElement](#contactlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON array with object IDs of contacts which were modified after the specified timestamp and were therefore not deleted.<br>In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactDeletionsResponse](#contactdeletionsresponse)|


## Consumes

* `application/json`


## Tags

* contacts


<a name="getcontact"></a>
# Gets a contact.
```
GET /contacts?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the contacts.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested contact.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An object containing all data of the requested contact. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactResponse](#contactresponse)|


## Tags

* contacts


<a name="getcontactbyuser"></a>
# Gets a contact by user ID (**available since SP4**).
```
GET /contacts?action=getuser
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|User ID (not Object ID) of the requested user.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An object containing all data of the requested contact. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactResponse](#contactresponse)|


## Tags

* contacts


<a name="getcontactlist"></a>
# Gets a list of contacts.
```
PUT /contacts?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id and folder of the contacts.|< [ContactListElement](#contactlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested contacts. Each array element describes one contact and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Consumes

* `application/json`


## Tags

* contacts


<a name="getcontactlistbyusers"></a>
# Gets a list of users (**available since SP4**).
```
PUT /contacts?action=listuser
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with user IDs.|< integer > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with contact data. Each array element describes one contact and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Consumes

* `application/json`


## Tags

* contacts


<a name="createcontactadvanced"></a>
# Creates a contact.
```
POST /contacts?action=new
```


## Description
Creates a new contact with contact images. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the contact's data while the image file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**file**  <br>*required*|The image file.|file||
|**FormData**|**json**  <br>*required*|Represents the normal request body as JSON string containing the contact data as described in the [ContactData](#/definitions/ContactData) model.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the contact or errors if some occurred.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* contacts


<a name="createcontact"></a>
# Creates a contact.
```
PUT /contacts?action=new
```


## Description
Creates a new contact. This request cannot add contact images. Therefor it
is necessary to use the `POST` method.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the contact's data. The field id is not included.|[ContactData](#contactdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the ID of the newly created contact. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ContactUpdateResponse](#contactupdateresponse)|


## Consumes

* `application/json`


## Tags

* contacts


<a name="searchcontacts"></a>
# Search for contacts.
```
PUT /contacts?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**collation**  <br>*optional*|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|enum (gbk, gb2312)||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|string||
|**Body**|**body**  <br>*required*|A JSON object containing search parameters.|[ContactSearchBody](#contactsearchbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactsResponse](#contactsresponse)|


## Consumes

* `application/json`


## Tags

* contacts


<a name="updatecontactadvanced"></a>
# Updates a contact.
```
POST /contacts?action=update
```


## Description
Updates a contact's data and images. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the contact's data while the image file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the contacts.|string||
|**Query**|**id**  <br>*required*|Object ID of the contact that shall be updated.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the updated contact. If the contact was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**FormData**|**file**  <br>*required*|The image file.|file||
|**FormData**|**json**  <br>*required*|Represents the normal request body as JSON string containing the contact data as described in [ContactData](#/definitions/ContactData) model. Only modified fields must be specified but at least "{}".|string|`"{}"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the contact or errors if some occurred.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* contacts


<a name="updatecontact"></a>
# Updates a contact.
```
PUT /contacts?action=update
```


## Description
Updates a contact's data. This request cannot change or add contact images. Therefore it
is necessary to use the `POST` method.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the contacts.|string||
|**Query**|**id**  <br>*required*|Object ID of the contact that shall be updated.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the updated contact. If the contact was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing the contact's data. Only modified fields must be specified. To remove some contact image send the image attribute set to null.|[ContactData](#contactdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with a timestamp. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ContactUpdateResponse](#contactupdateresponse)|


## Consumes

* `application/json`


## Tags

* contacts


<a name="getcontactupdates"></a>
# Gets updated contacts.
```
GET /contacts?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the contacts.|string||
|**Query**|**ignore**  <br>*optional*|Which kinds of updates should be ignored. Omit this parameter or set it to "deleted" to not have deleted tasks identifier in the response. Set this parameter to `false` and the response contains deleted tasks identifier.|enum (deleted)||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested contacts.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with new, modified and deleted contacts. New and modified contacts are represented by arrays.<br>The elements of each array contain the information specified by the corresponding identifiers in the<br>`columns` parameter. Deleted contacts (should the ignore parameter be ever implemented) would be identified<br>by their object IDs as integers, without being part of a nested array. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[ContactUpdatesResponse](#contactupdatesresponse)|


## Tags

* contacts


<a name="convertdata"></a>
# Converts data from source using a specific data handler.
```
PUT /conversion?action=convert
```


## Description
## Saving an iCal email attachment
If an iCal file is attached to an email, its content can be saved as appointments and tasks into given
calendar and task folder. If the fields "com.openexchange.groupware.calendar.confirmstatus" and
"com.openexchange.groupware.calendar.confirmmessage" are set, the data handler inserts the appointment with
the given status for the user, if the appointment does not exist. If it is already existing, the handler
just updates the participant status.
```json
{
  "datasource": {
    "identifier":"com.openexchange.mail.ical",
    "args":
    [
      {"com.openexchange.mail.conversion.fullname":"<folder-fullname>"},
      {"com.openexchange.mail.conversion.mailid":"<mail-id>"},
      {"com.openexchange.mail.conversion.sequenceid":"<attachment-sequence-id>"}
    ]
  },
  "datahandler": {
    "identifier":"com.openexchange.ical",
    "args":
    [
      {"com.openexchange.groupware.calendar.folder":"<calendar-folder-id>"},
      {"com.openexchange.groupware.task.folder":"<task-folder-id>"},
      {"com.openexchange.groupware.calendar.confirmstatus":"<status>"},
      {"com.openexchange.groupware.calendar.confirmmessage":"<message>"}
    ]
  }
}
```
The response is a JSON array of JSON objects each providing folder and object ID of added appointments/tasks, e.g.
`[{"folder_id":2567,"id":7689}, ...]`.
## Converting an iCal email attachment into JSON objects
If an iCal file is attached to an email, its content can be converted to JSON appointments and tasks.
```json
{
  "datasource": {
    "identifier":"com.openexchange.mail.ical",
    "args":
    [
      {"com.openexchange.mail.conversion.fullname":"<folder-fullname>"},
      {"com.openexchange.mail.conversion.mailid":"<mail-id>"},
      {"com.openexchange.mail.conversion.sequenceid":"<attachment-sequence-id>"}
    ]
  },
  "datahandler": {
    "identifier":"com.openexchange.ical.json",
    "args":
    [
      {"com.openexchange.groupware.calendar.timezone":"<timezone-id>"},
      {"com.openexchange.groupware.calendar.recurrencePosition":"<recurrence-position>"},
      {"com.openexchange.groupware.calendar.searchobject":"<true|false>"}
    ]
  }
}
```
The response is a JSON array of JSON objects for each appointment/task as described in the [TaskData](#/definitions/TaskData) and [AppointmentData](#/definitions/AppointmentData) model.
## Saving a vCard email attachment
If a vCard file is attached to an email, its content can be saved as contacts into given contact folder.
```json
{
  "datasource": {
    "identifier":"com.openexchange.mail.vcard",
    "args":
    [
      {"com.openexchange.mail.conversion.fullname":"<folder-fullname>"},
      {"com.openexchange.mail.conversion.mailid":"<mail-id>"},
      {"com.openexchange.mail.conversion.sequenceid":"<attachment-sequence-id>"}
    ]
  },
  "datahandler": {
    "identifier":"com.openexchange.contact",
    "args":
    [
      {"com.openexchange.groupware.contact.folder":"<contact-folder-id>"}
    ]
  }
}
```
The response is a JSON array of JSON objects each providing folder and object ID of added contacts, e.g.
`[{"folder_id":2567,"id":7689}, ...]`.
## Contact(s) attached to a new email as a vCard file
Obtain vCard data from spacified contact object(s).
```json
{
  "datasource": {
    "identifier":"com.openexchange.contact",
    "args":
    [
      {"folder":"<folder-id1>","id":"<id1>"},
      ...,
      "folder":"<folder-idn>","id":"<idn>"
    ]
  },
  "datahandler": {
    "identifier":"com.openexchange.mail.vcard",
    "args": []
  }
}
```
The response is a JSON object as described in [MailData](#/definitions/MailData) model.
`[{"folder_id":2567,"id":7689}, ...]`.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object the data source object and the data handler object.|[ConversionBody](#conversionbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The conversion result. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ConversionResponse](#conversionresponse)|


## Consumes

* `application/json`


## Tags

* conversion


<a name="exportascsv"></a>
# Exports contact data to a CSV file.
```
GET /export?action=CSV
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*optional*|A comma-separated list of columns to export, like "501,502". A column is specified by a numeric column identifier, see [Detailed contact data](#detailed-contact-data).|string||
|**Query**|**export_dlists**  <br>*optional*|Toggles whether distribution lists shall be exported too (default is `false`). (since 7.4.1)|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder whose content shall be exported. This must be a contact folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An input stream containing the content of the CSV file with the MIME type `text/csv`.|string|


## Tags

* export


<a name="exportasical"></a>
# Exports appointment and task data to an iCalendar file.
```
GET /export?action=ICAL
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder whose content shall be exported. This must be a calendar folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An input stream containing the content of the iCal file with the MIME type `text/calendar`.|string|


## Tags

* export


<a name="exportasvcard"></a>
# Exports contact data to a vCard file.
```
GET /export?action=VCARD
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder whose content shall be exported. This must be a contact folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An input stream containing the content of the vCard file with the MIME type `text/x-vcard`.|string|


## Tags

* export


<a name="getfile"></a>
# Requests a formerly uploaded file.
```
GET /file?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the uploaded file.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The content of the requested file is directly written into output stream.|string(binary)|
|**404**|Not found.|No Content|


## Produces

* `application/octet-stream`


## Tags

* file


<a name="keepalive"></a>
# Updates a file's last access timestamp and keeps it alive.
```
GET /file?action=keepalive
```


## Description
By updating the last access timestamp the file is prevented from being deleted from both session and disk
storage.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the uploaded file whose timestamp should be updated.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* file


<a name="uploadfile"></a>
# Uploads a file.
```
POST /file?action=new
```


## Description
It can be uploaded multiple files at once. Each file must be specified in an own form field
(the form field name is arbitrary).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**module**  <br>*required*|The module for which the file is uploaded to determine proper upload quota constraints (e.g. "mail", "infostore", etc.).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**type**  <br>*required*|The file type filter to define which file types are allowed during upload. Currently supported filters are: file (for all), text (for `text/*`), media (for image, audio or video), image (for `image/*`), audio (for `audio/*`), video (for `video/*`) and application (for `application/*`).|enum (file, text, media, image, audio, video, application)||
|**FormData**|**file**  <br>*required*|The file to upload.|file||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing a JSON array with the IDs of<br>the uploaded files or errors if some occurred. The files are accessible through the returned IDs<br>for future use.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* file


<a name="getallfileaccounts"></a>
# Gets all file storage accounts.
```
GET /fileaccount?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**filestorageService**  <br>*optional*|The identifier of a file storage service to list only those accounts that belong to that file storage service.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with JSON objects each describing one file storage account. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FileAccountsResponse](#fileaccountsresponse)|


## Tags

* filestorage


<a name="deletefileaccount"></a>
# Deletes a file storage account.
```
GET /fileaccount?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**filestorageService**  <br>*required*|The identifier of the file storage service the account belongs to.|string||
|**Query**|**id**  <br>*required*|The ID of the account to delete.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the number 1 on success. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FileAccountUpdateResponse](#fileaccountupdateresponse)|


## Tags

* filestorage


<a name="getfileaccount"></a>
# Gets a file storage account.
```
GET /fileaccount?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**filestorageService**  <br>*required*|The identifier of the file storage service the account belongs to.|string||
|**Query**|**id**  <br>*required*|The ID of the requested account.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the file storage account. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FileAccountResponse](#fileaccountresponse)|


## Tags

* filestorage


<a name="createfileaccount"></a>
# Creates a file storage account.
```
PUT /fileaccount?action=new
```


## Description
## Example for creating a new OAuth-based file storage account
First, get the description of the file storage service for which a new account is supposed to be created:
`GET /ajax/fileservice?action=get&id=boxcom&session=...`

The response might be:
```json
{
  id: "boxcom",
  displayName: "Box File Storage Service",
  configuration: {
    widget: "oauthAccount",
    options: {
      type: "com.openexchange.oauth.boxcom"
    },
    name: "account",
    displayName: "Select an existing account",
    mandatory: true
  }
}
```
Next get the associated OAuth account information:
`GET /ajax/oauth/accounts?action=all&serviceId=com.openexchange.oauth.boxcom&session=...`

The response might be:
```json
{
  "data":[
    {
      "id":333,
      "displayName":"My Box.com account",
      "serviceId":"com.openexchange.oauth.boxcom"
    }
  ]
}
```
Finally, create the file storage account:
```
PUT /ajax/fileaccount?action=new&session=...

{
  "filestorageService":"boxcom",
  "displayName":"My box.com account",
  "configuration":{
    "account":"333",
    "type":"com.openexchange.oauth.boxcom"
  }
}
```
The response provides the relative identifier of the newly created account.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the account to create, with at least the field `filestorageService` set.|[FileAccountData](#fileaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the ID of the newly created account. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FileAccountCreationResponse](#fileaccountcreationresponse)|


## Consumes

* `application/json`


## Tags

* filestorage


<a name="updatefileaccount"></a>
# Updates a file storage account.
```
PUT /fileaccount?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the updated data of the account. The fields `id` and `filestorageService` must be set.|[FileAccountData](#fileaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the number 1 on success. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FileAccountCreationResponse](#fileaccountcreationresponse)|


## Consumes

* `application/json`


## Tags

* filestorage


<a name="getallfileservices"></a>
# Gets all file storage services.
```
GET /fileservice?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with JSON objects each describing one file storage service. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FileServicesResponse](#fileservicesresponse)|


## Tags

* filestorage


<a name="getfileservice"></a>
# Gets a file storage service.
```
GET /fileservice?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the file storage service to load.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the file storage service. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FileServiceResponse](#fileserviceresponse)|


## Tags

* filestorage


<a name="doautocomplete"></a>
# Suggests possible search filters based on a user's input (**available since v7.6.1**).
```
PUT /find?action=autocomplete
```


## Description
Filters are grouped into categories, the so called facets.
## Facets
The style of a facet is responsible for how the according object is structured, how it is handled on the
server-side and how the client has to handle it. We distinguish three styles of facets:
 * simple
 * default
 * exclusive
___
Every facet value contains an embedded `filter` object. The filter must not be changed by the client, it has
to be seen as a black-box. Instead the filters of selected facet values have to be copied and sent to the
server with the subsequent requests.
## Simple facets
A simple facet is a special facet that has exactly one value. The facet's type and its value are strictly
coupled, in a way that a display name for both, facet and value would be redundant. A simple facet generally
denotes a logical field like 'phone number'. Internally this logical field can map to several internal
fields (e.g. 'phone_private', 'phone_mobile', 'phone_business'). In clients the facet as a whole can be
displayed as a single item. Example: "Search for 'term' in field 'phone number'".
## Default facets
A default facet contains multiple values and may be present multiple times in search requests to filter
results by a combination of different values (e.g. "mails with 'foo' and 'bar' in subject").

Facet values may be one- or two-dimensional. A one-dimensional value can be displayed as is and contains an
according filter object. A two-dimensional value contains an array "options" with every option defining
different semantics of how the value is used to filter the search results.
## Exclusive facets
An exclusive facet is a facet where the contained values are mutually exclusive. That means that the facet
must only be present once in an autocomplete or query request.

Facet values may be one- or two-dimensional. A one-dimensional value can be displayed as is and contains an
according filter object. A two-dimensional value contains an array "options" with every option defining
different semantics of how the value is used to filter the search results.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**limit**  <br>*optional*|The maximum number of values returned per facet.|integer||
|**Query**|**module**  <br>*required*|The name of the module within that the search shall be performed. Possible modules are: mail, contacts,<br>calendar, tasks, drive. Because a user may have limited access to modules the useable modules might only<br>be a subset of the available ones. Retrieve a list of allowed modules by querying the user configuration,<br>see module "config" (path `search`) or module "JSlob" (e.g. `id=io.ox/core`).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the user's input (specified in field `prefix`), already selected `facets`, and possible `options`.|[FindAutoCompleteBody](#findautocompletebody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the facets that were found. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[FindAutoCompleteResponse](#findautocompleteresponse)|


## Consumes

* `application/json`


## Tags

* find


<a name="doquery"></a>
# Performs the actual search and returns the found items (**available since v7.6.1**).
```
PUT /find?action=query
```


## Description
Before querying the search you should fetch the search filters (facets) by calling the `/find?action=autocomplete`
request.
## Active facets
Every value that has been selected by a user must be remembered and provided with every subsequent request.
The representation of a facet within a request body differs from the one within an autocomplete response.
We call those "active facets". Their representation is independent from their style.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*optional*|A comma-separated list of module-specific columns that shall be contained in the response items. See [Column identifiers](#column-identifiers) for the numeric IDs of fields for specific modules.|string||
|**Query**|**module**  <br>*required*|The name of the module within that the search shall be performed. Possible modules are: mail, contacts,<br>calendar, tasks, drive. Because a user may have limited access to modules the useable modules might only<br>be a subset of the available ones. Retrieve a list of allowed modules by querying the user configuration,<br>see module "config" (path `search`) or module "JSlob" (e.g. `id=io.ox/core`).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the selected `facets` and possible `options`. For pagination the keys `start` and `size` can be set.|[FindQueryBody](#findquerybody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the search result. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[FindQueryResponse](#findqueryresponse)|


## Consumes

* `application/json`


## Tags

* find


<a name="getvisiblefolders"></a>
# Gets all visible folders of a certain module (**available since v6.18.2**).
```
GET /folders?action=allVisible
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|string||
|**Query**|**content_type**  <br>*required*|The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "mail", "infostore").|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing three fields: "private", "public, and "shared". Each field is a<br>JSON array with data for all folders. Each folder is itself described by an array. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FoldersVisibilityResponse](#foldersvisibilityresponse)|


## Tags

* folders


<a name="clearfolders"></a>
# Clears the content of a list of folders.
```
PUT /folders?action=clear
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|
|**Body**|**body**  <br>*required*|A JSON array with object IDs of the folders.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON array containing the IDs of folders that could not be cleared due to a concurrent modification.<br>Meaning you receive an empty JSON array if everything worked well. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[FoldersCleanUpResponse](#folderscleanupresponse)|


## Consumes

* `application/json`


## Tags

* folders


<a name="deletefolders"></a>
# Deletes a list of folders.
```
PUT /folders?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**hardDelete**  <br>*optional*|If set to `true`, the folders are deleted permanently. Otherwise, and if the underlying storage<br>supports a trash folder and the folders are not yet located below the trash folder, they are moved<br>to the trash folder.|boolean|`"false"`|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*optional*|The optional timestamp of the last update of the deleted folders.|integer(int64)||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|
|**Body**|**body**  <br>*required*|A JSON array with object IDs of the folders.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with object IDs of folders that were NOT deleted. There may be a lot of different causes<br>for a not deleted folder: A folder has been modified in the mean time, the user does not have the<br>permission to delete it or those permissions have just been removed, the folder does not exist, etc.<br>You receive an empty JSON array if everything worked well. In case of errors the responsible fields<br>in the response are filled (see [Error handling](#error-handling)).|[FoldersCleanUpResponse](#folderscleanupresponse)|


## Consumes

* `application/json`


## Tags

* folders


<a name="getfolder"></a>
# Gets a folder.
```
GET /folders?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the requested folder. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[FolderResponse](#folderresponse)|


## Tags

* folders


<a name="getsubfolders"></a>
# Gets the subfolders of a specified parent folder.
```
GET /folders?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**all**  <br>*optional*|Set to 1 to list even not subscribed folders.|integer||
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|string||
|**Query**|**errorOnDuplicateName**  <br>*optional*||boolean||
|**Query**|**parent**  <br>*required*|Object ID of a folder, which is the parent folder of the requested folders.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all folders, which have the folder with the requested object<br>ID as parent. Each array element describes one folder and is itself an array. The elements of each array<br>contain the information specified by the corresponding identifiers in the `columns` parameter. In case of<br>errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FoldersResponse](#foldersresponse)|


## Tags

* folders


<a name="createfolder"></a>
# Creates a new folder.
```
PUT /folders?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**folder_id**  <br>*required*|The parent folder object ID of the newly created folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|
|**Body**|**body**  <br>*required*|JSON object with "folder" object containing the modified fields and optional "notification"<br>object to let added permission entities be notified about newly shared folders for all modules<br>except mail. (Example: {"folder":{"title":"test123"}} or {"folder":{"permissions":[{"bits":403710016,"entity":84,"group":false}]},"notification":{"transport":"mail","message":"The message"}})|[FolderBody](#folderbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the object ID of the folder. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FolderUpdateResponse](#folderupdateresponse)|


## Consumes

* `application/json`


## Tags

* folders


<a name="notifyaboutsharedfolder"></a>
# Notifies users or groups about a shared folder (**available since v7.8.0, priliminary**).
```
PUT /folders?action=notify
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the shared folder to notify about.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|
|**Body**|**body**  <br>*required*|JSON object providing the JSON array `entities`, which holds the entity ID(s) of the users or groups that<br>should be notified. To send a custom message to the recipients, an additional JSON object `notification` may<br>be included, inside of which an optional message can be passed (otherwise, some default message is used).<br>(Example: {"entities":["2332"]} or {"entities":["2332"],"notification":{"transport":"mail","message":"The message"}})|[FolderSharingNotificationBody](#foldersharingnotificationbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An empty JSON object. Any transport warnings that occurred during sending the<br>notifications are available in the warnings array of the response. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FolderSharingNotificationResponse](#foldersharingnotificationresponse)|


## Consumes

* `application/json`


## Tags

* folders


<a name="getpath"></a>
# Gets the parent folders above the specified folder.
```
GET /folders?action=path
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|string||
|**Query**|**id**  <br>*required*|Object ID of a folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with data for all parent nodes of a folder. Each array element describes one folder and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[FoldersResponse](#foldersresponse)|


## Tags

* folders


<a name="getrootfolders"></a>
# Gets the folders at the root level of the folder structure.
```
GET /folders?action=root
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all folders at the root level of the folder structure. Each array element<br>describes one folder and is itself an array. The elements of each array contain the information<br>specified by the corresponding identifiers in the `columns` parameter. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[FoldersResponse](#foldersresponse)|


## Tags

* folders


<a name="getsharedfolders"></a>
# Gets shared folders of a certain module (**available since v7.8.0, preliminary**).
```
GET /folders?action=shares
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**all**  <br>*optional*|Set to 1 to list even not subscribed folders.|integer||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|string||
|**Query**|**content_type**  <br>*required*|The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "mail", "infostore").|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all folders that are considered as shared by the user.<br>Each array element describes one folder and is itself an array. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FoldersResponse](#foldersresponse)|


## Tags

* folders


<a name="updatefolder"></a>
# Updates a folder.
```
PUT /folders?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**cascadePermissions**  <br>*optional*|`true` to cascade permissions to all sub-folders. The user must have administrative permissions to all<br>sub-folders subject to change. If one permission change fails, the entire operation fails. (since 7.8.0)|boolean|`"false"`|
|**Query**|**id**  <br>*required*|Object ID of the updated folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|
|**Body**|**body**  <br>*required*|JSON object with "folder" object containing the modified fields and optional "notification"<br>object to let added permission entities be notified about newly shared folders for all modules<br>except mail. (Example: {"folder":{"title":"test123"}} or {"folder":{"permissions":[{"bits":403710016,"entity":84,"group":false}]},"notification":{"transport":"mail","message":"The message"}})|[FolderBody](#folderbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the object id of the folder. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FolderUpdateResponse](#folderupdateresponse)|


## Consumes

* `application/json`


## Tags

* folders


<a name="getfolderupdates"></a>
# Gets the new, modified and deleted folders of a given folder.
```
GET /folders?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**allowed_modules**  <br>*optional*|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|string||
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|string||
|**Query**|**ignore**  <br>*optional*|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|enum (deleted)||
|**Query**|**parent**  <br>*required*|Object ID of a folder, which is the parent folder of the requested folders.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested folders.|integer(int64)||
|**Query**|**tree**  <br>*optional*|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|string|`"0"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with data for new, modified and deleted folders. New and modified folders are represented<br>by arrays. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. Deleted folders (should the ignore parameter be ever implemented)<br>would be identified by their object IDs as plain strings, without being part of a nested array. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FolderUpdatesResponse](#folderupdatesresponse)|


## Tags

* folders


<a name="getfreebusy"></a>
# Gets free/busy information (**available since v6.22.1**).
```
GET /freebusy?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**from**  <br>*required*|The lower (inclusive) limit of the requested time range.|integer(int64)||
|**Query**|**merged**  <br>*optional*|Indicates whether to pre-process free/busy data on the server or not. This includes sorting as well as merging overlapping free/busy intervals.|boolean||
|**Query**|**participant**  <br>*required*|The participant to get the free/busy data for. My be either an internal user-, group- or resource-ID,<br>or an email address for external participants.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**until**  <br>*required*|The upper (exclusive) limit of the requested time range.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of free/busy intervals. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[FreeBusyResponse](#freebusyresponse)|


## Tags

* freebusy


<a name="getfreebusylist"></a>
# Gets a list of free/busy information (**available since v6.22.1**).
```
PUT /freebusy?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**from**  <br>*required*|The lower (inclusive) limit of the requested time range.|integer(int64)||
|**Query**|**merged**  <br>*optional*|Indicates whether to pre-process free/busy data on the server or not. This includes sorting as well as merging overlapping free/busy intervals.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**until**  <br>*required*|The upper (exclusive) limit of the requested time range.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array with identifiers of participants to get free/busy data for. The identifier my refer<br>to an internal user-, group- or resource-ID, or to an email address for external participants.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the free/busy data for all requested participants. For each participant it is added<br>an object (with the participant's ID as key) that contains a field `data` that is an array with objects representing<br>free/busy information as described in [FreeBusyData](#/definitions/FreeBusyData) model. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[FreeBusysResponse](#freebusysresponse)|


## Consumes

* `application/json`


## Tags

* freebusy


<a name="deletegroup"></a>
# Deletes a group (**introduced 2008-06-12**).
```
PUT /group?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the group to delete.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object with the field `id` containing the unique identifier of the group.|[GroupListElement](#grouplistelement)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with an empty array if the group was deleted successfully. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[GroupsResponse](#groupsresponse)|


## Consumes

* `application/json`


## Tags

* groups


<a name="getgroup"></a>
# Gets a group.
```
GET /group?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the group.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the group data. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[GroupResponse](#groupresponse)|


## Tags

* groups


<a name="getgrouplist"></a>
# Gets a list of groups.
```
PUT /group?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id of the requested groups.|< [GroupListElement](#grouplistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of group objects. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[GroupsResponse](#groupsresponse)|


## Consumes

* `application/json`


## Tags

* groups


<a name="creategroup"></a>
# Creates a group (**introduced 2008-06-12**).
```
PUT /group?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the group data. The field id is not present.|[GroupData](#groupdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the ID of the newly created group. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[GroupUpdateResponse](#groupupdateresponse)|


## Consumes

* `application/json`


## Tags

* groups


<a name="searchgroups"></a>
# Searches for groups.
```
PUT /group?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object with the search parameters.|[GroupSearchBody](#groupsearchbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of group objects. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[GroupsResponse](#groupsresponse)|


## Consumes

* `application/json`


## Tags

* groups


<a name="updategroup"></a>
# Updates a group (**introduced 2008-06-12**).
```
PUT /group?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|ID of the group that shall be updated.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the group to update. If the group was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing the group data fields to change. Only modified fields are present and the field id is omitted.|[GroupData](#groupdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* groups


<a name="getgroupupdates"></a>
# Gets the new, modified and deleted groups (**available since v6.18.1, introduced 2010-09-13**).
```
GET /group?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested groups.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with fields `new`, `modified` and `deleted` representing arrays of new, modified and<br>deleted group objects. In case of errors the responsible fields in the response are filled<br>(see [Error handling](#error-handling)).|[GroupUpdatesResponse](#groupupdatesresponse)|


## Tags

* groups


<a name="gethalocontactpicture"></a>
# Gets a contact picture.
```
GET /halo/contact/picture
```


## Description
At least one of the optional search parameters should be set. All parameters are connected by OR during
the search. More specific parameters like `user_id` or `id` are prioritized in case of multiple matches.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**email**  <br>*optional*|An email to searchz for. Will pick global address book matches before regular matches. After that picks the most recently changed contact.|string||
|**Query**|**email1**  <br>*optional*|An alias for `email`.|string||
|**Query**|**email2**  <br>*optional*|An alias for `email`.|string||
|**Query**|**email3**  <br>*optional*|An alias for `email`.|string||
|**Query**|**id**  <br>*optional*|A contact ID.|string||
|**Query**|**internal_userid**  <br>*optional*|The internal user ID of a user whose picture you want to load.|integer||
|**Query**|**session**  <br>*optional*|Falls back to the public session cookie.|string||
|**Query**|**user_id**  <br>*optional*|An alias for `internal_userid`.|integer||
|**Query**|**userid**  <br>*optional*|An alias for `internal_userid`.|integer||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The picture with proper ETag and caching headers set.|string(binary)|
|**404**|If no picture could be found.|No Content|


## Tags

* halo


<a name="investigatecontacthalo"></a>
# Investigates a contact.
```
GET /halo/contact?action=investigate
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return. See [Column identifiers](#column-identifiers) for a mapping of numeric identifiers to fields.|string||
|**Query**|**end**  <br>*optional*|The end point. Only mandatory for provider "com.openexchange.halo.appointments".|integer(int64)||
|**Query**|**limit**  <br>*optional*|The maximum number of mails within the result. Optional for provider "com.openexchange.halo.mail".|integer||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Optional for provider "com.openexchange.halo.appointments".|string||
|**Query**|**provider**  <br>*required*|The halo provider, like "com.openexchange.halo.contacts". See `/halo/contact?action=services` for available providers.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified. Optional for provider "com.openexchange.halo.appointments".|string||
|**Query**|**start**  <br>*optional*|The start point. Only mandatory for provider "com.openexchange.halo.appointments".|integer(int64)||
|**Query**|**timezone**  <br>*optional*|The timezone.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested columns. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[HaloInvestigationResponse](#haloinvestigationresponse)|


## Tags

* halo


<a name="gethaloservices"></a>
# Gets all halo services.
```
GET /halo/contact?action=services
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with available halo providers. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[HaloServicesResponse](#haloservicesresponse)|


## Tags

* halo


<a name="getcontactprofileimage"></a>
# Requests a contact's profile image.
```
GET /image/contact/picture
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|The folder ID in which the contact resides.|string||
|**Query**|**id**  <br>*required*|The object ID of the contact.|integer||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The content of the requested image is directly written into output stream.|string(binary)|
|**400**|If request cannot be handled.|No Content|


## Produces

* `application/octet-stream`


## Tags

* image


<a name="getmp3coverimage"></a>
# Requests a MP3 cover image.
```
GET /image/file/mp3cover
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The identifier of the uploaded image.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The content of the requested image is directly written into output stream.|string(binary)|
|**400**|If request cannot be handled.|No Content|


## Produces

* `application/octet-stream`


## Tags

* image


<a name="getinlinemailimage"></a>
# Requests an inline image from a mail.
```
GET /image/mail/picture
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**accountId**  <br>*optional*|The mail account identifier|integer||
|**Query**|**folder**  <br>*required*|The folder ID in which the mail resides.|string||
|**Query**|**id**  <br>*required*|The object ID of the mail.|string||
|**Query**|**uid**  <br>*required*|The identifier of the image inside the referenced mail.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The content of the requested image is directly written into output stream.|string(binary)|
|**400**|If request cannot be handled.|No Content|


## Produces

* `application/octet-stream`


## Tags

* image


<a name="getmanagedimagefile"></a>
# Requests an image that was previously uploaded with the ajax file upload module.
```
GET /image/mfile/picture
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**uid**  <br>*required*|The identifier of the uploaded image.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The content of the requested image is directly written into output stream.|string(binary)|
|**400**|If request cannot be handled.|No Content|


## Produces

* `application/octet-stream`


## Tags

* image


<a name="getuserprofileimage"></a>
# Requests a user's profile image.
```
GET /image/user/picture
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The object ID of the user.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The content of the requested image is directly written into output stream.|string(binary)|
|**400**|If request cannot be handled.|No Content|


## Produces

* `application/octet-stream`


## Tags

* image


<a name="importcsv"></a>
# Imports contact data from CSV file.
```
POST /import?action=CSV
```


## Description
## Example CSV
```
"Given name","Sur name"
"Günther","Mustermann"
"Hildegard","Musterfrau"
```
The delimiter may be any CSV-valid character (e.g. "," or ";"). The first line must contain the column titles that are related
to the corresponding fields of the [ContactData](#/definitions/ContactData) model. See [Detailed contact data](#detailed-contact-data)
for a mapping of fields to CSV column titles.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**charset**  <br>*optional*|A fixed character encoding to use when parsing the uploaded file, overriding the built-in defaults, following the conventions documented in [RFC 2278](http://tools.ietf.org/html/rfc2278) (preliminary, since 7.6.2).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder into which the data should be imported. This must be a contact folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**file**  <br>*required*|The CSV file containing the contact data. The column titles are the ones described in [Detailed contact data](#detailed-contact-data).|file||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents<br>an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created contacts.<br>In case of errors the JSON object contains the well known [error fields](#error-handling).|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* import


<a name="importical"></a>
# Imports calendar data from iCalendar file.
```
POST /import?action=ICAL
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder into which the data should be imported. This may be be an appointment or a task folder.|string||
|**Query**|**ignoreUIDs**  <br>*optional*|When set to `true`, UIDs are partially ignored during import of tasks and appointments from iCal. Internally, each UID is replaced statically by a random one to preserve possibly existing relations between recurring appointments in the same iCal file, but at the same time to avoid collisions with already existing tasks and appointments.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**suppressNotification**  <br>*optional*|Can be used to disable the notifications for new appointments that are imported through the given iCal file. This help keeping the Inbox clean if a lot of appointments need to be imported. The value of this parameter does not matter because only for the existence of the parameter is checked.|boolean||
|**FormData**|**file**  <br>*required*|The iCal file containing the appointment and task data.|file||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents<br>an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created appointments/tasks.<br>In case of errors the JSON object contains the well known [error fields](#error-handling). Beside a field `warnings` may contain an array<br>of objects with warning data containing customary error fields.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* import


<a name="importoutlookcsv"></a>
# Imports contact data from an Outlook CSV file.
```
POST /import?action=OUTLOOK_CSV
```


## Description
## Example: exported Outlook CSV
```
First Name,Last Name
Günther,Mustermann
Hildegard,Musterfrau
```
The column titles in the first line of the CSV file may be those used by the English, French or German version of Outlook.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**charset**  <br>*optional*|A fixed character encoding to use when parsing the uploaded file, overriding the built-in defaults, following the conventions documented in [RFC 2278](http://tools.ietf.org/html/rfc2278) (preliminary, since 7.6.2).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder into which the data should be imported. This must be a contact folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**file**  <br>*required*|The CSV file **with Windows' default encoding CP-1252** containing the contact data. The column titles are those used by the English, French or German version of Outlook.|file||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents<br>an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created contacts.<br>In case of errors the JSON object contains the well known [error fields](#error-handling).|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* import


<a name="importvcard"></a>
# Imports data from vCard file.
```
POST /import?action=VCARD
```


## Description
## Supported vCard formats
 * vCard 2.1
 * vCard 3.0
 * vCalendar 1.0


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder into which the data should be imported. This must be a contact folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**file**  <br>*required*|The vCard file.|file||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents<br>an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created objects.<br>In case of errors the JSON object contains the well known [error fields](#error-handling).|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* import


<a name="getallinfoitems"></a>
# Gets all infoitems.
```
GET /infostore?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the infoitems.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all infoitems. Each array element describes one infoitem and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemsResponse](#infoitemsresponse)|


## Tags

* infostore


<a name="checkname"></a>
# Checks if a given file name is valid (**available since v7.8.1, preliminary**).
```
GET /infostore?action=checkname
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**name**  <br>*required*|The file name to check.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An empty JSON object when file name is valid. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* infostore


<a name="copyinfoitemadvanced"></a>
# Copies an infoitem.
```
POST /infostore?action=copy
```


## Description
Copies an infoitem's data with the possibility to change the file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the infoitem that shall be copies.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**file**  <br>*required*|The metadata as per `<input type="file" />`.|file||
|**FormData**|**json**  <br>*required*|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemData](#/definitions/InfoItemData) model. Only modified fields must be specified but at least `{"folder_id":"destination"}`.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* infostore


<a name="copyinfoitem"></a>
# Copies an infoitem.
```
PUT /infostore?action=copy
```


## Description
This request cannot change or add files. Therefore it is necessary to use the `POST` method.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the infoitem that shall be copied.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the modified fields of the destination infoitem. The field `id` must not be present.|[InfoItemData](#infoitemdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemUpdateResponse](#infoitemupdateresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="deleteinfoitems"></a>
# Deletes infoitems.
```
PUT /infostore?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**hardDelete**  <br>*optional*|Defaults to `false`. If set to `true`, the file is deleted permanently. Otherwise, and if the underlying storage supports a trash folder and the file is not yet located below the trash folder, it is moved to the trash folder.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the infoitems to delete.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array of objects with the fields `id` and `folder` representing infoitems that shall be deleted.|< [InfoItemListElement](#infoitemlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with an empty array if the infoitems were deleted successfully. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[InfoItemsResponse](#infoitemsresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="deleteinfoitemversions"></a>
# Deletes versions of an infoitem.
```
PUT /infostore?action=detach
```


## Description
## Note
When the current version of a document is deleted the new current version will be the latest version.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|The folder ID of the base object.|string||
|**Query**|**id**  <br>*required*|The ID of the base object.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the infoitem.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array of version numbers to detach.|< integer > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with an empty array of version numbers that were not deleted. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[InfoItemDetachResponse](#infoitemdetachresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="getinfoitemdocument"></a>
# Gets an infoitem document.
```
GET /infostore?action=document
```


## Description
It is possible to add a filename to the request's URI like `/infostore/{filename}?action=document`.
The filename may be added to the customary infostore path to suggest a filename to a Save-As dialog.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**content_type**  <br>*optional*|If present the response declares the given `content_type` in the Content-Type header.|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the infoitems.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested infoitem.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**version**  <br>*optional*|If present the infoitem data describes the given version. Otherwise the current version is returned.|integer||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The raw byte data of the document. The response type for the HTTP request is set accordingly to the defined mimetype for this infoitem or the content_type given.|string(binary)|


## Produces

* `application/octet-stream`


## Tags

* infostore


<a name="getinfoitem"></a>
# Gets an infoitem.
```
GET /infostore?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the infoitems.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested infoitem.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing all data of the requested infoitem. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemResponse](#infoitemresponse)|


## Tags

* infostore


<a name="getinfoitemlist"></a>
# Gets a list of infoitems.
```
PUT /infostore?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id of the infoitems.|< [InfoItemListElement](#infoitemlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested infoitems. Each array element describes one infoitem and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemsResponse](#infoitemsresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="lockinfoitem"></a>
# Locks an infoitem.
```
GET /infostore?action=lock
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**diff**  <br>*optional*|If present the value is added to the current time on the server (both in ms). The document will be locked until that time. If this parameter is not present, the document will be locked for a duration as configured on the server.|integer(int64)||
|**Query**|**id**  <br>*required*|Object ID of the infoitem that shall be locked.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* infostore


<a name="moveinfoitems"></a>
# Moves one or more infoitems to another folder.
```
PUT /infostore?action=move
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|ID of the destination folder.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects each referencing to an existing infoitem that is supposed to be moved to the destination folder.|< [InfoItemListElement](#infoitemlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with an array of infoitem identifiers that could not be moved (due to a conflict).<br>Th array is empty if everything went fine. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemsMovedResponse](#infoitemsmovedresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="createinfoitemadvanced"></a>
# Creates an infoitem.
```
POST /infostore?action=new
```


## Description
Creates a new infoitem with a file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**try_add_version**  <br>*optional*|Add new file version if file name exists|boolean||
|**FormData**|**file**  <br>*required*|The metadata as per `<input type="file" />`.|file||
|**FormData**|**json**  <br>*required*|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemBody](#/definitions/InfoItemBody) model.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* infostore


<a name="createinfoitem"></a>
# Creates an infoitem.
```
PUT /infostore?action=new
```


## Description
Creates a new contact. This request cannot add a file to the infoitem. Therefor it
is necessary to use the `POST` method.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the infoitem that shall be updated.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing a field `file` with the modified fields of the infoitem's data. It is possible to let added object permission entities be notified about newly shared files. In that case add a "notification" object.|< [InfoItemBody](#infoitembody) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemUpdateResponse](#infoitemupdateresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="notifyaboutsharedinfoitem"></a>
# Notifies users or groups about a shared infoitem (**available since v7.8.0, preliminary**).
```
PUT /infostore?action=notify
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the shared infoitem to notify about.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|JSON object providing the JSON array `entities`, which holds the entity ID(s) of the users or groups that<br>should be notified. To send a custom message to the recipients, an additional JSON object `notification` may<br>be included, inside of which an optional message can be passed (otherwise, some default message is used).<br>(Example: {"entities":["2332"]} or {"entities":["2332"],"notification":{"transport":"mail","message":"The message"}})|[InfoItemSharingNotificationBody](#infoitemsharingnotificationbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An empty JSON object. Any transport warnings that occurred during sending the<br>notifications are available in the warnings array of the response. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[InfoItemSharingNotificationResponse](#infoitemsharingnotificationresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="deleteallinfoitemversions"></a>
# Deletes all versions of an infoitem leaving only the base object.
```
PUT /infostore?action=revert
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|The folder ID of the base object.|string||
|**Query**|**id**  <br>*required*|The ID of the base object.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the infoitem.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* infostore


<a name="importattachment"></a>
# Saves an attachment in the infostore.
```
PUT /infostore?action=saveAs
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attached**  <br>*required*|The ID of the object to which the attachment belongs.|integer||
|**Query**|**attachment**  <br>*required*|The ID of the attachment to save.|string||
|**Query**|**folder**  <br>*required*|The folder ID of the object.|integer||
|**Query**|**module**  <br>*required*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the attachment's infoitem. The field `id`is not included. The fields in<br>this infoitem object override values from the attachment. The folder_id must be given. It is possible to<br>let added object permission entities be notified about newly shared files. In that case add a "notification" object.|< [InfoItemBody](#infoitembody) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemUpdateResponse](#infoitemupdateresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="searchinfoitems"></a>
# Search for infoitems.
```
PUT /infostore?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|string||
|**Query**|**end**  <br>*optional*|The last index (inclusive) from the ordered search, that is requested.|integer||
|**Query**|**folder**  <br>*optional*|The folder ID to restrict the search to. If not specified, all folders are searched.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|string||
|**Query**|**start**  <br>*optional*|The start index (inclusive, zero-based) in the ordered search, that is requested.|integer||
|**Body**|**body**  <br>*required*|A JSON object containing search parameters.|[InfoItemSearchBody](#infoitemsearchbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching infoitems. Infoitems are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[InfoItemsResponse](#infoitemsresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="getsharedinfoitems"></a>
# Gets shared infoitems (**available since v7.8.0, preliminary**).
```
GET /infostore?action=shares
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all infoitems that are considered as shared by the user.<br>Each array element describes one infoitem and is itself an array. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case<br>of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[InfoItemsResponse](#infoitemsresponse)|


## Tags

* infostore


<a name="unlockinfoitem"></a>
# Unlocks an infoitem.
```
GET /infostore?action=unlock
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the infoitem that shall be unlocked.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* infostore


<a name="updateinfoitemadvanced"></a>
# Updates an infoitem.
```
POST /infostore?action=update
```


## Description
Updates an infoitem's data and file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the infoitem that shall be updated.|string||
|**Query**|**offset**  <br>*optional*|Optionally sets the start offset in bytes where to append the data to the document, must be equal to the actual document's length (available since v7.8.1). Only available if the underlying File storage account supports the "RANDOM_FILE_ACCESS" capability.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the updated infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**FormData**|**file**  <br>*required*|The metadata as per `<input type="file" />`.|file||
|**FormData**|**json**  <br>*required*|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemBody](#/definitions/InfoItemBody) model. Only modified fields must be specified but at least "{}".|string|`"{}"`|


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* infostore


<a name="updateinfoitem"></a>
# Updates an infoitem.
```
PUT /infostore?action=update
```


## Description
Updates an infoitem's data. This request cannot change or add files. Therefore it
is necessary to use the `POST` method.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the infoitem that shall be updated.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing a field `file` with the modified fields of the infoitem's data. It is possible to let added object permission entities be notified about newly shared files. In that case add a "notification" object.|< [InfoItemBody](#infoitembody) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the object ID of the updated infoitem. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[InfoItemUpdateResponse](#infoitemupdateresponse)|


## Consumes

* `application/json`


## Tags

* infostore


<a name="getinfoitemupdates"></a>
# Gets the new, modified and deleted infoitems.
```
GET /infostore?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the infoitems.|string||
|**Query**|**ignore**  <br>*optional*|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|enum (deleted, )||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||
|**Query**|**timestamp**  <br>*optional*|Timestamp of the last update of the requested infoitems.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with new, modified and deleted infoitems. New and modified infoitems are represented by arrays. The<br>elements of each array contain the information specified by the corresponding identifiers in the `columns`<br>parameter. Deleted infoitems would be identified by their object IDs as string, without being part of<br>a nested array. In case of errors the responsible fields in the response are filled (see<br>[Error handling](#error-handling)).|[InfoItemUpdatesResponse](#infoitemupdatesresponse)|


## Tags

* infostore


<a name="getallversions"></a>
# Gets all versions of an infoitem.
```
GET /infostore?action=versions
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|string||
|**Query**|**id**  <br>*required*|Object ID of the infoitem whose versions are requested.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the infoitem. Each array element describes one infoitem and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. The timestamp is the timestamp relating to the requested infostore item.<br>In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[InfoItemsResponse](#infoitemsresponse)|


## Tags

* infostore


<a name="getdocumentsaszip"></a>
# Gets multiple documents as a ZIP archive (**available since v7.4.0**).
```
PUT /infostore?action=zipdocuments
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id, folder and optionally the documents' versions to include in the requested ZIP archive (if missing, it refers to the latest/current version).|< [InfoItemZipElement](#infoitemzipelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The raw byte data of the ZIP archive. The response type for the HTTP request is set to `application/zip`.|string(binary)|


## Consumes

* `application/json`


## Produces

* `application/zip`


## Tags

* infostore


<a name="getfolderaszip"></a>
# Gets a ZIP archive containing all ifoitems of a denoted folder (**availabel since v7.6.1**).
```
GET /infostore?action=zipfolder
```


## Description
It is possible to add a filename to the request's URI like `/infostore/{filename}?action=zipfolder`.
The filename may be added to the customary infostore path to suggest a filename to a Save-As dialog.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the infoitems.|string||
|**Query**|**recursive**  <br>*optional*|`true` to also include subfolders and their infoitems respectively; otherwise `false` to only consider the infoitems of specified.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The raw byte data of the ZIP archive. The response type for the HTTP request is set to `application/zip`.|string(binary)|


## Produces

* `application/zip`


## Tags

* infostore


<a name="getalljslobs"></a>
# Gets all JSlobs (**available since v6.22**).
```
GET /jslob?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**serviceId**  <br>*optional*|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of JSON objects each representing a certain JSON configuration.<br>In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[JSlobsResponse](#jslobsresponse)|


## Tags

* JSlob


<a name="getjsloblist"></a>
# Gets a list of JSlobs (**available since v6.22**).
```
PUT /jslob?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**serviceId**  <br>*optional*|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with the identifiers of the requested JSlobs.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of JSON objects each representing a certain JSON configuration.<br>In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[JSlobsResponse](#jslobsresponse)|


## Consumes

* `application/json`


## Tags

* JSlob


<a name="setjslob"></a>
# Stores or deletes a JSlob (**available since v6.22**).
```
PUT /jslob?action=set
```


## Description
To delete a JSON configuration just send an empty request body for the specified `id`.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*optional*|The JSlob indentifier.|string||
|**Query**|**serviceId**  <br>*optional*|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the JSON configuration to store. To delete the JSlob just send an empty body.|object||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* JSlob


<a name="updatejslob"></a>
# Updates a JSlob (**available since v6.22**).
```
PUT /jslob?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*optional*|The JSlob indentifier.|string||
|**Query**|**serviceId**  <br>*optional*|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|The JSON object containing the updated JSON configuration to store. Fields that are not included are thus not affected and survive the change. Use `/jslob?action=set` to delete fields or entire JSlob.|object||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* JSlob


<a name="acquireidentitytoken"></a>
# Acquires an identity token (**available since v7.6.0**).
```
GET /jump?action=identityToken
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**system**  <br>*optional*|The identifier for the external service/system, like "com.openexchange.jump.endpoint.mysystem".|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the identity token. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[JumpResponse](#jumpresponse)|


## Tags

* jump


<a name="redirect"></a>
# Refresh auto-login cookie
```
GET /login;jsessionid=1157370816112.OX1?action=redirect
```


## Description
**SECURITY WARNING!** Utilizing this request is **INSECURE!** This request allows to access a session with a
single one time token. This one time token may be delivered to the wrong client if the protocol has an
error or Apache or the load balancer make a mistake. This will cause a wrong user to be in a wrong
session. **IMMEDIATELY** consider not to use this request anymore. You have been warned. Use instead the
FormLogin that does not need to use the redirect request.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**client**  <br>*optional*|The client can be defined here newly if it is not correct on the login request itself.|string||
|**Query**|**random**  <br>*required*|A session random token to jump into the session. This random token is part of the login<br>response. Only a very short configurable time after the login it is allowed to jump into<br>the session with the random token.|string||
|**Query**|**store**  <br>*optional*|Tells the UI to do a store request after login to be able to use autologin request.|boolean||
|**Query**|**uiWebPath**  <br>*optional*|The optional path on the webserver to the UI. If this parameter is not given the configured uiWebPath is used.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* login


<a name="changeip"></a>
# Change IP of client host in a session.
```
POST /login?action=changeip
```


## Description
The following request is especially for integration with systems located in the providers
infrastructure. If those systems create a session with the following request the client host
IP address in the session can be changed. The IP check for following requests will be done using
this newly set client host IP address.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**clientIP**  <br>*required*|New IP address of the client host for the current session.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the string "1" as data attribute. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[ChangeIPResponse](#changeipresponse)|


## Tags

* login


<a name="doformlogin"></a>
# Login to the web frontend using a simple HTML form (**available since v6.20**).
```
POST /login?action=formlogin
```


## Description
This request implements a possible login to the web frontend by only using a simple HTML form.
The response contains a redirect link to the Web-UI. See [OXSessionFormLogin](http://oxpedia.org/wiki/index.php?title=OXSessionFormLogin) for details.
An example for such a form can be found in the backend's documentation folder
(/usr/share/doc/open-xchange-core) under examples/login.html.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**authId**  <br>*required*|Identifier for tracing every single login request passed between different systems in a cluster.<br>The value should be some token that is unique for every login request. This parameter must be<br>given as URL parameter and not inside the body of the POST request.|string||
|**FormData**|**autologin**  <br>*required*|True or false. True tells the UI to issue a store request for the session cookie.<br>This store request is necessary if you want the autologin request not to fail.|boolean|`"false"`|
|**FormData**|**client**  <br>*required*|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients<br>are used with Open-Xchange. If the autologin request should work the client must be the same as<br>the client sent by the UI in the normal login request.|string||
|**FormData**|**clientIP**  <br>*optional*|IP address of the client host for that the session is created. If this parameter is not<br>specified the IP address of the HTTP client doing this request is used.|string||
|**FormData**|**clientUserAgent**  <br>*optional*|Value of the User-Agent header of the client host for that the session is created.<br>If this parameter is not specified the User-Agent of the current HTTP client doing<br>this request is used.|string||
|**FormData**|**login**  <br>*required*|The login name.|string||
|**FormData**|**password**  <br>*required*|The password.|string(password)||
|**FormData**|**uiWebPath**  <br>*optional*|Defines another path on the web server where the UI is located. If this parameter is not defined<br>the configured default of the backend is used.|string||
|**FormData**|**version**  <br>*required*|Used version of the HTTP/JSON interface client.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A redirect to the web UI. The URL of the web UI is either taken from the given parameter<br>or from the configured default of the backend.|string|


## Produces

* `text/html`


## Tags

* login


<a name="dologin"></a>
# Login with user credentials.
```
POST /login?action=login
```


## Description
The login module is used to obtain a session from the user's login
credentials. Parameters are normally expected in the POST request body.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**authId**  <br>*optional*|Identifier for tracing every single login request passed between different systems in a cluster.<br>The value should be some token that is unique for every login request. This parameter must be<br>given as URL parameter and not inside the body of the POST request. (IS OPTIONAL, meaning can be empty)|string||
|**FormData**|**client**  <br>*optional*|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients are used with Open-Xchange.|string||
|**FormData**|**clientIP**  <br>*optional*|IP address of the client host for that the session is created. If this parameter is not specified the IP address of the HTTP client doing this request is used.|string||
|**FormData**|**clientUserAgent**  <br>*optional*|Value of the User-Agent header of the client host for that the session is created. If this parameter is not specified the User-Agent of the current HTTP client doing this request is used.|string||
|**FormData**|**name**  <br>*required*|The login name.|string||
|**FormData**|**password**  <br>*required*|The password (MUST be placed in the request body, otherwise the login request will be denied).|string(password)||
|**FormData**|**version**  <br>*optional*|Used version of the HTTP/JSON interface client.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the session ID used for all subsequent requests. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[LoginResponse](#loginresponse)|


## Tags

* login


<a name="dologout"></a>
# Does the logout.
```
GET /login?action=logout
```


## Description
Does the logout which invalidates the session.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**403**|FORBIDDEN. The server refuses to respond to the request.|No Content|


## Tags

* login


<a name="redeemtoken"></a>
# Redeem Token Login (**available since v7.4.0**).
```
POST /login?action=redeemToken
```


## Description
With a valid session it is possible to acquire a secret (see `token?action=acquireToken`). Using this secret another system is able
to generate a valid session. This session may also contain the users password (configurable).
The system in question needs to be registered at the server and has to identify itself with a key
configured at the open-xchange server. This is only for internal communication and by default no keys
are available.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**authId**  <br>*required*|Identifier for tracing every single login request passed between different systems in a cluster.<br>The value should be some token that is unique for every login request. This parameter must be<br>given as URL parameter and not inside the body of the POST request.|string||
|**FormData**|**client**  <br>*required*|Identifier of the client using the HTTP/JSON interface. The client must identifier must be the same for each request after creating the login session.|string||
|**FormData**|**secret**  <br>*required*|The value of the secret string for token logins. This is configured through the tokenlogin-secrets configuration file.|string||
|**FormData**|**token**  <br>*required*|The token created with `token?action=acquireToken`.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the session ID used for all subsequent requests. Additionally a random<br>token is contained to be used for the Easy Login method. If configured within tokenlogin-secrets<br>configuration file even the user password will be returned. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[LoginResponse](#loginresponse)|


## Tags

* login


<a name="refreshsecretcookie"></a>
# Refreshes the secret cookie (**available since v6.18.2**)
```
GET /login?action=refreshSecret
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* login


<a name="refreshautologincookie"></a>
# Refreshes the auto-login cookie.
```
GET /login?action=store
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* login


<a name="dotokenlogin"></a>
# Login for a very short living session (**available since v7.0.1**).
```
POST /login?action=tokenLogin
```


## Description
This request allows every possible client to create a very short living session. This session can then be transferred to any other client preferably a browser entering then the normal web interface. Then the sessions life time will be extended equally to every other session.

Compared to the login mechanism using the random token, this request is more secure because two tokens are used. One of these tokens is only known to the client and one is generated by the server. Only the combination of both tokens allows to use the session. The combination of both tokens must be done by the client creating the session.

**DISCLAIMER:** This request MUST NOT be used by some server side instance. If some server side instance uses this request to create a session for a browser on some client machine, then you have to transfer the full URL with server and client token over some connection to the client. This creates a VULNERABILITY if this is done. The token login method is only secure if this request is already sent from the same machine that later runs the browser using the created session.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**authId**  <br>*required*|Identifier for tracing every single login request passed between different systems in a cluster.<br>The value should be some token that is unique for every login request. This parameter must be<br>given as URL parameter and not inside the body of the POST request.|string||
|**FormData**|**autologin**  <br>*required*|True or false. True tells the UI to issue a store request for the session cookie. This store<br>request is necessary if you want the autologin request not to fail. This must be enabled on<br>the server and a client can test with the autologin request if it is enabled or not.|boolean||
|**FormData**|**client**  <br>*required*|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients are used with Open-Xchange.|string||
|**FormData**|**clientIP**  <br>*optional*|IP address of the client host for that the session is created. If this parameter is not specified the IP address of the HTTP client doing this request is used.|string||
|**FormData**|**clientToken**  <br>*required*|Client side identifier for accessing the session later. The value should be some token that is unique for every login request.|string||
|**FormData**|**clientUserAgent**  <br>*optional*|Value of the User-Agent header of the client host for that the session is created. If this<br>parameter is not specified the User-Agent of the current HTTP client doing this request is used.|string||
|**FormData**|**jsonResponse**  <br>*optional*|(since 7.8.0) True or false (default). Defines the returned data type as JSON. Default `false` will return a redirect.|boolean|`"false"`|
|**FormData**|**login**  <br>*required*|The login information.|string||
|**FormData**|**password**  <br>*required*|The password (MUST be placed in the request body, otherwise the login request will be denied).|string(password)||
|**FormData**|**uiWebPath**  <br>*optional*|Defines another path on the web server where the UI is located. If this parameter is not<br>defined the configured default of the backend is used.|string||
|**FormData**|**version**  <br>*required*|Version of the HTTP/JSON interface client. Only for statistic evaluations.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case `jsonResponse=true`, it is returned a JSON object. Otherwise a redirect to the web UI.<br>The URL of the web UI is either taken from the given parameter or from the configured default<br>of the backend. This redirect will only contain the server side token. The client side token<br>sent in the request must be appended by the client creating the session. The final URL must<br>have the form redirect_URL&clientToken=token. Both tokens are necessary to use the session and<br>both tokens must match. Otherwise the session is terminated. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[TokenLoginResponse](#tokenloginresponse)|


## Produces

* `application/json`
* `text/html`


## Tags

* login


<a name="accesssession"></a>
# Accesses a session that was previously created with the token login (**available since v7.0.1**).
```
POST /login?action=tokens
```


## Description
This request allows clients to access a session created with the `/login?action=tokenLogin` request.
When accessing the session its life time is extended equally to every other session.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**FormData**|**client**  <br>*required*|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients are used with Open-Xchange.|string||
|**FormData**|**clientToken**  <br>*required*|The password (MUST be placed in the request body, otherwise the login request will be denied).|string||
|**FormData**|**serverToken**  <br>*required*|The login name.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object conform to the normal response body containing the session identifier, the login, the identifier<br>and the locale of the user.  In case of errors the responsible fields in the response are filled<br>(see [Error handling](#error-handling)).|[TokensResponse](#tokensresponse)|


## Tags

* login


<a name="movemails"></a>
# Moves mails to the given category
```
PUT /mail/categories?action=move
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**category_id**  <br>*required*|The identifier of a category.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|'A JSON array of mail identifier, e.g.: [{"id":ID, "folder_id":FID},{"id":ID2, "folder_id":FID2}, {...}]'|< [Mail_CategoriesMoveBody](#mail_categoriesmovebody) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'An empty response if everything went well. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* mail_categories


<a name="train"></a>
# Add a new rule
```
PUT /mail/categories?action=train
```


## Description
Adds a new rule with the given mail addresses to the given category and optionally reorganize all existing mails in the inbox.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**apply-for-existing**  <br>*optional*|A flag indicating whether old mails should be reorganized. Defaults to 'false'.|boolean||
|**Query**|**apply-for-future-ones**  <br>*optional*|A flag indicating whether a rule should be created or not. Defaults to 'true'.|boolean||
|**Query**|**category_id**  <br>*required*|The identifier of a category.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|'A JSON object containing a "from" field which contains an array of mail addresses.'|[Mail_CategoriesTrainBody](#mail_categoriestrainbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'An empty response if everything went well. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* mail_categories


<a name="unread"></a>
# Retrieves the unread counts of active mail categories
```
GET /mail/categories?action=unread
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**category_ids**  <br>*optional*|A comma separated list of category identifiers. If set only the unread counters of this categories are retrieved.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'A JSON object with a field for each active category containing the number of unread messages. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[Mail_CategoriesUnreadResponse](#mail_categoriesunreadresponse)|


## Tags

* mail_categories


<a name="getallmails"></a>
# Gets all mails.
```
GET /mail?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|string||
|**Query**|**filter**  <br>*optional*|The category id to filter for. If set to "general" all mails which does not belong to any other category are retrieved.|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**left_hand_limit**  <br>*optional*|A positive integer number to specify the "left-hand" limit of the range to return.|integer||
|**Query**|**limit**  <br>*optional*|A positive integer number to specify how many items shall be returned according to given sorting; overrides `left_hand_limit`/`right_hand_limit` parameters and is equal to `left_hand_limit=0` and `right_hand_limit=<limit>`.|integer||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**right_hand_limit**  <br>*optional*|A positive integer number to specify the "right-hand" limit of the range to return.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'A JSON object containing an array with mail data. Each array element describes one mail and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[MailsResponse](#mailsresponse)|


## Tags

* mail


<a name="markallmailsasread"></a>
# Marks all mails of a folder as seen (**available since v7.6.0**).
```
PUT /mail?action=all_seen
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the value `true`. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailsAllSeenResponse](#mailsallseenresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="getmailattachment"></a>
# Gets a mail attachment.
```
GET /mail?action=attachment
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attachment**  <br>*optional*|ID of the requested attachment (can be substituted by the parameter `cid` otherwise this parameter is **madatory**).|string||
|**Query**|**cid**  <br>*optional*|Value of header 'Content-ID' of the requested attachment (can be substituted by the parameter `attachment` otherwise this parameter is **madatory**).|string||
|**Query**|**filter**  <br>*optional*|1 to apply HTML white-list filter rules if and only if requested attachment is of MIME type `text/htm*` **AND** parameter `save` is set to 0.|integer||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*required*|Object ID of the mail which contains the attachment.|string||
|**Query**|**save**  <br>*optional*|1 overwrites the defined mimetype for this attachment to force the download dialog, otherwise 0.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The raw byte data of the document. The response type for the HTTP Request is set accordingly to the defined mimetype for this attachment, except the parameter save is set to 1.|string(binary)|


## Tags

* mail


<a name="clearmailfolders"></a>
# Clears the content of mail folders.
```
PUT /mail?action=clear
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*optional*|Not IMAP: timestamp of the last update of the deleted mails.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array with object IDs of the mail folders that shall be cleared.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON array with IDs of mail folder that could not be cleared; meaning the response body is an empty<br>JSON array if everything went well. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailsCleanUpResponse](#mailscleanupresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="copymail"></a>
# Copies a mail to another folder.
```
PUT /mail?action=copy
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested mail that shall be copied.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the id of the destination folder.|[MailDestinationBody](#maildestinationbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the object ID and the folder ID of the copied mail. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailDestinationResponse](#maildestinationresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="getmailcount"></a>
# Gets the mail count.
```
GET /mail?action=count
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'A JSON object containing an integer value representing the folder's mail count. Not IMAP: with timestamp. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[MailCountResponse](#mailcountresponse)|


## Tags

* mail


<a name="deletemails"></a>
# Deletes mails.
```
PUT /mail?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*optional*|Not IMAP: timestamp of the last update of the deleted mails.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id and folder of the mails.|< [MailListElement](#maillistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Not IMAP: A JSON array with object IDs of mails which were modified after the specified timestamp and<br>were therefore not deleted. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailsCleanUpResponse](#mailscleanupresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="forwardmail"></a>
# Forwards a mail.
```
GET /mail?action=forward
```


## Description
Returns the data for the message that shall be forwarded.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested message.|string||
|**Query**|**max_size**  <br>*optional*|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**view**  <br>*optional*|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|enum (text, html)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailReplyResponse](#mailreplyresponse)|


## Tags

* mail


<a name="getmail"></a>
# Gets a mail.
```
GET /mail?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attach_src**  <br>*optional*|1 to let the JSON mail representation being extended by "source" field containing the mail raw RFC822 source data. (available since v7.6.1)|integer||
|**Query**|**edit**  <br>*optional*|1 indicates that this request should fill the message compose dialog to edit a message and thus display-specific date is going to be withheld.|integer||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*optional*|Object ID of the requested mail (can be substituded by `message_id` parameter).|string||
|**Query**|**max_size**  <br>*optional*|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|integer||
|**Query**|**message_id**  <br>*optional*|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**unseen**  <br>*optional*|Use `true` to leave an unseen mail as unseen although its content is requested.|boolean||
|**Query**|**view**  <br>*optional*|Specifies the view of the mail's body: raw (returns the content as it is, meaning no preparation are performed and thus no guarantee for safe contents is given (available since SP6 v6.10)), text ( forces the server to deliver a text-only version of the requested mail's body, even if content is HTML), textNoHtmlAttach (is the same as 'text', but does not deliver the HTML part as attachment in case of multipart/alternative content), html (to allow a possible HTML mail body being transferred as it is (but white-list filter applied)), noimg (to allow a possible HTML content being transferred but without original image src attributes which references external images; can be used to prevent loading external linked images (spam privacy protection)). **If set, the corresponding gui config setting will be ignored.**|enum (raw, text, textNoHtmlAttach, html, noimg)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing all data of the requested mail. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailResponse](#mailresponse)|


## Tags

* mail


<a name="getmailheaders"></a>
# Gets the message headers as plain text.
```
GET /mail?action=get&hdr=1
```


## Description
## Note
By setting the query parameter `hdr` to 1 the response type of the request action changes. Then
it is returned a JSON object with the field `data` containing the (formatted) message headers
as plain text.
The parameters below specify the ones that have an effect on the request.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*optional*|Object ID of the requested mail (can be substituded by `message_id` parameter).|string||
|**Query**|**message_id**  <br>*optional*|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**unseen**  <br>*optional*|Use `true` to leave an unseen mail as unseen although its content is requested.|boolean||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the message headers as plain text. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailHeadersResponse](#mailheadersresponse)|


## Tags

* mail


<a name="getmailsource"></a>
# Gets the complete message source as plain text.
```
GET /mail?action=get&src=1
```


## Description
## Note
By setting the query parameter `src` to 1 the response type of the request action changes. Then
it is returned a JSON object with the field `data` containing the complete message source as plain text.
The parameters below specify the ones that have an effect on the request.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*optional*|Object ID of the requested mail (can be substituded by `message_id` parameter).|string||
|**Query**|**message_id**  <br>*optional*|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|string||
|**Query**|**save**  <br>*optional*|1 to write the complete message source to output stream. **NOTE:** This parameter will only be used if parameter `src` is set to 1.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**unseen**  <br>*optional*|Use `true` to leave an unseen mail as unseen although its content is requested.|boolean||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the complete message source as plain text. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailSourceResponse](#mailsourceresponse)|


## Tags

* mail


<a name="importmail"></a>
# Import of mails as MIME data block (RFC822) (**available since v6.18**).
```
POST /mail?action=import
```


## Description
This request can be used to store a single or a lot of mails in the OX mail storage backend. This
action should be used instead of `/mail?action=new` because it is faster and tolerant to 8-bit encoded emails.

To import multiple mails add further form-data fields.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**flags**  <br>*optional*|In case the mail should be stored with status "read" (e.g. mail has been read already in the client inbox), the parameter "flags" has to be included. For information about mail flags see [Mail data](#/definitions/MailData) model.|string||
|**Query**|**folder**  <br>*required*|The ID of the folder into that the emails should be imported.|string||
|**Query**|**force**  <br>*optional*|If this parameter is set to `true`, the server skips checking the valid from address.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**file**  <br>*required*|The RFC822 encoded email as binary data.|file||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of JSON objects each with the folder identifier and the object ID<br>of the imported mail(s). In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[MailImportResponse](#mailimportresponse)|


## Consumes

* `multipart/form-data`


## Tags

* mail


<a name="getmaillist"></a>
# Gets a list of mails.
```
PUT /mail?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|string||
|**Query**|**headers**  <br>*optional*|(preliminary) A comma-separated list of header names. Each name requests denoted header from each mail.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id and folder of the requested mails.|< [MailListElement](#maillistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'A JSON object containing an array with mail data. Mails are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[MailsResponse](#mailsresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="sendmail"></a>
# Sends a mail.
```
POST /mail?action=new
```


## Description
The request accepts file fields in upload form that denote referenced files that are going to be appended
as attachments.
For "text/plain" mail bodies, the JSON boolean field "raw" may be specified inside the body's JSON
representation to signal that the text content shall be kept as-is; meaning to keep all formatting
intact.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**lineWrapAfter**  <br>*optional*|An integer value specifying the line-wrap setting (only effective for plain-text content); if absent the setting is taken from user's mail settings. Available with v7.8.1.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**FormData**|**json_0**  <br>*required*|Contains the rudimentary mail as JSON string (see [SendMailData](#/definitions/SendMailData) model) with just its message body<br>(as html content) defined in nested JSON array `attachments` and its header data (from, to,<br>subject, etc.). The field "content_type" defines whether the mail ought to be sent as plain text<br>("text/plain"), as html ("text/html") or as multipart/alternative ("ALTERNATIVE"). Sending a mail<br>requires some special fields inside JSON mail object. The field "infostore_ids" defines a JSON<br>array of infostore document ID(s) that ought to be appended to this mail as attachments. The field<br>"msgref" indicates the ID of the referenced original mail. Moreover the field "sendtype" indicates<br>the type of the message: 0 (normal new mail), 1 (a reply mail, field `msgref` must be present),<br>2 (a forward mail, field `msgref` must be present), 3 (draft edit operation, field `msgref` must be<br>present in order to delete previous draft message since e.g. IMAP does not support changing/replacing<br>a message but requires a delete-and-insert sequence), 4 (transport of a draft mail, field `msgref`<br>must be present), 6 (signals that user intends to send out a saved draft message and expects the draft<br>message (referenced by `msgref` field) being deleted after successful transport).|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page containing the object ID of the newly created mail or in case of errors an error object (see [File uploads](#file-uploads) as an example).|string|


## Consumes

* `multipart/form-data`
* `multipart/mixed`


## Produces

* `text/html`


## Tags

* mail


<a name="sendorsavemail"></a>
# Sends or saves a mail as MIME data block (RFC822) (**available since SP5**).
```
PUT /mail?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**flags**  <br>*optional*|In case the mail should be stored with status "read" (e.g. mail has been read already in the client<br>inbox), the parameter "flags" has to be included. If no `folder` parameter is specified, this parameter<br>must not be included. For information about mail flags see [Mail data](#/definitions/MailData) model.|string||
|**Query**|**folder**  <br>*optional*|In case the mail should not be sent out, but saved in a specific folder, the "folder" parameter<br>can be used. If the mail should be sent out to the recipient, the "folder" parameter must not be<br>included and the mail is stored in the folder "Sent Items".|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the folder ID and the object ID of the mail. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[MailDestinationResponse](#maildestinationresponse)|


## Consumes

* `text/plain`


## Tags

* mail


<a name="receiptmailack"></a>
# Requests a delivery receipt for a priviously sent mail.
```
PUT /mail?action=receipt_ack
```


## Description
This delivery receipt only acknowledges that the message could be receipted on the recipients computer.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the information of a mail for which a delivery receipt shall be requested.|[MailAckBody](#mailackbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with an empty data field if everything went well or a JSON object containing the error<br>information. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[MailAckResponse](#mailackresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="replymail"></a>
# Replies a mail.
```
GET /mail?action=reply
```


## Description
Returns the data for the message that shall be replied.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested message.|string||
|**Query**|**max_size**  <br>*optional*|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**setFrom**  <br>*optional*|A flag (`true`/`false`) that signals if "From" header shall be pre-selected according to a suitable recipient address that matches one of user's E-Mail address aliases. (available since v7.6.0)|boolean||
|**Query**|**view**  <br>*optional*|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|enum (text, html)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailReplyResponse](#mailreplyresponse)|


## Tags

* mail


<a name="replyallmail"></a>
# Replies a mail to all.
```
GET /mail?action=replyall
```


## Description
Returns the data for the message that shall be replied.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested message.|string||
|**Query**|**max_size**  <br>*optional*|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**setFrom**  <br>*optional*|A flag (`true`/`false`) that signals if "From" header shall be pre-selected according to a suitable recipient address that matches one of user's email address aliases. (available since v7.6.0)|boolean||
|**Query**|**view**  <br>*optional*|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|enum (text, html)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailReplyResponse](#mailreplyresponse)|


## Tags

* mail


<a name="resolvesharereference"></a>
# Resolves a given share reference
```
PUT /mail?action=resolve_share_reference
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object providing the share reference to resolve|[ResolveShareReferenceElement](#resolvesharereferenceelement)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'The JSON representation for the resolved share reference. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[ResolveShareReferenceResponse](#resolvesharereferenceresponse)|


## Consumes

* `application/json`


## Produces

* `application/json`


## Tags

* mail


<a name="searchmails"></a>
# Searches for mails.
```
PUT /mail?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Note: Applies only to root-level messages.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response or the string “thread” to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified. Note: Applies only to root-level messages.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the search term as introducted in [Advanced search](#advanced-search). Example:<br>`{"filter":["and",["=", {"field":"to"},"test1@example.com"],["not",["=",{"attachment":"name"},"document.pdf"]]]}`<br>which represents 'to = "test1@example.com" AND NOT from = "test2@example.com"'. Available field names are<br>`from`, `to`, `cc`, `bcc`, `subject`, `received_date`, `sent_date`, `size`, `flags`, `content`, `content_type`, `disp`, and `priority`.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|'A JSON object containing an array with matching mails. Mails are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).'|[MailsResponse](#mailsresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="getmailconversations"></a>
# Gets all mail conversations.
```
GET /mail?action=threadedAll
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**includeSent**  <br>*optional*|A boolean value to signal that conversations also include messages taken from special "sent" aka "sent items" folder.|boolean||
|**Query**|**left_hand_limit**  <br>*optional*|A positive integer number to specify the "left-hand" limit of the range to return. Note: Applies only to root-level messages.|integer||
|**Query**|**limit**  <br>*optional*|A positive integer number to specify how many items shall be returned according to given sorting; overrides `left_hand_limit`/`right_hand_limit` parameters and is equal to `left_hand_limit=0` and `right_hand_limit=<limit>`. Note: Applies only to root-level messages.|integer||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Note: Applies only to root-level messages.|string||
|**Query**|**right_hand_limit**  <br>*optional*|A positive integer number to specify the "right-hand" limit of the range to return. Note: Applies only to root-level messages.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response or the string “thread” to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified. Note: Applies only to root-level messages.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of objects, each representing a conversation's root message along<br>with its message thread. The root message's JSON object is filled according to the specified `columns`<br>and is enhanced by a special `thread` field representing the full message thread (including the root<br>message itself). `thread` is a JSON array of objects each representing a message in the conversation<br>sorted by time-line and filled with the specified `columns`. Not IMAP: with timestamp. In case of<br>errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[MailConversationsResponse](#mailconversationsresponse)|


## Tags

* mail


<a name="updatemail"></a>
# Updates a mail or a folder's messages and/or moves a mail to another folder.
```
PUT /mail?action=update
```


## Description
The update request can perform an update of the color label and flags of one mail object. Beside it
is possible to change the mail's folder, meaning move the mail to another folder. Both operations
can be performed at once too.

If neither parameter `id` nor parameter `message_id` is specified, all folder's messages are updated
accordingly (**available since v6.20).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*optional*|Object ID of the requested mail that shall be updated (**mandatory** if a mail shall be moved).|string||
|**Query**|**message_id**  <br>*optional*|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the new values that ought to be applied to mail and/or the id of the destination folder (if the mail shall be moved, otherwise it must not be specified).|[MailUpdateBody](#mailupdatebody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the object ID and the folder ID of an updated and/or moved mail or only<br>the folder ID if several mails are updated. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailDestinationResponse](#maildestinationresponse)|


## Consumes

* `application/json`


## Tags

* mail


<a name="getmailupdates"></a>
# Gets updated mails (not IMAP).
```
GET /mail?action=updates
```


## Description
## Not IMAP


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Just an empty JSON array is going to be returned since this action cannot be applied to IMAP. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MailUpdatesResponse](#mailupdatesresponse)|


## Tags

* mail


<a name="getattachmentsaszip"></a>
# Gets multiple mail attachments as a ZIP file.
```
GET /mail?action=zip_attachments
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attachment**  <br>*required*|A comma-separated list of IDs of the requested attachments.|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*required*|Object ID of the mail which contains the attachments.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The raw byte data of the ZIP file.|string(binary)|


## Produces

* `application/zip`


## Tags

* mail


<a name="getmailsaszip"></a>
# Gets multiple mails as a ZIP file.
```
GET /mail?action=zip_messages
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the mails.|string||
|**Query**|**id**  <br>*required*|A comma-separated list of Object IDs of the requested mails.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The raw byte data of the ZIP file.|string(binary)|


## Produces

* `application/zip`


## Tags

* mail


<a name="getconfig"></a>
# Gets the configuration of the mail filter backend.
```
GET /mailfilter?action=config
```


## Description
A mail filter can have different rules each containing one command. A command has a test condition and actions
that are executed if the condition is true. The list of available comparisions (that can be used in test
conditions) and the list of available actions depends on a given test and the mail filter server configuration
and must be determined at runtime.

All those dynamic values can be fetched via a config object at startup, which shows the capabilities of the server
to the client.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the fields `tests` (containing an array of available test-objects, see [Possible tests](#possible-tests) too) and `actioncommands`<br>(containing an array of [valid actions](#possible-action-commands)). In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailFilterConfigResponse](#mailfilterconfigresponse)|


## Tags

* mailfilter


<a name="deleterule"></a>
# Deletes a mail filter rule.
```
PUT /mailfilter?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||
|**Body**|**body**  <br>*required*|A JSON object with the ID of the rule to delete.|[MailFilterDeletionBody](#mailfilterdeletionbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* mailfilter


<a name="deletescript"></a>
# Deletes the whole mail filter script.
```
PUT /mailfilter?action=deletescript
```


## Description
This call is only used as workaround for parsing errors in the backend, so that the user is able to kick a whole script if it contains errors in the grammar.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* mailfilter


<a name="getscript"></a>
# Gets the whole mail filter script.
```
PUT /mailfilter?action=getscript
```


## Description
This call is only used as workaround for parsing errors in the backend, so that the user is able to get the plaintext of a complete script.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the text of the complete sieve script. In case of errors the responsible fields<br>in the response are filled (see [Error handling](#error-handling)).|[MailFilterScriptResponse](#mailfilterscriptresponse)|


## Tags

* mailfilter


<a name="getrules"></a>
# Gets all mail filter rules.
```
GET /mailfilter?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**flag**  <br>*optional*|If given, only rules with this flag are returned.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with an array of rule-objects. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailFilterRulesResponse](#mailfilterrulesresponse)|


## Tags

* mailfilter


<a name="createrule"></a>
# Creates a mail filter rule.
```
PUT /mailfilter?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the mail filter rule. If the field `position` is included, it's taken as the position of the rule in the array on the server side (this value shouldn't be greater than the size of all rules).|[MailFilterRule](#mailfilterrule)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the ID of the newly created rule. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MailFilterCreationResponse](#mailfiltercreationresponse)|


## Consumes

* `application/json`


## Tags

* mailfilter


<a name="reorderrules"></a>
# Reorders mail filter rules.
```
PUT /mailfilter?action=reorder
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||
|**Body**|**body**  <br>*required*|A JSON array with unique identifiers, which represents how the corresponding rules are order.|< integer > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* mailfilter


<a name="updaterule"></a>
# Updates a mail filter rule.
```
PUT /mailfilter?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**username**  <br>*optional*|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the rule with the `id` set (which identifies the rule to change). Only modified fields are present.|[MailFilterRule](#mailfilterrule)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* mailfilter


<a name="getallmessagingaccounts"></a>
# Gets all messaging accounts.
```
GET /messaging/account?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**messagingService**  <br>*optional*|List only those accounts that belong to the given `messagingService`.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with account objects. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[MessagingAccountsResponse](#messagingaccountsresponse)|


## Tags

* messaging


<a name="deletemessagingaccount"></a>
# Deletes a messaging account.
```
GET /messaging/account?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The messaging account ID.|integer||
|**Query**|**messagingService**  <br>*required*|The messaging service ID that the account belongs to.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the number 1 if deletion was successful. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MessagingAccountUpdateResponse](#messagingaccountupdateresponse)|


## Consumes

* `application/json`


## Tags

* messaging


<a name="getmessagingaccount"></a>
# Gets a messaging account.
```
GET /messaging/account?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The messaging account ID.|integer||
|**Query**|**messagingService**  <br>*required*|The messaging service ID that the account belongs to.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the requested account. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[MessagingAccountResponse](#messagingaccountresponse)|


## Tags

* messaging


<a name="createmessagingaccount"></a>
# Creates a messaging account.
```
PUT /messaging/account?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the account to create. The ID is generated by the server and must not be present.|[MessagingAccountData](#messagingaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the ID of the newly created account. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MessagingAccountUpdateResponse](#messagingaccountupdateresponse)|


## Consumes

* `application/json`


## Tags

* messaging


<a name="updatemessagingaccount"></a>
# Updates a messaging account.
```
PUT /messaging/account?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the modified data of the account. The fields `id` and `messagingService` must always be set.|[MessagingAccountData](#messagingaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the number 1 if update was successful. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[MessagingAccountUpdateResponse](#messagingaccountupdateresponse)|


## Consumes

* `application/json`


## Tags

* messaging


<a name="getallmessagingmessages"></a>
# Gets all messaging messages.
```
GET /messaging/message?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of column names, like "folder,headers,body". See [Messaging fields](#messaging fields) for valid column names.|string||
|**Query**|**folder**  <br>*required*|The folder ID, like "com.openexchange.messaging.twitter://535/defaultTimeline/directMessages".|string||
|**Query**|**order**  <br>*optional*|The order direction which can be "asc" for ascending (default) or "desc" for descending.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|A column name to sort by.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all messages. Each array element describes one message and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>column names in the `columns` parameter. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[MessagingMessagesResponse](#messagingmessagesresponse)|


## Tags

* messaging


<a name="getmessagingmessage"></a>
# Gets a messaging message.
```
GET /messaging/message?action=get
```


## Description
A messaging message consists of some metadata, headers and a content. The content attribute varies
by the content-type header. If the content-type is `text/*` it is a string, if it is `multipart/*` it
is an array of objects, each representing a part of the multipart. If it is anything else it is considered binary
and is a Base64 encoded string.

The folder ID of a message follows a predefined format: `[messagingService]://[accountId]/[path]`, like
`com.openexchange.messaging.twitter://535/defaultTimeline/directMessages`.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|The folder ID of the message.|string||
|**Query**|**id**  <br>*required*|The ID of the message to load.|string||
|**Query**|**peek**  <br>*optional*|If set to `true` the read/unread state of the message will not change. Default is `false`.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the message. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MessagingMessageResponse](#messagingmessageresponse)|


## Tags

* messaging


<a name="getmessagingmessagelist"></a>
# Gets a list of messaging messages.
```
PUT /messaging/message?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of column names, like "folder,headers,body". See [Messaging fields](#messaging fields) for valid column names.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|< < object > array > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for requested messages. Each array element describes one message and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>column names in the `columns` parameter. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[MessagingMessagesResponse](#messagingmessagesresponse)|


## Consumes

* `application/json`


## Tags

* messaging


<a name="performmessagingaction"></a>
# Performs a certain messaging action on a message.
```
PUT /messaging/message?action=perform
```


## Description
On actions of type "message" the body should contain the JSON representation of the message the action should be applied to.
To invoke a messaging action of type "storage" the folder and id are needed in URL parameters.
Messaging actions of type "none" need a messaging message and account.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**account**  <br>*optional*|The account ID. Only used on actions of type "none".|integer||
|**Query**|**folder**  <br>*optional*|The folder ID of the message. Only used on actions of type "storage".|string||
|**Query**|**id**  <br>*optional*|The ID of the message the action shall be invoked on. Only used on actions of type "storage".|string||
|**Query**|**messageAction**  <br>*required*|The message action to invoke.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|[MessagingMessageData](#messagingmessagedata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the number 1 if message could be sent. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MessagingMessageUpdateResponse](#messagingmessageupdateresponse)|


## Consumes

* `spplication/json`


## Tags

* messaging


<a name="sendmessagingmessage"></a>
# Sends a messaging message.
```
PUT /messaging/message?action=send
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**recipients**  <br>*optional*|A list of recipients as defined in RFC822, like "Joe Doe <joe@doe.org>". If set the message is sent to the given list of recipients, otherwise this defaults to the "To" header of the message.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|[MessagingMessageData](#messagingmessagedata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the number 1 if message could be sent. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[MessagingMessageUpdateResponse](#messagingmessageupdateresponse)|


## Consumes

* `spplication/json`


## Tags

* messaging


<a name="getallmessagingservices"></a>
# Gets all messaging services.
```
GET /messaging/service?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of messaging service objects. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[MessagingServicesResponse](#messagingservicesresponse)|


## Tags

* messaging


<a name="getmessagingservice"></a>
# Gets a messaging service.
```
GET /messaging/service?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the messaging service to load.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the messaging service. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[MessagingServiceResponse](#messagingserviceresponse)|


## Tags

* messaging


<a name="process"></a>
# Processes multiple requests to other modules in a single request.
```
PUT /multiple
```


## Description
## Not supported requests are:
 * the ones from modules login and multiple
 * POST requests with a multipart encoding (uploads)
 * GET requests which do not use an object as described in [Low level protocol](#low-level-protocol)

## Request body
A JSON array with JSON objects describing the requests. Each object contains a field `module` with the
name of the request's module and the field `action` with the concrete request action to execute. Additionally the
parameters of the request are added as fields too. A session parameter is not included! If the request has
a request body itself, this body is stored as a JSON object in a field `data`.

## Example: query reminder range and update a reminder's alarm
```json
[{"module":"reminder","action":"range","end":1497461067180},{"module":"reminder","action":"remindAgain","id":51,"data":{"alarm":1459478800000}}]
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**continue**  <br>*optional*|Specifies whether processing of requests should stop when an error occurs, or whether all request should be processed regardless of errors.|boolean||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with JSON objects, each describing one request.|< [SingleRequest](#singlerequest) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON array containing the response data of the processed requests where response[0] corresponds to request[0], response[1] to request[1], and so on.|< [SingleResponse](#singleresponse) > array|
|**400**|Syntactically incorrect request.|No Content|


## Consumes

* `application/json`


## Tags

* multiple


<a name="getalloauthaccounts"></a>
# Gets all OAuth accounts (**available since v6.20**).
```
GET /oauth/accounts?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**serviceId**  <br>*optional*|The service meta data identifier. If missing all accounts of all services are returned; otherwise all accounts of specified service are returned.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of JSON objects each describing an OAuth account. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthAccountsResponse](#oauthaccountsresponse)|


## Tags

* OAuth


<a name="createoauthaccount"></a>
# Creates an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=create
```


## Description
This action is typically called by provided call-back URL and is only intended for manual invocation if
"outOfBand" interaction is returned by preceeding `/oauth/account?action=init` step.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**displayName**  <br>*required*|The display name for the new account.|string||
|**Query**|**oauth_token**  <br>*required*|The request token from preceeding OAuth interaction.|string||
|**Query**|**oauth_verifier**  <br>*optional*|The verifier string which confirms that user granted access.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**uuid**  <br>*required*|The UUID of the preceeding OAuth interaction.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the newly created OAuth account. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthAccountResponse](#oauthaccountresponse)|


## Tags

* OAuth


<a name="deleteoauthaccount"></a>
# Deletes an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The account identifier.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object indicating whether the deletion was successful. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthAccountDeletionResponse](#oauthaccountdeletionresponse)|


## Tags

* OAuth


<a name="getoauthaccount"></a>
# Gets an OAuth account (**available since v6.20**).
```
GET /oauth/accounts?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The account identifier.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the OAuth account. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthAccountResponse](#oauthaccountresponse)|


## Tags

* OAuth


<a name="initoauthaccount"></a>
# Initializes the creation of an OAuth account (**available since v6.20**).
```
GET /oauth/accounts?action=init
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**displayName**  <br>*required*|The display name of the account.|string||
|**Query**|**serviceId**  <br>*required*|The service meta data identifier, e.g. "com.openexchange.oauth.twitter".|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the resulting interaction providing information to complete account creation.<br>In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[OAuthAccountInteractionResponse](#oauthaccountinteractionresponse)|


## Tags

* OAuth


<a name="updateoauthaccount"></a>
# Updates an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The account identifier. May also be provided in request body's JSON object by field `id`.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object providing the OAuth account data to update. Currently the only values which make sense being updated are `displayName` and the `token`-`secret`-pair.|[OAuthAccountData](#oauthaccountdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object indicating whether the update was successful. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthAccountUpdateResponse](#oauthaccountupdateresponse)|


## Consumes

* `application/json`


## Tags

* OAuth


<a name="getalloauthgrants"></a>
# Gets all OAuth grants (**available since v7.8.0**).
```
GET /oauth/grants?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of JSON objects each describing a granted access. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthGrantsResponse](#oauthgrantsresponse)|


## Tags

* OAuth


<a name="revokeoauthgrant"></a>
# Revokes access for an OAuth client (**available since v7.8.0**).
```
GET /oauth/grants?action=revoke
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**client**  <br>*required*|The ID of the client whose access shall be revoked.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Tags

* OAuth


<a name="getalloauthservices"></a>
# Gets all OAuth services' meta data (**available since v6.20**).
```
GET /oauth/services?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of JSON objects each describing an OAuth service's meta data. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthServicesResponse](#oauthservicesresponse)|


## Tags

* OAuth


<a name="getoauthservice"></a>
# Gets all OAuth service's meta data (**available since v6.20**).
```
GET /oauth/services?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The service's identifier.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the meta data of the OAuth service. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[OAuthServiceResponse](#oauthserviceresponse)|


## Tags

* OAuth


<a name="updatepassword"></a>
# Updates or changes the password of the current use.
```
PUT /passwordchange?action=update
```


## Description
## Note
The new password will be set without any checks. The client must ensure that it is the password the user wants to set.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the old and the new password.|[PasswordChangeBody](#passwordchangebody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* passwordchange


<a name="getfilestoreusage"></a>
# Gets the filestore usage data.
```
GET /quota?action=filestore
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the filestore quota. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[QuotaResponse](#quotaresponse)|


## Tags

* quota


<a name="getquotainformation"></a>
# Gets quota information (**available since v7.6.1, preliminary**).
```
GET /quota?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**account**  <br>*optional*|The account identifier within the module to get quota information for.|string||
|**Query**|**module**  <br>*optional*|The module identifier (e.g. "share_links", "filestorage", ...) to get quota information for, required if account is set.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|If `module` is not specified it is returned a JSON object containing all quota modules as fields.<br>Each field is an object itself consisting of a field `display_name` and a field `accounts`.<br>`accounts` is an array of JSON objects containing the properties `account_id`, `account_name`,<br>`countquota` (account's quota limit for the number of items, or not set if not defined),<br>`countuse` (account's actual usage for the number of items, or not set if no count quota defined),<br>`quota` (account's quota limit for the storage in bytes, or not set if not defined) and `use`<br>(account's actual usage for the storage in bytes, or not set if no storage quota defined). In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[QuotasResponse](#quotasresponse)|
|**400**|If a specified `module` is not existing.|No Content|


## Tags

* quota


<a name="getmailusage"></a>
# Gets the mail usage data.
```
GET /quota?action=mail
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the mail quota. In case of errors the responsible<br>fields in the response are filled (see [Error handling](#error-handling)).|[QuotaResponse](#quotaresponse)|


## Tags

* quota


<a name="deletereminders"></a>
# Deletes reminders (**available since v6.22**).
```
PUT /reminder?action=delete
```


## Description
Before version 6.22 the request body contained only a JSON object with the field `id`.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with JSON objects containing the field `id` of the reminders to delete.|< [ReminderListElement](#reminderlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with identifiers of reminders that were not deleted. In case of<br>errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[RemindersResponse](#remindersresponse)|


## Consumes

* `application/json`


## Tags

* reminder


<a name="getrange"></a>
# Gets a reminder range.
```
GET /reminder?action=range
```


## Description
Gets all reminders which are scheduled until the specified time (end date).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**end**  <br>*optional*|The end date of the reminder range.|integer(int64)||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for each reminder. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[RemindersResponse](#remindersresponse)|


## Tags

* reminder


<a name="remindagain"></a>
# Updates the reminder alarm (**available since v6.18.1**).
```
PUT /reminder?action=remindAgain
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the reminder whose alarm date shall be changed.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the field `alarm` which provides the new reminder date.|[ReminderUpdateBody](#reminderupdatebody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the updated reminder. In case of<br>errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[ReminderResponse](#reminderresponse)|


## Consumes

* `application/json`


## Tags

* reminder


<a name="getallresources"></a>
# Gets all resources.
```
GET /resource?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of resource identifiers. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[AllResourcesResponse](#allresourcesresponse)|


## Tags

* resources


<a name="deleteresources"></a>
# Deletes resources.
```
PUT /resource?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the group to delete.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array of objects with the field `id` containing the unique identifier of the resource.|< [ResourceListElement](#resourcelistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with an empty array if the resources were deleted successfully. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[ResourcesResponse](#resourcesresponse)|


## Consumes

* `application/json`


## Tags

* resources


<a name="getresource"></a>
# Gets a resource.
```
GET /resource?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the resource.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the resource data. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[ResourceResponse](#resourceresponse)|


## Tags

* resources


<a name="getresourcelist"></a>
# Gets a list of resources.
```
PUT /resource?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id of the requested resources.|< [ResourceListElement](#resourcelistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of resource objects. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[ResourcesResponse](#resourcesresponse)|


## Consumes

* `application/json`


## Tags

* resources


<a name="createresource"></a>
# Creates a resource.
```
PUT /resource?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the resource data. The field `id` is not present.|[ResourceData](#resourcedata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the ID of the newly created resource. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[ResourceUpdateResponse](#resourceupdateresponse)|


## Consumes

* `application/json`


## Tags

* resources


<a name="searchresources"></a>
# Searches for resources.
```
PUT /resource?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object with the search parameters.|[ResourceSearchBody](#resourcesearchbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of resource objects. In case of errors the responsible fields in the<br>response are filled (see [Error handling](#error-handling)).|[ResourcesResponse](#resourcesresponse)|


## Consumes

* `application/json`


## Tags

* resources


<a name="updateresource"></a>
# Updates a resource.
```
PUT /resource?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|ID of the resource that shall be updated.|integer||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the resource to update. If the resource was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing the resource data fields to change. Only modified fields are present and the field id is omitted.|[ResourceData](#resourcedata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* resources


<a name="getresourceupdates"></a>
# Gets the new, modified and deleted resources.
```
GET /resource?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested resources.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with fields `new`, `modified` and `deleted` representing arrays of new, modified and<br>deleted resource objects. In case of errors the responsible fields in the response are filled<br>(see [Error handling](#error-handling)).|[ResourceUpdatesResponse](#resourceupdatesresponse)|


## Tags

* resources


<a name="deletesharelink"></a>
# Deletes a share link (**available since v7.8.0**).
```
PUT /share/management?action=deleteLink
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the share target where the link should be deleted for.|[ShareTargetData](#sharetargetdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* share/management


<a name="getsharelink"></a>
# Creates or gets a share link (**available since v7.8.0**).
```
PUT /share/management?action=getLink
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the share target where the link should be generated for.|[ShareTargetData](#sharetargetdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing data of the (newly created) share link. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ShareLinkResponse](#sharelinkresponse)|


## Consumes

* `application/json`


## Tags

* share/management


<a name="sendsharelink"></a>
# Sends a share link (**available since v7.8.0**).
```
PUT /share/management?action=sendLink
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the share target and a list of recipients specified in a field `recipients` that<br>is a JSON array with a nested two-elements array containing the recipient information (first element is<br>personal name, second is email address). An optional field `message` can contain a notification.|[ShareLinkSendBody](#sharelinksendbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Transport warnings that occurred during sending the notifications are available in a `warnings` array.<br>In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[ShareLinkSendResponse](#sharelinksendresponse)|


## Consumes

* `application/json`


## Tags

* share/management


<a name="updatesharelink"></a>
# Updates a share link (**available since v7.8.0**).
```
PUT /share/management?action=updateLink
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|The timestamp of the last modification of the link. Used to detect concurrent modifications.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing the share target and share link properties of the link to update. Only modified fields should be set but at least the share target ones.|[ShareLinkUpdateBody](#sharelinkupdatebody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing data of the (newly created) share link. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[ShareLinkResponse](#sharelinkresponse)|


## Consumes

* `application/json`


## Tags

* share/management


<a name="getallsnippets"></a>
# Gets all snippets (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**type**  <br>*optional*|A list of comma-separated types to filter, e.g. "signature".|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all snippets. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[SnippetsResponse](#snippetsresponse)|


## Tags

* snippet


<a name="addsnippetattachment"></a>
# Attaches one or more files to an existing snippet (**available since v7.0.0/v6.22.0**).
```
POST /snippet?action=attach
```


## Description
It can be uploaded multiple files at once. Each file must be specified in an own form field
(the form field name is arbitrary).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The identifier of the snippet.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**type**  <br>*required*|The file type filter to define which file types are allowed during upload. Currently supported filters are: file (for all), text (for `text/*`), media (for image, audio or video), image (for `image/*`), audio (for `audio/*`), video (for `video/*`) and application (for `application/*`).|enum (file, text, media, image, audio, video, application)||
|**FormData**|**file**  <br>*required*|The attachment file.|file||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the ID of<br>the updated snippet or errors if some occurred.|string|


## Consumes

* `multipart/form-data`


## Produces

* `text/html`


## Tags

* snippet


<a name="deletesnippet"></a>
# Deletes one or multiple snippets (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=delete
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*optional*|The identifier of the snippet. Otherwise provide one or more identifiers in the request body's JSON array.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*optional*|A JSON array containing the identifiers of the snippets to delete.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* snippet


<a name="removesnippetattachments"></a>
# Detaches one or more files from an existing snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=detach
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The identifier of the snippet.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array with JSON objects each containing a field `id` with the identifier of an attachment that shall be removed.|< [SnippetAttachmentListElement](#snippetattachmentlistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the ID of the updated snippet. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[SnippetUpdateResponse](#snippetupdateresponse)|


## Consumes

* `application/json`


## Tags

* snippet


<a name="getsnippet"></a>
# Gets a snippet (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The identifier of the snippet.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the snippet. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[SnippetResponse](#snippetresponse)|


## Tags

* snippet


<a name="getsnippetattachment"></a>
# Gets the attachment of a snippet (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=getattachment
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**attachmentid**  <br>*required*|The identifier of the attachment.|string||
|**Query**|**id**  <br>*required*|The identifier of the snippet.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|The attachment's raw data.|string(binary)|
|**500**|A HTML page in case of errors.|string|


## Tags

* snippet


<a name="getsnippetlist"></a>
# Gets a list of snippets (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of snippet identifiers.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested snippets. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[SnippetsResponse](#snippetsresponse)|


## Consumes

* `application/json`


## Tags

* snippet


<a name="createsnippet"></a>
# Creates a snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object describing the snippet excluding its attachment(s). For adding attachments see `/snippet?action=attach` request.|[SnippetData](#snippetdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the ID of the newly created snippet. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[SnippetUpdateResponse](#snippetupdateresponse)|


## Consumes

* `application/json`


## Tags

* snippet


<a name="updatesnippet"></a>
# Updates a snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The identifier of the snippet.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object providing the fields that should be changed, excluding its attachments. For deleting attachments see `/snippet?action=detach` request.|[SnippetData](#snippetdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the updated snippet. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[SnippetResponse](#snippetresponse)|


## Consumes

* `application/json`


## Tags

* snippet


<a name="clearfolderssynced"></a>
# Clears a folder's content.
```
PUT /sync?action=refresh_server
```


## Description
## Note
Although the request offers to clear multiple folders at once it is recommended to clear only one folder per
request since if any exception occurs (e.g. missing permissions) the complete request is going to be aborted.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*optional*|A JSON array containing the folder ID(s).|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array of folder IDs that could not be cleared due to a concurrent modification.<br>In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[FoldersCleanUpResponse](#folderscleanupresponse)|


## Tags

* sync


<a name="getalltasks"></a>
# Gets all tasks.
```
GET /tasks?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the tasks.|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all tasks. Each array element describes one task and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[TasksResponse](#tasksresponse)|


## Tags

* tasks


<a name="confirmtask"></a>
# Confirms a task.
```
PUT /tasks?action=confirm
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the tasks.|string||
|**Query**|**id**  <br>*required*|Object ID of the task that shall be confirmed.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the task.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object with the fields `confirmation` and `confirmmessage`.|[TaskConfirmationBody](#taskconfirmationbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Nothing, except the standard response object with empty data, the timestamp of the confirmed and thereby<br>updated task, and maybe errors. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* tasks


<a name="deletetasks"></a>
# Deletes tasks (**available since v6.22**).
```
PUT /tasks?action=delete
```


## Description
Before version 6.22 the request body contained a JSON object with the fields `id` and `folder` and could
only delete one task.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the deleted tasks.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id and folder of the tasks.|< [TaskListElement](#tasklistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON array with object IDs of tasks which were modified after the specified timestamp and were therefore not deleted. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[TaskDeletionsResponse](#taskdeletionsresponse)|


## Consumes

* `application/json`


## Tags

* tasks


<a name="gettask"></a>
# Gets a task.
```
GET /tasks?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the tasks.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested task.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An object containing all data of the requested task. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[TaskResponse](#taskresponse)|


## Tags

* tasks


<a name="gettasklist"></a>
# Gets a list of tasks.
```
PUT /tasks?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of JSON objects with the id and folder of the tasks.|< [TaskListElement](#tasklistelement) > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested tasks. Each array element describes one task and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[TasksResponse](#tasksresponse)|


## Consumes

* `application/json`


## Tags

* tasks


<a name="createtask"></a>
# Creates a task.
```
PUT /tasks?action=new
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON object containing the task's data.|[TaskData](#taskdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the id of the newly created task. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[TaskUpdateResponse](#taskupdateresponse)|


## Consumes

* `application/json`


## Tags

* tasks


<a name="searchtasks"></a>
# Search for tasks.
```
PUT /tasks?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|string||
|**Body**|**body**  <br>*required*|A JSON object containing search parameters.|[TaskSearchBody](#tasksearchbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching tasks. Tasks are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[TasksResponse](#tasksresponse)|


## Consumes

* `application/json`


## Tags

* tasks


<a name="updatetask"></a>
# Updates a task.
```
PUT /tasks?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the tasks.|string||
|**Query**|**id**  <br>*required*|Object ID of the requested task.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested tasks.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing the task's data. Only modified fields are present.|[TaskData](#taskdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with a timestamp. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[TaskUpdateResponse](#taskupdateresponse)|


## Consumes

* `application/json`


## Tags

* tasks


<a name="gettaskupdates"></a>
# Gets the new, modified and deleted tasks.
```
GET /tasks?action=updates
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|string||
|**Query**|**folder**  <br>*required*|Object ID of the folder who contains the tasks.|string||
|**Query**|**ignore**  <br>*optional*|Which kinds of updates should be ignored. Omit this parameter or set it to "deleted" to not have deleted tasks identifier in the response. Set this parameter to `false` and the response contains deleted tasks identifier.|enum (deleted, )||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested tasks.|integer(int64)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An array with new, modified and deleted tasks. New and modified tasks are represented by arrays. The<br>elements of each array contain the information specified by the corresponding identifiers in the `columns`<br>parameter. Deleted tasks would be identified by their object IDs as integers, without being part of<br>a nested array. In case of errors the responsible fields in the response are filled (see<br>[Error handling](#error-handling)).|[TaskUpdatesResponse](#taskupdatesresponse)|


## Tags

* tasks


<a name="acquiretoken"></a>
# Gets a login token (**available since v7.4.0**).
```
GET /token?action=acquireToken
```


## Description
With a valid session it is possible to acquire a secret. Using this secret another system is able to
generate a valid session (see `login?action=redeemToken`).


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object with the timestamp of the creation date and a token which can be used to create a new<br>session. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[AcquireTokenResponse](#acquiretokenresponse)|


## Tags

* token


<a name="getcurrentuser"></a>
# Gets information about current user (**available since v7.6.2**).
```
GET /user/me
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the data of the current user. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[CurrentUserResponse](#currentuserresponse)|


## Tags

* user/me


<a name="getallusers"></a>
# Gets all users (**available since v6.14**).
```
GET /user?action=all
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for all users. Each array element describes one user and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[UsersResponse](#usersresponse)|


## Tags

* user


<a name="getuser"></a>
# Gets a user (**available since v6.14**).
```
GET /user?action=get
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*optional*|Object ID of the requested user. Since v6.18.1, this parameter is optional and the default is the currently logged in user.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|An object containing all data of the requested user. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[UserResponse](#userresponse)|


## Tags

* user


<a name="getuserattribute"></a>
# Gets a user attribute (**available since v6.20**).
```
GET /user?action=getAttribute
```


## Description
Gets a custom user attribute that was previously set with the `/user?action=setAttribute` request.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the user.|string||
|**Query**|**name**  <br>*required*|The name of the attribute.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing the attribute data. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[UserAttributeResponse](#userattributeresponse)|


## Tags

* user


<a name="getuserlist"></a>
# Gets a list of users (**available since v6.14**).
```
PUT /user?action=list
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Body**|**body**  <br>*required*|A JSON array of identifiers of the requested users. Since v6.18.1, a `null` value in the array is interpreted as the currently logged in user.|< string > array||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with data for the requested users. Each array element describes one user and<br>is itself an array. The elements of each array contain the information specified by the corresponding<br>identifiers in the `columns` parameter. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[UsersResponse](#usersresponse)|


## Consumes

* `application/json`


## Tags

* user


<a name="searchusers"></a>
# Search for users (**available since v6.14**).
```
PUT /user?action=search
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**columns**  <br>*required*|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|string||
|**Query**|**order**  <br>*optional*|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**sort**  <br>*optional*|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|string||
|**Body**|**body**  <br>*required*|A JSON object containing search parameters.|[UserSearchBody](#usersearchbody)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object containing an array with matching users. Users are represented by arrays. The elements of each array contain the<br>information specified by the corresponding identifiers in the `columns` parameter. In case of errors the<br>responsible fields in the response are filled (see [Error handling](#error-handling)).|[UsersResponse](#usersresponse)|


## Consumes

* `application/json`


## Tags

* user


<a name="setuserattribute"></a>
# Sets a user attribute (**available since v6.20**).
```
PUT /user?action=setAttribute
```


## Description
Sets a custom user attribute consisting of a name and a value. The attribute can later be
retrieved using the `/user?action=getAttribute` request.


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|The ID of the user.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**setIfAbsent**  <br>*optional*|Set to `true` to put the value only if the specified name is not already associated with a value, otherwise `false` to put value in any case.|boolean||
|**Body**|**body**  <br>*required*|A JSON object providing the name and the value of the attribute. If the `value` field is missing or `null`, the attribute is removed.|[UserAttribute](#userattribute)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|A JSON object providing the information whether the attribute could be set. In case of errors the responsible fields in the response are<br>filled (see [Error handling](#error-handling)).|[UserAttributionResponse](#userattributionresponse)|


## Consumes

* `application/json`


## Tags

* user


<a name="updateuser"></a>
# Updates a user (**available since v6.14**).
```
PUT /user?action=update
```


## Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**id**  <br>*required*|Object ID of the requested user.|string||
|**Query**|**session**  <br>*required*|A session ID previously obtained from the login module.|string||
|**Query**|**timestamp**  <br>*required*|Timestamp of the last update of the requested user. If the user was modified after the specified timestamp, then the update must fail.|integer(int64)||
|**Body**|**body**  <br>*required*|A JSON object containing the user's data. Only modified fields are present. From [Detailed user data](#detailed-user-data) only the fields `timezone` and `locale` are allowed to be updated.|[UserData](#userdata)||


## Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|[CommonResponse](#commonresponse)|


## Consumes

* `application/json`


## Tags

* user




<a name="definitions"></a>

<a name="acquiretokenresponse"></a>
# AcquireTokenResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[data](#acquiretokenresponse-data)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|

<a name="acquiretokenresponse-data"></a>
**data**

|Name|Description|Schema|
|---|---|---|
|**token**  <br>*optional*|The token that can be used for a new session.|string|


<a name="allresourcesresponse"></a>
# AllResourcesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of resource identifiers.|< integer > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="appointmentconfirmationbody"></a>
# AppointmentConfirmationBody

|Name|Description|Schema|
|---|---|---|
|**confirmation**  <br>*required*|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|integer|
|**confirmmessage**  <br>*required*|The confirmation message or comment.|string|
|**id**  <br>*optional*|User ID. Confirming for other users only works for appointments and not for tasks.|integer|


<a name="appointmentcreationconflict"></a>
# AppointmentCreationConflict

|Name|Description|Schema|
|---|---|---|
|**alarm**  <br>*optional*|Specifies when to notify the participants as the number of minutes before the start of the appointment (-1 for "no alarm"). For tasks, the Time value specifies the absolute time when the user should be notified.|integer(int64)|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**change_exceptions**  <br>*optional*|An array of Dates, representing all change exceptions of a sequence.|< integer(int64) > array|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**confirmations**  <br>*optional*|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|< [TaskConfirmation](#taskconfirmation) > array|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**day_in_month**  <br>*optional*|Specifies which day of a month is part of the sequence. Counting starts with 1. If the field "days" is also present, only days selected by that field are counted. If the number is bigger than the number of available days, the last available day is selected. Present if and only if recurrence_type > 2.|integer|
|**days**  <br>*optional*|Specifies which days of the week are part of a sequence. The value is a bitfield with bit 0 indicating sunday, bit 1 indicating monday and so on. May be present if recurrence_type > 1. If allowed but not present, the value defaults to 127 (all 7 days).|integer|
|**delete_exceptions**  <br>*optional*|An array of Dates, representing all delete exceptions of a sequence.|< integer(int64) > array|
|**end_date**  <br>*optional*|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|integer(int64)|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**full_time**  <br>*optional*|True if the event is a whole day appointment or task, false otherwise.|boolean|
|**hard_conflicts**  <br>*optional*|"true" if appointment represents a resource conflict.|boolean|
|**id**  <br>*optional*|Object ID.|string|
|**ignore_conflicts**  <br>*optional*|Ignore soft conflicts for the new or modified appointment. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|boolean|
|**interval**  <br>*optional*|Specifies an integer multiplier to the interval specified by recurrence_type. Present if and only if recurrence_type > 0. Must be 1 if recurrence_type = 4.|integer|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**location**  <br>*optional*|The location of the appointment.|string|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**month**  <br>*optional*|Month of the year in yearly sequencies. 0 represents January, 1 represents February and so on. Present if and only if recurrence_type = 4.|integer|
|**note**  <br>*optional*|Long description.|string|
|**notification**  <br>*optional*|If true, all participants are notified of any changes to this object. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|boolean|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**occurrences**  <br>*optional*|Specifies how often a recurrence should appear. May be present only if recurrence_type > 0.|integer|
|**organizer**  <br>*optional*|Contains the email address of the appointment organizer which is not necessarily an internal user. Not implemented for tasks.|string|
|**organizerId**  <br>*optional*|Contains the userIId of the appointment organizer if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|integer|
|**participants**  <br>*optional*|Each element identifies a participant, user, group or booked resource.|< [TaskParticipant](#taskparticipant) > array|
|**principal**  <br>*optional*|Contains the email address of the appointment principal which is not necessarily an internal user. Not implemented for tasks (introduced with 6.20.1).|string|
|**principalId**  <br>*optional*|Contains the userIId of the appointment principal if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|integer|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**recurrence_date_position**  <br>*optional*|Date of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|integer(int64)|
|**recurrence_id**  <br>*optional*|Object ID of the entire appointment sequence. Present on series and change exception appointments. Equals to object identifier on series appointment and is different to object identifier on change exceptions.|integer|
|**recurrence_position**  <br>*optional*|1-based position of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|integer|
|**recurrence_start**  <br>*optional*|Start of a sequence without time.|integer(int64)|
|**recurrence_type**  <br>*optional*|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|integer|
|**sequence**  <br>*optional*|iCal sequence number. Not implemented for tasks. Must be incremented on update. Will be incremented by the server, if not set.|integer|
|**shown_as**  <br>*optional*|Describes, how this appointment appears in availability queries: 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|integer|
|**start_date**  <br>*optional*|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|integer(int64)|
|**timezone**  <br>*optional*|The timezone of the appointment.|string|
|**title**  <br>*optional*|Short description.|string|
|**uid**  <br>*optional*|Can only be written when the object is created. Internal and external globally unique identifier of the appointment or task. Is used to recognize appointments within iCal files. If this attribute is not written it contains an automatic generated UUID.|string|
|**until**  <br>*optional*|Inclusive end date of a sequence. May be present only if recurrence_type > 0. The sequence has no end date if recurrence_type > 0 and this field is not present. Note: since this is a Date, the entire day after the midnight specified by the value is included.|integer(int64)|
|**users**  <br>*optional*|Each element represents a participant. User groups are resolved and are represented by their members. Any user can occur only once.|< [TaskUser](#taskuser) > array|


<a name="appointmentcreationdata"></a>
# AppointmentCreationData

|Name|Description|Schema|
|---|---|---|
|**conflicts**  <br>*optional*|An array of appointments which cause conflicts.|< [AppointmentCreationConflict](#appointmentcreationconflict) > array|
|**id**  <br>*optional*|ID of the appointment.|string|


<a name="appointmentcreationresponse"></a>
# AppointmentCreationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[AppointmentCreationData](#appointmentcreationdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="appointmentdata"></a>
# AppointmentData

|Name|Description|Schema|
|---|---|---|
|**alarm**  <br>*optional*|Specifies when to notify the participants as the number of minutes before the start of the appointment (-1 for "no alarm"). For tasks, the Time value specifies the absolute time when the user should be notified.|integer(int64)|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**change_exceptions**  <br>*optional*|An array of Dates, representing all change exceptions of a sequence.|< integer(int64) > array|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**confirmations**  <br>*optional*|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|< [TaskConfirmation](#taskconfirmation) > array|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**day_in_month**  <br>*optional*|Specifies which day of a month is part of the sequence. Counting starts with 1. If the field "days" is also present, only days selected by that field are counted. If the number is bigger than the number of available days, the last available day is selected. Present if and only if recurrence_type > 2.|integer|
|**days**  <br>*optional*|Specifies which days of the week are part of a sequence. The value is a bitfield with bit 0 indicating sunday, bit 1 indicating monday and so on. May be present if recurrence_type > 1. If allowed but not present, the value defaults to 127 (all 7 days).|integer|
|**delete_exceptions**  <br>*optional*|An array of Dates, representing all delete exceptions of a sequence.|< integer(int64) > array|
|**end_date**  <br>*optional*|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|integer(int64)|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**full_time**  <br>*optional*|True if the event is a whole day appointment or task, false otherwise.|boolean|
|**id**  <br>*optional*|Object ID.|string|
|**ignore_conflicts**  <br>*optional*|Ignore soft conflicts for the new or modified appointment. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|boolean|
|**interval**  <br>*optional*|Specifies an integer multiplier to the interval specified by recurrence_type. Present if and only if recurrence_type > 0. Must be 1 if recurrence_type = 4.|integer|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**location**  <br>*optional*|The location of the appointment.|string|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**month**  <br>*optional*|Month of the year in yearly sequencies. 0 represents January, 1 represents February and so on. Present if and only if recurrence_type = 4.|integer|
|**note**  <br>*optional*|Long description.|string|
|**notification**  <br>*optional*|If true, all participants are notified of any changes to this object. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|boolean|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**occurrences**  <br>*optional*|Specifies how often a recurrence should appear. May be present only if recurrence_type > 0.|integer|
|**organizer**  <br>*optional*|Contains the email address of the appointment organizer which is not necessarily an internal user. Not implemented for tasks.|string|
|**organizerId**  <br>*optional*|Contains the userIId of the appointment organizer if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|integer|
|**participants**  <br>*optional*|Each element identifies a participant, user, group or booked resource.|< [TaskParticipant](#taskparticipant) > array|
|**principal**  <br>*optional*|Contains the email address of the appointment principal which is not necessarily an internal user. Not implemented for tasks (introduced with 6.20.1).|string|
|**principalId**  <br>*optional*|Contains the userIId of the appointment principal if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|integer|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**recurrence_date_position**  <br>*optional*|Date of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|integer(int64)|
|**recurrence_id**  <br>*optional*|Object ID of the entire appointment sequence. Present on series and change exception appointments. Equals to object identifier on series appointment and is different to object identifier on change exceptions.|integer|
|**recurrence_position**  <br>*optional*|1-based position of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|integer|
|**recurrence_start**  <br>*optional*|Start of a sequence without time.|integer(int64)|
|**recurrence_type**  <br>*optional*|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|integer|
|**sequence**  <br>*optional*|iCal sequence number. Not implemented for tasks. Must be incremented on update. Will be incremented by the server, if not set.|integer|
|**shown_as**  <br>*optional*|Describes, how this appointment appears in availability queries: 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|integer|
|**start_date**  <br>*optional*|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|integer(int64)|
|**timezone**  <br>*optional*|The timezone of the appointment.|string|
|**title**  <br>*optional*|Short description.|string|
|**uid**  <br>*optional*|Can only be written when the object is created. Internal and external globally unique identifier of the appointment or task. Is used to recognize appointments within iCal files. If this attribute is not written it contains an automatic generated UUID.|string|
|**until**  <br>*optional*|Inclusive end date of a sequence. May be present only if recurrence_type > 0. The sequence has no end date if recurrence_type > 0 and this field is not present. Note: since this is a Date, the entire day after the midnight specified by the value is included.|integer(int64)|
|**users**  <br>*optional*|Each element represents a participant. User groups are resolved and are represented by their members. Any user can occur only once.|< [TaskUser](#taskuser) > array|


<a name="appointmentdeletionselement"></a>
# AppointmentDeletionsElement

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*|The object ID of the related folder.|string|
|**id**  <br>*required*|The object ID of the appointment.|string|
|**pos**  <br>*optional*|Value of the field recurrence_position, if present in the appointment.|integer|


<a name="appointmentdeletionsresponse"></a>
# AppointmentDeletionsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array with object IDs of appointments which were modified after the specified timestamp and were therefore not deleted.|< [AppointmentDeletionsElement](#appointmentdeletionselement) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="appointmentfreebusyitem"></a>
# AppointmentFreeBusyItem

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**confirmations**  <br>*optional*|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|< [TaskConfirmation](#taskconfirmation) > array|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**end_date**  <br>*optional*|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|integer(int64)|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**full_time**  <br>*optional*|True if the event is a whole day appointment or task, false otherwise.|boolean|
|**id**  <br>*optional*|Object ID.|string|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**participants**  <br>*optional*|Each element identifies a participant, user, group or booked resource.|< [TaskParticipant](#taskparticipant) > array|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**recurrence_type**  <br>*optional*|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|integer|
|**shown_as**  <br>*optional*|Describes, how this appointment appears in availability queries: 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|integer|
|**start_date**  <br>*optional*|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|integer(int64)|
|**title**  <br>*optional*|Short description.|string|


<a name="appointmentfreebusyresponse"></a>
# AppointmentFreeBusyResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||< [AppointmentFreeBusyItem](#appointmentfreebusyitem) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="appointmentinforesponse"></a>
# AppointmentInfoResponse

|Name|Description|Schema|
|---|---|---|
|**data**  <br>*optional*|Array with elements that correspond with days in the time range, explaining whether a day has appointments or not.|< boolean > array|


<a name="appointmentlistelement"></a>
# AppointmentListElement

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*|The object ID of the related folder.|string|
|**id**  <br>*required*|The object ID of the appointment.|string|
|**recurrence_date_position**  <br>*optional*|Date of an individual appointment in a sequence.|integer(int64)|
|**recurrence_position**  <br>*optional*|1-based position of an individual appointment in a sequence.|integer|


<a name="appointmentresponse"></a>
# AppointmentResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[AppointmentData](#appointmentdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="appointmentsearchbody"></a>
# AppointmentSearchBody

|Name|Description|Schema|
|---|---|---|
|**pattern**  <br>*optional*|Search pattern to find appointments. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|string|
|**startletter**  <br>*optional*|Search appointments with the given starting letter.|string|


<a name="appointmentupdatedata"></a>
# AppointmentUpdateData

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|ID of the appointment.|string|


<a name="appointmentupdateresponse"></a>
# AppointmentUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[AppointmentUpdateData](#appointmentupdatedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="appointmentupdatesresponse"></a>
# AppointmentUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of appointments.|< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="appointmentsresponse"></a>
# AppointmentsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of appointments. Each appointment is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="attachmentdata"></a>
# AttachmentData

|Name|Description|Schema|
|---|---|---|
|**attached**  <br>*optional*|The ID of the object this attachment is attached to.|integer|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**file_mimetype**  <br>*optional*|The MIME type of the attached file.|string|
|**file_size**  <br>*optional*|The file size (in bytes) of the attached file.|integer(int64)|
|**filename**  <br>*optional*|The filename of the attached file.|string|
|**folder**  <br>*optional*|The ID of the first folder in which the attached object resides.|integer|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**id**  <br>*optional*|Object ID.|string|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**module**  <br>*optional*|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**rft_flag**  <br>*optional*|If the attachment is a RTF attachment of outlook (outlook descriptions can be stored as RTF documents).|boolean|


<a name="attachmentresponse"></a>
# AttachmentResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[AttachmentData](#attachmentdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="attachmentupdatesresponse"></a>
# AttachmentUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of attachments.|< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="attachmentsresponse"></a>
# AttachmentsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of attachments. Each attachment is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="autoconfigresponse"></a>
# AutoConfigResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MailAccountData](#mailaccountdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="capabilitiesresponse"></a>
# CapabilitiesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of JSON objects each describing one capability.|< [CapabilityData](#capabilitydata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="capabilitydata"></a>
# CapabilityData

|Name|Description|Schema|
|---|---|---|
|**attributes**  <br>*optional*|A JSON object holding properties of the capability.|object|
|**id**  <br>*optional*|The identifier of the capability.|string|


<a name="capabilityresponse"></a>
# CapabilityResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[CapabilityData](#capabilitydata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="changeipresponse"></a>
# ChangeIPResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="commonresponse"></a>
# CommonResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="configbody"></a>
# ConfigBody

|Name|Description|Schema|
|---|---|---|
|**data**  <br>*required*|The new value of the node specified by path.|object|


<a name="configproperty"></a>
# ConfigProperty

|Name|Description|Schema|
|---|---|---|
|**name**  <br>*optional*|The name of the property.|string|
|**value**  <br>*optional*|The value of the property.|object|


<a name="configpropertybody"></a>
# ConfigPropertyBody

|Name|Description|Schema|
|---|---|---|
|**value**  <br>*optional*|The concrete value to set.|string|


<a name="configpropertyresponse"></a>
# ConfigPropertyResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ConfigProperty](#configproperty)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="configresponse"></a>
# ConfigResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Generic type which can be object, string, array, etc.|object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="contactdata"></a>
# ContactData

|Name|Description|Schema|
|---|---|---|
|**addressBusiness**  <br>*optional*|Support for Outlook 'business' address field. (since 6.20.1)|string|
|**addressHome**  <br>*optional*|Support for Outlook 'home' address field. (since 6.20.1)|string|
|**addressOther**  <br>*optional*|Support for Outlook 'other' address field. (since 6.20.1)|string|
|**anniversary**  <br>*optional*|The anniversary.|integer(int64)|
|**assistant_name**  <br>*optional*|The assistant's name.|string|
|**birthday**  <br>*optional*|The date of birth.|integer(int64)|
|**branches**  <br>*optional*|The branches.|string|
|**business_category**  <br>*optional*|The business category.|string|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**cellular_telephone1**  <br>*optional*|The cellular telephone number 1.|string|
|**cellular_telephone2**  <br>*optional*|The cellular telephone number 2.|string|
|**city_business**  <br>*optional*|The city of the business address.|string|
|**city_home**  <br>*optional*|The city of the home address.|string|
|**city_other**  <br>*optional*|The city of another address.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**commercial_register**  <br>*optional*|The commercial register.|string|
|**company**  <br>*optional*|The company name.|string|
|**country_business**  <br>*optional*|The country of the business address.|string|
|**country_home**  <br>*optional*|The country of the home address.|string|
|**country_other**  <br>*optional*|The country of another address.|string|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**default_address**  <br>*optional*|The default address.|integer|
|**department**  <br>*optional*|The department.|string|
|**display_name**  <br>*optional*|The display name.|string|
|**distribution_list**  <br>*optional*|If this contact is a distribution list, then this field is an array of objects. Each object describes a member of the list.|< [DistributionListMember](#distributionlistmember) > array|
|**email1**  <br>*optional*|The email address 1.|string|
|**email2**  <br>*optional*|The email address 2.|string|
|**email3**  <br>*optional*|The email address 3.|string|
|**employee_type**  <br>*optional*|The type of the employee.|string|
|**fax_business**  <br>*optional*|The business fax number.|string|
|**fax_home**  <br>*optional*|The home fax number.|string|
|**fax_other**  <br>*optional*|The other fax number.|string|
|**file_as**  <br>*optional*|The file name.|string|
|**first_name**  <br>*optional*|The given name.|string|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**id**  <br>*optional*|Object ID.|string|
|**image1**  <br>*optional*||string|
|**image1_content_type**  <br>*optional*|The content type of the image (like "image/png").|string|
|**image1_url**  <br>*optional*|The url to the image.|string|
|**image_last_modified**  <br>*optional*|The last modification of the image.|integer(int64)|
|**info**  <br>*optional*|An information.|string|
|**instant_messenger1**  <br>*optional*|The instant messenger address 1.|string|
|**instant_messenger2**  <br>*optional*|The instant messenger address 2.|string|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**last_name**  <br>*optional*|The sur name.|string|
|**manager_name**  <br>*optional*|The manager's name.|string|
|**marital_status**  <br>*optional*|The marital status.|string|
|**mark_as_distributionlist**  <br>*optional*||boolean|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**nickname**  <br>*optional*|The nickname.|string|
|**note**  <br>*optional*|A note.|string|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**number_of_children**  <br>*optional*|The number of children.|string|
|**number_of_distribution_list**  <br>*optional*|The number of objects in the distribution list.|integer|
|**number_of_employees**  <br>*optional*|The number of employees.|string|
|**number_of_images**  <br>*optional*|The number of images.|integer|
|**position**  <br>*optional*|The position.|string|
|**postal_code_business**  <br>*optional*|The postal code of the business address.|string|
|**postal_code_home**  <br>*optional*|The postal code of the home address.|string|
|**postal_code_other**  <br>*optional*|The postal code of another address.|string|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**profession**  <br>*optional*|The profession.|string|
|**room_number**  <br>*optional*|The room number.|string|
|**sales_volume**  <br>*optional*|The sales volume.|string|
|**second_name**  <br>*optional*|The middle name.|string|
|**spouse_name**  <br>*optional*|The name of the spouse.|string|
|**state_business**  <br>*optional*|The state of the business address.|string|
|**state_home**  <br>*optional*|The state of the home address.|string|
|**state_other**  <br>*optional*|The state of another address.|string|
|**street_business**  <br>*optional*|The street of the business address.|string|
|**street_home**  <br>*optional*|The street of the home address.|string|
|**street_other**  <br>*optional*|The street of another address.|string|
|**suffix**  <br>*optional*|The suffix.|string|
|**tax_id**  <br>*optional*|The tax id.|string|
|**telephone_assistant**  <br>*optional*|The assistant telephone number.|string|
|**telephone_business1**  <br>*optional*|The business telephone number 1.|string|
|**telephone_business2**  <br>*optional*|The business telephone number 2.|string|
|**telephone_callback**  <br>*optional*|The callback telephone number.|string|
|**telephone_car**  <br>*optional*|The car telephone number.|string|
|**telephone_company**  <br>*optional*|The company telephone number.|string|
|**telephone_home1**  <br>*optional*|The home telephone number 1.|string|
|**telephone_home2**  <br>*optional*|The home telephone number 2.|string|
|**telephone_ip**  <br>*optional*|The IP telephone number.|string|
|**telephone_isdn**  <br>*optional*|The ISDN telephone number.|string|
|**telephone_other**  <br>*optional*|The other telephone number.|string|
|**telephone_pager**  <br>*optional*|The pager telephone number.|string|
|**telephone_primary**  <br>*optional*|The primary telephone number.|string|
|**telephone_radio**  <br>*optional*|The radio telephone number.|string|
|**telephone_telex**  <br>*optional*|The telex telephone number.|string|
|**telephone_ttytdd**  <br>*optional*|The TTY/TDD telephone number.|string|
|**title**  <br>*optional*|The title.|string|
|**uid**  <br>*optional*|Can only be written when the object is created. Internal and external globally unique identifier of the contact. Is used to recognize contacts within vCard files. If this attribute is not written it contains an automatic generated UUID.|string|
|**url**  <br>*optional*|The url address or homepage.|string|
|**useCount**  <br>*optional*|In case of sorting purposes the column 609 is also available, which places global address book contacts at the beginning of the result. If 609 is used, the order direction (ASC, DESC) is ignored.|integer|
|**user_id**  <br>*optional*|The internal user id.|integer|
|**userfield01**  <br>*optional*|Dynamic Field 1.|string|
|**userfield02**  <br>*optional*|Dynamic Field 2.|string|
|**userfield03**  <br>*optional*|Dynamic Field 3.|string|
|**userfield04**  <br>*optional*|Dynamic Field 4.|string|
|**userfield05**  <br>*optional*|Dynamic Field 5.|string|
|**userfield06**  <br>*optional*|Dynamic Field 6.|string|
|**userfield07**  <br>*optional*|Dynamic Field 7.|string|
|**userfield08**  <br>*optional*|Dynamic Field 8.|string|
|**userfield09**  <br>*optional*|Dynamic Field 9.|string|
|**userfield10**  <br>*optional*|Dynamic Field 10.|string|
|**userfield11**  <br>*optional*|Dynamic Field 11.|string|
|**userfield12**  <br>*optional*|Dynamic Field 12.|string|
|**userfield13**  <br>*optional*|Dynamic Field 13.|string|
|**userfield14**  <br>*optional*|Dynamic Field 14.|string|
|**userfield15**  <br>*optional*|Dynamic Field 15.|string|
|**userfield16**  <br>*optional*|Dynamic Field 16.|string|
|**userfield17**  <br>*optional*|Dynamic Field 17.|string|
|**userfield18**  <br>*optional*|Dynamic Field 18.|string|
|**userfield19**  <br>*optional*|Dynamic Field 19.|string|
|**userfield20**  <br>*optional*|Dynamic Field 20.|string|
|**yomiCompany**  <br>*optional*|Kana based representation for the Company. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|string|
|**yomiFirstName**  <br>*optional*|Kana based representation for the First Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|string|
|**yomiLastName**  <br>*optional*|Kana based representation for the Last Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|string|


<a name="contactdeletionsresponse"></a>
# ContactDeletionsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="contactlistelement"></a>
# ContactListElement

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*|The object ID of the related folder.|string|
|**id**  <br>*required*|The object ID of the contact.|string|


<a name="contactresponse"></a>
# ContactResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ContactData](#contactdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="contactsearchbody"></a>
# ContactSearchBody

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Searches contacts where the categories match with the given search pattern.|string|
|**company**  <br>*optional*|Searches contacts where the company match with the given search pattern. (requires version >= 6.12)|string|
|**display_name**  <br>*optional*|Searches contacts where the display name match with the given display name.|string|
|**email1**  <br>*optional*|Searches contacts where the email1 address match with the given search pattern. (requires version >= 6.12)|string|
|**email2**  <br>*optional*|Searches contacts where the email2 address match with the given search pattern. (requires version >= 6.12)|string|
|**email3**  <br>*optional*|Searches contacts where the email3 address match with the given search pattern. (requires version >= 6.12)|string|
|**emailAutoComplete**  <br>*optional*|If set to true, results are guaranteed to contain at least one email adress and the search is performed as if orSearch were set to true. The actual value of orSearch is ignored.|boolean|
|**exactMatch**  <br>*optional*|If set to true, contacts are returned where the specified patterns match the corresponding fields exactly. Otherwise, a "startsWith" or "substring" comparison is used based on the "orSearch" parameter. (requires version > 6.22.1)|boolean|
|**first_name**  <br>*optional*|Searches contacts where the first name match with the given first name.|string|
|**folder**  <br>*optional*|If a list of folder identifiers or at least a single folder identifier is given, only in that folders will be searched for contacts. This paramenter is optional but searching in all contact folders that are viewable and where objects can be read in is more expensive on that database than searching in a dedicated number of them. The possibility to provide here an array of folder identifier has been added with 6.10.|< integer > array|
|**last_name**  <br>*optional*|Searches contacts where the last name match with the given last name.|string|
|**orSearch**  <br>*optional*|If set to true, a contact is returned if any specified pattern matches at the start of the corresponding field. Otherwise, a contact is returned if all specified patterns match any substring of the corresponding field.|boolean|
|**pattern**  <br>*optional*|Search pattern to find contacts. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves. Matching is performed against any substring of the field display_name.|string|
|**startletter**  <br>*optional*|Search contacts with the given startletter. If this field is present, the pattern is matched against the contact field which is specified by the property "contact_first_letter_field" on the server (default: last name). Otherwise, the pattern is matched against the display name.|boolean|


<a name="contactupdatedata"></a>
# ContactUpdateData

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|ID of a newly created contact.|string|


<a name="contactupdateresponse"></a>
# ContactUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ContactUpdateData](#contactupdatedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="contactupdatesresponse"></a>
# ContactUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of contacts.|< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="contactsresponse"></a>
# ContactsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of contacts. Each contact is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="conversionbody"></a>
# ConversionBody

|Name|Description|Schema|
|---|---|---|
|**datahandler**  <br>*optional*||[ConversionDataHandler](#conversiondatahandler)|
|**datasource**  <br>*optional*||[ConversionDataSource](#conversiondatasource)|


<a name="conversiondatahandler"></a>
# ConversionDataHandler

|Name|Description|Schema|
|---|---|---|
|**args**  <br>*optional*|A JSON array of optional JSON objects containing the name-value-pairs.|< [ConversionDataHandlerPair](#conversiondatahandlerpair) > array|
|**identifier**  <br>*optional*|The identifier of the data handler.|string|


<a name="conversiondatahandlerpair"></a>
# ConversionDataHandlerPair

|Name|Description|Schema|
|---|---|---|
|**com.openexchange.groupware.calendar.confirmmessage**  <br>*optional*|The message.|string|
|**com.openexchange.groupware.calendar.confirmstatus**  <br>*optional*|The status.|string|
|**com.openexchange.groupware.calendar.folder**  <br>*optional*|The calendar folder ID.|string|
|**com.openexchange.groupware.calendar.searchobject**  <br>*optional*|Can be `true` or `false`.|string|
|**com.openexchange.groupware.calendar.timezone**  <br>*optional*|The timezone ID.|string|
|**com.openexchange.groupware.contact.folder**  <br>*optional*|The contact folder ID.|string|
|**com.openexchange.groupware.task.folder**  <br>*optional*|The task folder ID.|string|
|**com.openexchange.grouware.calendar.recurrencePosition**  <br>*optional*|The recurrence position.|string|


<a name="conversiondatasource"></a>
# ConversionDataSource

|Name|Description|Schema|
|---|---|---|
|**args**  <br>*optional*|A JSON array of optional JSON objects containing the name-value-pairs.|< [ConversionDataSourcePair](#conversiondatasourcepair) > array|
|**identifier**  <br>*optional*|The identifier of the data source.|string|


<a name="conversiondatasourcepair"></a>
# ConversionDataSourcePair
A name-value-pair where only one name with a value must be filled out except the case when VCard data from speicified contact object(s) is obtained then the `folder` and `id` must be specified.


|Name|Description|Schema|
|---|---|---|
|**com.openexchange.mail.conversion.fullname**  <br>*optional*|The folder's full name.|string|
|**com.openexchange.mail.conversion.mailid**  <br>*optional*|The object ID of the mail.|string|
|**com.openexchange.mail.conversion.sequenceid**  <br>*optional*|The attachment sequence ID.|string|
|**folder**  <br>*optional*|A folder ID.|string|
|**id**  <br>*optional*|The ID.|string|


<a name="conversionresponse"></a>
# ConversionResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="currentuserdata"></a>
# CurrentUserData

|Name|Description|Schema|
|---|---|---|
|**context_admin**  <br>*optional*|The ID of the context's administrator user.|integer|
|**context_id**  <br>*optional*|The unique identifier of the user's context.|integer|
|**display_name**  <br>*optional*|The display name of the user.|string|
|**login_name**  <br>*optional*|The login name of the user.|string|
|**user_id**  <br>*optional*|The unique identifier of the user himself.|integer|


<a name="currentuserresponse"></a>
# CurrentUserResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[CurrentUserData](#currentuserdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="distributionlistmember"></a>
# DistributionListMember

|Name|Description|Schema|
|---|---|---|
|**display_name**  <br>*optional*|The display name.|string|
|**folder_id**  <br>*optional*|Parent folder ID of the member's contact if the member is an existing contact (preliminary, from 6.22 on).|string|
|**id**  <br>*optional*|Object ID of the member's contact if the member is an existing contact.|string|
|**mail**  <br>*optional*|The email address (mandatory before 6.22, afterwards optional if you are referring to an internal contact).|string|
|**mail_field**  <br>*optional*|Which email field of an existing contact (if any) is used for the mail field: 0 (independent contact), 1 (default email field, email1), 2 (second email field, email2), 3 (third email field, email3).|number|


<a name="fileaccountcreationresponse"></a>
# FileAccountCreationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The ID of the newly created account.|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="fileaccountdata"></a>
# FileAccountData

|Name|Description|Schema|
|---|---|---|
|**capabilities**  <br>*optional*|An array of capability names. Possible values are: FILE_VERSIONS, EXTENDED_METADATA, RANDOM_FILE_ACCESS, and LOCKS.|< string > array|
|**configuration**  <br>*optional*|The configuration data according to the form description of the relevant file storage service.|object|
|**displayName**  <br>*optional*|A user chosen, human-readable name to identify the account. Will also be translated into the folder name of the folder representing the accounts content.|string|
|**filestorageService**  <br>*optional*|The identifier of the file storage service this account belongs to.|string|
|**id**  <br>*optional*|The identifier of the file storage account in the scope of its file storage service (e.g. Infostore, Dropbox, ...). This is not writeable and is generated by the server.|string|
|**isDefaultAccount**  <br>*optional*|Indicates whether this account is the user's default account. Exactly one account will have this flag set to `true`.|boolean|
|**qualifiedId**  <br>*optional*|A global identifier of the file storage account across all file storage services. This is not writeable and is generated by the server.|string|
|**rootFolder**  <br>*optional*|The ID of the account's root folder within the folder tree. This is not writeable and is generated by the server.|string|


<a name="fileaccountresponse"></a>
# FileAccountResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[FileAccountData](#fileaccountdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="fileaccountupdateresponse"></a>
# FileAccountUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Returns 1 on success.|integer|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="fileaccountsresponse"></a>
# FileAccountsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of JSON objects each describing one file storage account.|< [FileAccountData](#fileaccountdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="fileserviceconfiguration"></a>
# FileServiceConfiguration

|Name|Description|Schema|
|---|---|---|
|**defaultValue**  <br>*optional*|Can contain a default value.|object|
|**displayName**  <br>*optional*|The display name of the field.|string|
|**mandatory**  <br>*optional*|Indicates whether the field is mandatory.|boolean|
|**name**  <br>*optional*|The name of the field.|string|
|**options**  <br>*optional*|A list of available options in the field.|< object > array|
|**widget**  <br>*optional*|The name of the widget.|string|


<a name="fileservicedata"></a>
# FileServiceData

|Name|Description|Schema|
|---|---|---|
|**configuration**  <br>*optional*|An array of dynamic form fields. Same as in PubSub.|< [FileServiceConfiguration](#fileserviceconfiguration) > array|
|**displayName**  <br>*optional*|A human-readable display name of the service, e.g. "Box File Storage Service"|string|
|**id**  <br>*optional*|The identifier of the file storage service, e.g. "boxcom".|string|


<a name="fileserviceresponse"></a>
# FileServiceResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[FileServiceData](#fileservicedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="fileservicesresponse"></a>
# FileServicesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of JSON objects each describing one service.|< [FileServiceData](#fileservicedata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="findactivefacet"></a>
# FindActiveFacet

|Name|Description|Schema|
|---|---|---|
|**filter**  <br>*optional*||[FindActiveFacetFilter](#findactivefacetfilter)|
|**id**  <br>*optional*|The ID of the according facet.|string|
|**value**  <br>*optional*|The ID of the according value. Must always be copied from the value object, not from a possibly according option (in the two-dimensional case).|string|


<a name="findactivefacetfilter"></a>
# FindActiveFacetFilter
The filter object, copied from the value or option.


|Name|Description|Schema|
|---|---|---|
|**fields**  <br>*optional*|An array of fields to search for.|< string > array|
|**queries**  <br>*optional*|An array of corresponding search values.|< string > array|


<a name="findautocompletebody"></a>
# FindAutoCompleteBody

|Name|Description|Schema|
|---|---|---|
|**facets**  <br>*optional*|An array of already selected facets, meaning categories the user has filtered by before.|< [FindFacetData](#findfacetdata) > array|
|**options**  <br>*optional*||[FindOptionsData](#findoptionsdata)|
|**prefix**  <br>*optional*|The user's search input.|string|


<a name="findautocompletedata"></a>
# FindAutoCompleteData

|Name|Description|Schema|
|---|---|---|
|**facets**  <br>*optional*|An array of facets each describing a possible search category or an already applied category.|< [FindFacetData](#findfacetdata) > array|


<a name="findautocompleteresponse"></a>
# FindAutoCompleteResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[FindAutoCompleteData](#findautocompletedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="findfacetdata"></a>
# FindFacetData

|Name|Description|Schema|
|---|---|---|
|**filter**  <br>*optional*||[FindFacetFilter](#findfacetfilter)|
|**flags**  <br>*optional*|An array of flags. Available flags: conflicts (specified as "conflicts:<other-id>", facets carrying this flag must not be combined with a facet of type <other-id>). (for simple, default, and exclusive)|< string > array|
|**id**  <br>*optional*|The ID of this facet. Must be unique within an autocomplete response. Can be used to distinguish and filter certain facets. (for simple, default, and exclusive)|string|
|**item**  <br>*optional*||[FindFacetItem](#findfacetitem)|
|**name**  <br>*optional*|A displayable (and localized) name for this facet. If absent, an `item` attribute is present. (for simple, default, and exclusive)|string|
|**options**  <br>*optional*|An array of facet values. (for exclusive)|< [FindFacetValue](#findfacetvalue) > array|
|**style**  <br>*optional*|The facet style, which can be one of: simple, default, or exclusive. Dependent on the style some fields are present and others are not.|string|
|**values**  <br>*optional*|An array of facet values. (for default)|< [FindFacetValue](#findfacetvalue) > array|


<a name="findfacetfilter"></a>
# FindFacetFilter
The filter to refine the search. (for simple)


|Name|Description|Schema|
|---|---|---|
|**fields**  <br>*optional*|An array of fields to search for.|< string > array|
|**queries**  <br>*optional*|An array of corresponding search values.|< string > array|


<a name="findfacetitem"></a>
# FindFacetItem
A more complex object to display this facet. Attributes are `name`, `detail` (optional), and `image_url` (optional). (for simple, default, and exclusive)


|Name|Description|Schema|
|---|---|---|
|**detail**  <br>*optional*|A displayable (and localized) detail name, like "in mail text".|string|
|**image_url**  <br>*optional*|An URL to a displayable image.|string|
|**name**  <br>*optional*|A displayable (and localized) name for the facet.|string|


<a name="findfacetvalue"></a>
# FindFacetValue

|Name|Description|Schema|
|---|---|---|
|**filter**  <br>*optional*||[FindFacetValueFilter](#findfacetvaluefilter)|
|**id**  <br>*optional*|The ID of the value. Must be unique within one facet.|string|
|**item**  <br>*optional*||[FindFacetValueItem](#findfacetvalueitem)|
|**name**  <br>*optional*|A displayable (and localized) name for this facet. If absent, an `item` attribute is present.|string|
|**options**  <br>*optional*|An array of options to refine the search.|< [FindFacetValueOption](#findfacetvalueoption) > array|


<a name="findfacetvaluefilter"></a>
# FindFacetValueFilter
The filter to refine the search.


|Name|Description|Schema|
|---|---|---|
|**fields**  <br>*optional*|An array of fields to search for.|< string > array|
|**queries**  <br>*optional*|An array of corresponding search values.|< string > array|


<a name="findfacetvalueitem"></a>
# FindFacetValueItem
A more complex object to display this facet. Attributes are `name`, `detail` (optional), and `image_url` (optional).


|Name|Description|Schema|
|---|---|---|
|**detail**  <br>*optional*|A displayable (and localized) detail name, like "in mail text".|string|
|**image_url**  <br>*optional*|An URL to a displayable image.|string|
|**name**  <br>*optional*|A displayable (and localized) name for the facet.|string|


<a name="findfacetvalueoption"></a>
# FindFacetValueOption

|Name|Description|Schema|
|---|---|---|
|**filter**  <br>*optional*||[FindFacetValueFilter](#findfacetvaluefilter)|
|**id**  <br>*optional*|The ID of the option. Must be unique within a set of options.|string|
|**name**  <br>*optional*|The displayable (and localized) name for this option.|string|


<a name="findoptionsdata"></a>
# FindOptionsData

|Name|Description|Schema|
|---|---|---|
|**admin**  <br>*optional*|Indicates whether the context admin shall be included if it matches any search criteria. If the context admin shall always be ignored (i.e. not returned), `false` has to be set.|boolean|
|**timezone**  <br>*optional*|The timezone to use if any dates are returned.|string|


<a name="findquerybody"></a>
# FindQueryBody

|Name|Description|Schema|
|---|---|---|
|**facets**  <br>*optional*|An array of selected facets that shall be applied for search.|< [FindActiveFacet](#findactivefacet) > array|
|**options**  <br>*optional*||[FindOptionsData](#findoptionsdata)|
|**size**  <br>*optional*|The page size of a pagination, if desired.|integer|
|**start**  <br>*optional*|The start of a pagination, if desired.|integer|


<a name="findqueryresponse"></a>
# FindQueryResponse

|Name|Description|Schema|
|---|---|---|
|**num_found**  <br>*optional*|The number of found items.|integer|
|**results**  <br>*optional*|An array of search results. Each result is described by a JSON object containing the fields specified in the `columns` parameter.|< object > array|
|**size**  <br>*optional*|The page size.|integer|
|**start**  <br>*optional*|The start of the pagination.|integer|


<a name="folderbody"></a>
# FolderBody

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*||[FolderData](#folderdata)|
|**notification**  <br>*optional*||[FolderBodyNotification](#folderbodynotification)|


<a name="folderbodynotification"></a>
# FolderBodyNotification

|Name|Description|Schema|
|---|---|---|
|**message**  <br>*optional*||string|
|**transport**  <br>*optional*|E.g. "mail".|string|


<a name="folderdata"></a>
# FolderData

|Name|Description|Schema|
|---|---|---|
|**account_id**  <br>*optional*|Will be null if the folder does not belong to any account (i.e. if its module doesn't support multiple accounts), is a virtual folder or an account-agnostic system folder. Since 7.8.0.|string|
|**capabilities**  <br>*optional*|Bit mask containing information about mailing system capabilites: bit 0 (mailing system supports permissions), bit 1 (mailing system supports ordering mails by their thread reference), bit 2 (mailing system supports quota restrictions), bit 3 (mailing system supports sorting), bit 4 (mailing system supports folder subscription).|integer|
|**com.openexchange.folderstorage.displayName**  <br>*optional*|Provides the display of the folder's owner. Read Only, Since 6.20.|string|
|**com.openexchange.publish.publicationFlag**  <br>*optional*|Indicates whether this folder is published. Read Only, provided by the com.openexchange.publish plugin, since 6.14.|boolean|
|**com.openexchange.share.extendedPermissions**  <br>*optional*||< [FolderExtendedPermission](#folderextendedpermission) > array|
|**com.openexchange.subscribe.subscriptionFlag**  <br>*optional*|Indicates whether this folder has subscriptions storing their content in this folder. Read Only, provided by the com.openexchange.subscribe plugin, since 6.14.|boolean|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**deleted**  <br>*optional*|The number of deleted objects in this Folder.|integer|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**id**  <br>*optional*|Object ID|string|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**last_modified_utc**  <br>*optional*|Timestamp of the last modification. Note that the type is Timestamp, not Time (added 2008-10-17, with SP5, temporary workaround).|integer(int64)|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**module**  <br>*optional*|Name of the module which implements this folder; e.g. "tasks", "calendar", "contacts", "infostore", or "mail"|string|
|**new**  <br>*optional*|The number of new objects in this Folder.|integer|
|**own_rights**  <br>*optional*|Permissions which apply to the current user, as described either in [Permission flags](#permission-flags) or in http://tools.ietf.org/html/rfc2086.|integer|
|**permissions**  <br>*optional*||< [FolderPermission](#folderpermission) > array|
|**standard_folder**  <br>*optional*|Indicates whether or not folder is marked as a default folder (only OX folder).|boolean|
|**standard_folder_type**  <br>*optional*|Indicates the default folder type: 0 (non-default folder), 1 (task), 2 (calendar), 3 (contact), 7 (inbox), 8 (infostore), 9 (drafts), 10 (sent), 11 (spam), 12 (trash).|integer|
|**subfolders**  <br>*optional*|true if this folder has subfolders.|boolean|
|**subscr_subflds**  <br>*optional*|Indicates whether subfolders should appear in folder tree or not.|boolean|
|**subscribed**  <br>*optional*|Indicates whether this folder should appear in folder tree or not. Standard folders cannot be unsubscribed.|boolean|
|**summary**  <br>*optional*|Information about contained objects.|string|
|**supported_capabilities**  <br>*optional*|Can contain "permissions", "publication", "quota", "sort", "subscription".|< string > array|
|**title**  <br>*optional*|Name of this folder.|string|
|**total**  <br>*optional*|The number of objects in this Folder.|integer|
|**type**  <br>*optional*|Type of folder.|integer|
|**unread**  <br>*optional*|The number of unread objects in this Folder.|integer|


<a name="folderextendedpermission"></a>
# FolderExtendedPermission

|Name|Description|Schema|
|---|---|---|
|**bits**  <br>*optional*|A number as described in [Permission flags](#permission-flags).|integer|
|**contact**  <br>*optional*|A (reduced) set of [Detailed contact data](#detailed-contact-data) for "user" and "guest" entities.|object|
|**display_name**  <br>*optional*|A display name for the permission entity.|string|
|**entity**  <br>*optional*|Identifier of the permission entity (i.e. user-, group- or guest-ID).|integer|
|**expiry_date**  <br>*optional*|The optionally set expiry date for "anonymous" entities.|integer(int64)|
|**password**  <br>*optional*|The optionally set password for "anonymous" entities.|string|
|**share_url**  <br>*optional*|The share link for "anonymous" entities.|string|
|**type**  <br>*optional*|Set "user" for an internal user, "group" for a group, "guest" for a guest, or "anonymous" for an anonymous permission entity.|string|


<a name="folderpermission"></a>
# FolderPermission

|Name|Description|Schema|
|---|---|---|
|**bits**  <br>*optional*|For non-mail folders, a number as described in [Permission flags](#permission-flags).|integer|
|**contact_folder**  <br>*optional*|The folder identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", required if "contact_id" is set).|string|
|**contact_id**  <br>*optional*|The object identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", optional).|string|
|**display_name**  <br>*optional*|The display name of the recipient (for type "guest", optional).|string|
|**email_address**  <br>*optional*|The e-mail address of the recipient (for type "guest").|string|
|**entity**  <br>*optional*|User ID of the user or group to which this permission applies (ignored for type "anonymous" or "guest").|integer|
|**expiry_date**  <br>*optional*|The end date / expiration time after which the share link is no longer accessible (for type "anonymous", optional).|integer(int64)|
|**group**  <br>*optional*|true if entity refers to a group, false if it refers to a user (ignored for type "anonymous" or "guest").|boolean|
|**password**  <br>*optional*|An additional secret / pin number an anonymous user needs to enter when accessing the share (for type "anonymous", optional).|string|
|**rights**  <br>*optional*|For mail folders, the rights string as defined in http://tools.ietf.org/html/rfc2086.|string|
|**type**  <br>*optional*|The recipient type, i.e. one of "user", "group", "guest", "anonymous" (required if no internal "entity" defined).|string|


<a name="folderresponse"></a>
# FolderResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[FolderData](#folderdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="foldersharingnotificationbody"></a>
# FolderSharingNotificationBody

|Name|Description|Schema|
|---|---|---|
|**entities**  <br>*required*|Array containing the entity ID(s) of the users or groups that shall be notified.|< string > array|
|**notification**  <br>*optional*||[FolderBodyNotification](#folderbodynotification)|


<a name="foldersharingnotificationdata"></a>
# FolderSharingNotificationData

|Name|Description|Schema|
|---|---|---|
|**warnings**  <br>*optional*|Can contain transport warnings that occured during sending the notifications.|< object > array|


<a name="foldersharingnotificationresponse"></a>
# FolderSharingNotificationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[FolderSharingNotificationData](#foldersharingnotificationdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="folderupdateresponse"></a>
# FolderUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The object id of the folder.|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="folderupdatesresponse"></a>
# FolderUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of folders.|< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="folderscleanupresponse"></a>
# FoldersCleanUpResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array with object IDs of folders that could not be processed because of a concurrent modification or something else.|< string > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="foldersresponse"></a>
# FoldersResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of folders. Each folder is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="foldersvisibilitydata"></a>
# FoldersVisibilityData

|Name|Description|Schema|
|---|---|---|
|**private**  <br>*optional*|Array of private folders. Each folder is described as an array itself.|< < object > array > array|
|**public**  <br>*optional*|Array of public folders. Each folder is described as an array itself.|< < object > array > array|
|**shared**  <br>*optional*|Array of shared folders. Each folder is described as an array itself.|< < object > array > array|


<a name="foldersvisibilityresponse"></a>
# FoldersVisibilityResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[FoldersVisibilityData](#foldersvisibilitydata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="freebusydata"></a>
# FreeBusyData

|Name|Description|Schema|
|---|---|---|
|**end_date**  <br>*optional*|The end time of the interval.|integer(int64)|
|**folder_id**  <br>*optional*|The folder ID of the corresponding appointment if available.|string|
|**full_time**  <br>*optional*|Indicates whether the corresponding appointment is a whole day appointment, not present otherwise.|boolean|
|**id**  <br>*optional*|The object ID of the corresponding appointment if available.|string|
|**location**  <br>*optional*|The location of the corresponding appointment if available.|string|
|**shown_as**  <br>*optional*|The busy status of this interval, one of: 0 (unknown), 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|integer|
|**start_date**  <br>*optional*|The start time of the interval.|integer(int64)|
|**title**  <br>*optional*|The title of the corresponding appointment if available.|string|


<a name="freebusyresponse"></a>
# FreeBusyResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of free/busy intervals.|< [FreeBusyData](#freebusydata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="freebusysresponse"></a>
# FreeBusysResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The free/busy data for all requested participants inside a JSON object with the participant IDs as keys,<br>like `{"data":{"3":{"data":[{"start_date":...},{"start_date":...]}},"19":{"data":[{"start_date":...}]}}}`. Besides a combined data<br>element for a requested group, all group members are resolved and listed separately in the result. If the `merged` parameter<br>is specified, an additional data element named `merged` representing a combined view for all requested participants is added<br>to the results implicity.|object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="groupdata"></a>
# GroupData

|Name|Description|Schema|
|---|---|---|
|**display_name**  <br>*optional*|Display name of the group.|string|
|**id**  <br>*optional*|The group ID.|integer|
|**last_modified_utc**  <br>*optional*|Timestamp of the last modification.|integer(int64)|
|**members**  <br>*optional*|The array contains identifiers of users that are member of the group.|< integer > array|
|**name**  <br>*optional*|Internal name with character restrictions.|string|


<a name="grouplistelement"></a>
# GroupListElement

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|ID of a group.|integer|


<a name="groupresponse"></a>
# GroupResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[GroupData](#groupdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="groupsearchbody"></a>
# GroupSearchBody

|Name|Description|Schema|
|---|---|---|
|**pattern**  <br>*optional*|Search pattern to find groups. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|string|


<a name="groupupdatedata"></a>
# GroupUpdateData

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|The ID of a newly created group.|integer|


<a name="groupupdateresponse"></a>
# GroupUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[GroupUpdateData](#groupupdatedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="groupupdatesdata"></a>
# GroupUpdatesData

|Name|Description|Schema|
|---|---|---|
|**deleted**  <br>*optional*|Array of deleted group objects.|< [GroupData](#groupdata) > array|
|**modified**  <br>*optional*|Array of modified group objects.|< [GroupData](#groupdata) > array|
|**new**  <br>*optional*|Array of new group objects.|< [GroupData](#groupdata) > array|


<a name="groupupdatesresponse"></a>
# GroupUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[GroupUpdatesData](#groupupdatesdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="groupsresponse"></a>
# GroupsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of group objects.|< [GroupData](#groupdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="haloinvestigationresponse"></a>
# HaloInvestigationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of halo data objects. Each object is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="haloservicesresponse"></a>
# HaloServicesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array with available halo providers.|< string > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="infoitembody"></a>
# InfoItemBody

|Name|Description|Schema|
|---|---|---|
|**file**  <br>*required*||[InfoItemData](#infoitemdata)|
|**notification**  <br>*optional*||[InfoItemBodyNotification](#infoitembodynotification)|


<a name="infoitembodynotification"></a>
# InfoItemBodyNotification
Responsible for sending out notifications for changed object permissions of an infoitem.


|Name|Description|Schema|
|---|---|---|
|**message**  <br>*optional*||string|
|**transport**  <br>*optional*|E.g. "mail".|string|


<a name="infoitemdata"></a>
# InfoItemData

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**com.openexchange.file.storage.mail.mailMetadata**  <br>*optional*|Contains additional metadata for items in the "maildrive" file storage (read-only). (available since 7.8.2)|object|
|**com.openexchange.realtime.resourceID**  <br>*optional*|The resource identifier for the infoitem for usage within the realtime component (read-only). (available since 7.8.0)|string|
|**com.openexchange.share.extendedObjectPermissions**  <br>*optional*|An array of extended object permissions (read-only). (available since 7.8.0)|< [InfoItemExtendedPermission](#infoitemextendedpermission) > array|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**current_version**  <br>*optional*|"true" if this version is the current version, "false" otherwise. Note: This is not writeable.|boolean|
|**description**  <br>*optional*|A description if the item.|string|
|**file_md5sum**  <br>*optional*|MD5Sum of the document.|string|
|**file_mimetype**  <br>*optional*|MIME type of the document. The client converts known types to more readable names before displaying them.|string|
|**file_size**  <br>*optional*|The size of the document in bytes.|integer(int64)|
|**filename**  <br>*optional*|Displayed filename of the document.|string|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**id**  <br>*optional*|Object ID.|string|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**locked_until**  <br>*optional*|The time until which this item will presumably be locked. Only set if the docment is currently locked, 0 otherwise.|integer(int64)|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**number_of_versions**  <br>*optional*|The number of all versions of the item. Note: This is not writeable.|integer|
|**object_permissions**  <br>*optional*|An array of object permissions (preliminary, available since v7.8.0).|< [InfoItemPermission](#infoitempermission) > array|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**shareable**  <br>*optional*|(read-only) Indicates if the item can be shared (preliminary, available since v7.8.0).|boolean|
|**title**  <br>*optional*|The title.|string|
|**url**  <br>*optional*|Link/URL to item.|string|
|**version**  <br>*optional*|Version of the document. NULL can be used to denote the most recent version and will be set in responses if versions are not supported by the underlying storage.|string|
|**version_comment**  <br>*optional*|A version comment is used to file a changelog for the file.|string|


<a name="infoitemdetachresponse"></a>
# InfoItemDetachResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||< integer > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="infoitemextendedpermission"></a>
# InfoItemExtendedPermission

|Name|Description|Schema|
|---|---|---|
|**bits**  <br>*optional*|A number specifying the permission flags: 0 (the numerical value indicating no object permissions), 1 (the numerical value indicating read object permissions), 2 (the numerical value indicating write object permissions. This implicitly includes the “read” permission (this is no bitmask)).|integer|
|**contact**  <br>*optional*||[ContactData](#contactdata)|
|**display_name**  <br>*optional*|A display name for the permission entity.|string|
|**entity**  <br>*optional*|Identifier of the permission entity (i.e. user-, group- or guest-ID).|integer|
|**expiry_date**  <br>*optional*|The optionally set expiry date for "anonymous" entities.|integer(int64)|
|**password**  <br>*optional*|The optionally set password for "anonymous" entities.|string|
|**share_url**  <br>*optional*|The share link for "anonymous" entities.|string|
|**type**  <br>*optional*|"user" for an internal user, "group" for a group, "guest" for a guest, or "anonymous" for an anonymous permission entity.|string|


<a name="infoitemlistelement"></a>
# InfoItemListElement

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*|The object ID of the related folder (e.g. "31841").|string|
|**id**  <br>*required*|The object ID of the infoitem (e.g. "31841/36639").|string|


<a name="infoitempermission"></a>
# InfoItemPermission

|Name|Description|Schema|
|---|---|---|
|**bits**  <br>*optional*|A number specifying the permission flags: 0 (the numerical value indicating no object permissions), 1 (the numerical value indicating read object permissions), 2 (the numerical value indicating write object permissions. This implicitly includes the “read” permission (this is no bitmask)).|integer|
|**contact_folder**  <br>*optional*|The folder identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", required if "contact_id" is set).|string|
|**contact_id**  <br>*optional*|The object identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", optional).|string|
|**display_name**  <br>*optional*|The display name of the recipient (for type "guest", optional).|string|
|**email_address**  <br>*optional*|The e-mail address of the recipient (for type "guest").|string|
|**entity**  <br>*optional*|User ID of the user or group to which this permission applies.|integer|
|**expiry_date**  <br>*optional*|The end date / expiration time after which the share link is no longer accessible (for type "anonymous", optional).|integer(int64)|
|**group**  <br>*optional*|Is true if entity refers to a group, false if it refers to a user.|boolean|
|**password**  <br>*optional*|An additional secret / pin number an anonymous user needs to enter when accessing the share (for type "anonymous", optional).|string|
|**type**  <br>*optional*|The recipient type, i.e. one of "user", "group", "guest", "anonymous" (required if no internal "entity" defined).|string|


<a name="infoitemresponse"></a>
# InfoItemResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[InfoItemData](#infoitemdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="infoitemsearchbody"></a>
# InfoItemSearchBody

|Name|Description|Schema|
|---|---|---|
|**pattern**  <br>*optional*|The search pattern, where "*" matches any sequence of characters.|string|


<a name="infoitemsharingnotificationbody"></a>
# InfoItemSharingNotificationBody

|Name|Description|Schema|
|---|---|---|
|**entities**  <br>*required*|Array containing the entity ID(s) of the users or groups that shall be notified.|< string > array|
|**notification**  <br>*optional*||[InfoItemBodyNotification](#infoitembodynotification)|


<a name="infoitemsharingnotificationdata"></a>
# InfoItemSharingNotificationData

|Name|Description|Schema|
|---|---|---|
|**warnings**  <br>*optional*|Can contain transport warnings that occured during sending the notifications.|< object > array|


<a name="infoitemsharingnotificationresponse"></a>
# InfoItemSharingNotificationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[InfoItemSharingNotificationData](#infoitemsharingnotificationdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="infoitemupdateresponse"></a>
# InfoItemUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The object ID of the updated infoitem.|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="infoitemupdatesresponse"></a>
# InfoItemUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of infoitems.|< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="infoitemzipelement"></a>
# InfoItemZipElement

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*|The object ID of the related folder (e.g. "31841").|string|
|**id**  <br>*required*|The object ID of the infoitem (e.g. "31841/36639").|string|
|**version**  <br>*optional*|The version of the infoitem.|string|


<a name="infoitemsmovedresponse"></a>
# InfoItemsMovedResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of infoitem identifiers that could not be moved (due to a conflict).|< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="infoitemsresponse"></a>
# InfoItemsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of infoitems. Each infoitem is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="jslobdata"></a>
# JSlobData

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|The identifier of the JSlob.|string|
|**meta**  <br>*optional*|A JSON object containing meta data.|object|
|**tree**  <br>*optional*|The JSON object that is stored in the JSlob.|object|


<a name="jslobsresponse"></a>
# JSlobsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array containing JSON configurations.|< [JSlobData](#jslobdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="jumpresponse"></a>
# JumpResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[JumpTokenData](#jumptokendata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="jumptokendata"></a>
# JumpTokenData

|Name|Description|Schema|
|---|---|---|
|**token**  <br>*optional*|The identifier of the token.|string|


<a name="loginresponse"></a>
# LoginResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**context_id**  <br>*optional*|The context ID.|integer|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**locale**  <br>*optional*|The users locale (e.g. "en_US").|string|
|**session**  <br>*optional*|The session ID.|string|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|
|**user**  <br>*optional*|The username.|string|
|**user_id**  <br>*optional*|The user ID.|integer|


<a name="mailaccountdata"></a>
# MailAccountData

|Name|Description|Schema|
|---|---|---|
|**addresses**  <br>*optional*|The comma-separated list of available email addresses including aliases (**only available for primary mail account**).|string|
|**archive**  <br>*optional*|The name of the archive folder. **Currently not functional!**|string|
|**archive_fullname**  <br>*optional*|The full name of the archive folder. **Currently not functional!**|string|
|**confirmed_ham**  <br>*optional*|The name of the default confirmed-ham folder.|string|
|**confirmed_ham_fullname**  <br>*optional*|Path to default confirmed-ham folder. Preferred over `confirmed_ham`.|string|
|**confirmed_spam**  <br>*optional*|The name of the default confirmed-spam folder.|string|
|**confirmed_span_fullname**  <br>*optional*|Path to default confirmed-spam folder. Preferred over `confirmed_spam`.|string|
|**drafts**  <br>*optional*|The name of the default drafts folder.|string|
|**drafts_fullname**  <br>*optional*|Path to default drafts folder. Preferred over `drafts`.|string|
|**id**  <br>*optional*|The account identifier.|integer|
|**login**  <br>*optional*|The login name.|string|
|**mail_port**  <br>*optional*|The mail server's port.|integer|
|**mail_protocol**  <br>*optional*|The mail server's protocol. **Always use basic protocol name.** E.g. use "imap" instead of "imaps".|string|
|**mail_secure**  <br>*optional*|Whether to establish a secure connection to mail server (SSL, TLS).|boolean|
|**mail_server**  <br>*optional*|The mail server's hostname or IP address.|string|
|**mail_starttls**  <br>*optional*|Whether to establish a secure connection to mail server via STARTTLS (available since v7.8.2).|boolean|
|**mail_url**  <br>*optional*|The mail server URL, e.g. "imap://imap.somewhere.com:143". **URL is preferred over single fields** (like `mail_server`, `mail_port`, etc.).|string|
|**meta**  <br>*optional*|Stores arbitrary JSON data as specified by client associated with the mail account.|string|
|**name**  <br>*optional*|The account's display name.|string|
|**password**  <br>*optional*|The (optional) password.|string|
|**personal**  <br>*optional*|The customizable personal part of the email address.|string|
|**pop3_delete_write_through**  <br>*optional*|If option `pop3_expunge_on_quite` is disabled, this field defines whether a deleted in local INBOX also deletes affected message in actual POP3 account.|boolean|
|**pop3_expunge_on_quit**  <br>*optional*|Whether POP3 messages shall be deleted on actual POP3 account after retrieval or not.|boolean|
|**pop3_path**  <br>*optional*|Path to POP3's virtual root folder in storage, default name of the POP3 account beside default folders.|string|
|**pop3_refresh_rate**  <br>*optional*|The interval in minutes the POP3 account is refreshed.|integer|
|**pop3_storage**  <br>*optional*|The name of POP3 storage provider, default is "mailaccount".|string|
|**primary_address**  <br>*optional*|The user's primary address in account, e.g. "someone@somewhere.com".|string|
|**reply_to**  <br>*optional*|The customizable reply-to email address.|string|
|**sent**  <br>*optional*|The name of the default sent folder.|string|
|**sent_fullname**  <br>*optional*|Path to default sent folder. Preferred over `sent`.|string|
|**spam**  <br>*optional*|The name of the default spam folder.|string|
|**spam_fullname**  <br>*optional*|Path to default spam folder. Preferred over `spam`.|string|
|**spam_handler**  <br>*optional*|The name of the spam handler used by account.|string|
|**transport_auth**  <br>*optional*|Specifies the source for mail transport (SMTP) credentials. Possible values: mail (signals to use the same credentials as given in associated mail store, e.g. IMAP or POP3), custom (signals that individual credentials are supposed to be used (fields `transport_login` and `transport_password` are considered), none (means the mail transport does not support any authentication mechansim). (available since v7.6.1)|string|
|**transport_login**  <br>*optional*|The transport login. **Please see `transport_auth` for the handling of this field.**|string|
|**transport_password**  <br>*optional*|The transport password. **Please see `transport_auth` for the handling of this field.**|string|
|**transport_port**  <br>*optional*|The transport server's port.|integer|
|**transport_protocol**  <br>*optional*|The transport server's protocol. **Always use basic protocol name.** E.g. use "smtp" instead of "smtps".|string|
|**transport_secure**  <br>*optional*|Whether to establish a secure connection to transport server (SSL, TLS).|boolean|
|**transport_server**  <br>*optional*|The transport server's hostname or IP address.|string|
|**transport_starttls**  <br>*optional*|Whether to establish a secure connection to transport server via STARTTLS (available since v7.8.2).|boolean|
|**transport_url**  <br>*optional*|The transport server URL, e.g. "smtp://smtp.somewhere.com:25". **URL is preferred over single fields** (like `transport_server`, `transport_port`, etc.).|string|
|**trash**  <br>*optional*|The name of the default trash folder.|string|
|**trash_fullname**  <br>*optional*|Path to default trash folder. Preferred over `trash`.|string|
|**unified_inbox_enabled**  <br>*optional*|Whether Unified INBOX is enabled.|boolean|


<a name="mailaccountdeletionresponse"></a>
# MailAccountDeletionResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array containing the identifiers of the mail accounts that were deleted.|< integer > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailaccountresponse"></a>
# MailAccountResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MailAccountData](#mailaccountdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailaccountupdateresponse"></a>
# MailAccountUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MailAccountData](#mailaccountdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|
|**warnings**  <br>*optional*|An array of error objects that occurred during the creation of the account.|< [CommonResponse](#commonresponse) > array|


<a name="mailaccountvalidationresponse"></a>
# MailAccountValidationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|A boolean if parameter `tree` is not specified indicating the validation result otherwise the folder<br>tree object (see **FolderData** model) extended by a field `subfolder_array` that contains possible subfolders.<br>In the tree case a value of `null` indicating a failed validation.|object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|
|**warnings**  <br>*optional*||[CommonResponse](#commonresponse)|


<a name="mailaccountsresponse"></a>
# MailAccountsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of mail accounts. Each account is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailackbody"></a>
# MailAckBody

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*optional*|The ID of the folder where the mail is placed.|string|
|**from**  <br>*optional*|The from email address.|string|
|**id**  <br>*optional*|The ID of the mail.|string|


<a name="mailackresponse"></a>
# MailAckResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailattachment"></a>
# MailAttachment

|Name|Description|Schema|
|---|---|---|
|**content**  <br>*optional*|Content as text. Present only if easily convertible to text.|string|
|**content_type**  <br>*optional*|MIME type.|string|
|**disp**  <br>*optional*|Attachment's disposition: null, inline, attachment or alternative.|string|
|**filename**  <br>*optional*|Displayed filename (mutually exclusive with content).|string|
|**id**  <br>*optional*|Object ID (unique only inside the same message).|string|
|**size**  <br>*optional*|Size of the attachment in bytes.|integer(int64)|


<a name="mailconversationdata"></a>
# MailConversationData

|Name|Description|Schema|
|---|---|---|
|**account_id**  <br>*optional*|Message's account identifier. Since v6.20.2.|integer|
|**account_name**  <br>*optional*|Message's account name.|string|
|**attachment**  <br>*optional*|Indicates whether this mail has attachments.|boolean|
|**attachments**  <br>*optional*|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|< [MailAttachment](#mailattachment) > array|
|**bcc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|< < string > array > array|
|**cc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|< < string > array > array|
|**cid**  <br>*optional*|The value of the "Content-ID" header, if the header is present.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|integer|
|**content_type**  <br>*optional*|The MIME type of the mail.|string|
|**disp_notification_to**  <br>*optional*|Content of message's header "Disposition-Notification-To".|string|
|**flag_seen**  <br>*optional*|Special field to sort mails by seen status.|string|
|**flags**  <br>*optional*|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|integer|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**from**  <br>*optional*|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|< < string > array > array|
|**headers**  <br>*optional*|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|object|
|**id**  <br>*optional*|Object ID of the mail.|string|
|**level**  <br>*optional*|Zero-based nesting level in a thread.|integer|
|**msg_ref**  <br>*optional*|Message reference on reply/forward.|string|
|**original_folder_id**  <br>*optional*|The original folder identifier (e.g. if fetched from "virtual/all" folder).|string|
|**original_id**  <br>*optional*|The original mail identifier (e.g. if fetched from "virtual/all" folder).|string|
|**priority**  <br>*optional*|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|integer|
|**received_date**  <br>*optional*|Date and time as measured by the receiving server.|integer(int64)|
|**sent_date**  <br>*optional*|Date and time as specified in the mail by the sending client.|integer(int64)|
|**size**  <br>*optional*|The size if the mail in bytes.|integer(int64)|
|**source**  <br>*optional*|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|string|
|**subject**  <br>*optional*|The mail's subject.|string|
|**thread**  <br>*optional*|JSON array consisting of JSON objects, each representing a message in the conversation.|< [MailData](#maildata) > array|
|**to**  <br>*optional*|Each element is a two-element array (see the from field) specifying one receiver.|< < string > array > array|
|**truncated**  <br>*optional*|true/false if the mail content was trimmed. Since v7.6.1|boolean|
|**unreadCount**  <br>*optional*||integer|
|**user**  <br>*optional*|An array with user-defined flags as strings.|< string > array|


<a name="mailconversationsresponse"></a>
# MailConversationsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of JSON objects each representing a conversation's root message.|< [MailConversationData](#mailconversationdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailcountresponse"></a>
# MailCountResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The folder's mail count.|integer|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="maildata"></a>
# MailData

|Name|Description|Schema|
|---|---|---|
|**account_id**  <br>*optional*|Message's account identifier. Since v6.20.2.|integer|
|**account_name**  <br>*optional*|Message's account name.|string|
|**attachment**  <br>*optional*|Indicates whether this mail has attachments.|boolean|
|**attachments**  <br>*optional*|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|< [MailAttachment](#mailattachment) > array|
|**bcc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|< < string > array > array|
|**cc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|< < string > array > array|
|**cid**  <br>*optional*|The value of the "Content-ID" header, if the header is present.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|integer|
|**content_type**  <br>*optional*|The MIME type of the mail.|string|
|**disp_notification_to**  <br>*optional*|Content of message's header "Disposition-Notification-To".|string|
|**flag_seen**  <br>*optional*|Special field to sort mails by seen status.|string|
|**flags**  <br>*optional*|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|integer|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**from**  <br>*optional*|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|< < string > array > array|
|**headers**  <br>*optional*|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|object|
|**id**  <br>*optional*|Object ID of the mail.|string|
|**level**  <br>*optional*|Zero-based nesting level in a thread.|integer|
|**msg_ref**  <br>*optional*|Message reference on reply/forward.|string|
|**original_folder_id**  <br>*optional*|The original folder identifier (e.g. if fetched from "virtual/all" folder).|string|
|**original_id**  <br>*optional*|The original mail identifier (e.g. if fetched from "virtual/all" folder).|string|
|**priority**  <br>*optional*|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|integer|
|**received_date**  <br>*optional*|Date and time as measured by the receiving server.|integer(int64)|
|**sent_date**  <br>*optional*|Date and time as specified in the mail by the sending client.|integer(int64)|
|**size**  <br>*optional*|The size if the mail in bytes.|integer(int64)|
|**source**  <br>*optional*|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|string|
|**subject**  <br>*optional*|The mail's subject.|string|
|**to**  <br>*optional*|Each element is a two-element array (see the from field) specifying one receiver.|< < string > array > array|
|**truncated**  <br>*optional*|true/false if the mail content was trimmed. Since v7.6.1|boolean|
|**user**  <br>*optional*|An array with user-defined flags as strings.|< string > array|


<a name="maildestinationbody"></a>
# MailDestinationBody

|Name|Description|Schema|
|---|---|---|
|**folder_id**  <br>*optional*|The object ID of the destination folder.|string|


<a name="maildestinationdata"></a>
# MailDestinationData

|Name|Description|Schema|
|---|---|---|
|**folder_id**  <br>*optional*|Object ID of the destination folder.|string|
|**id**  <br>*optional*|Object ID of the "new" mail.|string|


<a name="maildestinationresponse"></a>
# MailDestinationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MailDestinationData](#maildestinationdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailfilteraction"></a>
# MailFilterAction

|Name|Description|Schema|
|---|---|---|
|**addresses**  <br>*optional*|The addresses for which this vacation is responsible. That means for which addresses out of the aliases array of the user defining this filter, vacations will be sent.|< string > array|
|**days**  <br>*optional*|The days for which a vacation text is returned (for vacation-command).|string|
|**flags**  <br>*optional*|An array containing the flags which should be added to the mail. A flag can either be a system flag or a user flag. System flags begin with a backslash and can be: "seen", "answered", "flagged", "deleted", "draft" or "recent". User flags begin with a dollar sign and can contain any ASCII characters between 0x21 ("!") and 0x7E ("~") (inclusive), except for 0x22 ("), 0x25 (%), 0x28 ((), 0x29 ()), 0x2A (*), 0x5C (backslash), 0x5D (]) and 0x7B ({). Mail color flags as used by OX are implemented by user flags of the form `$cl_n`, where "n" is a number between 1 and 10 (inclusive). (for addflags-command)|< string > array|
|**from**  <br>*optional*|Support for the ":from" tag. Specifies the value of the from header for the auto-reply mail, e.g. Foo Bear <foo.bear@ox.io> (Since 7.8.1). The array of strings should be a simple JSONArray with length 2; the first element should include the personal part of the e-mail address and the second element the actual e-mail address. If only the e-mail address is available, that should be the only element of the array. (for vacation-command)|object|
|**id**  <br>*optional*|A string defining the object itself (e.g. "keep" or "discard").|string|
|**into**  <br>*optional*|This string takes the object id of the destination mail folder (for move-command).|string|
|**keys**  <br>*optional*|The public keys which should be used for encryption (for pgp-command).|< string > array|
|**message**  <br>*optional*|The content of the notification message (for notify-command).|string|
|**method**  <br>*optional*|The method of the notification message, eg. `mailto:012345678@sms.gateway` (for notify-command).|string|
|**subject**  <br>*optional*|The new subject for the returned message (can be left empty, when only adding RE:) (for vacation-command).|string|
|**text**  <br>*optional*|A string containing the reason why the mail is rejected (for reject-command) or a string containing the vacation text itself (for vacation-command).|string|
|**to**  <br>*optional*|A string containing where the mail should be redirected to (for redirect-command).|string|


<a name="mailfilterconfigdata"></a>
# MailFilterConfigData

|Name|Description|Schema|
|---|---|---|
|**actioncommands**  <br>*optional*|Array of available action commands.|< string > array|
|**tests**  <br>*optional*|Array of available test-objects.|< [MailFilterConfigTest](#mailfilterconfigtest) > array|


<a name="mailfilterconfigresponse"></a>
# MailFilterConfigResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MailFilterConfigData](#mailfilterconfigdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailfilterconfigtest"></a>
# MailFilterConfigTest

|Name|Description|Schema|
|---|---|---|
|**comparison**  <br>*optional*|An array of the valid comparison types for this test, see [Possible comparisons](#possible-comparisons).|< string > array|
|**test**  <br>*optional*|The name of the test, see [Possible tests](#possible-tests).|string|


<a name="mailfiltercreationresponse"></a>
# MailFilterCreationResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The id of the newly created rule.|integer|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailfilterdeletionbody"></a>
# MailFilterDeletionBody

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|The ID of the rule that shall be deleted.|integer|


<a name="mailfilternottest"></a>
# MailFilterNotTest
A test object which result will be negated.


|Name|Description|Schema|
|---|---|---|
|**comparison**  <br>*optional*|The comparison type, see [Possible comparisons](#possible-comparisons).|string|
|**datepart**  <br>*optional*|Type of the comparison, which can be "date", "weekday" or "time" (available since v7.6.1) (for currentdate-test).|string|
|**datevalue**  <br>*optional*|Contains the corresponding value to the datepart. For "date" and "time" this will be an array of "Date" (unix timestamp). For "weekday", it will be an array of integers ranging from 0 (sunday) to 6 (saturday) reflecting the equivalent weekday (for currentdate-test).|< integer(int64) > array|
|**extensionskey**  <br>*optional*|The [extension key](#possible-extensions) (for body-test).|string|
|**extensionsvalue**  <br>*optional*|A value for the given key. If the key has no value the value given here is ignored (for body-test).|string|
|**headers**  <br>*optional*|An array containing the header fields (for address-, envelope- and header-test).|< string > array|
|**id**  <br>*optional*|The name of the test command, see [Possible tests](#possible-tests).|string|
|**size**  <br>*optional*|The size in bytes (for size-test).|integer(int64)|
|**values**  <br>*optional*|An array containing the value for the header fields or the values for the body. The test will be true if any of the strings matches (for address-, envelope-, header-test and body-test).|< string > array|


<a name="mailfilterrule"></a>
# MailFilterRule

|Name|Description|Schema|
|---|---|---|
|**actioncmds**  <br>*optional*|An array of action commands.|< [MailFilterAction](#mailfilteraction) > array|
|**active**  <br>*optional*|If this rule is active or not.|boolean|
|**errormsg**  <br>*optional*|If this rule cannot be read in this string is filled containing a message why, or what part of the rule isn't known.|string|
|**flags**  <br>*optional*|An array containing flags which are set on this rule. Each flag can only contain the following characters: 1-9 a-z A-Z. Currently 3 flags are reserved here: "spam" which marks the default spam rule, "vacation" which marks the vacation rules and "autoforward" which marks an autoforwarding rule.|< string > array|
|**id**  <br>*optional*|A unique identifier of the rule (once created must not be changed).|integer|
|**position**  <br>*optional*|The position inside the mail filter list (starts with 0).|integer|
|**rulename**  <br>*optional*|A name describing the rule, can be empty but must not contain a line break.|string|
|**test**  <br>*optional*||[MailFilterTest](#mailfiltertest)|
|**text**  <br>*optional*|If this rule cannot be read in this string is filled containing the whole lines of this command.|string|


<a name="mailfilterrulesresponse"></a>
# MailFilterRulesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||< [MailFilterRule](#mailfilterrule) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailfilterscriptresponse"></a>
# MailFilterScriptResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The mail filter script.|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailfiltertest"></a>
# MailFilterTest

|Name|Description|Schema|
|---|---|---|
|**comparison**  <br>*optional*|The comparison type, see [Possible comparisons](#possible-comparisons).|string|
|**datepart**  <br>*optional*|Type of the comparison, which can be "date", "weekday" or "time" (available since v7.6.1) (for currentdate-test).|string|
|**datevalue**  <br>*optional*|Contains the corresponding value to the datepart. For "date" and "time" this will be an array of "Date" (unix timestamp). For "weekday", it will be an array of integers ranging from 0 (sunday) to 6 (saturday) reflecting the equivalent weekday (for currentdate-test).|< integer(int64) > array|
|**extensionskey**  <br>*optional*|The [extension key](#possible-extensions) (for body-test).|string|
|**extensionsvalue**  <br>*optional*|A value for the given key. If the key has no value the value given here is ignored (for body-test).|string|
|**headers**  <br>*optional*|An array containing the header fields (for address-, envelope- and header-test).|< string > array|
|**id**  <br>*optional*|The name of the test command, see [Possible tests](#possible-tests).|string|
|**size**  <br>*optional*|The size in bytes (for size-test).|integer(int64)|
|**test**  <br>*optional*||[MailFilterNotTest](#mailfilternottest)|
|**values**  <br>*optional*|An array containing the value for the header fields or the values for the body. The test will be true if any of the strings matches (for address-, envelope-, header-test and body-test).|< string > array|


<a name="mailheadersresponse"></a>
# MailHeadersResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The (formatted) message headers as plain text.|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailimportresponse"></a>
# MailImportResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of JSON objects each describing the folder ID and object ID of one imported mail.|< [MailDestinationData](#maildestinationdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="maillistelement"></a>
# MailListElement

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*|The object ID of the related folder.|string|
|**id**  <br>*required*|The object ID of the mail.|string|


<a name="mailreplydata"></a>
# MailReplyData

|Name|Description|Schema|
|---|---|---|
|**account_id**  <br>*optional*|Message's account identifier. Since v6.20.2.|integer|
|**account_name**  <br>*optional*|Message's account name.|string|
|**attachment**  <br>*optional*|Indicates whether this mail has attachments.|boolean|
|**attachments**  <br>*optional*|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|< [MailAttachment](#mailattachment) > array|
|**bcc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|< < string > array > array|
|**cc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|< < string > array > array|
|**cid**  <br>*optional*|The value of the "Content-ID" header, if the header is present.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|integer|
|**content_type**  <br>*optional*|The MIME type of the mail.|string|
|**disp_notification_to**  <br>*optional*|Content of message's header "Disposition-Notification-To".|string|
|**flag_seen**  <br>*optional*|Special field to sort mails by seen status.|string|
|**flags**  <br>*optional*|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|integer|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**from**  <br>*optional*|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|< < string > array > array|
|**headers**  <br>*optional*|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|object|
|**id**  <br>*optional*|Object ID of the mail.|string|
|**level**  <br>*optional*|Zero-based nesting level in a thread.|integer|
|**msg_ref**  <br>*optional*|Message reference on reply/forward.|string|
|**msgref**  <br>*optional*|Indicates the ID of the referenced original mail.|string|
|**original_folder_id**  <br>*optional*|The original folder identifier (e.g. if fetched from "virtual/all" folder).|string|
|**original_id**  <br>*optional*|The original mail identifier (e.g. if fetched from "virtual/all" folder).|string|
|**priority**  <br>*optional*|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|integer|
|**received_date**  <br>*optional*|Date and time as measured by the receiving server.|integer(int64)|
|**sent_date**  <br>*optional*|Date and time as specified in the mail by the sending client.|integer(int64)|
|**size**  <br>*optional*|The size if the mail in bytes.|integer(int64)|
|**source**  <br>*optional*|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|string|
|**subject**  <br>*optional*|The mail's subject.|string|
|**to**  <br>*optional*|Each element is a two-element array (see the from field) specifying one receiver.|< < string > array > array|
|**truncated**  <br>*optional*|true/false if the mail content was trimmed. Since v7.6.1|boolean|
|**user**  <br>*optional*|An array with user-defined flags as strings.|< string > array|


<a name="mailreplyresponse"></a>
# MailReplyResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MailReplyData](#mailreplydata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailresponse"></a>
# MailResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MailData](#maildata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailsourceresponse"></a>
# MailSourceResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The complete message source as plain text.|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailupdatebody"></a>
# MailUpdateBody

|Name|Description|Schema|
|---|---|---|
|**clear_flags**  <br>*optional*|A set of flags to remove. Note: Flags for "recent" (8) and "user" (64) are ignored (available since SP5 v6.10).|integer|
|**color_label**  <br>*optional*|The color number between 0 and 10.|integer|
|**flags**  <br>*optional*|A set of flags to add or remove. Note: Flags for "recent" (8) and "user" (64) are ignored.|integer|
|**folder_id**  <br>*optional*|The object ID of the destination folder (if the mail shall be moved).|string|
|**set_flags**  <br>*optional*|A set of flags to add. Note: Flags for "recent" (8) and "user" (64) are ignored (available since SP5 v6.10).|integer(int64)|
|**value**  <br>*optional*|Use true to add the flags specified by flags (logical OR) and false to remove them (logical AND with the inverted value).|boolean|


<a name="mailupdatesresponse"></a>
# MailUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mail_categoriesmovebody"></a>
# Mail_CategoriesMoveBody

|Name|Description|Schema|
|---|---|---|
|**folder_id**  <br>*optional*|The folder ID of the mail|string|
|**id**  <br>*optional*|The object ID of the mail|string|


<a name="mail_categoriestrainbody"></a>
# Mail_CategoriesTrainBody

|Name|Description|Schema|
|---|---|---|
|**from**  <br>*optional*|An array of email addresses|< string > array|


<a name="mail_categoriesunreadresponse"></a>
# Mail_CategoriesUnreadResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|A JSON object with a field for each active category containing the number of unread messages.|object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailsallseenresponse"></a>
# MailsAllSeenResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||boolean|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailscleanupresponse"></a>
# MailsCleanUpResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array with IDs of objects that could not be processed.|< string > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="mailsresponse"></a>
# MailsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of mails. Each mail is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingaccountdata"></a>
# MessagingAccountData

|Name|Description|Schema|
|---|---|---|
|**configuration**  <br>*optional*|The configuration data according to the `formDescription` of the relevant messaging service.|object|
|**displayName**  <br>*optional*|User chosen string to identify a given account. Will also be translated into the folder name of the folder representing the accounts content.|string|
|**id**  <br>*optional*|Identifier of the messaging account.|integer|
|**messagingService**  <br>*optional*|The messaging service ID of the messaging service this account belongs to.|string|


<a name="messagingaccountresponse"></a>
# MessagingAccountResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MessagingAccountData](#messagingaccountdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingaccountupdateresponse"></a>
# MessagingAccountUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The response value.|integer|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingaccountsresponse"></a>
# MessagingAccountsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array containing JSON objects representing messaging accounts.|< [MessagingAccountData](#messagingaccountdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingformdescription"></a>
# MessagingFormDescription

|Name|Description|Schema|
|---|---|---|
|**defaultValue**  <br>*optional*|Can contain a default value.|object|
|**displayName**  <br>*optional*|The display name of the field.|string|
|**mandatory**  <br>*optional*|Indicates whether the field is mandatory.|boolean|
|**name**  <br>*optional*|The name of the field.|string|
|**options**  <br>*optional*|A list of available options in the field.|< object > array|
|**widget**  <br>*optional*|The name of the widget.|string|


<a name="messagingmessagedata"></a>
# MessagingMessageData

|Name|Description|Schema|
|---|---|---|
|**body**  <br>*optional*|A JSON object representing the content of the message.|object|
|**colorLabel**  <br>*optional*|An arbitrary number marking the message in a certain color. The same as the color label common to all groupware objects.|integer|
|**flags**  <br>*optional*|Bitmask showing the state of this message. The same as in the module "mail".|integer|
|**folder**  <br>*optional*|The folder ID.|string|
|**headers**  <br>*optional*|A JSON object of header data. Usually the value is either a string or an array (if it has more than one value). Certain headers are rendered as more complex structures.|object|
|**id**  <br>*optional*|The ID of the message. Only unique in the given folder.|string|
|**picture**  <br>*optional*|The URL to a picture for this message.|string|
|**receivedDate**  <br>*optional*|The time this message was received.|integer(int64)|
|**sectionId**  <br>*optional*|The section ID of a certain message part, if the content-type is `multipart/*`.|string|
|**size**  <br>*optional*|The size of the message in bytes.|integer(int64)|
|**threadLevel**  <br>*optional*|The nesting level of this message according to the conversation it's belonged to. May not be set.|integer|
|**url**  <br>*optional*|A link to the messages origin currently used in RSS messages.|string|
|**user**  <br>*optional*|An array of strings representing user flags.|< string > array|


<a name="messagingmessageresponse"></a>
# MessagingMessageResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MessagingMessageData](#messagingmessagedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingmessageupdateresponse"></a>
# MessagingMessageUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The response value.|integer|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingmessagesresponse"></a>
# MessagingMessagesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of messages. Each message is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingservicedata"></a>
# MessagingServiceData

|Name|Description|Schema|
|---|---|---|
|**displayName**  <br>*optional*|Human-readable display name of the service.|string|
|**formDescription**  <br>*optional*|An array of dynamic form fields. Same as in PubSub.|< [MessagingFormDescription](#messagingformdescription) > array|
|**id**  <br>*optional*|The identifier of the messaging service. This is usually a string in reverse domain name notation, like "com.openexchange.messaging.twitter".|string|
|**messagingActions**  <br>*optional*|An array representing a dynamic set of actions that are possible with messages of this service.|< string > array|


<a name="messagingserviceresponse"></a>
# MessagingServiceResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[MessagingServiceData](#messagingservicedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="messagingservicesresponse"></a>
# MessagingServicesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array containing JSON objects representing messaging services.|< [MessagingServiceData](#messagingservicedata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthaccountdata"></a>
# OAuthAccountData

|Name|Description|Schema|
|---|---|---|
|**displayName**  <br>*optional*|The account's display name.|string|
|**id**  <br>*optional*|The numeric identifier of the OAuth account.|integer|
|**secret**  <br>*optional*|The token secret.|string|
|**serviceId**  <br>*optional*|The identifier of the associated service meta data, e.g. "com.openexchange.oauth.twitter".|string|
|**token**  <br>*optional*|The token.|string|


<a name="oauthaccountdeletionresponse"></a>
# OAuthAccountDeletionResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Indicates whether the the account was deleted successfully.|boolean|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthaccountinteraction"></a>
# OAuthAccountInteraction

|Name|Description|Schema|
|---|---|---|
|**authUrl**  <br>*optional*|The numeric identifier of the OAuth account.|string|
|**token**  <br>*optional*|The token.|string|
|**type**  <br>*optional*|The interaction type name, which can be "outOfBand" or "callback".|string|
|**uuid**  <br>*optional*|The UUID for this OAuth interaction.|string|


<a name="oauthaccountinteractionresponse"></a>
# OAuthAccountInteractionResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[OAuthAccountInteraction](#oauthaccountinteraction)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthaccountresponse"></a>
# OAuthAccountResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[OAuthAccountData](#oauthaccountdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthaccountupdateresponse"></a>
# OAuthAccountUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Indicates whether the the account was updated successfully.|boolean|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthaccountsresponse"></a>
# OAuthAccountsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of OAuth account objects.|< [OAuthAccountData](#oauthaccountdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthclientdata"></a>
# OAuthClientData

|Name|Description|Schema|
|---|---|---|
|**description**  <br>*optional*|A description of the client.|string|
|**icon**  <br>*optional*|A URL or path to obtain the client's icon via the image module.|string|
|**id**  <br>*optional*|The client's ID.|string|
|**name**  <br>*optional*|The client's/service's name.|string|
|**website**  <br>*optional*|A URL to the client's website.|string|


<a name="oauthgrantdata"></a>
# OAuthGrantData

|Name|Description|Schema|
|---|---|---|
|**client**  <br>*optional*||[OAuthClientData](#oauthclientdata)|
|**date**  <br>*optional*|The time when the access was granted.|integer(int64)|
|**scopes**  <br>*optional*|A mapping from scope tokens to translated, human-readable descriptions for every scope that was granted to the external service (example: {"read_contacts":"See all your contacts"}).|object|


<a name="oauthgrantsresponse"></a>
# OAuthGrantsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array containing one object for every granted access.|< [OAuthGrantData](#oauthgrantdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthservicemetadata"></a>
# OAuthServiceMetaData

|Name|Description|Schema|
|---|---|---|
|**displayName**  <br>*optional*|The service's display name.|string|
|**id**  <br>*optional*|The identifier of the service meta data, e.g. "com.openexchange.oauth.twitter".|string|


<a name="oauthserviceresponse"></a>
# OAuthServiceResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[OAuthServiceMetaData](#oauthservicemetadata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="oauthservicesresponse"></a>
# OAuthServicesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array with OAuth service meta data.|< [OAuthServiceMetaData](#oauthservicemetadata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="passwordchangebody"></a>
# PasswordChangeBody

|Name|Description|Schema|
|---|---|---|
|**new_password**  <br>*optional*|The new password the user wants to set or `null` to remove the password (especially for guest users).|string|
|**old_password**  <br>*optional*|The user's current password or `null` if the password wasn't set before (especially for guest users).|string|


<a name="quotadata"></a>
# QuotaData

|Name|Description|Schema|
|---|---|---|
|**quota**  <br>*optional*|Represents the maximum storage (-1 represents an unlimited quota).|integer(int64)|
|**use**  <br>*optional*|Represents the used storage.|integer(int64)|


<a name="quotaresponse"></a>
# QuotaResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[QuotaData](#quotadata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="quotasresponse"></a>
# QuotasResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Dependent on the request parameters: the payload may be a JSON object containing the quota modules as<br>fields that represent JSON objects itself with the properties "display_name" and "accounts" (array of account data objects)<br>or it may be a JSON array of account data objects if the parameter "module" specifies a certain quota module.|object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="reminderdata"></a>
# ReminderData

|Name|Description|Schema|
|---|---|---|
|**alarm**  <br>*optional*|The time of the alarm.|integer(int64)|
|**folder**  <br>*optional*|The ID of the folder through that the object can be read.|integer|
|**id**  <br>*optional*|The ID of the reminder.|integer|
|**last_modified**  <br>*optional*|The last modification timestamp of the reminder.|integer(int64)|
|**module**  <br>*optional*|The module of the reminder's target object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|integer|
|**recurrence_position**  <br>*optional*|The recurrence position for series appointments or 0 if no series.|integer|
|**server_time**  <br>*optional*|The time on the server.|integer(int64)|
|**target_id**  <br>*optional*|The object ID of the target this reminder is attached to.|integer|
|**user_id**  <br>*optional*|The ID of the user.|integer|


<a name="reminderlistelement"></a>
# ReminderListElement

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|The ID of the reminder.|integer|


<a name="reminderresponse"></a>
# ReminderResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ReminderData](#reminderdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="reminderupdatebody"></a>
# ReminderUpdateBody

|Name|Description|Schema|
|---|---|---|
|**alarm**  <br>*optional*|The new time of the alarm.|integer(int64)|


<a name="remindersresponse"></a>
# RemindersResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ReminderData](#reminderdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="resolvesharereferenceelement"></a>
# ResolveShareReferenceElement

|Name|Description|Schema|
|---|---|---|
|**reference**  <br>*required*|The reference string.|string|


<a name="resolvesharereferenceresponse"></a>
# ResolveShareReferenceResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[data](#resolvesharereferenceresponse-data)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|

<a name="resolvesharereferenceresponse-data"></a>
**data**

|Name|Description|Schema|
|---|---|---|
|**contextId**  <br>*optional*|The originator's context identifier.|integer|
|**expiration**  <br>*optional*|The optional expiration date of the share link.|integer(int64)|
|**files**  <br>*optional*|The file meta data.|< [files](#resolvesharereferenceresponse-files) > array|
|**password**  <br>*optional*|The optional password that protects the share link.|string|
|**shareToken**  <br>*optional*|The associated share token.|string|
|**userId**  <br>*optional*|The originator's user identifier.|integer|

<a name="resolvesharereferenceresponse-files"></a>
**files**

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**com.openexchange.file.storage.mail.mailMetadata**  <br>*optional*|Contains additional metadata for items in the "maildrive" file storage (read-only). (available since 7.8.2)|object|
|**com.openexchange.realtime.resourceID**  <br>*optional*|The resource identifier for the infoitem for usage within the realtime component (read-only). (available since 7.8.0)|string|
|**com.openexchange.share.extendedObjectPermissions**  <br>*optional*|An array of extended object permissions (read-only). (available since 7.8.0)|< [InfoItemExtendedPermission](#infoitemextendedpermission) > array|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**current_version**  <br>*optional*|"true" if this version is the current version, "false" otherwise. Note: This is not writeable.|boolean|
|**description**  <br>*optional*|A description if the item.|string|
|**file_md5sum**  <br>*optional*|MD5Sum of the document.|string|
|**file_mimetype**  <br>*optional*|MIME type of the document. The client converts known types to more readable names before displaying them.|string|
|**file_size**  <br>*optional*|The size of the document in bytes.|integer(int64)|
|**filename**  <br>*optional*|Displayed filename of the document.|string|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**id**  <br>*optional*|Object ID.|string|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**locked_until**  <br>*optional*|The time until which this item will presumably be locked. Only set if the docment is currently locked, 0 otherwise.|integer(int64)|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**number_of_versions**  <br>*optional*|The number of all versions of the item. Note: This is not writeable.|integer|
|**object_permissions**  <br>*optional*|An array of object permissions (preliminary, available since v7.8.0).|< [InfoItemPermission](#infoitempermission) > array|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**shareable**  <br>*optional*|(read-only) Indicates if the item can be shared (preliminary, available since v7.8.0).|boolean|
|**title**  <br>*optional*|The title.|string|
|**url**  <br>*optional*|Link/URL to item.|string|
|**version**  <br>*optional*|Version of the document. NULL can be used to denote the most recent version and will be set in responses if versions are not supported by the underlying storage.|string|
|**version_comment**  <br>*optional*|A version comment is used to file a changelog for the file.|string|


<a name="resourcedata"></a>
# ResourceData

|Name|Description|Schema|
|---|---|---|
|**availability**  <br>*optional*|Can be false to mark the resource currently unavailable.|boolean|
|**description**  <br>*optional*|The description of the resource.|string|
|**display_name**  <br>*optional*|Display name of the resource.|string|
|**id**  <br>*optional*|The resource ID.|integer|
|**last_modified**  <br>*optional*|Date and time of the last modification.|string|
|**last_modified_utc**  <br>*optional*|Timestamp of the last modification.|string|
|**mailaddress**  <br>*optional*|Email address of the resource.|string|
|**name**  <br>*optional*|Internal name with character restrictions.|string|


<a name="resourcelistelement"></a>
# ResourceListElement

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|ID of a resource.|integer|


<a name="resourceresponse"></a>
# ResourceResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ResourceData](#resourcedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="resourcesearchbody"></a>
# ResourceSearchBody

|Name|Description|Schema|
|---|---|---|
|**pattern**  <br>*optional*|Search pattern to find resources. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|string|


<a name="resourceupdatedata"></a>
# ResourceUpdateData

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|The ID of a newly created rsource.|integer|


<a name="resourceupdateresponse"></a>
# ResourceUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ResourceUpdateData](#resourceupdatedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="resourceupdatesdata"></a>
# ResourceUpdatesData

|Name|Description|Schema|
|---|---|---|
|**deleted**  <br>*optional*|Array of deleted resource objects.|< [ResourceData](#resourcedata) > array|
|**modified**  <br>*optional*|Array of modified resource objects.|< [ResourceData](#resourcedata) > array|
|**new**  <br>*optional*|Array of new resource objects.|< [ResourceData](#resourcedata) > array|


<a name="resourceupdatesresponse"></a>
# ResourceUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ResourceUpdatesData](#resourceupdatesdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="resourcesresponse"></a>
# ResourcesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of resource objects.|< [ResourceData](#resourcedata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="sendmaildata"></a>
# SendMailData

|Name|Description|Schema|
|---|---|---|
|**account_id**  <br>*optional*|Message's account identifier. Since v6.20.2.|integer|
|**account_name**  <br>*optional*|Message's account name.|string|
|**attachment**  <br>*optional*|Indicates whether this mail has attachments.|boolean|
|**attachments**  <br>*optional*|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|< [MailAttachment](#mailattachment) > array|
|**bcc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|< < string > array > array|
|**cc**  <br>*optional*|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|< < string > array > array|
|**cid**  <br>*optional*|The value of the "Content-ID" header, if the header is present.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|integer|
|**content_type**  <br>*optional*|The MIME type of the mail.|string|
|**disp_notification_to**  <br>*optional*|Content of message's header "Disposition-Notification-To".|string|
|**flag_seen**  <br>*optional*|Special field to sort mails by seen status.|string|
|**flags**  <br>*optional*|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|integer|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**from**  <br>*optional*|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|< < string > array > array|
|**headers**  <br>*optional*|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|object|
|**id**  <br>*optional*|Object ID of the mail.|string|
|**infostore_ids**  <br>*optional*|JSON array of infostore document ID(s) that ought to be appended to the mail as attachments.|< string > array|
|**level**  <br>*optional*|Zero-based nesting level in a thread.|integer|
|**msg_ref**  <br>*optional*|Message reference on reply/forward.|string|
|**msgref**  <br>*optional*|Indicates the ID of the referenced original mail.|string|
|**original_folder_id**  <br>*optional*|The original folder identifier (e.g. if fetched from "virtual/all" folder).|string|
|**original_id**  <br>*optional*|The original mail identifier (e.g. if fetched from "virtual/all" folder).|string|
|**priority**  <br>*optional*|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|integer|
|**received_date**  <br>*optional*|Date and time as measured by the receiving server.|integer(int64)|
|**sendtype**  <br>*optional*|Indicates the type of the meessage: 0 (normal new mail), 1 (a reply mail, field "msgref" must be present), 2 (a forward mail, field "msgref" must be present), 3 (draft edit operation, field "msgref" must be present in order to delete previous draft message since e.g. IMAP does not support changing/replacing a message but requires a delete-and-insert sequence), 4 (transport of a draft mail, field "msgref" must be present), 6 (signals that user intends to send out a saved draft message and expects the draft message (referenced by "msgref" field) being deleted after successful transport).|integer|
|**sent_date**  <br>*optional*|Date and time as specified in the mail by the sending client.|integer(int64)|
|**size**  <br>*optional*|The size if the mail in bytes.|integer(int64)|
|**source**  <br>*optional*|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|string|
|**subject**  <br>*optional*|The mail's subject.|string|
|**to**  <br>*optional*|Each element is a two-element array (see the from field) specifying one receiver.|< < string > array > array|
|**truncated**  <br>*optional*|true/false if the mail content was trimmed. Since v7.6.1|boolean|
|**user**  <br>*optional*|An array with user-defined flags as strings.|< string > array|
|**vcard**  <br>*optional*|The user's VCard.|integer|


<a name="sharelinkdata"></a>
# ShareLinkData

|Name|Description|Schema|
|---|---|---|
|**entity**  <br>*optional*|The identifier of the anonymous user entity for the share (read-only).|integer|
|**expiry_date**  <br>*optional*|The end date / expiration time after which the share link is no longer accessible.|integer(int64)|
|**is_new**  <br>*optional*|Whether the share link is new, i.e. it has been created by the `/share/management?action=getLink` request, or if it already existed (read-only).|boolean|
|**meta**  <br>*optional*|Can be used by the client to save arbitrary JSON data along with the share.|object|
|**password**  <br>*optional*|An additional secret / pin number an anonymous user needs to enter when accessing the share.|string|
|**url**  <br>*optional*|The link to share (read-only).|string|


<a name="sharelinkresponse"></a>
# ShareLinkResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[ShareLinkData](#sharelinkdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="sharelinksendbody"></a>
# ShareLinkSendBody

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*optional*|The folder identifier.|string|
|**item**  <br>*optional*|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|string|
|**message**  <br>*optional*|Can contain an optional custom message.|string|
|**module**  <br>*optional*|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|string|
|**recipients**  <br>*optional*|An array that lists the recipients. Each element is itself a two-element array specifying one recipient. The first element represents the personal name, the second element is the email address.|< < string > array > array|


<a name="sharelinksendresponse"></a>
# ShareLinkSendResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|
|**warnings**  <br>*optional*|Can contain possible warnings during sending of the notifications.|< object > array|


<a name="sharelinkupdatebody"></a>
# ShareLinkUpdateBody

|Name|Description|Schema|
|---|---|---|
|**entity**  <br>*optional*|The identifier of the anonymous user entity for the share (read-only).|integer|
|**expiry_date**  <br>*optional*|The end date / expiration time after which the share link is no longer accessible.|integer(int64)|
|**folder**  <br>*optional*|The folder identifier.|string|
|**is_new**  <br>*optional*|Whether the share link is new, i.e. it has been created by the `/share/management?action=getLink` request, or if it already existed (read-only).|boolean|
|**item**  <br>*optional*|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|string|
|**meta**  <br>*optional*|Can be used by the client to save arbitrary JSON data along with the share.|object|
|**module**  <br>*optional*|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|string|
|**password**  <br>*optional*|An additional secret / pin number an anonymous user needs to enter when accessing the share.|string|
|**url**  <br>*optional*|The link to share (read-only).|string|


<a name="sharetargetdata"></a>
# ShareTargetData

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*optional*|The folder identifier.|string|
|**item**  <br>*optional*|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|string|
|**module**  <br>*optional*|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|string|


<a name="singlerequest"></a>
# SingleRequest
Contains all currently available (resp. possible) parameters that could be specified to perform a request in the multiple module except `action`, `module`, and `data` which are part of the actual request itself.


|Name|Description|Schema|
|---|---|---|
|**accountId**  <br>*optional*|The "accountId" parameter of a request.|integer|
|**action**  <br>*required*|The name of the request's action like "all", "list", etc.|string|
|**all**  <br>*optional*|The "all" parameter of a request.|integer|
|**allowed_modules**  <br>*optional*|The "allowed_modules" parameter of a request.|string|
|**attach_src**  <br>*optional*|The "attach_src" parameter of a request.|boolean|
|**attached**  <br>*optional*|The "attached" parameter of a request.|integer|
|**attachment**  <br>*optional*|The "attachment" parameter of a request.|string|
|**attachmentid**  <br>*optional*|The "attachmentid" parameter of a request.|string|
|**cascadePermissions**  <br>*optional*|The "cascadePermissions" parameter of a request.|string|
|**cid**  <br>*optional*|The "cid" parameter of a request.|string|
|**client**  <br>*optional*|The "client" parameter of a request.|string|
|**columns**  <br>*optional*|The "columns" parameter of a request.|string|
|**content_type**  <br>*optional*|The "content_type" parameter of a request.|string|
|**data**  <br>*optional*|The request's body as a JSON object.|object|
|**diff**  <br>*optional*|The "diff" parameter of a request.|integer(int64)|
|**displayName**  <br>*optional*|The "displayName" parameter of a request.|string|
|**edit**  <br>*optional*|The "edit" parameter of a request.|integer|
|**email**  <br>*optional*|The "email" parameter of a request.|string|
|**email1**  <br>*optional*|The "email1" parameter of a request.|string|
|**email2**  <br>*optional*|The "email2" parameter of a request.|string|
|**email3**  <br>*optional*|The "email3" parameter of a request.|string|
|**end**  <br>*optional*|The "end" parameter of a request.|integer(int64)|
|**errorOnDuplicateName**  <br>*optional*|The "errorOnDuplicateName" parameter of a request.|boolean|
|**filestorageService**  <br>*optional*|The "filestorageService" parameter of a request.|string|
|**filter**  <br>*optional*|The "filter" parameter of a request.|integer|
|**flags**  <br>*optional*|The "falgs" parameter of a request.|integer|
|**folder**  <br>*optional*|The "folder" parameter of a request.|string|
|**force**  <br>*optional*|The "force" parameter of a request.|boolean|
|**force_secure**  <br>*optional*|The "force_secure" parameter of a request.|string|
|**from**  <br>*optional*|The "from" parameter of a request.|integer(int64)|
|**hardDelete**  <br>*optional*|The "hardDelete" parameter of a request.|boolean|
|**hdr**  <br>*optional*|The "hdr" parameter of a request.|integer|
|**headers**  <br>*optional*|The "headers" parameter of a request.|string|
|**id**  <br>*optional*|The "id" parameter of a request.|string|
|**ignore**  <br>*optional*|The "ignore" parameter of a request.|string|
|**includeSent**  <br>*optional*|The "includeSent" parameter of a request.|boolean|
|**internal_userid**  <br>*optional*|The "internal_userid" parameter of a request.|integer|
|**left_hand_limit**  <br>*optional*|The "left_hand_limit" parameter of a request.|integer|
|**limit**  <br>*optional*|The "limit" parameter of a request.|integer|
|**lineWrapAfter**  <br>*optional*|The "lineWrapAfter" parameter of a request.|integer|
|**max_size**  <br>*optional*|The "max_size" parameter of a request.|integer|
|**merged**  <br>*optional*|The "merged" parameter of a request.|boolean|
|**messageAction**  <br>*optional*|The "messageAction" parameter of a request.|string|
|**message_id**  <br>*optional*|The "message_id" parameter of a request.|string|
|**messagingService**  <br>*optional*|The "messagingService" parameter of a request.|string|
|**module**  <br>*required*|The name of the request's module like "mail", "folders", etc.|string|
|**name**  <br>*optional*|The "name" parameter of a request.|string|
|**oauth_token**  <br>*optional*|The "oauth_token" parameter of a request.|string|
|**oauth_verifier**  <br>*optional*|The "oauth_verifier" parameter of a request.|string|
|**occurrence**  <br>*optional*|The "occurrence" parameter of a request.|string|
|**order**  <br>*optional*|The "order" parameter of a request.|string|
|**parent**  <br>*optional*|The "parent" parameter of a request.|string|
|**participant**  <br>*optional*|The "participant" parameter of a request.|string|
|**password**  <br>*optional*|The "password" parameter of a request.|string|
|**peek**  <br>*optional*|The "peek" parameter of a request.|string|
|**provider**  <br>*optional*|The "provider" parameter of a request.|string|
|**query**  <br>*optional*|The "query" parameter of a request.|string|
|**recipients**  <br>*optional*|The "recipients" parameter of a request.|string|
|**recurrence_master**  <br>*optional*|The "recurrence_master" parameter of a request.|boolean|
|**recursive**  <br>*optional*|The "recursive" parameter of a request.|string|
|**right_hand_limit**  <br>*optional*|The "right_hand_limit" parameter of a request.|integer|
|**save**  <br>*optional*|The "save" parameter of a request.|integer|
|**serviceId**  <br>*optional*|The "serviceId" parameter of a request.|string|
|**setFrom**  <br>*optional*|The "setFrom" parameter of a request.|boolean|
|**setIfAbsent**  <br>*optional*|The "setIfAbsent" parameter of a request.|string|
|**showPrivate**  <br>*optional*|The "showPrivate" parameter of a request.|boolean|
|**sort**  <br>*optional*|The "sort" parameter of a request.|string|
|**src**  <br>*optional*|The "src" parameter of a request.|integer|
|**start**  <br>*optional*|The "start" parameter of a request.|integer(int64)|
|**system**  <br>*optional*|The "system" parameter of a request.|string|
|**timestamp**  <br>*optional*|The "timestamp" parameter of a request.|integer(int64)|
|**timezone**  <br>*optional*|The "timezone" parameter of a request.|string|
|**tree**  <br>*optional*|The "tree" parameter of a request.|string|
|**type**  <br>*optional*|The "type" parameter of a request.|integer|
|**uid**  <br>*optional*|The "uid" parameter of a request.|string|
|**unseen**  <br>*optional*|The "unseen" parameter of a request.|boolean|
|**until**  <br>*optional*|The "until" parameter of a request.|integer(int64)|
|**user_id**  <br>*optional*|The "user_id" parameter of a request.|integer|
|**userid**  <br>*optional*|The "userid" parameter of a request.|integer|
|**uuid**  <br>*optional*|The "uuid" parameter of a request.|string|
|**version**  <br>*optional*|The "version" parameter of a request.|integer|
|**view**  <br>*optional*|The "view" parameter of a request.|string|


<a name="singleresponse"></a>
# SingleResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The data of a request that was processed with the multiple module.|object|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="snippetattachment"></a>
# SnippetAttachment

|Name|Description|Schema|
|---|---|---|
|**contentid**  <br>*optional*|The content ID of the attachment.|string|
|**filename**  <br>*optional*|The file name of the attachment.|string|
|**id**  <br>*optional*|The ID of the attachment.|string|
|**mimetype**  <br>*optional*|The MIME type of the attachment.|string|
|**size**  <br>*optional*|The size of the attachment in bytes.|integer(int64)|


<a name="snippetattachmentlistelement"></a>
# SnippetAttachmentListElement

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|The identifier of an attachment.|string|


<a name="snippetdata"></a>
# SnippetData

|Name|Description|Schema|
|---|---|---|
|**accountid**  <br>*optional*|The identifier of the account.|integer|
|**content**  <br>*optional*|Contains the snippet's content.|string|
|**createdby**  <br>*optional*|The user ID of the creator.|integer|
|**displayname**  <br>*optional*|The display name of the snippet.|string|
|**files**  <br>*optional*|An array of attachments.|< [SnippetAttachment](#snippetattachment) > array|
|**id**  <br>*optional*|The ID of the snippet.|string|
|**misc**  <br>*optional*|Contains miscellaneous data as JSON object.|object|
|**module**  <br>*optional*|The module identifier, like "com.openexchange.mail".|string|
|**props**  <br>*optional*|Contains custom properties as JSON object.|object|
|**shared**  <br>*optional*|The shared flag.|boolean|
|**type**  <br>*optional*|The type of the snippet, like "signature".|string|


<a name="snippetresponse"></a>
# SnippetResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[SnippetData](#snippetdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="snippetupdateresponse"></a>
# SnippetUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|The ID of the new snippet.|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="snippetsresponse"></a>
# SnippetsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array of snippet objects.|< [SnippetData](#snippetdata) > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="taskconfirmation"></a>
# TaskConfirmation

|Name|Description|Schema|
|---|---|---|
|**display_name**  <br>*optional*|Display name of external participant.|string|
|**mail**  <br>*optional*|Email address of external participant.|string|
|**message**  <br>*optional*|Confirm message of the participant.|string|
|**status**  <br>*optional*|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|integer|
|**type**  <br>*optional*|Type of participant: 0 (user), 5 (external user).|integer|


<a name="taskconfirmationbody"></a>
# TaskConfirmationBody

|Name|Description|Schema|
|---|---|---|
|**confirmation**  <br>*optional*|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|integer|
|**confirmmessage**  <br>*optional*|The confirmation message or comment.|string|


<a name="taskdata"></a>
# TaskData

|Name|Description|Schema|
|---|---|---|
|**actual_costs**  <br>*optional*|A monetary attribute to store actual costs of a task. Allowed values must be in the range -9999999999.99 and 9999999999.99.|number(double)|
|**actual_duration**  <br>*optional*|Actual duration of the task, e.g. in minutes.|string|
|**after_complete**  <br>*optional*|Deprecated. Only present in AJAX interface. Value will not be stored on OX server.|integer(int64)|
|**alarm**  <br>*optional*|Specifies when to notify the participants as the number of minutes before the start of the appointment (-1 for "no alarm"). For tasks, the Time value specifies the absolute time when the user should be notified.|integer(int64)|
|**billing_information**  <br>*optional*|Billing information of the task.|string|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**companies**  <br>*optional*|Companies.|string|
|**confirmations**  <br>*optional*|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|< [TaskConfirmation](#taskconfirmation) > array|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**currency**  <br>*optional*|The currency, e.g. "EUR".|string|
|**date_completed**  <br>*optional*||integer(int64)|
|**day_in_month**  <br>*optional*|Specifies which day of a month is part of the sequence. Counting starts with 1. If the field "days" is also present, only days selected by that field are counted. If the number is bigger than the number of available days, the last available day is selected. Present if and only if recurrence_type > 2.|integer|
|**days**  <br>*optional*|Specifies which days of the week are part of a sequence. The value is a bitfield with bit 0 indicating sunday, bit 1 indicating monday and so on. May be present if recurrence_type > 1. If allowed but not present, the value defaults to 127 (all 7 days).|integer|
|**end_date**  <br>*optional*|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|integer(int64)|
|**end_time**  <br>*optional*|Exclusive end as Date for whole day tasks and as Time for normal tasks.|integer(int64)|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**full_time**  <br>*optional*|True if the event is a whole day appointment or task, false otherwise.|boolean|
|**id**  <br>*optional*|Object ID.|string|
|**interval**  <br>*optional*|Specifies an integer multiplier to the interval specified by recurrence_type. Present if and only if recurrence_type > 0. Must be 1 if recurrence_type = 4.|integer|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**month**  <br>*optional*|Month of the year in yearly sequencies. 0 represents January, 1 represents February and so on. Present if and only if recurrence_type = 4.|integer|
|**note**  <br>*optional*|Long description.|string|
|**notification**  <br>*optional*|If true, all participants are notified of any changes to this object. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|boolean|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**occurrences**  <br>*optional*|Specifies how often a recurrence should appear. May be present only if recurrence_type > 0.|integer|
|**organizer**  <br>*optional*|Contains the email address of the appointment organizer which is not necessarily an internal user. Not implemented for tasks.|string|
|**organizerId**  <br>*optional*|Contains the userIId of the appointment organizer if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|integer|
|**participants**  <br>*optional*|Each element identifies a participant, user, group or booked resource.|< [TaskParticipant](#taskparticipant) > array|
|**percent_completed**  <br>*optional*|How much of the task is completed. An integer number between 0 and 100.|integer|
|**principal**  <br>*optional*|Contains the email address of the appointment principal which is not necessarily an internal user. Not implemented for tasks (introduced with 6.20.1).|string|
|**principalId**  <br>*optional*|Contains the userIId of the appointment principal if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|integer|
|**priority**  <br>*optional*|The priority of the task: 1 (low), 2 (medium), 3 (high).|integer|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**recurrence_type**  <br>*optional*|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|integer|
|**sequence**  <br>*optional*|iCal sequence number. Not implemented for tasks. Must be incremented on update. Will be incremented by the server, if not set.|integer|
|**start_date**  <br>*optional*|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|integer(int64)|
|**start_time**  <br>*optional*|Inclusive start as Date for whole day tasks and Time for normal tasks.|integer(int64)|
|**status**  <br>*optional*|Status of the task: 1 (not started), 2 (in progress), 3 (done), 4 (waiting), 5 (deferred).|integer|
|**target_costs**  <br>*optional*|A monetary attribute to store target costs of a task. Allowed values must be in the range -9999999999.99 and 9999999999.99.|number(double)|
|**target_duration**  <br>*optional*|Target duration of the task, e.g. in minutes.|string|
|**title**  <br>*optional*|Short description.|string|
|**trip_meter**  <br>*optional*|The trip meter.|string|
|**uid**  <br>*optional*|Can only be written when the object is created. Internal and external globally unique identifier of the appointment or task. Is used to recognize appointments within iCal files. If this attribute is not written it contains an automatic generated UUID.|string|
|**until**  <br>*optional*|Inclusive end date of a sequence. May be present only if recurrence_type > 0. The sequence has no end date if recurrence_type > 0 and this field is not present. Note: since this is a Date, the entire day after the midnight specified by the value is included.|integer(int64)|
|**users**  <br>*optional*|Each element represents a participant. User groups are resolved and are represented by their members. Any user can occur only once.|< [TaskUser](#taskuser) > array|


<a name="taskdeletionsresponse"></a>
# TaskDeletionsResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|An array with object IDs of tasks which were modified after the specified timestamp and were therefore not deleted.|< string > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="tasklistelement"></a>
# TaskListElement

|Name|Description|Schema|
|---|---|---|
|**folder**  <br>*required*|The object ID of the related folder.|string|
|**id**  <br>*required*|The object ID of the task.|string|


<a name="taskparticipant"></a>
# TaskParticipant

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|User ID.|integer|
|**mail**  <br>*optional*|Mail address of an external participant.|string|
|**type**  <br>*optional*|Type of participant: 1 (user), 2 (user group), 3 (resource), 4 (resource group), 5 (external user)|integer|


<a name="taskresponse"></a>
# TaskResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[TaskData](#taskdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="tasksearchbody"></a>
# TaskSearchBody

|Name|Description|Schema|
|---|---|---|
|**end**  <br>*optional*|Exclusive end date for a time range the tasks should end in. If this parameter is omitted the time range has an open end.|integer(int64)|
|**folder**  <br>*optional*|Defines the folder to search for tasks in. If this is omitted in all task folders will be searched.|string|
|**pattern**  <br>*required*|Search pattern to find tasks. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|string|
|**start**  <br>*optional*|Inclusive start date for a time range the tasks should end in. If start is omitted end is ignored.|integer(int64)|


<a name="taskupdatedata"></a>
# TaskUpdateData

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*|ID of a newly created task.|string|


<a name="taskupdateresponse"></a>
# TaskUpdateResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[TaskUpdateData](#taskupdatedata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="taskupdatesresponse"></a>
# TaskUpdatesResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of tasks.|< object > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="taskuser"></a>
# TaskUser

|Name|Description|Schema|
|---|---|---|
|**confirm**  <br>*optional*|0 (none), 1 (accepted), 2 (declined), 3 (tentative)|integer|
|**confirmmessage**  <br>*optional*|Confirm message of the participant.|string|
|**display_name**  <br>*optional*|Displayable name of the participant.|string|
|**id**  <br>*optional*|User ID. Confirming for other users only works for appointments and not for tasks.|integer|


<a name="tasksresponse"></a>
# TasksResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of tasks. Each task is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="tokenloginresponse"></a>
# TokenLoginResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**jsessionid**  <br>*optional*||string|
|**serverToken**  <br>*optional*|The token generated by the server.|string|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|
|**url**  <br>*optional*|The URL of the redirect to the web UI.|string|
|**user**  <br>*optional*|The username.|string|
|**user_id**  <br>*optional*|The user ID.|integer|


<a name="tokensdata"></a>
# TokensData

|Name|Description|Schema|
|---|---|---|
|**context_id**  <br>*optional*|The context ID.|integer|
|**locale**  <br>*optional*|The users locale (e.g. "en_US").|string|
|**session**  <br>*optional*|The session ID.|string|
|**user**  <br>*optional*|The username.|string|
|**user_id**  <br>*optional*|The user ID.|integer|


<a name="tokensresponse"></a>
# TokensResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[TokensData](#tokensdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="userattribute"></a>
# UserAttribute

|Name|Description|Schema|
|---|---|---|
|**name**  <br>*optional*|The name of the attribute.|string|
|**value**  <br>*optional*|The value of the attribute.|string|


<a name="userattributeresponse"></a>
# UserAttributeResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[UserAttribute](#userattribute)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="userattributionresponse"></a>
# UserAttributionResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Indicates whether the attribute could be set.|boolean|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="userdata"></a>
# UserData

|Name|Description|Schema|
|---|---|---|
|**addressBusiness**  <br>*optional*|Support for Outlook 'business' address field. (since 6.20.1)|string|
|**addressHome**  <br>*optional*|Support for Outlook 'home' address field. (since 6.20.1)|string|
|**addressOther**  <br>*optional*|Support for Outlook 'other' address field. (since 6.20.1)|string|
|**aliases**  <br>*optional*|The user's aliases.|< string > array|
|**anniversary**  <br>*optional*|The anniversary.|integer(int64)|
|**assistant_name**  <br>*optional*|The assistant's name.|string|
|**birthday**  <br>*optional*|The date of birth.|integer(int64)|
|**branches**  <br>*optional*|The branches.|string|
|**business_category**  <br>*optional*|The business category.|string|
|**categories**  <br>*optional*|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|string|
|**cellular_telephone1**  <br>*optional*|The cellular telephone number 1.|string|
|**cellular_telephone2**  <br>*optional*|The cellular telephone number 2.|string|
|**city_business**  <br>*optional*|The city of the business address.|string|
|**city_home**  <br>*optional*|The city of the home address.|string|
|**city_other**  <br>*optional*|The city of another address.|string|
|**color_label**  <br>*optional*|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|integer|
|**commercial_register**  <br>*optional*|The commercial register.|string|
|**company**  <br>*optional*|The company name.|string|
|**contact_id**  <br>*optional*|The contact ID of the user.|string|
|**country_business**  <br>*optional*|The country of the business address.|string|
|**country_home**  <br>*optional*|The country of the home address.|string|
|**country_other**  <br>*optional*|The country of another address.|string|
|**created_by**  <br>*optional*|User ID of the user who created this object.|string|
|**creation_date**  <br>*optional*|Date and time of creation.|integer(int64)|
|**default_address**  <br>*optional*|The default address.|integer|
|**department**  <br>*optional*|The department.|string|
|**display_name**  <br>*optional*|The display name.|string|
|**distribution_list**  <br>*optional*|If this contact is a distribution list, then this field is an array of objects. Each object describes a member of the list.|< [DistributionListMember](#distributionlistmember) > array|
|**email1**  <br>*optional*|The email address 1.|string|
|**email2**  <br>*optional*|The email address 2.|string|
|**email3**  <br>*optional*|The email address 3.|string|
|**employee_type**  <br>*optional*|The type of the employee.|string|
|**fax_business**  <br>*optional*|The business fax number.|string|
|**fax_home**  <br>*optional*|The home fax number.|string|
|**fax_other**  <br>*optional*|The other fax number.|string|
|**file_as**  <br>*optional*|The file name.|string|
|**first_name**  <br>*optional*|The given name.|string|
|**folder_id**  <br>*optional*|Object ID of the parent folder.|string|
|**groups**  <br>*optional*|The IDs of user's groups.|< integer > array|
|**guest_created_by**  <br>*optional*|The ID of the user who has created this guest in case this user represents a guest user. 0 represents regular users. (preliminary, available since v7.8.0)|integer|
|**id**  <br>*optional*|Object ID.|string|
|**image1**  <br>*optional*||string|
|**image1_content_type**  <br>*optional*|The content type of the image (like "image/png").|string|
|**image1_url**  <br>*optional*|The url to the image.|string|
|**image_last_modified**  <br>*optional*|The last modification of the image.|integer(int64)|
|**info**  <br>*optional*|An information.|string|
|**instant_messenger1**  <br>*optional*|The instant messenger address 1.|string|
|**instant_messenger2**  <br>*optional*|The instant messenger address 2.|string|
|**lastModifiedOfNewestAttachmentUTC**  <br>*optional*|Timestamp of the newest attachment written with UTC time zone.|integer(int64)|
|**last_modified**  <br>*optional*|Date and time of the last modification.|integer(int64)|
|**last_name**  <br>*optional*|The sur name.|string|
|**locale**  <br>*optional*|The name of user's entire locale, with, language, country and variant separated by underbars (e.g. "en", "de_DE").|string|
|**login_info**  <br>*optional*|The user's login information.|string|
|**manager_name**  <br>*optional*|The manager's name.|string|
|**marital_status**  <br>*optional*|The marital status.|string|
|**mark_as_distributionlist**  <br>*optional*||boolean|
|**modified_by**  <br>*optional*|User ID of the user who last modified this object.|string|
|**nickname**  <br>*optional*|The nickname.|string|
|**note**  <br>*optional*|A note.|string|
|**number_of_attachments**  <br>*optional*|Number of attachments.|integer|
|**number_of_children**  <br>*optional*|The number of children.|string|
|**number_of_distribution_list**  <br>*optional*|The number of objects in the distribution list.|integer|
|**number_of_employees**  <br>*optional*|The number of employees.|string|
|**number_of_images**  <br>*optional*|The number of images.|integer|
|**position**  <br>*optional*|The position.|string|
|**postal_code_business**  <br>*optional*|The postal code of the business address.|string|
|**postal_code_home**  <br>*optional*|The postal code of the home address.|string|
|**postal_code_other**  <br>*optional*|The postal code of another address.|string|
|**private_flag**  <br>*optional*|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|boolean|
|**profession**  <br>*optional*|The profession.|string|
|**room_number**  <br>*optional*|The room number.|string|
|**sales_volume**  <br>*optional*|The sales volume.|string|
|**second_name**  <br>*optional*|The middle name.|string|
|**spouse_name**  <br>*optional*|The name of the spouse.|string|
|**state_business**  <br>*optional*|The state of the business address.|string|
|**state_home**  <br>*optional*|The state of the home address.|string|
|**state_other**  <br>*optional*|The state of another address.|string|
|**street_business**  <br>*optional*|The street of the business address.|string|
|**street_home**  <br>*optional*|The street of the home address.|string|
|**street_other**  <br>*optional*|The street of another address.|string|
|**suffix**  <br>*optional*|The suffix.|string|
|**tax_id**  <br>*optional*|The tax id.|string|
|**telephone_assistant**  <br>*optional*|The assistant telephone number.|string|
|**telephone_business1**  <br>*optional*|The business telephone number 1.|string|
|**telephone_business2**  <br>*optional*|The business telephone number 2.|string|
|**telephone_callback**  <br>*optional*|The callback telephone number.|string|
|**telephone_car**  <br>*optional*|The car telephone number.|string|
|**telephone_company**  <br>*optional*|The company telephone number.|string|
|**telephone_home1**  <br>*optional*|The home telephone number 1.|string|
|**telephone_home2**  <br>*optional*|The home telephone number 2.|string|
|**telephone_ip**  <br>*optional*|The IP telephone number.|string|
|**telephone_isdn**  <br>*optional*|The ISDN telephone number.|string|
|**telephone_other**  <br>*optional*|The other telephone number.|string|
|**telephone_pager**  <br>*optional*|The pager telephone number.|string|
|**telephone_primary**  <br>*optional*|The primary telephone number.|string|
|**telephone_radio**  <br>*optional*|The radio telephone number.|string|
|**telephone_telex**  <br>*optional*|The telex telephone number.|string|
|**telephone_ttytdd**  <br>*optional*|The TTY/TDD telephone number.|string|
|**timezone**  <br>*optional*|The time zone.|string|
|**title**  <br>*optional*|The title.|string|
|**uid**  <br>*optional*|Can only be written when the object is created. Internal and external globally unique identifier of the contact. Is used to recognize contacts within vCard files. If this attribute is not written it contains an automatic generated UUID.|string|
|**url**  <br>*optional*|The url address or homepage.|string|
|**useCount**  <br>*optional*|In case of sorting purposes the column 609 is also available, which places global address book contacts at the beginning of the result. If 609 is used, the order direction (ASC, DESC) is ignored.|integer|
|**user_id**  <br>*optional*|The internal user id.|integer|
|**userfield01**  <br>*optional*|Dynamic Field 1.|string|
|**userfield02**  <br>*optional*|Dynamic Field 2.|string|
|**userfield03**  <br>*optional*|Dynamic Field 3.|string|
|**userfield04**  <br>*optional*|Dynamic Field 4.|string|
|**userfield05**  <br>*optional*|Dynamic Field 5.|string|
|**userfield06**  <br>*optional*|Dynamic Field 6.|string|
|**userfield07**  <br>*optional*|Dynamic Field 7.|string|
|**userfield08**  <br>*optional*|Dynamic Field 8.|string|
|**userfield09**  <br>*optional*|Dynamic Field 9.|string|
|**userfield10**  <br>*optional*|Dynamic Field 10.|string|
|**userfield11**  <br>*optional*|Dynamic Field 11.|string|
|**userfield12**  <br>*optional*|Dynamic Field 12.|string|
|**userfield13**  <br>*optional*|Dynamic Field 13.|string|
|**userfield14**  <br>*optional*|Dynamic Field 14.|string|
|**userfield15**  <br>*optional*|Dynamic Field 15.|string|
|**userfield16**  <br>*optional*|Dynamic Field 16.|string|
|**userfield17**  <br>*optional*|Dynamic Field 17.|string|
|**userfield18**  <br>*optional*|Dynamic Field 18.|string|
|**userfield19**  <br>*optional*|Dynamic Field 19.|string|
|**userfield20**  <br>*optional*|Dynamic Field 20.|string|
|**yomiCompany**  <br>*optional*|Kana based representation for the Company. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|string|
|**yomiFirstName**  <br>*optional*|Kana based representation for the First Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|string|
|**yomiLastName**  <br>*optional*|Kana based representation for the Last Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|string|


<a name="userresponse"></a>
# UserResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*||[UserData](#userdata)|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|


<a name="usersearchbody"></a>
# UserSearchBody

|Name|Description|Schema|
|---|---|---|
|**display_name**  <br>*optional*|Searches users where the display name matches with the given display name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|string|
|**emailAutoComplete**  <br>*optional*|If set to `true`, results are guaranteed to contain at least one email adress and the search is performed by connecting the relevant fields through an OR search habit. This field is ignored if `pattern` is specified.|boolean|
|**first_name**  <br>*optional*|Searches users where the first name matches with the given first name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|string|
|**last_name**  <br>*optional*|Searches users where the last name matches with the given last name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|string|
|**orSearch**  <br>*optional*|If set to `true`, the fields `last_name`, `first_name` and `display_name` are connected through an OR search habit. This field is ignored if `pattern` is specified.|boolean|
|**pattern**  <br>*optional*|Search pattern to find tasks. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|string|
|**startletter**  <br>*optional*|Search users with the given startletter. If this field is present, the pattern is matched against the user field which is specified by the property "contact_first_letter_field" on the server (default: last name). Otherwise, the pattern is matched against the display name.|boolean|


<a name="usersresponse"></a>
# UsersResponse

|Name|Description|Schema|
|---|---|---|
|**categories**  <br>*optional*|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|string|
|**category**  <br>*optional*|Maintained for legacy reasons: The numeric representation of the first category.|integer|
|**code**  <br>*optional*|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|string|
|**data**  <br>*optional*|Array of user. Each user is described as an array itself.|< < object > array > array|
|**error**  <br>*optional*|The translated error message. Present in case of errors.|string|
|**error_desc**  <br>*optional*|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|string|
|**error_id**  <br>*optional*|Unique error identifier to help finding this error instance in the server logs.|string|
|**error_params**  <br>*optional*|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|< string > array|
|**error_stack**  <br>*optional*|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|< string > array|
|**timestamp**  <br>*optional*|The latest timestamp of the returned data (see [Updates](#updates)).|integer(int64)|




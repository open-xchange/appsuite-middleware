---
title: API Paths
classes: no-affix
---
# Gets all mail accounts (**available since v6.12**).
```
GET /account?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Mail account data](#mail-account-data).|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all mail accounts. Each array element describes one account and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountsResponse|


## Tags

* mailaccount

# Deletes a mail account (**available since v6.12**).
```
PUT /account?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array with the ID of the mail account that shall be deleted.|true|integer array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with identifiers of deleted accounts. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountDeletionResponse|


## Consumes

* application/json

## Tags

* mailaccount

# Gets a mail account (**available since v6.12**).
```
GET /account?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Account ID of the requested account.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested account. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailAccountResponse|


## Tags

* mailaccount

# Creates a new mail account (**available since v6.12**).
```
PUT /account?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the new account to create.|true|MailAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the inserted mail account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountUpdateResponse|


## Consumes

* application/json

## Tags

* mailaccount

# Updates a mail account (**available since v6.12**).
```
PUT /account?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object identifying (by field `id`) and describing the account to update. Only modified fields are present.|true|MailAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the updated mail account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountUpdateResponse|


## Consumes

* application/json

## Tags

* mailaccount

# Validates a mail account which shall be created (**available since v6.12**).
```
PUT /account?action=validate
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|Indicates whether on successful validation the folder tree shall be returned (or `null`on failure) or
if set to `false` or missing only a boolean is returned which indicates validation result.
|false|boolean||
|BodyParameter|body|A JSON object describing the account to validate.|true|MailAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object that contains a value representing the validation result (may be a boolean or a folder tree object).
If the validation fails then the error fields are filled and an additional `warnings` field might be added.
|MailAccountValidationResponse|


## Consumes

* application/json

## Tags

* mailaccount

# Gets all attachments for an object.
```
GET /attachment?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all attachments. Each array element describes one attachment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|AttachmentsResponse|


## Tags

* attachments

# Creates an attachment.
```
POST /attachment?action=attach
```

## Description

## Note
It is possible to create multiple attachments at once. Therefor add additional form fields and replace "[index]" in `json_[index]`
and `file_[index]` with the appropriate index, like `json_1`, `file_1`. The index always starts with 0 (mandatory attachment object).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|FormDataParameter|json_0|A JSON string representing an attachment object as described in [AttachmentData](#/definitions/AttachmentData) model with at least the fields `folder`, `attached` and `module`.|true|string||
|FormDataParameter|file_0|The attachment file as per `<input type="file" />`.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON array of object IDs of the newly created attachments or errors if some occurred.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* attachments

# Deletes attachments.
```
PUT /attachment?action=detach
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|BodyParameter|body|A JSON array with the identifiers of the attachments that shall be deleted.|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


## Consumes

* application/json

## Tags

* attachments

# Gets an attachment's document/filedata.
```
GET /attachment?action=document
```

## Description

It is possible to add a filename to the request's URI like `/attachment/{filename}?action=document`.
The filename may be added to the customary attachment path to suggest a filename to a Save-As dialog.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|id|Object ID of the requested attachment.|true|string||
|QueryParameter|content_type|If present the response declares the given `content_type` in the Content-Type header and not the attachments file MIME type.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the document. The response type for the HTTP request is set accordingly to the defined MIME type for this attachment or the content_type given.|string (binary)|


## Produces

* application/octet-stream

## Tags

* attachments

# Gets an attachment.
```
GET /attachment?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested infoitem.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested attachment. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|AttachmentResponse|


## Tags

* attachments

# Gets a list of attachments.
```
PUT /attachment?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|true|string||
|BodyParameter|body|A JSON array with the identifiers of the requested attachments.|true|integer array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested infoitems. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


## Consumes

* application/json

## Tags

* attachments

# Gets the new and deleted attachments.
```
GET /attachment?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|true|string||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|timestamp|Timestamp of the last update of the requested infoitems.|false|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|false|enum (deleted)||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new and deleted attachments. New attachments are represented by arrays. The
elements of each array contain the information specified by the corresponding identifiers in the `columns`
parameter. Deleted attachments would be identified by their object IDs as integer, without being part of
a nested array. In case of errors the responsible fields in the response are filled (see
[Error handling](#error-handling)).
|AttachmentUpdatesResponse|


## Tags

* attachments

# Gets the auto configuration for a mail account (**available since v6.22**).
```
POST /autoconfig?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|FormDataParameter|email|The email address for which a mail configuration will be discovered.|true|string||
|FormDataParameter|password|The corresponding password for the mail account.|true|string||
|FormDataParameter|force_secure|Enforces a secure connection for configured mail account, default is `true` (available since v7.8.2).|false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the best available settings for an appropriate mail server for the given email
address. The data may be incomplete or empty. In case of errors the responsible fields in the response
are filled (see [Error handling](#error-handling)).
|AutoConfigResponse|


## Tags

* autoconfig

# Gets all appointments.
```
GET /calendar?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which start on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which end before this date are returned.|true|integer (int64)||
|QueryParameter|folder|Object ID of the folder, whose contents are queried. If not specified, defaults to all calendar folders.|false|string||
|QueryParameter|recurrence_master|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|false|boolean||
|QueryParameter|showPrivate|Only works in shared folders: When enabled, shows private appointments of the folder owner. Such appointments are anonymized by stripping away all information except start date, end date and recurrence information (since 6.18)|false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with appointment data. Each array element describes one appointment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. Appointment sequencies are broken up into individual appointments
and each occurrence of a sequence in the requested range is returned separately. The appointments are
sorted in ascending order by the field `start_date`. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


## Tags

* calendar

# Confirms an appointment.
```
PUT /calendar?action=confirm
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the appointment that shall be confirmed.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the appointments.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the appointment.|true|integer (int64)||
|QueryParameter|occurrence|The numeric identifier of the occurrence to which the confirmation applies (in case "id" denotes a series appointment). Available since v7.6.0.|false|integer||
|BodyParameter|body|A JSON object with the fields `confirmation`, `confirmmessage` and optionally `id`.|true|AppointmentConfirmationBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Nothing, except the standard response object with empty data, the timestamp of the confirmed and thereby
updated appointment, and maybe errors. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CommonResponse|


## Consumes

* application/json

## Tags

* calendar

# Deletes appointments (**available since v6.22**).
```
PUT /calendar?action=delete
```

## Description

Before version 6.22 the request body contained a JSON object with the fields `id`, `folder` and
optionally `pos` and could only delete one appointment.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the deleted appointments.|true|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id, folder and optionally the recurrence position (if present in an appointment to fully identify it) of the appointments.|true|AppointmentDeletionsElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array of objects identifying the appointments which were modified after the specified timestamp and were therefore not deleted. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|AppointmentDeletionsResponse|


## Consumes

* application/json

## Tags

* calendar

# Gets appointments between a specified time range.
```
GET /calendar?action=freebusy
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Internal user id. Must be obtained from the contact module.|true|integer||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned.|true|integer (int64)||
|QueryParameter|type|Constant for user or resource (1 for users, 3 for resources).|true|enum (, )||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array of objects identifying the appointments which lie between start and end as described. In
case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentFreeBusyResponse|


## Tags

* calendar

# Gets an appointment.
```
GET /calendar?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested appointment.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the appointments.|true|string||
|QueryParameter|recurrence_position|Recurrence position of requested appointment.|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested appointment. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentResponse|


## Tags

* calendar

# Gets all change exceptions (**available since v7.2.0**).
```
GET /calendar?action=getChangeExceptions
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the appointment series.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the appointments.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with appointment data. Each array element describes one appointment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|AppointmentsResponse|


## Tags

* calendar

# Requests whether there are appointments on days in a specified time range.
```
GET /calendar?action=has
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which start on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which end before this date are returned.|true|integer (int64)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with the length of the number of days between `start` and `end`. Meaning,
each element corresponds with one day in the range that was queried, explaining whether there is an
appointment on this day (true) or not (false). In case of errors the responsible fields in the response
are filled (see [Error handling](#error-handling)).
|AppointmentInfoResponse|


## Tags

* calendar

# Gets a list of appointments.
```
PUT /calendar?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|QueryParameter|recurrence_master|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|false|boolean||
|BodyParameter|body|A JSON array of JSON objects with the id, folder and optionally either recurrence_position or recurrence_date_position of the requested appointments.|true|AppointmentListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested appointments. Each array element describes one appointment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response
are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


## Consumes

* application/json

## Tags

* calendar

# Creates an appointment.
```
PUT /calendar?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the appointment's data.|true|AppointmentData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the id of the newly created appointment if it was created successfully.
If the appointment could not be created due to conflicts, the response body is an object with the
field `conflicts`, which is an array of appointment objects which caused the conflict. Each appointment
object which represents a resource conflict contains an additional field `hard_conflict` with the
Boolean value true. If the user does not have read access to a conflicting appointment, only the
fields `id`, `start_date`, `end_date`, `shown_as` and `participants` are present and the field `participants`
contains only the participants which caused the conflict. In case of errors the responsible fields
in the response are filled (see [Error handling](#error-handling)).
|AppointmentCreationResponse|


## Consumes

* application/json

## Tags

* calendar

# Gets new appointments.
```
GET /calendar?action=newappointments
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned.|true|integer (int64)||
|QueryParameter|limit|Limits the number of returned objects to the given value.|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified and holds a column number, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with appointment data. Appointments are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


## Tags

* calendar

# Resolves the UID to an OX object ID.
```
GET /calendar?action=resolveuid
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|uid|The UID that shall be resolved.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the related object ID in the field `id`. If no object exists with the
specified UID or in case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|AppointmentUpdateResponse|


## Tags

* calendar

# Searches for appointments.
```
PUT /calendar?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|BodyParameter|body|A JSON object containing search parameters.|true|AppointmentSearchBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching appointments. Appointments are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


## Consumes

* application/json

## Tags

* calendar

# Updates an appointment.
```
PUT /calendar?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested appointment.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the appointments.|true|string||
|QueryParameter|timestamp|Timestamp of the updated appointment. If the appointment was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the appointment's data. The field `recurrence_id` is always present
if it is present in the original appointment. The field `recurrence_position` is present if
it is present in the original appointment and only this single appointment should be modified.
The field `id` is not present because it is already included as a parameter. Other fields are
present only if modified.
|true|AppointmentData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the id of the updated appointment. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentUpdateResponse|


## Consumes

* application/json

## Tags

* calendar

# Gets updated appointments.
```
GET /calendar?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested appointments.|true|integer (int64)||
|QueryParameter|folder|Object ID of the folder, whose contents are queried. That parameter may be absent in case ignore is set to "deleted", which means all accessible calendar folders are considered. If ignore is not set to "deleted", that parameter is mandatory.|false|string||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned. This parameter is optional in case a certain folder is queried, but mandatory if all accessible calendar folders are supposed to be considered (folder not specified).|false|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned. This parameter is optional in case a certain folder is queried, but mandatory if all accessible calendar folders are supposed to be considered (folder not specified).|false|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|false|enum (deleted)||
|QueryParameter|recurrence_master|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|false|boolean||
|QueryParameter|showPrivate|Only works in shared folders: When enabled, shows private appointments of the folder owner. Such appointments are anonymized by stripping away all information except start date, end date and recurrence information (since 6.18)|false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new, modified and deleted appointments. New and modified appointments are represented by arrays.
The elements of each array contain the information specified by the corresponding identifiers in the
`columns` parameter. Deleted appointments (should the ignore parameter be ever implemented) would be identified
by their object IDs as integers, without being part of a nested array. Appointment sequencies are broken up
into individual appointments and each modified occurrence of a sequence in the requested range is returned
separately. The appointments are sorted in ascending order by the field start_date. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentUpdatesResponse|


## Tags

* calendar

# Gets all capabilities (**available since v7.4.2**).
```
GET /capabilities?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all capabilities. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CapabilitiesResponse|


## Tags

* capabilities

# Gets a capability (**available since v7.4.2**).
```
GET /capabilities?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the capability|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the capability or an empty result, if capability not available. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CapabilityResponse|


## Tags

* capabilities

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|PathParameter|path|The path to the node.|true|enum (gui, fastgui, context_id, cookielifetime, identifier, contact_id, language, timezone, availableTimeZones, calendarnotification, tasknotification, reloadTimes, serverVersion, currentTime, maxUploadIdleTimeout, search, folder, folder/tasks, folder/calendar, folder/contacts, folder/infostore, folder/eas, mail, mail/addresses, mail/defaultaddress, mail/sendaddress, mail/folder, mail/folder/inbox, mail/folder/drafts, mail/folder/trash, mail/folder/spam, mail/folder/sent, mail/htmlinline, mail/colorquote, mail/emoticons, mail/harddelete, mail/inlineforward, mail/vcard, mail/notifyonreadack, mail/msgpreview, mail/ignorereplytext, mail/nocopytosent, mail/spambutton, participants, participants/autoSearch, participants/maximumNumberParticipants, participants/showWithoutEmail, participants/showDialog, availableModules, minimumSearchCharacters, modules, modules/portal, modules/portal/gui, modules/portal/module, modules/mail, modules/mail/addresses, modules/mail/appendmailtext, modules/mail/allowhtmlimages, modules/mailcolorquoted, modules/mail/contactCollectFolder, modules/mail/contactCollectEnabled, modules/mail/contactCollectOnMailAccess, modules/mail/contactCollectOnMailTransport, modules/mail/defaultaddress, modules/mail/deletemail, modules/mail/emoticons, modules/mail/defaultFolder, modules/mail/defaultFolder/drafts, modules/mail/defaultFolder/inbox, modules/mail/defaultFolder/sent, modules/mail/defaultFolder/spam, modules/mail/defaultFolder/trash, modules/mail/forwardmessage, modules/mail/gui, modules/mail/inlineattachments, modules/mail/linewrap, modules/mail/module, modules/mail/phishingheaders, modules/mail/replyallcc, modules/mail/sendaddress, modules/mail/spambutton, modules/mail/vcard, modules/calendar, modules/calendar/calendar_conflict, modules/calendar/calendar_freebusy, modules/calendar/calendar_teamview, modules/calendar/gui, modules/calendar/module, modules/calendar/notifyNewModifiedDeleted, modules/calendar/notifyAcceptedDeclinedAsCreator, modules/calendar/notifyAcceptedDeclinedAsParticipant, modules/calendar/defaultStatusPrivate, modules/calendar/defaultStatusPublic, modules/contacts, modules/contacts/gui, modules/contacts/mailAddressAutoSearch, modules/contacts/module, modules/contacts/singleFolderSearch, modules/contacts/characterSearch, modules/contacts/allFoldersForAutoComplete, modules/tasks, modules/tasks/gui, modules/tasks/module, modules/tasks/delegate_tasks, modules/tasks/notifyNewModifiedDeleted, modules/tasks/notifyAcceptedDeclinedAsCreator, modules/tasks/notifyAcceptedDeclinedAsParticipant, modules/infostore, modules/infostore/gui, modules/infostore/folder, modules/infostore/folder/trash, modules/infostore/folder/pictures, modules/infostore/folder/documents, modules/infostore/folder/music, modules/infostore/folder/videos, modules/infostore/folder/templates, modules/infostore/module, modules/interfaces, modules/interfaces/ical, modules/interfaces/vcard, modules/interfaces/syncml, modules/folder, modules/folder/gui, modules/folder/public_folders, modules/folder/read_create_shared_folders, modules/folder/tree, modules/com.openexchange.extras, modules/com.openexchange.extras/module, modules/com.openexchange.user.passwordchange, modules/com.openexchange.user.passwordchange/module, modules/com.openexchange.user.personaldata, modules/com.openexchange.user.personaldata/module, modules/com.openexchange.group, modules/com.openexchange.group/enabled, modules/com.openexchange.resource, modules/com.openexchange.resource/enabled, modules/com.openexchange.publish, modules/com.openexchange.publish/enabled, modules/com.openexchange.subscribe, modules/com.openexchange.subscribe/enabled, modules/olox20, modules/olox20/active, modules/olox20/module, modules/com.openexchange.oxupdater, modules/com.openexchange.oxupdater/module, modules/com.openexchange.oxupdater/active, modules/com.openexchange.passwordchange, modules/com.openexchange.passwordchange/showStrength, modules/com.openexchange.passwordchange/minLength, modules/com.openexchange.passwordchange/maxLength, modules/com.openexchange.passwordchange/regexp, modules/com.openexchange.passwordchange/special)||
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Value of the node specified by path. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ConfigResponse|


## Tags

* config

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|PathParameter|path|The path to the node.|true|enum (gui, fastgui, context_id, cookielifetime, identifier, contact_id, language, timezone, availableTimeZones, calendarnotification, tasknotification, reloadTimes, serverVersion, currentTime, maxUploadIdleTimeout, search, folder, folder/tasks, folder/calendar, folder/contacts, folder/infostore, folder/eas, mail, mail/addresses, mail/defaultaddress, mail/sendaddress, mail/folder, mail/folder/inbox, mail/folder/drafts, mail/folder/trash, mail/folder/spam, mail/folder/sent, mail/htmlinline, mail/colorquote, mail/emoticons, mail/harddelete, mail/inlineforward, mail/vcard, mail/notifyonreadack, mail/msgpreview, mail/ignorereplytext, mail/nocopytosent, mail/spambutton, participants, participants/autoSearch, participants/maximumNumberParticipants, participants/showWithoutEmail, participants/showDialog, availableModules, minimumSearchCharacters, modules, modules/portal, modules/portal/gui, modules/portal/module, modules/mail, modules/mail/addresses, modules/mail/appendmailtext, modules/mail/allowhtmlimages, modules/mailcolorquoted, modules/mail/contactCollectFolder, modules/mail/contactCollectEnabled, modules/mail/contactCollectOnMailAccess, modules/mail/contactCollectOnMailTransport, modules/mail/defaultaddress, modules/mail/deletemail, modules/mail/emoticons, modules/mail/defaultFolder, modules/mail/defaultFolder/drafts, modules/mail/defaultFolder/inbox, modules/mail/defaultFolder/sent, modules/mail/defaultFolder/spam, modules/mail/defaultFolder/trash, modules/mail/forwardmessage, modules/mail/gui, modules/mail/inlineattachments, modules/mail/linewrap, modules/mail/module, modules/mail/phishingheaders, modules/mail/replyallcc, modules/mail/sendaddress, modules/mail/spambutton, modules/mail/vcard, modules/calendar, modules/calendar/calendar_conflict, modules/calendar/calendar_freebusy, modules/calendar/calendar_teamview, modules/calendar/gui, modules/calendar/module, modules/calendar/notifyNewModifiedDeleted, modules/calendar/notifyAcceptedDeclinedAsCreator, modules/calendar/notifyAcceptedDeclinedAsParticipant, modules/calendar/defaultStatusPrivate, modules/calendar/defaultStatusPublic, modules/contacts, modules/contacts/gui, modules/contacts/mailAddressAutoSearch, modules/contacts/module, modules/contacts/singleFolderSearch, modules/contacts/characterSearch, modules/contacts/allFoldersForAutoComplete, modules/tasks, modules/tasks/gui, modules/tasks/module, modules/tasks/delegate_tasks, modules/tasks/notifyNewModifiedDeleted, modules/tasks/notifyAcceptedDeclinedAsCreator, modules/tasks/notifyAcceptedDeclinedAsParticipant, modules/infostore, modules/infostore/gui, modules/infostore/folder, modules/infostore/folder/trash, modules/infostore/folder/pictures, modules/infostore/folder/documents, modules/infostore/folder/music, modules/infostore/folder/videos, modules/infostore/folder/templates, modules/infostore/module, modules/interfaces, modules/interfaces/ical, modules/interfaces/vcard, modules/interfaces/syncml, modules/folder, modules/folder/gui, modules/folder/public_folders, modules/folder/read_create_shared_folders, modules/folder/tree, modules/com.openexchange.extras, modules/com.openexchange.extras/module, modules/com.openexchange.user.passwordchange, modules/com.openexchange.user.passwordchange/module, modules/com.openexchange.user.personaldata, modules/com.openexchange.user.personaldata/module, modules/com.openexchange.group, modules/com.openexchange.group/enabled, modules/com.openexchange.resource, modules/com.openexchange.resource/enabled, modules/com.openexchange.publish, modules/com.openexchange.publish/enabled, modules/com.openexchange.subscribe, modules/com.openexchange.subscribe/enabled, modules/olox20, modules/olox20/active, modules/olox20/module, modules/com.openexchange.oxupdater, modules/com.openexchange.oxupdater/module, modules/com.openexchange.oxupdater/active, modules/com.openexchange.passwordchange, modules/com.openexchange.passwordchange/showStrength, modules/com.openexchange.passwordchange/minLength, modules/com.openexchange.passwordchange/maxLength, modules/com.openexchange.passwordchange/regexp, modules/com.openexchange.passwordchange/special)||
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the value of the config node.|true|ConfigBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* config

# Gets a property of the configuration (**available since v7.6.2**).
```
GET /config?action=get_property
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|name|The name of the property to return.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON response providing the property's name and its value. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ConfigPropertyResponse|


## Tags

* config

# Sets a property of the configuration (**available since v7.6.2**).
```
PUT /config?action=set_property
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|name|The name of the property to return.|true|string||
|BodyParameter|body|A JSON object providing the value to set (Example: {"value":"test123"}).|true|ConfigPropertyBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON response providing the property's name and its value. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ConfigPropertyResponse|


## Consumes

* application/json

## Tags

* config

# Search for contacts by filter (**available since v6.20**).
```
PUT /contacts?action=advancedSearch
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||
|QueryParameter|admin|Specifies whether to include the contact representing the admin in the result or not. Defaults to `true`. (preliminary, since 7.4.2)|false|boolean||
|BodyParameter|body|A JSON object describing the search term as introducted in [Advanced search](#advanced-search). Example:
`{"filter":["and",["=", {"field":"last_name"},"Mustermann"],["=",{"field":"first_name"},"Max"]]}`
which represents 'last_name = "Mustermann" AND first_name = "Max"'. Valid fields are the ones specified in [Contact data](#/definitions/ContactData) model.
|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Consumes

* application/json

## Tags

* contacts

# Gets all contacts.
```
GET /contacts?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all contacts. Each array element describes one contact and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Tags

* contacts

# Search for contacts by anniversary (**available since v6.22.1, preliminary**).
```
GET /contacts?action=anniversaries
```

## Description

Finds contacts whose anniversary falls into a specified time range.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|start|The lower (inclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|end|The upper (exclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|folder|Object ID of the parent folder that is searched. If not set, all visible folders are used.|false|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Tags

* contacts

# Auto-complete conntacts (**available since v7.6.1, preliminary**).
```
GET /contacts?action=autocomplete
```

## Description

Finds contacts based on a prefix, usually used to auto-complete e-mail recipients while the user is typing.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|query|The query to search for.|true|string||
|QueryParameter|email|Whether to only include contacts with at least one e-mail address. Defaults to `true`.|false|boolean|true|
|QueryParameter|folder|Object ID of the parent folder that is searched. If not set, all visible folders are used.|false|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified. Since 7.8.1: If this parameter is missing, response is sorted by a user-specific use count of contacts, ID of contacts' parent folder and display name.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||
|QueryParameter|left_hand_limit|A positive integer number to specify the "left-hand" limit of the range to return.|false|integer||
|QueryParameter|right_hand_limit|A positive integer number to specify the "right-hand" limit of the range to return.|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the contact data. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Tags

* contacts

# Search for contacts by birthday (**available since v6.22.1, preliminary**).
```
GET /contacts?action=birthdays
```

## Description

Finds contacts whose birthday falls into a specified time range.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|start|The lower (inclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|end|The upper (exclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|folder|Object ID of the parent folder that is searched. If not set, all visible folders are used.|false|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Tags

* contacts

# Deletes contacts (**available since v6.22**).
```
PUT /contacts?action=delete
```

## Description

Before version 6.22 the request body contained a JSON object with the fields `id` and `folder` and could
only delete one contact.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the deleted contacts.|true|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the contacts.|true|ContactListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array with object IDs of contacts which were modified after the specified timestamp and were therefore not deleted.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactDeletionsResponse|


## Consumes

* application/json

## Tags

* contacts

# Gets a contact.
```
GET /contacts?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested contact.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested contact. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactResponse|


## Tags

* contacts

# Gets a contact by user ID (**available since SP4**).
```
GET /contacts?action=getuser
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|User ID (not Object ID) of the requested user.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested contact. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactResponse|


## Tags

* contacts

# Gets a list of contacts.
```
PUT /contacts?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the contacts.|true|ContactListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested contacts. Each array element describes one contact and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Consumes

* application/json

## Tags

* contacts

# Gets a list of users (**available since SP4**).
```
PUT /contacts?action=listuser
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|BodyParameter|body|A JSON array with user IDs.|true|integer array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with contact data. Each array element describes one contact and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Consumes

* application/json

## Tags

* contacts

# Creates a contact.
```
POST /contacts?action=new
```

## Description

Creates a new contact with contact images. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the contact's data while the image file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|FormDataParameter|json|Represents the normal request body as JSON string containing the contact data as described in the [ContactData](#/definitions/ContactData) model.|true|string||
|FormDataParameter|file|The image file.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the contact or errors if some occurred.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* contacts

# Creates a contact.
```
PUT /contacts?action=new
```

## Description

Creates a new contact. This request cannot add contact images. Therefor it
is necessary to use the `POST` method.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the contact's data. The field id is not included.|true|ContactData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created contact. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactUpdateResponse|


## Consumes

* application/json

## Tags

* contacts

# Search for contacts.
```
PUT /contacts?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||
|BodyParameter|body|A JSON object containing search parameters.|true|ContactSearchBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


## Consumes

* application/json

## Tags

* contacts

# Updates a contact.
```
POST /contacts?action=update
```

## Description

Updates a contact's data and images. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the contact's data while the image file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|id|Object ID of the contact that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the updated contact. If the contact was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|FormDataParameter|json|Represents the normal request body as JSON string containing the contact data as described in [ContactData](#/definitions/ContactData) model. Only modified fields must be specified but at least "{}".|true|string|{}|
|FormDataParameter|file|The image file.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the contact or errors if some occurred.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* contacts

# Updates a contact.
```
PUT /contacts?action=update
```

## Description

Updates a contact's data. This request cannot change or add contact images. Therefore it
is necessary to use the `POST` method.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|id|Object ID of the contact that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the updated contact. If the contact was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the contact's data. Only modified fields must be specified. To remove some contact image send the image attribute set to null.|true|ContactData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with a timestamp. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactUpdateResponse|


## Consumes

* application/json

## Tags

* contacts

# Gets updated contacts.
```
GET /contacts?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested contacts.|true|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Omit this parameter or set it to "deleted" to not have deleted tasks identifier in the response. Set this parameter to `false` and the response contains deleted tasks identifier.|false|enum (deleted)||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new, modified and deleted contacts. New and modified contacts are represented by arrays.
The elements of each array contain the information specified by the corresponding identifiers in the
`columns` parameter. Deleted contacts (should the ignore parameter be ever implemented) would be identified
by their object IDs as integers, without being part of a nested array. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactUpdatesResponse|


## Tags

* contacts

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object the data source object and the data handler object.|true|ConversionBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The conversion result. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ConversionResponse|


## Consumes

* application/json

## Tags

* conversion

# Exports contact data to a CSV file.
```
GET /export?action=CSV
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder whose content shall be exported. This must be a contact folder.|true|string||
|QueryParameter|columns|A comma-separated list of columns to export, like "501,502". A column is specified by a numeric column identifier, see [Detailed contact data](#detailed-contact-data).|false|string||
|QueryParameter|export_dlists|Toggles whether distribution lists shall be exported too (default is `false`). (since 7.4.1)|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An input stream containing the content of the CSV file with the MIME type `text/csv`.|string|


## Tags

* export

# Exports appointment and task data to an iCalendar file.
```
GET /export?action=ICAL
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder whose content shall be exported. This must be a calendar folder.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An input stream containing the content of the iCal file with the MIME type `text/calendar`.|string|


## Tags

* export

# Exports contact data to a vCard file.
```
GET /export?action=VCARD
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder whose content shall be exported. This must be a contact folder.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An input stream containing the content of the vCard file with the MIME type `text/x-vcard`.|string|


## Tags

* export

# Requests a formerly uploaded file.
```
GET /file?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the uploaded file.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested file is directly written into output stream.|string (binary)|
|404|Not found.|No Content|


## Produces

* application/octet-stream

## Tags

* file

# Updates a file's last access timestamp and keeps it alive.
```
GET /file?action=keepalive
```

## Description

By updating the last access timestamp the file is prevented from being deleted from both session and disk
storage.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the uploaded file whose timestamp should be updated.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Tags

* file

# Uploads a file.
```
POST /file?action=new
```

## Description

It can be uploaded multiple files at once. Each file must be specified in an own form field
(the form field name is arbitrary).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|module|The module for which the file is uploaded to determine proper upload quota constraints (e.g. "mail", "infostore", etc.).|true|string||
|QueryParameter|type|The file type filter to define which file types are allowed during upload. Currently supported filters are: file (for all), text (for `text/*`), media (for image, audio or video), image (for `image/*`), audio (for `audio/*`), video (for `video/*`) and application (for `application/*`).|true|enum (file, text, media, image, audio, video, application)||
|FormDataParameter|file|The file to upload.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON array with the IDs of
the uploaded files or errors if some occurred. The files are accessible through the returned IDs
for future use.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* file

# Gets all file storage accounts.
```
GET /fileaccount?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|filestorageService|The identifier of a file storage service to list only those accounts that belong to that file storage service.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with JSON objects each describing one file storage account. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FileAccountsResponse|


## Tags

* filestorage

# Deletes a file storage account.
```
GET /fileaccount?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|filestorageService|The identifier of the file storage service the account belongs to.|true|string||
|QueryParameter|id|The ID of the account to delete.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 on success. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountUpdateResponse|


## Tags

* filestorage

# Gets a file storage account.
```
GET /fileaccount?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|filestorageService|The identifier of the file storage service the account belongs to.|true|string||
|QueryParameter|id|The ID of the requested account.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the file storage account. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountResponse|


## Tags

* filestorage

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the account to create, with at least the field `filestorageService` set.|true|FileAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created account. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountCreationResponse|


## Consumes

* application/json

## Tags

* filestorage

# Updates a file storage account.
```
PUT /fileaccount?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the updated data of the account. The fields `id` and `filestorageService` must be set.|true|FileAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 on success. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountCreationResponse|


## Consumes

* application/json

## Tags

* filestorage

# Gets all file storage services.
```
GET /fileservice?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with JSON objects each describing one file storage service. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FileServicesResponse|


## Tags

* filestorage

# Gets a file storage service.
```
GET /fileservice?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the file storage service to load.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the file storage service. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileServiceResponse|


## Tags

* filestorage

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|module|The name of the module within that the search shall be performed. Possible modules are: mail, contacts,
calendar, tasks, drive. Because a user may have limited access to modules the useable modules might only
be a subset of the available ones. Retrieve a list of allowed modules by querying the user configuration,
see module "config" (path `search`) or module "JSlob" (e.g. `id=io.ox/core`).
|true|string||
|QueryParameter|limit|The maximum number of values returned per facet.|false|integer||
|BodyParameter|body|A JSON object containing the user's input (specified in field `prefix`), already selected `facets`, and possible `options`.|true|FindAutoCompleteBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the facets that were found. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FindAutoCompleteResponse|


## Consumes

* application/json

## Tags

* find

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|module|The name of the module within that the search shall be performed. Possible modules are: mail, contacts,
calendar, tasks, drive. Because a user may have limited access to modules the useable modules might only
be a subset of the available ones. Retrieve a list of allowed modules by querying the user configuration,
see module "config" (path `search`) or module "JSlob" (e.g. `id=io.ox/core`).
|true|string||
|QueryParameter|columns|A comma-separated list of module-specific columns that shall be contained in the response items. See [Column identifiers](#column-identifiers) for the numeric IDs of fields for specific modules.|false|string||
|BodyParameter|body|A JSON object containing the selected `facets` and possible `options`. For pagination the keys `start` and `size` can be set.|true|FindQueryBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the search result. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FindQueryResponse|


## Consumes

* application/json

## Tags

* find

# Gets all visible folders of a certain module (**available since v6.18.2**).
```
GET /folders?action=allVisible
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|content_type|The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "mail", "infostore").|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing three fields: "private", "public, and "shared". Each field is a
JSON array with data for all folders. Each folder is itself described by an array. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersVisibilityResponse|


## Tags

* folders

# Clears the content of a list of folders.
```
PUT /folders?action=clear
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||
|BodyParameter|body|A JSON array with object IDs of the folders.|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array containing the IDs of folders that could not be cleared due to a concurrent modification.
Meaning you receive an empty JSON array if everything worked well. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|FoldersCleanUpResponse|


## Consumes

* application/json

## Tags

* folders

# Deletes a list of folders.
```
PUT /folders?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|timestamp|The optional timestamp of the last update of the deleted folders.|false|integer (int64)||
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||
|QueryParameter|hardDelete|If set to `true`, the folders are deleted permanently. Otherwise, and if the underlying storage
supports a trash folder and the folders are not yet located below the trash folder, they are moved
to the trash folder.
|false|boolean|false|
|BodyParameter|body|A JSON array with object IDs of the folders.|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with object IDs of folders that were NOT deleted. There may be a lot of different causes
for a not deleted folder: A folder has been modified in the mean time, the user does not have the
permission to delete it or those permissions have just been removed, the folder does not exist, etc.
You receive an empty JSON array if everything worked well. In case of errors the responsible fields
in the response are filled (see [Error handling](#error-handling)).
|FoldersCleanUpResponse|


## Consumes

* application/json

## Tags

* folders

# Gets a folder.
```
GET /folders?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested folder.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the requested folder. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|FolderResponse|


## Tags

* folders

# Gets the subfolders of a specified parent folder.
```
GET /folders?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|parent|Object ID of a folder, which is the parent folder of the requested folders.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|all|Set to 1 to list even not subscribed folders.|false|integer||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||
|QueryParameter|errorOnDuplicateName||false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all folders, which have the folder with the requested object
ID as parent. Each array element describes one folder and is itself an array. The elements of each array
contain the information specified by the corresponding identifiers in the `columns` parameter. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersResponse|


## Tags

* folders

# Creates a new folder.
```
PUT /folders?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|folder_id|The parent folder object ID of the newly created folder.|true|string||
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||
|BodyParameter|body|JSON object with "folder" object containing the modified fields and optional "notification"
object to let added permission entities be notified about newly shared folders for all modules
except mail. (Example: {"folder":{"title":"test123"}} or {"folder":{"permissions":[{"bits":403710016,"entity":84,"group":false}]},"notification":{"transport":"mail","message":"The message"}})
|true|FolderBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the folder. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FolderUpdateResponse|


## Consumes

* application/json

## Tags

* folders

# Notifies users or groups about a shared folder (**available since v7.8.0, priliminary**).
```
PUT /folders?action=notify
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|id|Object ID of the shared folder to notify about.|true|string||
|BodyParameter|body|JSON object providing the JSON array `entities`, which holds the entity ID(s) of the users or groups that
should be notified. To send a custom message to the recipients, an additional JSON object `notification` may
be included, inside of which an optional message can be passed (otherwise, some default message is used).
(Example: {"entities":["2332"]} or {"entities":["2332"],"notification":{"transport":"mail","message":"The message"}})
|true|FolderSharingNotificationBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An empty JSON object. Any transport warnings that occurred during sending the
notifications are available in the warnings array of the response. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FolderSharingNotificationResponse|


## Consumes

* application/json

## Tags

* folders

# Gets the parent folders above the specified folder.
```
GET /folders?action=path
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of a folder.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with data for all parent nodes of a folder. Each array element describes one folder and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FoldersResponse|


## Tags

* folders

# Gets the folders at the root level of the folder structure.
```
GET /folders?action=root
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all folders at the root level of the folder structure. Each array element
describes one folder and is itself an array. The elements of each array contain the information
specified by the corresponding identifiers in the `columns` parameter. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|FoldersResponse|


## Tags

* folders

# Gets shared folders of a certain module (**available since v7.8.0, preliminary**).
```
GET /folders?action=shares
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|content_type|The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "mail", "infostore").|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|all|Set to 1 to list even not subscribed folders.|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all folders that are considered as shared by the user.
Each array element describes one folder and is itself an array. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersResponse|


## Tags

* folders

# Updates a folder.
```
PUT /folders?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the updated folder.|true|string||
|QueryParameter|timestamp|Timestamp of the updated folder. If the folder was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||
|QueryParameter|cascadePermissions|`true` to cascade permissions to all sub-folders. The user must have administrative permissions to all
sub-folders subject to change. If one permission change fails, the entire operation fails. (since 7.8.0)
|false|boolean|false|
|BodyParameter|body|JSON object with "folder" object containing the modified fields and optional "notification"
object to let added permission entities be notified about newly shared folders for all modules
except mail. (Example: {"folder":{"title":"test123"}} or {"folder":{"permissions":[{"bits":403710016,"entity":84,"group":false}]},"notification":{"transport":"mail","message":"The message"}})
|true|FolderBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object id of the folder. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FolderUpdateResponse|


## Consumes

* application/json

## Tags

* folders

# Gets the new, modified and deleted folders of a given folder.
```
GET /folders?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|parent|Object ID of a folder, which is the parent folder of the requested folders.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested folders.|true|integer (int64)||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|ignore|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|false|enum (deleted)||
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with data for new, modified and deleted folders. New and modified folders are represented
by arrays. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. Deleted folders (should the ignore parameter be ever implemented)
would be identified by their object IDs as plain strings, without being part of a nested array. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FolderUpdatesResponse|


## Tags

* folders

# Gets free/busy information (**available since v6.22.1**).
```
GET /freebusy?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|participant|The participant to get the free/busy data for. My be either an internal user-, group- or resource-ID,
or an email address for external participants.
|true|string||
|QueryParameter|from|The lower (inclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|until|The upper (exclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|merged|Indicates whether to pre-process free/busy data on the server or not. This includes sorting as well as merging overlapping free/busy intervals.|false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of free/busy intervals. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FreeBusyResponse|


## Tags

* freebusy

# Gets a list of free/busy information (**available since v6.22.1**).
```
PUT /freebusy?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|from|The lower (inclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|until|The upper (exclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|merged|Indicates whether to pre-process free/busy data on the server or not. This includes sorting as well as merging overlapping free/busy intervals.|false|boolean||
|BodyParameter|body|A JSON array with identifiers of participants to get free/busy data for. The identifier my refer
to an internal user-, group- or resource-ID, or to an email address for external participants.
|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the free/busy data for all requested participants. For each participant it is added
an object (with the participant's ID as key) that contains a field `data` that is an array with objects representing
free/busy information as described in [FreeBusyData](#/definitions/FreeBusyData) model. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FreeBusysResponse|


## Consumes

* application/json

## Tags

* freebusy

# Deletes a group (**introduced 2008-06-12**).
```
PUT /group?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the group to delete.|true|integer (int64)||
|BodyParameter|body|A JSON object with the field `id` containing the unique identifier of the group.|true|GroupListElement||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array if the group was deleted successfully. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupsResponse|


## Consumes

* application/json

## Tags

* groups

# Gets a group.
```
GET /group?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the group.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the group data. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupResponse|


## Tags

* groups

# Gets a list of groups.
```
PUT /group?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id of the requested groups.|true|GroupListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of group objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupsResponse|


## Consumes

* application/json

## Tags

* groups

# Creates a group (**introduced 2008-06-12**).
```
PUT /group?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the group data. The field id is not present.|true|GroupData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the ID of the newly created group. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupUpdateResponse|


## Consumes

* application/json

## Tags

* groups

# Searches for groups.
```
PUT /group?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object with the search parameters.|true|GroupSearchBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of group objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupsResponse|


## Consumes

* application/json

## Tags

* groups

# Updates a group (**introduced 2008-06-12**).
```
PUT /group?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|ID of the group that shall be updated.|true|integer||
|QueryParameter|timestamp|Timestamp of the last update of the group to update. If the group was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the group data fields to change. Only modified fields are present and the field id is omitted.|true|GroupData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


## Consumes

* application/json

## Tags

* groups

# Gets the new, modified and deleted groups (**available since v6.18.1, introduced 2010-09-13**).
```
GET /group?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested groups.|true|integer (int64)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with fields `new`, `modified` and `deleted` representing arrays of new, modified and
deleted group objects. In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|GroupUpdatesResponse|


## Tags

* groups

# Gets a contact picture.
```
GET /halo/contact/picture
```

## Description

At least one of the optional search parameters should be set. All parameters are connected by OR during
the search. More specific parameters like `user_id` or `id` are prioritized in case of multiple matches.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|Falls back to the public session cookie.|false|string||
|QueryParameter|internal_userid|The internal user ID of a user whose picture you want to load.|false|integer||
|QueryParameter|userid|An alias for `internal_userid`.|false|integer||
|QueryParameter|user_id|An alias for `internal_userid`.|false|integer||
|QueryParameter|id|A contact ID.|false|string||
|QueryParameter|email|An email to searchz for. Will pick global address book matches before regular matches. After that picks the most recently changed contact.|false|string||
|QueryParameter|email1|An alias for `email`.|false|string||
|QueryParameter|email2|An alias for `email`.|false|string||
|QueryParameter|email3|An alias for `email`.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The picture with proper ETag and caching headers set.|string (binary)|
|404|If no picture could be found.|No Content|


## Tags

* halo

# Investigates a contact.
```
GET /halo/contact?action=investigate
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|provider|The halo provider, like "com.openexchange.halo.contacts". See `/halo/contact?action=services` for available providers.|true|string||
|QueryParameter|timezone|The timezone.|false|string||
|QueryParameter|columns|A comma-separated list of columns to return. See [Column identifiers](#column-identifiers) for a mapping of numeric identifiers to fields.|true|string||
|QueryParameter|start|The start point. Only mandatory for provider "com.openexchange.halo.appointments".|false|integer (int64)||
|QueryParameter|end|The end point. Only mandatory for provider "com.openexchange.halo.appointments".|false|integer (int64)||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified. Optional for provider "com.openexchange.halo.appointments".|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Optional for provider "com.openexchange.halo.appointments".|false|string||
|QueryParameter|limit|The maximum number of mails within the result. Optional for provider "com.openexchange.halo.mail".|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested columns. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|HaloInvestigationResponse|


## Tags

* halo

# Gets all halo services.
```
GET /halo/contact?action=services
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with available halo providers. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|HaloServicesResponse|


## Tags

* halo

# Requests a contact's profile image.
```
GET /image/contact/picture
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|folder|The folder ID in which the contact resides.|true|string||
|QueryParameter|id|The object ID of the contact.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


## Produces

* application/octet-stream

## Tags

* image

# Requests a MP3 cover image.
```
GET /image/file/mp3cover
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|id|The identifier of the uploaded image.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


## Produces

* application/octet-stream

## Tags

* image

# Requests an inline image from a mail.
```
GET /image/mail/picture
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|folder|The folder ID in which the mail resides.|true|string||
|QueryParameter|id|The object ID of the mail.|true|string||
|QueryParameter|uid|The identifier of the image inside the referenced mail.|true|string||
|QueryParameter|accountId|The mail account identifier|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


## Produces

* application/octet-stream

## Tags

* image

# Requests an image that was previously uploaded with the ajax file upload module.
```
GET /image/mfile/picture
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|uid|The identifier of the uploaded image.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


## Produces

* application/octet-stream

## Tags

* image

# Requests a user's profile image.
```
GET /image/user/picture
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|id|The object ID of the user.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


## Produces

* application/octet-stream

## Tags

* image

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This must be a contact folder.|true|string||
|QueryParameter|charset|A fixed character encoding to use when parsing the uploaded file, overriding the built-in defaults, following the conventions documented in [RFC 2278](http://tools.ietf.org/html/rfc2278) (preliminary, since 7.6.2).|false|string||
|FormDataParameter|file|The CSV file containing the contact data. The column titles are the ones described in [Detailed contact data](#detailed-contact-data).|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created contacts.
In case of errors the JSON object contains the well known [error fields](#error-handling).
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* import

# Imports calendar data from iCalendar file.
```
POST /import?action=ICAL
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This may be be an appointment or a task folder.|true|string||
|QueryParameter|suppressNotification|Can be used to disable the notifications for new appointments that are imported through the given iCal file. This help keeping the Inbox clean if a lot of appointments need to be imported. The value of this parameter does not matter because only for the existence of the parameter is checked.|false|boolean||
|QueryParameter|ignoreUIDs|When set to `true`, UIDs are partially ignored during import of tasks and appointments from iCal. Internally, each UID is replaced statically by a random one to preserve possibly existing relations between recurring appointments in the same iCal file, but at the same time to avoid collisions with already existing tasks and appointments.|false|boolean||
|FormDataParameter|file|The iCal file containing the appointment and task data.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created appointments/tasks.
In case of errors the JSON object contains the well known [error fields](#error-handling). Beside a field `warnings` may contain an array
of objects with warning data containing customary error fields.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* import

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This must be a contact folder.|true|string||
|QueryParameter|charset|A fixed character encoding to use when parsing the uploaded file, overriding the built-in defaults, following the conventions documented in [RFC 2278](http://tools.ietf.org/html/rfc2278) (preliminary, since 7.6.2).|false|string||
|FormDataParameter|file|The CSV file **with Windows' default encoding CP-1252** containing the contact data. The column titles are those used by the English, French or German version of Outlook.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created contacts.
In case of errors the JSON object contains the well known [error fields](#error-handling).
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* import

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This must be a contact folder.|true|string||
|FormDataParameter|file|The vCard file.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created objects.
In case of errors the JSON object contains the well known [error fields](#error-handling).
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* import

# Gets all infoitems.
```
GET /infostore?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all infoitems. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


## Tags

* infostore

# Checks if a given file name is valid (**available since v7.8.1, preliminary**).
```
GET /infostore?action=checkname
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|name|The file name to check.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An empty JSON object when file name is valid. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|CommonResponse|


## Tags

* infostore

# Copies an infoitem.
```
POST /infostore?action=copy
```

## Description

Copies an infoitem's data with the possibility to change the file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be copies.|true|string||
|FormDataParameter|json|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemData](#/definitions/InfoItemData) model. Only modified fields must be specified but at least `{"folder_id":"destination"}`.|true|string||
|FormDataParameter|file|The metadata as per `<input type="file" />`.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* infostore

# Copies an infoitem.
```
PUT /infostore?action=copy
```

## Description

This request cannot change or add files. Therefore it is necessary to use the `POST` method.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be copied.|true|string||
|BodyParameter|body|A JSON object containing the modified fields of the destination infoitem. The field `id` must not be present.|true|InfoItemData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


## Consumes

* application/json

## Tags

* infostore

# Deletes infoitems.
```
PUT /infostore?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitems to delete.|true|integer (int64)||
|QueryParameter|hardDelete|Defaults to `false`. If set to `true`, the file is deleted permanently. Otherwise, and if the underlying storage supports a trash folder and the file is not yet located below the trash folder, it is moved to the trash folder.|false|boolean||
|BodyParameter|body|A JSON array of objects with the fields `id` and `folder` representing infoitems that shall be deleted.|true|InfoItemListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array if the infoitems were deleted successfully. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


## Consumes

* application/json

## Tags

* infostore

# Deletes versions of an infoitem.
```
PUT /infostore?action=detach
```

## Description

## Note
When the current version of a document is deleted the new current version will be the latest version.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the base object.|true|string||
|QueryParameter|folder|The folder ID of the base object.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem.|true|integer (int64)||
|BodyParameter|body|A JSON array of version numbers to detach.|true|integer array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array of version numbers that were not deleted. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|InfoItemDetachResponse|


## Consumes

* application/json

## Tags

* infostore

# Gets an infoitem document.
```
GET /infostore?action=document
```

## Description

It is possible to add a filename to the request's URI like `/infostore/{filename}?action=document`.
The filename may be added to the customary infostore path to suggest a filename to a Save-As dialog.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|id|Object ID of the requested infoitem.|true|string||
|QueryParameter|version|If present the infoitem data describes the given version. Otherwise the current version is returned.|false|integer||
|QueryParameter|content_type|If present the response declares the given `content_type` in the Content-Type header.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the document. The response type for the HTTP request is set accordingly to the defined mimetype for this infoitem or the content_type given.|string (binary)|


## Produces

* application/octet-stream

## Tags

* infostore

# Gets an infoitem.
```
GET /infostore?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested infoitem.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemResponse|


## Tags

* infostore

# Gets a list of infoitems.
```
PUT /infostore?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id of the infoitems.|true|InfoItemListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested infoitems. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


## Consumes

* application/json

## Tags

* infostore

# Locks an infoitem.
```
GET /infostore?action=lock
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be locked.|true|string||
|QueryParameter|diff|If present the value is added to the current time on the server (both in ms). The document will be locked until that time. If this parameter is not present, the document will be locked for a duration as configured on the server.|false|integer (int64)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


## Tags

* infostore

# Moves one or more infoitems to another folder.
```
PUT /infostore?action=move
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|ID of the destination folder.|true|string||
|BodyParameter|body|A JSON array of JSON objects each referencing to an existing infoitem that is supposed to be moved to the destination folder.|true|InfoItemListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an array of infoitem identifiers that could not be moved (due to a conflict).
Th array is empty if everything went fine. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsMovedResponse|


## Consumes

* application/json

## Tags

* infostore

# Creates an infoitem.
```
POST /infostore?action=new
```

## Description

Creates a new infoitem with a file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|try_add_version|Add new file version if file name exists|false|boolean||
|FormDataParameter|json|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemBody](#/definitions/InfoItemBody) model.|true|string||
|FormDataParameter|file|The metadata as per `<input type="file" />`.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* infostore

# Creates an infoitem.
```
PUT /infostore?action=new
```

## Description

Creates a new contact. This request cannot add a file to the infoitem. Therefor it
is necessary to use the `POST` method.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing a field `file` with the modified fields of the infoitem's data. It is possible to let added object permission entities be notified about newly shared files. In that case add a "notification" object.|true|InfoItemBody array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


## Consumes

* application/json

## Tags

* infostore

# Notifies users or groups about a shared infoitem (**available since v7.8.0, preliminary**).
```
PUT /infostore?action=notify
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the shared infoitem to notify about.|true|string||
|BodyParameter|body|JSON object providing the JSON array `entities`, which holds the entity ID(s) of the users or groups that
should be notified. To send a custom message to the recipients, an additional JSON object `notification` may
be included, inside of which an optional message can be passed (otherwise, some default message is used).
(Example: {"entities":["2332"]} or {"entities":["2332"],"notification":{"transport":"mail","message":"The message"}})
|true|InfoItemSharingNotificationBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An empty JSON object. Any transport warnings that occurred during sending the
notifications are available in the warnings array of the response. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemSharingNotificationResponse|


## Consumes

* application/json

## Tags

* infostore

# Deletes all versions of an infoitem leaving only the base object.
```
PUT /infostore?action=revert
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the base object.|true|string||
|QueryParameter|folder|The folder ID of the base object.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem.|true|integer (int64)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


## Tags

* infostore

# Saves an attachment in the infostore.
```
PUT /infostore?action=saveAs
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|attachment|The ID of the attachment to save.|true|string||
|BodyParameter|body|A JSON object describing the attachment's infoitem. The field `id`is not included. The fields in
this infoitem object override values from the attachment. The folder_id must be given. It is possible to
let added object permission entities be notified about newly shared files. In that case add a "notification" object.
|true|InfoItemBody array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


## Consumes

* application/json

## Tags

* infostore

# Search for infoitems.
```
PUT /infostore?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|folder|The folder ID to restrict the search to. If not specified, all folders are searched.|false|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|start|The start index (inclusive, zero-based) in the ordered search, that is requested.|false|integer||
|QueryParameter|end|The last index (inclusive) from the ordered search, that is requested.|false|integer||
|BodyParameter|body|A JSON object containing search parameters.|true|InfoItemSearchBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching infoitems. Infoitems are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


## Consumes

* application/json

## Tags

* infostore

# Gets shared infoitems (**available since v7.8.0, preliminary**).
```
GET /infostore?action=shares
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all infoitems that are considered as shared by the user.
Each array element describes one infoitem and is itself an array. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


## Tags

* infostore

# Unlocks an infoitem.
```
GET /infostore?action=unlock
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be unlocked.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


## Tags

* infostore

# Updates an infoitem.
```
POST /infostore?action=update
```

## Description

Updates an infoitem's data and file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the updated infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|QueryParameter|offset|Optionally sets the start offset in bytes where to append the data to the document, must be equal to the actual document's length (available since v7.8.1). Only available if the underlying File storage account supports the "RANDOM_FILE_ACCESS" capability.|false|integer||
|FormDataParameter|json|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemBody](#/definitions/InfoItemBody) model. Only modified fields must be specified but at least "{}".|true|string|{}|
|FormDataParameter|file|The metadata as per `<input type="file" />`.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* infostore

# Updates an infoitem.
```
PUT /infostore?action=update
```

## Description

Updates an infoitem's data. This request cannot change or add files. Therefore it
is necessary to use the `POST` method.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing a field `file` with the modified fields of the infoitem's data. It is possible to let added object permission entities be notified about newly shared files. In that case add a "notification" object.|true|InfoItemBody array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the updated infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


## Consumes

* application/json

## Tags

* infostore

# Gets the new, modified and deleted infoitems.
```
GET /infostore?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested infoitems.|false|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|false|enum (deleted, )||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new, modified and deleted infoitems. New and modified infoitems are represented by arrays. The
elements of each array contain the information specified by the corresponding identifiers in the `columns`
parameter. Deleted infoitems would be identified by their object IDs as string, without being part of
a nested array. In case of errors the responsible fields in the response are filled (see
[Error handling](#error-handling)).
|InfoItemUpdatesResponse|


## Tags

* infostore

# Gets all versions of an infoitem.
```
GET /infostore?action=versions
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem whose versions are requested.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the infoitem. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. The timestamp is the timestamp relating to the requested infostore item.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


## Tags

* infostore

# Gets multiple documents as a ZIP archive (**available since v7.4.0**).
```
PUT /infostore?action=zipdocuments
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id, folder and optionally the documents' versions to include in the requested ZIP archive (if missing, it refers to the latest/current version).|true|InfoItemZipElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP archive. The response type for the HTTP request is set to `application/zip`.|string (binary)|


## Consumes

* application/json

## Produces

* application/zip

## Tags

* infostore

# Gets a ZIP archive containing all ifoitems of a denoted folder (**availabel since v7.6.1**).
```
GET /infostore?action=zipfolder
```

## Description

It is possible to add a filename to the request's URI like `/infostore/{filename}?action=zipfolder`.
The filename may be added to the customary infostore path to suggest a filename to a Save-As dialog.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|recursive|`true` to also include subfolders and their infoitems respectively; otherwise `false` to only consider the infoitems of specified.|false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP archive. The response type for the HTTP request is set to `application/zip`.|string (binary)|


## Produces

* application/zip

## Tags

* infostore

# Gets all JSlobs (**available since v6.22**).
```
GET /jslob?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each representing a certain JSON configuration.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|JSlobsResponse|


## Tags

* JSlob

# Gets a list of JSlobs (**available since v6.22**).
```
PUT /jslob?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||
|BodyParameter|body|A JSON array with the identifiers of the requested JSlobs.|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each representing a certain JSON configuration.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|JSlobsResponse|


## Consumes

* application/json

## Tags

* JSlob

# Stores or deletes a JSlob (**available since v6.22**).
```
PUT /jslob?action=set
```

## Description

To delete a JSON configuration just send an empty request body for the specified `id`.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The JSlob indentifier.|false|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||
|BodyParameter|body|A JSON object containing the JSON configuration to store. To delete the JSlob just send an empty body.|true|object||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* JSlob

# Updates a JSlob (**available since v6.22**).
```
PUT /jslob?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The JSlob indentifier.|false|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||
|BodyParameter|body|The JSON object containing the updated JSON configuration to store. Fields that are not included are thus not affected and survive the change. Use `/jslob?action=set` to delete fields or entire JSlob.|true|object||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* JSlob

# Acquires an identity token (**available since v7.6.0**).
```
GET /jump?action=identityToken
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|system|The identifier for the external service/system, like "com.openexchange.jump.endpoint.mysystem".|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the identity token. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|JumpResponse|


## Tags

* jump

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|random|A session random token to jump into the session. This random token is part of the login
response. Only a very short configurable time after the login it is allowed to jump into
the session with the random token.
|true|string||
|QueryParameter|client|The client can be defined here newly if it is not correct on the login request itself.|false|string||
|QueryParameter|store|Tells the UI to do a store request after login to be able to use autologin request.|false|boolean||
|QueryParameter|uiWebPath|The optional path on the webserver to the UI. If this parameter is not given the configured uiWebPath is used.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Tags

* login

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|clientIP|New IP address of the client host for the current session.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the string "1" as data attribute. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ChangeIPResponse|


## Tags

* login

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|FormDataParameter|login|The login name.|true|string||
|FormDataParameter|password|The password.|true|string (password)||
|QueryParameter|authId|Identifier for tracing every single login request passed between different systems in a cluster.
The value should be some token that is unique for every login request. This parameter must be
given as URL parameter and not inside the body of the POST request.
|true|string||
|FormDataParameter|client|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients
are used with Open-Xchange. If the autologin request should work the client must be the same as
the client sent by the UI in the normal login request.
|true|string||
|FormDataParameter|version|Used version of the HTTP/JSON interface client.|true|string||
|FormDataParameter|autologin|True or false. True tells the UI to issue a store request for the session cookie.
This store request is necessary if you want the autologin request not to fail.
|true|boolean|false|
|FormDataParameter|uiWebPath|Defines another path on the web server where the UI is located. If this parameter is not defined
the configured default of the backend is used.
|false|string||
|FormDataParameter|clientIP|IP address of the client host for that the session is created. If this parameter is not
specified the IP address of the HTTP client doing this request is used.
|false|string||
|FormDataParameter|clientUserAgent|Value of the User-Agent header of the client host for that the session is created.
If this parameter is not specified the User-Agent of the current HTTP client doing
this request is used.
|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A redirect to the web UI. The URL of the web UI is either taken from the given parameter
or from the configured default of the backend.
|string|


## Produces

* text/html

## Tags

* login

# Login with user credentials.
```
POST /login?action=login
```

## Description

The login module is used to obtain a session from the user's login
credentials. Parameters are normally expected in the POST request body.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|FormDataParameter|name|The login name.|true|string||
|FormDataParameter|password|The password (MUST be placed in the request body, otherwise the login request will be denied).|true|string (password)||
|QueryParameter|authId|Identifier for tracing every single login request passed between different systems in a cluster.
The value should be some token that is unique for every login request. This parameter must be
given as URL parameter and not inside the body of the POST request. (IS OPTIONAL, meaning can be empty)
|false|string||
|FormDataParameter|client|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients are used with Open-Xchange.|false|string||
|FormDataParameter|version|Used version of the HTTP/JSON interface client.|false|string||
|FormDataParameter|clientIP|IP address of the client host for that the session is created. If this parameter is not specified the IP address of the HTTP client doing this request is used.|false|string||
|FormDataParameter|clientUserAgent|Value of the User-Agent header of the client host for that the session is created. If this parameter is not specified the User-Agent of the current HTTP client doing this request is used.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the session ID used for all subsequent requests. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|LoginResponse|


## Tags

* login

# Does the logout.
```
GET /login?action=logout
```

## Description

Does the logout which invalidates the session.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|403|FORBIDDEN. The server refuses to respond to the request.|No Content|


## Tags

* login

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|FormDataParameter|token|The token created with `token?action=acquireToken`.|true|string||
|QueryParameter|authId|Identifier for tracing every single login request passed between different systems in a cluster.
The value should be some token that is unique for every login request. This parameter must be
given as URL parameter and not inside the body of the POST request.
|true|string||
|FormDataParameter|client|Identifier of the client using the HTTP/JSON interface. The client must identifier must be the same for each request after creating the login session.|true|string||
|FormDataParameter|secret|The value of the secret string for token logins. This is configured through the tokenlogin-secrets configuration file.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the session ID used for all subsequent requests. Additionally a random
token is contained to be used for the Easy Login method. If configured within tokenlogin-secrets
configuration file even the user password will be returned. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|LoginResponse|


## Tags

* login

# Refreshes the secret cookie (**available since v6.18.2**)
```
GET /login?action=refreshSecret
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Tags

* login

# Refreshes the auto-login cookie.
```
GET /login?action=store
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Tags

* login

# Login for a very short living session (**available since v7.0.1**).
```
POST /login?action=tokenLogin
```

## Description

This request allows every possible client to create a very short living session. This session can then be transferred to any other client preferably a browser entering then the normal web interface. Then the sessions life time will be extended equally to every other session.

Compared to the login mechanism using the random token, this request is more secure because two tokens are used. One of these tokens is only known to the client and one is generated by the server. Only the combination of both tokens allows to use the session. The combination of both tokens must be done by the client creating the session.

**DISCLAIMER:** This request MUST NOT be used by some server side instance. If some server side instance uses this request to create a session for a browser on some client machine, then you have to transfer the full URL with server and client token over some connection to the client. This creates a VULNERABILITY if this is done. The token login method is only secure if this request is already sent from the same machine that later runs the browser using the created session.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|FormDataParameter|login|The login information.|true|string||
|FormDataParameter|password|The password (MUST be placed in the request body, otherwise the login request will be denied).|true|string (password)||
|FormDataParameter|clientToken|Client side identifier for accessing the session later. The value should be some token that is unique for every login request.|true|string||
|QueryParameter|authId|Identifier for tracing every single login request passed between different systems in a cluster.
The value should be some token that is unique for every login request. This parameter must be
given as URL parameter and not inside the body of the POST request.
|true|string||
|FormDataParameter|client|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients are used with Open-Xchange.|true|string||
|FormDataParameter|version|Version of the HTTP/JSON interface client. Only for statistic evaluations.|true|string||
|FormDataParameter|autologin|True or false. True tells the UI to issue a store request for the session cookie. This store
request is necessary if you want the autologin request not to fail. This must be enabled on
the server and a client can test with the autologin request if it is enabled or not.
|true|boolean||
|FormDataParameter|uiWebPath|Defines another path on the web server where the UI is located. If this parameter is not
defined the configured default of the backend is used.
|false|string||
|FormDataParameter|clientIP|IP address of the client host for that the session is created. If this parameter is not specified the IP address of the HTTP client doing this request is used.|false|string||
|FormDataParameter|clientUserAgent|Value of the User-Agent header of the client host for that the session is created. If this
parameter is not specified the User-Agent of the current HTTP client doing this request is used.
|false|string||
|FormDataParameter|jsonResponse|(since 7.8.0) True or false (default). Defines the returned data type as JSON. Default `false` will return a redirect.|false|boolean|false|


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case `jsonResponse=true`, it is returned a JSON object. Otherwise a redirect to the web UI.
The URL of the web UI is either taken from the given parameter or from the configured default
of the backend. This redirect will only contain the server side token. The client side token
sent in the request must be appended by the client creating the session. The final URL must
have the form redirect_URL&clientToken=token. Both tokens are necessary to use the session and
both tokens must match. Otherwise the session is terminated. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|TokenLoginResponse|


## Produces

* application/json
* text/html

## Tags

* login

# Accesses a session that was previously created with the token login (**available since v7.0.1**).
```
POST /login?action=tokens
```

## Description

This request allows clients to access a session created with the `/login?action=tokenLogin` request.
When accessing the session its life time is extended equally to every other session.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|FormDataParameter|serverToken|The login name.|true|string||
|FormDataParameter|clientToken|The password (MUST be placed in the request body, otherwise the login request will be denied).|true|string||
|FormDataParameter|client|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients are used with Open-Xchange.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object conform to the normal response body containing the session identifier, the login, the identifier
and the locale of the user.  In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|TokensResponse|


## Tags

* login

# Moves mails to the given category
```
PUT /mail/categories?action=move
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|category_id|The identifier of a category.|true|string||
|BodyParameter|body|'A JSON array of mail identifier, e.g.: [{"id":ID, "folder_id":FID},{"id":ID2, "folder_id":FID2}, {...}]' 
|true|Mail_CategoriesMoveBody array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'An empty response if everything went well. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|CommonResponse|


## Consumes

* application/json

## Tags

* mail_categories

# Add a new rule
```
PUT /mail/categories?action=train
```

## Description

Adds a new rule with the given mail addresses to the given category and optionally reorganize all existing mails in the inbox.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|category_id|The identifier of a category.|true|string||
|QueryParameter|apply-for-existing|A flag indicating whether old mails should be reorganized. Defaults to 'false'.|false|boolean||
|QueryParameter|apply-for-future-ones|A flag indicating whether a rule should be created or not. Defaults to 'true'.|false|boolean||
|BodyParameter|body|'A JSON object containing a "from" field which contains an array of mail addresses.' 
|true|Mail_CategoriesTrainBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'An empty response if everything went well. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|CommonResponse|


## Consumes

* application/json

## Tags

* mail_categories

# Retrieves the unread counts of active mail categories
```
GET /mail/categories?action=unread
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|category_ids|A comma separated list of category identifiers. If set only the unread counters of this categories are retrieved.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object with a field for each active category containing the number of unread messages. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|Mail_CategoriesUnreadResponse|


## Tags

* mail_categories

# Gets all mails.
```
GET /mail?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|left_hand_limit|A positive integer number to specify the "left-hand" limit of the range to return.|false|integer||
|QueryParameter|right_hand_limit|A positive integer number to specify the "right-hand" limit of the range to return.|false|integer||
|QueryParameter|limit|A positive integer number to specify how many items shall be returned according to given sorting; overrides `left_hand_limit`/`right_hand_limit` parameters and is equal to `left_hand_limit=0` and `right_hand_limit=<limit>`.|false|integer||
|QueryParameter|filter|The category id to filter for. If set to "general" all mails which does not belong to any other category are retrieved.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an array with mail data. Each array element describes one mail and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailsResponse|


## Tags

* mail

# Marks all mails of a folder as seen (**available since v7.6.0**).
```
PUT /mail?action=all_seen
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the value `true`. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailsAllSeenResponse|


## Consumes

* application/json

## Tags

* mail

# Gets a mail attachment.
```
GET /mail?action=attachment
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the mail which contains the attachment.|true|string||
|QueryParameter|attachment|ID of the requested attachment (can be substituted by the parameter `cid` otherwise this parameter is **madatory**).|false|string||
|QueryParameter|cid|Value of header 'Content-ID' of the requested attachment (can be substituted by the parameter `attachment` otherwise this parameter is **madatory**).|false|string||
|QueryParameter|save|1 overwrites the defined mimetype for this attachment to force the download dialog, otherwise 0.|false|integer||
|QueryParameter|filter|1 to apply HTML white-list filter rules if and only if requested attachment is of MIME type `text/htm*` **AND** parameter `save` is set to 0.|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the document. The response type for the HTTP Request is set accordingly to the defined mimetype for this attachment, except the parameter save is set to 1.|string (binary)|


## Tags

* mail

# Clears the content of mail folders.
```
PUT /mail?action=clear
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Not IMAP: timestamp of the last update of the deleted mails.|false|integer (int64)||
|BodyParameter|body|A JSON array with object IDs of the mail folders that shall be cleared.|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array with IDs of mail folder that could not be cleared; meaning the response body is an empty
JSON array if everything went well. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailsCleanUpResponse|


## Consumes

* application/json

## Tags

* mail

# Copies a mail to another folder.
```
PUT /mail?action=copy
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested mail that shall be copied.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|BodyParameter|body|A JSON object containing the id of the destination folder.|true|MailDestinationBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the object ID and the folder ID of the copied mail. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailDestinationResponse|


## Consumes

* application/json

## Tags

* mail

# Gets the mail count.
```
GET /mail?action=count
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an integer value representing the folder's mail count. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailCountResponse|


## Tags

* mail

# Deletes mails.
```
PUT /mail?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Not IMAP: timestamp of the last update of the deleted mails.|false|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the mails.|true|MailListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Not IMAP: A JSON array with object IDs of mails which were modified after the specified timestamp and
were therefore not deleted. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailsCleanUpResponse|


## Consumes

* application/json

## Tags

* mail

# Forwards a mail.
```
GET /mail?action=forward
```

## Description

Returns the data for the message that shall be forwarded.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested message.|true|string||
|QueryParameter|view|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|false|enum (text, html)||
|QueryParameter|max_size|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailReplyResponse|


## Tags

* mail

# Gets a mail.
```
GET /mail?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested mail (can be substituded by `message_id` parameter).|false|string||
|QueryParameter|message_id|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|false|string||
|QueryParameter|edit|1 indicates that this request should fill the message compose dialog to edit a message and thus display-specific date is going to be withheld.|false|integer||
|QueryParameter|view|Specifies the view of the mail's body: raw (returns the content as it is, meaning no preparation are performed and thus no guarantee for safe contents is given (available since SP6 v6.10)), text ( forces the server to deliver a text-only version of the requested mail's body, even if content is HTML), textNoHtmlAttach (is the same as 'text', but does not deliver the HTML part as attachment in case of multipart/alternative content), html (to allow a possible HTML mail body being transferred as it is (but white-list filter applied)), noimg (to allow a possible HTML content being transferred but without original image src attributes which references external images; can be used to prevent loading external linked images (spam privacy protection)). **If set, the corresponding gui config setting will be ignored.**|false|enum (raw, text, textNoHtmlAttach, html, noimg)||
|QueryParameter|unseen|Use `true` to leave an unseen mail as unseen although its content is requested.|false|boolean||
|QueryParameter|max_size|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|false|integer||
|QueryParameter|attach_src|1 to let the JSON mail representation being extended by "source" field containing the mail raw RFC822 source data. (available since v7.6.1)|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailResponse|


## Tags

* mail

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested mail (can be substituded by `message_id` parameter).|false|string||
|QueryParameter|message_id|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|false|string||
|QueryParameter|unseen|Use `true` to leave an unseen mail as unseen although its content is requested.|false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the message headers as plain text. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailHeadersResponse|


## Tags

* mail

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested mail (can be substituded by `message_id` parameter).|false|string||
|QueryParameter|message_id|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|false|string||
|QueryParameter|unseen|Use `true` to leave an unseen mail as unseen although its content is requested.|false|boolean||
|QueryParameter|save|1 to write the complete message source to output stream. **NOTE:** This parameter will only be used if parameter `src` is set to 1.|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the complete message source as plain text. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailSourceResponse|


## Tags

* mail

# Import of mails as MIME data block (RFC822) (**available since v6.18**).
```
POST /mail?action=import
```

## Description

This request can be used to store a single or a lot of mails in the OX mail storage backend. This
action should be used instead of `/mail?action=new` because it is faster and tolerant to 8-bit encoded emails.

To import multiple mails add further form-data fields.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|The ID of the folder into that the emails should be imported.|true|string||
|QueryParameter|flags|In case the mail should be stored with status "read" (e.g. mail has been read already in the client inbox), the parameter "flags" has to be included. For information about mail flags see [Mail data](#/definitions/MailData) model.|false|string||
|QueryParameter|force|If this parameter is set to `true`, the server skips checking the valid from address.|false|boolean||
|FormDataParameter|file|The RFC822 encoded email as binary data.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each with the folder identifier and the object ID
of the imported mail(s). In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailImportResponse|


## Consumes

* multipart/form-data

## Tags

* mail

# Gets a list of mails.
```
PUT /mail?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|headers|(preliminary) A comma-separated list of header names. Each name requests denoted header from each mail.|false|string||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the requested mails.|true|MailListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an array with mail data. Mails are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailsResponse|


## Consumes

* application/json

## Tags

* mail

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|lineWrapAfter|An integer value specifying the line-wrap setting (only effective for plain-text content); if absent the setting is taken from user's mail settings. Available with v7.8.1.|false|integer||
|FormDataParameter|json_0|Contains the rudimentary mail as JSON string (see [SendMailData](#/definitions/SendMailData) model) with just its message body
(as html content) defined in nested JSON array `attachments` and its header data (from, to,
subject, etc.). The field "content_type" defines whether the mail ought to be sent as plain text
("text/plain"), as html ("text/html") or as multipart/alternative ("ALTERNATIVE"). Sending a mail
requires some special fields inside JSON mail object. The field "infostore_ids" defines a JSON
array of infostore document ID(s) that ought to be appended to this mail as attachments. The field
"msgref" indicates the ID of the referenced original mail. Moreover the field "sendtype" indicates
the type of the message: 0 (normal new mail), 1 (a reply mail, field `msgref` must be present),
2 (a forward mail, field `msgref` must be present), 3 (draft edit operation, field `msgref` must be
present in order to delete previous draft message since e.g. IMAP does not support changing/replacing
a message but requires a delete-and-insert sequence), 4 (transport of a draft mail, field `msgref`
must be present), 6 (signals that user intends to send out a saved draft message and expects the draft
message (referenced by `msgref` field) being deleted after successful transport).
|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page containing the object ID of the newly created mail or in case of errors an error object (see [File uploads](#file-uploads) as an example).
|string|


## Consumes

* multipart/form-data
* multipart/mixed

## Produces

* text/html

## Tags

* mail

# Sends or saves a mail as MIME data block (RFC822) (**available since SP5**).
```
PUT /mail?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|In case the mail should not be sent out, but saved in a specific folder, the "folder" parameter
can be used. If the mail should be sent out to the recipient, the "folder" parameter must not be
included and the mail is stored in the folder "Sent Items".
|false|string||
|QueryParameter|flags|In case the mail should be stored with status "read" (e.g. mail has been read already in the client
inbox), the parameter "flags" has to be included. If no `folder` parameter is specified, this parameter
must not be included. For information about mail flags see [Mail data](#/definitions/MailData) model.
|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the folder ID and the object ID of the mail. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailDestinationResponse|


## Consumes

* text/plain

## Tags

* mail

# Requests a delivery receipt for a priviously sent mail.
```
PUT /mail?action=receipt_ack
```

## Description

This delivery receipt only acknowledges that the message could be receipted on the recipients computer.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the information of a mail for which a delivery receipt shall be requested.|true|MailAckBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty data field if everything went well or a JSON object containing the error
information. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailAckResponse|


## Consumes

* application/json

## Tags

* mail

# Replies a mail.
```
GET /mail?action=reply
```

## Description

Returns the data for the message that shall be replied.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested message.|true|string||
|QueryParameter|view|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|false|enum (text, html)||
|QueryParameter|setFrom|A flag (`true`/`false`) that signals if "From" header shall be pre-selected according to a suitable recipient address that matches one of user's E-Mail address aliases. (available since v7.6.0)|false|boolean||
|QueryParameter|max_size|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailReplyResponse|


## Tags

* mail

# Replies a mail to all.
```
GET /mail?action=replyall
```

## Description

Returns the data for the message that shall be replied.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested message.|true|string||
|QueryParameter|view|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|false|enum (text, html)||
|QueryParameter|setFrom|A flag (`true`/`false`) that signals if "From" header shall be pre-selected according to a suitable recipient address that matches one of user's email address aliases. (available since v7.6.0)|false|boolean||
|QueryParameter|max_size|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailReplyResponse|


## Tags

* mail

# Resolves a given share reference
```
PUT /mail?action=resolve_share_reference
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object providing the share reference to resolve|true|ResolveShareReferenceElement||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'The JSON representation for the resolved share reference. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|ResolveShareReferenceResponse|


## Consumes

* application/json

## Produces

* application/json

## Tags

* mail

# Searches for mails.
```
PUT /mail?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response or the string “thread” to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified. Note: Applies only to root-level messages.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Note: Applies only to root-level messages.|false|string||
|BodyParameter|body|A JSON object describing the search term as introducted in [Advanced search](#advanced-search). Example:
`{"filter":["and",["=", {"field":"to"},"test1@example.com"],["not",["=",{"field":"from"},"test2@example.com"]]]}`
which represents 'to = "test1@example.com" AND NOT from = "test2@example.com"'. Available field names are
`from`, `to`, `cc`, `bcc`, `subject`, `received_date`, `sent_date`, `size`, `flags`, `content`, `content_type`, `disp`, and `priority`.
|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an array with matching mails. Mails are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailsResponse|


## Consumes

* application/json

## Tags

* mail

# Gets all mail conversations.
```
GET /mail?action=threadedAll
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response or the string “thread” to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified. Note: Applies only to root-level messages.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Note: Applies only to root-level messages.|false|string||
|QueryParameter|includeSent|A boolean value to signal that conversations also include messages taken from special "sent" aka "sent items" folder.|false|boolean||
|QueryParameter|left_hand_limit|A positive integer number to specify the "left-hand" limit of the range to return. Note: Applies only to root-level messages.|false|integer||
|QueryParameter|right_hand_limit|A positive integer number to specify the "right-hand" limit of the range to return. Note: Applies only to root-level messages.|false|integer||
|QueryParameter|limit|A positive integer number to specify how many items shall be returned according to given sorting; overrides `left_hand_limit`/`right_hand_limit` parameters and is equal to `left_hand_limit=0` and `right_hand_limit=<limit>`. Note: Applies only to root-level messages.|false|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of objects, each representing a conversation's root message along
with its message thread. The root message's JSON object is filled according to the specified `columns`
and is enhanced by a special `thread` field representing the full message thread (including the root
message itself). `thread` is a JSON array of objects each representing a message in the conversation
sorted by time-line and filled with the specified `columns`. Not IMAP: with timestamp. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailConversationsResponse|


## Tags

* mail

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested mail that shall be updated (**mandatory** if a mail shall be moved).|false|string||
|QueryParameter|message_id|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|false|string||
|BodyParameter|body|A JSON object containing the new values that ought to be applied to mail and/or the id of the destination folder (if the mail shall be moved, otherwise it must not be specified).|true|MailUpdateBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the object ID and the folder ID of an updated and/or moved mail or only
the folder ID if several mails are updated. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailDestinationResponse|


## Consumes

* application/json

## Tags

* mail

# Gets updated mails (not IMAP).
```
GET /mail?action=updates
```

## Description

## Not IMAP


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of either columns or header names to return, like "600,601,X-Custom-Header". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Just an empty JSON array is going to be returned since this action cannot be applied to IMAP. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailUpdatesResponse|


## Tags

* mail

# Gets multiple mail attachments as a ZIP file.
```
GET /mail?action=zip_attachments
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the mail which contains the attachments.|true|string||
|QueryParameter|attachment|A comma-separated list of IDs of the requested attachments.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP file.|string (binary)|


## Produces

* application/zip

## Tags

* mail

# Gets multiple mails as a ZIP file.
```
GET /mail?action=zip_messages
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|A comma-separated list of Object IDs of the requested mails.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP file.|string (binary)|


## Produces

* application/zip

## Tags

* mail

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the fields `tests` (containing an array of available test-objects, see [Possible tests](#possible-tests) too) and `actioncommands`
(containing an array of [valid actions](#possible-action-commands)). In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailFilterConfigResponse|


## Tags

* mailfilter

# Deletes a mail filter rule.
```
PUT /mailfilter?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON object with the ID of the rule to delete.|true|MailFilterDeletionBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* mailfilter

# Deletes the whole mail filter script.
```
PUT /mailfilter?action=deletescript
```

## Description

This call is only used as workaround for parsing errors in the backend, so that the user is able to kick a whole script if it contains errors in the grammar.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Tags

* mailfilter

# Gets the whole mail filter script.
```
PUT /mailfilter?action=getscript
```

## Description

This call is only used as workaround for parsing errors in the backend, so that the user is able to get the plaintext of a complete script.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the text of the complete sieve script. In case of errors the responsible fields
in the response are filled (see [Error handling](#error-handling)).
|MailFilterScriptResponse|


## Tags

* mailfilter

# Gets all mail filter rules.
```
GET /mailfilter?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|flag|If given, only rules with this flag are returned.|false|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an array of rule-objects. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailFilterRulesResponse|


## Tags

* mailfilter

# Creates a mail filter rule.
```
PUT /mailfilter?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON object describing the mail filter rule. If the field `position` is included, it's taken as the position of the rule in the array on the server side (this value shouldn't be greater than the size of all rules).|true|MailFilterRule||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created rule. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailFilterCreationResponse|


## Consumes

* application/json

## Tags

* mailfilter

# Reorders mail filter rules.
```
PUT /mailfilter?action=reorder
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON array with unique identifiers, which represents how the corresponding rules are order.|true|integer array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* mailfilter

# Updates a mail filter rule.
```
PUT /mailfilter?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON object describing the rule with the `id` set (which identifies the rule to change). Only modified fields are present.|true|MailFilterRule||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* mailfilter

# Gets all messaging accounts.
```
GET /messaging/account?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messagingService|List only those accounts that belong to the given `messagingService`.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with account objects. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingAccountsResponse|


## Tags

* messaging

# Deletes a messaging account.
```
GET /messaging/account?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messagingService|The messaging service ID that the account belongs to.|true|string||
|QueryParameter|id|The messaging account ID.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if deletion was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MessagingAccountUpdateResponse|


## Consumes

* application/json

## Tags

* messaging

# Gets a messaging account.
```
GET /messaging/account?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messagingService|The messaging service ID that the account belongs to.|true|string||
|QueryParameter|id|The messaging account ID.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the requested account. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingAccountResponse|


## Tags

* messaging

# Creates a messaging account.
```
PUT /messaging/account?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the account to create. The ID is generated by the server and must not be present.|true|MessagingAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MessagingAccountUpdateResponse|


## Consumes

* application/json

## Tags

* messaging

# Updates a messaging account.
```
PUT /messaging/account?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the modified data of the account. The fields `id` and `messagingService` must always be set.|true|MessagingAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if update was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MessagingAccountUpdateResponse|


## Consumes

* application/json

## Tags

* messaging

# Gets all messaging messages.
```
GET /messaging/message?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of column names, like "folder,headers,body". See [Messaging fields](#messaging fields) for valid column names.|true|string||
|QueryParameter|folder|The folder ID, like "com.openexchange.messaging.twitter://535/defaultTimeline/directMessages".|true|string||
|QueryParameter|sort|A column name to sort by.|false|string||
|QueryParameter|order|The order direction which can be "asc" for ascending (default) or "desc" for descending.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all messages. Each array element describes one message and
is itself an array. The elements of each array contain the information specified by the corresponding
column names in the `columns` parameter. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MessagingMessagesResponse|


## Tags

* messaging

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the message to load.|true|string||
|QueryParameter|folder|The folder ID of the message.|true|string||
|QueryParameter|peek|If set to `true` the read/unread state of the message will not change. Default is `false`.|false|boolean||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the message. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MessagingMessageResponse|


## Tags

* messaging

# Gets a list of messaging messages.
```
PUT /messaging/message?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of column names, like "folder,headers,body". See [Messaging fields](#messaging fields) for valid column names.|true|string||
|BodyParameter|body|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|true|object array array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for requested messages. Each array element describes one message and
is itself an array. The elements of each array contain the information specified by the corresponding
column names in the `columns` parameter. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MessagingMessagesResponse|


## Consumes

* application/json

## Tags

* messaging

# Performs a certain messaging action on a message.
```
PUT /messaging/message?action=perform
```

## Description

On actions of type "message" the body should contain the JSON representation of the message the action should be applied to.
To invoke a messaging action of type "storage" the folder and id are needed in URL parameters.
Messaging actions of type "none" need a messaging message and account.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messageAction|The message action to invoke.|true|string||
|QueryParameter|id|The ID of the message the action shall be invoked on. Only used on actions of type "storage".|false|string||
|QueryParameter|folder|The folder ID of the message. Only used on actions of type "storage".|false|string||
|QueryParameter|account|The account ID. Only used on actions of type "none".|false|integer||
|BodyParameter|body|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|true|MessagingMessageData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if message could be sent. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MessagingMessageUpdateResponse|


## Consumes

* spplication/json

## Tags

* messaging

# Sends a messaging message.
```
PUT /messaging/message?action=send
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|recipients|A list of recipients as defined in RFC822, like "Joe Doe <joe@doe.org>". If set the message is sent to the given list of recipients, otherwise this defaults to the "To" header of the message.|false|string||
|BodyParameter|body|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|true|MessagingMessageData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if message could be sent. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MessagingMessageUpdateResponse|


## Consumes

* spplication/json

## Tags

* messaging

# Gets all messaging services.
```
GET /messaging/service?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of messaging service objects. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingServicesResponse|


## Tags

* messaging

# Gets a messaging service.
```
GET /messaging/service?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the messaging service to load.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the messaging service. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingServiceResponse|


## Tags

* messaging

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
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|continue|Specifies whether processing of requests should stop when an error occurs, or whether all request should be processed regardless of errors.|false|boolean||
|BodyParameter|body|A JSON array with JSON objects, each describing one request.|true|SingleRequest array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array containing the response data of the processed requests where response[0] corresponds to request[0], response[1] to request[1], and so on.|SingleResponse array|
|400|Syntactically incorrect request.|No Content|


## Consumes

* application/json

## Tags

* multiple

# Gets all OAuth accounts (**available since v6.20**).
```
GET /oauth/accounts?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The service meta data identifier. If missing all accounts of all services are returned; otherwise all accounts of specified service are returned.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each describing an OAuth account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountsResponse|


## Tags

* OAuth

# Creates an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=create
```

## Description

This action is typically called by provided call-back URL and is only intended for manual invocation if
"outOfBand" interaction is returned by preceeding `/oauth/account?action=init` step.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|oauth_token|The request token from preceeding OAuth interaction.|true|string||
|QueryParameter|uuid|The UUID of the preceeding OAuth interaction.|true|string||
|QueryParameter|displayName|The display name for the new account.|true|string||
|QueryParameter|oauth_verifier|The verifier string which confirms that user granted access.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the newly created OAuth account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountResponse|


## Tags

* OAuth

# Deletes an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The account identifier.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object indicating whether the deletion was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountDeletionResponse|


## Tags

* OAuth

# Gets an OAuth account (**available since v6.20**).
```
GET /oauth/accounts?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The account identifier.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the OAuth account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountResponse|


## Tags

* OAuth

# Initializes the creation of an OAuth account (**available since v6.20**).
```
GET /oauth/accounts?action=init
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The service meta data identifier, e.g. "com.openexchange.oauth.twitter".|true|string||
|QueryParameter|displayName|The display name of the account.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the resulting interaction providing information to complete account creation.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|OAuthAccountInteractionResponse|


## Tags

* OAuth

# Updates an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The account identifier. May also be provided in request body's JSON object by field `id`.|true|integer||
|BodyParameter|body|A JSON object providing the OAuth account data to update. Currently the only values which make sense being updated are `displayName` and the `token`-`secret`-pair.|true|OAuthAccountData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object indicating whether the update was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountUpdateResponse|


## Consumes

* application/json

## Tags

* OAuth

# Gets all OAuth grants (**available since v7.8.0**).
```
GET /oauth/grants?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each describing a granted access. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthGrantsResponse|


## Tags

* OAuth

# Revokes access for an OAuth client (**available since v7.8.0**).
```
GET /oauth/grants?action=revoke
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|client|The ID of the client whose access shall be revoked.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Tags

* OAuth

# Gets all OAuth services' meta data (**available since v6.20**).
```
GET /oauth/services?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each describing an OAuth service's meta data. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthServicesResponse|


## Tags

* OAuth

# Gets all OAuth service's meta data (**available since v6.20**).
```
GET /oauth/services?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The service's identifier.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the meta data of the OAuth service. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthServiceResponse|


## Tags

* OAuth

# Updates or changes the password of the current use.
```
PUT /passwordchange?action=update
```

## Description

## Note
The new password will be set without any checks. The client must ensure that it is the password the user wants to set.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the old and the new password.|true|PasswordChangeBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* passwordchange

# Gets the filestore usage data.
```
GET /quota?action=filestore
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the filestore quota. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|QuotaResponse|


## Tags

* quota

# Gets quota information (**available since v7.6.1, preliminary**).
```
GET /quota?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|module|The module identifier (e.g. "share_links", "filestorage", ...) to get quota information for, required if account is set.|false|string||
|QueryParameter|account|The account identifier within the module to get quota information for.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|If `module` is not specified it is returned a JSON object containing all quota modules as fields.
Each field is an object itself consisting of a field `display_name` and a field `accounts`.
`accounts` is an array of JSON objects containing the properties `account_id`, `account_name`,
`countquota` (account's quota limit for the number of items, or not set if not defined),
`countuse` (account's actual usage for the number of items, or not set if no count quota defined),
`quota` (account's quota limit for the storage in bytes, or not set if not defined) and `use`
(account's actual usage for the storage in bytes, or not set if no storage quota defined). In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|QuotasResponse|
|400|If a specified `module` is not existing.|No Content|


## Tags

* quota

# Gets the mail usage data.
```
GET /quota?action=mail
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the mail quota. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|QuotaResponse|


## Tags

* quota

# Deletes reminders (**available since v6.22**).
```
PUT /reminder?action=delete
```

## Description

Before version 6.22 the request body contained only a JSON object with the field `id`.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array with JSON objects containing the field `id` of the reminders to delete.|true|ReminderListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with identifiers of reminders that were not deleted. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|RemindersResponse|


## Consumes

* application/json

## Tags

* reminder

# Gets a reminder range.
```
GET /reminder?action=range
```

## Description

Gets all reminders which are scheduled until the specified time (end date).

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|end|The end date of the reminder range.|false|integer (int64)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for each reminder. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|RemindersResponse|


## Tags

* reminder

# Updates the reminder alarm (**available since v6.18.1**).
```
PUT /reminder?action=remindAgain
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the reminder whose alarm date shall be changed.|true|integer||
|BodyParameter|body|A JSON object containing the field `alarm` which provides the new reminder date.|true|ReminderUpdateBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the updated reminder. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|ReminderResponse|


## Consumes

* application/json

## Tags

* reminder

# Gets all resources.
```
GET /resource?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of resource identifiers. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|AllResourcesResponse|


## Tags

* resources

# Deletes resources.
```
PUT /resource?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the group to delete.|true|integer (int64)||
|BodyParameter|body|A JSON array of objects with the field `id` containing the unique identifier of the resource.|true|ResourceListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array if the resources were deleted successfully. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourcesResponse|


## Consumes

* application/json

## Tags

* resources

# Gets a resource.
```
GET /resource?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the resource.|true|integer||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the resource data. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourceResponse|


## Tags

* resources

# Gets a list of resources.
```
PUT /resource?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id of the requested resources.|true|ResourceListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of resource objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourcesResponse|


## Consumes

* application/json

## Tags

* resources

# Creates a resource.
```
PUT /resource?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the resource data. The field `id` is not present.|true|ResourceData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the ID of the newly created resource. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourceUpdateResponse|


## Consumes

* application/json

## Tags

* resources

# Searches for resources.
```
PUT /resource?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object with the search parameters.|true|ResourceSearchBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of resource objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourcesResponse|


## Consumes

* application/json

## Tags

* resources

# Updates a resource.
```
PUT /resource?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|ID of the resource that shall be updated.|true|integer||
|QueryParameter|timestamp|Timestamp of the last update of the resource to update. If the resource was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the resource data fields to change. Only modified fields are present and the field id is omitted.|true|ResourceData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


## Consumes

* application/json

## Tags

* resources

# Gets the new, modified and deleted resources.
```
GET /resource?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested resources.|true|integer (int64)||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with fields `new`, `modified` and `deleted` representing arrays of new, modified and
deleted resource objects. In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|ResourceUpdatesResponse|


## Tags

* resources

# Deletes a share link (**available since v7.8.0**).
```
PUT /share/management?action=deleteLink
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the share target where the link should be deleted for.|true|ShareTargetData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* share/management

# Creates or gets a share link (**available since v7.8.0**).
```
PUT /share/management?action=getLink
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the share target where the link should be generated for.|true|ShareTargetData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing data of the (newly created) share link. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ShareLinkResponse|


## Consumes

* application/json

## Tags

* share/management

# Sends a share link (**available since v7.8.0**).
```
PUT /share/management?action=sendLink
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the share target and a list of recipients specified in a field `recipients` that
is a JSON array with a nested two-elements array containing the recipient information (first element is
personal name, second is email address). An optional field `message` can contain a notification.
|true|ShareLinkSendBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Transport warnings that occurred during sending the notifications are available in a `warnings` array.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|ShareLinkSendResponse|


## Consumes

* application/json

## Tags

* share/management

# Updates a share link (**available since v7.8.0**).
```
PUT /share/management?action=updateLink
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|The timestamp of the last modification of the link. Used to detect concurrent modifications.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the share target and share link properties of the link to update. Only modified fields should be set but at least the share target ones.|true|ShareLinkUpdateBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing data of the (newly created) share link. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ShareLinkResponse|


## Consumes

* application/json

## Tags

* share/management

# Gets all snippets (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|type|A list of comma-separated types to filter, e.g. "signature".|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all snippets. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetsResponse|


## Tags

* snippet

# Attaches one or more files to an existing snippet (**available since v7.0.0/v6.22.0**).
```
POST /snippet?action=attach
```

## Description

It can be uploaded multiple files at once. Each file must be specified in an own form field
(the form field name is arbitrary).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|QueryParameter|type|The file type filter to define which file types are allowed during upload. Currently supported filters are: file (for all), text (for `text/*`), media (for image, audio or video), image (for `image/*`), audio (for `audio/*`), video (for `video/*`) and application (for `application/*`).|true|enum (file, text, media, image, audio, video, application)||
|FormDataParameter|file|The attachment file.|true|file||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the ID of
the updated snippet or errors if some occurred.
|string|


## Consumes

* multipart/form-data

## Produces

* text/html

## Tags

* snippet

# Deletes one or multiple snippets (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=delete
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet. Otherwise provide one or more identifiers in the request body's JSON array.|false|string||
|BodyParameter|body|A JSON array containing the identifiers of the snippets to delete.|false|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* snippet

# Detaches one or more files from an existing snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=detach
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|BodyParameter|body|A JSON array with JSON objects each containing a field `id` with the identifier of an attachment that shall be removed.|true|SnippetAttachmentListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the updated snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetUpdateResponse|


## Consumes

* application/json

## Tags

* snippet

# Gets a snippet (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetResponse|


## Tags

* snippet

# Gets the attachment of a snippet (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=getattachment
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|QueryParameter|attachmentid|The identifier of the attachment.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The attachment's raw data.|string (binary)|
|500|A HTML page in case of errors.|string|


## Tags

* snippet

# Gets a list of snippets (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of snippet identifiers.|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested snippets. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetsResponse|


## Consumes

* application/json

## Tags

* snippet

# Creates a snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the snippet excluding its attachment(s). For adding attachments see `/snippet?action=attach` request.|true|SnippetData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetUpdateResponse|


## Consumes

* application/json

## Tags

* snippet

# Updates a snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|BodyParameter|body|A JSON object providing the fields that should be changed, excluding its attachments. For deleting attachments see `/snippet?action=detach` request.|true|SnippetData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the updated snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetResponse|


## Consumes

* application/json

## Tags

* snippet

# Clears a folder's content.
```
PUT /sync?action=refresh_server
```

## Description

## Note
Although the request offers to clear multiple folders at once it is recommended to clear only one folder per
request since if any exception occurs (e.g. missing permissions) the complete request is going to be aborted.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array containing the folder ID(s).|false|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of folder IDs that could not be cleared due to a concurrent modification.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersCleanUpResponse|


## Tags

* sync

# Gets all tasks.
```
GET /tasks?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all tasks. Each array element describes one task and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TasksResponse|


## Tags

* tasks

# Confirms a task.
```
PUT /tasks?action=confirm
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the task that shall be confirmed.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the task.|true|integer (int64)||
|BodyParameter|body|A JSON object with the fields `confirmation` and `confirmmessage`.|true|TaskConfirmationBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Nothing, except the standard response object with empty data, the timestamp of the confirmed and thereby
updated task, and maybe errors. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CommonResponse|


## Consumes

* application/json

## Tags

* tasks

# Deletes tasks (**available since v6.22**).
```
PUT /tasks?action=delete
```

## Description

Before version 6.22 the request body contained a JSON object with the fields `id` and `folder` and could
only delete one task.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the deleted tasks.|true|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the tasks.|true|TaskListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array with object IDs of tasks which were modified after the specified timestamp and were therefore not deleted. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskDeletionsResponse|


## Consumes

* application/json

## Tags

* tasks

# Gets a task.
```
GET /tasks?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested task.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested task. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskResponse|


## Tags

* tasks

# Gets a list of tasks.
```
PUT /tasks?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the tasks.|true|TaskListElement array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested tasks. Each array element describes one task and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TasksResponse|


## Consumes

* application/json

## Tags

* tasks

# Creates a task.
```
PUT /tasks?action=new
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the task's data.|true|TaskData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the id of the newly created task. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskUpdateResponse|


## Consumes

* application/json

## Tags

* tasks

# Search for tasks.
```
PUT /tasks?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|BodyParameter|body|A JSON object containing search parameters.|true|TaskSearchBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching tasks. Tasks are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|TasksResponse|


## Consumes

* application/json

## Tags

* tasks

# Updates a task.
```
PUT /tasks?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|id|Object ID of the requested task.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested tasks.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the task's data. Only modified fields are present.|true|TaskData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with a timestamp. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskUpdateResponse|


## Consumes

* application/json

## Tags

* tasks

# Gets the new, modified and deleted tasks.
```
GET /tasks?action=updates
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested tasks.|true|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Omit this parameter or set it to "deleted" to not have deleted tasks identifier in the response. Set this parameter to `false` and the response contains deleted tasks identifier.|false|enum (deleted, )||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new, modified and deleted tasks. New and modified tasks are represented by arrays. The
elements of each array contain the information specified by the corresponding identifiers in the `columns`
parameter. Deleted tasks would be identified by their object IDs as integers, without being part of
a nested array. In case of errors the responsible fields in the response are filled (see
[Error handling](#error-handling)).
|TaskUpdatesResponse|


## Tags

* tasks

# Gets a login token (**available since v7.4.0**).
```
GET /token?action=acquireToken
```

## Description

With a valid session it is possible to acquire a secret. Using this secret another system is able to
generate a valid session (see `login?action=redeemToken`).


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the timestamp of the creation date and a token which can be used to create a new
session. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|AcquireTokenResponse|


## Tags

* token

# Gets information about current user (**available since v7.6.2**).
```
GET /user/me
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the current user. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CurrentUserResponse|


## Tags

* user/me

# Gets all users (**available since v6.14**).
```
GET /user?action=all
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all users. Each array element describes one user and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UsersResponse|


## Tags

* user

# Gets a user (**available since v6.14**).
```
GET /user?action=get
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested user. Since v6.18.1, this parameter is optional and the default is the currently logged in user.|false|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested user. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UserResponse|


## Tags

* user

# Gets a user attribute (**available since v6.20**).
```
GET /user?action=getAttribute
```

## Description

Gets a custom user attribute that was previously set with the `/user?action=setAttribute` request.

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the user.|true|string||
|QueryParameter|name|The name of the attribute.|true|string||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the attribute data. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|UserAttributeResponse|


## Tags

* user

# Gets a list of users (**available since v6.14**).
```
PUT /user?action=list
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|true|string||
|BodyParameter|body|A JSON array of identifiers of the requested users. Since v6.18.1, a `null` value in the array is interpreted as the currently logged in user.|true|string array||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested users. Each array element describes one user and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UsersResponse|


## Consumes

* application/json

## Tags

* user

# Search for users (**available since v6.14**).
```
PUT /user?action=search
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|BodyParameter|body|A JSON object containing search parameters.|true|UserSearchBody||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching users. Users are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|UsersResponse|


## Consumes

* application/json

## Tags

* user

# Sets a user attribute (**available since v6.20**).
```
PUT /user?action=setAttribute
```

## Description

Sets a custom user attribute consisting of a name and a value. The attribute can later be
retrieved using the `/user?action=getAttribute` request.


## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the user.|true|string||
|QueryParameter|setIfAbsent|Set to `true` to put the value only if the specified name is not already associated with a value, otherwise `false` to put value in any case.|false|boolean||
|BodyParameter|body|A JSON object providing the name and the value of the attribute. If the `value` field is missing or `null`, the attribute is removed.|true|UserAttribute||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object providing the information whether the attribute could be set. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UserAttributionResponse|


## Consumes

* application/json

## Tags

* user

# Updates a user (**available since v6.14**).
```
PUT /user?action=update
```

## Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested user.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested user. If the user was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the user's data. Only modified fields are present. From [Detailed user data](#detailed-user-data) only the fields `timezone` and `locale` are allowed to be updated.|true|UserData||


## Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


## Consumes

* application/json

## Tags

* user


---
title: API Definitions
classes: no-affix
---
# AcquireTokenResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AllResourcesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of resource identifiers.|false|integer array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AppointmentConfirmationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|confirmation|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|true|integer||
|confirmmessage|The confirmation message or comment.|true|string||
|id|User ID. Confirming for other users only works for appointments and not for tasks.|false|integer||


# AppointmentCreationConflict
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|hard_conflicts|"true" if appointment represents a resource conflict.|false|boolean||
|recurrence_id|Object ID of the entire appointment sequence. Present on series and change exception appointments. Equals to object identifier on series appointment and is different to object identifier on change exceptions.|false|integer||
|recurrence_position|1-based position of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|false|integer||
|recurrence_date_position|Date of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|false|integer (int64)||
|change_exceptions|An array of Dates, representing all change exceptions of a sequence.|false|integer (int64) array||
|delete_exceptions|An array of Dates, representing all delete exceptions of a sequence.|false|integer (int64) array||
|location|The location of the appointment.|false|string||
|shown_as|Describes, how this appointment appears in availability queries: 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|false|integer||
|timezone|The timezone of the appointment.|false|string||
|recurrence_start|Start of a sequence without time.|false|integer (int64)||
|ignore_conflicts|Ignore soft conflicts for the new or modified appointment. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|false|boolean||
|title|Short description.|false|string||
|start_date|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|false|integer (int64)||
|end_date|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|false|integer (int64)||
|note|Long description.|false|string||
|alarm|Specifies when to notify the participants as the number of minutes before the start of the appointment (-1 for "no alarm"). For tasks, the Time value specifies the absolute time when the user should be notified.|false|integer (int64)||
|recurrence_type|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|false|integer||
|days|Specifies which days of the week are part of a sequence. The value is a bitfield with bit 0 indicating sunday, bit 1 indicating monday and so on. May be present if recurrence_type > 1. If allowed but not present, the value defaults to 127 (all 7 days).|false|integer||
|day_in_month|Specifies which day of a month is part of the sequence. Counting starts with 1. If the field "days" is also present, only days selected by that field are counted. If the number is bigger than the number of available days, the last available day is selected. Present if and only if recurrence_type > 2.|false|integer||
|month|Month of the year in yearly sequencies. 0 represents January, 1 represents February and so on. Present if and only if recurrence_type = 4.|false|integer||
|interval|Specifies an integer multiplier to the interval specified by recurrence_type. Present if and only if recurrence_type > 0. Must be 1 if recurrence_type = 4.|false|integer||
|until|Inclusive end date of a sequence. May be present only if recurrence_type > 0. The sequence has no end date if recurrence_type > 0 and this field is not present. Note: since this is a Date, the entire day after the midnight specified by the value is included.|false|integer (int64)||
|notification|If true, all participants are notified of any changes to this object. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|false|boolean||
|participants|Each element identifies a participant, user, group or booked resource.|false|TaskParticipant array||
|users|Each element represents a participant. User groups are resolved and are represented by their members. Any user can occur only once.|false|TaskUser array||
|occurrences|Specifies how often a recurrence should appear. May be present only if recurrence_type > 0.|false|integer||
|uid|Can only be written when the object is created. Internal and external globally unique identifier of the appointment or task. Is used to recognize appointments within iCal files. If this attribute is not written it contains an automatic generated UUID.|false|string||
|organizer|Contains the email address of the appointment organizer which is not necessarily an internal user. Not implemented for tasks.|false|string||
|sequence|iCal sequence number. Not implemented for tasks. Must be incremented on update. Will be incremented by the server, if not set.|false|integer||
|confirmations|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|false|TaskConfirmation array||
|organizerId|Contains the userIId of the appointment organizer if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|false|integer||
|principal|Contains the email address of the appointment principal which is not necessarily an internal user. Not implemented for tasks (introduced with 6.20.1).|false|string||
|principalId|Contains the userIId of the appointment principal if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|false|integer||
|full_time|True if the event is a whole day appointment or task, false otherwise.|false|boolean||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# AppointmentCreationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of the appointment.|false|string||
|conflicts|An array of appointments which cause conflicts.|false|AppointmentCreationConflict array||


# AppointmentCreationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|AppointmentCreationData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AppointmentData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|recurrence_id|Object ID of the entire appointment sequence. Present on series and change exception appointments. Equals to object identifier on series appointment and is different to object identifier on change exceptions.|false|integer||
|recurrence_position|1-based position of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|false|integer||
|recurrence_date_position|Date of an individual appointment in a sequence. Present if and only if recurrence_type > 0.|false|integer (int64)||
|change_exceptions|An array of Dates, representing all change exceptions of a sequence.|false|integer (int64) array||
|delete_exceptions|An array of Dates, representing all delete exceptions of a sequence.|false|integer (int64) array||
|location|The location of the appointment.|false|string||
|shown_as|Describes, how this appointment appears in availability queries: 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|false|integer||
|timezone|The timezone of the appointment.|false|string||
|recurrence_start|Start of a sequence without time.|false|integer (int64)||
|ignore_conflicts|Ignore soft conflicts for the new or modified appointment. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|false|boolean||
|title|Short description.|false|string||
|start_date|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|false|integer (int64)||
|end_date|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|false|integer (int64)||
|note|Long description.|false|string||
|alarm|Specifies when to notify the participants as the number of minutes before the start of the appointment (-1 for "no alarm"). For tasks, the Time value specifies the absolute time when the user should be notified.|false|integer (int64)||
|recurrence_type|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|false|integer||
|days|Specifies which days of the week are part of a sequence. The value is a bitfield with bit 0 indicating sunday, bit 1 indicating monday and so on. May be present if recurrence_type > 1. If allowed but not present, the value defaults to 127 (all 7 days).|false|integer||
|day_in_month|Specifies which day of a month is part of the sequence. Counting starts with 1. If the field "days" is also present, only days selected by that field are counted. If the number is bigger than the number of available days, the last available day is selected. Present if and only if recurrence_type > 2.|false|integer||
|month|Month of the year in yearly sequencies. 0 represents January, 1 represents February and so on. Present if and only if recurrence_type = 4.|false|integer||
|interval|Specifies an integer multiplier to the interval specified by recurrence_type. Present if and only if recurrence_type > 0. Must be 1 if recurrence_type = 4.|false|integer||
|until|Inclusive end date of a sequence. May be present only if recurrence_type > 0. The sequence has no end date if recurrence_type > 0 and this field is not present. Note: since this is a Date, the entire day after the midnight specified by the value is included.|false|integer (int64)||
|notification|If true, all participants are notified of any changes to this object. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|false|boolean||
|participants|Each element identifies a participant, user, group or booked resource.|false|TaskParticipant array||
|users|Each element represents a participant. User groups are resolved and are represented by their members. Any user can occur only once.|false|TaskUser array||
|occurrences|Specifies how often a recurrence should appear. May be present only if recurrence_type > 0.|false|integer||
|uid|Can only be written when the object is created. Internal and external globally unique identifier of the appointment or task. Is used to recognize appointments within iCal files. If this attribute is not written it contains an automatic generated UUID.|false|string||
|organizer|Contains the email address of the appointment organizer which is not necessarily an internal user. Not implemented for tasks.|false|string||
|sequence|iCal sequence number. Not implemented for tasks. Must be incremented on update. Will be incremented by the server, if not set.|false|integer||
|confirmations|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|false|TaskConfirmation array||
|organizerId|Contains the userIId of the appointment organizer if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|false|integer||
|principal|Contains the email address of the appointment principal which is not necessarily an internal user. Not implemented for tasks (introduced with 6.20.1).|false|string||
|principalId|Contains the userIId of the appointment principal if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|false|integer||
|full_time|True if the event is a whole day appointment or task, false otherwise.|false|boolean||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# AppointmentDeletionsElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the appointment.|true|string||
|folder|The object ID of the related folder.|true|string||
|pos|Value of the field recurrence_position, if present in the appointment.|false|integer||


# AppointmentDeletionsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array with object IDs of appointments which were modified after the specified timestamp and were therefore not deleted.|false|AppointmentDeletionsElement array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AppointmentFreeBusyItem
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|full_time|True if the event is a whole day appointment or task, false otherwise.|false|boolean||
|confirmations|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|false|TaskConfirmation array||
|title|Short description.|false|string||
|start_date|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|false|integer (int64)||
|end_date|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|false|integer (int64)||
|participants|Each element identifies a participant, user, group or booked resource.|false|TaskParticipant array||
|recurrence_type|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|false|integer||
|shown_as|Describes, how this appointment appears in availability queries: 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|false|integer||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# AppointmentFreeBusyResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|AppointmentFreeBusyItem array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AppointmentInfoResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array with elements that correspond with days in the time range, explaining whether a day has appointments or not.|false|boolean array||


# AppointmentListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the appointment.|true|string||
|folder|The object ID of the related folder.|true|string||
|recurrence_position|1-based position of an individual appointment in a sequence.|false|integer||
|recurrence_date_position|Date of an individual appointment in a sequence.|false|integer (int64)||


# AppointmentResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|AppointmentData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AppointmentSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find appointments. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||
|startletter|Search appointments with the given starting letter.|false|string||


# AppointmentUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of the appointment.|false|string||


# AppointmentUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|AppointmentUpdateData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AppointmentUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of appointments.|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AppointmentsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of appointments. Each appointment is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AttachmentData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder|The ID of the first folder in which the attached object resides.|false|integer||
|attached|The ID of the object this attachment is attached to.|false|integer||
|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|false|integer||
|filename|The filename of the attached file.|false|string||
|file_size|The file size (in bytes) of the attached file.|false|integer (int64)||
|file_mimetype|The MIME type of the attached file.|false|string||
|rft_flag|If the attachment is a RTF attachment of outlook (outlook descriptions can be stored as RTF documents).|false|boolean||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# AttachmentResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|AttachmentData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AttachmentUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of attachments.|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AttachmentsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of attachments. Each attachment is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# AutoConfigResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailAccountData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# CapabilitiesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of JSON objects each describing one capability.|false|CapabilityData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# CapabilityData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the capability.|false|string||
|attributes|A JSON object holding properties of the capability.|false|object||


# CapabilityResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|CapabilityData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ChangeIPResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# CommonResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ConfigBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The new value of the node specified by path.|true|object||


# ConfigProperty
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|The name of the property.|false|string||
|value|The value of the property.|false|object||


# ConfigPropertyBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|value|The concrete value to set.|false|string||


# ConfigPropertyResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ConfigProperty||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ConfigResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Generic type which can be object, string, array, etc.|false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ContactData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|uid|Can only be written when the object is created. Internal and external globally unique identifier of the contact. Is used to recognize contacts within vCard files. If this attribute is not written it contains an automatic generated UUID.|false|string||
|display_name|The display name.|false|string||
|first_name|The given name.|false|string||
|last_name|The sur name.|false|string||
|second_name|The middle name.|false|string||
|suffix|The suffix.|false|string||
|title|The title.|false|string||
|street_home|The street of the home address.|false|string||
|postal_code_home|The postal code of the home address.|false|string||
|city_home|The city of the home address.|false|string||
|state_home|The state of the home address.|false|string||
|country_home|The country of the home address.|false|string||
|birthday|The date of birth.|false|integer (int64)||
|marital_status|The marital status.|false|string||
|number_of_children|The number of children.|false|string||
|profession|The profession.|false|string||
|nickname|The nickname.|false|string||
|spouse_name|The name of the spouse.|false|string||
|anniversary|The anniversary.|false|integer (int64)||
|note|A note.|false|string||
|department|The department.|false|string||
|position|The position.|false|string||
|employee_type|The type of the employee.|false|string||
|room_number|The room number.|false|string||
|street_business|The street of the business address.|false|string||
|postal_code_business|The postal code of the business address.|false|string||
|city_business|The city of the business address.|false|string||
|state_business|The state of the business address.|false|string||
|country_business|The country of the business address.|false|string||
|user_id|The internal user id.|false|integer||
|number_of_employees|The number of employees.|false|string||
|sales_volume|The sales volume.|false|string||
|tax_id|The tax id.|false|string||
|commercial_register|The commercial register.|false|string||
|branches|The branches.|false|string||
|business_category|The business category.|false|string||
|info|An information.|false|string||
|manager_name|The manager's name.|false|string||
|assistant_name|The assistant's name.|false|string||
|street_other|The street of another address.|false|string||
|postal_code_other|The postal code of another address.|false|string||
|city_other|The city of another address.|false|string||
|state_other|The state of another address.|false|string||
|country_other|The country of another address.|false|string||
|telephone_business1|The business telephone number 1.|false|string||
|telephone_business2|The business telephone number 2.|false|string||
|fax_business|The business fax number.|false|string||
|telephone_callback|The callback telephone number.|false|string||
|telephone_car|The car telephone number.|false|string||
|telephone_company|The company telephone number.|false|string||
|telephone_home1|The home telephone number 1.|false|string||
|telephone_home2|The home telephone number 2.|false|string||
|fax_home|The home fax number.|false|string||
|cellular_telephone1|The cellular telephone number 1.|false|string||
|cellular_telephone2|The cellular telephone number 2.|false|string||
|telephone_other|The other telephone number.|false|string||
|fax_other|The other fax number.|false|string||
|email1|The email address 1.|false|string||
|email2|The email address 2.|false|string||
|email3|The email address 3.|false|string||
|url|The url address or homepage.|false|string||
|telephone_isdn|The ISDN telephone number.|false|string||
|telephone_pager|The pager telephone number.|false|string||
|telephone_primary|The primary telephone number.|false|string||
|telephone_radio|The radio telephone number.|false|string||
|telephone_telex|The telex telephone number.|false|string||
|telephone_ttytdd|The TTY/TDD telephone number.|false|string||
|instant_messenger1|The instant messenger address 1.|false|string||
|instant_messenger2|The instant messenger address 2.|false|string||
|telephone_ip|The IP telephone number.|false|string||
|telephone_assistant|The assistant telephone number.|false|string||
|company|The company name.|false|string||
|image1||false|string||
|image1_content_type|The content type of the image (like "image/png").|false|string||
|image1_url|The url to the image.|false|string||
|number_of_images|The number of images.|false|integer||
|image_last_modified|The last modification of the image.|false|integer (int64)||
|distribution_list|If this contact is a distribution list, then this field is an array of objects. Each object describes a member of the list.|false|DistributionListMember array||
|number_of_distribution_list|The number of objects in the distribution list.|false|integer||
|mark_as_distributionlist||false|boolean||
|file_as|The file name.|false|string||
|default_address|The default address.|false|integer||
|useCount|In case of sorting purposes the column 609 is also available, which places global address book contacts at the beginning of the result. If 609 is used, the order direction (ASC, DESC) is ignored.|false|integer||
|yomiFirstName|Kana based representation for the First Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|false|string||
|yomiLastName|Kana based representation for the Last Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|false|string||
|yomiCompany|Kana based representation for the Company. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|false|string||
|addressHome|Support for Outlook 'home' address field. (since 6.20.1)|false|string||
|addressBusiness|Support for Outlook 'business' address field. (since 6.20.1)|false|string||
|addressOther|Support for Outlook 'other' address field. (since 6.20.1)|false|string||
|userfield01|Dynamic Field 1.|false|string||
|userfield02|Dynamic Field 2.|false|string||
|userfield03|Dynamic Field 3.|false|string||
|userfield04|Dynamic Field 4.|false|string||
|userfield05|Dynamic Field 5.|false|string||
|userfield06|Dynamic Field 6.|false|string||
|userfield07|Dynamic Field 7.|false|string||
|userfield08|Dynamic Field 8.|false|string||
|userfield09|Dynamic Field 9.|false|string||
|userfield10|Dynamic Field 10.|false|string||
|userfield11|Dynamic Field 11.|false|string||
|userfield12|Dynamic Field 12.|false|string||
|userfield13|Dynamic Field 13.|false|string||
|userfield14|Dynamic Field 14.|false|string||
|userfield15|Dynamic Field 15.|false|string||
|userfield16|Dynamic Field 16.|false|string||
|userfield17|Dynamic Field 17.|false|string||
|userfield18|Dynamic Field 18.|false|string||
|userfield19|Dynamic Field 19.|false|string||
|userfield20|Dynamic Field 20.|false|string||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# ContactDeletionsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ContactListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the contact.|true|string||
|folder|The object ID of the related folder.|true|string||


# ContactResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ContactData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ContactSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find contacts. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves. Matching is performed against any substring of the field display_name.|false|string||
|startletter|Search contacts with the given startletter. If this field is present, the pattern is matched against the contact field which is specified by the property "contact_first_letter_field" on the server (default: last name). Otherwise, the pattern is matched against the display name.|false|boolean||
|folder|If a list of folder identifiers or at least a single folder identifier is given, only in that folders will be searched for contacts. This paramenter is optional but searching in all contact folders that are viewable and where objects can be read in is more expensive on that database than searching in a dedicated number of them. The possibility to provide here an array of folder identifier has been added with 6.10.|false|integer array||
|last_name|Searches contacts where the last name match with the given last name.|false|string||
|first_name|Searches contacts where the first name match with the given first name.|false|string||
|display_name|Searches contacts where the display name match with the given display name.|false|string||
|email1|Searches contacts where the email1 address match with the given search pattern. (requires version >= 6.12)|false|string||
|email2|Searches contacts where the email2 address match with the given search pattern. (requires version >= 6.12)|false|string||
|email3|Searches contacts where the email3 address match with the given search pattern. (requires version >= 6.12)|false|string||
|company|Searches contacts where the company match with the given search pattern. (requires version >= 6.12)|false|string||
|categories|Searches contacts where the categories match with the given search pattern.|false|string||
|orSearch|If set to true, a contact is returned if any specified pattern matches at the start of the corresponding field. Otherwise, a contact is returned if all specified patterns match any substring of the corresponding field.|false|boolean||
|emailAutoComplete|If set to true, results are guaranteed to contain at least one email adress and the search is performed as if orSearch were set to true. The actual value of orSearch is ignored.|false|boolean||
|exactMatch|If set to true, contacts are returned where the specified patterns match the corresponding fields exactly. Otherwise, a "startsWith" or "substring" comparison is used based on the "orSearch" parameter. (requires version > 6.22.1)|false|boolean||


# ContactUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a newly created contact.|false|string||


# ContactUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ContactUpdateData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ContactUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of contacts.|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ContactsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of contacts. Each contact is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ConversionBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|datasource||false|ConversionDataSource||
|datahandler||false|ConversionDataHandler||


# ConversionDataHandler
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|identifier|The identifier of the data handler.|false|string||
|args|A JSON array of optional JSON objects containing the name-value-pairs.|false|ConversionDataHandlerPair array||


# ConversionDataHandlerPair
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|com.openexchange.groupware.calendar.folder|The calendar folder ID.|false|string||
|com.openexchange.groupware.task.folder|The task folder ID.|false|string||
|com.openexchange.groupware.calendar.confirmstatus|The status.|false|string||
|com.openexchange.groupware.calendar.confirmmessage|The message.|false|string||
|com.openexchange.groupware.calendar.timezone|The timezone ID.|false|string||
|com.openexchange.grouware.calendar.recurrencePosition|The recurrence position.|false|string||
|com.openexchange.groupware.calendar.searchobject|Can be `true` or `false`.|false|string||
|com.openexchange.groupware.contact.folder|The contact folder ID.|false|string||


# ConversionDataSource
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|identifier|The identifier of the data source.|false|string||
|args|A JSON array of optional JSON objects containing the name-value-pairs.|false|ConversionDataSourcePair array||


# ConversionDataSourcePair

A name-value-pair where only one name with a value must be filled out except the case when VCard data from speicified contact object(s) is obtained then the `folder` and `id` must be specified.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|com.openexchange.mail.conversion.fullname|The folder's full name.|false|string||
|com.openexchange.mail.conversion.mailid|The object ID of the mail.|false|string||
|com.openexchange.mail.conversion.sequenceid|The attachment sequence ID.|false|string||
|folder|A folder ID.|false|string||
|id|The ID.|false|string||


# ConversionResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# CurrentUserData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|context_id|The unique identifier of the user's context.|false|integer||
|user_id|The unique identifier of the user himself.|false|integer||
|context_admin|The ID of the context's administrator user.|false|integer||
|login_name|The login name of the user.|false|string||
|display_name|The display name of the user.|false|string||


# CurrentUserResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|CurrentUserData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# DistributionListMember
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|Object ID of the member's contact if the member is an existing contact.|false|string||
|folder_id|Parent folder ID of the member's contact if the member is an existing contact (preliminary, from 6.22 on).|false|string||
|display_name|The display name.|false|string||
|mail|The email address (mandatory before 6.22, afterwards optional if you are referring to an internal contact).|false|string||
|mail_field|Which email field of an existing contact (if any) is used for the mail field: 0 (independent contact), 1 (default email field, email1), 2 (second email field, email2), 3 (third email field, email3).|false|number||


# FileAccountCreationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The ID of the newly created account.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FileAccountData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the file storage account in the scope of its file storage service (e.g. Infostore, Dropbox, ...). This is not writeable and is generated by the server.|false|string||
|filestorageService|The identifier of the file storage service this account belongs to.|false|string||
|qualifiedId|A global identifier of the file storage account across all file storage services. This is not writeable and is generated by the server.|false|string||
|displayName|A user chosen, human-readable name to identify the account. Will also be translated into the folder name of the folder representing the accounts content.|false|string||
|rootFolder|The ID of the account's root folder within the folder tree. This is not writeable and is generated by the server.|false|string||
|isDefaultAccount|Indicates whether this account is the user's default account. Exactly one account will have this flag set to `true`.|false|boolean||
|capabilities|An array of capability names. Possible values are: FILE_VERSIONS, EXTENDED_METADATA, RANDOM_FILE_ACCESS, and LOCKS.|false|string array||
|configuration|The configuration data according to the form description of the relevant file storage service.|false|object||


# FileAccountResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|FileAccountData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FileAccountUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Returns 1 on success.|false|integer||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FileAccountsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of JSON objects each describing one file storage account.|false|FileAccountData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FileServiceConfiguration
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|widget|The name of the widget.|false|string||
|name|The name of the field.|false|string||
|displayName|The display name of the field.|false|string||
|mandatory|Indicates whether the field is mandatory.|false|boolean||
|options|A list of available options in the field.|false|object array||
|defaultValue|Can contain a default value.|false|object||


# FileServiceData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the file storage service, e.g. "boxcom".|false|string||
|displayName|A human-readable display name of the service, e.g. "Box File Storage Service"|false|string||
|configuration|An array of dynamic form fields. Same as in PubSub.|false|FileServiceConfiguration array||


# FileServiceResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|FileServiceData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FileServicesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of JSON objects each describing one service.|false|FileServiceData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FindActiveFacet
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the according facet.|false|string||
|value|The ID of the according value. Must always be copied from the value object, not from a possibly according option (in the two-dimensional case).|false|string||
|filter||false|FindActiveFacetFilter||


# FindActiveFacetFilter

The filter object, copied from the value or option.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|fields|An array of fields to search for.|false|string array||
|queries|An array of corresponding search values.|false|string array||


# FindAutoCompleteBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|prefix|The user's search input.|false|string||
|facets|An array of already selected facets, meaning categories the user has filtered by before.|false|FindFacetData array||
|options||false|FindOptionsData||


# FindAutoCompleteData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|facets|An array of facets each describing a possible search category or an already applied category.|false|FindFacetData array||


# FindAutoCompleteResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|FindAutoCompleteData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FindFacetData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|style|The facet style, which can be one of: simple, default, or exclusive. Dependent on the style some fields are present and others are not.|false|string||
|id|The ID of this facet. Must be unique within an autocomplete response. Can be used to distinguish and filter certain facets. (for simple, default, and exclusive)|false|string||
|name|A displayable (and localized) name for this facet. If absent, an `item` attribute is present. (for simple, default, and exclusive)|false|string||
|item||false|FindFacetItem||
|flags|An array of flags. Available flags: conflicts (specified as "conflicts:<other-id>", facets carrying this flag must not be combined with a facet of type <other-id>). (for simple, default, and exclusive)|false|string array||
|filter||false|FindFacetFilter||
|values|An array of facet values. (for default)|false|FindFacetValue array||
|options|An array of facet values. (for exclusive)|false|FindFacetValue array||


# FindFacetFilter

The filter to refine the search. (for simple)

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|fields|An array of fields to search for.|false|string array||
|queries|An array of corresponding search values.|false|string array||


# FindFacetItem

A more complex object to display this facet. Attributes are `name`, `detail` (optional), and `image_url` (optional). (for simple, default, and exclusive)

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|A displayable (and localized) name for the facet.|false|string||
|detail|A displayable (and localized) detail name, like "in mail text".|false|string||
|image_url|An URL to a displayable image.|false|string||


# FindFacetValue
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the value. Must be unique within one facet.|false|string||
|name|A displayable (and localized) name for this facet. If absent, an `item` attribute is present.|false|string||
|item||false|FindFacetValueItem||
|filter||false|FindFacetValueFilter||
|options|An array of options to refine the search.|false|FindFacetValueOption array||


# FindFacetValueFilter

The filter to refine the search.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|fields|An array of fields to search for.|false|string array||
|queries|An array of corresponding search values.|false|string array||


# FindFacetValueItem

A more complex object to display this facet. Attributes are `name`, `detail` (optional), and `image_url` (optional).

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|A displayable (and localized) name for the facet.|false|string||
|detail|A displayable (and localized) detail name, like "in mail text".|false|string||
|image_url|An URL to a displayable image.|false|string||


# FindFacetValueOption
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the option. Must be unique within a set of options.|false|string||
|name|The displayable (and localized) name for this option.|false|string||
|filter||false|FindFacetValueFilter||


# FindOptionsData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|timezone|The timezone to use if any dates are returned.|false|string||
|admin|Indicates whether the context admin shall be included if it matches any search criteria. If the context admin shall always be ignored (i.e. not returned), `false` has to be set.|false|boolean||


# FindQueryBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|facets|An array of selected facets that shall be applied for search.|false|FindActiveFacet array||
|options||false|FindOptionsData||
|start|The start of a pagination, if desired.|false|integer||
|size|The page size of a pagination, if desired.|false|integer||


# FindQueryResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|num_found|The number of found items.|false|integer||
|start|The start of the pagination.|false|integer||
|size|The page size.|false|integer||
|results|An array of search results. Each result is described by a JSON object containing the fields specified in the `columns` parameter.|false|object array||


# FolderBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder||true|FolderData||
|notification||false|FolderBodyNotification||


# FolderBodyNotification
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|transport|E.g. "mail".|false|string||
|message||false|string||


# FolderData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|Object ID|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|last_modified_utc|Timestamp of the last modification. Note that the type is Timestamp, not Time (added 2008-10-17, with SP5, temporary workaround).|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|title|Name of this folder.|false|string||
|module|Name of the module which implements this folder; e.g. "tasks", "calendar", "contacts", "infostore", or "mail"|false|string||
|type|Type of folder.|false|integer||
|subfolders|true if this folder has subfolders.|false|boolean||
|own_rights|Permissions which apply to the current user, as described either in [Permission flags](#permission-flags) or in http://tools.ietf.org/html/rfc2086.|false|integer||
|permissions||false|FolderPermission array||
|summary|Information about contained objects.|false|string||
|standard_folder|Indicates whether or not folder is marked as a default folder (only OX folder).|false|boolean||
|total|The number of objects in this Folder.|false|integer||
|new|The number of new objects in this Folder.|false|integer||
|unread|The number of unread objects in this Folder.|false|integer||
|deleted|The number of deleted objects in this Folder.|false|integer||
|capabilities|Bit mask containing information about mailing system capabilites: bit 0 (mailing system supports permissions), bit 1 (mailing system supports ordering mails by their thread reference), bit 2 (mailing system supports quota restrictions), bit 3 (mailing system supports sorting), bit 4 (mailing system supports folder subscription).|false|integer||
|subscribed|Indicates whether this folder should appear in folder tree or not. Standard folders cannot be unsubscribed.|false|boolean||
|subscr_subflds|Indicates whether subfolders should appear in folder tree or not.|false|boolean||
|standard_folder_type|Indicates the default folder type: 0 (non-default folder), 1 (task), 2 (calendar), 3 (contact), 7 (inbox), 8 (infostore), 9 (drafts), 10 (sent), 11 (spam), 12 (trash).|false|integer||
|supported_capabilities|Can contain "permissions", "publication", "quota", "sort", "subscription".|false|string array||
|account_id|Will be null if the folder does not belong to any account (i.e. if its module doesn't support multiple accounts), is a virtual folder or an account-agnostic system folder. Since 7.8.0.|false|string||
|com.openexchange.publish.publicationFlag|Indicates whether this folder is published. Read Only, provided by the com.openexchange.publish plugin, since 6.14.|false|boolean||
|com.openexchange.subscribe.subscriptionFlag|Indicates whether this folder has subscriptions storing their content in this folder. Read Only, provided by the com.openexchange.subscribe plugin, since 6.14.|false|boolean||
|com.openexchange.folderstorage.displayName|Provides the display of the folder's owner. Read Only, Since 6.20.|false|string||
|com.openexchange.share.extendedPermissions||false|FolderExtendedPermission array||


# FolderExtendedPermission
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entity|Identifier of the permission entity (i.e. user-, group- or guest-ID).|false|integer||
|bits|A number as described in [Permission flags](#permission-flags).|false|integer||
|type|Set "user" for an internal user, "group" for a group, "guest" for a guest, or "anonymous" for an anonymous permission entity.|false|string||
|display_name|A display name for the permission entity.|false|string||
|contact|A (reduced) set of [Detailed contact data](#detailed-contact-data) for "user" and "guest" entities.|false|object||
|share_url|The share link for "anonymous" entities.|false|string||
|password|The optionally set password for "anonymous" entities.|false|string||
|expiry_date|The optionally set expiry date for "anonymous" entities.|false|integer (int64)||


# FolderPermission
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|bits|For non-mail folders, a number as described in [Permission flags](#permission-flags).|false|integer||
|rights|For mail folders, the rights string as defined in http://tools.ietf.org/html/rfc2086.|false|string||
|entity|User ID of the user or group to which this permission applies (ignored for type "anonymous" or "guest").|false|integer||
|group|true if entity refers to a group, false if it refers to a user (ignored for type "anonymous" or "guest").|false|boolean||
|type|The recipient type, i.e. one of "user", "group", "guest", "anonymous" (required if no internal "entity" defined).|false|string||
|password|An additional secret / pin number an anonymous user needs to enter when accessing the share (for type "anonymous", optional).|false|string||
|email_address|The e-mail address of the recipient (for type "guest").|false|string||
|display_name|The display name of the recipient (for type "guest", optional).|false|string||
|contact_id|The object identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", optional).|false|string||
|contact_folder|The folder identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", required if "contact_id" is set).|false|string||
|expiry_date|The end date / expiration time after which the share link is no longer accessible (for type "anonymous", optional).|false|integer (int64)||


# FolderResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|FolderData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FolderSharingNotificationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entities|Array containing the entity ID(s) of the users or groups that shall be notified.|true|string array||
|notification||false|FolderBodyNotification||


# FolderSharingNotificationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|warnings|Can contain transport warnings that occured during sending the notifications.|false|object array||


# FolderSharingNotificationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|FolderSharingNotificationData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FolderUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The object id of the folder.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FolderUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of folders.|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FoldersCleanUpResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array with object IDs of folders that could not be processed because of a concurrent modification or something else.
|false|string array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FoldersResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of folders. Each folder is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FoldersVisibilityData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|private|Array of private folders. Each folder is described as an array itself.|false|object array array||
|public|Array of public folders. Each folder is described as an array itself.|false|object array array||
|shared|Array of shared folders. Each folder is described as an array itself.|false|object array array||


# FoldersVisibilityResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|FoldersVisibilityData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FreeBusyData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the corresponding appointment if available.|false|string||
|folder_id|The folder ID of the corresponding appointment if available.|false|string||
|title|The title of the corresponding appointment if available.|false|string||
|start_date|The start time of the interval.|false|integer (int64)||
|end_date|The end time of the interval.|false|integer (int64)||
|shown_as|The busy status of this interval, one of: 0 (unknown), 1 (reserved), 2 (temporary), 3 (absent), 4 (free).|false|integer||
|location|The location of the corresponding appointment if available.|false|string||
|full_time|Indicates whether the corresponding appointment is a whole day appointment, not present otherwise.|false|boolean||


# FreeBusyResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of free/busy intervals.|false|FreeBusyData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# FreeBusysResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The free/busy data for all requested participants inside a JSON object with the participant IDs as keys,
like `{"data":{"3":{"data":[{"start_date":...},{"start_date":...]}},"19":{"data":[{"start_date":...}]}}}`. Besides a combined data
element for a requested group, all group members are resolved and listed separately in the result. If the `merged` parameter
is specified, an additional data element named `merged` representing a combined view for all requested participants is added
to the results implicity.
|false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# GroupData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The group ID.|false|integer||
|display_name|Display name of the group.|false|string||
|name|Internal name with character restrictions.|false|string||
|members|The array contains identifiers of users that are member of the group.|false|integer array||
|last_modified_utc|Timestamp of the last modification.|false|integer (int64)||


# GroupListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a group.|false|integer||


# GroupResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|GroupData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# GroupSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find groups. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||


# GroupUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of a newly created group.|false|integer||


# GroupUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|GroupUpdateData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# GroupUpdatesData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|new|Array of new group objects.|false|GroupData array||
|modified|Array of modified group objects.|false|GroupData array||
|deleted|Array of deleted group objects.|false|GroupData array||


# GroupUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|GroupUpdatesData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# GroupsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of group objects.|false|GroupData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# HaloInvestigationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of halo data objects. Each object is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# HaloServicesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array with available halo providers.|false|string array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# InfoItemBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|file||true|InfoItemData||
|notification||false|InfoItemBodyNotification||


# InfoItemBodyNotification

Responsible for sending out notifications for changed object permissions of an infoitem.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|transport|E.g. "mail".|false|string||
|message||false|string||


# InfoItemData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|object_permissions|An array of object permissions (preliminary, available since v7.8.0).|false|InfoItemPermission array||
|shareable|(read-only) Indicates if the item can be shared (preliminary, available since v7.8.0).|false|boolean||
|title|The title.|false|string||
|url|Link/URL to item.|false|string||
|filename|Displayed filename of the document.|false|string||
|file_mimetype|MIME type of the document. The client converts known types to more readable names before displaying them.|false|string||
|file_size|The size of the document in bytes.|false|integer (int64)||
|version|Version of the document. New documents start at 1. Every update increments the version by 1.|false|string||
|description|A description if the item.|false|string||
|locked_until|The time until which this item will presumably be locked. Only set if the docment is currently locked, 0 otherwise.|false|integer (int64)||
|file_md5sum|MD5Sum of the document.|false|string||
|version_comment|A version comment is used to file a changelog for the file.|false|string||
|current_version|"true" if this version is the current version, "false" otherwise. Note: This is not writeable.|false|boolean||
|number_of_versions|The number of all versions of the item. Note: This is not writeable.|false|integer||
|com.openexchange.share.extendedObjectPermissions|An array of extended object permissions (read-only). (available since 7.8.0)|false|InfoItemExtendedPermission array||
|com.openexchange.realtime.resourceID|The resource identifier for the infoitem for usage within the realtime component (read-only). (available since 7.8.0)|false|string||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# InfoItemDetachResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|integer array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# InfoItemExtendedPermission
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entity|Identifier of the permission entity (i.e. user-, group- or guest-ID).|false|integer||
|bits|A number specifying the permission flags: 0 (the numerical value indicating no object permissions), 1 (the numerical value indicating read object permissions), 2 (the numerical value indicating write object permissions. This implicitly includes the “read” permission (this is no bitmask)).|false|integer||
|type|"user" for an internal user, "group" for a group, "guest" for a guest, or "anonymous" for an anonymous permission entity.|false|string||
|display_name|A display name for the permission entity.|false|string||
|contact||false|ContactData||
|share_url|The share link for "anonymous" entities.|false|string||
|password|The optionally set password for "anonymous" entities.|false|string||
|expiry_date|The optionally set expiry date for "anonymous" entities.|false|integer (int64)||


# InfoItemListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the infoitem (e.g. "31841/36639").|true|string||
|folder|The object ID of the related folder (e.g. "31841").|true|string||


# InfoItemPermission
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|bits|A number specifying the permission flags: 0 (the numerical value indicating no object permissions), 1 (the numerical value indicating read object permissions), 2 (the numerical value indicating write object permissions. This implicitly includes the “read” permission (this is no bitmask)).|false|integer||
|entity|User ID of the user or group to which this permission applies.|false|integer||
|group|Is true if entity refers to a group, false if it refers to a user.|false|boolean||
|type|The recipient type, i.e. one of "user", "group", "guest", "anonymous" (required if no internal "entity" defined).|false|string||
|password|An additional secret / pin number an anonymous user needs to enter when accessing the share (for type "anonymous", optional).|false|string||
|email_address|The e-mail address of the recipient (for type "guest").|false|string||
|display_name|The display name of the recipient (for type "guest", optional).|false|string||
|contact_id|The object identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", optional).|false|string||
|contact_folder|The folder identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", required if "contact_id" is set).|false|string||
|expiry_date|The end date / expiration time after which the share link is no longer accessible (for type "anonymous", optional).|false|integer (int64)||


# InfoItemResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|InfoItemData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# InfoItemSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|The search pattern, where "*" matches any sequence of characters.|false|string||


# InfoItemSharingNotificationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entities|Array containing the entity ID(s) of the users or groups that shall be notified.|true|string array||
|notification||false|InfoItemBodyNotification||


# InfoItemSharingNotificationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|warnings|Can contain transport warnings that occured during sending the notifications.|false|object array||


# InfoItemSharingNotificationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|InfoItemSharingNotificationData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# InfoItemUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The object ID of the updated infoitem.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# InfoItemUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of infoitems.|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# InfoItemZipElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the infoitem (e.g. "31841/36639").|true|string||
|folder|The object ID of the related folder (e.g. "31841").|true|string||
|version|The version of the infoitem.|false|string||


# InfoItemsMovedResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of infoitem identifiers that could not be moved (due to a conflict).|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# InfoItemsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of infoitems. Each infoitem is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# JSlobData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the JSlob.|false|string||
|tree|The JSON object that is stored in the JSlob.|false|object||
|meta|A JSON object containing meta data.|false|object||


# JSlobsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array containing JSON configurations.|false|JSlobData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# JumpResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|JumpTokenData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# JumpTokenData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|token|The identifier of the token.|false|string||


# LoginResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|session|The session ID.|false|string||
|user|The username.|false|string||
|user_id|The user ID.|false|integer||
|context_id|The context ID.|false|integer||
|locale|The users locale (e.g. "en_US").|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailAccountData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The account identifier.|false|integer||
|name|The account's display name.|false|string||
|login|The login name.|false|string||
|password|The (optional) password.|false|string||
|mail_url|The mail server URL, e.g. "imap://imap.somewhere.com:143". **URL is preferred over single fields** (like `mail_server`, `mail_port`, etc.).|false|string||
|mail_server|The mail server's hostname or IP address.|false|string||
|mail_port|The mail server's port.|false|integer||
|mail_protocol|The mail server's protocol. **Always use basic protocol name.** E.g. use "imap" instead of "imaps".|false|string||
|mail_secure|Whether to establish a secure connection to mail server (SSL, TLS).|false|boolean||
|mail_starttls|Whether to establish a secure connection to mail server via STARTTLS (available since v7.8.2).|false|boolean||
|transport_url|The transport server URL, e.g. "smtp://smtp.somewhere.com:25". **URL is preferred over single fields** (like `transport_server`, `transport_port`, etc.).|false|string||
|transport_server|The transport server's hostname or IP address.|false|string||
|transport_port|The transport server's port.|false|integer||
|transport_protocol|The transport server's protocol. **Always use basic protocol name.** E.g. use "smtp" instead of "smtps".|false|string||
|transport_secure|Whether to establish a secure connection to transport server (SSL, TLS).|false|boolean||
|transport_login|The transport login. **Please see `transport_auth` for the handling of this field.**|false|string||
|transport_password|The transport password. **Please see `transport_auth` for the handling of this field.**|false|string||
|transport_auth|Specifies the source for mail transport (SMTP) credentials. Possible values: mail (signals to use the same credentials as given in associated mail store, e.g. IMAP or POP3), custom (signals that individual credentials are supposed to be used (fields `transport_login` and `transport_password` are considered), none (means the mail transport does not support any authentication mechansim). (available since v7.6.1)|false|string||
|transport_starttls|Whether to establish a secure connection to transport server via STARTTLS (available since v7.8.2).|false|boolean||
|primary_address|The user's primary address in account, e.g. "someone@somewhere.com".|false|string||
|spam_handler|The name of the spam handler used by account.|false|string||
|trash|The name of the default trash folder.|false|string||
|sent|The name of the default sent folder.|false|string||
|drafts|The name of the default drafts folder.|false|string||
|spam|The name of the default spam folder.|false|string||
|confirmed_spam|The name of the default confirmed-spam folder.|false|string||
|confirmed_ham|The name of the default confirmed-ham folder.|false|string||
|unified_inbox_enabled|Whether Unified INBOX is enabled.|false|boolean||
|trash_fullname|Path to default trash folder. Preferred over `trash`.|false|string||
|sent_fullname|Path to default sent folder. Preferred over `sent`.|false|string||
|drafts_fullname|Path to default drafts folder. Preferred over `drafts`.|false|string||
|spam_fullname|Path to default spam folder. Preferred over `spam`.|false|string||
|confirmed_span_fullname|Path to default confirmed-spam folder. Preferred over `confirmed_spam`.|false|string||
|confirmed_ham_fullname|Path to default confirmed-ham folder. Preferred over `confirmed_ham`.|false|string||
|pop3_refresh_rate|The interval in minutes the POP3 account is refreshed.|false|integer||
|pop3_expunge_on_quit|Whether POP3 messages shall be deleted on actual POP3 account after retrieval or not.|false|boolean||
|pop3_delete_write_through|If option `pop3_expunge_on_quite` is disabled, this field defines whether a deleted in local INBOX also deletes affected message in actual POP3 account.|false|boolean||
|pop3_storage|The name of POP3 storage provider, default is "mailaccount".|false|string||
|pop3_path|Path to POP3's virtual root folder in storage, default name of the POP3 account beside default folders.|false|string||
|personal|The customizable personal part of the email address.|false|string||
|reply_to|The customizable reply-to email address.|false|string||
|addresses|The comma-separated list of available email addresses including aliases (**only available for primary mail account**).|false|string||
|meta|Stores arbitrary JSON data as specified by client associated with the mail account.|false|string||
|archive|The name of the archive folder. **Currently not functional!**|false|string||
|archive_fullname|The full name of the archive folder. **Currently not functional!**|false|string||


# MailAccountDeletionResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array containing the identifiers of the mail accounts that were deleted.|false|integer array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailAccountResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailAccountData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailAccountUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailAccountData||
|warnings|An array of error objects that occurred during the creation of the account.|false|CommonResponse array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailAccountValidationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|A boolean if parameter `tree` is not specified indicating the validation result otherwise the folder
tree object (see **FolderData** model) extended by a field `subfolder_array` that contains possible subfolders.
In the tree case a value of `null` indicating a failed validation.
|false|object||
|warnings||false|CommonResponse||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailAccountsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of mail accounts. Each account is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailAckBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|from|The from email address.|false|string||
|folder|The ID of the folder where the mail is placed.|false|string||
|id|The ID of the mail.|false|string||


# MailAckResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailAttachment
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|Object ID (unique only inside the same message).|false|string||
|content_type|MIME type.|false|string||
|content|Content as text. Present only if easily convertible to text.|false|string||
|filename|Displayed filename (mutually exclusive with content).|false|string||
|size|Size of the attachment in bytes.|false|integer (int64)||
|disp|Attachment's disposition: null, inline, attachment or alternative.|false|string||


# MailConversationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|unreadCount||false|integer||
|thread|JSON array consisting of JSON objects, each representing a message in the conversation.|false|MailData array||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|false|integer||
|id|Object ID of the mail.|false|string||
|folder_id|Object ID of the parent folder.|false|string||
|attachment|Indicates whether this mail has attachments.|false|boolean||
|from|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|false|string array array||
|to|Each element is a two-element array (see the from field) specifying one receiver.|false|string array array||
|cc|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|false|string array array||
|bcc|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|false|string array array||
|subject|The mail's subject.|false|string||
|size|The size if the mail in bytes.|false|integer (int64)||
|sent_date|Date and time as specified in the mail by the sending client.|false|integer (int64)||
|received_date|Date and time as measured by the receiving server.|false|integer (int64)||
|flags|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|false|integer||
|level|Zero-based nesting level in a thread.|false|integer||
|disp_notification_to|Content of message's header "Disposition-Notification-To".|false|string||
|priority|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|false|integer||
|msg_ref|Message reference on reply/forward.|false|string||
|flag_seen|Special field to sort mails by seen status.|false|string||
|account_name|Message's account name.|false|string||
|account_id|Message's account identifier. Since v6.20.2.|false|integer||
|user|An array with user-defined flags as strings.|false|string array||
|headers|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|false|object||
|attachments|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|false|MailAttachment array||
|truncated|true/false if the mail content was trimmed. Since v7.6.1|false|boolean||
|source|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|false|string||
|cid|The value of the "Content-ID" header, if the header is present.|false|string||
|original_id|The original mail identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|original_folder_id|The original folder identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|content_type|The MIME type of the mail.|false|string||


# MailConversationsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of JSON objects each representing a conversation's root message.|false|MailConversationData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailCountResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The folder's mail count.|false|integer||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|false|integer||
|id|Object ID of the mail.|false|string||
|folder_id|Object ID of the parent folder.|false|string||
|attachment|Indicates whether this mail has attachments.|false|boolean||
|from|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|false|string array array||
|to|Each element is a two-element array (see the from field) specifying one receiver.|false|string array array||
|cc|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|false|string array array||
|bcc|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|false|string array array||
|subject|The mail's subject.|false|string||
|size|The size if the mail in bytes.|false|integer (int64)||
|sent_date|Date and time as specified in the mail by the sending client.|false|integer (int64)||
|received_date|Date and time as measured by the receiving server.|false|integer (int64)||
|flags|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|false|integer||
|level|Zero-based nesting level in a thread.|false|integer||
|disp_notification_to|Content of message's header "Disposition-Notification-To".|false|string||
|priority|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|false|integer||
|msg_ref|Message reference on reply/forward.|false|string||
|flag_seen|Special field to sort mails by seen status.|false|string||
|account_name|Message's account name.|false|string||
|account_id|Message's account identifier. Since v6.20.2.|false|integer||
|user|An array with user-defined flags as strings.|false|string array||
|headers|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|false|object||
|attachments|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|false|MailAttachment array||
|truncated|true/false if the mail content was trimmed. Since v7.6.1|false|boolean||
|source|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|false|string||
|cid|The value of the "Content-ID" header, if the header is present.|false|string||
|original_id|The original mail identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|original_folder_id|The original folder identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|content_type|The MIME type of the mail.|false|string||


# MailDestinationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder_id|The object ID of the destination folder.|false|string||


# MailDestinationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder_id|Object ID of the destination folder.|false|string||
|id|Object ID of the "new" mail.|false|string||


# MailDestinationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailDestinationData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailFilterAction
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|A string defining the object itself (e.g. "keep" or "discard").|false|string||
|to|A string containing where the mail should be redirected to (for redirect-command).|false|string||
|into|This string takes the object id of the destination mail folder (for move-command).|false|string||
|text|A string containing the reason why the mail is rejected (for reject-command) or a string containing the vacation text itself (for vacation-command).|false|string||
|days|The days for which a vacation text is returned (for vacation-command).|false|string||
|addresses|The addresses for which this vacation is responsible. That means for which addresses out of the aliases array of the user defining this filter, vacations will be sent.|false|string array||
|from|Support for the ":from" tag. Specifies the value of the from header for the auto-reply mail, e.g. Foo Bear <foo.bear@ox.io> (Since 7.8.1). The array of strings should be a simple JSONArray with length 2; the first element should include the personal part of the e-mail address and the second element the actual e-mail address. If only the e-mail address is available, that should be the only element of the array. (for vacation-command)|false|object||
|subject|The new subject for the returned message (can be left empty, when only adding RE:) (for vacation-command).|false|string||
|flags|An array containing the flags which should be added to the mail. A flag can either be a system flag or a user flag. System flags begin with a backslash and can be: "seen", "answered", "flagged", "deleted", "draft" or "recent". User flags begin with a dollar sign and can contain any ASCII characters between 0x21 ("!") and 0x7E ("~") (inclusive), except for 0x22 ("), 0x25 (%), 0x28 ((), 0x29 ()), 0x2A (*), 0x5C (backslash), 0x5D (]) and 0x7B ({). Mail color flags as used by OX are implemented by user flags of the form `$cl_n`, where "n" is a number between 1 and 10 (inclusive). (for addflags-command)|false|string array||
|message|The content of the notification message (for notify-command).|false|string||
|method|The method of the notification message, eg. `mailto:012345678@sms.gateway` (for notify-command).|false|string||
|keys|The public keys which should be used for encryption (for pgp-command).|false|string array||


# MailFilterConfigData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|tests|Array of available test-objects.|false|MailFilterConfigTest array||
|actioncommands|Array of available action commands.|false|string array||


# MailFilterConfigResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailFilterConfigData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailFilterConfigTest
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|test|The name of the test, see [Possible tests](#possible-tests).|false|string||
|comparison|An array of the valid comparison types for this test, see [Possible comparisons](#possible-comparisons).|false|string array||


# MailFilterCreationResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The id of the newly created rule.|false|integer||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailFilterDeletionBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the rule that shall be deleted.|false|integer||


# MailFilterNotTest

A test object which result will be negated.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The name of the test command, see [Possible tests](#possible-tests).|false|string||
|comparison|The comparison type, see [Possible comparisons](#possible-comparisons).|false|string||
|headers|An array containing the header fields (for address-, envelope- and header-test).|false|string array||
|values|An array containing the value for the header fields or the values for the body. The test will be true if any of the strings matches (for address-, envelope-, header-test and body-test).|false|string array||
|size|The size in bytes (for size-test).|false|integer (int64)||
|datepart|Type of the comparison, which can be "date", "weekday" or "time" (available since v7.6.1) (for currentdate-test).|false|string||
|datevalue|Contains the corresponding value to the datepart. For "date" and "time" this will be an array of "Date" (unix timestamp). For "weekday", it will be an array of integers ranging from 0 (sunday) to 6 (saturday) reflecting the equivalent weekday (for currentdate-test).|false|integer (int64) array||
|extensionskey|The [extension key](#possible-extensions) (for body-test).|false|string||
|extensionsvalue|A value for the given key. If the key has no value the value given here is ignored (for body-test).|false|string||


# MailFilterRule
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|A unique identifier of the rule (once created must not be changed).|false|integer||
|position|The position inside the mail filter list (starts with 0).|false|integer||
|rulename|A name describing the rule, can be empty but must not contain a line break.|false|string||
|active|If this rule is active or not.|false|boolean||
|flags|An array containing flags which are set on this rule. Each flag can only contain the following characters: 1-9 a-z A-Z. Currently 3 flags are reserved here: "spam" which marks the default spam rule, "vacation" which marks the vacation rules and "autoforward" which marks an autoforwarding rule.|false|string array||
|test||false|MailFilterTest||
|actioncmds|An array of action commands.|false|MailFilterAction array||
|text|If this rule cannot be read in this string is filled containing the whole lines of this command.|false|string||
|errormsg|If this rule cannot be read in this string is filled containing a message why, or what part of the rule isn't known.|false|string||


# MailFilterRulesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailFilterRule array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailFilterScriptResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The mail filter script.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailFilterTest
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|test||false|MailFilterNotTest||
|id|The name of the test command, see [Possible tests](#possible-tests).|false|string||
|comparison|The comparison type, see [Possible comparisons](#possible-comparisons).|false|string||
|headers|An array containing the header fields (for address-, envelope- and header-test).|false|string array||
|values|An array containing the value for the header fields or the values for the body. The test will be true if any of the strings matches (for address-, envelope-, header-test and body-test).|false|string array||
|size|The size in bytes (for size-test).|false|integer (int64)||
|datepart|Type of the comparison, which can be "date", "weekday" or "time" (available since v7.6.1) (for currentdate-test).|false|string||
|datevalue|Contains the corresponding value to the datepart. For "date" and "time" this will be an array of "Date" (unix timestamp). For "weekday", it will be an array of integers ranging from 0 (sunday) to 6 (saturday) reflecting the equivalent weekday (for currentdate-test).|false|integer (int64) array||
|extensionskey|The [extension key](#possible-extensions) (for body-test).|false|string||
|extensionsvalue|A value for the given key. If the key has no value the value given here is ignored (for body-test).|false|string||


# MailHeadersResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The (formatted) message headers as plain text.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailImportResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of JSON objects each describing the folder ID and object ID of one imported mail.|false|MailDestinationData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the mail.|true|string||
|folder|The object ID of the related folder.|true|string||


# MailReplyData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|msgref|Indicates the ID of the referenced original mail.|false|string||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|false|integer||
|id|Object ID of the mail.|false|string||
|folder_id|Object ID of the parent folder.|false|string||
|attachment|Indicates whether this mail has attachments.|false|boolean||
|from|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|false|string array array||
|to|Each element is a two-element array (see the from field) specifying one receiver.|false|string array array||
|cc|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|false|string array array||
|bcc|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|false|string array array||
|subject|The mail's subject.|false|string||
|size|The size if the mail in bytes.|false|integer (int64)||
|sent_date|Date and time as specified in the mail by the sending client.|false|integer (int64)||
|received_date|Date and time as measured by the receiving server.|false|integer (int64)||
|flags|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|false|integer||
|level|Zero-based nesting level in a thread.|false|integer||
|disp_notification_to|Content of message's header "Disposition-Notification-To".|false|string||
|priority|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|false|integer||
|msg_ref|Message reference on reply/forward.|false|string||
|flag_seen|Special field to sort mails by seen status.|false|string||
|account_name|Message's account name.|false|string||
|account_id|Message's account identifier. Since v6.20.2.|false|integer||
|user|An array with user-defined flags as strings.|false|string array||
|headers|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|false|object||
|attachments|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|false|MailAttachment array||
|truncated|true/false if the mail content was trimmed. Since v7.6.1|false|boolean||
|source|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|false|string||
|cid|The value of the "Content-ID" header, if the header is present.|false|string||
|original_id|The original mail identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|original_folder_id|The original folder identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|content_type|The MIME type of the mail.|false|string||


# MailReplyResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailReplyData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MailData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailSourceResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The complete message source as plain text.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailUpdateBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder_id|The object ID of the destination folder (if the mail shall be moved).|false|string||
|color_label|The color number between 0 and 10.|false|integer||
|flags|A set of flags to add or remove. Note: Flags for "recent" (8) and "user" (64) are ignored.|false|integer||
|value|Use true to add the flags specified by flags (logical OR) and false to remove them (logical AND with the inverted value).|false|boolean||
|set_flags|A set of flags to add. Note: Flags for "recent" (8) and "user" (64) are ignored (available since SP5 v6.10).|false|integer (int64)||
|clear_flags|A set of flags to remove. Note: Flags for "recent" (8) and "user" (64) are ignored (available since SP5 v6.10).|false|integer||


# MailUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailsAllSeenResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|boolean||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailsCleanUpResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array with IDs of objects that could not be processed.|false|string array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MailsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of mails. Each mail is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingAccountData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|Identifier of the messaging account.|false|integer||
|messagingService|The messaging service ID of the messaging service this account belongs to.|false|string||
|displayName|User chosen string to identify a given account. Will also be translated into the folder name of the folder representing the accounts content.|false|string||
|configuration|The configuration data according to the `formDescription` of the relevant messaging service.|false|object||


# MessagingAccountResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MessagingAccountData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingAccountUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The response value.|false|integer||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingAccountsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array containing JSON objects representing messaging accounts.|false|MessagingAccountData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingFormDescription
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|widget|The name of the widget.|false|string||
|name|The name of the field.|false|string||
|displayName|The display name of the field.|false|string||
|mandatory|Indicates whether the field is mandatory.|false|boolean||
|options|A list of available options in the field.|false|object array||
|defaultValue|Can contain a default value.|false|object||


# MessagingMessageData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the message. Only unique in the given folder.|false|string||
|folder|The folder ID.|false|string||
|threadLevel|The nesting level of this message according to the conversation it's belonged to. May not be set.|false|integer||
|flags|Bitmask showing the state of this message. The same as in the module "mail".|false|integer||
|receivedDate|The time this message was received.|false|integer (int64)||
|colorLabel|An arbitrary number marking the message in a certain color. The same as the color label common to all groupware objects.|false|integer||
|user|An array of strings representing user flags.|false|string array||
|size|The size of the message in bytes.|false|integer (int64)||
|picture|The URL to a picture for this message.|false|string||
|url|A link to the messages origin currently used in RSS messages.|false|string||
|sectionId|The section ID of a certain message part, if the content-type is `multipart/*`.|false|string||
|headers|A JSON object of header data. Usually the value is either a string or an array (if it has more than one value). Certain headers are rendered as more complex structures.|false|object||
|body|A JSON object representing the content of the message.|false|object||


# MessagingMessageResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MessagingMessageData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingMessageUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The response value.|false|integer||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingMessagesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of messages. Each message is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingServiceData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the messaging service. This is usually a string in reverse domain name notation, like "com.openexchange.messaging.twitter".|false|string||
|displayName|Human-readable display name of the service.|false|string||
|formDescription|An array of dynamic form fields. Same as in PubSub.|false|MessagingFormDescription array||
|messagingActions|An array representing a dynamic set of actions that are possible with messages of this service.|false|string array||


# MessagingServiceResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|MessagingServiceData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# MessagingServicesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array containing JSON objects representing messaging services.|false|MessagingServiceData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthAccountData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The numeric identifier of the OAuth account.|false|integer||
|displayName|The account's display name.|false|string||
|serviceId|The identifier of the associated service meta data, e.g. "com.openexchange.oauth.twitter".|false|string||
|token|The token.|false|string||
|secret|The token secret.|false|string||


# OAuthAccountDeletionResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Indicates whether the the account was deleted successfully.|false|boolean||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthAccountInteraction
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|authUrl|The numeric identifier of the OAuth account.|false|string||
|type|The interaction type name, which can be "outOfBand" or "callback".|false|string||
|token|The token.|false|string||
|uuid|The UUID for this OAuth interaction.|false|string||


# OAuthAccountInteractionResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|OAuthAccountInteraction||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthAccountResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|OAuthAccountData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthAccountUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Indicates whether the the account was updated successfully.|false|boolean||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthAccountsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of OAuth account objects.|false|OAuthAccountData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthClientData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The client's ID.|false|string||
|name|The client's/service's name.|false|string||
|description|A description of the client.|false|string||
|website|A URL to the client's website.|false|string||
|icon|A URL or path to obtain the client's icon via the image module.|false|string||


# OAuthGrantData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|client||false|OAuthClientData||
|scopes|A mapping from scope tokens to translated, human-readable descriptions for every scope that was granted to the external service (example: {"read_contacts":"See all your contacts"}).|false|object||
|date|The time when the access was granted.|false|integer (int64)||


# OAuthGrantsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array containing one object for every granted access.|false|OAuthGrantData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthServiceMetaData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the service meta data, e.g. "com.openexchange.oauth.twitter".|false|string||
|displayName|The service's display name.|false|string||


# OAuthServiceResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|OAuthServiceMetaData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# OAuthServicesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array with OAuth service meta data.|false|OAuthServiceMetaData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# PasswordChangeBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|old_password|The user's current password or `null` if the password wasn't set before (especially for guest users).|false|string||
|new_password|The new password the user wants to set or `null` to remove the password (especially for guest users).|false|string||


# QuotaData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|quota|Represents the maximum storage (-1 represents an unlimited quota).|false|integer (int64)||
|use|Represents the used storage.|false|integer (int64)||


# QuotaResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|QuotaData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# QuotasResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Dependent on the request parameters: the payload may be a JSON object containing the quota modules as
fields that represent JSON objects itself with the properties "display_name" and "accounts" (array of account data objects)
or it may be a JSON array of account data objects if the parameter "module" specifies a certain quota module.
|false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ReminderData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the reminder.|false|integer||
|target_id|The object ID of the target this reminder is attached to.|false|integer||
|folder|The ID of the folder through that the object can be read.|false|integer||
|alarm|The time of the alarm.|false|integer (int64)||
|module|The module of the reminder's target object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|false|integer||
|server_time|The time on the server.|false|integer (int64)||
|user_id|The ID of the user.|false|integer||
|last_modified|The last modification timestamp of the reminder.|false|integer (int64)||
|recurrence_position|The recurrence position for series appointments or 0 if no series.|false|integer||


# ReminderListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the reminder.|false|integer||


# ReminderResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ReminderData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ReminderUpdateBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|alarm|The new time of the alarm.|false|integer (int64)||


# RemindersResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ReminderData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ResourceData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The resource ID.|false|integer||
|display_name|Display name of the resource.|false|string||
|name|Internal name with character restrictions.|false|string||
|mailaddress|Email address of the resource.|false|string||
|availability|Can be false to mark the resource currently unavailable.|false|boolean||
|description|The description of the resource.|false|string||
|last_modified|Date and time of the last modification.|false|string||
|last_modified_utc|Timestamp of the last modification.|false|string||


# ResourceListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a resource.|false|integer||


# ResourceResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ResourceData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ResourceSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find resources. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||


# ResourceUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of a newly created rsource.|false|integer||


# ResourceUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ResourceUpdateData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ResourceUpdatesData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|new|Array of new resource objects.|false|ResourceData array||
|modified|Array of modified resource objects.|false|ResourceData array||
|deleted|Array of deleted resource objects.|false|ResourceData array||


# ResourceUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ResourceUpdatesData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ResourcesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of resource objects.|false|ResourceData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# SendMailData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|infostore_ids|JSON array of infostore document ID(s) that ought to be appended to the mail as attachments.|false|string array||
|msgref|Indicates the ID of the referenced original mail.|false|string||
|sendtype|Indicates the type of the meessage: 0 (normal new mail), 1 (a reply mail, field "msgref" must be present), 2 (a forward mail, field "msgref" must be present), 3 (draft edit operation, field "msgref" must be present in order to delete previous draft message since e.g. IMAP does not support changing/replacing a message but requires a delete-and-insert sequence), 4 (transport of a draft mail, field "msgref" must be present), 6 (signals that user intends to send out a saved draft message and expects the draft message (referenced by "msgref" field) being deleted after successful transport).|false|integer||
|vcard|The user's VCard.|false|integer||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|false|integer||
|id|Object ID of the mail.|false|string||
|folder_id|Object ID of the parent folder.|false|string||
|attachment|Indicates whether this mail has attachments.|false|boolean||
|from|Each element is a two-element array specifying one sender (address). The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|false|string array array||
|to|Each element is a two-element array (see the from field) specifying one receiver.|false|string array array||
|cc|Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|false|string array array||
|bcc|Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|false|string array array||
|subject|The mail's subject.|false|string||
|size|The size if the mail in bytes.|false|integer (int64)||
|sent_date|Date and time as specified in the mail by the sending client.|false|integer (int64)||
|received_date|Date and time as measured by the receiving server.|false|integer (int64)||
|flags|Various system flags. A sum of zero or more of following values (see javax.mail.Flags.Flag for details): 1 (answered), 2 (deleted), 4 (draft), 8 (flagged), 16 (recent), 32 (seen), 64 (user), 128 (spam), 256 (forwarded).|false|integer||
|level|Zero-based nesting level in a thread.|false|integer||
|disp_notification_to|Content of message's header "Disposition-Notification-To".|false|string||
|priority|Value of message's X-Priority header: 0 (no priority), 5 (very low), 4 (low), 3 (normal), 2 (high), 1 (very high).|false|integer||
|msg_ref|Message reference on reply/forward.|false|string||
|flag_seen|Special field to sort mails by seen status.|false|string||
|account_name|Message's account name.|false|string||
|account_id|Message's account identifier. Since v6.20.2.|false|integer||
|user|An array with user-defined flags as strings.|false|string array||
|headers|A map with fields for every non-standard header. The header name is the field name. The header value is the value of the field as string.|false|object||
|attachments|Each element is an attachment. The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|false|MailAttachment array||
|truncated|true/false if the mail content was trimmed. Since v7.6.1|false|boolean||
|source|RFC822 source of the mail. Only present for "?action=get&attach_src=true".|false|string||
|cid|The value of the "Content-ID" header, if the header is present.|false|string||
|original_id|The original mail identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|original_folder_id|The original folder identifier (e.g. if fetched from "virtual/all" folder).|false|string||
|content_type|The MIME type of the mail.|false|string||


# ShareLinkData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|url|The link to share (read-only).|false|string||
|entity|The identifier of the anonymous user entity for the share (read-only).|false|integer||
|is_new|Whether the share link is new, i.e. it has been created by the `/share/management?action=getLink` request, or if it already existed (read-only).|false|boolean||
|expiry_date|The end date / expiration time after which the share link is no longer accessible.|false|integer (int64)||
|password|An additional secret / pin number an anonymous user needs to enter when accessing the share.|false|string||
|meta|Can be used by the client to save arbitrary JSON data along with the share.|false|object||


# ShareLinkResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|ShareLinkData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ShareLinkSendBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|recipients|An array that lists the recipients. Each element is itself a two-element array specifying one recipient. The first element represents the personal name, the second element is the email address.|false|string array array||
|message|Can contain an optional custom message.|false|string||
|module|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|false|string||
|folder|The folder identifier.|false|string||
|item|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|false|string||


# ShareLinkSendResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|warnings|Can contain possible warnings during sending of the notifications.|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# ShareLinkUpdateBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|url|The link to share (read-only).|false|string||
|entity|The identifier of the anonymous user entity for the share (read-only).|false|integer||
|is_new|Whether the share link is new, i.e. it has been created by the `/share/management?action=getLink` request, or if it already existed (read-only).|false|boolean||
|expiry_date|The end date / expiration time after which the share link is no longer accessible.|false|integer (int64)||
|password|An additional secret / pin number an anonymous user needs to enter when accessing the share.|false|string||
|meta|Can be used by the client to save arbitrary JSON data along with the share.|false|object||
|module|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|false|string||
|folder|The folder identifier.|false|string||
|item|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|false|string||


# ShareTargetData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|module|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|false|string||
|folder|The folder identifier.|false|string||
|item|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|false|string||


# SingleRequest

Contains all currently available (resp. possible) parameters that could be specified to perform a request in the multiple module except `action`, `module`, and `data` which are part of the actual request itself.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|module|The name of the request's module like "mail", "folders", etc.|true|string||
|action|The name of the request's action like "all", "list", etc.|true|string||
|data|The request's body as a JSON object.|false|object||
|columns|The "columns" parameter of a request.|false|string||
|tree|The "tree" parameter of a request.|false|string||
|allowed_modules|The "allowed_modules" parameter of a request.|false|string||
|parent|The "parent" parameter of a request.|false|string||
|all|The "all" parameter of a request.|false|integer||
|errorOnDuplicateName|The "errorOnDuplicateName" parameter of a request.|false|boolean||
|id|The "id" parameter of a request.|false|string||
|timestamp|The "timestamp" parameter of a request.|false|integer (int64)||
|ignore|The "ignore" parameter of a request.|false|string||
|cascadePermissions|The "cascadePermissions" parameter of a request.|false|string||
|hardDelete|The "hardDelete" parameter of a request.|false|boolean||
|content_type|The "content_type" parameter of a request.|false|string||
|sort|The "sort" parameter of a request.|false|string||
|order|The "order" parameter of a request.|false|string||
|folder|The "folder" parameter of a request.|false|string||
|start|The "start" parameter of a request.|false|integer (int64)||
|end|The "end" parameter of a request.|false|integer (int64)||
|email|The "email" parameter of a request.|false|string||
|query|The "query" parameter of a request.|false|string||
|left_hand_limit|The "left_hand_limit" parameter of a request.|false|integer||
|right_hand_limit|The "right_hand_limit" parameter of a request.|false|integer||
|recurrence_master|The "recurrence_master" parameter of a request.|false|boolean||
|showPrivate|The "showPrivate" parameter of a request.|false|boolean||
|occurrence|The "occurrence" parameter of a request.|false|string||
|type|The "type" parameter of a request.|false|integer||
|limit|The "limit" parameter of a request.|false|integer||
|uid|The "uid" parameter of a request.|false|string||
|includeSent|The "includeSent" parameter of a request.|false|boolean||
|headers|The "headers" parameter of a request.|false|string||
|message_id|The "message_id" parameter of a request.|false|string||
|edit|The "edit" parameter of a request.|false|integer||
|hdr|The "hdr" parameter of a request.|false|integer||
|src|The "src" parameter of a request.|false|integer||
|save|The "save" parameter of a request.|false|integer||
|view|The "view" parameter of a request.|false|string||
|unseen|The "unseen" parameter of a request.|false|boolean||
|max_size|The "max_size" parameter of a request.|false|integer||
|attach_src|The "attach_src" parameter of a request.|false|boolean||
|attachment|The "attachment" parameter of a request.|false|string||
|cid|The "cid" parameter of a request.|false|string||
|filter|The "filter" parameter of a request.|false|integer||
|lineWrapAfter|The "lineWrapAfter" parameter of a request.|false|integer||
|flags|The "falgs" parameter of a request.|false|integer||
|force|The "force" parameter of a request.|false|boolean||
|setFrom|The "setFrom" parameter of a request.|false|boolean||
|version|The "version" parameter of a request.|false|integer||
|recursive|The "recursive" parameter of a request.|false|string||
|diff|The "diff" parameter of a request.|false|integer (int64)||
|attached|The "attached" parameter of a request.|false|integer||
|accountId|The "accountId" parameter of a request.|false|integer||
|password|The "password" parameter of a request.|false|string||
|force_secure|The "force_secure" parameter of a request.|false|string||
|name|The "name" parameter of a request.|false|string||
|setIfAbsent|The "setIfAbsent" parameter of a request.|false|string||
|serviceId|The "serviceId" parameter of a request.|false|string||
|oauth_token|The "oauth_token" parameter of a request.|false|string||
|uuid|The "uuid" parameter of a request.|false|string||
|oauth_verifier|The "oauth_verifier" parameter of a request.|false|string||
|displayName|The "displayName" parameter of a request.|false|string||
|client|The "client" parameter of a request.|false|string||
|participant|The "participant" parameter of a request.|false|string||
|from|The "from" parameter of a request.|false|integer (int64)||
|until|The "until" parameter of a request.|false|integer (int64)||
|merged|The "merged" parameter of a request.|false|boolean||
|messagingService|The "messagingService" parameter of a request.|false|string||
|peek|The "peek" parameter of a request.|false|string||
|recipients|The "recipients" parameter of a request.|false|string||
|messageAction|The "messageAction" parameter of a request.|false|string||
|attachmentid|The "attachmentid" parameter of a request.|false|string||
|provider|The "provider" parameter of a request.|false|string||
|timezone|The "timezone" parameter of a request.|false|string||
|internal_userid|The "internal_userid" parameter of a request.|false|integer||
|userid|The "userid" parameter of a request.|false|integer||
|user_id|The "user_id" parameter of a request.|false|integer||
|email1|The "email1" parameter of a request.|false|string||
|email2|The "email2" parameter of a request.|false|string||
|email3|The "email3" parameter of a request.|false|string||
|system|The "system" parameter of a request.|false|string||
|filestorageService|The "filestorageService" parameter of a request.|false|string||


# SingleResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The data of a request that was processed with the multiple module.|false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# SnippetAttachment
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the attachment.|false|string||
|filename|The file name of the attachment.|false|string||
|mimetype|The MIME type of the attachment.|false|string||
|contentid|The content ID of the attachment.|false|string||
|size|The size of the attachment in bytes.|false|integer (int64)||


# SnippetAttachmentListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of an attachment.|false|string||


# SnippetData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the snippet.|false|string||
|accountid|The identifier of the account.|false|integer||
|createdby|The user ID of the creator.|false|integer||
|displayname|The display name of the snippet.|false|string||
|type|The type of the snippet, like "signature".|false|string||
|content|Contains the snippet's content.|false|string||
|module|The module identifier, like "com.openexchange.mail".|false|string||
|shared|The shared flag.|false|boolean||
|misc|Contains miscellaneous data as JSON object.|false|object||
|props|Contains custom properties as JSON object.|false|object||
|files|An array of attachments.|false|SnippetAttachment array||


# SnippetResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|SnippetData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# SnippetUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The ID of the new snippet.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# SnippetsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array of snippet objects.|false|SnippetData array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# TaskConfirmation
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|type|Type of participant: 0 (user), 5 (external user).|false|integer||
|mail|Email address of external participant.|false|string||
|display_name|Display name of external participant.|false|string||
|status|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|false|integer||
|message|Confirm message of the participant.|false|string||


# TaskConfirmationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|confirmation|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|false|integer||
|confirmmessage|The confirmation message or comment.|false|string||


# TaskData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|status|Status of the task: 1 (not started), 2 (in progress), 3 (done), 4 (waiting), 5 (deferred).|false|integer||
|percent_completed|How much of the task is completed. An integer number between 0 and 100.|false|integer||
|actual_costs|A monetary attribute to store actual costs of a task. Allowed values must be in the range -9999999999.99 and 9999999999.99.|false|number (double)||
|actual_duration|Actual duration of the task, e.g. in minutes.|false|string||
|after_complete|Deprecated. Only present in AJAX interface. Value will not be stored on OX server.|false|integer (int64)||
|billing_information|Billing information of the task.|false|string||
|target_costs|A monetary attribute to store target costs of a task. Allowed values must be in the range -9999999999.99 and 9999999999.99.|false|number (double)||
|target_duration|Target duration of the task, e.g. in minutes.|false|string||
|priority|The priority of the task: 1 (low), 2 (medium), 3 (high).|false|integer||
|currency|The currency, e.g. "EUR".|false|string||
|trip_meter|The trip meter.|false|string||
|companies|Companies.|false|string||
|date_completed||false|integer (int64)||
|start_time|Inclusive start as Date for whole day tasks and Time for normal tasks.|false|integer (int64)||
|end_time|Exclusive end as Date for whole day tasks and as Time for normal tasks.|false|integer (int64)||
|title|Short description.|false|string||
|start_date|Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time).|false|integer (int64)||
|end_date|Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time).|false|integer (int64)||
|note|Long description.|false|string||
|alarm|Specifies when to notify the participants as the number of minutes before the start of the appointment (-1 for "no alarm"). For tasks, the Time value specifies the absolute time when the user should be notified.|false|integer (int64)||
|recurrence_type|Specifies the type of the recurrence for a task sequence: 0 (none, single event), 1(daily), 2 (weekly), 3 (monthly), 4 (yearly).|false|integer||
|days|Specifies which days of the week are part of a sequence. The value is a bitfield with bit 0 indicating sunday, bit 1 indicating monday and so on. May be present if recurrence_type > 1. If allowed but not present, the value defaults to 127 (all 7 days).|false|integer||
|day_in_month|Specifies which day of a month is part of the sequence. Counting starts with 1. If the field "days" is also present, only days selected by that field are counted. If the number is bigger than the number of available days, the last available day is selected. Present if and only if recurrence_type > 2.|false|integer||
|month|Month of the year in yearly sequencies. 0 represents January, 1 represents February and so on. Present if and only if recurrence_type = 4.|false|integer||
|interval|Specifies an integer multiplier to the interval specified by recurrence_type. Present if and only if recurrence_type > 0. Must be 1 if recurrence_type = 4.|false|integer||
|until|Inclusive end date of a sequence. May be present only if recurrence_type > 0. The sequence has no end date if recurrence_type > 0 and this field is not present. Note: since this is a Date, the entire day after the midnight specified by the value is included.|false|integer (int64)||
|notification|If true, all participants are notified of any changes to this object. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|false|boolean||
|participants|Each element identifies a participant, user, group or booked resource.|false|TaskParticipant array||
|users|Each element represents a participant. User groups are resolved and are represented by their members. Any user can occur only once.|false|TaskUser array||
|occurrences|Specifies how often a recurrence should appear. May be present only if recurrence_type > 0.|false|integer||
|uid|Can only be written when the object is created. Internal and external globally unique identifier of the appointment or task. Is used to recognize appointments within iCal files. If this attribute is not written it contains an automatic generated UUID.|false|string||
|organizer|Contains the email address of the appointment organizer which is not necessarily an internal user. Not implemented for tasks.|false|string||
|sequence|iCal sequence number. Not implemented for tasks. Must be incremented on update. Will be incremented by the server, if not set.|false|integer||
|confirmations|Each element represents a confirming participant. This can be internal and external user. Not implemented for tasks.|false|TaskConfirmation array||
|organizerId|Contains the userIId of the appointment organizer if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|false|integer||
|principal|Contains the email address of the appointment principal which is not necessarily an internal user. Not implemented for tasks (introduced with 6.20.1).|false|string||
|principalId|Contains the userIId of the appointment principal if it is an internal user. Not implemented for tasks (introduced with 6.20.1).|false|integer||
|full_time|True if the event is a whole day appointment or task, false otherwise.|false|boolean||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# TaskDeletionsResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|An array with object IDs of tasks which were modified after the specified timestamp and were therefore not deleted.|false|string array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# TaskListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the task.|true|string||
|folder|The object ID of the related folder.|true|string||


# TaskParticipant
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|User ID.|false|integer||
|type|Type of participant: 1 (user), 2 (user group), 3 (resource), 4 (resource group), 5 (external user)|false|integer||
|mail|Mail address of an external participant.|false|string||


# TaskResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|TaskData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# TaskSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find tasks. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|true|string||
|folder|Defines the folder to search for tasks in. If this is omitted in all task folders will be searched.|false|string||
|start|Inclusive start date for a time range the tasks should end in. If start is omitted end is ignored.|false|integer (int64)||
|end|Exclusive end date for a time range the tasks should end in. If this parameter is omitted the time range has an open end.|false|integer (int64)||


# TaskUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a newly created task.|false|string||


# TaskUpdateResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|TaskUpdateData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# TaskUpdatesResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of tasks.|false|object array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# TaskUser
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|User ID. Confirming for other users only works for appointments and not for tasks.|false|integer||
|display_name|Displayable name of the participant.|false|string||
|confirm|0 (none), 1 (accepted), 2 (declined), 3 (tentative)|false|integer||
|confirmmessage|Confirm message of the participant.|false|string||


# TasksResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of tasks. Each task is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# TokenLoginResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|serverToken|The token generated by the server.|false|string||
|jsessionid||false|string||
|user|The username.|false|string||
|user_id|The user ID.|false|integer||
|url|The URL of the redirect to the web UI.|false|string||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# TokensData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|session|The session ID.|false|string||
|user|The username.|false|string||
|user_id|The user ID.|false|integer||
|context_id|The context ID.|false|integer||
|locale|The users locale (e.g. "en_US").|false|string||


# TokensResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|TokensData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# UserAttribute
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|The name of the attribute.|false|string||
|value|The value of the attribute.|false|string||


# UserAttributeResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|UserAttribute||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# UserAttributionResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Indicates whether the attribute could be set.|false|boolean||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# UserData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|aliases|The user's aliases.|false|string array||
|timezone|The time zone.|false|string||
|locale|The name of user's entire locale, with, language, country and variant separated by underbars (e.g. "en", "de_DE").|false|string||
|groups|The IDs of user's groups.|false|integer array||
|contact_id|The contact ID of the user.|false|string||
|login_info|The user's login information.|false|string||
|guest_created_by|The ID of the user who has created this guest in case this user represents a guest user. 0 represents regular users. (preliminary, available since v7.8.0)|false|integer||
|uid|Can only be written when the object is created. Internal and external globally unique identifier of the contact. Is used to recognize contacts within vCard files. If this attribute is not written it contains an automatic generated UUID.|false|string||
|display_name|The display name.|false|string||
|first_name|The given name.|false|string||
|last_name|The sur name.|false|string||
|second_name|The middle name.|false|string||
|suffix|The suffix.|false|string||
|title|The title.|false|string||
|street_home|The street of the home address.|false|string||
|postal_code_home|The postal code of the home address.|false|string||
|city_home|The city of the home address.|false|string||
|state_home|The state of the home address.|false|string||
|country_home|The country of the home address.|false|string||
|birthday|The date of birth.|false|integer (int64)||
|marital_status|The marital status.|false|string||
|number_of_children|The number of children.|false|string||
|profession|The profession.|false|string||
|nickname|The nickname.|false|string||
|spouse_name|The name of the spouse.|false|string||
|anniversary|The anniversary.|false|integer (int64)||
|note|A note.|false|string||
|department|The department.|false|string||
|position|The position.|false|string||
|employee_type|The type of the employee.|false|string||
|room_number|The room number.|false|string||
|street_business|The street of the business address.|false|string||
|postal_code_business|The postal code of the business address.|false|string||
|city_business|The city of the business address.|false|string||
|state_business|The state of the business address.|false|string||
|country_business|The country of the business address.|false|string||
|user_id|The internal user id.|false|integer||
|number_of_employees|The number of employees.|false|string||
|sales_volume|The sales volume.|false|string||
|tax_id|The tax id.|false|string||
|commercial_register|The commercial register.|false|string||
|branches|The branches.|false|string||
|business_category|The business category.|false|string||
|info|An information.|false|string||
|manager_name|The manager's name.|false|string||
|assistant_name|The assistant's name.|false|string||
|street_other|The street of another address.|false|string||
|postal_code_other|The postal code of another address.|false|string||
|city_other|The city of another address.|false|string||
|state_other|The state of another address.|false|string||
|country_other|The country of another address.|false|string||
|telephone_business1|The business telephone number 1.|false|string||
|telephone_business2|The business telephone number 2.|false|string||
|fax_business|The business fax number.|false|string||
|telephone_callback|The callback telephone number.|false|string||
|telephone_car|The car telephone number.|false|string||
|telephone_company|The company telephone number.|false|string||
|telephone_home1|The home telephone number 1.|false|string||
|telephone_home2|The home telephone number 2.|false|string||
|fax_home|The home fax number.|false|string||
|cellular_telephone1|The cellular telephone number 1.|false|string||
|cellular_telephone2|The cellular telephone number 2.|false|string||
|telephone_other|The other telephone number.|false|string||
|fax_other|The other fax number.|false|string||
|email1|The email address 1.|false|string||
|email2|The email address 2.|false|string||
|email3|The email address 3.|false|string||
|url|The url address or homepage.|false|string||
|telephone_isdn|The ISDN telephone number.|false|string||
|telephone_pager|The pager telephone number.|false|string||
|telephone_primary|The primary telephone number.|false|string||
|telephone_radio|The radio telephone number.|false|string||
|telephone_telex|The telex telephone number.|false|string||
|telephone_ttytdd|The TTY/TDD telephone number.|false|string||
|instant_messenger1|The instant messenger address 1.|false|string||
|instant_messenger2|The instant messenger address 2.|false|string||
|telephone_ip|The IP telephone number.|false|string||
|telephone_assistant|The assistant telephone number.|false|string||
|company|The company name.|false|string||
|image1||false|string||
|image1_content_type|The content type of the image (like "image/png").|false|string||
|image1_url|The url to the image.|false|string||
|number_of_images|The number of images.|false|integer||
|image_last_modified|The last modification of the image.|false|integer (int64)||
|distribution_list|If this contact is a distribution list, then this field is an array of objects. Each object describes a member of the list.|false|DistributionListMember array||
|number_of_distribution_list|The number of objects in the distribution list.|false|integer||
|mark_as_distributionlist||false|boolean||
|file_as|The file name.|false|string||
|default_address|The default address.|false|integer||
|useCount|In case of sorting purposes the column 609 is also available, which places global address book contacts at the beginning of the result. If 609 is used, the order direction (ASC, DESC) is ignored.|false|integer||
|yomiFirstName|Kana based representation for the First Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|false|string||
|yomiLastName|Kana based representation for the Last Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|false|string||
|yomiCompany|Kana based representation for the Company. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)|false|string||
|addressHome|Support for Outlook 'home' address field. (since 6.20.1)|false|string||
|addressBusiness|Support for Outlook 'business' address field. (since 6.20.1)|false|string||
|addressOther|Support for Outlook 'other' address field. (since 6.20.1)|false|string||
|userfield01|Dynamic Field 1.|false|string||
|userfield02|Dynamic Field 2.|false|string||
|userfield03|Dynamic Field 3.|false|string||
|userfield04|Dynamic Field 4.|false|string||
|userfield05|Dynamic Field 5.|false|string||
|userfield06|Dynamic Field 6.|false|string||
|userfield07|Dynamic Field 7.|false|string||
|userfield08|Dynamic Field 8.|false|string||
|userfield09|Dynamic Field 9.|false|string||
|userfield10|Dynamic Field 10.|false|string||
|userfield11|Dynamic Field 11.|false|string||
|userfield12|Dynamic Field 12.|false|string||
|userfield13|Dynamic Field 13.|false|string||
|userfield14|Dynamic Field 14.|false|string||
|userfield15|Dynamic Field 15.|false|string||
|userfield16|Dynamic Field 16.|false|string||
|userfield17|Dynamic Field 17.|false|string||
|userfield18|Dynamic Field 18.|false|string||
|userfield19|Dynamic Field 19.|false|string||
|userfield20|Dynamic Field 20.|false|string||
|id|Object ID.|false|string||
|created_by|User ID of the user who created this object.|false|string||
|modified_by|User ID of the user who last modified this object.|false|string||
|creation_date|Date and time of creation.|false|integer (int64)||
|last_modified|Date and time of the last modification.|false|integer (int64)||
|folder_id|Object ID of the parent folder.|false|string||
|categories|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|false|string||
|private_flag|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|false|boolean||
|color_label|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|false|integer||
|number_of_attachments|Number of attachments.|false|integer||
|lastModifiedOfNewestAttachmentUTC|Timestamp of the newest attachment written with UTC time zone.|false|integer (int64)||


# UserResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data||false|UserData||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


# UserSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find tasks. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||
|startletter|Search users with the given startletter. If this field is present, the pattern is matched against the user field which is specified by the property "contact_first_letter_field" on the server (default: last name). Otherwise, the pattern is matched against the display name.|false|boolean||
|last_name|Searches users where the last name matches with the given last name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|false|string||
|first_name|Searches users where the first name matches with the given first name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|false|string||
|display_name|Searches users where the display name matches with the given display name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|false|string||
|orSearch|If set to `true`, the fields `last_name`, `first_name` and `display_name` are connected through an OR search habit. This field is ignored if `pattern` is specified.|false|boolean||
|emailAutoComplete|If set to `true`, results are guaranteed to contain at least one email adress and the search is performed by connecting the relevant fields through an OR search habit. This field is ignored if `pattern` is specified.|false|boolean||


# UsersResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array of user. Each user is described as an array itself.|false|object array array||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||



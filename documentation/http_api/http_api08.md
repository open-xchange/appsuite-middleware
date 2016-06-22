---
title: API Definitions
classes: no-affix
---

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




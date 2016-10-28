# Column identifiers


Below you find the identifiers for object fields of certain data objects (models) that can be used in the `columns` parameter of a request to return
specific field data of single or multiple objects.

## Common object data

| ID | Name | Type | Value |
|:----|:------|:------|:-------|
| 1 | id |String|Object ID|
| 2 | created_by |String|User ID of the user who created this object.|
| 3 | modified_by |String|User ID of the user who last modified this object.|
| 4 | creation_date |Time|Date and time of creation.|
| 5 | last_modified |Time|Date and time of the last modification.|
| 20 | folder_id |String|Object ID of the parent folder.|
| 100 | categories |String|String containing comma separated the categories. Order is preserved. Changing the order counts as modification of the object. Not present in folder objects.|
| 101 | private_flag |Boolean|Overrides folder permissions in shared private folders: When true, this object is not visible to anyone except the owner. Not present in folder objects.|
| 102 | color_label |Number|Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive). Not present in folder objects.|
| 104 | number_of_attachments |Number|Number of attachments|
| 105 | lastModifiedOfNewestAttachmentUTC |Time|Date and time of the newest attachment written with UTC time zone.|


## Permission object

| Name | Type | Value |
|:------|:------|:-------|
|bits| Number | For non-mail folders, a number as described in [Permission flags](#permission-flags).|
|rights| String | For mail folders, the rights string as defined in [RFC 2086](http://tools.ietf.org/html/rfc2086).|
|entity| Number | User ID of the user or group to which this permission applies (ignored for type "anonymous" or "guest").|
|group| Boolean | true if entity refers to a group, false if it refers to a user (ignored for type "anonymous" or "guest").|
|type| String | The recipient type, i.e. one of "user", "group", "guest", "anonymous" (required if no internal "entity" defined).|
|password| String | An additional secret / pin number an anonymous user needs to enter when accessing the share (for type "anonymous", optional) .|
|email_address| String | The e-mail address of the recipient (for type "guest").|
|display_name| String | The display name of the recipient (for type "guest", optional).|
|contact_id| String | The object identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", optional).|
|contact_folder| String | The folder identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", required if "contact_id" is set).|
|expiry_date| Time | The end date / expiration time after which the share link is no longer accessible (for type "anonymous", optional).|


## Extended permission object

| Name | Type | Value |
|:------|:------|:-------|
|entity| Number | Identifier of the permission entity (i.e. user-, group- or guest-ID).|
|bits| Number | A number as described in [Permission flags](#permission-flags).|
|type| String | "user" for an internal user, "group" for a group, "guest" for a guest, or "anonymous" for an anonymous permission entity.|
|display_name| String | A display name for the permission entity.|
|contact| Object | A (reduced) set of [Detailed contact data](#detailed-contact-data) for "user" and "guest" entities.|
|share_url| String | The share link for "anonymous" entities.|
|password| String | The optionally set password for "anonymous" entities.|
|expiry_date| Date | The optionally set expiry date for "anonymous" entities.|


## Common folder data

| ID | Name | Type | Value |
|:----|:------|:------|:-------|
| 1 | id |String|Object ID|
| 2 | created_by |String|User ID of the user who created this object.|
| 3 | modified_by |String|User ID of the user who last modified this object.|
| 4 | creation_date |Time|Date and time of creation.|
| 5 | last_modified |Time|Date and time of the last modification.|
| 6 | last_modified_utc |Timestamp|Timestamp of the last modification. Note that the type is Timestamp, not Time. See [Date and time](#date-and-time) for details. (added 2008-10-17, with SP5, temporary workaround)|
| 20 | folder_id |String|Object ID of the parent folder.|

## Detailed folder data

| ID | Name | Type | Value |
|:----|:------|:------|:-------|
| 300 | title |String|Name of this folder.|
| 301 | module |String|Name of the module which implements this folder; e.g. "tasks", "calendar", "contacts", "infostore", or "mail"|
| 302 | type |Number| See [Type of folder](#type-of-folder)|
| 304 | subfolders |Boolean|true if this folder has subfolders.|
| 305 | own_rights |Number or String|Permissions which apply to the current user, as described either in [Permission flags](#permission-flags) or in [RFC 2086](http://tools.ietf.org/html/rfc2086).|
| 306 | permissions |Array|Each element is an object described in [Permission object](#permission-object).|
| 307 | summary |String|Information about contained objects.|
| 308 | standard_folder |Boolean|Indicates whether or not folder is marked as a default folder (only OX folder)|
| 309 | total |Number|The number of objects in this Folder.|
| 310 | new |Number|The number of new objects in this Folder.|
| 311 | unread |Number|The number of unread objects in this Folder.|
| 312 | deleted |Number|The number of deleted objects in this Folder.|
| 313 | capabilities |Number|Bit mask containing information about mailing system capabilites, as described in [capabilities](#capabilities).|
| 314 | subscribed |Boolean|Indicates whether this folder should appear in folder tree or not. Note: Standard folders cannot be unsubscribed.|
| 315 | subscr_subflds |Boolean|Indicates whether subfolders should appear in folder tree or not.|
| 316 | standard_folder_type |Number|Indicates the default folder type. Zero for non-default folder. See [Standard folder types](#standard-folder-types)|
| 317 | supported_capabilities |Array|Each element is a String identifying a supported folder capability as described in [supported capabilities](#supported-capabilities). Only applicable for non-mail folders. Read Only, Since 7.4.0.|
| 318 | account_id |String|Will be null if the folder does not belong to any account (i.e. if its module doesn't support multiple accounts), is a virtual folder or an account-agnostic system folder. Since 7.8.0.|
| 3010 | com.openexchange.publish.publicationFlag |Boolean|Indicates whether this folder is published. Read Only, provided by the com.openexchange.publish plugin, since 6.14.|
| 3020 | com.openexchange.subscribe.subscriptionFlag |Boolean|Indicates whether this folder has subscriptions storing their content in this folder. Read Only, provided by the com.openexchange.subscribe plugin, since 6.14.|
| 3030 | com.openexchange.folderstorage.displayName |String|Provides the display of the folder's owner. Read Only, Since 6.20.|
| 3060 | com.openexchange.share.extendedPermissions |Array|Each element is an object described in [Extended permission object](#extended-permission-object). Read Only, Since 7.8.0.|

### Type of folder

|Number|Type|
|:-----|:---|
|1|	private|
|2|	public|
|3|	shared|
|5|	system folder|
|7|	This type is no more in use (legacy type). Will be removed with a future update!|
|16| trash|
|20| pictures|
|21| documents|
|22| music|
|23| videos|
|24| templates|

## Detailed task and appointment data

|ID   | Name  |	Type  |	Value  |
|:----|:------|:------|:-------|
|200 | title| String | Short description.|
|201 | start_date| Date | or Time	Inclusive start of the event as Date for tasks and whole day appointments and Time for normal appointments. For sequencies, this date must be part of the sequence, i. e. sequencies always start at this date. (deprecated for tasks since v7.6.1, replaced by start_time and full_time)|
|202 | end_date| Date | or Time	Exclusive end of the event as Date for tasks and whole day appointments and as Time for normal appointments. (deprecated for tasks since v7.6.1, replaced by end_time and full_time)|
|203 | note| String | Long description.|
|204 | alarm| Number | or Time	Specifies when to notify the participants as the number of minutes before the start of the appointment (-1 for "no alarm"). For tasks, the Time value specifies the absolute time when the user should be notified.|
|209 | recurrence_type| Number | Specifies the type of the recurrence for a task sequence. See [Task sequence type](#task-sequence-type)|
|212 | days| Number | Specifies which days of the week are part of a sequence. The value is a bitfield with bit 0 indicating sunday, bit 1 indicating monday and so on. May be present if recurrence_type > 1. If allowed but not present, the value defaults to 127 (all 7 days).|
|213 | day_in_month| Number | Specifies which day of a month is part of the sequence. Counting starts with 1. If the field "days" is also present, only days selected by that field are counted. If the number is bigger than the number of available days, the last available day is selected. Present if and only if recurrence_type > 2.|
|214 | month| Number | Month of the year in yearly sequencies. 0 represents January, 1 represents February and so on. Present if and only if recurrence_type = 4.|
|215 | interval| Number | Specifies an integer multiplier to the interval specified by recurrence_type. Present if and only if recurrence_type > 0. Must be 1 if recurrence_type = 4.|
|216 | until| Date | Inclusive end date of a sequence. May be present only if recurrence_type > 0. The sequence has no end date if recurrence_type > 0 and this field is not present. Note: since this is a Date, the entire day after the midnight specified by the value is included.|
|217 | notification| Boolean | If true, all participants are notified of any changes to this object. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client.|
|220 | participants| Array | Each element identifies a participant, user, group or booked resource as described in [Participant identifier](#participant-identifier).|
|221 | users| Array | Each element represents a participant as described in [User participant object](#user-participant-object). User groups are resolved and are represented by their members. Any user can occur only once.|
|222 | occurrences| Number | Specifies how often a recurrence should appear. May be present only if recurrence_type > 0.|
|223 | uid| String | Can only be written when the object is created. Internal and external globally unique identifier of the appointment or task. Is used to recognize appointments within iCal files. If this attribute is not written it contains an automatic generated UUID.|
|224 | organizer| String | Contains the email address of the appointment organizer which is not necessarily an internal user. Not implemented for tasks.|
|225 | sequence| Number | iCal sequence number. Not implemented for tasks. Must be incremented on update. Will be incremented by the server, if not set.|
|226 | confirmations| Array | Each element represents a confirming participant as described in [Confirming participant](#confirming-participant). This can be internal and external user. Not implemented for tasks.|
|227 | organizerId| Number | Contains the userIId of the appointment organizer if it is an internal user. Not implemented for tasks. (Introduced with 6.20.1)|
|228 | principal| String | Contains the email address of the appointment principal which is not necessarily an internal user. Not implemented for tasks. (Introduced with 6.20.1)|
|229 | principalId| Number | Contains the userIId of the appointment principal if it is an internal user. Not implemented for tasks. (Introduced with 6.20.1)|
|401 | full_time| Boolean | True if the event is a whole day appointment or task, false otherwise.|

### Task sequence type

|Number | Description|
|:------|:-----------|
|0 | none (single event)|
|1 | daily|
|2 | weekly|
|3 | monthly|
|4 | yearly|


### Participant identifier
|Name | Type | Value|
|:----|:-----|:-----|
|id | Number | User ID|
|type | Number | See [Participant types](#participant-types)|
|mail | String | mail address of an external participant|


### Participant types

|Number | Type |
|:------|:-----|
|1 | user|
|2 | user group|
|3 | resource|
|4 | resource group|
|5 | external user|


### User participant object

|Name | Type | Value|
|:----|:-----|:-----|
|id | Number | User ID. Confirming for other users only works for appointments and not for tasks.|
|display_name | String | Displayable name of the participant.|
|confirmation | Number | See [Confirmation status](#confirmation-status) |
|confirmmessage | String | Confirm Message of the participant|

### Confirmation status

|Number | Status |
|:------|:-------|
|0 | none|
|1 | accepted|
|2 | declined|
|3 | tentative|

### Confirming participant

|Name | Type | Value|
|:----|:-----|:-----|
|type | Number | Either 1 = user or 5 = external user.|
|mail | String | email address of external participant|
|display_name | String | display name of external participant|
|status | Number | See [Confirmation status](#confirmation-status)|
|message | String | Confirm Message of the participant|

## Detailed task data

|ID   | Name  |	Type  |	Value  |
|:----|:------|:------|:-------|
|300 | status| Number |	Status of the task. See [Task status](#task-status)|
|301 | percent_completed| Number | How much of the task is completed. An integer number between 0 and 100.|
|302 | actual_costs| Number | A monetary attribute to store actual costs of a task. Allowed values must be in the range -9999999999.99 and 9999999999.99.|
|303 | actual_duration|||
|304 | after_complete| Date| Deprecated. Only present in AJAX interface. Value will not be stored on OX server.|
|305 | billing_information|||
|307 | target_costs| Number| A monetary attribute to store target costs of a task. Allowed values must be in the range -9999999999.99 and 9999999999.99.|
|308 | target_duration|||
|309 | priority| Number |	1 = LOW, 2 = MEDIUM, 3 = HIGH|
|312 | currency|||
|313 | trip_meter|||
|314 | companies|||
|315 | date_completed|||
|316 | start_time| Date or Time | Inclusive start as Date for whole day tasks and Time for normal tasks.|
|317 | end_time| Date or Time |	Exclusive end as Date for whole day tasks and as Time for normal tasks.|

### Task status

|Number | Status |
|:------|:-------|
|1 | not started|
|2 | in progress|
|3 | done|
|4 | waiting|
|5 | deferred|

## Detailed contact data

|ID  | Displayed name | Name | Type | Description | 
|:---|:---------------|:-----|:-----|:------------|
|223 ||uid | String | Can only be written when the object is created. Internal and external globally unique identifier of the contact. Is used to recognize contacts within vCard files. If this attribute is not written it contains an automatic generated UUID.|
|500 |Display name | display_name | String ||
|501 |Given name | first_name |	String | First name.||
|502 |Sur name | last_name |	String | Last name.||
|503 |Middle name |	second_name | String ||
|504 |Suffix | suffix |	String ||
|505 |Title | title | String ||
|506 |Street home |	street_home | String ||
|507 |Postal code home | postal_code_home | String||
|508 |City home | city_home | String||
|509 |State home | state_home | String||
|510 |Country home | country_home |	String||
|511 |Birthday|	birthday | Date||
|512 |Marital status| marital_status| String||
|513 |Number of children | number_of_children |	String ||
|514 |Profession | profession |	String ||
|515 |Nickname | nickname | String ||
|516 |Spouse name |	spouse_name | String ||
|517 |Anniversary |	anniversary | Date ||
|518 |Note | note | String ||
|519 |Department | department | String ||
|520 |Position|	position | String ||
|521 |Employee type | employee_type | String ||
|522 |Room number | room_number | String ||
|523 |Street business |	street_business | String ||
|524 |Internal user id | user_id | Number ||
|525 |Postal code business | postal_code_business |	String ||
|526 |City business | city_business | String ||
|527 |State business |	state_business | String ||
|528 |Country business | country_business |	String ||
|529 |Number of employee | number_of_employees | String ||
|530 |Sales volume | sales_volume |	String ||
|531 |Tax id | tax_id |	String ||
|532 |Commercial register |	commercial_register | String ||
|533 |Branches | branches |	String ||
|534 |Business category | business_category	| String ||
|535 |Info | info |	String ||
|536 |Manager's name | manager_name | String ||
|537 |Assistant's name | assistant_name | String ||
|538 |Street other | street_other | String ||
|539 |City other | city_other | String ||
|540 |Postal code other | postal_code_other | String ||
|541 |Country other | country_other | String ||
|542 |Telephone business 1 | telephone_business1 | String ||
|543 |Telephone business 2 | telephone_business2 | String ||
|544 |FAX business | fax_business | String ||
|545 |Telephone callback | telephone_callback | String ||
|546 |Telephone car | telephone_car | String ||
|547 |Telephone company | telephone_company | String ||
|548 |Telephone home 1 | telephone_home1 | String ||
|549 |Telephone home 2 | telephone_home2 | String ||
|550 |FAX home | fax_home |	String||
|551 |Cellular telephone 1 | cellular_telephone1 | String ||
|552 |Cellular telephone 2 | cellular_telephone2 | String ||
|553 |Telephone other | telephone_other | String ||
|554 |FAX other | fax_other | String ||
|555 |Email 1 | email1 | String ||
|556 |Email 2 |	email2 | String ||
|557 |Email 3 |	email3 | String ||
|558 |URL | url | String ||
|559 |Telephone ISDN | telephone_isdn |	String ||
|560 |Telephone pager |	telephone_pager | String ||
|561 |Telephone primary | telephone_primary | String ||
|562 |Telephone radio |	telephone_radio | String ||
|563 |Telephone telex |	telephone_telex | String ||
|564 |Telephone TTY/TDD | telephone_ttytdd | String ||
|565 |Instantmessenger 1 | instant_messenger1 |	String ||
|566 |Instantmessenger 2 | instant_messenger2 |	String ||
|567 |Telephone IP | telephone_ip |	String ||
|568 |Telephone assistant |	telephone_assistant | String ||
|569 |Company | company | String ||
|570 ||	image1 | String ||
|571 |Dynamic Field 1 | userfield01 | String ||
|572 |Dynamic Field 2 | userfield02 | String ||
|573 |Dynamic Field 3 | userfield03 | String ||
|574 |Dynamic Field 4 | userfield04 | String ||
|575 |Dynamic Field 5 | userfield05 | String ||
|576 |Dynamic Field 6 | userfield06 | String ||
|577 |Dynamic Field 7 | userfield07 | String ||
|578 |Dynamic Field 8 | userfield08 | String ||
|579 |Dynamic Field 9 | userfield09 | String ||
|580 |Dynamic Field 10 | userfield10 | String ||
|581 |Dynamic Field 11 | userfield11 | String ||
|582 |Dynamic Field 12 | userfield12 | String ||
|583 |Dynamic Field 13 | userfield13 | String ||
|584 |Dynamic Field 14 | userfield14 | String ||
|585 |Dynamic Field 15 | userfield15 | String ||
|586 |Dynamic Field 16 | userfield16 | String ||
|587 |Dynamic Field 17 | userfield17 | String ||
|588 |Dynamic Field 18 | userfield18 | String ||
|589 |Dynamic Field 19 | userfield19 | String ||
|590 |Dynamic Field 20 | userfield20 | String | Contains a UUID if one was assigned (after 6.18.2)|
|592 || distribution_list | Array | If this contact is a distribution list, then this field is an array of objects. Each object describes a member of the list as defined in [Distribution list member](#distribution-list-member).|
|594 | Number of distributionlists | number_of_distribution_list | Number ||
|596 || number_of_images | Number ||
|597 || image_last_modified | Timestamp ||
|598 |State other |	state_other | String ||
|599 ||	file_as | String ||
|601 || image1_content_type | String ||
|602 || mark_as_distributionlist | Boolean ||
|605 |Default address |	default_address | Number ||
|606 ||	image1_url | String ||
|608 ||	useCount | Number |	In case of sorting purposes the column 609 is also available, which places global address book contacts at the beginning of the result. If 609 is used, the order direction (ASC, DESC) is ignored.||
|610 ||	yomiFirstName | String | Kana based representation for the First Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)||
|611 ||	yomiLastName | String |	Kana based representation for the Last Name. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)||
|612 ||	yomiCompany | String | Kana based representation for the Company. Commonly used in japanese environments for searchin/sorting issues. (since 6.20)||
|613 ||	addressHome | String | Support for Outlook 'home' address field. (since 6.20.1)||
|614 ||	addressBusiness | String | Support for Outlook 'business' address field. (since 6.20.1)||
|615 ||	addressOther | String |	Support for Outlook 'other' address field. (since 6.20.1)||



### Distribution list member
| Name | Type | Value |
|:-----|:-----|:------|
| id | String | Object ID of the member's contact if the member is an existing contact. |
| folder_id | String | Parent folder ID of the member's contact if the member is an existing contact (preliminary, from 6.22 on). |
| display_name | String | Display name |
| mail | String | Email address (mandatory before 6.22, afterwards optional if you are referring to an internal contact) |
| mail_field | Number | Which email field of an existing contact (if any) is used for the mail field. See [Mail fields](#mail-fields). |

### Mail fields

|Number|Field|
|:-----|:----|
|0 | independent contact |
|1 | default email field (email1) |
|2 | second email field (email2) |
|3 | third email field (email3) |

## Detailed appointment data

|ID |	Name |	Type | Value |
|:--|:-------|:------|:------|
|206 |	recurrence_id |	Number | Object ID of the entire appointment sequence. Present on series and change exception appointments. Equals to object identifier on series appointment and is different to object identifier on change exceptions. |
|207 |	recurrence_position | Number | 1-based position of an individual appointment in a sequence. Present if and only if recurrence_type > 0. |
|208 |	recurrence_date_position | Date | Date of an individual appointment in a sequence. Present if and only if recurrence_type > 0. |
|210 |	change_exceptions |	Array | An array of Dates, representing all change exceptions of a sequence. |
|211 |	delete_exceptions |	Array | An array of Dates, representing all delete exceptions of a sequence. |
|400 |	location | String | Location |
|402 |	shown_as | Number | Describes, how this appointment appears in availability queries. See [Appointment availability](#appointment-availability) |
|408 |	timezone | String | Timezone |
|410 |	recurrence_start |	Date | Start of a sequence without time |
||ignore_conflicts | Boolean |	Ignore soft conflicts for the new or modified appointment. This flag is valid for the current change only, i. e. it is not stored in the database and is never sent by the server to the client. |

### Appointment availability

|Number|Value|
|:-----|:----|
|1 | reserved|
|2 | temporary|
|3 | absent|
|4 | free|

## Detailed mail data

|ID   | Name  |	Type  |	Value  |
|:----|:------|:------|:-------|
|102 | color_label | Number | Color number used by Outlook to label the object. The assignment of colors to numbers is arbitrary and specified by the client. The numbers are integer numbers between 0 and 10 (inclusive).|
|600 | id | String | Object ID|
|601 | folder_id | String |	Object ID of the parent folder|
|602 | attachment | Boolean | Specifies whether this mail has attachments.|
|603 | from | Array | Each element is a two-element array specifying one sender. The first element of each address is the personal name, the second element is the email address. Missing address parts are represented by null values.|
|604 | to | Array |	Each element is a two-element array (see the from field) specifying one receiver.|
|605 | cc | Array |	Each element is a two-element array (see the from field) specifying one carbon-copy receiver.|
|606 | bcc | Array | Each element is a two-element array (see the from field) specifying one blind carbon-copy receiver.|
|607 | subject | String | Subject line.|
|608 | size | Number | Size of the mail in bytes.|
|609 | sent_date | Time | Date and time as specified in the mail by the sending client.|
|610 | received_date | Time | Date and time as measured by the receiving server.|
|611 | flags | Number |	Various system flags. A sum of zero or more of values described in [Mail system flags](#mail-system-flags). See javax.mail.Flags.Flag for details.|
|612 | level | Number |	Zero-based nesting level in a thread.|
|613 | disp_notification_to | String | Content of message's header “Disposition-Notification-To”|
|614 | priority | Number | Value of message's “X-Priority” header. See [X-Priority header](#x-priority-header).|
|615 | msg_ref | String | Message reference on reply/forward.|
|651 | flag_seen | String |	Special field to sort mails by seen status|
|652 | account_name | String | Message's account name.|
|653 | account_id | int | Message's account identifier. Since v6.20.2|
||user | Array | An array with user-defined flags as strings.|
||headers | Object | An object with a field for every non-standard header. The header name is the field name. The header value is the value of the field as string.|
||attachments | Array | Each element is an attachment as described in [Attachment](#attachment). The first element is the mail text. If the mail has multiple representations (multipart-alternative), then the alternatives are placed after the mail text and have the field disp set to alternative.|
||nested_msgs | Array | Each element is a mail object as described in this table, except for fields id, folder_id and attachment.|
||truncated | boolean | true/false if the mail content was trimmed. Since v7.6.1|
||source | String | RFC822 source of the mail. Only present for action=get&attach_src=true|
||cid | String | The value of the "Content-ID" header, if the header is present.|
|654 | original_id | String | The original mail identifier (e.g. if fetched from "virtual/all" folder).|
|655 | original_folder_id | String | The original folder identifier (e.g. if fetched from "virtual/all" folder).|
|656 | content_type | String | The Content-Type of a mail; e.g. multipart/mixed; boundary="-0123456abcdefg--".|
|657 | answered | String | Special field to sort mails by answered status.|
|658 | forwarded | String | Special field to sort mails by forwarded status. Note that mail service needs either support a \Forwarded system flag or a $Forwared user flag |
|659 | draft | String | Special field to sort mails by draft flag.|
|660 | flagged | String | Special field to sort mails by flagged status.|
|661 | date | String | The date of a mail message. As configured, either the internal received date or mail's sent date (as given by <code>"Date"</code> header). Supposed to be the replacement for ``sent_date`` (609) or ``received_date`` (610) to let the Open-Xchange Middleware decide based on configuration for ``com.openexchange.mail.preferSentDate`` proeprty what to consider. Supported at both - ``columns`` parameter and ``sort`` parameter.|

### Mail system flags

|Number | Description |
|:------|:------------|
|1 | answered |
|2 | deleted |
|4 | draft |
|8 | flagged |
|16 | recent |
|32 | seen |
|64 | user |
|128 | spam |
|256 | forwarded |

### X-Priority header

|Number | Description |
|:------|:------------|
|0 | No priority |
|5 | Very Low |
|4 | Low |
|3 | Normal |
|2 | High |
|1 | Very High |

### Attachment

|Name |	Type | Value |
|:----|:-----|:------|
|id |	String | Object ID (unique only inside the same message) |
|content_type |	String | MIME type |
|content |	String | Content as text. Present only if easily convertible to text. |
|filename |	String | Displayed filename (mutually exclusive with content). |
|size |	Number | Size of the attachment in bytes. |
|disp |	String | Attachment's disposition: null, inline, attachment or alternative. |


## Detailed infoitem data

|ID   | Name  | Type  | Value  |
|:----|:------|:------|:-------|
|108 | object_permissions | Array | Each element is an object described in [Object Permission object](#object-permission-object) (preliminary, available with 7.8.0). |
|109 | shareable | Boolean | (read-only) Indicates if the item can be shared (preliminary, available with 7.8.0). |
|700 | title | String | Title |
|701 | url | String | Link/URL |
|702 | filename | String | Displayed filename of the document. |
|703 | file_mimetype | String | MIME type of the document. The client converts known types to more readable names before displaying them. |
|704 | file_size | Number | Size of the document in bytes. |
|705 | version | Number | Version number of the document. New documents start at 1. Every update increments the version by 1. |
|706 | description | String | Description |
|707 | locked_until | Time | The time until which this item will presumably be locked. Only set if the docment is currently locked, 0 otherwise. |
|708 | file_md5sum | String | MD5Sum of the document. Not yet implemented, so this is currently always empty. |
|709 | version_comment | String | A version comment is used to file a changelog for the file. |
|710 | current_version | Boolean | “true” if this version is the current version “false” otherwise. Note: This is not writeable |
|711 | number_of_versions | Number | The number of all versions of the infoitem. Note: This is not writeable. |
|7010 | com.openexchange.share.extendedObjectPermissions | Array | Each element is an object described in [Extended object permission object](#extended-object-permission-object). Read Only, Since 7.8.0.|
|7020 | com.openexchange.realtime.resourceID | String | The resource identifier for the infoitem for usage within the realtime component. Read Only, Since 7.8.0. |


### Object Permission object

|Name |	Type | Value |
|:----|:-----|:------|
| bits | Number | A number as described in Object Permission flags.|
| entity | Number | User ID of the user or group to which this permission applies.|
| group | Boolean | true if entity refers to a group, false if it refers to a user.|
| type | String | The recipient type, i.e. one of "user", "group", "guest", "anonymous" (required if no internal "entity" defined).|
| password | String | An additional secret / pin number an anonymous user needs to enter when accessing the share (for type "anonymous", optional) .|
| email_address | String | The e-mail address of the recipient (for type "guest").|
| display_name | String | The display name of the recipient (for type "guest", optional).|
| contact_id | String | The object identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", optional).|
| contact_folder | String | The folder identifier of the corresponding contact entry if the recipient was chosen from the address book (for type "guest", required if "contact_id" is set).|
| expiry_date | Time | The end date / expiration time after which the share link is no longer accessible (for type "anonymous", optional).|

### Extended object permission object

|Name |	Type | Value |
|:----|:-----|:------|
|entity | Number | Identifier of the permission entity (i.e. user-, group- or guest-ID). |
|bits | Number | A number as described in Object Permission flags. |
|type | String | "user" for an internal user, "group" for a group, "guest" for a guest, or "anonymous" for an anonymous permission entity. |
|display_name | String | A display name for the permission entity. |
|contact | Object | A (reduced) set of Detailed contact data for "user" and "guest" entities. |
|share_url | String | The share link for "anonymous" entities. |
|password | String | The optionally set password for "anonymous" entities. |
|expiry_date | Date | The optionally set expiry date for "anonymous" entities. |



## Attachment object

|ID   |Name   | Type  | Description|
|:----|:------|:------|:-----------|
|800 |folder | Number | The ID of the first Folder in which the attached object resides.|
|801 |attached | Number | The object id of the object this attachement is attached to.|
|802 |module | Number | The Module of this Object. Possible values are described in [Attachment module](#attachment-module)|
|803 |filename | String | The filename of the attached file.|
|804 |file_size | Number | The file size (in bytes) of the attached file.|
|805 |file_mimetype | String | The MIME-Type of the attached file|
|806 |rft_flag | Boolean | If the attachment is a RTF Attachment of Outlook. (Outlook descriptions can be stored as RTF Documents).|

### Attachment module

|Number | Name |
|:------|:-----|
|1 | Appointment |
|4 | Task |
|7 | Contact |
|137 | Infostore |

## Mail account data

|ID   |Name   | Type  | Value |
|:----|:------|:------|:-----------|
| 1001 | id | Number | Account ID |
| 1002 | login | String | The login. |
| 1003 | password | String | The (optional) password. |
| 1004 | mail_url | String | The mail server URL; e.g. "imap://imap.somewhere.com:143". URL is preferred over single fields (like mail_server, mail_port, etc.) |
| 1005 | transport_url | String | The transport server URL; e.g. "smtp://smtp.somewhere.com:25". URL is preferred over single fields (like transport_server, transport_port, etc.) |
| 1006 | name | String | Account's display name. |
| 1007 | primary_address | String | User's primary address in account; e.g. "someone@somewhere.com" |
| 1008 | spam_handler | String | The name of the spam handler used by account. |
| 1009 | trash | String | The name of the default trash folder. |
| 1010 | sent | String | The name of the default sent folder. |
| 1011 | drafts | String | The name of the default drafts folder. |
| 1012 | spam | String | The name of the default spam folder. |
| 1013 | confirmed_spam | String | The name of the default confirmed-spam folder. |
| 1014 | confirmed_ham | String | The name of the default confirmed-ham folder. |
| 1015 | mail_server | String | The mail server's hostname or IP address. |
| 1016 | mail_port | Number | The mail server's port. |
| 1017 | mail_protocol | String | The mail server's protocol. Always use basic protocol name. E.g. use "imap" instead of "imaps" |
| 1018 | mail_secure | Boolean | Whether to establish a secure connection to mail server (SSL, TLS). |
| 1019 | transport_server | String | The transport server's hostname or IP address. |
| 1020 | transport_port | Number | The transport server's port. |
| 1021 | transport_protocol | String | The transport server's protocol. Always use basic protocol name. E.g. use "smtp" instead of "smtps" |
| 1022 | transport_secure | Boolean | Whether to establish a secure connection to transport server (SSL, TLS). |
| 1023 | transport_login | String | The transport login. Please see "transport_auth" for the handling of this field. |
| 1024 | transport_password | String | The transport password. Please see "transport_auth" for the handling of this field. |
| 1025 | unified_inbox_enabled | Boolean | If enabled for Unified INBOX |
| 1026 | trash_fullname | String | Path to default trash folder. Preferred over "trash" |
| 1027 | sent_fullname | String | Path to default sent folder. Preferred over "sent" |
| 1028 | drafts_fullname | String | Path to default drafts folder. Preferred over "drafts" |
| 1029 | spam_fullname | String | Path to default spam folder. Preferred over "spam" |
| 1030 | confirmed_spam_fullname | String | Path to default confirmed-spam folder. Preferred over "confirmed_spam" |
| 1031 | confirmed_ham_fullname | String | Path to default confirmed-ham folder. Preferred over "confirmed_ham" |
| 1032 | pop3_refresh_rate | Number | The interval in minutes the POP3 account is refreshed |
| 1033 | pop3_expunge_on_quit | Boolean | Whether POP3 messages shall be deleted on actual POP3 account after retrieval or not |
| 1034 | pop3_delete_write_through | Boolean | If option "pop3_expunge_on_quit" is disabled, this property defines whether a delete in local INBOX also deletes affected message in actual POP3 account |
| 1035 | pop3_storage | String | The name of POP3 storage provider, default is "mailaccount" |
| 1036 | pop3_path | String | Path to POP3's virtual root folder in storage, default is name of the POP3 account beside default folders |
| 1037 | personal | String | The customizable personal part of email address |
| 1038 | reply_to | String | The customizable reply-to email address |
| 1039 | addresses | String | The comma-separated list of available E-Mail addresses including aliases. !! Only available for primary mail account !! |
| 1040 | meta | JSON | data	Stores arbitrary JSON data as specified by client associated with the mail account |
| 1041 | archive | String | The name of the archive folder. Currently not functional! |
| 1042 | archive_fullname | String | The full name of the archive folder. Currently not functional! |
| 1043 | transport_auth | String | Available since v7.6.1 Specifies the source for mail transport (SMTP) credentials. See [Credential source](#credential-source).|
| 1044 | mail_starttls | Boolean | Available since v7.8.2 Whether to establish a secure connection to mail server via STARTTLS. |
| 1045 | transport_starttls | Boolean | Available since v7.8.2 Whether to establish a secure connection to transport server via STARTTLS. |

### Credential source

|Value | Description |
|:-----|:------------|
| mail | Signals to use the same credentials as given in associated mail store (IMAP, POP3). |
| custom | Signals that individual credentials are supposed to be used (fields "transport_login" and "transport_password" are considered). |
| none | Means the mail transport does not support any authentication mechanism (rare case!) |

## Detailed user data

|ID | Displayed name | Name | Type | Value |
|:--|:---------------|:-----|:-----|:------|
|610 | Aliases | aliases | Array | The user's aliases |
|611 | Time zone | timezone | String | The time zone ID. |
|612 | Locale | locale | String | The name of user's entire locale, with the language, country and variant separated by underbars. E.g. "en", "de_DE" |
|613 | Groups |	groups | Array | The IDs of user's groups |
|614 | Contact ID |	contact_id | Number | The contact ID of the user |
|615 | Login info |	login_info | String | The user's login information |
|616 | Guest Created By | guest_created_by | Number | The ID of the user who has created this guest in case this user represents a guest user; it is 0 for regular users (preliminary, available with v7.8.0) |

## Messaging message columns

|Name | Description |
|:----|:------------|
|id | The id attribute|
|folderId |  The folder attribute|
|contentType | The "Content-Type" header|
|from | The "From" header|
|to | The "To" header|
|cc | The "Cc" header|
|bcc | The "Bcc" header|
|subject | The "Subject" header|
|size | The size attribute|
|sentDate | The "Date" header|
|receivedDate | The receivedDate attribute|
|flags | The flags attribute|
|threadLevel | The threadLevel attribute|
|dispositionNotificationTo | The "Disposition-Notification-To" header.|
|priority | The "X-Priority" header|
|colorLabel | The colorLabel attribute|
|url | The url attribute|
|body | The content attribute|
|headers | The headers attribute|
|picture | The url to the message picture.|


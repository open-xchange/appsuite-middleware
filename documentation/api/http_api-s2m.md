# OX HTTP API

## Overview
Documentation of the Open-Xchange HTTP API which is used by the new AJAX GUI.
## Introduction
### Low level protocol
The client accesses the server through HTTP GET, POST and PUT requests. HTTP cookies are used for authentication and must therefore be processed and sent back by the client as specified by [RFC 6265](http://tools.ietf.org/html/rfc6265). The HTTP API is accessible at URIs starting with `/ajax`. Each server module has a unique name and its own sub-namespace with that name below `/ajax`, e. g. all access to the module "tasks" is via URIs starting with `/ajax/tasks`.

Text encoding is always UTF-8. Data is sent from the server to the client as text/javascript and interpreted by the client to obtain an ECMAScript object. The HTTP API uses only a small subset of the ECMAScript syntax. This subset is roughly described by the following BNF:
```
Value   ::= "null" | Boolean | Number | String | Array | Object
Boolean ::= "true" | "false"
Number  ::= see NumericLiteral in ECMA 262 3rd edition
String  ::= \"([^"\n\\]|\\["\n\\])*\"
Array   ::= "[]" | "[" Value ("," Value)* "]"
Object  ::= "{}" | "{" Name ":" Value ("," Name ":" Value)* "}"
Name    ::= [A-Fa-f][0-9A-Fa-f_]*
```
Numbers are the standard signed integer and floating point numbers. Strings can contain any character, except double quotes, newlines and backslashes, which must be escaped by a backslash. Control characters in strings (other than newline) are not supported. Whitespace is allowed between any two tokens. See [JSON](http://json.org/) and [ECMA 262, 3rd edition](http://www.ecma-international.org/publications/standards/Ecma-262.htm) for the formal definition.

The response body consists of an object, which contains up to four fields as described in Response body. The field data contains the actual payload which is described in following chapters. The fields `timestamp`, `error` and `error_params` are present when data objects are returned, if an error occurred and if the error message contains conversion specifiers, respectively. Following sections describe the contents of these fields in more detail.

| Name | Type | Value |
|------|------|-------|
| data | Value | Payload of the response. |
| timestamp | Timestamp | The latest timestamp of the returned data (see Updates). |
| error | String | The translated error message. Present in case of errors. |
| error_params | Array | As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style). |
| error_id | String | Unique error identifier to help finding this error instance in the server logs. |
| error_desc | String | The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available |
| code | String | Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012" |
| error_stack | Array | If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in 'server.properties') this field provides the stack trace of associated Java exception represented as a JSON array |
| categories | String OR Array | Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs. |
| category | Number | Maintained for legacy reasons: The numeric representation of the first category. |

Data from the client to the server can be sent in several formats. Small amounts of data are sent as `application/x-www-urlencoded` in query parameters in the request URI. For POST requests, some or all parameters may be sent in the request body instead of in the URI using any valid encoding for POST requests. Alternatively, some requests specify that data is sent as `text/javascript` in the body of a PUT request. The format of the request body for PUT requests is the same as for sending data from the server to the client, except that the payload is sent directly, without being wrapped in another object.

When updating existing data, the client sends only fields that were modified. To explicitly delete a field, the field is sent with the value null. For fields of type `String`, the empty string "" is equivalent to `null`.
### Error handling
If the session of the user times out, if the client doesn't send a session ID or if the session for the specified session ID can not be found then the server returns the above described response object, that contains an error code and an error message. If the request URI or the request body is malformed or incomplete then the server returns the reponse object with an error message, too. In case of internal server errors, especially Java exceptions, or if the server is down, it returns the HTTP status code 503, Service Unavailable. Other severe errors may return other HTTP status values.

Application errors, which can be caused by a user and are therefore expected during the operation of the groupware, are reported by setting the field error in the returned object, as described in Response body. Since the error messages are translated by the client, they can not be composed of multiple variable parts. Instead, the error message can contain simplified printf()-style conversion specifications, which are replaced by elements from the array in the field `error_params`. If `error_params` is not present, no replacement occurs, even if parts of the error message match the syntax of a conversion specification.

A simplified conversion specification, as used for error messages, is either of the form _%s_ or _%n$s_, where _n_ is a 1-based decimal parameter index. The conversion specifications are replaced from left to right by elements from `error_params`, starting at the first element. _%s_ is replaced by the current element and the current index is incremented. _%n$s_ is replaced by the _n_'th element and the current index is set to the _(n + 1)_'th element.

Some error message contain data sizes which must be expressed in Bytes or Kilobytes etc., depending on the actual value. Since the unit must be translated, this conversion is performed by the client. Unfortunately, standard printf()-style formatting does not have a specifier for this kind of translation. Therefore, the conversion specification for sizes is the same as for normal strings, and the client has to determine which parameters to translate based on the error code. The current error codes and the corresponding size parameters are listed below:

| Error code | Parameter indices |
|------------|-------------------|
| CON-0101 | 2, 3 |
| FLS-0003 | 1, 2, 3 |
| MSG-0065 | 1, 3 |
| MSG-0066 | 1 |
| NON-0005 | 1, 2 |

### Date and time
Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC. Leap seconds are ignored, therefore this number is always an integer multiple of 8.64e7.

Because ECMAScript Date objects have no way to explicitly specify a timezone for calculations, timezone correction must be performed on the server. Dates with time are transmitted as the number of milliseconds since 1970-01-01 00:00 UTC (again, ignoring leap seconds) plus the offset between the user's timezone and UTC at the time in question. (See the Java method java.util.TimeZone.getOffset(long)). Unless optional URL parameter `timezone` is present. Then dates with time are transmitted as the number of milliseconds since 1970-01-01 00:00 UTC (again, ignoring leap seconds) plus the offset between the _specified_ timezone and UTC at the time in question.

For some date and time values, especially timestamps, monotonicity is more important than the actual value. Such values are transmitted as the number of milliseconds since 1970-01-01 00:00 UTC, ignoring leap seconds and without timezone correction. If possible, a unique strictly monotonic increasing value should be used instead, as it avoids some race conditions described below.

This specification refers to these three interpretations of the type Number as separate data types.

| Type | Time | Timezone | Comment |
|------|------|----------|---------|
| Date | No | UTC | Date without time. |
| Time | Yes | User | Date and time. |
| Timestamp | Yes | UTC | Timestamp or unique sequence number. |

### Updates
To allow efficient synchronization of a client with changes made by other clients and to detect conflicts, the server stores a timestamp of the last modification for each object. Whenever the server transmits data objects to the client, the response object described in Response body includes the field `timestamp`. This field contains a timestamp value which is computed as the maximum of the timestamps of all transmitted objects.

When requesting updates to a previously retrieved set of objects, the client sends the last timestamp which belongs to that set of objects. The response contains all updates with timestamps greater than the one specified by the client. The field timestamp of the response contains the new maximum timestamp value.

If multiple different objects may have the same timestamp values, then a race condition exists when an update is processed between two such objects being modified. The first, already modified object will be included in the update response and its timestamp will be the maximum timestamp value sent in the timestamp field of the response. If the second object is modified later but gets the same timestamp, the client will never see the update to that object because the next update request from the client supplies the same timestamp value, but only modifications with greater timestamp values are returned.

If unique sequence numbers can't be used as timestamps, then the risk of the race condition can be at least minimized by storing timestamps in the most precise format and/or limiting update results to changes with timestamp values which are measurably smaller than the current timestamp value.

### Editing

Editing objects is performed one object at a time. There may be multiple objects being edited by the same client simulataneously, but this is achieved by repeating the steps required for editing a single object. There is no batch edit or upload command.

To edit an object, a client first requests the entire object from the server. The server response contains the `timestamp` field described in the previous section. For in-place editing inside a view of multiple objects, where only already retrieved fields can be changed, retrieving the entire object is not necessary, and the last timestamp of the view is used as the timestamp of each object in it.

When sending the modified object back to the server, only modified fields need to be included in the sent object. The request also includes the timestamp of the edited object. The timestamp is used by the server to ensure that the object was not edited by another client in the meantime. If the current timestamp of the object is greater than the timestamp supplied by the client, then a conflict is detected and the field error is set in the response. Otherwise, the object gets a new timestamp and the response to the client is empty.

If the client displays the edited object in a view together with other objects, then the client will need to perform an update of that view immediately after successfully uploading an edited object.

### File uploads

File uploads are made by sending a POST request that submits both the file and the needed fields as parts of a request of content-type “multipart/form-data” or “multipart/mixed”. The file metadata are stored in a form field “file” (much like an `<input type=”file” name=”file” />` would do). In general a call that allows file uploads via POST will have a corresponding call using PUT to send object data. The JSON-encoded object-data that is send as the body of a corresponding PUT call is, when performed as a POST with file uploads, put into the request parameter “json”.

Since the upload is performed directly by the browser and is not an Ajax call, the normal callback mechanism for asynchronous Javascript calls cannot be used to obtain the result. For this reason the server responds to these POST calls with a complete HTML page that performs the callback and should not be displayed to the user. The HTML response is functionally equivalent to:

```
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <META http-equiv="Content-Type" content=\"text/html; charset=UTF-8\">
    <script type="text/javascript">
      (parent["callback_action"] || window.opener && window.opener["callback_action"])
      ({json})
    </script>
  </head>
</html>
```
The placeholders `{json}` is replaced by the response with the timestamp that would be expected from the corresponding PUT method. The placeholder `action` is replaced by the value of the parameter `action` of the request (except for the import bundle, which is named "import" instead of the action name for legacy purposes). The content-type of the answer is `text/html`.

**Non-browser clients don't need to interpret HTML or JavaScript.** The JSON data can be recognized by the outermost `({` and `})`, where the inner braces are part of the JSON value. For example, the regular expression `\((\{.*\})\)` captures the entire JSON value in its first capturing group.

## Column identifiers
Below you find the identifiers for object fields of certain data objects (models) that can be used in the `columns` parameter of a request to return
specific field data of single or multiple objects.
### Common object data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 1 | id | 2 | created_by | 3 | modified_by | 4 | creation_date |
| 5 | last_modified | 20 | folder_id | 100 | categories | 101 | private_flag |
| 102 | color_label | 104 | number_of_attachments | 105 | lastModifiedOfNewestAttachmentUTC |
### Common folder data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 1 | id | 2 | created_by | 3 | modified_by | 4 | creation_date |
| 5 | last_modified | 6 | last_modified_utc | 20 | folder_id |
### Detailed folder data
| ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|
| 300 | title | 301 | module | 302 | type |
| 304 | subfolders | 305 | own_rights | 306 | permissions |
| 307 | summary | 308 | standard_folder | 309 | total |
| 310 | new | 311 | unread | 312 | deleted |
| 313 | capabilities | 314 | subscribed | 315 | subscr_subflds |
| 316 | standard_folder_type | 317 | supported_capabilities | 318 | account_id |
| 3010 | com.openexchange.publish.publicationFlag | 3020 | com.openexchange.subscribe.subscriptionFlag | 3030 | com.openexchange.folderstorage.displayName |
| 3060 | com.openexchange.share.extendedPermissions |
### Detailed task and appointment data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 200 | title | 201 | start_date | 202 | end_date | 203 | note |
| 204 | alarm | 209 | recurrence_type | 212 | days | 213 | day_in_month |
| 214 | month | 215 | interval | 216 | until | 217 | notification |
| 220 | participants | 221 | users | 222 | occurrences |223 | uid |
| 224 | organizer | 225 | sequence | 226 | confirmations | 227 | organizerId |
| 228 | principal | 229 | principalId | 401 | full_time |
### Detailed task data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 300 | status | 301 | percent_completed | 302 | actual_costs | 303 | actual_duration |
| 304 | after_complete | 305 | billing_information | 307 | target_costs | 308 | target_duration |
| 309 | priority | 312 | currency | 313 | trip_meter | 314 | companies |
| 315 | date_completed | 316 | start_time | 317 | end_time |
### Detailed contact data
| ID | Name | CSV column title | ID | Name | CSV column title | ID | Name | CSV column title |
|----|------|------------------|----|------|------------------|----|------|------------------|
| 223 | uid |   | 500 | display_name | Display name | 501 | first_name | Given name |
| 502 | last_name | Sur name | 503 | second_name | Middle name | 504 | suffix | Suffix |
| 505 | title | Title | 506 | street_home | Street home | 507 | postal_code_home | Postal code home |
| 508 | city_home | City home | 509 | state_home | State home | 510 | country_home | Country home |
| 511 | birthday | Birthday | 512 | martial_status | Martial status | 513 | number_of_children | Number of children |
| 514 | profession | Profession | 515 | nickname | Nickname | 516 | spouse_name | Spouse name |
| 517 | anniversary | Anniversary | 518 | note | Note | 519 | department | Department |
| 520 | position | Position | 521 | employee_type | Employee type | 522 | room_number | Room number |
| 523 | street_business | Street business | 524 | user_id | Internal user id | 525 | postal_code_business | Postal code business |
| 526 | city_business | City business | 527 | state_business | State business | 528 | country_business | Country business |
| 529 | number_of_employees | Number of employee | 530 | sales_volume | Sales volume | 531 | tax_id | Tax id |
| 532 | commercial_register | Commercial register | 533 | branches | Branches | 534 | business_category | Business category |
| 535 | info | Info | 536 | manager_name | Manager's name | 537 | assistant_name | Assistant's name |
| 538 | street_other | Street other | 539 | city_other | City other | 540 | postal_code_other | Postal code other |
| 541 | country_other | Country other | 542 | telephone_business1 | Telephone business 1 | 543 | telephone_business2 | Telephone business 2 |
| 544 | fax_business | FAX business | 545 | telephone_callback | Telephone callback | 546 | telephone_car | Telephone car |
| 547 | telephone_company | Telephone company | 548 | telephone_home1 | Telephone home 1 | 549 | telephone_home2 | Telephone home 2 |
| 550 | fax_home | FAX home | 551 | cellular_telephone1 | Cellular telephone 1 | 552 | cellular_telephone2 | Cellular telephone 2 |
| 553 | telephone_other | Telephone other | 554 | fax_other | FAX other | 555 | email1 | Email 1 |
| 556 | email2 | Email 2 | 557 | email3 | Email 3 | 558 | url | URL |
| 559 | telephone_isdn | Telephone ISDN | 560 | telephone_pager | Telephone pager | 561 | telephone_primary | Telephone primary |
| 562 | telephone_radio | Telephone radio | 563 | telephone_telex | Telephone telex | 564 | telephone_ttytdd | Telephone TTY/TDD |
| 565 | instant_messenger1 | Instantmessenger 1 | 566 | instant_messenger2 | Instantmessenger 2 | 567 | telephone_ip | Telephone IP |
| 568 | telephone_assistant | Telephone assistant | 569 | company | Company | 570 | image1 |   |
| 571 | userfield01 | Dynamic Field 1 | 572 | userfield02 | Dynamic Field 2 | 573 | userfield03 | Dynamic Field 3 |
| 574 | userfield04 | Dynamic Field 4 | 575 | userfield05 | Dynamic Field 5 | 576 | userfield06 | Dynamic Field 6 |
| 577 | userfield07 | Dynamic Field 7 | 578 | userfield08 | Dynamic Field 8 | 579 | userfield09 | Dynamic Field 9 |
| 580 | userfield10 | Dynamic Field 10 | 581 | userfield11 | Dynamic Field 11 | 582 | userfield12 | Dynamic Field 12 |
| 583 | userfield13 | Dynamic Field 13 | 584 | userfield14 | Dynamic Field 14 | 585 | userfield15 | Dynamic Field 15 |
| 586 | userfield16 | Dynamic Field 16 | 587 | userfield17 | Dynamic Field 17 | 588 | userfield18 | Dynamic Field 18 |
| 589 | userfield19 | Dynamic Field 19 | 590 | userfield20 | Dynamic Field 20 | 592 | distribution_list |   |
| 594 | number_of_distribution_list | Number of distributionlists | 596 | number_of_images |   | 597 | image_last_modified |   |
| 598 | state_other | State other | 599 | file_as |   | 601 | image1_content_type |   |
| 602 | mark_as_distributionlist |   | 605 | default_address | Default address | 606 | image1_url |   |
| 608 | useCount |   | 610 | yomiFirstName |   | 611 | yomiLastName |   |
| 612 | yomiCompany |   | 613 | addressHome |   | 614 | addressBusiness |   |
| 615 | addressOther |   |
### Detailed appointment data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 206 | recurrence_id | 207 | recurrence_position | 208 | recurrence_date_position | 210 | change_exceptions |
| 211 | delete_exceptions | 400 | location | 402 | shown_as | 408 | timezone |
| 410 | recurrence_start |
### Detailed mail data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 102 | color_label | 600 | id | 601 | folder_id | 602 | attachment |
| 603 | from | 604 | to | 605 | cc | 606 | bcc |
| 607 | subject | 608 | size | 609 | sent_date | 610 | received_date |
| 611 | flags | 612 | level | 613 | disp_notification_to | 614 | priority |
| 615 | msg_ref | 651 | flag_seen | 652 | account_name | 653 | account_id |
| 654 | original_id | 655 | original_folder_id |
### Detailed infoitem data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 108 | object_permissions | 109 | shareable | 700 | title | 701 | url |
| 702 | filename | 703 | file_mimetype | 704 | file_size | 705 | version |
| 706 | description | 707 | locked_until | 708 | file_md5sum | 709 | version_comment |
| 710 | current_version | 711 | number_of_versions | 7010 | com.openexchange.share.extendedObjectPermissions | 7020 | com.openexchange.realtime.resourceID |
### Attachment data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 800 | folder | 801 | attached | 802 | module | 803 | filename |
| 804 | file_size | 805 | file_mimetype | 806 | rft_flag |
### Mail account data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 1001 | id | 1002 | login | 1003 | password | 1004 | mail_url |
| 1005 | transport_url | 1006 | name | 1007 | primary_address | 1008 | spam_handler |
| 1009 | trash | 1010 | sent | 1011 | drafts | 1012 | spam |
| 1013 | confirmed_spam | 1014 | confirmed_ham | 1015 | mail_server | 1016 | mail_port |
| 1017 | mail_protocol | 1018 | mail_secure | 1019 | transport_server | 1020 | transport_port |
| 1021 | transport_protocol | 1022 | transport_secure | 1023 | transport_login | 1024 | transport_password |
| 1025 | unified_inbox_enabled | 1026 | trash_fullname | 1027 | sent_fullname | 1028 | drafts_fullname |
| 1029 | spam_fullname | 1030 | confirmed_spam_fullname | 1031 | confirmed_ham_fullname | 1032 | pop3_refresh_rate |
| 1033 | pop3_expunge_on_quit | 1034 | pop3_delete_write_through | 1035 | pop3_storage | 1036 | pop3_path |
| 1037 | personal | 1038 | reply_to | 1039 | addresses | 1040 | meta |
| 1041 | archive | 1042 | archive_fullname | 1043 | transport_auth | 1044 | mail_starttls |
| 1045 | transport_starttls |
### Detailed user data
| ID | Name | ID | Name | ID | Name | ID | Name |
|----|------|----|------|----|------|----|------|
| 610 | aliases | 611 | timezone | 612 | locale | 613 | groups |
| 614 | contact_id | 615 | login_info | 616 | guest_created_by |
### Messaging fields
| Name | Name | Name | Name | Name | Name | Name | Name | Name | Name |
|------|------|------|------|------|------|------|------|------|------|
| id | folder | from | to | cc | bcc | headers | subject | body | contentType |
| size | sentDate | receivedDate | flags | threadLevel | dispositionNotificationTo | priority | colorLabel | url | picture |

## Flags / bit masks
### Permission flags
| Bits | Value | Bits | Value |
|------|-------|------|-------|
| **0-6** | **Folder permissions:** | **21-27** | **Delete permissions for objects in folder:** |
|   | 0 (no permissions) |   | 0 (no permissions) |
|   | 1 (see the folder) |   | 1 (delete only own objects) |
|   | 2 (create objects in folder) |   | 2 (delete all objects) |
|   | 4 (create subfolders) |   | 64 (all permissions) |
|   | 64 (all permissions) |
| **7-13** | **Read permissions for objects in folder:** | **28** | **Admin flag:** |
|   | 0 (no permissions) |   | 0 (no permissions) |
|   | 1 (read only own objects) |   | 1 (every operation modifying the folder in some way requires this permission (e.g. changing the folder name) |
|   | 2 (read all objects) |
|   | 64 (all permissions) |
| **14-20** | **Write permissions for objects in folder:** |
|   | 0 (no permissions) |
|   | 1 (modify only own objects) |
|   | 2 (modify all objects) |
|   | 64 (all permissions) |

## Mail filter
### Possible tests
| Name | Description |
|------|-------------|
| address | This test type applies to addresses only. So it may be used for all header fields which contain addresses. This test returns true if any combination of the header-list and values-list arguments match. |
| envelope | This test applies to the envelope of a mail. This test isn't used under normal circumstances as the envelope isn't accessible in all mail setups. This test returns true if any combination of the header-list and values-list arguments match. |
| true | A test for a true result (can be used if an action command should be executed every time). |
| not | Negates a given test. |
| size | Deals with the size of the mail. |
| currentdate | Compares a given date with the current date (available since v6.20) |
| header | Tests against all headers of a mail. So with this test in contrast to the address test also fields such as subject can be handled. This test returns true if any combination of the header-list and values-list arguments match. |
| body | Tests against the content of a mail. |
| allof | Defines an AND condition between several tests. |
| anyof | Defines an OR condition between several tests. |
### Possible comparisons
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| is | If a field is equal to a given value. | contains | If a field contains a given value at any position. |
| matches | Tests if the value matches the value in the specified field ("*" matches zero or more characters, "?" matches a single character, to use these characters themselves they have to be escaped via backslash). | regex | Tests if a given regular expression matches with the value present in the specified field. |
| user | Tests if the user part of an e-mail address is the value given here. This means in herbert+mustermann@example.com. The user checks the part herbert (only possible in conjunction with the `address` test). | detail | Tests if the detail part of an e-mail address is the value given here. In the example above this evaluates to mustermann (only possible in conjunction with the `address` test). |
#### Possible currentdate comparisons
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| is | Used in the date test to check for a value equal to the given one. | ge | Used in the date test to check for a value greater or equal to the given one. |
| le | Used in the date test to check for a value less or equal to the given one. |   |   |
#### Possible size comparisons
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| over | Used in the size test to check for a value greater than the given one. | under | Used in the size test to check for a value less than the given one. |
### Possible extensions
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| content | An extension used in conjunction with the body test to define the content which should be considered. This extension will need a parameter specifying the mime-type of the part of the message which will be searched. | text | An extension used in conjunction with the body test to define that only the text of the body should be considered in this test. This extension takes no parameter. |
### Possible action commands
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| keep | Keeps a mail non-changed. | discard | Discards a mail without any processing. |
| redirect | Redirects a mail to a given e-mail address. | move | Moves a mail into a given subfolder (the syntax of the subfolder given here must be the correct syntax of the underlying IMAP-server and is up to the GUI to detect things such as altnamespace or unixhierarchysep). |
| reject | Rejects the mail with a given text. | stop | Stops any further progressing of a mail. |
| vacation | Creates a vacation mail. | addflags | Adds flags to a mail. |
| notify | Adds a notification. | pgp | Encrypts a mail via pgp. |

## Advanced search
This section describes the syntax of the JSON object representing the search term. The search term is embedded
in a JSON object with the field `filter`, like `{"filter":[search term]}`. In general the structure of a search
term is in prefix notation, meaning the operator is written before its operands: `[">", 5, 2]` represents the condition
"5 > 2".

Available operators are:
 * comparision operators ">", "<", "=", "<=", ">=", "<>"
 * logic operators "not", "and", "or"

Comparison operators have exactly two operands. Each operand can either be a field name or a constant. A field name is a JSON object
with the member `field` specifying the field name, e.g. `{"field":"first_name"}`. The available field names depend on the module
that implements the search. Primitive JSON types are interpreted as constants. Arrays are not valid operands for comparison operators!

The logic operator "not" has exactly one operand, the other logic operators can have any number of operands. Each operand must be an
array representing a nested search expression.

Example:
```json
{
  "filter":[
    "and",
    [
      "=",
      {
        "field":"field_name1"
      },
      "value1"
    ],
    [
      "not",
      [
        ">",
        {
          "field":"field_name2"
        },
        "value2"
      ]
    ]
  ]
}
```
Represents the expression `field_name1 = value1 AND NOT field_name2 > value2`.
___


### Version information
Version: 7.8.2

### Contact information
Contact: Open-Xchange GmbH
Contact Email: info@open-xchange.com

### URI scheme
Host: example.com
BasePath: /ajax
Schemes: HTTPS

### Tags

* login: The login module is used to obtain a session from the user's login credentials. To understand the details
of the different login methods, see the article titled "[Login variations](http://oxpedia.org/wiki/index.php?title=Login_variations)".

Because of security reasons each login variation will reject requests containing the parameter
"password" within the URL query (starting with 7.8.0). 

* config: The config module is used to retrieve and set user-specific configuration. The configuration is
stored in a tree. Each node of the tree has a name and a value. The values of leaf nodes are strings
which store the actual configuration data. The values of inner nodes are defined recursively as
objects with one field for each child node. The name and the value of each field is the name and the
value of the corresponding child node, respectively.

* folders: The folders module is used to access the OX folder structure.

Folders with some kind of special:

| ID | Type | Description |
|----|------|-------------|
| 6 | contacts | System Users |

* tasks: The tasks module is used to access task information.
* contacts: The contacts module is used to access contact information.
* calendar: The calendar module is used to access calendar data.
* mail: The mail module is used to access mail data. When mails are stored on an IMAP server, some functionality is not available due to restrictions of the IMAP protocol. Such functionality is marked with "not IMAP".
* groups: The group module allows to query available groups. It is mainly used by the dialog for the selection of participants.
* resources: The resource module allows to query available resources. It is mainly used by the dialog for the selection of participants.
* infostore: The module infostore or filestore or files or drive has been renamed quite often. Whatever its name, it combines the knowledge database, bookmarks and document storage.
* attachments: The module attachments allows file attachments to arbitrary objects. An attachment always belongs to an object (called 'attached') in a certain folder of a certain module.
* reminder: The reminder module provides the ability to fetch all active reminders for a user between two dates.
* multiple: The multiple module allows to bundle multiple requests to most other modules in a single request.
* quota: The filestore module allows accesssing information about the use and quota of the filestore.
* import: The module import allows to import specific module data (like Contacts, Tasks or Appointments) in several formats (iCal, vCard, CSV) into a folder. Please note: The callback for all actions of this bundle is `callback_import`, not `callback_$actionname` for legacy purposes.
* export: The module export allows to export specific module data (like contacts, tasks or appointments) from a folder in several formats (iCal, vCard, CSV).
* sync: The module sync delivers several core API extensions to support common operations used in a mobile synchronization environment.
* token: The module token delivers several core API extensions to support token based logins.
* mailfilter: The mailfilter module is used to access all mail filter related options.

First of all the main structure of a mail filter script is, that it has different rules. Each of them contains one command. This command takes a test condition which executes the actions given in that command if the test condition is true.
The test condition consists of a test command and some arguments for this command depending on the command itself. Because the available tests depend on the mail filter server, these tests must be determined at runtime. So that no test field is transferred to the server which it isn't able to handle. Examples for tests are `address`, `allof` and `anyof`.
Each test has a special comparison. The list of available comparisons depends on the test given and the mail filter server configuration so they have to be determined at runtime too.

* file: The ajax file upload module offers to store files in server's dedicated download directory for a configureable amount of time. The files are then accessible for further operations like inline images in (html) mails.
* image: The image module allows to download images from Open-Xchange server without providing a session ID in request's URL parameters.
* conversion: The conversion module is a generic module to request data from a data source and to process obtained/submitted data with a data handler. Thus data is converted from a data source by a data handler.
* mailaccount: The mailaccount module is used to manage multiple mail accounts held by a user (available since v6.12).
* autoconfig: The module autoconfig can be used to request the best available settings for an appropriate mail server (available since v6.22).
* user: The user module is used to access user information (available since v6.14).
* user/me: The module user/me is used to access formal information about current user (available since v7.6.2).
* OAuth: The Open-Xchange server can act as an OAuth client (starting with v6.20) or be an OAuth provider itself (starting with v7.8.0). The OAuth module supports both aspects:
 * Manage multiple OAuth accounts for certain online services for a user. The OAuth mechanism allows the Open-Xchange application to act as behalf of this user using previously obtained access tokens granted by user. The according interface is divided into two parts: Account access and service's meta data access.
 * Manage granted accesses of external services that can access a users data on his behalf, called "grants".

* JSlob: The JSlob module is used to store and retrieve arbitrary JSON-structured configuration for a single user (available since v6.22).
* freebusy: Provides access to free/busy information (available since v6.22.1).
* messaging: The messaging module is divided into services which represent a messaging backend (they add a new folder module "messaging"),
into accounts which represent the concrete configuration of accounts for a given messaging service, and into messages which
represent single messages consisting of some metadata, headers and a content.

* snippet: Available since v7.0.0/v6.22.0.
* halo
* capabilities: Provides access to capabilities, i.e. modules or features that are available on the backend and the user has access to (available since v7.4.2).
* jump: The jump module is used to pass an acquired identity token for an authenticated user from one system to another for a single sign-on (available since v7.6.0).
* find: The find module consists of calls for performing searches within the modules mail, contacts, tasks and drive.
It was designed to provide an iterative approach where the search criteria can be refined step-wise until
the desired items are found.  The starting point is always an `autocomplete` request, that suggests possible
search filters based on a users input. Those filters are grouped into categories, called "facets".
A facet may provide one or more values with every value being a possible filter. A client is meant to remember
every value that was selected by a user and include it within the following `autocomplete` and `query` requests,
while `query` performs the actual search and returns the found items.

Some of the objects returned by the server contain former user input. A client must never interpret strings as HTML
but always as plain text to be not vulnerable for CSS attacks!

Preliminary, available since v7.6.1.

* share/management: The share/management module can create and manage share links via different actions. Dedicated actions to list all shares of a
user can be found in the corresponding modules, like `/folders?action=shares` of module "folders" or `/infostore?action=shares`
of module "infostore".

Preliminary, available since v7.8.0.

* passwordchange: Via the passwordchange module the users can change their password.
* filestorage: The file storage module provides access to file storage backends, e.g. Drive, Dropbox, etc.
* mail_categories: The module mail_categories allows to manage mail categories.


### Consumes

* application/x-www-form-urlencoded


### Produces

* application/json


## Paths
### Gets all mail accounts (**available since v6.12**).
```
GET /account?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Mail account data](#mail-account-data).|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all mail accounts. Each array element describes one account and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountsResponse|


#### Tags

* mailaccount

### Deletes a mail account (**available since v6.12**).
```
PUT /account?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array with the ID of the mail account that shall be deleted.|true|integer array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with identifiers of deleted accounts. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountDeletionResponse|


#### Consumes

* application/json

#### Tags

* mailaccount

### Gets a mail account (**available since v6.12**).
```
GET /account?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Account ID of the requested account.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested account. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailAccountResponse|


#### Tags

* mailaccount

### Creates a new mail account (**available since v6.12**).
```
PUT /account?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the new account to create.|true|MailAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the inserted mail account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountUpdateResponse|


#### Consumes

* application/json

#### Tags

* mailaccount

### Updates a mail account (**available since v6.12**).
```
PUT /account?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object identifying (by field `id`) and describing the account to update. Only modified fields are present.|true|MailAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the updated mail account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailAccountUpdateResponse|


#### Consumes

* application/json

#### Tags

* mailaccount

### Validates a mail account which shall be created (**available since v6.12**).
```
PUT /account?action=validate
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|Indicates whether on successful validation the folder tree shall be returned (or `null`on failure) or
if set to `false` or missing only a boolean is returned which indicates validation result.
|false|boolean||
|BodyParameter|body|A JSON object describing the account to validate.|true|MailAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object that contains a value representing the validation result (may be a boolean or a folder tree object).
If the validation fails then the error fields are filled and an additional `warnings` field might be added.
|MailAccountValidationResponse|


#### Consumes

* application/json

#### Tags

* mailaccount

### Gets all attachments for an object.
```
GET /attachment?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all attachments. Each array element describes one attachment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|AttachmentsResponse|


#### Tags

* attachments

### Creates an attachment.
```
POST /attachment?action=attach
```

#### Description

#### Note
It is possible to create multiple attachments at once. Therefor add additional form fields and replace "[index]" in `json_[index]`
and `file_[index]` with the appropriate index, like `json_1`, `file_1`. The index always starts with 0 (mandatory attachment object).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|FormDataParameter|json_0|A JSON string representing an attachment object as described in [AttachmentData](#/definitions/AttachmentData) model with at least the fields `folder`, `attached` and `module`.|true|string||
|FormDataParameter|file_0|The attachment file as per `<input type="file" />`.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON array of object IDs of the newly created attachments or errors if some occurred.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* attachments

### Deletes attachments.
```
PUT /attachment?action=detach
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|BodyParameter|body|A JSON array with the identifiers of the attachments that shall be deleted.|true|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Consumes

* application/json

#### Tags

* attachments

### Gets an attachment's document/filedata.
```
GET /attachment?action=document
```

#### Description

It is possible to add a filename to the request's URI like `/attachment/{filename}?action=document`.
The filename may be added to the customary attachment path to suggest a filename to a Save-As dialog.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|id|Object ID of the requested attachment.|true|string||
|QueryParameter|content_type|If present the response declares the given `content_type` in the Content-Type header and not the attachments file MIME type.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the document. The response type for the HTTP request is set accordingly to the defined MIME type for this attachment or the content_type given.|string (binary)|


#### Produces

* application/octet-stream

#### Tags

* attachments

### Gets an attachment.
```
GET /attachment?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested infoitem.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested attachment. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|AttachmentResponse|


#### Tags

* attachments

### Gets a list of attachments.
```
PUT /attachment?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|attached|The ID of the object to which the attachment belongs.|true|integer||
|QueryParameter|folder|The folder ID of the object.|true|integer||
|QueryParameter|module|The module type of the object: 1 (appointment), 4 (task), 7 (contact), 137 (infostore).|true|integer||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,800". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Attachment data](#attachment-data).|true|string||
|BodyParameter|body|A JSON array with the identifiers of the requested attachments.|true|integer array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested infoitems. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


#### Consumes

* application/json

#### Tags

* attachments

### Gets the new and deleted attachments.
```
GET /attachment?action=updates
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new and deleted attachments. New attachments are represented by arrays. The
elements of each array contain the information specified by the corresponding identifiers in the `columns`
parameter. Deleted attachments would be identified by their object IDs as integer, without being part of
a nested array. In case of errors the responsible fields in the response are filled (see
[Error handling](#error-handling)).
|AttachmentUpdatesResponse|


#### Tags

* attachments

### Gets the auto configuration for a mail account (**available since v6.22**).
```
POST /autoconfig?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|FormDataParameter|email|The email address for which a mail configuration will be discovered.|true|string||
|FormDataParameter|password|The corresponding password for the mail account.|true|string||
|FormDataParameter|force_secure|Enforces a secure connection for configured mail account, default is `true` (available since v7.8.2).|false|boolean||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the best available settings for an appropriate mail server for the given email
address. The data may be incomplete or empty. In case of errors the responsible fields in the response
are filled (see [Error handling](#error-handling)).
|AutoConfigResponse|


#### Tags

* autoconfig

### Gets all appointments.
```
GET /calendar?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which start on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which end before this date are returned.|true|integer (int64)||
|QueryParameter|folder|Object ID of the folder, whose contents are queried. If not specified, defaults to all calendar folders.|false|string||
|QueryParameter|recurrence_master|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|false|boolean||
|QueryParameter|showPrivate|Only works in shared folders: When enabled, shows private appointments of the folder owner. Such appointments are anonymized by stripping away all information except start date, end date and recurrence information (since 6.18)|false|boolean||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with appointment data. Each array element describes one appointment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. Appointment sequencies are broken up into individual appointments
and each occurrence of a sequence in the requested range is returned separately. The appointments are
sorted in ascending order by the field `start_date`. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


#### Tags

* calendar

### Confirms an appointment.
```
PUT /calendar?action=confirm
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the appointment that shall be confirmed.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the appointments.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the appointment.|true|integer (int64)||
|QueryParameter|occurrence|The numeric identifier of the occurrence to which the confirmation applies (in case "id" denotes a series appointment). Available since v7.6.0.|false|integer||
|BodyParameter|body|A JSON object with the fields `confirmation`, `confirmmessage` and optionally `id`.|true|AppointmentConfirmationBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Nothing, except the standard response object with empty data, the timestamp of the confirmed and thereby
updated appointment, and maybe errors. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Consumes

* application/json

#### Tags

* calendar

### Deletes appointments (**available since v6.22**).
```
PUT /calendar?action=delete
```

#### Description

Before version 6.22 the request body contained a JSON object with the fields `id`, `folder` and
optionally `pos` and could only delete one appointment.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the deleted appointments.|true|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id, folder and optionally the recurrence position (if present in an appointment to fully identify it) of the appointments.|true|AppointmentDeletionsElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array of objects identifying the appointments which were modified after the specified timestamp and were therefore not deleted. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|AppointmentDeletionsResponse|


#### Consumes

* application/json

#### Tags

* calendar

### Gets appointments between a specified time range.
```
GET /calendar?action=freebusy
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Internal user id. Must be obtained from the contact module.|true|integer||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned.|true|integer (int64)||
|QueryParameter|type|Constant for user or resource (1 for users, 3 for resources).|true|enum (, )||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array of objects identifying the appointments which lie between start and end as described. In
case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentFreeBusyResponse|


#### Tags

* calendar

### Gets an appointment.
```
GET /calendar?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested appointment.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the appointments.|true|string||
|QueryParameter|recurrence_position|Recurrence position of requested appointment.|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested appointment. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentResponse|


#### Tags

* calendar

### Gets all change exceptions (**available since v7.2.0**).
```
GET /calendar?action=getChangeExceptions
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the appointment series.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the appointments.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with appointment data. Each array element describes one appointment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|AppointmentsResponse|


#### Tags

* calendar

### Requests whether there are appointments on days in a specified time range.
```
GET /calendar?action=has
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which start on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which end before this date are returned.|true|integer (int64)||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with the length of the number of days between `start` and `end`. Meaning,
each element corresponds with one day in the range that was queried, explaining whether there is an
appointment on this day (true) or not (false). In case of errors the responsible fields in the response
are filled (see [Error handling](#error-handling)).
|AppointmentInfoResponse|


#### Tags

* calendar

### Gets a list of appointments.
```
PUT /calendar?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|QueryParameter|recurrence_master|Extract the recurrence to several appointments. The default value is false so every appointment of the recurrence will be calculated.|false|boolean||
|BodyParameter|body|A JSON array of JSON objects with the id, folder and optionally either recurrence_position or recurrence_date_position of the requested appointments.|true|AppointmentListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested appointments. Each array element describes one appointment and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response
are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


#### Consumes

* application/json

#### Tags

* calendar

### Creates an appointment.
```
PUT /calendar?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the appointment's data.|true|AppointmentData||


#### Responses
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


#### Consumes

* application/json

#### Tags

* calendar

### Gets new appointments.
```
GET /calendar?action=newappointments
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|QueryParameter|start|Lower inclusive limit of the queried range as a Date. Only appointments which end on or after this date are returned.|true|integer (int64)||
|QueryParameter|end|Upper exclusive limit of the queried range as a Date. Only appointments which start before this date are returned.|true|integer (int64)||
|QueryParameter|limit|Limits the number of returned objects to the given value.|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified and holds a column number, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with appointment data. Appointments are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


#### Tags

* calendar

### Resolves the UID to an OX object ID.
```
GET /calendar?action=resolveuid
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|uid|The UID that shall be resolved.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the related object ID in the field `id`. If no object exists with the
specified UID or in case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|AppointmentUpdateResponse|


#### Tags

* calendar

### Searches for appointments.
```
PUT /calendar?action=search
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed appointment data](#detailed-appointment-data).|true|string||
|BodyParameter|body|A JSON object containing search parameters.|true|AppointmentSearchBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching appointments. Appointments are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentsResponse|


#### Consumes

* application/json

#### Tags

* calendar

### Updates an appointment.
```
PUT /calendar?action=update
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the id of the updated appointment. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|AppointmentUpdateResponse|


#### Consumes

* application/json

#### Tags

* calendar

### Gets updated appointments.
```
GET /calendar?action=updates
```

#### Parameters
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


#### Responses
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


#### Tags

* calendar

### Gets all capabilities (**available since v7.4.2**).
```
GET /capabilities?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all capabilities. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CapabilitiesResponse|


#### Tags

* capabilities

### Gets a capability (**available since v7.4.2**).
```
GET /capabilities?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the capability|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the capability or an empty result, if capability not available. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CapabilityResponse|


#### Tags

* capabilities

### Sets the value of a configuration node.
```
PUT /config/{path}
```

#### Description

The configuration is stored in a tree. Each node of the tree has a name and a value.
The values of leaf nodes are strings which store the actual configuration data. The
values of inner nodes are defined recursively as objects with one field for each child node.
The name and the value of each field is the name and the value of the corresponding child
node, respectively.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|PathParameter|path|The path to the node.|true|enum (gui, fastgui, context_id, cookielifetime, identifier, contact_id, language, timezone, availableTimeZones, calendarnotification, tasknotification, reloadTimes, serverVersion, currentTime, maxUploadIdleTimeout, search, folder, folder/tasks, folder/calendar, folder/contacts, folder/infostore, folder/eas, mail, mail/addresses, mail/defaultaddress, mail/sendaddress, mail/folder, mail/folder/inbox, mail/folder/drafts, mail/folder/trash, mail/folder/spam, mail/folder/sent, mail/htmlinline, mail/colorquote, mail/emoticons, mail/harddelete, mail/inlineforward, mail/vcard, mail/notifyonreadack, mail/msgpreview, mail/ignorereplytext, mail/nocopytosent, mail/spambutton, participants, participants/autoSearch, participants/maximumNumberParticipants, participants/showWithoutEmail, participants/showDialog, availableModules, minimumSearchCharacters, modules, modules/portal, modules/portal/gui, modules/portal/module, modules/mail, modules/mail/addresses, modules/mail/appendmailtext, modules/mail/allowhtmlimages, modules/mailcolorquoted, modules/mail/contactCollectFolder, modules/mail/contactCollectEnabled, modules/mail/contactCollectOnMailAccess, modules/mail/contactCollectOnMailTransport, modules/mail/defaultaddress, modules/mail/deletemail, modules/mail/emoticons, modules/mail/defaultFolder, modules/mail/defaultFolder/drafts, modules/mail/defaultFolder/inbox, modules/mail/defaultFolder/sent, modules/mail/defaultFolder/spam, modules/mail/defaultFolder/trash, modules/mail/forwardmessage, modules/mail/gui, modules/mail/inlineattachments, modules/mail/linewrap, modules/mail/module, modules/mail/phishingheaders, modules/mail/replyallcc, modules/mail/sendaddress, modules/mail/spambutton, modules/mail/vcard, modules/calendar, modules/calendar/calendar_conflict, modules/calendar/calendar_freebusy, modules/calendar/calendar_teamview, modules/calendar/gui, modules/calendar/module, modules/calendar/notifyNewModifiedDeleted, modules/calendar/notifyAcceptedDeclinedAsCreator, modules/calendar/notifyAcceptedDeclinedAsParticipant, modules/calendar/defaultStatusPrivate, modules/calendar/defaultStatusPublic, modules/contacts, modules/contacts/gui, modules/contacts/mailAddressAutoSearch, modules/contacts/module, modules/contacts/singleFolderSearch, modules/contacts/characterSearch, modules/contacts/allFoldersForAutoComplete, modules/tasks, modules/tasks/gui, modules/tasks/module, modules/tasks/delegate_tasks, modules/tasks/notifyNewModifiedDeleted, modules/tasks/notifyAcceptedDeclinedAsCreator, modules/tasks/notifyAcceptedDeclinedAsParticipant, modules/infostore, modules/infostore/gui, modules/infostore/folder, modules/infostore/folder/trash, modules/infostore/folder/pictures, modules/infostore/folder/documents, modules/infostore/folder/music, modules/infostore/folder/videos, modules/infostore/folder/templates, modules/infostore/module, modules/interfaces, modules/interfaces/ical, modules/interfaces/vcard, modules/interfaces/syncml, modules/folder, modules/folder/gui, modules/folder/public_folders, modules/folder/read_create_shared_folders, modules/folder/tree, modules/com.openexchange.extras, modules/com.openexchange.extras/module, modules/com.openexchange.user.passwordchange, modules/com.openexchange.user.passwordchange/module, modules/com.openexchange.user.personaldata, modules/com.openexchange.user.personaldata/module, modules/com.openexchange.group, modules/com.openexchange.group/enabled, modules/com.openexchange.resource, modules/com.openexchange.resource/enabled, modules/com.openexchange.publish, modules/com.openexchange.publish/enabled, modules/com.openexchange.subscribe, modules/com.openexchange.subscribe/enabled, modules/olox20, modules/olox20/active, modules/olox20/module, modules/com.openexchange.oxupdater, modules/com.openexchange.oxupdater/module, modules/com.openexchange.oxupdater/active, modules/com.openexchange.passwordchange, modules/com.openexchange.passwordchange/showStrength, modules/com.openexchange.passwordchange/minLength, modules/com.openexchange.passwordchange/maxLength, modules/com.openexchange.passwordchange/regexp, modules/com.openexchange.passwordchange/special)||
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the value of the config node.|true|ConfigBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* config

### Gets data of a configuration node.
```
GET /config/{path}
```

#### Description

The configuration is stored in a tree. Each node of the tree has a name and a value.
The values of leaf nodes are strings which store the actual configuration data. The
values of inner nodes are defined recursively as objects with one field for each child node.
The name and the value of each field is the name and the value of the corresponding child
node, respectively.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|PathParameter|path|The path to the node.|true|enum (gui, fastgui, context_id, cookielifetime, identifier, contact_id, language, timezone, availableTimeZones, calendarnotification, tasknotification, reloadTimes, serverVersion, currentTime, maxUploadIdleTimeout, search, folder, folder/tasks, folder/calendar, folder/contacts, folder/infostore, folder/eas, mail, mail/addresses, mail/defaultaddress, mail/sendaddress, mail/folder, mail/folder/inbox, mail/folder/drafts, mail/folder/trash, mail/folder/spam, mail/folder/sent, mail/htmlinline, mail/colorquote, mail/emoticons, mail/harddelete, mail/inlineforward, mail/vcard, mail/notifyonreadack, mail/msgpreview, mail/ignorereplytext, mail/nocopytosent, mail/spambutton, participants, participants/autoSearch, participants/maximumNumberParticipants, participants/showWithoutEmail, participants/showDialog, availableModules, minimumSearchCharacters, modules, modules/portal, modules/portal/gui, modules/portal/module, modules/mail, modules/mail/addresses, modules/mail/appendmailtext, modules/mail/allowhtmlimages, modules/mailcolorquoted, modules/mail/contactCollectFolder, modules/mail/contactCollectEnabled, modules/mail/contactCollectOnMailAccess, modules/mail/contactCollectOnMailTransport, modules/mail/defaultaddress, modules/mail/deletemail, modules/mail/emoticons, modules/mail/defaultFolder, modules/mail/defaultFolder/drafts, modules/mail/defaultFolder/inbox, modules/mail/defaultFolder/sent, modules/mail/defaultFolder/spam, modules/mail/defaultFolder/trash, modules/mail/forwardmessage, modules/mail/gui, modules/mail/inlineattachments, modules/mail/linewrap, modules/mail/module, modules/mail/phishingheaders, modules/mail/replyallcc, modules/mail/sendaddress, modules/mail/spambutton, modules/mail/vcard, modules/calendar, modules/calendar/calendar_conflict, modules/calendar/calendar_freebusy, modules/calendar/calendar_teamview, modules/calendar/gui, modules/calendar/module, modules/calendar/notifyNewModifiedDeleted, modules/calendar/notifyAcceptedDeclinedAsCreator, modules/calendar/notifyAcceptedDeclinedAsParticipant, modules/calendar/defaultStatusPrivate, modules/calendar/defaultStatusPublic, modules/contacts, modules/contacts/gui, modules/contacts/mailAddressAutoSearch, modules/contacts/module, modules/contacts/singleFolderSearch, modules/contacts/characterSearch, modules/contacts/allFoldersForAutoComplete, modules/tasks, modules/tasks/gui, modules/tasks/module, modules/tasks/delegate_tasks, modules/tasks/notifyNewModifiedDeleted, modules/tasks/notifyAcceptedDeclinedAsCreator, modules/tasks/notifyAcceptedDeclinedAsParticipant, modules/infostore, modules/infostore/gui, modules/infostore/folder, modules/infostore/folder/trash, modules/infostore/folder/pictures, modules/infostore/folder/documents, modules/infostore/folder/music, modules/infostore/folder/videos, modules/infostore/folder/templates, modules/infostore/module, modules/interfaces, modules/interfaces/ical, modules/interfaces/vcard, modules/interfaces/syncml, modules/folder, modules/folder/gui, modules/folder/public_folders, modules/folder/read_create_shared_folders, modules/folder/tree, modules/com.openexchange.extras, modules/com.openexchange.extras/module, modules/com.openexchange.user.passwordchange, modules/com.openexchange.user.passwordchange/module, modules/com.openexchange.user.personaldata, modules/com.openexchange.user.personaldata/module, modules/com.openexchange.group, modules/com.openexchange.group/enabled, modules/com.openexchange.resource, modules/com.openexchange.resource/enabled, modules/com.openexchange.publish, modules/com.openexchange.publish/enabled, modules/com.openexchange.subscribe, modules/com.openexchange.subscribe/enabled, modules/olox20, modules/olox20/active, modules/olox20/module, modules/com.openexchange.oxupdater, modules/com.openexchange.oxupdater/module, modules/com.openexchange.oxupdater/active, modules/com.openexchange.passwordchange, modules/com.openexchange.passwordchange/showStrength, modules/com.openexchange.passwordchange/minLength, modules/com.openexchange.passwordchange/maxLength, modules/com.openexchange.passwordchange/regexp, modules/com.openexchange.passwordchange/special)||
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Value of the node specified by path. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ConfigResponse|


#### Tags

* config

### Gets a property of the configuration (**available since v7.6.2**).
```
GET /config?action=get_property
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|name|The name of the property to return.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON response providing the property's name and its value. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ConfigPropertyResponse|


#### Tags

* config

### Sets a property of the configuration (**available since v7.6.2**).
```
PUT /config?action=set_property
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|name|The name of the property to return.|true|string||
|BodyParameter|body|A JSON object providing the value to set (Example: {"value":"test123"}).|true|ConfigPropertyBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON response providing the property's name and its value. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ConfigPropertyResponse|


#### Consumes

* application/json

#### Tags

* config

### Search for contacts by filter (**available since v6.20**).
```
PUT /contacts?action=advancedSearch
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Consumes

* application/json

#### Tags

* contacts

### Gets all contacts.
```
GET /contacts?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all contacts. Each array element describes one contact and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Tags

* contacts

### Search for contacts by anniversary (**available since v6.22.1, preliminary**).
```
GET /contacts?action=anniversaries
```

#### Description

Finds contacts whose anniversary falls into a specified time range.

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Tags

* contacts

### Auto-complete conntacts (**available since v7.6.1, preliminary**).
```
GET /contacts?action=autocomplete
```

#### Description

Finds contacts based on a prefix, usually used to auto-complete e-mail recipients while the user is typing.

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the contact data. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Tags

* contacts

### Search for contacts by birthday (**available since v6.22.1, preliminary**).
```
GET /contacts?action=birthdays
```

#### Description

Finds contacts whose birthday falls into a specified time range.

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Tags

* contacts

### Deletes contacts (**available since v6.22**).
```
PUT /contacts?action=delete
```

#### Description

Before version 6.22 the request body contained a JSON object with the fields `id` and `folder` and could
only delete one contact.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the deleted contacts.|true|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the contacts.|true|ContactListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array with object IDs of contacts which were modified after the specified timestamp and were therefore not deleted.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactDeletionsResponse|


#### Consumes

* application/json

#### Tags

* contacts

### Gets a contact.
```
GET /contacts?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested contact.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested contact. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactResponse|


#### Tags

* contacts

### Gets a contact by user ID (**available since SP4**).
```
GET /contacts?action=getuser
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|User ID (not Object ID) of the requested user.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested contact. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactResponse|


#### Tags

* contacts

### Gets a list of contacts.
```
PUT /contacts?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the contacts.|true|ContactListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested contacts. Each array element describes one contact and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Consumes

* application/json

#### Tags

* contacts

### Gets a list of users (**available since SP4**).
```
PUT /contacts?action=listuser
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|BodyParameter|body|A JSON array with user IDs.|true|integer array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with contact data. Each array element describes one contact and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Consumes

* application/json

#### Tags

* contacts

### Creates a contact.
```
PUT /contacts?action=new
```

#### Description

Creates a new contact. This request cannot add contact images. Therefor it
is necessary to use the `POST` method.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the contact's data. The field id is not included.|true|ContactData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created contact. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactUpdateResponse|


#### Consumes

* application/json

#### Tags

* contacts

### Creates a contact.
```
POST /contacts?action=new
```

#### Description

Creates a new contact with contact images. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the contact's data while the image file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|FormDataParameter|json|Represents the normal request body as JSON string containing the contact data as described in the [ContactData](#/definitions/ContactData) model.|true|string||
|FormDataParameter|file|The image file.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the contact or errors if some occurred.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* contacts

### Search for contacts.
```
PUT /contacts?action=search
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|collation|Allows you to specify a collation to sort the contacts by. As of 6.20, only supports "gbk" and "gb2312", not needed for other languages. Parameter sort should be set for this to work. (preliminary, since 6.20)|false|enum (gbk, gb2312)||
|BodyParameter|body|A JSON object containing search parameters.|true|ContactSearchBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching contacts. Contacts are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactsResponse|


#### Consumes

* application/json

#### Tags

* contacts

### Updates a contact.
```
PUT /contacts?action=update
```

#### Description

Updates a contact's data. This request cannot change or add contact images. Therefore it
is necessary to use the `POST` method.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|id|Object ID of the contact that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the updated contact. If the contact was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the contact's data. Only modified fields must be specified. To remove some contact image send the image attribute set to null.|true|ContactData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with a timestamp. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ContactUpdateResponse|


#### Consumes

* application/json

#### Tags

* contacts

### Updates a contact.
```
POST /contacts?action=update
```

#### Description

Updates a contact's data and images. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the contact's data while the image file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|id|Object ID of the contact that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the updated contact. If the contact was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|FormDataParameter|json|Represents the normal request body as JSON string containing the contact data as described in [ContactData](#/definitions/ContactData) model. Only modified fields must be specified but at least "{}".|true|string|{}|
|FormDataParameter|file|The image file.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the contact or errors if some occurred.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* contacts

### Gets updated contacts.
```
GET /contacts?action=updates
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the contacts.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,500". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed contact data](#detailed-contact-data).|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested contacts.|true|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Omit this parameter or set it to "deleted" to not have deleted tasks identifier in the response. Set this parameter to `false` and the response contains deleted tasks identifier.|false|enum (deleted)||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new, modified and deleted contacts. New and modified contacts are represented by arrays.
The elements of each array contain the information specified by the corresponding identifiers in the
`columns` parameter. Deleted contacts (should the ignore parameter be ever implemented) would be identified
by their object IDs as integers, without being part of a nested array. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|ContactUpdatesResponse|


#### Tags

* contacts

### Converts data from source using a specific data handler.
```
PUT /conversion?action=convert
```

#### Description

#### Saving an iCal email attachment
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
#### Converting an iCal email attachment into JSON objects
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
#### Saving a vCard email attachment
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
#### Contact(s) attached to a new email as a vCard file
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


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object the data source object and the data handler object.|true|ConversionBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The conversion result. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ConversionResponse|


#### Consumes

* application/json

#### Tags

* conversion

### Exports contact data to a CSV file.
```
GET /export?action=CSV
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder whose content shall be exported. This must be a contact folder.|true|string||
|QueryParameter|columns|A comma-separated list of columns to export, like "501,502". A column is specified by a numeric column identifier, see [Detailed contact data](#detailed-contact-data).|false|string||
|QueryParameter|export_dlists|Toggles whether distribution lists shall be exported too (default is `false`). (since 7.4.1)|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An input stream containing the content of the CSV file with the MIME type `text/csv`.|string|


#### Tags

* export

### Exports appointment and task data to an iCalendar file.
```
GET /export?action=ICAL
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder whose content shall be exported. This must be a calendar folder.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An input stream containing the content of the iCal file with the MIME type `text/calendar`.|string|


#### Tags

* export

### Exports contact data to a vCard file.
```
GET /export?action=VCARD
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder whose content shall be exported. This must be a contact folder.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An input stream containing the content of the vCard file with the MIME type `text/x-vcard`.|string|


#### Tags

* export

### Requests a formerly uploaded file.
```
GET /file?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the uploaded file.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested file is directly written into output stream.|string (binary)|
|404|Not found.|No Content|


#### Produces

* application/octet-stream

#### Tags

* file

### Updates a file's last access timestamp and keeps it alive.
```
GET /file?action=keepalive
```

#### Description

By updating the last access timestamp the file is prevented from being deleted from both session and disk
storage.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the uploaded file whose timestamp should be updated.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Tags

* file

### Uploads a file.
```
POST /file?action=new
```

#### Description

It can be uploaded multiple files at once. Each file must be specified in an own form field
(the form field name is arbitrary).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|module|The module for which the file is uploaded to determine proper upload quota constraints (e.g. "mail", "infostore", etc.).|true|string||
|QueryParameter|type|The file type filter to define which file types are allowed during upload. Currently supported filters are: file (for all), text (for `text/*`), media (for image, audio or video), image (for `image/*`), audio (for `audio/*`), video (for `video/*`) and application (for `application/*`).|true|enum (file, text, media, image, audio, video, application)||
|FormDataParameter|file|The file to upload.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON array with the IDs of
the uploaded files or errors if some occurred. The files are accessible through the returned IDs
for future use.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* file

### Gets all file storage accounts.
```
GET /fileaccount?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|filestorageService|The identifier of a file storage service to list only those accounts that belong to that file storage service.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with JSON objects each describing one file storage account. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FileAccountsResponse|


#### Tags

* filestorage

### Deletes a file storage account.
```
GET /fileaccount?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|filestorageService|The identifier of the file storage service the account belongs to.|true|string||
|QueryParameter|id|The ID of the account to delete.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 on success. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountUpdateResponse|


#### Tags

* filestorage

### Gets a file storage account.
```
GET /fileaccount?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|filestorageService|The identifier of the file storage service the account belongs to.|true|string||
|QueryParameter|id|The ID of the requested account.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the file storage account. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountResponse|


#### Tags

* filestorage

### Creates a file storage account.
```
PUT /fileaccount?action=new
```

#### Description

#### Example for creating a new OAuth-based file storage account
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


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the account to create, with at least the field `filestorageService` set.|true|FileAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created account. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountCreationResponse|


#### Consumes

* application/json

#### Tags

* filestorage

### Updates a file storage account.
```
PUT /fileaccount?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the updated data of the account. The fields `id` and `filestorageService` must be set.|true|FileAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 on success. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileAccountCreationResponse|


#### Consumes

* application/json

#### Tags

* filestorage

### Gets all file storage services.
```
GET /fileservice?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with JSON objects each describing one file storage service. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FileServicesResponse|


#### Tags

* filestorage

### Gets a file storage service.
```
GET /fileservice?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the file storage service to load.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the file storage service. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FileServiceResponse|


#### Tags

* filestorage

### Suggests possible search filters based on a user's input (**available since v7.6.1**).
```
PUT /find?action=autocomplete
```

#### Description

Filters are grouped into categories, the so called facets.
#### Facets
The style of a facet is responsible for how the according object is structured, how it is handled on the
server-side and how the client has to handle it. We distinguish three styles of facets:
 * simple
 * default
 * exclusive
___
Every facet value contains an embedded `filter` object. The filter must not be changed by the client, it has
to be seen as a black-box. Instead the filters of selected facet values have to be copied and sent to the
server with the subsequent requests.
#### Simple facets
A simple facet is a special facet that has exactly one value. The facet's type and its value are strictly
coupled, in a way that a display name for both, facet and value would be redundant. A simple facet generally
denotes a logical field like 'phone number'. Internally this logical field can map to several internal
fields (e.g. 'phone_private', 'phone_mobile', 'phone_business'). In clients the facet as a whole can be
displayed as a single item. Example: "Search for 'term' in field 'phone number'".
#### Default facets
A default facet contains multiple values and may be present multiple times in search requests to filter
results by a combination of different values (e.g. "mails with 'foo' and 'bar' in subject").

Facet values may be one- or two-dimensional. A one-dimensional value can be displayed as is and contains an
according filter object. A two-dimensional value contains an array "options" with every option defining
different semantics of how the value is used to filter the search results.
#### Exclusive facets
An exclusive facet is a facet where the contained values are mutually exclusive. That means that the facet
must only be present once in an autocomplete or query request.

Facet values may be one- or two-dimensional. A one-dimensional value can be displayed as is and contains an
according filter object. A two-dimensional value contains an array "options" with every option defining
different semantics of how the value is used to filter the search results.


#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the facets that were found. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FindAutoCompleteResponse|


#### Consumes

* application/json

#### Tags

* find

### Performs the actual search and returns the found items (**available since v7.6.1**).
```
PUT /find?action=query
```

#### Description

Before querying the search you should fetch the search filters (facets) by calling the `/find?action=autocomplete`
request.
#### Active facets
Every value that has been selected by a user must be remembered and provided with every subsequent request.
The representation of a facet within a request body differs from the one within an autocomplete response.
We call those "active facets". Their representation is independent from their style.


#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the search result. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FindQueryResponse|


#### Consumes

* application/json

#### Tags

* find

### Gets all visible folders of a certain module (**available since v6.18.2**).
```
GET /folders?action=allVisible
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|content_type|The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "mail", "infostore").|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing three fields: "private", "public, and "shared". Each field is a
JSON array with data for all folders. Each folder is itself described by an array. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersVisibilityResponse|


#### Tags

* folders

### Clears the content of a list of folders.
```
PUT /folders?action=clear
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||
|BodyParameter|body|A JSON array with object IDs of the folders.|true|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array containing the IDs of folders that could not be cleared due to a concurrent modification.
Meaning you receive an empty JSON array if everything worked well. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|FoldersCleanUpResponse|


#### Consumes

* application/json

#### Tags

* folders

### Deletes a list of folders.
```
PUT /folders?action=delete
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with object IDs of folders that were NOT deleted. There may be a lot of different causes
for a not deleted folder: A folder has been modified in the mean time, the user does not have the
permission to delete it or those permissions have just been removed, the folder does not exist, etc.
You receive an empty JSON array if everything worked well. In case of errors the responsible fields
in the response are filled (see [Error handling](#error-handling)).
|FoldersCleanUpResponse|


#### Consumes

* application/json

#### Tags

* folders

### Gets a folder.
```
GET /folders?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested folder.|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the requested folder. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|FolderResponse|


#### Tags

* folders

### Gets the subfolders of a specified parent folder.
```
GET /folders?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|parent|Object ID of a folder, which is the parent folder of the requested folders.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|all|Set to 1 to list even not subscribed folders.|false|integer||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||
|QueryParameter|errorOnDuplicateName||false|boolean||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all folders, which have the folder with the requested object
ID as parent. Each array element describes one folder and is itself an array. The elements of each array
contain the information specified by the corresponding identifiers in the `columns` parameter. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersResponse|


#### Tags

* folders

### Creates a new folder.
```
PUT /folders?action=new
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the folder. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FolderUpdateResponse|


#### Consumes

* application/json

#### Tags

* folders

### Notifies users or groups about a shared folder (**available since v7.8.0, priliminary**).
```
PUT /folders?action=notify
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An empty JSON object. Any transport warnings that occurred during sending the
notifications are available in the warnings array of the response. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FolderSharingNotificationResponse|


#### Consumes

* application/json

#### Tags

* folders

### Gets the parent folders above the specified folder.
```
GET /folders?action=path
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of a folder.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with data for all parent nodes of a folder. Each array element describes one folder and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FoldersResponse|


#### Tags

* folders

### Gets the folders at the root level of the folder structure.
```
GET /folders?action=root
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all folders at the root level of the folder structure. Each array element
describes one folder and is itself an array. The elements of each array contain the information
specified by the corresponding identifiers in the `columns` parameter. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|FoldersResponse|


#### Tags

* folders

### Gets shared folders of a certain module (**available since v7.8.0, preliminary**).
```
GET /folders?action=shares
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|content_type|The desired content type (either numbers or strings; e.g. "tasks", "calendar", "contacts", "mail", "infostore").|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|all|Set to 1 to list even not subscribed folders.|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all folders that are considered as shared by the user.
Each array element describes one folder and is itself an array. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersResponse|


#### Tags

* folders

### Updates a folder.
```
PUT /folders?action=update
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object id of the folder. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|FolderUpdateResponse|


#### Consumes

* application/json

#### Tags

* folders

### Gets the new, modified and deleted folders of a given folder.
```
GET /folders?action=updates
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|parent|Object ID of a folder, which is the parent folder of the requested folders.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested folders.|true|integer (int64)||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,300". Each column is specified by a numeric column identifier, see [Common folder data](#common-folder-data) and [Detailed folder data](#detailed-folder-data).|true|string||
|QueryParameter|tree|The identifier of the folder tree. If missing "0" (primary folder tree) is assumed.|false|string|0|
|QueryParameter|ignore|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|false|enum (deleted)||
|QueryParameter|allowed_modules|(Preliminary) An array of modules (either numbers or strings; e.g. "tasks,calendar,contacts,mail") supported by requesting client. If missing, all available modules are considered.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with data for new, modified and deleted folders. New and modified folders are represented
by arrays. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. Deleted folders (should the ignore parameter be ever implemented)
would be identified by their object IDs as plain strings, without being part of a nested array. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FolderUpdatesResponse|


#### Tags

* folders

### Gets free/busy information (**available since v6.22.1**).
```
GET /freebusy?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|participant|The participant to get the free/busy data for. My be either an internal user-, group- or resource-ID,
or an email address for external participants.
|true|string||
|QueryParameter|from|The lower (inclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|until|The upper (exclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|merged|Indicates whether to pre-process free/busy data on the server or not. This includes sorting as well as merging overlapping free/busy intervals.|false|boolean||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of free/busy intervals. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FreeBusyResponse|


#### Tags

* freebusy

### Gets a list of free/busy information (**available since v6.22.1**).
```
PUT /freebusy?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|from|The lower (inclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|until|The upper (exclusive) limit of the requested time range.|true|integer (int64)||
|QueryParameter|merged|Indicates whether to pre-process free/busy data on the server or not. This includes sorting as well as merging overlapping free/busy intervals.|false|boolean||
|BodyParameter|body|A JSON array with identifiers of participants to get free/busy data for. The identifier my refer
to an internal user-, group- or resource-ID, or to an email address for external participants.
|true|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the free/busy data for all requested participants. For each participant it is added
an object (with the participant's ID as key) that contains a field `data` that is an array with objects representing
free/busy information as described in [FreeBusyData](#/definitions/FreeBusyData) model. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|FreeBusysResponse|


#### Consumes

* application/json

#### Tags

* freebusy

### Deletes a group (**introduced 2008-06-12**).
```
PUT /group?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the group to delete.|true|integer (int64)||
|BodyParameter|body|A JSON object with the field `id` containing the unique identifier of the group.|true|GroupListElement||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array if the group was deleted successfully. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupsResponse|


#### Consumes

* application/json

#### Tags

* groups

### Gets a group.
```
GET /group?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the group.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the group data. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupResponse|


#### Tags

* groups

### Gets a list of groups.
```
PUT /group?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id of the requested groups.|true|GroupListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of group objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupsResponse|


#### Consumes

* application/json

#### Tags

* groups

### Creates a group (**introduced 2008-06-12**).
```
PUT /group?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the group data. The field id is not present.|true|GroupData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the ID of the newly created group. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupUpdateResponse|


#### Consumes

* application/json

#### Tags

* groups

### Searches for groups.
```
PUT /group?action=search
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object with the search parameters.|true|GroupSearchBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of group objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|GroupsResponse|


#### Consumes

* application/json

#### Tags

* groups

### Updates a group (**introduced 2008-06-12**).
```
PUT /group?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|ID of the group that shall be updated.|true|integer||
|QueryParameter|timestamp|Timestamp of the last update of the group to update. If the group was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the group data fields to change. Only modified fields are present and the field id is omitted.|true|GroupData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Consumes

* application/json

#### Tags

* groups

### Gets the new, modified and deleted groups (**available since v6.18.1, introduced 2010-09-13**).
```
GET /group?action=updates
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested groups.|true|integer (int64)||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with fields `new`, `modified` and `deleted` representing arrays of new, modified and
deleted group objects. In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|GroupUpdatesResponse|


#### Tags

* groups

### Gets a contact picture.
```
GET /halo/contact/picture
```

#### Description

At least one of the optional search parameters should be set. All parameters are connected by OR during
the search. More specific parameters like `user_id` or `id` are prioritized in case of multiple matches.


#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The picture with proper ETag and caching headers set.|string (binary)|
|404|If no picture could be found.|No Content|


#### Tags

* halo

### Investigates a contact.
```
GET /halo/contact?action=investigate
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested columns. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|HaloInvestigationResponse|


#### Tags

* halo

### Gets all halo services.
```
GET /halo/contact?action=services
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with available halo providers. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|HaloServicesResponse|


#### Tags

* halo

### Requests a contact's profile image.
```
GET /image/contact/picture
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|folder|The folder ID in which the contact resides.|true|string||
|QueryParameter|id|The object ID of the contact.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


#### Produces

* application/octet-stream

#### Tags

* image

### Requests a MP3 cover image.
```
GET /image/file/mp3cover
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|id|The identifier of the uploaded image.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


#### Produces

* application/octet-stream

#### Tags

* image

### Requests an inline image from a mail.
```
GET /image/mail/picture
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|folder|The folder ID in which the mail resides.|true|string||
|QueryParameter|id|The object ID of the mail.|true|string||
|QueryParameter|uid|The identifier of the image inside the referenced mail.|true|string||
|QueryParameter|accountId|The mail account identifier|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


#### Produces

* application/octet-stream

#### Tags

* image

### Requests an image that was previously uploaded with the ajax file upload module.
```
GET /image/mfile/picture
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|uid|The identifier of the uploaded image.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


#### Produces

* application/octet-stream

#### Tags

* image

### Requests a user's profile image.
```
GET /image/user/picture
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|id|The object ID of the user.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The content of the requested image is directly written into output stream.|string (binary)|
|400|If request cannot be handled.|No Content|


#### Produces

* application/octet-stream

#### Tags

* image

### Imports contact data from CSV file.
```
POST /import?action=CSV
```

#### Description

#### Example CSV
```
"Given name","Sur name"
"Günther","Mustermann"
"Hildegard","Musterfrau"
```
The delimiter may be any CSV-valid character (e.g. "," or ";"). The first line must contain the column titles that are related
to the corresponding fields of the [ContactData](#/definitions/ContactData) model. See [Detailed contact data](#detailed-contact-data)
for a mapping of fields to CSV column titles.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This must be a contact folder.|true|string||
|QueryParameter|charset|A fixed character encoding to use when parsing the uploaded file, overriding the built-in defaults, following the conventions documented in [RFC 2278](http://tools.ietf.org/html/rfc2278) (preliminary, since 7.6.2).|false|string||
|FormDataParameter|file|The CSV file containing the contact data. The column titles are the ones described in [Detailed contact data](#detailed-contact-data).|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created contacts.
In case of errors the JSON object contains the well known [error fields](#error-handling).
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* import

### Imports calendar data from iCalendar file.
```
POST /import?action=ICAL
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This may be be an appointment or a task folder.|true|string||
|QueryParameter|suppressNotification|Can be used to disable the notifications for new appointments that are imported through the given iCal file. This help keeping the Inbox clean if a lot of appointments need to be imported. The value of this parameter does not matter because only for the existence of the parameter is checked.|false|boolean||
|QueryParameter|ignoreUIDs|When set to `true`, UIDs are partially ignored during import of tasks and appointments from iCal. Internally, each UID is replaced statically by a random one to preserve possibly existing relations between recurring appointments in the same iCal file, but at the same time to avoid collisions with already existing tasks and appointments.|false|boolean||
|FormDataParameter|file|The iCal file containing the appointment and task data.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created appointments/tasks.
In case of errors the JSON object contains the well known [error fields](#error-handling). Beside a field `warnings` may contain an array
of objects with warning data containing customary error fields.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* import

### Imports contact data from an Outlook CSV file.
```
POST /import?action=OUTLOOK_CSV
```

#### Description

#### Example: exported Outlook CSV
```
First Name,Last Name
Günther,Mustermann
Hildegard,Musterfrau
```
The column titles in the first line of the CSV file may be those used by the English, French or German version of Outlook.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This must be a contact folder.|true|string||
|QueryParameter|charset|A fixed character encoding to use when parsing the uploaded file, overriding the built-in defaults, following the conventions documented in [RFC 2278](http://tools.ietf.org/html/rfc2278) (preliminary, since 7.6.2).|false|string||
|FormDataParameter|file|The CSV file **with Windows' default encoding CP-1252** containing the contact data. The column titles are those used by the English, French or German version of Outlook.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created contacts.
In case of errors the JSON object contains the well known [error fields](#error-handling).
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* import

### Imports data from vCard file.
```
POST /import?action=VCARD
```

#### Description

#### Supported vCard formats
 * vCard 2.1
 * vCard 3.0
 * vCalendar 1.0


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder into which the data should be imported. This must be a contact folder.|true|string||
|FormDataParameter|file|The vCard file.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the field `data` that represents
an array of objects each consisting of the fields `id`, `folder_id` and `last_modified` of the newly created objects.
In case of errors the JSON object contains the well known [error fields](#error-handling).
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* import

### Gets all infoitems.
```
GET /infostore?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all infoitems. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


#### Tags

* infostore

### Checks if a given file name is valid (**available since v7.8.1, preliminary**).
```
GET /infostore?action=checkname
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|name|The file name to check.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An empty JSON object when file name is valid. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Tags

* infostore

### Copies an infoitem.
```
PUT /infostore?action=copy
```

#### Description

This request cannot change or add files. Therefore it is necessary to use the `POST` method.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be copied.|true|string||
|BodyParameter|body|A JSON object containing the modified fields of the destination infoitem. The field `id` must not be present.|true|InfoItemData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Copies an infoitem.
```
POST /infostore?action=copy
```

#### Description

Copies an infoitem's data with the possibility to change the file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be copies.|true|string||
|FormDataParameter|json|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemData](#/definitions/InfoItemData) model. Only modified fields must be specified but at least `{"folder_id":"destination"}`.|true|string||
|FormDataParameter|file|The metadata as per `<input type="file" />`.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* infostore

### Deletes infoitems.
```
PUT /infostore?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitems to delete.|true|integer (int64)||
|QueryParameter|hardDelete|Defaults to `false`. If set to `true`, the file is deleted permanently. Otherwise, and if the underlying storage supports a trash folder and the file is not yet located below the trash folder, it is moved to the trash folder.|false|boolean||
|BodyParameter|body|A JSON array of objects with the fields `id` and `folder` representing infoitems that shall be deleted.|true|InfoItemListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array if the infoitems were deleted successfully. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Deletes versions of an infoitem.
```
PUT /infostore?action=detach
```

#### Description

#### Note
When the current version of a document is deleted the new current version will be the latest version.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the base object.|true|string||
|QueryParameter|folder|The folder ID of the base object.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem.|true|integer (int64)||
|BodyParameter|body|A JSON array of version numbers to detach.|true|integer array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array of version numbers that were not deleted. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|InfoItemDetachResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Gets an infoitem document.
```
GET /infostore?action=document
```

#### Description

It is possible to add a filename to the request's URI like `/infostore/{filename}?action=document`.
The filename may be added to the customary infostore path to suggest a filename to a Save-As dialog.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|id|Object ID of the requested infoitem.|true|string||
|QueryParameter|version|If present the infoitem data describes the given version. Otherwise the current version is returned.|false|integer||
|QueryParameter|content_type|If present the response declares the given `content_type` in the Content-Type header.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the document. The response type for the HTTP request is set accordingly to the defined mimetype for this infoitem or the content_type given.|string (binary)|


#### Produces

* application/octet-stream

#### Tags

* infostore

### Gets an infoitem.
```
GET /infostore?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested infoitem.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemResponse|


#### Tags

* infostore

### Gets a list of infoitems.
```
PUT /infostore?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id of the infoitems.|true|InfoItemListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested infoitems. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Locks an infoitem.
```
GET /infostore?action=lock
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be locked.|true|string||
|QueryParameter|diff|If present the value is added to the current time on the server (both in ms). The document will be locked until that time. If this parameter is not present, the document will be locked for a duration as configured on the server.|false|integer (int64)||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Tags

* infostore

### Moves one or more infoitems to another folder.
```
PUT /infostore?action=move
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|ID of the destination folder.|true|string||
|BodyParameter|body|A JSON array of JSON objects each referencing to an existing infoitem that is supposed to be moved to the destination folder.|true|InfoItemListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an array of infoitem identifiers that could not be moved (due to a conflict).
Th array is empty if everything went fine. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemsMovedResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Creates an infoitem.
```
PUT /infostore?action=new
```

#### Description

Creates a new contact. This request cannot add a file to the infoitem. Therefor it
is necessary to use the `POST` method.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing a field `file` with the modified fields of the infoitem's data. It is possible to let added object permission entities be notified about newly shared files. In that case add a "notification" object.|true|InfoItemBody array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Creates an infoitem.
```
POST /infostore?action=new
```

#### Description

Creates a new infoitem with a file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|FormDataParameter|json|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemBody](#/definitions/InfoItemBody) model.|true|string||
|FormDataParameter|file|The metadata as per `<input type="file" />`.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* infostore

### Notifies users or groups about a shared infoitem (**available since v7.8.0, preliminary**).
```
PUT /infostore?action=notify
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the shared infoitem to notify about.|true|string||
|BodyParameter|body|JSON object providing the JSON array `entities`, which holds the entity ID(s) of the users or groups that
should be notified. To send a custom message to the recipients, an additional JSON object `notification` may
be included, inside of which an optional message can be passed (otherwise, some default message is used).
(Example: {"entities":["2332"]} or {"entities":["2332"],"notification":{"transport":"mail","message":"The message"}})
|true|InfoItemSharingNotificationBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An empty JSON object. Any transport warnings that occurred during sending the
notifications are available in the warnings array of the response. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemSharingNotificationResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Deletes all versions of an infoitem leaving only the base object.
```
PUT /infostore?action=revert
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the base object.|true|string||
|QueryParameter|folder|The folder ID of the base object.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem.|true|integer (int64)||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Tags

* infostore

### Saves an attachment in the infostore.
```
PUT /infostore?action=saveAs
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the newly created infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Search for infoitems.
```
PUT /infostore?action=search
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching infoitems. Infoitems are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Gets shared infoitems (**available since v7.8.0, preliminary**).
```
GET /infostore?action=shares
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all infoitems that are considered as shared by the user.
Each array element describes one infoitem and is itself an array. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case
of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


#### Tags

* infostore

### Unlocks an infoitem.
```
GET /infostore?action=unlock
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be unlocked.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Tags

* infostore

### Updates an infoitem.
```
PUT /infostore?action=update
```

#### Description

Updates an infoitem's data. This request cannot change or add files. Therefore it
is necessary to use the `POST` method.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing a field `file` with the modified fields of the infoitem's data. It is possible to let added object permission entities be notified about newly shared files. In that case add a "notification" object.|true|InfoItemBody array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the object ID of the updated infoitem. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|InfoItemUpdateResponse|


#### Consumes

* application/json

#### Tags

* infostore

### Updates an infoitem.
```
POST /infostore?action=update
```

#### Description

Updates an infoitem's data and file. The normal request body must be placed as form-data using the
content-type `multipart/form-data`. The form field `json` contains the infoitem's data while the file
must be placed in a file field named `file` (see also [File uploads](#file-uploads)).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem that shall be updated.|true|string||
|QueryParameter|timestamp|Timestamp of the updated infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|QueryParameter|offset|Optionally sets the start offset in bytes where to append the data to the document, must be equal to the actual document's length (available since v7.8.1). Only available if the underlying File storage account supports the "RANDOM_FILE_ACCESS" capability.|false|integer||
|FormDataParameter|json|Represents the normal request body as JSON string containing the infoitem's data as described in the [InfoItemBody](#/definitions/InfoItemBody) model. Only modified fields must be specified but at least "{}".|true|string|{}|
|FormDataParameter|file|The metadata as per `<input type="file" />`.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing the object ID of the infoitem or errors if some occurred.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* infostore

### Gets the new, modified and deleted infoitems.
```
GET /infostore?action=updates
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested infoitems.|false|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Currently, the only valid value – "deleted" – causes deleted object IDs not to be returned.|false|enum (deleted, )||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new, modified and deleted infoitems. New and modified infoitems are represented by arrays. The
elements of each array contain the information specified by the corresponding identifiers in the `columns`
parameter. Deleted infoitems would be identified by their object IDs as string, without being part of
a nested array. In case of errors the responsible fields in the response are filled (see
[Error handling](#error-handling)).
|InfoItemUpdatesResponse|


#### Tags

* infostore

### Gets all versions of an infoitem.
```
GET /infostore?action=versions
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the infoitem whose versions are requested.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,700". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data) and [Detailed infoitem data](#detailed-infoitem-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the infoitem. Each array element describes one infoitem and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. The timestamp is the timestamp relating to the requested infostore item.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|InfoItemsResponse|


#### Tags

* infostore

### Gets multiple documents as a ZIP archive (**available since v7.4.0**).
```
PUT /infostore?action=zipdocuments
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id, folder and optionally the documents' versions to include in the requested ZIP archive (if missing, it refers to the latest/current version).|true|InfoItemZipElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP archive. The response type for the HTTP request is set to `application/zip`.|string (binary)|


#### Consumes

* application/json

#### Produces

* application/zip

#### Tags

* infostore

### Gets a ZIP archive containing all ifoitems of a denoted folder (**availabel since v7.6.1**).
```
GET /infostore?action=zipfolder
```

#### Description

It is possible to add a filename to the request's URI like `/infostore/{filename}?action=zipfolder`.
The filename may be added to the customary infostore path to suggest a filename to a Save-As dialog.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the infoitems.|true|string||
|QueryParameter|recursive|`true` to also include subfolders and their infoitems respectively; otherwise `false` to only consider the infoitems of specified.|false|boolean||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP archive. The response type for the HTTP request is set to `application/zip`.|string (binary)|


#### Produces

* application/zip

#### Tags

* infostore

### Gets all JSlobs (**available since v6.22**).
```
GET /jslob?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each representing a certain JSON configuration.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|JSlobsResponse|


#### Tags

* JSlob

### Gets a list of JSlobs (**available since v6.22**).
```
PUT /jslob?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||
|BodyParameter|body|A JSON array with the identifiers of the requested JSlobs.|true|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each representing a certain JSON configuration.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|JSlobsResponse|


#### Consumes

* application/json

#### Tags

* JSlob

### Stores or deletes a JSlob (**available since v6.22**).
```
PUT /jslob?action=set
```

#### Description

To delete a JSON configuration just send an empty request body for the specified `id`.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The JSlob indentifier.|false|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||
|BodyParameter|body|A JSON object containing the JSON configuration to store. To delete the JSlob just send an empty body.|true|object||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* JSlob

### Updates a JSlob (**available since v6.22**).
```
PUT /jslob?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The JSlob indentifier.|false|string||
|QueryParameter|serviceId|The identifier for the JSlob service, default is "com.openexchange.jslob.config".|false|string||
|BodyParameter|body|The JSON object containing the updated JSON configuration to store. Fields that are not included are thus not affected and survive the change. Use `/jslob?action=set` to delete fields or entire JSlob.|true|object||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* JSlob

### Acquires an identity token (**available since v7.6.0**).
```
GET /jump?action=identityToken
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|system|The identifier for the external service/system, like "com.openexchange.jump.endpoint.mysystem".|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the identity token. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|JumpResponse|


#### Tags

* jump

### Refresh auto-login cookie
```
GET /login;jsessionid=1157370816112.OX1?action=redirect
```

#### Description

**SECURITY WARNING!** Utilizing this request is **INSECURE!** This request allows to access a session with a
single one time token. This one time token may be delivered to the wrong client if the protocol has an
error or Apache or the load balancer make a mistake. This will cause a wrong user to be in a wrong
session. **IMMEDIATELY** consider not to use this request anymore. You have been warned. Use instead the
FormLogin that does not need to use the redirect request.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|random|A session random token to jump into the session. This random token is part of the login
response. Only a very short configurable time after the login it is allowed to jump into
the session with the random token.
|true|string||
|QueryParameter|client|The client can be defined here newly if it is not correct on the login request itself.|false|string||
|QueryParameter|store|Tells the UI to do a store request after login to be able to use autologin request.|false|boolean||
|QueryParameter|uiWebPath|The optional path on the webserver to the UI. If this parameter is not given the configured uiWebPath is used.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Tags

* login

### Change IP of client host in a session.
```
POST /login?action=changeip
```

#### Description

The following request is especially for integration with systems located in the providers
infrastructure. If those systems create a session with the following request the client host
IP address in the session can be changed. The IP check for following requests will be done using
this newly set client host IP address.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|clientIP|New IP address of the client host for the current session.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the string "1" as data attribute. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|ChangeIPResponse|


#### Tags

* login

### Login to the web frontend using a simple HTML form (**available since v6.20**).
```
POST /login?action=formlogin
```

#### Description

This request implements a possible login to the web frontend by only using a simple HTML form.
The response contains a redirect link to the Web-UI. See [OXSessionFormLogin](http://oxpedia.org/wiki/index.php?title=OXSessionFormLogin) for details.
An example for such a form can be found in the backend's documentation folder
(/usr/share/doc/open-xchange-core) under examples/login.html.


#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A redirect to the web UI. The URL of the web UI is either taken from the given parameter
or from the configured default of the backend.
|string|


#### Produces

* text/html

#### Tags

* login

### Login with user credentials.
```
POST /login?action=login
```

#### Description

The login module is used to obtain a session from the user's login
credentials. Parameters are normally expected in the POST request body.


#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the session ID used for all subsequent requests. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|LoginResponse|


#### Tags

* login

### Does the logout.
```
GET /login?action=logout
```

#### Description

Does the logout which invalidates the session.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|403|FORBIDDEN. The server refuses to respond to the request.|No Content|


#### Tags

* login

### Redeem Token Login (**available since v7.4.0**).
```
POST /login?action=redeemToken
```

#### Description

With a valid session it is possible to acquire a secret (see `token?action=acquireToken`). Using this secret another system is able
to generate a valid session. This session may also contain the users password (configurable).
The system in question needs to be registered at the server and has to identify itself with a key
configured at the open-xchange server. This is only for internal communication and by default no keys
are available.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|FormDataParameter|token|The token created with `token?action=acquireToken`.|true|string||
|QueryParameter|authId|Identifier for tracing every single login request passed between different systems in a cluster.
The value should be some token that is unique for every login request. This parameter must be
given as URL parameter and not inside the body of the POST request.
|true|string||
|FormDataParameter|client|Identifier of the client using the HTTP/JSON interface. The client must identifier must be the same for each request after creating the login session.|true|string||
|FormDataParameter|secret|The value of the secret string for token logins. This is configured through the tokenlogin-secrets configuration file.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the session ID used for all subsequent requests. Additionally a random
token is contained to be used for the Easy Login method. If configured within tokenlogin-secrets
configuration file even the user password will be returned. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|LoginResponse|


#### Tags

* login

### Refreshes the secret cookie (**available since v6.18.2**)
```
GET /login?action=refreshSecret
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Tags

* login

### Refreshes the auto-login cookie.
```
GET /login?action=store
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Tags

* login

### Login for a very short living session (**available since v7.0.1**).
```
POST /login?action=tokenLogin
```

#### Description

This request allows every possible client to create a very short living session. This session can then be transferred to any other client preferably a browser entering then the normal web interface. Then the sessions life time will be extended equally to every other session.

Compared to the login mechanism using the random token, this request is more secure because two tokens are used. One of these tokens is only known to the client and one is generated by the server. Only the combination of both tokens allows to use the session. The combination of both tokens must be done by the client creating the session.

**DISCLAIMER:** This request MUST NOT be used by some server side instance. If some server side instance uses this request to create a session for a browser on some client machine, then you have to transfer the full URL with server and client token over some connection to the client. This creates a VULNERABILITY if this is done. The token login method is only secure if this request is already sent from the same machine that later runs the browser using the created session.


#### Parameters
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


#### Responses
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


#### Produces

* application/json
* text/html

#### Tags

* login

### Accesses a session that was previously created with the token login (**available since v7.0.1**).
```
POST /login?action=tokens
```

#### Description

This request allows clients to access a session created with the `/login?action=tokenLogin` request.
When accessing the session its life time is extended equally to every other session.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|FormDataParameter|serverToken|The login name.|true|string||
|FormDataParameter|clientToken|The password (MUST be placed in the request body, otherwise the login request will be denied).|true|string||
|FormDataParameter|client|Identifier of the client using the HTTP/JSON interface. This is for statistic evaluations what clients are used with Open-Xchange.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object conform to the normal response body containing the session identifier, the login, the identifier
and the locale of the user.  In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|TokensResponse|


#### Tags

* login

### Moves mails to the given category
```
PUT /mail/categories?action=move
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|category_id|The identifier of a category.|true|string||
|BodyParameter|body|'A JSON array of mail identifier, e.g.: [{"id":ID, "folder_id":FID},{"id":ID2, "folder_id":FID2}, {...}]' 
|true|Mail_CategoriesMoveBody array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'An empty response if everything went well. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|CommonResponse|


#### Consumes

* application/json

#### Tags

* mail_categories

### Add a new rule
```
PUT /mail/categories?action=train
```

#### Description

Adds a new rule with the given mail addresses to the given category and optionally reorganize all existing mails in the inbox.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|category_id|The identifier of a category.|true|string||
|QueryParameter|apply-for-existing|A flag indicating whether old mails should be reorganized. Defaults to 'false'.|false|boolean||
|QueryParameter|apply-for-future-ones|A flag indicating whether a rule should be created or not. Defaults to 'true'.|false|boolean||
|BodyParameter|body|'A JSON object containing a "from" field which contains an array of mail addresses.' 
|true|Mail_CategoriesTrainBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'An empty response if everything went well. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|CommonResponse|


#### Consumes

* application/json

#### Tags

* mail_categories

### Retrieves the unread counts of active mail categories
```
GET /mail/categories?action=unread
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|category_ids|A comma separated list of category identifiers. If set only the unread counters of this categories are retrieved.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object with a field for each active category containing the number of unread messages. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|Mail_CategoriesUnreadResponse|


#### Tags

* mail_categories

### Gets all mails.
```
GET /mail?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "600,601". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|QueryParameter|left_hand_limit|A positive integer number to specify the "left-hand" limit of the range to return.|false|integer||
|QueryParameter|right_hand_limit|A positive integer number to specify the "right-hand" limit of the range to return.|false|integer||
|QueryParameter|limit|A positive integer number to specify how many items shall be returned according to given sorting; overrides `left_hand_limit`/`right_hand_limit` parameters and is equal to `left_hand_limit=0` and `right_hand_limit=<limit>`.|false|integer||
|QueryParameter|filter|The category id to filter for. If set to "general" all mails which does not belong to any other category are retrieved.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an array with mail data. Each array element describes one mail and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailsResponse|


#### Tags

* mail

### Marks all mails of a folder as seen (**available since v7.6.0**).
```
PUT /mail?action=all_seen
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the value `true`. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailsAllSeenResponse|


#### Consumes

* application/json

#### Tags

* mail

### Gets a mail attachment.
```
GET /mail?action=attachment
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the mail which contains the attachment.|true|string||
|QueryParameter|attachment|ID of the requested attachment (can be substituted by the parameter `cid` otherwise this parameter is **madatory**).|false|string||
|QueryParameter|cid|Value of header 'Content-ID' of the requested attachment (can be substituted by the parameter `attachment` otherwise this parameter is **madatory**).|false|string||
|QueryParameter|save|1 overwrites the defined mimetype for this attachment to force the download dialog, otherwise 0.|false|integer||
|QueryParameter|filter|1 to apply HTML white-list filter rules if and only if requested attachment is of MIME type `text/htm*` **AND** parameter `save` is set to 0.|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the document. The response type for the HTTP Request is set accordingly to the defined mimetype for this attachment, except the parameter save is set to 1.|string (binary)|


#### Tags

* mail

### Clears the content of mail folders.
```
PUT /mail?action=clear
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Not IMAP: timestamp of the last update of the deleted mails.|false|integer (int64)||
|BodyParameter|body|A JSON array with object IDs of the mail folders that shall be cleared.|true|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array with IDs of mail folder that could not be cleared; meaning the response body is an empty
JSON array if everything went well. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailsCleanUpResponse|


#### Consumes

* application/json

#### Tags

* mail

### Copies a mail to another folder.
```
PUT /mail?action=copy
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested mail that shall be copied.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|BodyParameter|body|A JSON object containing the id of the destination folder.|true|MailDestinationBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the object ID and the folder ID of the copied mail. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailDestinationResponse|


#### Consumes

* application/json

#### Tags

* mail

### Gets the mail count.
```
GET /mail?action=count
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an integer value representing the folder's mail count. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailCountResponse|


#### Tags

* mail

### Deletes mails.
```
PUT /mail?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Not IMAP: timestamp of the last update of the deleted mails.|false|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the mails.|true|MailListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Not IMAP: A JSON array with object IDs of mails which were modified after the specified timestamp and
were therefore not deleted. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailsCleanUpResponse|


#### Consumes

* application/json

#### Tags

* mail

### Forwards a mail.
```
GET /mail?action=forward
```

#### Description

Returns the data for the message that shall be forwarded.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested message.|true|string||
|QueryParameter|view|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|false|enum (text, html)||
|QueryParameter|max_size|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailReplyResponse|


#### Tags

* mail

### Gets a mail.
```
GET /mail?action=get
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailResponse|


#### Tags

* mail

### Gets the message headers as plain text.
```
GET /mail?action=get&hdr=1
```

#### Description

#### Note
By setting the query parameter `hdr` to 1 the response type of the request action changes. Then
it is returned a JSON object with the field `data` containing the (formatted) message headers
as plain text.
The parameters below specify the ones that have an effect on the request.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested mail (can be substituded by `message_id` parameter).|false|string||
|QueryParameter|message_id|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|false|string||
|QueryParameter|unseen|Use `true` to leave an unseen mail as unseen although its content is requested.|false|boolean||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the message headers as plain text. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailHeadersResponse|


#### Tags

* mail

### Gets the complete message source as plain text.
```
GET /mail?action=get&src=1
```

#### Description

#### Note
By setting the query parameter `src` to 1 the response type of the request action changes. Then
it is returned a JSON object with the field `data` containing the complete message source as plain text.
The parameters below specify the ones that have an effect on the request.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested mail (can be substituded by `message_id` parameter).|false|string||
|QueryParameter|message_id|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|false|string||
|QueryParameter|unseen|Use `true` to leave an unseen mail as unseen although its content is requested.|false|boolean||
|QueryParameter|save|1 to write the complete message source to output stream. **NOTE:** This parameter will only be used if parameter `src` is set to 1.|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the complete message source as plain text. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailSourceResponse|


#### Tags

* mail

### Import of mails as MIME data block (RFC822) (**available since v6.18**).
```
POST /mail?action=import
```

#### Description

This request can be used to store a single or a lot of mails in the OX mail storage backend. This
action should be used instead of `/mail?action=new` because it is faster and tolerant to 8-bit encoded emails.

To import multiple mails add further form-data fields.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|The ID of the folder into that the emails should be imported.|true|string||
|QueryParameter|flags|In case the mail should be stored with status "read" (e.g. mail has been read already in the client inbox), the parameter "flags" has to be included. For information about mail flags see [Mail data](#/definitions/MailData) model.|false|string||
|QueryParameter|force|If this parameter is set to `true`, the server skips checking the valid from address.|false|boolean||
|FormDataParameter|file|The RFC822 encoded email as binary data.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each with the folder identifier and the object ID
of the imported mail(s). In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailImportResponse|


#### Consumes

* multipart/form-data

#### Tags

* mail

### Gets a list of mails.
```
PUT /mail?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "600,601". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|headers|(preliminary) A comma-separated list of header names. Each name requests denoted header from each mail.|false|string||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the requested mails.|true|MailListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an array with mail data. Mails are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailsResponse|


#### Consumes

* application/json

#### Tags

* mail

### Sends or saves a mail as MIME data block (RFC822) (**available since SP5**).
```
PUT /mail?action=new
```

#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the folder ID and the object ID of the mail. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailDestinationResponse|


#### Consumes

* text/plain

#### Tags

* mail

### Sends a mail.
```
POST /mail?action=new
```

#### Description

The request accepts file fields in upload form that denote referenced files that are going to be appended
as attachments.
For "text/plain" mail bodies, the JSON boolean field "raw" may be specified inside the body's JSON
representation to signal that the text content shall be kept as-is; meaning to keep all formatting
intact.


#### Parameters
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


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page containing the object ID of the newly created mail or in case of errors an error object (see [File uploads](#file-uploads) as an example).
|string|


#### Consumes

* multipart/form-data
* multipart/mixed

#### Produces

* text/html

#### Tags

* mail

### Requests a delivery receipt for a priviously sent mail.
```
PUT /mail?action=receipt_ack
```

#### Description

This delivery receipt only acknowledges that the message could be receipted on the recipients computer.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the information of a mail for which a delivery receipt shall be requested.|true|MailAckBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty data field if everything went well or a JSON object containing the error
information. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailAckResponse|


#### Consumes

* application/json

#### Tags

* mail

### Replies a mail.
```
GET /mail?action=reply
```

#### Description

Returns the data for the message that shall be replied.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested message.|true|string||
|QueryParameter|view|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|false|enum (text, html)||
|QueryParameter|setFrom|A flag (`true`/`false`) that signals if "From" header shall be pre-selected according to a suitable recipient address that matches one of user's E-Mail address aliases. (available since v7.6.0)|false|boolean||
|QueryParameter|max_size|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailReplyResponse|


#### Tags

* mail

### Replies a mail to all.
```
GET /mail?action=replyall
```

#### Description

Returns the data for the message that shall be replied.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested message.|true|string||
|QueryParameter|view|Content 'text' forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. 'html' to allow a possible HTML mail body being transferred as it is (but white-list filter applied). NOTE: if set, the corresponding gui config setting will be ignored. (available since SP6)|false|enum (text, html)||
|QueryParameter|setFrom|A flag (`true`/`false`) that signals if "From" header shall be pre-selected according to a suitable recipient address that matches one of user's email address aliases. (available since v7.6.0)|false|boolean||
|QueryParameter|max_size|A positive integer number (greater than 10000) to specify how many characters of the message content will be returned. If the number is smaller than 10000 the value will be ignored and 10000 used. (available since v7.6.1)|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing all data of the requested mail. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailReplyResponse|


#### Tags

* mail

### Searches for mails.
```
PUT /mail?action=search
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "600,601". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response or the string “thread” to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified. Note: Applies only to root-level messages.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Note: Applies only to root-level messages.|false|string||
|BodyParameter|body|A JSON object describing the search term as introducted in [Advanced search](#advanced-search). Example:
`{"filter":["and",["=", {"field":"to"},"test1@example.com"],["not",["=",{"field":"from"},"test2@example.com"]]]}`
which represents 'to = "test1@example.com" AND NOT from = "test2@example.com"'. Available field names are
`from`, `to`, `cc`, `bcc`, `subject`, `received_date`, `sent_date`, `size`, `flags`, `content`, `content_type`, `disp`, and `priority`.
|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|'A JSON object containing an array with matching mails. Mails are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. Not IMAP: with timestamp. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).'
|MailsResponse|


#### Consumes

* application/json

#### Tags

* mail

### Gets all mail conversations.
```
GET /mail?action=threadedAll
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "600,601". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response or the string “thread” to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified. Note: Applies only to root-level messages.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. Note: Applies only to root-level messages.|false|string||
|QueryParameter|includeSent|A boolean value to signal that conversations also include messages taken from special "sent" aka "sent items" folder.|false|boolean||
|QueryParameter|left_hand_limit|A positive integer number to specify the "left-hand" limit of the range to return. Note: Applies only to root-level messages.|false|integer||
|QueryParameter|right_hand_limit|A positive integer number to specify the "right-hand" limit of the range to return. Note: Applies only to root-level messages.|false|integer||
|QueryParameter|limit|A positive integer number to specify how many items shall be returned according to given sorting; overrides `left_hand_limit`/`right_hand_limit` parameters and is equal to `left_hand_limit=0` and `right_hand_limit=<limit>`. Note: Applies only to root-level messages.|false|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of objects, each representing a conversation's root message along
with its message thread. The root message's JSON object is filled according to the specified `columns`
and is enhanced by a special `thread` field representing the full message thread (including the root
message itself). `thread` is a JSON array of objects each representing a message in the conversation
sorted by time-line and filled with the specified `columns`. Not IMAP: with timestamp. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MailConversationsResponse|


#### Tags

* mail

### Updates a mail or a folder's messages and/or moves a mail to another folder.
```
PUT /mail?action=update
```

#### Description

The update request can perform an update of the color label and flags of one mail object. Beside it
is possible to change the mail's folder, meaning move the mail to another folder. Both operations
can be performed at once too.

If neither parameter `id` nor parameter `message_id` is specified, all folder's messages are updated
accordingly (**available since v6.20).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the requested mail that shall be updated (**mandatory** if a mail shall be moved).|false|string||
|QueryParameter|message_id|(Preliminary) The value of "Message-Id" header of the requested mail. This parameter is a substitute for "id" parameter.|false|string||
|BodyParameter|body|A JSON object containing the new values that ought to be applied to mail and/or the id of the destination folder (if the mail shall be moved, otherwise it must not be specified).|true|MailUpdateBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the object ID and the folder ID of an updated and/or moved mail or only
the folder ID if several mails are updated. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailDestinationResponse|


#### Consumes

* application/json

#### Tags

* mail

### Gets updated mails (not IMAP).
```
GET /mail?action=updates
```

#### Description

#### Not IMAP


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "600,601". Each column is specified by a numeric column identifier, see [Detailed mail data](#detailed-mail-data).|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Just an empty JSON array is going to be returned since this action cannot be applied to IMAP. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MailUpdatesResponse|


#### Tags

* mail

### Gets multiple mail attachments as a ZIP file.
```
GET /mail?action=zip_attachments
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|Object ID of the mail which contains the attachments.|true|string||
|QueryParameter|attachment|A comma-separated list of IDs of the requested attachments.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP file.|string (binary)|


#### Produces

* application/zip

#### Tags

* mail

### Gets multiple mails as a ZIP file.
```
GET /mail?action=zip_messages
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the mails.|true|string||
|QueryParameter|id|A comma-separated list of Object IDs of the requested mails.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The raw byte data of the ZIP file.|string (binary)|


#### Produces

* application/zip

#### Tags

* mail

### Gets the configuration of the mail filter backend.
```
GET /mailfilter?action=config
```

#### Description

A mail filter can have different rules each containing one command. A command has a test condition and actions
that are executed if the condition is true. The list of available comparisions (that can be used in test
conditions) and the list of available actions depends on a given test and the mail filter server configuration
and must be determined at runtime.

All those dynamic values can be fetched via a config object at startup, which shows the capabilities of the server
to the client.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the fields `tests` (containing an array of available test-objects, see [Possible tests](#possible-tests) too) and `actioncommands`
(containing an array of [valid actions](#possible-action-commands)). In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailFilterConfigResponse|


#### Tags

* mailfilter

### Deletes a mail filter rule.
```
PUT /mailfilter?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON object with the ID of the rule to delete.|true|MailFilterDeletionBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* mailfilter

### Deletes the whole mail filter script.
```
PUT /mailfilter?action=deletescript
```

#### Description

This call is only used as workaround for parsing errors in the backend, so that the user is able to kick a whole script if it contains errors in the grammar.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Tags

* mailfilter

### Gets the whole mail filter script.
```
PUT /mailfilter?action=getscript
```

#### Description

This call is only used as workaround for parsing errors in the backend, so that the user is able to get the plaintext of a complete script.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the text of the complete sieve script. In case of errors the responsible fields
in the response are filled (see [Error handling](#error-handling)).
|MailFilterScriptResponse|


#### Tags

* mailfilter

### Gets all mail filter rules.
```
GET /mailfilter?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|flag|If given, only rules with this flag are returned.|false|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an array of rule-objects. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailFilterRulesResponse|


#### Tags

* mailfilter

### Creates a mail filter rule.
```
PUT /mailfilter?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON object describing the mail filter rule. If the field `position` is included, it's taken as the position of the rule in the array on the server side (this value shouldn't be greater than the size of all rules).|true|MailFilterRule||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created rule. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MailFilterCreationResponse|


#### Consumes

* application/json

#### Tags

* mailfilter

### Reorders mail filter rules.
```
PUT /mailfilter?action=reorder
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON array with unique identifiers, which represents how the corresponding rules are order.|true|integer array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* mailfilter

### Updates a mail filter rule.
```
PUT /mailfilter?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|username|Must contain the user name for **admin mode**. So the normal credentials are taken for authentication but the mail filter of the user with this username is being changed.|false|string||
|BodyParameter|body|A JSON object describing the rule with the `id` set (which identifies the rule to change). Only modified fields are present.|true|MailFilterRule||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* mailfilter

### Gets all messaging accounts.
```
GET /messaging/account?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messagingService|List only those accounts that belong to the given `messagingService`.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with account objects. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingAccountsResponse|


#### Tags

* messaging

### Deletes a messaging account.
```
GET /messaging/account?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messagingService|The messaging service ID that the account belongs to.|true|string||
|QueryParameter|id|The messaging account ID.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if deletion was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MessagingAccountUpdateResponse|


#### Consumes

* application/json

#### Tags

* messaging

### Gets a messaging account.
```
GET /messaging/account?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messagingService|The messaging service ID that the account belongs to.|true|string||
|QueryParameter|id|The messaging account ID.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the requested account. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingAccountResponse|


#### Tags

* messaging

### Creates a messaging account.
```
PUT /messaging/account?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the account to create. The ID is generated by the server and must not be present.|true|MessagingAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MessagingAccountUpdateResponse|


#### Consumes

* application/json

#### Tags

* messaging

### Updates a messaging account.
```
PUT /messaging/account?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the modified data of the account. The fields `id` and `messagingService` must always be set.|true|MessagingAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if update was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|MessagingAccountUpdateResponse|


#### Consumes

* application/json

#### Tags

* messaging

### Gets all messaging messages.
```
GET /messaging/message?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of column names, like "folder,headers,body". See [Messaging fields](#messaging fields) for valid column names.|true|string||
|QueryParameter|folder|The folder ID, like "com.openexchange.messaging.twitter://535/defaultTimeline/directMessages".|true|string||
|QueryParameter|sort|A column name to sort by.|false|string||
|QueryParameter|order|The order direction which can be "asc" for ascending (default) or "desc" for descending.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all messages. Each array element describes one message and
is itself an array. The elements of each array contain the information specified by the corresponding
column names in the `columns` parameter. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MessagingMessagesResponse|


#### Tags

* messaging

### Gets a messaging message.
```
GET /messaging/message?action=get
```

#### Description

A messaging message consists of some metadata, headers and a content. The content attribute varies
by the content-type header. If the content-type is `text/*` it is a string, if it is `multipart/*` it
is an array of objects, each representing a part of the multipart. If it is anything else it is considered binary
and is a Base64 encoded string.

The folder ID of a message follows a predefined format: `[messagingService]://[accountId]/[path]`, like
`com.openexchange.messaging.twitter://535/defaultTimeline/directMessages`.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the message to load.|true|string||
|QueryParameter|folder|The folder ID of the message.|true|string||
|QueryParameter|peek|If set to `true` the read/unread state of the message will not change. Default is `false`.|false|boolean||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the message. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MessagingMessageResponse|


#### Tags

* messaging

### Gets a list of messaging messages.
```
PUT /messaging/message?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of column names, like "folder,headers,body". See [Messaging fields](#messaging fields) for valid column names.|true|string||
|BodyParameter|body|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|true|object array array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for requested messages. Each array element describes one message and
is itself an array. The elements of each array contain the information specified by the corresponding
column names in the `columns` parameter. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|MessagingMessagesResponse|


#### Consumes

* application/json

#### Tags

* messaging

### Performs a certain messaging action on a message.
```
PUT /messaging/message?action=perform
```

#### Description

On actions of type "message" the body should contain the JSON representation of the message the action should be applied to.
To invoke a messaging action of type "storage" the folder and id are needed in URL parameters.
Messaging actions of type "none" need a messaging message and account.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|messageAction|The message action to invoke.|true|string||
|QueryParameter|id|The ID of the message the action shall be invoked on. Only used on actions of type "storage".|false|string||
|QueryParameter|folder|The folder ID of the message. Only used on actions of type "storage".|false|string||
|QueryParameter|account|The account ID. Only used on actions of type "none".|false|integer||
|BodyParameter|body|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|true|MessagingMessageData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if message could be sent. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MessagingMessageUpdateResponse|


#### Consumes

* spplication/json

#### Tags

* messaging

### Sends a messaging message.
```
PUT /messaging/message?action=send
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|recipients|A list of recipients as defined in RFC822, like "Joe Doe <joe@doe.org>". If set the message is sent to the given list of recipients, otherwise this defaults to the "To" header of the message.|false|string||
|BodyParameter|body|A JSON array of JSON arrays with the folder and ID as elements each identifying a message.|true|MessagingMessageData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the number 1 if message could be sent. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|MessagingMessageUpdateResponse|


#### Consumes

* spplication/json

#### Tags

* messaging

### Gets all messaging services.
```
GET /messaging/service?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of messaging service objects. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingServicesResponse|


#### Tags

* messaging

### Gets a messaging service.
```
GET /messaging/service?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the messaging service to load.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the messaging service. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|MessagingServiceResponse|


#### Tags

* messaging

### Processes multiple requests to other modules in a single request.
```
PUT /multiple
```

#### Description

#### Not supported requests are:
 * the ones from modules login and multiple
 * POST requests with a multipart encoding (uploads)
 * GET requests which do not use an object as described in [Low level protocol](#low-level-protocol)

#### Request body
A JSON array with JSON objects describing the requests. Each object contains a field `module` with the
name of the request's module and the field `action` with the concrete request action to execute. Additionally the
parameters of the request are added as fields too. A session parameter is not included! If the request has
a request body itself, this body is stored as a JSON object in a field `data`.

#### Example: query reminder range and update a reminder's alarm
```json
[{"module":"reminder","action":"range","end":1497461067180},{"module":"reminder","action":"remindAgain","id":51,"data":{"alarm":1459478800000}}]
```


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|continue|Specifies whether processing of requests should stop when an error occurs, or whether all request should be processed regardless of errors.|false|boolean||
|BodyParameter|body|A JSON array with JSON objects, each describing one request.|true|SingleRequest array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array containing the response data of the processed requests where response[0] corresponds to request[0], response[1] to request[1], and so on.|SingleResponse array|
|400|Syntactically incorrect request.|No Content|


#### Consumes

* application/json

#### Tags

* multiple

### Gets all OAuth accounts (**available since v6.20**).
```
GET /oauth/accounts?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The service meta data identifier. If missing all accounts of all services are returned; otherwise all accounts of specified service are returned.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each describing an OAuth account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountsResponse|


#### Tags

* OAuth

### Creates an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=create
```

#### Description

This action is typically called by provided call-back URL and is only intended for manual invocation if
"outOfBand" interaction is returned by preceeding `/oauth/account?action=init` step.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|oauth_token|The request token from preceeding OAuth interaction.|true|string||
|QueryParameter|uuid|The UUID of the preceeding OAuth interaction.|true|string||
|QueryParameter|displayName|The display name for the new account.|true|string||
|QueryParameter|oauth_verifier|The verifier string which confirms that user granted access.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the newly created OAuth account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountResponse|


#### Tags

* OAuth

### Deletes an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The account identifier.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object indicating whether the deletion was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountDeletionResponse|


#### Tags

* OAuth

### Gets an OAuth account (**available since v6.20**).
```
GET /oauth/accounts?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The account identifier.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the OAuth account. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountResponse|


#### Tags

* OAuth

### Initializes the creation of an OAuth account (**available since v6.20**).
```
GET /oauth/accounts?action=init
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|serviceId|The service meta data identifier, e.g. "com.openexchange.oauth.twitter".|true|string||
|QueryParameter|displayName|The display name of the account.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the resulting interaction providing information to complete account creation.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|OAuthAccountInteractionResponse|


#### Tags

* OAuth

### Updates an OAuth account (**available since v6.20**).
```
PUT /oauth/accounts?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The account identifier. May also be provided in request body's JSON object by field `id`.|true|integer||
|BodyParameter|body|A JSON object providing the OAuth account data to update. Currently the only values which make sense being updated are `displayName` and the `token`-`secret`-pair.|true|OAuthAccountData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object indicating whether the update was successful. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthAccountUpdateResponse|


#### Consumes

* application/json

#### Tags

* OAuth

### Gets all OAuth grants (**available since v7.8.0**).
```
GET /oauth/grants?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each describing a granted access. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthGrantsResponse|


#### Tags

* OAuth

### Revokes access for an OAuth client (**available since v7.8.0**).
```
GET /oauth/grants?action=revoke
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|client|The ID of the client whose access shall be revoked.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Tags

* OAuth

### Gets all OAuth services' meta data (**available since v6.20**).
```
GET /oauth/services?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of JSON objects each describing an OAuth service's meta data. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthServicesResponse|


#### Tags

* OAuth

### Gets all OAuth service's meta data (**available since v6.20**).
```
GET /oauth/services?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The service's identifier.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the meta data of the OAuth service. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|OAuthServiceResponse|


#### Tags

* OAuth

### Updates or changes the password of the current use.
```
PUT /passwordchange?action=update
```

#### Description

#### Note
The new password will be set without any checks. The client must ensure that it is the password the user wants to set.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the old and the new password.|true|PasswordChangeBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* passwordchange

### Gets the filestore usage data.
```
GET /quota?action=filestore
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the filestore quota. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|QuotaResponse|


#### Tags

* quota

### Gets quota information (**available since v7.6.1, preliminary**).
```
GET /quota?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|module|The module identifier (e.g. "share_links", "filestorage", ...) to get quota information for, required if account is set.|false|string||
|QueryParameter|account|The account identifier within the module to get quota information for.|false|string||


#### Responses
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


#### Tags

* quota

### Gets the mail usage data.
```
GET /quota?action=mail
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the mail quota. In case of errors the responsible
fields in the response are filled (see [Error handling](#error-handling)).
|QuotaResponse|


#### Tags

* quota

### Deletes reminders (**available since v6.22**).
```
PUT /reminder?action=delete
```

#### Description

Before version 6.22 the request body contained only a JSON object with the field `id`.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array with JSON objects containing the field `id` of the reminders to delete.|true|ReminderListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with identifiers of reminders that were not deleted. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|RemindersResponse|


#### Consumes

* application/json

#### Tags

* reminder

### Gets a reminder range.
```
GET /reminder?action=range
```

#### Description

Gets all reminders which are scheduled until the specified time (end date).

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|end|The end date of the reminder range.|false|integer (int64)||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for each reminder. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|RemindersResponse|


#### Tags

* reminder

### Updates the reminder alarm (**available since v6.18.1**).
```
PUT /reminder?action=remindAgain
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the reminder whose alarm date shall be changed.|true|integer||
|BodyParameter|body|A JSON object containing the field `alarm` which provides the new reminder date.|true|ReminderUpdateBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the updated reminder. In case of
errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|ReminderResponse|


#### Consumes

* application/json

#### Tags

* reminder

### Gets all resources.
```
GET /resource?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of resource identifiers. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|AllResourcesResponse|


#### Tags

* resources

### Deletes resources.
```
PUT /resource?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the group to delete.|true|integer (int64)||
|BodyParameter|body|A JSON array of objects with the field `id` containing the unique identifier of the resource.|true|ResourceListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with an empty array if the resources were deleted successfully. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourcesResponse|


#### Consumes

* application/json

#### Tags

* resources

### Gets a resource.
```
GET /resource?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the resource.|true|integer||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the resource data. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourceResponse|


#### Tags

* resources

### Gets a list of resources.
```
PUT /resource?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id of the requested resources.|true|ResourceListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of resource objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourcesResponse|


#### Consumes

* application/json

#### Tags

* resources

### Creates a resource.
```
PUT /resource?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the resource data. The field `id` is not present.|true|ResourceData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the ID of the newly created resource. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourceUpdateResponse|


#### Consumes

* application/json

#### Tags

* resources

### Searches for resources.
```
PUT /resource?action=search
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object with the search parameters.|true|ResourceSearchBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of resource objects. In case of errors the responsible fields in the
response are filled (see [Error handling](#error-handling)).
|ResourcesResponse|


#### Consumes

* application/json

#### Tags

* resources

### Updates a resource.
```
PUT /resource?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|ID of the resource that shall be updated.|true|integer||
|QueryParameter|timestamp|Timestamp of the last update of the resource to update. If the resource was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the resource data fields to change. Only modified fields are present and the field id is omitted.|true|ResourceData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Consumes

* application/json

#### Tags

* resources

### Gets the new, modified and deleted resources.
```
GET /resource?action=updates
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested resources.|true|integer (int64)||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with fields `new`, `modified` and `deleted` representing arrays of new, modified and
deleted resource objects. In case of errors the responsible fields in the response are filled
(see [Error handling](#error-handling)).
|ResourceUpdatesResponse|


#### Tags

* resources

### Deletes a share link (**available since v7.8.0**).
```
PUT /share/management?action=deleteLink
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the share target where the link should be deleted for.|true|ShareTargetData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* share/management

### Creates or gets a share link (**available since v7.8.0**).
```
PUT /share/management?action=getLink
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the share target where the link should be generated for.|true|ShareTargetData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing data of the (newly created) share link. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ShareLinkResponse|


#### Consumes

* application/json

#### Tags

* share/management

### Sends a share link (**available since v7.8.0**).
```
PUT /share/management?action=sendLink
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the share target and a list of recipients specified in a field `recipients` that
is a JSON array with a nested two-elements array containing the recipient information (first element is
personal name, second is email address). An optional field `message` can contain a notification.
|true|ShareLinkSendBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Transport warnings that occurred during sending the notifications are available in a `warnings` array.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|ShareLinkSendResponse|


#### Consumes

* application/json

#### Tags

* share/management

### Updates a share link (**available since v7.8.0**).
```
PUT /share/management?action=updateLink
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|The timestamp of the last modification of the link. Used to detect concurrent modifications.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the share target and share link properties of the link to update. Only modified fields should be set but at least the share target ones.|true|ShareLinkUpdateBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing data of the (newly created) share link. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|ShareLinkResponse|


#### Consumes

* application/json

#### Tags

* share/management

### Gets all snippets (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|type|A list of comma-separated types to filter, e.g. "signature".|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all snippets. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetsResponse|


#### Tags

* snippet

### Attaches one or more files to an existing snippet (**available since v7.0.0/v6.22.0**).
```
POST /snippet?action=attach
```

#### Description

It can be uploaded multiple files at once. Each file must be specified in an own form field
(the form field name is arbitrary).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|QueryParameter|type|The file type filter to define which file types are allowed during upload. Currently supported filters are: file (for all), text (for `text/*`), media (for image, audio or video), image (for `image/*`), audio (for `audio/*`), video (for `video/*`) and application (for `application/*`).|true|enum (file, text, media, image, audio, video, application)||
|FormDataParameter|file|The attachment file.|true|file||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A HTML page as described in [File uploads](#file-uploads) containing a JSON object with the ID of
the updated snippet or errors if some occurred.
|string|


#### Consumes

* multipart/form-data

#### Produces

* text/html

#### Tags

* snippet

### Deletes one or multiple snippets (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=delete
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet. Otherwise provide one or more identifiers in the request body's JSON array.|false|string||
|BodyParameter|body|A JSON array containing the identifiers of the snippets to delete.|false|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* snippet

### Detaches one or more files from an existing snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=detach
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|BodyParameter|body|A JSON array with JSON objects each containing a field `id` with the identifier of an attachment that shall be removed.|true|SnippetAttachmentListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the updated snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetUpdateResponse|


#### Consumes

* application/json

#### Tags

* snippet

### Gets a snippet (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetResponse|


#### Tags

* snippet

### Gets the attachment of a snippet (**available since v7.0.0/v6.22.0**).
```
GET /snippet?action=getattachment
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|QueryParameter|attachmentid|The identifier of the attachment.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|The attachment's raw data.|string (binary)|
|500|A HTML page in case of errors.|string|


#### Tags

* snippet

### Gets a list of snippets (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array of snippet identifiers.|true|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested snippets. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetsResponse|


#### Consumes

* application/json

#### Tags

* snippet

### Creates a snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object describing the snippet excluding its attachment(s). For adding attachments see `/snippet?action=attach` request.|true|SnippetData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the ID of the newly created snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetUpdateResponse|


#### Consumes

* application/json

#### Tags

* snippet

### Updates a snippet (**available since v7.0.0/v6.22.0**).
```
PUT /snippet?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The identifier of the snippet.|true|string||
|BodyParameter|body|A JSON object providing the fields that should be changed, excluding its attachments. For deleting attachments see `/snippet?action=detach` request.|true|SnippetData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the updated snippet. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|SnippetResponse|


#### Consumes

* application/json

#### Tags

* snippet

### Clears a folder's content.
```
PUT /sync?action=refresh_server
```

#### Description

#### Note
Although the request offers to clear multiple folders at once it is recommended to clear only one folder per
request since if any exception occurs (e.g. missing permissions) the complete request is going to be aborted.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON array containing the folder ID(s).|false|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array of folder IDs that could not be cleared due to a concurrent modification.
In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|FoldersCleanUpResponse|


#### Tags

* sync

### Gets all tasks.
```
GET /tasks?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all tasks. Each array element describes one task and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TasksResponse|


#### Tags

* tasks

### Confirms a task.
```
PUT /tasks?action=confirm
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the task that shall be confirmed.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the task.|true|integer (int64)||
|BodyParameter|body|A JSON object with the fields `confirmation` and `confirmmessage`.|true|TaskConfirmationBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|Nothing, except the standard response object with empty data, the timestamp of the confirmed and thereby
updated task, and maybe errors. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CommonResponse|


#### Consumes

* application/json

#### Tags

* tasks

### Deletes tasks (**available since v6.22**).
```
PUT /tasks?action=delete
```

#### Description

Before version 6.22 the request body contained a JSON object with the fields `id` and `folder` and could
only delete one task.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the deleted tasks.|true|integer (int64)||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the tasks.|true|TaskListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON array with object IDs of tasks which were modified after the specified timestamp and were therefore not deleted. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskDeletionsResponse|


#### Consumes

* application/json

#### Tags

* tasks

### Gets a task.
```
GET /tasks?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested task.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested task. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskResponse|


#### Tags

* tasks

### Gets a list of tasks.
```
PUT /tasks?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|BodyParameter|body|A JSON array of JSON objects with the id and folder of the tasks.|true|TaskListElement array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested tasks. Each array element describes one task and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TasksResponse|


#### Consumes

* application/json

#### Tags

* tasks

### Creates a task.
```
PUT /tasks?action=new
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|BodyParameter|body|A JSON object containing the task's data.|true|TaskData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the id of the newly created task. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskUpdateResponse|


#### Consumes

* application/json

#### Tags

* tasks

### Search for tasks.
```
PUT /tasks?action=search
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|BodyParameter|body|A JSON object containing search parameters.|true|TaskSearchBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching tasks. Tasks are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|TasksResponse|


#### Consumes

* application/json

#### Tags

* tasks

### Updates a task.
```
PUT /tasks?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|id|Object ID of the requested task.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested tasks.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the task's data. Only modified fields are present.|true|TaskData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with a timestamp. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|TaskUpdateResponse|


#### Consumes

* application/json

#### Tags

* tasks

### Gets the new, modified and deleted tasks.
```
GET /tasks?action=updates
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|folder|Object ID of the folder who contains the tasks.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,200". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed task and appointment data](#detailed-task-and-appointment-data) and [Detailed task data](#detailed-task-data).|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested tasks.|true|integer (int64)||
|QueryParameter|ignore|Which kinds of updates should be ignored. Omit this parameter or set it to "deleted" to not have deleted tasks identifier in the response. Set this parameter to `false` and the response contains deleted tasks identifier.|false|enum (deleted, )||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An array with new, modified and deleted tasks. New and modified tasks are represented by arrays. The
elements of each array contain the information specified by the corresponding identifiers in the `columns`
parameter. Deleted tasks would be identified by their object IDs as integers, without being part of
a nested array. In case of errors the responsible fields in the response are filled (see
[Error handling](#error-handling)).
|TaskUpdatesResponse|


#### Tags

* tasks

### Gets a login token (**available since v7.4.0**).
```
GET /token?action=acquireToken
```

#### Description

With a valid session it is possible to acquire a secret. Using this secret another system is able to
generate a valid session (see `login?action=redeemToken`).


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object with the timestamp of the creation date and a token which can be used to create a new
session. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).
|AcquireTokenResponse|


#### Tags

* token

### Gets information about current user (**available since v7.6.2**).
```
GET /user/me
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the data of the current user. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|CurrentUserResponse|


#### Tags

* user/me

### Gets all users (**available since v6.14**).
```
GET /user?action=all
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entities should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for all users. Each array element describes one user and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UsersResponse|


#### Tags

* user

### Gets a user (**available since v6.14**).
```
GET /user?action=get
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested user. Since v6.18.1, this parameter is optional and the default is the currently logged in user.|false|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|An object containing all data of the requested user. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UserResponse|


#### Tags

* user

### Gets a user attribute (**available since v6.20**).
```
GET /user?action=getAttribute
```

#### Description

Gets a custom user attribute that was previously set with the `/user?action=setAttribute` request.

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the user.|true|string||
|QueryParameter|name|The name of the attribute.|true|string||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing the attribute data. In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|UserAttributeResponse|


#### Tags

* user

### Gets a list of users (**available since v6.14**).
```
PUT /user?action=list
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|true|string||
|BodyParameter|body|A JSON array of identifiers of the requested users. Since v6.18.1, a `null` value in the array is interpreted as the currently logged in user.|true|string array||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with data for the requested users. Each array element describes one user and
is itself an array. The elements of each array contain the information specified by the corresponding
identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UsersResponse|


#### Consumes

* application/json

#### Tags

* user

### Search for users (**available since v6.14**).
```
PUT /user?action=search
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|columns|A comma-separated list of columns to return, like "1,501,610". Each column is specified by a numeric column identifier, see [Common object data](#common-object-data), [Detailed contact data](#detailed-contact-data) and [Detailed user data](#detailed-user-data).|true|string||
|QueryParameter|sort|The identifier of a column which determines the sort order of the response. If this parameter is specified , then the parameter order must be also specified.|false|string||
|QueryParameter|order|"asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.|false|string||
|BodyParameter|body|A JSON object containing search parameters.|true|UserSearchBody||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object containing an array with matching users. Users are represented by arrays. The elements of each array contain the
information specified by the corresponding identifiers in the `columns` parameter. In case of errors the
responsible fields in the response are filled (see [Error handling](#error-handling)).
|UsersResponse|


#### Consumes

* application/json

#### Tags

* user

### Sets a user attribute (**available since v6.20**).
```
PUT /user?action=setAttribute
```

#### Description

Sets a custom user attribute consisting of a name and a value. The attribute can later be
retrieved using the `/user?action=getAttribute` request.


#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|The ID of the user.|true|string||
|QueryParameter|setIfAbsent|Set to `true` to put the value only if the specified name is not already associated with a value, otherwise `false` to put value in any case.|false|boolean||
|BodyParameter|body|A JSON object providing the name and the value of the attribute. If the `value` field is missing or `null`, the attribute is removed.|true|UserAttribute||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|A JSON object providing the information whether the attribute could be set. In case of errors the responsible fields in the response are
filled (see [Error handling](#error-handling)).
|UserAttributionResponse|


#### Consumes

* application/json

#### Tags

* user

### Updates a user (**available since v6.14**).
```
PUT /user?action=update
```

#### Parameters
|Type|Name|Description|Required|Schema|Default|
|----|----|----|----|----|----|
|QueryParameter|session|A session ID previously obtained from the login module.|true|string||
|QueryParameter|id|Object ID of the requested user.|true|string||
|QueryParameter|timestamp|Timestamp of the last update of the requested user. If the user was modified after the specified timestamp, then the update must fail.|true|integer (int64)||
|BodyParameter|body|A JSON object containing the user's data. Only modified fields are present. From [Detailed user data](#detailed-user-data) only the fields `timezone` and `locale` are allowed to be updated.|true|UserData||


#### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|In case of errors the responsible fields in the response are filled (see [Error handling](#error-handling)).|CommonResponse|


#### Consumes

* application/json

#### Tags

* user

## Definitions
### AcquireTokenResponse
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


### AllResourcesResponse
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


### AppointmentConfirmationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|confirmation|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|true|integer||
|confirmmessage|The confirmation message or comment.|true|string||
|id|User ID. Confirming for other users only works for appointments and not for tasks.|false|integer||


### AppointmentCreationConflict
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


### AppointmentCreationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of the appointment.|false|string||
|conflicts|An array of appointments which cause conflicts.|false|AppointmentCreationConflict array||


### AppointmentCreationResponse
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


### AppointmentData
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


### AppointmentDeletionsElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the appointment.|true|string||
|folder|The object ID of the related folder.|true|string||
|pos|Value of the field recurrence_position, if present in the appointment.|false|integer||


### AppointmentDeletionsResponse
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


### AppointmentFreeBusyItem
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


### AppointmentFreeBusyResponse
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


### AppointmentInfoResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|Array with elements that correspond with days in the time range, explaining whether a day has appointments or not.|false|boolean array||


### AppointmentListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the appointment.|true|string||
|folder|The object ID of the related folder.|true|string||
|recurrence_position|1-based position of an individual appointment in a sequence.|false|integer||
|recurrence_date_position|Date of an individual appointment in a sequence.|false|integer (int64)||


### AppointmentResponse
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


### AppointmentSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find appointments. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||
|startletter|Search appointments with the given starting letter.|false|string||


### AppointmentUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of the appointment.|false|string||


### AppointmentUpdateResponse
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


### AppointmentUpdatesResponse
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


### AppointmentsResponse
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


### AttachmentData
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


### AttachmentResponse
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


### AttachmentUpdatesResponse
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


### AttachmentsResponse
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


### AutoConfigResponse
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


### CapabilitiesResponse
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


### CapabilityData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the capability.|false|string||
|attributes|A JSON object holding properties of the capability.|false|object||


### CapabilityResponse
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


### ChangeIPResponse
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


### CommonResponse
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


### ConfigBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|The new value of the node specified by path.|true|object||


### ConfigProperty
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|The name of the property.|false|string||
|value|The value of the property.|false|object||


### ConfigPropertyBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|value|The concrete value to set.|false|string||


### ConfigPropertyResponse
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


### ConfigResponse
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


### ContactData
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


### ContactDeletionsResponse
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


### ContactListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the contact.|true|string||
|folder|The object ID of the related folder.|true|string||


### ContactResponse
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


### ContactSearchBody
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


### ContactUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a newly created contact.|false|string||


### ContactUpdateResponse
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


### ContactUpdatesResponse
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


### ContactsResponse
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


### ConversionBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|datasource||false|ConversionDataSource||
|datahandler||false|ConversionDataHandler||


### ConversionDataHandler
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|identifier|The identifier of the data handler.|false|string||
|args|A JSON array of optional JSON objects containing the name-value-pairs.|false|ConversionDataHandlerPair array||


### ConversionDataHandlerPair
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


### ConversionDataSource
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|identifier|The identifier of the data source.|false|string||
|args|A JSON array of optional JSON objects containing the name-value-pairs.|false|ConversionDataSourcePair array||


### ConversionDataSourcePair

A name-value-pair where only one name with a value must be filled out except the case when VCard data from speicified contact object(s) is obtained then the `folder` and `id` must be specified.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|com.openexchange.mail.conversion.fullname|The folder's full name.|false|string||
|com.openexchange.mail.conversion.mailid|The object ID of the mail.|false|string||
|com.openexchange.mail.conversion.sequenceid|The attachment sequence ID.|false|string||
|folder|A folder ID.|false|string||
|id|The ID.|false|string||


### ConversionResponse
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


### CurrentUserData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|context_id|The unique identifier of the user's context.|false|integer||
|user_id|The unique identifier of the user himself.|false|integer||
|context_admin|The ID of the context's administrator user.|false|integer||
|login_name|The login name of the user.|false|string||
|display_name|The display name of the user.|false|string||


### CurrentUserResponse
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


### DistributionListMember
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|Object ID of the member's contact if the member is an existing contact.|false|string||
|folder_id|Parent folder ID of the member's contact if the member is an existing contact (preliminary, from 6.22 on).|false|string||
|display_name|The display name.|false|string||
|mail|The email address (mandatory before 6.22, afterwards optional if you are referring to an internal contact).|false|string||
|mail_field|Which email field of an existing contact (if any) is used for the mail field: 0 (independent contact), 1 (default email field, email1), 2 (second email field, email2), 3 (third email field, email3).|false|number||


### FileAccountCreationResponse
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


### FileAccountData
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


### FileAccountResponse
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


### FileAccountUpdateResponse
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


### FileAccountsResponse
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


### FileServiceConfiguration
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|widget|The name of the widget.|false|string||
|name|The name of the field.|false|string||
|displayName|The display name of the field.|false|string||
|mandatory|Indicates whether the field is mandatory.|false|boolean||
|options|A list of available options in the field.|false|object array||
|defaultValue|Can contain a default value.|false|object||


### FileServiceData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the file storage service, e.g. "boxcom".|false|string||
|displayName|A human-readable display name of the service, e.g. "Box File Storage Service"|false|string||
|configuration|An array of dynamic form fields. Same as in PubSub.|false|FileServiceConfiguration array||


### FileServiceResponse
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


### FileServicesResponse
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


### FindActiveFacet
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the according facet.|false|string||
|value|The ID of the according value. Must always be copied from the value object, not from a possibly according option (in the two-dimensional case).|false|string||
|filter||false|FindActiveFacetFilter||


### FindActiveFacetFilter

The filter object, copied from the value or option.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|fields|An array of fields to search for.|false|string array||
|queries|An array of corresponding search values.|false|string array||


### FindAutoCompleteBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|prefix|The user's search input.|false|string||
|facets|An array of already selected facets, meaning categories the user has filtered by before.|false|FindFacetData array||
|options||false|FindOptionsData||


### FindAutoCompleteData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|facets|An array of facets each describing a possible search category or an already applied category.|false|FindFacetData array||


### FindAutoCompleteResponse
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


### FindFacetData
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


### FindFacetFilter

The filter to refine the search. (for simple)

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|fields|An array of fields to search for.|false|string array||
|queries|An array of corresponding search values.|false|string array||


### FindFacetItem

A more complex object to display this facet. Attributes are `name`, `detail` (optional), and `image_url` (optional). (for simple, default, and exclusive)

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|A displayable (and localized) name for the facet.|false|string||
|detail|A displayable (and localized) detail name, like "in mail text".|false|string||
|image_url|An URL to a displayable image.|false|string||


### FindFacetValue
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the value. Must be unique within one facet.|false|string||
|name|A displayable (and localized) name for this facet. If absent, an `item` attribute is present.|false|string||
|item||false|FindFacetValueItem||
|filter||false|FindFacetValueFilter||
|options|An array of options to refine the search.|false|FindFacetValueOption array||


### FindFacetValueFilter

The filter to refine the search.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|fields|An array of fields to search for.|false|string array||
|queries|An array of corresponding search values.|false|string array||


### FindFacetValueItem

A more complex object to display this facet. Attributes are `name`, `detail` (optional), and `image_url` (optional).

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|A displayable (and localized) name for the facet.|false|string||
|detail|A displayable (and localized) detail name, like "in mail text".|false|string||
|image_url|An URL to a displayable image.|false|string||


### FindFacetValueOption
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the option. Must be unique within a set of options.|false|string||
|name|The displayable (and localized) name for this option.|false|string||
|filter||false|FindFacetValueFilter||


### FindOptionsData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|timezone|The timezone to use if any dates are returned.|false|string||
|admin|Indicates whether the context admin shall be included if it matches any search criteria. If the context admin shall always be ignored (i.e. not returned), `false` has to be set.|false|boolean||


### FindQueryBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|facets|An array of selected facets that shall be applied for search.|false|FindActiveFacet array||
|options||false|FindOptionsData||
|start|The start of a pagination, if desired.|false|integer||
|size|The page size of a pagination, if desired.|false|integer||


### FindQueryResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|num_found|The number of found items.|false|integer||
|start|The start of the pagination.|false|integer||
|size|The page size.|false|integer||
|results|An array of search results. Each result is described by a JSON object containing the fields specified in the `columns` parameter.|false|object array||


### FolderBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder||true|FolderData||
|notification||false|FolderBodyNotification||


### FolderBodyNotification
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|transport|E.g. "mail".|false|string||
|message||false|string||


### FolderData
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


### FolderExtendedPermission
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


### FolderPermission
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


### FolderResponse
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


### FolderSharingNotificationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entities|Array containing the entity ID(s) of the users or groups that shall be notified.|true|string array||
|notification||false|FolderBodyNotification||


### FolderSharingNotificationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|warnings|Can contain transport warnings that occured during sending the notifications.|false|object array||


### FolderSharingNotificationResponse
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


### FolderUpdateResponse
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


### FolderUpdatesResponse
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


### FoldersCleanUpResponse
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


### FoldersResponse
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


### FoldersVisibilityData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|private|Array of private folders. Each folder is described as an array itself.|false|object array array||
|public|Array of public folders. Each folder is described as an array itself.|false|object array array||
|shared|Array of shared folders. Each folder is described as an array itself.|false|object array array||


### FoldersVisibilityResponse
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


### FreeBusyData
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


### FreeBusyResponse
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


### FreeBusysResponse
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


### GroupData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The group ID.|false|integer||
|display_name|Display name of the group.|false|string||
|name|Internal name with character restrictions.|false|string||
|members|The array contains identifiers of users that are member of the group.|false|integer array||
|last_modified_utc|Timestamp of the last modification.|false|integer (int64)||


### GroupListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a group.|false|integer||


### GroupResponse
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


### GroupSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find groups. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||


### GroupUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of a newly created group.|false|integer||


### GroupUpdateResponse
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


### GroupUpdatesData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|new|Array of new group objects.|false|GroupData array||
|modified|Array of modified group objects.|false|GroupData array||
|deleted|Array of deleted group objects.|false|GroupData array||


### GroupUpdatesResponse
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


### GroupsResponse
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


### HaloInvestigationResponse
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


### HaloServicesResponse
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


### InfoItemBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|file||true|InfoItemData||
|notification||false|InfoItemBodyNotification||


### InfoItemBodyNotification

Responsible for sending out notifications for changed object permissions of an infoitem.

|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|transport|E.g. "mail".|false|string||
|message||false|string||


### InfoItemData
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


### InfoItemDetachResponse
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


### InfoItemExtendedPermission
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


### InfoItemListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the infoitem (e.g. "31841/36639").|true|string||
|folder|The object ID of the related folder (e.g. "31841").|true|string||


### InfoItemPermission
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


### InfoItemResponse
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


### InfoItemSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|The search pattern, where "*" matches any sequence of characters.|false|string||


### InfoItemSharingNotificationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|entities|Array containing the entity ID(s) of the users or groups that shall be notified.|true|string array||
|notification||false|InfoItemBodyNotification||


### InfoItemSharingNotificationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|warnings|Can contain transport warnings that occured during sending the notifications.|false|object array||


### InfoItemSharingNotificationResponse
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


### InfoItemUpdateResponse
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


### InfoItemUpdatesResponse
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


### InfoItemZipElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the infoitem (e.g. "31841/36639").|true|string||
|folder|The object ID of the related folder (e.g. "31841").|true|string||
|version|The version of the infoitem.|false|string||


### InfoItemsMovedResponse
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


### InfoItemsResponse
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


### JSlobData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the JSlob.|false|string||
|tree|The JSON object that is stored in the JSlob.|false|object||
|meta|A JSON object containing meta data.|false|object||


### JSlobsResponse
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


### JumpResponse
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


### JumpTokenData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|token|The identifier of the token.|false|string||


### LoginResponse
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


### MailAccountData
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


### MailAccountDeletionResponse
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


### MailAccountResponse
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


### MailAccountUpdateResponse
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


### MailAccountValidationResponse
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


### MailAccountsResponse
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


### MailAckBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|from|The from email address.|false|string||
|folder|The ID of the folder where the mail is placed.|false|string||
|id|The ID of the mail.|false|string||


### MailAckResponse
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


### MailAttachment
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|Object ID (unique only inside the same message).|false|string||
|content_type|MIME type.|false|string||
|content|Content as text. Present only if easily convertible to text.|false|string||
|filename|Displayed filename (mutually exclusive with content).|false|string||
|size|Size of the attachment in bytes.|false|integer (int64)||
|disp|Attachment's disposition: null, inline, attachment or alternative.|false|string||


### MailConversationData
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


### MailConversationsResponse
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


### MailCountResponse
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


### MailData
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


### MailDestinationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder_id|The object ID of the destination folder.|false|string||


### MailDestinationData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder_id|Object ID of the destination folder.|false|string||
|id|Object ID of the "new" mail.|false|string||


### MailDestinationResponse
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


### MailFilterAction
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


### MailFilterConfigData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|tests|Array of available test-objects.|false|MailFilterConfigTest array||
|actioncommands|Array of available action commands.|false|string array||


### MailFilterConfigResponse
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


### MailFilterConfigTest
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|test|The name of the test, see [Possible tests](#possible-tests).|false|string||
|comparison|An array of the valid comparison types for this test, see [Possible comparisons](#possible-comparisons).|false|string array||


### MailFilterCreationResponse
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


### MailFilterDeletionBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the rule that shall be deleted.|false|integer||


### MailFilterNotTest

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


### MailFilterRule
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


### MailFilterRulesResponse
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


### MailFilterScriptResponse
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


### MailFilterTest
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


### MailHeadersResponse
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


### MailImportResponse
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


### MailListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the mail.|true|string||
|folder|The object ID of the related folder.|true|string||


### MailReplyData
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


### MailReplyResponse
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


### MailResponse
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


### MailSourceResponse
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


### MailUpdateBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|folder_id|The object ID of the destination folder (if the mail shall be moved).|false|string||
|color_label|The color number between 0 and 10.|false|integer||
|flags|A set of flags to add or remove. Note: Flags for "recent" (8) and "user" (64) are ignored.|false|integer||
|value|Use true to add the flags specified by flags (logical OR) and false to remove them (logical AND with the inverted value).|false|boolean||
|set_flags|A set of flags to add. Note: Flags for "recent" (8) and "user" (64) are ignored (available since SP5 v6.10).|false|integer (int64)||
|clear_flags|A set of flags to remove. Note: Flags for "recent" (8) and "user" (64) are ignored (available since SP5 v6.10).|false|integer||


### MailUpdatesResponse
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


### Mail_CategoriesMoveBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the mail|false|string||
|folder_id|The folder ID of the mail|false|string||


### Mail_CategoriesTrainBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|from|An array of email addresses|false|string array||


### Mail_CategoriesUnreadResponse
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|data|A JSON object with a field for each active category containing the number of unread messages.|false|object||
|error|The translated error message. Present in case of errors.|false|string||
|error_params|As of 7.4.2: Empty JSON array. Before that: Parameters for the error message that would need to be replaced in the error string (in a printf-format style).|false|string array||
|error_id|Unique error identifier to help finding this error instance in the server logs.|false|string||
|error_desc|The technical error message (always English) useful for debugging the problem. Might be the same as error message if there is no more information available.|false|string||
|error_stack|If configured (see "com.openexchange.ajax.response.includeStackTraceOnError" in "server.properties") this field provides the stack trace of associated Java exception represented as a JSON array.|false|string array||
|code|Error code consisting of an upper-case module identifier and a four-digit message number, separated by a dash; e.g. "MSG-0012"|false|string||
|categories|Either a single (String) or list (Array) of upper-case category identifiers to which the error belongs.|false|string||
|category|Maintained for legacy reasons: The numeric representation of the first category.|false|integer||
|timestamp|The latest timestamp of the returned data (see [Updates](#updates)).|false|integer (int64)||


### MailsAllSeenResponse
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


### MailsCleanUpResponse
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


### MailsResponse
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


### MessagingAccountData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|Identifier of the messaging account.|false|integer||
|messagingService|The messaging service ID of the messaging service this account belongs to.|false|string||
|displayName|User chosen string to identify a given account. Will also be translated into the folder name of the folder representing the accounts content.|false|string||
|configuration|The configuration data according to the `formDescription` of the relevant messaging service.|false|object||


### MessagingAccountResponse
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


### MessagingAccountUpdateResponse
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


### MessagingAccountsResponse
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


### MessagingFormDescription
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|widget|The name of the widget.|false|string||
|name|The name of the field.|false|string||
|displayName|The display name of the field.|false|string||
|mandatory|Indicates whether the field is mandatory.|false|boolean||
|options|A list of available options in the field.|false|object array||
|defaultValue|Can contain a default value.|false|object||


### MessagingMessageData
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


### MessagingMessageResponse
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


### MessagingMessageUpdateResponse
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


### MessagingMessagesResponse
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


### MessagingServiceData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the messaging service. This is usually a string in reverse domain name notation, like "com.openexchange.messaging.twitter".|false|string||
|displayName|Human-readable display name of the service.|false|string||
|formDescription|An array of dynamic form fields. Same as in PubSub.|false|MessagingFormDescription array||
|messagingActions|An array representing a dynamic set of actions that are possible with messages of this service.|false|string array||


### MessagingServiceResponse
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


### MessagingServicesResponse
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


### OAuthAccountData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The numeric identifier of the OAuth account.|false|integer||
|displayName|The account's display name.|false|string||
|serviceId|The identifier of the associated service meta data, e.g. "com.openexchange.oauth.twitter".|false|string||
|token|The token.|false|string||
|secret|The token secret.|false|string||


### OAuthAccountDeletionResponse
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


### OAuthAccountInteraction
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|authUrl|The numeric identifier of the OAuth account.|false|string||
|type|The interaction type name, which can be "outOfBand" or "callback".|false|string||
|token|The token.|false|string||
|uuid|The UUID for this OAuth interaction.|false|string||


### OAuthAccountInteractionResponse
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


### OAuthAccountResponse
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


### OAuthAccountUpdateResponse
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


### OAuthAccountsResponse
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


### OAuthClientData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The client's ID.|false|string||
|name|The client's/service's name.|false|string||
|description|A description of the client.|false|string||
|website|A URL to the client's website.|false|string||
|icon|A URL or path to obtain the client's icon via the image module.|false|string||


### OAuthGrantData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|client||false|OAuthClientData||
|scopes|A mapping from scope tokens to translated, human-readable descriptions for every scope that was granted to the external service (example: {"read_contacts":"See all your contacts"}).|false|object||
|date|The time when the access was granted.|false|integer (int64)||


### OAuthGrantsResponse
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


### OAuthServiceMetaData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of the service meta data, e.g. "com.openexchange.oauth.twitter".|false|string||
|displayName|The service's display name.|false|string||


### OAuthServiceResponse
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


### OAuthServicesResponse
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


### PasswordChangeBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|old_password|The user's current password or `null` if the password wasn't set before (especially for guest users).|false|string||
|new_password|The new password the user wants to set or `null` to remove the password (especially for guest users).|false|string||


### QuotaData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|quota|Represents the maximum storage (-1 represents an unlimited quota).|false|integer (int64)||
|use|Represents the used storage.|false|integer (int64)||


### QuotaResponse
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


### QuotasResponse
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


### ReminderData
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


### ReminderListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the reminder.|false|integer||


### ReminderResponse
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


### ReminderUpdateBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|alarm|The new time of the alarm.|false|integer (int64)||


### RemindersResponse
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


### ResourceData
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


### ResourceListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a resource.|false|integer||


### ResourceResponse
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


### ResourceSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find resources. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||


### ResourceUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of a newly created rsource.|false|integer||


### ResourceUpdateResponse
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


### ResourceUpdatesData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|new|Array of new resource objects.|false|ResourceData array||
|modified|Array of modified resource objects.|false|ResourceData array||
|deleted|Array of deleted resource objects.|false|ResourceData array||


### ResourceUpdatesResponse
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


### ResourcesResponse
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


### SendMailData
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


### ShareLinkData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|url|The link to share (read-only).|false|string||
|entity|The identifier of the anonymous user entity for the share (read-only).|false|integer||
|is_new|Whether the share link is new, i.e. it has been created by the `/share/management?action=getLink` request, or if it already existed (read-only).|false|boolean||
|expiry_date|The end date / expiration time after which the share link is no longer accessible.|false|integer (int64)||
|password|An additional secret / pin number an anonymous user needs to enter when accessing the share.|false|string||
|meta|Can be used by the client to save arbitrary JSON data along with the share.|false|object||


### ShareLinkResponse
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


### ShareLinkSendBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|recipients|An array that lists the recipients. Each element is itself a two-element array specifying one recipient. The first element represents the personal name, the second element is the email address.|false|string array array||
|message|Can contain an optional custom message.|false|string||
|module|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|false|string||
|folder|The folder identifier.|false|string||
|item|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|false|string||


### ShareLinkSendResponse
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


### ShareLinkUpdateBody
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


### ShareTargetData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|module|The folder's module name, i.e. one of "tasks", "calendar", "contacts" or "infostore".|false|string||
|folder|The folder identifier.|false|string||
|item|The object identifier, in case the share target is a single item. This must not be present to share a complete folder.|false|string||


### SingleRequest

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


### SingleResponse
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


### SnippetAttachment
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The ID of the attachment.|false|string||
|filename|The file name of the attachment.|false|string||
|mimetype|The MIME type of the attachment.|false|string||
|contentid|The content ID of the attachment.|false|string||
|size|The size of the attachment in bytes.|false|integer (int64)||


### SnippetAttachmentListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The identifier of an attachment.|false|string||


### SnippetData
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


### SnippetResponse
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


### SnippetUpdateResponse
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


### SnippetsResponse
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


### TaskConfirmation
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|type|Type of participant: 0 (user), 5 (external user).|false|integer||
|mail|Email address of external participant.|false|string||
|display_name|Display name of external participant.|false|string||
|status|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|false|integer||
|message|Confirm message of the participant.|false|string||


### TaskConfirmationBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|confirmation|0 (none), 1 (accepted), 2 (declined), 3 (tentative).|false|integer||
|confirmmessage|The confirmation message or comment.|false|string||


### TaskData
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


### TaskDeletionsResponse
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


### TaskListElement
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|The object ID of the task.|true|string||
|folder|The object ID of the related folder.|true|string||


### TaskParticipant
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|User ID.|false|integer||
|type|Type of participant: 1 (user), 2 (user group), 3 (resource), 4 (resource group), 5 (external user)|false|integer||
|mail|Mail address of an external participant.|false|string||


### TaskResponse
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


### TaskSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find tasks. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|true|string||
|folder|Defines the folder to search for tasks in. If this is omitted in all task folders will be searched.|false|string||
|start|Inclusive start date for a time range the tasks should end in. If start is omitted end is ignored.|false|integer (int64)||
|end|Exclusive end date for a time range the tasks should end in. If this parameter is omitted the time range has an open end.|false|integer (int64)||


### TaskUpdateData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|ID of a newly created task.|false|string||


### TaskUpdateResponse
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


### TaskUpdatesResponse
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


### TaskUser
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|id|User ID. Confirming for other users only works for appointments and not for tasks.|false|integer||
|display_name|Displayable name of the participant.|false|string||
|confirm|0 (none), 1 (accepted), 2 (declined), 3 (tentative)|false|integer||
|confirmmessage|Confirm message of the participant.|false|string||


### TasksResponse
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


### TokenLoginResponse
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


### TokensData
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|session|The session ID.|false|string||
|user|The username.|false|string||
|user_id|The user ID.|false|integer||
|context_id|The context ID.|false|integer||
|locale|The users locale (e.g. "en_US").|false|string||


### TokensResponse
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


### UserAttribute
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|name|The name of the attribute.|false|string||
|value|The value of the attribute.|false|string||


### UserAttributeResponse
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


### UserAttributionResponse
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


### UserData
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


### UserResponse
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


### UserSearchBody
|Name|Description|Required|Schema|Default|
|----|----|----|----|----|
|pattern|Search pattern to find tasks. In the pattern, the character "*" matches zero or more characters and the character "?" matches exactly one character. All other characters match only themselves.|false|string||
|startletter|Search users with the given startletter. If this field is present, the pattern is matched against the user field which is specified by the property "contact_first_letter_field" on the server (default: last name). Otherwise, the pattern is matched against the display name.|false|boolean||
|last_name|Searches users where the last name matches with the given last name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|false|string||
|first_name|Searches users where the first name matches with the given first name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|false|string||
|display_name|Searches users where the display name matches with the given display name. The character "*" matches zero or more characters and the character "?" matches exactly one character. This field is ignored if `pattern` is specified.|false|string||
|orSearch|If set to `true`, the fields `last_name`, `first_name` and `display_name` are connected through an OR search habit. This field is ignored if `pattern` is specified.|false|boolean||
|emailAutoComplete|If set to `true`, results are guaranteed to contain at least one email adress and the search is performed by connecting the relevant fields through an OR search habit. This field is ignored if `pattern` is specified.|false|boolean||


### UsersResponse
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



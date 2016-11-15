# Introduction

This pages describes the HTTP API of the OX Middleware.

## Low level protocol

The client accesses the server through HTTP GET, POST and PUT requests. HTTP cookies are used for authentication and must therefore be processed and sent back by the client as specified by [RFC 6265](http://tools.ietf.org/html/rfc6265). The HTTP API is accessible at URIs starting with `/ajax`. Each server module has a unique name and its own sub-namespace with that name below `/ajax`, e. g. all access to the module "tasks" is via URIs starting with `/ajax/tasks`.

Text encoding is always UTF-8. Data is sent from the server to the client as text/javascript and interpreted by the client to obtain an ECMAScript object. The HTTP API uses only a small subset of the ECMAScript syntax. This subset is roughly described by the following BNF:

```
Value	::= "null" | Boolean | Number | String | Array | Object
Boolean	::= "true" | "false"
Number	::= see NumericLiteral in ECMA 262 3rd edition
String	::= \"([^"\n\\]|\\["\n\\])*\"
Array	::= "[]" | "[" Value ("," Value)* "]"
Object	::= "{}" | "{" Name ":" Value ("," Name ":" Value)* "}"
Name	::= [A-Fa-f][0-9A-Fa-f_]*
```

Numbers are the standard signed integer and floating point numbers. Strings can contain any character, except double quotes, newlines and backslashes, which must be escaped by a backslash. Control characters in strings (other than newline) are not supported. Whitespace is allowed between any two tokens. See [JSON](http://json.org/) and [ECMA 262, 3rd edition](http://www.ecma-international.org/publications/standards/Ecma-262.htm) for the formal definition.

The response body consists of an object, which contains up to four fields as described in Response body. The field data contains the actual payload which is described in following chapters. The fields `timestamp`, `error` and `error_params` are present when data objects are returned, if an error occurred and if the error message contains conversion specifiers, respectively. Following sections describe the contents of these fields in more detail.<br>

| Name | Type | Value |
|:------|:------|:-------|
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

## Error handling
If the session of the user times out, if the client doesn't send a session ID or if the session for the specified session ID can not be found then the server returns the above described response object, that contains an error code and an error message. If the request URI or the request body is malformed or incomplete then the server returns the reponse object with an error message, too. In case of internal server errors, especially Java exceptions, or if the server is down, it returns the HTTP status code 503, Service Unavailable. Other severe errors may return other HTTP status values.

Application errors, which can be caused by a user and are therefore expected during the operation of the groupware, are reported by setting the field error in the returned object, as described in Response body. Since the error messages are translated by the client, they can not be composed of multiple variable parts. Instead, the error message can contain simplified printf()-style conversion specifications, which are replaced by elements from the array in the field `error_params`. If `error_params` is not present, no replacement occurs, even if parts of the error message match the syntax of a conversion specification.

A simplified conversion specification, as used for error messages, is either of the form _%s_ or _%n$s_, where _n_ is a 1-based decimal parameter index. The conversion specifications are replaced from left to right by elements from `error_params`, starting at the first element. _%s_ is replaced by the current element and the current index is incremented. _%n$s_ is replaced by the _n_'th element and the current index is set to the _(n + 1)_'th element.

Some error message contain data sizes which must be expressed in Bytes or Kilobytes etc., depending on the actual value. Since the unit must be translated, this conversion is performed by the client. Unfortunately, standard printf()-style formatting does not have a specifier for this kind of translation. Therefore, the conversion specification for sizes is the same as for normal strings, and the client has to determine which parameters to translate based on the error code. The current error codes and the corresponding size parameters are listed below:

| Error code | Parameter indices |
|:------------|:-------------------|
| CON-0101 | 2, 3 |
| FLS-0003 | 1, 2, 3 |
| MSG-0065 | 1, 3 |
| MSG-0066 | 1 |
| NON-0005 | 1, 2 |

## Date and time

Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC. Leap seconds are ignored, therefore this number is always an integer multiple of 8.64e7.

Because ECMAScript Date objects have no way to explicitly specify a timezone for calculations, timezone correction must be performed on the server. Dates with time are transmitted as the number of milliseconds since 1970-01-01 00:00 UTC (again, ignoring leap seconds) plus the offset between the user's timezone and UTC at the time in question. (See the Java method java.util.TimeZone.getOffset(long)). Unless optional URL parameter `timezone` is present. Then dates with time are transmitted as the number of milliseconds since 1970-01-01 00:00 UTC (again, ignoring leap seconds) plus the offset between the _specified_ timezone and UTC at the time in question.

For some date and time values, especially timestamps, monotonicity is more important than the actual value. Such values are transmitted as the number of milliseconds since 1970-01-01 00:00 UTC, ignoring leap seconds and without timezone correction. If possible, a unique strictly monotonic increasing value should be used instead, as it avoids some race conditions described below.

This specification refers to these three interpretations of the type Number as separate data types.

| Type | Time | Timezone | Comment |
|:------|:------|:----------|:---------|
| Date | No | UTC | Date without time. |
| Time | Yes | User | Date and time. |
| Timestamp | Yes | UTC | Timestamp or unique sequence number. |

## Updates

To allow efficient synchronization of a client with changes made by other clients and to detect conflicts, the server stores a timestamp of the last modification for each object. Whenever the server transmits data objects to the client, the response object described in Response body includes the field `timestamp`. This field contains a timestamp value which is computed as the maximum of the timestamps of all transmitted objects.

When requesting updates to a previously retrieved set of objects, the client sends the last timestamp which belongs to that set of objects. The response contains all updates with timestamps greater than the one specified by the client. The field timestamp of the response contains the new maximum timestamp value.

If multiple different objects may have the same timestamp values, then a race condition exists when an update is processed between two such objects being modified. The first, already modified object will be included in the update response and its timestamp will be the maximum timestamp value sent in the timestamp field of the response. If the second object is modified later but gets the same timestamp, the client will never see the update to that object because the next update request from the client supplies the same timestamp value, but only modifications with greater timestamp values are returned.

If unique sequence numbers can't be used as timestamps, then the risk of the race condition can be at least minimized by storing timestamps in the most precise format and/or limiting update results to changes with timestamp values which are measurably smaller than the current timestamp value.

## Editing

Editing objects is performed one object at a time. There may be multiple objects being edited by the same client simulataneously, but this is achieved by repeating the steps required for editing a single object. There is no batch edit or upload command.

To edit an object, a client first requests the entire object from the server. The server response contains the `timestamp` field described in the previous section. For in-place editing inside a view of multiple objects, where only already retrieved fields can be changed, retrieving the entire object is not necessary, and the last timestamp of the view is used as the timestamp of each object in it.

When sending the modified object back to the server, only modified fields need to be included in the sent object. The request also includes the timestamp of the edited object. The timestamp is used by the server to ensure that the object was not edited by another client in the meantime. If the current timestamp of the object is greater than the timestamp supplied by the client, then a conflict is detected and the field error is set in the response. Otherwise, the object gets a new timestamp and the response to the client is empty.

If the client displays the edited object in a view together with other objects, then the client will need to perform an update of that view immediately after successfully uploading an edited object.

## File uploads

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


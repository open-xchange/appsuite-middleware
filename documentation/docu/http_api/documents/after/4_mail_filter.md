# Mail filter

This chapter gives a detailed description of the mail filter module. It should be uses as a source of information in addition to the http api.

First of all the main structure of a mail filter script is, that it has different rules. Each of them contains one command. This command takes a test condition which executes the actions given in that command if the test condition is true.

The test condition consists of a test command and some arguments for this command depending on the command itself. Because the available tests depend on the mail filter server, these tests must be determined at runtime. So that no test field is transferred to the server which it isn't able to handle. Examples for tests are address, allof and anyof.

Each test has a special comparison. The list of available comparisons depends on the test given and the mail filter server configuration so they have to be determined at runtime too.

Each time you want to do some action on a mail you need a so called action command. This describes what to do with a mail. To action commands the same applies as to the test commands and their comparison types, they must be determined at runtime.

All those dynamical values can be fetched via a config object at startup. This object shows the capabilities of the server to the client. This allows the client to show only the capabilities the server actually has to the user and to send only objects to the server which produce no errors on the server side.

To deal with this high flexibility of mail filters this specification can be roughly divided into 2 parts. A non-changing core and dynamical extensions. The core specification is that each rule consists of a name, an ID, a value showing if this rule is active or not and a tag which can be set on a rule to mark that rule for a special purpose. Furthermore each rule takes a test object, specifying in what case the following actions are triggered, and one or many actioncommands, specifying the actions itself.

The objects for the tests and the actions are dynamical, so the set presented in this specification may be changed over the time, but only compatible changes are allowed to the objects, otherwise new test or action objects have to be introduced. Due to the fact that not every sieve implementation will offer all the capabilities written in this document, the server will sent his configuration if a special request is made. This configuration will show which of the tests and which of the actions are allowed. So for example if the server signals that it is capable of handling vacation, sending a vacation object as action is safe.

Furthermore some tests use a comparison field as stated above which specifies how the fields are compared. The values of this field must also be determined at runtime. So in the configuration object there is a special part which shows the comparisons which are available. Note that not all comparisons can be used with every test. Some of them are denoted to a special test, which is described in [Possible comparisons](#possible-comparisons).

## Possible tests

<div class="mailFilterToc">

| Name | Description |
|:------|:-------------|
| [address](#address-test) | This test type applies to addresses only. So it may be used for all header fields which contain addresses. This test returns true if any combination of the header-list and values-list arguments match. |
| [envelope](#envelope-test) | This test applies to the envelope of a mail. This test isn't used under normal circumstances as the envelope isn't accessible in all mail setups. This test returns true if any combination of the header-list and values-list arguments match. |
| [true](#true-test) | A test for a true result (can be used if an action command should be executed every time). |
| [not](#not-test) | Negates a given test. |
| [size](#not-test) | Deals with the size of the mail. |
| [currentdate](#currentdate-test) | Compares a given date with the current date (available since v6.20) |
| [header](#header-test) | Tests against all headers of a mail. So with this test in contrast to the address test also fields such as subject can be handled. This test returns true if any combination of the header-list and values-list arguments match. |
| [body](#body-test) | Tests against the content of a mail. |
| [allof](#allof-test) | Defines an AND condition between several tests. |
| [anyof](#anyof-test) | Defines an OR condition between several tests. |

</div>

### Possible comparisons

| Name | Description |
|:------|:-------------|
| is | If a field is equal to a given value. |
| contains | If a field contains a given value at any position. |
| matches | Tests if the value matches the value in the specified field ("*" matches zero or more characters, "?" matches a single character, to use these characters themselves they have to be escaped via backslash). | 
| regex | Tests if a given regular expression matches with the value present in the specified field. |
| user | Tests if the user part of an e-mail address is the value given here. This means in herbert+mustermann@example.com. The user checks the part herbert (only possible in conjunction with the `address` test). | 
| detail | Tests if the detail part of an e-mail address is the value given here. In the example above this evaluates to mustermann (only possible in conjunction with the `address` test). |

### Possible currentdate comparisons

| Name | Description |
|:------|:-------------|
| is | Used in the date test to check for a value equal to the given one. |
| ge | Used in the date test to check for a value greater or equal to the given one. |
| le | Used in the date test to check for a value less or equal to the given one. |

### Possible size comparisons

| Name | Description |
|:------|:-------------|
| over | Used in the size test to check for a value greater than the given one.|
| under | Used in the size test to check for a value less than the given one. |

### Possible extensions

| Name | Description |
|:------|:-------------|
| content | An extension used in conjunction with the body test to define the content which should be considered. This extension will need a parameter specifying the mime-type of the part of the message which will be searched. |
| text | An extension used in conjunction with the body test to define that only the text of the body should be considered in this test. This extension takes no parameter. |

## Structure of tests

This section describes the structures of tests.

### address-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String |address | A string describing the test command.|
|comparison | String ||	Available types can be found in the config object. (see [Possible comparisons](#possible-comparisons)).|
|headers | Array ||	A string array containing the header fields.|
|values | Array || A string array containing the value for the header fields. The test will be true if any of the strings matches.|

### envelope-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | envelope | A string describing the test command. |
|comparison | String || Available types can be found in the config object. (see [Possible comparisons](#possible-comparisons)).|
|headers | Array || A string array containing the header fields.|
|values | Array || A string array containing the value for the header fields. The test will be true if any of the strings matches.|

### true-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | true | A string describing the test command.|

### not-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | not | A string describing the test command.|
|test | Object || One of the test objects which result will be negated.|

### size-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | size | A string describing the test command.|
|comparison | String || Only two types are valid here. A description can be found in [Possible size comparisons](#possible-size-comparisons).|
|size | Number || A number specifying the size for this comparison, in bytes.|

### currentdate-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | currentdate | A string describing the test command.|
|comparison | String || Only three types are valid here. A description can be found in [Possible currentdate comparisons](#possible-currentdate-comparisons).|
|datepart | String || A string containing the string "date", "weekday" or "time" (available with 7.6.1) as we only allow fix date, time and weekday comparisions for now.|
|datevalue | Array || A value array containing the corresponding value to the datepart. For "date" and "time" this will be an array of "Date" (unix timestamp). For "weekday", it will be an array of integers ranging from 0 to 6, reflecting the equivalent weekday, starting from Sunday through Saturday, i.e. 0 - Sunday, ..., 6 - Saturday. The test will be true if any of the values matches|

### header-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | header |	A string describing the test command.|
|comparison | String || Available types can be found in the config object. (see [Possible comparisons](#possible-comparisons)).|
|headers | Array || A string array containing the header fields.|
|values | Array || A string array containing the values for the header fields. The test will be true if any of the strings matches.|

### allof-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | allof | A string describing the test command.|
|tests | Array || A array of tests.|

### anyof-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | anyof | A string describing the test command.|
|tests | Array || A array of tests.|

### body-test

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | body | A string describing the test command.|
|comparison | String || Available types can be found in the config object. (see [Possible comparisons](#possible-comparisons)).|
|extensionskey | String || The extension key can be one of the value found in [Possible extensions](#possible-extensions).|
|extensionsvalue | String || A value for the given key. If the key has no value (see [Possible extensions](#possible-extensions) for this information) the value given here is ignored.|
|values | Array || A string array containing the values for the body. The test will be true if any of the strings matches.|

## Possible action commands

<div class="mailFilterToc">

| Name | Description |
|:------|:-------------|
| [keep](#keep-command) | Keeps a mail non-changed. |
| [discard](#discard-command) | Discards a mail without any processing. |
| [redirect](#redirect-command) | Redirects a mail to a given e-mail address. |
| [move](#move-command) | Moves a mail into a given subfolder (the syntax of the subfolder given here must be the correct syntax of the underlying IMAP-server and is up to the GUI to detect things such as altnamespace or unixhierarchysep). |
| [reject](#reject-command) | Rejects the mail with a given text. |
| [stop](#stop-command) | Stops any further progressing of a mail. |
| [vacation](#vacation-command) | Creates a vacation mail. |
| [addflags](#addflags-command) | Adds flags to a mail. |
| [notify](#notify-command) | Adds a notification. |
| [pgp](#pgp-command) | Encrypts a mail via pgp. |

</div>

## Structure of action commands

This section describes the structures of action commands.

### keep-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | keep | A string defining the object itself. |

### discard-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | discard | A string defining the object itself.|

### redirect-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | redirect | A string defining the object itself.|
|to | String ||	A string containing where the mail should be redirected to.|

### move-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | move | A string defining the object itself.|
|into |	String || This string takes the object id of the destination mail folder as specified in the HTTP API of the groupware.|

### reject-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | reject |	A string defining the object itself.|
|text |	String || A string containing the reason why the mail is rejected.|

### stop-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | stop | A string defining the object itself.|

### vacation-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | vacation | A string defining the object itself.|
|days | Integer || The days for which a vacation text is returned.|
|addresses | Array || The addresses for which this vacation is responsible. That means for which addresses out of the aliases array of the user defining this filter, vacations will be sent.|
|from | String or Array || Support for the ':from' tag. Specifies the value of the from header for the auto-reply mail, e.g. Foo Bear <foo.bear@ox.io> (Since 7.8.1). The array of strings should be a simple JSONArray with length 2; the first element should include the personal part of the e-mail address and the second element the actual e-mail address. If only the e-mail address is available, that should be the only element of the array.|
|subject | String || The new subject for the returned message (can be left empty, when only adding RE:).|
|text | String || The vacation text itself.|

### addflags-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | addflags | A string defining the object itself.|
|flags | Array || An array containing the flags which should be added to that mail. A flag can be either a system flag or a user flag. System flags begin with a backslash (\) and can be one of the ones describes in [Flags](#flags). \
\
System flags are case-insensitive. \
\
User flags begin with a dollar sign ($) and can contain any ASCII characters between 0x21 (!) and 0x7E (~), inclusive, except for the characters 0x22, 0x25, 0x28, 0x29, 0x2A, 0x5C, 0x5D and 0x7B, which correspond to \
\
"%()*\]{ \
\
Mail color flags as used by OX are implemented by user flags of the form $cl_n, where n is a number between 1 and 10, includive.\
\
See [RFC 3501](http://tools.ietf.org/html/rfc3501) for further details on IMAP flags and their meanings.|


#### Flags

|Name|
|:---|
|\\seen|
|\\answered|
|\\flagged|
|\\deleted|
|\\draft|
|\\recent|

### notify-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | notify | A string defining the object itself.|
|message | String || the content of the notification message. |
|method | String || the method of the notification message, e.g. "mailto:012345678@sms.gateway". |

### pgp-command

|Name |Type|Value|Description|
|:----|:---|:----|:----------|
|id | String | pgp | A string defining the object itself.|
|keys | Array || The public keys as string which should be used for encryption.|


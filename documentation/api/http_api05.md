---
title: Mail filter
classes: no-affix
---
# Possible tests
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
# Possible comparisons
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| is | If a field is equal to a given value. | contains | If a field contains a given value at any position. |
| matches | Tests if the value matches the value in the specified field ("*" matches zero or more characters, "?" matches a single character, to use these characters themselves they have to be escaped via backslash). | regex | Tests if a given regular expression matches with the value present in the specified field. |
| user | Tests if the user part of an e-mail address is the value given here. This means in herbert+mustermann@example.com. The user checks the part herbert (only possible in conjunction with the `address` test). | detail | Tests if the detail part of an e-mail address is the value given here. In the example above this evaluates to mustermann (only possible in conjunction with the `address` test). |
## Possible currentdate comparisons
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| is | Used in the date test to check for a value equal to the given one. | ge | Used in the date test to check for a value greater or equal to the given one. |
| le | Used in the date test to check for a value less or equal to the given one. |   |   |
## Possible size comparisons
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| over | Used in the size test to check for a value greater than the given one. | under | Used in the size test to check for a value less than the given one. |
# Possible extensions
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| content | An extension used in conjunction with the body test to define the content which should be considered. This extension will need a parameter specifying the mime-type of the part of the message which will be searched. | text | An extension used in conjunction with the body test to define that only the text of the body should be considered in this test. This extension takes no parameter. |
# Possible action commands
| Name | Description | Name | Description |
|------|-------------|------|-------------|
| keep | Keeps a mail non-changed. | discard | Discards a mail without any processing. |
| redirect | Redirects a mail to a given e-mail address. | move | Moves a mail into a given subfolder (the syntax of the subfolder given here must be the correct syntax of the underlying IMAP-server and is up to the GUI to detect things such as altnamespace or unixhierarchysep). |
| reject | Rejects the mail with a given text. | stop | Stops any further progressing of a mail. |
| vacation | Creates a vacation mail. | addflags | Adds flags to a mail. |
| notify | Adds a notification. | pgp | Encrypts a mail via pgp. |


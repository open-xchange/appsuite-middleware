---
title: senduserfeedback
icon: far fa-circle
tags: Administration, Command Line tools, User, Feedback
package: open-xchange-userfeedback
---

# NAME

senduserfeedback - Send user feedback

# SYNOPSIS

**senduserfeedback -U myUser:myPassword [OPTIONS]**

# DESCRIPTION

This command line tool sends the user feedback via mail.
 
# OPTIONS


**--api-root** *arg*
: URL to an alternative HTTP API endpoint. Example: 'https://192.168.0.1:8443/userfeedback/v1/'

 **-b**, **--body** *arg*
: The mail body (plain text).

**-c**, **--compress**
: Use to gzip-compress exported feedback.

**-e**, **--end-time** *arg*
: End time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given before this time is considered. If not set, all feedback since -s is considered.

**-g**, **--context-group** *arg*
: The context group identifying the global DB where the feedback is stored. Default: 'default'.

**-h**, **--help**
: Prints the help text

**-r**, **--recipients** *arg*
: Single Recipient's mail address like "Displayname <email@example.com>" or the local path to a CSV file containing all the recipients, starting with an '@' (@/tmp/file.csv). Where the address is followed by the display name, seperated by a comma.

**-s**, **--start-time** *arg*
: Start time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given after this time is considered. If not set, all feedback up to -e is considered.

**-S**, **--subject** *arg*
: The mail subject. Default: "User Feedback Report: [time range]".
 
**-t**, **--type** *arg*
: The feedback type. Default: 'star-rating-v1'. Alternative value: 'nps-v1'.

**-U**, **--api-user** *user:password*
: Username and password to use for REST API authentication (user:password).


# EXAMPLES

**senduserfeedback -U myUser:myPassword -s 1487348317 -r "Displayname <email@example.com>"**


# SEE ALSO

[exportuserfeedback(1)](exportuserfeedback), [deleteuserfeedback(1)](deleteuserfeedback)
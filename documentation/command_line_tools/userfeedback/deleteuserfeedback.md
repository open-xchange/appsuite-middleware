---
title: deleteuserfeedback
icon: far fa-circle
tags: Administration, Command Line tools, User, Feedback
package: open-xchange-userfeedback
---

# NAME

deleteuserfeedback - Delete user feedback

# SYNOPSIS

**deleteuserfeedback -U myUser:myPassword [-t type] [-g ctx_grp] [-s time] [-e time]**

# DESCRIPTION

This command line tool deletes the user feedback.
 
# OPTIONS


**--api-root** *arg*
: URL to an alternative HTTP API endpoint. Example: 'https://192.168.0.1:8443/userfeedback/v1/'

**-e**, **--end-time** *arg*
: End time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given before this time is considered. If not set, all feedback since -s is considered.

**-g**, **--context-group** *arg*
: The context group identifying the global DB where the feedback is stored. Default: 'default'.

**-h**, **--help**
: Prints the help text

**-s**, **--start-time** *arg*
: Start time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given after this time is considered. If not set, all feedback up to -e is considered.

**-t**, **--type** *arg*
: The feedback type. Default: 'star-rating-v1'. Alternative value: 'nps-v1'.

**-U**, **--api-user** *user:password*
: Username and password to use for REST API authentication (user:password).


# EXAMPLES

**deleteuserfeedback -s 1487348317**


# SEE ALSO

[exportuserfeedback(1)](exportuserfeedback), [senduserfeedback(1)](senduserfeedback)
---
title: logincounter
icon: far fa-circle
tags: Administration, Command Line tools, Monitoring
---

# NAME

logincounter - prints the amount of user logins.

# SYNOPSIS

**logincounter** [OPTION]...

# DESCRIPTION

This command line tool prints the amount of user logins.

Whenever a user is logged in through the login servlet, the context id, user id, client identification string and a time stamp is saved to the database. If a database entry for this combination of context, user and client is already present, only the timestamp is updated. This means that the database holds the information for the last login of a specific user with a specific client. This data can be retrieved again with the logincounter tool to show how many users logged in through which client(s) in a given timeframe.

Please note that only logins are counted. Depending on the server configuration and the client, sessions may be reused when a user re-accesses the system even after a few hours or days. In this case the login timestamp is not updated. This means that logincounter does not necessarily show the number of active users, especially if the request only covers a short timeframe.

The clients provided by Open-Xchange use the following client identification strings:

| Client                                        | client identification string      |
|-----------------------------------------------|-----------------------------------|
| Open-Xchange AppSuite (Web UI)                | open-xchange-appsuite             |
| Open-Xchange Server 6 (Web UI)                | com.openexchange.ox.gui.dhtml     |
| Connector for Business Mobility (Active Sync) | USM-EAS                           |
| Connector 2 for Microsoft Outlook 	        | USM-JSON                          |
| OX Notifier 	                                | OpenXchange.HTTPClient.OXNotifier |
| CardDAV                                       | CARDDAV                           |
| CalDAV 	                                    | CALDAV                            |
| Mobile Web App (legacy, OX6 generation)       | com.openexchange.mobileapp        |
| OX Drive (native iOS client)                  | OpenXchange.iosClient.OXDrive     |
| OX Drive (native Android client)              | OpenXchange.Android.OXDrive       |
| OX Drive (native Mac OS X client)             | OSX.OXDrive                       |
| OX Drive (native Windows client)              | OpenXchange.HTTPClient.OXDrive    |

A custom login page (see [Open-Xchange_servlet_for_external_login_masks](http://oxpedia.org/wiki/index.php?title=Open-Xchange_servlet_for_external_login_masks) may (and should) set a custom client identification string.

The output of logincounter can be filtered by client identification strings with the -r or --regex parameter followed by a regular expression matching the desired string(s).

# OPTIONS
 
**-a**, **--aggregate**
: Optional. Aggregates the counts by users. Only the total number of logins without duplicate counts (caused by multiple clients per user) is returned.

**-t**, **--start** *startDate*
: Required. Sets the start date for the detecting range. Example: 2009-12-31T00:00:00

**-e**, **--end** *endDate*
: Required. Sets the end date for the detecting range. Example: 2010-01-1T23:59:59

**-r**, **--regex** *regex*
: Optional. Limits the counter to login devices that match regex.

**-H**, **--host** *jmxHost*
: The optional JMX host (default:localhost)

**-l**, **--login** *jmxLogin*
: The optional JMX login (if JMX authentication is enabled)

**-p**, **--port** *jmxPort*
: The optional JMX port (default:9999)

**-s**, **--password** *jmxPassword*
: The optional JMX password (if JMX authentication is enabled)

**-h**, **--help**
: Prints a help text

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite)

# EXAMPLES

**logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59"**

Shows all logins in the month of June 2014 by clients

**logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -a**

Shows all logins in the month of June 2014, but remove duplicate logins from the "Total" count. (This means a user who logged in with two different clients is only counted once in the total count.)

**logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r ".\*OXDrive"**

Restricts output to logins through any OX Drive client.

**logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r ".\*OXDrive" -a**

Same as above, but remove duplicates in the total count (i.e. users using OX Drive on two platforms are only counted once).

**logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r "open-xchange-appsuite"**

Shows all logins through the App Suite Web UI.

**logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r "(open-xchange-appsuite|com.openexchange.ox.gui.dhtml)"**

Shows all logins through either the App Suite or the Open-Xchange Server 6 Web UI.

# SEE ALSO

[logconf(1)](logconf), [includestacktrace(1)](includestacktrace), [lastlogintimestamp(1)](lastlogintimestamp)
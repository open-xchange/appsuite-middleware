---
title: LoginCounter
---

Whenever a user is logged in through the login servlet, the context id, user id, client identification string and a time stamp is saved to the database. If a database entry for this combination of context, user and client is already present, only the timestamp is updated. This means that the database holds the information for the last login of a specific user with a specific client. This data can be retrieved again with the logincounter tool to show how many users logged in through which client(s) in a given timeframe.

Please note that only logins are counted. Depending on the server configuration and the client, sessions may be reused when a user re-accesses the system even after a few hours or days. In this case the login timestamp is not updated. This means that logincounter does not necessarily show the number of active users, especially if the request only covers a short timeframe.

The clients provided by Open-Xchange use the following client identification strings:

Client                                        | client identification string
----------------------------------------------|---------------------------
Open-Xchange AppSuite (Web UI)	               | `open-xchange-appsuite`
Open-Xchange Server 6 (Web UI)	               | `com.openexchange.ox.gui.dhtml`
Connector for Business Mobility (Active Sync) | `USM-EAS`
Connector 2 for Microsoft Outlook             | `USM-JSON`
OX Notifier                                   | `OpenXchange.HTTPClient.OXNotifier`
CardDAV                                       | `CARDDAV`
CalDAV                                        | `CALDAV`
Mobile Web App (legacy, OX6 generation)       | `com.openexchange.mobileapp`
OX Drive (native iOS client)                  | `OpenXchange.iosClient.OXDrive`
OX Drive (native Android client)              | `OpenXchange.Android.OXDrive`
OX Drive (native Mac OS X client)             | `OSX.OXDrive`
OX Drive (native Windows client)              | `OpenXchange.HTTPClient.OXDrive`

A custom login page (see (Open-Xchange_servlet_for_external_login_masks)[http://oxpedia.org/wiki/index.php?title=Open-Xchange_servlet_for_external_login_masks]) may (and should) set a custom client identification string.

The output of logincounter can be filtered by client identification strings with the `-r` or `--regex` parameter followed by a regular expression matching the desired string(s).

## Using the logincounter tool

```bash
 # logincounter --help
 usage: logincounter
  -a,--aggregate     Optional. Aggregates the counts by users. Only the
                     total number of logins without duplicate counts
                     (caused by multiple clients per user) is returned.
  -e,--end <arg>     Required. Sets the end date for the detecting range.
                     Example: 2010-01-1 23:59:59
  -h,--help          Prints a help text
  -r,--regex <arg>   Optional. Limits the counter to login devices that
                     match regex.
  -s,--start <arg>   Required. Sets the start date for the detecting range.
                     Example: 2009-12-31 00:00:00
```

## Examples

Show all logins in the month of June 2014 by clients:

```bash
$ /opt/open-xchange/sbin/logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59"
```
Show all logins in the month of June 2014, but remove duplicate logins from the "Total" count. (This means a user who logged in with two different clients is only counted once in the total count.)

```bash
$ /opt/open-xchange/sbin/logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -a
```
Restrict output to logins through any OX Drive client:

```bash
$ /opt/open-xchange/sbin/logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r ".*OXDrive"
```
Same as above, but remove duplicates in the total count (i.e. users using OX Drive on two platforms are only counted once):

```bash
$ /opt/open-xchange/sbin/logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r ".*OXDrive" -a
```
Show all logins through the App Suite Web UI:

```bash
$ logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r "open-xchange-appsuite"
```
Show all logins through either the App Suite or the Open-Xchange Server 6 Web UI:

```bash
$ logincounter -s "2014-06-01 00:00:00" -e "2014-06-30 23:59:59" -r "(open-xchange-appsuite|com.openexchange.ox.gui.dhtml)"
```
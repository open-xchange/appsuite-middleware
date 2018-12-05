---
title: lastlogintimestamp
---

# Outputs the time stamp of the last login for a user using a certain client

Prints the time stamp of the last login for a user using a certain client.

Basically, a client identifier is a freely choosable string that a client is allowed to pass on login. Therefore a listing of known client identifers:

 - ``open-xchange-appsuite``<br>The client identifier for App Suite UI
 - ``com.openexchange.ox.gui.dhtml``<br>The client identifier for OX6 UI
 - ``open-xchange-mailapp``<br>The client identifier for Mail App
 - ``USM-EAS``<br>The client identifier for USM/EAS
 - ``USM-JSON``<br>The client identifier for USM/JSON (Outlook Connector)
 - ``CARDDAV``<br>The client identifier for CardDAV
 - ``CALDAV``<br>The client identifier for CalDAV

## Parameters

 - ``-c,--context <arg>``<br>
 A valid (numeric) context identifier
 - ``-h,--help``<br>
 Prints a help text
 - ``-u,--user <arg>``<br>
 A valid (numeric) user identifier
 - ``-i <arg>``<br>
 A valid (numeric) user identifier as alternative for the ``-u, --user`` option
 - ``-d,--datepattern <arg>``<br>
 The optional date pattern used for formatting retrieved time stamp; e.g ``EEE, d MMM yyyy HH:mm:ss Z`` would yield ``Wed, 4 Jul 2001 12:08:56 -0700``
 - ``-s,--server <arg>``<br>
 The optional RMI host (default:localhost)
 - ``-A,--adminuser <arg>``<br>
 The master admin username
 - ``-P,--adminpass <arg>``<br>
 The master admin password
 - ``-p,--port <arg>``<br>
 The optional RMI port (default:1099)
 - ``--responsetimeout <arg>``<br>
 The optional response timeout in seconds when reading data from server (default: 0s; infinite)
 - ``-t,--client <arg>``<br>
 A client identifier; e.g ``"com.openexchange.ox.gui.dhtml"``
 - ``--list-clients ``<br>
 Outputs a table of known client identifiers

### Examples

```
./lastlogintimestamp -c 1 -u 6 -A oxadminmaster -P secret -t open-xchange-appsuite

Thu, 5 Jan 2017 09:28:03 CET
```
Outputs the last login time stamp of user 6 in context 1 for client ``open-xchange-appsuite`` in default date pattern

```
./lastlogintimestamp -A oxadminmaster -P secret -c 1 -u 6 -t open-xchange-appsuite -d "\"yyyy.MM.dd G 'at' HH:mm:ss z\""
```
Outputs the last login time stamp of user 6 in context 1 for client ``open-xchange-appsuite`` with a custom date pattern


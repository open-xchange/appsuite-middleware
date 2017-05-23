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
 - ``-H,--host <arg>``<br>
 The optional JMX host (default:localhost)
 - ``-l,--login <arg>``<br>
 The optional JMX login (if JMX has authentication enabled)
 - ``-s,--password <arg>``<br>
 The optional JMX password (if JMX has authentication enabled)
 - ``-p,--port <arg>``<br>
 The optional JMX port (default:9999)
 - ``--responsetimeout <arg>``<br>
 The optional response timeout in seconds when reading data from server (default: 0s; infinite)
 - ``-t,--client <arg>``<br>
 A client identifier; e.g ``"com.openexchange.ox.gui.dhtml"``

### Examples

```
./lastlogintimestamp -c 1 -u 6 -l jmxadmin -s iprotectyourjmx -t open-xchange-appsuite

Thu, 5 Jan 2017 09:28:03 CET
```
Outputs the last login time stamp of user 6 in context 1 for client ``open-xchange-appsuite`` in default date pattern

```
./lastlogintimestamp -c 1 -u 6 -t open-xchange-appsuite -d "\"yyyy.MM.dd G 'at' HH:mm:ss z\""
```
Outputs the last login time stamp of user 6 in context 1 for client ``open-xchange-appsuite`` with a custom date pattern


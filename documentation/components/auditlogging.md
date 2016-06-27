---
title: Audit Logging
---

# How it works

Starting with v7.8.2 the Open-Xchange Server provides a special SLF4J logger named ``com.openexchange.log.audit.AuditLogService`` that currently tracks the following events

 - Login to Open-Xchange Server
 - Logout from Open-Xchange Server
 - Connect to internal/external IMAP
 - Connect to internal/external SMTP
 - Connect to internal/external POP3

That special SLF4J logger can be operated in two different modes:

1. Standard mode
2. Dedicated mode

## Standard mode

In standard mode the logger uses the regular Open-Xchange Server logback configuration. Then the logger outputs its log messages in the same way and to the same localtion as configured for Open-Xchange Server through ``logback.xml`` configuration file.

## Dedicated mode

In dedicated mode the logger uses its own file logging. Then the logger writes its messages to dedicated rotating files using its own layout pattern.

# Installation

This feature is included in ``open-xchange-core`` package. Thus, no additional packages are required being installed.

# Configuration

An administrator is able to configure this feature through `/opt/open-xchange/etc/slf4j-auditlog.properties` file. The options are reloadable at any time.

## Enabling

The feature is enabled via ``com.openexchange.log.audit.slf4j.enabled`` property, which defaults to ``false``. If set to ``true``the property ``com.openexchange.log.audit.slf4j.level`` defines what log level to choose when outputting log messages (default is ``info``).

## Message layout

This section describes all properties that influence how a log message looks like

 - ``com.openexchange.log.audit.slf4j.delimiter`` specifies the delimiter string to use between individual attributes in one log message; e.g. ``com.openexchange.log.audit.slf4j.delimiter="|"`` yields:

  ```
   ox.login|login=thorben|ip=::1|timestamp=2016-06-10T10:31:10Z
  ```

 - ``com.openexchange.log.audit.slf4j.includeAttributeNames`` defines whether attribute names are supposed to be included in log message or not; e.g. ``com.openexchange.log.audit.slf4j.includeAttributeNames=false``yields:

  ```
   ox.login|thorben|::1|2016-06-10T10:31:10Z
  ```

 - ``com.openexchange.log.audit.slf4j.date.pattern`` allows to specify in what format a date/time-stamp is logged. By default ISO-8601 formatting is used. The administrator is able to specify any date pattern according to [Date and Time Patterns](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html). Accompanying options ``com.openexchange.log.audit.slf4j.date.locale`` and ``com.openexchange.log.audit.slf4j.date.timezone`` allow to also set utilized locale and time zone for date formatting.

## Message destination

The special property ``com.openexchange.log.audit.slf4j.file.location`` defines whether the logger uses its own file logging and layout or if standard log configuration is used.

If no value is specified for that property standard logging is performed.

### Dedicate log files

If set to a non-empty file location/pattern, dedicated log files are created. Example ``com.openexchange.log.audit.slf4j.file.location=/var/log/open-xchange/slf4j-auditlog.log`` yields log files:

 - /var/log/open-xchange/slf4j-auditlog.log
 - /var/log/open-xchange/slf4j-auditlog.log.1
 - /var/log/open-xchange/slf4j-auditlog.log.2
 - ...
 - /var/log/open-xchange/slf4j-auditlog.log.N

These properties defines how many files are created, what its max. size is and what layout pattern is used for a log line:

 - ``com.openexchange.log.audit.slf4j.file.size`` specifies the max. file size in bytes (default is ``2097152``). If exeeced, the files are rotated.
 - ``com.openexchange.log.audit.slf4j.file.count`` sets how many files are created when rotating files (default is ``99``).
 - ``com.openexchange.log.audit.slf4j.file.pattern`` specifies the layout for a log line. Default is ``"%sanitisedMessage%n"``, which simply outputs the log message (sanitised from all ESC sequences) and a line-break. However, the layout string may contain extended information according to [logback pattern layout](http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout)
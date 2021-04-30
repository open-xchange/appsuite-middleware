---
title: Socket Monitoring
icon: fa-eye
tags: Monitoring, Administration, Configuration, Logging
---

<!-- Once we upgrade to FontAwesome 5.x use the 'fa-network-wired' icon -->

This article describes how monitoring for socket connection can be enabled and configured. A socket connection is any kind of TCP connection the Open-Xchange Middleware opens to an end-point; be it a back-end service like IMAP, MySQL, etc. or certain remote HTTP end-points (e.g. OAuth).

What type of socket connections is supposed to be monitored is configurable.

# General Properties

The ``com.openexchange.monitoring.sockets.enabled`` property enables/disables the socket monitoring system. On the monitoring system two different monitors can be attached: one for tracing sockets and one for logging the socket traffic. 


# Tracing Monitoring
The ``com.openexchange.monitoring.sockets.tracing.enabled`` property enables/disables the tracing monitor for sockets. If enabled, the Open-Xchange Middleware integrates its own client socket implementation factory into the Java stack. That factory wraps each opened socket with a monitor that delegates to the concrete socket and gives certain call-backs to registered socket monitoring listeners.

Such a listener the Open-Xchange Middleware ships with is a tracing listener, which is configurable, too. That listener allows to

 - Record a certain amount of samples (traced write-read durations) per socket, which allows to perform certain queries via a JMX Mbean
 - Enriches common logging with log properties that reveal how much (accumulated) time the current thread spent in socket wait and in what status the last read attempt is in (OK, TIMED_OUT, ...)
 - Enable a dedicated file logger that writes socket related log messages

# Enriched Logging

If ``com.openexchange.monitoring.sockets.tracing.enabled`` is set to ``true``, any logged message is enriched with the following properties per socket connection:

 - ``com.openexchange.monitoring.sockets.{ID}.accumulatedWaitMillis``
 - ``com.openexchange.monitoring.sockets.{ID}.host``
 - ``com.openexchange.monitoring.sockets.{ID}.port``
 - ``com.openexchange.monitoring.sockets.{ID}.status``

{ID} is replaced with socket's internal unique ID (to differentiate between two sockets to the same end-point).


``host`` and ``port`` are self-explanatory. ``accumulatedWaitMillis`` is the accumulated amount of time in milliseconds the current thread spent waiting in total for reads from a certain socket. ``status`` signals in what status the last read attemt terminated (``OK``, ``TIMED_OUT``, ``CONNECT_ERROR``, ``READ_ERROR``, etc.)

Example for a long-running IMAP operation (copying many mails from a GMail account to primary one):

```
2017-06-09T15:30:50,235+0200 INFO  [OXTimer-0000025] com.openexchange.http.requestwatcher.internal.RequestWatcherServiceImpl$Watcher.handleEntry(RequestWatcherServiceImpl$Watcher.java:265)
Request with age 172,163ms (2m 52s 164ms) exceeds max. age of 5,000ms (5s). Request's properties:
  __threadId=193
  ...
  com.openexchange.mail.accountId=0
  com.openexchange.mail.fullName=default0/INBOX/a008
  com.openexchange.mail.host=mail.devel.open-xchange.com:143
  com.openexchange.mail.login=thorben
  com.openexchange.mail.session=a3065807aa10433cbc3f75425ee0c546-17-1337-2WvxFw94Qsk4
  com.openexchange.monitoring.sockets.605310613.accumulatedWaitMillis=144725
  com.openexchange.monitoring.sockets.605310613.host=imap.gmail.com
  com.openexchange.monitoring.sockets.605310613.port=993
  com.openexchange.monitoring.sockets.605310613.status=OK
  com.openexchange.monitoring.sockets.1028946037.accumulatedWaitMillis=21214
  com.openexchange.monitoring.sockets.1028946037.host=mail.devel.open-xchange.com
  com.openexchange.monitoring.sockets.1028946037.port=143
  com.openexchange.monitoring.sockets.1028946037.status=OK
  com.openexchange.request.trackingId=1599856545-799928348
  ...
com.openexchange.http.requestwatcher.osgi.services.RequestTrace: tracked request
	at java.net.SocketInputStream.socketRead0(Native Method)
	at java.net.SocketInputStream.read(SocketInputStream.java:152)
```

# Sample Recording

The tracing socket listener is able to record a certain amount of samples for a socket communication to generate some sort of history allowing to collect certain statisitcs from it.

The ``com.openexchange.monitoring.sockets.tracing.numberOfSamplesPerSocket`` property (default is ``1000``) specifies how many samples are allowed to be tracked per socket in a LIFO (stack) manner. Older samples, which exceed that limitation, are dropped from the stack. A value of less than/equal to ``0`` (zero) effectively disables sample recording.

Moreover, the ``com.openexchange.monitoring.sockets.tracing.keepIdleThreshold`` option (default is ``300000``) defines the time in milliseconds when an idle sample collection gets removed in the background.

To narrow down the recorded samples to relevant ones, the sample recording offers the following properties:

 - ``com.openexchange.monitoring.sockets.tracing.thresholdMillis`` (default is ``100``) specifies the threshold in milliseconds that is required to be exceeded to let a sample be added to recorded sample collection. Otherwise the sample is discarded. This allows to only collect those samples that exceed a quite long-running wait for a socket read.
 - ``com.openexchange.monitoring.sockets.tracing.filter.hostnames`` allows to define a filter based on host names, IP addresses or IP address ranges. That property allows to exclude socket samples for outer end-points and to only consider internal ones. I.e ``192.168.0.1-192.168.255.255, 10.20.0.1/255, *.mydomain.org, special-service.internal.org``
 - ``com.openexchange.monitoring.sockets.tracing.filter.ports`` allows to defines a filter based on port numbers. That property allows to only consider socket samples for certain serves (e.g. ``143, 993`` for only IMAP).

## MBean/JMX Access

If sample recording is enabled (property ``com.openexchange.monitoring.sockets.tracing.numberOfSamplesPerSocket`` set to a value greater than ``0``) an MBean is registered, which is accessible via built-in JMX monitoring console.

That MBean offers to query the sample history:

 - Retrieve the average duration for request/response round-trips on socket connections matching an optional host/port filter.
 - Retrieve the average timeouts for request/response round-trips on socket connections matching an optional host/port filter.
 - List the average durations for request/response round-trips on socket connections grouped by hosts/addresses

# Dedicated File Logging

Additionally, the tracing and traffic socket listeners allows to enable a dedicated file logging to output socket-related messages through ``com.openexchange.monitoring.sockets.tracing.logging.enabled`` and ``com.openexchange.monitoring.sockets.traffic.logging.enabled`` properties respectively.

 - ``com.openexchange.monitoring.sockets.tracing.logging.level`` specifies the log level for that dedicated logging allowing the values ``error``, ``warn`` and ``info``
  - ``error`` only logs socket messages dealing about any kind of I/O error that occurred
  - ``warn`` also includes waits for socket read that exceed the ``com.openexchange.monitoring.sockets.tracing.thresholdMillis`` value
  - ``info`` also includes any wait for a socket read
 - ``com.openexchange.monitoring.sockets.tracing.logging.fileLocation`` defines the file location (e.g. ``/var/log/open-xchange/sockets/socket.log``)
 - ``com.openexchange.monitoring.sockets.tracing.logging.fileLimit`` defines the max. size for file
 - ``com.openexchange.monitoring.sockets.tracing.logging.fileCount`` defines how many individual log files are allowed to be created

 The socket traffic logger has a set of similar properties in the ``com.openexchange.monitoring.sockets.traffic.logging.*`` domain. Simply replace all ``tracing`` with ``traffic`` sub-parts in the property names to enable the similar properties for the socket traffic logger. For the socket traffic logger, the extra property `com.openexchange.monitoring.sockets.traffic.file.logging.enabled` needs to be set to `true` as well to enable traffic logging on a separate file.

# Socket Logging

Since 7.10.3, the OX middleware is offering to administrators the ability to log all incoming and outgoing socket traffic for debugging purposes. The socket logging is based on bundle names, meaning that it can be activated for a specific bundle, e.g. for `com.openexchange.oauth`. To enable socket logging, simply set the property ``com.openexchange.monitoring.sockets.traffic.logging.enabled`` to ``true``. To divert the traffic logging to a separate file use the property  `com.openexchange.monitoring.sockets.traffic.file.logging.fileLocation`.

To register or unregister a bundle for/from socket logging during operation, use the `socketLogging` command line tool. For example:

```bash
$ /opt/open-xchange/sbin/socketLogging -A oxadminmaster -P secret -r -n com.openexchange.rss
```

Registers the specified bundle to the socket logging registry.

```bash
$ /opt/open-xchange/sbin/socketLogging -A oxadminmaster -P secret -u -n com.openexchange.rss
```

Unregisters the specified bundle to the socket logging registry.

```bash
$ /opt/open-xchange/sbin/socketLogging -A oxadminmaster -P secret -lr
```

Lists all registered bundles for which socket logging is enabled.

Note that you will have to either add the bundle name to the `logback.xml`  (as shown above), or via the `logback` command line tool. Furthermore, the `socketLogging` command line tool can be combined with `logback` to apply the logging only for a specific context or user. Also note that enabling socket logging for the entire bundle it won't work. This is a limitation we introduced to prevent excessive I/O and expensive stacktrace analysis. This means that a socket traffic logging filter has to be enabled for a user or a user's session.

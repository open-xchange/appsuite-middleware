---
title: Logging
icon: fa-clipboard-list
tags: Logging, Configuration, Installation
---

With OX App Suite 7.4.2 the groupware server introduces [LOGBack](http://logback.qos.ch/) as logging implementation. Formerly [log4j 1.2](http://logging.apache.org/log4j/1.2) or `java.util.logging` have been used for logging. As of now the package `open-xchange-log4j` has become obsolete and LOGBack is entirely contained in `open-xchange-core`. No additional packages are needed even for syslog-based logging or sophisticated logging configurations anymore. Log4j and `java.util.logging` have been completely replaced by LOGBack.

# For Operators

## Configuration
LOGBacks configuration is defined in `/opt/open-xchange/etc/logback.xml`. We ship the file pre-configured for new installations and set some useful defaults. Without any changes, the groupware writes log files to `/var/log/open-xchange`. See chapter [Backward Compatibility](#backward-compatibility) for detailed information on product upgrades from older versions. Refer to the [LOGBack manual](http://logback.qos.ch/manual/index.html) for details on configuring LOGBack.

## Backward Compatibility

### Configuration Migration
In most cases no further work should be necessary after upgrading to OX App Suite 7.4.2. Based on your existing logging configuration (`/opt/open-xchange/etc/file-logging.properties` or `/opt/open-xchange/etc/log4j.xml`) we automatically migrate custom configured logger levels via package post installation scripts to the new logback configuration.

We also configure file-based logging or syslog-based logging in accordance with the default configuration of the formerly used logging implementation. That means depending whether open-xchange-log4j was installed or not, the package post installation scripts modify the default logback configuration to send logs to a remote syslog daemon. You don't have to change anything if

 * you use plain file logging and did not modify the 'handler'-section in file-logging.properties.
 * you use syslog-based logging via log4j and did not change the configured 'SERVER_LOG' appender.

Please note: If you customized handlers or appenders you have to port your changes manually to /opt/open-xchange/etc/logback.xml. Refer to the [LOGBack manual](http://logback.qos.ch/manual/index.html) manual fors details. 

### Log File Format

We keep the former default layout for log events on product upgrades. So nothing should break if you wrote your own log file parser.

_Please note_: We also ship a pre-configured new log file layout. You are strongly encouraged to use this one for file-based logging, as it provides more information and is better structured than the old one. To enable it, change the appender reference of appender `ASYNC` to `FILE` instead of `FILE_COMPAT` in `/opt/open-xchange/etc/logback.xml`.

Before: 


```xml
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
  ...
  <appender-ref ref="FILE_COMPAT" />
</appender>
```

After:

```xml
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
  ...
  <appender-ref ref="FILE" />
</appender>
```

## Configuration Changes from Version 7.10.4+
Since version 7.10.4 there is a slight change in the property naming of the `logback.xml` file. The changes are as follows:

* The base package of all the extensions is further refined into sub-packages each containing a specific extension. There are four in total:
  * `appenders`: Contains all available supported appenders
	 * Logstash (since 7.6.2)
     * Kafka (since 7.10.4)
  * `converters`: Contains all available supported converters
     * `ExtendedReplacingCompositeConverter`
     * `LineMDCConverter`
     * `LogSanitisingConverter`
     * `ThreadIdConverter`
  * `encoders`: Contains all available supported encoders
     * `ExtendedPatternLayoutEncoder`
     * `JSONEncoder`
  * `policies`: Contains all available supported policies
     * `FixedWindowRollingPolicy`
* The property and class names of the four previously mentioned extensions has changed to reflect the package name. In particular, the fully qualified property and class name now contains the package name, AND in case of an appender, the appender name as well. For example, the FixedWindowRollingPolicy, previously configured as `<rollingPolicy class="com.openexchange.logback.extensions.FixedWindowRollingPolicy">`, will now be configured as `<rollingPolicy class="com.openexchange.logback.extensions.policies.FixedWindowRollingPolicy">`. For an extended list on how the names were changed and how to update your configuration files, please refer to the next section.

## Upgrading from an older version

### Appenders

The appender's property names were renamed to reflect the package name and are now available with the following format: com.openexchange.logback.extensions.<APPENDER_NAME>.<PROPERTY_NAME>. The APPENDER_NAME  reflects the appender name  property and the PROPERTY_NAME  reflects the actual property name. Both supported appenders, Kafka and Logstash, have the following common properties:

 * `encoder`: Defines the encoder used to encode log entries before writing them to the specified appender. Defaults to `JSONEncoder`.
 * `queueSize`: The internal queue size of the appender. Defaults to 2048.
 * `alwaysPersistEvents`: Whether the log events that are in the queue and cannot be sent to the remote server, will be persisted to the `open-xchange-console.log` file as JSON formatted events.
 * `loadFactor`: The load factor of the internal queue before flushing it. Defaults to 0.67.
 * `connectionTimeout`: The connection timeout in milliseconds. Defaults to 5000.

Depending on which appender is configured, the appropriate fully qualified name should be used. For example, to configure the queueSize  for the `logstash` appender, the property `com.openexchange.logback.extensions.appenders.logstash.queueSize` should be used, and for the `kafka` appender the property `com.openexchange.logback.extensions.appenders.kafka.queueSize`. The same applies for all common properties.

### Custom Fields
The `actionClass` for the custom fields is renamed from `com.openexchange.logback.extensions.logstash.CustomFieldsAction` to `com.openexchange.logback.extensions.encoders.CustomFieldsAction`.

###Converters
The `converters`' class names are renamed to `com.openexchange.logback.extensions.converters.<CONVERTER_NAME>`, e.g. for the `LogSanitisingConverter` the new name will be `com.openexchange.logback.extensions.converters.LogSanitisingConverter`.

###Encoders
The `encoders`' class names are renamed to `com.openexchange.logback.extensions.encoders.<ENCODER_NAME>`, e.g. for the `JSONEncoder` the new name will be `om.openexchange.logback.extensions.encoders.JSONEncoder`.

###Policies
The `policies`' class names are renamed to `com.openexchange.logback.extensions.policies.<POLICY_NAME>`, e.g. for `FixedWindowRollingPolicy` the new name will be `com.openexchange.logback.extensions.policies.FixedWindowRollingPolicy`.


# New Features

## Config-based Asynchronous Logging
Publishing log events to files, syslogd or the like is performed asynchronously. Therefore the root loggers appender is set to `ASYNC`, which is configured as follows:

```xml
<configuration>
...
  <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>2048</queueSize>
    <discardingThreshold>0</discardingThreshold>
    <includeCallerData>true</includeCallerData>
    <appender-ref ref="FILE" />
  </appender>
...
</configuration>
```

The most important property is `appender-ref`. It denotes the name of the appender, that finally writes log events out in some way. A detailed description of the other parameters can be found [here](http://logback.qos.ch/manual/appenders.html#AsyncAppender).

If you introduce your own log appender, only change the above mentioned `appender-ref` property. Don't remove or deactivate the configuration for asynchronous logging! It might decrease the applications performance significantly.

## Suppression Of Stacktraces

It is possible to suppress Java stacktraces in log messages for configured categories. Those categories are part of OX App Suites internal error and exception handling. Suppression can be configured in server.properties by adjusting the value for `com.openexchange.log.suppressedCategories`. See below excerpt of `/opt/open-xchange/etc/server.properties`: 

```properties
...
# Specify the OXException categories (comma separated) to be suppressed when logging.
# The Exception itself will still be logged as configured, but the StackTraces are omitted.
# Valid categories are ERROR, TRY_AGAIN, USER_INPUT, PERMISSION_DENIED, CONFIGURATION, CONNECTIVITY, SERVICE_DOWN, TRUNCATED, CONFLICT, CAPACITY, WARNING
# Default is USER_INPUT.
com.openexchange.log.suppressedCategories=USER_INPUT
...
```

## Tracing Of Sessions, Users and Contexts

It is possible to enable extended logging for defined scopes like sessions, users or contexts at a whole. This means that every log event which belongs to a configured scope will be logged despite of it's level, if it's logger belongs to a configured whitelist. The whitelist is configured in `logback.xml`:

```xml
<property scope="context" name="com.openexchange.logging.filter.loggerWhitelist" value="com.openexchange" />
```

The whitelists purpose is to avoid flooding your log files with unwanted tracing events from e.g. 3rd party libraries. The "value"-attribute can be adjusted. It may contain a comma-separated list of loggers. It's hierarchical. That means `com.openexchange` also allows all child-loggers.

Example: Imagine logback is configured to log on level `INFO` and additionally the logger `com.openexchange.example.SomeLogger` is only allowed to log warnings and errors. The whitelist is configured as shown above.

```xml
<configuration>
...
  <property scope="context" name="com.openexchange.logging.filter.loggerWhitelist" value="com.openexchange" />
...
  <root level="INFO">
    <appender-ref ref="ASYNC" />
  </root>
  <logger name="com.openexchange.example.SomeLogger" level="WARN"/>
...
</configuration>
```

If tracing is activated for user '5' in context '1', events belonging to this user are written out, even if they deceed the configured thresholds. I.e. you can find log entries with level `DEBUG` from logger `com.openexchange.example.SomeLogger` that belong to the traced user.

```
2013-12-09 15:29:31,641 DEBUG [OXWorker-0000001] com.openexchange.example.SomeLogger
Some very useful debug message.
 com.openexchange.session.sessionId=711c651fa4c94dc0894c733c92bc77d4
 com.openexchange.session.contextId=1
 com.openexchange.session.userId=5
 com.openexchange.session.userName=oxuser
 ...
```

The decision whether to log such an event is based on the whitelist and the context information that every log event carries along. These information is structured as key-value-pairs and always printed below the log events message. Afterwards it's easy to find the tracing events in the log file. You just have to search for `com.openexchange.session.contextId=1` and `com.openexchange.session.userId=5`. If you trace a specific session, according events can be found with looking for `com.openexchange.session.sessionId=session_id_to_trace`. For further dynamic filtering and configuration you can refer to the [command line tool](/components/middleware/command_line_tools/logging_and_monitoring/logconf.html).

## New Log Event Layout

We ship a new default layout for log events that

 * contains a more precise timestamp
 * carries the name of the current thread
 * is easier to parse

Example:

```
2013-12-11 12:41:35,660 INFO  [LoginPerformer-0000003] com.openexchange.caching.internal.JCSCache
Cache 'UserPermissionBits' is operating in distributed mode
 com.openexchange.ajax.action=login
 com.openexchange.ajax.requestNumber=5
 ...
```

Please note: The new layout applys only for fresh installations. On upgrades you have to enable it manually. See [Log File Format](#new-log-event-layout) for details.

## Custom Fields Support

With version 7.6.3 as well as with version 7.8.2, we introduced a new functionality to the `LOGSTASH` appender, that of the custom fields. The appender can now be configured to log arbitrary custom fields with every log entry in Logstash. Note that since 7.10.4, the custom fields are supported in both Logstash and Kafka appenders. For the new naming see the section [Upgrading from an older version](#upgrading-from-an-older-version).

The only thing that needs to be done to get this working is to add a few lines to the `logback.xml` First, a newRuleXML element has to be registered under the configuration tag, in order to activate the CustomFieldsAction:

*From 7.6.3 & 7.8.2-7.10.3*

```xml
<newRule pattern="*/encoder/customField" actionClass="com.openexchange.logback.extensions.logstash.CustomFieldsAction"/>
```

*From 7.10.4*

```xml
<newRule pattern="*/encoder/customField" actionClass="com.openexchange.logback.extensions.encoders.CustomFieldsAction"/>
```

...and that's pretty much it! Now, you can define your own customFields inside of the <encoder> tag of any of the encoders, e.g. for versions prior to 7.10.4:

```xml
  <encoder class="com.openexchange.logback.extensions.logstash.LogstashEncoder">
    <customField name="foo" value="bar"/>
    <customfield name="thx" value="1137"/>
  </encoder>
```

And for versions starting from 7.10.4:

```xml
  <encoder class="com.openexchange.logback.extensions.encoders.JSONEncoder">
    <customField name="foo" value="bar"/>
    <customfield name="thx" value="1137"/>
  </encoder>
```

## Remote Logging

### Logstash

In 7.6.2 we introduced a new feature that allows the OX server to log directly to a [logstash](http://logstash.net/%7C) server.

### Versions prior to 7.10.4
This information is valid from 7.6.2 up until 7.10.3.

To enable that feature you will need (besides the logstash server) to add/enable the OX logstash appender in the logback.xml file:

```xml
<property scope="context" name="com.openexchange.logback.extensions.logstash.enabled" value="true" />
<property scope="context" name="com.openexchange.logback.extensions.logstash.alwaysPersistEvents" value="true" />
<property scope="context" name="com.openexchange.logback.extensions.logstash.loadFactor" value="0.67" />
<appender name="LOGSTASH" class="com.openexchange.logback.extensions.logstash.LogstashSocketAppender">
  <remoteHost>localhost</remoteHost>
  <port>31337</port>
  <reconnectionDelay>10000</reconnectionDelay>
  <eventDelayLimit>30000</eventDelayLimit>
  <encoder class="com.openexchange.logback.extensions.logstash.LogstashEncoder"/>
  <queueSize>2048</queueSize>
</appender>
```

The three properties defined at the beginning of the XML snippet have the following use:

 * `com.openexchange.logback.extensions.logstash.enabled` - This flag is used to enable the JMX metrics for the event queue size in the appender
 * `com.openexchange.logback.extensions.logstash.alwaysPersistEvents` - If set to true, then log events that are in queue and can not be send to the logstash server (for what ever reason) are persisted to open-xchange-console.log as JSON formated logstash events.
 * `com.openexchange.logback.extensions.logstash.loadFactor` - The load factor of the queue before flushing it.

The logstash appender has to then be referenced to the root logger:

```xml
<root level="INFO">
  <appender-ref ref="LOGSTASH" />
</root>
```

### Versions since 7.10.4
Since 7.10.4 there was a [slight change](#configuration-changes-from-version-7-10-4) in the property naming of `logback.xml`. Hence, the upgraded logstash configuration will look as follows:

```xml
<property scope="context" name="com.openexchange.logback.extensions.appenders.logstash.enabled" value="true" />
<property scope="context" name="com.openexchange.logback.extensions.appenders.logstash.alwaysPersistEvents" value="true" />
<property scope="context" name="com.openexchange.logback.extensions.appenders.logstash.loadFactor" value="0.67" />
<appender name="LOGSTASH" class="com.openexchange.logback.extensions.appenders.logstash.LogstashSocketAppender">
  <remoteHost>localhost</remoteHost>
  <port>31337</port>
  <reconnectionDelay>10000</reconnectionDelay>
  <eventDelayLimit>30000</eventDelayLimit>
  <encoder class="com.openexchange.logback.extensions.encoders.JSONEncoder"/>
  <queueSize>2048</queueSize>
</appender>
```

The logstash configuration stays with versions prior to 7.10.4.

### Kafka
Since 7.10.4, there is a new appender introduced, namely "Kafka" which can directly write log entries to a [Kafka](https://kafka.apache.org/) topic in a Kafka cluster. The appender's class name is `com.openexchange.logback.extensions.appenders.KafkaAppender`  and can be configured as follows:

```xml
<appender name="KAFKA" class="com.openexchange.logback.extensions.appenders.kafka.KafkaAppender">
    <servers>kafka-node-01:9092,kafka-node-02:9092,kafka-node-03:9092</servers>
    <topic>logs</topic>
    <key>node03</key>
</appender>
```

The three properties defined at the beginning of the XML snippet have the following use:
 * `servers`: Defines the Kafka servers, i.e. brokers. Mandatory.
 * `topic`: The configured Kafka topic that will receive the logging events. Mandatory.
 * `key`: Defines a custom partitioning key. Optional. If omitted then the [`HOSTNAME`](http://logback.qos.ch/manual/configuration.html#hostname) property will be used.

#### Kafka Configuration

You should already have a Kafka cluster running and a topic already created. The Kafka configuration is out of the scope of this guide. Please refer to this [article](https://kafka.apache.org/quickstart) for that.

# MDC properties explanation

 * `__threadId=1618`: The thread identifier
 * `com.openexchange.ajax.action=autologin`: The AJAX action
 * `com.openexchange.ajax.module=login`: The AJAX module for the AJAX action
 * `com.openexchange.grizzly.queryString=action=autologin&client=open-xchange-appsuite&rampup=true&rampupFor=open-xchange-appsuite&version=7.6.2-22`: The complete query string that is contained in the request URL after the path
 * `com.openexchange.grizzly.method`: The HTTP method
 * `com.openexchange.grizzly.remoteAddress=1.2.3.4`: The client's IP address (depends on the configuration described here)
 * `com.openexchange.grizzly.remotePort=65127`: The client's port
 * `com.openexchange.grizzly.requestURI=/ajax/login`: The request URI without any parameters
 * `com.openexchange.grizzly.serverName=10.0.0.1`: The host name of the server to which the request was sent. It is the value of the part before ":" in the 'Host' header value, if any, or the resolved server name, or the server IP address (depends on the configuration described here)
 * `com.openexchange.grizzly.servletPath=/ajax/login`: The part of this request's URL that calls the servlet. This path starts with a "/" character and includes either the servlet name or a path to the servlet, but does not include any extra path information or a query string.
 * `com.openexchange.grizzly.session=35387551837281237793.koxg4l7scaj0qgxpmh1d`: The sticky session identifier from Apache to the OX backend
 * `com.openexchange.grizzly.threadName=OXWorker-0001337`: The middleware's thread name
 * `com.openexchange.grizzly.userAgent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0`: The content of the 'User-Agent' header.
 * `com.openexchange.request.trackingId=983767a5c98646a988247b0ef83d6bb3`: The tracking identifier of the request
 * `com.openexchange.session.authId=342dfecaf0f5425f9b70b13115835e9a`: The authentication identifier of the session
 * `com.openexchange.session.clientId=open-xchange-appsuite`: The client identifier
 * `com.openexchange.session.contextId=27`: The context identifier
 * `com.openexchange.session.loginName=oxdude`: The login name
 * `com.openexchange.session.sessionId=faf5223338ea4371aacbbb9745a934a1`: The session identifier
 * `com.openexchange.session.userId=2`: The user identifier
 * `com.openexchange.session.userName=oxdude`: The user name
 * `com.openexchange.database.schema=oxdatabase_7`: The database schema
 * `com.openexchange.mail.accountId`: The mail account identifier used in the request
 * `com.openexchange.mail.host`: The mail host used in the requst
 * `com.openexchange.mail.login`: The login identifier used to login to the mail server

# For Developers

## Instantiate Loggers

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clazz {

    private static final Logger LOG = LoggerFactory.getLogger(Clazz.class);

    ...

}
```

Using Apache Commons Logging as logging facade is depracted as of OX App Suite 7.4.2. Formerly we used own wrapper classes to enable extended logging features. With the current logging implementation this is not necessary anymore. Obtaining loggers via

`com.openexchange.log.Log.loggerFor(Clazz.class);` or
`com.openexchange.log.LogFactory.getLog(Clazz.class);` or
`com.openexchange.log.Log.valueOf(LogFactory.getLog(Clazz.class));`

still works for backward compatibility, but should not be used for new classes. Existing classes should be ported to SLF4J, when touched during refactorings, bugfixes etc.

Of course it is also required to fix the corresponding bundles `META-INF/MANIFEST.MF` file.

Remove

```
com.openexchange.log
org.apache.commons.logging
```

Add

`org.slf4j`

# Logging Statements

With SLF4J you should avoid code like this:

```java
if (LOG.isDebugEnabled()) {
    LOG.debug("Some debug message for user " + userId + " in context " + contextId + ".");
}
```

Instead relinquish the if-statement completely and make use of SLF4Js printf()-like syntax for log statements:

```java
LOG.debug("Some debug message for user {} in context {}.", userId, contextId);
```

Please read [What is the fastest way of (not) logging?](http://www.slf4j.org/faq.html#logging_performance) for details. 

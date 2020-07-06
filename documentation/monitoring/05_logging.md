---
title: Logging
icon: fa-stream
---

The pipeline of how events are getting logged in logback is documented [here](http://logback.qos.ch/manual/architecture.html), but in a nutshell the event is forwarded via the logger to an appender which, as the name implies, appends the event to a logging target (file, console, whatever). The appender has a specific formatter attached to it which is responsible for the formatting of the event prior appending.

Logback comes with a variaty of predefined appenders, even appenders that transmit events over the wire to a remote logging server. Oh cool! So we use that one and we are done! Well, not quite. The appenders that ship with logback are transmitting the logging events over wire, yes, but they serialize the objects before doing so. In order for logging servers (such as Logstash or even Kafka) to be able to parse those events, a deserialization has to take place before being imported. Logback has the `SocketAppender` and `ServerSocketAppender` duo for exactly this purpose. Alas, this does not solve our problem, because we need an extra component running in front of the ELK stack, which defeats the purpose of removing unnecessary steps and layers from the logging pipeline.

# Logging to Logstash

With logstash we have many different inputs, outputs, codecs and filters at our disposal which we can choose from and combine with each other in order to achieve bridging OX and logstash.

Since logstash already supports JSON objects as logging events, it would only be natural to convert all logging events to JSON objects in OX side and send them over to logstash. So, the idea would be to use the tcp input plugin in conjunction with the json codec, to import the logging events to logstash.

The TCP protocol is very resilient and sufficient for transmitting log events but there is a slight “obstacle”; the scheduling and dispatching of packets is only influenced by the operating system, meaning that the probability of two or more events resulting into being transmitted in one TCP packet, or split into more than one TCP packets is quite high. This would lead Logstash into being unable to parse them correctly. Fortunately, logstash's codec repertoire is quite rich and sure enough we can very easily overcome the “obstacle” with the line codec, which basicaly handles every line as a single event. Perfect!

So, let's have a quick look at the logback extension (i.e. the LOgXstash appender) that handles the logging event/json transformation. The class LogstashSocketAppender derives from the AppenderBase class (part of the logback framework) and is a replica of the AbstractSocketAppender except from the method that handles the way the events are proccessed prior dispatching. For that purpose a JSONEncoder (deriving from EncoderBase) and a JSONormatter are hooked with the LogstashSocketAppender to handle just that, i.e. encode/transform the ILoggingEvent objects into JSON objects.

The logging fields that are taken into consideration are:

    @timestamp
    @version
    level
    loggerName
    threadName
    message
    marker
    stacktrace
    MDC properties

The @ prefix indicates logstash fields that are going to be replaced by the ones from the event.

# Logging to Kafka
For Kafka, the scenario is simple: publish JSON serialised messages to a topic and that's it.

# AppSuite Versions 7.4.2+

With 7.4.2 we have switched to [SLF4J](http://www.slf4j.org/) as logging facade and [LOGBack](http://logback.qos.ch/) as logging backend. More information can be found in the official knowledge-base [article](http://oxpedia.org/wiki/index.php?title=AppSuite:OX_Logging).

# AppSuite Versions 7.10.5+

As of version 7.10.5 of the AppSuite (and version 2.0.0 of the Open-Xchange Java Commons libraries) there is a slight change in the property naming of the `logback.xml` file. The changes are as follows:

* The base package of all the extensions is further refined into sub-packages each containing a specific extension. There are four in total:
  * `appenders`: Contains all available supported appenders
     * Logstash (since 7.6.2)
     * Kafka (since 7.10.5)
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
* The property and class names of the four previously mentioned extensions has changed to reflect the package name. In particular, the fully qualified property and class name now contains the package name, AND in case of an appender, the appender name as well. For example, the `FixedWindowRollingPolicy`, previously configured as `<rollingPolicy class="com.openexchange.logback.extensions.FixedWindowRollingPolicy">` , will now be configured as `<rollingPolicy class="com.openexchange.logback.extensions.policies.FixedWindowRollingPolicy">`. For an extended list on how the names were changed and how to update your configuration files, please refer to the next section.

# Upgrading from an older version

## Appenders

The appender's property names were renamed to reflect the package name and are now available with the following format: `com.openexchange.logback.extensions.<APPENDER_NAME>.<PROPERTY_NAME>`. The `APPENDER_NAME`  reflects the appender name  property and the `PROPERTY_NAME` reflects the actual property name. Both supported appenders, Kafka and Logstash, have the following common properties:

* `encoder`: Defines the encoder used to encode log entries before writing them to the specified appender. Defaults to `JSONEncoder`.
* `queueSize`: The internal queue size of the appender. Defaults to 2048.
* `alwaysPersistEvents`: Whether the log events that are in the queue and cannot be sent to the remote server, will be persisted to the `open-xchange-console.log` file as JSON formatted events.
* `loadFactor`: The load factor of the internal queue before flushing it. Defaults to 0.67.
* `connectionTimeout`: The connection timeout in milliseconds. Defaults to 5000.

Depending on which `appender` is configured, the appropriate fully qualified name should be used. For example, to configure the `queueSize` for the `logstash` appender, the property `com.openexchange.logback.extensions.appenders.logstash.queueSize`  should be used, and for the `kafka`  appender the property `com.openexchange.logback.extensions.appenders.kafka.queueSize`. The same applies for all common properties.

### Logstash Appender
Since 7.6.2, middleware can write log entries directly to a Logstash server (see [Logging with ELK Stack](https://confluence.open-xchange.com/display/MID/Logging+with+ELK+Stack)). The logstash appender's class name is now `com.openexchange.logback.extensions.appenders.logstash.LogstashSocketAppender`, and can be configured as follows:

```xml
<appender name="LOGSTASH" class="com.openexchange.logback.extensions.appenders.logstash.LogstashSocketAppender">
   ...
</appender>
```

The logstash  appender has two individual mandatory properties that can be configured, namely:
* `server`: Defines the logstash server name or IP address
* `port`: Defines the port that the logstash server is listening

Both properties are mandatory and the appender won't start if either one is missing.

Some other optional properties for this appender are:

* `keepAlive`: Whether the socket connection to the logstash server shall be kept alive at all times. Under the hood, enables the [SO_KEEPALIVE](https://docs.oracle.com/javase/8/docs/api/java/net/StandardSocketOptions.html#SO_KEEPALIVE) flag of the `Socket`. Defaults to `false`.
* `acceptConnectionTimeout`: Defines the `SO_KEEPALIVE` timeout of the Socket in milliseconds. Defaults to 10000.
* `eventDelayLimit`: Defines the [duration](http://logback.qos.ch/apidocs/ch/qos/logback/core/util/Duration.html) that the the appender is allowed to block if the underlying internal queue is full. Once this limit is reached, the event is dropped. Defaults to 0.
* `reconnectionDelay`: Defines the [duration](http://logback.qos.ch/apidocs/ch/qos/logback/core/util/Duration.html) the appender will wait between each failed connection attempt to the server. The default value of this option is to 30 seconds .

## Custom Fields

The actionClass for the custom fields is renamed from `com.openexchange.logback.extensions.logstash.CustomFieldsAction` to `com.openexchange.logback.extensions.encoders.CustomFieldAction` .

## Converters

The converters' class names are renamed to `com.openexchange.logback.extensions.converters.<CONVERTER_NAME>` , e.g. for the `LogSanitisingConverter` the new name will be `com.openexchange.logback.extensions.converters.LogSanitisingConverter`.

## Encoders

The encoders' class names are renamed to `com.openexchange.logback.extensions.encoders.<ENCODER_NAME>` , e.g. for the `JSONEncoder`  the new name will be `com.openexchange.logback.extensions.encoders.JSONEncoder`.

## Policies

The policies ' class names are renamed to `com.openexchange.logback.extensions.policies.<POLICY_NAME>`, e.g. for `FixedWindowRollingPolicy` the new name will be `com.openexchange.logback.extensions.policies.FixedWindowRollingPolicy`.

# Configuration

## Logstash Appender
The only thing that needs to be done is to add/enable the logstash appender in the logback.xml (or logback-test.xml) file in the backend and reference that appender to the root logger. Please refer to the previous section for any changes regarding the property naming.

```xml
<property scope="context" name="com.openexchange.logback.extensions.logstash.enabled" value="true" />
<property scope="context" name="com.openexchange.logback.extensions.logstash.alwaysPersistEvents" value="true" />
<property scope="context" name="com.openexchange.logback.extensions.logstash.loadFactor" value="0.67" />
<property scope="context" name="com.openexchange.logback.extensions.logstash.acceptConnectionTimeout" value="10000" />
<property scope="context" name="com.openexchange.logback.extensions.logstash.keepAlive" value="true" />
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

```xml
<root level="INFO">
       <appender-ref ref="LOGSTASH" />
</root>
```

#### Logstash Server Configuration

Of course the logstash server should also be appropriately configured. The input configuration file in logstash.d/10-input-logx.conf should look like this:

```ruby
# For LogX
tcp {
  port => 31337
  type => "LogX"
  codec => json_lines
}
```
You should already have a Kafka cluster running and a topic already created. The Kafka configuration is out of the scope of this guide. Please refer to this article for that.
And the filter in `logstash.d/40-filter-logx.conf`:
```ruby
if [type] == "LogX" {
  date {
      match => ["timestamp", "ISO8601"]
      timezone => "Europe/Berlin"
      remove_field => ["timestamp"]
  }
}
```
## Kafka Appender
Since 7.10.5, there is a new appender introduced, namely "Kafka" which can directly write log entries to a Kafka topic in a Kafka cluster. The appender's class name is `com.openexchange.logback.extensions.appenders.KafkaAppender`  and can be configured as follows:

```xml
<appender name="KAFKA" class="com.openexchange.logback.extensions.appenders.kafka.KafkaAppender">
    <servers>kafka-node01:9092,kafka-node02:9092</servers>
    <topic>logs</topic>
    <key>node03</key>
</appender>
```
### Kafka Configuration
You should already have a Kafka cluster running and a topic already created. The Kafka configuration is out of the scope of this guide. Please refer to this [article](https://kafka.apache.org/quickstart) for that.

# Custom Fields Support

With version 7.6.3 as well as with version 7.8.2, we introduced a new functionality to the LOGSTASH appender, that of the custom fields. The appender can now be configured to log arbitrary custom fields with every log entry in Logstash. Note that since 7.10.4, the custom fields are supported in both Logstash and Kafka appenders. For the new naming refer to this [article](https://confluence.open-xchange.com/display/MID/Logging+with+Logback).

The only thing that needs to be done to get this working is to add a few lines to the logback.xml. First, a newRuleXML element has to be registered under the configuration tag, in order to activate the CustomFieldsAction:

```xml
<newRule pattern="*/encoder/customField" actionClass="com.openexchange.logback.extensions.logstash.CustomFieldsAction"/>
```
...and that's pretty much it! Now, you can define your own customFields inside of the <encoder> tag of the appender, e.g.:

```xml
<encoder class="com.openexchange.logback.extensions.encoders.JSONEncoder">
  <customField name="foo" value="bar"/>
  <customfield name="thx" value="1137"/>
</encoder>
```

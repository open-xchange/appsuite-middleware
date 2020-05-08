---
title: Micrometer
icon: fa-fire-alt
tags: Monitoring, Administration
---

# Introduction

With 7.10.4 of the OX AppSuite the way metrics are gathered changes. The current implementation that uses the `MetricService` is now deprecated and instead [Micrometer](https://micrometer.io/) with its [Prometheus](https://prometheus.io/) implementation is used.
All metrics are exposed via the `/metrics` rest endpoint.  

# How to migrate metrics from MetricService to Micrometer

Developers can still use the `MetricService`, but it is going to be removed in a future release. Therefore it is advised to migrate existing metrics and its tooling to the new Micrometer/Prometheus format.
The migration is very simple. Instead of using an OSGi service, one can define and register metrics via static methods, similar to SLF4J. A simple counter can be registered as follows:

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

Counter myCounter = Counter.builder("metric.group." + "metric.name")
                            .description("Some description")
                            .baseUnit("unit")
                            .tags("tagname1", "tagvalue1", "tagname2", "tagevalue2")
                            .register(Metrics.globalRegistry);
```

You just have to make sure that you follow the naming convention. The name of the meter should be in lower case and words must be separated by a dot (e.g. "cache.events.delivered").
In case dynamic tags are used you don't have to worry about checking the registry for an already existing meter, because if the meter is already registered, the register command returns the previously registered meter.
It is nevertheless recommended to create metrics statically wherever possible.

For most use cases Micrometer provides an adequate meter, but its framework doesn't support [Dropwizard](https://www.dropwizard.io/)'s meter type. This means if you want to measure something like throughput you will have to create two metrics for it: a timer and a counter.

# Configuration

All metrics the AppSuite registers (`appsuite.*`) can be individually enabled/disabled. The distribution statistics for metrics of type `Timer` and `DistributionSummnary` is also configurable. All metrics' properties share the same domain name, i.e. `com.openexchange.metrics.micrometer`. Note that all metrics' properties are reloadable.

## Enable/Disable

All metrics, which are enabled by default, can be disabled with the property `com.openexchange.metrics.micrometer.enable.all` set to `false`. The postfix `enable.all` is the wildcard for all registered metrics, including those of `jvm.*` and `process.*`, which micrometer registers by default. Those metrics will be enabled *only* if all metrics are enabled, i.e. with the `enable.all` property (omitting the shared domain name for now on for the sake of clarity), otherwise they will be disabled.

Each metric can be individually be enabled and disabled by appending to the `enable` property their unique name. The following example will clarify things a bit more.

```bash
com.openexchange.metrics.micrometer.enable.all=false
com.openexchange.metrics.micrometer.enable.appsuite.sessions=true
```

With this configuration, only the HTTP API metrics will be enabled and nothing else. If you now visit the `/metrics` servlet end-point, the output should look similar to the following:

```bash
# HELP appsuite_sessions_active_count The number of active sessions or in other words the number of sessions within the first two short term containers.
# TYPE appsuite_sessions_active_count gauge
appsuite_sessions_active_count{client="all",} 0.0
# HELP appsuite_sessions_short_term_count The number of sessions in the short term containers
# TYPE appsuite_sessions_short_term_count gauge
appsuite_sessions_short_term_count{client="all",} 0.0
# HELP appsuite_sessions_long_term_count The number of sessions in the long term containers
# TYPE appsuite_sessions_long_term_count gauge
appsuite_sessions_long_term_count{client="all",} 0.0
# HELP appsuite_sessions_max_count The maximum number of sessions for this node.
# TYPE appsuite_sessions_max_count gauge
appsuite_sessions_max_count{client="all",} 50000.0
# HELP appsuite_sessions_total_count The number of total sessions
# TYPE appsuite_sessions_total_count gauge
appsuite_sessions_total_count{client="all",} 0.0
```

## Distribution Statistics

The distribution statistics for metrics of type `Timer` and `DistributionSummnary` is also configurable. This entails configuration for enabling/disabling the percentiles histogram, the minimum and maximum values of the histogram, the publishing of concrete percentiles (e.g. 0.5, 0.75, etc.) and the publishing of SLA concrete values (e.g. 50ms, 100ms, etc.). 

### Enable/Disable Percentiles Histogram

By enabling/disabling the percentiles histogram with the property `distribution.histogram`, all/none (respectively) value buckets for that specific `DistributionSummary` metric are published. In a more concrete example:

```bash
com.openexchange.metrics.micrometer.enable.all=false
com.openexchange.metrics.micrometer.enable.appsuite.httpapi.requests=true
com.openexchange.metrics.micrometer.distribution.histogram.appsuite.http.requests=true
```

With this configuration, all but the `appsuite.httpapi.requests` metrics are published and for those metrics the percentiles histogram for all value buckets is published as well. It will produce an output as follows:

```bash
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.001",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.001048576",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.001398101",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.001747626",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.002097151",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.002446676",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.002796201",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.003145726",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.003495251",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.003844776",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.004194304",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.005592405",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.006990506",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.008388607",} 0.0
[... omitting full output for the sake of clarity ...]
appsuite_httpapi_requests_seconds_bucket{action="config",module="apps/manifests",status="OK",le="17.179869184",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="config",module="apps/manifests",status="OK",le="22.906492245",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="config",module="apps/manifests",status="OK",le="28.633115306",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="config",module="apps/manifests",status="OK",le="30.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="config",module="apps/manifests",status="OK",le="60.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="config",module="apps/manifests",status="OK",le="+Inf",} 0.0
```

As you can see, the percentiles histogram contains ALL recorded values.

### Concrete Percentiles Histogram

It is possible to narrow down and aggregate the value buckets in a more compact way. For this, the property `distribution.percentiles` comes into play. It accepts a comma-separated list of double values where each value should be between zero (0) and one (1) (both values exclusive).

```bash
com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests=false
com.openexchange.metrics.micrometer.distribution.percentiles.appsuite.http.requests=0.5,0.75,0.9,0.95,0.99,0.999
```

With this configuration, only the specified value buckets are published for that specific metric. The output that produced is as follows:

```bash
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds{action="autologin",module="login",status="TRY_AGAIN",quantile="0.5",} 0.0
appsuite_httpapi_requests_seconds{action="autologin",module="login",status="TRY_AGAIN",quantile="0.75",} 0.0
appsuite_httpapi_requests_seconds{action="autologin",module="login",status="TRY_AGAIN",quantile="0.9",} 0.0
appsuite_httpapi_requests_seconds{action="autologin",module="login",status="TRY_AGAIN",quantile="0.95",} 0.0
appsuite_httpapi_requests_seconds{action="autologin",module="login",status="TRY_AGAIN",quantile="0.99",} 0.0
appsuite_httpapi_requests_seconds{action="autologin",module="login",status="TRY_AGAIN",quantile="0.999",} 0.0
```

### SLA Values

Another useful configuration that can be applied to all `Timer` and `DistributionSummary` based metrics is that of the concrete SLA values. The property `distribution.sla` accepts values as a comma-separated list of timespan entries. For example:

```bash
com.openexchange.metrics.micrometer.distribution.sla.appsuite.httpapi.requests=50ms, 100ms, 150ms, 200ms, 250ms, 300ms, 400ms, 500ms, 750ms, 1s, 2s, 5s, 10s, 30s, 1m
```

With this configuration, only the values defined by the SLA are published, producing an output as follows:

```bash
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.05",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.1",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.15",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.2",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.25",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.3",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.4",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.5",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="0.75",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="1.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="2.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="5.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="10.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="30.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="60.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="autologin",module="login",status="TRY_AGAIN",le="+Inf",} 0.0
```

The accepted timespan abbreviations are:

  * `ms`: Milliseconds
  * `s`: Seconds
  * `m`: Minutes
  * `h`: Hours
  * `d`: Days
  * `w`: Weeks

### Minimum/Maximum Percentile Values

The minimum and maximum expected values can also be configured for all `Timer` and `DistributionSummary` based metrics. The properties `distribution.minimum` and `distribution.maximum` accept long values. The minimum and maximum values define a lower and upper bound of percentile histogram buckets respectively. See following example:

```bash
com.openexchange.metrics.micrometer.distribution.minimum.appsuite.httpapi.requests=10
com.openexchange.metrics.micrometer.distribution.maximum.appsuite.httpapi.requests=100
```

## Tag Filtering
It is also possible to further narrow down the published metrics by using a `filter` property (`com.openexchange.metrics.micrometer.filter`) with which a filter can be defined (either an exact match or a regular expression). For example:

```bash
com.openexchange.metrics.micrometer.filter.some_name=appsuite.httpapi.requests{action="all",module="folders"}
```

Note the `some_name` suffix of the property. This will be used as a pseudo name for the metric name and the filter to allow access to the previously defined properties, namely the enable/disable and distribution statistics. The following configuration should enable all `appsuite.httpapi.request` for the `all` action of the `folder` module and publish all SLAs for 50, 100 and 150 ms:

```bash
com.openexchange.metrics.micrometer.enable.some_name=true
com.openexchange.metrics.micrometer.distribution.histogram.some_name=false
com.openexchange.metrics.micrometer.distribution.sla.some_name=50ms,100ms,150ms
```

The `filter` property also supports an expression language similar to [this](https://prometheus.io/docs/prometheus/latest/querying/examples/).

All expressions starting with a tilde (`~`) after the equals (`=`) sign, are treated as regular expressions. All expressions that instead of an equals sign (`=`) have an exclamation mark (`!`) and are followed by the tilde (`~`), are treated as negated regular expressions. Observe the following example:

```bash
com.openexchange.metrics.micrometer.filter.read_connections=appsuite.mysql.connections.usage{type="write",class=~".*db",pool!~"-1|-2"}
```

It only publishes all `appsuite.mysql.connections.usage` metrics that are of type `write` their class ends with `db` and their pools are neither `-1` nor `-2`. The output is then similar to this:

```bash
# HELP appsuite_mysql_connections_usage_seconds_max The time between acquiration and returning a connection back to pool
# TYPE appsuite_mysql_connections_usage_seconds_max gauge
appsuite_mysql_connections_usage_seconds_max{class="userdb",pool="3",type="write",} 0.0
appsuite_mysql_connections_usage_seconds_max{class="userdb",pool="6",type="write",} 0.0
# HELP appsuite_mysql_connections_usage_seconds The time between acquiration and returning a connection back to pool
# TYPE appsuite_mysql_connections_usage_seconds summary
appsuite_mysql_connections_usage_seconds{class="userdb",pool="3",type="write",quantile="0.9",} 0.0
appsuite_mysql_connections_usage_seconds{class="userdb",pool="3",type="write",quantile="0.95",} 0.0
appsuite_mysql_connections_usage_seconds{class="userdb",pool="3",type="write",quantile="0.99",} 0.0
appsuite_mysql_connections_usage_seconds{class="userdb",pool="3",type="write",quantile="0.999",} 0.0
appsuite_mysql_connections_usage_seconds_count{class="userdb",pool="3",type="write",} 0.0
appsuite_mysql_connections_usage_seconds_sum{class="userdb",pool="3",type="write",} 0.0
appsuite_mysql_connections_usage_seconds{class="userdb",pool="6",type="write",quantile="0.9",} 0.0
appsuite_mysql_connections_usage_seconds{class="userdb",pool="6",type="write",quantile="0.95",} 0.0
appsuite_mysql_connections_usage_seconds{class="userdb",pool="6",type="write",quantile="0.99",} 0.0
appsuite_mysql_connections_usage_seconds{class="userdb",pool="6",type="write",quantile="0.999",} 0.0
appsuite_mysql_connections_usage_seconds_count{class="userdb",pool="6",type="write",} 0.0
appsuite_mysql_connections_usage_seconds_sum{class="userdb",pool="6",type="write",} 0.0
```

## Summary
To sum up, the following properties can be used to further configure the Micrometer metrics (The `com.openexchange.metrics.micrometer` prefix is always implied):

  * `enable.<METRIC_NAME>`: Enables/Disables that specific metric
  * `enable.all`: Enables/Disables all metrics.
  * `distribution.histogram.<METRIC_NAME>`: Enables/Disables the percentiles histogram of that specific metric.
  * `distribution.percentiles.<METRIC_NAME>`: Defines the percentiles to publish for that specific metric.
  * `distribution.sla.<METRIC_NAME>`: Defines the SLAs to publish for that specific metric.
  * `distribution.minimum.<METRIC_NAME>`: Defines the lower bound of percentile histogram buckets to publish for that specific metric.
  * `distribution.maximum.<METRIC_NAME>`: Defines the upper bound of percentile histogram buckets to publish for that specific metric.
  * `filter.<FILTER_NAME>`: Defines a filter which can further narrow down all metrics by tags. The `<FILTER_NAME>` is then used to access all `enable` and `distribution.*` properties (it replaces the `<METRIC_NAME>`).
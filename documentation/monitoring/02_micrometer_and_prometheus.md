---
title: Micrometer & Prometheus
icon: fa-fire-alt
tags: Monitoring, Administration
---

# Introduction

With OX App Suite 7.10.4 the way metrics are gathered changes. The current implementation is based on [Micrometer](https://micrometer.io/) with [Prometheus](https://prometheus.io/) being used as registry implementation and exposition format.

All metrics are exposed via HTTP under the `/metrics` endpoint. The endpoint can be secured with HTTP Basic Auth using the configuration properties `com.openexchange.metrics.micrometer.basicauth.login` and `com.openexchange.metrics.micrometer.basicauth.password`.


# Migration

While we tried to keep breaking changes as small as possible, the new approach affects most monitoring metrics that were formerly exposed as MBeans **within the** `com.openexchange.metrics` **namespace**. Most have been converted into Prometheus metrics only. The table below states the respective MBean attributes and replacements.

| MBean Name                                         | Package                   | New Name                | Additional info |
|:--------------------------------------------------|:--------------------------|:------------------------|:----------------------:|
| name=RequestTimes.<HTTP_METHOD_NAME\>,type=s3      | open-xchange-filestore-s3 | appsuite_filestore_s3_requests_seconds_*|  |
| name=S3UploadThroughput,type=s3                   | open-xchange-filestore-s3 |appsuite_filestore_s3_transferred_bytes_total{type="S3UploadThroughput",}| |
| name=S3DownloadThroughput,type=s3                 | open-xchange-filestore-s3 |appsuite_filestore_s3_transferred_bytes_total{type="S3DownloadThroughput",} | |
| name=Meter.totalIPChanges,type=ipcheck            | open-xchange-core | | No direct replacement. Sum up all appsuite_ipchanges_*_total counters. |
| name=Meter.acceptedIPChanges,type=ipcheck         | open-xchange-core | | No direct replacement. Sum up all appsuite_ipchanges_accepted_total counters. |
| name=Meter.deniedIPChanges,type=ipcheck           | open-xchange-core | | No direct replacement. Sum up all appsuite_ipchanges_denied_total counters. |
| name=Meter.acceptedPrivateIP,type=ipcheck         | open-xchange-core | appsuite_ipchanges_accepted_total{reason="PRIVATE_IPV4",status="*",} | |
| name=Meter.acceptedWhiteListed,type=ipcheck       | open-xchange-core | appsuite_ipchanges_accepted_total{reason="WHITE_LISTED",status="*",} | |
| name=Meter.acceptedEligibleIPChanges,type=ipcheck | open-xchange-core | appsuite_ipchanges_accepted_total{reason="ELIGIBLE",status="*",} | |
| name=Meter.deniedException,type=ipcheck           | open-xchange-core | appsuite_ipchanges_denied_total{reason="EXCEPTION",status="*",} | |
| name=Meter.deniedCountryChanged,type=ipcheck      | open-xchange-core | appsuite_ipchanges_denied_total{reason="COUNTRY_CHANGE",status="*",} | |
| client=all,name=ActiveCount,type=sessiond         | open-xchange-core | appsuite_sessions_active_total\{client="all",\} | |
| client=all,name=TotalCount,type=sessiond          | open-xchange-core | appsuite_sessions_total\{client="all",\} | |
| client=all,name=ShortTermCount,type=sessiond      | open-xchange-core | appsuite_sessions_short_term_total\{client="all",\} | |
| client=all,name=LongTermCount,type=sessiond       | open-xchange-core | appsuite_sessions_long_term_total\{client="all",\} | |
| name=<CacheRegionName\>.offeredEvents,type=cache   | open-xchange-core | appsuite_jcs_cache_events_offered_total\{region="<REGION\>",\} | |
| name=<CacheRegionName\>.deliveredEvents,type=cache | open-xchange-core | appsuite_jcs_cache_events_delivered_total\{region="<REGION\>",\} | |
| name=offeredEvents,type=cache                     | open-xchange-core |appsuite_jcs_cache_events_offered_total\{region="*"\} | No direct replacement. Sum up over all regions. |
| name=deliveredEvents,type=cache                   | open-xchange-core |appsuite_jcs_cache_events_delivered_total\{region="*"\} | No direct replacement. Sum up over all regions. |
| name=Cache Hit,type=antivirus                     | open-xchange-antivirus | appsuite_cache_gets_total{cache="antivirus",result="hit",} | |
| name=Cache Miss,type=antivirus                    | open-xchange-antivirus | appsuite_cache_gets_total{cache="antivirus",result="miss",} | |
| name=Cache Invalidations,type=antivirus           | open-xchange-antivirus | appsuite_cache_puts_total{cache="antivirus",} ; appsuite_cache_evictions_total{cache="antivirus",} | No direct replacement. |
| name=Scanning Rate,type=antivirus                 | open-xchange-antivirus | appsuite_antivirus_scans_duration_seconds_* | |
| name=Scanning Time,type=antivirus                 | open-xchange-antivirus | appsuite_antivirus_scans_duration_seconds_* | |
| name=Transfer Rate,type=antivirus                 | open-xchange-antivirus | appsuite_antivirus_transfer_bytes_total | |
| name=imap,type=requestRate,server=<HostName@Port\> | open-xchange-imap | appsuite_imap_commands_seconds_sum / appsuite_imap_commands_seconds_count | Merged with former errorRate using "status" tag. |
| name=imap,type=errorRate,server=<HostName@Port\> | open-xchange-imap | | Merged with former successRate using "status" tag. See above. |
| name=mailfilter,type=requestRate,server=<HostName@Port\> | open-xchange-mailfilter | appsuite_mailfilter_commands_seconds_sum / appsuite_mailfilter_commands_seconds_count | Merged with former errorRate using "status" tag. |
| name=mailfilter,type=errorRate,server=<HostName@Port\> | open-xchange-mailfilter | | Merged with former successRate using "status" tag. See above. |
| type=circuit-breakers,name=*,protocol=*,account=* | open-xchange-core | appsuite_circuitbreaker_* | CB state and config values are all available as gauges. |
| name=sproxyd,type=EndpointPool.TotalSize,filestore=<FilestoreId\> | open-xchange-filestore-sproxyd | appsuite_sproxyd_endpoints_total\{filestore="<FilestoreId\>",\} | |
| name=sproxyd,type=EndpointPool.Available,filestore=<FilestoreId\> | open-xchange-filestore-sproxyd | appsuite_sproxyd_endpoints_available\{filestore="<FilestoreId\>",\} | |
| name=sproxyd,type=EndpointPool.Unavailable,filestore=<FilestoreId\> | open-xchange-filestore-sproxyd | appsuite_sproxyd_endpoints_unavailable\{filestore="<FilestoreId\>",\} | |
| instance=<instance\>,status=<status\>,client=<client\>,name=RequestTimes,type=httpclient,method=<method> | open-xchange-core | appsuite_httpclient_* | |


# Important Metrics

Basically the output of the `/metrics` endpoint at runtime is the reference of available metrics according to the actual workloads served by that node. However, there are some important and noteworthy metrics that should be considered to be monitored in any case. These are briefly listed below.


## Health and Version Information

Some useful parts of the `/health` output (see also [Health Checks](./01_health_checks.html)) are mirrored to the Prometheus output for convenience:

```bash
# HELP appsuite_health_status Application health status
# TYPE appsuite_health_status gauge
appsuite_health_status{status="DOWN",} 0.0
appsuite_health_status{status="UP",} 1.0
# HELP appsuite_version_info App Suite version
# TYPE appsuite_version_info gauge
appsuite_version_info{build_date="2020-06-15",server_version="7.10.4-Rev1",} 1.0
```


## Process and JVM Metrics

Crucial process and Java Virtual Machine related metrics are exposed to monitor application uptime, memory (heap and non-heap), threads and garbage collection.


### Uptime and JVM Info

```bash
# HELP process_uptime_seconds The uptime of the Java virtual machine
# TYPE process_uptime_seconds gauge
process_uptime_seconds 102.111
# HELP process_start_time_seconds Start time of the process since unix epoch.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.593071293529E9
# HELP jvm_info JVM version info
# TYPE jvm_info gauge
jvm_info{runtime="OpenJDK Runtime Environment",vendor="Oracle Corporation",version="1.8.0_192-b12",} 1.0
```


### Memory and Threads

Memory metrics are distiguished by `committed`, `used` and `max` bytes, exposed per area (`heap` or `nonheap`) and memory region (e.g. `Metaspace`, `CMS Old Gen`, etc. Tag: `id`). Native memory buffers are also monitored and distinguished by `direct` and `mapped` buffers. For breviety the output example is shortened, grep for `jvm_memory` or `jvm_buffer` to get the full picture of a running JVM.

```bash
# HELP jvm_memory_committed_bytes The amount of memory in bytes that is committed for the Java virtual machine to use
# TYPE jvm_memory_committed_bytes gauge
jvm_memory_committed_bytes{area="heap",id="CMS Old Gen",} 2.01326592E8
# HELP jvm_buffer_memory_used_bytes An estimate of the memory that the Java virtual machine is using for this buffer pool
# TYPE jvm_buffer_memory_used_bytes gauge
jvm_buffer_memory_used_bytes{id="direct",} 1639545.0
jvm_buffer_memory_used_bytes{id="mapped",} 0.0
```

To monitor garbage collection activity, a summary per garbage collector is exposed:

```bash
# HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
# TYPE jvm_gc_collection_seconds summary
jvm_gc_collection_seconds_count{gc="ConcurrentMarkSweep",} 3.0
jvm_gc_collection_seconds_sum{gc="ConcurrentMarkSweep",} 0.068
```

Threads can be counted by state (runnable, blocked, waiting, etc.) but also aggregated counts are available:

```
# HELP jvm_threads_states_threads The current number of threads having NEW state
# TYPE jvm_threads_states_threads gauge
jvm_threads_states_threads{state="runnable",} 23.0
[...]
# HELP jvm_threads_peak_threads The peak live thread count since the Java virtual machine started or peak was reset
# TYPE jvm_threads_peak_threads gauge
jvm_threads_peak_threads 117.0
# HELP jvm_threads_live_threads The current number of live threads including both daemon and non-daemon threads
# TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads 104.0
```


## API Request Metrics

All App Suite APIs (HTTP API, WebDAV, SOAP, REST APIs) have been equipped with RED (Rate / Errors / Duration) metrics, so you can monitor request rates, response latencies (average and quantiles) and success-to-failure rates. You can find more on that approach in [The RED Method](https://grafana.com/files/grafanacon_eu_2018/Tom_Wilkie_GrafanaCon_EU_2018.pdf).

### HTTP API

Request times are recorded per module, action and response status, like so:

```bash
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.05",} 86.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.1",} 86.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.15",} 87.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.2",} 111.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.25",} 111.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.3",} 111.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.4",} 111.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.5",} 111.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="0.75",} 120.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="1.0",} 120.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="2.0",} 120.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="5.0",} 121.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="10.0",} 121.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="30.0",} 121.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="60.0",} 121.0
appsuite_httpapi_requests_seconds_bucket{action="all",module="user",status="OK",le="+Inf",} 121.0
appsuite_httpapi_requests_seconds_count{action="all",module="user",status="OK",} 121.0
appsuite_httpapi_requests_seconds_sum{action="all",module="user",status="OK",} 13.027
```

Per default all occurred permutations of the according tags are available at `/metrics`. The status tag has either value `OK` or - in case of an error - the exception category, e.g. `ERROR`, `USER_INPUT`, `TRY_AGAIN`, etc.


### WebDAV API

WebDAV requests for CalDAV, CardDAV and "Infostore" are recorded as follows:

```bash
# HELP appsuite_webdav_requests_seconds Records the timing of webdav requests
# TYPE appsuite_webdav_requests_seconds histogram
appsuite_webdav_requests_seconds_bucket{interface="CALDAV",method="DELETE",resource="caldav",status="OK",le="0.05",} 1.0
[...]
appsuite_webdav_requests_seconds_bucket{interface="CALDAV",method="DELETE",resource="caldav",status="OK",le="+Inf",} 2.0
appsuite_webdav_requests_seconds_count{interface="CALDAV",method="DELETE",resource="caldav",status="OK",} 2.0
appsuite_webdav_requests_seconds_sum{interface="CALDAV",method="DELETE",resource="caldav",status="OK",} 0.144
```


### SOAP API

See `appsuite_webdav_requests_seconds` histogram.


### REST APIs

See `appsuite_restapi_requests_seconds` histogram.


## Outbound Request Metrics

The RED approach has also been introduced to outbound connections, for


### S3 Filestores

See metrics starting with `appsuite_filestore_s3_`. Features:

* HTTP connection pool statistics
* Request/Response rate, latencies and errors


### MySQL / MariaDB

See metrics starting with `appsuite_mysql_`. Features:

* Connection pool monitoring (per DB pool).
* Connection usage rate and times
* Connection timeout errors


### IMAP / ManageSieve

See metrics starting with `appsuite_imap_` and `appsuite_mailfilter`. Features:

* Command rates, latencies and errors.


### Other HTTP connections

See metrics starting with `appsuite_httpclient_`. Features:

* Connection pool statistics
* Request/Response rate, latencies and errors


## JCS Cache Metrics

See metrics starting with `appsuite_jcs_cache_`. Features (each per cache region):

* Hit/miss rates
* Put operations
* Number of cached elements


## Thread Pool Metrics

See metrics starting with `appsuite_executor_`. Features:

* Pool statistics
* Number / rate of completed tasks
* Currently queued tasks


## Sessions

See metrics starting with `appsuite_sessions_`. Features:

* Number of sessions
** active (within the first two short-term containers)
** short-term
** long-term
** total
** max. (per node)


# Operator Guide

For all monitoring metrics, developers make conscious decisions whether they shall be gathered and exposed by default and also on which detail level exposition should occur. If gathering of more detailed metrics for a certain aspect of the application is possible, this is typically handled by feature-specific configuration. The exposition of monitoring details however can be controlled by a central piece of configuration. All according properties share the same namespace `com.openexchange.metrics.micrometer` and are generally reloadable. Certain more advanced configurations that go beyond enabling/disabling a metric might still need a service restart. We advise to change configuration during runtime, reload, observe and restart only if the output doesn't match the expected result yet.

All metrics can be individually enabled/disabled. By default a comprehensive set of metrics is exposed.

Timing metrics like latencies are configurable in terms of their exposed format (rate only vs. summary vs. histogram). For histograms it is possible to specify the concrete latency buckets or add certain ones to a generated default set of buckets (called `slo`s). For percentile summaries, the pre-calculated quantiles can be set or overridden using configuration.


## Configuration guidelines

Certain metrics within configuration are specified using their in-code names or a prefix of that. Naming follows the [Micrometer best practices](https://micrometer.io/docs/concepts#_naming_meters). Within the Prometheus output, meters are renamed programmatically to match the standards of Prometheus. It is usually possible to derive the configuration name of a certain metric from its according Prometheus output name. Some details on this can be found [here](https://micrometer.io/docs/registry/prometheus), while generally the following rules apply:

* The `appsuite` prefix is deliberately part of the metric name and must be specified in configuration
* Underscores `_` are typically dots `.` in code
* In Prometheus output metrics have often suffixes according to their data type. These are appended by Micrometer and **NOT** part of the metric names.


### Gauge example

Static values like limits or numeric values that can increase and decrease over time are monitored as Micrometer `Gauge`s, resulting in corresponding Prometheus `gauge` metrics. For example the current total number of pooled database connections is exposed as follows:

```bash
# HELP appsuite_mysql_connections_total The total number of pooled connections of this db pool
# TYPE appsuite_mysql_connections_total gauge
appsuite_mysql_connections_total{class="configdb",pool="-1",type="read",} 2.0
appsuite_mysql_connections_total{class="configdb",pool="-2",type="write",} 1.0
appsuite_mysql_connections_total{class="userdb",pool="2",type="read",} 0.0
appsuite_mysql_connections_total{class="userdb",pool="3",type="write",} 0.0
```
For gauges, the in-code name is typically exactly the exposed one. In this case it is `appsuite.mysql.connections.total`.


### Counter example

Metrics that are covered by ever-increasing numbers are recorded by so called `Counter` metrics. For example the number of database connection timeouts is exposed as follows:

```bash
# HELP appsuite_mysql_connections_timeout_total The number of timeouts
# TYPE appsuite_mysql_connections_timeout_total counter
appsuite_mysql_connections_timeout_total{class="configdb",pool="-1",type="read",} 0.0
appsuite_mysql_connections_timeout_total{class="configdb",pool="-2",type="write",} 0.0
appsuite_mysql_connections_timeout_total{class="userdb",pool="2",type="read",} 0.0
appsuite_mysql_connections_timeout_total{class="userdb",pool="3",type="write",} 0.0
```

Here the in-code name of the metric is `appsuite.mysql.connections.timeout`. As the metric is of Micrometer type `Counter`, it is mapped to Prometheus type `counter` and a suffix of `_total` is added to its name.


### Timer example

Every HTTP API call is recorded along with its targeted module and action, duration and protocol status. The according output looks as follows:

```bash
# HELP appsuite_httpapi_requests_seconds_max HTTP API request times
# TYPE appsuite_httpapi_requests_seconds_max gauge
appsuite_httpapi_requests_seconds_max{action="login",module="login",status="OK",} 0.0
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds_count{action="login",module="login",status="OK",} 0.0
appsuite_httpapi_requests_seconds_sum{action="login",module="login",status="OK",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.05",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.1",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.15",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.2",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.25",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.3",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.4",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.5",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.75",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="5.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="10.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="30.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="60.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="+Inf",} 0.0
```

In-code this is generated by a single metric `appsuite.httpapi.requests` of type `Timer`. The Prometheus format converter creates two different meters out of that: `appsuite_httpapi_requests_seconds_max` of type `gauge` and `appsuite_httpapi_requests_seconds_(count | sum | bucket)` of type `histogram`. Note how the data type determines the amended name suffixes of `_seconds_max`, `_seconds_count`, `_seconds_sum` and `_seconds_bucket`. These are explicitly not part of the in-code name of the metric.

Per configuration, the latency buckets can be omitted or their number and granularity be changed. The histogram can even be converted into a `summary` with defined quantiles. Further information on this can be found further down below.


## Enable/Disable

All metrics, which are enabled by default, can be disabled with the property `com.openexchange.metrics.micrometer.enable.all` set to `false`. The suffix `enable.all` is the wildcard for all registered metrics, including those of `jvm.*` and `process.*`. Those metrics will be enabled *only* if all metrics are enabled, i.e. with the `enable.all` property (omitting the shared domain name for now on for the sake of clarity), otherwise they will be disabled.

Each metric can be individually be enabled and disabled by appending to the `enable` property a prefix of their unique name. The following example will clarify things a bit more.

```bash
com.openexchange.metrics.micrometer.enable.all=false
com.openexchange.metrics.micrometer.enable.appsuite.sessions=true
```

With this configuration, only the session metrics will be enabled and nothing else. If you now request the `/metrics` data, the output should look similar to the following:

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

The distribution statistics for metrics of type `Timer` and `DistributionSummary` is also configurable. This entails configuration for enabling/disabling the percentiles histogram, the minimum and maximum values of the histogram, the publishing of concrete percentiles (e.g. 0.5, 0.75, etc.) and the publishing of SLO concrete values (e.g. 50ms, 100ms, etc.). Whether a certain metric is of type `Timer` or `DistributionSummary` and therefore latency buckets or quantiles can be enabled can usually be determined by the sole presence of according `<metric_name>_seconds_count` `<metric_name>_seconds_sum` counters.

Usually any timing metrics are pre-configured with SLOs only to reduce the number of exposed time series. For not so critical timing metrics the histogram is usually disabled, leading to only max, sum and count metrics being published. This pre-configuration happens in-code and can be overridden using the configuration possibilities described in the following sections.


### Override Histogram Buckets

A `Timer` metric with disabled histogram or pre-defined set of latency buckets can be reconfigured to expose (even more) latency buckets to let Prometheus calculate latency percentiles. For example, HTTP API requests are exposed per default as follows. Note that we focus only on the `/appsuite/api/login?action=login` calls here for brevity. Actually you will find according time series for all occurred modules, actions and status code permutations.

```bash
# HELP appsuite_httpapi_requests_seconds_max HTTP API request times
# TYPE appsuite_httpapi_requests_seconds_max gauge
appsuite_httpapi_requests_seconds_max{action="login",module="login",status="OK",} 1.964
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds_count{action="login",module="login",status="OK",} 1.0
appsuite_httpapi_requests_seconds_sum{action="login",module="login",status="OK",} 1.964
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.05",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.1",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.15",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.2",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.25",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.3",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.4",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.5",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.75",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.0",} 1.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="5.0",} 1.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="10.0",} 1.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="30.0",} 1.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="60.0",} 1.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="+Inf",} 1.0
```

The metric has histogram buckets exposed, but with a limited set of fix values, which are pre-defined using "SLOs" alone. The in-code configuration is equivalent to `com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests=50ms, 100ms, 150ms, 200ms, 250ms, 300ms, 400ms, 500ms, 750ms, 1s, 2s, 5s, 10s, 30s, 1m`. It is now for example possible to enable more fine-grained latency buckets within certain boundaries. The following configuration leads to a set of purely calculated buckets between 100ms and 30s:

```bash
# enable calculated histogram buckets
com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests = true
# disable pre-defined histogram buckets by nulling the value
com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests =
# set lower bucket boundary
com.openexchange.metrics.micrometer.distribution.minimum.appsuite.httpapi.requests = 100ms
# set upper bucket boundary
com.openexchange.metrics.micrometer.distribution.maximum.appsuite.httpapi.requests = 30s
```

This leads to the following output instead:


```bash
# HELP appsuite_httpapi_requests_seconds_max HTTP API request times
# TYPE appsuite_httpapi_requests_seconds_max gauge
appsuite_httpapi_requests_seconds_max{action="login",module="login",status="OK",} 0.0
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds_count{action="login",module="login",status="OK",} 0.0
appsuite_httpapi_requests_seconds_sum{action="login",module="login",status="OK",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.1",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.111848106",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.134217727",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.156587348",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.178956969",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.20132659",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.223696211",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.246065832",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.268435456",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.357913941",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.447392426",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.536870911",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.626349396",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.715827881",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.805306366",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.894784851",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.984263336",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.073741824",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.431655765",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.789569706",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.147483647",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.505397588",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.863311529",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="3.22122547",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="3.579139411",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="3.937053352",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="4.294967296",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="5.726623061",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="7.158278826",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="8.589934591",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="10.021590356",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="11.453246121",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="12.884901886",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="14.316557651",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="15.748213416",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="17.179869184",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="22.906492245",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="28.633115306",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="30.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="+Inf",} 0.0
```

Please note that minimum and maximum boundaries only take effect, when `distribution.histogram.[metric] = true`. Most `Timer`s come instead with SLOs only per default to limit the number of exposed bucket time series. 

Now to expose certain latencies in addition (usually to monitor a SLO conformity), you could change the configuration as follows to include concrete buckets for e.g. 500ms, 1s and 2s:

```bash
# enable calculated histogram buckets
com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests = true
# monitor certain latency SLOs
com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests = 500ms, 1s, 2s
# set lower bucket boundary
com.openexchange.metrics.micrometer.distribution.minimum.appsuite.httpapi.requests = 100ms
# set upper bucket boundary
com.openexchange.metrics.micrometer.distribution.maximum.appsuite.httpapi.requests = 30s
```

Leading to:

```bash
# HELP appsuite_httpapi_requests_seconds_max HTTP API request times
# TYPE appsuite_httpapi_requests_seconds_max gauge
appsuite_httpapi_requests_seconds_max{action="login",module="login",status="OK",} 0.0
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds histogram
appsuite_httpapi_requests_seconds_count{action="login",module="login",status="OK",} 0.0
appsuite_httpapi_requests_seconds_sum{action="login",module="login",status="OK",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.1",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.111848106",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.134217727",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.156587348",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.178956969",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.20132659",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.223696211",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.246065832",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.268435456",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.357913941",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.447392426",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.5",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.536870911",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.626349396",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.715827881",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.805306366",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.894784851",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="0.984263336",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.073741824",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.431655765",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="1.789569706",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.147483647",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.505397588",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="2.863311529",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="3.22122547",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="3.579139411",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="3.937053352",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="4.294967296",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="5.726623061",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="7.158278826",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="8.589934591",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="10.021590356",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="11.453246121",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="12.884901886",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="14.316557651",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="15.748213416",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="17.179869184",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="22.906492245",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="28.633115306",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="30.0",} 0.0
appsuite_httpapi_requests_seconds_bucket{action="login",module="login",status="OK",le="+Inf",} 0.0
```

Note the additional buckets `le="0.5"`, `le="1.0"` and `le="2.0"`.

The accepted timespan abbreviations for `minimum`, `maximum` and `slo` are:

  * `ms`: Milliseconds
  * `s`: Seconds
  * `m`: Minutes
  * `h`: Hours
  * `d`: Days
  * `w`: Weeks


### Expose Percentile Summaries

It is also possible to have quantiles pre-calculated in-app and exposed instead of raw histogram buckets. For this, the property `distribution.percentiles` comes into play. It accepts a comma-separated list of double values where each value should be between zero (0) and one (1) (both values exclusive). Again, using the HTTP API `Timer` as an example, we could do the following:

```bash
# Disable SLOs
com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests =
# Set certain percentiles
com.openexchange.metrics.micrometer.distribution.percentiles.appsuite.httpapi.requests = 0.5, 0.75, 0.95, 0.99, 0.999
```

With this configuration, the former Prometheus `histogram` is effectively converted into a `summary` and produces this output:

```bash
# HELP appsuite_httpapi_requests_seconds_max HTTP API request times
# TYPE appsuite_httpapi_requests_seconds_max gauge
appsuite_httpapi_requests_seconds_max{action="login",module="login",status="OK",} 4.828
# HELP appsuite_httpapi_requests_seconds HTTP API request times
# TYPE appsuite_httpapi_requests_seconds summary
appsuite_httpapi_requests_seconds_count{action="login",module="login",status="OK",} 6.0
appsuite_httpapi_requests_seconds_sum{action="login",module="login",status="OK",} 9.82
appsuite_httpapi_requests_seconds{action="login",module="login",status="OK",quantile="0.5",} 0.67108864
appsuite_httpapi_requests_seconds{action="login",module="login",status="OK",quantile="0.75",} 0.704643072
appsuite_httpapi_requests_seconds{action="login",module="login",status="OK",quantile="0.95",} 0.704643072
appsuite_httpapi_requests_seconds{action="login",module="login",status="OK",quantile="0.99",} 0.704643072
appsuite_httpapi_requests_seconds{action="login",module="login",status="OK",quantile="0.999",} 0.704643072
```

## Tag Filtering

It is also possible to further narrow down configuration of metrics to certain ones, by matching their tag values. The configuration format allows to specify according filter expressions similar to [PromQL](https://prometheus.io/docs/prometheus/latest/querying/examples/). For that one can set a `filter` property (`com.openexchange.metrics.micrometer.filter`) with which a name and tag filter can be defined. It supports exact and regex matches including negations For example:

```bash
com.openexchange.metrics.micrometer.filter.some_name=appsuite.httpapi.requests{action="all",module="folders"}
```

Note the `some_name` suffix of the property. This will be used as a pseudo name for the metric name and the filter to allow access to the previously defined properties, namely the enable/disable and distribution statistics. The following configuration should enable all `appsuite.httpapi.request` for the `all` action of the `folder` module and publish only latency buckets for 50, 100 and 150ms:

```bash
com.openexchange.metrics.micrometer.enable.some_name=true
com.openexchange.metrics.micrometer.distribution.histogram.some_name=false
com.openexchange.metrics.micrometer.distribution.slo.some_name=50ms,100ms,150ms
```

The `filter` expression supports exact positive and negative matching through the `=` and `!=` operators as well as positive and negative regex matching through the `=~` and `!~` operators. For example:

```bash
com.openexchange.metrics.micrometer.enable.appsuite.mysql.connections.usage=false
com.openexchange.metrics.micrometer.filter.read_connections=appsuite.mysql.connections.usage{type="write",class=~".*db",pool!~"-1|-2"}
com.openexchange.metrics.micrometer.enable.read_connections=true
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
  * `distribution.slo.<METRIC_NAME>`: Defines the SLOs to publish for that specific metric.
  * `distribution.minimum.<METRIC_NAME>`: Defines the lower bound of percentile histogram buckets to publish for that specific metric.
  * `distribution.maximum.<METRIC_NAME>`: Defines the upper bound of percentile histogram buckets to publish for that specific metric.
  * `filter.<FILTER_NAME>`: Defines a filter which can further narrow down all metrics by tags. The `<FILTER_NAME>` is then used to access all `enable` and `distribution.*` properties (it replaces the `<METRIC_NAME>`).


# Developer Guide

The `com.openexchange.metrics.MetricService` has been deprecated. It can still be used at the moment, but is going to be removed in a future release. Therefore it is advised to migrate existing metrics and its tooling to the new Micrometer/Prometheus format.

The migration is very simple. Instead of using an OSGi service, one can define and register metrics via static methods, similar to SLF4J. You need to import the following packages in your `MANIFEST.MF`:

```
io.micrometer.core.instrument,
io.micrometer.core.instrument.composite
```

The latter one is a runtime dependency, don't skip it!

A simple counter can then be registered as follows:

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

Counter myCounter = Counter.builder("metric.group." + "metric.name")
                            .description("Some description")
                            .baseUnit("unit")
                            .tags("tagname1", "tagvalue1", "tagname2", "tagevalue2")
                            .register(Metrics.globalRegistry);
```

You just have to make sure that you follow the naming convention. The name of the meter should be in lower case and words must be separated by a dot (e.g. "cache.events.delivered"). Application-specific core metrics SHOULD be prefixed with `appsuite.` to make them easily distinguishable.

In case dynamic tags are used you don't have to worry about checking the registry for an already existing meter, because if the meter is already registered, the register command returns the previously registered meter.
It is nevertheless recommended to create metrics statically wherever possible.

# Visualization

## Grafana - Dashboards as Code

With App Suite 7.10.4 we are offering Grafana Dashboards as Code for `App Suite` and the `Java Virtual Machine`.

### Requirements

- `Jsonnet` is a data templating language &rightarrow; [https://github.com/google/jsonnet](https://github.com/google/jsonnet)
- `Grafonnet` is a `Jsonnet` library for generating Grafana dashboards &rightarrow; [https://github.com/grafana/grafonnet-lib](https://github.com/grafana/grafonnet-lib)
- The dashboard source files &rightarrow; [https://github.com/open-xchange/appsuite-middleware/tree/master/monitoring/grafana](https://github.com/open-xchange/appsuite-middleware/tree/master/monitoring/grafana)

Please refer to the `README.md` files in the corresponding repositories for detailed informations, on how to install `Jsonnet` and `Grafonnet`.

### Build and import dashboards

There are couple of ways to build and generate Grafana dashboards from `jsonnet` source files. The following example will show _how_ to build them locally. To generate for example the `JSON` for the `App Suite` dashboard, just follow the step mentioned below:

```bash
jsonnet -J /path/to/grafonnet-lib /path/to/dashboards/appsuite_mw.jsonnet -o ./appsuite_mw.json
```

or without the `-o` parameter to print the `JSON` to `STDOUT`:

```bash
jsonnet -J /path/to/grafonnet-lib /path/to/dashboards/appsuite_mw.jsonnet
```

The generated `JSON` can be imported or provisioned to Grafana and should look like:

![app_suite_grafana_dashboard](02_micrometer_and_prometheus/app_suite_grafana_dashboard.png 'app_suite_grafana_dashboard')

### Add Prometheus datasource

The _default_ datasource should be named `Prometheus` so it is automatically picked up by the graphs:

![prometheus_ds_settings](02_micrometer_and_prometheus/prometheus_ds_settings.png 'prometheus_ds_settings')

### Prometheus configuration

The dashboards rely on the `service` label to distinguish the different App Suite services. So please make sure that, in the Prometheus configuration, each of the targets has the `service` label defined. Let's say you want to monitor an App Suite with IP `10.20.30.40` then the excerpt of the config should look like this:

```yaml
scrape_configs:
  - job_name: appsuite
    static_configs:
      - targets: ['10.20.30.40:8009']
        labels:
          env: dev
          job: appsuite
          service: mw
```

## Zabbix - Prometheus Integration

### Requirements

- The [Zabbix](https://www.zabbix.com) - [Prometheus](https://prometheus.io) integration is available in _version 4.2_ and higher.
- The new metrics for App Suite are available in _version 7.10.4_ and higher.

### Host configuration

- Go to **Configuration** > **Hosts**.
- Click on **Create host** to open the host configuration page.
- Enter a hostname `Prometheus` and select at least one host group.

### Simple items

First create an `HTTP agent master item` to do any Prometheus monitoring:

- Go to **Configuration** > **Hosts**.
- Click on the `Prometheus` host > **Items**.
- Click on **Create item**.
- Enter or change item parameters:
  - Name: **`Get App Suite Metrics`**
  - Type: **`HTTP agent`**
  - Key: **`appsuite_metrics.get`**
  - URL: **`http://<APPSUITE_HOST>:<APPSUITE_PORT>/metrics`**
  - Type of information: **`Text`**
  - Update interval: **`10s`**
  - History storage period: **`Do not keep history`**
- Click on **Add** to save item.

Here is an example on how to extract the amount of active App Suite sessions based on the output of the _master_ item.

- Create another item for the `Prometheus` host.
- Enter or change item parameters:
  - Name: **`Active session`**
  - Type: **`Dependent item`**
  - Key: **`appsuite.sessions.active.total`**
  - Master item: Select the master item from above **`Get App Suite Metrics`**
  - Type of information: **`Numeric (unsigned)`**
- Now click on the **`Preprocessing`** tab:
  - Add a preprocessing step: `Prometheus pattern`
  - Pattern: **`appsuite_sessions_active_total{client=~".*"}`**
- And save item.

### Low-level discovery

There is another way to automatically create items called `low-level discovery`. The next example shows how to create a discovery rule and automatically create items for the `App Suite DB Pools`.

- Go to **Configuration** > **Host** and select the **Prometheus** host > **Discovery rules**
- Click on **Create discovery rule**
- Enter or change item parameters:
  - Name: **`DB Pool connections LLD`**
  - Type: **`Dependent item`**
  - Key: **`appsuite.dbpool.discovery`**
  - Master item: Select the master item **`Get App Suite Metrics`**
- Click on the **`Preprocessing`** tab:
  - Add a preprocessing step: `Prometheus to JSON`
  - Parameters: `{__name__=~"^appsuite_mysql_connections(?:_total)?$"}`
- Click on the **`LLD macros`** tab:
  - Add the following `LLD macros`

| LLD macro    | JSONPath         |
| :----------- | :--------------- |
| `{#DBCLASS}` | `$.labels.class` |
| `{#DBPOOL}`  | `$.labels.pool`  |
| `{#DBTYPE}`  | `$.labels.type`  |
| `{#HELP}`    | `$.help`         |
| `{#METRIC}`  | `$.name`         |

- Save discovery rule.

Now create an item prototype for that discovery rule.

- Click on **Create item prototype**
- Enter or change item prototype parameters:
  - Name: **`appsuite_mysql_connections_active: "{#DBCLASS}", Pool "{#DBPOOL}", Type "{#DBTYPE}"`**
  - Type: **`Dependent item`**
  - Key: **`appsuite.mysql.connections.active["{#DBCLASS}","{#DBPOOL}","{#DBTYPE}"]`**
  - Master item: Select the master item **`Get App Suite Metrics`**
  - Description: **`{#HELP}`**
- Click on the **`Preprocessing`** tab:
  - Add a preprocessing step: `Prometheus pattern`
  - Pattern: **`appsuite_mysql_connections_active{type="{#DBTYPE}",class="{#DBCLASS}",pool="{#DBPOOL}"}`**
- Save item prototype.

### Template

There is an example template which contains the items and the low-level discovery rule from this walkthrough. To import the template go to

- **Configuration** > **Templates**
- Click on **Import**
- Import file **`template_ox_appsuite_prom.xml`**

To apply now this template e.g. to host `10.20.30.40`

- Go to **Configuration** > **Hosts**
- Click on **Create host** to open the host configuration page.
- Enter a hostname `appsuite-node1` and select at least one host group.
- Click on **Templates** tab
- Link new template **`Template OX App Suite`**
- Click on **Macros** tab and select **Inherited and host macros**
- Change macro **`{$APPSUITE.CONN}`** value from `127.0.0.1` to `10.20.30.40`
- Click on **Add** to save host

Switch to **Monitoring** > **Latest data** to check whether the template is applied properly or not. If everything is fine, then there should be some values displayed e.g.

![latest_data](02_micrometer_and_prometheus/zabbix_latest_data.png 'latest_data')
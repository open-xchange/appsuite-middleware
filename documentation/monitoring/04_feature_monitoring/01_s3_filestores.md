---
title: S3 Filestores
icon: fas fa-cubes
---

# General

For S3 filestores, client-side HTTP connection pools and request/response latencies can be monitored. This requires client-centric configuration, i.e. access to the storage system must use the `com.openexchange.filestore.s3client.` configuration style instead of per-filestore (`com.openexchange.filestore.s3.`) configuration as it was common pre-7.10.4. See [this](../../administration/s3_client_configuration.html) for details.

With S3 clients and the mapping from registered filestores to configured clients properly configured, monitoring can be enabled by setting `com.openexchange.filestore.s3.metricCollection = true`. This setting results in four additional outputs that can be recorded by monitoring and logging systems:

1. For each configured client, connection pool monitoring appears in Prometheus output.
2. Each request-response roundtrip to S3 is logged along with recent statistic information.
3. Each request-response roundtrip is measured in terms of latency and appears in Prometheus output.
4. Overall (across all clients) byte throughput is measured and appears in Prometheus output.

1.-3. however only apply, if the number of different configured clients is smaller than a configurable threshold `com.openexchange.filestore.s3.maxNumberOfMonitoredClients` (default: 20). This is to limit the exposed Prometheus time series in cases where - for whatever reasons - large numbers of clients are configured. 


# Example configuration

The following configuration is assumed throughout the other sections of this article, if not stated otherwise. It specifies the usage of AWS S3 (instead of an API-compatible on-premise system) with a single client "default" being configured. The client connects to region "eu-central-1" and handles all buckets with name prefix "appsuite-files-". Its connection pool is limited to 20 concurrent connections.

A single registered filestore is explicitly mapped to a bucket name that is handled by the "default" client.

Metric collection is enabled.


```
com.openexchange.filestore.s3client.default.region = eu-central-1
com.openexchange.filestore.s3client.default.pathStyleAccess = false
com.openexchange.filestore.s3client.default.encryption = none
com.openexchange.filestore.s3client.default.signerOverride =
com.openexchange.filestore.s3client.default.maxConnectionPoolSize = 20
com.openexchange.filestore.s3client.default.credentialsSource = iam
com.openexchange.filestore.s3client.default.buckets = appsuite-files-*

com.openexchange.filestore.s3.appsuite-files-1.bucketName = appsuite-files-1

com.openexchange.filestore.s3.metricCollection = true
```


# Logging output

Each request of the default client is now logged by a logger that is part of the used S3 Java Client SDK. Example:

```
2020-05-21T11:58:37,985+0200 INFO  [OXWorker-0000020] com.amazonaws.util.AWSRequestMetricsFullSupport.log(AWSRequestMetricsFullSupport.java:203)
ServiceName=[Amazon S3], StatusCode=[200], ServiceEndpoint=[https://st-files-2-push.s3.eu-central-1.amazonaws.com], RequestType=[GetObjectMetadataRequest], AWSRequestID=[3A7F314BC82CD57A], HttpClientPoolPendingCount=0, RetryCapacityConsumed=0, HttpClientPoolAvailableCount=1, RequestCount=1, HttpClientPoolLeasedCount=0, ResponseProcessingTime=[8.717], ClientExecuteTime=[25.678], HttpClientSendRequestTime=[0.251], HttpRequestTime=[16.294], RequestSigningTime=[0.183], CredentialsRequestTime=[0.002, 0.001], HttpClientReceiveResponseTime=[15.52], 
 com.openexchange.ajax.action=new
 com.openexchange.ajax.module=files
 com.openexchange.database.schema=oxdatabase_5
 com.openexchange.grizzly.method=POST
 com.openexchange.grizzly.queryString=action=new&extendedResponse=true&force_json_response=true&session=826e1b3e60524064918b00cb84bbe535&timestamp=2116800000000&try_add_version=true
 com.openexchange.grizzly.remoteAddress=127.0.0.1
 com.openexchange.grizzly.remotePort=55869
 com.openexchange.grizzly.requestURI=/appsuite/api/files
 com.openexchange.grizzly.serverName=localhost
 com.openexchange.grizzly.servletPath=/appsuite/api/files
 com.openexchange.grizzly.threadName=OXWorker-0000020
 com.openexchange.grizzly.userAgent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36
 com.openexchange.hostname=localhost
 com.openexchange.localhost.ipAddress=127.0.0.1
 com.openexchange.localhost.version=7.10.4-Rev0
 com.openexchange.request.trackingId=1154563797-577281941
 com.openexchange.session.authId=392403a1e9324659bc40f8e6bff1ce42
 com.openexchange.session.clientId=open-xchange-appsuite
 com.openexchange.session.contextId=1
 com.openexchange.session.loginName=peter
 com.openexchange.session.sessionId=826e1b3e60524064918b00cb84bbe535
 com.openexchange.session.userId=3
 com.openexchange.session.userName=peter@example.com
```

In cases of no apparent issues with S3 connections, i.e. when not being in a troubleshooting situation, this logging might be too verbose. It can be disabled using the typical configuration in `logback.xml`:

```xml
<logger name="com.amazonaws.latency" level="OFF"/>`
```


# Connection pool monitoring

This will show up in Prometheus output:

```
# HELP appsuite_filestore_s3_connections_pending The number of threads pending on getting a HTTP connection from the pool.
# TYPE appsuite_filestore_s3_connections_pending gauge
appsuite_filestore_s3_connections_pending{client="default",} 0.0
# HELP appsuite_filestore_s3_connections_max The configured maximum number of concurrent connections.
# TYPE appsuite_filestore_s3_connections_max gauge
appsuite_filestore_s3_connections_max{client="default",} 20.0
# HELP appsuite_filestore_s3_connections_leased The number of leased pooled HTTP connections.
# TYPE appsuite_filestore_s3_connections_leased gauge
appsuite_filestore_s3_connections_leased{client="default",} 0.0
# HELP appsuite_filestore_s3_connections_available The number of available pooled HTTP connections.
# TYPE appsuite_filestore_s3_connections_available gauge
appsuite_filestore_s3_connections_available{client="default",} 1.0
```

The ratio of leased vs. max connections is worth to be observed. If it reaches 100%, pending connections will go up and effectively users will need to wait for an available connection until their requests can be performed. A constantly high value of used connections might hint towards a too small pool size or even latency issues with the S3 system.


# Operation latencies and error rates

Latencies and error rates show up in Prometheus output like so:

```
# HELP appsuite_filestore_s3_requests_seconds_max S3 HTTP request times
# TYPE appsuite_filestore_s3_requests_seconds_max gauge
appsuite_filestore_s3_requests_seconds_max{client="default",status="200",type="PutObjectRequest",} 0.1
appsuite_filestore_s3_requests_seconds_max{client="default",status="200",type="GetObjectMetadataRequest",} 0.019
appsuite_filestore_s3_requests_seconds_max{client="default",status="200",type="GetObjectRequest",} 0.096
appsuite_filestore_s3_requests_seconds_max{client="default",status="200",type="HeadBucketRequest",} 3.031
appsuite_filestore_s3_requests_seconds_max{client="default",status="400",type="HeadBucketRequest",} 0.264
# HELP appsuite_filestore_s3_requests_seconds S3 HTTP request times
# TYPE appsuite_filestore_s3_requests_seconds summary
appsuite_filestore_s3_requests_seconds_count{client="default",status="200",type="PutObjectRequest",} 1.0
appsuite_filestore_s3_requests_seconds_sum{client="default",status="200",type="PutObjectRequest",} 0.1
appsuite_filestore_s3_requests_seconds_count{client="default",status="200",type="GetObjectMetadataRequest",} 1.0
appsuite_filestore_s3_requests_seconds_sum{client="default",status="200",type="GetObjectMetadataRequest",} 0.019
appsuite_filestore_s3_requests_seconds_count{client="default",status="200",type="GetObjectRequest",} 1.0
appsuite_filestore_s3_requests_seconds_sum{client="default",status="200",type="GetObjectRequest",} 0.096
appsuite_filestore_s3_requests_seconds_count{client="default",status="200",type="HeadBucketRequest",} 1.0
appsuite_filestore_s3_requests_seconds_sum{client="default",status="200",type="HeadBucketRequest",} 3.031
appsuite_filestore_s3_requests_seconds_count{client="default",status="400",type="HeadBucketRequest",} 1.0
appsuite_filestore_s3_requests_seconds_sum{client="default",status="400",type="HeadBucketRequest",} 0.264
```

The separation by status code and request type helps to narrow down issues with high error rates or latencies. The usual Micrometer configuration can be used to even turn these summaries into histograms, if needed. See [here](../02_micrometer_and_prometheus.html) for details.


# Transferred bytes

This is an overall metric, applied to all S3 clients. It measures the amount of data sent and received to/from S3. Prometheus can convert this into a rate.

```
# HELP appsuite_filestore_s3_transferred_bytes_total The size of s3 requests.
# TYPE appsuite_filestore_s3_transferred_bytes_total counter
appsuite_filestore_s3_transferred_bytes_total{type="S3UploadThroughput",} 153523.0
appsuite_filestore_s3_transferred_bytes_total{type="S3DownloadThroughput",} 153523.0
```

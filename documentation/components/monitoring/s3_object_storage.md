---
title: S3 Storage Monitoring Metrics
---

The OX middleware now exposes monitoring metrics for the S3 object storage via JMX.

# How to enable

The metric collection can be simply enabled with the following property:

```properties
com.openexchange.filestore.s3.metricCollection=true
```

# What's Collected

Two type of metrics are collected: 

 * the amount of HTTP requests per HTTP method
 * the throughput (separated to upload and download rates)

## HTTP Requests per HTTP Method

For every HTTP method a `timer` metric is created which provides different distributions for the amount of requests it collects. The rate unit is set to events per second. The different distributions are:

 * 50th percentile
 * 75th percentile
 * 95th percentile
 * 99th percentile
 * 99.9th percentile
 * 1 minute rate
 * 5 minute rate
 * 15 minute rate
 * Mean rate
 * Standard deviation
 * Minumum
 * Maximum
 * Mean value
 * Count

Each HTTP method registers its own MBean dynamically, meaning that until metrics are collected it will not appear in the JMX list.

## Throughput

For the throughput there are two `meter` metrics registered, one for download and one for upload. The rate unit is set to bytes per second. The different distributions are:

 * Count
 * Mean rate
 * 1 minute rate
 * 5 minute rate
 * 15 minute rate

# Monitoring Endpoints

The mbeans are registered under the main `com.openexchange.metrics` mbean. The `s3` storage sub-component contains all relevant mbeans.

For every HTTP method there is a `com.openexchange.metrics:name=RequestTimes.<HTTP_METHOD_NAME>,type=s3` endpoint and for the throughput the `com.openexchange.metrics:name=S3UploadThroughput,type=s3` and `com.openexchange.metrics:name=S3DownloadThroughput,type=s3` for upload and download respectively.

# Fetching Value

To fetch the metrics for a specific HTTP method via Jolokia, say for `PUT` you can issue the command:

```bash
$ curl http://username:password@localhost:8009/monitoring/jolokia/read/com.openexchange.metrics:name=RequestTimes.PUT,type=s3
```
And the response would look similar to this:

```json
{
  "request": {
    "mbean": "com.openexchange.metrics:name=RequestTimes.PUT,type=s3",
    "type": "read"
  },
  "value": {
    "75thPercentile": 395,
    "StdDev": 75.27537765635286,
    "Mean": 350.8621046884102,
    "98thPercentile": 611,
    "RateUnit": "events/second",
    "95thPercentile": 450,
    "99thPercentile": 611,
    "Max": 611,
    "Count": 50,
    "FiveMinuteRate": 1.2702780904523332,
    "50thPercentile": 368,
    "MeanRate": 0.19481812786961306,
    "Min": 175,
    "OneMinuteRate": 0.053904838208509866,
    "DurationUnit": "milliseconds",
    "999thPercentile": 611,
    "FifteenMinuteRate": 2.1514407038999352
  },
  "timestamp": 1519307091,
  "status": 200
}
```

You can also request single values from the MBean like this:

```bash
$ curl http://username:password@localhost:8009/monitoring/jolokia/read/com.openexchange.metrics:name=RequestTimes.PUT,type=s3/Mean
```

```json
{
  "request": {
    "mbean": "com.openexchange.metrics:name=RequestTimes.PUT,type=s3",
    "attribute": "Mean",
    "type": "read"
  },
  "value": 350.8621046884102,
  "timestamp": 1519307127,
  "status": 200
}
```
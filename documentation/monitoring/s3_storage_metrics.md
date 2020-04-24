---
title: S3 Storage Metrics
icon: fa-cubes
tags: Monitoring, Administration, S3, Configuration
---

The OX middleware exposes monitoring metrics for the S3 object storage like all other metrics via the /metrics rest endpoint in the prometheus format.

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

For every HTTP method a `timer` metric is created with the name `appsuite_s3_requestTimes_<HTTP_METHOD_NAME>`. The rate unit is set to events per second.

## Throughput

For every type (download and upload) there are two metrics registered. `appsuite_s3_requestSizeTimer{type=<Type>}` tracks the timing of the request and `appsuite_s3_requestSize{type=<Type>}` tracks the size.
With both of them combined you can measure the throughput.


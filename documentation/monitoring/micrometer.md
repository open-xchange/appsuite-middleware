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
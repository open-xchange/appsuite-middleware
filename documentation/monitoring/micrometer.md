---
title: Micrometer
icon: fa-info
tags: Monitoring, Administration
---

# Introduction

With 7.10.4 the ox appsuite changes the way metrics are gathered. The current implementation that uses the MetricService is now deprecated and instead micrometer with its prometheus implementation is used.
All metrics are exposed via the /metrics rest endpoint.  

# How to migrate metrics from MetricService to micrometer

Developers can still use the MetricService, but it is going to be removed in a future release. Therefore it is advised to migrate existing metrics and its tooling to the new micrometer/prometheus format.
The migration is very simple. Instead of using an osgi service one can define and register metrics via static methods, similar to  SLF4J. A simple counter can be registered as follows:

```java
Counter myCounter = Counter.builder("metric.group." + "metric.name")
                            .description("Some description")
                            .baseUnit("unit")
                            .tags("tagname1", "tagvalue1", "tagname2", "tagevalue2")
                            .register(Metrics.globalRegistry);
```

You just have to make sure that you follow the naming convention. The name of the meter should be in lower case and words must be separated by a dot (e.g. "cache.events.delivered").
In case dynamic tags are used you don't have to worry about checking the registry for an already existing meter, because if the meter is already registered the register command returns the previously registered meter.
It is nevertheless recommended to create metrics statically wherever possible.

For most use cases micrometer provides an adequate meter, but micrometer doesn't support dropwizards meter type. This means if you want to measure something like throughput you will have to create two metrics for it: a timer and a counter.
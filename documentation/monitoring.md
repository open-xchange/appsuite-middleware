---
title: Monitoring
icon: fa-heartbeat
---

**IMPORTANT:** Starting with 7.10.4 App Suite Middleware has migrated its most relevant monitoring metrics to [Micrometer](https://micrometer.io/) and publishes them in [Prometheus format](https://prometheus.io/docs/instrumenting/exposition_formats/) under an HTTP endpoint `/metrics`. Future monitoring metrics will only be implemented using that approach, MBean-based monitoring exposed via JMX and Jolokia is now considered deprecated.

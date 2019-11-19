---
title: Circuit Breaker
icon: fas fa-charging-station
tags: Mail, Configuration, Administration, Monitoring
---

# Introduction

Whenever the App Suite middleware relies on an external component to complete an incoming client request, the overall response time mainly depends on the actual response of the external component. For example, when there is some higher load on the IMAP server, and due to the high load, the response latency increases, this leads to an increased response time for the requests to the middleware. Each of those handled requests consumes resources (threads, memory), which directly impacts the general responsiveness of the Java process.

Moreover, if requests aren't returned in a timely fashion by the middleware node, the user or client might issue them again after a proxy timeout was encountered, leading to more stalled requests as they're waiting for the external component, too. Or, the load balancer could even decide that the middleware node is not responding properly anymore, and as a consequence, begins to route requests to other middleware nodes, which in turn will forward the incoming requests to the same external subsystem under load, so that the problem gets cascaded throughout all nodes in the cluster.

A common pattern to survive such situations is a so-called Circuit Breaker - a logical piece within the stack that detects or tracks failures in external systems and prevents the application from trying to perform an action repeatedly where it is known that it will fail, prior trying again after some backoff time. 

Since v7.10.3, such a Circuit Breaker is available for connections IMAP endpoints. It is disabled by default, but can be activated after some parameters of the target environment have been evaluated. This article describes some basic principles of the implementation and provides guidelines for enabling the feature.

# How it works

- states (open/closed/half-open)
- failure/success rate
- general or specific/named accounts, enpoints, ports

# Installation


# Configuration, Monitoring & Tuning



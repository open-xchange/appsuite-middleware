---
title: Grizzly HTTP Server
icon: fas fa-tachometer-alt
tags: Monitoring, Administration
---

Monitoring
==========

To get an overview about all available Grizzly metrics, perform a `GET /monitoring/jolokia/list/org.glassfish.grizzly`. It will describe all Grizzly related MBeans along with their attributes. Find below a selection of relevant attributes to monitor.

It is noteworthy that all of belows MBeans show up twice if the HTTPS listener is enabled. Metrics can be distinguished by the `name` attribute of the respective MBeans, which contains either `http-listener` or `https-listener` as part of its string value. It is possible to use wildcards ("*") when requesting MBean outputs via Jolokia. Note that all examples below refer to HTTP**S** while plain HTTP is more common in production environments.


HTTP requests
-------------

`GET /monitoring/jolokia/read/org.glassfish.grizzly:type=HttpServerFilter,pp=*https-listener*,name=*`


### Relevant attributes

* current-suspended-request-count: (Integer) The current number of requests that are suspended to be processed at a later point in time.
* requests-received-count: (Long) The total number of requests received.
* requests-completed-count: (Long) The total number of requests that have been successfully completed.
* requests-cancelled-count: (Long) The total number of suspended requests that have been cancelled.
* requests-timed-out-count: (Long) The total number of suspended requests that have been timed out.


### Example response

    {
        "request": {
            "mbean": "org.glassfish.grizzly:name=*,pp=*https-listener*,type=HttpServerFilter",
            "type": "read"
        },
        "value": {
            "org.glassfish.grizzly:name=HttpServerFilter,pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[https-listener]],type=HttpServerFilter": {
                "current-suspended-request-count": 0,
                "Parent": {
                    "objectName": "org.glassfish.grizzly:name=NetworkListener[https-listener],pp=/gmbal-root/HttpServer[HttpServer],type=NetworkListener"
                },
                "requests-received-count": 56,
                "requests-cancelled-count": 0,
                "Children": [],
                "requests-completed-count": 55,
                "requests-timed-out-count": 0,
                "Name": "HttpServerFilter"
            }
        },
        "timestamp": 1583255498,
        "status": 200
    }


TCP connections
---------------

GET `/monitoring/jolokia/read/org.glassfish.grizzly:type=TCPNIOTransport,pp=*https-listener*,name=*`


### Relevant attributes

* open-connections-count: (Integer) Number of currently open connections.
* total-connections-count: (Long) Number of accepted connections.
* last-error: (String) Description and time of the latest occurred error.
* bytes-written: (Long) Total bytes written.
* bytes-read: (Long) Total bytes read.

Furthermore this MBean outputs some inet socket configuration attributes like `socket-tcp-no-delay`, `client-socket-so-timeout`. These do not need to be recorded periodically as they do not change, but can be of informational use when debugging issues.


### Example response

    {
        "request": {
            "mbean": "org.glassfish.grizzly:name=*,pp=*https-listener*,type=TCPNIOTransport",
            "type": "read"
        },
        "value": {
            "org.glassfish.grizzly:name=Transport,pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[https-listener]],type=TCPNIOTransport": {
                "client-socket-so-timeout": 60000,
                "selector-handler": "org.glassfish.grizzly.nio.DefaultSelectorHandler",
                "open-connections-count": 1,
                "Parent": {
                    "objectName": "org.glassfish.grizzly:name=NetworkListener[https-listener],pp=/gmbal-root/HttpServer[HttpServer],type=NetworkListener"
                },
                "io-strategy": "org.glassfish.grizzly.strategies.SameThreadIOStrategy",
                "socket-tcp-no-delay": true,
                "socket-keep-alive": true,
                "Name": "Transport",
                "socket-reuse-address": true,
                "channel-distributor": "org.glassfish.grizzly.nio.RoundRobinConnectionDistributor",
                "selection-key-handler": "org.glassfish.grizzly.nio.DefaultSelectionKeyHandler",
                "Children": [
                    {
                        "objectName": "org.glassfish.grizzly:name=MemoryManager,pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[https-listener]]/TCPNIOTransport[Transport],type=HeapMemoryManager"
                    }
                ],
                "state": "STARTED (Tue Mar 03 15:01:30 GMT 2020)",
                "processor-selector": "N/A",
                "server-socket-so-timeout": 0,
                "client-connect-timeout-millis": 30000,
                "total-connections-count": 69,
                "last-error": "N/A",
                "selector-threads-count": 3,
                "read-buffer-size": -1,
                "thread-pool-type": "com.openexchange.http.grizzly.threadpool.GrizzlyExecutorService",
                "bound-addresses": "[/0:0:0:0:0:0:0:0:8010]",
                "bytes-written": 271565,
                "processor": "org.glassfish.grizzly.filterchain.DefaultFilterChain",
                "socket-linger": -1,
                "bytes-read": 63382,
                "write-buffer-size": -1
            }
        },
        "timestamp": 1583255409,
        "status": 200
    }


Memory statistics
-----------------

`GET /monitoring/jolokia/read/org.glassfish.grizzly:type=HeapMemoryManager,pp=*https-listener*,name=*`


### Relevant attributes

All these numbers are only ever increasing. Currently allocated pool memory can be determined by subtracting released from allocated bytes.

* pool-allocated-bytes: (Long) Total number of bytes allocated from memory pool
* pool-released-bytes: (Long) Total number of bytes released to memory pool
* real-allocated-bytes: (Long) Total number of bytes allocated using ByteBuffer.allocate(...) operation
* total-allocated-bytes: (Long) Total number of allocated bytes (real + pool)


### Example response

    {
        "request": {
            "mbean": "org.glassfish.grizzly:name=*,pp=*https-listener*,type=HeapMemoryManager",
            "type": "read"
        },
        "value": {
            "org.glassfish.grizzly:name=MemoryManager,pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[https-listener]]/TCPNIOTransport[Transport],type=HeapMemoryManager": {
                "real-allocated-bytes": 775002,
                "pool-released-bytes": 2310254,
                "max-buffer-size": 65536,
                "Parent": {
                    "objectName": "org.glassfish.grizzly:name=Transport,pp=/gmbal-root/HttpServer[HttpServer]/NetworkListener[NetworkListener[https-listener]],type=TCPNIOTransport"
                },
                "total-allocated-bytes": 3109410,
                "pool-allocated-bytes": 2334408,
                "Children": [],
                "Name": "MemoryManager"
            }
        },
        "timestamp": 1583255631,
        "status": 200
    }

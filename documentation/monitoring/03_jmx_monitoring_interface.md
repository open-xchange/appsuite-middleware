---
title: JMX Interface
icon: fa-info
tags: Monitoring, Administration
---

# Deprecation Warning

**It is strongly recommended to switch to the Prometheus-based monitoring whenever possible! The mentioned MBeans are deprecated and might be removed in a future release. Parity in terms of available data points will be created during the next releases. See [Micrometer & Prometheus](02_micrometer_and_prometheus.html) for further information.**

# Introduction

App Suite Middleware offers the ability to fetch runtime information of the Java virtual machine and the application itself. This article will give you information about the most common items from a monitoring perspective, and possible alarm trigger values.

# Important monitoring interface items

The following items seem to be the most important ones for monitoring the application. Of course there are several others available as well, and those might also be used if you require additional information about the runtime. Please use the common interfaces to fetch these information from the application (JMX, showruntimestats).

## Active user sessions

### java.lang:type=OperatingSystem,MaxFileDescriptorCount

Maximum number of file handles that can be taken by the Java virtual machine.

### java.lang:type=OperatingSystem,OpenFileDescriptorCount

Number of all file handles taken by the Java virtual machine currently. This includes all created sockets and virtual machine resources, too. Example notification value: (MaxFileDescriptorCount - OpenFileDescriptorCount) < 100. Monitor to determine if the number of open files that can be opened by the vm is sufficient.

### com.openexchange.pooling:name=Overview,NumConnections

Total number of currently open database connections. This includes database connections to all database machines in the cluster (ConfigDB, User-DB). Example notification value: The number of database connections raises to the number of Grizzly sockets or even beyond that. This means that the response of the database is too slow and needs to be monitored.

### com.openexchange.monitoring:name=GeneralMonitor,NumberOfIMAPConnections

Total number of currently opened connections to the mail servers. This are connections using the IMAP protocol. Monitor breakouts to determine if the IMAP servers are able to handle the number of requests.

## Threads

### java.lang:type=Threading,ThreadCount
 
Number of total threads running inside the Java virtual machine. This includes the number of threads from the thread pool mentioned next. Some components of Open-Xchange and the Java virtual machine create their threads on their own without using the thread pool.

### com.openexchange.threadpool:name=ThreadPoolInformation,ActiveCount
 
Number of threads created from the internal thread pool. The thread pool efficiently deals with creating and destroying threads and keeps the fork rate as low as possible. All essential components in Open-Xchange create their threads using this thread pool.

## Thread pool tasks

### com.openexchange.threadpool:name=ThreadPoolInformation,TaskCount

Total number of tasks to be executed and submitted to the thread pool. The graph should show he difference  between the current value and the last value. Example notification value: The number of newly submitted tasks raises extraordinary.

### com.openexchange.threadpool:name=ThreadPoolInformation,CompletedTaskCount

The total number of tasks executed by the thread pool. The graph should show the difference  between the current value and the last value.

## Broken connections

Every increase of one of this numbers is an indicator that fetching data from one of the backend systems did not work as expected.

### com.openexchange.monitoring:name=MailInterfaceMonitor,NumTimeoutConnections = 0
 
Number of IMAP connections that got into a timeout.

### com.openexchange.monitoring:name=MailInterfaceMonitor,NumFailedLogins = 0
 
Number of IMAP login attempts that failed.

### com.openexchange.monitoring:name=MailInterfaceMonitor,NumBrokenConnections = 0

Number of IMAP data fetches that failed somehow.

### com.openexchange.pooling:name=ConfigDB Read,NumBrokenConnections = 0

Number of connections to the config database slave that encountered a problem.

### com.openexchange.pooling:name=ConfigDB Write,NumBrokenConnections = 0

Number of connections to the config database master that encountered a problem.

### com.openexchange.pooling:name=DB Pool <masterNum>,NumBrokenConnections = 0

Number of connections to the user database master that encountered a problem. Get the identifier of this database server from the listdatabase command.

### com.openexchange.pooling:name=DB Pool <slaveNum>,NumBrokenConnections = 0

Number of connections to the user database slave that encountered a problem. Get the identifier of this database server from the listdatabase command.

## Database <identifier> connections

Get the identifier of this database server from the listdatabase command.

### com.openexchange.pooling:name=DB Pool <identifier>,NumActive = 0
 
Current number of used connections to this database. These connections sent a SQL command to the database server or data from the database is read.

### com.openexchange.pooling:name=DB Pool <identifier>,NumIdle = 3

Current number of established but not used connections to this database.

### com.openexchange.pooling:name=DB Pool <identifier>,NumWaiting = 0

Number of threads waiting for a database connection if the maximum configured number of database connections is already opened. As early as threads need to wait for database connections the performance will degrade extraordinary.

### com.openexchange.pooling:name=DB Pool <identifier>,PoolSize = 3

Sum of active and idle connections to the database.

## Database <identifier> times

### com.openexchange.pooling:name=DB Pool <identifier>,AvgUseTime = 0.416

Average time a thread occupies a database connection to fetch some data. This average is calculated from the last 1000 use times. A raise in the average use time indicates that the database servers are becoming slower and overall performance may degrade.

## Database replication monitoring

### com.openexchange.pooling:name=Overview,MasterConnectionsFetched = 287

Number of fetches of connections to the master database. Compared to the number of fetches of connections to the slave database this indicates the ratio of writes to reads on the database.

### com.openexchange.pooling:name=Overview,SlaveConnectionsFetched = 1268334

Number of fetches of connections to the slave database. Every time data needs to be read a connection to the slave is fetched.

### com.openexchange.pooling:name=Overview,MasterInsteadOfSlave = 47

Open-Xchange monitors the replication from master to slave for every context/tenant. If data is just written to the master and it is detected that the slave does not have this information yet, a connection to the master is used instead of a connection to the slave to read most actual data. If you encounter a raise in this number it is an indicator that the replication on the database servers becomes more slow. A drawback of that is that the master server faces more load.

## Memory usage

- java.lang:name=Eden Space,type=MemoryPool,Usage = [used=4027408]
- java.lang:name=Survivor Space,type=MemoryPool,Usage = [used=828448]
- java.lang:name=CMS Old Gen,type=MemoryPool,Usage = [used=32447696]

In total those three memory spaces reflect the total usage of non application memory usage for the Java virtual machine. Eden is used for new objects with the youngest lifetime, Survivor for older objects, and Old Gen for the oldest objects. In total this gives you the information how much memory is used for all your sessions. Divided through the number of sessions it gives you the indication of how much memory is used per session.


# Remote JMX Access

Connecting to JMX via remote can be difficult, especially in environments with NAT and firewalls. A simple solution to this would be to use SSH to create a SOCKS tunnel as described [here](https://stackoverflow.com/a/19706256/2944578) and connect through that tunnel. The following assumes the default configuration for `/opt/open-xchange/etc/management.properties`:
 
	JMXPort=9999
	JMXServerPort=-1
	JMXBindAddress=localhost
	JMXLogin=
	JMXPassword=
	JMXPasswordHashAlgorithm=

On the JMX client machine, pick an arbitrary port (e.g. 7777), and create a SOCKS tunnel with SSH ("centos" is the remote user on the target / middleware machine "10.20.28.116"):

	$ ssh -fN -D 7777 centos@10.20.28.116

Clients like JConsole can then connect to the middleware using the standard (localhost) URL and some additional -J parameters. Example:

	$ jconsole -J-DsocksProxyHost=localhost -J-DsocksProxyPort=7777 \
		-J-DsocksNonProxyHosts= service:jmx:rmi:///jndi/rmi://localhost:9999/server

 
# Jolokia HTTP Bridge

JMX MBeans can be accessed using a specific Java network protocol that uses RMI under the hood. To make the interface more convenient to use by non-Java clients, we support [Jolokia](http://www.jolokia.org/) as a remote JMX-Bridge over HTTP. Jolokia is part of the `open-xchange-core` package, no extra packages are needed. It is mandatory to configure username/password to access the interface. Afterwards it becomes available under `http://localhost:8009/monitoring/jolokia`.

## Enable Jolokia

The jolokia properties are documented [here](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=features&feature=Jolokia).

In `etc/jolokia.properties`, enable Jolokia by setting the following properties:

```properties
com.openexchange.jolokia.start = true
com.openexchange.jolokia.user = youruser
com.openexchange.jolokia.password = yourpassword
```
Note that Jolokia will not be enabled when no user/password is set.

You can optionally adjust this setting:

```properties
com.openexchange.jolokia.servlet.name = /monitoring/jolokia
```
If you do, you need to adjust the examples below as well.

## Allow access from other hosts

As Jolokia represents a JMX-Interface it is highly recommended not to forward it to the internet!

This is by default set through the use of `com.openexchange.jolokia.restrict.to.localhost = true` and can be changed by either setting it to false or providing a `jolokia-access.xml` inside `/opt/open-xchange/etc/`

For further information how to setup this file, http://www.jolokia.org/reference/html/security.html is a good start as all those settings are usable.

However, there is an optional configuration possibility in case you want to access the Jolokia interface from other hosts other than `localhost`. This may be very helpful during the development phase of a project. Please be aware that this interface exposes lots of "interesting" data, so if you remove the restriction to localhost, you need to ensure by other means (network setup, firewalls, web server configuration, ...) that no unauthorised access is possible on production systems.

In `etc/jolokia.properties`, set:

```properties
com.openexchange.jolokia.restrict.to.localhost = false
```
In your web server configuration, enable access to the Jolokia servlet. For Apache this is possible by adding a `ProxyPass` directive for each OX host in the cluster:

```apache
 ProxyPass /monitoring/ox1/jolokia http://ox1-ip:8009/monitoring/jolokia
 ProxyPass /monitoring/ox2/jolokia http://ox2-ip:8009/monitoring/jolokia
 ...
```
On a default installation as described by our installation guides, this would be in `proxy_http.conf`.

Reload your apache config and restart the `open-xchange` service for the changes to take effect.


### Jolokia with Munin Scripts

**Deprecation warning: Munin scripts are no longer maintained. Please switch to more modern monitoring clients like [Telegraf](https://www.influxdata.com/time-series-platform/telegraf/) to scrape JMX metrics via Jolokia and ship them to a monitoring system.**

The package `open-xchange-munin-scripts`, which is part of the `backend` software repository, must be installed.

Munin-scripts for Jolokia need an additional perl modules json and lwp, which are set inside the depenecies. Those should be installed automaticly.

If not, please install the following modules:

Debian:

* libwww-perl
* libjson-perl

rpm:

* perl-JSON
* perl-libwww-perl

#### Supplementary notes for distributed munin clients

##### Munin node

The default munin node configuration only allows connections from localhost, which means that munin master has to run on the same host. If there already is a munin master running, that master IP has to be added to the file /etc/munin/munin-node.conf on each node:

```
\# A list of addresses that are allowed to connect.  This must be a
\# regular expression, since Net::Server does not understand CIDR-style
\# network notation unless the perl module Net::CIDR is installed.  You
\# may repeat the allow line as many times as you'd like

allow ^127\.0\.0\.1$
```

##### Munin master

Make sure the munin and apache packages are installed. If only localhost is going to be monitored, the default configuration is sufficient. Other munin nodes can be added in the ``/etc/munin/munin.conf`` file:

```
\# a simple host tree
[localhost.localdomain]
    address 127.0.0.1
    use_node_name yes
By default, the munin monitoring web page is only reached from localhost, other hosts or networks can be added in /etc/apache2/conf.d/munin:

Allow from localhost 127.0.0.0/8 ::1    # the default setting
Allow from 10.99.0.0/8                  # added network
```

The munin webpage is located at [http://yourhost_where_munin_is_running.org/munin](http://yourhost_where_munin_is_running.org/munin).

## Configuring for OX Documents / Documentconverter

OX Documents and Documentconverter monitoring can used by installing the additional packages ``open-xchange-documents-monitoring``. The Documentconverter uses a different port to access monitoring data. The corresponding ``oxJolokiaURL`` is has to be configured with an entry in the settings.

```
[ox_documentconverter*]
env.oxJolokiaUrl http://localhost:8008/monitoring/jolokia
```

See this [article](http://oxpedia.org/wiki/index.php?title=AppSuite:DocumentsMonitoring) for more details.

## Example

This section describes how to access information exposed through JMX by Open-Xchange with the Jolokia JMX-to-HTTP bridge, using "Login Counter" as an example.

On localhost, call:

```bash
$ curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/list > ox.json
```
If you enabled access from other hosts, you can also use a standard web browser. For example open the following URL in a browser:

```
http://<yourserver>/monitoring/ox1/jolokia/list
```
You'll be asked for user name and password through a standard HTTP authentication window.

## Access specific information

The `ox.json` file you created in the last step gives you a complete list on what data is available through this interface.

As an example, the "Login Counter" interface (which is also used by the 
`logincounter` command line tool) is described like this:

```json
"com.openexchange.reporting": {
  "name=Login Counter": {
    "desc": "Information on the management interface of the MBean",
    "op": {
      "getLastLoginTimeStamp": {
        "ret": "java.util.List",
        "desc": "Operation exposed for management",
        "args": [
          {
            "desc": "",
            "name": "p1",
            "type": "int"
          },
          {
            "desc": "",
            "name": "p2",
            "type": "int"
          },
          {
            "desc": "",
            "name": "p3",
            "type": "java.lang.String"
          }
        ]
      },
      "getNumberOfLogins": {
        "ret": "java.util.Map",
        "desc": "Operation exposed for management",
        "args": [
          {
            "desc": "",
            "name": "p1",
            "type": "java.util.Date"
          },
          {
            "desc": "",
            "name": "p2",
            "type": "java.util.Date"
          },
          {
            "desc": "",
            "name": "p3",
            "type": "boolean"
          },
          {
            "desc": "",
            "name": "p4",
            "type": "java.lang.String"
          }
        ]
      }
    }
  }
}
```

A more detailed documentation on how to use the Jolokia interface can be found [here](http://www.jolokia.org/reference/html/protocol.html). Furthermore, [here](http://www.jolokia.org/reference/html/protocol.html#serialization) you can find documentation of the list of datatypes that can be passed as arguments and received in return values.

More information about the parameters `p1`, `p2`, etc. can be found at the [source code](http://oxpedia.org/wiki/index.php?title=SourceCodeAccess).

In the example above, to get the number of logins in a specific timeframe and with a specific client, we need to call the method `getNumberOfLogins` with the parameters `startDate`, `endDate`, `aggregate` and `clientstring`. They correspond to the command line parameters of the `logincounter` command line tool as described [here](http://oxpedia.org/wiki/index.php?title=AppSuite:Logincounter).

In `curl` this call would look like this:

```bash
$ curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/exec/com.openexchange.reporting:name=Login%20Counter/getNumberOfLogins/2015-01-01T00:00:00/2015-01-31T23:59:59/true/open-xchange-appsuite/
```
If you have enabled access to the Jolokia interface from other hosts, the same information can be viewed in any web browser:

```bash
http://<yourserver>/monitoring/ox1/jolokia/exec/com.openexchange.reporting:name=Login%20Counter/getNumberOfLogins/2015-01-01T00:00:00/2015-01-31T23:59:59/true/open-xchange-appsuite/


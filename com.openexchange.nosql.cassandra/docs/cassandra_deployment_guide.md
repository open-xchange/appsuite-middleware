#Deployment Guide for the OX Cassandra Service

##Installation on OX App Suite

###Debian GNU/Linux 8.0

Add the following entry to /etc/apt/sources.list.d/open-xchange.list if not already present:

```bash
deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianJessie/ /

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianJessie/ /
```

and then run

```bash
$ apt-get update
$ apt-get install open-xchange-cassandra
```

###SUSE Linux Enterprise Server 12

Add the package repository using zypper if not already present:

```bash
$ zypper ar https://software.open-xchange.com/products/appsuite/stable/backend/SLE_12 ox
```

If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that the most recent packages get installed:

```bash
$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/SLES12 ox-updates
```

and run

```bash
$ zypper ref
$ zypper in open-xchange-cassandra
```

###RedHat Enterprise Linux 6 / CentOS 6

Start a console and create a software repository file if not already present:

```bash
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL6/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

and run

```bash
$ yum update
$ yum install open-xchange-cassandra
```

###RedHat Enterprise Linux 7 / CentOS 7

Start a console and create a software repository file if not already present:

```bash
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL7/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

and run

```bash
$ yum update
$ yum install open-xchange-cassandra
```

## Configuration

First make sure that you have a running Cassandra cluster. Configuring a Cassandra cluster is outside the scope of this guide. More information on that topic can be found [here](http://cassandra.apache.org/doc/latest/getting_started/index.html).

The most important setting that needs to be configured is:

```bash
com.openexchange.nosql.cassandra.clusterContactPoints
```
It defines the Cassandra seed node(s), i.e. the entry point(s) for the Cassandra cluster. The OX Cassandra service will try to connect to all those seed nodes upon start-up and it will automatically discover the rest of the nodes in the cluster. It is recommended to add more than one node to avoid any down times in case the seed node is temporarily available on OX Cassandra service's start-up.

The second most important setting is:

```bash
com.openexchange.nosql.cassandra.port
```
This setting defines the port on which the CQL native transport listens for clients, i.e. the port on which each Cassandra node in the cluster listens for CQL clients. The OX Cassandra service it uses CQL to perform queries to the cluster. Defaults to ```9042```.

Unless you know what you are doing, the rest of the settings can be left to their default values. For more information about those settings you can see [here](https://documentation.open-xchange.com/7.8.4/middleware/configuration/properties.html#cassandra)

## Monitoring
There are three different metric categories captured during the operation of the OX Cassandra service:

  * Cluster metrics
  * Keyspace metrics
  * Individual node metrics

The cluster metrics capture general information about the operations of the OX Cassandra service with the entire Cassandra cluster. Among others the most useful ones are the amount of open connections towards the cluster, the amount of connection errors, queued tasks, read/write timeouts, retries and ignores.

The second major category is metrics of the keyspaces. This category contains four pieces of information per keyspace, namely the user types that are used with in the keyspace, the replication options for that particular keyspace, the tables and the functions.

The third category provides information about all nodes of the cluster. Information like, the Cassandra version that is installed on the node, the open connections that the OX Cassandra service has to that particular node, the in flight queries (queries that are executing), the amount of trashed connections and the state of the node (either UP or DONW). The nodes are hierarchically structured in a way that can be easily distinguished in which rack and which datacenter they belong to.

The majority of the metrics from all categories are plotted to munin graphs. There are four graphs plotting information about the Cassandra connections, ignores, retries and tasks.

## Logging
Different levels of logging are involved in the OX Cassandra service's loggers `com.openexchange.nosql.cassandra`.
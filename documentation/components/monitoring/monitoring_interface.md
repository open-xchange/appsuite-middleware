---
title: Monitoring Interface
---

# Introduction

The Open-Xchange JMX offers the ability to fetch runtime information of the Java virtual machine, and about the Open-Xchange Groupware backend. This article will give you information about the most common items from a monitoring perspective, and possible alarm trigger values.

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

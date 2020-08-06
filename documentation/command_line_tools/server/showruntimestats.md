---
title: showruntimestats
icon: far fa-circle
tags: Administration, Command Line tools, Server
package: open-xchange-admin
---

# NAME

showruntimestats - Shows the runtimes stats of this node

# SYNOPSIS

**showruntimestats** [OPTIONS..]

# DESCRIPTION

This command line tool shos runtime-stats of the node the tool runs on.

# OPTIONS

**-h**, **--help**
: Prints a help text

**--environment**
: Show info about commandline environment

**--nonl**
: Remove all newlines (\n) from output

**--responsetimeout** *responsetimeout*
: The response timeout in seconds for reading response from the backend (default 0s; infinite)

**-H**, **--host** *host*
: The specifies the host
          
**-p**, **--port** *port*
: The specifies the port

**-T**, **--timeout** *timeout*
: The timeout in seconds for the connection creation to the backend (default 15s)

**-J**, **--jmxauthuser** *jmxauthuser*
: The jmx username (required when jmx authentication enabled)

**-P**,**--jmxauthpassword** *jmxauthpassword*
: The jmx username (required when jmx authentication enabled)

**-x**, **--xchangestats**
: Shows Open-Xchange stats
    
**--threadpoolstats**
: Shows OX-Server threadpool stats

**-r**, **--runtimestats**
: Shows Java runtime stats    
**-o**, **--osstats**
: Shows operating system stats

**-t**, **--threadingstats**
: Shows threading stats
       
**-a**, **--allstats**
: Shows all stats
             
**-s**, **--showoperations**
: Shows the operations for the registered beans

**-d**, **--dooperation** *operation* 
: Syntax is <canonical object name (the first part from showoperatons)>!<operationname>

**-i**, **--sessionstats**
: Shows the statistics of the session container

**-j**, **--cachestats**
: Shows the statistics of the cache objects

**-u**, **--usmsessionstats**
: Shows the statistics of the USM session container

**-c**, **--clusterstats**
: Shows the cluster statistics

**-g**, **--grizzlystats**
: Shows the grizzly statistics

**--pnsstats**
: Shows the push notification service statistics

**--websocketstats**
: Shows the web socket statistics

**-z**, **--gcstats**
: Shows the gc statistics
     
**-m**, **--memory**
: Shows memory usage of threads

**-M**, **--Memory**
: Shows memory usage of threads including stack traces

**-y**, **--documentconverterstats**
: Shows the documentconverter stats

**-I**,**--imageconverterstats**
: Shows the imageconverter stats

**-f**, **--officestats**
: Shows the office stats
      
**-e**, **--eventstats**
: Shows the OSGi EventAdmin stats

**--generalstats**
: Shows the open-xchange general stats

**--mailinterfacestats**
: Shows the open-xchange mailinterface stats

**--poolingstats**
: Shows the open-xchange pooling stats

**--callmonitorstats**
: Shows admin.monitor Call Monitor stats

**--misc**
: Shows stats for general and threading

**--overview**
: Shows stats for pooling and OperatingSystem

**--memorypool**
: Shows stats for memory pool usage of the Java runtime

**-n**, **--niobufferstats**
: Shows the NIO buffer stats
  

# SEE ALSO

[listserver(1)](listserver), [changeserver(1)](changeserver), [registerserver(1)](registerserver)

# Anti-Virus Infrastructure

## Installation
The node that is going to serve as an ICAP server and Anti-Virus service (Clam AV in this case) needs its respective packages to be installed.

First, ensure that the node is up-to-date by issuing:
```
apt-get update && apt-get upgrade -y
```

Then, install the C-ICAP server and the Clam AV:
```
apt-get install c-icap libc-icap-mod-virus-scan
```

The later package will install the Clam AV alongside its dependencies and its C-ICAP library for binding to the C-ICAP server as an ICAP service.

## Configuration
The C-ICAP server needs some minor configuration. First, edit the `/etc/c-icap/c-icap.conf` file and set the `ServerName` and optionally the `ServerAdmin` directives accordingly, e.g.:
```
ServerName av.io.ox
ServerAdmin foo@io.ox
```
The `ServerName` will be used to all C-ICAP requests when querying the C-ICAP server.

To enable/bind the ClamAV library as a C-ICAP service, all its required is to include the `virus_scan.conf` file in the C-ICAP server's configuration file `/etc/c-icap/c-icap.conf`. At the end of the later add the following line:
```
Include virus_scan.conf
```
The `virus_scan.conf` file should be at the same directory as the `c-icap.conf` file, namely in `/etc/c-icap`.

In the `virus_scan.conf` file the only thing that is required is to uncomment the desired anti-virus engine, in this case the `clamav_mod.conf`
```
Include clamav_mod.conf
```
Note that the `clamad_mod.conf` corresponds to the the daemon of the anti-virus and not the C-ICAP library.

Last, edit the `/etc/default/c-icap` file to enable the C-ICAP daemon to run automatically on start-up and set the following option:
```
START=yes
```

Now, the services can be started. Be sure to have a look at the log files during start-up to identify any potential problems. By default the log files are stored in `/var/log/c-icap` and `/var/log/clamav` for C-ICAP and ClamAV respectively. A simple:
```
tail -F /var/log/c-icap/access.log /var/log/c-icap/server.log /var/log/clamav/freshclam.log
```
ought to do the job.

To (re-)start the services execute:
```
service c-icap restart
service clamav-freshclam restart
```

Note that the C-ICAP server runs by default on TCP port  ```1344```, therefore the firewall rules on the node (if any) need to be adjusted accordingly.

To verify that the port is open, execute:
```
netstat -ntpl | grep ':1344'
```

## Test

The installation/configuration can be tested via TELNET. According to the RFC-3507, all ICAP services MUST implement the OPTIONS method. The OPTIONS method simply gives a hint about the configuration of a particular service. The previously installed C-ICAP server comes by default with the ```echo``` service, which is used to verify that the C-ICAP server is properly configured and operational. 

Though the exact format of the C-ICAP protocol is outside the scope of this guide, worth mentioning is note that all commands issued to a C-ICAP server require to be terminated by two CRLFs (i.e. ENTER).

This can be tested via TELNET as follows:
```
telnet node 1344
```

Sure enough we will be greeted by the standard telnet prompt:
```
Trying node...
Connected to node.
Escape character is '^]'.

```

Now, issue the following command to the TELNET prompt:
```
OPTIONS icap://av.io.ox/echo ICAP/1.0
```
and follow it with two CRLFs (i.e. hit ENTER twice).

Once the OPTIONS method is terminated by two subsequent CRLFs, the C-ICAP server will answer with a similar response:
```
ICAP/1.0 200 OK
Methods: RESPMOD, REQMOD
Service: C-ICAP/0.4.4 server - Echo demo service
ISTag: CI0001-XXXXXXXXX
Transfer-Preview: *
Options-TTL: 3600
Date: Fri, 03 Aug 2018 09:30:31 GMT
Preview: 1024
Allow: 204
X-Include: X-Authenticated-User, X-Authenticated-Groups
Encapsulated: null-body=0
```

To test that the ClamAV C-ICAP service is properly configured and enabled, an OPTIONS command can be issued to that service. The service's name is set by default to `avscan`. This can be configured in the `virus_scan.conf` file with the `ServiceAlias` directive.

```
OPTIONS icap://av.io.ox/echo ICAP/1.0
```

The server should answer with a similar response:
```
ICAP/1.0 200 OK
Methods: RESPMOD, REQMOD
Service: C-ICAP/0.4.4 server - Antivirus service
ISTag: CI0001-AoBbjpzRkMRdpo50KLvKUAAA
Transfer-Preview: *
Options-TTL: 3600
Date: Fri, 03 Aug 2018 09:30:44 GMT
Preview: 1024
Allow: 204
Encapsulated: null-body=0
```

Congratulations, you have a running anti-virus service via a C-ICAP server.

## Reference Setup

A C-ICAP server with ClamAV is already part of the OX infrastructure and runs on our OpenStack environment. The server is reachable under `10.20.28.190`.
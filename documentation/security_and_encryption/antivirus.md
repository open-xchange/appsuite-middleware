---
title: Anti-Virus
classes: toc
icon: fa-bug
tags: Security, Installation, Configuration
---

# Introduction
Since 7.10.2, the middleware is equipped with the ability to preform anti-virus scanning on mail and PIM attachments as well as drive items upon download (only via the AppSuite UI so far; i.e. synced drive items will NOT be scanned). To enable this feature a few new components should be added to your infrastructure.

In a nutshell the middleware is "outsourcing" the anti-virus scans to a remote anti-virus server via  requests based on the [ICAP](https://tools.ietf.org/html/rfc3507) protocol. And those are the two components with which your infrastructure needs to be enhanced for the anti-virus feature to work properly: the ICAP server and an AntiVirus server/daemon which is ICAP-capable.

In the following example we will use [ClamAV](https://www.clamav.net/) as the Anti-Virus service and the default ICAP server most Linux distributions are providing. It is worth mentioning that for increased performance, the ICAP server AND the Anti-Virus service will be installed locally to the OX middleware node, something that will conserve bandwidth usage.

# Installation
Install the following packages via your OS's package manager:

```
open-xchange-antivirus
c-icap
libc-icap-mod-virus-scan
```

The later package will install ClamAV alongside its dependencies and its C-ICAP library for binding to the C-ICAP server as an ICAP service.

# Configuration

## OX
The only thing that needs to be configured on the middleware's side is to actually enable the anti-virus scanning via the `com.openexchange.antivirus.enabled` property, which by default is set to `false`. There are other things that you can configure, such as max file size that is allowed to be scanned or the timeout of the ICAP client. For more information on the configuration have a look [here](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}/#mode=tags&tag=anti-virus).

### Advanced Configuration

#### Maximum File Size
The default maximum allowed file size in mega-bytes that is acceptable for the middleware to scan is set to `100`. You can increase or decrease that value at your leisure by adjusting the value in the `com.openexchange.antivirus.maxFileSize` property. If the file size that is to be scanned exceeds the configured amount, then a warning message will be displayed to the user informing him about the fact and whether he still wants to download the content.

Note that at the moment the entire data stream is fetched twice from the underlying file storage; once to send it to the Anti-Virus service for scanning, and once to deliver it to the user. With that being said, be aware that enabling the Anti-Virus feature _will_ increase the I/O in your file storage servers.

#### ICAP Client Timeout
The connection time-out of the middleware's ICAP client can be configured via the `com.openexchange.icap.client.socketTimeout` property and defaults to 10.000 milliseconds. This property ensures that in case of a third party service disruptions (service down/unreachable/updating/what-have-you) the download of the file will not be block indefinitely and a warning message will be returned to the user instead, prompting him that the file was not scanned due to xyz and it is his decision if he wants to download the content unscanned.

## C-ICAP
The C-ICAP server needs some minor configuration to work properly. First, edit the `/etc/c-icap/c-icap.conf` file and set the `ServerName` and optionally the `ServerAdmin` directives accordingly, e.g.:

```
ServerName av.io.ox
ServerAdmin foo@io.ox
```
The `ServerName` will be used to all C-ICAP requests when querying the C-ICAP server.

To enable/bind the ClamAV library as a C-ICAP service, all its required is to include the `virus_scan.conf` file in the C-ICAP server's configuration file `/etc/c-icap/c-icap.conf`. At the end of the later add the following line:

```
Include virus_scan.conf
```

## ClamAV
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

# Testing

## ICAP/Anti-Virus Infrastructure

The installation/configuration can be tested via TELNET. According to the [RFC-3507]((https://tools.ietf.org/html/rfc3507), all ICAP services MUST implement the OPTIONS method. The OPTIONS method simply gives a hint about the configuration of a particular service. The previously installed C-ICAP server comes by default with the ```echo``` service, which is used to verify that the C-ICAP server is properly configured and operational. 

Though the exact format of the C-ICAP protocol is outside the scope of this guide, worth mentioning is note that all commands issued to a C-ICAP server require to be terminated by two CRLFs (i.e. ENTER).

This can be tested via TELNET as follows:

```
telnet localhost 1344
```

Sure enough you will be greeted by the standard telnet prompt:

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
Date: Mon, 01 Apr 2018 13:37:00 GMT
Preview: 1024
Allow: 204
X-Include: X-Authenticated-User, X-Authenticated-Groups
Encapsulated: null-body=0
```

To test that the ClamAV C-ICAP service is properly configured and enabled, an OPTIONS command can be issued to that service. The service's name is set by default to `avscan`. This can be configured in the `virus_scan.conf` file with the `ServiceAlias` directive.

```
OPTIONS icap://av.io.ox/avscan ICAP/1.0
```

The server should answer with a similar response:

```
ICAP/1.0 200 OK
Methods: RESPMOD, REQMOD
Service: C-ICAP/0.4.4 server - Antivirus service
ISTag: CI0001-AoBbjpzRkMRdpo50KLvKUAAA
Transfer-Preview: *
Options-TTL: 3600
Date: Mon, 01 Apr 2018 13:37:00 GMT
Preview: 1024
Allow: 204
Encapsulated: null-body=0
```

Congratulations, you have a running anti-virus service via a C-ICAP server.

## OX AppSuite
To test that your entire stack is working as expected, you can upload some harmless test viruses on your test user's account (send them either as a mail attachment, or upload them as PIM attachment or in his drive account) and then try downloading them via the AppSuite UI. Harmless test viruses can be found [here](https://www.ikarussecurity.com/support/virus-info/test-viruses/).
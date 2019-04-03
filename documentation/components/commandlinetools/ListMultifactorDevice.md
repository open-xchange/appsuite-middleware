---
title: ListMultifactorDevice
---

# List multifactor authentication devices
This tool lists multifactor authentication devices configured for a specific user.

## Parameters

- ``-A,--adminuser <arg>``<br>
Admin user name for authentication
- ``-P,--adminpass <arg>``<br>
Admin password for authentication
- ``--api-root <arg>``      
URL to an alternative HTTP API endpoint. Example:
'https://192.168.0.1:8443/admin/v1'
- ``-c,--context <arg>``<br>
A valid context identifier
- ``-h,--help``<br>
Prints a help text
- ``-i,--userid <arg>``<br>
A valid user identifier

### Examples

<code>listmultifactordevice -c 10 -i 17 </code>

Lists all devices for user 17 in context 10.

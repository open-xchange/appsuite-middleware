---
title: DeleteMultifactorDevice
---

# Deletes a multifactor authentication device
This tool deletes a multifactor authentication device for a specific user.

## Parameters

- ``--api-root <arg>``      
URL to an alternative HTTP API endpoint. Example:
'https://192.168.0.1:8443/admin/v1'
- ``-c,--context <arg>``<br>
A valid context identifier
- ``-d,--device <arg>``<br>
The ID of the device
- ``-h,--help``<br>
Prints a help text
- ``-i,--userid <arg>``<br>
A valid user identifier
- ``-r,--provider <arg>``<br>
The name of the device's provider
- ``-U,--api-user <arg>``        
Username and password to use for API authentication (user:password).

### Examples

<code>deletemultifactordevice -c 10 -i 17 -p TOTP -d 123 </code>

Deletes the TOTP device "123" for user 17 in context 10. 

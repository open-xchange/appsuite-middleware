---
title: deleteinvisible
---

# Description
The deleteinvisible command line tool can be used to remove not visible data inside a context. Before calling this CLT the module access for all users in the context should be changed to the according permissions. This CLT uses the changed permissions to determine what data is not visible for the context users anymore. Changing permissions can be done by either changing the module access for every single user in the context through the CLT 'changeuser' or by the CLT for changing the module access for an entire context.

*Be careful because the deleted data is lost completely and can not be restored anymore.*

## Parameters

 - ``-A,--adminuser <arg>``<br>
 Admin user name for authentication
 - ``-P,--adminpass <arg>``<br>
 Admin password for authentication
 - ``-c,--context <arg>``<br>
 A valid context identifier
 - `` -N,--contextname <contextname> ``
 A valid context name
 - ``-h,--help``<br>
 Prints a help text
 - ``--environment ``
  Show info about commandline environment
 - `` --nonl   ``
 Remove all newlines (\n) from output
 - ``--responsetimeout <responsetimeout> `` 
 response timeout in seconds for reading response from the backend (default 0s; infinite)
     

### Examples

```
./deleteinvisible -A admin -P secret -c 1

context 4 invisible data deleted
```
Delete the invisible data for a specific context.
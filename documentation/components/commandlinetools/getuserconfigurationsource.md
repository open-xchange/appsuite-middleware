---
title: getuserconfigurationsource
---

# Outputs the current configuration/capabilities for a user

Prints the current configuration and/or capabilities for a user to the console depending on whether ``-a,--user-capabilities`` and/or
``-o,--user-configuration`` is specified.

``-o,--user-configuration`` allows to specify a pattern to look-up certain configuration properties that apply to the user.
A certain property matches the pattern if the pattern ignore-case occurs in the property's name.

In case ``-a,--user-capabilities`` is specified, then all user-associated capabilities are supposed to be printed.
The applicable capabilities are collected from different sources:

 * ``permissions``: Capabilities granted/denied as per a user's permissions
 * ``configuration``: Capabilities granted/denied as per configuration (config-cascade)
 * ``provisioning``: Capabilities granted/denied as per provisioning tools (via ``"capabilities-to-add"``, ``"capabilities-to-remove"`` and ``"capabilities-to-drop"`` command-line options)
 * ``programmatic``: Capabilities granted/denied as per application logic (e.g. availability of a certain services)

## Parameters

 - ``-A,--adminuser <arg>``<br>
 Admin user name for authentication
 - ``-a,--user-capabilities``<br>
 Signals to output the capabilities associated with the given user.
 - ``-c,--context <arg>``<br>
 A valid context identifier
 - ``-h,--help``<br>
 Prints a help text
 - ``-i,--userid <arg>``<br>
 A valid user identifier
 - ``-o,--user-configuration <arg>``<br>
 Specifies the pattern for the configuration option/s (associated with the given user) that are supposed to be printed.
 - ``-P,--adminpass <arg>``<br>
 Admin password for authentication
 - ``-p,--port <arg>``<br>
 The optional JMX port (default:9999)
 - ``--responsetimeout <arg>``<br>
 The optional response timeout in seconds when reading data from server (default: 0s; infinite)
 - ``-s,--server <arg>``<br>
 The optional RMI server (default: localhost)

### Examples

```
./getuserconfigurationsource -A admin -P secret -c 1 -i 2 --user-capabilities
```
Lists all available capabilities distinguished by capability source

```
./getuserconfigurationsource -A admin -P secret -c 1 -i 2 --user-configuration com.openexchange.mail
```
Lists all available properties having "com.openexchange.mail" in their name.

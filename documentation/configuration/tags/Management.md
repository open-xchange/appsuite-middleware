---
title: Management
---

This page shows all properties with the tag: Management

| __Key__ | JMXPort |
|:----------------|:--------|
| __Description__ | Define the port for the RMI Registry.<br> |
| __Default__ | 9999 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RMI.html">RMI</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | management.properties |

---
| __Key__ | JMXServerPort |
|:----------------|:--------|
| __Description__ | Define the JMX RMI Connector Server port. Typically chosen randomly by JVM.<br>-1 means that the port is randomly determined by JVM.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RMI.html">RMI</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | management.properties |

---
| __Key__ | JMXBindAddress |
|:----------------|:--------|
| __Description__ | Define the bind address for JMX agent.<br>Use value "\*" to let the JMX monitor bind to all interfaces.<br> |
| __Default__ | localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Monitoring.html">Monitoring</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | management.properties |

---
| __Key__ | JMXLogin |
|:----------------|:--------|
| __Description__ | Define the JMX login for authentication.<br>Leaving this property empty means not to use authentication.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | management.properties |

---
| __Key__ | JMXPassword |
|:----------------|:--------|
| __Description__ | Define the JMX password in SHA hashed version.<br>This property only has effect if property "JMXLogin" is set.<br><br>======================================================================<br>             Using Perl to generate the SHA hash<br>======================================================================<br><br>The following Perl command can be used to generate such a password:<br>(requires to install the Digest::SHA1 Perl module)<br><br>  perl -M'Digest::SHA1 qw(sha1_base64)' -e 'print sha1_base64("YOURSECRET")."=\n";'<br><br>NOTE:<br>Since Debian Wheezy and Ubuntu 12.04 the corresponding Perl module has been replaced with "Digest::SHA" (and "Digest::SHA1" is no longer maintained)<br><br>======================================================================<br>             Using ruby to generate the SHA hash<br>======================================================================<br><br>Alternatively, ruby can be used to generate the appropriate SHA1 hash:<br><br>  ruby -rdigest -e 'puts Digest::SHA1.base64digest("YOURSECRET")'<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | JMXLogin |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | management.properties |

---

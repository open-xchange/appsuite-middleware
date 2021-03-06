---
title: Security Manager
icon: fa-link
tags: Security, Configuration
---

The OX Security Manager utilizes the java security manager, along with the OSGI security manager, to help prevent abnormal or unauthorized access to system resources.  The primary focus currently is preventing any file system access that isn't required for normal appsuite functionality

# Enabling Security Manager

The security manager must be started using JAVA parameters specified in ox-scriptconf.sh

```
-Dorg.osgi.framework.security=osgi -Djava.security.policy=/opt/open-xchange/etc/all.policy -Dopenexchange.security.policy=/opt/open-xchange/etc/security/policies.policy -Duser.dir=/opt/open-xchange/bundles -Djna.platform.library.path=/usr/lib/x86_64-linux-gnu:/usr/lib64
```

To enable the OX Security Manager, you must edit the ox-scriptconf.sh file located in /opt/open-xchange/etc directory. The following line starting with JAVA\_OPTS\_SECURITY should be commented out in the default installation.  Remove the # at the beginning of the line to activate the security manager, then restart the middleware.

In many installations, this is the only step that will be required.

When fully loaded, the Security Manager will test to see if it can read from the root directory. This should be blocked if everything is installed properly. When operating correctly, there will be one line in the logs during startup (INFO level), that reads: “Security manager started successfully and functioning"

## Startup Configuration Details

The parameters are as follows

```
-Dorg.osgi.framework.security=osgi
```
This specifies the security framework, which should be OSGI.  OSGI framework extends the basic java security manager, adding additional functionality such as the ability to specify actions to reject.

```
-Djava.security.policy=/opt/open-xchange/etc/all.policy
```
This all.policy file is loaded for the java security manager.  This policy file should allow all, and shouldn't be changed.  This is required for the OSGI service to fully start up.  The OSGI security manager will be the service denying unauthorized access.  If this file is not included, the OSGI framework will not be able to start properly.

```
-Dopenexchange.security.policy=/opt/open-xchange/etc/policies.policy
```
This the security policy file loaded for the OSGI security manager.  The format of this file will be specified below.  This file is loaded very early during startup, and should permit access to the OX configuration files and well as the java temporary storage.  Additional restrictions can be added to this file.  The order of rules is important, and one of the final rules should be a general denial of the file system.

```
-Duser.dir=/opt/open-xchange/bundles
```
The user.dir specifies the java working directory to be whitelisted

```
-Djna.platform.library.path=/usr/lib/x86_64-linux-gnu:/usr/lib64
```
Specifies the lookup path for jna.platform library.  Required for hunspell.

# Configuring Security Manager

## Configuration file loading

The majority of file system whitelisting will be done by checking existing configuration files.  This minimizes the amount of configuration required.

During startup, the .list files are parsed from the security configuration directory (for example /opt/open-xchange/etc/security).  These files contains a list of configuration settings to use to create a whitelist of files and directories the middleware should be allowed to access.  These files will come with the basic settings required for appsuite function as well as individual installed components (Guard, Documents, etc).  Additional .list files may be created to whitelist additional directories and files.

Sample configuration

```
# DocumentConverter
com.openexchange.documentconverter.scratchDir:RW
com.openexchange.documentconverter.installDir
file:com.openexchange.documentconverter.blacklistFile
```
The format is [file:]configuration[permissions] with the items in brackets optional.  If the line entry begins with "file:", then the configuration is going to specify a file only, and not authorize the entire directory.
Default permission is read-only.  If write is required, ":RW" or ":W" should be appended to the configuration

Java and environment variables may be used. To specify a java variable, use ${var}. To specify a environment variable, use $var.

Examples: 

```
${jna.platform.library.path} 
$PATH
```
Additional .list files may be added to the security directory.

**Do not edit the shipped .list files, as they will be over-written during updates. If you need to add additional configuration parameters, please create new .list files**


## Policy File Configuration

The policy file comes pre-populated with rules that are required for the initial appsuite startup.  This includes authentication for the configuration directory.  This file follows the OSGI specification [OSGI Core](https://osgi.org/specification/osgi.core/7.0.0/service.condpermadmin.html#i1716478)

The basic structure is

```
policy      ::= access '{' conditions permissions '}' name?
access      ::= 'ALLOW' | 'DENY'       // case insensitive
conditions  ::= ( '[' qname quoted-string* ']' )*
permissions ::= ( '(' qname (quoted-string
                         quoted-string?)? ')' )+
name        ::= quoted-string
```
Java variables may be added by using ${variable.name} such as ${java.home}.  All names MUST be unique

```
ALLOW {
    ( java.io.FilePermission "${openexchange.propdir}${/}-" "READ" )
} "Allow access to configuration"

DENY {
    ( java.io.FilePermission "/-" "READ,WRITE" )
} "Deny access to any other folders"

```
Order DOES matter.  Here, the midddleware is allowed to read from the propdir, but otherwise is denied.

This policy file is loaded very early during startup.  It is used before the configuration files are parsed as above.  The bulk of the directory whitelisting will be done trough the configuration files.  But this file may require modifications for atypical directories, such as java temporary directory, or \proc for cpu monitoring

### Conditions
Currently, the only condition implemented is BundleCondition, which is based on the name of the bundle trying to perform the action.  The entire bundle stack must be authorized.
Example:

```
ALLOW { [ com.openexchange.security.conditions.BundleCondition "com.openexchange.documentconverter.server" "com.openexchange.osgi" ]
    ( java.io.FilePermission "/proc/-" "READ" )
} "Allow access to cpuInfo"
```

This allows documentconverter.server to read from /proc/.  The com.openexchange.osgi bundle is on the stack, so must be authenticated also.  All other bundles will be denied permission to read from the directory unless otherwise authorized.






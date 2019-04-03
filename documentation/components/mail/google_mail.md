---
title: Google Mail
classes: toc
---

# Preparations

At first, install the UI plugin `open-xchange-gui-mail-accounts-plugin` from the update tree. The plugin will install into the default UI plugin folder, e.g. `/var/www/ox6/plugins/com.openexchange.mail.accounts/` on debian. This plugin has no server component. It's registered by automatically adding its name to `plugins/static.conf` during post installation. UI updates do not replace this file.

## OS Repositories
 
### Debian GNU/Linux 8.0

Add the following entry to `/etc/apt/sources.list.d/open-xchange.list` if not already present:

```
deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianJessie/ /

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianJessie/ /
```

### Debian GNU/Linux 9.0

Add the following entry to `/etc/apt/sources.list.d/open-xchange.list` if not already present:

```
deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianStretch/ /

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianStretch/ /
```

### SUSE Linux Enterprise Server 12

Add the package repository using `zypper` if not already present:

```
$ zypper ar https://software.open-xchange.com/products/appsuite/stable/backend/SLE_12 ox
If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that the most recent packages get installed:

$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/SLES11 ox-updates
```

### RedHat Enterprise Linux 6

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL6/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

### RedHat Enterprise Linux 7

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL7/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

### CentOS 6

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL6/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

### CentOS 7

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL7/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

## Install on OX App Suite

Refer to [OS Repositories](#os-repositories) on how to configure the software repository of your operating system.

### Debian GNU/Linux 8.0/9.0

```
$ apt-get update
$ apt-get install open-xchange-gui-mail-accounts-plugin
```

### SUSE Linux Enterprise Server 12

```
$ zypper ref
$ zypper in open-xchange-gui-mail-accounts-plugin
```

### RedHat Enterprise Linux 6/7 and CentOS 6/7

```
$ yum update
$ yum install open-xchange-gui-mail-accounts-plugin
```

# Define Services

Inside the plugin folder (see above), you will find the configuration file `services/config.js`. This file contains all services that you want to provide to your customers. Syntactically it's plain JavaScript and from the syntax perspective it's quite self-explaining. Here's an example of how to define just one single service, in this case it's "*GoogleMail*":

```
ox.mail.services = {
   gmail: {
       // Google Mail
       name: "Google Mail",
       domains: ["gmail.com", "googlemail.com"],
       products: {
           imap: {
               name: "IMAP",
               transport_server: "smtp.gmail.com",
               mail_protocol: "imap",
               mail_server: "imap.gmail.com"
           },
           pop: {
               name: "POP3",
               transport_server: "smtp.gmail.com",
               mail_server: "pop.gmail.com"
           }
       },
       description: "In order to use your GMail account, you have to enable ....", /*i18n*/
       logo: {
           src: "gmail.png",
           height: "59px"
       }
   }
};
```

Each service has an unique id ("*gmail*"), a name which appears in the UI ("*Google Mail*"), a list of domains this service supports ("*gmail.com*" and "*googlemail.com*"), a description (for example help the user at figuring out how to enable POP3), as well as a logo and the colour of the name (e.g. #E20074 for telecom magenta).

Moreover, each service has products instead of just differentiating IMAP from POP3. Some hosters have a huge set of different types of mail accounts (free, secure, fast, premium, business etc.) independent of the technical perspective (i.e. IMAP or POP3).

Each product has an unique id. The following fields are available:

| **Field**  | **Description / Defaults**  |
|---|---|
| name                | appears in the UI   |
| transport_protocol  | default is smtp     |
| transport_server    | IP or domain        |
| transport_port      | 25 or 465 (SSL)     |
| transport_auth      | use authentication  |
| transport_auth_port | cut off domain part, e.g. "me" instead of "me@domain.tld" |
| transport_secure    | use SSL  |
| mail_protocol       | "imap" or "pop3", defaults to "pop3"  |
| mail_server         | IP or domain  |
| mail_port           | defaults: 110 (pop3), 995 (pop3/ssl), 143 (imap), 993 (imap/ssl)  |
| mail_auth_short     | same as transport_auth_short  |
| mail_secure         | use SSL  |

# Enable Specific Services Only / Cascading Config

Furthermore, the plugin will look for a particular configuration path, that is `ui/mail/services/enabled`. If this path exists, you can control which services are enabled by the backend. In order to do so, you can add the following to `/opt/open-xchange/etc/groupware/settings/ui.properties` , for example:

```
ui/mail/services/enabled/gmail = true
ui/mail/services/enabled/t_online = true
```

In this case only two services are enabled. All other services that might be defined in `/<plugindir>/services/config.js` will be ignored. This configuration supports the Cascading-Configuration, i.e. you can set values for different contexts or context groups. This, of course, requires a proper YAML file.
    

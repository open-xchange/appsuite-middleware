---
Title: Command Line Tool Documentation
---

This document provides extended informations about some of the command line tools of the OX middleware.


# List capabilities and configurations

The OX middleware provides this clt's to list capabilities or configurations:

* getuserconfigurationsource
* getusercapabilities
* getcontextcapabilities

The clt's getusercapabilites and getcontextcapbilites either list only the provisioned capabilities on user or context level.
The getuserconfigurationsource clt is much more powerful. It allows to list both capabilities and configurations.
For the usecase of capabilities it lists the complete configuration of capabilities. For this matter it lists the capabilities on a source based level.
Currenlty there are four source:

* permission
* configuration
* provisioning
* programmatic

For each source it shows which capabilities are added by this source and which capabilities are revoked.
In addition it lists the active capabilities for the user at the end of the output.

The second use case of this clt is to list a filtered list of configurations.
For example the following command would list all imap configurations for the given user:

    getuserconfigurationsource -A <ctxadmin> -P <password> -c <cid> -u <uid> -o com.openexchange.imap

Besides the name of the configuration and its value the clt also displays the scope of the configuration.

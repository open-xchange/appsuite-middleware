---
title: OX Display
---

[OX Display](http://oxpedia.org/wiki/index.php?title=AppSuite:OX_Monetization) advertising plugins can be configured by making use of server-side configuration. For that, App Suite Middleware offers an administrative API to set the according configuration and App Suite UI can fetch and use that configuration through [App Suite HTTP API](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#!/Advertisement/getAdvertisementConfig).

Whether server-side configuration is an option, depends on the specific [advertisement plugin](https://documentation.open-xchange.com/components/display/1.4.1/articles/).


## Tenants and Packages

The server-side advertisement configuration has been designed in a way, that it supports multi-tenancy and multi-package setups.

**Multi-tenancy:** A single App Suite deployment that serves multiple isolated customers through the [reseller plugin](http://oxpedia.org/wiki/index.php?title=Reseller_Bundle). In the following, the terms reseller, brand and tenant are interchangeable.

**Multi-package:** Different pricings/feature sets. Think of free and paid accounts with the latter having less or no advertisement, while the former are subject to advertisement revenue alone.


## Installation and Configuration

As a first step, install the `open-xchange-advertisement` package on all middleware nodes. Second, all nodes need to be equipped with a piece of configuration, that defines resellers/tenants, packages and the way to determine a user's package ("package scheme").

Configuration takes place in file `/opt/open-xchange/etc/advertisement.properties`. Per default that file doesn't exist and server-side advertisement configuration is unavailable for all users. Configuration options are described [here](/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.advertisement). Configuration changes can be applied without server restart with `/opt/open-xchange/sbin/reloadconfiguration`.


### Global Package Scheme

The global scheme is the default scheme. It assumes that there are no (or just one) reseller and just one package. It uses the value `default` for both reseller and package.


### Access Combination Package Scheme

This scheme uses access combination names as package names. The name is determined by the provisioned module accesses of the user, see `ModuleAccessDefinitions.properties`. An example package name would be `groupware_premium`, referring to all users within a tenant that have the according access combination set.


### Taxonomy/Types Package Scheme

This scheme is based on [context taxonomies](http://oxpedia.org/wiki/index.php?title=ConfigCascade#Core_Concepts_-_Context_Taxonomy). It requires a unique taxonomy/type per context, referring to the actual package. For example, each context tagged with `--taxonomy/types=freemail` would be configured by storing an advertisement configuration for package `freemail` of the according tenant.


## Provisioning

Advertisement configuration per tenant and package can be set through an [administrative API](https://documentation.open-xchange.com/components/middleware/rest/7.10.1/index.html?version=7.10.1#tag/Advertisement).

Without the reseller plugin installed, the reseller name is always `default`. With package scheme `Global`, the only available package is always `default`.


### Preview

To support previewing configuration changes before making them effective for all users of a package, the API contains calls to set and delete the advertisement configuration for a certain user, based on its context ID and user ID. The idea behind that is, to create a preview user per package and tenant. Configuration changes would then in a first step be set for the according preview user alone. After verifying that the configuration works as expected, it can be rolled out to the whole package, using the respective API call.


### Command-Line Tools

#### removeadvertisementconfigs

This tool allows the master admin to remove the advertisement configurations. He can either remove all or a single reseller configurations.
It is also possible to remove only the resellers, which are not active anymore (e.g in case they are deleted).

**Help text:**

    -A,--adminuser <arg>         Admin username
    -c,--clean                   If set the clt only removes configurations
                                 of resellers which doesn't exist any more.
    -h,--help                    Prints a help text
    -i,--includePreviews         If set the clt also removes preview
                                 configurations. This is only applicable in
                                 case the argument 'clean' is used.
    -P,--adminpass <arg>         Admin password
    -p,--port <arg>              The optional RMI port (default:1099)
    -r,--reseller <arg>          Defines the reseller for which the
                                 configurations should be deleted. Use
                                 'default' for the default reseller or in
                                 case no reseller are defined. If missing all
                                 configurations are deleted instead.
       --responsetimeout <arg>   The optional response timeout in seconds
                                 when reading data from server (default: 0s;
                                 infinite)
    -s,--server <arg>            The optional RMI server (default: localhost)



## Implementation Details

The configurations will be stored as JSON strings within ConfigDB in the table `advertisement_config`. Additional there is another table with the name `advertisement_mapping` which contains a mapping from a pair of 
reseller and package to an advertisement config. Here reseller references the name of the reseller and package references a package name. In case there is no reseller or in case of a single package, the entry can be replaced by the value `default`.

For example if you have no reseller and two packages with the names "packA" and "packB" you use "default" for the reseller and either "packA" or "packB" for the package. 
Which packages are available depends on the configuration of the middleware. Furmost it depends on which package scheme is configured.



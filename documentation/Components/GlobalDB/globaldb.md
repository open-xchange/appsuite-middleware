Cross-context database
======================

For storing data acrosss context boundaries, a so called "global" database is used by the server. For example, such shared data could be information about guest users or data for registered OAuth applications. This article gives an overview about the basic concepts and how to setup a global database.

Register databases
------------------

As a prerequisite, a global database needs to be registered in the Open-Xchange server's configuration database. At the storage layer, global databases are handled just like the oridnary `context` databases storing all the existing groupware data. Therefore, the same underlying technologies can be used for replication like master/slave- or Galera-based setups, as well as registering additional database pools for sharding in very large installations.

So, much similar to the groupware databases that store context-internal data, a new global database can be registered using the `registerdatabase` commandline tool. To prevent the registered database being picked up for groupware data, set the `maxunit` argument to `0`:

`$ ./registerdatabase -A oxadminmaster -P secret --name oxglobal --hostname 10.20.30.217 --dbuser openexchange --dbpasswd secret --master true --maxunit 0`
`database 8 registered`

Remember the returned database identifier, `8` in the above example, we'll need it at a later stage.

Optionally, in case a database slave should be added, you can register it afterwards by specifying the database identifier as `masterid` like follows:

`$ ./registerdatabase -A oxadminmaster -P secret --name oxglobal_slave --hostname 10.20.30.219 --dbuser openexchange --dbpasswd secret --master false  --maxunit 0 --masterid=8`
`database 9 registered`

For smaller or test environments, it's also possible to re-use a previously registered groupware database for cross-context data, i.e. any database marked as `master` by the `listdatabase` utiltiy may be used. Please note that all cross-context data is not accounted when calculating the database weight during context creation.

Context Groups
--------------

As already mentioned, data inside the global database is shared between and available to all contexts in the installation. For setups serving multiple different "brands" or "domains", this may not be the desired behavior, so that an additional level of data separation is needed for cross-context data. Therefore, one or more contexts can be classified into a specific "group". The association with the group is done by assigning the property `com.openexchange.context.group` to a context via config cascade. A context may only be part of one group, contexts without group association automatically fall into the "default" group.

As an example, a hoster is selling his hosted e-mail services under two different brands, called "Clever Hosting" and "Smart Hosting". Each customer that registered an account at "Clever Hosting" now is put into the context group "clever\_hosting" by setting the property `com.openexchange.context.group` at any level of the config cascade, e.g. during provisioning when creating the context like:

`$ ./createcontext [...] --config/com.openexchange.context.group=clever_hosting`

Or, if you already tagged your contexts with different taxonomy types (like `clever` and `smart` in our example), the assignment to a context group can also be achieved at "ContextSet" level inside an `.yml`-file of your `contextSets` definitions:

    clever_group:
        withTags: clever
        com.openexchange.context.group: clever_hosting
        ui/product/name: "Clever Hosting"
        [...]
     
    smart_group:
        withTags: smart
        com.openexchange.context.group: smart_hosting
        ui/product/name: "Smart Hosting"
        [...]

In the global database, the context group identifier serves as differentiating key for various data. For example, data of a guest user that was invited to a share in one context of a group can be used throughout all other contexts of the same group. Or, there may be a different set of registered OAuth applications available for each context group. Having contexts separated into different groups also allows to use different global databases as defined in the additional sections of the configuration file `globaldb.yml`. The next chapter describes the global database configuration file in more detail.

Configure databases
-------------------

Global databases are defined in the configuration file `globaldb.yml`. It mainly serves the purpose to map registered global databases to context groups. In case no advanced context grouping is necessary, e.g. because there's only a single brand in the installation, it's sufficient to supply the identifier of the previously registered master database, see [Register databases](#Register_databases "wikilink") above, inside the `default` section of the configuration file. For example, if the identifier of the global database master is `8`, the section would look like:

    default:
        groups: [default]
        id: 8

This would lead to all cross-context data being stored at this database, assigning distinguished context groups as described in [Context Groups](#Context_Groups "wikilink") is not required.

If there are multiple context groups, those can be directed to a specific global database by adding their names to the `groups` property of a configuration section. For example, if the groups `smart_hosting` and `clever_hosting` should use database `8`, too, one could define:

    default:
        groups: [default,smart_hosting,clever_hosting]
        id: 8

Removing the default group name `default` from the groups list is not recommended, and should only be done if you can ensure that each context of the installation has it's group assignment defined correctly in the property `com.openexchange.context.group`.

Having contexts separated into different groups also allows to use different global databases as defined in the additional sections of the configuration file `globaldb.yml`. Since context groups are bound to a configuration section by specifying the group name in the "groups" array, and each configuration section may target another global database, in theory there could be as much global databases as there are defined context groups. For example, to enforce data from the context groups `smart_hosting` and `clever_hosting` being stored at separate databases, the following configuration sections would route them to the database identifiers `8` and `14` respectively:

    clever:
        groups: [clever_hosting]
        id: 8

    smart:
        groups: [smart_hosting]
        id: 14

Reload global db configuration
------------------------------

After updating the globaldb.yml you have to reload the server configuration by using /opt/open-xchange/sbin/reloadconfiguration command line tool.

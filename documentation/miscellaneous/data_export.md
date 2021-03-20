---
title: Data export
icon: fa-file-export
tags: Configuration, Installation
---

The data export feature allows users to submit a data export of his/her personal data packed into one or more ZIP archives. The actual export is a background task that completes at any time in future. Once ready, the user is notified via E-Mail that he/she is now able to download the packages.

As mentioned before, a data export is designed as a background task. When triggered by the user, it collects user-related data, which can be a lengthy as well as an I/O- and compute-intensive operation. For this reason, the data export feature provides several options to distribute and schedule this operation as best as possible. The most important options for this, which are also mentioned and presented below:

* Only certain nodes handle the processing of triggered data exports via `com.openexchange.gdpr.dataexport.active`. These could be special nodes in a cluster not serving regular calls.
* Processing of data exports takes place only at certain times via `com.openexchange.gdpr.dataexport.schedule`
* The number of data exports processed in parallel is controlled by `com.openexchange.gdpr.dataexport.numberOfConcurrentTasks`

Since the artefacts and result files are typically quite big files, the configuration for the data export feature requires to specify a dedicated file storage, which is used to manage those big files. Thus common file storage associated with a context or user is not burdened and quota is unaffected as well.

Furthermore, scheduling of data export tasks within an Open-Xchange installation is a global task. Therefore, the common payload databases are not suitable to store data export information since determination execution of the next adequate data export task requires to look-up every payload database on every registered database host. Therefore, the data export feature requires setup of the global database to have a central database for fast and easy look-up.

Currently, the Open-Xchange Middleware ships with 5 standard providers for the data export:

* E-Mails
* Calendar
* Contacts
* Tasks
* Drive

# Prerequisites

* A special transport must be configured for data export notifications. This transport is configured in <code>noreply.properties</code>. All properties therein are config-cascade capable, so their values can be sensitive to the current user or context.
* Choose the appropriate file storage to use for storing export artefacts and result files (both **not** accounting to quota)
 * Either register a dedicated file storage via `registerfilestore` or
 * Select an existing file storage having enough capacities since artefacts/result files are typically quite big files
* Enable global database as explained [here]({{ site.baseurl }}/middleware/administration/global_database.html)

# Installation and Configuration

As a first step, install the `open-xchange-gdpr-dataexport` package on all middleware nodes.

Once installed, set the following configuration properties

* `com.openexchange.gdpr.dataexport.fileStorageId` Specifies the identifier of the file storage to use. This configuration property is config-cascade aware.
* `com.openexchange.gdpr.dataexport.enabled` Whether the feature is available for a user. This configuration property is config-cascade aware.
* `com.openexchange.gdpr.dataexport.active` Whether processing of data export tasks is enabled in this node.
* `com.openexchange.gdpr.dataexport.schedule` The pattern specifying when processing of data export tasks is allowed. A comma-separated listing of tokens, while a token consists of a weekday and an optional time range
A weekday is one of: `Mon`, `Tue`, `Wed`, `Thu`, `Fri`, `Sat` and `Sun`. It also accepts ranges e.g. `Mon-Fri`
A time range is a hour of day range of the 24h clock. E.g. `0-6` for 0 to 6 in the morning or `22:30-24` for 22:30h until 24h in the evening. Also accepts comma-separated pairs; e.g. `0-6,22:30-24`
A more complex example: `Mon 0:12-6:45; Tue-Thu 0-7:15; Fri 0-6,22:30-24; Sat,Sun 0-8`
* `com.openexchange.gdpr.dataexport.numberOfConcurrentTasks` How many task are allowed being processed concurrently on a node

Please find further configuration properties in the [configuration documentation section](https://documentation.open-xchange.com/components/middleware/config/develop/#mode=search&term=com.openexchange.gdpr.dataexport)

# Command-line tools

The data export feature ships with a set of command-line tools to

* List the status of submitted data export tasks. Either all, by context or by user
* Request cancelation for certain data export tasks by user or context
* Check and repair consistency of the associated file storage

## List data export tasks via `listdataexports`

Usage

```
listdataexports [-c <contextId>] [-i <userId>] -A <masterAdmin | contextAdmin> -P <masterAdminPassword |
                       contextAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] [--responsetimeout <responseTimeout>] |
                       [-h]
 -A,--adminuser <adminUser>       Admin username
 -c,--context <contextId>         The context identifier. If context identifier is given, only data export tasks
                                  associated with denoted context are listed; otherwise all data export tasks are
                                  listed.
 -h,--help                        Prints this help text
 -i,--userid <userId>             The user identifier. If user identifier is given, only the data export task associated
                                  with denoted user is listed. This user-sensitive output also includes the
                                  task-associated work items and result files.
 -p,--port <rmiPort>              The optional RMI port (default:1099)
 -P,--adminpass <adminPassword>   Admin password
    --responsetimeout <timeout>   The optional response timeout in seconds when reading data from server (default: 0s;
                                  infinite)
 -s,--server <rmiHost>            The optional RMI server (default: localhost)

```

### Examples

List all data exports:

`$ listdataexports -A oxadminmaster -P secret`


List all data exports of a certain context:

`$ listdataexports -c 1234 -A oxadminmaster -P secret`

List the data export of a certain user. This outputs includes task-associated work items and result files (if any):

`$ listdataexports -c 1234 -i 3 -A oxadminmaster -P secret`

## Request cancelation for data export tasks via `canceldataexports`

Usage

```
canceldataexports -c <contextId> [-i <userId>] -A <masterAdmin | contextAdmin> -P <masterAdminPassword |
                         contextAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] [--responsetimeout <responseTimeout>] |
                         [-h]
 -A,--adminuser <adminUser>       Admin username
 -c,--context <contextId>         The context identifier. If only context identifier is given, all data export tasks
                                  associated with denoted context are requested for being canceled
 -h,--help                        Prints this help text
 -i,--userid <userId>             The user identifier. If this option is set, only the data export task associated with
                                  that user is canceled
 -p,--port <rmiPort>              The optional RMI port (default:1099)
 -P,--adminpass <adminPassword>   Admin password
    --responsetimeout <timeout>   The optional response timeout in seconds when reading data from server (default: 0s;
                                  infinite)
 -s,--server <rmiHost>            The optional RMI server (default: localhost)
```

### Examples

Request to cancel all data exports for a certain context:

`$ canceldataexports -c 1234 -A oxadminmaster -P secret`

Request to cancel the data export for a certain user

`$ canceldataexports -c 1234 -i 3 -A oxadminmaster -P secret`

## List (& repair) orphaned file storage entities or references to non-existent file storage entities via `dataexportconsistency`

Usage

```
dataexportconsistency -f <filestoreId_1> ... [-r] -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s
                             <RMI-Server] [--responsetimeout <responseTimeout>] | [-h]
 -A,--adminuser <adminUser>       Admin username
 -f,--filestores <filestoreIds>   Accepts one or more file storage identifiers
 -h,--help                        Prints this help text
 -p,--port <rmiPort>              The optional RMI port (default:1099)
 -P,--adminpass <adminPassword>   Admin password
 -r,--repair                      Repairs orphaned files and export task items.
    --responsetimeout <timeout>   The optional response timeout in seconds when reading data from server (default: 0s;
                                  infinite)
 -s,--server <rmiHost>            The optional RMI server (default: localhost)

```
 
### Examples

List orphaned file storage entities or references to non-existent file storage entities:

`$ dataexportconsistency -f 34 -A oxadminmaster -P secret`

Repair orphaned file storage entities or references to non-existent file storage entities:

`$ dataexportconsistency -f 34 --repair -A oxadminmaster -P secret`

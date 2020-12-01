---
title: oxsysreport
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

oxsysreport - Creates an OX support tarball

# SYNOPSIS

**oxsysreport** [OPTION VALUE]...

# DESCRIPTION

The `/opt/open-xchange/sbin/oxsysreport` tool will collect a lot of useful log and system information into a tar archive. This so called OX Support Tarball contains Apache and OX configurations, OX logs and system information. This and a qualified error report makes sure that our support department get all needed details for fast effective help. We always recommend to encrypt your e-mail with our Support Key when attaching this tarball.

# OPTIONS

**--tempdir** *path*
: The temporary path for storing all collected files for the tarball. If, e.g. due to space limitations, you need to switch from `/tmp` to a different drive for collecting and processing the data, you can choose an alternative but existing path here. Default `/tmp`.

**--logdir** *folder_name*
: The output log directory name. We do not recommend to use this option, but you can choose the output log directory name which is also root folder of the OX Support Tarball. Please note, this temporary folder will be removed by default and you need to choose an not existing directory name.

**--log-archive** *tarball_path*
: The tarball path. This allows to choose an alternative tarball name and storage location. Please note, the script will check for and break on already existing file arguments. Defaults to `/tmp/ox_support_infos-<timestamp>.tar.gz`.

**--keep-tmp-file** *true/false*
: Whether to keep the temporary files. If you like to observe the data collection of the log directory without unpacking the tarball or to add additional files for repacking, feel free to use this option. The log directory path will be printed at the first lines of the `oxsysreport` output.
This information is valid from 7.6.0 on. Defaults to `false`.

**--thread-dump** *true/false*
: Whether to generate a thread dump. Defaults to `false`. This option normally should only be used in case it is requested by our Support department. The collected thread dumps may help to identify the root cause of long running threads, critical load and very high CPU usage. In case of an incident with very high CPU usage and a lack of resources, please consider the addditional usage of `--skip-compression`.

This option triggers the thread dump creation via `kill -3` and logs the related top output into the `/commands/top -d 3 -bHn 5 (thread-dump)` file. The thread dump on systems other than IBM Java will be stored within the `/var/log/open-xchange/open-xchange-console.log`, on IBM Java they will be stored separately inside the support tarball as `/tmp/javacore.<timestamp>.txt`. This information is valid from 7.6.0 on.

**--heap-dump** *true/false*
: Whether to generate a heap dump. Required tools for this operation are `OX heapdump` or `jmap` and `sudo` (Only if not IBM Java). Defaults to `false`. This option normally should only be used in case it is requested by our Support department. The collected heap dumps could be very helpful in memory or OX service outage incidents. Please keep in mind that you have to call oxsysreport with this option before restarting the service.

First you will get a warning dialog with some system details and with the choice to proceed or to cancel. While creating Java heap dumps, your JVM will be stopped and the OX Service is not functional. This step may take several minutes and you might have to manually restart the `open-xchange service` afterwards. You also have to make sure that you have enough free disk space for processing the data in directories which are defined by `--tempdir` and `--log-archive`. Depending on your max heap size `-Xmx` defined by your ox-scriptconf.sh of your groupware Java Virtual Machine and amount of logs, several gigabyte could be needed!

This option triggers the creation of an heap dump which will be stored on systems other than IBM Java Systems within `/commands/jmap -dump:file (heap-dump).bz2` and `/commands/jmap -histo (heap-dump)`. The two jmap files beside without `(heap-dump)` marker contain the stdout/stderr of jmap by running this command. Please note, the `.bz2` suffix (and compression) will be suppressed by the `--skip-compression` option.

On IBM Java Systems the jmap command is not available and `kill -ABRT` will do the job instead. This will kill the JVM completely by writing the heap dump. Afterwards the dump will be moved within the tarball and processed by the tool jextract. Depending on `--skip-compression` option jextract will compress it as zip or not. The heap dump will be located in `/tmp/core.<timestamp>.dmp.zip` within the tarball. The stdout/stderr of jextract will be written to `/commands/jextract core.<timestamp>.dmp (heap-dump)`. On IBM Java the OX groupware service will be restarted by this script. This information is valid from 7.8.2 on.

**--report-client** *true/false*
: Whether to execute the OX Report Client. The [OXReportClient](https://documentation.open-xchange.com/7.8.3/middleware/components/report_client.html) generates an overview about features and users from your database. On larger installations this tool may take very long and so we disabled it by default.
This information is valid from 7.6.0 on. Defaults to `false`.

**--skip-compression** *true/false*
: Whether to compress the tarball. Please use this option only in case your system is affected by very high CPU usage and the oxsysreport compression stuck by the lack of resources. If this option is set, no compression takes place and this will affect the OX Support Tarball, as well as nested tarballs and possible heap dumps. The resulting tarball can get huge and we recommend to keep the `--exclude-old-logs` option in mind to also drop potentially outdated logs. Please always compress this tarball before handing it over to our support department.
This information is valid from 7.6.0 on. Defaults to `false`.

**--skip-system-tools** *true/false*
: Whether to skip system tools. Defaults to `false`

**--exclude-old-logs** *0...n Days*
: Whether to exclude logs older than the specified day amount. We recommend to use this option if you are able to locate the period of a specific issue or by handing over an follow up tarball. This option will exclude all separate log files beyond the modified period which are stored on your system in `/var/log/open-xchange` or `/var/log/univention`. This option does not affect mandatory files like e.g. `open-xchange-console.log`. If logs are dropped they are listed in the generated overview file `/modified-and-excluded-files.txt` of the tarball.
This information is valid from 7.6.0 on.

**--exclude-ox-secrets** *true/false*
: Whether to remove any confidential details from the OX configuration files. For versions >= 7.6.1 defaults to `true`. For versions below that defaults to `false`. This option removes all unexpected, as well as whole confidential known files from the `/opt/open-xchange/etc/` copy of the support tarball. Also confidential information like passwords and keys will be replaced by a white and blacklist heuristic with a `<REMOVED BY OXSYSREPORT>` marker. The replacement works only on OX properties files and the `/modified-and-excluded-files.txt` file will contain the details of affected files.

**-h**, **--h**
: Prints a help text.

# EXAMPLES

Please keep in mind that the following examples just give a rough overview and arguments should be adapted to your own needs.

**root@server# /opt/open-xchange/sbin/oxsysreport**

This is the default usage.

**root@server# /opt/open-xchange/sbin/oxsysreport --exclude-old-logs 2 --exclude-ox-secrets true**

Exclude all logs older than two days and remove OX configuration secrets.

**root@server# /opt/open-xchange/sbin/oxsysreport --thread-dump true --skip-compression true --exclude-old-logs 0**

Create thread dumps, skip compression and include only today's logs.

**root@server# /opt/open-xchange/sbin/oxsysreport --heap-dump true --tempdir /var/tmp/ --log-archive /root/ox_support_infos-$(date +%Y%m%d-%H%M%S).tar.gz**

Create a heap dump, choose a different storage location for the tarball and process data.

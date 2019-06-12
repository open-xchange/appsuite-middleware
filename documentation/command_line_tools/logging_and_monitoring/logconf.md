---
title: logconf
icon: far fa-circle
tags: Administration, Command Line tools, Monitoring
---

# NAME

logconf - creates log filters for sessions, context and (user/context) tuples, suppresses stacktraces for defined exception categories and dynamically changes log levels of specified loggers.

# SYNOPSIS

**logconf** [OPTION]...

# DESCRIPTION

This command line tool creates log filters for sessions, context and (user/context) tuples, suppresses stacktraces for defined exception categories and dynamically changes log levels of specified loggers. It is also possible to create a filter for a user or a context or a session and set the level of the desired loggers independently. 

# OPTIONS

**-a**, **--add**
: Flag to add the filter. Mutually exclusive with `-d`.

**-d**, **--delete**
: Flag to delete the filter. Mutually exclusive with `-a`.

**-c**, **--context** *contextId*
: The context id for which to enable logging.

**-u**, **--user** *userId*
: The user id for which to enable logging.

**-e**, **--session** *sessionId*
: The session id for which to enable logging.

**-l**, **--level** *level*
: Define the log level (e.g. -l com.openexchange.appsuite=DEBUG). When the -d flag is present the arguments of this switch should be supplied without the level (e.g. -d -l com.openexchange.appsuite).

**-cf**, **--clear-filters**
: Clear all logging filters.

**-la**, **--list-appenders**
: Lists all root appenders and any available statistics.

**-le**, **--list-exception-category**
: Get a list with all supressed exception categories.

**-lf**, **--list-filters**
: Get a list with all logging filters of the system.

**-ll**, **--list-loggers**
: Get a list with all loggers of the system. Can optionally have a list with loggers as arguments, i.e. -ll *logger1 logger2* OR the keyword 'dynamic' that instructs the command line tool to fetch all dynamically modified loggers. Any other keyword is then ignored, and a full list will be retrieved.

**-oec**, **--override-exception-categories** *exceptionCategories*
: Override the exception categories to be suppressed.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost).

**-p**, **--port** *rmiPort*
: The optional RMI port (default:1099).

**-h**, **--help**
: Prints a help text.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# Logging Filters vs. Loggers vs. Exception Categories

As already mentioned in the beginning, with this command line tool you can do multiple things, therefore there are different "categories" of data objects so to speak, i.e. Logging Filters, Loggers and Exception Categories. Hence, specific flags of the tool are explicitly used for only one of these data object categories and nowhere else (with the exception of the `-l` flag):

 * Manage logging filters for: sessions, contexts and context/user tuples. For the above three cases the `-a`, `-d` and `-lf` flags are relevant.
 * Change the log level of already existing loggers of the system. For this case the `-l` and `-ll` flags are relevant.
 * Suppress the stack-traces for defined exception categories. For this case only the `-oec`
  flag is relevant.

# Exception Categories vs. Log Levels

The OX server defines different categories to the OX exceptions that are thrown during the operation. Those categories should not be confused with the logging levels `OFF`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`, `ALL` of the logback framework which are described here.

The following list briefly describes each OX exception category.

 - CAPACITY: The category for a 3rd party system when reporting capacity restrictions, e.g. quota
 - CONFIGURATION: The category for a configuration issue (e.g. missing required property)
 - CONFLICT: The category for conflicting data
 - CONNECTIVITY: The category for a connectivity issue, e.g. broken/lost TCP connection
 - ERROR: The category for an error
 - PERMISSION_DENIED: The category for a permission-denied issue
 - SERVICE_DOWN: The category for a missing service or system, e.g. database
 - TRUNCATED: The category for truncated data
 - TRY_AGAIN: The category for a try-again issue
 - USER_INPUT: The category for an invalid user input
 - WARNING: The category for a warning displayed to the user

# Examples of a Few Category Log Entries

Stack-traces are omitted for clarity's sake.

**USER_INPUT**

`20115-09-01T15:35:33,582+0200 ERROR [OXWorker-0000006] com.openexchange.exception.OXException: MSG-1013 Categories=USER_INPUT Message='Message could not be sent to the following recipients: \[invalid@non-existing.tld\] (550 - 550 5.1.1 <invalid@non-existing.tld>: Recipient address rejected: User unknown in virtual mailbox table)' exceptionID=-1316379722-11`

**TRY_AGAIN**

`2015-09-01T15:58:59,009+0200 ERROR [RMI TCP Connection(4)-127.0.0.1] com.openexchange.consistency.Consistency.erroroutput(Consistency.java:551)
com.openexchange.exception.OXException: SRV-0001 Categories=TRY_AGAIN Message='The required service com.openexchange.database.DatabaseService is temporary not available. Please try again later.' exceptionID=251360648-2`

**SERVICE_DOWN**

`2015-09-01T16:06:40,017+0200 ERROR [Thread-12] com.openexchange.database.migration.internal.DBMigrationExecutor.run(DBMigrationExecutor.java:152)
com.openexchange.exception.OXException: DBP-0001 Categories=SERVICE_DOWN Message='Cannot get connection to config DB.' exceptionID=-1771518749-4`

`2015-09-01T16:07:07,803+0200 WARN [OXTimer-0000007] com.openexchange.push.impl.balancing.reschedulerpolicy.PermanentListenerRescheduler.reschedule(PermanentListenerRescheduler.java:332)
Failed to distribute permanent listeners among cluster nodes
com.openexchange.exception.OXException: DBP-0001 Categories=SERVICE_DOWN Message='Cannot get connection to config DB.' exceptionID=-1771518749-6`

# Command Output

INFO/ERROR/WARNING: [msg1, msg2, ... msgn]

Operation [operation name] with parameters: {...} succeeded. 

# EXAMPLES

**logconf -u 1618 -c 314 -a -l com.openexchange.appsuite=DEBUG com.openexchange.mail=TRACE -A oxadminmaster -P secret**

Creates a user/context filter for user 1618, context 314 and loggers c.o.mail and c.o.appsuite with levels TRACE and DEBUG respectively

**logconf -u 1618 -c 314 -d -l com.openexchange.appsuite -A oxadminmaster -P secret**

Removes a logger from a user/context filter with user 1618 and context 314

**logconf -l com.openexchange.appsuite=DEBUG com.openexchange.usm=DEBUG -A oxadminmaster -P secret**

Change the log level for the com.openexchange.appsuite logger

**logconf -oec CONFIGURATION CONNECTIVITY -A oxadminmaster -P secret**

Suppress category CONFIGURATION and CONNECTIVITY

# SEE ALSO

[includestacktrace(1)](includestacktrace), [lastlogintimestamp(1)](lastlogintimestamp), [logincounter(1)](logincounter)
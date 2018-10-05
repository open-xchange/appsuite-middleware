---
title: Data Migration
---

# Introduction

When upgrading the server, several new database tables are created and existing calendar data is migrated. This process is required since the existing structures cannot be extended properly to support the new data model. 

For table creation, the update task ``com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask`` is registered; the actual migration is performed within the task ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask``. Depending on the amount of existing calendar data, the migration might take some time - in the magnitude of 1 second per 1K rows in ``prg_dates``. So as usual, the upgrade should be scheduled accordingly. The following chapters will provide some more details about the data migration.  

# Upgrade Process

By default, the update tasks are triggered automatically once a user from a not-upgraded schema makes a request to an upgraded middleware node. While the tasks run, access to the database schema is restricted by marking all contained groupware contexts *disabled* temporarily. Alternatively, update tasks can be triggered explicitly by invoking the ``runupdate``, ``runallupdate`` or ``forceupdatetask`` commandline utility manually.

The migration process itself sequentially converts calendar data from one context after the other. Within each context, the data is processed in fixed batches, where one batch of source data is loaded from the legacy tables, then converted to the new format, and finally persisted in the destination tables. The batch size can be controlled via configuration property ``com.openexchange.calendar.migration.batchSize``, and defaults to 500. A migration is also performed for the data about deleted appointment, back to a certain timeframe in the past (configurable via ``com.openexchange.calendar.migration.maxTombstoneAgeInMonths``, per default 12 months). This data is preserved to aid proper synchronization with external clients (CalDAV or USM/EAS). 

Depending on the expected amount of calendar data, the timespan where the contexts from a migrating schema are disabled may be significantly higher than during previous update tasks. Also, an increased load of the database can be expected. Therefore, especially larger installations or setups with lots of calendar data may require additional preparations. Things to take into consideration include

- Amount of calendar data per schema (number of rows in ``prg_dates``)
- Number of database schemas
- Potential concurrency of schema updates
- Impact on other running subsystems in the cluster

## Logging

To have a verbose progress logging of the migration, the log level for ``com.openexchange.chronos.storage.rdb.migration`` should be increased to ``ALL``, optionally writing to a separate appender. The following snippet shows an example of such an appender/logger pair defined in ``logback.xml``:

```xml
<appender class="ch.qos.logback.core.rolling.RollingFileAppender" name="UPGRADE-FILE">
  <file>/var/log/open-xchange/open-xchange-upgrade.log.0</file>
  <rollingPolicy class="com.openexchange.logback.extensions.FixedWindowRollingPolicy">
    <fileNamePattern>/var/log/open-xchange/open-xchange-upgrade.log.%i</fileNamePattern>
    <minIndex>1</minIndex>
    <maxIndex>9</maxIndex>
  </rollingPolicy>
  <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
    <maxFileSize>10MB</maxFileSize>
  </triggeringPolicy>
  <encoder class="com.openexchange.logback.extensions.ExtendedPatternLayoutEncoder">
    <pattern>%date{"yyyy-MM-dd'T'HH:mm:ss,SSSZ"} %-5level [%thread] %class.%method\(%class{0}.java:%line\)%n%msg%n%exception{full}</pattern>
  </encoder>
</appender>
<logger additivity="false" level="ALL" name="com.openexchange.chronos.storage.rdb.migration">
  <appender-ref ref="UPGRADE-FILE"/>
</logger>
```

Doing so, the actual progress of each context is printed out regularly, as well as a summary about possibly auto-handled data inconsistencies afterwards (see Malformed Data for further details below).

## Testing the Migration

Especially in larger setups or installation with heavy calendar usage, it's recommended to test the migration in a lab or staging environment prior moving forward to the upgrade of the productive system. Ideally, some tests can be executed against a snapshot of the production database on a similar-sized database system. So the database shouldn't be created from an SQL dump but really through a snapshot of the database files. The reason is that databases are suffering from fragmentation if used for a longer timespan. Thus doing this operation on a new database created from a dump will not contain the fragmentation of the production database leading to a big performance increase. This deviation is to be expected in the factor X range so the dump variant is not just a few percent faster.

The following list gives an overview about the necessary preparations before performing a test migration - the actual list depends on the concrete setup.

* Prepare an isolated database with the backed up dump or clone of the data to migrate
   - **Important:** the ``db_pool`` table in ConfigDB must be changed to reference the cloned UserDBs
* Configure an isolated OX middleware node against this database
   - **Important:** read/write connections and credentials in ``configdb.properties`` must match the cloned ConfigDB
   - GlobalDB identifier in ``globaldb.yml`` needs to be adjusted
   - ensure that ``SERVER_NAME`` in ``system.properties`` is adjusted properly
* Upgrade the open-xchange packages on that middleware node to the new release
	- **Important:** Do not run scripts like ``listuser``, they will trigger update task runs!
* Configure logging for ``com.openexchange.chronos.storage.rdb.migration`` appropriately as described above
* To run the calendar migration task separately, the task's names ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask``, ``com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectFilenamesTask`` and ``com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectRangesTask`` should be uncommented or added in the configuration file ``excludedupdatetasks.properties``
* (Re-)Start the open-xchange service once and execute all other update tasks of the new release by invoking the ``runallupdate`` commandline utility
* Remove or comment the previously added entries for ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask``, ``com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectFilenamesTask`` and ``com.openexchange.chronos.storage.rdb.groupware.CalendarEventCorrectRangesTask`` in ``excludedupdatetasks.properties`` again
* Restart the open-xchange service again

Now, the calendar data migration can be triggered on a specific schema by executing the ``forceupdatetask`` commandline utility, passing ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask`` as argument for the task name. While the task runs, the progress can be traced by ``tail``ing the logfile ``/var/log/open-xchange/open-xchange-upgrade.log.0`` (the actual name may vary based on the configured logger). Afterwards, the generated upgrade log can be reviewed for possible data inconsistencies, and the elapsed time for the migration per context or for a whole schema should be noted down to allow a forecast of the runtime when performing the migration of the productive system.

The execution of the update task is repeatable using the ``forceupdatetask`` commandline utility, as the calendar data in the legacy tables is always preserved, and any previously migrated data in the destination tables is purged implicitly in case there are remnants of previous migrations. This allows to repeat the update task in the same schema within different scenarios, such as changed MySQL configuration parameters. 

## Performing the Migration

As stated above, the calendar data migration might take some time on larger installations, so it should be tested, planned and scheduled accordingly. As most other update tasks, the migration will operate in *blocking* mode, which means that all contexts of a schema will be disabled while during the upgrade, i.e. client requests cannot be served temporarily. 

Afterwards, when the migration of calendar data is finished successfully, the calendar stack will automatically switch into a special mode where any changes that are persisted in the new tables are also *replayed* to the legacy tables. This is done to still provide a downgrade option to the previous version of the groupware server in case such a disaster recovery should ever be required. Additionally, this ensures that during a *rolling upgrade* scenario, where all groupware nodes are updated one after each other, up-to-date calendar data can also be read from nodes that are still running the previous version of the server. However, write access to calendar data from not yet upgraded groupware nodes is actively prevented, which ensure that no stale data is produced in the legacy database tables during the upgrade phase. Doing so, this *read-only* mode will only be effective on those middleware nodes of the cluster that have not been upgraded, so it is recommended to quickly roll out the updated packages on all middleware nodes of the cluster once the data migration has been performed.

The fact that users served via not upgraded middleware nodes not having "writable" access to their calendar data once their database schmema has been upgraded, should also be taken into account if it is planned to roll out the database changes manually beforehand, prior performing the package upgrade of the middleware nodes. Therefore, it should be desired to keep the timespan between migrating the calendar data and upgrading all middleware nodes in the cluster as small as possible, to mitigate the inconveniences of the potential *read-only* phase.

With a future upgrade, the storage is then switched into a "normal" operation mode again, along with purging the no longer needed legacy database tables. However, in case the server upgrade is completed and there's definitely no need to access the data in the old calendar tables and the downgrade option can be excluded, it's already possible to activate the update task ``com.openexchange.chronos.storage.rdb.migration.ChronosStoragePurgeLegacyDataTask`` by setting ``com.openexchange.calendar.migration.purgeLegacyData`` to ``true``. Then, aforementioned update task gets registered and can be executed. 

In case the migration task fails unexpectedly, the legacy data will still be used for both reading and writing, so that the calendaring system is still in a working state, with a reduced functionality. A subsequent migration attempt can then be performed using the ``runupdate`` commandline utility after the issues that led to the failed migration have been resolved.    
In case the migration task finishes successfully, but other circumstances force a disaster recovery in form of a downgrade of the installation to the previous version, downgraded nodes will still not be able to perform *write* operations on the legacy tables. To get out of this mode, it's necessary to unlist the successful execution of ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask`` manually from the system. This would require the manual deletion of the corresponding entry in the database (``DELETE FROM updateTask WHERE taskName='com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask';`` in all groupware schemas).  Upon the next upgrade of the server to the new version, the migration will then be executed again.

# Malformed Data

When reading legacy calendar during the migration, a couple of problems might arise due to malformed or erroneously stored calendar data. Usually, these are minor problems, and appropriate workarounds are available to transfer the data into a usable state again. Whenever this happens, a warning is produced that is decorated with a classification of the problem severity (*trivial*, *minor*, *normal*, *major*, *critical*, *blocker*).
The following list gives an overview about typical problems and the applied workarounds

1. *Preserving orphaned user attendee Attendee [cuType=INDIVIDUAL, partStat=NEEDS-ACTION, uri=..., entity=...]*
   
   *Trivial* problem severity. The legacy database table that holds entries for each participating internal user (``prg_dates_members``) contains a reference to a specific entity, but the database table that holds the associated permission (``prg_date_rights``) for this entity is missing, i.e. neither the user was added individually as participant, nor a group participant exists where the user is an actual member of. This might happen when there used to be a group that listed the user participant as member, but either the group has been deleted in the meantime, or the user has been removed from the group's member list.
   
   As general workaround, such orphaned attendees that do not have an associated entry in ``prg_date_rights`` are added as independent individual attendees of the event automatically when being loaded from the legacy storage. 

2. *Auto-correcting stored calendar user type for Attendee [cuType=RESOURCE, partStat=null, uri=null, entity=...] to "..."]*

   *Trivial* problem severity. There used to be times where it was possible that internal user attendees could be invited to events as resource attendees. Doing so, there's no corresponding entry for the user in the legacy ``prg_dates_members`` table.
   
   This is detected by checking the actual existence of internal resource entities, and optionally auto-correcting the actual calendar user type if a matching internal user can be looked up for the entity identifier. 

3. *Skipping non-existent Attendee [cuType=..., partStat=null, uri=null, entity=...]*

   *Minor* problem severity. The legacy database tables contain references to an internal group- or resource-attendee that does no longer exist in the system. Since no appropriate external representation exists due to the lack of a calendar address URI, such attendees are skipped.

4. *Skipping invalid recurrence date position "..."*

   *Minor* problem severity. The recurrence identifier for change- or delete exceptions in event series has to be derived from the legacy recurrence date position, which is the date (without time) where the original occurrence would have started, and is slightly different from the definition of ``RECURRENCE-ID`` in iCalendar. When converting the values, it is ensured that only valid recurrence identifiers are taken over, however, there may be some errorneous values stored, which have to be excluded.
   
5. *Falling back to external attendee representation for non-existent user Attendee [cuType=INDIVIDUAL, partStat=null, uri=null, entity=...]*

   *Minor* problem severity. The legacy database tables contain references to an internal user-attendee that does no longer exist in the system. Since an appropriate external representation (using the stored e-mail address of the calendar user), such attendees are preserved and converted to external individual attendees.

6. *Ignoring invalid legacy series pattern "..."*
        
   *Major* problem severity. Recurrence information of event series used to be stored in a proprietary format in the legacy database and is converted to a standards-compliant ``RRULE`` when being loaded. Usually, all possible series pattern can be transferred without problems. However if for any reason the conversion fails, the recurrence information needs to be removed from the event.
   
7. *Ignoring invalid legacy ReminderData [reminderMinutes=..., nextTriggerTime=...] for user ...*

   *Minor* problem severity. The legacy reminder information cannot be converted to a valid alarm, and is skipped implicitly.

All warnings that occurred during the migration will get logged with level ``INFO`` for each context.

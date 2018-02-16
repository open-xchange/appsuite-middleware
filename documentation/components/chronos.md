---
title: Calendar v2 aka Chronos 
---


# Details

This section provides some deeper insights for certain topics.

## Date/Time handling

There are some different modes for the start- and end-date of events. This affects all areas where dates are used and exchanged (HTTP API, iCal, Java, database). Most basically, there are dates (without time fraction) and datetimes (a date with time fraction). For the latter one, three different forms are possible.

### Date

A calendar date with no time component, i.e. only year, month and date are set. It is not possible to assign a concrete timezone to a date, so that dates do always appear in the timezone of the actual viewer. *All-day* events start- and end on a date. This characteristic is called *floating*, and needs to be handled appropriately whenever concrete periods of such *all-day* events need to be considered, e.g. during free/busy lookups or conflict checks.

### Date with Time

A precise calendar date and time consisting of day, year, month, date, hour, minute, second. A datetime may be defined in three different forms, which are **local time** (no specific timezone), in **UTC**, or in **local time with timezone reference**. 

1. **Local time** - no specific timezone
    - Used for *floating* events
    - Represent the same year, month, day, hour, minute, second no matter in what time zone they are observed
    - Example in iCal: ``DTSTART:19980118T230000``
    - In the database, the unix timestamp is stored, and the timezone is set to ``NULL``

2. **UTC** - UTC timezone
    - Easiest form, but not much useful for events
    - Example in iCal: ``DTSTART:19980118T230000Z``
    - In the database, the unix timestamp is stored, and the timezone is set to ``UTC``

3. **Local time with Timezone reference** - specific timezone
    - Most common form for events
    - Example in iCal: ``DTSTART;TZID=America/New_York:19980119T020000``
    - In the database, the unix timestamp ans the timezone are stored

### Usage

Internally, througout the new Chronos stack, a DateTime object from the 3rd party library ``rfc5545-datetime`` is used, where dedicated support for the different modes are available. In the database, we have separat columns for the actual value of the start- and end-date of an event, the associated timezones and the *all-day* flag. 


### References / further reading
- https://tools.ietf.org/html/rfc5545
- https://devguide-calconnect.rhcloud.com/Handling-Dates-and-Times
- https://tools.ietf.org/html/rfc4791#section-7.3


## Relation of Organizer / Principal / Folder-Owner / Creator

### Terminology

**Folder-Owner** ~ The owner of a personal calendar folder
- In OX, this is the user with the identifier of the folder's ``createdBy`` property, if the folder type is *private*
- In CalDAV, this is expressed in the calendar collection's ``calendar-owner`` property:  
  RFC 4791, 12.1: *This property is used for browsing clients to find out the user, group or resource for which the calendar events are scheduled. Sometimes the calendar is a user's calendar, in which case the value SHOULD be the user's principal URL from WebDAV ACL. (In this case the DAV:owner property probably has the same principal URL value.)*
- In Outlook, this owner is usually called the *principal*, which is the principal name for the mailbox user.

**Organizer** ~ The organizer of an event
- In OX (legacy), this is the user who created the event
- In iCalendar, this is the ``ORGANIZER`` property and points to the organizer of the event
- In CalDAV, the organizer determines the type of scheduling object resources:  
  RFC 6638, 3.1: *A calendar object resource is considered to be a valid organizer scheduling object resource if the "ORGANIZER" iCalendar property is present and set in all the calendar components to a value that matches one of the calendar user addresses of the owner of the calendar collection.*  
  And: * "A calendar object resource is considered to be a valid attendee scheduling object resource if the "ORGANIZER" iCalendar property is present and set in all the calendar components to the same value and doesn't match one of the calendar user addresses of the owner of the calendar collection, and if at least one of the "ATTENDEE" iCalendar property values matches one of the calendar user addresses of the owner of the calendar collection."*  
  So, for newly created events, the ``ORGANIZER`` property of the iCal data must match the *owner* of the parent calendar collection.
- In Outlook, this is the meeting organizer


### Difficulties
In case an event is created within the personal calendar of a specific user, this user is automatically also the organizer of the event. This is the most common case and is easy to handle and understand.
Difficulties arise as soon as an event is created in the personal calendar folder of another user. Then, the user is acting **on behalf** of the calendar owner. This is often referred-to as the *secretary* functionality in the calendar, where the manager grants full permissions to his secretary to manage his appointments. In this case, some special handling applies towards the assignment and interpretation of the organizer attribute.

### Legacy: Organizer and Principal
The legacy calendar implementation used the properties "principal" and "organizer" for storing an event's organizer and a possibly different owner. 
The ``organizer`` (and ``organizerId``) always holds the user who creates the appointment, regardless of the parent folder. The ``principal`` (and ``principalId``, respectively) properties are set to the parent calendar folder's owner, in case it is a personal calendar that is *shared* by another user.
The properties are stored as-is in the database, and are accessible in the same way via the HTTP API. The ``createdBy`` property is set to the identifier of the folder owner as well.

**Example:**
- Eva (id 5) has granted read/write permissions for his personal calendar folder to Tobias (id 4)
- Tobias creates a new appointment in Eva's calendar
- Database: ``created_from=5,principalId=5,principal='eva@local.ox',organizerId=4,organizer='tobias@local.ox'``
- HTTP API: ``"created_by":5,"principal":"eva@local.ox","organizerId":4,"organizer":"tobias@local.ox"`` 

### Chronos: Organizer and sent-by

In the Chronos stack, there's no dedicated "principal". Instead, it is ensured that the organizer is always the actual calendar owner for newly created events (An exception to this rule are imported scheduling object resources from external organizers, as described at RFC 6638, section 3.2.2.2). In case the event is created on behalf of the folder owner by another calendar user (e.g. the secretary), this is expressed via the ``SENT-BY`` attribute within the organizer.       

### Conversion 

In order to convert between the legacy properties for organizer/principal and the organizer as it is used within the Chronos stack, the following conversions are performed:

**HTTP API, Appointment->Event and Database, prg_dates->Event**
- ``principal``/``principalId`` not set:  
  Organizer is taken from ``organizer``/``organizerId``
  Organizer's "sent-by" is empty
- ``principal``/``principalId`` are set:  
  Organizer is taken from ``principal``/``principalId``  
  Organizer's ``sentBy`` is constructed from ``organizer``/``organizerId``

**HTTP API, Event->Appointment and Database, Event->prg_dates**
- Organizer's "sent-by" is set:  
  ``organizer``/``organizerId`` is taken from Organizer's sent-by  
  ``principal``/``principalId`` is taken from Organizer  
- Organizer's "sent-by" not set:  
  ``organizer``/``organizerId`` is taken from Organizer  
  ``principal``/``principalId`` empty  
  
### References / further reading
- https://tools.ietf.org/html/rfc4791
- https://tools.ietf.org/html/rfc6638
- https://msdn.microsoft.com/en-us/library/office/bb856541.aspx
- https://sogo.nu/bugs/view.php?id=3368
- http://lists.calconnect.org/pipermail/tc-sharing-l/2014-December/000052.html
- https://docs.google.com/document/d/1mRFEgCWIFbFz2v_L0odlI1oIDyOZESBrGFJm7Sun15Y/edit
- https://bugs.open-xchange.com/show_bug.cgi?id=21620
- com.openexchange.calendar.itip.ITipCalendarWrapper.onBehalfOf(int)
- com.openexchange.calendar.itip.ITipConsistencyCalendar.setPrincipal(CalendarDataObject)
- com.openexchange.calendar.json.actions.chronos.EventConverter.getOrganizer(int, String, int, String)


## Per-Attendee delete exceptions

As invited attendees may delete a meeting from their personal calendar if they do not want to attend ("decline, and remove me from attendee list"), they may also do so for specific occurrences of a recurring event series. From the organizer's and the other attendee's point of view, this leads to a new change exception event with an updated list of attendees (with this deleting attendee being no longer listed there). However, for the attendee who has deleted a specific occurrence of the series, this rather means the creation of a new delete exception in the event series. 

According to the RFC 6638, in such a scenario the attendee effectively gets a different set of delete exception dates (EXDATE property in iCal), while the organizer and the other attendees see this exception date as overridden instance (change exception): 

> "As another example, an "Attendee" could be excluded from one instance of a recurring event.  In that case, the organizer scheduling object resource will include an overridden instance with an "ATTENDEE" list that does not include the "Attendee" being excluded.  Any scheduling messages delivered to the "Attendee" will not specify the overridden instance but rather will include an "EXDATE" property in the "master" component that defines the recurrence set."  

While appropriate handling has originally been in place as incoming/outgoing "patches" within the CalDAV implementation, this is now considered directly within the Chronos service itself, so that the change- and delete-exception arrays in series events may be different based on the actual calendar user.

### References / further reading
- https://tools.ietf.org/html/rfc6638#section-3.2.6
- com.openexchange.chronos.impl.Utils.applyExceptionDates(CalendarStorage, Event, int)
- com.openexchange.chronos.impl.performer.UpdatePerformer.updateDeleteExceptions(Event, Event)
 

## Classification / Private flag

The legacy *private* flag (``pflag`` in database) is used to hide sensitive details from appointments to other users. Participants of the appointment may always see all details of such appointments, while other users who are able to access such appointments based to their permissions (e.g. in shared folders) will only have a restricted view on them. This basically includes the start- and end-time, identifying properties such as the UID, and the appointment's *shown-as* value. Instead of the appointment title, usually "Private" is shown instead.

In iCalendar, this relates to the classification property of an event, with the possible values ``PUBLIC`` (default), ``PRIVATE`` and ``CONFIDENTIAL``. While documentation about the exact meanings of ``PRIVATE`` and ``CONFIDENTIAL`` are quite rare, the legacy *private* flag best matches the semantics of ``CONFIDENTIAL``, i.e. only start- and end-times of the events are visible when being read by non-participating users. So, the legacy *private* flag will be converted to the classification ``CONFIDENTIAL``; vice-versa, both ``PRIVATE`` and ``CONFIDENTIAL`` will make the *private* flag ``true``. Consequently, the parameter ``showPrivate`` of the legacy HTTP API is applied to ``CONFIDENTIAL``ly marked events.  

Internally, the following semantics apply for the classifications:

- ``PUBLIC`` (default)
  No special handling, i.e. all event properties are exposed to non-attending users in shared folders. 
- ``CONFIDENTIAL``
  Only certain non-classified event properties are exposed to non-attending users in shared folders (basically all date- and time-related properties as well as administrative fields like identifiers), so that effectively such events appear as anonymous 'blocks' when being viewed in a calendar. The event's summary is replaced with the static text "Private". Additionally, such events are still considered in conflict- and free/busy-queries.
- ``PRIVATE``
  Events are not exposed to non-attending users in shared folders at all. Additionally, such events are only considered in conflict- and free/busy-queries in case of an attending resource attendee. 

For synchronization via CalDAV, the event classification is passed and read *as-is*, with the exception of the Apple calendar clients who require some special treatment (in form of a proprietary spec) in this topic. When exporting calendar object resources whose classification is different from ``PUBLIC`` for an Apple client, the parent ``VCALENDAR`` component is decorated with an additional property ``X-CALENDARSERVER-ACCESS``, set to the value of the (series master) event's classification. In the same way during import, the passed value of ``X-CALENDARSERVER-ACCESS`` is considered and transferred from the parent ``VCALENDAR`` component to each contained ``VEVENT``.

### References / further reading
- https://tools.ietf.org/html/rfc5545#section-3.8.1.3
- https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-privateevents.txt
- http://blog.coeno.com/offentliche-private-und-vertrauliche-kalenderereignisse-und-wie-man-sie-richtig-nutzt/
- com.openexchange.chronos.Classification
- com.openexchange.chronos.compat.Event2Appointment.getPrivateFlag(Classification)
- com.openexchange.chronos.compat.Appointment2Event.getClassification(boolean)
- com.openexchange.chronos.impl.Check.classificationIsValid(Classification, UserizedFolder)
- com.openexchange.chronos.impl.Utils.NON_CLASSIFIED_FIELDS


## Move between Folders

Whenever events are moved between different folders, some special handling applies, especially for move operations between different *types* of folders. We basically differentiate between two types of folders, which are on the one hand *personal* calendar folders of the internal users, and *public* folders on the other hand that are not directly associated explicitly with a user. The following gives an overview about the possible move actions and their outcome.

### General restrictions

The user performing the move must be equipped with appropriate permissions for the source- and target folder. For the source folder, these are at least *Read folder*, *Read own/all objects*, *Write own/all objects* and *Delete own/all objects*. *All* or *own* depends on the event being created by a different user or not. In the target folder, at least the following permissions must be granted: *Create objects in folder*. 

Additionally, recurring event series (or change exception events) cannot be moved. And finally, there's a limitation towards moving events with a classification of ``PRIVATE`` or ``CONFIDENTIAL`` (which correlates to the legacy *private* flag). In this case, events must not be moved to a *public* folder, or moved between personal folders of different user (~ *shared* to *private* and vice versa).

After the general restrictions have been checked and are fulfilled, the following happens depending on the source- and target folder type. Implicitly, this also includes triggering of further updates, like updating the folder informations for stored alarm triggers or inserting tombstone objects in the database so that the deletion from the source folder can be successfully tracked by differential synchronization algorithms.    

### Public calendar folder 1 -> Public calendar folder 2
- Update the common public folder identifier of the event

### Personal calendar folder 1 of User A -> Personal calendar folder 2 of same User A
- Update attendee A's parent folder identifier

### Personal calendar folder of User A -> Personal calendar folder of other User B (executed by user A, *"re-assign" event*)
- Remove the original calendar user attendee A 
- Ensure that calendar user B becomes attendee and set take over his parent folder identifier
- Reset the parent folder identifier of all other attendee's to their default calendar

### Personal calendar folder of User A -> Personal calendar folder of other User B (not executed by user A)
- Ensure that calendar user B becomes attendee and set take over his parent folder identifier
- Reset the parent folder identifier of all other attendee's to their default calendar
  
### References / further reading
- com.openexchange.chronos.impl.performer.MovePerformer
- https://intranet.open-xchange.com/wiki/backend-team:info:calendar#move_appointments_between_folders
- com.openexchange.ajax.appointment.MoveTestNew

   
## Conversion of Recurrence Rules

Within the Chronos stack, the recurrence information is transferred and treated as plain ``RRULE`` string, like it is defined in RFC 5545. Any series calculations are performed ad-hoc, under the constraints of some further influencing properties (start-date, start timezone, all-day flag) of the series master event. In the legacy implementation, the recurrence information was split over different properties in the class ``CalendarDataObject``, and provided as such to clients accessing the HTTP API. In particular, the properties ``recurrence_type``, ``days``, ``day_in_month``, ``month``, ``interval`` and ``until``. In the database, this recurrence information is stored in a combined field as so-called *series pattern*. 

For interoperability with the old API and database format, distinct conversion routines are in place, which were built upon already existing (de-)serialization routines to and from iCalendar. This also means that consequently still only those recurrence rules are supported that can also be expressed with the legacy series pattern, at least as long as the data is stored in this format. However, any recurrence calculations in the Chronos stack do already support all ``RRULE`` parts as described in RFC 5545.

### Supported ``RRULE`` parts

The following list gives an overview about the supported ``RRULE`` parts, based on the limitations described before.
- ``FREQ:DAILY``: ``INTERVAL``, ``UNTIL``, ``COUNT``
- ``FREQ:WEEKLY``: ``INTERVAL``, ``UNTIL``, ``COUNT``, ``BYDAY``
- ``FREQ:MONTHLY``: ``INTERVAL``, ``UNTIL``, ``COUNT``, ``BYDAY``, ``BYSETPOS``, ``BYMONTHDAY``
- ``FREQ:YEARLY``: ``INTERVAL``, ``UNTIL``, ``COUNT``, ``BYDAY``, ``BYMONTH``, ``BYSETPOS``, ``BYMONTHDAY``

### ``UNTIL`` handling

If an event series is decorated with an end date, this used to be stored as *the date of the last occurrence without time fraction in milliseconds since the Unix epoch*. However, in RFC 5545, the corresponding ``UNTIL`` part of a ``RRULE`` is just defined as 
> The UNTIL rule part defines a DATE or DATE-TIME value that bounds the recurrence rule in an inclusive manner.

Therefore, some information might currently get lost when converting a recurrence rule to a legacy series pattern, since the time fraction of the date-time value from the ``RRULE`` has to be truncated prior saving. Also, some special checks need to be in place during conversion to prevent an additional occurrence if the client-supplied ``UNTIL`` lies behind the last occurrence (and possibly right before the start date of a next occurrence).  

### References / further reading
- https://tools.ietf.org/html/rfc5545#section-3.3.10
- https://intranet.open-xchange.com/wiki/backend-team:info:calendar#serientermine
- com.openexchange.groupware.calendar.CalendarDataObject
- com.openexchange.chronos.compat.Recurrence
- com.openexchange.chronos.compat.Recurrence.getSeriesEnd


## Reset of Participation Status

Whenever an existing event with attendees is *rescheduled*, each attendee's participation status is reset to ``NEEDS-INFO``. A reschedule occurs, when any ``DTSTART``, ``DTEND``, ``DURATION``, ``RRULE``, ``RDATE``, or ``EXDATE`` property changes such that existing recurrence instances are impacted by the changes (RFC 6638, 3.2.8).

Internally, these kind of checks are performed along with each event update is checked. Optionally, the standard allows to behave similarly in case of other changes, e.g. a changed event location, however, currently only time changes are considered.  

### References / further reading
- https://tools.ietf.org/html/rfc6638#section-3.2.8
- com.openexchange.chronos.impl.performer.UpdatePerformer.needsParticipationStatusReset


## Reset of Change Exceptions

When an event series is updated, there are situations where all previously created change exceptions need to be reset, i.e. they get removed so that there are only regular occurrences again. In the legacy implementation, this has always been the case whenever the series pattern is changed (in any way), or the recurring master's start-/endtime are updated. Implicitly, this would also reset any participation status of the attendees, see above. 

In the Chronos stack, a slightly enhanced approach is used so that not necessarily all previous change- and delete-exceptions are lost along with an updated recurrence rule. Now, whenever the recurrence rule is changed, the implementation checks if there are existing exceptions whose ``RECURRENCE-ID`` would still be matched by the recurrence set of the updated rule. If so, the change- or delete-exception is kept, otherwise it is still removed. In other words, if the original start time of an existing exception would still be a occurrence of the new recurrence rule, then it can be preserved. For example, any exceptions prior an updated ``UNTIL`` or ``COUNT`` part survive the update. Or, when changing a previously ``DAILY`` rule to only occur on *working days*, any previous exceptions on those days are kept.      

### References / further reading
- com.openexchange.calendar.api.CalendarCollection.detectTimeChange
- com.openexchange.calendar.CalendarOperation.checkPatternChange
- com.openexchange.calendar.CalendarMySQL.deleteAllRecurringExceptions


## External Calendar Users

At many places, one needs to differentiate between *internal* and *external* calendar users. Internal calendar users are all provisioned groupware entities within the same context, i.e. all resources, users and groups. All other calendar users are treated as external entities. This is especially also true for entities from other contexts of the same installation.

### Scheduling

Currently, all external calendar users require a valid e-mail address that can be used for scheduling via iMIP. This is enforced by validating the URIs of external attendees prior saving. Attempting to save or update an event containing an external calendar user w/o valid e-mail address will fail; the check may be skipped when setting ``com.openexchange.chronos.skipExternalAttendeeURIChecks`` to ``false``.

### URI Encoding / International Domain Names (IDN)

E-Mail addresses of calendar users may contain non-ASCII characters, e.g. from international domain names like *müller.com*. When being used in a calendar user's URI along with the ``mailto:`` scheme, they are usually transferred as percent-encoded UTF-8. For example, for an attendee with e-mail address "horst@müller.com", the value of the corresponding ``ATTENDEE`` property would look like the following (while the ``CN`` parameter would still just use the UTF-8 string as-is):

> ``ATTENDEE;CN="Horst Müller";CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION:mailto:horst@m%C3%BCller.com``

Internally within the Chronos stack, all URIs are kept as (escaped) URI string, which would match the representation of the corresponding value in an iCalendar ``ATTENDEE`` or ``ORGANIZER`` property.

When being converted back to a plain e-mail address string (as used for external participants in the legacy stack), such URIs are decoded implicitly; additionally, ASCII-encoded (punycode) addresses with international domain names (IDN) are converted back to their unicode representation, too. This is the case when converting to an external participant in the legacy HTTP API, as well as when storing such an attendee in the ``dateExternal`` table. 

### References / further reading

- https://tools.ietf.org/html/rfc5545#section-3.3.3
- com.openexchange.chronos.common.CalendarUtils.extractEMailAddress(String)
- com.openexchange.chronos.impl.Check.requireValidEMail(T)


## Provider/Account Framework

Besides the default, internal calendar, there may be further calendar sources that should be integrated into a user's calendar module. For example, calendar feeds from external sources, or a virtual calendar containing the upcoming birthdays found in the user's address book. Therefore, a new layer is introduced that provides access to all available calendar *accounts* from different *providers* of a user using the same API.

### Calendar Providers and Accounts

A calendar provider implements the functionality to access the calendar data of a specific calendar source. Calendar access is always bound to a specific account of a user within the calendar provider, i.e. each calendar provider provides a number of accounts for different users. A user may have a fixed number of accounts (e.g. exactly one) within a concrete provider, or there can be multiple accounts for the user. 

Have a look at the chapter 'Default implementations' to get an overview over providers shipped by Open-Xchange.

### External Calendar Account Caching

The default calendar providers implemented by Open-Xchange (like ICal, SchedJoules, ...) use a generic caching approach to avoid expensive calls to external resources as much as possible. This means that events (and all related information like attendees and alarms) from the external resource gets persisted like events from the internal calendar after their first retrieval. All upcoming requests will be answered by the cache until a defined refresh interval is exceeded.

The caching layer itself is able to handle information on a per-folder base. Cached folders that have been deleted on the remote site will be removed immediately from the cache while new folders on remote will be cached instantly. 

In general the cache should be a blackbox for those implementations using it. Of course there are some utilities and hooks for the provider to be able to invalidate the folder cache as the cache doesn't know all possible cases.

#### Refreshing cache

Each Calendar Provider implementation can use its own refresh interval, again on a per-folder base. These value defines how long the cache should be used to answer requests for a dedicated folder of the underlying calendar. After the interval for the folder is exceeded the cache state will be updated by contacting the external calendar. New events will be added, removed ones will be deleted and changed events will be updated. 

The refesh interval is defined in minutes. If the calendar provider implementation does provide a value bigger than 0 this will be taken into account. Otherwise the default of one day will be used.

In cases where new folders have been added or previously existing folders have been removed on the external calendar site the caching layer will directly update its internal state which means changes on the folder structure will be instantly visible without considering the refresh interval!

#### Error handling

The caching allows to handle data that will normally fail to be executed. Among other things the following error cases will be handled by the generic caching layer:

- data truncation: external data that is too big to be stored will be truncated so that no import will fail.
- corrupt data: data that won't met the requirements to be stored will be handled as smooth as possible. For instance if start or timestamp of an event is missing the complete event will be ignored.
- missing UID: if an event lacks on appropriate UID definitions it is not possible to generate a reproducible diff for already persisted and externally available events. Due to this fact the caching will delete all persisted events for the folder and re-import those from the calendar provider.

As always the errors will be handled per folder and the underlying implementation has the opportunity to deal with it. In addition it can define an interval in which the external resource should not be requested after an error occurred. This prevents a continuous requests loop to the external provider which permanently will fail because of the same error. After the given interval time is exceeded the cache connects to the external provider again and hopefully the error is gone ...

### Default implementations

As already mentioned Open-Xchange provides some default implementations for external accounts.

#### ICal Calendar Provider

Using the ICal Calendar Provider allows to subscribe to various remote calendar feeds that meet the requirements defined by the administrator (size, schemes, ...). The user just has to give an URI and optional credentials to subscribe the feed. To optimize requests to external resources the ICal Calendar Provider implementation is based on the generic caching layer described above. This means the calendar provider has to define its own refresh interval.

##### Refresh interval

In general the administrator is able to configure a refresh interval (by using the well known ConfigCascade) that will be used for each subscribed feed. As feeds might also define a refresh interval (by having 'REFRESH-INTERVAL' set) this value will be used under the assumption that the feed does even better know which interval makes sense.

##### Cache invalidation

As the caching layer isn't aware of changes that should result in a cache invalidation the implementation has to call back in such cases. For the ICal implementation some user actions have to trigger such an invalidation. These actions can be summarized as account reconfiguration which contains endpoint (URI or auth) changes that might result in a complete new ICal subscription with other events.

##### Configuration

The following configuration can be made by the administrator:

- a maximal file size that is allowed for a feed: This ensures smooth processing of the feed by the Open-Xchange server and prevents undesired states. Each feed exceeding the limit will be denied. Per default the limit is set to 5MB.
- a host blacklist: Due to security reasons the blacklist avoids connections to nodes of the entire network. Per default the blacklist contains 127.0.0.1-127.255.255.255 and localhost.
- supported schemes: Due to security reasons (for instance avoid 'file') only a defined set of schemes should be supported. Per default these are http, https and webcal
- various connection parameters: This contains timeouts and a maximum number of connections.

Have a look at the property documentation page on http://documentation.open-xchange.com for more details.

#### SchedJoules

...

#### Google Calendar

...


### ...


## Group-scheduled Events

In iCalendar, there is a strict separation between simple events without further attendees in a user's calendar, and so-called *group-scheduled* events. Group-scheduled events are *meetings* with a defined organizer and one or more attendees, while not group-scheduled ones are *published* events, or events in a single user's calendar only. 

### Legacy: Implicit Participant

In the legacy implementation, appointments were always stored with at least the calendar user being a participant. This has the side effect of the actual parent folder information always being stored along with the participants, and not within the appointment itself (except for the fixed parent folder identifier for appointments in public folders). Doing so, lookups based on the parent folder could be performed by matching against the attendee's folders for private and shared folders. 

Furthermore, since also simple, not group-scheduled appointments were stored with the calendar user as participant, the list of "all appointments of a certain user" could be built up in a very effective way. This also aided free/busy lookups and conflict checks, respectively.    

However, based on RFC 5545, this is handling was **wrong** (the CalDAV layer already tried to work around this difference with some patches to remove this implicit attendee during export, and re-apply it during import again).   

### Chronos: No implicit Participant 

In the new Chronos stack, we're going to be standards-compliant here, i.e. we'll no longer add the current calendar user as attendee and organizer implicitly in case no further attendees are defined. 

### References / further reading
- https://bugs.horde.org/ticket/10697


## Migration of legacy data

When upgrading the server, several new database tables are created and existing calendar data is migrated. This process is required since the existing structures cannot be extended properly to support the new data model. 

For table creation, the update task ``com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask`` is registered; the actual migration is performed within the task ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask``. Depending on the amount of existing calendar data, the migration might take some time - in the magnitude of 1 second per 1K rows in ``prg_dates``. So as usual, the upgrade should be scheduled accordingly. The following chapters will provide some more details about the data migration.  

### Upgrade Process

By default, the update tasks are triggered automatically once a user from a not-upgraded schema makes a request to an upgraded middleware node. While the tasks run, access to the database schema is restricted by marking all contained groupware contexts *disabled* temporarily. Alternatively, update tasks can be triggered explicitly by invoking the ``runupdate``, ``runallupdate`` or ``forceupdatetask`` commandline utility manually.

The migration process itself sequentially converts calendar data from one context after the other. Within each context, the data is processed in fixed batches, where one batch of source data is loaded from the legacy tables, then converted to the new format, and finally persisted in the destination tables. The batch size can be controlled via configuration property ``com.openexchange.chronos.migration.batchSize``, and defaults to 500. A migration is also performed for the data about deleted appointment, back to a certain timeframe in the past (configurable via ``com.openexchange.chronos.migration.maxTombstoneAgeInMonths``, per default 12 months). This data is preserved to aid proper synchronization with external clients (CalDAV or USM/EAS). 

Depending on the expected amount of calendar data, the timespan where the contexts from a migrating schema are disabled may be significantly higher than during previous update tasks. Also, an increased load of the database can be expected. Therefore, especially larger installations or setups with lots of calendar data may require additional preparations. Things to take into consideration include

- Amount of calendar data per schema (number of rows in ``prg_dates``)
- Number of database schemas
- Potential concurrency of schema updates
- Impact on other running subsystems in the cluster

#### Logging

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

#### Testing the Migration

Especially in larger setups or installation with heavy calendar usage, it's recommended to test the migration in a lab or staging environment prior moving forward to the upgrade of the productive system. Ideally, some tests can be executed against a backed up dump or clone of the productive data, in order to get a feeling about the expected runtime and impact. 

The following list gives an overview about the necessary preparations before performing a test migration - the actual list depends on the concrete setup.

"Prepare an isolated database with the backed up dump or clone of the data to migrate", wollen wir da auf die Dinge hinweisen, über die wir selbst gestolpert waren. Ich hatte folgendes mitgeschrieben:

* Prepare an isolated database with the backed up dump or clone of the data to migrate
   - **Important:** the ``db_pool`` table in ConfigDB must be changed to reference the cloned UserDBs
* Configure an isolated OX middleware node against this database
   - **Important:** read/write connections and credentials in ``configdb.properties`` must match the cloned ConfigDB
   - GlobalDB identifier in ``globaldb.yml`` needs to be adjusted
   - ensure that ``SERVER_NAME`` in ``system.properties`` is adjusted properly
* Upgrade the open-xchange packages on that middleware node to the new release
	- **Important:** Do not run scripts like ``listuser``, they will trigger update task runs!
* Configure logging for ``com.openexchange.chronos.storage.rdb.migration`` appropriately as described above
* To run the calendar migration task separately, the task's name ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask`` should be uncommented or added in the configuration file ``excludedupdatetasks.properties``
* (Re-)Start the open-xchange service once and execute all other update tasks of the new release by invoking the ``runallupdate`` commandline utility
* Remove or comment the previously added entry for ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask`` in ``excludedupdatetasks.properties`` again
* Restart the open-xchange service again

Now, the calendar data migration can be triggered on a specific schema by executing the ``forceupdatetask`` commandline utility, passing ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask`` as argument for the task name. While the task runs, the progress can be traced by ``tail``ing the logfile ``/var/log/open-xchange/open-xchange-upgrade.log.0`` (the actual name may vary based on the configured logger). Afterwards, the generated upgrade log can be reviewed for possible data inconsistencies, and the elapsed time for the migration per context or for a whole schema should be noted down to allow a forecast of the runtime when performing the migration of the productive system.

The execution of the update task is repeatable using the ``forceupdatetask`` commandline utility, as the calendar data in the legacy tables is always preserved, and any previously migrated data in the destination tables is purged implicitly in case there are remnants of previous migrations. This allows to repeat the update task in the same schema within different scenarios, such as changed mysql configuration parameters. 

#### Performing the Migration

As stated above, the calendar data migration might take some time on larger installations, so it should be tested, planned and scheduled accordingly. As most other update tasks, the migration will operate in *blocking* mode, which means that all contexts of a schema will be disabled while during the upgrade, i.e. client requests cannot be served temporarily. 

Afterwards, when the migration of calendar data is finished successfully, the calendar stack will automatically switch into a special mode where any changes that are persisted in the new tables are also *replayed* to the legacy tables. This is done to still provide a downgrade option to the previous version of the groupware server in case such a disaster recovery should ever be required. Additionally, this ensures that during a *rolling upgrade* scenario, where all groupware nodes are updated one after each other, up-to-date calendar data can also be read from nodes that are still running the previous version of the server. However, write access to calendar data from not yet upgraded groupware nodes is actively prevented, which ensure that no stale data is produced in the legacy database tables during the upgrade phase. Doing so, this *read-only* mode will only be effective on those middleware nodes of the cluster that have not been upgraded, so it is recommended to quickly roll out the updated packaes on all middleware nodes of the cluster once the data migration has been performed.

The fact that users served via not upgraded middleware nodes not having "writable" access to their calendar data once their database schmema has been upgraded, should also be taken into account if it is planned to roll out the database changes manually beforehand, prior performing the package upgrade of the middleware nodes. Therefore, it should be desired to keep the timespan between migrating the calendar data and upgrading all middleware nodes in the cluster as small as possible, to mitigate the inconveniences of the potential *read-only* phase.

With a future upgrade, the storage is then switched into a "normal" operation mode again, along with purging the no longer needed legacy database tables. In case the migration task fails unexpectedly, the legacy data will still be used for both reading and writing, so that the calendaring system is still in a working state, with a reduced functionality. A subsequent migration attempt can then be performed using the ``runupdate`` commandline utility after the issues that led to the failed migration have been resolved.    
In case the migration task finishes successfully, but other circumstances force a disaster recovery in form of a downgrade of the installation to the previous version, downgraded nodes will still not be able to perform *write* operations on the legacy tables. To get out of this mode, it's necessary to unlist the successful execution of ``com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask`` manually from the system. Upon the next upgrade of the server to the new version, the migration will then be executed again.     

### Malformed Data

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

### References / further reading
- com.openexchange.chronos.storage.rdb.legacy.RdbAttendeeStorage.isIgnoreOrphanedUserAttendees


## Last-Modified / Created

In the legacy stack, the properties that store the creation- and last-modification time of an event were handled by the server implicitly each time the entry was written in the database, so that they're actually *read-only* for clients. Similarly, the entity identifiers of the acting user were stored along as created- and modified-by. Additionally, the last-modified property was also used to prevent so-called *lost updates*, in a way that clients were only allowed to update or delete an event where the last-modified timestamp matches the one of the version last read by this client. Also, the last-modified property was used to compute resource ETags and to drive the ``sync-collection`` report in CalDAV.

While letting the server assign those timestamps based on the current time automatically is feasible for events being initially created on that system, it is problematic whenever data is imported from an external source - e.g. a subscribed external calendar feed, or a manually imported iCalendar file. Also, the created- and modified-by properties would be rather misleading in such scenarios. Therefore, the new calendar implementation will handle those properties in the following way:

- The properties ``created-by`` and ``modified-by`` are no longer mandatory (i.e. may be ``NULL``ed if not applicable)
- If set, the properties ``created-by`` and ``modified-by`` still refer to internal calendar users
- The properties ``last-modified`` and ``created`` are no longer mandatory (i.e. may be ``NULL``ed if not applicable)
- If set, the properties ``created`` and ``last-modified`` do not necessarily store the server timestamp the event was (last) written, and may contain values from an external source
- For synchronization purposes, an additional synthetic property ``timestamp`` is introduced, which is set to the current server timestamp whenever the event is written

For backwards compatibility, the new ``timestamp`` property is used to drive the ``last-modified`` property in the legacy HTTP API. Additionally, due to the ``NOT NULL`` column definitions, the respective values in the legacy storage are derived from the acting calendar user and the timestamp property as needed. 

### References / further reading
- https://tools.ietf.org/html/rfc5545#section-3.8.7
- http://oxpedia.org/index.php?title=HTTP_API#Date_and_time
- https://bugzilla.mozilla.org/show_bug.cgi?id=303663#c2




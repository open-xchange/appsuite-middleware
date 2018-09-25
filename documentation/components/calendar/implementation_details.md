---
title: Implementation Details 
---

This section provides some deeper insights for certain topics of the new calendar implementation. Whenever appropriate, semantical differences to the previous calendar stack or the standards are highlighted.
 

# Date/Time handling

There are some different modes for the start- and end-date of events. This affects all areas where dates are used and exchanged (HTTP API, iCal, Java, database). Most basically, there are dates (without time fraction) and datetimes (a date with time fraction). For the latter one, three different forms are possible.

## Date

A calendar date with no time component, i.e. only year, month and date are set. It is not possible to assign a concrete timezone to a date, so that dates do always appear in the timezone of the actual viewer. *All-day* events start- and end on a date. This characteristic is called *floating*, and needs to be handled appropriately whenever concrete periods of such *all-day* events need to be considered, e.g. during free/busy lookups or conflict checks.

**Date**

  - Used for *all-day* events
  - Are *floating*, implicitly
    - Example in iCal: ``DTSTART:19980118``
    - In the database, the unix timestamp is stored, and the timezone is set to ``NULL``, additionally the ``allDay`` column is ``true``
    - In the HTTP-API, only the ``value`` property is set, without time-fraction, e.g.: { "value":"19980118" }

## Date with Time

A precise calendar date and time consisting of day, year, month, date, hour, minute, second. A datetime may be defined in three different forms, which are **local time** (no specific timezone), in **UTC**, or in **local time with timezone reference**. 

1. **Local time** - no specific timezone
    - Used for *floating* events
    - Represent the same year, month, day, hour, minute, second no matter in what time zone they are observed
    - Example in iCal: ``DTSTART:19980118T230000``
    - In the database, the unix timestamp is stored, and the timezone is set to ``NULL``
    - In the HTTP-API, only the ``value`` property is set, e.g.: ``{ "value":"19980118T230000" }``

2. **UTC** - UTC timezone
    - Easiest form, but not much useful for events
    - Example in iCal: ``DTSTART:19980118T230000Z``
    - In the database, the unix timestamp is stored, and the timezone is set to ``UTC``
    - In the HTTP-API, the ``value`` property is set with a trailing ``Z`` as in RFC 5545, e.g.: ``{ "value":"19980118T230000Z" }``

3. **Local time with Timezone reference** - specific timezone
    - Most common form for events
    - Example in iCal: ``DTSTART;TZID=America/New_York:19980119T020000``
    - In the database, the unix timestamp ans the timezone are stored
    - In the HTTP-API, an additional ``tzid`` property is included, e.g.: ``{ "value":"19980118T230000", "tzid":"America/New_York" }``

## Usage

Internally, throughout the new Chronos stack, a DateTime object from the 3rd party library ``rfc5545-datetime`` is used, where dedicated support for the different modes are available. In the database, we have separate columns for the actual value of the start- and end-date of an event, the associated timezones and the *all-day* flag. 

In the HTTP-API, the start- and end-date properties of events are serialized within a JSON object, which is based on the definitions in RFC 5545, as follows:

```js
DateTimeData {
  value (string, optional): A date-time value without timezone information as specified in rfc 5545 chapter 3.3.5. E.g. "20170708T220000" ,
  tzid (string, optional): A timezone identifier. E.g. "America/New_York"
}  
```

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc5545
- https://github.com/dmfs/rfc5545-datetime
- https://devguide.calconnect.org/Handling-Dates-and-Times/
- https://tools.ietf.org/html/rfc4791#section-7.3

***


# Timezones

Timezones play an important role in calendaring and scheduling, as they allow to define an instant of time in a certain geopolitical region, relative to the *Coordinated Universal Time* (UTC). Especially when scheduling events with attendees that are located in different timezones, it is important to derive the same point of time of the event in each timezone, based on the underlying timezone definitions.   

## Internal Handling

Whenever events whose start- and enddate are decorated with a specific timezone identifier, the concrete instant of time is evaluated dynamically based on the corresponding timezone information from the Java runtime environment. This instant of time is then used for all sorts of operations with the start- and endtime, e.g. scheduling, recurrence calculations, free/busy lookups, sorting and so on.

The matching timezone is looked up based on the *timezone identifier*, which is usually a string in the form "continent/city" (for example "America/New_York"), as defined in the in the Olson Timezone Database, which is the most common internationally agreed standard for timezones. Therefore, it is required that all used timezone identifiers are available in the server, which is normally the case if the Java runtime environment is updated regularly. 

## Parsing

Not all clients are using the same set of timezone definitions, and especially clients that do not use timezones from the common Olson database may be problematic as an internally known timezone needs to be selected so that it can be processed by the server. The most prominent example are clients from Windows, which often rely on a Windows-internal set of timezone definitions that use different identifiers. 

In order to also accept non-Olson timezones, such unknown timezones are attempted to be mapped in the following, best-effort way:

- If an unknown timezone is parsed during an update operation, and the parsed timezone has the same rules as the timezone of the originally set timezone, fall back to the original timezone
- If an unknown timezone is parsed, and the parsed timezone has the same rules as the timezone of the calendar user, fall back to the timezone of the calendar user
- If an unknown timezone is parsed, and the parsed timezone has the same rules as the timezone of the current session's user, fall back to the timezone of the session user
- If an unknown timezone is parsed, and a known mapping from the parsed Windows timezone identifier to Olson exists, use the mapped Olson timezone
- If an unknown timezone is parsed, and at least one known timezone with the same rules exists, use the timezone whose identifier is most similar (*Levenshtein distance*) to the parsed one
- Use the calendar user timezone, otherwise

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc5545
- https://devguide.calconnect.org/Time-Zones/Time-Zones/
- http://www.twinsun.com/tz/tz-link.htm
- http://www.oracle.com/technetwork/java/javase/timezones-137583.html
- com.openexchange.chronos.impl.Utils#selectTimeZone
- http://unicode.org/repos/cldr/trunk/common/supplemental/windowsZones.xml

***


# Relation of Organizer / Principal / Folder-Owner / Creator

## Terminology

**Folder-Owner** ~ The owner of a personal calendar folder

- In OX, this is the user with the identifier of the folder's ``createdBy`` property, if the folder type is *private*
- In CalDAV, this is expressed in the calendar collection's ``calendar-owner`` property:  
  RFC 4791, 12.1: *This property is used for browsing clients to find out the user, group or resource for which the calendar events are scheduled. Sometimes the calendar is a user's calendar, in which case the value SHOULD be the user's principal URL from WebDAV ACL. (In this case the DAV:owner property probably has the same principal URL value.)*
- In Outlook, this owner is usually called the *principal*, which is the principal name for the mailbox user.

**Organizer** ~ The organizer of an event

- In OX (legacy), this is the user who created the event
- In iCalendar, this is the ``ORGANIZER`` property and points to the organizer of the event
- In CalDAV, the organizer determines the type of scheduling object resources:  
  RFC 6638, 3.1: *A calendar object resource is considered to be a valid organizer scheduling object resource if the "ORGANIZER" iCalendar property is present and set in all the calendar components to a value that matches one of the calendar user addresses of the owner of the calendar collection.* and *A calendar object resource is considered to be a valid attendee scheduling object resource if the "ORGANIZER" iCalendar property is present and set in all the calendar components to the same value and doesn't match one of the calendar user addresses of the owner of the calendar collection, and if at least one of the "ATTENDEE" iCalendar property values matches one of the calendar user addresses of the owner of the calendar collection.*  
  So, for newly created events, the ``ORGANIZER`` property of the iCal data must match the *owner* of the parent calendar collection.
- In Outlook, this is the meeting organizer


## Difficulties
In case an event is created within the personal calendar of a specific user, this user is automatically also the organizer of the event. This is the most common case and is easy to handle and understand.
Difficulties arise as soon as an event is created in the personal calendar folder of another user. Then, the user is acting **on behalf** of the calendar owner. This is often referred-to as the *secretary* functionality in the calendar, where the manager grants full permissions to his secretary to manage his appointments. In this case, some special handling applies towards the assignment and interpretation of the organizer attribute.

## Legacy: Organizer and Principal
The legacy calendar implementation used the properties "principal" and "organizer" for storing an event's organizer and a possibly different owner. 
The ``organizer`` (and ``organizerId``) always holds the user who creates the appointment, regardless of the parent folder. The ``principal`` (and ``principalId``, respectively) properties are set to the parent calendar folder's owner, in case it is a personal calendar that is *shared* by another user.
The properties are stored as-is in the database, and are accessible in the same way via the HTTP API. The ``createdBy`` property is set to the identifier of the folder owner as well.

**Example:**

- Eva (id 5) has granted read/write permissions for his personal calendar folder to Tobias (id 4)
- Tobias creates a new appointment in Eva's calendar
- Database: ``created_from=5,principalId=5,principal='eva@local.ox',organizerId=4,organizer='tobias@local.ox'``
- HTTP API: ``"created_by":5,"principal":"eva@local.ox","organizerId":4,"organizer":"tobias@local.ox"`` 

## Chronos: Organizer and sent-by

In the Chronos stack, there's no dedicated "principal". Instead, it is ensured that the organizer is always the actual calendar owner for newly created events (An exception to this rule are imported scheduling object resources from external organizers, as described at RFC 6638, section 3.2.2.2). In case the event is created on behalf of the folder owner by another calendar user (e.g. the secretary), this is expressed via the ``SENT-BY`` attribute within the organizer.       

## Conversion 

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
  
***

**_References / further reading:_**

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

***


# Per-Attendee delete exceptions

As invited attendees may delete a meeting from their personal calendar if they do not want to attend ("decline, and remove me from attendee list"), they may also do so for specific occurrences of a recurring event series. From the organizer's and the other attendee's point of view, this leads to a new change exception event with an updated list of attendees (with this deleting attendee being no longer listed there). However, for the attendee who has deleted a specific occurrence of the series, this rather means the creation of a new delete exception in the event series. 

According to the RFC 6638, in such a scenario the attendee effectively gets a different set of delete exception dates (``EXDATE`` property in iCal), while the organizer and the other attendees see this exception date as overridden instance (change exception): 

> As another example, an "Attendee" could be excluded from one instance of a recurring event.  In that case, the organizer scheduling object resource will include an overridden instance with an "ATTENDEE" list that does not include the "Attendee" being excluded.  Any scheduling messages delivered to the "Attendee" will not specify the overridden instance but rather will include an "EXDATE" property in the "master" component that defines the recurrence set.  

While appropriate handling has originally been in place as incoming/outgoing "patches" within the CalDAV implementation, this is now considered directly within the Chronos service itself, so that the change- and delete-exception arrays in series events may be different based on the actual calendar user.

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc6638#section-3.2.6
- com.openexchange.chronos.impl.Utils.applyExceptionDates(CalendarStorage, Event, int)
- com.openexchange.chronos.impl.performer.UpdatePerformer.updateDeleteExceptions(Event, Event)

***
 

# Classification / Private flag

The legacy *private* flag (``pflag`` in database) is used to hide sensitive details from appointments to other users. Participants of the appointment may always see all details of such appointments, while other users who are able to access such appointments based to their permissions (e.g. in shared folders) will only have a restricted view on them. This basically includes the start- and end-time, identifying properties such as the ``UID``, and the appointment's *shown-as* value. Instead of the appointment title, usually "Private" is shown instead.

In iCalendar, this relates to the classification property of an event, with the possible values ``PUBLIC`` (default), ``PRIVATE`` and ``CONFIDENTIAL``. While documentation about the exact meanings of ``PRIVATE`` and ``CONFIDENTIAL`` are quite rare, the legacy *private* flag best matches the semantics of ``CONFIDENTIAL``, i.e. only start- and end-times of the events are visible when being read by non-participating users. So, the legacy *private* flag will be converted to the classification ``CONFIDENTIAL``; vice-versa, both ``PRIVATE`` and ``CONFIDENTIAL`` will make the *private* flag ``true``. Consequently, the parameter ``showPrivate`` of the legacy HTTP API is applied to ``CONFIDENTIAL``ly marked events.  

Internally, the following semantics apply for the classifications:

- ``PUBLIC`` (default)
  No special handling, i.e. all event properties are exposed to non-attending users in shared folders. 
- ``CONFIDENTIAL``
  Only certain non-classified event properties are exposed to non-attending users in shared folders (basically all date- and time-related properties as well as administrative fields like identifiers), so that effectively such events appear as anonymous 'blocks' when being viewed in a calendar. The event's summary is replaced with the static text "Private". Additionally, such events are still considered in conflict- and free/busy-queries.
- ``PRIVATE``
  Events are not exposed to non-attending users in shared folders at all. Additionally, such events are only considered in conflict- and free/busy-queries in case of an attending resource attendee. 

For synchronization via CalDAV, the event classification is passed and read *as-is*, with the exception of the Apple calendar clients who require some special treatment (in form of a proprietary spec) in this topic. When exporting calendar object resources whose classification is different from ``PUBLIC`` for an Apple client, the parent ``VCALENDAR`` component is decorated with an additional property ``X-CALENDARSERVER-ACCESS``, set to the value of the (series master) event's classification. In the same way during import, the passed value of ``X-CALENDARSERVER-ACCESS`` is considered and transferred from the parent ``VCALENDAR`` component to each contained ``VEVENT``.

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc5545#section-3.8.1.3
- https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-privateevents.txt
- http://blog.coeno.com/offentliche-private-und-vertrauliche-kalenderereignisse-und-wie-man-sie-richtig-nutzt/
- com.openexchange.chronos.Classification
- com.openexchange.chronos.compat.Event2Appointment.getPrivateFlag(Classification)
- com.openexchange.chronos.compat.Appointment2Event.getClassification(boolean)
- com.openexchange.chronos.impl.Check.classificationIsValid(Classification, UserizedFolder)
- com.openexchange.chronos.impl.Utils.NON_CLASSIFIED_FIELDS

***


# Move between Folders

Whenever events are moved between different folders, some special handling applies, especially for move operations between different *types* of folders. We basically differentiate between two types of folders, which are on the one hand *personal* calendar folders of the internal users, and *public* folders on the other hand that are not directly associated explicitly with a user. The following gives an overview about the possible move actions and their outcome.

## General restrictions

The user performing the move must be equipped with appropriate permissions for the source- and target folder. For the source folder, these are at least *Read folder*, *Read own/all objects*, *Write own/all objects* and *Delete own/all objects*. *All* or *own* depends on the event being created by a different user or not. In the target folder, at least the following permissions must be granted: *Create objects in folder*. 

In *group-scheduled* events with multiple attendees, the calendar user's *role* in the event is checked as well, i.e. it's required to be the organizer of the event (see "Permissions" below. Additionally, recurring event series (or change exception events) cannot be moved. And finally, there's a limitation towards moving events with a classification of ``PRIVATE`` or ``CONFIDENTIAL`` (which correlates to the legacy *private* flag). In this case, events must not be moved to a *public* folder, or moved between personal folders of different users (~ *shared* to *private* and vice versa).

## Move Sceanrios

After the general restrictions have been checked and are fulfilled, the following happens depending on the source- and target folder type. Implicitly, this also includes triggering of further updates, like updating the folder informations for stored alarm triggers or inserting tombstone objects in the database so that the deletion from the source folder can be successfully tracked by differential synchronization protocols.    

### Public calendar folder 1 -> Public calendar folder 2
- Update the common public folder identifier of the event

### Personal calendar folder 1 of User A -> Personal calendar folder 2 of same User A
- Update attendee A's parent folder identifier

### Personal calendar folder of User A -> Personal calendar folder of other User B (non *group-scheduled* event)
- Update the common folder identifier of the event
- Update the calendar user of the event to user B

### Personal calendar folder of User A -> Personal calendar folder of other User B ("pseudo" *group-scheduled* event with one attendee/organizer)
- Update the original calendar user attendee A to user attendee B and take over his target folder identifier

### Personal calendar folder of User A -> Personal calendar folder of other User B (*group-scheduled* event with multiple attendees)
- Not allowed to avoid ambiguities

### Public calendar folder -> Personal calendar folder of User A
- Update attendee A's parent folder identifier accordingly
- Assign default parent folder identifier for all other user attendees 

### Personal calendar folder of User A -> Public calendar folder
- Take over common public folder identifier for all user attendees 

***

**_References / further reading:_**

- com.openexchange.chronos.impl.performer.MovePerformer
- https://intranet.open-xchange.com/wiki/backend-team:info:calendar#move_appointments_between_folders
- com.openexchange.ajax.appointment.MoveTestNew

***

   
# Conversion of Recurrence Rules

Within the Chronos stack, the recurrence information is transferred and treated as plain ``RRULE`` string, like it is defined in RFC 5545. Any series calculations are performed ad-hoc, under the constraints of some further influencing properties (start-date, start timezone, all-day flag) of the series master event. In the legacy implementation, the recurrence information was split over different properties in the class ``CalendarDataObject``, and provided as such to clients accessing the HTTP API. In particular, the properties ``recurrence_type``, ``days``, ``day_in_month``, ``month``, ``interval`` and ``until``. In the database, this recurrence information is stored in a combined field as so-called *series pattern*. 

For interoperability with the old API and database format, distinct conversion routines are in place, which were built upon already existing (de-)serialization routines to and from iCalendar. This also means that consequently still only those recurrence rules are supported that can also be expressed with the legacy series pattern, at least as long as the data is stored in this format. However, any recurrence calculations in the Chronos stack do already support all ``RRULE`` parts as described in RFC 5545.

## Supported ``RRULE`` parts

The following list gives an overview about the supported ``RRULE`` parts, based on the limitations described before.

- ``FREQ:DAILY``: ``INTERVAL``, ``UNTIL``, ``COUNT``
- ``FREQ:WEEKLY``: ``INTERVAL``, ``UNTIL``, ``COUNT``, ``BYDAY``
- ``FREQ:MONTHLY``: ``INTERVAL``, ``UNTIL``, ``COUNT``, ``BYDAY``, ``BYSETPOS``, ``BYMONTHDAY``
- ``FREQ:YEARLY``: ``INTERVAL``, ``UNTIL``, ``COUNT``, ``BYDAY``, ``BYMONTH``, ``BYSETPOS``, ``BYMONTHDAY``

## ``UNTIL`` handling

If an event series is decorated with an end date, this used to be stored as *the date of the last occurrence without time fraction in milliseconds since the Unix epoch*. However, in RFC 5545, the corresponding ``UNTIL`` part of a ``RRULE`` is just defined as 
> The UNTIL rule part defines a DATE or DATE-TIME value that bounds the recurrence rule in an inclusive manner.

Therefore, some information might currently get lost when converting a recurrence rule to a legacy series pattern, since the time fraction of the date-time value from the ``RRULE`` has to be truncated prior saving. Also, some special checks need to be in place during conversion to prevent an additional occurrence if the client-supplied ``UNTIL`` lies behind the last occurrence (and possibly right before the start date of a next occurrence).  

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc5545#section-3.3.10
- https://intranet.open-xchange.com/wiki/backend-team:info:calendar#serientermine
- com.openexchange.groupware.calendar.CalendarDataObject
- com.openexchange.chronos.compat.Recurrence
- com.openexchange.chronos.compat.Recurrence.getSeriesEnd

***


# Reset of Participation Status

Whenever an existing event with attendees is *rescheduled*, each attendee's participation status is reset to ``NEEDS-ACTION``. A reschedule occurs, when any ``DTSTART``, ``DTEND``, ``DURATION``, ``RRULE``, ``RDATE``, or ``EXDATE`` property changes such that existing recurrence instances are impacted by the changes (RFC 6638, 3.2.8) - i.e., whenever the period of an event changes. 

## Internal Handling

Internally, these kind of checks are performed along with each event update is checked. Optionally, the standard allows to behave similarly in case of other changes, e.g. a changed event location, however, currently only time changes are considered.  

In contrast to the rather strict definition in the standard, the following, slightly adjusted rules are applied when determining if the attendee's participation status will be resetted or not:

- For series events, reset if there are 'new' occurrences (caused by a modified or extended rule)
- For series events, reset if there are 'new' occurrences (caused by the reinstatement of previous delete exceptions)
- Reset if updated start is before the original start
- Reset if updated end is after the original end
- Don't reset otherwise

Doing so, the parstats are not resetted whenever the event's effective timeframe is reduced, e.g. the event is re-scheduled to start half an hour later, or end a couple of minutes earlier.

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc6638#section-3.2.8
- com.openexchange.chronos.impl.performer.EventUpdateProcessor#needsParticipationStatusReset

***


# Reset of Change Exceptions

When an event series is updated, there are situations where all previously created change exceptions need to be reset, i.e. they get removed so that there are only regular occurrences again. In the legacy implementation, this has always been the case whenever the series pattern is changed (in any way), or the recurring master's start-/endtime are updated. Implicitly, this would also reset any participation status of the attendees, see above. 

In the Chronos stack, a slightly enhanced approach is used so that not necessarily all previous change- and delete-exceptions are lost along with an updated recurrence rule. Now, whenever the recurrence rule is changed, the implementation checks if there are existing exceptions whose ``RECURRENCE-ID`` would still be matched by the recurrence set of the updated rule. If so, the change- or delete-exception is kept, otherwise it is still removed. In other words, if the original start time of an existing exception would still be a occurrence of the new recurrence rule, then it can be preserved. For example, any exceptions prior an updated ``UNTIL`` or ``COUNT`` part survive the update. Or, when changing a previously ``DAILY`` rule to only occur on *working days*, any previous exceptions on those days are kept.      

Additionally, it is now possible to apply a changed recurrence rule to a specific and all future occurrences of the series, which is handled by splitting the event series into two parts - see below for more details.

***

**_References / further reading:_**

- com.openexchange.calendar.api.CalendarCollection.detectTimeChange
- com.openexchange.calendar.CalendarOperation.checkPatternChange
- com.openexchange.calendar.CalendarMySQL.deleteAllRecurringExceptions

***


# Smart update of Event Series

There are cases where changes to an event series should be performed for one recurrence instance on to the future. For example, when a new member is added for a regular team meeting, or, if the location of an event changes. In order to support these use cases, the server supports splitting event series into two parts, as well as propagating series changes to existing change exceptions.

## Series Split

Splitting an existing event series into two parts is the basis to support use cases where changes to an event series should be applied from a specific occurrence on to the future. The logic to split event series is based on a preliminary specification of the Apple Calendar Server (see links below). Such a split is performed at a specific *split point* which is typically the recurrence identifier of the occurrence that is considered for the update. 

The split is actually performed by creating a new, detached event series for the part prior the split point, and shifting the date of the first occurrence of the existing series to the split point. Existing overridden instances and any per-attendee data is preserved in both resulting event series, additionally, the series are linked via the ``RELATED-TO`` property. 

Technically, the following steps are performed when an event series is splitted:

1. A new series event representing the 'detached' part prior to the split time is created, based on the original series master
2. The recurrence rule of the detached series is adjusted to have a fixed ``UNTIL`` one second or day prior the split point
3. The recurrence rule, start- and end-date for the existing event series are modified to begin on or after the split point
4. Existing delete and change exception dates are distributed between both series (prior / on or after the split time)
5. Existing overridden occurrences are assigned to the new detached event series, if prior split time
6. Both series are decorated with the same, newly generated ``RELATED-TO`` property 

Afterwards, typically the new event series is adjusted in a second step, so that a client effectively can perform an update to an event series that should be applied to a specific and all future occurrences.

## Propagate changes to Exceptions

As described above, when dealing with recurring events, it is often necessary to apply a change from one occurrence on into the future (e.g., add a new attendee part way through the series of meetings).

Previously, existing change exceptions are not touched at all when the series master event is modified. In the calendar implementation, we integrated some smart detection if a change to the recurring master event could also be applied to existing change excpetions, e.g. after a new attendee is added, or if the event location changes. As the web client does not have all change exceptions handy, the logic to 'propagate' the changes is implemented in the middleware.

To avoid possible ambiguities, only certain changes considered, where the change can also be applied in some or all change exceptions intuitively. In particular, the following cases are taken into account:

- For all *simple* changed event fields, it is checked if the modified property is equal in the original series master event and in the change exception. Simple event fields are (preliminary): ``CLASSIFICATION``, ``TRANSP``, ``SUMMARY``, ``LOCATION``, ``DESCRIPTION``, ``CATEGORIES``, ``COLOR``, ``URL``, ``GEO``, ``TRANSP``, ``STATUS``. If not, leave the property in the change exception as-is (i.e. do not propagate this change). If yes, also apply the change in the change exception.
- Newly added attendees are also added in existing change exception events, unless they're not already attending there.
- Removed attendees are also removed from change exceptions, in case they previously attended there, too.
- For changes to an event's start- and/or enddate, the same change is only propagated if both properties are equal to the original value in the change exception, i.e. the change exception's timeslot is still matching the recurrence.

***

**_References / further reading:_**

- https://raw.githubusercontent.com/apple/ccs-calendarserver/master/doc/Extensions/caldav-recursplit.txt
- com.openexchange.chronos.impl.performer.EventUpdateProcessor#propagateToChangeExceptions
- com.openexchange.chronos.impl.performer#SplitPerformer

***


# External Calendar Users

At many places, one needs to differentiate between *internal* and *external* calendar users. Internal calendar users are all provisioned groupware entities within the same context, i.e. all resources, users and groups. All other calendar users are treated as external entities. This is especially also true for entities from other contexts of the same installation.

## Scheduling

Currently, all external calendar users require a valid e-mail address that can be used for scheduling via iMIP. This is enforced by validating the URIs of external attendees prior saving. Attempting to save or update an event containing an external calendar user w/o valid e-mail address will fail; the check may be skipped when setting ``com.openexchange.chronos.skipExternalAttendeeURIChecks`` to ``false``.

## URI Encoding / International Domain Names (IDN)

E-Mail addresses of calendar users may contain non-ASCII characters, e.g. from international domain names like *müller.com*. When being used in a calendar user's URI along with the ``mailto:`` scheme, they are usually transferred as percent-encoded UTF-8. For example, for an attendee with e-mail address "horst@müller.com", the value of the corresponding ``ATTENDEE`` property would look like the following (while the ``CN`` parameter would still just use the UTF-8 string as-is):

> ``ATTENDEE;CN="Horst Müller";CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION:mailto:horst@m%C3%BCller.com``

Internally within the Chronos stack, all URIs are kept as (escaped) URI string, which would match the representation of the corresponding value in an iCalendar ``ATTENDEE`` or ``ORGANIZER`` property.

When being converted back to a plain e-mail address string (as used for external participants in the legacy stack), such URIs are decoded implicitly; additionally, ASCII-encoded (punycode) addresses with international domain names (IDN) are converted back to their unicode representation, too. This is the case when converting to an external participant in the legacy HTTP API, as well as when storing such an attendee in the ``dateExternal`` table. 

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc5545#section-3.3.3
- com.openexchange.chronos.common.CalendarUtils.extractEMailAddress(String)
- com.openexchange.chronos.impl.Check.requireValidEMail(T)

***


# Provider/Account Framework

Besides the default, internal calendar, there may be further calendar sources that should be integrated into a user's calendar module. For example, calendar feeds from external sources, or a virtual calendar containing the upcoming birthdays found in the user's address book. Therefore, a new layer is introduced that provides access to all available calendar *accounts* from different *providers* of a user using the same API.

## Calendar Providers and Accounts

A calendar provider implements the functionality to access the calendar data of a specific calendar source. Calendar access is always bound to a specific account of a user within the calendar provider, i.e. each calendar provider provides a number of accounts for different users. A user may have a fixed number of accounts (e.g. exactly one) within a concrete provider, or there can be multiple accounts for the user. 

Have a look at the chapter 'Default implementations' to get an overview over providers shipped by Open-Xchange.

## External Calendar Account Caching

The default calendar providers implemented by Open-Xchange (like ICal, SchedJoules, ...) use a generic caching approach to avoid expensive calls to external resources as much as possible. This means that events (and all related information like attendees and alarms) from the external resource gets persisted like events from the internal calendar after their first retrieval. All upcoming requests will be answered by the cache until a defined refresh interval is exceeded.

The caching layer itself is able to handle information on a per-folder base. Cached folders that have been deleted on the remote site will be removed immediately from the cache while new folders on remote will be cached instantly. 

In general the cache should be a blackbox for those implementations using it. Of course there are some utilities and hooks for the provider to be able to invalidate the folder cache as the cache doesn't know all possible cases.

### Refreshing cache

Each Calendar Provider implementation can use its own refresh interval, again on a per-folder base. These value defines how long the cache should be used to answer requests for a dedicated folder of the underlying calendar. After the interval for the folder is exceeded the cache state will be updated by contacting the external calendar. New events will be added, removed ones will be deleted and changed events will be updated. 

The refresh interval is defined in minutes. If the calendar provider implementation does provide a value bigger than 0 this will be taken into account. Otherwise the default of one day will be used.

In cases where new folders have been added or previously existing folders have been removed on the external calendar site the caching layer will directly update its internal state which means changes on the folder structure will be instantly visible without considering the refresh interval!

### Error handling

The caching allows to handle data that will normally fail to be executed. Among other things the following error cases will be handled by the generic caching layer:

- data truncation: external data that is too big to be stored will be truncated so that no import will fail.
- corrupt data: data that won't met the requirements to be stored will be handled as smooth as possible. For instance if start or timestamp of an event is missing the complete event will be ignored.
- missing UID: if an event lacks on appropriate UID definitions it is not possible to generate a reproducible diff for already persisted and externally available events. Due to this fact the caching will delete all persisted events for the folder and re-import those from the calendar provider.

As always the errors will be handled per folder and the underlying implementation has the opportunity to deal with it. In addition it can define an interval in which the external resource should not be requested after an error occurred. This prevents a continuous requests loop to the external provider which permanently will fail because of the same error. After the given interval time is exceeded the cache connects to the external provider again and hopefully the error is gone ...

## Default implementations

As already mentioned Open-Xchange provides some default implementations for external accounts.

### ICal Calendar Provider

Using the ICal Calendar Provider allows to subscribe to various remote calendar feeds that meet the requirements defined by the administrator (size, schemes, ...). The user just has to give an URI and optional credentials to subscribe the feed. To optimize requests to external resources the ICal Calendar Provider implementation is based on the generic caching layer described above. This means the calendar provider has to define its own refresh interval.

#### Refresh interval

In general the administrator is able to configure a refresh interval (by using the well known ConfigCascade) that will be used for each subscribed feed. As feeds might also define a refresh interval (by having 'REFRESH-INTERVAL' set) this value will be used under the assumption that the feed does even better know which interval makes sense.

#### Cache invalidation

As the caching layer isn't aware of changes that should result in a cache invalidation the implementation has to call back in such cases. For the ICal implementation some user actions have to trigger such an invalidation. These actions can be summarized as account reconfiguration which contains endpoint (URI or auth) changes that might result in a complete new ICal subscription with other events.

#### Configuration

The following configuration can be made by the administrator:

- a maximal file size that is allowed for a feed: This ensures smooth processing of the feed by the Open-Xchange server and prevents undesired states. Each feed exceeding the limit will be denied. Per default the limit is set to 5MB.
- a host blacklist: Due to security reasons the blacklist avoids connections to nodes of the entire network. Per default the blacklist contains 127.0.0.1-127.255.255.255 and localhost.
- supported schemes: Due to security reasons (for instance avoid 'file') only a defined set of schemes should be supported. Per default these are http, https and webcal
- various connection parameters: This contains timeouts and a maximum number of connections.

Have a look at the property documentation page on http://documentation.open-xchange.com for more details.

### SchedJoules

The SchedJoules calendar provider provides the ability to subscribe public calendars from the SchedJoules servers. The fetched data is cached locally in memory of each middleware node to save bandwidth and speed up the request processing. The SchedJoules API uses an ``Authorization`` header with an API key which is configured on the middleware side and is being transmitted on every request the middleware node is firing towards the SchedJoules servers.

### Google Calendar

The google calendar provider provides the possibility to subscribe to the primary calendar of a google account. The only requirement is a working google oauth account with the 'calendar_ro' scope.
To reduce the amount of data transported from google to the the middleware the google provider uses the incremental update feature of the google calendar. After the initial synchronization the google provider only requests the changes from the google server. This allows the use of a shorter refresh interval and therefore leads to more up-to-date data.

Please also note that old google subscription are migrated and removed afterwards. So they are lost in case of a downgrade.

## Birthdays Calendar

The Birthdays Calendar is a calendar that automatically populates with the known birthdays of everyone in a user's address books. A corresponding calendar account is added by default for each user once that has the ``contacts`` module permission. However, this can be disabled beforehand or afterwards by removing the ``calendar_birthdays`` capability via config-cascade.

The calendar works by providing a yearly event series for each contact that has a set birthday property. The calendar data is always created on the fly dynamically by querying the contact storage, i.e. no additional event data is actually stored. However, it is still possible to add alarms for birthday events, which are then stored within the usual calendar storage. 

Since the underlying calendar account is automatically provisioned, it cannot be deleted by the user - though the folder may still be unsubscribed to hide it from the user interface. 


# Group-scheduled Events

In iCalendar, there is a strict separation between simple events without further attendees in a user's calendar, and so-called *group-scheduled* events. Group-scheduled events are *meetings* with a defined organizer and one or more attendees, while not group-scheduled ones are *published* events, or events in a single user's calendar only. 

## Legacy: Implicit Participant

In the legacy implementation, appointments were always stored with at least the calendar user being a participant. This has the side effect of the actual parent folder information always being stored along with the participants, and not within the appointment itself (except for the fixed parent folder identifier for appointments in public folders). Doing so, lookups based on the parent folder could be performed by matching against the attendee's folders for private and shared folders. 

Furthermore, since also simple, not group-scheduled appointments were stored with the calendar user as participant, the list of "all appointments of a certain user" could be built up in a very effective way. This also aided free/busy lookups and conflict checks, respectively.    

However, based on RFC 5545, this is handling was **wrong** (the CalDAV layer already tried to work around this difference with some patches to remove this implicit attendee during export, and re-apply it during import again).   

## Chronos: No implicit Participant (possible)

While not necessarily needed, for now events are still stored using the calendar user as implicit participant, so that compatibility can be guaranteed for the legacy data structure and for existing clients. For CalDAV, the same workarounds as described above are still in place. 

Eventually, once no backwards compatibility is needed anymore, this quirk will be removed, i.e. we'll no longer add the current calendar user as attendee and organizer implicitly in case no further attendees are defined. 

***

**_References / further reading:_**

- https://bugs.horde.org/ticket/10697
- com.openexchange.chronos.impl.Utils#isEnforceDefaultAttendee

***


# Event Flags

For displaying events in the user interface, or to enable/disable appropriate actions for events, clients typically need to query multiple properties of events from the server. This includes the obvious properties like summary, start- or enddate, but also further properties to determine if the calendar user is the organizer of the event or an attendee, or information about the user's own participation status. 

Previously, a client needed to fetch virtually all available properties when loading events from the server, and then performed multiple implicit checks dynamically, e.g. to decide whether the background is drawn "striped" or not, or if events can be "dragged" around in the grid. However, this caused some significant amount of data to be transferred between server and client, and also required some evaluations in the client which are rather in the calendar server's domain, especially towards the handling of different roles within group-scheduled events.

Therefore, a new, virtual *read-only* property for events was introduced: event *flags*.

Via this property, events will get decorated with different aspects that are relevant for the client, e.g. "has attachments", "is recurring", "has alarm(s)", "is organizer", and so on. In particular, the following event flags are supported:
 
- ``attachment``: The event contains at least one attachment. 
- ``alarms``: The calendar user has at least one alarm associated with the event.
- ``scheduled``: Event is a *group-scheduled* meeting with an organizer.
- ``organizer``: The calendar user is the *organizer* of the meeting. 
- ``attendee``: The calendar user is *attendee* of the meeting.
- ``private``: Event is classified *private*, so is invisible for others. 
- ``confidential``: Event is classified as *confidential*, so only start and end time are visible for others.
- ``transparent``: Event is *transparent* for the calendar user, i.e. invisible to free/busy time searches.
- ``event_tentative``: Indicates that the event's overall status is *tentative*.
- ``event_confirmed``: Indicates that the event's overall status is *definite*.
- ``event_cancelled``: Indicates that the event's overall status is *canceled*.
- ``needs_action``: The calendar user's participation status is *needs action*. 
- ``accepted``: The calendar user's participation status is *accepted*.
- ``declined``: The calendar user's participation status is *declined*.
- ``tentative``: The calendar user's participation status is *tentative*.
- ``delegated``: The calendar user's participation status is *delegated*.
- ``series``: The event represents the *master* of a recurring event series, or an expanded (regular) occurrence of a series. 
- ``overridden``: The event represents an exception / overridden instance of a recurring event series.
- ``first_occurrence``: The event represents the *first* occurrence of a recurring event series.
- ``last_occurrence``: The event represents the *last* occurrence of a recurring event series.

***

**_References / further reading:_**

- com.openexchange.chronos.EventFlag
- com.openexchange.chronos.common.CalendarUtils#getFlags

***


# Last-Modified / Created

In the legacy stack, the properties that store the creation- and last-modification time of an event were handled by the server implicitly each time the entry was written in the database, so that they're actually *read-only* for clients. Similarly, the entity identifiers of the acting user were stored along as created- and modified-by. Additionally, the last-modified property was also used to prevent so-called *lost updates*, in a way that clients were only allowed to update or delete an event where the last-modified timestamp matches the one of the version last read by this client. Also, the last-modified property was used to compute resource ETags and to drive the ``sync-collection`` report in CalDAV.

While letting the server assign those timestamps based on the current time automatically is feasible for events being initially created on that system, it is problematic whenever data is imported from an external source - e.g. a subscribed external calendar feed, or a manually imported iCalendar file. Also, the created- and modified-by properties would be rather misleading in such scenarios. Therefore, the new calendar implementation will handle those properties in the following way:

- The properties ``created-by`` and ``modified-by`` are no longer mandatory (i.e. may be ``NULL``ed if not applicable)
- If set, the properties ``created-by`` and ``modified-by`` still refer to internal calendar users
- The properties ``last-modified`` and ``created`` are no longer mandatory (i.e. may be ``NULL``ed if not applicable)
- If set, the properties ``created`` and ``last-modified`` do not necessarily store the server timestamp the event was (last) written, and may contain values from an external source
- For synchronization purposes, an additional synthetic property ``timestamp`` is introduced, which is set to the current server timestamp whenever the event is written

For backwards compatibility, the new ``timestamp`` property is used to drive the ``last-modified`` property in the legacy HTTP API. Additionally, due to the ``NOT NULL`` column definitions, the respective values in the legacy storage are derived from the acting calendar user and the timestamp property as needed. 

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc5545#section-3.8.7
- http://oxpedia.org/index.php?title=HTTP_API#Date_and_time
- https://bugzilla.mozilla.org/show_bug.cgi?id=303663#c2

***


# Allowed Attendee Changes

Previously, only the user's permissions in the underlying folder were considered when checking if an event can be updated or not. For meetings with multiple attendees that usually appear in each of the attendee's default calendar folders, this meant that every internal user that attends a meeting was able to edit or delete the event. 

However, iCalendar standards require to consider different *roles* here - mainly depending on the calendar user being the organizer of an event or not. While the organizer can perform any changes to the event, the other attendees are quite limited regarding the allowed changes (see RFC 6638, section 3.2.2.1): 

> "Attendees" are allowed to make some changes to a scheduling object resource, though key properties such as start time, end time, location, and summary are typically under the control of the "Organizer".

In order to comply with the standards, the new calendaring stack introduces appropriate restrictions in case the user is not the organizer. Effectively, the permitted changes then boil down to modifications of the user's personal alarms and his own participation status. Additionally, the attendee is still allowed to remove himself from an event (beyond declining it). Those changes can also be performed on a single instance of a recurring event series (which may indirectly cause new change and/or delete exceptions for the series).

When acting on behalf of another user (i.e. the action is performed within a shared calendar folder), always this folder owner is considered when determining if the event is updated as organizer or attendee.

## HTTP API

In case a client attempts to modify a group-scheduled event in a not allowed way, an appropriate exception is thrown (code ``CAL-4038``). To aid the differentiation between attendee- and organizer-scheduling object resources, the event's flags contain additional hints (flags ``scheduled``, ``attendee`` and ``organizer``) which can be evaluated appropriately, in addition to the permissions of the folder the events is located in.

Additionally, to aid the typical *reply* of an attendee to a meeting request, a new, dedicated action ``updateAttendee`` has been introduced that allows to adjust the calendar user's *own* attendee property, as well as to apply his set of alarms for the event.

## CalDAV

In case a client attempts to modify a group-scheduled event using the CalDAV interface in a not allowed way, the request is answered with the ``CALDAV:allowed-attendee-scheduling-object-change`` precondition error. Besides the per-user properties defined in RFC 6638, there are no further exceptions. However, if a client attempts to store a non-standard *X*-property in the iCalendar resource, no error is thrown and the extended property is dropped silently (as clients actually do it, e.g. a custom ``X-APPLE-TRAVEL-ADVISORY-BEHAVIOR`` or ``X-LIC-ERROR``).

***

**_References / further reading:_**

- https://tools.ietf.org/html/rfc6638#section-3.2.2
- com.openexchange.chronos.impl.performer.AbstractUpdatePerformer#requireWritePermissions

***


# Permissions

When interacting with the calendar and scheduling subsystem, three different permission concepts need to be considered. On the one hand, the user's effective permissions in the folder (that represents the actual view on the event data) is taken into account, just like one is used to from other groupware modules. On the other hand, the user's *role* within a certain group-scheduled event affects the possible actions, i.e. if a user is also an attendee or the organizer of the event. The latter aspect basically introduces an additional layer of object permissions for events. On top of that, the classification of an event may be used to restrict access to certain properties of an event for other users. 

## Permissions by Role

As stated above, within *group-scheduled* events with multiple attendees, a calendar user can have certain roles - i.e. the user is *organizer* of the event, or he is an *attendee*. For events that are located in personal calendar folders (*shared* or *private*), these are actually the only possibilities. In *public* calendar folders, the user might also be neither organizer, nor attendee.

Considering the role, a user that is attendee or organizer of a group-scheduled event is able to read event data. Also, an attendee is always allowed to modify his own attendee property (especially his participation status), as well as he has control over his own alarms. Additionally, he may still delete himself from the attendee list. However, whenever event data should be manipulated in way beyond the allowed attendee changes (see above), the calendar user needs to be the organizer of the event.

The access rights that are implicitly given by the user's role in an event are always valid, even if the event is statically located in a (public) calendar folder that is not visible for the user. For example, the API allows to create an event in a *public* calendar folder and invite attendees that do not have access to this folder. Interaction with such events would still be possible (e.g. by using the corresponding links from the invitation mail, by looking up the events via search, or by querying virtual event collections for the user ("all my events").

Note that the *role* is always interpreted for the actual calendar user based on the folder that represents the current view on an event, which means that this calendar user may be different from the current session user in case of a *shared* calendar folder, while the calendar user equals the current session user in *private* and *public* folders. See chapter "Relation of Organizer / Principal / Folder-Owner / Creator" for further details. 

## Folder Permissions

For calendar folders, the same set of permissions can be applied as for folders in other groupware modules, i.e. users and groups can be added to the permission lists; each entity with an individual set of folder-/read-/write- and delete-permission. The effective folder permission of a specific user is the calculated maximum permission (based on the users own and/or any group entity permissions that are defined). 

In the calendar module, the folder permissions are primarily considered for *create* operations, since modifications and deletions of group-scheduled events are rather restricted based on the calendar user's particular role in the event once it has been created, see above. Additionally, in *shared* folders, where a user acts on behalf the calendar owner, the folder permissions determine which actions are allowed for the proxy user (besides of the restrictions based on the calendar user's role). 

## Permissions by Classification

The classification may be used to restrict access to the event as such, or to specific properties of an event, to other users that do not participate in the event as attendee. So, even if an event would be visible within a *shared* folder of the calendar user for other users, depending on the event's classification, it would be either invisible (for a ``PRIVATE`` classification), or only the event's period would appear (for a ``CONFIDENTIAL`` classification).

More details about the possible event classifications are described in chapter "Classification / Private flag" above.    

## HTTP API

Clients that need to be aware of which actions are actually possible by a certain calendar user for a group-scheduled event in a specific folder should consider the appropriate event flags ``scheduled``, ``organizer`` and ``attendee``, along with the effective permissions of the current session user in the underlying folder. Then, the following rules apply:

- New events can be created in case the folder permissions include at least *create own* objects.
- Whenever data should be manipulated in a way beyond the allowed attendee changes (see above), the calendar user needs to be the organizer of the event, hence the ``organizer`` flag would have to be present. Additionally, the folder permissions need to allow *write object* permissions for the event in question (i.e. either *write all* or *write own* objects).
- Whenever attendee-related data such as the participation status or the user's personal alarms should be modified, the calendar user needs to be an attendee of the event, hence the ``attendee`` flag would have to be present. No additional folder permissions are required.


# UTF-8 Support

The new calendaring stack uses new database tables to store event data. Data from existing events is migrated during the upgrade, see chapter "Migration of legacy data" above. The same database tables are also used for caching data of external calendar subscriptions.

The newly introduced database tables come with full support for storing all symbols from the Unicode character set, whose code points range from U+000000 to U+10FFFF. However, this may also require to adjust some MySQL configuration.

## UTF-8 in MySQL

UTF-8 is a variable-width encoding that encodes each symbol using one to four bytes, i.e. commonly used symbols with a lower code point (ASCII) are encoded using fewer bytes, while it is still possible to store BMP (U+000000 to U+00FFFF) and astral symbols (U+010000 to U+10FFFF). 

The default ``utf8`` character set of MySQL uses a maximum of three bytes per character, so it only allows unicode characters in the BMP range, while any supplementary astral symbols cannot be stored at all. Attempting to do so results in an incorrect string warning, which is treated as error by the middleware. Whenever 4-byte characters have to be stored, the extended character set ``utf8mb4`` is required.

## JDBC Connection Character Set

All strings sent from the Java middleware using the JDBC driver to the database are converted automatically from native Java Unicode to the client encoding. The encoding between client (Connector/J) and MySQL server is automatically detected upon connection. So, once the server advertises support for ``utf8mb4``, this will be negotiated automatically, hence there is no need to modify the driver settings (as defined in ``configdb.properties``).

So, in order to finally use 4-byte UTF-8 character sets, the MySQL server should be configured with ``character_set_server=utf8mb4``. Otherwise, the allowed characters may still be restricted to the BMP range (``utf8`` with three bytes per character), and 

## Replaying Storage

As noted above in "Migration of legacy data", the database will work temporarily in a mode where all write operations are 'replayed' to the legacy database tables after the migration is finished, to prevent data loss in case a downgrade should ever be required. As the previously used database tables do still not support astral symbols, those problematic characters are removed implicitly when storing them there. 

***

**_References / further reading:_**

- https://dev.mysql.com/doc/connector-j/en/connector-j-reference-charsets.html
- https://bugs.open-xchange.com/show_bug.cgi?id=54504
- https://confluence.open-xchange.com/display/MID/MySql+charsets+and+collations

***


# Import and Export

This chapter will describe the impact on calendar data during an import or an export of an calendar. 

## Import

During the import all information about attendees and the organizer will be ignored. The following case that lead to this decision.  

Lets consider the case that the event is organized by another entity than the user importing the event. Furthermore lets assume the user importing isn't attending the event (normally this is handled via iTIP or other internal mechanisms). Based on our current concept of "personal" calendar folders of internal user (see [Group-scheduled Events](#group-scheduled-events)) we aren't able to save the event without saving the current user as attendee. This means that the user would always become a 'party-crasher'. To avoid this the only feasible way is to remove the organizer. The attendees then get removed avoid the impression that the event was taken over by a new organizer. 


## Export
The export will only touch so called 'pseudo group scheduled' events. Events that are only attended by the organizer herself are considered 'pseudo group scheduled'. Those events will be exported without the organizer and the attendee information as those are only persisted to legacy reasons (see [Group-scheduled Events](#group-scheduled-events)). 

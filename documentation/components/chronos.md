---
title: Calendar v2 aka Chronos 
---


# Workflows

## Create Event

1. Get & check target folder

    - folder must exist
    - user must be able to access the folder
    - folder must be of module "calendar"
    - user must have "create objects" permissions

2. Prepare event data

    1. Prepare basic data
       - apply next object identifier
       - take over common public folder identifier if folder is of type "public"   
       - init sequence with 0
       - if no UID is set by client, apply random UID
       - if UID is set by client, check for uniqueness in context
       - set "created by" to current calendar user, creation date to "now"
       - set "modified by" to current calendar user, last modified date to "now"
       - if no organizer is set by client, set organizer to current calender user
    



 (a) Prepare basic data
   - apply next object identifier
   - take over common public folder identifier if folder is of type "public"   
   - init sequence with 0
   - if no UID is set by client, apply random UID
   - if UID is set by client, check for uniqueness in context
   - set "created by" to current calendar user, creation date to "now"
   - set "modified by" to current calendar user, last modified date to "now"
   - if no organizer is set by client, set organizer to current calender user
   
 (b) Prepare organizer data
   - if organizer is set by client, try and resolve to an internal entity
   - if "internal" organizer is set by client, check that it matches the calendar user
   - if "external" organizer is set by client, take over as-is
   - if session user != calendar user, take over session user in "sent-by" attribute
   
 (c) Prepare date/time data
   - check start- and enddate are specified, and start date is not after end date
   - if event is marked as "all-day", truncate the time fractions from start- and end-date (in UTC)
   - if event is marked as "all-day", and the truncated start- and endtime are equal, add one day to enddate
   - take over start/end timezone if set
   - if no timezone set, fallback to calendar user's configured timezone

 (d) Prepare further data
   - if classification is specified, check that it is allowed in target folder
     (in "public" calendar folders, the classification may only by "PUBLIC")
   - if no classification is specified, assume a PUBLIC classification
   - take over event status if set, otherwise fall back to "confirmed"
   - take over color if set
   
 (e) Prepare recurrence related data
   - if a recurrence rule is set, check it for validity
   - if a recurrence rule is set, take over event object identifier as series id
   - if delete exception dates are set, take them over after checking they are valid
   
 (f) Prepare further data
   - take over summary, location, description, categories, filename if set

4. Prepare attendee data

5. Check for conflicts

6.


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

### References / further reading
- https://tools.ietf.org/html/rfc5545
- https://devguide-calconnect.rhcloud.com/Handling-Dates-and-Times


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

In iCalendar, this relates to the classification property of an event, with the possible values ``PUBLIC`` (default), ``PRIVATE`` and ``CONFIDENTIAL``. While documentation about the exact meanings of ``PRIVATE`` and ``CONFIDENTIAL`` are quite rare, but the legacy *private* flag best matches the semantics of ``CONFIDENTIAL``, i.e. only start- and end-times of the events are visible when being read by non-participating users. So, the legacy *private* flag will be converted to the classification ``CONFIDENTIAL``; vice-versa, both ``PRIVATE`` and ``CONFIDENTIAL`` will make the *private* flag ``true``.

### References / further reading
- https://tools.ietf.org/html/rfc5545#section-3.8.1.3
- https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-privateevents.txt
- http://blog.coeno.com/offentliche-private-und-vertrauliche-kalenderereignisse-und-wie-man-sie-richtig-nutzt/
- com.openexchange.chronos.Classification
- com.openexchange.chronos.compat.Event2Appointment.getPrivateFlag(Classification)
- com.openexchange.chronos.compat.Appointment2Event.getClassification(boolean)
- com.openexchange.chronos.impl.Check.classificationIsValid(Classification, UserizedFolder)


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

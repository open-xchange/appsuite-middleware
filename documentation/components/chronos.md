---
title: Calendar v2 aka Chronos 
---


# Details

This section provides some deeper insights for certain topics.

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

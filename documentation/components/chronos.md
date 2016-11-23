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

In the Chronos stack, there's no dedicated "principal". Instead, it is ensured that the organizer is always the actual calendar owner for newly created events. An exception to this rule are imported scheduling object resources from external organizers. In case the event is created on behalf of the folder owner by another calendar user (e.g. the secretary), this is expressed via the ``SENT-BY`` attribute within the organizer.       

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
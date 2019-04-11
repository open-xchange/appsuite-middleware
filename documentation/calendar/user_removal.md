---
title: Removal of calendar data for a user
---

This article will describe the different effects on the calendar data when removing calendar access for a user.

# Overview
There are three different ways to remove calendar access for a user:

 * Downgrade a user or context
 * Delete the users context
 * Delete a user


# Downgrade a user or context 
When removing calendar access for a user or context no data will be removed. Only if the command line tool [DeleteInvisible][1] is executed the calendar data will be touched.

In case of a single user downgrade every event that is located within the private folder(s) will be removed. Events located in public folder will only be removed if there are no internal attendees left participating. In case there are other internal attendees in the event the user will be removed from the event. If the removed user was the organizer the context administrator will become the new organizer of the event.

In case the whole context is downgraded the same logic is used, efficiently removing all context related calendar data.


# Delete a context
Deleting a context will permanently remove all calendar data assignable to that context. No traces will be left.


# Delete a user
Deleting a user will permanently remove all calendar data assignable to that user. Likewise downgrading a single user, the deleted user will be removed as attendee from all event the user participates in and replaced by the context administrator in events the user organized. Event where the user was the last internal attendee will be removed too. 

In addition, the deleted user is replaced by the context administrator in all events left the user created, modified the last or is the assigned calendar user.



# References / further reading
1. [DeleteInvisible][1]



[//]: # (Reference links - won't be shown in converted file)
[//]: # (Explicit references)
[1]: {{ site.baseurl }}/middleware/components/commandlinetools/deleteinvisible.html

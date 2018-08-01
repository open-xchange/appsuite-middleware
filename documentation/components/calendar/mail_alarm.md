---
title: Mail alarm handling
---

This article will describe how eMail alarms are handled by the middleware. 

# Overview

In addition to display and audio alarms it is now possible to create eMail alarms. 
Unlike other alarm types the eMail alarm is usually not announced via the /chronos/alarm?action=pending action. 
Instead a (single) mail is send in due time via the configured no reply address. The following sections describe the mechanism behind it and how it can be configured.

# Mechanism

The mechanism to properly send a single mail in due time consists of two parts which cooperate closely. 

## Worker thread

The first part is a worker thread who runs in a periodic interval and checks if there are any due alarm trigger to process.
The worker then schedules a delivery task for each due trigger. When the trigger time has come the delivery task checks again if he should send the mail, deletes the trigger and then sends the mail.
This worker thread only runs once in a cluster. For this purpose it uses a ClusterTimerService and ideally, this service uses Hazelcast.

All synchronization is achieved by using the database. Essential is the new column processed in the calendar_alarm_trigger table which contains the utc timestamp of the trigger which scheduled the delivery task for the trigger. Those triggers are not touched unless the trigger is overdue. This means that if one node crashes after a delivery task is scheduled and before the mail is send it can be picked up again by another node after a defined time (e.g. 5 minutes after the trigger time).

## CalendarHandler

The worker thread alone would be sufficient in a static univers but reality is a different matter. Events are created, changed and deleted and this inevitably leads to changes to the alarms and their triggers. The second part of the mechanism - a calendar handler - is therefore responsible to handle those dynamic events. 
For every change to an event it checks if the trigger has been changed as well. If this is the case it schedules new delivery tasks and/or cancels already scheduled tasks.

### Example 1

It is 10:13 a.m. and an event with an alarm at the start time of the event is moved from tommorow 10:30 a.m. to today 10:30 a.m. 
Normally this trigger would be picked up in time by the worker thread which runs e.g. every hour. 
In this scenario the worker thread would probably too late to pick the trigger up. Therefore the calendar handler indentifies this situation and schedules a delivery task for 10:30 a.m.

### Example 2

It is 10:13 a.m. and an event with an alarm at the start time of the event is moved from today 10:30 a.m. to tomorrow 10:30 a.m. 
An delivery task is therefore already scheduled for this alarm by the worker thread. The calendar handler indentifies this situation and cancels the delivery task.
The worker thread will then pick up the trigger tommorow before the alarm is due.

There are many more situation in which the handler needs to make adjustments (e.g. changes to a series pattern, changes to alarms itself, etc.).

# Configuration

The worker thread works right out of the box, but it can be configured to ones needs and the environment situation. This chapter provies an overview of those config parameters.

First of all the worker can be completely disabled:

    com.openexchange.calendar.alarm.mail.backgroundWorker.enabled=false

One can configure the interval in minutes. E.g. to run the worker only once a hour:

    com.openexchange.calendar.alarm.mail.backgroundWorker.period=60

It is also possible to configure an inital delay in minutes:

    com.openexchange.calendar.alarm.mail.backgroundWorker.initialDelay=30

The lookAhead value defines the time in minutes the worker looks into the future. It must be at least as big as the period value.
It is advisable a buffer time to this value. E.g. 5 minutes.

    com.openexchange.calendar.alarm.mail.backgroundWorker.lookAhead=65

In case a node crashes or a delivery worker runs into an unexpected error the trigger is picked up after a wait time by the next run of the worker thread.
This wait time in minutes can be configured. E.g. to wait 10 minutes before picking up the trigger again:
  
    com.openexchange.calendar.alarm.mail.backgroundWorker.overdueWaitTime=10

In a perfect world a send mail would be received immediately after it was send, but in a lot of environments this is usually not the case.
Typically it takes some seconds until the mail is send out. In case of a mail reminder this time shift doesn't feels right. 
To circumvent this situation the worker thread can be configured to send the mails earlier. It's advisable to configure this value to the average delay of the mail infrastructure.
E.g. to send the mail 10 seconds before the trigger time:

    com.openexchange.calendar.alarm.mail.backgroundWorker.time.shift=10


 
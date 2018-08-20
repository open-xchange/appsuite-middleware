---
title: Mail alarm handling
---

This article will describe how e-mail alarms are handled by the middleware. 

# Overview

In addition to display and audio alarms, it also possible to create e-mail alarms. Unlike the other alarm types, the e-mail is usually not announced to the web client via the /chronos/alarm?action=pending action. Instead, a (single) e-mail is sent when the alarm's trigger is due via the configured *no-reply* transport account. The following sections describe the mechanisms behind it and how it can be configured.

# Mechanism

The mechanism to properly send a single mail in due time consists of two parts which cooperate closely. 

## Worker thread

The first part is a worker thread that runs in a periodic interval and checks if there are any due alarm triggers to process. The worker then schedules a delivery task for each due trigger. When the trigger time has come, the delivery task checks again if he should send the mail, deletes the trigger and then sends the mail.
This worker thread only runs once in a cluster. For this purpose, it uses the ClusterTimerService which utilizes Hazelcast to synchronize the executions within the cluster.

All synchronization is achieved using the database. Essential is the new column ``processed`` in the calendar_alarm_trigger table which contains the UTC timestamp of the trigger which scheduled the delivery task for this trigger. Those triggers are not touched unless the trigger is overdue. This means that if one node crashes after a delivery task is scheduled and before the mail is sent, it can be picked up again by another node after a defined time (e.g. 5 minutes after the trigger time).

## Calendar Handler

The worker thread alone would be sufficient in a static environment. However, when events are created, changed or deleted, this inevitably leads to changes to the associated alarms and their triggers. The second part of the mechanism - a calendar handler - is therefore responsible to handle those dynamic events. 

For every change to an event, it checks if the trigger has been changed as well. If this is the case, it schedules new delivery tasks and/or cancels already scheduled tasks.

### Example 1

It is 10:13 a.m. and an event with an alarm at the start time of the event is moved from tomorrow 10:30 a.m. to today 10:30 a.m.
 
Normally this trigger would be picked up in time by the worker thread which runs e.g. every hour. In this scenario, the worker thread would probably be too late to pick the trigger up and adjust it. Therefore, the calendar handler identifies this situation and schedules a delivery task for 10:30 a.m.

### Example 2

It is 10:13 a.m. and an event with an alarm at the start time of the event is moved from today 10:30 a.m. to tomorrow 10:30 a.m. 

An delivery task is therefore already scheduled for this alarm by the worker thread. The calendar handler identifies this situation and cancels the delivery task. The worker thread will then pick up the trigger tomorrow before the alarm is due.

There are many more situation in which the handler needs to make adjustments (e.g. changes to a series pattern, changes to alarms itself, etc.).

# Configuration

The worker thread works right out of the box, but it can be configured to ones needs and the environment situation. This chapter provides an overview of those config parameters.

First of all the worker can be completely disabled:

    com.openexchange.calendar.alarm.mail.backgroundWorker.enabled=false

One can configure the interval in minutes. E.g. to run the worker only once a hour:

    com.openexchange.calendar.alarm.mail.backgroundWorker.period=60

It is also possible to configure an initial delay in minutes, after the node was started:

    com.openexchange.calendar.alarm.mail.backgroundWorker.initialDelay=30

The lookAhead value defines the time in minutes the worker looks into the future. It must be at least as big as the period value. It is advisable to add a small buffer to this value, e.g. period + 5 minutes:

    com.openexchange.calendar.alarm.mail.backgroundWorker.lookAhead=65

In case a node crashes or a delivery worker runs into an unexpected error, the trigger is picked up after a wait time by the next run of the worker thread. This wait time in minutes can be configured. E.g. to wait 10 minutes before picking up the trigger again:
  
    com.openexchange.calendar.alarm.mail.backgroundWorker.overdueWaitTime=10

In a perfect world an e-mail would be received immediately after it was sent, but in a lot of environments this is usually not the case. Typically it takes some time until the mail is transported. In case of an e-mail reminder, this time shift doesn't feels right. To circumvent this situation, the worker thread can be configured to send the e-mails earlier. It's advisable to configure this value to the average delay of the mail infrastructure. E.g., to send the mail 230 milliseconds before the trigger time:

    com.openexchange.calendar.alarm.mail.backgroundWorker.time.shift=230

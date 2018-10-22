---
title: Message alarm handling
---

This article will describe how message alarms like e-mail alarms are handled by the middleware. 

# Overview

In addition to display and audio alarms, it also possible to create message alarms, currently available are e-mail and sms. Unlike the other alarm types, message alarms are usually not announced to the web client via the /chronos/alarm?action=pending action. Instead, a (single) message is sent when the alarm's trigger is due. For e-mail this is done via the configured *no-reply* transport account and for sms a SMSServiceSPI service is used to send the message. The following sections describe the mechanisms behind it and how it can be configured.

# Mechanism

The mechanism to properly send a single message in due time consists of two parts which cooperate closely. 

## Worker thread

The first part is a worker thread that runs in a periodic interval and checks if there are any due alarm triggers to process. The worker then schedules a delivery task for each due trigger. When the trigger time has come, the delivery task checks again if he should send the message, deletes the trigger and then sends the message. This worker thread only runs once in a cluster. For this purpose, it uses the ClusterTimerService which utilizes Hazelcast to synchronize the executions within the cluster.

All synchronization is achieved using the database. Essential is the new column ``processed`` in the calendar_alarm_trigger table which contains the UTC timestamp of the trigger which scheduled the delivery task for this trigger. Those triggers are not touched unless the trigger is overdue. This means that if one node crashes after a delivery task is scheduled and before the message is sent, it can be picked up again by another node after a defined time (e.g. 5 minutes after the trigger time).

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

    com.openexchange.calendar.alarm.message.backgroundWorker.enabled=false

It is also possible to enable or disable single message types. E.g. for sms:

    com.openexchange.calendar.alarm.sms.enabled = false

One can configure the interval in minutes. E.g. to run the worker only once a hour:

    com.openexchange.calendar.alarm.message.backgroundWorker.period=60

It is also possible to configure an initial delay in minutes, after the node was started:

    com.openexchange.calendar.alarm.message.backgroundWorker.initialDelay=30

The lookAhead value defines the time in minutes the worker looks into the future. It must be at least as big as the period value. It is advisable to add a small buffer to this value, e.g. period + 5 minutes:

    com.openexchange.calendar.alarm.message.backgroundWorker.lookAhead=65

In case a node crashes or a delivery worker runs into an unexpected error, the trigger is picked up after a wait time by the next run of the worker thread. This wait time in minutes can be configured. E.g. to wait 10 minutes before picking up the trigger again:
  
    com.openexchange.calendar.alarm.message.backgroundWorker.overdueWaitTime=10

In a perfect world a message would be received immediately after it was sent, but in a lot of environments this is usually not the case. Typically it takes some time until the message is transported. In case of an message reminder, this time shift doesn't feels right. To circumvent this situation, the worker thread can be configured to send the message earlier. It's advisable to configure this value to the average delay of the infrastructure. E.g., to send the mail 230 milliseconds before the trigger time:

    com.openexchange.calendar.alarm.mail.time.shift=230

The same can be configured independently for the sms alarm:

    com.openexchange.calendar.alarm.sms.time.shift=230


# Ratelimiting

Since a single user is potentially able to create a hugh amount of alarm at the same time and therefore could cause a lot of messages to be send in a short timeframe it is possible to limit the amount of send messages via a ratelimit. To enable the ratelimit you only need to configure the amount and the timeframe to use. E.g., to only allow 50 mails in one hour:

```
com.openexchange.calendar.alarm.mail.limit.amount = 50
com.openexchange.calendar.alarm.mail.limit.timeframe = 60000
```

You can also disable the ratelimit by setting the amount value to a negative value:

    com.openexchange.calendar.alarm.mail.limit.amount = -1

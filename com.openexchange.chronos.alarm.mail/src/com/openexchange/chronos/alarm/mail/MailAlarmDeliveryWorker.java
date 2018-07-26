/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.alarm.mail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AdministrativeAlarmTriggerStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Updater;
import com.openexchange.java.util.Pair;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * The {@link MailAlarmDeliveryWorker} checks if there are any mail alarm triggers which needed to be executed within the next timeframe ({@link #lookAhead}).
 * It then marks those triggers as processed and schedules a {@link SingleMailDeliveryTask} at the appropriate time (shifted forward by the {@link #mailShift} value)
 * for each of them.
 *
 * It also picks up old triggers, which are already marked as processed by other threats, if they are overdue ({@link #overdueWaitTime}).
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailAlarmDeliveryWorker implements Runnable {

    protected static final Logger LOG = LoggerFactory.getLogger(MailAlarmDeliveryWorker.class);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private final AdministrativeAlarmTriggerStorage storage;
    private final DatabaseService dbservice;
    private final int timeUnit;
    private final ContextService ctxService;
    private final TimerService timerService;

    private final Map<Key, ScheduledTimerTask> scheduledTasks = new ConcurrentHashMap<>();
    private final CalendarStorageFactory factory;
    private final CalendarUtilities calUtil;
    private long lastCheck = 0;

    private final MailAlarmNotificationService mailService;
    private final int mailShift;
    private final int lookAhead;
    private final int overdueWaitTime;

    /**
     *
     * Initializes a new {@link MailAlarmDeliveryWorker}.
     *
     * @param storage An {@link AdministrativeAlarmTriggerStorage}
     * @param factory A {@link CalendarStorageFactory} which provides the storage.
     * @param dbservice A {@link DatabaseService} which provides the db connections.
     * @param ctxService A {@link ContextService} to load the Context.
     * @param calUtil A {@link CalendarUtilities} to perform addition
     * @param timerService A {@link TimerService} to provide {@link EntityResolver} for each context
     * @param mailService The {@link MailAlarmNotificationService} to send the mails with
     * @param lookAhead The time value the worker is looking ahead. Depends on timeUnit.
     * @param timeUnit Specifies the time unit of the lookAhead parameter as a {@link Calendar} field. E.g. {@link Calendar#MINUTE}.
     * @param mailShift The time in milliseconds the mail send is shifter forward.
     * @param overdueWaitTime The time in minutes to wait until an old trigger is picked up.
     * @throws OXException If not administrative storage could be created.
     */
    public MailAlarmDeliveryWorker( AdministrativeAlarmTriggerStorage storage,
                                    CalendarStorageFactory factory,
                                    DatabaseService dbservice,
                                    ContextService ctxService,
                                    CalendarUtilities calUtil,
                                    TimerService timerService,
                                    MailAlarmNotificationService mailService,
                                    int lookAhead,
                                    int timeUnit,
                                    int mailShift,
                                    int overdueWaitTime) throws OXException {
        this.storage = storage;
        this.dbservice = dbservice;
        this.timeUnit = timeUnit;
        this.ctxService = ctxService;
        this.timerService = timerService;
        this.factory = factory;
        this.calUtil = calUtil;
        this.mailService = mailService;
        this.lookAhead = lookAhead;
        this.mailShift = mailShift;
        this.overdueWaitTime = overdueWaitTime;
    }

    @Override
    public void run() {
        LOG.info("Started mail alarm delivery worker...");
        Calendar until = Calendar.getInstance(UTC);
        until.add(timeUnit, lookAhead);
        boolean succesfull = false;
        try {
            List<Integer> ctxIds = ctxService.getDistinctContextsPerSchema();
            Calendar currentUTCTime = Calendar.getInstance(UTC);
            lastCheck = currentUTCTime.getTimeInMillis();
            for (Integer ctxId : ctxIds) {
                // Test if schema is ready
                if (!Updater.getInstance().getStatus(ctxId).isExecutedSuccessfully(MailAlarmDeliveryWorkerUpdateTask.class.getName())) {
                    continue;
                }

                Connection con = dbservice.getForUpdateTask(ctxId);
                boolean readOnly = true;
                try {
                    con.setAutoCommit(false);
                    Calendar overdueTime = Calendar.getInstance(UTC);
                    overdueTime.add(Calendar.MINUTE, -Math.abs(overdueWaitTime));
                    Map<Pair<Integer, Integer>, AlarmTrigger> lockedTriggers = storage.getAndLockTriggers(con, until.getTime(), overdueTime.getTime());
                    if (lockedTriggers.isEmpty()) {
                        continue;
                    }

                    storage.setProcessingStatus(con, lockedTriggers, currentUTCTime.getTimeInMillis());
                    readOnly = false;
                    for (Entry<Pair<Integer, Integer>, AlarmTrigger> entry: lockedTriggers.entrySet()) {
                        int cid = entry.getKey().getFirst();
                        int account = entry.getKey().getSecond();
                        AlarmTrigger trigger = entry.getValue();

                        CalendarStorage ctxStorage = factory.create(ctxService.getContext(cid), account, optEntityResolver(cid), new SimpleDBProvider(con, con), DBTransactionPolicy.NO_TRANSACTIONS);
                        Alarm alarm = ctxStorage.getAlarmStorage().loadAlarm(trigger.getAlarm());
                        Calendar calTriggerTime = Calendar.getInstance(UTC);
                        calTriggerTime.setTimeInMillis(entry.getValue().getTime());

                        Calendar now = Calendar.getInstance(UTC);

                        long delay = (calTriggerTime.getTimeInMillis() - now.getTimeInMillis()) - mailShift;
                        if (delay < 0) {
                            delay = 0;
                        }

                        SingleMailDeliveryTask task = createTask(cid, account, alarm, trigger, currentUTCTime.getTimeInMillis());
                        ScheduledTimerTask timer = timerService.schedule(task, delay, TimeUnit.MILLISECONDS);
                        Key key = key(cid, account, trigger.getEventId(), alarm.getId());
                        scheduledTasks.put(key, timer);
                        LOG.trace("Created a new mail alarm task for {}", key);
                    }
                    con.commit();
                    succesfull = true;
                } catch (SQLException e) {
                    // ignore retry next time
                    LOG.error(e.getMessage(), e);
                } finally {
                    if (con != null) {
                        if (succesfull == false) {
                            try {
                                con.rollback();
                            } catch (SQLException e) {
                                // ignore
                            }
                        }

                        try {
                            con.setAutoCommit(true);
                        } catch (SQLException e1) {
                            // ignore
                        }
                    }
                    if (readOnly) {
                        dbservice.backForUpdateTaskAfterReading(ctxId, con);
                    } else {
                        dbservice.backForUpdateTask(ctxId, con);
                    }
                }
            }
        } catch (OXException e) {
            // Nothing that can be done here. Just retry it with the next run
            LOG.error(e.getMessage(), e);
        }
        LOG.info("Mail Alarm delivery worker run finished!");
    }

    /**
     * Creates a new {@link SingleMailDeliveryTask}
     *
     * @param cid The context id
     * @param account The account id
     * @param alarm The {@link Alarm}
     * @param trigger The {@link AlarmTrigger}
     * @param processed The processed value
     * @return The task
     * @throws OXException If the context couldn't be loaded
     */
    private SingleMailDeliveryTask createTask(int cid, int account, Alarm alarm, AlarmTrigger trigger, long processed) throws OXException {
        return new SingleMailDeliveryTask(dbservice, storage, mailService, factory, calUtil, ctxService.getContext(cid), account, alarm, trigger, processed, this);
    }

    /**
     * Creates a {@link Key}
     *
     * @param cid The context id
     * @param account The account id
     * @param eventId The event id
     * @param alarm The alarm id
     * @return the {@link Key}
     */
    Key key(int cid, int account, String eventId, int alarm) {
        return new Key(cid, account, eventId, alarm);
    }

    /**
     * Cancels all tasks for the given event id
     *
     * @param eventID The event id to cancel tasks for. E.g. because the event is deleted.
     */
    public void cancelAll(int cid, int accountId, String eventId) {
        Iterator<Entry<Key, ScheduledTimerTask>> iterator = scheduledTasks.entrySet().iterator();
        for (Entry<Key, ScheduledTimerTask> entry : scheduledTasks.entrySet()) {
            Key key = entry.getKey();
            if (key.getCid() == cid && key.getAccount() == accountId && key.getEventId().equals(eventId)) {
                LOG.trace("Canceled mail alarm task for {}", key);
                entry.getValue().cancel();
                iterator.remove();
            }
        }
    }

    /**
     * Checks if the given events contain alarm trigger which must be triggered before the next run of the {@link MailAlarmDeliveryWorker}
     *
     * @param events A list of updated and newly created events
     * @param cid The context id
     * @param account The account id
     */
    public void checkEvents(List<Event> events, int cid, int account) {
        Connection con;
        try {
            con = dbservice.getWritable(cid);
            boolean readonly = true;
            boolean successfull = false;
            try {
                con.setAutoCommit(false);
                readonly = checkEvents(con, events, cid, account);
                con.commit();
                successfull = true;
            } catch (SQLException e) {
                LOG.error("Error while scheduling mail alarm task: {}", e.getMessage(), e);
            } finally {
                if (con != null) {
                    if (!successfull) {
                        try {
                            con.rollback();
                        } catch (SQLException e) {
                            // ignore
                        }
                    }
                    try {
                        con.setAutoCommit(true);
                    } catch (SQLException e) {
                        // ignore
                    }
                    if (readonly) {
                        dbservice.backWritableAfterReading(cid, con);
                    } else {
                        dbservice.backWritable(cid, con);
                    }
                }
            }
        } catch (OXException e) {
            LOG.error("Error while trying to handle event: {}", e.getMessage(), e);
            // Can be ignored. Triggers are picked up with the next run of the MailAlarmDeliveryWorker
        }
    }

    boolean checkEvents(Connection con, List<Event> events, int cid, int account) throws OXException {
        boolean result = true;
        Calendar cal = Calendar.getInstance(UTC);
        if (lastCheck != 0) {
            cal.setTimeInMillis(lastCheck);
        }
        cal.add(timeUnit, lookAhead);
        for (Event event : events) {
            Map<Pair<Integer, Integer>, AlarmTrigger> triggerMap = storage.getAndLockMailAlarmTriggers(con, cid, account, event.getId());
            // Schedule a task for all triggers before the next usual interval
            for (Entry<Pair<Integer, Integer>, AlarmTrigger> entry : triggerMap.entrySet()) {
                Key key = key(cid, account, event.getId(), entry.getValue().getAlarm());
                if (entry.getValue().getTime() > cal.getTimeInMillis()) {
                    cancelTask(key);
                    continue;
                }
                CalendarStorage storage = factory.create(ctxService.getContext(cid), account, optEntityResolver(cid), new SimpleDBProvider(con, con), DBTransactionPolicy.NO_TRANSACTIONS);
                Alarm alarm = storage.getAlarmStorage().loadAlarm(entry.getValue().getAlarm());
                scheduleTask(con, key, alarm, entry.getValue());
                result = false;
            }
        }
        return result;
    }

    /**
     * Schedules a {@link SingleMailDeliveryTask} for the given {@link AlarmTriggerWrapper}
     *
     * @param con The connection to use
     * @param key The {@link Key} to the {@link SingleMailDeliveryTask}
     * @param alarm The {@link Alarm} of the {@link AlarmTriggerWrapper}
     * @param trigger The {@link AlarmTriggerWrapper}
     */
    private void scheduleTask(Connection con, Key key, Alarm alarm, AlarmTrigger trigger) {
        cancelTask(key);
        boolean processingSet = false;
        try {
            Calendar calTriggerTime = Calendar.getInstance(UTC);
            calTriggerTime.setTimeInMillis(trigger.getTime());
            Calendar now = Calendar.getInstance(UTC);

            storage.setProcessingStatus(con, Collections.singletonMap(new Pair<>(key.getCid(), key.getAccount()), trigger), now.getTimeInMillis());
            processingSet = true;

            long delay = calTriggerTime.getTimeInMillis() - now.getTimeInMillis();
            if (delay < 0) {
                delay = 0;
            }

            LOG.trace("Created new task for {}", key);
            SingleMailDeliveryTask task = createTask(key.getCid(), key.getAccount(), alarm, trigger, now.getTimeInMillis());
            ScheduledTimerTask timer = timerService.schedule(task, delay, TimeUnit.MILLISECONDS);
            scheduledTasks.put(key, timer);
        } catch (OXException e) {
            if (processingSet) {
                try {
                    // If the error is thrown after the processed value is successfully set then set it back to 0 so the next task can pick it up
                    storage.setProcessingStatus(con, Collections.singletonMap(new Pair<>(key.getCid(), key.getAccount()), trigger), 0l);
                } catch (OXException e1) {
                    // Can be ignored. The trigger is picked up once the trigger time is overdue.
                }
            }
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Cancels the task specified by the key if one exists
     *
     * @param key The key
     */
    private void cancelTask(Key key) {
        ScheduledTimerTask scheduledTimerTask = scheduledTasks.get(key);
        if (scheduledTimerTask != null) {
            LOG.trace("Canceled mail alarm task for {}", key);
            scheduledTimerTask.cancel();
            scheduledTasks.remove(key);
        }
    }

    /**
     * Cancels all running thread and tries to reset their processed values
     */
    public void cancel() {
        for (Entry<Key, ScheduledTimerTask> entry : scheduledTasks.entrySet()) {
            Key key = entry.getKey();
            Connection con = null;
            try {
                con = dbservice.getWritable(key.getCid());

                if (storage != null && con != null) {
                    try {
                        AlarmTrigger trigger = new AlarmTrigger();
                        trigger.setAlarm(key.getId());
                        storage.setProcessingStatus(con, Collections.singletonMap(new Pair<>(key.getCid(), key.getAccount()), trigger), 0l);
                        LOG.trace("Properly resettet the processed status of the alarm trigger for {}", key);
                    } catch (OXException e) {
                        // ignore
                    }
                }
            } catch (OXException e1) {
                // ignore
            }
            entry.getValue().cancel();
            LOG.trace("Canceled mail alarm delivery task for {}", key);
        }
        scheduledTasks.clear();
    }

    /**
     * Optionally gets an entity resolver for the supplied context.
     *
     * @param contextId The identifier of the context to get the entity resolver for
     * @return The entity resolver, or <code>null</code> if not available
     */
    private EntityResolver optEntityResolver(int contextId) {
        try {
            return calUtil.getEntityResolver(contextId);
        } catch (OXException e) {
            LOG.trace("Error getting entity resolver for context: {}", Integer.valueOf(contextId), e);
        }
        return null;
    }

    /**
     * Removes the {@link SingleMailDeliveryTask} defined by the given key from the local map.
     *
     * @param key The key to remove
     */
    public void remove(Key key) {
        if(key != null) {
            scheduledTasks.remove(key);
        }
    }

    /**
     * {@link Key} is a identifying key for a {@link SingleMailDeliveryTask}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.1
     */
    private class Key {

        private final int cid, account, id;
        private final String eventId;

        /**
         * Initializes a new {@link MailAlarmDeliveryWorker.key}.
         */
        public Key(int cid, int account, String eventId, int id) {
            this.cid = cid;
            this.account = account;
            this.id = id;
            this.eventId = eventId;
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + cid;
            hash = hash * 31 + account;
            hash = hash * 31 + eventId.hashCode();
            hash = hash * 31 + id;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                return obj.hashCode() == this.hashCode();
            }
            return false;
        }

        /**
         * Gets the eventId
         *
         * @return The eventId
         */
        public String getEventId() {
            return eventId;
        }

        /**
         * Gets the cid
         *
         * @return The cid
         */
        public int getCid() {
            return cid;
        }

        /**
         * Gets the account
         *
         * @return The account
         */
        public int getAccount() {
            return account;
        }

        @Override
        public String toString() {
            return "Key [cid=" + cid + "|account=" + account + "|eventId=" + eventId + "|alarmId=" + id + "]";
        }

        /**
         * Gets the id
         *
         * @return The id
         */
        public int getId() {
            return id;
        }
    }

    /**
     *
     * {@link SingleMailDeliveryTask} executes the mail delivery for a calendar mail alarm.
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.1
     */
    private class SingleMailDeliveryTask implements Runnable {

        Context ctx;
        private final Alarm alarm;
        private final CalendarStorageFactory factory;
        private final AlarmTrigger trigger;
        private final CalendarUtilities calUtil;
        private final long processed;
        private final int account;
        private final MailAlarmDeliveryWorker callback;
        private final DatabaseService dbservice;
        private final AdministrativeAlarmTriggerStorage storage;
        private final MailAlarmNotificationService mailService;


        /**
         * Initializes a new {@link SingleMailDeliveryTask}.
         *
         * @param factory The {@link CalendarStorageFactory}
         * @param calUtil The {@link CalendarUtilities}
         * @param ctx The {@link Context}
         * @param account The account id
         * @param alarm The {@link Alarm}
         * @param trigger The {@link AlarmTrigger}
         * @param processed The processed value
         */
        public SingleMailDeliveryTask(DatabaseService dbservice, AdministrativeAlarmTriggerStorage storage, MailAlarmNotificationService mailService, CalendarStorageFactory factory, CalendarUtilities calUtil, Context ctx, int account, Alarm alarm, AlarmTrigger trigger, long processed, MailAlarmDeliveryWorker callback) {
            this.ctx = ctx;
            this.alarm = alarm;
            this.factory = factory;
            this.trigger = trigger;
            this.calUtil = calUtil;
            this.processed = processed;
            this.account = account;
            this.callback = callback;
            this.dbservice = dbservice;
            this.storage = storage;
            this.mailService = mailService;
        }

        @Override
        public void run() {
            Connection writeCon = null;
            boolean isReadOnly = true;
            boolean processFinished = false;
            try {
                writeCon = dbservice.getWritable(ctx);
                writeCon.setAutoCommit(false);
                // do the delivery and update the db entries (e.g. like setting the acknowledged field)
                Event event = prepareEvent(writeCon);
                writeCon.commit();
                processFinished = true;
                if(event != null) {
                    isReadOnly = false;
                    sendMail(event);
                }
                // If the triggers has been updated (deleted + inserted) check if a trigger needs to be scheduled.
                if(event != null) {
                    checkEvent(writeCon, event);
                }
            } catch (OXException | SQLException e) {
                try {
                    if (writeCon != null) {
                        // rollback the last transaction
                        writeCon.rollback();
                        // if the error occurred during the process retry the hole operation once
                        if(processFinished == false) {
                            try {
                                writeCon.setAutoCommit(false);
                                // do the delivery and update the db entries (e.g. like setting the acknowledged field)
                                Event event = prepareEvent(writeCon);
                                writeCon.commit();
                                if(event != null) {
                                    isReadOnly = false;
                                    sendMail(event);
                                }
                                processFinished = true;
                                // If the triggers has been updated (deleted + inserted) check if a trigger needs to be scheduled.
                                if(event != null) {
                                    checkEvent(writeCon, event);
                                }
                            } catch (SQLException | OXException e1) {
                                // Nothing that can be done. Do a rollback and reset the processed value if necessary
                                writeCon.rollback();
                                if (processFinished == false) {
                                    try {
                                        storage.setProcessingStatus(writeCon, Collections.singletonMap(new Pair<>(ctx.getContextId(), account), trigger), 0l);
                                        isReadOnly = false;
                                    } catch (OXException e2) {
                                        // ignore
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException sql) {
                    // ignore
                }
            } finally {
                callback.remove(key(ctx.getContextId(), account, trigger.getEventId(), alarm.getId()));
                if (writeCon != null) {
                    try {
                        writeCon.setAutoCommit(true);
                    } catch (SQLException e) {
                        // ignore
                    }
                }
                if (isReadOnly) {
                    dbservice.backWritableAfterReading(ctx, writeCon);
                } else {
                    dbservice.backWritable(ctx, writeCon);
                }
            }
        }

        /**
         * Checks if the delivery is still necessary and prepares the event for it:
         *  - Acknowledges the alarm
         *  - Deletes and reinserts the alarm triggers
         *  - updates the event timestamp
         *
         * @param writeCon A writable connection
         * @return The prepared Event or null
         * @throws OXException
         */
        private Event prepareEvent(Connection writeCon) throws OXException {
            SimpleDBProvider provider = new SimpleDBProvider(writeCon, writeCon);
            CalendarStorage storage = factory.create(ctx, account, optEntityResolver(ctx.getContextId()), provider, DBTransactionPolicy.NO_TRANSACTIONS);
            AlarmTrigger loadedTrigger = storage.getAlarmTriggerStorage().loadTrigger(trigger.getAlarm());
            if (loadedTrigger == null || loadedTrigger.getProcessed() != processed) {
                // Abort since the triggers is either gone or picked up by another node (e.g. because of an update)
                LOG.trace("Skipped mail alarm task for {}. Its trigger is not up to date!", key(ctx.getContextId(), account, trigger.getEventId(), alarm.getId()));
                return null;
            }
            Event event = storage.getEventStorage().loadEvent(trigger.getEventId(), null);
            storage.getUtilities().loadAdditionalEventData(trigger.getUserId(), event, null);
            List<Alarm> alarms = event.getAlarms();
            for (Alarm tmpAlarm : alarms) {
                if (tmpAlarm.getId() == alarm.getId()) {
                    tmpAlarm.setAcknowledged(new Date());
                    break;
                }
            }
            storage.getAlarmStorage().updateAlarms(event, trigger.getUserId(), alarms);
            Map<Integer, List<Alarm>> loadAlarms = storage.getAlarmStorage().loadAlarms(event);
            storage.getAlarmTriggerStorage().deleteTriggers(event.getId());
            storage.getAlarmTriggerStorage().insertTriggers(event, loadAlarms);
            touch(storage.getEventStorage(), event.getId());
            return event;
        }

        /**
         * Delivers the mail
         *
         * @param event The event of the alarm
         */
        private void sendMail(Event event) {
            Key key = key(ctx.getContextId(), account, event.getId(), alarm.getId());
            try {
                mailService.send(event, ctx.getContextId(), trigger.getUserId());
                LOG.trace("Mail successfully send for {}", key);
            } catch (OXException e) {
                LOG.warn("Unable to send mail for calendar alarm ({}): {}", key, e.getMessage(), e);
            }
        }

        /**
         * <i>Touches</i> an event in the storage by setting it's timestamp property to the current time
         *
         * @param storage The {@link EventStorage}
         * @param id The identifier of the event to <i>touch</i>
         */
        protected void touch(EventStorage storage, String id) throws OXException {
            Event eventUpdate = new Event();
            eventUpdate.setId(id);
            eventUpdate.setTimestamp(System.currentTimeMillis());
            storage.updateEvent(eventUpdate);
        }

        private void checkEvent(Connection writeCon, Event event) throws SQLException, OXException {
            checkEvents(writeCon, Collections.singletonList(event), ctx.getContextId(), account);
            writeCon.commit();
        }

        /**
         * Optionally gets an entity resolver for the supplied context.
         *
         * @param contextId The identifier of the context to get the entity resolver for
         * @return The entity resolver, or <code>null</code> if not available
         */
        private EntityResolver optEntityResolver(int contextId) {
            try {
                return calUtil.getEntityResolver(contextId);
            } catch (OXException e) {
                LOG.trace("Error getting entity resolver for context: {}", Integer.valueOf(contextId), e);
            }
            return null;
        }

    }

}

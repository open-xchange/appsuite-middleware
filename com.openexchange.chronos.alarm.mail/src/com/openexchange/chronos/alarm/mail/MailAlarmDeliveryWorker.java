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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
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
    private final ContextService ctxService;
    private final TimerService timerService;

    private final Map<Key, ScheduledTimerTask> scheduledTasks = new ConcurrentHashMap<>();
    private final CalendarStorageFactory factory;
    private final CalendarUtilities calUtil;
    private volatile long lastCheck = 0;

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
     * @param lookAhead The time value in minutes the worker is looking ahead.
     * @param mailShift The time in milliseconds the mail send is shifter forward.
     * @param overdueWaitTime The time in minutes to wait until an old trigger is picked up.
     * @throws OXException If not administrative storage could be created.
     */
    public MailAlarmDeliveryWorker(AdministrativeAlarmTriggerStorage storage, CalendarStorageFactory factory, DatabaseService dbservice, ContextService ctxService, CalendarUtilities calUtil, TimerService timerService, MailAlarmNotificationService mailService, int lookAhead, int mailShift, int overdueWaitTime) throws OXException {
        this.storage = storage;
        this.dbservice = dbservice;
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
        until.add(Calendar.MINUTE, lookAhead);
        try {
            List<Integer> ctxIds = ctxService.getDistinctContextsPerSchema();
            Calendar currentUTCTime = Calendar.getInstance(UTC);
            lastCheck = currentUTCTime.getTimeInMillis();
            for (Integer ctxId : ctxIds) {
                // Test if schema is ready
                UpdateStatus status = Updater.getInstance().getStatus(ctxId);
                if (!status.isExecutedSuccessfully(MailAlarmDeliveryWorkerUpdateTask.class.getName()) || status.backgroundUpdatesRunning() || status.blockingUpdatesRunning()) {
                    continue;
                }
                Connection readCon = null;
                Connection writeCon = null;
                boolean successful = false;
                boolean readOnly = true;
                try {

                    Calendar overdueTime = Calendar.getInstance(UTC);
                    overdueTime.add(Calendar.MINUTE, -Math.abs(overdueWaitTime));
                    readCon = dbservice.getReadOnly(ctxId);
                    Map<Pair<Integer, Integer>, List<AlarmTrigger>> lockedTriggers = storage.getAndLockTriggers(readCon, until.getTime(), overdueTime.getTime(), false);
                    if (lockedTriggers.isEmpty()) {
                        successful = true;
                        continue;
                    }

                    writeCon = dbservice.getForUpdateTask(ctxId);
                    writeCon.setAutoCommit(false);
                    lockedTriggers = storage.getAndLockTriggers(writeCon, until.getTime(), overdueTime.getTime(), true);
                    if (lockedTriggers.isEmpty()) {
                        successful = true;
                        continue;
                    }
                    readOnly = false;
                    storage.setProcessingStatus(writeCon, lockedTriggers, currentUTCTime.getTimeInMillis());
                    writeCon.commit();
                    spawnDeliveryTaskForTriggers(lockedTriggers, until, currentUTCTime);
                    successful = true;
                } catch (SQLException e) {
                    // ignore retry next time
                    LOG.error(e.getMessage(), e);
                } finally {
                    if (readCon != null) {
                        dbservice.backReadOnly(ctxId, readCon);
                    }
                    if (successful == false) {
                        Databases.rollback(writeCon);
                    }
                    Databases.autocommit(writeCon);
                    if (writeCon != null) {
                        if (readOnly) {
                            dbservice.backForUpdateTaskAfterReading(ctxId, writeCon);
                        } else {
                            dbservice.backForUpdateTask(ctxId, writeCon);
                        }
                    }
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (Exception e) {
            // Nothing that can be done here. Just retry it with the next run
            LOG.error(e.getMessage(), e);
        }
        LOG.info("Mail Alarm delivery worker run finished!");
    }

    private void spawnDeliveryTaskForTriggers(Map<Pair<Integer, Integer>, List<AlarmTrigger>> lockedTriggers, Calendar until, Calendar currentUTCTime) throws OXException {
        for (Entry<Pair<Integer, Integer>, List<AlarmTrigger>> entry : lockedTriggers.entrySet()) {
            int cid = entry.getKey().getFirst();
            int account = entry.getKey().getSecond();
            Connection readOnly = null;
            try {
                readOnly = dbservice.getReadOnly(cid);
                CalendarStorage ctxStorage = factory.create(ctxService.getContext(cid), account, optEntityResolver(cid), new SimpleDBProvider(readOnly, readOnly), DBTransactionPolicy.NO_TRANSACTIONS);
                for (AlarmTrigger trigger : entry.getValue()) {
                    try {
                        Alarm alarm = ctxStorage.getAlarmStorage().loadAlarm(trigger.getAlarm());
                        Calendar calTriggerTime = Calendar.getInstance(UTC);
                        calTriggerTime.setTimeInMillis(trigger.getTime());
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
                    } catch (UnsupportedOperationException e) {
                        LOG.error("Can't handle mail alarms as long as the legacy storage is used.");
                        continue;
                    }
                }
            } finally {
                if (readOnly != null) {
                    dbservice.backReadOnly(cid, readOnly);
                }
            }
        }
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
     * @param cid The context id
     * @param accountId The account id
     * @param eventId The event id to cancel tasks for. E.g. because the event is deleted.
     */
    public void cancelAll(int cid, int accountId, String eventId) {
        Iterator<Entry<Key, ScheduledTimerTask>> iterator = scheduledTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Key, ScheduledTimerTask> entry = iterator.next();
            Key key = entry.getKey();
            if (key.getCid() == cid && key.getAccount() == accountId && key.getEventId().equals(eventId)) {
                LOG.trace("Canceled mail alarm task for {}", key);
                entry.getValue().cancel();
                iterator.remove();
            }
        }
    }

    /**
     * Cancels all tasks for the given event ids
     *
     * @param cid The context id
     * @param accountId The account id
     * @param eventIds The event ids to cancel tasks for. E.g. because those events are deleted.
     */
    public void cancelAll(int cid, int accountId, Collection<String> eventIds) {
        for (String eventId : eventIds) {
            cancelAll(cid, accountId, eventId);
        }
    }

    /**
     * Checks if the given events contain alarm trigger which must be triggered before the next run of the {@link MailAlarmDeliveryWorker} and
     * schedules a task for each trigger.
     *
     * @param events A list of updated and newly created events
     * @param cid The context id
     * @param account The account id
     */
    public void checkAndScheduleTasksForEvents(List<Event> events, int cid, int account) {
        Connection readCon = null;
        Connection writeCon = null;
        try {
            readCon = dbservice.getReadOnly(cid);
            boolean successful = false;
            boolean readOnly = true;
            try {
                List<AlarmTrigger> triggers = checkEvents(readCon, events, cid, account, false);
                if (triggers.isEmpty() == false) {
                    // If there are due alarm triggers get a writable connection and lock those triggers
                    writeCon = dbservice.getWritable(cid);
                    writeCon.setAutoCommit(false);
                    triggers = checkEvents(writeCon, events, cid, account, true);
                    if (triggers.isEmpty() == false) {
                        readOnly = false;
                        CalendarStorage calStorage = factory.create(ctxService.getContext(cid), account, optEntityResolver(cid), new SimpleDBProvider(readCon, writeCon), DBTransactionPolicy.NO_TRANSACTIONS);
                        for (AlarmTrigger trigger : triggers) {
                            scheduleTaskForEvent(writeCon, calStorage, key(cid, account, trigger.getEventId(), trigger.getAlarm()), trigger);
                        }
                    }
                    writeCon.commit();
                    successful = true;
                }
            } catch (SQLException e) {
                LOG.error("Error while scheduling mail alarm task: {}", e.getMessage(), e);
            } finally {
                if (readCon != null) {
                    dbservice.backReadOnly(cid, readCon);
                }
                if (writeCon != null) {
                    if (successful == false) {
                        Databases.rollback(writeCon);
                    }
                    Databases.autocommit(writeCon);
                    if (readOnly) {
                        dbservice.backWritableAfterReading(cid, writeCon);
                    } else {
                        dbservice.backWritable(cid, writeCon);
                    }
                }
            }
        } catch (OXException e) {
            LOG.error("Error while trying to handle event: {}", e.getMessage(), e);
            // Can be ignored. Triggers are picked up with the next run of the MailAlarmDeliveryWorker
        }
    }

    /**
     * Checks the given events for mail alarms which need to be triggered soon
     *
     * @param con The connection to use
     * @param events The events to check
     * @param cid The id of the context the events belong to
     * @param account The id of the account the events belong to
     * @param isWriteCon The whether the given connection is a write connection or not
     * @return A list of AlarmTriggers which needs to be scheduled
     * @throws OXException
     */
    List<AlarmTrigger> checkEvents(Connection con, List<Event> events, int cid, int account, boolean isWriteCon) throws OXException {
        Calendar cal = Calendar.getInstance(UTC);
        if (lastCheck != 0) {
            cal.setTimeInMillis(lastCheck);
        }
        cal.add(Calendar.MINUTE, lookAhead);
        List<AlarmTrigger> result = null;
        for (Event event : events) {
            Map<Pair<Integer, Integer>, List<AlarmTrigger>> triggerMap = storage.getMailAlarmTriggers(con, cid, account, event.getId(), isWriteCon);
            // Schedule a task for all triggers before the next usual interval
            for (Entry<Pair<Integer, Integer>, List<AlarmTrigger>> entry : triggerMap.entrySet()) {
                for (AlarmTrigger trigger : entry.getValue()) {
                    Key key = key(cid, account, event.getId(), trigger.getAlarm());
                    if (trigger.getTime() > cal.getTimeInMillis()) {
                        cancelTask(key);
                        continue;
                    }
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(trigger);
                }
            }
        }
        return result == null ? Collections.emptyList() : result;
    }

    void scheduleTaskForEvent(Connection writeCon, CalendarStorage storage, Key key, AlarmTrigger trigger) throws OXException {
        try {
            Alarm alarm = storage.getAlarmStorage().loadAlarm(trigger.getAlarm());
            scheduleTask(writeCon, key, alarm, trigger);
        } catch (UnsupportedOperationException e) {
            LOG.error("Can't handle mail alarms as long as the legacy storage is used.");
        }
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

            storage.setProcessingStatus(con, Collections.singletonMap(new Pair<>(key.getCid(), key.getAccount()), Collections.singletonList(trigger)), now.getTimeInMillis());
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
                    storage.setProcessingStatus(con, Collections.singletonMap(new Pair<>(key.getCid(), key.getAccount()), Collections.singletonList(trigger)), 0l);
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
        ScheduledTimerTask scheduledTimerTask = scheduledTasks.remove(key);
        if (scheduledTimerTask != null) {
            LOG.trace("Canceled mail alarm task for {}", key);
            scheduledTimerTask.cancel();
        }
    }

    /**
     * Cancels all running thread and tries to reset their processed values
     */
    public void cancel() {
        Map<Integer, List<Entry<Key, ScheduledTimerTask>>> entries = cancelAllScheduledTasks();
        for (Entry<Integer, List<Entry<Key, ScheduledTimerTask>>> cidEntry : entries.entrySet()) {
            Connection con = null;
            try {
                Map<Pair<Integer, Integer>, List<AlarmTrigger>> triggers = new HashMap<>(cidEntry.getValue().size());
                for (Entry<Key, ScheduledTimerTask> entry : cidEntry.getValue()) {
                    Key key = entry.getKey();
                    AlarmTrigger trigger = new AlarmTrigger();
                    trigger.setAlarm(key.getId());
                    Pair<Integer, Integer> pair = new Pair<Integer, Integer>(key.getCid(), key.getAccount());
                    List<AlarmTrigger> list = triggers.get(pair);
                    if (list == null) {
                        list = new ArrayList<>();
                        triggers.put(pair, list);
                    }
                    list.add(trigger);
                    LOG.trace("Try to reset the processed status of the alarm trigger for {}", key);
                }
                con = dbservice.getWritable(cidEntry.getKey());
                if (storage != null && con != null) {
                    storage.setProcessingStatus(con, triggers, 0l);
                    LOG.trace("Successfully resetted the processed stati for context {}.", cidEntry.getKey());
                }
            } catch (OXException e1) {
                // ignore
            } finally {
                Databases.close(con);
            }
        }

        scheduledTasks.clear();
    }

    /**
     * Cancels all scheduled tasks and returns mapping of cids to those tasks
     *
     * @return The cid / List of entries mapping
     */
    private Map<Integer, List<Entry<Key, ScheduledTimerTask>>> cancelAllScheduledTasks() {
        Map<Integer, List<Entry<Key, ScheduledTimerTask>>> entries = new HashMap<>();
        for (Entry<Key, ScheduledTimerTask> entry : scheduledTasks.entrySet()) {
            Key key = entry.getKey();
            entry.getValue().cancel();
            List<Entry<Key, ScheduledTimerTask>> list = entries.get(key.getCid());
            if (list == null) {
                list = new ArrayList<>();
                entries.put(key.getCid(), list);
            }
            list.add(entry);
            LOG.trace("Canceled mail alarm delivery task for {}", key);
        }
        return entries;
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
        if (key != null) {
            scheduledTasks.remove(key);
        }
    }

}

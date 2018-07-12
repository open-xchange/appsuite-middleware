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
import com.openexchange.chronos.AlarmTriggerWrapper;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AdministrativeCalendarStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Updater;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MailAlarmDeliveryWorker}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailAlarmDeliveryWorker implements Runnable{

    protected static final Logger LOG = LoggerFactory.getLogger(MailAlarmDeliveryWorker.class);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    final AdministrativeCalendarStorage storage;
    final DatabaseService dbservice;
    private final int timeUnit;
    private final ContextService ctxService;
    private final TimerService timerService;

    Map<Key, ScheduledTimerTask> scheduledTasks = new ConcurrentHashMap<>();
    private final CalendarStorageFactory factory;
    private final CalendarUtilities calUtil;
    private long lastCheck=0;

    final MailAlarmNotificationService mailService;
    private final int mailShift;
    private final int lookAhead;

    /**
     *
     * Initializes a new {@link MailAlarmDeliveryWorker}.
     *
     * @param factory A {@link CalendarStorageFactory}
     * @param dbservice A {@link DatabaseService}
     * @param ctxService A {@link ContextService}
     * @param calUtil A {@link CalendarUtilities}
     * @param timerService A {@link TimerService}
     * @param mailService The {@link MailAlarmNotificationService} to send the mails with
     * @param period The time period this worker should look into the future
     * @param timeUnit Specifies the time unit of the period parameter as a {@link Calendar} field. E.g. {@link Calendar#MINUTE}.
     * @param mailShift The time in minutes the
     * @param lookAhead The time in minutes the worker is looking ahead
     * @throws OXException
     */
    public MailAlarmDeliveryWorker(CalendarStorageFactory factory, DatabaseService dbservice, ContextService ctxService, CalendarUtilities calUtil, TimerService timerService, MailAlarmNotificationService mailService,  int lookAhead, int timeUnit, int mailShift) throws OXException {
        this.storage = factory.createAdministrative();
        this.dbservice = dbservice;
        this.timeUnit = timeUnit;
        this.ctxService = ctxService;
        this.timerService = timerService;
        this.factory = factory;
        this.calUtil = calUtil;
        this.mailService = mailService;
        this.lookAhead = lookAhead;
        this.mailShift = mailShift;
    }

    @Override
    public void run() {
        Calendar until = Calendar.getInstance(UTC);
        until.add(timeUnit, lookAhead);
        boolean succesfull = false;
        try {
            List<Integer> ctxIds = ctxService.getDistinctContextsPerSchema();

            for (Integer ctxId : ctxIds) {
                // Test if schema is ready
                if(!Updater.getInstance().getStatus(ctxId).isExecutedSuccessfully(AddProcessedColumnUpdateTask.class.getName())) {
                    continue;
                }

                Connection con = dbservice.getForUpdateTask(ctxId);
                boolean readOnly = true;
                try {
                    con.setAutoCommit(false);
                    Calendar currentUTCTime = Calendar.getInstance(UTC);
                    List<AlarmTriggerWrapper> lockedTriggers = storage.getAlarmTriggerStorage().getAndLockTriggers(con, until.getTime());
                    lastCheck = currentUTCTime.getTimeInMillis();
                    if(lockedTriggers.isEmpty()) {
                        continue;
                    }

                    storage.getAlarmTriggerStorage().setProcessingStatus(con, lockedTriggers, currentUTCTime.getTimeInMillis());
                    readOnly = false;
                    for(AlarmTriggerWrapper trigger: lockedTriggers) {

                        Alarm alarm = storage.getAlarmStorage().getAlarm(con, trigger.getCtx(), trigger.getAccount(), trigger.getAlarmTrigger().getAlarm());
                        Calendar calTriggerTime = Calendar.getInstance(UTC);
                        calTriggerTime.setTimeInMillis(trigger.getAlarmTrigger().getTime());

                        Calendar now = Calendar.getInstance(UTC);

                        long delay = (calTriggerTime.getTimeInMillis() - now.getTimeInMillis()) - mailShift;
                        if(delay < 0) {
                            delay = 0;
                        }
                        LOG.info("Created new task for cid={}, account={}, alarm {}", trigger.getCtx(), trigger.getAccount(), alarm.getId());
                        SingleMailDeliveryTask task = new SingleMailDeliveryTask(factory, calUtil, ctxService.getContext(trigger.getCtx()), alarm, trigger, currentUTCTime.getTimeInMillis());
                        ScheduledTimerTask timer = timerService.schedule(task, delay, TimeUnit.MILLISECONDS);
                        scheduledTasks.put(key(trigger.getCtx(), trigger.getAccount(), trigger.getAlarmTrigger().getEventId(), alarm.getId()), timer);
                    }
                    con.commit();
                    succesfull=true;
                } catch (SQLException e) {
                    if(con != null) {
                        try {
                            con.setAutoCommit(true);
                        } catch (SQLException e1) {
                            // ignore
                        }
                    }
                } finally {
                    if(succesfull == false && con!=null) {
                        try {
                            con.rollback();
                        } catch (SQLException e) {
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
    }

    public void cancelTask(int cid, int account, String eventId, Alarm alarm) {
        ScheduledTimerTask scheduledTimerTask = scheduledTasks.get(key(cid, account, eventId, alarm.getId()));
        if(scheduledTimerTask!=null) {
            scheduledTimerTask.cancel();
        }
    }

    Key key(int cid, int account, String eventId, int alarm) {
        return new Key(cid, account, eventId, alarm);
    }

    private class SingleMailDeliveryTask implements Runnable{

        Context ctx;
        private final Alarm alarm;
        private final CalendarStorageFactory factory;
        private final AlarmTriggerWrapper wrapper;
        private final CalendarUtilities calUtil;
        private final long processed;

        /**
         * Initializes a new {@link MailAlarmDeliveryWorker.SingleMailDeliveryTask}.
         */
        public SingleMailDeliveryTask(CalendarStorageFactory factory, CalendarUtilities calUtil, Context ctx, Alarm alarm, AlarmTriggerWrapper trigger, long processed) {
            this.ctx = ctx;
            this.alarm = alarm;
            this.factory = factory;
            this.wrapper = trigger;
            this.calUtil = calUtil;
            this.processed = processed;
        }

        @Override
        public void run() {
            Connection writeCon = null;
            boolean isReadOnly = true;
            try {
                writeCon = dbservice.getWritable(ctx);
                process(writeCon);
            } catch (OXException | SQLException e) {
                try {
                    if(writeCon != null) {
                        writeCon.rollback();
                        try {
                            isReadOnly = process(writeCon);
                        } catch (SQLException | OXException e1) {
                            // Nothing that can be done. Do a rollback and reset the processed value
                            writeCon.rollback();
                            try {
                                factory.createAdministrative().getAlarmTriggerStorage().setProcessingStatus(writeCon, Collections.singletonList(wrapper), 0l);
                                isReadOnly = false;
                            } catch (OXException e2) {
                                // ignore
                            }
                        }
                    }
                } catch (SQLException sql) {
                    // ignore
                }
            } finally {
                if(isReadOnly) {
                    dbservice.backWritableAfterReading(ctx, writeCon);
                } else {
                    dbservice.backWritable(ctx, writeCon);
                }
            }
        }

        private boolean process(Connection writeCon) throws OXException, SQLException {
            boolean isReadOnly = true;
            SimpleDBProvider provider = new SimpleDBProvider(writeCon, writeCon);
            writeCon.setAutoCommit(false);
            CalendarStorage storage = factory.create(ctx, wrapper.getAccount(), optEntityResolver(ctx.getContextId()), provider, DBTransactionPolicy.NO_TRANSACTIONS);
            AlarmTrigger loadedTrigger = storage.getAlarmTriggerStorage().loadTrigger(wrapper.getAlarmTrigger().getAlarm());
            if(loadedTrigger == null || loadedTrigger.getProcessed() != processed) {
                // Abort since the triggers is either gone or picked up by another node (e.g. because of an update)
                LOG.info("Skipped task since its trigger is not up to date!");
                return isReadOnly;
            }
            Event event = storage.getEventStorage().loadEvent(wrapper.getAlarmTrigger().getEventId(), null);
            storage.getUtilities().loadAdditionalEventData(wrapper.getAlarmTrigger().getUserId(), event, null);
            List<Alarm> alarms = event.getAlarms();
            for(Alarm tmpAlarm: alarms) {
                if(tmpAlarm.getId() == alarm.getId()) {
                    tmpAlarm.setAcknowledged(new Date());
                    break;
                }
            }
            storage.getAlarmStorage().updateAlarms(event, wrapper.getAlarmTrigger().getUserId(), alarms);
            isReadOnly = false;
            Map<Integer, List<Alarm>> loadAlarms = storage.getAlarmStorage().loadAlarms(event);
            storage.getAlarmTriggerStorage().deleteTriggers(event.getId());
            storage.getAlarmTriggerStorage().insertTriggers(event, loadAlarms);
            writeCon.commit();
            writeCon.setAutoCommit(true);
            LOG.info("Mail successfully send!");
            try {
                mailService.send(event, ctx.getContextId(), wrapper.getAlarmTrigger().getUserId());
            } catch(OXException e) {
                LOG.warn("Unable to send mail for calendar alarm: "+e.getMessage(), e);
            }
            scheduledTasks.remove(key(ctx.getContextId(), wrapper.getAccount(), event.getId(), alarm.getId()));
            return isReadOnly;
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
                LOG.warn("Error getting entity resolver for context {}: {}", Integer.valueOf(contextId), e.getMessage(), e);
            }
            return null;
        }

    }

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
            if(obj instanceof Key) {
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
            return "Key ["+cid+"|"+account+"|"+eventId+"|"+id+"]";
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
     * Cancels all tasks for the given event id
     *
     * @param eventID The event id to cancel for
     */
    public void cancelAll(String eventId) {
        Iterator<Entry<Key, ScheduledTimerTask>> iterator = scheduledTasks.entrySet().iterator();
        while(iterator.hasNext()) {
            Entry<Key, ScheduledTimerTask> next = iterator.next();
            if(next.getKey().getEventId().equals(eventId)) {
                LOG.info("Canceled "+next.getKey().toString());
                next.getValue().cancel();
                iterator.remove();
            }
        }
    }

    public void checkEvents(List<Event> events, int cid, int account) {
        Connection con;
        try {
            con = dbservice.getWritable(cid);

            boolean readonly = true;
            try {
                AdministrativeCalendarStorage storage = factory.createAdministrative();
                Calendar cal = Calendar.getInstance(UTC);
                cal.setTimeInMillis(lastCheck);
                cal.add(timeUnit, lookAhead);
                for (Event event : events) {
                    List<AlarmTriggerWrapper> wrappers = storage.getAlarmTriggerStorage().getAlarmTriggers(con, cid, account, event.getId());
                    // Schedule a task for all triggers before the next usual interval
                    for (AlarmTriggerWrapper wrapper : wrappers) {
                        if (wrapper == null || wrapper.getAlarmTrigger().getTime() > cal.getTimeInMillis()) {
                            continue;
                        }
                        Alarm alarm = storage.getAlarmStorage().getAlarm(con, cid, account, wrapper.getAlarmTrigger().getAlarm());
                        scheduleTask(con, key(cid, account, event.getId(), alarm.getId()), alarm, wrapper);
                        readonly = false;
                    }
                }
            } finally {
                if (readonly) {
                    dbservice.backWritableAfterReading(cid, con);
                } else {
                    dbservice.backWritable(cid, con);
                }
            }
        } catch (OXException e) {
            // Can be ignored. Triggers are picked up with the next run of the MailAlarmDeliveryWorker thread
        }
    }

    private void scheduleTask(Connection con, Key key, Alarm alarm, AlarmTriggerWrapper wrapper) {
        ScheduledTimerTask scheduledTimerTask = scheduledTasks.get(key);
        if(scheduledTimerTask!=null) {
            LOG.trace("Canceled mail alarm task for cid={}, account={}, alarm {}", key.getCid(), key.getAccount(), alarm.getId());
            scheduledTimerTask.cancel();
            scheduledTasks.remove(key);
        }
        boolean processingSet = false;
        try {
            Calendar calTriggerTime = Calendar.getInstance(UTC);
            calTriggerTime.setTimeInMillis(wrapper.getAlarmTrigger().getTime());
            Calendar now = Calendar.getInstance(UTC);

            factory.createAdministrative().getAlarmTriggerStorage().setProcessingStatus(con, Collections.singletonList(wrapper), now.getTimeInMillis());
            processingSet = true;

            long delay = calTriggerTime.getTimeInMillis() - now.getTimeInMillis();
            if (delay < 0) {
                delay = 0;
            }

            LOG.info("Created new task for cid={}, account={}, alarm {}", key.getCid(), key.getAccount(), alarm.getId());
            SingleMailDeliveryTask task = new SingleMailDeliveryTask(factory, calUtil, ctxService.getContext(key.getCid()), alarm, wrapper, now.getTimeInMillis());
            ScheduledTimerTask timer = timerService.schedule(task, delay, TimeUnit.MILLISECONDS);
            scheduledTasks.put(key, timer);
        } catch (OXException e) {
            if(processingSet) {
                try {
                    // If the error is thrown after the processed value is successfully set then set it back to 0 so the next task can pick it up
                    factory.createAdministrative().getAlarmTriggerStorage().setProcessingStatus(con, Collections.singletonList(wrapper), 0l);
                } catch (OXException e1) {
                    // Can be ignored. The trigger is picked up once the trigger time is overdue.
                }
            }
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Cancels all running thread and tries to reset the processed values
     */
    public void cancel() {
        AdministrativeCalendarStorage storage = null;
        try {
            storage = factory.createAdministrative();
        } catch (OXException e) {
            // ignore
        }
        for (Entry<Key, ScheduledTimerTask> entry : scheduledTasks.entrySet()) {
            Key key = entry.getKey();
            Connection con = null;
            try {
                con = dbservice.getWritable(key.getCid());

                if (storage != null && con != null) {
                    try {
                        AlarmTrigger trigger = new AlarmTrigger();
                        trigger.setAlarm(key.getId());
                        storage.getAlarmTriggerStorage().setProcessingStatus(con, Collections.singletonList(new AlarmTriggerWrapper(trigger, key.getCid(), key.getAccount())), 0l);
                    } catch (OXException e) {
                        // ignore
                    }
                }
            } catch (OXException e1) {
                // ignore
            }
            entry.getValue().cancel();
        }
    }

}

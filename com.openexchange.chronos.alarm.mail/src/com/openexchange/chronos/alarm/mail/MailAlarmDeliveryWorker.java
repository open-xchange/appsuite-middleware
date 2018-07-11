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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    final AdministrativeCalendarStorage storage;
    final DatabaseService dbservice;
    private final int calendarType, timeframe;
    private final ContextService ctxService;
    private final TimerService timerService;

    Map<Key, ScheduledTimerTask> scheduledTasks = new ConcurrentHashMap<>();
    private final CalendarStorageFactory factory;
    private final CalendarUtilities calUtil;
    private long lastCheck=0;

    /**
     * Initializes a new {@link MailAlarmDeliveryWorker}.
     * @throws OXException
     */
    public MailAlarmDeliveryWorker(CalendarStorageFactory factory, DatabaseService dbservice, ContextService ctxService, CalendarUtilities calUtil, TimerService timerService, int timeframe, int calendarType) throws OXException {
        this.storage = factory.createAdministrative();
        this.dbservice = dbservice;
        this.timeframe = timeframe;
        this.calendarType = calendarType;
        this.ctxService = ctxService;
        this.timerService = timerService;
        this.factory = factory;
        this.calUtil = calUtil;
    }

    @Override
    public void run() {
        Calendar until = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        until.add(calendarType, timeframe);
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
                    Calendar currentUTCTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    List<AlarmTriggerWrapper> lockedTriggers = storage.getAlarmTriggerStorage().getAndLockTriggers(con, until.getTime());
                    lastCheck = currentUTCTime.getTimeInMillis();
                    if(lockedTriggers.isEmpty()) {
                        continue;
                    }

                    storage.getAlarmTriggerStorage().setProcessingStatus(con, lockedTriggers, currentUTCTime.getTimeInMillis());
                    readOnly = false;
                    for(AlarmTriggerWrapper trigger: lockedTriggers) {

                        Alarm alarm = storage.getAlarmStorage().getAlarm(con, trigger.getCtx(), trigger.getAccount(), trigger.getAlarmTrigger().getAlarm());
                        Calendar calTriggerTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calTriggerTime.setTimeInMillis(trigger.getAlarmTrigger().getTime());

                        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

                        long delay = calTriggerTime.getTimeInMillis() - now.getTimeInMillis();
                        if(delay < 0) {
                            delay = 0;
                        }
                        LOG.info("Created new task for cid={}, account={}, alarm {}", trigger.getCtx(), trigger.getAccount(), alarm.getId());
                        SingleMailDeliveryTask task = new SingleMailDeliveryTask(factory, calUtil, ctxService.getContext(trigger.getCtx()), trigger.getAccount(), alarm, trigger.getAlarmTrigger(), currentUTCTime.getTimeInMillis());
                        ScheduledTimerTask timer = timerService.schedule(task, delay, TimeUnit.MILLISECONDS);
                        scheduledTasks.put(key(trigger.getCtx(), trigger.getAccount(), trigger.getAlarmTrigger().getEventId(), alarm.getId()), timer);
                    }
                } finally {
                    if (readOnly) {
                        dbservice.backForUpdateTaskAfterReading(ctxId, con);
                    } else {
                        dbservice.backForUpdateTask(ctxId, con);
                    }
                }

            }
        } catch (OXException e) {
            //TODO handle error
        }
    }

    public void cancelTask(int cid, int account, String eventId, Alarm alarm) {
        ScheduledTimerTask scheduledTimerTask = scheduledTasks.get(key(cid, account, eventId, alarm.getId()));
        if(scheduledTimerTask!=null) {
            scheduledTimerTask.cancel();
        }
    }

    private Key key(int cid, int account, String eventId, int alarm) {
        return new Key(cid, account, eventId, alarm);
    }

    private class SingleMailDeliveryTask implements Runnable{

        Context ctx;
        private final int account;
        private final Alarm alarm;
        private final CalendarStorageFactory factory;
        private final AlarmTrigger trigger;
        private final CalendarUtilities calUtil;
        private final long processed;

        /**
         * Initializes a new {@link MailAlarmDeliveryWorker.SingleMailDeliveryTask}.
         */
        public SingleMailDeliveryTask(CalendarStorageFactory factory, CalendarUtilities calUtil, Context ctx, int account, Alarm alarm, AlarmTrigger trigger, long processed) {
            this.ctx = ctx;
            this.account = account;
            this.alarm = alarm;
            this.factory = factory;
            this.trigger = trigger;
            this.calUtil = calUtil;
            this.processed = processed;
        }

        @Override
        public void run() {
            try {
                CalendarStorage storage = factory.create(ctx, account, optEntityResolver(ctx.getContextId()));
                AlarmTrigger loadedTrigger = storage.getAlarmTriggerStorage().loadTrigger(trigger.getAlarm());
                if(loadedTrigger == null || loadedTrigger.getProcessed() != processed) {
                    // Abort since the triggers is either gone or picked up by another node (e.g. because of an update)
                    LOG.info("Skipped task since its trigger is not up to date!");
                    return;
                }
                Event event = storage.getEventStorage().loadEvent(trigger.getEventId(), null);
                storage.getUtilities().loadAdditionalEventData(trigger.getUserId(), event, null);
                List<Alarm> alarms = event.getAlarms();
                for(Alarm tmpAlarm: alarms) {
                    if(tmpAlarm.getId() == alarm.getId()) {
                        tmpAlarm.setAcknowledged(new Date());
                        break;
                    }
                }
                storage.getAlarmStorage().updateAlarms(event, trigger.getUserId(), alarms);
                Map<Integer, List<Alarm>> loadAlarms = storage.getAlarmStorage().loadAlarms(event);
                storage.getAlarmTriggerStorage().deleteTriggers(event.getId());
                storage.getAlarmTriggerStorage().insertTriggers(event, loadAlarms);
                LOG.info("Mail successfully send!");
                // TODO send mail
            } catch (OXException e) {
                // TODO handle exception
                // retry one time and then reset processed status?!
            }
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
    }

    /**
     * Cancels all tasks for the given event id
     *
     * @param eventID The event id to cancel for
     */
    public void cancelAll(String eventId) {
        for(Key key:scheduledTasks.keySet()) {
            if(key.getEventId().equals(eventId)) {
                scheduledTasks.get(key).cancel();
            }
        }
    }

    public void checkEvents(List<Event> events, int cid, int account) throws OXException {
        Connection con = dbservice.getWritable(cid);
        boolean readonly = true;
        try {
            AdministrativeCalendarStorage storage = factory.createAdministrative();
            for (Event event : events) {
                List<AlarmTriggerWrapper> wrappers = storage.getAlarmTriggerStorage().getAlarmTriggers(con, cid, account, event.getId());
                // Schedule a task for all triggers before the next usual interval
                for(AlarmTriggerWrapper wrapper : wrappers) {
                    if (wrapper == null || wrapper.getAlarmTrigger().getTime() > lastCheck + TimeUnit.MINUTES.toMillis(10)) {
                        continue;
                    }
                    Alarm alarm = storage.getAlarmStorage().getAlarm(con, cid, account, wrapper.getAlarmTrigger().getAlarm());
                    scheduleTask(con, key(cid, account, event.getId(), alarm.getId()), alarm, wrapper);
                    readonly = false;
                }
            }
        } finally {
            if(readonly) {
                dbservice.backWritableAfterReading(cid, con);
            } else {
                dbservice.backWritable(cid, con);
            }
        }
    }

    private void scheduleTask(Connection con, Key key, Alarm alarm, AlarmTriggerWrapper wrapper) {
        ScheduledTimerTask scheduledTimerTask = scheduledTasks.get(key);
        if(scheduledTimerTask!=null) {
            LOG.info("Canceled old task for cid={}, account={}, alarm {}", key.getCid(), key.getAccount(), alarm.getId());
            scheduledTimerTask.cancel();
            scheduledTasks.remove(key);
        }

        try {
            Calendar calTriggerTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calTriggerTime.setTimeInMillis(wrapper.getAlarmTrigger().getTime());
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            factory.createAdministrative().getAlarmTriggerStorage().setProcessingStatus(con, Collections.singletonList(wrapper), now.getTimeInMillis());

            long delay = calTriggerTime.getTimeInMillis() - now.getTimeInMillis();
            if (delay < 0) {
                delay = 0;
            }

            LOG.info("Created new task for cid={}, account={}, alarm {}", key.getCid(), key.getAccount(), alarm.getId());
            SingleMailDeliveryTask task = new SingleMailDeliveryTask(factory, calUtil, ctxService.getContext(key.getCid()), key.getAccount(), alarm, wrapper.getAlarmTrigger(), now.getTimeInMillis());
            ScheduledTimerTask timer = timerService.schedule(task, delay, TimeUnit.MILLISECONDS);
            scheduledTasks.put(key, timer);
        } catch (OXException e) {
            //TODO handle exception
        }
    }

}

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

package com.openexchange.chronos.alarm.message.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.chronos.provider.AdministrativeCalendarProvider;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AdministrativeAlarmTriggerStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.Pair;
import com.openexchange.ratelimit.Rate;
import com.openexchange.ratelimit.RateLimiterFactory;

/**
 *
 * {@link SingleMessageDeliveryTask} executes the delivery for a calendar message alarm.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
class SingleMessageDeliveryTask implements Runnable {

    public static class Builder {
        Context ctx;
        private Alarm alarm;
        private CalendarStorageFactory factory;
        private AlarmTrigger trigger;
        private CalendarUtilities calUtil;
        private long processed;
        private int account;
        private MessageAlarmDeliveryWorker callback;
        private DatabaseService dbservice;
        private AdministrativeAlarmTriggerStorage storage;
        private AlarmNotificationService alarmNotificationService;
        private CalendarProviderRegistry calendarProviderRegistry;
        private AdministrativeCalendarAccountService administrativeCalendarAccountService;
        private RateLimiterFactory rateLimitFactory;

        public Builder setCtx(Context ctx) {
            this.ctx = ctx;
            return this;
        }

        public Builder setAlarm(Alarm alarm) {
            this.alarm = alarm;
            return this;
        }

        public Builder setFactory(CalendarStorageFactory factory) {
            this.factory = factory;
            return this;
        }

        public Builder setTrigger(AlarmTrigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder setCalUtil(CalendarUtilities calUtil) {
            this.calUtil = calUtil;
            return this;
        }

        public Builder setProcessed(long processed) {
            this.processed = processed;
            return this;
        }

        public Builder setAccount(int account) {
            this.account = account;
            return this;
        }

        public Builder setCallback(MessageAlarmDeliveryWorker callback) {
            this.callback = callback;
            return this;
        }

        public Builder setDbservice(DatabaseService dbservice) {
            this.dbservice = dbservice;
            return this;
        }

        public Builder setStorage(AdministrativeAlarmTriggerStorage storage) {
            this.storage = storage;
            return this;
        }

        public Builder setAlarmNotificationService(AlarmNotificationService alarmNotificationService) {
            this.alarmNotificationService = alarmNotificationService;
            return this;
        }

        public Builder setCalendarProviderRegistry(CalendarProviderRegistry calendarProviderRegistry) {
            this.calendarProviderRegistry = calendarProviderRegistry;
            return this;
        }

        public Builder setAdministrativeCalendarAccountService(AdministrativeCalendarAccountService administrativeCalendarAccountService) {
            this.administrativeCalendarAccountService = administrativeCalendarAccountService;
            return this;
        }

        public Builder setRateLimitFactory(RateLimiterFactory rateLimitFactory) {
            this.rateLimitFactory = rateLimitFactory;
            return this;
        }

        public SingleMessageDeliveryTask build() {
            return new SingleMessageDeliveryTask(   dbservice,
                                                    storage,
                                                    alarmNotificationService,
                                                    factory,
                                                    calUtil,
                                                    calendarProviderRegistry,
                                                    administrativeCalendarAccountService,
                                                    rateLimitFactory,
                                                    ctx,
                                                    account,
                                                    alarm,
                                                    trigger,
                                                    processed,
                                                    callback);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SingleMessageDeliveryTask.class);

    Context ctx;
    private final Alarm alarm;
    private final CalendarStorageFactory factory;
    private final AlarmTrigger trigger;
    private final CalendarUtilities calUtil;
    private final long processed;
    private final int account;
    private final MessageAlarmDeliveryWorker callback;
    private final DatabaseService dbservice;
    private final AdministrativeAlarmTriggerStorage storage;
    private final AlarmNotificationService notificationService;
    private final CalendarProviderRegistry calendarProviderRegistry;
    private final AdministrativeCalendarAccountService administrativeCalendarAccountService;
    private final RateLimiterFactory rateLimitFactory;

    /**
     * Initializes a new {@link SingleMessageDeliveryTask}.
     *
     * @param dbservice The {@link DatabaseService}
     * @param storage The {@link AdministrativeAlarmTriggerStorage}
     * @param notificationService The {@link AlarmNotificationService}
     * @param factory The {@link CalendarStorageFactory}
     * @param calUtil The {@link CalendarUtilities}
     * @param calendarProviderRegistry The {@link CalendarProviderRegistry}
     * @param administrativeCalendarAccountService An {@link AdministrativeCalendarAccountService}
     * @param ctx The {@link Context}
     * @param account The account id
     * @param alarm The {@link Alarm}
     * @param trigger The {@link AlarmTrigger}
     * @param callback The {@link MessageAlarmDeliveryWorker} which started this task
     * @param processed The processed value
     */
    protected SingleMessageDeliveryTask(   DatabaseService dbservice,
                                        AdministrativeAlarmTriggerStorage storage,
                                        AlarmNotificationService notificationService,
                                        CalendarStorageFactory factory,
                                        CalendarUtilities calUtil,
                                        CalendarProviderRegistry calendarProviderRegistry,
                                        AdministrativeCalendarAccountService administrativeCalendarAccountService,
                                        RateLimiterFactory rateLimitFactory,
                                        Context ctx,
                                        int account,
                                        Alarm alarm,
                                        AlarmTrigger trigger,
                                        long processed,
                                        MessageAlarmDeliveryWorker callback) {
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
        this.notificationService = notificationService;
        this.calendarProviderRegistry = calendarProviderRegistry;
        this.administrativeCalendarAccountService = administrativeCalendarAccountService;
        this.rateLimitFactory = rateLimitFactory;

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
            if (event != null) {
                Databases.autocommit(writeCon);
                dbservice.backWritable(ctx, writeCon);
                writeCon = null;
                // send the message
                sendMessage(event);
                // If the triggers has been updated (deleted + inserted) check if a trigger needs to be scheduled.
                writeCon = dbservice.getWritable(ctx);
                writeCon.setAutoCommit(false);
                isReadOnly = checkEvent(writeCon, event);
                writeCon.commit();
            }
        } catch (OXException | SQLException e) {
            if (writeCon != null) {
                // rollback the last transaction
                Databases.rollback(writeCon);
                // if the error occurred during the process retry the hole operation once
                if (processFinished == false) {
                    try {
                        writeCon.setAutoCommit(false);
                        // do the delivery and update the db entries (e.g. like setting the acknowledged field)
                        Event event = prepareEvent(writeCon);
                        writeCon.commit();
                        if (event != null) {
                            isReadOnly = false;
                            sendMessage(event);
                        }
                        processFinished = true;
                        // If the triggers has been updated (deleted + inserted) check if a trigger needs to be scheduled.
                        if (event != null) {
                            checkEvent(writeCon, event);
                            writeCon.commit();
                        }
                    } catch (SQLException | OXException e1) {
                        // Nothing that can be done. Do a rollback and reset the processed value if necessary
                        Databases.rollback(writeCon);
                        if (processFinished == false) {
                            try {
                                storage.setProcessingStatus(writeCon, Collections.singletonMap(new Pair<>(ctx.getContextId(), account), Collections.singletonList(trigger)), 0l);
                                isReadOnly = false;
                            } catch (OXException e2) {
                                // ignore
                            }
                        }
                    }
                }
            }
        } finally {
            callback.remove(new Key(ctx.getContextId(), account, trigger.getEventId(), alarm.getId()));
            Databases.autocommit(writeCon);
            if (writeCon != null) {
                if (isReadOnly) {
                    dbservice.backWritableAfterReading(ctx, writeCon);
                } else {
                    dbservice.backWritable(ctx, writeCon);
                }
            }
        }
    }

    /**
     * Checks if the delivery is still necessary and prepares the event for it:
     * - Acknowledges the alarm
     * - Deletes and reinserts the alarm triggers
     * - updates the event timestamp
     *
     * @param writeCon A writable connection
     * @return The prepared Event or null
     * @throws OXException
     */
    private Event prepareEvent(Connection writeCon) throws OXException {
        SimpleDBProvider provider = new SimpleDBProvider(writeCon, writeCon);
        CalendarStorage calStorage = factory.create(ctx, account, optEntityResolver(ctx.getContextId()), provider, DBTransactionPolicy.NO_TRANSACTIONS);
        AlarmTrigger loadedTrigger = calStorage.getAlarmTriggerStorage().loadTrigger(trigger.getAlarm());
        if (loadedTrigger == null || loadedTrigger.getProcessed() != processed) {
            // Abort since the triggers is either gone or picked up by another node (e.g. because of an update)
            LOG.trace("Skipped message alarm task for {}. Its trigger is not up to date!", new Key(ctx.getContextId(), account, trigger.getEventId(), alarm.getId()));
            return null;
        }
        Event event = null;
        CalendarAccount calendarAccount = null;
        AdministrativeCalendarProvider adminCalProvider = null;
        try {
            calendarAccount = administrativeCalendarAccountService.getAccount(ctx.getContextId(), trigger.getUserId(), account);
            if (calendarAccount == null) {
                LOG.trace("Unable to load calendar account.");
                return null;
            }
            CalendarProvider calendarProvider = calendarProviderRegistry.getCalendarProvider(calendarAccount.getProviderId());
            if (calendarProvider instanceof AdministrativeCalendarProvider) {
                adminCalProvider = (AdministrativeCalendarProvider) calendarProvider;
                event = adminCalProvider.getEventByAlarm(ctx, calendarAccount, trigger.getEventId(), null);
            } else {
                LOG.trace("Unable to load event for the given provider.");
                return null;
            }
        } catch (OXException e) {
            LOG.trace("Unable to load event with id {}: {}",trigger.getEventId(), e.getMessage());
            throw e;
        }
        if (event == null) {
            LOG.trace("Unable to load event with id {}.", trigger.getEventId());
            return null;
        }
        List<Alarm> alarms = event.getAlarms();
        for (Alarm tmpAlarm : alarms) {
            if (tmpAlarm.getId() == alarm.getId()) {
                tmpAlarm.setAcknowledged(new Date());
                break;
            }
        }
        calStorage.getAlarmStorage().updateAlarms(event, trigger.getUserId(), alarms);
        Map<Integer, List<Alarm>> loadAlarms = calStorage.getAlarmStorage().loadAlarms(event);
        calStorage.getAlarmTriggerStorage().deleteTriggers(event.getId());
        calStorage.getAlarmTriggerStorage().insertTriggers(event, loadAlarms);
        if (adminCalProvider != null && calendarAccount != null) {
            adminCalProvider.touchEvent(ctx, calendarAccount, trigger.getEventId());
        }
        return event;
    }

    /**
     * Delivers the message
     *
     * @param event The event of the alarm
     */
    private void sendMessage(Event event) {
            Key key = new Key(ctx.getContextId(), account, event.getId(), alarm.getId());
            try {
                int userId = trigger.getUserId();
                int contextId = ctx.getContextId();
                if(notificationService.isEnabled(userId, contextId)) {
                if (checkRateLimit(alarm.getAction(), notificationService.getRate(userId, contextId), trigger.getUserId(), ctx.getContextId())) {
                        notificationService.send(event, alarm, ctx.getContextId(), account, trigger.getUserId(), trigger.getTime().longValue());
                        LOG.trace("Message successfully send for {}", key);
                    } else {
                        LOG.info("Due to the rate limit it is not possible to send the message for {}", key);
                    }
                } else {
                    LOG.trace("Message dropped because the AlarmNotificationService is not enabled for user {}.", trigger.getUserId());
                }
            } catch (OXException e) {
                LOG.warn("Unable to send message for calendar alarm ({}): {}", key, e.getMessage(), e);
            }
    }

    private static final String RATE_LIMIT_PREFIX = "MESSAGE_ALARM_";

    /**
     *
     */
    private boolean checkRateLimit(AlarmAction action, Rate rate, int userId, int ctxId) {
        if (!rate.isEnabled() || rateLimitFactory == null) {
            return true;
        }
        try {
            return rateLimitFactory.createLimiter(RATE_LIMIT_PREFIX + action.getValue(), rate, userId, ctxId).acquire();
        } catch (OXException e) {
            LOG.warn("Unable to create RateLimiter.", e);
            return false;
        }
    }

    private boolean checkEvent(Connection writeCon, Event event) throws OXException {
        int cid = ctx.getContextId();
        List<AlarmTrigger> triggers = callback.checkEvents(writeCon, Collections.singletonList(event), cid, account, true);
        if (triggers.isEmpty() == false) {
            CalendarStorage calStorage = factory.create(ctx, account, optEntityResolver(cid), new SimpleDBProvider(writeCon, writeCon), DBTransactionPolicy.NO_TRANSACTIONS);
            for (AlarmTrigger tmpTrigger : triggers) {
                callback.scheduleTaskForEvent(writeCon, calStorage, new Key(cid, account, tmpTrigger.getEventId(), tmpTrigger.getAlarm()), tmpTrigger);
            }
            return false;
        }
        return true;
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
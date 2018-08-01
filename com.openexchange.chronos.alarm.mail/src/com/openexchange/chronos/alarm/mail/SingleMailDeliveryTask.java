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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.Pair;

/**
 *
 * {@link SingleMailDeliveryTask} executes the mail delivery for a calendar mail alarm.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
class SingleMailDeliveryTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SingleMailDeliveryTask.class);

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
            if (event != null) {
                isReadOnly = false;
                sendMail(event);
            }
            // If the triggers has been updated (deleted + inserted) check if a trigger needs to be scheduled.
            if (event != null) {
                checkEvent(writeCon, event);
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
                            sendMail(event);
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
        CalendarStorage storage = factory.create(ctx, account, optEntityResolver(ctx.getContextId()), provider, DBTransactionPolicy.NO_TRANSACTIONS);
        AlarmTrigger loadedTrigger = storage.getAlarmTriggerStorage().loadTrigger(trigger.getAlarm());
        if (loadedTrigger == null || loadedTrigger.getProcessed() != processed) {
            // Abort since the triggers is either gone or picked up by another node (e.g. because of an update)
            LOG.trace("Skipped mail alarm task for {}. Its trigger is not up to date!", new Key(ctx.getContextId(), account, trigger.getEventId(), alarm.getId()));
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
        Key key = new Key(ctx.getContextId(), account, event.getId(), alarm.getId());
        try {
            mailService.send(event, ctx.getContextId(), trigger.getUserId(), trigger.getTime().longValue());
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
        int cid = ctx.getContextId();
        List<AlarmTrigger> triggers = callback.checkEvents(writeCon, Collections.singletonList(event), cid, account, true);
        if (triggers.isEmpty() == false) {
            CalendarStorage calStorage = factory.create(ctx, account, optEntityResolver(cid), new SimpleDBProvider(writeCon, writeCon), DBTransactionPolicy.NO_TRANSACTIONS);
            for (AlarmTrigger trigger : triggers) {
                callback.scheduleTaskForEvent(writeCon, calStorage, new Key(cid, account, trigger.getEventId(), trigger.getAlarm()), trigger);
            }
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
            LOG.trace("Error getting entity resolver for context: {}", Integer.valueOf(contextId), e);
        }
        return null;
    }

}
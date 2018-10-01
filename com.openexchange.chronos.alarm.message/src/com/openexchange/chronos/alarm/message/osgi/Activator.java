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

package com.openexchange.chronos.alarm.message.osgi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmConfigTreeItem;
import com.openexchange.chronos.alarm.message.impl.AlarmNotificationServiceRegistry;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmCalendarHandler;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmConfig;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmDeliveryWorker;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmDeliveryWorkerUpdateTask;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.storage.AdministrativeAlarmTriggerStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.Tools;
import com.openexchange.ratelimit.RateLimiterFactory;
import com.openexchange.resource.ResourceService;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.templating.TemplateService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class Activator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private static final String CLUSTER_ID = "com.openexchange.chronos.alarm.mail.worker";

    private final Map<ScheduledTimerTask, MessageAlarmDeliveryWorker> scheduledTasks = new ConcurrentHashMap<>();

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class, DatabaseService.class, TimerService.class, CalendarStorageFactory.class, CalendarUtilities.class,
            LeanConfigurationService.class, UserService.class, ServerConfigService.class, NotificationMailFactory.class, TranslatorFactory.class,
            ConfigurationService.class, ClusterTimerService.class, AdministrativeAlarmTriggerStorage.class, TemplateService.class,
            ResourceService.class, HtmlService.class, CalendarProviderRegistry.class, AdministrativeCalendarAccountService.class, RateLimiterFactory.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final MessageAlarmDeliveryWorkerUpdateTask task = new MessageAlarmDeliveryWorkerUpdateTask();
        registerService(UpdateTaskProviderService.class.getName(), new UpdateTaskProviderService() {

            @Override
            public Collection<UpdateTaskV2> getUpdateTasks() {
                return Arrays.asList(((UpdateTaskV2) task));
            }
        });
        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, this);
        if (!leanConfig.getBooleanProperty(MessageAlarmConfig.ENABLED)) {
            LOG.info("Skipped starting the mail alarm delivery worker, because it is disabled.");
            LOG.info("Successfully started bundle "+this.context.getBundle().getSymbolicName());
            return;
        }

        AdministrativeAlarmTriggerStorage storage = Tools.requireService(AdministrativeAlarmTriggerStorage.class, this);
        TimerService timerService = Tools.requireService(TimerService.class, this);
        ClusterTimerService clusterTimerService = Tools.requireService(ClusterTimerService.class, this);
        DatabaseService dbService = Tools.requireService(DatabaseService.class, this);
        CalendarStorageFactory calendarStorageFactory = Tools.requireService(CalendarStorageFactory.class, this);
        ContextService ctxService = Tools.requireService(ContextService.class, this);
        CalendarUtilities calUtil = Tools.requireService(CalendarUtilities.class, this);
        CalendarProviderRegistry calendarProviderRegistry = Tools.requireService(CalendarProviderRegistry.class, this);
        AdministrativeCalendarAccountService administrativeCalendarAccountService = Tools.requireService(AdministrativeCalendarAccountService.class, this);
        RateLimiterFactory rateLimitFactory = Tools.requireService(RateLimiterFactory.class, this);


        int period = leanConfig.getIntProperty(MessageAlarmConfig.PERIOD);
        int lookAhead = leanConfig.getIntProperty(MessageAlarmConfig.LOOK_AHEAD);
        if (lookAhead < period) {
            LOG.warn("The {} value is smaller than the {} value. Falling back to {}.", MessageAlarmConfig.LOOK_AHEAD.getFQPropertyName(), MessageAlarmConfig.PERIOD.getFQPropertyName(), period);
            lookAhead = period;
        }
        int initialDelay = leanConfig.getIntProperty(MessageAlarmConfig.INITIAL_DELAY);
        int workerCount = leanConfig.getIntProperty(MessageAlarmConfig.WORKER_COUNT);
        if (workerCount <= 0) {
            workerCount = 1;
        }
        int overdueWaitTime = Math.abs(leanConfig.getIntProperty(MessageAlarmConfig.OVERDUE));
        if (workerCount > 1) {
            LOG.warn("Using " + workerCount + " mail alarm worker. Increasing the value above 1 should not be used in a production environment and only be used for testing purposes.");
        }

        AlarmNotificationServiceRegistry registry = new AlarmNotificationServiceRegistry();
        track(AlarmNotificationService.class, registry);
        openTrackers();

        boolean registeredCalendarHandler = false;
        for (int x = 0; x < workerCount; x++) {
            MessageAlarmDeliveryWorker worker = new MessageAlarmDeliveryWorker.Builder()
                                                 .setStorage(storage)
                                                 .setCalendarStorageFactory(calendarStorageFactory)
                                                 .setDbService(dbService)
                                                 .setCtxService(ctxService)
                                                 .setCalUtil(calUtil)
                                                 .setTimerService(timerService)
                                                 .setAlarmNotificationServiceRegistry(registry)
                                                 .setCalendarProviderRegistry(calendarProviderRegistry)
                                                 .setAdministrativeCalendarAccountService(administrativeCalendarAccountService)
                                                 .setLookAhead(lookAhead)
                                                 .setOverdueWaitTime(overdueWaitTime)
                                                 .setRateLimitFactory(rateLimitFactory).build();
            ScheduledTimerTask scheduledTimerTask = clusterTimerService.scheduleAtFixedRate(CLUSTER_ID, worker, initialDelay, period, TimeUnit.MINUTES);
            scheduledTasks.put(scheduledTimerTask, worker);
            if (!registeredCalendarHandler) {
                // only register a calendar handler for the first worker
                registerService(CalendarHandler.class, new MessageAlarmCalendarHandler(worker));
                registeredCalendarHandler = true;
            }
        }

        MessageAlarmConfigTreeItem item = new MessageAlarmConfigTreeItem(registry);
        registerService(PreferencesItemService.class, item);
        registerService(ConfigTreeEquivalent.class, item);
        LOG.info("Successfully started bundle "+this.context.getBundle().getSymbolicName());
    }

    @Override
    protected void stopBundle() throws Exception {
        for (Entry<ScheduledTimerTask, MessageAlarmDeliveryWorker> entry : scheduledTasks.entrySet()) {
            entry.getValue().cancel();
            entry.getKey().cancel(true);
        }
        super.stopBundle();
        LOG.info("Successfully stopped bundle "+this.context.getBundle().getSymbolicName());
    }

}

/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.alarm.message.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.chronos.alarm.message.impl.AlarmNotificationServiceRegistry;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmCalendarHandler;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmConfig;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmConfigTreeItem;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmDeliveryWorker;
import com.openexchange.chronos.alarm.message.impl.MessageAlarmDeliveryWorkerUpdateTask;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.AdministrativeAlarmTriggerStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.cleanup.DefaultCleanUpJob;
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
    private MessageAlarmDeliveryWorker worker;
    private CleanUpInfo jobInfo;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class, DatabaseService.class, TimerService.class, CalendarStorageFactory.class, CalendarUtilities.class,
            LeanConfigurationService.class, UserService.class, ServerConfigService.class, NotificationMailFactory.class, TranslatorFactory.class,
            ConfigurationService.class, AdministrativeAlarmTriggerStorage.class, TemplateService.class,
            ResourceService.class, HtmlService.class, CalendarProviderRegistry.class, AdministrativeCalendarAccountService.class, RateLimiterFactory.class,
            RecurrenceService.class, DatabaseCleanUpService.class};
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
            LOG.info("Successfully started bundle " + this.context.getBundle().getSymbolicName());
            return;
        }

        int period = leanConfig.getIntProperty(MessageAlarmConfig.PERIOD);
        int lookAhead = leanConfig.getIntProperty(MessageAlarmConfig.LOOK_AHEAD);
        if (lookAhead < period) {
            LOG.warn("The {} value is smaller than the {} value. Falling back to {}.", MessageAlarmConfig.LOOK_AHEAD.getFQPropertyName(), MessageAlarmConfig.PERIOD.getFQPropertyName(), I(period));
            lookAhead = period;
        }
        int initialDelay = leanConfig.getIntProperty(MessageAlarmConfig.INITIAL_DELAY);
        int overdueWaitTime = Math.abs(leanConfig.getIntProperty(MessageAlarmConfig.OVERDUE));

        AlarmNotificationServiceRegistry registry = new AlarmNotificationServiceRegistry();
        track(AlarmNotificationService.class, registry);
        openTrackers();

        worker = new MessageAlarmDeliveryWorker(this, registry, lookAhead, overdueWaitTime);
        jobInfo = getServiceSafe(DatabaseCleanUpService.class).scheduleCleanUpJob(DefaultCleanUpJob.builder() //@formatter:off
                    .withId(worker.getClass())
                    .withDelay(Duration.ofMinutes(period))
                    .withInitialDelay(Duration.ofMinutes(initialDelay))
                    .withRunsExclusive(false)
                    .withExecution(worker)
                    .withPreferNoConnectionTimeout(true)
                    .build());//@formatter:on
        registerService(CalendarHandler.class, new MessageAlarmCalendarHandler(worker));

        MessageAlarmConfigTreeItem item = new MessageAlarmConfigTreeItem(registry);
        registerService(PreferencesItemService.class, item);
        registerService(ConfigTreeEquivalent.class, item);
        LOG.info("Successfully started bundle " + this.context.getBundle().getSymbolicName());
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            CleanUpInfo jobInfo = this.jobInfo;
            if (null != jobInfo) {
                this.jobInfo = null;
                jobInfo.cancel(true);
            }
        } finally {
            if (null != worker) {
                worker.cancel();
            }
            super.stopBundle();

        }
        LOG.info("Successfully stopped bundle " + this.context.getBundle().getSymbolicName());
    }

}

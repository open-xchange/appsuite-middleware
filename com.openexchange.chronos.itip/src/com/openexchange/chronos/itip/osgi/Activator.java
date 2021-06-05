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

package com.openexchange.chronos.itip.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import java.util.Dictionary;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.itip.CalendarITipIntegrationUtility;
import com.openexchange.chronos.itip.EventNotificationPool;
import com.openexchange.chronos.itip.ITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.analyzers.DefaultITipAnalyzerService;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.ITipNotificationMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.ITipNotificationParticipantResolver;
import com.openexchange.chronos.itip.handler.ITipHandler;
import com.openexchange.chronos.itip.performers.DefaultITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.sender.DefaultMailSenderService;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.sender.PoolingMailSenderService;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.resource.ResourceService;
import com.openexchange.templating.TemplateService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 *
 * {@link Activator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, TimerService.class, ContextService.class, CalendarStorageFactory.class, RecurrenceService.class, UserService.class, ResourceService.class, ICalService.class, CalendarService.class,
            HtmlService.class, TemplateService.class, CalendarUtilities.class, GroupService.class, RegionalSettingsService.class, FolderService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { HostnameService.class, DescriptionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.set(this);
        ConfigurationService config = Services.getService(ConfigurationService.class);

        TimerService timers = Services.getService(TimerService.class);
        MailSenderService sender = new DefaultMailSenderService();

        CalendarITipIntegrationUtility util = new CalendarITipIntegrationUtility();
        ITipNotificationParticipantResolver resolver = new ITipNotificationParticipantResolver(util);
        ITipNotificationMailGeneratorFactory generatorFactory = new ITipNotificationMailGeneratorFactory(resolver, util, this);

        if (config.getBoolProperty("com.openexchange.calendar.notify.poolenabled", true)) {
            int detailInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.detail", 120000);
            int stateChangeInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.states", 600000);
            int priorityInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.priority", 900000);
            
            EventNotificationPool pool = new EventNotificationPool(timers, generatorFactory, sender, detailInterval, stateChangeInterval, priorityInterval);
            sender = new PoolingMailSenderService(pool, sender);
        }

        Dictionary<String, Object> analyzerProps = withRanking(DefaultITipAnalyzerService.RANKING); // Default
        registerService(ITipAnalyzerService.class, new DefaultITipAnalyzerService(util), analyzerProps);
        Dictionary<String, Object> factoryProps = withRanking(DefaultITipActionPerformerFactoryService.RANKING); // Default
        registerService(ITipActionPerformerFactoryService.class, new DefaultITipActionPerformerFactoryService(util, sender, generatorFactory), factoryProps);

        registerService(ITipMailGeneratorFactory.class, generatorFactory);
        registerService(MailSenderService.class, sender);
        registerService(CalendarHandler.class, new ITipHandler(generatorFactory, sender));
    }

}

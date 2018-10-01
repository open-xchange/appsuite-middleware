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

package com.openexchange.chronos.itip.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Constants;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.itip.CalendarITipIntegrationUtility;
import com.openexchange.chronos.itip.EventNotificationPool;
import com.openexchange.chronos.itip.ITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.analyzers.DefaultITipAnalyzerService;
import com.openexchange.chronos.itip.generators.ITipNotificationParticipantResolver;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.ITipNotificationMailGeneratorFactory;
import com.openexchange.chronos.itip.handler.ITipHandler;
import com.openexchange.chronos.itip.performers.DefaultITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.sender.DefaultMailSenderService;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.itip.sender.PoolingMailSenderService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.HousekeepingActivator;
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
            HtmlService.class, TemplateService.class, CalendarUtilities.class, GroupService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { HostnameService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.set(this);
        ConfigurationService config = Services.getService(ConfigurationService.class);

        int detailInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.detail", 120000);
        int stateChangeInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.states", 600000);
        int priorityInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.priority", 900000);
        boolean poolEnabled = config.getBoolProperty("com.openexchange.calendar.notify.poolenabled", true);

        TimerService timers = Services.getService(TimerService.class);
        MailSenderService sender = new DefaultMailSenderService();

        CalendarITipIntegrationUtility util = new CalendarITipIntegrationUtility();
        ITipNotificationParticipantResolver resolver = new ITipNotificationParticipantResolver(util);
        ITipNotificationMailGeneratorFactory generatorFactory = new ITipNotificationMailGeneratorFactory(resolver, util, this);

        if (poolEnabled) {
            EventNotificationPool pool = new EventNotificationPool(timers, generatorFactory, sender, detailInterval, stateChangeInterval, priorityInterval);
            sender = new PoolingMailSenderService(pool, sender);
        }

        Dictionary<String, Object> analyzerProps = new Hashtable<>();
        analyzerProps.put(Constants.SERVICE_RANKING, DefaultITipAnalyzerService.RANKING); // Default
        registerService(ITipAnalyzerService.class, new DefaultITipAnalyzerService(util), analyzerProps);
        Dictionary<String, Object> factoryProps = new Hashtable<>();
        factoryProps.put(Constants.SERVICE_RANKING, DefaultITipActionPerformerFactoryService.RANKING); // Default
        registerService(ITipActionPerformerFactoryService.class, new DefaultITipActionPerformerFactoryService(util, sender, generatorFactory), factoryProps);

        registerService(ITipMailGeneratorFactory.class, generatorFactory);
        registerService(MailSenderService.class, sender);
        registerService(CalendarHandler.class, new ITipHandler(generatorFactory, sender));
    }

}

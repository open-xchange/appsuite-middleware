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

package com.openexchange.calendar.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import javax.activation.MailcapCommandMap;
import org.osgi.framework.Constants;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.api.CalendarFeature;
import com.openexchange.calendar.api.itip.CalendarITipIntegrationUtility;
import com.openexchange.calendar.api.itip.DefaultNotificationParticipantResolver;
import com.openexchange.calendar.itip.AppointmentNotificationPool;
import com.openexchange.calendar.itip.ITipAnalyzerService;
import com.openexchange.calendar.itip.ITipDingeMacherFactoryService;
import com.openexchange.calendar.itip.ITipFeature;
import com.openexchange.calendar.itip.NotifyFeature;
import com.openexchange.calendar.itip.analyzers.DefaultITipAnalyzerService;
import com.openexchange.calendar.itip.generators.AttachmentMemory;
import com.openexchange.calendar.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.calendar.itip.generators.NotificationMailGeneratorFactory;
import com.openexchange.calendar.itip.performers.DefaultITipDingeMacherFactoryService;
import com.openexchange.calendar.itip.sender.DefaultMailSenderService;
import com.openexchange.calendar.itip.sender.MailSenderService;
import com.openexchange.calendar.itip.sender.PoolingMailSenderService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.data.conversion.ical.itip.ITipEmitter;
import com.openexchange.data.conversion.ical.itip.ITipParser;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.templating.TemplateService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;


/**
 * {@link ITipActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ITipActivator extends HousekeepingActivator {

    private static List<CalendarFeature> features = new ArrayList<CalendarFeature>(2);
    private static AppointmentSqlFactory factory = null;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class, ResourceService.class, UserService.class, GroupService.class, TemplateService.class, TimerService.class, ITipEmitter.class, ConfigurationService.class, HtmlService.class, AttachmentBase.class, ITipParser.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ContextService contexts = getService(ContextService.class);
        final UserService users = getService(UserService.class);
        final ResourceService resources = getService(ResourceService.class);
        final GroupService groups = getService(GroupService.class);
        final ConfigurationService config = getService(ConfigurationService.class);
        final ITipEmitter emitter = getService(ITipEmitter.class);
        final HtmlService htmlService = getService(HtmlService.class);
        final AttachmentBase attachments = getService(AttachmentBase.class);
        final UserConfigurationStorage userConfigs = UserConfigurationStorage.getInstance();
        final TimerService timers = getService(TimerService.class);

        int detailInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.detail", 120000);
        int stateChangeInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.states", 600000);
        int priorityInterval = config.getIntProperty("com.openexchange.calendar.notify.interval.priority", 900000);
        boolean poolEnabled = false;

        final AttachmentMemory attachmentMemory = new AttachmentMemory(detailInterval * 3, timers);
        MailSenderService sender = new DefaultMailSenderService(emitter, htmlService, attachments, contexts, users, userConfigs, attachmentMemory);

        final AppointmentSqlFactory sqlFactory = new AppointmentSqlFactory();
        final CalendarITipIntegrationUtility util = new CalendarITipIntegrationUtility(sqlFactory, new CalendarCollection(), contexts);
        final DefaultNotificationParticipantResolver resolver = new DefaultNotificationParticipantResolver(groups, users, resources, config, util );
        final NotificationMailGeneratorFactory mails = new NotificationMailGeneratorFactory(resolver, util, this, attachmentMemory);


        if (poolEnabled) {
            AppointmentNotificationPool pool = new AppointmentNotificationPool(timers, mails, sender, detailInterval, stateChangeInterval, priorityInterval);
            sender = new PoolingMailSenderService(pool, sender);
        }

        Dictionary<String, Object> analyzerProps = new Hashtable<String, Object>();
        analyzerProps.put(Constants.SERVICE_RANKING, DefaultITipAnalyzerService.RANKING); // Default
        registerService(ITipAnalyzerService.class, new DefaultITipAnalyzerService(util, this), analyzerProps);
        Dictionary<String, Object> factoryProps = new Hashtable<String, Object>();
        factoryProps.put(Constants.SERVICE_RANKING, DefaultITipDingeMacherFactoryService.RANKING); // Default
        registerService(ITipDingeMacherFactoryService.class, new DefaultITipDingeMacherFactoryService(util, sender, mails), factoryProps);

        registerService(ITipMailGeneratorFactory.class, mails);

        registerService(MailSenderService.class, sender);

        features.add(new NotifyFeature(mails, sender, attachmentMemory, this));
        features.add(new ITipFeature(this));
        setFeatureIfPossible();

        track(MailcapCommandMap.class, new MailcapServiceTracker(context));
        openTrackers();
    }


    public static void initFeatures(final AppointmentSqlFactory f) {
        factory = f;
        setFeatureIfPossible();
    }

    public static void setFeatureIfPossible() {
        if (factory != null && !features.isEmpty()) {
        	for (final CalendarFeature feature : features) {
				factory.addCalendarFeature(feature);
			}
        }
    }

}

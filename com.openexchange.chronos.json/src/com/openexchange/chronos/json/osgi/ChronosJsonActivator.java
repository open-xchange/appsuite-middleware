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

package com.openexchange.chronos.json.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.alarm.json.AlarmActionFactory;
import com.openexchange.chronos.availability.json.mapper.AvailableMapper;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.itip.ITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.json.action.ITipActionFactory;
import com.openexchange.chronos.itip.json.converter.ITipAnalysisResultConverter;
import com.openexchange.chronos.json.action.ChronosActionFactory;
import com.openexchange.chronos.json.action.account.ChronosAccountActionFactory;
import com.openexchange.chronos.json.converter.AlarmTriggerConverter;
import com.openexchange.chronos.json.converter.CalendarResultConverter;
import com.openexchange.chronos.json.converter.CalendarResultsPerEventIdConverter;
import com.openexchange.chronos.json.converter.EventConflictResultConverter;
import com.openexchange.chronos.json.converter.EventResultConverter;
import com.openexchange.chronos.json.converter.EventsPerFolderResultConverter;
import com.openexchange.chronos.json.converter.FreeBusyConverter;
import com.openexchange.chronos.json.converter.handler.EventFieldDataHandler;
import com.openexchange.chronos.json.converter.handler.Json2OXExceptionDataHandler;
import com.openexchange.chronos.json.converter.handler.Json2ObjectDataHandler;
import com.openexchange.chronos.json.converter.handler.Json2XPropertiesDataHandler;
import com.openexchange.chronos.json.converter.handler.OXException2JsonDataHandler;
import com.openexchange.chronos.json.converter.handler.Object2JsonDataHandler;
import com.openexchange.chronos.json.converter.handler.XProperties2JsonDataHandler;
import com.openexchange.chronos.json.converter.mapper.AlarmMapper;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.oauth.ChronosOAuthScope;
import com.openexchange.chronos.json.oauth.OAuthScopeDescription;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.resource.ResourceService;

/**
 * {@link ChronosJsonActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChronosJsonActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            IDBasedCalendarAccessFactory.class, CalendarUtilities.class, CalendarService.class, LeanConfigurationService.class,
            CalendarAccountService.class, ConversionService.class, ITipActionPerformerFactoryService.class,
            ContactService.class, ResourceService.class, GroupService.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(ChronosJsonActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * register json module
             */
            registerModule(new ChronosActionFactory(this), "chronos");
            registerModule(new ChronosAccountActionFactory(this), "chronos/account");
            registerModule(new AlarmActionFactory(this), "chronos/alarm");

            /*
             * ITip stuff
             */
            RankingAwareNearRegistryServiceTracker<ITipAnalyzerService> analyzerTracker = new RankingAwareNearRegistryServiceTracker<>(context, ITipAnalyzerService.class, 0);
            RankingAwareNearRegistryServiceTracker<ITipActionPerformerFactoryService> factoryTracker = new RankingAwareNearRegistryServiceTracker<>(context, ITipActionPerformerFactoryService.class, 0);
            rememberTracker(analyzerTracker);
            rememberTracker(factoryTracker);
            openTrackers();
            registerModule(new ITipActionFactory(this, analyzerTracker, factoryTracker), "chronos/itip");

            // Availability disabled until further notice
            //registerModule(new AvailabilityActionFactory(this), "chronos/availability");
            /*
             * register oauth provider
             */
            registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ChronosOAuthScope.OAUTH_READ_SCOPE, OAuthScopeDescription.READ_ONLY) {

                @Override
                public boolean canBeGranted(CapabilitySet capabilities) {
                    return capabilities.contains(Permission.CALENDAR.getCapabilityName());
                }
            });
            registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ChronosOAuthScope.OAUTH_WRITE_SCOPE, OAuthScopeDescription.WRITABLE) {

                @Override
                public boolean canBeGranted(CapabilitySet capabilities) {
                    return capabilities.contains(Permission.CALENDAR.getCapabilityName());
                }
            });
            /*
             * register result converters
             */
            registerService(ResultConverter.class, new FreeBusyConverter());
            registerService(ResultConverter.class, new EventResultConverter(this));
            registerService(ResultConverter.class, new EventsPerFolderResultConverter(this));
            registerService(ResultConverter.class, new EventConflictResultConverter());
            registerService(ResultConverter.class, new CalendarResultConverter(this));
            registerService(ResultConverter.class, new CalendarResultsPerEventIdConverter(this));
            registerService(ResultConverter.class, new AlarmTriggerConverter());
            registerService(ResultConverter.class, new ITipAnalysisResultConverter());
            /*
             * register data handlers
             */
            registerService(DataHandler.class, new Json2ObjectDataHandler<>(
                EventMapper.getInstance()), singletonDictionary("identifier", DataHandlers.JSON2EVENT));
            registerService(DataHandler.class, new Json2ObjectDataHandler<>(
                AlarmMapper.getInstance()), singletonDictionary("identifier", DataHandlers.JSON2ALARM));
            registerService(DataHandler.class, new Object2JsonDataHandler<>(
                AlarmMapper.getInstance(), Alarm.class, Alarm[].class), singletonDictionary("identifier", DataHandlers.ALARM2JSON));
            registerService(DataHandler.class, new Json2ObjectDataHandler<>(
                AvailableMapper.getInstance()), singletonDictionary("identifier", DataHandlers.JSON2AVAILABLE));
            registerService(DataHandler.class, new Object2JsonDataHandler<>(
                AvailableMapper.getInstance(), Available.class, Available[].class), singletonDictionary("identifier", DataHandlers.AVAILABLE2JSON));
            registerService(DataHandler.class, new XProperties2JsonDataHandler(), singletonDictionary("identifier", DataHandlers.XPROPERTIES2JSON));
            registerService(DataHandler.class, new Json2XPropertiesDataHandler(), singletonDictionary("identifier", DataHandlers.JSON2XPROPERTIES));
            registerService(DataHandler.class, new OXException2JsonDataHandler(), singletonDictionary("identifier", DataHandlers.OXEXCEPTION2JSON));
            registerService(DataHandler.class, new Json2OXExceptionDataHandler(), singletonDictionary("identifier", DataHandlers.JSON2OXEXCEPTION));
            registerService(DataHandler.class, new EventFieldDataHandler(), singletonDictionary("identifier", DataHandlers.STRING_ARRAY_TO_EVENT_FIELDS));

        } catch (Exception e) {
            getLogger(ChronosJsonActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(ChronosJsonActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}

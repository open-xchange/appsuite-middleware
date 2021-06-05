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

package com.openexchange.calendar.json.osgi;

import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.calendar.json.converters.AppointmentResultConverter;
import com.openexchange.calendar.json.converters.EventResultConverter;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.itip.ITipActionPerformerFactoryService;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.json.ITipActionFactory;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.user.UserService;

/**
 * {@link AppointmentJSONActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AppointmentJSONActivator extends AJAXModuleActivator {

    private static final Class<?>[] NEEDED = new Class[] {
        CalendarService.class, UserService.class, RecurrenceService.class, ConversionService.class, ITipActionPerformerFactoryService.class, ITipAnalyzerService.class
    };

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        //        final Dictionary<String, Integer> props = new Hashtable<String, Integer>(1, 1);
        //        props.put(TargetService.MODULE_PROPERTY, I(Types.APPOINTMENT));
        //        registerService(TargetService.class, new ModifyThroughDependant(), props);
        registerModule(new AppointmentActionFactory(this), "calendar");
        registerService(ResultConverter.class, new AppointmentResultConverter(this));
        registerService(ResultConverter.class, new EventResultConverter(this));



        RankingAwareNearRegistryServiceTracker<ITipAnalyzerService> rankingTracker = new RankingAwareNearRegistryServiceTracker<>(context, ITipAnalyzerService.class, 0);
        RankingAwareNearRegistryServiceTracker<ITipActionPerformerFactoryService> factoryTracker = new RankingAwareNearRegistryServiceTracker<>(context, ITipActionPerformerFactoryService.class, 0);
        ServiceTracker<CapabilityService, CapabilityService> capabilityTracker = track(CapabilityService.class);
        rememberTracker(rankingTracker);
        rememberTracker(factoryTracker);
        rememberTracker(capabilityTracker);
        openTrackers();

        ITipActionFactory.INSTANCE = new ITipActionFactory(this, rankingTracker, factoryTracker);
        registerModule(ITipActionFactory.INSTANCE, "calendar/itip");


        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(AppointmentActionFactory.OAUTH_READ_SCOPE, OAuthScopeDescription.READ_ONLY) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.CALENDAR.getCapabilityName());
            }
        });
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(AppointmentActionFactory.OAUTH_WRITE_SCOPE, OAuthScopeDescription.WRITABLE) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.CALENDAR.getCapabilityName());
            }
        });

        trackService(ObjectUseCountService.class);
        openTrackers();
    }

}

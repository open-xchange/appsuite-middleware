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

package com.openexchange.chronos.provider.composition.impl;

import static com.openexchange.chronos.provider.CalendarProviders.getCapabilityName;
import static com.openexchange.chronos.provider.CalendarProviders.getEnabledPropertyName;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CalendarProviderTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarProviderTracker extends RankingAwareNearRegistryServiceTracker<CalendarProvider> {

    private final ConcurrentMap<String, ServiceRegistration<CapabilityChecker>> checkerRegistrations;
    final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarProviderTracker}.
     *
     * @param calendarProviders The bundle context
     * @param services A service lookup reference
     */
    public CalendarProviderTracker(BundleContext context, ServiceLookup services) {
        super(context, CalendarProvider.class);
        this.checkerRegistrations = new ConcurrentHashMap<String, ServiceRegistration<CapabilityChecker>>();
        this.services = services;
    }

    @Override
    protected void onServiceAdded(CalendarProvider provider) {
        /*
         * declare capability for calendar provider
         */
        String capabilityName = getCapabilityName(provider);
        services.getService(CapabilityService.class).declareCapability(capabilityName);
        /*
         * register an appropriate capability checker
         */
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(CapabilityChecker.PROPERTY_CAPABILITIES, capabilityName);
        ServiceRegistration<CapabilityChecker> checkerRegistration = context.registerService(CapabilityChecker.class, new CapabilityChecker() {

            @Override
            public boolean isEnabled(String capability, Session session) throws OXException {
                ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                if (serverSession.isAnonymous()) {
                    return false;
                }
                if (false == serverSession.getUserPermissionBits().hasCalendar()) {
                    return false;
                }
                ConfigView configView = requireService(ConfigViewFactory.class, services).getView(session.getUserId(), session.getContextId());
                return ConfigViews.getDefinedBoolPropertyFrom(getEnabledPropertyName(provider), provider.getDefaultEnabled(), configView) && provider.isAvailable(session);
            }
        }, serviceProperties);
        if (null != checkerRegistrations.putIfAbsent(provider.getId(), checkerRegistration)) {
            checkerRegistration.unregister();
        }
    }

    @Override
    protected void onServiceRemoved(CalendarProvider provider) {
        /*
         * unregister capability checker for calendar provider
         */
        ServiceRegistration<CapabilityChecker> checkerRegistration = checkerRegistrations.remove(provider.getId());
        if (null != checkerRegistration) {
            checkerRegistration.unregister();
        }
        /*
         * undeclare capability for calendar provider
         */
        services.getService(CapabilityService.class).undeclareCapability(getCapabilityName(provider));
    }

}

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

package com.openexchange.contact.provider.composition.impl;

import static com.openexchange.contact.provider.ContactsProviders.getCapabilityName;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.google.common.collect.ImmutableMap;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.provider.ContactProviderProperty;
import com.openexchange.contact.provider.ContactsProvider;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ContactsProviderTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsProviderTracker extends RankingAwareNearRegistryServiceTracker<ContactsProvider> {

    private final ConcurrentMap<String, ServiceRegistration<CapabilityChecker>> checkerRegistrations;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactsProviderTracker}.
     *
     * @param context The bundle context
     * @param services The service lookup reference
     */
    public ContactsProviderTracker(BundleContext context, ServiceLookup services) {
        super(context, ContactsProvider.class);
        this.services = services;
        this.checkerRegistrations = new ConcurrentHashMap<>();
    }

    @Override
    protected void onServiceAdded(ContactsProvider provider) {
        // Declare capability for contact provider
        String capabilityName = getCapabilityName(provider);
        services.getService(CapabilityService.class).declareCapability(capabilityName);

        // Register an appropriate capability checker
        Dictionary<String, Object> serviceProperties = new Hashtable<>(1);
        serviceProperties.put(CapabilityChecker.PROPERTY_CAPABILITIES, capabilityName);
        ServiceRegistration<CapabilityChecker> checkerRegistration = context.registerService(CapabilityChecker.class, (capability, session) -> {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            if (serverSession.isAnonymous()) {
                return false;
            }
            if (false == serverSession.getUserPermissionBits().hasContact()) {
                return false;
            }
            return isProviderEnabled(provider) && provider.isAvailable(session);
        }, serviceProperties);

        if (null != checkerRegistrations.putIfAbsent(provider.getId(), checkerRegistration)) {
            checkerRegistration.unregister();
        }
    }

    @Override
    protected void onServiceRemoved(ContactsProvider provider) {
        // Unregister capability checker for contact provider
        ServiceRegistration<CapabilityChecker> checkerRegistration = checkerRegistrations.remove(provider.getId());
        if (null != checkerRegistration) {
            checkerRegistration.unregister();
        }
        // Undeclare capability for contact provider
        services.getService(CapabilityService.class).undeclareCapability(getCapabilityName(provider));
    }

    /**
     * Checks whether the specified {@link ContactsProvider} is enabled
     *
     * @param provider The provider to check
     * @return <code>true</code> if the provider is enabled; <code>false</code> otherwise
     * @throws OXException if the {@link LeanConfigurationService} is absent or any other error is occurred
     */
    private boolean isProviderEnabled(ContactsProvider provider) throws OXException {
        LeanConfigurationService service = requireService(LeanConfigurationService.class, services);
        return service.getBooleanProperty(ContactProviderProperty.enabled, ImmutableMap.of(ContactProviderProperty.OPTIONAL_NAME, provider.getId()));
    }
}

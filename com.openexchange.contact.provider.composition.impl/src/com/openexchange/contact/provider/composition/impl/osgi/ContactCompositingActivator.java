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

package com.openexchange.contact.provider.composition.impl.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.provider.ContactsAccountService;
import com.openexchange.contact.provider.ContactsProviderRegistry;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.contact.provider.composition.impl.CompositingIDBasedContactsAccessFactory;
import com.openexchange.contact.provider.composition.impl.ContactsProviderRegistryImpl;
import com.openexchange.contact.provider.composition.impl.ContactsProviderTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ContactCompositingActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactCompositingActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(ContactCompositingActivator.class);

    /**
     * Initializes a new {@link ContactCompositingActivator}.
     */
    public ContactCompositingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContactsAccountService.class, LeanConfigurationService.class, CapabilityService.class, ConfigViewFactory.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle());

        ContactsProviderTracker providerTracker = new ContactsProviderTracker(context, this);
        rememberTracker(providerTracker);
        openTrackers();

        ContactsProviderRegistry providerRegistry = new ContactsProviderRegistryImpl(providerTracker);
        registerService(ContactsProviderRegistry.class, providerRegistry);

        CompositingIDBasedContactsAccessFactory accessFactory = new CompositingIDBasedContactsAccessFactory(providerRegistry, this);
        registerService(IDBasedContactsAccessFactory.class, accessFactory);
        ServerServiceRegistry.getInstance().addService(IDBasedContactsAccessFactory.class, accessFactory);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle());
        super.stopBundle();
    }
}

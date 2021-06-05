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

package com.openexchange.contact.provider.internal.osgi;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.provider.ContactsAccountService;
import com.openexchange.contact.provider.ContactsProvider;
import com.openexchange.contact.provider.internal.InternalContactsProvider;
import com.openexchange.contact.storage.ContactsStorageFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;

/**
 * {@link InternalContactsProviderActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class InternalContactsProviderActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(InternalContactsProviderActivator.class);

    /**
     * Initializes a new {@link InternalContactsProviderActivator}.
     */
    public InternalContactsProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContactService.class, ContactsStorageFactory.class, ContactsAccountService.class, FolderUserPropertyStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("Starting bundle {}", context.getBundle());
            registerService(ContactsProvider.class, new InternalContactsProvider(this));
        } catch (Exception e) {
            LOG.error("Error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle());
        super.stop(context);
    }
}

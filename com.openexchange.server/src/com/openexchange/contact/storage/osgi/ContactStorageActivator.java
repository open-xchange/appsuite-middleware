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

package com.openexchange.contact.storage.osgi;

import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.internal.DefaultContactStorageRegistry;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ContactStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactStorageActivator extends HousekeepingActivator {

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactStorageActivator.class);

    /**
     * Initializes a new {@link ContactStorageActivator}.
     */
    public ContactStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * prepare startup
             */
            LOG.info("starting bundle: com.openexchange.contact.storage");
            final DefaultContactStorageRegistry registry = new DefaultContactStorageRegistry();
            /*
             * start tracking
             */
            super.track(ContactStorage.class, new ContactStorageListener(registry));
            super.openTrackers();
            /*
             * register services
             */
            super.registerService(ContactStorageRegistry.class, registry);
        } catch (Exception e) {
            LOG.error("error starting \"com.openexchange.contact.storage\"", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.contact.storage");
        super.stopBundle();
    }

}

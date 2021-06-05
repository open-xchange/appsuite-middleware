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

import org.osgi.framework.ServiceReference;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.internal.DefaultContactStorageRegistry;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link ContactStorageListener} - Recognizes {@link ContactStorage} services.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactStorageListener implements SimpleRegistryListener<ContactStorage> {

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactStorageListener.class);

    private final DefaultContactStorageRegistry registry;

    /**
     * Initializes a new {@link ContactStorageListener}.
     *
     * @param registry the registry to use
     */
    public ContactStorageListener(final DefaultContactStorageRegistry registry) {
        super();
        this.registry = registry;
        LOG.debug("initialized.");
    }

    @Override
    public void added(final ServiceReference<ContactStorage> ref, final ContactStorage service) {
        LOG.info("adding contact storage: {}", service);
        this.registry.addStorage(service);
    }

    @Override
    public void removed(final ServiceReference<ContactStorage> ref, final ContactStorage service) {
        LOG.info("removing contact storage: {}", service);
        this.registry.removeStorage(service);
    }

}

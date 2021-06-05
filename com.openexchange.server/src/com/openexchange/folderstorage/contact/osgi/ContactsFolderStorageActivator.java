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

package com.openexchange.folderstorage.contact.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.contact.ContactsFolderStorage;
import com.openexchange.folderstorage.contact.field.ContactsAccountErrorField;
import com.openexchange.folderstorage.contact.field.ContactsConfigField;
import com.openexchange.folderstorage.contact.field.ContactsProviderField;
import com.openexchange.folderstorage.contact.field.ExtendedPropertiesField;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link ContactsFolderStorageActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsFolderStorageActivator extends HousekeepingActivator {

    private ServiceTracker<?, ?> dependentTracker;

    /**
     * Initializes a new {@link ContactsFolderStorageActivator}.
     */
    public ContactsFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(ContactsFolderStorageActivator.class).info("Starting bundle {}", context.getBundle());
            reinit();
            openTrackers();
        } catch (Exception e) {
            getLogger(ContactsFolderStorageActivator.class).error("Error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    private synchronized void reinit() throws OXException {
        ServiceTracker<?, ?> tracker = this.dependentTracker;
        if (null != tracker) {
            this.dependentTracker = null;
            tracker.close();
            tracker = null;
        }
        Dictionary<String, String> serviceProperties = new Hashtable<>(1);
        serviceProperties.put("tree", FolderStorage.REAL_TREE_ID);
        DependentServiceRegisterer<FolderStorage> registerer = new DependentServiceRegisterer<>(context, FolderStorage.class, ContactsFolderStorage.class, serviceProperties, IDBasedContactsAccessFactory.class);
        try {
            tracker = new ServiceTracker<>(context, registerer.getFilter(), registerer);
        } catch (InvalidSyntaxException e) {
            throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create(e);
        }
        this.dependentTracker = tracker;
        tracker.open();

        // Register custom fields
        registerService(FolderField.class, ExtendedPropertiesField.getInstance());
        registerService(FolderField.class, ContactsProviderField.getInstance());
        registerService(FolderField.class, ContactsConfigField.getInstance());
        registerService(FolderField.class, ContactsAccountErrorField.getInstance());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        getLogger(ContactsFolderStorageActivator.class).info("Stopping bundle {}", context.getBundle());
        ServiceTracker<?, ?> tracker = this.dependentTracker;
        if (null != tracker) {
            this.dependentTracker = null;
            tracker.close();
            tracker = null;
        }
        super.stopBundle();
    }

}

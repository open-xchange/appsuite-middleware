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

package com.openexchange.contact.vcard.storage.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.contact.vcard.storage.impl.DefaultVCardStorageFactory;
import com.openexchange.contact.vcard.storage.impl.DefaultVCardStorageMetadataStore;
import com.openexchange.contact.vcard.storage.impl.DefaultVCardStorageService;
import com.openexchange.contact.vcard.storage.impl.VCardCleaner;
import com.openexchange.contact.vcard.storage.impl.VCardFilestoreLocationUpdater;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.osgi.HousekeepingActivator;

/**
 *
 * {@link ContactVCardStorageActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ContactVCardStorageActivator extends HousekeepingActivator {

    private static final String COM_OPENEXCHANGE_CONTACT_STORE_V_CARDS = "com.openexchange.contact.storeVCards";

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactVCardStorageActivator.class);

    /**
     * Initializes a new {@link ContactVCardStorageActivator}.
     */
    public ContactVCardStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, DatabaseService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: com.openexchange.contact.vcard.storage.impl");

            registerService(FileLocationHandler.class, new VCardFilestoreLocationUpdater());
            VCardStorageFactory vCardStorageFactory = new DefaultVCardStorageFactory(new DefaultVCardStorageService());
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] { VCardCleaner.EVENT_TOPIC });
            registerService(EventHandler.class, new VCardCleaner(getService(ConfigViewFactory.class), vCardStorageFactory), serviceProperties);
            registerService(VCardStorageMetadataStore.class, new DefaultVCardStorageMetadataStore(getService(DatabaseService.class)));

            boolean enabled = getService(ConfigurationService.class).getBoolProperty(COM_OPENEXCHANGE_CONTACT_STORE_V_CARDS, true);
            if (enabled) {
                registerService(VCardStorageFactory.class, vCardStorageFactory);
            }
        } catch (Exception exception) {
            LOG.error("error starting com.openexchange.contact.vcard.storage.impl", exception);
            throw exception;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.contact.vcard.storage.impl");

        super.stopBundle();
    }
}

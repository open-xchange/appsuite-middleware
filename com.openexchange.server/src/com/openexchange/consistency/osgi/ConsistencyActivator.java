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

package com.openexchange.consistency.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.Logger;
import com.openexchange.consistency.ConsistencyService;
import com.openexchange.consistency.internal.ConsistencyServiceImpl;
import com.openexchange.consistency.rmi.ConsistencyRMIServiceImpl;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.database.DatabaseService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.ObfuscatorService;

/**
 * {@link ConsistencyActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ConsistencyActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ConsistencyActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.consistency");
        ConsistencyServiceLookup.set(this);
        registerService(ConsistencyService.class, new ConsistencyServiceImpl(this));
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", ConsistencyRMIServiceImpl.RMI_NAME);
        registerService(Remote.class, new ConsistencyRMIServiceImpl(this), serviceProperties);

        trackService(VCardStorageMetadataStore.class);
        trackService(DatabaseService.class);
        trackService(ConsistencyService.class);
        trackService(ObfuscatorService.class);

        openTrackers();
    }

    @Override
    public void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.consistency");
        ConsistencyServiceLookup.set(null);

        closeTrackers();
        super.stopBundle();
    }
}

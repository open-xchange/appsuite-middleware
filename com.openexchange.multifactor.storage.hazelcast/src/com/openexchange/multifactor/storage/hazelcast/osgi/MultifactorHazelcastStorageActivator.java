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

package com.openexchange.multifactor.storage.hazelcast.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.multifactor.storage.hazelcast.impl.PortableTokenFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MultifactorHazelcastStorageActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorHazelcastStorageActivator  extends HousekeepingActivator{

    private static final Logger LOG = LoggerFactory.getLogger(MultifactorHazelcastStorageActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());
        registerService(CustomPortableFactory.class, new PortableTokenFactory());

    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }

}

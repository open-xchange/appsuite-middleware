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

package com.openexchange.fragment.properties.loader.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.fragment.properties.loader.FragmentPropertiesLoader;
import com.openexchange.fragment.properties.loader.internal.PropertiesLoader;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * 
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class Activator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle \"com.openexchange.fragments.properties.loader\".");
        registerService(FragmentPropertiesLoader.class, new PropertiesLoader());
    }
    
    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle \"com.openexchange.fragments.properties.loader\".");
        super.stopBundle();
    }
}

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

package com.openexchange.jump.osgi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyFilter;
import com.openexchange.jump.Endpoint;
import com.openexchange.jump.JumpService;
import com.openexchange.jump.internal.EndpointImpl;
import com.openexchange.jump.internal.JumpServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link JumpActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JumpActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link JumpActivator}.
     */
    public JumpActivator() {
        super();
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(JumpActivator.class);
        logger.info("Starting bundle \"com.openexchange.jump\"");
        try {
            // Collect configured end-points
            final ConfigurationService configService = getService(ConfigurationService.class);
            final String prefix = "com.openexchange.jump.endpoint.";
            final Map<String, String> cEndpoints = configService.getProperties(new PropertyFilter() {

                @Override
                public boolean accept(String name, String value) {
                    return name.startsWith(prefix);
                }
            });

            // Only keep their system name as key
            final Map<String, Endpoint> endpoints = new LinkedHashMap<String, Endpoint>(cEndpoints.size());
            final int prefixLen = prefix.length();
            for (Entry<String, String> entry : cEndpoints.entrySet()) {
                final String name = entry.getKey();
                final EndpointImpl endpoint = new EndpointImpl(name.length() > prefixLen ? name.substring(prefixLen) : name, entry.getValue());
                endpoints.put(endpoint.getSystemName(), endpoint);
            }

            // Create service instance
            final JumpServiceImpl jumpServiceImpl = new JumpServiceImpl(endpoints, context);
            // As tracker for end-points
            rememberTracker(jumpServiceImpl);
            openTrackers();
            // As service
            registerService(JumpService.class, jumpServiceImpl, null);

            logger.info("Bundle \"com.openexchange.jump\" successfully started");
        } catch (Exception e) {
            logger.error("Failed starting bundle \"com.openexchange.jump\"");
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

}

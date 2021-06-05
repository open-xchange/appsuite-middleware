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


package com.openexchange.hazelcast.configuration;

import com.hazelcast.config.Config;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link HazelcastConfigurationService} - Provides the Hazelcast configuration.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SingletonService
public interface HazelcastConfigurationService  {

    /**
     * Gets a value indicating whether Hazelcast services are enabled or not.
     *
     * @return <code>true</code> if Hazelcast is enabled, <code>false</code>, otherwise
     */
    boolean isEnabled() throws OXException;

    /**
     * Gets the Hazelcast configuration.
     *
     * @return The configuration
     */
    Config getConfig() throws OXException;

    /**
     * Gets the actual name of a distributed map based on the supplied prefix. <p/>
     *
     * To support some kind of versioning, map names may be defined with an index suffix in configuration files, such as
     * <code>mymap-5</code>. This method browses the known map configurations for the full name of the map as registered in hazelcast
     * based on the supplied prefix, which would be <code>mymap-</code> in the above example.
     *
     * @param namePrefix The name prefix, e.g. <code>mymap-</code>
     * @return The full name of the map, e.g. <code>mymap-5</code>
     * @throws OXException If no matching map configuration was found
     */
    String discoverMapName(String namePrefix) throws OXException;

    /**
     * Signals whether Hazelcast is supposed to be shut-down in case an Out-Of-Memory error occurred
     *
     * @return <code>true</code> to shut-down; otherwise <code>false</code>
     */
    boolean shutdownOnOutOfMemory();

}

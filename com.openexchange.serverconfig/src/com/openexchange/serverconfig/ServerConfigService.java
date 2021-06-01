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

package com.openexchange.serverconfig;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ServerConfigService}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
@SingletonService
public interface ServerConfigService {

    /**
     * Gets the computed server config
     *
     * @param hostname The hostname to use when building the {@link ServerConfig}
     * @param userID The userID when building the {@link ServerConfig}
     * @param contextID The contextID when building the {@link ServerConfig}
     * @return the computed server config
     * @throws OXException if computing the server config fails
     */
    ServerConfig getServerConfig(String hostname, int userID, int contextID) throws OXException;

    /**
     * Gets the computed server config
     *
     * @param hostname The hostname to use when building the {@link ServerConfig}
     * @param session The session to use when building the {@link ServerConfig}
     * @return the computed server config
     * @throws OXException if computing the server config fails
     */
    ServerConfig getServerConfig(String hostname, Session session) throws OXException;

    /**
     * Gets the {@link ServerConfigServicesLookup} that is used when computing the server config.
     *
     * @return The look-up
     */
    ServerConfigServicesLookup getServerConfigServicesLookup();

    /**
     * Gets all custom properties for a given host from as-config.yml.
     *
     * @param hostName, the name of the host, to get the properties for
     * @param userID, can be -1 if no id is available at present
     * @param contextID, can be -1 if no id is available at present
     * @param configViewFactory, the factory that should be used to get all properties available
     * @return the whole map with all custom properties for a host
     * @throws OXException
     */
    List<Map<String, Object>> getCustomHostConfigurations(String hostName, int userID, int contextID) throws OXException;

}

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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.capabilities.Capability;


/**
 * {@link ServerConfig} - A simple ServerConfig Object to ease extraction of well known/most used items of the dynamically generated
 * ServerConfig Map. You can inspect all items via {@link ServerConfig#asMap()}.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public interface ServerConfig {

    /**
     * Get the whole config object as a read-only map, might contain more entries than available via documented methods as the object is built
     * dynamically.
     *
     * @return The whole config object as map
     */
    Map<String, Object> asMap();

    /**
     * Get the set of {@link Capability}s available for the {@link User} you requested the {@link ServerConfig} for
     * @return the set of {@link Capability}s
     */
    Set<Capability> getCapabilities();

    /**
     * Check if the server is forced to assume HTTPS connections
     *
     * @return <code>true</code> if the server is forced to assume HTTPS connections, otherwise <code>false</code>
     */
    boolean isForceHttps();

    /**
     * Get the hosts configuration
     * @return An array of hosts
     */
    String[] getHosts();

    /**
     * Get a list of known languages as key -> value pairs e.g. "en_GB" -> "English (UK)"
     *
     * @return The list of languages
     */
    List<SimpleEntry<String, String>> getLanguages();

    /**
     * Get the server version
     *
     * @return the server version
     */
    String getServerVersion();

    /**
     * Get the server build date
     *
     * @return the server build date
     */
    String getServerBuildDate();

    /**
     * Get the ui version
     *
     * @return the ui version
     */
    String getUIVersion();

    /**
     * Get the product name
     *
     * @return the product name
     */
    String getProductName();

    /**
     * Get a filtered config object as map. Not all ServerConfig entries are needed for the client. {@link ClientServerConfigFilter}s can
     * be registered to influence the filtering process.
     *
     * @return The filtered config object as map
     */
    Map<String, Object> forClient();

    /**
     * Get the style configuration for notification mails
     *
     * @return The configuration
     */
    NotificationMailConfig getNotificationMailConfig();
}

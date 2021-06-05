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

package com.openexchange.advertisement;

import java.util.List;
import java.util.Map;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AdvertisementConfigService} - The service to manage advertisement configurations for users and/or resellers.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public interface AdvertisementConfigService {

    static final String PACKAGE_ALL = "default";
    static final String RESELLER_ALL = "default";

    public enum ConfigResultType {
        CREATED, UPDATED, DELETED, IGNORED, ERROR
    }

    public static final String CONFIG_PREFIX = "com.openexchange.advertisement.";

    /**
     * Checks if an advertisement configuration is available for the given user
     *
     * @param session The user session
     * @return <code>true</code> if such an advertisement configuration is available; otherwise <code>false</code>
     */
    public boolean isAvailable(Session session);

    /**
     * Retrieves the advertisement configuration for a given user
     *
     * @param session The user session
     * @return The JSON representation for the configuration
     * @throws OXException If configuration cannot be returned
     */
    public JSONValue getConfig(Session session) throws OXException;

    /**
     * Sets an advertisement configuration for a given user by name. This is for testing purpose only.
     * <p>
     * Setting the configuration parameter to null will delete the current configuration for the user.
     *
     * @param name The login name of the user
     * @param ctxId The context identifier
     * @param config The advertisement configuration
     * @return The {@link ConfigResult}
     */
    public ConfigResult setConfigByName(String name, int ctxId, String config);

    /**
     * Sets an advertisement configuration for a given user. This is for testing purpose only.
     * <p>
     * Setting the configuration parameter to <code>null</code> will delete the current configuration for the user.
     *
     * @param userId The user identifier
     * @param ctxId The context identifier
     * @param config The advertisement configuration
     * @return The {@link ConfigResult}
     */
    public ConfigResult setConfig(int userId, int ctxId, String config);

    /**
     * Sets an advertisement configuration for a given package of a given reseller.
     * <p>
     * Setting the configuration parameter to <code>null</code> will delete the current configuration.
     *
     * @param reseller The reseller name
     * @param pack The package name
     * @param config The advertisement configuration
     * @return The {@link ConfigResult}
     */
    public ConfigResult setConfig(String reseller, String pack, String config);

    /**
     * Sets all advertisement configurations for a given reseller.
     * <p>
     * The configs String must contain a JSONArray of JSONObjects with the following structure:
     * <p>
     * {<br>
     * "package": "package1",<br>
     * "config": "configdata..."<br>
     * }<br>
     * <p>
     *
     * Setting the configuration parameter to <code>null</code> will delete the current configuration for the reseller.
     *
     * @param reseller The reseller name
     * @param configs An array of package informations and advertisement configurations
     * @return A list of {@link ConfigResult}
     * @throws OXException in case configs couldn't be parsed
     */
    public List<ConfigResult> setConfig(String reseller, Map<String, String> configs) throws OXException;

    /**
     * Retrieves the package scheme identifier for this configuration service.
     *
     * @return The package scheme identifier
     */
    public String getSchemeId();

}

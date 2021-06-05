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

package com.openexchange.groupware.upgrade.impl;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.Nullable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.configuration.ServerProperty;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upgrade.SegmentedUpdateService;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.share.ShareProperty;

/**
 * {@link SegmentedUpdateServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class SegmentedUpdateServiceImpl implements SegmentedUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(SegmentedUpdateServiceImpl.class);

    /**
     * Initialises a new {@link SegmentedUpdateServiceImpl}.
     */
    public SegmentedUpdateServiceImpl() {
        super();
    }

    @Override
    public @Nullable String getMigrationRedirectURL(@Nullable String host) throws OXException {
        return getProperty(host, ServerProperty.migrationRedirectURL);
    }

    @Override
    public String getSharingMigrationRedirectURL(@Nullable String host) throws OXException {
        String url = getProperty(host, ShareProperty.migrationRedirectURL);
        if (Strings.isEmpty(url)) {
          url = getProperty(host, ServerProperty.migrationRedirectURL);
        }
        return url;
    }

    /**
     * Retrieves the requested {@link Property} by first checking in the 'as-config.yaml'
     * and then trying the lean configuration
     *
     * @param host the host
     * @param property The property to fetch
     * @return The value of the property
     * @throws OXException if the property cannot be retrieved or any other error is occurred
     */
    private String getProperty(String host, Property property) throws OXException {
        String migrationRedirectURL = getFor(host, property);
        if (Strings.isEmpty(migrationRedirectURL)) {
            migrationRedirectURL = getFromLean(property);
        }
        if (Strings.isEmpty(migrationRedirectURL)) {
            LOG.warn("The property '{}' is not set.", property);
        }
        return migrationRedirectURL;
    }

    /**
     * Gets the migration URL from the yaml configuration for the specified host
     *
     * @param host The host for which to retrieve the migration URL
     * @return The migrationURL if present, <code>null</code> if not configured or if the <code>host</code> is <code>null</code>.
     * @throws OXException if an error is occurred
     */
    private @Nullable String getFor(@Nullable String host, Property property) throws OXException {
        ServerConfigService serverConfigService = ServerServiceRegistry.getInstance().getService(ServerConfigService.class);
        if (serverConfigService == null || Strings.isEmpty(host)) {
            return null;
        }
        List<Map<String, Object>> customHostConfigurations = serverConfigService.getCustomHostConfigurations(host, -1, -1);
        for (Map<String, Object> map : customHostConfigurations) {
            Object object = map.get(property.getFQPropertyName());
            if (object == null || false == (object instanceof String)) {
                continue;
            }
            String migrationURL = (String) object;
            LOG.debug("Found the following migrationRedirectURL config for host {} in as-config.yml: {}", host, migrationURL);
            return migrationURL;
        }
        return null;
    }

    /**
     * Gets the migration URL from the lean configuration
     *
     * @return The migration URL or <code>null</code> if not configured.
     */
    private String getFromLean(Property property) {
        LeanConfigurationService leanConfigService = ServerServiceRegistry.getInstance().getService(LeanConfigurationService.class);
        if (leanConfigService == null) {
            return null;
        }
        String migrationRedirectURL = leanConfigService.getProperty(property);
        LOG.debug("Use the following migrationRedirectURL taken from server configuration: {}", migrationRedirectURL);
        return migrationRedirectURL;
    }
}

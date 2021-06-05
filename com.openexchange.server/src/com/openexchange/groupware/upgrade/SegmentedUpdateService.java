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

package com.openexchange.groupware.upgrade;

import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;

/**
 * {@link SegmentedUpdateService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface SegmentedUpdateService {

    /**
     * Returns the configured migrationRedirectURL by consulting the configuration for defined hosts (as-config.yml)
     * and falling back to server configuration (server.properties) if no URL was defined for the host.
     * 
     * @param host The host for which to get the migrationRedirectURL (<code>null</code> if no host is configured)
     * @return The redirect URL for mentioned host (if configured) or <code>null</code> if no configuration can be found.
     * @throws OXException if an error is occurred
     */
    @Nullable
    String getMigrationRedirectURL(@Nullable String host) throws OXException;

    /**
     * Returns the configured migrationRedirectURL for sharing by consulting the configuration for defined hosts (as-config.yml)
     * and falling back to server configuration (server.properties) if no URL was defined for the host.
     * 
     * @param host The host for which to get the migrationRedirectURL (<code>null</code> if no host is configured)
     * @return The redirect URL for mentioned host (if configured) or <code>null</code> if no configuration can be found.
     * @throws OXException if an error is occurred
     */
    @Nullable
    String getSharingMigrationRedirectURL(@Nullable String host) throws OXException;
}

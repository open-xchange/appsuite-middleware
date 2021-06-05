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

package com.openexchange.database.internal;

import com.openexchange.database.ConnectionType;

/**
 * {@link ConnectionTypeAwareConfigurationWrapper} is a wrapper for {@link Configuration}s which provides the {@link ConnectionType}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class ConnectionTypeAwareConfigurationWrapper implements ConnectionTypeAware {

    private ConnectionType type;
    private Configuration config;
    
    /**
     * Initializes a new {@link ConnectionTypeAwareConfigurationWrapper}.
     * 
     * @param type The {@link ConnectionType}
     * @param config The {@link Configuration}
     */
    public ConnectionTypeAwareConfigurationWrapper(ConnectionType type, Configuration config) {
        super();
        this.type = type;
        this.config = config;
    }

    /**
     * Gets the {@link Configuration}
     *
     * @return The {@link Configuration}
     */
    public Configuration getConfig() {
        return config;
    }

    @Override
    public ConnectionType getConnectionType() {
        return type;
    }

}

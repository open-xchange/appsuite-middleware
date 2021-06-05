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

import java.util.Properties;
import com.openexchange.database.ConnectionType;

/**
 * Data to create connections to some specific database.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConnectionData implements ConnectionTypeAware {

    /**
     * Creates a new builder for an instance of <code>ConnectionData</code>
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for an instance of <code>ConnectionData</code> */
    public static class Builder {

        private String url;
        private String driverClass;
        private Properties props;
        private boolean block;
        private int max;
        private int min;
        private ConnectionType type = ConnectionType.WRITABLE;

        /**
         * Initializes a new {@link ConnectionData.Builder}.
         */
        Builder() {
            super();
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withDriverClass(String driverClass) {
            this.driverClass = driverClass;
            return this;
        }

        public Builder withProps(Properties props) {
            this.props = props;
            return this;
        }

        public Builder withBlock(boolean block) {
            this.block = block;
            return this;
        }

        public Builder withMax(int max) {
            this.max = max;
            return this;
        }

        public Builder withMin(int min) {
            this.min = min;
            return this;
        }
        
        public Builder withType(ConnectionType type) {
            this.type = type;
            return this;
        }

        /**
         * Creates the <code>ConnectionData</code> instance from this builder's arguments.
         *
         * @return The <code>ConnectionData</code> instance
         */
        public ConnectionData build() {
            return new ConnectionData(url, driverClass, props, block, max, min, type);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * The ASCII-only URL to database.
     */
    final String url;

    /**
     * The driver class name.
     */
    final String driverClass;

    /**
     * The properties.
     */
    final Properties props;

    /**
     * The block flag.
     */
    final boolean block;

    /**
     * The max. limit.
     */
    final int max;

    /**
     * The min. limit
     */
    final int min;
    
    /**
     * The connection type
     */
    final ConnectionType type;

    /**
     * Initializes a new {@link ConnectionData}.
     * 
     * @param url
     * @param driverClass
     * @param props
     * @param block
     * @param max
     * @param min
     * @param type The {@link ConnectionType}
     * 
     */
    ConnectionData(String url, String driverClass, Properties props, boolean block, int max, int min, ConnectionType type) {
        super();
        this.url = url;
        this.driverClass = driverClass;
        this.props = props;
        this.block = block;
        this.max = max;
        this.min = min;
        this.type = type;
    }
    
    @Override
    public ConnectionType getConnectionType() {
        return type;
    }

}

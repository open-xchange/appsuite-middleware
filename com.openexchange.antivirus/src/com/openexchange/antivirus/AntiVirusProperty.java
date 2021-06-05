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

package com.openexchange.antivirus;

import com.openexchange.config.lean.Property;

/**
 * {@link AntiVirusProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum AntiVirusProperty implements Property {
    /**
     * Defines whether the Anti-Virus capability is enabled
     * Default: false
     */
    enabled(Boolean.FALSE),
    /**
     * Defines the address of the server.
     * Default: localhost
     */
    server("localhost"),
    /**
     * Defines the port at which the C-ICAP server is running
     * Default: 1344
     */
    port(Integer.valueOf(1344)),
    /**
     * Defines the anti-virus service's name
     * Default: avscan
     */
    service("avscan"),
    /**
     * Dictates the operation mode of the service. In 'streaming' mode
     * the data stream that will reach the end-point-client after will
     * be coming from the ICAP/AV server. In 'double-fetch' mode the
     * data stream will have to be fetched from the storage twice (one
     * for scanning and one for delivering to the end-point-client).
     * The streaming mode is still at an experimental phase.
     *
     * Default: double-fetch
     */
    mode("double-fetch"),
    /**
     * Defines the maximum file size (in MB) that is acceptable for the underlying
     * Anti-Virus service to scan. Files larger than that size will NOT be scanned
     * and an appropriate warning will be displayed to the user.
     *
     * Default: 100
     */
    maxFileSize(Integer.valueOf(100));

    private final Object defaultValue;
    private static final String PREFIX = "com.openexchange.antivirus.";

    /**
     * Initialises a new {@link AntiVirusProperty}.
     */
    private AntiVirusProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}

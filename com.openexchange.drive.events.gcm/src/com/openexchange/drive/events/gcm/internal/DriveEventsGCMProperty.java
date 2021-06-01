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

package com.openexchange.drive.events.gcm.internal;

import com.openexchange.config.lean.Property;


/**
 * {@link DriveEventsGCMProperty}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.2
 */
public enum DriveEventsGCMProperty implements Property {

    /**
     * Enables or disables push event notifications to clients using the Google
     * Cloud Messaging (GCM) service. This requires a valid configuration for the
     * GCM API key.
     * Default: false
     */
    ENABLED("enabled", Boolean.FALSE),

    /**
     * Specifies the API key of the server application.
     * Default: no default
     */
    KEY("key", null)
    ;

    public static final String FRAGMENT_FILE_NAME = "drive.properties";
    private static final String PREFIX = "com.openexchange.drive.events.gcm.";

    private final Object defaultValue;
    private final String name;

    private DriveEventsGCMProperty(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + name;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}

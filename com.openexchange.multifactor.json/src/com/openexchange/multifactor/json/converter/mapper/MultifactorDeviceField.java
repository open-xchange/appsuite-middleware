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

package com.openexchange.multifactor.json.converter.mapper;

/**
 * {@link MultifactorDeviceField}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorDeviceField {

    /**
     * The unique ID of the device
     */
    ID("id"),
    /**
     * The name of the provider
     */
    PROVIDER_NAME("providerName"),
    /**
     * The name of the device
     */
    NAME("name"),
    /**
     * The enable state of the device
     */
    ENABLED("enabled"),
    /**
     * Whether or not the device is a backup device
     */
    IS_BACKUP("backup"),

    /**
     * Additional, provider specific, parameters for the device, required to perform an multifactor action
     */
    PARAMETERS("parameters");

    private String jsonName;

    /**
     * Initializes a new {@link MultifactorDeviceField}.
     *
     * @param jsonName The json name of the field
     */
    private MultifactorDeviceField(String jsonName) {
        this.jsonName = jsonName;
    }

    /**
     * Gets the JSON name of the enum-value
     *
     * @return The JSON name
     */
    public String getJsonName() {
        return jsonName;
    }
}

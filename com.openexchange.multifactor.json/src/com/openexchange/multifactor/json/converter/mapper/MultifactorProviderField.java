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
 * {@link MultifactorProviderField}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public enum MultifactorProviderField {

    /**
     * The name of the provider
     */
    NAME("name"),

    /**
     * Whether or not the provider is a backup provider
     */
    IS_BACKUP("backupProvider"),

    /**
     * Whether or not the provider is a backup only provider
     */
    IS_BACKUP_ONLY("backupOnlyProvider");

    private String jsonName;

    /**
     * Initializes a new {@link MultifactorProviderField}.
     *
     * @param jsonName The JSON name of the field
     */
    private MultifactorProviderField(String jsonName) {
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

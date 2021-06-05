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

package com.openexchange.jsieve.commands;


/**
 * {@link JSONMatchType}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class JSONMatchType {

    private final String jsonName;
    private final String required;
    private final int versionRequirement;

    /**
     * Initializes a new {@link JSONMatchType}.
     */
    public JSONMatchType(String jsonName, String required, int versionRequirement) {
        super();
        this.jsonName = jsonName;
        this.required=required;
        this.versionRequirement = versionRequirement;
    }


    /**
     * Gets the jsonName
     *
     * @return The jsonName
     */
    public String getJsonName() {
        return jsonName;
    }


    /**
     * Gets the required
     *
     * @return The required
     */
    public String getRequired() {
        return required;
    }


    /**
     * Gets the minimum version requirement or 0 for no requirement.
     *
     * @return The versionRequirement
     */
    public int getVersionRequirement() {
        return versionRequirement;
    }



}

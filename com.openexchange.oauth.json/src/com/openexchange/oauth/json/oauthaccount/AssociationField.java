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

package com.openexchange.oauth.json.oauthaccount;

/**
 * {@link AssociationField} - Enumeration for OAuth account association fields.
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum AssociationField {

    /**
     * The identifier
     */
    ID("id"),
    /**
     * The folder (optional)
     */
    FOLDER("folder"),
    /**
     * The module
     */
    MODULE("module"),
    /**
     * The scopes
     */
    SCOPES("scopes"),
    /**
     * The name
     */
    NAME("name"),
    ;

    private String name;

    /**
     * Initialises a new {@link AssociationField}.
     * 
     * @param name The association's name
     */
    private AssociationField(String name) {
        this.name = name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }
}

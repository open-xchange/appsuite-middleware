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

package com.openexchange.contact.common;

import javax.activation.DataHandler;
import com.openexchange.exception.OXException;

/**
 * {@link DataHandlers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public enum DataHandlers {

    CONTACT("com.openexchange.contact"),

    /** The identifier of the data handler to convert a Contact to its JSON representation */
    CONTACT2JSON("com.openexchange.contact.json"),

    /** The identifier of the data handler to convert from an extended properties JSON representation to {@link ExtendedProperties}. */
    JSON2XPROPERTIES("com.openexchange.contact.json2xproperties"),

    /** The identifier of the data handler to convert from an {@link ExtendedProperties} to its JSON representation. */
    XPROPERTIES2JSON("com.openexchange.contact.xproperties2json"),

    /** The identifier of the data handler to convert from an ox exception's JSON representation to {@link OXException}s. */
    JSON2OXEXCEPTION("com.openexchange.contact.json2oxexception"),

    /** The identifier of the data handler to convert from an {@link OXException} to its JSON representation. */
    OXEXCEPTION2JSON("com.openexchange.contact.oxexception2json"),
    ;

    private String id;

    /**
     * Initialises a new {@link DataHandlers}.
     */
    private DataHandlers(String id) {
        this.id = id;
    }

    /**
     * Returns the {@link DataHandler}'s identifier
     *
     * @return the identifier
     */
    public String getId() {
        return id;
    }
}

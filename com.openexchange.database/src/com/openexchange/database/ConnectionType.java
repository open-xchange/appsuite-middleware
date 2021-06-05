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

package com.openexchange.database;


/**
 * {@link ConnectionType} - the type of a db connection. Either writable or readonly.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public enum ConnectionType {
    READONLY("read"),
    WRITABLE("write");
    
    private String tagName;
    
    /**
     * Initializes a new {@link ConnectionType}.
     */
    private ConnectionType(String tagName) {
        this.tagName = tagName;
    }
    
    /**
     * Gets an adequate {@link ConnectionType} 
     *
     * @param writable Whether a writable is required
     * @return The {@link ConnectionType}
     */
    public static ConnectionType get(boolean writable){
        return writable ? WRITABLE : READONLY;
    }
    
    /**
     * Gets the tagName for this {@link ConnectionType}
     *
     * @return The tag name
     */
    public String getTagName() {
        return tagName;
    }
}

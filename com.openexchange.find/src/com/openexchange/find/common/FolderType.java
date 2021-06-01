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

package com.openexchange.find.common;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.container.FolderObject;

/**
 * The folder type enumeration.
 */
public enum FolderType {

    /**
     * The type denoting private folders.
     */
    PRIVATE("private", FolderObject.PRIVATE),
    /**
     * The type denoting public folders.
     */
    PUBLIC("public", FolderObject.PUBLIC),
    /**
     * The type denoting shared folders.
     */
    SHARED("shared", FolderObject.SHARED)
    ;

    private static final Map<String, FolderType> typesById = new HashMap<String, FolderType>(3);
    static {
        for (FolderType type : values()) {
            typesById.put(type.getIdentifier(), type);
        }
    }

    private final String identifier;

    private final int intId;

    private FolderType(final String identifier, final int intId) {
        this.identifier = identifier;
        this.intId = intId;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the identifier as specified in {@link FolderObject}.
     */
    public int getIntIdentifier() {
        return intId;
    }

    /**
     * Gets the type by its identifier.
     *
     * @return The type or <code>null</code> if unknown.
     */
    public static FolderType getByIdentifier(String identifier) {
        return typesById.get(identifier);
    }
}
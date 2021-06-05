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

package com.openexchange.folderstorage;

import com.openexchange.config.lean.Property;

/**
 * {@link MovePermissionProperty}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public enum MovePermissionProperty implements Property {

    MOVE_TO_PUBLIC("permissions.moveToPublic", "inherit"),
    MOVE_TO_PRIVATE("permissions.moveToPrivate", "inherit"),
    MOVE_TO_SHARED("permissions.moveToShared", "inherit"),
    ;

    private final String propName;
    private Object defaultVal;

    /**
     * Initializes a new {@link MovePermissionProperty}.
     */
    private MovePermissionProperty(String propName, Object defaultVal) {
        this.propName = propName;
        this.defaultVal = defaultVal;
    }

    @Override
    public String getFQPropertyName() {
        return "com.openexchange.folderstorage." + propName;
    }

    @Override
    public Object getDefaultValue() {
        return defaultVal;
    }

}

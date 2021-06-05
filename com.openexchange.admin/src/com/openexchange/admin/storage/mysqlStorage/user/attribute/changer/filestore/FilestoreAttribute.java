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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.filestore;

import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;

/**
 * {@link FilestoreAttribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum FilestoreAttribute implements Attribute {

    ID("filestore-id", "filestore_id", Integer.class),
    OWNER("filestore-owner", "filestore_owner", Integer.class),
    NAME("filestore-name", "filestore_name", Integer.class),
    ;

    private final String sqlFieldName;
    private final Class<?> originalType;
    private static final String TABLE = "user";
    private final String attributeName;

    /**
     * 
     * Initialises a new {@link FilestoreAttribute}.
     * 
     * @param sqlFieldName the names of the attribute
     */
    private FilestoreAttribute(String attributeName, String sqlFieldName, Class<?> originalType) {
        this.attributeName = attributeName;
        this.sqlFieldName = sqlFieldName;
        this.originalType = originalType;
    }

    @Override
    public String getSQLFieldName() {
        return sqlFieldName;
    }

    @Override
    public Class<?> getOriginalType() {
        return originalType;
    }

    @Override
    public String getSQLTableName() {
        return TABLE;
    }

    @Override
    public String getName() {
        return attributeName;
    }
}

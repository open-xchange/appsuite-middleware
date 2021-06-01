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

package com.openexchange.group.internal;

import static com.openexchange.group.GroupStorage.StorageType.ACTIVE;
import static com.openexchange.group.GroupStorage.StorageType.DELETED;
import java.util.EnumMap;
import java.util.Map;
import com.openexchange.group.GroupStorage.StorageType;

/**
 * This class helps with defining SQL strings.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SQLStrings {

    /**
     * Prevent instantiation.
     */
    private SQLStrings() {
        super();
    }

    /**
     * Tables for groups.
     */
    public static final Map<StorageType, String> GROUP_TABLES =
        new EnumMap<StorageType, String>(StorageType.class);

    public static final Map<StorageType, String> INSERT_GROUP =
        new EnumMap<StorageType, String>(StorageType.class);

    static {
        GROUP_TABLES.put(ACTIVE, "groups");
        GROUP_TABLES.put(DELETED, "del_groups");
        final String tableName = "@tableName@";

        final String sql = "INSERT INTO " + tableName + "(cid,id,identifier," + "displayName,lastModified,gidNumber) VALUES (?,?,?,?,?,?)";
        for (final StorageType type : StorageType.values()) {
            INSERT_GROUP.put(type, sql.replaceAll(tableName, GROUP_TABLES.get(type)));
        }
    }
}

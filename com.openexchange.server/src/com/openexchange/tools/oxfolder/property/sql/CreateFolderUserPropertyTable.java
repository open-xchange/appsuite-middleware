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

package com.openexchange.tools.oxfolder.property.sql;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreateFolderUserPropertyTable}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class CreateFolderUserPropertyTable extends AbstractCreateTableImpl {

    public final static String TABLE_NAME = "oxfolder_user_property";

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { TABLE_NAME };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { buildUserPropertyFolderTable() };
    }

    /**
     * Build the {@value #TABLE_NAME} table
     * 
     * @return SQL <code>CREATE TABLE</code> command
     */
    private String buildUserPropertyFolderTable() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");
        builder.append(TABLE_NAME);
        builder.append(" (");
        builder.append("cid INT(10) UNSIGNED NOT NULL,");
        builder.append("fuid INT(10) UNSIGNED NOT NULL,");
        builder.append("userid INT(10) UNSIGNED NOT NULL,");
        builder.append("name VARCHAR(128) NOT NULL,");
        builder.append("value TINYTEXT NOT NULL,");
        builder.append("PRIMARY KEY (cid, fuid, userid, name)");
        builder.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;");
        return builder.toString();
    }

}

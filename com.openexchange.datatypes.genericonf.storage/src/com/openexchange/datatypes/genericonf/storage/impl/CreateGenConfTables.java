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

package com.openexchange.datatypes.genericonf.storage.impl;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * Creates the tables for the generic conf storage if a new schema is created.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class CreateGenConfTables extends AbstractCreateTableImpl {

    /**
     * Default constructor.
     */
    public CreateGenConfTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        return createdTables;
    }

    @Override
    public String[] getCreateStatements() {
        return createsPrimaryKey;
    }

    private static final String[] createdTables = { "genconf_attributes_strings", "genconf_attributes_bools", "sequence_genconf" };

    private static final String[] createsPrimaryKey = {
        "CREATE TABLE genconf_attributes_strings (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,name VARCHAR(100) DEFAULT NULL,value VARCHAR(256) DEFAULT NULL,uuid BINARY(16) NOT NULL,PRIMARY KEY (cid, id, uuid),KEY (cid,id,name)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
        "CREATE TABLE genconf_attributes_bools (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,name VARCHAR(100) DEFAULT NULL,value BOOL DEFAULT NULL,uuid BINARY(16) NOT NULL,PRIMARY KEY (cid, id, uuid),KEY (cid,id,name)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
        "CREATE TABLE sequence_genconf (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,PRIMARY KEY (cid)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci" };

}

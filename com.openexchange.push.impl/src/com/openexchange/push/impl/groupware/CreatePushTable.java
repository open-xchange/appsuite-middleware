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

package com.openexchange.push.impl.groupware;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreatePushTable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CreatePushTable extends AbstractCreateTableImpl {

    public static final String getCreateTableStatement() {
        return ("CREATE TABLE registeredPush (" +
        "cid INT4 UNSIGNED NOT NULL," +
        "user INT4 UNSIGNED NOT NULL," +
        "client VARCHAR(64) CHARACTER SET latin1 NOT NULL," +
        "PRIMARY KEY (cid, user, client)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
    }

    public CreatePushTable() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { getCreateTableStatement() };
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "registeredPush" };
    }

}

/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.storage.rdb.groupware;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link ChronosCreateTableService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChronosCreateTableService extends AbstractCreateTableImpl {

    /**
     * Gets the <code>CREATE TABLE</code> statements for the <i>chronos</i> tables, mapped by their table names.
     *
     * @return The <code>CREATE TABLE</code> statements mapped by their table name
     */
    static Map<String, String> getTablesByName() {
        Map<String, String> tablesByName = new HashMap<String, String>(); //@formatter:off
        tablesByName.put("calendar_sequence",
            "CREATE TABLE calendar_sequence (" +
                "cid INT4 UNSIGNED NOT NULL," +
                "account INT4 UNSIGNED NOT NULL," +
                "id INT4 UNSIGNED NOT NULL," +
                "PRIMARY KEY (cid,account)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;"
        );
        tablesByName.put("calendar_account",
            "CREATE TABLE calendar_account (" +
                "cid INT4 UNSIGNED NOT NULL," +
                "id INT4 UNSIGNED NOT NULL," +
                "provider VARCHAR(64) CHARACTER SET latin1 NOT NULL," +
                "user INT4 UNSIGNED NOT NULL," +
                "modified BIGINT(20) NOT NULL," +
                "data BLOB," +
                "PRIMARY KEY (cid,id)," +
                "KEY `user` (cid,user)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;"
        );
        return tablesByName; //@formatter:on
    }

    /**
     * Initializes a new {@link ChronosCreateTableService}.
     */
    public ChronosCreateTableService() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        Collection<String> createStatements = getTablesByName().values();
        return createStatements.toArray(new String[createStatements.size()]);
    }

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        Set<String> tableNames = getTablesByName().keySet();
        return tableNames.toArray(new String[tableNames.size()]);
    }

}

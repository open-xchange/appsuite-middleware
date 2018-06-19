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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask;
import com.openexchange.tools.update.Column;

/**
 * {@link ContactTablesUtf8Mb4UpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ContactTablesUtf8Mb4UpdateTask extends SimpleConvertUtf8ToUtf8mb4UpdateTask {

    private static final Logger LOG = LoggerFactory.getLogger(ContactTablesUtf8Mb4UpdateTask.class);

    /**
     * Initialises a new {@link ContactTablesUtf8Mb4UpdateTask}.
     */
    public ContactTablesUtf8Mb4UpdateTask() {
        //@formatter:off
        super(Arrays.asList("prg_dlist", "del_dlist", "prg_contacts_linkage", "prg_contacts_image", "del_contacts_image"),
            "com.openexchange.contact.storage.rdb.sql.CorrectNumberOfImagesTask");
        //@formatter:on
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask#before(com.openexchange.groupware.update.PerformParameters, java.sql.Connection)
     */
    @Override
    protected void before(PerformParameters params, Connection connection) throws SQLException {
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field01" }, new int[] { -1, 191 });
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field65" }, new int[] { -1, 191 });
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field66" }, new int[] { -1, 191 });
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field67" }, new int[] { -1, 191 });

        recreateKey(connection, "del_contacts", new String[] { "cid", "field01" }, new int[] { -1, 191 });
        recreateKey(connection, "del_contacts", new String[] { "cid", "field65" }, new int[] { -1, 191 });
        recreateKey(connection, "del_contacts", new String[] { "cid", "field66" }, new int[] { -1, 191 });
        recreateKey(connection, "del_contacts", new String[] { "cid", "field67" }, new int[] { -1, 191 });

        Column column = new Column("field17", "TEXT COLLATE utf8mb4_unicode_ci NULL");
        String schema = params.getSchema().getSchema();

        resetZeroedTimestampColumn(connection, "timestampfield01");
        resetZeroedTimestampColumn(connection, "timestampfield02");

        LOG.info("");

        changeTable(connection, schema, "prg_contacts", Collections.emptyMap(), Collections.singletonList(column), Collections.emptyList());
        changeTable(connection, schema, "del_contacts", Collections.emptyMap(), Collections.singletonList(column), Collections.emptyList());
    }

    /**
     * Resets the value of the column with the specified name to <code>NULL</code> if the timestamp is '0000-00-00'
     * 
     * @param connection The {@link Connection}
     * @param columnName The column name
     * @throws SQLException if an SQL error is occurred
     */
    private void resetZeroedTimestampColumn(Connection connection, String columnName) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE prg_contacts SET " + columnName + "=NULL WHERE " + columnName + "='0000-00-00'");
            LOG.info("Reset {} rows for column '{}' that contained invalid timestamps", ps.executeUpdate(), columnName);
        } finally {
            Databases.closeSQLStuff(ps);
        }
    }
}

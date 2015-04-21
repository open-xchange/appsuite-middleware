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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.tools.file.external;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link QuotaFileStorages} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class QuotaFileStorages {

    /**
     * Initializes a new {@link QuotaFileStorages}.
     */
    private QuotaFileStorages() {
        super();
    }


    // -----------------------------------------------------------------------------------------------------------------------------

    private static final ConcurrentMap<String, Boolean> v780Schemas = new ConcurrentHashMap<String, Boolean>();

    /**
     * Gets a value indicating whether the supplied database connection points to a database schema that already contains changes introduced
     * with version <code>7.8.0</code>, i.e. if the <code>user</code> table already has the <code>guestCreatedBy</code> column.
     *
     * @param connection The connection to check
     * @param contextID The context identifier
     * @return <code>true</code> if the <code>user</code> table has the <code>guestCreatedBy</code> column, <code>false</code>, otherwise
     */
    public static boolean hasUserColumn(Connection connection, int contextID) throws OXException {
        try {
            String schemaName = connection.getCatalog();
            if (null == schemaName) {
                schemaName = ServerServiceRegistry.getServize(DatabaseService.class).getSchemaName(contextID);
                if (null == schemaName) {
                    throw LdapExceptionCode.UNEXPECTED_ERROR.create("No schema name for connection");
                }
            }
            return hasUserColumn(connection, schemaName);
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        }
    }

    /**
     * Gets a value indicating whether the supplied database connection points to a database schema that already contains changes introduced
     * with version <code>7.8.0</code>, i.e. if the <code>user</code> table already has the <code>guestCreatedBy</code> column.
     *
     * @param connection The connection to check
     * @param contextID The context identifier
     * @return <code>true</code> if the <code>user</code> table has the <code>guestCreatedBy</code> column, <code>false</code>, otherwise
     */
    public static boolean hasUserColumn(Connection connection, String schemaName) throws OXException {
        try {
            Boolean value = v780Schemas.get(schemaName);
            if (null == value) {
                ResultSet result = null;
                try {
                    result = connection.getMetaData().getColumns(null, schemaName, "user", "filestore_usage");
                    value = Boolean.valueOf(result.next());
                } finally {
                    DBUtils.closeSQLStuff(result);
                }
                v780Schemas.putIfAbsent(schemaName, value);
            }
            return value.booleanValue();
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        }
    }

}

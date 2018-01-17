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

package com.openexchange.multifactor.storage.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link MultifactorStorageCommon} contains shared code in multifactor storage classes
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public abstract class MultifactorStorageCommon {

    /**
     * Removes all multifactor devices for a given user
     *
     * @param dbs Database service to use
     * @param sql SQL statement to use
     * @param userId Id of the user
     * @param contextId Id of the context
     * @return <code>true</code> if items removed
     * @throws OXException
     */
    protected boolean deleteAllForUser(DatabaseService dbs, String sql, int userId, int contextId) throws OXException {
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        int rows = 0;
        try {
            statement = connection.prepareStatement(sql);
            int param = 1;
            statement.setInt(param++, contextId);
            statement.setInt(param++, userId);
            rows = statement.executeUpdate();
            return rows > 0 ? true : false;
        } catch (final SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                if (rows > 0) {
                    dbs.backWritable(connection);
                } else {
                    dbs.backWritableAfterReading(connection);
                }
            }
        }
    }

    /**
     * Removes all multifactor devices for a given context
     *
     * @param dbs Database service to use
     * @param sql SQL statement to use
     * @param contextId Id of the context
     * @return <code>true</code> if rows affected
     * @throws OXException
     */
    protected boolean deleteAllForContext(DatabaseService dbs, String sql, int contextId) throws OXException {
        final Connection connection = dbs.getWritable(contextId);
        PreparedStatement statement = null;
        int rows = 0;
        try {
            statement = connection.prepareStatement(sql);
            int param = 1;
            statement.setInt(param++, contextId);
            rows = statement.executeUpdate();
            return rows > 0 ? true : false;
        } catch (final SQLException e) {
            throw MultifactorExceptionCodes.SQL_EXCEPTION.create(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                if (rows > 0) {
                    dbs.backWritable(connection);
                } else {
                    dbs.backWritableAfterReading(connection);
                }
            }
        }
    }

}

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

package com.openexchange.share.limit.storage;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.share.limit.FileAccess;
import com.openexchange.share.limit.exceptions.LimitExceptionCodes;

/**
 *
 * Database storage implementation for the limiting
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.2
 */
public class RdbFileAccessStorage {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbFileAccessStorage.class);

    /** SQL statement to insert a new file access */
    protected static final String INSERT_FILE_ACCESS = "INSERT INTO fileAccess (cid, userid, size, accessed) VALUES (?, ?, ?, ?)";

    protected static final String DELETE_ACCESSES_OLDER_THAN_DEFINED = "DELETE FROM fileAccess WHERE cid=? AND userId=? AND accessed<?"; //only delete for dedicated user as there might be different sliding windows for other users

    protected static final String RESOLVE_COUNT_AND_RATE = "SELECT count(*), SUM(size) FROM fileAccess WHERE cid=? AND userId=? AND accessed BETWEEN ? AND ? LIMIT 1"; // count(*) retrieves count

    /**
     * Singleton implementation.
     */
    private static volatile RdbFileAccessStorage impl;

    public static RdbFileAccessStorage getInstance() {
        RdbFileAccessStorage tmp = impl;
        if (null == tmp) {
            synchronized (RdbFileAccessStorage.class) {
                tmp = impl;
                if (null == tmp) {
                    tmp = new RdbFileAccessStorage();
                    impl = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Adds a new file access
     * 
     * @param contextId The id of the users context
     * @param userId The id of the user in given context
     * @param size The used size in bytes
     * @param connection Existing connection that should be used to add the file access.
     * @throws OXException
     */
    public void addAccess(int contextId, int userId, long size, Connection connection) throws OXException {
        if (connection == null) {
            throw LimitExceptionCodes.NO_CONNECTION_PROVIDED.create();
        }

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(INSERT_FILE_ACCESS);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            statement.setLong(3, size);
            statement.setLong(4, new Date().getTime());

            long affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                LOG.error("There have been " + affectedRows + " changes for adding a new file access but there should only be 1. Executed SQL: " + statement.toString());
            }
        } catch (final SQLException e) {
            throw LimitExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(statement);
        }
    }

    /**
     * Removes file accesses for given user that are older than defined by provided timestamp.
     * 
     * @param contextId The id of the users context
     * @param userId The id of the user in given context
     * @param timestamp The timestamp older entries will be removed
     * @param connection Existing connection that should be used to remove the file accesses.
     * @throws OXException
     */
    public void removeAccesses(int contextId, int userId, long timestamp, Connection connection) throws OXException {
        if (connection == null) {
            throw LimitExceptionCodes.NO_CONNECTION_PROVIDED.create();
        }

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(DELETE_ACCESSES_OLDER_THAN_DEFINED);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            statement.setLong(3, timestamp);

            statement.executeUpdate();
        } catch (final SQLException e) {
            throw LimitExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(statement);
        }
    }

    /**
     * Returns the usage from given start timestamp to now.
     * 
     * @param contextId The id of the users context
     * @param userId The id of the user in given context
     * @param start The start timestamp to start search
     * @param connection Existing (read) connection that should be used to get the file accesses.
     * @return Persisted used {@link FileAccess} for the given timeframe
     * @throws OXException
     */
    public FileAccess getUsage(int contextId, int userId, long start, Connection connection) throws OXException {
        long now = new Date().getTime();
        return getUsage(contextId, userId, start, now, connection);
    }

    /**
     * Returns the usage from given start timestamp to given end timestamp.
     * 
     * @param contextId The id of the users context
     * @param userId The id of the user in given context
     * @param start The start timestamp for the search
     * @param end The end timestamp for the search
     * @param connection Existing (read) connection that should be used to get the file accesses.
     * @return Persisted used {@link FileAccess} for the given timeframe
     * @throws OXException
     */
    public FileAccess getUsage(int contextId, int userId, long start, long end, Connection connection) throws OXException {
        if (connection == null) {
            throw LimitExceptionCodes.NO_CONNECTION_PROVIDED.create();
        }

        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(RESOLVE_COUNT_AND_RATE);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            statement.setLong(3, start);
            statement.setLong(4, end);
            result = statement.executeQuery();

            if (result.next()) {
                int count = result.getInt(1);
                long bytes = result.getLong(2);
                return new FileAccess(contextId, userId, start, end, count, bytes);
            }
        } catch (final SQLException e) {
            throw LimitExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, statement);
        }
        return new FileAccess(contextId, userId, start, end, 0, 0);
    }
}

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

package com.openexchange.groupware.impl.id;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.id.IDExceptionCodes;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link IDGeneratorServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IDGeneratorServiceImpl implements IDGeneratorService {

    /**
     * Initializes a new {@link IDGeneratorServiceImpl}.
     */
    public IDGeneratorServiceImpl() {
        super();
    }

    @Override
    public int getId(final String type, final int contextId) throws OXException {
        return getId(type, contextId, 1);
    }

    @Override
    public int getId(final String type, final int contextId, final int minId) throws OXException {
        /*
         * Get appropriate connection
         */
        final Connection con = getWritableConnection(contextId);
        try {
            /*
             * Try to perform an UPDATE
             */
            final int id = getId(type, contextId, minId, con);
            if (id < 0) {
                /*
                 * Failed
                 */
                throw IDExceptionCodes.ID_GEN_FAILED.create();
            }
            /*
             * Return identifier
             */
            return id;
        } catch (final SQLException e) {
            throw IDExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw IDExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Database.back(contextId, true, con);
        }
    }

    private static Connection getWritableConnection(final int contextId) throws OXException {
        return Database.get(contextId, true);
    }

    private static int getId(final String type, final int contextId, final int minId, final Connection con) throws SQLException {
        /*
         * Start
         */
        int retval = -1;
        int retry = 0;
        boolean tryInsert = true;
        while (retval < 0 && retry++ < 5) {
            /*
             * Try to perform an UPDATE
             */
            int cur;
            int increment;
            do {
                cur = performSelect(type, contextId, con);
                if (cur < 0) {
                    if (tryInsert) {
                        if (performInsert(type, contextId, minId, con)) {
                            return minId;
                        }
                        tryInsert = false;
                    }
                    cur = performSelect(type, contextId, con);
                }
                if (cur < minId) {
                    increment = minId - cur;
                } else {
                    increment = 1;
                }
            } while (!compareAndSet(type, contextId, cur, cur + increment, con) && retry++ < 5);
            retval = cur + increment;
            /*
             * Retry...
             */
        }
        /*
         * Return value
         */
        return retval;
    }

    private static boolean compareAndSet(final String type, final int contextId, final int expected, final int newValue, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE sequenceIds SET id = ? WHERE cid = ? AND type = ? AND id = ?");
            stmt.setInt(1, newValue);
            stmt.setInt(2, contextId);
            stmt.setString(3, type);
            stmt.setInt(4, expected);
            final int result = stmt.executeUpdate();
            return (result > 0);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int performSelect(final String type, final int contextId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM sequenceIds WHERE cid = ? AND type = ?");
            stmt.setInt(1, contextId);
            stmt.setString(2, type);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static boolean performInsert(final String type, final int contextId, final int firstValue, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO sequenceIds (cid, type, id) VALUES (?, ?, ?)");
            stmt.setInt(1, contextId);
            stmt.setString(2, type);
            stmt.setInt(3, firstValue);
            try {
                final int result = stmt.executeUpdate();
                return (result > 0);
            } catch (final SQLException e) {
                return false;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}

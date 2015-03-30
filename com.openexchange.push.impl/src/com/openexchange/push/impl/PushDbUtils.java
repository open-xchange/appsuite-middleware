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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.push.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushUser;
import com.openexchange.push.impl.osgi.Services;

/**
 * {@link PushDbUtils}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PushDbUtils {

    /**
     * Initializes a new {@link PushDbUtils}.
     */
    private PushDbUtils() {
        super();
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the available push registrations
     *
     * @return The push registrations
     * @throws OXException If push registrations cannot be returned
     */
    public static List<PushUser> getPushRegistrations() throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);

        // Query context2push_registration table
        Set<Integer> contextIds = getContextsWithPushRegistrations(service);
        if (contextIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<PushUser> users = new LinkedList<PushUser>();
        for (Integer contextId : contextIds) {
            addPushRegistrationsFromAssociatedSchema(contextId.intValue(), users, service);
        }
        return users;
    }

    private static void addPushRegistrationsFromAssociatedSchema(int contextId, List<PushUser> users, DatabaseService service) throws OXException {
        Connection con = service.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user FROM registeredPush ORDER BY cid, user");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return;
            }

            do {
                int currentContextId = rs.getInt(1);
                int currentUserId = rs.getInt(1);
                users.add(new PushUser(currentUserId, currentContextId));
            } while (rs.next());
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            service.backReadOnly(contextId, con);
        }
    }

    private static Set<Integer> getContextsWithPushRegistrations(DatabaseService service) throws OXException {
        Connection con = service.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM context2push_registration");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptySet();
            }
            Set<Integer> contextIds = new LinkedHashSet<Integer>(128);
            Set<Integer> alreadyProcessed = new HashSet<Integer>();
            do {
                Integer contextId = Integer.valueOf(rs.getInt(1));
                if (alreadyProcessed.add(contextId)) {
                    contextIds.add(contextId);
                    for (int iContextId : service.getContextsInSameSchema(contextId.intValue())) {
                        alreadyProcessed.add(Integer.valueOf(iContextId));
                    }
                }
            } while (rs.next());
            return contextIds;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            service.backReadOnly(con);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Inserts a push registration for specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on successful registration; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public static boolean insertPushRegistration(int userId, int contextId) throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getWritable(contextId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            boolean inserted = insertPushRegistration(userId, contextId, con);
            if (inserted) {
                markContextForPush(contextId, service);
            }

            con.commit();
            rollback = false;

            return inserted;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            service.backWritable(contextId, con);
        }
    }

    /**
     * Inserts a push registration for specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return <code>true</code> on successful registration; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    private static boolean insertPushRegistration(int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return false;
            }

            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("INSERT INTO registeredPush (cid,user) VALUES (?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static boolean markContextForPush(int contextId, DatabaseService service) throws OXException {
        Connection con = service.getWritable();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO context2push_registration (cid) VALUES (?)");
            stmt.setInt(1, contextId);
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            service.backWritable(con);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Deletes a push registration for specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on successful deletion; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public static boolean deletePushRegistration(int userId, int contextId) throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getWritable(contextId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            boolean[] unmark = new boolean[1];
            unmark[0] = false;

            boolean deleted = deletePushRegistration(userId, contextId, unmark, con);

            if (unmark[0]) {
                unmarkContextForPush(contextId, service);
            }

            con.commit();
            rollback = false;
            return deleted;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            service.backWritable(contextId, con);
        }
    }

    /**
     * Deletes a push registration for specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return <code>true</code> on successful deletion; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    private static boolean deletePushRegistration(int userId, int contextId, boolean[] unmark, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? FOR UPDATE");
            rs = stmt.executeQuery();
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("DELETE FROM registeredPush WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            boolean deleted = stmt.executeUpdate() > 0;
            Databases.closeSQLStuff(stmt);

            if (deleted) {
                stmt = con.prepareStatement("SELECT COUNT(user) FROM registeredPush WHERE cid=?");
                rs = stmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                unmark[0] = count <= 0;
            }

            return deleted;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static boolean unmarkContextForPush(int contextId, DatabaseService service) throws OXException {
        Connection con = service.getWritable();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM context2push_registration WHERE cid=?");
            stmt.setInt(1, contextId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            service.backWritable(con);
        }
    }

}

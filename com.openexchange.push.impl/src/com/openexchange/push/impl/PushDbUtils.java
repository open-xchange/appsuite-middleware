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

package com.openexchange.push.impl;

import static com.openexchange.tools.update.Tools.tableExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserClient;
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

    /**
     * Possible delete results.
     */
    public static enum DeleteResult {
        /**
         * Nothing deleted.
         */
        NOT_DELETED,
        /**
         * Deleted registration only for associated client.
         */
        DELETED_FOR_CLIENT,
        /**
         * Deleted last and therefore all registrations.
         */
        DELETED_COMPLETELY;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Has push registration
     *
     * @param pushUser The push user to check
     * @return <code>true</code> if a push registration is available; otherwise <code>false</code>
     * @throws OXException If push registrations cannot be returned
     */
    public static boolean hasPushRegistration(PushUser pushUser) throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);
        int contextId = pushUser.getContextId();
        Connection con = service.getReadOnly(contextId);
        try {
            return hasPushRegistration(pushUser, con);
        } finally {
            service.backReadOnly(contextId, con);
        }
    }

    private static boolean hasPushRegistration(PushUser pushUser, Connection con) throws OXException {
        int contextId = pushUser.getContextId();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, pushUser.getUserId());
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Has push registration for given client
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if a push registration is available; otherwise <code>false</code>
     * @throws OXException If push registrations cannot be returned
     */
    public static boolean hasPushRegistrationForClient(int userId, int contextId, String client) throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getReadOnly(contextId);
        try {
            return hasPushRegistrationForClient(userId, contextId, client, con);
        } finally {
            service.backReadOnly(contextId, con);
        }
    }

    private static boolean hasPushRegistrationForClient(int userId, int contextId, String client, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? AND user=? AND client=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, client);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the available push client registrations
     *
     * @return The push client registrations
     * @throws OXException If push client registrations cannot be returned
     */
    public static List<PushUserClient> getPushClientRegistrations() throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);

        // Query context2push_registration table
        Set<Integer> contextIds = getContextsWithPushRegistrations(service);
        if (contextIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<PushUserClient> clientSet = new HashSet<PushUserClient>(64);
        for (Integer contextId : contextIds) {
            addPushClientRegistrationsFromAssociatedSchema(contextId.intValue(), clientSet, service);
        }

        List<PushUserClient> lclients = new ArrayList<PushUserClient>(clientSet);
        Collections.sort(lclients);
        return lclients;
    }

    private static void addPushClientRegistrationsFromAssociatedSchema(int contextId, Set<PushUserClient> clientSet, DatabaseService service) throws OXException {
        Connection con = service.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, client FROM registeredPush ORDER BY cid, user");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return;
            }

            do {
                clientSet.add(new PushUserClient(new PushUser(rs.getInt(2), rs.getInt(1)), rs.getString(3)));
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

        Set<PushUser> userSet = new HashSet<PushUser>(64);
        for (Integer contextId : contextIds) {
            addPushRegistrationsFromAssociatedSchema(contextId.intValue(), userSet, service);
        }

        List<PushUser> lusers = new ArrayList<PushUser>(userSet);
        Collections.sort(lusers);
        return lusers;
    }

    private static void addPushRegistrationsFromAssociatedSchema(int contextId, Set<PushUser> userSet, DatabaseService service) throws OXException {
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
                userSet.add(new PushUser(rs.getInt(2), rs.getInt(1)));
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
            if (false == tableExists(con, "context2push_registration")) {
                return Collections.emptySet();
            }
            stmt = con.prepareStatement("SELECT cid FROM context2push_registration");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptySet();
            }
            Set<Integer> contextIds = new LinkedHashSet<Integer>(128);
            Set<Integer> alreadyProcessed = new HashSet<Integer>(1024);
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
     * Inserts a push registration associated with the client of specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param clientId The client identifier
     * @return <code>true</code> on successful registration; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public static boolean insertPushRegistration(int userId, int contextId, String clientId) throws OXException {
        if (hasPushRegistrationForClient(userId, contextId, clientId)) {
            return false;
        }

        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getWritable(contextId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            boolean inserted = insertPushRegistration(userId, contextId, clientId, con);
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

    private static boolean insertPushRegistration(int userId, int contextId, String clientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO registeredPush (cid,user,client) VALUES (?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, clientId);
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    return false;
                }
                throw e;
            }
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static boolean markContextForPush(int contextId, DatabaseService service) throws OXException {
        int updated = 0;
        Connection con = service.getWritable();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO context2push_registration (cid) VALUES (?)");
            stmt.setInt(1, contextId);
            try {
                updated = stmt.executeUpdate();
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    return false;
                }
                throw e;
            }
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (0 < updated) {
                service.backWritable(con);
            } else {
                service.backWritableAfterReading(con);
            }
        }
        return 0 < updated;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Deletes a push registration associated with the client of specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on successful deletion; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public static DeleteResult deleteAllPushRegistrations(int userId, int contextId) throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getWritable(contextId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            boolean[] unmark = new boolean[1];
            unmark[0] = false;

            boolean deleted = deleteAllPushRegistrations(userId, contextId, unmark, con);

            DeleteResult deleteResult;
            if (unmark[0]) {
                unmarkContextForPush(contextId, service);
                deleteResult = DeleteResult.DELETED_COMPLETELY;
            } else {
                deleteResult = (deleted ? DeleteResult.DELETED_COMPLETELY : DeleteResult.NOT_DELETED);
            }

            con.commit();
            rollback = false;

            return deleteResult;
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

    private static boolean deleteAllPushRegistrations(int userId, int contextId, boolean[] unmark, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? FOR UPDATE");
            stmt.setInt(1, contextId);
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
                stmt.setInt(1, contextId);
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

    /**
     * Deletes a push registration associated with the client of specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param clientId The client identifier
     * @return <code>true</code> on successful deletion; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public static DeleteResult deletePushRegistration(int userId, int contextId, String clientId) throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getWritable(contextId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            boolean[] unmark = new boolean[1];
            unmark[0] = false;

            boolean deleted = deletePushRegistration(userId, contextId, clientId, unmark, con);

            DeleteResult deleteResult;
            if (unmark[0]) {
                unmarkContextForPush(contextId, service);
                deleteResult = DeleteResult.DELETED_COMPLETELY;
            } else {
                deleteResult = (deleted ? DeleteResult.DELETED_FOR_CLIENT : DeleteResult.NOT_DELETED);
            }

            con.commit();
            rollback = false;

            return deleteResult;
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

    private static boolean deletePushRegistration(int userId, int contextId, String clientId, boolean[] unmark, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? FOR UPDATE");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("DELETE FROM registeredPush WHERE cid=? AND user=? AND client=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, clientId);
            boolean deleted = stmt.executeUpdate() > 0;
            Databases.closeSQLStuff(stmt);

            if (deleted) {
                stmt = con.prepareStatement("SELECT COUNT(user) FROM registeredPush WHERE cid=?");
                stmt.setInt(1, contextId);
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

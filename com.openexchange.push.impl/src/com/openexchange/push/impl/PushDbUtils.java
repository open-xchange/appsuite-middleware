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

package com.openexchange.push.impl;

import static com.openexchange.tools.update.Tools.tableExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
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
         * Deleted last and therefore all registrations for associated user.
         */
        DELETED_COMPLETELY,
        /**
         * Deleted last registration in user-associated context.
         */
        DELETED_ALL_IN_CONTEXT;
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
     * @param optClient The optional client
     * @return <code>true</code> if a push registration is available; otherwise <code>false</code>
     * @throws OXException If push registrations cannot be returned
     */
    public static boolean hasPushRegistrationForClient(int userId, int contextId, String optClient) throws OXException {
        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getReadOnly(contextId);
        try {
            return hasPushRegistrationForClient(userId, contextId, optClient, con);
        } finally {
            service.backReadOnly(contextId, con);
        }
    }

    private static boolean hasPushRegistrationForClient(int userId, int contextId, String optClient, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(null == optClient ? "SELECT 1 FROM registeredPush WHERE cid=? AND user=?" : "SELECT 1 FROM registeredPush WHERE cid=? AND user=? AND client=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            if (null != optClient) {
                stmt.setString(3, optClient);
            }
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
        ContextService contextService = Services.requireService(ContextService.class);

        // Query context2push_registration table
        List<Integer> contextIds = getContextsWithPushRegistrations(service, contextService);
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
        ContextService contextService = Services.requireService(ContextService.class);

        // Query context2push_registration table
        List<Integer> contextIds = getContextsWithPushRegistrations(service, contextService);
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

    private static List<Integer> getContextsWithPushRegistrations(DatabaseService service, ContextService contextService) throws OXException {
        Connection con = service.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (false == tableExists(con, "context2push_registration")) {
                return Collections.emptyList();
            }
            stmt = con.prepareStatement("SELECT cid FROM context2push_registration");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Integer> queriedContextIds = new ArrayList<>();
            do {
                queriedContextIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());

            Map<PoolAndSchema, List<Integer>> schemaAssociations = contextService.getSchemaAssociationsFor(queriedContextIds);
            List<Integer> contextIds = new ArrayList<Integer>(schemaAssociations.size());
            for (List<Integer> cids : schemaAssociations.values()) {
                contextIds.add(cids.get(0));
            }
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
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            boolean inserted = insertPushRegistration(userId, contextId, clientId, con);
            if (inserted) {
                markContextForPush(contextId, service);
            }

            con.commit();
            rollback = 2;

            return inserted;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            service.backWritable(contextId, con);
        }
    }

    private static boolean insertPushRegistration(int userId, int contextId, String clientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? FOR UPDATE");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("INSERT IGNORE INTO registeredPush (cid,user,client) VALUES (?,?,?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, clientId);
            try {
                int rows = stmt.executeUpdate();
                return rows > 0;
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
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static void markContextForPush(int contextId, DatabaseService service) throws OXException {
        Connection con = service.getWritable();
        PreparedStatement stmt = null;
        int updated = 0;
        try {
            stmt = con.prepareStatement("INSERT IGNORE INTO context2push_registration (cid) VALUES (?)");
            stmt.setInt(1, contextId);
            try {
                updated = stmt.executeUpdate();
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    return;
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
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Deletes push registrations associated with all clients of specified user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on successful deletion; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    public static DeleteResult deleteAllPushRegistrations(int userId, int contextId) throws OXException {
        return deletePushRegistration0(userId, contextId, null);
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
        return deletePushRegistration0(userId, contextId, clientId);
    }

    private static DeleteResult deletePushRegistration0(int userId, int contextId, String optClientId) throws OXException {
        if (false == hasPushRegistrationForClient(userId, contextId, optClientId)) {
            return DeleteResult.NOT_DELETED;
        }

        DatabaseService service = Services.requireService(DatabaseService.class);
        Connection con = service.getWritable(contextId);
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            DeleteResult deleteResult = deletePushRegistration(userId, contextId, optClientId, con);
            if (deleteResult == DeleteResult.DELETED_ALL_IN_CONTEXT) {
                unmarkContextForPush(contextId, service);
            }

            con.commit();
            rollback = 2;

            return deleteResult;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            service.backWritable(contextId, con);
        }
    }

    private static DeleteResult deletePushRegistration(int userId, int contextId, String optClientId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? FOR UPDATE");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
            rs = null;

            DeleteResult deleteResult;
            if (null == optClientId) {
                stmt = con.prepareStatement("DELETE FROM registeredPush WHERE cid=? AND user=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                boolean deleted = stmt.executeUpdate() > 0;
                Databases.closeSQLStuff(stmt);
                stmt = null;
                deleteResult = deleted ? DeleteResult.DELETED_COMPLETELY : DeleteResult.NOT_DELETED;
            } else {
                stmt = con.prepareStatement("DELETE FROM registeredPush WHERE cid=? AND user=? AND client=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, optClientId);
                boolean deleted = stmt.executeUpdate() > 0;
                Databases.closeSQLStuff(stmt);
                stmt = null;
                deleteResult = deleted ? DeleteResult.DELETED_FOR_CLIENT : DeleteResult.NOT_DELETED;

                // Check if all for user were deleted through dropping push registration
                if (deleted) {
                    stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=? AND user=?");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        deleteResult = DeleteResult.DELETED_COMPLETELY;
                    }
                    Databases.closeSQLStuff(rs, stmt);
                    stmt = null;
                    rs = null;
                }
            }

            // Check if all in context were deleted through dropping push registration
            if (deleteResult != DeleteResult.NOT_DELETED) {
                stmt = con.prepareStatement("SELECT 1 FROM registeredPush WHERE cid=?");
                stmt.setInt(1, contextId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    deleteResult = DeleteResult.DELETED_ALL_IN_CONTEXT;
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
                rs = null;
            }

            return deleteResult;
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

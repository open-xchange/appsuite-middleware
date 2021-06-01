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

package com.openexchange.context.rmi;

import static com.openexchange.java.Autoboxing.I;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;

/**
 * {@link ContextRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ContextRMIServiceImpl implements ContextRMIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextRMIServiceImpl.class);

    /**
     * Initialises a new {@link ContextRMIServiceImpl}.
     */
    public ContextRMIServiceImpl() {
        super();
    }

    @Override
    public boolean checkLogin2ContextMapping() throws RemoteException {
        DatabaseService databaseService = getDatabaseService();

        Connection con = null;
        try {
            con = databaseService.getWritable();
            // Get context identifiers
            TIntList contextIds = new TIntLinkedList();
            {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT DISTINCT t1.cid FROM login2context AS t1 WHERE CONCAT('', t1.cid) NOT IN (SELECT t2.login_info FROM login2context AS t2 WHERE t2.cid = t1.cid)");
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        contextIds.add(rs.getInt(1));
                    }
                } catch (SQLException e) {
                    throw new RemoteException(e.getMessage(), e);
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }

            if (contextIds.isEmpty()) {
                return false;
            }

            // Iterate context identifiers
            int inserted = 0;
            for (int contextId : contextIds.toArray()) {
                try {
                    inserted += tryInsert(contextId, con);
                } catch (SQLException e) {
                    throw new RemoteException(e.getMessage(), e);
                }
            }

            boolean invalidate = inserted > 0;
            if (invalidate) {
                // Invalidate cache
                ContextStorage cs = ContextStorage.getInstance();
                for (int contextId : contextIds.toArray()) {
                    invalidateContext(contextId, cs);
                }
            }
            return invalidate;
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        } finally {
            databaseService.backWritable(con);
        }
    }

    @Override
    public boolean checkLogin2ContextMapping(int contextId) throws RemoteException {
        DatabaseService databaseService = getDatabaseService();
        Connection con = null;
        boolean invalidate = false;
        try {
            con = databaseService.getWritable();
            invalidate = tryInsert(contextId, con) == 1;
        } catch (SQLException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        } finally {
            databaseService.backWritable(con);
        }

        if (invalidate) {
            // Invalidate cache
            ContextStorage cs = ContextStorage.getInstance();
            invalidateContext(contextId, cs);
        }
        return invalidate;
    }

    ///////////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Try inserting a login mapping to the 'login2context' table.
     *
     * @param contextId The context identifier
     * @param connection The writeable connection to configdb
     * @return The amount of affected rows
     * @throws SQLException if an SQL error is occurred
     */
    private int tryInsert(int contextId, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO login2context (cid, login_info) VALUES (?, ?)")) {
            stmt.setInt(1, contextId);
            stmt.setString(2, Integer.toString(contextId));
            try {
                return stmt.executeUpdate();
            } catch (Exception e) {
                LOGGER.warn("Couldn't add context identifier to login2context mappings for context {}", Integer.valueOf(contextId), e);
            }
            return 0;
        }
    }

    /**
     * Invalidates the specified context
     *
     * @param contextId The context identifier
     * @param contextStorage The {@link ContextStorage}
     */
    private void invalidateContext(int contextId, ContextStorage contextStorage) {
        try {
            contextStorage.invalidateContext(contextId);
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(ContextRMIServiceImpl.class);
            logger.warn("Error invalidating cached infos of context {} in context storage", I(contextId), e);
        }
    }

    /**
     * Retrieves the {@link DatabaseService}
     *
     * @return The {@link DatabaseService}
     * @throws RemoteException if the {@link DatabaseService} is absent
     */
    private DatabaseService getDatabaseService() throws RemoteException {
        DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (null == databaseService) {
            OXException oxe = ServiceExceptionCode.absentService(DatabaseService.class);
            throw new RemoteException(oxe.getMessage(), oxe);
        }
        return databaseService;
    }
}

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

package com.openexchange.push.dovecot.locking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link DbDovecotPushClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DbDovecotPushClusterLock extends AbstractDovecotPushClusterLock {

    private static final String ATTR_DOVECOT_LOCK = "dovecotpush.lock";

    /**
     * Initializes a new {@link DbDovecotPushClusterLock}.
     */
    public DbDovecotPushClusterLock(ServiceLookup services) {
        super(services);
    }

    @Override
    public Type getType() {
        return Type.DATABASE;
    }

    @Override
    public boolean acquireLock(SessionInfo sessionInfo) throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        int contextId = sessionInfo.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            return acquireDbLock(sessionInfo, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private boolean acquireDbLock(SessionInfo sessionInfo, Connection con) throws OXException {
        int contextId = sessionInfo.getContextId();
        int userId = sessionInfo.getUserId();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("INSERT INTO user_attribute (cid, id, name, value, uuid)");
            long now = System.currentTimeMillis();
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_DOVECOT_LOCK);
            stmt.setString(pos++, generateValue(now, sessionInfo));
            stmt.setBytes(pos++, UUIDs.toByteArray(UUID.randomUUID()));
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_DOVECOT_LOCK);
            if (stmt.executeUpdate() > 0) {
                return true;
            }

            // Check if elapsed
            Databases.closeSQLStuff(stmt);
            stmt = con.prepareStatement("SELECT value FROM user_attribute WHERE cid=? AND id=? AND name=?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_DOVECOT_LOCK);
            rs = stmt.executeQuery();
            String previous = rs.getString(pos);
            Databases.closeSQLStuff(rs, stmt);

            // Check if valid
            if (validValue(previous, now)) {
                // Locked
                return false;
            }

            // Invalid entry - try to replace it mutually exclusive
            stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND id=? AND name=? AND value=?");
            pos = 1;
            stmt.setString(pos++, generateValue(now, sessionInfo));
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_DOVECOT_LOCK);
            stmt.setString(pos++, previous);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        int contextId = sessionInfo.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            refreshDbLock(sessionInfo, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private void refreshDbLock(SessionInfo sessionInfo, Connection con) throws OXException {
        int contextId = sessionInfo.getContextId();
        int userId = sessionInfo.getUserId();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_attribute SET value=? WHERE cid=? AND id=? AND name=?");
            int pos = 1;
            stmt.setString(pos++, generateValue(System.currentTimeMillis(), sessionInfo));
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_DOVECOT_LOCK);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        int contextId = sessionInfo.getContextId();
        Connection con = databaseService.getWritable(contextId);
        try {
            releaseDbLock(sessionInfo, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private void releaseDbLock(SessionInfo sessionInfo, Connection con) throws OXException {
        int contextId = sessionInfo.getContextId();
        int userId = sessionInfo.getUserId();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE cid=? AND id=? AND name=?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, ATTR_DOVECOT_LOCK);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
